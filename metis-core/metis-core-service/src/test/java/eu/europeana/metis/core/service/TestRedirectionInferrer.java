package eu.europeana.metis.core.service;

import static eu.europeana.metis.core.service.TestRedirectionBase.getExecutablePluginTypes;
import static eu.europeana.metis.core.service.TestRedirectionBase.getTestDataset;
import static eu.europeana.metis.core.service.TestRedirectionBase.getWorkflowPostReindex;
import static eu.europeana.metis.core.service.TestRedirectionBase.getWorkflowPreReindex;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import eu.europeana.metis.core.dao.DataEvolutionUtils;
import eu.europeana.metis.core.dao.PluginWithExecutionId;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.ExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.PluginType;
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
class TestRedirectionInferrer {

  @Mock
  WorkflowExecutionDao workflowExecutionDao;

  @Mock
  DataEvolutionUtils dataEvolutionUtils;

  @InjectMocks
  RedirectionInferrer redirectionInferrer;

  @Test
  void shouldRedirectsBePerformed_whenRootAncestorDifferent_expectRedirect() {
    final Dataset dataset = getTestDataset();
    final WorkflowExecution workflowExecution = getWorkflowPostReindex(dataset);
    final PluginWithExecutionId<ExecutablePlugin> indexToPublishPluginWithExecutionId =
        new PluginWithExecutionId<>("executionId",
            (ExecutablePlugin) workflowExecution.getMetisPluginWithType(PluginType.PUBLISH).get());
    final PluginWithExecutionId<ExecutablePlugin> indexToPreviewPluginWithExecutionId2 =
        new PluginWithExecutionId<>("executionId2",
            (ExecutablePlugin) workflowExecution.getMetisPluginWithType(PluginType.PREVIEW).get());
    when(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(anyString(), any(), eq(Boolean.FALSE)))
        .thenReturn(indexToPublishPluginWithExecutionId);

    when(dataEvolutionUtils.getRootAncestor(any()))
        .thenReturn(indexToPublishPluginWithExecutionId)
        .thenReturn(indexToPreviewPluginWithExecutionId2);

    final PluginWithExecutionId<ExecutablePlugin> predecessor = new PluginWithExecutionId<>("executionId",
        (ExecutablePlugin) workflowExecution.getMetisPluginWithType(PluginType.PREVIEW).get());

    boolean redirectsToBePerformed = redirectionInferrer.shouldRedirectsBePerformed(dataset, predecessor,
        ExecutablePluginType.PREVIEW, new ArrayList<>());

    assertTrue(redirectsToBePerformed);
  }

  @Test
  void shouldRedirectsBePerformed_whenRootAncestorTheSame_expectNoRedirect() {
    final Dataset dataset = getTestDataset();
    final WorkflowExecution workflowExecution = getWorkflowPreReindex(dataset);

    final PluginWithExecutionId<ExecutablePlugin> indexToPublishPluginWithExecutionId =
        new PluginWithExecutionId<>("executionId",
            (ExecutablePlugin) workflowExecution.getMetisPluginWithType(PluginType.PUBLISH).get());
    when(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(anyString(), any(), eq(Boolean.FALSE)))
        .thenReturn(indexToPublishPluginWithExecutionId);

    when(dataEvolutionUtils.getRootAncestor(any()))
        .thenReturn(indexToPublishPluginWithExecutionId)
        .thenReturn(indexToPublishPluginWithExecutionId);

    final PluginWithExecutionId<ExecutablePlugin> predecessor = new PluginWithExecutionId<>("executionId",
        (ExecutablePlugin) workflowExecution.getMetisPluginWithType(PluginType.PREVIEW).get());

    boolean redirectsToBePerformed = redirectionInferrer.shouldRedirectsBePerformed(dataset, predecessor,
        ExecutablePluginType.PREVIEW, new ArrayList<>());

    assertFalse(redirectsToBePerformed);
  }

