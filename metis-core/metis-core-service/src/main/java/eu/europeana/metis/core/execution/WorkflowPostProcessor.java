package eu.europeana.metis.core.execution;

import static eu.europeana.metis.network.ExternalRequestUtil.retryableExternalRequestForNetworkExceptionsThrowing;

import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.common.model.dps.RecordState;
import eu.europeana.cloud.common.model.dps.SubTaskInfo;
import eu.europeana.cloud.service.dps.exception.DpsException;
import eu.europeana.cloud.service.dps.metis.indexing.TargetIndexingDatabase;
import eu.europeana.metis.core.common.RecordIdUtils;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.DepublishRecordIdDao;
import eu.europeana.metis.core.dao.PluginWithExecutionId;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.Dataset.PublicationFitness;
import eu.europeana.metis.core.dataset.DepublishRecordId.DepublicationStatus;
import eu.europeana.metis.core.exceptions.InvalidIndexPluginException;
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.core.util.DepublishRecordIdSortField;
import eu.europeana.metis.core.util.SortDirection;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.DataStatus;
import eu.europeana.metis.core.workflow.plugins.DepublishPlugin;
import eu.europeana.metis.core.workflow.plugins.IndexToPreviewPlugin;
import eu.europeana.metis.core.workflow.plugins.IndexToPublishPlugin;
import eu.europeana.metis.core.workflow.plugins.MetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.exception.BadContentException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * This object can perform post-processing for workflows.
 */
