package eu.europeana.metis.core.service;

import eu.europeana.metis.CommonStringValues;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.core.dao.DatasetXsltDao;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetXslt;
import eu.europeana.metis.core.exceptions.PluginExecutionNotAllowed;
import eu.europeana.metis.core.execution.ExecutionRules;
import eu.europeana.metis.core.workflow.ValidationProperties;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePluginMetadata;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.DataStatus;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.HTTPHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPreviewPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPublishPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.LinkCheckingPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.core.workflow.plugins.TransformationPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ValidationExternalPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ValidationInternalPluginMetadata;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;

/**
 * Class that contains various functionality for "helping" the {@link OrchestratorService}.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-10-11
 */
public class OrchestratorHelper {

  private final WorkflowExecutionDao workflowExecutionDao;
  private final DatasetXsltDao datasetXsltDao;

  private ValidationProperties validationExternalProperties; // Use getter and setter!
  private ValidationProperties validationInternalProperties; // Use getter and setter!
  private String metisCoreUrl; // Use getter and setter for this field!
  private boolean metisUseAlternativeIndexingEnvironment; // Use getter and setter for this field!
  private int defaultSamplingSizeForLinkChecking; // Use getter and setter for this field!

  /**
   * Constructor with parameters required to support the {@link OrchestratorService}
   *
   * @param workflowExecutionDao the Dao instance to access the workflowExecutions
   * @param datasetXsltDao the Dao instance to access the dataset xslts
   */
  public OrchestratorHelper(WorkflowExecutionDao workflowExecutionDao,
      DatasetXsltDao datasetXsltDao) {
    this.workflowExecutionDao = workflowExecutionDao;
    this.datasetXsltDao = datasetXsltDao;
  }

  void validateAndTrimHarvestParameters(Workflow workflow)
      throws MalformedURLException, URISyntaxException {
    OaipmhHarvestPluginMetadata oaipmhPluginMetadata = (OaipmhHarvestPluginMetadata) workflow
        .getPluginMetadata(ExecutablePluginType.OAIPMH_HARVEST);
    if (oaipmhPluginMetadata != null) {
      URL url = new URL(oaipmhPluginMetadata.getUrl().trim()); // this would check for the protocol
      URI validatedUri = url.toURI();// does the extra checking required for validation of URI

      //Remove all the query parameters
      String urlWithoutQueryParameters = new URI(validatedUri.getScheme(),
          validatedUri.getAuthority(), validatedUri.getPath(), null, null).toString();
      oaipmhPluginMetadata.setUrl(urlWithoutQueryParameters);
      oaipmhPluginMetadata.setMetadataFormat(oaipmhPluginMetadata.getMetadataFormat() == null ? null
          : oaipmhPluginMetadata.getMetadataFormat().trim());
      oaipmhPluginMetadata.setSetSpec(oaipmhPluginMetadata.getSetSpec() == null ? null
          : oaipmhPluginMetadata.getSetSpec().trim());
    }

    HTTPHarvestPluginMetadata httpHarvestPluginMetadata = (HTTPHarvestPluginMetadata) workflow
        .getPluginMetadata(ExecutablePluginType.HTTP_HARVEST);
    if (httpHarvestPluginMetadata != null) {
      URL u = new URL(
          httpHarvestPluginMetadata.getUrl().trim()); // this would check for the protocol
      u.toURI(); // does the extra checking required for validation of URI
      httpHarvestPluginMetadata.setUrl(httpHarvestPluginMetadata.getUrl().trim());
    }
  }

  private boolean doesPluginHaveAllErrorRecords(AbstractExecutablePlugin plugin) {
    return plugin != null && plugin.getExecutionProgress() != null
        && plugin.getExecutionProgress().getProcessedRecords() == plugin
        .getExecutionProgress().getErrors();
  }

