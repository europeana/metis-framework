package eu.europeana.metis.core.service;

import eu.europeana.metis.core.dao.DataEvolutionUtils;
import eu.europeana.metis.core.dao.PluginWithExecutionId;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.workflow.plugins.ExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import java.util.EnumSet;
import java.util.List;
import java.util.function.BooleanSupplier;
import org.springframework.util.CollectionUtils;

public class RedirectionInferrer {

  private final WorkflowExecutionDao workflowExecutionDao;
  private final DataEvolutionUtils dataEvolutionUtils;

  public RedirectionInferrer(WorkflowExecutionDao workflowExecutionDao, DataEvolutionUtils dataEvolutionUtils) {
    this.workflowExecutionDao = workflowExecutionDao;
    this.dataEvolutionUtils = dataEvolutionUtils;
  }

  /**
   * Determines whether to apply redirection as part of the given plugin. We apply the following heuristics to determining this,
   * based on the information available to us, and erring on the side of caution in the sense that it is better to perform it once
   * too many than once too few:
   * <ol>
   * <li>
   * If this is the first plugin of its kind for this dataset, we check for redirects if and only if
   * the dataset properties specify any datasets to redirect from.
   * </li>
   * <li>
   * If this is not the first plugin of its kind:
   * <ol type="a">
   * <li>
   * If a harvesting occurred after the last plugin of the same kind we assume that the records may
   * have changed and/or moved and we perform a redirection.
   * </li>
   * <li>
   * If the dataset properties (which includes the list of datasets to redirect from) have changed
   * since the last plugin of the same kind we assume that the list of datasets to redirect from may
   * have changed and we perform a redirection if and only if the dataset properties specify any
   * datasets to redirect from.
   * </li>
   * </ol>
   * </li>
   * </ol>
   * If none of these conditions apply, we do not check for redirects.
   *
   * @param dataset The dataset.
   * @param workflowPredecessor The plugin on which the new workflow is based as a predecessor. Can be null.
   * @param executablePluginType The type of the plugin as part of which we may wish to perform redirection.
   * @param typesInWorkflowBeforeThisPlugin The types of the plugins that come before this plugin in the new workflow.
   * @return Whether to apply redirection as part of this plugin.
   */
  public boolean shouldRedirectsBePerformed(Dataset dataset,
      PluginWithExecutionId<ExecutablePlugin> workflowPredecessor,
      ExecutablePluginType executablePluginType,
      List<ExecutablePluginType> typesInWorkflowBeforeThisPlugin) {

    // Get some history from the database: find the latest successful plugin of the same type.
    // Note: we don't limit to valid data: perhaps the data is deprecated after reindexing.
    final PluginWithExecutionId<ExecutablePlugin> latestSuccessfulPlugin = workflowExecutionDao
        .getLatestSuccessfulExecutablePlugin(dataset.getDatasetId(),
            EnumSet.of(executablePluginType), false);

    // Check if we can find the answer in the workflow itself. Iterate backwards and see what we find.
    for (int i = typesInWorkflowBeforeThisPlugin.size() - 1; i >= 0; i--) {
      final ExecutablePluginType type = typesInWorkflowBeforeThisPlugin.get(i);
      if (DataEvolutionUtils.getHarvestPluginGroup().contains(type)) {
        // If we find a harvest (occurring after any plugin of this type),
        // we know we need to perform redirects only if there is a non null latest successful plugin or there are datasets to redirect from.
        return latestSuccessfulPlugin != null || !CollectionUtils
            .isEmpty(dataset.getDatasetIdsToRedirectFrom());
      }
      if (type == executablePluginType) {
        // If we find another plugin of the same type (after any harvest) we know we don't need to perform redirect.
        return false;
      }
    }

    // If we have a previous execution of this plugin, we see if things have changed since then.
    final boolean performRedirect;
    if (latestSuccessfulPlugin == null) {
      // If it's the first plugin execution, just check if dataset ids to redirect from are present.
      performRedirect = !CollectionUtils.isEmpty(dataset.getDatasetIdsToRedirectFrom());
    } else {
      // Check if since the latest plugin's execution, the dataset information is updated and (now)
      // contains dataset ids to redirect from.
      final boolean datasetUpdatedSinceLatestPlugin = dataset.getUpdatedDate() != null &&
          dataset.getUpdatedDate().compareTo(latestSuccessfulPlugin.getPlugin().getFinishedDate())
              >= 0 && !CollectionUtils.isEmpty(dataset.getDatasetIdsToRedirectFrom());

      // Check if the latest plugin execution is based on a different harvest as this one will be.
      // If this plugin's harvest cannot be determined, assume it is not the same (this shouldn't
      // happen as we checked the workflow already). This is a lambda: we wish to evaluate on demand.
      final BooleanSupplier rootDiffersForLatestPlugin = () -> workflowPredecessor == null
          || !dataEvolutionUtils.getRootAncestor(latestSuccessfulPlugin)
                                .equals(dataEvolutionUtils.getRootAncestor(workflowPredecessor));

      // In either of these situations, we perform a redirect.
      performRedirect = datasetUpdatedSinceLatestPlugin || rootDiffersForLatestPlugin.getAsBoolean();
    }

    // Done
    return performRedirect;
  }
}
