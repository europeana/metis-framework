package eu.europeana.metis.framework.test;

import eu.europeana.metis.framework.dao.ExecutionDao;
import eu.europeana.metis.framework.dao.FailedRecordsDao;
import eu.europeana.metis.framework.exceptions.NoDatasetFoundException;
import eu.europeana.metis.framework.mongo.MongoProvider;
import eu.europeana.metis.framework.service.DatasetService;
import eu.europeana.metis.framework.service.Orchestrator;
import eu.europeana.metis.framework.workflow.Execution;
import eu.europeana.metis.framework.workflow.FailedRecords;
import eu.europeana.metis.utils.NetworkUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mongodb.morphia.Morphia;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Created by ymamakis on 11/17/16.
 */
public class TestOrchestrator {
    private static ExecutionDao executionDao;
    private static DatasetService datasetService;
    private static FailedRecordsDao failedRecordsDao;
    private static Orchestrator orchestrator;
    private static eu.europeana.metis.mongo.MongoProvider mongoProvider;

    @BeforeClass
    public static void prepare() throws IOException, InterruptedException {
        int port = NetworkUtil.getAvailableLocalPort();
        mongoProvider = new eu.europeana.metis.mongo.MongoProvider();
        mongoProvider.start(port);
        MongoProvider provider = new MongoProvider("localhost",port, "test",null,null);
        Morphia morphia = new Morphia();
        executionDao = new ExecutionDao(provider.getDatastore().getMongo(),morphia,provider.getDatastore().getDB().getName());

        Morphia morphia2 = new Morphia();
        morphia.map(FailedRecords.class);
        failedRecordsDao =  new FailedRecordsDao(provider.getDatastore().getMongo(),morphia2,provider.getDatastore().getDB().getName());
        datasetService = Mockito.mock(DatasetService.class);
        orchestrator = new Orchestrator();
        ReflectionTestUtils.setField(orchestrator,"executionDao",executionDao);
        ReflectionTestUtils.setField(orchestrator,"datasetService",datasetService);
        ReflectionTestUtils.setField(orchestrator,"failedRecordsDao",failedRecordsDao);
    }

    @Test
    public void testCreationOfExecution() throws NoDatasetFoundException {
        Mockito.when(datasetService.exists("test")).thenReturn(true);
        Mockito.when(datasetService.exists("tests")).thenReturn(true);
        try {
            String id = orchestrator.execute("test", "void", null);
            Assert.assertNotNull(id);
            Execution execution = orchestrator.getExecution(id);
            Assert.assertNotNull(execution);
            Assert.assertTrue(StringUtils.equals("test",execution.getDatasetId()));
            Assert.assertNotNull(orchestrator.getExecutionsByDates(execution.getStartedAt(),new Date()));
            Assert.assertEquals(1,orchestrator.getExecutionsByDates(execution.getStartedAt(),new Date()));
            Assert.assertEquals(1,orchestrator.getActiveExecutions().size());
            Assert.assertEquals(execution,orchestrator.getActiveExecutions().get(0));
            Assert.assertEquals(1,orchestrator.getAllExecutions(0,1).size());
            Assert.assertEquals(execution,orchestrator.getAllExecutions(0,1).get(0));
            Assert.assertEquals(1,orchestrator.getAllExecutionsForDataset("test",0,1).size());
            Assert.assertEquals(0,orchestrator.getAllExecutionsForDataset("test1",0,1).size());
            Assert.assertEquals(0,orchestrator.getAllExecutionsForDatasetByDates("test1",0,1,execution.getStartedAt(),execution.getFinishedAt()).size());
            Assert.assertEquals(1,orchestrator.getAllExecutionsForDatasetByDates("test",0,1,execution.getStartedAt(),execution.getFinishedAt()).size());
            Assert.assertEquals(Collections.emptyList(),orchestrator.getFailedRecords(id,0,10));
            List<String> records = new ArrayList<>();
            records.add("test");
            FailedRecords failedRecords = new FailedRecords();
            failedRecords.setExecutionId(id);
            failedRecords.setRecords(records);
            failedRecordsDao.save(failedRecords);
            Assert.assertNotEquals(Collections.emptyList(),orchestrator.getFailedRecords(id,0,10));
            Assert.assertEquals(1,orchestrator.getFailedRecords(id,0,10).size());
            String id2 = orchestrator.schedule("tests","void",null,100);
            orchestrator.executeScheduled();
            Assert.assertNotNull(orchestrator.getExecution(id2).getStartedAt());
            Assert.assertTrue(orchestrator.getAvailableWorkflows().contains("void"));
            Assert.assertFalse(orchestrator.getAvailableWorkflows().contains("test"));
        } catch (Exception e){
            //DO NOTHING
        }
    }

    @AfterClass
    public static void after()
    {
        mongoProvider.stop();
    }
}
