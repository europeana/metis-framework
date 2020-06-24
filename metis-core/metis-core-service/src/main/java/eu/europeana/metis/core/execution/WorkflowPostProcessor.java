package eu.europeana.metis.core.execution;

import eu.europeana.metis.core.dao.DepublishRecordIdDao;
import eu.europeana.metis.core.dao.PluginWithExecutionId;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.dataset.DepublishRecordId.DepublicationStatus;
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.DataStatus;
import eu.europeana.metis.core.workflow.plugins.DepublishPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.MetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This object can perform post processing for workflows.
 */
public class WorkflowPostProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowPostProcessor.class);

  private final DepublishRecordIdDao depublishRecordIdDao;
  private final WorkflowExecutionDao workflowExecutionDao;

  /**
   * Constructor.
   *
   * @param depublishRecordIdDao The DAO for depublished records.
   * @param workflowExecutionDao The DAO for workflow executions.
   */
  public WorkflowPostProcessor(DepublishRecordIdDao depublishRecordIdDao,
      WorkflowExecutionDao workflowExecutionDao) {
    this.depublishRecordIdDao = depublishRecordIdDao;
    this.workflowExecutionDao = workflowExecutionDao;
  }

  /**
   * This method performs post processing after an individual workflow step.
   *
   * @param plugin The plugin that was successfully executed.
   * @param datasetId The dataset ID to which the plugin belongs.
   */
  void performPluginPostProcessing(AbstractExecutablePlugin<?> plugin, String datasetId) {

    final PluginType pluginType = plugin.getPluginType();
    LOGGER.info("Starting postprocessing of plugin {} in dataset {}.", pluginType, datasetId);
    //Reset depublish status if index to PUBLISH
    if (pluginType == PluginType.PUBLISH) {
      publishPostProcess(datasetId);
    } else if (pluginType == PluginType.DEPUBLISH) {
      depublishPostProcess(plugin, datasetId);
    }
    LOGGER.info("Finished postprocessing of plugin {} in dataset {}.", pluginType, datasetId);
  }

  private void publishPostProcess(String datasetId) {
    depublishRecordIdDao.markRecordIdsWithDepublicationStatus(datasetId,
        DepublicationStatus.PENDING_DEPUBLICATION, null);
  }

  private void depublishPostProcess(AbstractExecutablePlugin<?> plugin, String datasetId) {
    final boolean datasetDepublish = ((DepublishPluginMetadata) plugin.getPluginMetadata())
        .isDatasetDepublish();
    if (datasetDepublish) { //Reset depublish status of records if depublishing dataset
      depublishRecordIdDao.markRecordIdsWithDepublicationStatus(datasetId,
          DepublicationStatus.PENDING_DEPUBLICATION, null);
      //Find latest PUBLISH Type Plugin and set dataStatus
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
    } else { //Set depublish status if depublishing records
      // TODO: 6/23/20 To be worked when record depublication with ecloud is completed
    }
  }
}
