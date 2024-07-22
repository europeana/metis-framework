package eu.europeana.metis.core.service;

import eu.europeana.metis.core.common.TransformationParameters;
import eu.europeana.metis.core.dao.DatasetXsltDao;
import eu.europeana.metis.core.dao.DepublishRecordIdDao;
import eu.europeana.metis.core.dao.PluginWithExecutionId;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetXslt;
import eu.europeana.metis.core.dataset.DepublishRecordId;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * Class that contains various functionality for "helping" the {@link OrchestratorService}.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-10-11
 */
public class WorkflowExecutionFactory {

  private final DatasetXsltDao datasetXsltDao;
  private final DepublishRecordIdDao depublishRecordIdDao;
  private final RedirectionInferrer redirectionInferrer;

  private ValidationProperties validationExternalProperties; // Use getter and setter!
  private ValidationProperties validationInternalProperties; // Use getter and setter!
  private int defaultSamplingSizeForLinkChecking; // Use getter and setter for this field!

  /**
   * Constructor with parameters required to support the {@link OrchestratorService}
   *
   * @param datasetXsltDao the Dao instance to access the dataset xslts
   * @param depublishRecordIdDao The Dao instance to access depublish records.
   * @param redirectionInferrer the service instance to access redirection logic
   */
  public WorkflowExecutionFactory(DatasetXsltDao datasetXsltDao,
      DepublishRecordIdDao depublishRecordIdDao, RedirectionInferrer redirectionInferrer) {
    this.datasetXsltDao = datasetXsltDao;
    this.depublishRecordIdDao = depublishRecordIdDao;
    this.redirectionInferrer = redirectionInferrer;
  }

  // Expect the dataset to be synced with eCloud.
  // Does not save the workflow execution.
  WorkflowExecution createWorkflowExecution(Workflow workflow, Dataset dataset,
      PluginWithExecutionId<ExecutablePlugin> predecessor, int priority)
      throws BadContentException {

    // Create the plugins
    final List<AbstractExecutablePlugin<?>> workflowPlugins = new ArrayList<>();
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

  private AbstractExecutablePlugin<?> createWorkflowExecutionPlugin(Dataset dataset,
      PluginWithExecutionId<ExecutablePlugin> workflowPredecessor,
      AbstractExecutablePluginMetadata pluginMetadata,
      List<ExecutablePluginType> typesInWorkflowBeforeThisPlugin) throws BadContentException {

    // Add some extra configuration to the plugin metadata depending on the type.
    if (pluginMetadata instanceof TransformationPluginMetadata transformationPluginMetadata) {
      setupXsltIdForPluginMetadata(dataset, transformationPluginMetadata);
    } else if (pluginMetadata instanceof ValidationExternalPluginMetadata validationExternalPluginMetadata) {
      this.setupValidationExternalForPluginMetadata(validationExternalPluginMetadata, getValidationExternalProperties());
    } else if (pluginMetadata instanceof ValidationInternalPluginMetadata validationInternalPluginMetadata) {
      this.setupValidationInternalForPluginMetadata(validationInternalPluginMetadata, getValidationInternalProperties());
    } else if (pluginMetadata instanceof IndexToPreviewPluginMetadata indexToPreviewPluginMetadata) {
      indexToPreviewPluginMetadata.setDatasetIdsToRedirectFrom(dataset.getDatasetIdsToRedirectFrom());
      boolean performRedirects = redirectionInferrer.shouldRedirectsBePerformed(dataset, workflowPredecessor,
          ExecutablePluginType.PREVIEW, typesInWorkflowBeforeThisPlugin);
      indexToPreviewPluginMetadata.setPerformRedirects(performRedirects);
    } else if (pluginMetadata instanceof IndexToPublishPluginMetadata indexToPublishPluginMetadata) {
      indexToPublishPluginMetadata.setDatasetIdsToRedirectFrom(dataset.getDatasetIdsToRedirectFrom());
      boolean performRedirects = redirectionInferrer.shouldRedirectsBePerformed(dataset, workflowPredecessor,
          ExecutablePluginType.PUBLISH, typesInWorkflowBeforeThisPlugin);
      indexToPublishPluginMetadata.setPerformRedirects(performRedirects);
    } else if (pluginMetadata instanceof DepublishPluginMetadata depublishPluginMetadata) {
      setupDepublishPluginMetadata(dataset, depublishPluginMetadata);
    } else if (pluginMetadata instanceof LinkCheckingPluginMetadata linkCheckingPluginMetadata) {
      linkCheckingPluginMetadata.setSampleSize(getDefaultSamplingSizeForLinkChecking());
    }

    // Create the plugin
    return ExecutablePluginFactory.createPlugin(pluginMetadata);
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
    final TransformationParameters transformationParameters = new TransformationParameters(dataset);
    pluginMetadata.setDatasetName(transformationParameters.getDatasetName());
    pluginMetadata.setCountry(transformationParameters.getEdmCountry());
    pluginMetadata.setLanguage(transformationParameters.getEdmLanguage());
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
