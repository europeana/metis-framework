package eu.europeana.metis.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.DeleteOptions;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProviderImpl;
import eu.europeana.metis.core.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePluginMetadata;
import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-04
 */
class TestWorkflowDao {

  private static WorkflowDao workflowDao;
  private static EmbeddedLocalhostMongo embeddedLocalhostMongo;
  private static MorphiaDatastoreProviderImpl provider;

  @BeforeAll
  static void prepare() {
    embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
    embeddedLocalhostMongo.start();
    String mongoHost = embeddedLocalhostMongo.getMongoHost();
    int mongoPort = embeddedLocalhostMongo.getMongoPort();
    MongoClient mongoClient = MongoClients
        .create(String.format("mongodb://%s:%s", mongoHost, mongoPort));
    provider = new MorphiaDatastoreProviderImpl(mongoClient, "test");

    workflowDao = new WorkflowDao(provider);
  }

  @AfterAll
  static void destroy() {
    embeddedLocalhostMongo.stop();
  }

  @AfterEach
  void cleanUp() {
    Datastore datastore = provider.getDatastore();
    datastore.find(Workflow.class).delete(new DeleteOptions().multi(true));
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

    List<AbstractExecutablePluginMetadata> metisPluginsMetadata = workflow
        .getMetisPluginsMetadata();
    List<AbstractExecutablePluginMetadata> retrievedUserWorkflowMetisPluginsMetadata = retrievedWorkflow
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
    assertTrue(workflowDao.workflowExistsForDataset(workflow.getDatasetId()));
    assertFalse(workflowDao.workflowExistsForDataset(workflow.getDatasetId() + "X"));
  }

  @Test
  void getUserWorkflow() {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    workflowDao.create(workflow);
    assertNotNull(workflowDao.getWorkflow(workflow.getDatasetId()));
  }
}
