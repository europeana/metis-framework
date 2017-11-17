package eu.europeana.metis.core.dao;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPluginMetadata;
import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;
import java.io.IOException;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongodb.morphia.Datastore;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-04
 */
public class TestWorkflowDao {

  private static WorkflowDao workflowDao;
  private static EmbeddedLocalhostMongo embeddedLocalhostMongo;
  private static MorphiaDatastoreProvider provider;

  @BeforeClass
  public static void prepare() throws IOException {
    embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
    embeddedLocalhostMongo.start();
    String mongoHost = embeddedLocalhostMongo.getMongoHost();
    int mongoPort = embeddedLocalhostMongo.getMongoPort();
    ServerAddress address = new ServerAddress(mongoHost, mongoPort);
    MongoClient mongoClient = new MongoClient(address);
    provider = new MorphiaDatastoreProvider(mongoClient, "test");

    workflowDao = new WorkflowDao(provider);
    workflowDao.setUserWorkflowsPerRequest(5);
  }

  @AfterClass
  public static void destroy() {
    embeddedLocalhostMongo.stop();
  }

  @After
  public void cleanUp() {
    Datastore datastore = provider.getDatastore();
    datastore.delete(datastore.createQuery(Workflow.class));
  }

  @Test
  public void createUserWorkflow() {
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    String objectId = workflowDao.create(workflow);
    Assert.assertNotNull(objectId);
  }

  @Test
  public void testUpdateUserWorkflow() {
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    workflowDao.create(workflow);
    String updatedWorkflowName = "updatedWorkflowName";
    workflow.setWorkflowName(updatedWorkflowName);
    String objectId = workflowDao.update(workflow);
    Assert.assertNotNull(objectId);
    Workflow updatedWorkflow = workflowDao.getById(objectId);
    Assert.assertEquals(updatedWorkflowName, updatedWorkflow.getWorkflowName());
  }

  @Test
  public void getById() {
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    String objectId = workflowDao.create(workflow);
    Workflow retrievedWorkflow = workflowDao.getById(objectId);
    Assert.assertEquals(workflow.getWorkflowOwner(), retrievedWorkflow.getWorkflowOwner());
    Assert.assertEquals(workflow.getWorkflowName(), retrievedWorkflow.getWorkflowName());
    Assert.assertEquals(workflow.isHarvestPlugin(), retrievedWorkflow.isHarvestPlugin());
    Assert
        .assertEquals(workflow.isTransformPlugin(), retrievedWorkflow.isTransformPlugin());

    List<AbstractMetisPluginMetadata> metisPluginsMetadata = workflow.getMetisPluginsMetadata();
    List<AbstractMetisPluginMetadata> retrievedUserWorkflowMetisPluginsMetadata = retrievedWorkflow
        .getMetisPluginsMetadata();
    Assert.assertEquals(metisPluginsMetadata.size(),
        retrievedUserWorkflowMetisPluginsMetadata.size());
    Assert.assertEquals(retrievedUserWorkflowMetisPluginsMetadata.get(0).getPluginType(),
        metisPluginsMetadata.get(0).getPluginType());
    Assert.assertEquals(
        retrievedUserWorkflowMetisPluginsMetadata.get(0).getParameters().get("GroupA").size(),
        metisPluginsMetadata.get(0).getParameters().get("GroupA").size());
  }

  @Test
  public void delete() {
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    workflowDao.create(workflow);
    Assert.assertTrue(workflowDao.delete(workflow));
    Assert.assertFalse(workflowDao.delete(workflow));
  }

  @Test
  public void deleteUserWorkflow() {
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    workflowDao.create(workflow);
    Assert.assertTrue(workflowDao
        .deleteUserWorkflow(workflow.getWorkflowOwner(), workflow.getWorkflowName()));
    Assert.assertFalse(workflowDao
        .deleteUserWorkflow(workflow.getWorkflowOwner(), workflow.getWorkflowName()));
  }

  @Test
  public void exists() {
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    workflowDao.create(workflow);
    Assert.assertNotNull(workflowDao.exists(workflow));
  }

  @Test
  public void getUserWorkflow() {
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    workflowDao.create(workflow);
    Assert.assertNotNull(workflowDao
        .getUserWorkflow(workflow.getWorkflowOwner(), workflow.getWorkflowName()));
  }

  @Test
  public void getAllUserWorkflows()
  {
    int userWorkflowsToCreate = workflowDao.getUserWorkflowsPerRequest() + 1;
    for (int i = 0; i < userWorkflowsToCreate; i++)
    {
      Workflow workflow = TestObjectFactory.createUserWorkflowObject();
      workflow.setWorkflowName(String.format("%s%s", TestObjectFactory.WORKFLOWNAME, i));
      workflowDao.create(workflow);
    }
    String nextPage = null;
    int allUserWorkflowsCount = 0;
    do {
      ResponseListWrapper<Workflow> userWorkflowResponseListWrapper = new ResponseListWrapper<>();
      userWorkflowResponseListWrapper.setResultsAndLastPage(workflowDao
          .getAllUserWorkflows(TestObjectFactory.WORKFLOWOWNER, nextPage), workflowDao.getUserWorkflowsPerRequest());
      allUserWorkflowsCount+=userWorkflowResponseListWrapper.getListSize();
      nextPage = userWorkflowResponseListWrapper.getNextPage();
    }while(nextPage != null);

    Assert.assertEquals(userWorkflowsToCreate, allUserWorkflowsCount);
  }


}
