package eu.europeana.metis.core.test;

import eu.europeana.metis.core.service.DatasetService;
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class TestOrchestratorService {
  private static DatasetService datasetService;
  private static OrchestratorService orchestratorService;
  private static EmbeddedLocalhostMongo embeddedLocalhostMongo;

  @BeforeClass
  public static void prepare() throws IOException, InterruptedException {
//    embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
//    embeddedLocalhostMongo.start();
//    String mongoHost = embeddedLocalhostMongo.getMongoHost();
//    int mongoPort = embeddedLocalhostMongo.getMongoPort();
//    ServerAddress address = new ServerAddress(mongoHost, mongoPort);
//    MongoClient mongoClient = new MongoClient(address);
//    MorphiaDatastoreProvider provider = new MorphiaDatastoreProvider(mongoClient, "test");
//    Morphia morphia = new Morphia();
//    executionDao = new ExecutionDao(provider.getDatastore().getMongo(), morphia,
//        provider.getDatastore().getDB().getName());
//
//    Morphia morphia2 = new Morphia();
//    morphia.map(FailedRecords.class);
//    failedRecordsDao = new FailedRecordsDao(provider.getDatastore().getMongo(), morphia2,
//        provider.getDatastore().getDB().getName());
//    datasetService = Mockito.mock(DatasetService.class);
//    PluginRegistry<AbstractMetisPlugin, String> plugins = new SimplePluginRegistry<>();
//    orchestratorService = new OrchestratorService(executionDao, datasetService, failedRecordsDao, );
//    ReflectionTestUtils.setField(orchestratorService, "executionDao", executionDao);
//    ReflectionTestUtils.setField(orchestratorService, "datasetService", datasetService);
//    ReflectionTestUtils.setField(orchestratorService, "failedRecordsDao", failedRecordsDao);
  }

//  @Test
//  public void testCreationOfExecution() throws NoDatasetFoundException {
//    Mockito.when(datasetService.existsDatasetByDatasetName("test")).thenReturn(true);
//    Mockito.when(datasetService.existsDatasetByDatasetName("tests")).thenReturn(true);
//    try {
//      String id = orchestratorService.execute("test", "void", null, null);
//      Assert.assertNotNull(id);
//      Execution execution = orchestratorService.getExecution(id);
//      Assert.assertNotNull(execution);
//      Assert.assertTrue(StringUtils.equals("test", execution.getDatasetId()));
//      Assert.assertNotNull(
//          orchestratorService.getExecutionsByDates(execution.getStartedAt(), new Date(), null));
//      Assert.assertEquals(1,
//          orchestratorService.getExecutionsByDates(execution.getStartedAt(), new Date(), null));
//      Assert.assertEquals(1, orchestratorService.getActiveExecutions(null).size());
//      Assert.assertEquals(execution, orchestratorService.getActiveExecutions(null).get(0));
//      Assert.assertEquals(1, orchestratorService.getAllExecutions(0, 1, null).size());
//      Assert.assertEquals(execution, orchestratorService.getAllExecutions(0, 1, null).get(0));
//      Assert.assertEquals(1, orchestratorService.getAllExecutionsForDataset("test", 0, 1, null).size());
//      Assert.assertEquals(0, orchestratorService.getAllExecutionsForDataset("test1", 0, 1, null).size());
//      Assert.assertEquals(0, orchestratorService
//          .getAllExecutionsForDatasetByDates("test1", 0, 1, execution.getStartedAt(),
//              execution.getFinishedAt(), null).size());
//      Assert.assertEquals(1, orchestratorService
//          .getAllExecutionsForDatasetByDates("test", 0, 1, execution.getStartedAt(),
//              execution.getFinishedAt(), null).size());
//      Assert.assertEquals(Collections.emptyList(), orchestratorService.getFailedRecords(id, 0, 10));
//      List<String> records = new ArrayList<>();
//      records.add("test");
//      FailedRecords failedRecords = new FailedRecords();
//      failedRecords.setExecutionId(id);
//      failedRecords.setRecords(records);
//      failedRecordsDao.save(failedRecords);
//      Assert.assertNotEquals(Collections.emptyList(), orchestratorService.getFailedRecords(id, 0, 10));
//      Assert.assertEquals(1, orchestratorService.getFailedRecords(id, 0, 10).size());
//      String id2 = orchestratorService.schedule("tests", "void", null, null, 100);
//      orchestratorService.executeScheduled();
//      Assert.assertNotNull(orchestratorService.getExecution(id2).getStartedAt());
//      Assert.assertTrue(orchestratorService.getAllOrchestratorPlugins().contains("void"));
//      Assert.assertFalse(orchestratorService.getAllOrchestratorPlugins().contains("test"));
//    } catch (Exception e) {
//      //DO NOTHING
//    }
//  }

  @AfterClass
  public static void after() {
    embeddedLocalhostMongo.stop();
  }
}
