package eu.europeana.metis.dereference.service.dao;

import static eu.europeana.metis.network.ExternalRequestUtil.retryableExternalRequestForNetworkExceptions;

import com.mongodb.DuplicateKeyException;
import com.mongodb.client.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.DeleteOptions;
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
  public ProcessedEntity getByResourceId(String resourceId) {
    return retryableExternalRequestForNetworkExceptions(
        () -> datastore.find(ProcessedEntity.class).filter(Filters.eq("resourceId", resourceId))
                       .first());
  }

  /**
   * Get an entity by vocabulary ID.
   *
   * @param vocabularyId The vocabuylaryDi to retrieve
   * @return The entity with the given vocabulary ID.
   */
  public ProcessedEntity getByVocabularyId(String vocabularyId) {
    return retryableExternalRequestForNetworkExceptions(
        () -> datastore.find(ProcessedEntity.class).filter(Filters.eq("vocabularyId", vocabularyId))
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
   * Delete an entity with no description in XML resources. Empty or Null
   **/
  public void purgeByNullOrEmptyXml() {
    retryableExternalRequestForNetworkExceptions(() ->
        datastore.find(ProcessedEntity.class)
                 .filter(Filters.eq("xml", null))
                 .delete(new DeleteOptions().multi(true)));
  }

  /**
   * Delete an entity by resource ID.
   *
   * @param resourceId The resource ID (URI) to delete
   **/
  public void purgeByResourceId(String resourceId) {
    retryableExternalRequestForNetworkExceptions(() ->
        datastore.find(ProcessedEntity.class)
                 .filter(Filters.eq("resourceId", resourceId))
                 .delete(new DeleteOptions()));
  }

  /**
   * Delete the entity based on its vocabulary ID.
   *
   * @param vocabularyId The ID of the vocabulary to delete.
   **/
  public void purgeByVocabularyId(String vocabularyId) {
    retryableExternalRequestForNetworkExceptions(() ->
        datastore.find(ProcessedEntity.class)
                 .filter(Filters.eq("vocabularyId", vocabularyId))
                 .delete(new DeleteOptions().multi(true)));
  }

  /**
   * Remove all entities.
   */
  public void purgeAll() {
    retryableExternalRequestForNetworkExceptions(
        () -> datastore.find(ProcessedEntity.class).delete(new DeleteOptions().multi(true)));
  }

  /**
   * Size of Processed entities
   *
   * @return amount of documents in db
   */
  protected long size() {
    return retryableExternalRequestForNetworkExceptions(
        () ->  datastore.find(ProcessedEntity.class).stream().count());
  }
}
