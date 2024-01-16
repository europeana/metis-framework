package eu.europeana.metis.core.service;

import static eu.europeana.metis.core.service.TestRedirectionBase.getExecutionProgress;
import static eu.europeana.metis.core.service.TestRedirectionBase.getHttpHarvestPlugin;
import static eu.europeana.metis.core.service.TestRedirectionBase.getHttpHarvestPluginMetadata;
import static eu.europeana.metis.core.service.TestRedirectionBase.getIndexToPreviewPlugin;
import static eu.europeana.metis.core.service.TestRedirectionBase.getIndexToPreviewPluginMetadata;
import static eu.europeana.metis.core.service.TestRedirectionBase.getIndexToPublishPlugin;
import static eu.europeana.metis.core.service.TestRedirectionBase.getIndexToPublishPluginMetadata;
import static eu.europeana.metis.core.service.TestRedirectionBase.getTestDataset;
import static eu.europeana.metis.core.service.TestRedirectionBase.getWorkflow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import eu.europeana.metis.core.dao.DataEvolutionUtils;
import eu.europeana.metis.core.dao.DatasetXsltDao;
import eu.europeana.metis.core.dao.DepublishRecordIdDao;
import eu.europeana.metis.core.dao.PluginWithExecutionId;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.ExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.HTTPHarvestPlugin;
import eu.europeana.metis.core.workflow.plugins.HTTPHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPreviewPlugin;
import eu.europeana.metis.core.workflow.plugins.IndexToPreviewPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPublishPlugin;
import eu.europeana.metis.core.workflow.plugins.IndexToPublishPluginMetadata;
import eu.europeana.metis.exception.BadContentException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestRedirection {

  private WorkflowExecutionDao workflowExecutionDao;
  private DataEvolutionUtils dataEvolutionUtils;
  private WorkflowExecutionFactory workflowExecutionFactory;

  @BeforeEach
  void setup() {
    workflowExecutionDao = mock(WorkflowExecutionDao.class);
    dataEvolutionUtils = mock(DataEvolutionUtils.class);
    DatasetXsltDao datasetXsltDao = mock(DatasetXsltDao.class);
    DepublishRecordIdDao depublishRecordIdDao = mock(DepublishRecordIdDao.class);
    RedirectionInferrer redirectionInferrer = new RedirectionInferrer(workflowExecutionDao, dataEvolutionUtils);

    workflowExecutionFactory = new WorkflowExecutionFactory(datasetXsltDao, depublishRecordIdDao, redirectionInferrer);
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
    final IndexToPreviewPlugin indexToPreviewPlugin = getIndexToPreviewPlugin(indexToPreviewPluginMetadata,
        getExecutionProgress());

    final IndexToPublishPluginMetadata indexToPublishPluginMetadata = getIndexToPublishPluginMetadata(indexToPreviewPlugin);
    final IndexToPublishPlugin indexToPublishPlugin = getIndexToPublishPlugin(indexToPublishPluginMetadata,
        getExecutionProgress());

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

    final WorkflowExecution workflowExecution = workflowExecutionFactory.createWorkflowExecution(workflow, dataset, predecessor,
        priority);

    final AbstractMetisPlugin<IndexToPublishPluginMetadata> abstractMetisPlugin = workflowExecution.getMetisPlugins().stream()
                                                                                                   .findFirst().get();
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
    final IndexToPreviewPlugin indexToPreviewPlugin = getIndexToPreviewPlugin(indexToPreviewPluginMetadata,
        getExecutionProgress());

    final IndexToPublishPluginMetadata indexToPublishPluginMetadata = getIndexToPublishPluginMetadata(indexToPreviewPlugin);
    final IndexToPublishPlugin indexToPublishPlugin = getIndexToPublishPlugin(indexToPublishPluginMetadata,
        getExecutionProgress());

    final PluginWithExecutionId<ExecutablePlugin> indexToPublishPluginPluginWithExecutionId =
        new PluginWithExecutionId<>("executionId", indexToPublishPlugin);

    when(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(anyString(), any(), anyBoolean()))
        .thenReturn(indexToPublishPluginPluginWithExecutionId);

    final ObjectId objectId = new ObjectId();
    final Workflow workflow = getWorkflow(objectId, getIndexToPublishPluginMetadata(indexToPreviewPlugin));
    final Dataset dataset = getTestDataset();
    dataset.setDatasetIdsToRedirectFrom(List.of("253"));
    final PluginWithExecutionId<ExecutablePlugin> predecessor = new PluginWithExecutionId<>("executionId", indexToPreviewPlugin);

    final WorkflowExecution workflowExecution = workflowExecutionFactory.createWorkflowExecution(workflow, dataset, predecessor,
        priority);

    final AbstractMetisPlugin<IndexToPublishPluginMetadata> abstractMetisPlugin = workflowExecution.getMetisPlugins().stream()
                                                                                                   .findFirst().get();
    assertTrue(abstractMetisPlugin.getPluginMetadata().isPerformRedirects());
  }
}
