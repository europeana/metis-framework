package eu.europeana.metis.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPluginMetadata;
import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mongodb.morphia.Datastore;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-04
 */
class TestWorkflowDao {

  private static WorkflowDao workflowDao;
  private static EmbeddedLocalhostMongo embeddedLocalhostMongo;
  private static MorphiaDatastoreProvider provider;

  @BeforeAll
  static void prepare() {
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

  @AfterAll
  static void destroy() {
    embeddedLocalhostMongo.stop();
  }

  @AfterEach
  void cleanUp() {
    Datastore datastore = provider.getDatastore();
    datastore.delete(datastore.createQuery(Workflow.class));
  }

  @Test
  void createUserWorkflow() {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    String objectId = workflowDao.create(workflow);
    assertNotNull(objectId);
  }

  @Test
  void testUpdateUserWorkflow() {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    workflowDao.create(workflow);
    workflow.setMetisPluginsMetadata(null);
    String objectId = workflowDao.update(workflow);
    assertNotNull(objectId);
    Workflow updatedWorkflow = workflowDao.getById(objectId);
    assertEquals(0, updatedWorkflow.getMetisPluginsMetadata().size());
  }

  @Test
  void getById() {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    String objectId = workflowDao.create(workflow);
    Workflow retrievedWorkflow = workflowDao.getById(objectId);
    assertEquals(workflow.getDatasetId(), retrievedWorkflow.getDatasetId());

    List<AbstractMetisPluginMetadata> metisPluginsMetadata = workflow.getMetisPluginsMetadata();
    List<AbstractMetisPluginMetadata> retrievedUserWorkflowMetisPluginsMetadata = retrievedWorkflow
        .getMetisPluginsMetadata();
    assertEquals(metisPluginsMetadata.size(),
        retrievedUserWorkflowMetisPluginsMetadata.size());
    assertEquals(retrievedUserWorkflowMetisPluginsMetadata.get(0).getPluginType(),
        metisPluginsMetadata.get(0).getPluginType());
  }

  @Test
  void delete() {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    workflowDao.create(workflow);
    assertTrue(workflowDao.delete(workflow));
    assertFalse(workflowDao.delete(workflow));
  }

  @Test
  void deleteUserWorkflow() {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    workflowDao.create(workflow);
    assertTrue(workflowDao.deleteWorkflow(workflow.getDatasetId()));
    assertFalse(workflowDao.deleteWorkflow(workflow.getDatasetId()));
  }

  @Test
  void exists() {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    workflowDao.create(workflow);
    assertNotNull(workflowDao.exists(workflow));
  }

  @Test
  void getUserWorkflow() {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    workflowDao.create(workflow);
    assertNotNull(workflowDao.getWorkflow(workflow.getDatasetId()));
  }
}
