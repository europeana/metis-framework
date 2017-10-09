package eu.europeana.metis.core.test.dao;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import eu.europeana.metis.core.dao.UserWorkflowDao;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.UserWorkflow;
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
public class TestUserWorkflowDao {

  private static UserWorkflowDao userWorkflowDao;
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

    userWorkflowDao = new UserWorkflowDao(provider);
    userWorkflowDao.setUserWorkflowsPerRequest(5);
  }

  @AfterClass
  public static void destroy() {
    embeddedLocalhostMongo.stop();
  }

  @After
  public void cleanUp() {
    Datastore datastore = provider.getDatastore();
    datastore.delete(datastore.createQuery(UserWorkflow.class));
  }

  @Test
  public void createUserWorkflow() {
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    String objectId = userWorkflowDao.create(userWorkflow);
    Assert.assertNotNull(objectId);
  }

  @Test
  public void testUpdateUserWorkflow() {
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    userWorkflowDao.create(userWorkflow);
    String updatedWorkflowName = "updatedWorkflowName";
    userWorkflow.setWorkflowName(updatedWorkflowName);
    String objectId = userWorkflowDao.update(userWorkflow);
    Assert.assertNotNull(objectId);
    UserWorkflow updatedUserWorkflow = userWorkflowDao.getById(objectId);
    Assert.assertEquals(updatedWorkflowName, updatedUserWorkflow.getWorkflowName());
  }

  @Test
  public void getById() {
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    String objectId = userWorkflowDao.create(userWorkflow);
    UserWorkflow retrievedUserWorkflow = userWorkflowDao.getById(objectId);
    Assert.assertEquals(userWorkflow.getWorkflowOwner(), retrievedUserWorkflow.getWorkflowOwner());
    Assert.assertEquals(userWorkflow.getWorkflowName(), retrievedUserWorkflow.getWorkflowName());
    Assert.assertEquals(userWorkflow.isHarvestPlugin(), retrievedUserWorkflow.isHarvestPlugin());
    Assert
        .assertEquals(userWorkflow.isTransformPlugin(), retrievedUserWorkflow.isTransformPlugin());

    List<AbstractMetisPluginMetadata> metisPluginsMetadata = userWorkflow.getMetisPluginsMetadata();
    List<AbstractMetisPluginMetadata> retrievedUserWorkflowMetisPluginsMetadata = retrievedUserWorkflow
        .getMetisPluginsMetadata();
    Assert.assertEquals(metisPluginsMetadata.size(),
        retrievedUserWorkflowMetisPluginsMetadata.size());
    Assert.assertEquals(retrievedUserWorkflowMetisPluginsMetadata.get(0).getPluginType(),
        metisPluginsMetadata.get(0).getPluginType());
    Assert.assertEquals(
        retrievedUserWorkflowMetisPluginsMetadata.get(1).getParameters().get("GroupA").size(),
        metisPluginsMetadata.get(1).getParameters().get("GroupA").size());
  }

  @Test
  public void delete() {
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    userWorkflowDao.create(userWorkflow);
    Assert.assertTrue(userWorkflowDao.delete(userWorkflow));
    Assert.assertFalse(userWorkflowDao.delete(userWorkflow));
  }

  @Test
  public void deleteUserWorkflow() {
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    userWorkflowDao.create(userWorkflow);
    Assert.assertTrue(userWorkflowDao
        .deleteUserWorkflow(userWorkflow.getWorkflowOwner(), userWorkflow.getWorkflowName()));
    Assert.assertFalse(userWorkflowDao
        .deleteUserWorkflow(userWorkflow.getWorkflowOwner(), userWorkflow.getWorkflowName()));
  }

  @Test
  public void exists() {
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    userWorkflowDao.create(userWorkflow);
    Assert.assertNotNull(userWorkflowDao.exists(userWorkflow));
  }

  @Test
  public void getUserWorkflow() {
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    userWorkflowDao.create(userWorkflow);
    Assert.assertNotNull(userWorkflowDao
        .getUserWorkflow(userWorkflow.getWorkflowOwner(), userWorkflow.getWorkflowName()));
  }

  @Test
  public void getAllUserWorkflows()
  {
    int userWorkflowsToCreate = userWorkflowDao.getUserWorkflowsPerRequest() + 1;
    for (int i = 0; i < userWorkflowsToCreate; i++)
    {
      UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
      userWorkflow.setWorkflowName(String.format("%s%s", TestObjectFactory.WORKFLOWNAME, i));
      userWorkflowDao.create(userWorkflow);
    }
    String nextPage = null;
    int allUserWorkflowsCount = 0;
    do {
      ResponseListWrapper<UserWorkflow> userWorkflowResponseListWrapper = new ResponseListWrapper<>();
      userWorkflowResponseListWrapper.setResultsAndLastPage(userWorkflowDao
          .getAllUserWorkflows(TestObjectFactory.WORKFLOWOWNER, nextPage), userWorkflowDao.getUserWorkflowsPerRequest());
      allUserWorkflowsCount+=userWorkflowResponseListWrapper.getListSize();
      nextPage = userWorkflowResponseListWrapper.getNextPage();
    }while(nextPage != null);

    Assert.assertEquals(userWorkflowsToCreate, allUserWorkflowsCount);
  }


}
