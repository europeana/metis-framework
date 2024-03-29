package eu.europeana.metis.core.dao;

import static eu.europeana.metis.core.common.DaoFieldNames.DATASET_ID;
import static eu.europeana.metis.core.common.DaoFieldNames.ID;

import com.mongodb.client.result.DeleteResult;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.network.ExternalRequestUtil;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * DAO object for workflows.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-29
 */
@Repository
public class WorkflowDao implements MetisDao<Workflow, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowDao.class);
  private final MorphiaDatastoreProvider morphiaDatastoreProvider;

  /**
   * Constructs the DAO
   *
   * @param morphiaDatastoreProvider {@link MorphiaDatastoreProvider} used to access Mongo
   */
  public WorkflowDao(MorphiaDatastoreProvider morphiaDatastoreProvider) {
    this.morphiaDatastoreProvider = morphiaDatastoreProvider;
  }

  @Override
  public Workflow create(Workflow workflow) {
    final ObjectId objectId = Optional.ofNullable(workflow.getId()).orElseGet(ObjectId::new);
    workflow.setId(objectId);
    final Workflow workflowSaved = ExternalRequestUtil.retryableExternalRequestForNetworkExceptions(
        () -> morphiaDatastoreProvider.getDatastore().save(workflow));
    LOGGER.info("Workflow for datasetId '{}' created in Mongo", workflow.getDatasetId());
    return workflowSaved;
  }


  @Override
  public String update(Workflow workflow) {
    final Workflow workflowSaved = ExternalRequestUtil
        .retryableExternalRequestForNetworkExceptions(
            () -> morphiaDatastoreProvider.getDatastore().save(workflow));
    LOGGER.info("Workflow for datasetId '{}' updated in Mongo", workflow.getDatasetId());
    return workflowSaved == null ? null : workflowSaved.getId().toString();
  }

  @Override
  public Workflow getById(String id) {
    Query<Workflow> query = morphiaDatastoreProvider.getDatastore()
        .find(Workflow.class).filter(Filters.eq(ID.getFieldName(), new ObjectId(id)));
    return ExternalRequestUtil.retryableExternalRequestForNetworkExceptions(query::first);
  }

  @Override
  public boolean delete(Workflow workflow) {
    return deleteWorkflow(workflow.getDatasetId());
  }

  /**
   * Delete a workflow using datasetId.
   *
   * @param datasetId the dataset identifier
   * @return true if the workflow was found and deleted
   */
  public boolean deleteWorkflow(String datasetId) {
    Query<Workflow> query = morphiaDatastoreProvider.getDatastore().find(Workflow.class);
    query.filter(Filters.eq(DATASET_ID.getFieldName(), datasetId));
    DeleteResult deleteResult = ExternalRequestUtil
        .retryableExternalRequestForNetworkExceptions(query::delete);
    LOGGER.info("Workflow with datasetId {}, deleted from Mongo", datasetId);
    return (deleteResult == null ? 0 : deleteResult.getDeletedCount()) == 1;
  }

  /**
   * Check existence of a workflow for the given Dataset ID.
   *
   * @param datasetId The dataset ID.
   * @return whether a workflow exists for this dataset.
   */
  public boolean workflowExistsForDataset(String datasetId) {
    return null != getWorkflowId(datasetId);
  }

  private String getWorkflowId(String datasetId) {
    Workflow storedWorkflow = ExternalRequestUtil
        .retryableExternalRequestForNetworkExceptions(
            () -> morphiaDatastoreProvider.getDatastore().find(Workflow.class)
                .filter(Filters.eq(DATASET_ID.getFieldName(), datasetId))
                .first(new FindOptions().projection().include(ID.getFieldName())));
    return storedWorkflow == null ? null : storedWorkflow.getId().toString();
  }

  /**
   * Get a workflow using a datasetId.
   *
   * @param datasetId the dataset id
   * @return {@link Workflow}
   */
  public Workflow getWorkflow(String datasetId) {
    return ExternalRequestUtil
        .retryableExternalRequestForNetworkExceptions(
            () -> morphiaDatastoreProvider.getDatastore().find(Workflow.class)
                .filter(Filters.eq(DATASET_ID.getFieldName(), datasetId)).first());
  }
}

