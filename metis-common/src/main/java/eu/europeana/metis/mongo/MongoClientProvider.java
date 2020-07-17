package eu.europeana.metis.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import eu.europeana.metis.mongo.MongoProperties.ReadPreferenceValue;
import java.util.Optional;
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
 * These defaults can be overridden or additional (default) settings can be set by extending this
 * class and overriding {@link #getDefaultOptionsBuilder()}.
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
    final Builder optionsBuilder = getDefaultOptionsBuilder();
    final MongoClientURI uri;
    try {
      uri = new MongoClientURI(connectionUri, optionsBuilder);
    } catch (RuntimeException e) {
      final E wrappingException = exceptionCreator.apply("Invalid connection URL.");
      wrappingException.initCause(e); // Use this to prevent false positive in SonarQube issue.
      throw wrappingException;
    }
    this.authenticationDatabase = uri.getDatabase();
    this.creator = () -> new MongoClient(uri);
  }

  /**
   * Constructor from a {@link MongoProperties} object. The properties object can provide settings
   * that will override the default settings.
   *
   * @param properties The properties of the Mongo connection. Note that if the passed properties
   * object is changed after calling this method, those changes will not be reflected when calling
   * {@link #createMongoClient()}.
   */
  public MongoClientProvider(MongoProperties<E> properties) {
    final ReadPreference readPreference = Optional.ofNullable(properties.getReadPreferenceValue())
        .map(ReadPreferenceValue::getReadPreferenceSupplier).map(Supplier::get)
        .orElse(DEFAULT_READ_PREFERENCE);
    final Builder optionsBuilder = getDefaultOptionsBuilder()
        .sslEnabled(properties.mongoEnableSsl())
        .readPreference(readPreference);
    final MongoCredential mongoCredential = properties.getMongoCredentials();
    this.authenticationDatabase = Optional.ofNullable(mongoCredential)
        .map(MongoCredential::getSource).orElse(null);
    if (mongoCredential == null) {
      this.creator = () -> new MongoClient(properties.getMongoHosts(), optionsBuilder.build());
    } else {
      this.creator = () -> new MongoClient(properties.getMongoHosts(), mongoCredential,
          optionsBuilder.build());
    }
  }

  protected Builder getDefaultOptionsBuilder() {
    return new Builder()
        // TODO: 7/16/20 Remove default retry writes after upgrade to mongo server version 4.2
        .retryWrites(DEFAULT_RETRY_WRITES)
        .maxConnectionIdleTime(DEFAULT_MAX_CONNECTION_IDLE_MILLIS)
        .readPreference(DEFAULT_READ_PREFERENCE);
  }

  /**
   * Convenience method for {@link #MongoClientProvider(String, Function)}. See that constructor
   * for the details.
   *
   * @param connectionUri The connection URI.
   * @return An instance.
   */
  public static MongoClientProvider<IllegalArgumentException> create(String connectionUri) {
    return new MongoClientProvider<>(connectionUri, IllegalArgumentException::new);
  }

  /**
   * Convenience method for {@link #MongoClientProvider(String, Function)}. See that constructor
   * for the details.
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