  boolean addHarvestingPlugin(Dataset dataset, Workflow workflow,
      List<AbstractExecutablePlugin> metisPlugins) {
    OaipmhHarvestPluginMetadata oaipmhMetadata = (OaipmhHarvestPluginMetadata) workflow
        .getPluginMetadata(ExecutablePluginType.OAIPMH_HARVEST);
    HTTPHarvestPluginMetadata httpMetadata = (HTTPHarvestPluginMetadata) workflow
        .getPluginMetadata(ExecutablePluginType.HTTP_HARVEST);
    final AbstractExecutablePlugin plugin;
    if (oaipmhMetadata != null && oaipmhMetadata.isEnabled()) {
      plugin = createPlugin(oaipmhMetadata);
      oaipmhMetadata.setDatasetId(dataset.getDatasetId());
    } else if (httpMetadata != null && httpMetadata.isEnabled()) {
      plugin = createPlugin(httpMetadata);
      httpMetadata.setDatasetId(dataset.getDatasetId());
    } else {
      plugin = null;
    }
    if (plugin != null) {
      metisPlugins.add(plugin);
      return true;
    }
    return false;
  }

  boolean addNonHarvestPlugins(Dataset dataset, Workflow workflow,
      ExecutablePluginType enforcedPluginType, List<AbstractExecutablePlugin> metisPlugins,
      boolean firstPluginDefined) throws PluginExecutionNotAllowed {

    final List<AbstractExecutablePluginMetadata> enabledValidNonHarvestPluginMetadata = workflow
        .getMetisPluginsMetadata().stream().filter(Objects::nonNull)
        .filter(AbstractExecutablePluginMetadata::isEnabled).filter(
            metadata -> !ExecutionRules.getHarvestPluginGroup()
                .contains(metadata.getExecutablePluginType())).collect(Collectors.toList());

    for (AbstractExecutablePluginMetadata abstractExecutablePluginMetadata : enabledValidNonHarvestPluginMetadata) {
      if (abstractExecutablePluginMetadata != null && abstractExecutablePluginMetadata
          .isEnabled()) {
        firstPluginDefined = addNonHarvestPlugin(dataset, abstractExecutablePluginMetadata,
            enforcedPluginType, metisPlugins, firstPluginDefined);
      }
    }
    return firstPluginDefined;
  }

  private boolean addNonHarvestPlugin(Dataset dataset,
      AbstractExecutablePluginMetadata pluginMetadata,
      ExecutablePluginType enforcedPluginType, List<AbstractExecutablePlugin> metisPlugins,
      boolean firstPluginDefined)
      throws PluginExecutionNotAllowed {
    ExecutablePluginType pluginType = pluginMetadata.getExecutablePluginType();
    if (!firstPluginDefined) {
      AbstractExecutablePlugin previousPlugin = getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(
          dataset.getDatasetId(), pluginMetadata.getExecutablePluginType(), enforcedPluginType);
      // Set all previous revision information
      pluginMetadata.setPreviousRevisionInformation(previousPlugin);
    }

    // Sanity check
    if (ExecutionRules.getHarvestPluginGroup().contains(pluginType)) {
      //This is practically impossible to happen since the pluginMetadata has to be valid in the Workflow using a pluginType, before reaching this state.
      throw new PluginExecutionNotAllowed(CommonStringValues.PLUGIN_EXECUTION_NOT_ALLOWED);
    }

    // Add some extra configuration to the plugin metadata
    switch (pluginType) {
      case TRANSFORMATION:
        setupXsltUrlForPluginMetadata(dataset, ((TransformationPluginMetadata) pluginMetadata));
        break;
      case VALIDATION_EXTERNAL:
        this.setupValidationForPluginMetadata(pluginMetadata, getValidationExternalProperties());
        break;
      case VALIDATION_INTERNAL:
        this.setupValidationForPluginMetadata(pluginMetadata, getValidationInternalProperties());
        break;
      case PREVIEW:
        ((IndexToPreviewPluginMetadata) pluginMetadata).setDatasetId(dataset.getDatasetId());
        ((IndexToPreviewPluginMetadata) pluginMetadata).setUseAlternativeIndexingEnvironment(
            isMetisUseAlternativeIndexingEnvironment());
        break;
      case PUBLISH:
        ((IndexToPublishPluginMetadata) pluginMetadata).setDatasetId(dataset.getDatasetId());
        ((IndexToPublishPluginMetadata) pluginMetadata).setUseAlternativeIndexingEnvironment(
            isMetisUseAlternativeIndexingEnvironment());
        break;
      case LINK_CHECKING:
        ((LinkCheckingPluginMetadata) pluginMetadata)
            .setSampleSize(getDefaultSamplingSizeForLinkChecking());
        break;
      default:
        break;
    }

    // Create plugin
    final AbstractExecutablePlugin plugin = createPlugin(pluginMetadata);
    metisPlugins.add(plugin);
    firstPluginDefined = true;

    return firstPluginDefined;
  }

