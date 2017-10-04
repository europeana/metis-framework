package eu.europeana.metis.core.test.dao;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import eu.europeana.metis.core.dao.UserWorkflowDao;
import eu.europeana.metis.core.dataset.OaipmhHarvestingMetadata;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.workflow.UserWorkflow;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.VoidDereferencePluginMetadata;
import eu.europeana.metis.core.workflow.plugins.VoidOaipmhHarvestPluginMetadata;
import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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

  private static final String WORKFLOWOWNER = "workflowOwner";
  private static final String WORKFLOWNAME = "workflowName";

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
  public void testCreateUserWorkflow() {
    UserWorkflow userWorkflow = createUserWorkflowObject();
    String objectId = userWorkflowDao.create(userWorkflow);
    Assert.assertNotNull(objectId);
  }

  @Test
  public void testUpdateUserWorkflow() {
    UserWorkflow userWorkflow = createUserWorkflowObject();
    userWorkflowDao.create(userWorkflow);
    String updatedWorkflowName = "updatedWorkflowName";
    userWorkflow.setWorkflowName(updatedWorkflowName);
    String objectId = userWorkflowDao.update(userWorkflow);
    Assert.assertNotNull(objectId);
    UserWorkflow updatedUserWorkflow = userWorkflowDao.getById(objectId);
    Assert.assertEquals(updatedWorkflowName, updatedUserWorkflow.getWorkflowName());
  }

  @Test
  public void testGetById() {
    UserWorkflow userWorkflow = createUserWorkflowObject();
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
  public void testDelete() {
    UserWorkflow userWorkflow = createUserWorkflowObject();
    userWorkflowDao.create(userWorkflow);
    Assert.assertTrue(userWorkflowDao.delete(userWorkflow));
    Assert.assertFalse(userWorkflowDao.delete(userWorkflow));
  }

  @Test
  public void testDeleteUserWorkflow() {
    UserWorkflow userWorkflow = createUserWorkflowObject();
    userWorkflowDao.create(userWorkflow);
    Assert.assertTrue(userWorkflowDao
        .deleteUserWorkflow(userWorkflow.getWorkflowOwner(), userWorkflow.getWorkflowName()));
    Assert.assertFalse(userWorkflowDao
        .deleteUserWorkflow(userWorkflow.getWorkflowOwner(), userWorkflow.getWorkflowName()));
  }

  @Test
  public void testExists() {
    UserWorkflow userWorkflow = createUserWorkflowObject();
    userWorkflowDao.create(userWorkflow);
    Assert.assertNotNull(userWorkflowDao.exists(userWorkflow));
  }

  @Test
  public void testGetUserWorkflow() {
    UserWorkflow userWorkflow = createUserWorkflowObject();
    userWorkflowDao.create(userWorkflow);
    Assert.assertNotNull(userWorkflowDao
        .getUserWorkflow(userWorkflow.getWorkflowOwner(), userWorkflow.getWorkflowName()));
  }

  @Test
  public void testGetAllUserWorkflows()
  {
    int userWorkflowsToCreate = userWorkflowDao.getUserWorkflowsPerRequest() + 1;
    for (int i = 0; i < userWorkflowsToCreate; i++)
    {
      UserWorkflow userWorkflow = createUserWorkflowObject();
      userWorkflow.setWorkflowName(String.format("%s%s", WORKFLOWNAME, i));
      userWorkflowDao.create(userWorkflow);
    }
    String nextPage = null;
    int allUserWorkflowsCount = 0;
    do {
      ResponseListWrapper<UserWorkflow> userWorkflowResponseListWrapper = new ResponseListWrapper<>();
      userWorkflowResponseListWrapper.setResultsAndLastPage(userWorkflowDao
          .getAllUserWorkflows(WORKFLOWOWNER, nextPage), userWorkflowDao.getUserWorkflowsPerRequest());
      allUserWorkflowsCount+=userWorkflowResponseListWrapper.getListSize();
      nextPage = userWorkflowResponseListWrapper.getNextPage();
    }while(nextPage != null);

    Assert.assertEquals(userWorkflowsToCreate, allUserWorkflowsCount);
  }


  private static UserWorkflow createUserWorkflowObject() {
    UserWorkflow userWorkflow = new UserWorkflow();
    userWorkflow.setHarvestPlugin(true);
    userWorkflow.setTransformPlugin(false);
    userWorkflow.setWorkflowOwner(WORKFLOWOWNER);
    userWorkflow.setWorkflowName(WORKFLOWNAME);

    OaipmhHarvestingMetadata oaipmhHarvestingMetadata = new OaipmhHarvestingMetadata(
        "metadataFormat", "setSpec", "http://test.me.now");
    ArrayList<String> oaiParameters = new ArrayList<>();
    oaiParameters.add("oai_parameter_a");
    oaiParameters.add("oai_parameter_b");
    HashMap<String, List<String>> oaiParameterGroups = new HashMap<>();
    oaiParameterGroups.put("GroupA", oaiParameters);
    oaiParameterGroups.put("GroupB", oaiParameters);

    VoidOaipmhHarvestPluginMetadata voidOaipmhHarvestPluginMetadata = new VoidOaipmhHarvestPluginMetadata(
        oaipmhHarvestingMetadata, oaiParameterGroups);

    ArrayList<String> dereferenceParameters = new ArrayList<>();
    dereferenceParameters.add("dereference_parameter_a");
    dereferenceParameters.add("dereference_parameter_b");
    HashMap<String, List<String>> dereferenceParameterGroups = new HashMap<>();
    dereferenceParameterGroups.put("GroupA", dereferenceParameters);
    dereferenceParameterGroups.put("GroupB", dereferenceParameters);
    VoidDereferencePluginMetadata voidDereferencePluginMetadata = new VoidDereferencePluginMetadata(
        dereferenceParameterGroups);

    List<AbstractMetisPluginMetadata> abstractMetisPluginMetadata = new ArrayList<>();
    abstractMetisPluginMetadata.add(voidOaipmhHarvestPluginMetadata);
    abstractMetisPluginMetadata.add(voidDereferencePluginMetadata);
    userWorkflow.setMetisPluginsMetadata(abstractMetisPluginMetadata);

    return userWorkflow;
  }

}
