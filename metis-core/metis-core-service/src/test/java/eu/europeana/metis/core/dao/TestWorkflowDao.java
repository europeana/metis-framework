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
    workflowDao.setWorkflowsPerRequest(5);
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
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    String objectId = workflowDao.create(workflow);
    Assert.assertNotNull(objectId);
  }

  @Test
  public void testUpdateUserWorkflow() {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    workflowDao.create(workflow);
    workflow.setMetisPluginsMetadata(null);
    String objectId = workflowDao.update(workflow);
    Assert.assertNotNull(objectId);
    Workflow updatedWorkflow = workflowDao.getById(objectId);
    Assert.assertEquals(0, updatedWorkflow.getMetisPluginsMetadata().size());
  }

  @Test
  public void getById() {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    String objectId = workflowDao.create(workflow);
    Workflow retrievedWorkflow = workflowDao.getById(objectId);
    Assert.assertEquals(workflow.getWorkflowOwner(), retrievedWorkflow.getWorkflowOwner());

    List<AbstractMetisPluginMetadata> metisPluginsMetadata = workflow.getMetisPluginsMetadata();
    List<AbstractMetisPluginMetadata> retrievedUserWorkflowMetisPluginsMetadata = retrievedWorkflow
        .getMetisPluginsMetadata();
    Assert.assertEquals(metisPluginsMetadata.size(),
        retrievedUserWorkflowMetisPluginsMetadata.size());
    Assert.assertEquals(retrievedUserWorkflowMetisPluginsMetadata.get(0).getPluginType(),
        metisPluginsMetadata.get(0).getPluginType());
  }

  @Test
  public void delete() {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    workflowDao.create(workflow);
    Assert.assertTrue(workflowDao.delete(workflow));
    Assert.assertFalse(workflowDao.delete(workflow));
  }

  @Test
  public void deleteUserWorkflow() {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    workflowDao.create(workflow);
    Assert.assertTrue(workflowDao.deleteWorkflow(workflow.getDatasetId()));
    Assert.assertFalse(workflowDao.deleteWorkflow(workflow.getDatasetId()));
  }

  @Test
  public void exists() {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    workflowDao.create(workflow);
    Assert.assertNotNull(workflowDao.exists(workflow));
  }

  @Test
  public void getUserWorkflow() {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    workflowDao.create(workflow);
    Assert.assertNotNull(workflowDao.getWorkflow(workflow.getDatasetId()));
  }

  @Test
  public void getAllUserWorkflows()
  {
    int userWorkflowsToCreate = workflowDao.getWorkflowsPerRequest() + 1;
    for (int i = 0; i < userWorkflowsToCreate; i++)
    {
      Workflow workflow = TestObjectFactory.createWorkflowObject();
      workflow.setDatasetId(TestObjectFactory.DATASETID + i);
      workflowDao.create(workflow);
    }
    int nextPage = 0;
    int allUserWorkflowsCount = 0;
    do {
      ResponseListWrapper<Workflow> userWorkflowResponseListWrapper = new ResponseListWrapper<>();
      userWorkflowResponseListWrapper.setResultsAndLastPage(workflowDao
          .getAllWorkflows(TestObjectFactory.WORKFLOWOWNER, nextPage), workflowDao.getWorkflowsPerRequest(), nextPage);
      allUserWorkflowsCount+=userWorkflowResponseListWrapper.getListSize();
      nextPage = userWorkflowResponseListWrapper.getNextPage();
    }while(nextPage != -1);

    Assert.assertEquals(userWorkflowsToCreate, allUserWorkflowsCount);
  }


}
