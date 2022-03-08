package eu.europeana.metis.mongo.connection;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoClientSettings.Builder;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.ConnectionPoolSettings;
import eu.europeana.metis.mongo.connection.MongoProperties.ReadPreferenceValue;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class can set up and provide a Mongo client given the Mongo properties. It applies the following default values:
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
 * <li>
 * The application name (used among other things for server logging) is defaulted to
 * {@value #DEFAULT_APPLICATION_NAME}.
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
  private static final int DEFAULT_MAX_CONNECTIONS = 20;
  private static final boolean DEFAULT_RETRY_WRITES = false;
  private static final String DEFAULT_APPLICATION_NAME = "Europeana Application Suite";

  private final MongoClientCreator<E> creator;
  private final String authenticationDatabase;

  /**
   * Constructor from a connection URI string (see the documentation of {@link MongoClientURI} for the details). The connection
   * URL can provide settings that will override the default settings.
   *
   * @param connectionUri The connection URI as a string
   * @param exceptionCreator How to report exceptions.
   * @throws E In case the connection URI is not valid.
   */
  public MongoClientProvider(String connectionUri, Function<String, E> exceptionCreator) throws E {

    final ConnectionString connectionString;
    try {
      connectionString = new ConnectionString(connectionUri);
    } catch (RuntimeException e) {
      final E wrappingException = exceptionCreator.apply("Invalid connection URL.");
      wrappingException.initCause(e); // Use this to prevent false positive in SonarQube issue.
      throw wrappingException;
    }
    this.authenticationDatabase = connectionString.getDatabase();
    final Builder clientSettingsBuilder = getDefaultClientSettingsBuilder();
    clientSettingsBuilder.applyConnectionString(connectionString);
    final MongoClientSettings mongoClientSettings = clientSettingsBuilder.build();

    this.creator = () -> MongoClients.create(mongoClientSettings);
  }

  /**
   * Constructor from a {@link MongoProperties} object. The caller needs to provide settings that will be used instead of the
   * default settings.
   *
   * @param properties The properties of the Mongo connection. Note that if the passed properties object is changed after calling
   * this method, those changes will not be reflected when creating mongo clients.
   * @param clientSettingsBuilder The settings to be applied. The default settings will not be used. The caller can however choose
   * to incorporate the default settings as needed by using a client settings builder obtained from {@link
   * #getDefaultClientSettingsBuilder()} as input.
   * @throws E In case the properties are wrong
   */
  public MongoClientProvider(MongoProperties<E> properties, Builder clientSettingsBuilder)
      throws E {
    final ReadPreference readPreference = Optional.ofNullable(properties.getReadPreferenceValue())
                                                  .map(ReadPreferenceValue::getReadPreferenceSupplier).map(Supplier::get)
                                                  .orElse(DEFAULT_READ_PREFERENCE);
    clientSettingsBuilder.readPreference(readPreference);

    final List<ServerAddress> mongoHosts = properties.getMongoHosts();
    final MongoCredential mongoCredential = properties.getMongoCredentials();
    this.authenticationDatabase = Optional.ofNullable(mongoCredential)
                                          .map(MongoCredential::getSource).orElse(null);
    clientSettingsBuilder
        .applyToSslSettings(builder -> builder.enabled(properties.mongoEnableSsl()));
    clientSettingsBuilder.applyToClusterSettings(builder -> builder.hosts(mongoHosts));
    if (mongoCredential != null) {
      clientSettingsBuilder.credential(mongoCredential);
    }
    Optional.ofNullable(properties.getApplicationName()).filter(name -> !name.isBlank())
            .ifPresent(clientSettingsBuilder::applicationName);

    clientSettingsBuilder.applyToConnectionPoolSettings(
        builder -> builder.applySettings(createConnectionPoolSettings(properties.getMaxConnectionPoolSize())));
    final MongoClientSettings mongoClientSettings = clientSettingsBuilder.build();

    this.creator = () -> MongoClients.create(mongoClientSettings);
  }

  /**
   * Constructor from a {@link MongoProperties} object, using the default settings.
   *
   * @param properties The properties of the Mongo connection. Note that if the passed properties object is changed after calling
   * this method, those changes will not be reflected when calling {@link #createMongoClient()}.
   * @throws E In case the properties are wrong
   */
  public MongoClientProvider(MongoProperties<E> properties) throws E {
    this(properties, getDefaultClientSettingsBuilder());
  }

  /**
   * This method provides access to the default settings.
   *
   * @return A new instance of {@link Builder} with the default settings.
   */
  public static MongoClientSettings.Builder getDefaultClientSettingsBuilder() {
    return MongoClientSettings.builder()
                              // TODO: 7/16/20 Remove default retry writes after upgrade to mongo server version 4.2
                              .retryWrites(DEFAULT_RETRY_WRITES)
                              .applyToConnectionPoolSettings(builder -> builder.applySettings(getDefaultConnectionPoolSettings()))
                              .readPreference(DEFAULT_READ_PREFERENCE)
                              .applicationName(DEFAULT_APPLICATION_NAME);
  }

  /**
   * Convenience method for {@link #MongoClientProvider(String, Function)}. See that constructor for the details.
   *
   * @param connectionUri The connection URI.
   * @return An instance.
   */
  public static MongoClientProvider<IllegalArgumentException> create(String connectionUri) {
    return new MongoClientProvider<>(connectionUri, IllegalArgumentException::new);
  }

  /**
   * Convenience method for {@link #MongoClientProvider(String, Function)}. See that constructor for the details.
   *
   * @param connectionUri The connection URI.
   * @return A supplier for {@link MongoClient} instances based on this class.
   */
  public static Supplier<MongoClient> createAsSupplier(String connectionUri) {
    return create(connectionUri)::createMongoClient;
  }

  /**
   * Get the default connection pool settings
   *
   * @return the default connection pool settings
   */
  private static ConnectionPoolSettings getDefaultConnectionPoolSettings() {
    return createConnectionPoolSettings(null);
  }

  /**
   * Returns the authentication database for mongo connections that are provided. Can be null (signifying that the default is to
   * be used or that no authentication is specified).
   *
   * @return The authentication database.
   */
  public final String getAuthenticationDatabase() {
    return authenticationDatabase;
  }

  /**
   * Creates a Mongo client. This method can be called multiple times and will create and return a different client each time. The
   * calling code is responsible for properly closing the created client.
   *
   * @return A mongo client.
   * @throws E In case there is a problem with creating the client.
   */
  public final MongoClient createMongoClient() throws E {
    return creator.createMongoClient();
  }

  /**
   * Create a connection pool settings object. Settings that are null will be set to default settings.
   *
   * @param maxPoolSize the maximum connection pool size
   * @return the connection pool settings
   */
  static ConnectionPoolSettings createConnectionPoolSettings(Integer maxPoolSize) {
    final ConnectionPoolSettings.Builder builder = ConnectionPoolSettings.builder();
    builder.maxConnectionIdleTime(DEFAULT_MAX_CONNECTION_IDLE_MILLIS, TimeUnit.MILLISECONDS);
    if (maxPoolSize != null && maxPoolSize > 0) {
      builder.maxSize(maxPoolSize);
    } else {
      builder.maxSize(DEFAULT_MAX_CONNECTIONS);
    }
    return builder.build();
  }

  private interface MongoClientCreator<E extends Exception> {

    MongoClient createMongoClient() throws E;
  }
}
