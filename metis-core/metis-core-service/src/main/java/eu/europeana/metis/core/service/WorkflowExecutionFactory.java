package eu.europeana.metis.core.service;

import eu.europeana.metis.core.dao.DatasetXsltDao;
import eu.europeana.metis.core.dao.DepublishRecordIdDao;
import eu.europeana.metis.core.dao.PluginWithExecutionId;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.dao.WorkflowUtils;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetXslt;
import eu.europeana.metis.core.dataset.DepublishRecordId.DepublicationStatus;
import eu.europeana.metis.core.util.DepublishRecordIdSortField;
import eu.europeana.metis.core.util.SortDirection;
import eu.europeana.metis.core.workflow.ValidationProperties;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePluginMetadata;
import eu.europeana.metis.core.workflow.plugins.DepublishPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginFactory;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.IndexToPreviewPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPublishPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.LinkCheckingPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.TransformationPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ValidationExternalPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ValidationInternalPluginMetadata;
import eu.europeana.metis.exception.BadContentException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BooleanSupplier;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

/**
 * Class that contains various functionality for "helping" the {@link OrchestratorService}.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-10-11
 */
public class WorkflowExecutionFactory {

  private final DatasetXsltDao datasetXsltDao;
  private final DepublishRecordIdDao depublishRecordIdDao;
  private final WorkflowExecutionDao workflowExecutionDao;
  private final WorkflowUtils workflowUtils;

  private ValidationProperties validationExternalProperties; // Use getter and setter!
  private ValidationProperties validationInternalProperties; // Use getter and setter!
  private boolean metisUseAlternativeIndexingEnvironment; // Use getter and setter for this field!
  private int defaultSamplingSizeForLinkChecking; // Use getter and setter for this field!

  /**
   * Constructor with parameters required to support the {@link OrchestratorService}
   *
   * @param datasetXsltDao the Dao instance to access the dataset xslts
   * @param depublishRecordIdDao The Dao instance to access depublish records.
   * @param workflowExecutionDao the Dao instance to access the workflow executions
   * @param workflowUtils the utilities class for workflow operations
   */
  public WorkflowExecutionFactory(DatasetXsltDao datasetXsltDao,
      DepublishRecordIdDao depublishRecordIdDao, WorkflowExecutionDao workflowExecutionDao,
      WorkflowUtils workflowUtils) {
    this.datasetXsltDao = datasetXsltDao;
    this.depublishRecordIdDao = depublishRecordIdDao;
    this.workflowExecutionDao = workflowExecutionDao;
    this.workflowUtils = workflowUtils;
  }

  // Expect the dataset to be synced with eCloud.
  // Does not save the workflow execution.
  WorkflowExecution createWorkflowExecution(Workflow workflow, Dataset dataset,
      PluginWithExecutionId<ExecutablePlugin> predecessor, int priority)
      throws BadContentException {

    // Create the plugins
    final List<AbstractExecutablePlugin> workflowPlugins = new ArrayList<>();
    final List<ExecutablePluginType> typesInWorkflow = new ArrayList<>();
    for (AbstractExecutablePluginMetadata pluginMetadata : workflow.getMetisPluginsMetadata()) {
      if (pluginMetadata.isEnabled()) {
        workflowPlugins.add(
            createWorkflowExecutionPlugin(dataset, predecessor, pluginMetadata, typesInWorkflow));
        typesInWorkflow.add(pluginMetadata.getExecutablePluginType());
      }
    }

    // Set the predecessor
    if (predecessor != null) {
      workflowPlugins.get(0).getPluginMetadata()
          .setPreviousRevisionInformation(predecessor.getPlugin());
    }

    // Done: create workflow with all the information.
    return new WorkflowExecution(dataset, workflowPlugins, priority);
  }