  private static AbstractExecutablePlugin createPlugin(
      AbstractExecutablePluginMetadata metadata) {
    final AbstractExecutablePlugin plugin = metadata.getExecutablePluginType()
        .getNewPlugin(metadata);
    plugin.setId(new ObjectId().toString() + "-" + plugin.getPluginType().name());
    plugin.setDataStatus(DataStatus.NOT_YET_GENERATED);
    return plugin;
  }

  private void setupValidationForPluginMetadata(AbstractExecutablePluginMetadata metadata,
      ValidationProperties validationProperties) {
    if (metadata instanceof ValidationExternalPluginMetadata) {
      final ValidationExternalPluginMetadata castMetadata =
          (ValidationExternalPluginMetadata) metadata;
      castMetadata.setUrlOfSchemasZip(validationProperties.getUrlOfSchemasZip());
      castMetadata.setSchemaRootPath(validationProperties.getSchemaRootPath());
      castMetadata.setSchematronRootPath(validationProperties.getSchematronRootPath());
    } else if (metadata instanceof ValidationInternalPluginMetadata) {
      final ValidationInternalPluginMetadata castMetadata =
          (ValidationInternalPluginMetadata) metadata;
      castMetadata.setUrlOfSchemasZip(validationProperties.getUrlOfSchemasZip());
      castMetadata.setSchemaRootPath(validationProperties.getSchemaRootPath());
      castMetadata.setSchematronRootPath(validationProperties.getSchematronRootPath());
    } else {
      throw new IllegalArgumentException("The provided metadata does not have the right type. "
          + "Expecting metadata for a validation plugin, but instead received metadata of type "
          + metadata.getClass().getName() + ".");
    }
  }

  boolean checkWorkflowForPluginType(Workflow workflow, ExecutablePluginType pluginType)
      throws PluginExecutionNotAllowed {
    final Set<ExecutablePluginType> pluginTypesSetThatPluginTypeCanBeBasedOn =
        ExecutionRules.getPluginTypesSetThatPluginTypeCanBeBasedOn(pluginType);
    return pluginTypeOccursOnlyAfter(workflow, pluginType,
        pluginTypesSetThatPluginTypeCanBeBasedOn);
  }

  /**
   * <p>
   * This method tests whether in this workflow all plugins of the given type occur after at least
   * one plugin of one of the given earlier types. So, more formally, it returns true if any of the
   * following occurs:
   * <ol>
   * <li>The given plugin type is part of the {@link ExecutionRules#getHarvestPluginGroup}</li>
   * <li>The workflow contains at least one plugin of the given plugin type and it's predecessor is
   * an acceptable earlier type. For a link checking plugins, it's predecessor is checked instead of
   * it's own.</li>
   * </ol>
   * </p>
   * <p>
   * Note that this method does not assume that each plugin type only occurs once.
   * </p>
   *
   * @param workflow the workflow to be checked
   * @param pluginType the plugin type that we are testing for. Cannot be null.
   * @param possibleEarlierPluginTypes the possible plugin types that has to occur before the other
   * given type. Can be null or empty (in which case the result will be false, unless it's a
   * harvesting plugin).
   * @return whether the workflow contains plugins of the two given types in the given order.
   * @throws PluginExecutionNotAllowed if the first plugin base cannot be resolved
   */
  public boolean pluginTypeOccursOnlyAfter(Workflow workflow,
      ExecutablePluginType pluginType, Set<ExecutablePluginType> possibleEarlierPluginTypes)
      throws PluginExecutionNotAllowed {
    if (pluginType == null) {
      throw new IllegalArgumentException();
    }
    if (ExecutionRules.getHarvestPluginGroup().contains(pluginType)) {
      return true;
    } else if ((possibleEarlierPluginTypes == null || possibleEarlierPluginTypes.isEmpty())) {
      return false;
    }
    boolean earlierPluginTypeFound = false;

    final List<AbstractExecutablePluginMetadata> metisPluginsMetadata = workflow
        .getMetisPluginsMetadata();
    for (int i = 0; i < metisPluginsMetadata.size(); i++) {
      final AbstractExecutablePluginMetadata plugin = metisPluginsMetadata.get(i);
      ExecutablePluginType pluginTypeInWorkflow = plugin.getExecutablePluginType();

      if (pluginTypeInWorkflow == pluginType && i == 0) {
        earlierPluginTypeFound = true;
        break;
      } else if (pluginTypeInWorkflow == pluginType) {
        break;
      }
      //Resolve source of link checking
      if (pluginTypeInWorkflow == ExecutablePluginType.LINK_CHECKING && i != 0) {
        pluginTypeInWorkflow = ExecutablePluginType
            .getPluginTypeFromEnumName(metisPluginsMetadata.get(i - 1).getPluginType().name());
      } else if (pluginTypeInWorkflow == ExecutablePluginType.LINK_CHECKING) {
        final AbstractExecutablePlugin linkCheckingBasePlugin = getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(
            workflow.getDatasetId(),
            workflow.getMetisPluginsMetadata().get(0).getExecutablePluginType(), null);
        pluginTypeInWorkflow = ExecutablePluginType
            .getPluginTypeFromEnumName(linkCheckingBasePlugin.getPluginType().name());
      }
      if (metisPluginsMetadata.get(i + 1).getExecutablePluginType() == pluginType) {
        earlierPluginTypeFound = earlierPluginTypeFound || possibleEarlierPluginTypes
            .contains(pluginTypeInWorkflow);
      }
    }
    return earlierPluginTypeFound;
  }

