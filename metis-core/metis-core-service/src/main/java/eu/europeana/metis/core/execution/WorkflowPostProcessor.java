package eu.europeana.metis.core.execution;

import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.common.model.dps.RecordState;
import eu.europeana.cloud.common.model.dps.SubTaskInfo;
import eu.europeana.cloud.service.dps.exception.DpsException;
import eu.europeana.metis.core.common.DepublishRecordIdUtils;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.DepublishRecordIdDao;
import eu.europeana.metis.core.dao.PluginWithExecutionId;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.Dataset.PublicationFitness;
import eu.europeana.metis.core.dataset.DepublishRecordId.DepublicationStatus;
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.DataStatus;
import eu.europeana.metis.core.workflow.plugins.DepublishPlugin;
import eu.europeana.metis.core.workflow.plugins.MetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This object can perform post processing for workflows.
 */
public class WorkflowPostProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowPostProcessor.class);

  private final DepublishRecordIdDao depublishRecordIdDao;
  private final DatasetDao datasetDao;
  private final WorkflowExecutionDao workflowExecutionDao;
  private final DpsClient dpsClient;

  /**
   * Constructor.
   *  @param depublishRecordIdDao The DAO for depublished records.
   * @param datasetDao The DAO for datasets
   * @param workflowExecutionDao The DAO for workflow executions.
   */
  public WorkflowPostProcessor(DepublishRecordIdDao depublishRecordIdDao,
      DatasetDao datasetDao, WorkflowExecutionDao workflowExecutionDao, DpsClient dpsClient) {
    this.depublishRecordIdDao = depublishRecordIdDao;
    this.datasetDao = datasetDao;
    this.workflowExecutionDao = workflowExecutionDao;
    this.dpsClient = dpsClient;
  }

  /**
   * This method performs post processing after an individual workflow step.
   *
   * @param plugin The plugin that was successfully executed.
   * @param datasetId The dataset ID to which the plugin belongs.
   */
  void performPluginPostProcessing(AbstractExecutablePlugin<?> plugin, String datasetId)
          throws DpsException {

    final PluginType pluginType = plugin.getPluginType();
    LOGGER.info("Starting postprocessing of plugin {} in dataset {}.", pluginType, datasetId);
    //Reset depublish status if index to PUBLISH
    if (pluginType == PluginType.PUBLISH) {
      publishPostProcess(datasetId);
    } else if (pluginType == PluginType.DEPUBLISH) {
      depublishPostProcess((DepublishPlugin) plugin, datasetId);
    }
    LOGGER.info("Finished postprocessing of plugin {} in dataset {}.", pluginType, datasetId);
  }

  private void publishPostProcess(String datasetId) {
    depublishRecordIdDao.markRecordIdsWithDepublicationStatus(datasetId, null,
        DepublicationStatus.PENDING_DEPUBLICATION, null);
  }

  private void depublishPostProcess(DepublishPlugin plugin, String datasetId) throws DpsException {
    if (plugin.getPluginMetadata().isDatasetDepublish()) {
      depublishDatasetPostProcess(datasetId);
    } else {
      depublishRecordPostProcess(plugin, datasetId);
    }
  }

  private void depublishDatasetPostProcess(String datasetId){

    // Set all depublished records back to PENDING.
    depublishRecordIdDao.markRecordIdsWithDepublicationStatus(datasetId, null,
            DepublicationStatus.PENDING_DEPUBLICATION, null);

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

  private void depublishRecordPostProcess(DepublishPlugin depublishPlugin, String datasetId)
          throws DpsException {

    // Retrieve the successfully depublished records.
    final long externalTaskId = Long.parseLong(depublishPlugin.getExternalTaskId());
    final List<SubTaskInfo> subTasks = dpsClient
            .getDetailedTaskReport(depublishPlugin.getTopologyName(), externalTaskId);

    // Mark the records as DEPUBLISHED.
    final Map<String, Set<String>> successfulRecords = subTasks.stream()
            .filter(subTask -> subTask.getRecordState() == RecordState.SUCCESS)
            .map(SubTaskInfo::getResource).map(DepublishRecordIdUtils::decomposeFullRecordId)
            .collect(Collectors.groupingBy(Pair::getLeft,
                    Collectors.mapping(Pair::getRight, Collectors.toSet())));
    successfulRecords.forEach((dataset, records) ->
            depublishRecordIdDao.markRecordIdsWithDepublicationStatus(dataset, records,
                    DepublicationStatus.DEPUBLISHED, depublishPlugin.getFinishedDate()));

    // Set publication fitness to PARTIALLY FIT (if not set to the more severe UNFIT).
    final Dataset dataset = datasetDao.getDatasetByDatasetId(datasetId);
    if (dataset.getPublicationFitness() != PublicationFitness.UNFIT) {
      dataset.setPublicationFitness(PublicationFitness.PARTIALLY_FIT);
      datasetDao.update(dataset);
    }
  }
}
