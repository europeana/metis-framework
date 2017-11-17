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
public class UserWorkflowExecutionDao implements MetisDao<WorkflowExecution, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserWorkflowExecutionDao.class);
  private static final String WORKFLOW_STATUS = "workflowStatus";
  private static final String DATASET_NAME = "datasetName";
  private final MorphiaDatastoreProvider morphiaDatastoreProvider;
  private int userWorkflowExecutionsPerRequest = 5;

  @Autowired
  public UserWorkflowExecutionDao(MorphiaDatastoreProvider morphiaDatastoreProvider) {
    this.morphiaDatastoreProvider = morphiaDatastoreProvider;
  }

  @Override
  public String create(WorkflowExecution workflowExecution) {
    Key<WorkflowExecution> userWorkflowExecutionKey = morphiaDatastoreProvider.getDatastore().save(
        workflowExecution);
    LOGGER.debug(
        "WorkflowExecution for datasetName '{}' with workflowOwner '{}' and workflowName '{}' created in Mongo",
        workflowExecution.getDatasetName(), workflowExecution.getWorkflowOwner(),
        workflowExecution.getWorkflowName());
    return userWorkflowExecutionKey.getId().toString();
  }

  @Override
  public String update(WorkflowExecution workflowExecution) {
    Key<WorkflowExecution> userWorkflowExecutionKey = morphiaDatastoreProvider.getDatastore().save(
        workflowExecution);
    LOGGER.debug(
        "WorkflowExecution for datasetName '{}' with workflowOwner '{}' and workflowName '{}' updated in Mongo",
        workflowExecution.getDatasetName(), workflowExecution.getWorkflowOwner(),
        workflowExecution.getWorkflowName());
    return userWorkflowExecutionKey.getId().toString();
  }

  public void updateWorkflowPlugins(WorkflowExecution workflowExecution) {
    UpdateOperations<WorkflowExecution> userWorkflowExecutionUpdateOperations = morphiaDatastoreProvider
        .getDatastore()
        .createUpdateOperations(WorkflowExecution.class);
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore().find(WorkflowExecution.class)
        .filter("_id", workflowExecution.getId());
    userWorkflowExecutionUpdateOperations
        .set("metisPlugins", workflowExecution.getMetisPlugins());
    UpdateResults updateResults = morphiaDatastoreProvider.getDatastore()
        .update(query, userWorkflowExecutionUpdateOperations);
    LOGGER.debug(
        "WorkflowExecution metisPlugins for datasetName '{}' with workflowOwner '{}' and workflowName '{}' updated in Mongo. (UpdateResults: {})",
        workflowExecution.getDatasetName(), workflowExecution.getWorkflowOwner(),
        workflowExecution.getWorkflowName(), updateResults.getUpdatedCount());
  }

  public void updateMonitorInformation(WorkflowExecution workflowExecution) {
    UpdateOperations<WorkflowExecution> userWorkflowExecutionUpdateOperations = morphiaDatastoreProvider
        .getDatastore()
        .createUpdateOperations(WorkflowExecution.class);
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore().find(WorkflowExecution.class)
        .filter("_id", workflowExecution.getId());
    userWorkflowExecutionUpdateOperations
        .set(WORKFLOW_STATUS, workflowExecution.getWorkflowStatus());
    if (workflowExecution.getStartedDate() != null) {
      userWorkflowExecutionUpdateOperations
          .set("startedDate", workflowExecution.getStartedDate());
    }
    if (workflowExecution.getUpdatedDate() != null) {
      userWorkflowExecutionUpdateOperations
          .set("updatedDate", workflowExecution.getUpdatedDate());
    }
    userWorkflowExecutionUpdateOperations
        .set("metisPlugins", workflowExecution.getMetisPlugins());
    UpdateResults updateResults = morphiaDatastoreProvider.getDatastore()
        .update(query, userWorkflowExecutionUpdateOperations);
    LOGGER.debug(
        "WorkflowExecution monitor information for datasetName '{}' with workflowOwner '{}' and workflowName '{}' updated in Mongo. (UpdateResults: {})",
        workflowExecution.getDatasetName(), workflowExecution.getWorkflowOwner(),
        workflowExecution.getWorkflowName(), updateResults.getUpdatedCount());
  }

  public void setCancellingState(WorkflowExecution workflowExecution) {
    UpdateOperations<WorkflowExecution> userWorkflowExecutionUpdateOperations = morphiaDatastoreProvider
        .getDatastore()
        .createUpdateOperations(WorkflowExecution.class);
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore().find(WorkflowExecution.class)
        .filter("_id", workflowExecution.getId());
    userWorkflowExecutionUpdateOperations.set("cancelling", Boolean.TRUE);
    UpdateResults updateResults = morphiaDatastoreProvider.getDatastore()
        .update(query, userWorkflowExecutionUpdateOperations);
    LOGGER.debug(
        "WorkflowExecution cancelling for datasetName '{}' with workflowOwner '{}' and workflowName '{}' set to true in Mongo. (UpdateResults: {})",
        workflowExecution.getDatasetName(), workflowExecution.getWorkflowOwner(),
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

  public WorkflowExecution getRunningOrInQueueExecution(String datasetName) {
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .find(WorkflowExecution.class)
        .field(DATASET_NAME).equal(
            datasetName);
    query.or(query.criteria(WORKFLOW_STATUS).equal(WorkflowStatus.INQUEUE),
        query.criteria(WORKFLOW_STATUS).equal(WorkflowStatus.RUNNING));
    return query.get();
  }

  public boolean exists(WorkflowExecution workflowExecution) {
    return morphiaDatastoreProvider.getDatastore().find(WorkflowExecution.class)
        .field(DATASET_NAME).equal(
            workflowExecution.getDatasetName()).field("workflowOwner").equal(
            workflowExecution.getWorkflowOwner()).field("workflowName")
        .equal(workflowExecution.getWorkflowName())
        .project("_id", true).get() != null;
  }

  public String existsAndNotCompleted(String datasetName) {
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .find(WorkflowExecution.class)
        .field(DATASET_NAME).equal(
            datasetName);
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

  public WorkflowExecution getRunningUserWorkflowExecution(String datasetName) {
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .createQuery(WorkflowExecution.class);
    query.field(DATASET_NAME).equal(
        datasetName)
        .field(WORKFLOW_STATUS).equal(WorkflowStatus.RUNNING);
    return query.get();
  }

  public List<WorkflowExecution> getAllUserWorkflowExecutions(String datasetName,
      String workflowOwner,
      String workflowName,
      WorkflowStatus workflowStatus, String nextPage) {
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .createQuery(WorkflowExecution.class);
    query.field(DATASET_NAME).equal(datasetName)
        .field("workflowOwner").equal(workflowOwner)
        .field("workflowName").equal(workflowName);
    if (workflowStatus != null && workflowStatus != WorkflowStatus.NULL) {
      query.field(WORKFLOW_STATUS).equal(workflowStatus);
    }
    query.order("_id");
    if (StringUtils.isNotEmpty(nextPage)) {
      query.field("_id").greaterThan(new ObjectId(nextPage));
    }
    return query.asList(new FindOptions().limit(userWorkflowExecutionsPerRequest));
  }

  public List<WorkflowExecution> getAllUserWorkflowExecutions(WorkflowStatus workflowStatus,
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
    return query.asList(new FindOptions().limit(userWorkflowExecutionsPerRequest));
  }

  public int getUserWorkflowExecutionsPerRequest() {
    return userWorkflowExecutionsPerRequest;
  }

  public void setUserWorkflowExecutionsPerRequest(int userWorkflowExecutionsPerRequest) {
    this.userWorkflowExecutionsPerRequest = userWorkflowExecutionsPerRequest;
  }

  public boolean isCancelled(ObjectId id) {
    return morphiaDatastoreProvider.getDatastore().find(WorkflowExecution.class).field("_id").equal(id)
        .project(WORKFLOW_STATUS, true).get().getWorkflowStatus() == WorkflowStatus.CANCELLED;
  }

  public boolean isCancelling(ObjectId id) {
    return morphiaDatastoreProvider.getDatastore().find(WorkflowExecution.class).field("_id").equal(id)
        .project("cancelling", true).get().isCancelling();
  }

  public boolean isExecutionActive(WorkflowExecution workflowExecutionToCheck,
      int monitorCheckInSecs) {
    try {
      Date updatedDateBefore = workflowExecutionToCheck.getUpdatedDate();
      Thread.sleep(2 * monitorCheckInSecs * 1000L);
      WorkflowExecution workflowExecution = this
          .getById(workflowExecutionToCheck.getId().toString());
      return (updatedDateBefore != null && updatedDateBefore.compareTo(workflowExecution.getUpdatedDate()) < 0) ||
          (updatedDateBefore == null && workflowExecution.getUpdatedDate() != null)
          || workflowExecution.getFinishedDate() != null;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();  // set interrupt flag
      LOGGER.warn("Thread was interrupted", e);
      return true;
    }
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

  public boolean deleteAllByDatasetName(String datasetName) {
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .createQuery(WorkflowExecution.class);
    query.field(DATASET_NAME).equal(datasetName);
    WriteResult delete = morphiaDatastoreProvider.getDatastore().delete(query);
    LOGGER.debug("WorkflowExecution with datasetName: {}, deleted from Mongo", datasetName);
    return delete.getN() >= 1;
  }

  public void updateAllDatasetNames(String datasetName, String newDatasetName) {
    UpdateOperations<WorkflowExecution> userWorkflowExecutionUpdateOperations = morphiaDatastoreProvider
        .getDatastore()
        .createUpdateOperations(WorkflowExecution.class);
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore().find(WorkflowExecution.class)
        .filter(DATASET_NAME, datasetName);
    userWorkflowExecutionUpdateOperations.set(DATASET_NAME, newDatasetName);
    UpdateResults updateResults = morphiaDatastoreProvider.getDatastore()
        .update(query, userWorkflowExecutionUpdateOperations);
    LOGGER.debug(
        "WorkflowExecution with datasetName '{}' renamed to '{}'. (UpdateResults: {})",
        datasetName, newDatasetName, updateResults.getUpdatedCount());
  }
}
