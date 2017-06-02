package eu.europeana.metis.core.dao;

import com.mongodb.WriteResult;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.workflow.UserWorkflow;
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
 * @since 2017-05-29
 */
public class UserWorkflowDao implements MetisDao<UserWorkflow, String> {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserWorkflow.class);
  private int userWorkflowsPerRequest = 5;
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
    Key<UserWorkflow> userWorkflowKey = provider.getDatastore().save(
        userWorkflow);
    LOGGER.info("UserWorkflow '" + userWorkflow.getWorkflowName() + "' updated with owner '" + userWorkflow
        .getOwner() + "' in Mongo");
    return userWorkflowKey.getId().toString();
  }

  @Override
  public UserWorkflow getById(String id) {
    return null;
  }

  @Override
  public boolean delete(UserWorkflow userWorkflow) {
    return false;
  }


  public boolean deleteUserWorkflow(String owner, String workflowName){
    Query<UserWorkflow> query = provider.getDatastore().createQuery(UserWorkflow.class);
    query.field("owner").equal(owner);
    query.field("workflowName").equal(workflowName);
    WriteResult delete = provider.getDatastore().delete(query);
    LOGGER.info("UserWorkflow with owner '" + owner + "' and workflowName '" + workflowName + "' deleted from Mongo");
    return delete.getN() == 1;
  }

  public String exists(UserWorkflow userWorkflow)
  {
    UserWorkflow storedUserWorkflow = provider.getDatastore().find(UserWorkflow.class).field("owner")
        .equal(
            userWorkflow.getOwner()).field("workflowName").equal(userWorkflow.getWorkflowName())
        .project("_id", true).get();
    return storedUserWorkflow!=null?storedUserWorkflow.getId().toString():null;
  }

  public UserWorkflow getUserWorkflow(String owner, String workflowName) {
    return provider.getDatastore().find(UserWorkflow.class).field("owner").equal(owner)
        .field("workflowName").equal(workflowName)
        .get();
  }

  public List<UserWorkflow> getAllUserWorkflows(String owner, String nextPage) {
    Query<UserWorkflow> query = provider.getDatastore()
        .createQuery(UserWorkflow.class);
    query.field("owner").equal(owner);
    query.order("_id");
    if (StringUtils.isNotEmpty(nextPage)) {
      query.field("_id").greaterThan(new ObjectId(nextPage));
    }
    return query.asList(new FindOptions().limit(userWorkflowsPerRequest));
  }

  public int getUserWorkflowsPerRequest() {
    return userWorkflowsPerRequest;
  }

  public void setUserWorkflowsPerRequest(int userWorkflowsPerRequest) {
    this.userWorkflowsPerRequest = userWorkflowsPerRequest;
  }
}

