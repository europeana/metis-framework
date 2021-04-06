package eu.europeana.metis.core.dao;

import eu.europeana.metis.core.dataset.DepublishRecordId.DepublicationStatus;
import eu.europeana.metis.core.exceptions.PluginExecutionNotAllowed;
import eu.europeana.metis.core.util.DepublishRecordIdSortField;
import eu.europeana.metis.core.util.SortDirection;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePluginMetadata;
import eu.europeana.metis.core.workflow.plugins.DepublishPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.HTTPHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.GenericMetisException;
import eu.europeana.metis.utils.CommonStringValues;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.util.CollectionUtils;

/**
 * This class is a utility class that can answer questions related to the validation of workflows.
 */
public class WorkflowValidationUtils {

  private final DepublishRecordIdDao depublishRecordIdDao;
  private final DataEvolutionUtils dataEvolutionUtils;

  /**
   * Constructor.
   *
   * @param depublishRecordIdDao the depublication record id dao
   * @param dataEvolutionUtils The utilities class for sorting out data evolution
   */
  public WorkflowValidationUtils(DepublishRecordIdDao depublishRecordIdDao,
          DataEvolutionUtils dataEvolutionUtils) {
    this.depublishRecordIdDao = depublishRecordIdDao;
    this.dataEvolutionUtils = dataEvolutionUtils;
  }

  /**
   * This method validates the workflow plugin sequence. In particular, it checks:
   * <ol>
   * <li>That the workflow is not empty and contains plugins with valid types,</li>
   * <li>That the first plugin is not link checking (except when it is the only plugin),</li>
   * <li>That no two plugins of the same type occur in the workflow,</li>
   * <li>That if depublish is enabled no other plugins are allowed in the workflow,</li>
   * <li>That the first plugin has a valid predecessor plugin in the dataset's history (as defined by
   * {@link DataEvolutionUtils#getPredecessorTypes(ExecutablePluginType)}), the type of which can be
   * overridden by the enforced predecessor type, and the root plugin (i.e. harvest) of which is
   * equal to the latest successful harvest (i.e. no old data should be processed after new data has
   * been introduced),</li>
   * <li>That all subsequent plugins have a valid predecessor within the workflow (as defined by
   * {@link DataEvolutionUtils#getPredecessorTypes(ExecutablePluginType)}),</li>
   * <li>That harvesting plugins have valid URL settings.</li>
   * </ol>
   *
   * @param workflow The workflow to validate.
   * @param enforcedPredecessorType If not null, overrides the predecessor type of the first
   * plugin.
   * @return The predecessor of the first plugin. Or null if no predecessor is required.
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link BadContentException} In case the workflow is empty, or contains plugins with
   * invalid types.</li>
   * <li>{@link PluginExecutionNotAllowed} in case the plugin sequence as provided is not
   * allowed.</li>
   * </ul>
   */
  public PluginWithExecutionId<ExecutablePlugin> validateWorkflowPlugins(Workflow workflow,
          ExecutablePluginType enforcedPredecessorType) throws GenericMetisException {

    // Workflow should have a plugin list.
    if (workflow.getMetisPluginsMetadata() == null) {
      throw new BadContentException("Workflow should not be empty.");
    }

    // Compile the list of enabled plugins.
    final List<AbstractExecutablePluginMetadata> enabledPlugins = workflow.getMetisPluginsMetadata()
            .stream().filter(ExecutablePluginMetadata::isEnabled).collect(Collectors.toList());

    // Workflow should not be empty and all should have a type.
    if (enabledPlugins.isEmpty()) {
      throw new BadContentException("Workflow should not be empty.");
    }
    if (enabledPlugins.stream().map(AbstractExecutablePluginMetadata::getExecutablePluginType)
            .anyMatch(Objects::isNull)) {
      throw new BadContentException(
              "There are enabled plugins of which the type could not be determined.");
    }

    // Validate dataset/record depublication
    validateDepublishPlugin(workflow.getDatasetId(), enabledPlugins);

    // Validate and normalize the harvest parameters of harvest plugins (even if not enabled)
    validateAndTrimHarvestParameters(workflow.getDatasetId(), enabledPlugins);

    // Check that first plugin is not link checking (except if it is the only plugin)
    if (enabledPlugins.size() > 1
            && enabledPlugins.get(0).getPluginType() == PluginType.LINK_CHECKING) {
      throw new PluginExecutionNotAllowed(CommonStringValues.PLUGIN_EXECUTION_NOT_ALLOWED);
    }

    // Make sure that all enabled plugins (except the first) have a predecessor within the workflow.
    final EnumSet<ExecutablePluginType> previousTypesInWorkflow = EnumSet
            .of(enabledPlugins.get(0).getExecutablePluginType());
    for (int i = 1; i < enabledPlugins.size(); i++) {

      // Find the permissible predecessors
      final ExecutablePluginType pluginType = enabledPlugins.get(i).getExecutablePluginType();
      final Set<ExecutablePluginType> permissiblePredecessors = DataEvolutionUtils
              .getPredecessorTypes(pluginType);

      // Check if we have the right predecessor plugin types in the workflow
      final boolean hasNoPredecessor = !permissiblePredecessors.isEmpty() &&
              permissiblePredecessors.stream().noneMatch(previousTypesInWorkflow::contains);
      if (hasNoPredecessor) {
        throw new PluginExecutionNotAllowed(CommonStringValues.PLUGIN_EXECUTION_NOT_ALLOWED);
      }

      // Add the plugin type to those we have seen
      previousTypesInWorkflow.add(pluginType);
    }

    // We should now have seen all types. Make sure that there are no duplicates
    if (previousTypesInWorkflow.size() != enabledPlugins.size()) {
      throw new PluginExecutionNotAllowed(CommonStringValues.PLUGIN_EXECUTION_NOT_ALLOWED);
    }

    // Check the presence of the predecessor and return it.
    return dataEvolutionUtils
            .computePredecessorPlugin(enabledPlugins.get(0).getExecutablePluginType(),
                    enforcedPredecessorType, workflow.getDatasetId());
  }

