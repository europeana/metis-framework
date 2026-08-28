package eu.europeana.metis.dereference.service.dao;

import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.filters.Filters.ne;
import static dev.morphia.query.updates.UpdateOperators.set;
import static dev.morphia.query.updates.UpdateOperators.setOnInsert;
import static eu.europeana.metis.dereference.ProcessedEntity.MONGO_ID_FIELD;
import static eu.europeana.metis.dereference.ProcessedEntity.RESOURCE_ID_FIELD;
import static eu.europeana.metis.dereference.ProcessedEntity.RESULT_STATUS_FIELD;
import static eu.europeana.metis.dereference.ProcessedEntity.VOCABULARY_ID_FIELD;
import static eu.europeana.metis.dereference.ProcessedEntity.XML_FIELD;
import static eu.europeana.metis.network.ExternalRequestUtil.retryableExternalRequestForNetworkExceptions;

import com.mongodb.client.MongoClient;
import com.mongodb.client.result.UpdateResult;
import dev.morphia.Datastore;
import dev.morphia.DeleteOptions;
import dev.morphia.Morphia;
import dev.morphia.UpdateOptions;
import dev.morphia.mapping.DiscriminatorFunction;
import dev.morphia.mapping.MapperOptions;
import dev.morphia.mapping.NamingStrategy;
import dev.morphia.query.Query;
import eu.europeana.enrichment.api.external.DereferenceResultStatus;
import eu.europeana.metis.dereference.ProcessedEntity;
import java.util.Map;
import java.util.Optional;
import org.bson.types.ObjectId;

/**
 * Data Access Object (DAO) for managing {@link ProcessedEntity} objects in the database. This class provides methods for
 * retrieving, creating, updating, and deleting entities in the associated datastore.
 */
public class ProcessedEntityDao {

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
        () -> datastore.find(ProcessedEntity.class).filter(eq(RESOURCE_ID_FIELD, resourceId))
                       .first());
  }

  /**
   * Get an entity by vocabulary ID.
   *
   * @param vocabularyId The vocabularyId to retrieve
   * @return The entity with the given vocabulary ID.
   */
  public ProcessedEntity getByVocabularyId(String vocabularyId) {
    return retryableExternalRequestForNetworkExceptions(
        () -> datastore.find(ProcessedEntity.class).filter(eq(VOCABULARY_ID_FIELD, vocabularyId))
                       .first());
  }


  /**
   * Saves a processed entity using cache-aware overwrite rules.
   * <p>
   * If no document exists for the resource, a new document is inserted.
   * <p>
   * If the supplied entity has a successful dereference status, it will overwrite any existing document for the same resource.
   * <p>
   * If the supplied entity has a non-successful dereference status, it will overwrite an existing document only when the existing
   * document is also non-successful. Existing successful results are preserved.
   * <p>
   * This method is implemented using an atomic MongoDB upsert operation to prevent race conditions when multiple instances
   * process the same resource concurrently. If this method is called without checking the cache first, the same heuristics will
   * apply for all results.
   *
   * @param processedEntity the processed entity to save
   */
  public void saveConditionally(ProcessedEntity processedEntity) {
    retryableExternalRequestForNetworkExceptions(() -> upsertResult(processedEntity));
  }

  private UpdateResult upsertResult(ProcessedEntity processedEntity) {
    final Query<ProcessedEntity> query =
        datastore.find(ProcessedEntity.class).filter(eq(RESOURCE_ID_FIELD, processedEntity.getResourceId()));

    if (processedEntity.getResultStatus() != DereferenceResultStatus.SUCCESS) {
      query.filter(ne(RESULT_STATUS_FIELD, DereferenceResultStatus.SUCCESS));
    }

    return query.update(
        new UpdateOptions().upsert(true),
        set(XML_FIELD, processedEntity.getXml()),
        set(VOCABULARY_ID_FIELD, processedEntity.getVocabularyId()),
        set(RESULT_STATUS_FIELD, processedEntity.getResultStatus()),
        setOnInsert(Map.of(
            MONGO_ID_FIELD, Optional.ofNullable(processedEntity.getId()).orElseGet(ObjectId::new),
            RESOURCE_ID_FIELD, processedEntity.getResourceId()
        ))
    );
  }

  /**
   * Delete an entity with no description in XML resources. Empty or Null
   **/
  public void purgeByNullOrEmptyXml() {
    retryableExternalRequestForNetworkExceptions(() ->
        datastore.find(ProcessedEntity.class)
                 .filter(eq(XML_FIELD, null))
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
                 .filter(eq(RESOURCE_ID_FIELD, resourceId))
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
                 .filter(eq(VOCABULARY_ID_FIELD, vocabularyId))
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
        () -> datastore.find(ProcessedEntity.class).stream().count());
  }
}