public class WorkflowPostProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowPostProcessor.class);

  private static final int ECLOUD_REQUEST_BATCH_SIZE = 1000;

  private final DepublishRecordIdDao depublishRecordIdDao;
  private final DatasetDao datasetDao;
  private final WorkflowExecutionDao workflowExecutionDao;
  private final DpsClient dpsClient;

  /**
   * Constructor.
   *
   * @param depublishRecordIdDao The DAO for de-published records
   * @param datasetDao The DAO for datasets
   * @param workflowExecutionDao The DAO for workflow executions
   * @param dpsClient the dps client
   */
  public WorkflowPostProcessor(DepublishRecordIdDao depublishRecordIdDao,
      DatasetDao datasetDao, WorkflowExecutionDao workflowExecutionDao, DpsClient dpsClient) {
    this.depublishRecordIdDao = depublishRecordIdDao;
    this.datasetDao = datasetDao;
    this.workflowExecutionDao = workflowExecutionDao;
    this.dpsClient = dpsClient;
  }

  /**
   * Performs post-processing for indexing plugins
   *
   * @param indexPlugin The index plugin
   * @param datasetId The dataset id
   * @throws DpsException If communication with e-cloud dps failed
   * @throws InvalidIndexPluginException If invalid type of plugin
   * @throws BadContentException In case the records would violate the maximum number of de-published records that each dataset
   * can have.
   */
  private void indexPostProcess(AbstractExecutablePlugin<?> indexPlugin, String datasetId)
      throws DpsException, InvalidIndexPluginException, BadContentException {
    TargetIndexingDatabase targetIndexingDatabase;
    switch (indexPlugin) {
      case IndexToPreviewPlugin indexToPreviewPlugin -> targetIndexingDatabase = indexToPreviewPlugin.getTargetIndexingDatabase();
      case IndexToPublishPlugin indexToPublishPlugin -> {
        targetIndexingDatabase = indexToPublishPlugin.getTargetIndexingDatabase();
        reinstateDepublishRecordIdsStatus((IndexToPublishPlugin) indexPlugin, datasetId);
      }
      default -> throw new InvalidIndexPluginException("Plugin is not of the types supported");
    }
    final Integer databaseTotalRecords = retryableExternalRequestForNetworkExceptionsThrowing(() ->
        (int) dpsClient.getTotalMetisDatabaseRecords(datasetId, targetIndexingDatabase));
    indexPlugin.getExecutionProgress().setTotalDatabaseRecords(databaseTotalRecords);
  }

  private void reinstateDepublishRecordIdsStatus(IndexToPublishPlugin indexPlugin, String datasetId)
      throws BadContentException, DpsException {
    final boolean isIncremental = indexPlugin.getPluginMetadata().isIncrementalIndexing();
    if (isIncremental) {
      // get all currently de-published records IDs from the database and create their full versions
      final Set<String> depublishedRecordIds = depublishRecordIdDao.getAllDepublishRecordIdsWithStatus(
          datasetId, DepublishRecordIdSortField.DEPUBLICATION_STATE, SortDirection.ASCENDING,
          DepublicationStatus.DEPUBLISHED);
      final Map<String, String> depublishedRecordIdsByFullId = depublishedRecordIds.stream()
                                                                                   .collect(Collectors.toMap(
                                                                                       id -> RecordIdUtils.composeFullRecordId(
                                                                                           datasetId, id),
                                                                                       Function.identity()));

      // Check which have been published by the index action - use full record IDs for eCloud.
      if (!CollectionUtils.isEmpty(depublishedRecordIdsByFullId)) {
        final List<String> publishedRecordIds = dpsClient.searchPublishedDatasetRecords(datasetId,
            new ArrayList<>(depublishedRecordIdsByFullId.keySet()));

        // Remove the 'depublished' status. Note: we need to check for an empty result (otherwise
        // the DAO call will update all records). Use the simple record IDs again.
        if (!CollectionUtils.isEmpty(publishedRecordIds)) {
          depublishRecordIdDao.markRecordIdsWithDepublicationStatus(datasetId,
              publishedRecordIds.stream().map(depublishedRecordIdsByFullId::get)
                                .collect(Collectors.toSet()), DepublicationStatus.PENDING_DEPUBLICATION, null, null);
        }
      }
    } else {
      // reset de-publish status, pass null, all records will be de-published
      depublishRecordIdDao.markRecordIdsWithDepublicationStatus(datasetId, null,
          DepublicationStatus.PENDING_DEPUBLICATION, null, null);
    }
  }

  /**
   * Performs post-processing for de-publish plugins
   *
   * @param depublishPlugin The de-publish plugin
   * @param datasetId The dataset id
   * @throws DpsException If communication with e-cloud dps failed
   */
  private void depublishPostProcess(DepublishPlugin depublishPlugin, String datasetId)
      throws DpsException {
    if (depublishPlugin.getPluginMetadata().isDatasetDepublish()) {
      depublishDatasetPostProcess(datasetId);
    } else {
      depublishRecordPostProcess(depublishPlugin, datasetId);
    }
  }

  /**
   * @param depublishPlugin The de-publish plugin
   * @param datasetId The dataset id
   * @throws DpsException If communication with e-cloud dps failed
   */
  private void depublishRecordPostProcess(DepublishPlugin depublishPlugin, String datasetId)
      throws DpsException {

    // Retrieve the successfully depublished records.
    final long externalTaskId = Long.parseLong(depublishPlugin.getExternalTaskId());
    final List<SubTaskInfo> subTasks = new ArrayList<>();
    List<SubTaskInfo> subTasksBatch;
    do {
      subTasksBatch = retryableExternalRequestForNetworkExceptionsThrowing(
          () -> dpsClient.getDetailedTaskReportBetweenChunks(
              depublishPlugin.getTopologyName(), externalTaskId, subTasks.size(),
              subTasks.size() + ECLOUD_REQUEST_BATCH_SIZE));
      subTasks.addAll(subTasksBatch);
    } while (subTasksBatch.size() == ECLOUD_REQUEST_BATCH_SIZE);

    // Mark the records as DEPUBLISHED.
    final Map<String, Set<String>> successfulRecords = subTasks.stream()
                                                               .filter(subTask -> subTask.getRecordState() == RecordState.SUCCESS)
                                                               .map(SubTaskInfo::getResource)
                                                               .map(RecordIdUtils::decomposeFullRecordId)
                                                               .collect(Collectors.groupingBy(Pair::getLeft,
                                                                   Collectors.mapping(Pair::getRight, Collectors.toSet())));
    successfulRecords.forEach((dataset, records) ->
        depublishRecordIdDao.markRecordIdsWithDepublicationStatus(dataset, records,
            DepublicationStatus.DEPUBLISHED, new Date(), depublishPlugin.getPluginMetadata().getDepublicationReason()));

    // Set publication fitness to PARTIALLY FIT (if not set to the more severe UNFIT).
    final Dataset dataset = datasetDao.getDatasetByDatasetId(datasetId);
    if (dataset.getPublicationFitness() != PublicationFitness.UNFIT) {
      dataset.setPublicationFitness(PublicationFitness.PARTIALLY_FIT);
      datasetDao.update(dataset);
    }
  }

  /**
   * @param datasetId The dataset id
   */
  private void depublishDatasetPostProcess(String datasetId) {

    // Set all depublished records back to PENDING.
    depublishRecordIdDao.markRecordIdsWithDepublicationStatus(datasetId, null,
        DepublicationStatus.PENDING_DEPUBLICATION, null, null);
    // Find latest PUBLISH Type Plugin and set dataStatus to DELETED.
    final PluginWithExecutionId<MetisPlugin> latestSuccessfulPlugin = workflowExecutionDao
        .getLatestSuccessfulPlugin(datasetId, OrchestratorService.PUBLISH_TYPES);
    if (Objects.nonNull(latestSuccessfulPlugin) && Objects
        .nonNull(latestSuccessfulPlugin.getPlugin())) {
      final WorkflowExecution workflowExecutionToUpdate = workflowExecutionDao
          .getById(latestSuccessfulPlugin.getExecutionId());
      final Optional<AbstractMetisPlugin> metisPluginWithType = workflowExecutionToUpdate
          .getMetisPluginWithType(latestSuccessfulPlugin.getPlugin().getPluginType());
      if (metisPluginWithType.isPresent()) {
        metisPluginWithType.get().setDataStatus(DataStatus.DELETED);
        workflowExecutionDao.updateWorkflowPlugins(workflowExecutionToUpdate);
      }
    }
    // Set publication fitness to UNFIT.
    final Dataset dataset = datasetDao.getDatasetByDatasetId(datasetId);
    dataset.setPublicationFitness(PublicationFitness.UNFIT);
    datasetDao.update(dataset);
  }

  /**
   * This method performs post-processing after an individual workflow step.
   *
   * @param plugin The plugin that was successfully executed
   * @param datasetId The dataset ID to which the plugin belongs
   * @throws DpsException If communication with e-cloud dps failed
   * @throws InvalidIndexPluginException If invalid type of plugin
   * @throws BadContentException In case the records would violate the maximum number of de-published records that each dataset
   * can have.
   */
  void performPluginPostProcessing(AbstractExecutablePlugin<?> plugin, String datasetId)
      throws DpsException, InvalidIndexPluginException, BadContentException {

    final PluginType pluginType = plugin.getPluginType();
    LOGGER.info("Starting postprocessing of plugin {} in dataset {}.", pluginType, datasetId);
    if (pluginType == PluginType.PREVIEW || pluginType == PluginType.PUBLISH) {
      indexPostProcess(plugin, datasetId);
    } else if (pluginType == PluginType.DEPUBLISH) {
      depublishPostProcess((DepublishPlugin) plugin, datasetId);
    }
    LOGGER.info("Finished postprocessing of plugin {} in dataset {}.", pluginType, datasetId);
  }
}
