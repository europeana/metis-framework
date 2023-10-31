package eu.europeana.metis.core.service;

import eu.europeana.cloud.common.model.dps.TaskState;
import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.core.common.Language;
import eu.europeana.metis.core.dao.DataEvolutionUtils;
import eu.europeana.metis.core.dao.PluginWithExecutionId;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.DataStatus;
import eu.europeana.metis.core.workflow.plugins.ExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginFactory;
import eu.europeana.metis.core.workflow.plugins.ExecutionProgress;
import eu.europeana.metis.core.workflow.plugins.HTTPHarvestPlugin;
import eu.europeana.metis.core.workflow.plugins.HTTPHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPreviewPlugin;
import eu.europeana.metis.core.workflow.plugins.IndexToPreviewPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPublishPlugin;
import eu.europeana.metis.core.workflow.plugins.IndexToPublishPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.exception.BadContentException;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestRedirection {

    @Mock
    private WorkflowExecutionDao workflowExecutionDao;

    @Mock
    private DataEvolutionUtils dataEvolutionUtils;

    @InjectMocks
    private WorkflowExecutionFactory workflowExecutionFactory;

    @NotNull
    private static Workflow getWorkflow(ObjectId objectId, IndexToPublishPluginMetadata indexToPublishPluginMetadata) {
        final Workflow workflow = new Workflow();
        workflow.setDatasetId("datasetId");
        workflow.setId(objectId);
        workflow.setMetisPluginsMetadata(List.of(indexToPublishPluginMetadata));
        return workflow;
    }

    @NotNull
    private static IndexToPublishPluginMetadata getIndexToPublishPluginMetadata(IndexToPreviewPlugin indexToPreviewPlugin) {
        final IndexToPublishPluginMetadata indexToPublishPluginMetadata = new IndexToPublishPluginMetadata();
        indexToPublishPluginMetadata.setIncrementalIndexing(false);
        indexToPublishPluginMetadata.setHarvestDate(Date.from(Instant.now()));
        indexToPublishPluginMetadata.setPreserveTimestamps(false);
        indexToPublishPluginMetadata.setEnabled(true);
        indexToPublishPluginMetadata.setDatasetIdsToRedirectFrom(List.of());
        indexToPublishPluginMetadata.setPerformRedirects(true);
        indexToPublishPluginMetadata.setPreviousRevisionInformation(indexToPreviewPlugin);
        indexToPublishPluginMetadata.setRevisionNamePreviousPlugin("revisionName");
        return indexToPublishPluginMetadata;
    }

    @NotNull
    private static HTTPHarvestPlugin getHttpHarvestPlugin(HTTPHarvestPluginMetadata httpHarvestPluginMetadata,
                                                          Date startDate,
                                                          Date finishDate,
                                                          ExecutionProgress executionProgress) {
        final HTTPHarvestPlugin httpHarvestPlugin = (HTTPHarvestPlugin)
                ExecutablePluginFactory.createPlugin(httpHarvestPluginMetadata);
        httpHarvestPlugin.setExecutionProgress(executionProgress);
        httpHarvestPlugin.setPluginStatus(PluginStatus.FINISHED);
        httpHarvestPlugin.setStartedDate(startDate);
        httpHarvestPlugin.setFinishedDate(finishDate);
        httpHarvestPlugin.setDataStatus(DataStatus.VALID);
        return httpHarvestPlugin;
    }

    @NotNull
    private static IndexToPreviewPlugin getIndexToPreviewPlugin(IndexToPreviewPluginMetadata indexToPreviewPluginMetadata,
                                                                ExecutionProgress executionProgress) {
        final IndexToPreviewPlugin indexToPreviewPlugin = (IndexToPreviewPlugin)
                ExecutablePluginFactory.createPlugin(indexToPreviewPluginMetadata);
        indexToPreviewPlugin.setExecutionProgress(executionProgress);
        indexToPreviewPlugin.setPluginStatus(PluginStatus.FINISHED);
        indexToPreviewPlugin.setStartedDate(Date.from(Instant.now().minus(90, ChronoUnit.MINUTES)));
        indexToPreviewPlugin.setFinishedDate(Date.from(Instant.now().minus(60, ChronoUnit.MINUTES)));
        indexToPreviewPlugin.setDataStatus(DataStatus.VALID);
        return indexToPreviewPlugin;
    }

    @NotNull
    private static IndexToPublishPlugin getIndexToPublishPlugin(IndexToPublishPluginMetadata indexToPublishPluginMetadata,
                                                                ExecutionProgress executionProgress) {
        final IndexToPublishPlugin indexToPublishPlugin = (IndexToPublishPlugin)
                ExecutablePluginFactory.createPlugin(indexToPublishPluginMetadata);
        indexToPublishPlugin.setExecutionProgress(executionProgress);
        indexToPublishPlugin.setPluginStatus(PluginStatus.FINISHED);
        indexToPublishPlugin.setStartedDate(Date.from(Instant.now().minus(45, ChronoUnit.MINUTES)));
        indexToPublishPlugin.setFinishedDate(Date.from(Instant.now().minus(30, ChronoUnit.MINUTES)));
        indexToPublishPlugin.setDataStatus(DataStatus.VALID);
        return indexToPublishPlugin;
    }

    @NotNull
    private static ExecutionProgress getExecutionProgress() {
        final ExecutionProgress executionProgress = new ExecutionProgress();
        executionProgress.setStatus(TaskState.PROCESSED);
        executionProgress.setExpectedRecords(1);
        executionProgress.setProcessedRecords(1);
        executionProgress.setTotalDatabaseRecords(1);
        return executionProgress;
    }

    @NotNull
    private static HTTPHarvestPluginMetadata getHttpHarvestPluginMetadata() {
        final HTTPHarvestPluginMetadata httpHarvestPluginMetadata = new HTTPHarvestPluginMetadata();
        httpHarvestPluginMetadata.setUrl("https://url.org");
        httpHarvestPluginMetadata.setUser("user");
        httpHarvestPluginMetadata.setIncrementalHarvest(false);
        httpHarvestPluginMetadata.setEnabled(true);
        return httpHarvestPluginMetadata;
    }

    @NotNull
    private static IndexToPreviewPluginMetadata getIndexToPreviewPluginMetadata() {
        final IndexToPreviewPluginMetadata indexToPreviewPluginMetadata = new IndexToPreviewPluginMetadata();
        indexToPreviewPluginMetadata.setIncrementalIndexing(false);
        indexToPreviewPluginMetadata.setHarvestDate(Date.from(Instant.now().minus(100, ChronoUnit.MINUTES)));
        indexToPreviewPluginMetadata.setPreserveTimestamps(false);
        indexToPreviewPluginMetadata.setEnabled(true);
        indexToPreviewPluginMetadata.setDatasetIdsToRedirectFrom(List.of());
        indexToPreviewPluginMetadata.setPerformRedirects(true);
        return indexToPreviewPluginMetadata;
    }

    @NotNull
    private static Dataset getTestDataset() {
        final Dataset dataset = new Dataset();
        dataset.setDatasetId("datasetId");
        dataset.setCountry(Country.GERMANY);
        dataset.setDescription("");
        dataset.setOrganizationId("1482250000001617026");
        dataset.setCreatedByUserId("1482250000016772002");
        dataset.setLanguage(Language.MUL);
        dataset.setDatasetIdsToRedirectFrom(List.of());
        dataset.setOrganizationName("Europeana Foundation");
        dataset.setCreatedByUserId("userId");
        dataset.setCreatedDate(Date.from(Instant.now().minus(120, ChronoUnit.MINUTES)));
        dataset.setUpdatedDate(Date.from(Instant.now()));
        dataset.setReplacedBy("");
        dataset.setDataProvider("Kunsthochschule Kassel");
        dataset.setProvider("EFG");
        dataset.setIntermediateProvider("");
        dataset.setNotes("");
        dataset.setEcloudDatasetId("377ac607-f729-483d-a86d-2c005150c46d");
        return dataset;
    }

    @Test
    void redirectionReviewWithPerformRedirectsWhenAncestorRootIsDifferent() throws BadContentException {
        final int priority = 0;
        final HTTPHarvestPluginMetadata httpHarvestPluginMetadata = getHttpHarvestPluginMetadata();
        final HTTPHarvestPlugin httpHarvestPlugin = getHttpHarvestPlugin(httpHarvestPluginMetadata,
                Date.from(Instant.now().minus(42, ChronoUnit.MINUTES)),
                Date.from(Instant.now().minus(28, ChronoUnit.MINUTES)),
                getExecutionProgress());
        final PluginWithExecutionId<ExecutablePlugin> httpHarvestPluginWithExecutionId =
                new PluginWithExecutionId<>("executionId", httpHarvestPlugin);

        final HTTPHarvestPluginMetadata httpHarvestPluginMetadata2 = getHttpHarvestPluginMetadata();
        final HTTPHarvestPlugin httpHarvestPlugin2 = getHttpHarvestPlugin(httpHarvestPluginMetadata2,
                Date.from(Instant.now().minus(45, ChronoUnit.MINUTES)),
                Date.from(Instant.now().minus(30, ChronoUnit.MINUTES)),
                getExecutionProgress());
        final PluginWithExecutionId<ExecutablePlugin> httpHarvestPluginWithExecutionId2 =
                new PluginWithExecutionId<>("executionId2", httpHarvestPlugin2);

        final IndexToPreviewPluginMetadata indexToPreviewPluginMetadata = getIndexToPreviewPluginMetadata();
        final IndexToPreviewPlugin indexToPreviewPlugin = getIndexToPreviewPlugin(indexToPreviewPluginMetadata, getExecutionProgress());

        final IndexToPublishPluginMetadata indexToPublishPluginMetadata = getIndexToPublishPluginMetadata(indexToPreviewPlugin);
        final IndexToPublishPlugin indexToPublishPlugin = getIndexToPublishPlugin(indexToPublishPluginMetadata, getExecutionProgress());

        final PluginWithExecutionId<ExecutablePlugin> indexToPublishPluginWithExecutionId =
                new PluginWithExecutionId<>("executionId", indexToPublishPlugin);
        when(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(anyString(), any(), anyBoolean()))
                .thenReturn(indexToPublishPluginWithExecutionId);

        when(dataEvolutionUtils.getRootAncestor(any()))
                .thenReturn(httpHarvestPluginWithExecutionId)
                .thenReturn(httpHarvestPluginWithExecutionId2);

        final ObjectId objectId = new ObjectId();
        final Workflow workflow = getWorkflow(objectId, getIndexToPublishPluginMetadata(indexToPreviewPlugin));
        final Dataset dataset = getTestDataset();
        final PluginWithExecutionId<ExecutablePlugin> predecessor = new PluginWithExecutionId<>("executionId", indexToPreviewPlugin);

        final WorkflowExecution workflowExecution = workflowExecutionFactory.createWorkflowExecution(workflow, dataset, predecessor, priority);

        final AbstractMetisPlugin<IndexToPublishPluginMetadata> abstractMetisPlugin = workflowExecution.getMetisPlugins().stream().findFirst().get();
        assertTrue(abstractMetisPlugin.getPluginMetadata().isPerformRedirects());
    }

    @Test
    void redirectionReviewWithPerformRedirectsWhenRedirectIdsPresent() throws BadContentException {
        final int priority = 0;
        final HTTPHarvestPluginMetadata httpHarvestPluginMetadata = getHttpHarvestPluginMetadata();
        final HTTPHarvestPlugin httpHarvestPlugin = getHttpHarvestPlugin(httpHarvestPluginMetadata,
                Date.from(Instant.now().minus(42, ChronoUnit.MINUTES)),
                Date.from(Instant.now().minus(28, ChronoUnit.MINUTES)),
                getExecutionProgress());
        final PluginWithExecutionId<ExecutablePlugin> httpHarvestPluginWithExecutionId =
                new PluginWithExecutionId<>("executionId", httpHarvestPlugin);

        final IndexToPreviewPluginMetadata indexToPreviewPluginMetadata = getIndexToPreviewPluginMetadata();
        final IndexToPreviewPlugin indexToPreviewPlugin = getIndexToPreviewPlugin(indexToPreviewPluginMetadata, getExecutionProgress());

        final IndexToPublishPluginMetadata indexToPublishPluginMetadata = getIndexToPublishPluginMetadata(indexToPreviewPlugin);
        final IndexToPublishPlugin indexToPublishPlugin = getIndexToPublishPlugin(indexToPublishPluginMetadata, getExecutionProgress());

        final PluginWithExecutionId<ExecutablePlugin> indexToPublishPluginPluginWithExecutionId =
                new PluginWithExecutionId<>("executionId", indexToPublishPlugin);

        when(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(anyString(), any(), anyBoolean()))
                .thenReturn(indexToPublishPluginPluginWithExecutionId);

        final ObjectId objectId = new ObjectId();
        final Workflow workflow = getWorkflow(objectId, getIndexToPublishPluginMetadata(indexToPreviewPlugin));
        final Dataset dataset = getTestDataset();
        dataset.setDatasetIdsToRedirectFrom(List.of("253"));
        final PluginWithExecutionId<ExecutablePlugin> predecessor = new PluginWithExecutionId<>("executionId", indexToPreviewPlugin);

        final WorkflowExecution workflowExecution = workflowExecutionFactory.createWorkflowExecution(workflow, dataset, predecessor, priority);

        final AbstractMetisPlugin<IndexToPublishPluginMetadata> abstractMetisPlugin = workflowExecution.getMetisPlugins().stream().findFirst().get();
        assertTrue(abstractMetisPlugin.getPluginMetadata().isPerformRedirects());
    }
}
