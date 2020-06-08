package eu.europeana.metis.core.execution;

import eu.europeana.metis.core.dao.DepublishedRecordDao;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
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

    // If it is a index to publish plugin, we need to reset all the depublished record statuses.
    if (plugin.getPluginType() == PluginType.PUBLISH) {
      LOGGER.info("Starting postprocessing for an index to publish in dataset {}.", datasetId);
      depublishedRecordDao.markAllAsNotDepublished(datasetId);
      LOGGER.info("Finished postprocessing for an index to publish in dataset {}.", datasetId);
    }
  }
}