  void overwriteNewPluginMetadataOnWorkflowAndDisableOtherPluginMetadata(Workflow workflow,
      Workflow storedWorkflow) {
    //Overwrite only ones provided and disable the rest, already stored, plugins
    workflow.getMetisPluginsMetadata().forEach(pluginMetadata -> pluginMetadata.setEnabled(true));
    List<AbstractExecutablePluginMetadata> storedPluginsExcludingNewPlugins = storedWorkflow
        .getMetisPluginsMetadata()
        .stream().filter(pluginMetadata ->
            workflow.getPluginMetadata(pluginMetadata.getExecutablePluginType()) == null)
        .peek(pluginMetadata -> pluginMetadata.setEnabled(false))
        .collect(Collectors.toList());
    workflow.setMetisPluginsMetadata(Stream.concat(storedPluginsExcludingNewPlugins.stream(),
        workflow.getMetisPluginsMetadata().stream()).collect(Collectors.toList()));
  }

  /**
   * Finds the latest finished plugin that is not of any {@link ExecutionRules#getHarvestPluginGroup()}
   * and does not contain all records as failed. If none found with the above requirements, an
   * exception will be thrown of type {@link PluginExecutionNotAllowed}
   *
   * @param datasetId the dataset id of which the plugin is to be found
   * @param pluginType the plugin type of which a previous of that plugin is to be searched for
   * @param enforcedPluginType the plugin type that bypasses the check of the {@code pluginType}
   * value for the previous plugin search
   * @return the latest valid finished plugin
   * @throws PluginExecutionNotAllowed if no plugin was found with meeting the requirements
   */
  public AbstractExecutablePlugin getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(
      String datasetId, ExecutablePluginType pluginType, ExecutablePluginType enforcedPluginType)
      throws PluginExecutionNotAllowed {
    AbstractExecutablePlugin latestFinishedPluginIfRequestedPluginAllowedForExecution =
        ExecutionRules.getLatestFinishedPluginIfRequestedPluginAllowedForExecution(pluginType,
            enforcedPluginType, datasetId, workflowExecutionDao);
    if ((latestFinishedPluginIfRequestedPluginAllowedForExecution == null
        && !ExecutionRules.getHarvestPluginGroup().contains(pluginType))
        || this.doesPluginHaveAllErrorRecords(
        latestFinishedPluginIfRequestedPluginAllowedForExecution)) {
      throw new PluginExecutionNotAllowed(CommonStringValues.PLUGIN_EXECUTION_NOT_ALLOWED);
    }
    return latestFinishedPluginIfRequestedPluginAllowedForExecution;
  }

