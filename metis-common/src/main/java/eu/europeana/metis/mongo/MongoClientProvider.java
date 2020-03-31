package eu.europeana.metis.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoCredential;

/**
 * This class can set up and provide a Mongo client given the Mongo properties.
 *
 * @param <E> The type of exception thrown when the properties are not valid.
 */
public class MongoClientProvider<E extends Exception> {

  private final MongoProperties<E> properties;

  /**
   * Constructor.
   *
   * @param properties The properties of the Mongo connection.
   */
  public MongoClientProvider(MongoProperties<E> properties) {
    this.properties = properties;
  }

  /**
   * Creates a Mongo client from the properties. This method can be called multiple times and will
   * return a different client each time.
   *
   * @return A mongo client.
   * @throws E In case there is a problem with the supplied properties.
   */
  public MongoClient createMongoClient() throws E {
    final Builder optionsBuilder = new Builder().sslEnabled(this.properties.mongoEnableSsl());
    final MongoCredential mongoCredential = this.properties.getMongoCredentials();
    final MongoClient mongoClient;
    if (mongoCredential == null) {
      mongoClient = new MongoClient(properties.getMongoHosts(), optionsBuilder.build());
    } else {
      mongoClient = new MongoClient(properties.getMongoHosts(), mongoCredential,
              optionsBuilder.build());
    }
    return mongoClient;
  }
}
