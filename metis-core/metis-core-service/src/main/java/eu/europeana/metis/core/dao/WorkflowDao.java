package eu.europeana.metis.core.dao;

import com.mongodb.WriteResult;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.rest.RequestLimits;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.utils.ExternalRequestUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-29
 */
@Repository
public class WorkflowDao implements MetisDao<Workflow, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowDao.class);
  private static final String DATASET_ID = "datasetId";
  private int workflowsPerRequest = RequestLimits.WORKFLOWS_PER_REQUEST.getLimit();
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
    final Key<Workflow> workflowKey = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(() -> morphiaDatastoreProvider.getDatastore().save(workflow));
    LOGGER.info("Workflow for datasetId '{}' created in Mongo", workflow.getDatasetId());
    return workflowKey != null ? workflowKey.getId().toString() : null;
  }


  @Override
  public String update(Workflow workflow) {
    final Key<Workflow> workflowKey = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(() -> morphiaDatastoreProvider.getDatastore().save(workflow));
    LOGGER.info("Workflow for datasetId '{}' updated in Mongo", workflow.getDatasetId());
    return workflowKey != null ? workflowKey.getId().toString() : null;
  }

  @Override
  public Workflow getById(String id) {
    Query<Workflow> query = morphiaDatastoreProvider.getDatastore()
        .find(Workflow.class)
        .field("_id").equal(new ObjectId(id));
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(query::get);
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
    query.field(DATASET_ID).equal(datasetId);
    WriteResult delete = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(() -> morphiaDatastoreProvider.getDatastore().delete(query));
    LOGGER.info("Workflow with datasetId {}, deleted from Mongo", datasetId);
    return (delete != null ? delete.getN() : 0) == 1;
  }

  /**
   * Check existence of a workflow using a {@link Workflow} class.
   * <p>It will check based on the {@link Workflow#getDatasetId()} ()}</p>
   *
   * @param workflow the {@link Workflow}
   * @return null or the {@link ObjectId} of the object
   */
  public String exists(Workflow workflow) {
    Workflow storedWorkflow = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(() -> morphiaDatastoreProvider.getDatastore().find(Workflow.class)
            .field(DATASET_ID).equal(workflow.getDatasetId())
            .project("_id", true).get());
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
        .retryableExternalRequestConnectionReset(() -> morphiaDatastoreProvider.getDatastore().find(Workflow.class)
            .field(DATASET_ID).equal(datasetId).get());
  }

  public int getWorkflowsPerRequest() {
    synchronized (this) {
      return workflowsPerRequest;
    }
  }

  public void setWorkflowsPerRequest(int workflowsPerRequest) {
    synchronized (this) {
      this.workflowsPerRequest = workflowsPerRequest;
    }
  }
}

