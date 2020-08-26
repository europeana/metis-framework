package eu.europeana.metis.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.MongoClientSettings.Builder;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClients;
import eu.europeana.metis.mongo.MongoProperties.ReadPreferenceValue;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class can set up and provide a Mongo client given the Mongo properties. It applies the
 * following default values:
 * <ul>
 * <li>
 * The read preference for the connection is defaulted to {@link ReadPreference#secondaryPreferred()}.
 * </li>
 * <li>
 * The maximum idle time for connections (in the connection pool) is defaulted to
 * {@value #DEFAULT_MAX_CONNECTION_IDLE_MILLIS}.
 * </li>
 * <li>
 * Whether writes should be retried if they fail due to a network error is defaulted to
 * {@value #DEFAULT_RETRY_WRITES}.
 * </li>
 * </ul>
 * These defaults can be overridden or additional (default) settings can be set upon
 * construction. To facilitate this, this class offers access to the default settings by means of
 * the method {@link #getDefaultClientSettingsBuilder()}. The use of this method is voluntary.
 *
 * @param <E> The type of exception thrown when the properties are not valid.
 */
public class MongoClientProvider<E extends Exception> {

  private static final ReadPreference DEFAULT_READ_PREFERENCE = ReadPreference.secondaryPreferred();
  private static final int DEFAULT_MAX_CONNECTION_IDLE_MILLIS = 30_000;
  private static final boolean DEFAULT_RETRY_WRITES = false;

  private final MongoClientCreator<E> creator;
  private final String authenticationDatabase;

  /**
   * Constructor from a connection URI string (see the documentation of {@link MongoClientURI} for
   * the details). The connection URL can provide settings that will override the default settings.
   *
   * @param connectionUri The connection URI as a string
   * @param exceptionCreator How to report exceptions.
   * @throws E In case the connection URI is not valid.
   */
  public MongoClientProvider(String connectionUri, Function<String, E> exceptionCreator) throws E {
    final Builder clientSettingsBuilder = getDefaultClientSettingsBuilder();

    final ConnectionString connectionString;
    try {
      connectionString = new ConnectionString(connectionUri);
    } catch (RuntimeException e) {
      final E wrappingException = exceptionCreator.apply("Invalid connection URL.");
      wrappingException.initCause(e); // Use this to prevent false positive in SonarQube issue.
      throw wrappingException;
    }
    this.authenticationDatabase = connectionString.getDatabase();

    this.creator = () -> MongoClients
        .create(clientSettingsBuilder.applyConnectionString(connectionString).build());
  }

  /**
   * Constructor from a {@link MongoProperties} object. The caller can provide settings that will
   * override the default settings (i.e. the default settings will not be used).
   *
   * @param properties The properties of the Mongo connection. Note that if the passed properties
   * object is changed after calling this method, those changes will not be reflected when calling
   * @param clientSettingsBuilder The settings to be applied. The default settings will not be used. The
   * caller can incorporate the default settings by using an client settings builder obtained from {@link
   * #getDefaultClientSettingsBuilder()}. {@link #createMongoClient()}.
   */
  public MongoClientProvider(MongoProperties<E> properties, Builder clientSettingsBuilder) {
    final ReadPreference readPreference = Optional.ofNullable(properties.getReadPreferenceValue())
        .map(ReadPreferenceValue::getReadPreferenceSupplier).map(Supplier::get)
        .orElse(DEFAULT_READ_PREFERENCE);
    clientSettingsBuilder.readPreference(readPreference);

    final MongoCredential mongoCredential = properties.getMongoCredentials();
    this.authenticationDatabase = Optional.ofNullable(mongoCredential)
        .map(MongoCredential::getSource).orElse(null);
    clientSettingsBuilder.applyToSslSettings(builder -> builder.enabled(properties.mongoEnableSsl()));
    if (mongoCredential == null) {
      this.creator = () -> {
        final List<ServerAddress> mongoHosts = properties.getMongoHosts();
        final MongoClientSettings mongoClientSettings = clientSettingsBuilder
            .applyToClusterSettings(builder -> builder.hosts(mongoHosts)).build();
        return MongoClients.create(mongoClientSettings);
      };
    } else {
      this.creator = () -> {
        final List<ServerAddress> mongoHosts = properties.getMongoHosts();
        final MongoClientSettings mongoClientSettings = clientSettingsBuilder
            .applyToClusterSettings(builder -> builder.hosts(mongoHosts))
            .credential(mongoCredential).build();
        return MongoClients.create(mongoClientSettings);
      };
    }
  }

  /**
   * Constructor from a {@link MongoProperties} object, using the default settings.
   *
   * @param properties The properties of the Mongo connection. Note that if the passed properties
   * object is changed after calling this method, those changes will not be reflected when calling
   * {@link #createMongoClient()}.
   */
  public MongoClientProvider(MongoProperties<E> properties) {
    this(properties, getDefaultClientSettingsBuilder());
  }

  /**
   * This method provides access to the default settings.
   *
   * @return A new instance of {@link Builder} with the default settings.
   */
  public static Builder getDefaultClientSettingsBuilder() {
    return MongoClientSettings.builder()
        // TODO: 7/16/20 Remove default retry writes after upgrade to mongo server version 4.2
        .retryWrites(DEFAULT_RETRY_WRITES)
        .applyToConnectionPoolSettings(
            builder -> builder
                .maxConnectionIdleTime(DEFAULT_MAX_CONNECTION_IDLE_MILLIS, TimeUnit.MILLISECONDS))
        .readPreference(DEFAULT_READ_PREFERENCE);
  }

  /**
   * Convenience method for {@link #MongoClientProvider(String, Function)}. See that constructor for
   * the details.
   *
   * @param connectionUri The connection URI.
   * @return An instance.
   */
  public static MongoClientProvider<IllegalArgumentException> create(String connectionUri) {
    return new MongoClientProvider<>(connectionUri, IllegalArgumentException::new);
  }

  /**
   * Convenience method for {@link #MongoClientProvider(String, Function)}. See that constructor for
   * the details.
   *
   * @param connectionUri The connection URI.
   * @return A supplier for {@link MongoClient} instances based on this class.
   */
  public static Supplier<MongoClient> createAsSupplier(String connectionUri) {
    return create(connectionUri)::createMongoClient;
  }

  /**
   * Returns the authentication database for mongo connections that are provided. This is provided
   * for backwards compatibility. Can be null (signifying that the default is to be used or that no
   * authentication is specified).
   *
   * @return The authentication database.
   * @deprecated Provided only for backwards compatibility. May be removed in future releases.
   */
  @Deprecated(forRemoval = true)
  public final String getAuthenticationDatabase() {
    return authenticationDatabase;
  }

  /**
   * Creates a Mongo client. This method can be called multiple times and will create and return a
   * different client each time. The calling code is responsible for properly closing the created
   * client.
   *
   * @return A mongo client.
   * @throws E In case there is a problem with creating the client.
   */
  public final MongoClient createMongoClient() throws E {
    return creator.createMongoClient();
  }

  private interface MongoClientCreator<E extends Exception> {

    MongoClient createMongoClient() throws E;
  }
}
