package eu.europeana.metis.core.dao;

import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
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
public class UserWorkflowExecutionDao implements MetisDao<UserWorkflowExecution, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserWorkflowExecutionDao.class);
  private final MorphiaDatastoreProvider provider;
  private int userWorkflowExecutionsPerRequest = 5;

  @Autowired
  public UserWorkflowExecutionDao(MorphiaDatastoreProvider provider) {
    this.provider = provider;
  }

  @Override
  public String create(UserWorkflowExecution userWorkflowExecution) {
    Key<UserWorkflowExecution> userWorkflowExecutionKey = provider.getDatastore().save(
        userWorkflowExecution);
    LOGGER.info("UserWorkflowExecution for datasetName '" + userWorkflowExecution.getDatasetName()
        + "' with owner '" + userWorkflowExecution.getOwner() + "' and workflowName '"
        + userWorkflowExecution.getWorkflowName() + "' created in Mongo");
    return userWorkflowExecutionKey.getId().toString();
  }

  @Override
  public String update(UserWorkflowExecution userWorkflowExecution) {
    Key<UserWorkflowExecution> userWorkflowExecutionKey = provider.getDatastore().save(
        userWorkflowExecution);
    LOGGER.debug("UserWorkflowExecution for datasetName '" + userWorkflowExecution.getDatasetName()
        + "' with owner '" + userWorkflowExecution.getOwner() + "' and workflowName '"
        + userWorkflowExecution.getWorkflowName() + "' updated in Mongo");
    return userWorkflowExecutionKey.getId().toString();
  }

  public void updateWorkflowPlugins(UserWorkflowExecution userWorkflowExecution) {
    UpdateOperations<UserWorkflowExecution> userWorkflowExecutionUpdateOperations = provider
        .getDatastore()
        .createUpdateOperations(UserWorkflowExecution.class);
    Query<UserWorkflowExecution> query = provider.getDatastore().find(UserWorkflowExecution.class)
        .filter("_id", userWorkflowExecution.getId());
    userWorkflowExecutionUpdateOperations
        .set("metisPlugins", userWorkflowExecution.getMetisPlugins());
    UpdateResults updateResults = provider.getDatastore()
        .update(query, userWorkflowExecutionUpdateOperations);
    LOGGER.debug(
        "UserWorkflowExecution metisPlugins for datasetName '" + userWorkflowExecution
            .getDatasetName()
            + "' with owner '" + userWorkflowExecution.getOwner() + "' and workflowName '"
            + userWorkflowExecution.getWorkflowName() + "' updated in Mongo. (UpdateResults: "
            + updateResults.getUpdatedCount() + ")");
  }

  public void updateMonitorInformation(UserWorkflowExecution userWorkflowExecution) {
    UpdateOperations<UserWorkflowExecution> userWorkflowExecutionUpdateOperations = provider
        .getDatastore()
        .createUpdateOperations(UserWorkflowExecution.class);
    Query<UserWorkflowExecution> query = provider.getDatastore().find(UserWorkflowExecution.class)
        .filter("_id", userWorkflowExecution.getId());
    userWorkflowExecutionUpdateOperations
        .set("workflowStatus", userWorkflowExecution.getWorkflowStatus());
    if (userWorkflowExecution.getStartedDate() != null) {
      userWorkflowExecutionUpdateOperations
          .set("startedDate", userWorkflowExecution.getStartedDate());
    }
    if (userWorkflowExecution.getUpdatedDate() != null) {
      userWorkflowExecutionUpdateOperations
          .set("updatedDate", userWorkflowExecution.getUpdatedDate());
    }
    userWorkflowExecutionUpdateOperations
        .set("metisPlugins", userWorkflowExecution.getMetisPlugins());
    UpdateResults updateResults = provider.getDatastore()
        .update(query, userWorkflowExecutionUpdateOperations);
    LOGGER.debug(
        "UserWorkflowExecution monitor information for datasetName '" + userWorkflowExecution
            .getDatasetName()
            + "' with owner '" + userWorkflowExecution.getOwner() + "' and workflowName '"
            + userWorkflowExecution.getWorkflowName() + "' updated in Mongo. (UpdateResults: "
            + updateResults.getUpdatedCount() + ")");
  }

  public void setCancellingState(UserWorkflowExecution userWorkflowExecution) {
    UpdateOperations<UserWorkflowExecution> userWorkflowExecutionUpdateOperations = provider
        .getDatastore()
        .createUpdateOperations(UserWorkflowExecution.class);
    Query<UserWorkflowExecution> query = provider.getDatastore().find(UserWorkflowExecution.class)
        .filter("_id", userWorkflowExecution.getId());
    userWorkflowExecutionUpdateOperations.set("cancelling", true);
    UpdateResults updateResults = provider.getDatastore()
        .update(query, userWorkflowExecutionUpdateOperations);
    LOGGER.debug(
        "UserWorkflowExecution cancelling for datasetName '" + userWorkflowExecution
            .getDatasetName()
            + "' with owner '" + userWorkflowExecution.getOwner() + "' and workflowName '"
            + userWorkflowExecution.getWorkflowName() + "' set to true in Mongo. (UpdateResults: "
            + updateResults.getUpdatedCount() + ")");
  }

  @Override
  public UserWorkflowExecution getById(String id) {
    Query<UserWorkflowExecution> query = provider.getDatastore()
        .find(UserWorkflowExecution.class)
        .field("_id").equal(new ObjectId(id));
    return query.get();
  }

  @Override
  public boolean delete(UserWorkflowExecution userWorkflowExecution) {
    return false;
  }

  public UserWorkflowExecution getRunningOrInQueueExecution(String datasetName) {
    Query<UserWorkflowExecution> query = provider.getDatastore()
        .find(UserWorkflowExecution.class)
        .field("datasetName").equal(
            datasetName);
    query.or(query.criteria("workflowStatus").equal(WorkflowStatus.INQUEUE),
        query.criteria("workflowStatus").equal(WorkflowStatus.RUNNING));
    return query.get();
  }

  public boolean exists(UserWorkflowExecution userWorkflowExecution) {
    return provider.getDatastore().find(UserWorkflowExecution.class)
        .field("datasetName").equal(
            userWorkflowExecution.getDatasetName()).field("owner").equal(
            userWorkflowExecution.getOwner()).field("workflowName")
        .equal(userWorkflowExecution.getWorkflowName())
        .project("_id", true).get() != null;
  }

  public boolean cancel(UserWorkflowExecution userWorkflowExecution) {
    return false;
  }

  public String existsAndNotCompleted(String datasetName) {
    Query<UserWorkflowExecution> query = provider.getDatastore()
        .find(UserWorkflowExecution.class)
        .field("datasetName").equal(
            datasetName);
    query.or(query.criteria("workflowStatus").equal(WorkflowStatus.INQUEUE),
        query.criteria("workflowStatus").equal(WorkflowStatus.RUNNING));
    query.project("_id", true);
    query.project("workflowStatus", true);

    UserWorkflowExecution storedUserWorkflowExecution = query.get();
    if (storedUserWorkflowExecution != null) {
      return storedUserWorkflowExecution.getId().toString();
    }
    return null;
  }

  public UserWorkflowExecution getRunningUserWorkflowExecution(String datasetName, String owner,
      String workflowName) {
    Query<UserWorkflowExecution> query = provider.getDatastore()
        .createQuery(UserWorkflowExecution.class);
    query.field("datasetName").equal(
        datasetName)
        .field("owner").equal(owner)
        .field("workflowName").equal(workflowName)
        .field("workflowStatus").equal(WorkflowStatus.RUNNING);
    return query.get();
  }

  public List<UserWorkflowExecution> getAllUserWorkflowExecutions(String datasetName, String owner,
      String workflowName,
      WorkflowStatus workflowStatus, String nextPage) {
    Query<UserWorkflowExecution> query = provider.getDatastore()
        .createQuery(UserWorkflowExecution.class);
    query.field("datasetName").equal(datasetName)
        .field("owner").equal(owner)
        .field("workflowName").equal(workflowName);
    if (workflowStatus != null && workflowStatus != WorkflowStatus.NULL) {
      query.field("workflowStatus").equal(workflowStatus);
    }
    query.order("_id");
    if (StringUtils.isNotEmpty(nextPage)) {
      query.field("_id").greaterThan(new ObjectId(nextPage));
    }
    return query.asList(new FindOptions().limit(userWorkflowExecutionsPerRequest));
  }

  public List<UserWorkflowExecution> getAllUserWorkflowExecutions(WorkflowStatus workflowStatus,
      String nextPage) {
    Query<UserWorkflowExecution> query = provider.getDatastore()
        .createQuery(UserWorkflowExecution.class);
    if (workflowStatus != null && workflowStatus != WorkflowStatus.NULL) {
      query.field("workflowStatus").equal(workflowStatus);
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
    return provider.getDatastore().find(UserWorkflowExecution.class).field("_id").equal(id)
        .project("workflowStatus", true).get().getWorkflowStatus() == WorkflowStatus.CANCELLED;
  }

  public boolean isCancelling(ObjectId id) {
    return provider.getDatastore().find(UserWorkflowExecution.class).field("_id").equal(id)
        .project("cancelling", true).get().isCancelling();
  }

  public boolean isExecutionActive(UserWorkflowExecution userWorkflowExecutionToCheck,
      int monitorCheckInSecs) {
    try {
      Date updatedDateBefore = userWorkflowExecutionToCheck.getUpdatedDate();
//      //Wait between 2-3 times the monitorCheckInSecs to make sure that 2 checks for the same execution won't be identical
//      Random rand = new Random();
//      float random = 2 + rand.nextFloat() * (3 - 2);
//      Thread.sleep((long) ((random * monitorCheckInSecs) * 1000));
      Thread.sleep(2 * monitorCheckInSecs * 1000);
      UserWorkflowExecution userWorkflowExecution = this
          .getById(userWorkflowExecutionToCheck.getId().toString());
      return updatedDateBefore != null
          && updatedDateBefore.compareTo(userWorkflowExecution.getUpdatedDate()) < 0;
    } catch (InterruptedException e) {
      LOGGER.warn("Thread was interruped", e);
      return true;
    }
  }

  public void removeActiveExecutionsFromList(List<UserWorkflowExecution> userWorkflowExecutions,
      int monitorCheckInSecs) {
    try {
      Thread.sleep(2 * monitorCheckInSecs * 1000);
      for (Iterator<UserWorkflowExecution> iterator = userWorkflowExecutions.iterator();
          iterator.hasNext(); ) {
        UserWorkflowExecution userWorkflowExecutionToCheck = iterator.next();
        UserWorkflowExecution userWorkflowExecution = this
            .getById(userWorkflowExecutionToCheck.getId().toString());
        if (userWorkflowExecutionToCheck.getUpdatedDate() != null && userWorkflowExecutionToCheck.getUpdatedDate()
            .compareTo(userWorkflowExecution.getUpdatedDate()) < 0) {
          iterator.remove();
        }
      }

    } catch (InterruptedException e) {
      LOGGER.warn("Thread was interruped", e);
    }
  }
}
