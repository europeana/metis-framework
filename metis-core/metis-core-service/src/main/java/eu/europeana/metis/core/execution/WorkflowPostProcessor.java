package eu.europeana.metis.core.execution;

import eu.europeana.metis.core.dao.DepublishedRecordDao;
import eu.europeana.metis.core.dataset.DepublishedRecord.DepublicationStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.DepublishPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This object can perform post processing for workflows.
 */
public class WorkflowPostProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowPostProcessor.class);

  private final DepublishedRecordDao depublishedRecordDao;

  /**
   * Constructor.
   *
   * @param depublishedRecordDao The DAO for depublished records.
   */
  public WorkflowPostProcessor(DepublishedRecordDao depublishedRecordDao) {
    this.depublishedRecordDao = depublishedRecordDao;
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
      depublishedRecordDao.markRecordIdsWithDepublicationStatus(datasetId,
          DepublicationStatus.PENDING_DEPUBLICATION, null);
    } else if (pluginType == PluginType.DEPUBLISH) {
      final boolean datasetDepublish = ((DepublishPluginMetadata) plugin.getPluginMetadata())
          .isDatasetDepublish();
      if (datasetDepublish) {
        //Reset depublish status if DEPUBLISH dataset
        depublishedRecordDao.markRecordIdsWithDepublicationStatus(datasetId,
            DepublicationStatus.PENDING_DEPUBLICATION, null);
      } else {
        //Set depublish status if DEPUBLISH records
        depublishedRecordDao
            .markRecordIdsWithDepublicationStatus(datasetId, DepublicationStatus.DEPUBLISHED,
                plugin.getStartedDate());
      }
    }
    LOGGER.info("Finished postprocessing of plugin {} in dataset {}.", pluginType, datasetId);
  }
}
