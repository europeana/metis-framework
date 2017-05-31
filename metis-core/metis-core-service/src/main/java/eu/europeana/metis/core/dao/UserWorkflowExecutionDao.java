package eu.europeana.metis.core.dao;

import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-26
 */
public class UserWorkflowExecutionDao implements MetisDao<UserWorkflowExecution, String> {

  private final Logger LOGGER = LoggerFactory.getLogger(UserWorkflowExecutionDao.class);
  private final MorphiaDatastoreProvider provider;

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

  public UserWorkflowExecution getRunningOrInQueueExecution(String datasetName,
      String owner,
      String workflowName) {
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

  public boolean cancel(UserWorkflowExecution userWorkflowExecution)
  {
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
}
