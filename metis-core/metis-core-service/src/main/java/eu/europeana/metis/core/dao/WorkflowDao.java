package eu.europeana.metis.core.dao;

import com.mongodb.WriteResult;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.workflow.Workflow;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.FindOptions;
import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-29
 */
@Repository
public class WorkflowDao implements MetisDao<Workflow, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowDao.class);
  private static final String WORKFLOW_OWNER = "workflowOwner";
  public static final String WORKFLOW_NAME = "workflowName";
  private int workflowsPerRequest = 5;
  private final MorphiaDatastoreProvider morphiaDatastoreProvider;

  @Autowired
  public WorkflowDao(MorphiaDatastoreProvider morphiaDatastoreProvider) {
    this.morphiaDatastoreProvider = morphiaDatastoreProvider;
  }

  @Override
  public String create(Workflow workflow) {
    Key<Workflow> workflowKey = morphiaDatastoreProvider.getDatastore().save(
        workflow);
    LOGGER.info("Workflow '{}' created with workflowOwner '{}' in Mongo",
        workflow.getWorkflowName(), workflow
            .getWorkflowOwner());
    return workflowKey.getId().toString();
  }

  @Override
  public String update(Workflow workflow) {
    Key<Workflow> workflowKey = morphiaDatastoreProvider.getDatastore().save(
        workflow);
    LOGGER.info("Workflow '{}' updated with workflowOwner '{}' in Mongo",
        workflow.getWorkflowName(), workflow
            .getWorkflowOwner());
    return workflowKey.getId().toString();
  }

  @Override
  public Workflow getById(String id) {
    Query<Workflow> query = morphiaDatastoreProvider.getDatastore()
        .find(Workflow.class)
        .field("_id").equal(new ObjectId(id));
    return query.get();
  }

  @Override
  public boolean delete(Workflow workflow) {
    return deleteWorkflow(workflow.getWorkflowOwner(), workflow.getWorkflowName());
  }


  public boolean deleteWorkflow(String workflowOwner, String workflowName) {
    Query<Workflow> query = morphiaDatastoreProvider.getDatastore().createQuery(Workflow.class);
    query.field(WORKFLOW_OWNER).equal(workflowOwner);
    query.field(WORKFLOW_NAME).equal(workflowName);
    WriteResult delete = morphiaDatastoreProvider.getDatastore().delete(query);
    LOGGER.info("Workflow with workflowOwner: {}, and workflowName {}, deleted from Mongo",
        workflowOwner, workflowName);
    return delete.getN() == 1;
  }

  public String exists(Workflow workflow) {
    Workflow storedWorkflow = morphiaDatastoreProvider.getDatastore().find(Workflow.class)
        .field(WORKFLOW_OWNER)
        .equal(
            workflow.getWorkflowOwner()).field(WORKFLOW_NAME)
        .equal(workflow.getWorkflowName())
        .project("_id", true).get();
    return storedWorkflow != null ? storedWorkflow.getId().toString() : null;
  }

  public Workflow getWorkflow(String workflowOwner, String workflowName) {
    return morphiaDatastoreProvider.getDatastore().find(Workflow.class).field(WORKFLOW_OWNER)
        .equal(workflowOwner)
        .field(WORKFLOW_NAME).equal(workflowName)
        .get();
  }

  public List<Workflow> getAllWorkflows(String workflowOwner, String nextPage) {
    Query<Workflow> query = morphiaDatastoreProvider.getDatastore()
        .createQuery(Workflow.class);
    query.field(WORKFLOW_OWNER).equal(workflowOwner);
    query.order("_id");
    if (StringUtils.isNotEmpty(nextPage)) {
      query.field("_id").greaterThan(new ObjectId(nextPage));
    }
    return query.asList(new FindOptions().limit(workflowsPerRequest));
  }

  public int getWorkflowsPerRequest() {
    return workflowsPerRequest;
  }

  public void setWorkflowsPerRequest(int workflowsPerRequest) {
    this.workflowsPerRequest = workflowsPerRequest;
  }
}