  private void setupXsltUrlForPluginMetadata(Dataset dataset,
      TransformationPluginMetadata pluginMetadata) {
    DatasetXslt xsltObject;
    if (pluginMetadata.isCustomXslt()) {
      xsltObject = datasetXsltDao.getById(dataset.getXsltId().toString());
    } else {
      xsltObject = datasetXsltDao.getLatestDefaultXslt();
    }
    if (xsltObject != null && StringUtils.isNotEmpty(xsltObject.getXslt())) {
      pluginMetadata.setXsltUrl(getMetisCoreUrl() + RestEndpoints
          .resolve(RestEndpoints.DATASETS_XSLT_XSLTID,
              Collections.singletonList(xsltObject.getId().toString())));
    }
    pluginMetadata.setDatasetId(dataset.getDatasetId());
    //DatasetName in Transformation should be a concatenation datasetId_datasetName
    pluginMetadata.setDatasetName(dataset.getDatasetId() + "_" + dataset.getDatasetName());
    pluginMetadata.setCountry(dataset.getCountry().getName());
    pluginMetadata.setLanguage(dataset.getLanguage().name().toLowerCase(Locale.US));
  }

  Pair<WorkflowExecution, AbstractMetisPlugin> getPreviousExecutionAndPlugin(
      AbstractMetisPlugin plugin, String datasetId) {

    // Check whether we are at the end of the chain.
    final Date previousPluginTimestamp = plugin.getPluginMetadata()
        .getRevisionTimestampPreviousPlugin();
    final PluginType previousPluginType = PluginType.getPluginTypeFromEnumName(
        plugin.getPluginMetadata().getRevisionNamePreviousPlugin());
    if (previousPluginTimestamp == null || previousPluginType == null) {
      return null;
    }

    // Obtain the previous execution and plugin.
    final WorkflowExecution previousExecution = workflowExecutionDao
        .getByTaskExecution(previousPluginTimestamp, previousPluginType, datasetId);
    final AbstractMetisPlugin previousPlugin = previousExecution == null ? null
        : previousExecution.getMetisPluginWithType(previousPluginType).orElse(null);
    if (previousExecution == null || previousPlugin == null) {
      return null;
    }

    // Done
    return new ImmutablePair<>(previousExecution, previousPlugin);
  }

  boolean listContainsDuplicates(List<AbstractExecutablePluginMetadata> list) {
    final Set<PluginType> collect = list.stream()
        .map(AbstractExecutablePluginMetadata::getPluginType).collect(Collectors.toSet());
    return collect.size() != list.size();
  }

  public ValidationProperties getValidationExternalProperties() {
    synchronized (this) {
      return validationExternalProperties;
    }
  }

  public void setValidationExternalProperties(ValidationProperties validationExternalProperties) {
    synchronized (this) {
      this.validationExternalProperties = validationExternalProperties;
    }
  }

  public ValidationProperties getValidationInternalProperties() {
    synchronized (this) {
      return validationInternalProperties;
    }
  }

  public void setValidationInternalProperties(ValidationProperties validationInternalProperties) {
    synchronized (this) {
      this.validationInternalProperties = validationInternalProperties;
    }
  }

  public void setMetisCoreUrl(String metisCoreUrl) {
    synchronized (this) {
      this.metisCoreUrl = metisCoreUrl;
    }
  }

  private String getMetisCoreUrl() {
    synchronized (this) {
      return this.metisCoreUrl;
    }
  }

  private boolean isMetisUseAlternativeIndexingEnvironment() {
    synchronized (this) {
      return metisUseAlternativeIndexingEnvironment;
    }
  }

  public void setMetisUseAlternativeIndexingEnvironment(
      boolean metisUseAlternativeIndexingEnvironment) {
    synchronized (this) {
      this.metisUseAlternativeIndexingEnvironment = metisUseAlternativeIndexingEnvironment;
    }
  }

  private int getDefaultSamplingSizeForLinkChecking() {
    synchronized (this) {
      return defaultSamplingSizeForLinkChecking;
    }
  }

  public void setDefaultSamplingSizeForLinkChecking(int defaultSamplingSizeForLinkChecking) {
    synchronized (this) {
      this.defaultSamplingSizeForLinkChecking = defaultSamplingSizeForLinkChecking;
    }
  }
}
