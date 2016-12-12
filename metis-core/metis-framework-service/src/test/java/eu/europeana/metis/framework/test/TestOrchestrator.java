package eu.europeana.metis.framework.test;

import eu.europeana.metis.framework.dao.ExecutionDao;
import eu.europeana.metis.framework.dao.FailedRecordsDao;
import eu.europeana.metis.framework.service.DatasetService;
import eu.europeana.metis.framework.service.Orchestrator;
import eu.europeana.metis.framework.workflow.Execution;
import eu.europeana.metis.framework.workflow.FailedRecords;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by ymamakis on 11/17/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestAppConfig.class})
public class TestOrchestrator {

    @Autowired
    private ExecutionDao executionDao;
    @Autowired
    private DatasetService datasetService;
    @Autowired
    private FailedRecordsDao failedRecordsDao;
    @Autowired
    private OrderAwarePluginRegistry registry;
    @Autowired
    private Orchestrator orchestrator;

    @Test
    public void testCreationOfExecution(){
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
}
