package eu.europeana.metis.dereference.service.dao;

import static eu.europeana.metis.network.ExternalRequestUtil.retryableExternalRequestForNetworkExceptions;

import com.mongodb.DuplicateKeyException;
import com.mongodb.client.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.DiscriminatorFunction;
import dev.morphia.mapping.MapperOptions;
import dev.morphia.mapping.NamingStrategy;
import dev.morphia.query.experimental.filters.Filters;
import eu.europeana.metis.dereference.ProcessedEntity;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAO for processed entities
 */
public class ProcessedEntityDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessedEntityDao.class);

  private final Datastore datastore;

  /**
   * Constructor.
   *
   * @param mongo Client to the mongo database.
   * @param databaseName The name of the database.
   */
  public ProcessedEntityDao(MongoClient mongo, String databaseName) {
    final MapperOptions mapperOptions = MapperOptions.builder().discriminatorKey("className")
        .discriminator(DiscriminatorFunction.className())
        .collectionNaming(NamingStrategy.identity()).build();
    this.datastore = Morphia.createDatastore(mongo, databaseName, mapperOptions);
    this.datastore.getMapper().map(ProcessedEntity.class);
  }

  /**
   * Get an entity by resource ID.
   *
   * @param resourceId The resource ID (URI) to retrieve
   * @return The entity with the given resource ID.
   */
  public ProcessedEntity get(String resourceId) {
    return retryableExternalRequestForNetworkExceptions(
        () -> datastore.find(ProcessedEntity.class).filter(Filters.eq("resourceId", resourceId))
            .first());
  }

  /**
   * Save an entity.
   *
   * @param processedEntity The vocabulary or entity to save
   */
  public void save(ProcessedEntity processedEntity) {
    try {
      final ObjectId objectId = Optional.ofNullable(processedEntity.getId())
          .orElseGet(ObjectId::new);
      processedEntity.setId(objectId);
      retryableExternalRequestForNetworkExceptions(() -> datastore.save(processedEntity));
    } catch (DuplicateKeyException e) {
      LOGGER.info("Attempted to save duplicate record {}, race condition expected.",
          processedEntity.getResourceId());
      LOGGER.debug("Attempted to save duplicate record - exception details:", e);
    }
  }

  /**
   * Remove all entities.
   */
  public void purgeAll() {
    retryableExternalRequestForNetworkExceptions(
        () -> datastore.find(ProcessedEntity.class).delete());
  }
}
