package eu.europeana.metis.core.service;

import static eu.europeana.metis.core.service.TestRedirectionBase.getExecutionProgress;
import static eu.europeana.metis.core.service.TestRedirectionBase.getIndexToPreviewPlugin;
import static eu.europeana.metis.core.service.TestRedirectionBase.getIndexToPreviewPluginMetadata;
import static eu.europeana.metis.core.service.TestRedirectionBase.getIndexToPublishPlugin;
import static eu.europeana.metis.core.service.TestRedirectionBase.getIndexToPublishPluginMetadata;
import static eu.europeana.metis.core.service.TestRedirectionBase.getTestDataset;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import eu.europeana.metis.core.dao.DataEvolutionUtils;
import eu.europeana.metis.core.dao.PluginWithExecutionId;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.workflow.plugins.ExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.IndexToPreviewPlugin;
import eu.europeana.metis.core.workflow.plugins.IndexToPreviewPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPublishPlugin;
import eu.europeana.metis.core.workflow.plugins.IndexToPublishPluginMetadata;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestRedirectionService {

  @Mock
  WorkflowExecutionDao workflowExecutionDao;

  @Mock
  DataEvolutionUtils dataEvolutionUtils;

  @InjectMocks
  RedirectionService redirectionService;

  @Test
  void shouldRedirectsBePerformed_whenRootAncestor_expectSuccess() {
    final Dataset dataset = getTestDataset();
    final IndexToPreviewPluginMetadata indexToPreviewPluginMetadata = getIndexToPreviewPluginMetadata();
    final IndexToPreviewPlugin indexToPreviewPlugin = getIndexToPreviewPlugin(indexToPreviewPluginMetadata,
        getExecutionProgress());
    final IndexToPublishPluginMetadata indexToPublishPluginMetadata = getIndexToPublishPluginMetadata(indexToPreviewPlugin);
    final IndexToPublishPlugin indexToPublishPlugin = getIndexToPublishPlugin(indexToPublishPluginMetadata,
        getExecutionProgress());
    final IndexToPublishPlugin indexToPreviewPlugin2 = getIndexToPublishPlugin(indexToPublishPluginMetadata,
        getExecutionProgress());
    final PluginWithExecutionId<ExecutablePlugin> indexToPublishPluginWithExecutionId =
        new PluginWithExecutionId<>("executionId", indexToPublishPlugin);
    final PluginWithExecutionId<ExecutablePlugin> indexToPreviewPluginWithExecutionId2 =
        new PluginWithExecutionId<>("executionId2", indexToPreviewPlugin2);
    when(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(anyString(), any(), eq(Boolean.FALSE)))
        .thenReturn(indexToPublishPluginWithExecutionId);

    when(dataEvolutionUtils.getRootAncestor(any()))
        .thenReturn(indexToPublishPluginWithExecutionId)
        .thenReturn(indexToPreviewPluginWithExecutionId2);

    final PluginWithExecutionId<ExecutablePlugin> predecessor = new PluginWithExecutionId<>("executionId", indexToPreviewPlugin);
    final List<ExecutablePluginType> typesInWorkflow = new ArrayList<>();

    boolean redirectsToBePerformed = redirectionService.shouldRedirectsBePerformed(dataset, predecessor, ExecutablePluginType.PREVIEW, typesInWorkflow);

    assertTrue(redirectsToBePerformed);
  }

  @Test
  void shouldRedirectsBePerformed_whenDatasetUpdate_expectSuccess() {
    final Dataset dataset = getTestDataset();
    dataset.setUpdatedDate(Date.from(Instant.now()));
    dataset.setDatasetIdsToRedirectFrom(List.of("258"));
    final IndexToPreviewPluginMetadata indexToPreviewPluginMetadata = getIndexToPreviewPluginMetadata();
    final IndexToPreviewPlugin indexToPreviewPlugin = getIndexToPreviewPlugin(indexToPreviewPluginMetadata,
        getExecutionProgress());

    final PluginWithExecutionId<ExecutablePlugin> predecessor = new PluginWithExecutionId<>("executionId", indexToPreviewPlugin);
    final List<ExecutablePluginType> typesInWorkflow = new ArrayList<>();

    boolean redirectsToBePerformed = redirectionService.shouldRedirectsBePerformed(dataset, predecessor, ExecutablePluginType.PREVIEW, typesInWorkflow);

    assertTrue(redirectsToBePerformed);
  }

  @Test
  void shouldRedirectsBePerformed_whenNoLatestSuccessfulPluginButDatasetRedirects_expectSuccess() {
    final Dataset dataset = getTestDataset();
    dataset.setUpdatedDate(Date.from(Instant.now()));
    dataset.setDatasetIdsToRedirectFrom(List.of("258"));

    final List<ExecutablePluginType> typesInWorkflow = new ArrayList<>();

    boolean redirectsToBePerformed = redirectionService.shouldRedirectsBePerformed(dataset, null, ExecutablePluginType.PREVIEW, typesInWorkflow);

    assertTrue(redirectsToBePerformed);
  }

}
