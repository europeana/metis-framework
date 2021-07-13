package eu.europeana.metis.repository.dao;

import com.mongodb.client.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.DiscriminatorFunction;
import dev.morphia.mapping.MapperOptions;
import dev.morphia.mapping.NamingStrategy;

/**
 * Database Access Object for {@link Record} instances.
 */
public class RecordDao {

  private final Datastore datastore;

  /**
   * Constructor.
   *
   * @param mongo Client to the mongo database.
   * @param databaseName The name of the database.
   */
  public RecordDao(MongoClient mongo, String databaseName) {
    final MapperOptions mapperOptions = MapperOptions.builder().discriminatorKey("className")
            .discriminator(DiscriminatorFunction.className())
            .collectionNaming(NamingStrategy.identity()).build();
    this.datastore = Morphia.createDatastore(mongo, databaseName, mapperOptions);
    this.datastore.getMapper().map(Record.class);
  }
}