  @Test
  void shouldRedirectsBePerformed_whenLatestSuccessfulPluginButDatasetUpdate_expectRedirect() {
    final Dataset dataset = getTestDataset();
    dataset.setUpdatedDate(Date.from(Instant.now()));
    dataset.setDatasetIdsToRedirectFrom(List.of("258"));
    final WorkflowExecution workflowExecution = getWorkflowPostReindex(dataset);

    final PluginWithExecutionId<ExecutablePlugin> indexToPreviewPluginWithExecutionId =
        new PluginWithExecutionId<>("executionId",
            (ExecutablePlugin) workflowExecution.getMetisPluginWithType(PluginType.PREVIEW).get());
    when(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(anyString(), any(), eq(Boolean.FALSE)))
        .thenReturn(indexToPreviewPluginWithExecutionId);
    final PluginWithExecutionId<ExecutablePlugin> predecessor = new PluginWithExecutionId<>("executionId",
        (ExecutablePlugin) workflowExecution.getMetisPluginWithType(PluginType.PREVIEW).get());

    boolean redirectsToBePerformed = redirectionInferrer.shouldRedirectsBePerformed(dataset, predecessor,
        ExecutablePluginType.PREVIEW, new ArrayList<>());

    assertTrue(redirectsToBePerformed);
  }

  @Test
  void shouldRedirectsBePerformed_whenLatestSuccessfulPluginButNoDatasetUpdate_expectNoRedirect() {
    final Dataset dataset = getTestDataset();
    final WorkflowExecution workflowExecution = getWorkflowPostReindex(dataset);

    final PluginWithExecutionId<ExecutablePlugin> indexToPreviewPluginWithExecutionId =
        new PluginWithExecutionId<>("executionId",
            (ExecutablePlugin) workflowExecution.getMetisPluginWithType(PluginType.PREVIEW).get());
    when(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(anyString(), any(), eq(Boolean.FALSE)))
        .thenReturn(indexToPreviewPluginWithExecutionId);
    when(dataEvolutionUtils.getRootAncestor(any()))
        .thenReturn(indexToPreviewPluginWithExecutionId)
        .thenReturn(indexToPreviewPluginWithExecutionId);
    final PluginWithExecutionId<ExecutablePlugin> predecessor = new PluginWithExecutionId<>("executionId",
        (ExecutablePlugin) workflowExecution.getMetisPluginWithType(PluginType.PREVIEW).get());

    boolean redirectsToBePerformed = redirectionInferrer.shouldRedirectsBePerformed(dataset, predecessor,
        ExecutablePluginType.PREVIEW, new ArrayList<>());

    assertFalse(redirectsToBePerformed);
  }

  @Test
  void shouldRedirectsBePerformed_whenNoLatestSuccessfulPluginButDatasetRedirects_expectRedirect() {
    final Dataset dataset = getTestDataset();
    dataset.setDatasetIdsToRedirectFrom(List.of("258"));
    when(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(anyString(), any(), eq(Boolean.FALSE)))
        .thenReturn(null);
    boolean redirectsToBePerformed = redirectionInferrer.shouldRedirectsBePerformed(dataset, null,
        ExecutablePluginType.PREVIEW,
        new ArrayList<>());

    assertTrue(redirectsToBePerformed);
  }

  @Test
  void shouldRedirectsBePerformed_whenNoLatestSuccessfulPluginAndNoDatasetRedirects_expectNoRedirect() {
    final Dataset dataset = getTestDataset();
    when(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(anyString(), any(), eq(Boolean.FALSE)))
        .thenReturn(null);

    boolean redirectsToBePerformed = redirectionInferrer.shouldRedirectsBePerformed(dataset, null,
        ExecutablePluginType.PREVIEW,
        new ArrayList<>());

    assertFalse(redirectsToBePerformed);
  }

  @Test
  void shouldRedirectsBePerformed_whenTypesInWorkflowSameAsExecutablePluginType_expectNoRedirect() {
    final Dataset dataset = getTestDataset();
    when(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(anyString(), any(), eq(Boolean.FALSE)))
        .thenReturn(null);

    boolean redirectsToBePerformed = redirectionInferrer.shouldRedirectsBePerformed(dataset, null, ExecutablePluginType.PREVIEW,
        getExecutablePluginTypes());

    assertFalse(redirectsToBePerformed);
  }

  @Test
  void shouldRedirectsBePerformed_whenTypesInWorkflowLatestHarvestAndDatasetRedirect_expectRedirect() {
    final Dataset dataset = getTestDataset();
    dataset.setDatasetIdsToRedirectFrom(List.of("258"));
    when(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(anyString(), any(), eq(Boolean.FALSE)))
        .thenReturn(null);

    boolean redirectsToBePerformed = redirectionInferrer.shouldRedirectsBePerformed(dataset, null,
        ExecutablePluginType.HTTP_HARVEST,
        getExecutablePluginTypes());

    assertTrue(redirectsToBePerformed);
  }
}
