package eu.europeana.metis.core.service;

import eu.europeana.cloud.common.model.dps.TaskState;
import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.core.common.Language;
import eu.europeana.metis.core.dao.DataEvolutionUtils;
import eu.europeana.metis.core.dao.DatasetXsltDao;
import eu.europeana.metis.core.dao.DepublishRecordIdDao;
import eu.europeana.metis.core.dao.PluginWithExecutionId;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.DataStatus;
import eu.europeana.metis.core.workflow.plugins.ExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginFactory;
import eu.europeana.metis.core.workflow.plugins.ExecutionProgress;
import eu.europeana.metis.core.workflow.plugins.IndexToPreviewPlugin;
import eu.europeana.metis.core.workflow.plugins.IndexToPreviewPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPublishPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.exception.BadContentException;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedirectionTest {

    @Mock
    private DatasetXsltDao datasetXsltDao;

    @Mock
    private DepublishRecordIdDao depublishRecordIdDao;

    @Mock
    private WorkflowExecutionDao workflowExecutionDao;

    @Mock
    private DataEvolutionUtils dataEvolutionUtils;

    @InjectMocks
    private WorkflowExecutionFactory workflowExecutionFactory;

    @Test
    void redirectionReview() throws BadContentException {
        int priority = 0;
        IndexToPublishPluginMetadata indexToPublishPluginMetadata = new IndexToPublishPluginMetadata();
        indexToPublishPluginMetadata.setIncrementalIndexing(false);
        indexToPublishPluginMetadata.setHarvestDate(Date.from(Instant.now()));
        indexToPublishPluginMetadata.setPreserveTimestamps(false);
        indexToPublishPluginMetadata.setEnabled(true);
        indexToPublishPluginMetadata.setDatasetIdsToRedirectFrom(List.of());
        // IndexToPublishPlugin indexToPublishPlugin = (IndexToPublishPlugin) ExecutablePluginFactory.createPlugin(indexToPublishPluginMetadata);

        IndexToPreviewPluginMetadata indexToPreviewPluginMetadata = new IndexToPreviewPluginMetadata();
        indexToPreviewPluginMetadata.setIncrementalIndexing(false);
        indexToPreviewPluginMetadata.setHarvestDate(Date.from(Instant.now().minus(100, ChronoUnit.MINUTES)));
        indexToPreviewPluginMetadata.setPreserveTimestamps(false);
        indexToPreviewPluginMetadata.setEnabled(true);
        indexToPreviewPluginMetadata.setDatasetIdsToRedirectFrom(List.of());

        IndexToPreviewPlugin indexToPreviewPlugin = (IndexToPreviewPlugin) ExecutablePluginFactory.createPlugin(indexToPreviewPluginMetadata);
        ExecutionProgress executionProgress = new ExecutionProgress();
        executionProgress.setStatus(TaskState.PROCESSED);
        executionProgress.setExpectedRecords(1);
        executionProgress.setProcessedRecords(1);
        executionProgress.setTotalDatabaseRecords(1);
        indexToPreviewPlugin.setExecutionProgress(executionProgress);
        indexToPreviewPlugin.setPluginStatus(PluginStatus.FINISHED);
        indexToPreviewPlugin.setStartedDate(Date.from(Instant.now().minus(90, ChronoUnit.MINUTES)));
        indexToPreviewPlugin.setFinishedDate(Date.from(Instant.now().minus(60, ChronoUnit.MINUTES)));
        indexToPreviewPlugin.setDataStatus(DataStatus.VALID);

        PluginWithExecutionId<ExecutablePlugin> indexToPreviewPluginPluginWithExecutionId = new PluginWithExecutionId<>("executionId", indexToPreviewPlugin);

        indexToPreviewPluginPluginWithExecutionId.getPlugin().getFinishedDate();

        when(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(anyString(), any(), anyBoolean()))
                .thenReturn(indexToPreviewPluginPluginWithExecutionId);
        when(dataEvolutionUtils.getRootAncestor(any()))
                .thenReturn(indexToPreviewPluginPluginWithExecutionId);

        final String datasetId = "datasetId";
        final ObjectId objectId = new ObjectId();

        Workflow workflow = new Workflow();
        workflow.setDatasetId(datasetId);
        workflow.setId(objectId);
        workflow.setMetisPluginsMetadata(List.of(indexToPublishPluginMetadata));

        Dataset dataset = new Dataset();
        dataset.setDatasetId(datasetId);
        dataset.setCountry(Country.NETHERLANDS);
        dataset.setLanguage(Language.NL);
        dataset.setDatasetIdsToRedirectFrom(List.of());
        dataset.setOrganizationName("Organization");
        dataset.setCreatedByUserId("userId");
        dataset.setCreatedDate(Date.from(Instant.now().minus(120, ChronoUnit.MINUTES)));
        dataset.setUpdatedDate(Date.from(Instant.now()));

        PluginWithExecutionId<ExecutablePlugin> predecessor = new PluginWithExecutionId<>("executionId", indexToPreviewPlugin);

        WorkflowExecution workflowExecution = workflowExecutionFactory.createWorkflowExecution(workflow, dataset, predecessor, priority);

        // add assertion
    }


}
