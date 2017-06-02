package eu.europeana.metis.core.dao;

import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.FindOptions;
import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-26
 */
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

  @Override
  public UserWorkflowExecution getById(String id) {
    return null;
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

  public List<UserWorkflowExecution> getAllUserWorkflowExecutions(WorkflowStatus workflowStatus, String nextPage) {
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
}
