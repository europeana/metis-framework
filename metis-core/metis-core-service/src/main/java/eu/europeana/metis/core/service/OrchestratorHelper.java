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
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginFactory;
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
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.GenericMetisException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

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

  private void validateAndTrimHarvestParameters(Workflow workflow) throws BadContentException {
    try {
      OaipmhHarvestPluginMetadata oaipmhPluginMetadata = (OaipmhHarvestPluginMetadata) workflow
          .getPluginMetadata(ExecutablePluginType.OAIPMH_HARVEST);
      if (oaipmhPluginMetadata != null) {
        // this would check for the protocol
        URL url = new URL(oaipmhPluginMetadata.getUrl().trim());
        // does the extra checking required for validation of URI
        URI validatedUri = url.toURI();

        //Remove all the query parameters
        String urlWithoutQueryParameters = new URI(validatedUri.getScheme(),
            validatedUri.getAuthority(), validatedUri.getPath(), null, null).toString();
        oaipmhPluginMetadata.setUrl(urlWithoutQueryParameters);
        oaipmhPluginMetadata.setMetadataFormat(oaipmhPluginMetadata.getMetadataFormat() == null ?
            null : oaipmhPluginMetadata.getMetadataFormat().trim());
        oaipmhPluginMetadata.setSetSpec(oaipmhPluginMetadata.getSetSpec() == null ? null
            : oaipmhPluginMetadata.getSetSpec().trim());
      }

      HTTPHarvestPluginMetadata httpHarvestPluginMetadata = (HTTPHarvestPluginMetadata) workflow
          .getPluginMetadata(ExecutablePluginType.HTTP_HARVEST);
      if (httpHarvestPluginMetadata != null) {
        // this would check for the protocol
        URL u = new URL(httpHarvestPluginMetadata.getUrl().trim());
        // does the extra checking required for validation of URI
        u.toURI();
        httpHarvestPluginMetadata.setUrl(httpHarvestPluginMetadata.getUrl().trim());
      }
    } catch (MalformedURLException | URISyntaxException e) {
      throw new BadContentException("Harvesting parameters are invalid", e);
    }
  }

  boolean addHarvestingPlugin(Workflow workflow, List<AbstractExecutablePlugin> metisPlugins) {
    OaipmhHarvestPluginMetadata oaipmhMetadata = (OaipmhHarvestPluginMetadata) workflow
        .getPluginMetadata(ExecutablePluginType.OAIPMH_HARVEST);
    HTTPHarvestPluginMetadata httpMetadata = (HTTPHarvestPluginMetadata) workflow
        .getPluginMetadata(ExecutablePluginType.HTTP_HARVEST);
    final AbstractExecutablePlugin plugin;
    if (oaipmhMetadata != null && oaipmhMetadata.isEnabled()) {
      plugin = ExecutablePluginFactory.createPlugin(oaipmhMetadata);
    } else if (httpMetadata != null && httpMetadata.isEnabled()) {
      plugin = ExecutablePluginFactory.createPlugin(httpMetadata);
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
      AbstractExecutablePlugin previousPlugin = ExecutionRules
          .getPredecessorPlugin(pluginMetadata.getExecutablePluginType(), enforcedPluginType,
              dataset.getDatasetId(), this.workflowExecutionDao);
      // Set all previous revision information
      // TODO JV do this for all plugins, not just the first one. It's currently done in the WorkflowExecutor.
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
        ((IndexToPreviewPluginMetadata) pluginMetadata).setUseAlternativeIndexingEnvironment(
            isMetisUseAlternativeIndexingEnvironment());
        break;
      case PUBLISH:
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
    final AbstractExecutablePlugin plugin = ExecutablePluginFactory.createPlugin(pluginMetadata);
    metisPlugins.add(plugin);
    firstPluginDefined = true;

    return firstPluginDefined;
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

  void validateWorkflow(Workflow workflow) throws GenericMetisException {
    validateAndTrimHarvestParameters(workflow);
    ExecutionRules.validateWorkflowPlugins(workflow);
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