  private AbstractExecutablePlugin createWorkflowExecutionPlugin(Dataset dataset,
      PluginWithExecutionId<ExecutablePlugin> workflowPredecessor,
      AbstractExecutablePluginMetadata pluginMetadata,
      List<ExecutablePluginType> typesInWorkflowBeforeThisPlugin) throws BadContentException {

    // Add some extra configuration to the plugin metadata depending on the type.
    if (pluginMetadata instanceof TransformationPluginMetadata) {
      setupXsltIdForPluginMetadata(dataset, ((TransformationPluginMetadata) pluginMetadata));
    } else if (pluginMetadata instanceof ValidationExternalPluginMetadata) {
      this.setupValidationExternalForPluginMetadata(
          (ValidationExternalPluginMetadata) pluginMetadata, getValidationExternalProperties());
    } else if (pluginMetadata instanceof ValidationInternalPluginMetadata) {
      this.setupValidationInternalForPluginMetadata(
          (ValidationInternalPluginMetadata) pluginMetadata, getValidationInternalProperties());
    } else if (pluginMetadata instanceof IndexToPreviewPluginMetadata) {
      ((IndexToPreviewPluginMetadata) pluginMetadata)
          .setUseAlternativeIndexingEnvironment(isMetisUseAlternativeIndexingEnvironment());
      ((IndexToPreviewPluginMetadata) pluginMetadata)
          .setDatasetIdsToRedirectFrom(dataset.getDatasetIdsToRedirectFrom());
      boolean performRedirects = shouldRedirectsBePerformed(dataset, workflowPredecessor,
          ExecutablePluginType.PREVIEW, typesInWorkflowBeforeThisPlugin);
      ((IndexToPreviewPluginMetadata) pluginMetadata).setPerformRedirects(performRedirects);
    } else if (pluginMetadata instanceof IndexToPublishPluginMetadata) {
      ((IndexToPublishPluginMetadata) pluginMetadata)
          .setUseAlternativeIndexingEnvironment(isMetisUseAlternativeIndexingEnvironment());
      ((IndexToPublishPluginMetadata) pluginMetadata)
          .setDatasetIdsToRedirectFrom(dataset.getDatasetIdsToRedirectFrom());
      boolean performRedirects = shouldRedirectsBePerformed(dataset, workflowPredecessor,
          ExecutablePluginType.PUBLISH, typesInWorkflowBeforeThisPlugin);
      ((IndexToPublishPluginMetadata) pluginMetadata).setPerformRedirects(performRedirects);
    } else if (pluginMetadata instanceof DepublishPluginMetadata) {
      ((DepublishPluginMetadata) pluginMetadata)
          .setUseAlternativeIndexingEnvironment(isMetisUseAlternativeIndexingEnvironment());
      setupDepublishPluginMetadata(dataset, ((DepublishPluginMetadata) pluginMetadata));
    } else if (pluginMetadata instanceof LinkCheckingPluginMetadata) {
      ((LinkCheckingPluginMetadata) pluginMetadata)
          .setSampleSize(getDefaultSamplingSizeForLinkChecking());
    }

    // Create the plugin
    return ExecutablePluginFactory.createPlugin(pluginMetadata);
  }

