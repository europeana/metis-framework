package eu.europeana.metis.core.dao;

import com.mongodb.WriteResult;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.FindOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-26
 */
@Repository
public class WorkflowExecutionDao implements MetisDao<WorkflowExecution, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowExecutionDao.class);
  private static final String WORKFLOW_STATUS = "workflowStatus";
  private static final String DATASET_ID = "datasetId";
  private final MorphiaDatastoreProvider morphiaDatastoreProvider;
  private int workflowExecutionsPerRequest = 5;

  @Autowired
  public WorkflowExecutionDao(MorphiaDatastoreProvider morphiaDatastoreProvider) {
    this.morphiaDatastoreProvider = morphiaDatastoreProvider;
  }

  @Override
  public String create(WorkflowExecution workflowExecution) {
    Key<WorkflowExecution> workflowExecutionKey = morphiaDatastoreProvider.getDatastore().save(
        workflowExecution);
    LOGGER.debug(
        "WorkflowExecution for datasetName '{}' with workflowOwner '{}' and workflowName '{}' created in Mongo",
        workflowExecution.getDatasetId(), workflowExecution.getWorkflowOwner(),
        workflowExecution.getWorkflowName());
    return workflowExecutionKey.getId().toString();
  }

  @Override
  public String update(WorkflowExecution workflowExecution) {
    Key<WorkflowExecution> workflowExecutionKey = morphiaDatastoreProvider.getDatastore().save(
        workflowExecution);
    LOGGER.debug(
        "WorkflowExecution for datasetName '{}' with workflowOwner '{}' and workflowName '{}' updated in Mongo",
        workflowExecution.getDatasetId(), workflowExecution.getWorkflowOwner(),
        workflowExecution.getWorkflowName());
    return workflowExecutionKey.getId().toString();
  }

  public void updateWorkflowPlugins(WorkflowExecution workflowExecution) {
    UpdateOperations<WorkflowExecution> workflowExecutionUpdateOperations = morphiaDatastoreProvider
        .getDatastore()
        .createUpdateOperations(WorkflowExecution.class);
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .find(WorkflowExecution.class)
        .filter("_id", workflowExecution.getId());
    workflowExecutionUpdateOperations
        .set("metisPlugins", workflowExecution.getMetisPlugins());
    UpdateResults updateResults = morphiaDatastoreProvider.getDatastore()
        .update(query, workflowExecutionUpdateOperations);
    LOGGER.debug(
        "WorkflowExecution metisPlugins for datasetName '{}' with workflowOwner '{}' and workflowName '{}' updated in Mongo. (UpdateResults: {})",
        workflowExecution.getDatasetId(), workflowExecution.getWorkflowOwner(),
        workflowExecution.getWorkflowName(), updateResults.getUpdatedCount());
  }

  public void updateMonitorInformation(WorkflowExecution workflowExecution) {
    UpdateOperations<WorkflowExecution> workflowExecutionUpdateOperations = morphiaDatastoreProvider
        .getDatastore()
        .createUpdateOperations(WorkflowExecution.class);
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .find(WorkflowExecution.class)
        .filter("_id", workflowExecution.getId());
    workflowExecutionUpdateOperations
        .set(WORKFLOW_STATUS, workflowExecution.getWorkflowStatus());
    if (workflowExecution.getStartedDate() != null) {
      workflowExecutionUpdateOperations
          .set("startedDate", workflowExecution.getStartedDate());
    }
    if (workflowExecution.getUpdatedDate() != null) {
      workflowExecutionUpdateOperations
          .set("updatedDate", workflowExecution.getUpdatedDate());
    }
    workflowExecutionUpdateOperations
        .set("metisPlugins", workflowExecution.getMetisPlugins());
    UpdateResults updateResults = morphiaDatastoreProvider.getDatastore()
        .update(query, workflowExecutionUpdateOperations);
    LOGGER.debug(
        "WorkflowExecution monitor information for datasetName '{}' with workflowOwner '{}' and workflowName '{}' updated in Mongo. (UpdateResults: {})",
        workflowExecution.getDatasetId(), workflowExecution.getWorkflowOwner(),
        workflowExecution.getWorkflowName(), updateResults.getUpdatedCount());
  }

  public void setCancellingState(WorkflowExecution workflowExecution) {
    UpdateOperations<WorkflowExecution> workflowExecutionUpdateOperations = morphiaDatastoreProvider
        .getDatastore()
        .createUpdateOperations(WorkflowExecution.class);
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .find(WorkflowExecution.class)
        .filter("_id", workflowExecution.getId());
    workflowExecutionUpdateOperations.set("cancelling", Boolean.TRUE);
    UpdateResults updateResults = morphiaDatastoreProvider.getDatastore()
        .update(query, workflowExecutionUpdateOperations);
    LOGGER.debug(
        "WorkflowExecution cancelling for datasetName '{}' with workflowOwner '{}' and workflowName '{}' set to true in Mongo. (UpdateResults: {})",
        workflowExecution.getDatasetId(), workflowExecution.getWorkflowOwner(),
        workflowExecution.getWorkflowName(), updateResults.getUpdatedCount());
  }

  @Override
  public WorkflowExecution getById(String id) {
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .find(WorkflowExecution.class)
        .field("_id").equal(new ObjectId(id));
    return query.get();
  }

  @Override
  public boolean delete(WorkflowExecution workflowExecution) {
    return false;
  }

  public WorkflowExecution getRunningOrInQueueExecution(long datasetId) {
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .find(WorkflowExecution.class)
        .field(DATASET_ID).equal(
            datasetId);
    query.or(query.criteria(WORKFLOW_STATUS).equal(WorkflowStatus.INQUEUE),
        query.criteria(WORKFLOW_STATUS).equal(WorkflowStatus.RUNNING));
    return query.get();
  }

  public boolean exists(WorkflowExecution workflowExecution) {
    return morphiaDatastoreProvider.getDatastore().find(WorkflowExecution.class)
        .field(DATASET_ID).equal(
            workflowExecution.getDatasetId()).field("workflowOwner").equal(
            workflowExecution.getWorkflowOwner()).field("workflowName")
        .equal(workflowExecution.getWorkflowName())
        .project("_id", true).get() != null;
  }

  public String existsAndNotCompleted(long datasetId) {
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .find(WorkflowExecution.class).field(DATASET_ID).equal(datasetId);
    query.or(query.criteria(WORKFLOW_STATUS).equal(WorkflowStatus.INQUEUE),
        query.criteria(WORKFLOW_STATUS).equal(WorkflowStatus.RUNNING));
    query.project("_id", true);
    query.project(WORKFLOW_STATUS, true);

    WorkflowExecution storedWorkflowExecution = query.get();
    if (storedWorkflowExecution != null) {
      return storedWorkflowExecution.getId().toString();
    }
    return null;
  }

  public WorkflowExecution getRunningWorkflowExecution(long datasetId) {
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .createQuery(WorkflowExecution.class);
    query.field(DATASET_ID).equal(
        datasetId)
        .field(WORKFLOW_STATUS).equal(WorkflowStatus.RUNNING);
    return query.get();
  }

  public List<WorkflowExecution> getAllWorkflowExecutions(long datasetId,
      String workflowOwner,
      String workflowName,
      WorkflowStatus workflowStatus, String nextPage) {
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .createQuery(WorkflowExecution.class);
    query.field(DATASET_ID).equal(datasetId)
        .field("workflowOwner").equal(workflowOwner)
        .field("workflowName").equal(workflowName);
    if (workflowStatus != null && workflowStatus != WorkflowStatus.NULL) {
      query.field(WORKFLOW_STATUS).equal(workflowStatus);
    }
    query.order("_id");
    if (StringUtils.isNotEmpty(nextPage)) {
      query.field("_id").greaterThan(new ObjectId(nextPage));
    }
    return query.asList(new FindOptions().limit(workflowExecutionsPerRequest));
  }

  public List<WorkflowExecution> getAllWorkflowExecutions(WorkflowStatus workflowStatus,
      String nextPage) {
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .createQuery(WorkflowExecution.class);
    if (workflowStatus != null && workflowStatus != WorkflowStatus.NULL) {
      query.field(WORKFLOW_STATUS).equal(workflowStatus);
    }
    query.order("_id");
    if (StringUtils.isNotEmpty(nextPage)) {
      query.field("_id").greaterThan(new ObjectId(nextPage));
    }
    return query.asList(new FindOptions().limit(workflowExecutionsPerRequest));
  }

  public int getWorkflowExecutionsPerRequest() {
    return workflowExecutionsPerRequest;
  }

  public void setWorkflowExecutionsPerRequest(int workflowExecutionsPerRequest) {
    this.workflowExecutionsPerRequest = workflowExecutionsPerRequest;
  }

  public boolean isCancelled(ObjectId id) {
    return
        morphiaDatastoreProvider.getDatastore().find(WorkflowExecution.class).field("_id").equal(id)
            .project(WORKFLOW_STATUS, true).get().getWorkflowStatus() == WorkflowStatus.CANCELLED;
  }

  public boolean isCancelling(ObjectId id) {
    return morphiaDatastoreProvider.getDatastore().find(WorkflowExecution.class).field("_id")
        .equal(id)
        .project("cancelling", true).get().isCancelling();
  }

  public boolean isExecutionActive(WorkflowExecution workflowExecutionToCheck,
      int monitorCheckInSecs) {
    try {
      Date updatedDateBefore = workflowExecutionToCheck.getUpdatedDate();
      Thread.sleep(2 * monitorCheckInSecs * 1000L);
      WorkflowExecution workflowExecution = this
          .getById(workflowExecutionToCheck.getId().toString());
      return hasUpdatedDateChanged(updatedDateBefore, workflowExecution.getUpdatedDate())
          || workflowExecution.getFinishedDate() != null;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();  // set interrupt flag
      LOGGER.warn("Thread was interrupted", e);
      return true;
    }
  }

  private boolean hasUpdatedDateChanged(Date updatedDateBefore, Date updatedDateAfter) {
    return (updatedDateBefore != null && updatedDateBefore.compareTo(updatedDateAfter) < 0) ||
        (updatedDateBefore == null && updatedDateAfter != null);
  }

  public void removeActiveExecutionsFromList(List<WorkflowExecution> workflowExecutions,
      int monitorCheckInSecs) {
    try {
      Thread.sleep(2 * monitorCheckInSecs * 1000L);
      for (Iterator<WorkflowExecution> iterator = workflowExecutions.iterator();
          iterator.hasNext(); ) {
        WorkflowExecution workflowExecutionToCheck = iterator.next();
        WorkflowExecution workflowExecution = this
            .getById(workflowExecutionToCheck.getId().toString());
        if (workflowExecutionToCheck.getUpdatedDate() != null
            && workflowExecutionToCheck.getUpdatedDate()
            .compareTo(workflowExecution.getUpdatedDate()) < 0) {
          iterator.remove();
        }
      }

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();  // set interrupt flag
      LOGGER.warn("Thread was interruped", e);
    }
  }

  public boolean deleteAllByDatasetId(long datasetId) {
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .createQuery(WorkflowExecution.class);
    query.field(DATASET_ID).equal(datasetId);
    WriteResult delete = morphiaDatastoreProvider.getDatastore().delete(query);
    LOGGER.debug("WorkflowExecution with datasetId: {}, deleted from Mongo", datasetId);
    return delete.getN() >= 1;
  }
}
