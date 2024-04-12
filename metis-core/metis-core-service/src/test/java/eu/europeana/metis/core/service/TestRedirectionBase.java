package eu.europeana.metis.core.service;

import eu.europeana.cloud.common.model.dps.TaskState;
import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.core.common.Language;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.Dataset.PublicationFitness;
import eu.europeana.metis.core.workflow.ValidationProperties;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.DataStatus;
import eu.europeana.metis.core.workflow.plugins.EnrichmentPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginFactory;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.ExecutionProgress;
import eu.europeana.metis.core.workflow.plugins.HTTPHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPreviewPlugin;
import eu.europeana.metis.core.workflow.plugins.IndexToPreviewPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPublishPlugin;
import eu.europeana.metis.core.workflow.plugins.IndexToPublishPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.MediaProcessPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.NormalizationPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.core.workflow.plugins.ReindexToPreviewPlugin;
import eu.europeana.metis.core.workflow.plugins.ReindexToPreviewPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ReindexToPublishPlugin;
import eu.europeana.metis.core.workflow.plugins.ReindexToPublishPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ThrottlingLevel;
import eu.europeana.metis.core.workflow.plugins.TransformationPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ValidationExternalPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ValidationInternalPluginMetadata;
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
  static HTTPHarvestPluginMetadata getHttpHarvestPluginMetadata() {
    final HTTPHarvestPluginMetadata httpHarvestPluginMetadata = new HTTPHarvestPluginMetadata();
    httpHarvestPluginMetadata.setUrl("https://url.org");
    httpHarvestPluginMetadata.setUser("user");
    httpHarvestPluginMetadata.setIncrementalHarvest(false);
    httpHarvestPluginMetadata.setEnabled(true);
    return httpHarvestPluginMetadata;
  }

  @NotNull
  static ValidationExternalPluginMetadata getValidationExternalPluginMetadata(String revisionNamePreviousPlugin,
      Date revisionTimeStampPreviousPlugin) {
    final ValidationExternalPluginMetadata validationExternalPluginMetadata = new ValidationExternalPluginMetadata();
    validationExternalPluginMetadata.setEnabled(true);
    validationExternalPluginMetadata.setRevisionNamePreviousPlugin(revisionNamePreviousPlugin);
    validationExternalPluginMetadata.setRevisionTimestampPreviousPlugin(revisionTimeStampPreviousPlugin);
    validationExternalPluginMetadata.setUrlOfSchemasZip("http://ftp.eanadev.org/schema_zips/europeana_schemas-20220809.zip");
    validationExternalPluginMetadata.setSchemaRootPath("EDM.xsd");
    validationExternalPluginMetadata.setSchematronRootPath("schematron/schematron.xsl");
    return validationExternalPluginMetadata;
  }

  @NotNull
  static TransformationPluginMetadata getTransformationPluginMetadata(Dataset dataset, String revisionNamePreviousPlugin,
      Date revisionTimeStampPreviousPlugin) {
    final TransformationPluginMetadata transformationPluginMetadata = new TransformationPluginMetadata();
    transformationPluginMetadata.setEnabled(true);
    transformationPluginMetadata.setCustomXslt(false);
    transformationPluginMetadata.setCountry("Netherlands");
    transformationPluginMetadata.setLanguage("nl");
    transformationPluginMetadata.setDatasetName(dataset.getDatasetName());
    transformationPluginMetadata.setRevisionNamePreviousPlugin(revisionNamePreviousPlugin);
    transformationPluginMetadata.setRevisionTimestampPreviousPlugin(revisionTimeStampPreviousPlugin);
    return transformationPluginMetadata;
  }

  @NotNull
  static ValidationInternalPluginMetadata getValidationInternalPluginMetadata(String revisionNamePreviousPlugin,
      Date revisionTimeStampPreviousPlugin) {
    final ValidationInternalPluginMetadata validationInternalPluginMetadata = new ValidationInternalPluginMetadata();
    validationInternalPluginMetadata.setEnabled(true);
    validationInternalPluginMetadata.setUrlOfSchemasZip("http://ftp.eanadev.org/schema_zips/europeana_schemas-20220809.zip");
    validationInternalPluginMetadata.setSchemaRootPath("EDM-INTERNAL.xsd");
    validationInternalPluginMetadata.setSchematronRootPath("schematron/schematron-internal.xsl");
    validationInternalPluginMetadata.setRevisionNamePreviousPlugin(revisionNamePreviousPlugin);
    validationInternalPluginMetadata.setRevisionTimestampPreviousPlugin(revisionTimeStampPreviousPlugin);
    return validationInternalPluginMetadata;
  }

  @NotNull
  static NormalizationPluginMetadata getNormalizationPluginMetadata(String revisionNamePreviousPlugin,
      Date revisionTimeStampPreviousPlugin) {
    final NormalizationPluginMetadata normalizationPluginMetadata = new NormalizationPluginMetadata();
    normalizationPluginMetadata.setEnabled(true);
    normalizationPluginMetadata.setRevisionNamePreviousPlugin(revisionNamePreviousPlugin);
    normalizationPluginMetadata.setRevisionTimestampPreviousPlugin(revisionTimeStampPreviousPlugin);
    return normalizationPluginMetadata;
  }

  @NotNull
  static EnrichmentPluginMetadata getEnrichmentPluginMetadata(String revisionNamePreviousPlugin,
      Date revisionTimeStampPreviousPlugin) {
    final EnrichmentPluginMetadata enrichmentPluginMetadata = new EnrichmentPluginMetadata();
    enrichmentPluginMetadata.setEnabled(true);
    enrichmentPluginMetadata.setRevisionNamePreviousPlugin(revisionNamePreviousPlugin);
    enrichmentPluginMetadata.setRevisionTimestampPreviousPlugin(revisionTimeStampPreviousPlugin);
    return enrichmentPluginMetadata;
  }

  @NotNull
  static MediaProcessPluginMetadata getMediaProcessPluginMetadata(String revisionNamePreviousPlugin,
      Date revisionTimeStampPreviousPlugin) {
    final MediaProcessPluginMetadata mediaProcessPluginMetadata = new MediaProcessPluginMetadata();
    mediaProcessPluginMetadata.setEnabled(true);
    mediaProcessPluginMetadata.setRevisionNamePreviousPlugin(revisionNamePreviousPlugin);
    mediaProcessPluginMetadata.setRevisionTimestampPreviousPlugin(revisionTimeStampPreviousPlugin);
    mediaProcessPluginMetadata.setThrottlingLevel(ThrottlingLevel.STRONG);
    return mediaProcessPluginMetadata;
  }

  @NotNull
  static ReindexToPreviewPluginMetadata getReindexToPreviewPluginMetadata(String revisionNamePreviousPlugin,
      Date revisionTimeStampPreviousPlugin) {
    final ReindexToPreviewPluginMetadata reindexToPreviewPluginMetadata = new ReindexToPreviewPluginMetadata();
    reindexToPreviewPluginMetadata.setRevisionNamePreviousPlugin(revisionNamePreviousPlugin);
    reindexToPreviewPluginMetadata.setRevisionTimestampPreviousPlugin(revisionTimeStampPreviousPlugin);
    return reindexToPreviewPluginMetadata;
  }

  @NotNull
  static ReindexToPublishPluginMetadata getReindexToPublishPluginMetadata(String revisionNamePreviousPlugin,
      Date revisionTimeStampPreviousPlugin) {
    final ReindexToPublishPluginMetadata reindexToPublishPluginMetadata = new ReindexToPublishPluginMetadata();
    reindexToPublishPluginMetadata.setRevisionNamePreviousPlugin(revisionNamePreviousPlugin);
    reindexToPublishPluginMetadata.setRevisionTimestampPreviousPlugin(revisionTimeStampPreviousPlugin);
    return reindexToPublishPluginMetadata;
  }

  @NotNull
  static IndexToPreviewPluginMetadata getIndexToPreviewPluginMetadata(Date harvestDate, String revisionNamePreviousPlugin,
      Date revisionTimeStampPreviousPlugin) {
    final IndexToPreviewPluginMetadata indexToPreviewPluginMetadata = new IndexToPreviewPluginMetadata();
    indexToPreviewPluginMetadata.setIncrementalIndexing(false);
    indexToPreviewPluginMetadata.setHarvestDate(harvestDate);
    indexToPreviewPluginMetadata.setPreserveTimestamps(false);
    indexToPreviewPluginMetadata.setEnabled(true);
    indexToPreviewPluginMetadata.setDatasetIdsToRedirectFrom(List.of());
    indexToPreviewPluginMetadata.setPerformRedirects(true);
    indexToPreviewPluginMetadata.setRevisionNamePreviousPlugin(revisionNamePreviousPlugin);
    indexToPreviewPluginMetadata.setRevisionTimestampPreviousPlugin(revisionTimeStampPreviousPlugin);

    return indexToPreviewPluginMetadata;
  }

  @NotNull
  static IndexToPublishPluginMetadata getIndexToPublishPluginMetadata(Date harvestDate, String revisionNamePreviousPlugin,
      Date revisionTimeStampPreviousPlugin) {
    final IndexToPublishPluginMetadata indexToPublishPluginMetadata = new IndexToPublishPluginMetadata();
    indexToPublishPluginMetadata.setIncrementalIndexing(false);
    indexToPublishPluginMetadata.setHarvestDate(harvestDate);
    indexToPublishPluginMetadata.setPreserveTimestamps(false);
    indexToPublishPluginMetadata.setEnabled(true);
    indexToPublishPluginMetadata.setDatasetIdsToRedirectFrom(List.of());
    indexToPublishPluginMetadata.setPerformRedirects(true);
    indexToPublishPluginMetadata.setRevisionNamePreviousPlugin(revisionNamePreviousPlugin);
    indexToPublishPluginMetadata.setRevisionTimestampPreviousPlugin(revisionTimeStampPreviousPlugin);
    return indexToPublishPluginMetadata;
  }

  @NotNull
  static Dataset getTestDataset() {
    final Dataset dataset = new Dataset();
    dataset.setDatasetId("datasetId");
    dataset.setCountry(Country.GERMANY);
    dataset.setDatasetName("dataset test name");
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
    dataset.setPublicationFitness(PublicationFitness.FIT);
    return dataset;
  }

  @NotNull
  static ExecutionProgress getExecutionProgress() {
    final ExecutionProgress executionProgress = new ExecutionProgress();
    executionProgress.setStatus(TaskState.PROCESSED);
    executionProgress.setExpectedRecords(1);
    executionProgress.setProcessedRecords(1);
    executionProgress.setProgressPercentage(100);
    executionProgress.setIgnoredRecords(0);
    executionProgress.setDeletedRecords(0);
    executionProgress.setErrors(0);
    executionProgress.setTotalDatabaseRecords(-1);
    return executionProgress;
  }

  @NotNull
  static AbstractExecutablePlugin getExecutablePlugin(ExecutablePluginMetadata executablePluginMetadata,
      Date startDate,
      Date updateDate,
      Date finishDate,
      DataStatus dataStatus,
      String id,
      ExecutionProgress executionProgress) {
    final AbstractExecutablePlugin executablePlugin =
        ExecutablePluginFactory.createPlugin(executablePluginMetadata);
    executablePlugin.setExecutionProgress(executionProgress);
    executablePlugin.setId(id);
    executablePlugin.setPluginStatus(PluginStatus.FINISHED);
    executablePlugin.setStartedDate(startDate);
    executablePlugin.setUpdatedDate(updateDate);
    executablePlugin.setFinishedDate(finishDate);
    executablePlugin.setDataStatus(dataStatus);
    executablePlugin.setExternalTaskId(String.valueOf(Instant.now().toEpochMilli()));
    return executablePlugin;
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

  static Date getDateMinusMinutes(Date date, long minutes) {
    return Date.from(Instant.from(date.toInstant()).minus(minutes, ChronoUnit.MINUTES));
  }

  @NotNull
  static WorkflowExecution getWorkflowPreReindex(Dataset dataset) {
    final WorkflowExecution workflowExecution = new WorkflowExecution();
    workflowExecution.setDatasetId(dataset.getDatasetId());
    workflowExecution.setWorkflowStatus(WorkflowStatus.FINISHED);
    workflowExecution.setEcloudDatasetId(dataset.getEcloudDatasetId());
    workflowExecution.setStartedBy(dataset.getCreatedByUserId());
    workflowExecution.setWorkflowPriority(0);
    workflowExecution.setCancelling(false);
    Date templateDate = Date.from(Instant.now());

    workflowExecution.setCreatedDate(templateDate);
    workflowExecution.setStartedDate(getDateMinusMinutes(templateDate,28));
    workflowExecution.setUpdatedDate(getDateMinusMinutes(templateDate,20));
    workflowExecution.setFinishedDate(getDateMinusMinutes(templateDate,20));

    workflowExecution.setMetisPlugins(List.of(
        getExecutablePlugin(getHttpHarvestPluginMetadata(),
            getDateMinusMinutes(templateDate,28),
            getDateMinusMinutes(templateDate,28),
            getDateMinusMinutes(templateDate,28),
            DataStatus.VALID,
            "75671dea3818387b1e4bd92c-HTTP_HARVEST",
            getExecutionProgress()
        ),
        getExecutablePlugin(getValidationExternalPluginMetadata("HTTP_HARVEST",
                getDateMinusMinutes(templateDate,28)),
            getDateMinusMinutes(templateDate,27),
            getDateMinusMinutes(templateDate,27),
            getDateMinusMinutes(templateDate,27),
            DataStatus.VALID,
            "75671dea3818387b1e4bd92d-VALIDATION_EXTERNAL",
            getExecutionProgress()
        ),
        getExecutablePlugin(getTransformationPluginMetadata(dataset, "VALIDATION_EXTERNAL",
                getDateMinusMinutes(templateDate,27)),
            getDateMinusMinutes(templateDate,26),
            getDateMinusMinutes(templateDate,26),
            getDateMinusMinutes(templateDate,26),
            DataStatus.VALID,
            "75671dea3818387b1e4bd92e-TRANSFORMATION",
            getExecutionProgress()
        ),
        getExecutablePlugin(getValidationInternalPluginMetadata("TRANSFORMATION",
                getDateMinusMinutes(templateDate,26)),
            getDateMinusMinutes(templateDate,25),
            getDateMinusMinutes(templateDate,25),
            getDateMinusMinutes(templateDate,25),
            DataStatus.VALID,
            "75671dea3818387b1e4bd92f-VALIDATION_INTERNAL",
            getExecutionProgress()
        ),
        getExecutablePlugin(getNormalizationPluginMetadata("VALIDATION_INTERNAL",
                getDateMinusMinutes(templateDate,25)),
            getDateMinusMinutes(templateDate,24),
            getDateMinusMinutes(templateDate,24),
            getDateMinusMinutes(templateDate,24),
            DataStatus.VALID,
            "75671dea3818387b1e4bd930-NORMALIZATION",
            getExecutionProgress()
        ),
        getExecutablePlugin(getEnrichmentPluginMetadata("NORMALIZATION",
                getDateMinusMinutes(templateDate,24)),
            getDateMinusMinutes(templateDate,23),
            getDateMinusMinutes(templateDate,23),
            getDateMinusMinutes(templateDate,23),
            DataStatus.DEPRECATED,
            "75671dea3818387b1e4bd931-ENRICHMENT",
            getExecutionProgress()
        ),
        getExecutablePlugin(getMediaProcessPluginMetadata("ENRICHMENT",
                getDateMinusMinutes(templateDate,23)),
            getDateMinusMinutes(templateDate,22),
            getDateMinusMinutes(templateDate,22),
            getDateMinusMinutes(templateDate,22),
            DataStatus.DEPRECATED,
            "75671dea3818387b1e4bd932-MEDIA_PROCESS",
            getExecutionProgress()
        ),
        getExecutablePlugin(getIndexToPreviewPluginMetadata(
                getDateMinusMinutes(templateDate,28),
                "MEDIA_PROCESS",
                getDateMinusMinutes(templateDate,22)),
            getDateMinusMinutes(templateDate,21),
            getDateMinusMinutes(templateDate,21),
            getDateMinusMinutes(templateDate,21),
            DataStatus.DEPRECATED,
            "75671dea3818387b1e4bd933-PREVIEW",
            getExecutionProgress()
        ),
        getExecutablePlugin(getIndexToPublishPluginMetadata(
                getDateMinusMinutes(templateDate,28),
                "PREVIEW",
                getDateMinusMinutes(templateDate,21)),
            getDateMinusMinutes(templateDate,20),
            getDateMinusMinutes(templateDate,20),
            getDateMinusMinutes(templateDate,20),
            DataStatus.DEPRECATED,
            "75671dea3818387b1e4bd934-PUBLISH",
            getExecutionProgress()
        )
    ));
    return workflowExecution;
  }

  @NotNull
  static WorkflowExecution getWorkflowReindex(Dataset dataset, Date harvestDate) {
    final WorkflowExecution workflowExecution = new WorkflowExecution();
    workflowExecution.setDatasetId(dataset.getDatasetId());
    workflowExecution.setWorkflowStatus(WorkflowStatus.FINISHED);
    workflowExecution.setEcloudDatasetId(dataset.getEcloudDatasetId());
    workflowExecution.setStartedBy(dataset.getCreatedByUserId());
    workflowExecution.setWorkflowPriority(0);
    workflowExecution.setCancelling(false);
    Date templateDate = Date.from(Instant.now());

    workflowExecution.setCreatedDate(templateDate);
    workflowExecution.setStartedDate(getDateMinusMinutes(templateDate,19));
    workflowExecution.setUpdatedDate(getDateMinusMinutes(templateDate,11));
    workflowExecution.setFinishedDate(getDateMinusMinutes(templateDate,11));

    ReindexToPreviewPluginMetadata reindexToPreviewPluginMetadata = getReindexToPreviewPluginMetadata("VALIDATION_INTERNAL",harvestDate);
    ReindexToPreviewPlugin reindexToPreviewPlugin = new ReindexToPreviewPlugin(reindexToPreviewPluginMetadata);
    reindexToPreviewPlugin.setStartedDate(getDateMinusMinutes(templateDate,11));
    reindexToPreviewPlugin.setUpdatedDate(getDateMinusMinutes(templateDate,11));
    reindexToPreviewPlugin.setFinishedDate(getDateMinusMinutes(templateDate,11));
    reindexToPreviewPlugin.setPluginStatus(PluginStatus.FINISHED);
    reindexToPreviewPlugin.setDataStatus(DataStatus.VALID);
    reindexToPreviewPlugin.setId("74b628a62d563e2ef58976d0-REINDEX_TO_PREVIEW");

    ReindexToPublishPluginMetadata reindexToPublishPluginMetadata = getReindexToPublishPluginMetadata("REINDEX_TO_PREVIEW", getDateMinusMinutes(templateDate,11) );
    ReindexToPublishPlugin reindexToPublishPlugin = new ReindexToPublishPlugin(reindexToPublishPluginMetadata);
    reindexToPublishPlugin.setStartedDate(getDateMinusMinutes(templateDate,11));
    reindexToPublishPlugin.setUpdatedDate(getDateMinusMinutes(templateDate,11));
    reindexToPublishPlugin.setFinishedDate(getDateMinusMinutes(templateDate,11));
    reindexToPublishPlugin.setPluginStatus(PluginStatus.FINISHED);
    reindexToPublishPlugin.setDataStatus(DataStatus.VALID);
    reindexToPublishPlugin.setId("74b628a62d563e2ef58976d1-REINDEX_TO_PUBLISH");

    workflowExecution.setMetisPlugins(List.of(
        reindexToPreviewPlugin,
        reindexToPublishPlugin
    ));
    return workflowExecution;
  }

  @NotNull
  static WorkflowExecution getWorkflowPostReindex(Dataset dataset) {
    final WorkflowExecution workflowExecution = new WorkflowExecution();
    workflowExecution.setDatasetId(dataset.getDatasetId());
    workflowExecution.setWorkflowStatus(WorkflowStatus.FINISHED);
    workflowExecution.setEcloudDatasetId(dataset.getEcloudDatasetId());
    workflowExecution.setStartedBy(dataset.getCreatedByUserId());
    workflowExecution.setWorkflowPriority(0);
    workflowExecution.setCancelling(false);
    Date templateDate = Date.from(Instant.now());

    workflowExecution.setCreatedDate(templateDate);
    workflowExecution.setStartedDate(getDateMinusMinutes(templateDate,10));
    workflowExecution.setUpdatedDate(getDateMinusMinutes(templateDate,9));
    workflowExecution.setFinishedDate(getDateMinusMinutes(templateDate,9));

    workflowExecution.setMetisPlugins(List.of(
        getExecutablePlugin(getHttpHarvestPluginMetadata(),
            getDateMinusMinutes(templateDate,10),
            getDateMinusMinutes(templateDate,10),
            getDateMinusMinutes(templateDate,10),
            DataStatus.VALID,
            "85671dea3818387b1e4bd92c-HTTP_HARVEST",
            getExecutionProgress()
        ),
        getExecutablePlugin(getValidationExternalPluginMetadata("HTTP_HARVEST",
                getDateMinusMinutes(templateDate,10)),
            getDateMinusMinutes(templateDate,8),
            getDateMinusMinutes(templateDate,8),
            getDateMinusMinutes(templateDate,8),
            DataStatus.VALID,
            "85671dea3818387b1e4bd92d-VALIDATION_EXTERNAL",
            getExecutionProgress()
        ),
        getExecutablePlugin(getTransformationPluginMetadata(dataset, "VALIDATION_EXTERNAL",
                getDateMinusMinutes(templateDate,8)),
            getDateMinusMinutes(templateDate,7),
            getDateMinusMinutes(templateDate,7),
            getDateMinusMinutes(templateDate,7),
            DataStatus.VALID,
            "85671dea3818387b1e4bd92e-TRANSFORMATION",
            getExecutionProgress()
        ),
        getExecutablePlugin(getValidationInternalPluginMetadata("TRANSFORMATION",
                getDateMinusMinutes(templateDate,7)),
            getDateMinusMinutes(templateDate,6),
            getDateMinusMinutes(templateDate,6),
            getDateMinusMinutes(templateDate,6),
            DataStatus.VALID,
            "85671dea3818387b1e4bd92f-VALIDATION_INTERNAL",
            getExecutionProgress()
        ),
        getExecutablePlugin(getNormalizationPluginMetadata("VALIDATION_INTERNAL",
                getDateMinusMinutes(templateDate,6)),
            getDateMinusMinutes(templateDate,5),
            getDateMinusMinutes(templateDate,5),
            getDateMinusMinutes(templateDate,5),
            DataStatus.VALID,
            "85671dea3818387b1e4bd930-NORMALIZATION",
            getExecutionProgress()
        ),
        getExecutablePlugin(getEnrichmentPluginMetadata("NORMALIZATION",
                getDateMinusMinutes(templateDate,5)),
            getDateMinusMinutes(templateDate,4),
            getDateMinusMinutes(templateDate,4),
            getDateMinusMinutes(templateDate,4),
            DataStatus.VALID,
            "85671dea3818387b1e4bd931-ENRICHMENT",
            getExecutionProgress()
        ),
        getExecutablePlugin(getMediaProcessPluginMetadata("ENRICHMENT",
                getDateMinusMinutes(templateDate,4)),
            getDateMinusMinutes(templateDate,3),
            getDateMinusMinutes(templateDate,3),
            getDateMinusMinutes(templateDate,3),
            DataStatus.VALID,
            "85671dea3818387b1e4bd932-MEDIA_PROCESS",
            getExecutionProgress()
        ),
        getExecutablePlugin(getIndexToPreviewPluginMetadata(
                getDateMinusMinutes(templateDate,10),
                "MEDIA_PROCESS",
                getDateMinusMinutes(templateDate,3)),
            getDateMinusMinutes(templateDate,2),
            getDateMinusMinutes(templateDate,2),
            getDateMinusMinutes(templateDate,2),
            DataStatus.VALID,
            "85671dea3818387b1e4bd933-PREVIEW",
            getExecutionProgress()
        ),
        getExecutablePlugin(getIndexToPublishPluginMetadata(
                getDateMinusMinutes(templateDate,10),
                "PREVIEW",
                getDateMinusMinutes(templateDate,2)),
            getDateMinusMinutes(templateDate,1),
            getDateMinusMinutes(templateDate,1),
            getDateMinusMinutes(templateDate,1),
            DataStatus.VALID,
            "85671dea3818387b1e4bd934-PUBLISH",
            getExecutionProgress()
        )
    ));
    return workflowExecution;
  }

  @NotNull
  static Workflow getWorkflowFromNormalization(Dataset dataset) {
    final ObjectId objectId = new ObjectId();
    final Workflow workflow = new Workflow();
    workflow.setDatasetId(dataset.getDatasetId());
    workflow.setId(objectId);
    workflow.setMetisPluginsMetadata(List.of(
        getNormalizationPluginMetadata(null,null),
        getEnrichmentPluginMetadata(null,null),
        getMediaProcessPluginMetadata(null,null),
        getIndexToPreviewPluginMetadata(null,null,null),
        getIndexToPublishPluginMetadata(null,null,null)));
    return workflow;
  }

  static ValidationProperties getValidationExternalProperties() {
    return new ValidationProperties("http://ftp.eanadev.org/schema_zips/europeana_schemas-20220809.zip",
        "EDM.xsd",
        "schematron/schematron.xsl");
  }
  static ValidationProperties getValidationInternalProperties() {
    return new ValidationProperties("http://ftp.eanadev.org/schema_zips/europeana_schemas-20220809.zip",
        "EDM-INTERNAL.xsd",
        "schematron/schematron-internal.xsl");
  }
}
