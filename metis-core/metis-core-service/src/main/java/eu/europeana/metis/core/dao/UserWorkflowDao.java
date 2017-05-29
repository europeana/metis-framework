package eu.europeana.metis.core.dao;

import com.mongodb.WriteResult;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.workflow.UserWorkflow;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-29
 */
public class UserWorkflowDao implements MetisDao<UserWorkflow, String> {
  private final Logger LOGGER = LoggerFactory.getLogger(UserWorkflow.class);
  private final MorphiaDatastoreProvider provider;

  @Autowired
  public UserWorkflowDao(MorphiaDatastoreProvider provider) {
    this.provider = provider;
  }

  @Override
  public String create(UserWorkflow userWorkflow) {
    Key<UserWorkflow> userWorkflowKey = provider.getDatastore().save(
        userWorkflow);
    LOGGER.info("UserWorkflow '" + userWorkflow.getWorkflowName() + "' created with owner '" + userWorkflow
        .getOwner() + "' in Mongo");
    return userWorkflowKey.getId().toString();
  }

  @Override
  public String update(UserWorkflow userWorkflow) {
    return null;
  }

  @Override
  public UserWorkflow getById(String id) {
    return null;
  }

  @Override
  public boolean delete(UserWorkflow userWorkflow) {
    return false;
  }


  public boolean deleteUserWorkflowByOwnerAndWorkflowName(String owner, String workflowName){
    Query<UserWorkflow> query = provider.getDatastore().createQuery(UserWorkflow.class);
    query.field("owner").equal(owner);
    query.field("workflowName").equal(workflowName);
    WriteResult delete = provider.getDatastore().delete(query);
    LOGGER.info("UserWorkflow with owner '" + owner + "' and workflowName '" + workflowName + "' deleted from Mongo");
    return delete.getN() == 1;
  }

  public boolean exists(UserWorkflow userWorkflow)
  {
    return provider.getDatastore().find(UserWorkflow.class).field("owner").equal(
        userWorkflow.getOwner()).field("workflowName").equal(userWorkflow.getWorkflowName())
        .project("_id", true).get() != null;
  }

  public UserWorkflow getUserWorkflowByOwnerAndWorkflowName(String owner, String workflowName) {
    return provider.getDatastore().find(UserWorkflow.class).field("owner").equal(owner)
        .field("workflowName").equal(workflowName)
        .get();
  }

  public boolean existsUserWorkflowByOwnerAndWorkflowName(String owner, String workflowName) {
    return provider.getDatastore().find(UserWorkflow.class).field("owner").equal(owner)
        .field("workflowName").equal(workflowName)
        .project("_id", true).get() != null;
  }
}