  /**
   * Determines whether to apply redirection as part of the given plugin. We apply the following
   * heuristics to determining this, based on the information available to us, and erring on the
   * side of caution in the sense that it is better to perform it once too many than once too few:
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
   * @param workflowPredecessor The plugin on which the new workflow is based as a predecessor. Can
   * be null.
   * @param executablePluginType The type of the plugin as part of which we may wish to perform
   * redirection.
   * @param typesInWorkflowBeforeThisPlugin The types of the plugins that come before this plugin in
   * the new workflow.
   * @return Whether to apply redirection as part of this plugin.
   */
  private boolean shouldRedirectsBePerformed(Dataset dataset,
      PluginWithExecutionId<ExecutablePlugin> workflowPredecessor,
      ExecutablePluginType executablePluginType,
      List<ExecutablePluginType> typesInWorkflowBeforeThisPlugin) {

    // Get some history from the database: find the latest successful plugin of the same type.
    final PluginWithExecutionId<ExecutablePlugin> latestSuccessfulPlugin = workflowExecutionDao
        .getLatestSuccessfulExecutablePlugin(dataset.getDatasetId(),
            EnumSet.of(executablePluginType), true);

    // Check if we can find the answer in the workflow itself. Iterate backwards and see what we find.
    for (int i = typesInWorkflowBeforeThisPlugin.size() - 1; i >= 0; i--) {
      final ExecutablePluginType type = typesInWorkflowBeforeThisPlugin.get(i);
      if (WorkflowUtils.getHarvestPluginGroup().contains(type)) {
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
          || !workflowUtils.getRootAncestor(latestSuccessfulPlugin)
          .equals(workflowUtils.getRootAncestor(workflowPredecessor));

      // In either of these situations, we perform a redirect.
      performRedirect =
          datasetUpdatedSinceLatestPlugin || rootDiffersForLatestPlugin.getAsBoolean();
    }

    // Done
    return performRedirect;
  }

  private void setupValidationExternalForPluginMetadata(ValidationExternalPluginMetadata metadata,
      ValidationProperties validationProperties) {
    metadata.setUrlOfSchemasZip(validationProperties.getUrlOfSchemasZip());
    metadata.setSchemaRootPath(validationProperties.getSchemaRootPath());
    metadata.setSchematronRootPath(validationProperties.getSchematronRootPath());
  }

  private void setupValidationInternalForPluginMetadata(ValidationInternalPluginMetadata metadata,
      ValidationProperties validationProperties) {
    metadata.setUrlOfSchemasZip(validationProperties.getUrlOfSchemasZip());
    metadata.setSchemaRootPath(validationProperties.getSchemaRootPath());
    metadata.setSchematronRootPath(validationProperties.getSchematronRootPath());
  }

  private void setupXsltIdForPluginMetadata(Dataset dataset,
      TransformationPluginMetadata pluginMetadata) {
    DatasetXslt xsltObject;
    if (pluginMetadata.isCustomXslt()) {
      xsltObject = datasetXsltDao.getById(dataset.getXsltId().toString());
    } else {
      xsltObject = datasetXsltDao.getLatestDefaultXslt();
    }
    if (xsltObject != null && StringUtils.isNotEmpty(xsltObject.getXslt())) {
      pluginMetadata.setXsltId(xsltObject.getId().toString());
    }
    //DatasetName in Transformation should be a concatenation datasetId_datasetName
    pluginMetadata.setDatasetName(dataset.getDatasetId() + "_" + dataset.getDatasetName());
    pluginMetadata.setCountry(dataset.getCountry().getName());
    pluginMetadata.setLanguage(dataset.getLanguage().name().toLowerCase(Locale.US));
  }

  private void setupDepublishPluginMetadata(Dataset dataset, DepublishPluginMetadata pluginMetadata)
      throws BadContentException {
    final Set<String> recordIdsToDepublish = pluginMetadata.getRecordIdsToDepublish();
    if (!pluginMetadata.isDatasetDepublish()) {
      final Set<String> pendingDepublicationIds;
      if (recordIdsToDepublish.isEmpty()) {
        //Get all record ids that are marked as PENDING_DEPUBLICATION in the database
        pendingDepublicationIds = depublishRecordIdDao
            .getAllDepublishRecordIdsWithStatus(dataset.getDatasetId(),
                DepublishRecordIdSortField.DEPUBLICATION_STATE, SortDirection.ASCENDING,
                DepublicationStatus.PENDING_DEPUBLICATION);
      } else {
        //Match provided record ids that are marked as PENDING_DEPUBLICATION in the database
        pendingDepublicationIds = depublishRecordIdDao
            .getAllDepublishRecordIdsWithStatus(dataset.getDatasetId(),
                DepublishRecordIdSortField.DEPUBLICATION_STATE, SortDirection.ASCENDING,
                DepublicationStatus.PENDING_DEPUBLICATION, recordIdsToDepublish);
      }
      pluginMetadata.setRecordIdsToDepublish(pendingDepublicationIds);
    }
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
