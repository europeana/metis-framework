package eu.europeana.metis.core.service;

import eu.europeana.cloud.common.model.dps.TaskState;
import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.core.common.Language;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.plugins.DataStatus;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginFactory;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.ExecutionProgress;
import eu.europeana.metis.core.workflow.plugins.HTTPHarvestPlugin;
import eu.europeana.metis.core.workflow.plugins.HTTPHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPreviewPlugin;
import eu.europeana.metis.core.workflow.plugins.IndexToPreviewPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPublishPlugin;
import eu.europeana.metis.core.workflow.plugins.IndexToPublishPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

public class TestRedirectionBase {

  @NotNull
  static Workflow getWorkflow(ObjectId objectId, IndexToPublishPluginMetadata indexToPublishPluginMetadata) {
    final Workflow workflow = new Workflow();
    workflow.setDatasetId("datasetId");
    workflow.setId(objectId);
    workflow.setMetisPluginsMetadata(List.of(indexToPublishPluginMetadata));
    return workflow;
  }

  @NotNull
  static IndexToPublishPluginMetadata getIndexToPublishPluginMetadata(IndexToPreviewPlugin indexToPreviewPlugin) {
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
  static HTTPHarvestPlugin getHttpHarvestPlugin(HTTPHarvestPluginMetadata httpHarvestPluginMetadata,
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
  static HTTPHarvestPluginMetadata getHttpHarvestPluginMetadata() {
    final HTTPHarvestPluginMetadata httpHarvestPluginMetadata = new HTTPHarvestPluginMetadata();
    httpHarvestPluginMetadata.setUrl("https://url.org");
    httpHarvestPluginMetadata.setUser("user");
    httpHarvestPluginMetadata.setIncrementalHarvest(false);
    httpHarvestPluginMetadata.setEnabled(true);
    return httpHarvestPluginMetadata;
  }

  @NotNull
  static Dataset getTestDataset() {
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

  @NotNull
  static IndexToPreviewPluginMetadata getIndexToPreviewPluginMetadata() {
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
  static ExecutionProgress getExecutionProgress() {
    final ExecutionProgress executionProgress = new ExecutionProgress();
    executionProgress.setStatus(TaskState.PROCESSED);
    executionProgress.setExpectedRecords(1);
    executionProgress.setProcessedRecords(1);
    executionProgress.setTotalDatabaseRecords(1);
    return executionProgress;
  }

  @NotNull
  static IndexToPreviewPlugin getIndexToPreviewPlugin(IndexToPreviewPluginMetadata indexToPreviewPluginMetadata,
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
  static IndexToPublishPlugin getIndexToPublishPlugin(IndexToPublishPluginMetadata indexToPublishPluginMetadata,
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
  static List<ExecutablePluginType> getExecutablePluginTypes() {
    final List<ExecutablePluginType> typesInWorkflow = new ArrayList<>();
    typesInWorkflow.add(ExecutablePluginType.HTTP_HARVEST);
    typesInWorkflow.add(ExecutablePluginType.VALIDATION_EXTERNAL);
    typesInWorkflow.add(ExecutablePluginType.TRANSFORMATION);
    typesInWorkflow.add(ExecutablePluginType.VALIDATION_INTERNAL);
    typesInWorkflow.add(ExecutablePluginType.NORMALIZATION);
    typesInWorkflow.add(ExecutablePluginType.ENRICHMENT);
    typesInWorkflow.add(ExecutablePluginType.MEDIA_PROCESS);
    typesInWorkflow.add(ExecutablePluginType.PREVIEW);
    typesInWorkflow.add(ExecutablePluginType.PUBLISH);
    return typesInWorkflow;
  }
}
