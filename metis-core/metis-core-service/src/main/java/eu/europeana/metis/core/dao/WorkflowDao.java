package eu.europeana.metis.core.dao;

import static eu.europeana.metis.core.common.DaoFieldNames.DATASET_ID;
import static eu.europeana.metis.core.common.DaoFieldNames.ID;

import com.mongodb.WriteResult;
import dev.morphia.Key;
import dev.morphia.query.Query;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.utils.ExternalRequestUtil;
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
  public String create(Workflow workflow) {
    final Workflow workflowSaved = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(
            () -> morphiaDatastoreProvider.getDatastore().save(workflow));
    LOGGER.info("Workflow for datasetId '{}' created in Mongo", workflow.getDatasetId());
    return workflowSaved == null ? null : workflowSaved.getId().toString();
  }


  @Override
  public String update(Workflow workflow) {
    final Workflow workflowSaved = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(
            () -> morphiaDatastoreProvider.getDatastore().save(workflow));
    LOGGER.info("Workflow for datasetId '{}' updated in Mongo", workflow.getDatasetId());
    return workflowSaved == null ? null : workflowSaved.getId().toString();
  }

  @Override
  public Workflow getById(String id) {
    Query<Workflow> query = morphiaDatastoreProvider.getDatastore()
        .find(Workflow.class)
        .field(ID.getFieldName()).equal(new ObjectId(id));
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(query::first);
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
    Query<Workflow> query = morphiaDatastoreProvider.getDatastore().createQuery(Workflow.class);
    query.field(DATASET_ID.getFieldName()).equal(datasetId);
    WriteResult delete = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(
            () -> morphiaDatastoreProvider.getDatastore().delete(query));
    LOGGER.info("Workflow with datasetId {}, deleted from Mongo", datasetId);
    return (delete == null ? 0 : delete.getN()) == 1;
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
        .retryableExternalRequestConnectionReset(
            () -> morphiaDatastoreProvider.getDatastore().find(Workflow.class)
                .field(DATASET_ID.getFieldName()).equal(datasetId)
                .project(ID.getFieldName(), true).first());
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
        .retryableExternalRequestConnectionReset(
            () -> morphiaDatastoreProvider.getDatastore().find(Workflow.class)
                .field(DATASET_ID.getFieldName()).equal(datasetId).first());
  }
}

