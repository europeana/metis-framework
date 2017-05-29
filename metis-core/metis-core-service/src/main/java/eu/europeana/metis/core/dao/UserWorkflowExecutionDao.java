package eu.europeana.metis.core.dao;

import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
import org.mongodb.morphia.Key;
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
    LOGGER.info("UserWorkflowExecution '" + userWorkflowExecution.getWorkflowName() + "' created with owner '" + userWorkflowExecution
        .getOwner() + "' in Mongo");
    return userWorkflowExecutionKey.getId().toString();
  }

  @Override
  public String update(UserWorkflowExecution userWorkflowExecution) {
    return null;
  }

  @Override
  public UserWorkflowExecution getById(String id) {
    return null;
  }

  @Override
  public boolean delete(UserWorkflowExecution userWorkflowExecution) {
    return false;
  }

  public boolean exists(UserWorkflowExecution userWorkflowExecution)
  {
    return provider.getDatastore().find(UserWorkflowExecution.class).field("owner").equal(
        userWorkflowExecution.getOwner()).field("workflowName").equal(userWorkflowExecution.getWorkflowName())
      .project("_id", true).get() != null;
  }
}