  private void validateAndTrimHarvestParameters(String datasetId,
          Iterable<AbstractExecutablePluginMetadata> enabledPlugins) throws BadContentException {
    for (AbstractExecutablePluginMetadata pluginMetadata : enabledPlugins) {
      if (pluginMetadata instanceof OaipmhHarvestPluginMetadata) {
        final OaipmhHarvestPluginMetadata oaipmhMetadata = (OaipmhHarvestPluginMetadata) pluginMetadata;
        final URI validatedUri = validateUrl(oaipmhMetadata.getUrl());
        oaipmhMetadata
                .setUrl(new URIBuilder(validatedUri).removeQuery().setFragment(null).toString());
        oaipmhMetadata.setMetadataFormat(oaipmhMetadata.getMetadataFormat() == null ? null
                : oaipmhMetadata.getMetadataFormat().trim());
        oaipmhMetadata.setSetSpec(
                oaipmhMetadata.getSetSpec() == null ? null : oaipmhMetadata.getSetSpec().trim());
        if (oaipmhMetadata.isIncrementalHarvest() && !isIncrementalHarvestingAllowed(datasetId)) {
          throw new BadContentException("Can't perform incremental harvesting for this dataset.");
        }
      }
      if (pluginMetadata instanceof HTTPHarvestPluginMetadata) {
        final HTTPHarvestPluginMetadata httpMetadata = (HTTPHarvestPluginMetadata) pluginMetadata;
        httpMetadata.setUrl(validateUrl(httpMetadata.getUrl()).toString());
      }
    }
  }

  /**
   * This method returns whether currently it is permitted/possible to perform incremental
   * harvesting for the given dataset.
   *
   * @param datasetId The ID of the dataset for which to check.
   * @return Whether we can perform incremental harvesting for the dataset.
   */
  public boolean isIncrementalHarvestingAllowed(String datasetId) {
    // We need to do the entire analysis to make sure that all publish actions are consistent.
    return !CollectionUtils.isEmpty(dataEvolutionUtils.getPublishedHarvestIncrements(datasetId));
  }

  private void validateDepublishPlugin(String datasetId,
          List<AbstractExecutablePluginMetadata> enabledPlugins) throws BadContentException {
    // If depublish requested, make sure it's the only plugin in the workflow
    final Optional<DepublishPluginMetadata> depublishPluginMetadata = enabledPlugins.stream()
            .filter(plugin -> plugin.getExecutablePluginType().toPluginType() == PluginType.DEPUBLISH)
            .map(plugin -> (DepublishPluginMetadata) plugin).findFirst();
    if (enabledPlugins.size() > 1 && depublishPluginMetadata.isPresent()) {
      throw new BadContentException(
              "If DEPUBLISH plugin enabled, no other enabled plugins are allowed.");
    }

    // If record depublication requested, check if there are pending record ids in the db
    if (depublishPluginMetadata.isPresent() && !depublishPluginMetadata.get()
            .isDatasetDepublish()) {
      final Set<String> pendingDepublicationIds = depublishRecordIdDao
              .getAllDepublishRecordIdsWithStatus(datasetId,
                      DepublishRecordIdSortField.DEPUBLICATION_STATE, SortDirection.ASCENDING,
                      DepublicationStatus.PENDING_DEPUBLICATION);
      if (CollectionUtils.isEmpty(pendingDepublicationIds)) {
        throw new BadContentException(
                "Record depublication requested but there are no pending depublication record ids in the db");
      }
    }
  }

  private static URI validateUrl(String urlString) throws BadContentException {
    if (urlString == null) {
      throw new BadContentException("Harvesting parameters are missing");
    }
    try {
      return new URL(urlString.trim()).toURI();
    } catch (MalformedURLException | URISyntaxException e) {
      throw new BadContentException("Harvesting parameters are invalid", e);
    }
  }
}
