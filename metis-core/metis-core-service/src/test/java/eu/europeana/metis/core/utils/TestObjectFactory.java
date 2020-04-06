package eu.europeana.metis.core.utils;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import eu.europeana.cloud.common.model.dps.ErrorDetails;
import eu.europeana.cloud.common.model.dps.NodeStatistics;
import eu.europeana.cloud.common.model.dps.RecordState;
import eu.europeana.cloud.common.model.dps.StatisticsReport;
import eu.europeana.cloud.common.model.dps.SubTaskInfo;
import eu.europeana.cloud.common.model.dps.TaskErrorInfo;
import eu.europeana.cloud.common.model.dps.TaskErrorsInfo;
import eu.europeana.metis.authentication.user.AccountRole;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.authentication.user.MetisUserAccessToken;
import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.core.common.Language;
import eu.europeana.metis.core.dao.WorkflowExecutionDao.ExecutionDatasetPair;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetXslt;
import eu.europeana.metis.core.rest.Record;
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledWorkflow;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePluginMetadata;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.EnrichmentPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginFactory;
import eu.europeana.metis.core.workflow.plugins.LinkCheckingPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.NormalizationPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.TransformationPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ValidationExternalPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ValidationInternalPluginMetadata;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2019-03-15
 */
public class TestObjectFactory {

  public static final int DATASETID = 100;
  public static final String XSLTID = "5a9821af34f04b794dcf63df";
  public static final String EXECUTIONID = "5a5dc67ba458bb00083d49e3";
  public static final String DATASETNAME = "datasetName";
  public static final String EMAIL = "user.metis@europeana.eu";
  public static final String AUTHORIZATION_HEADER = "Bearer 1234567890qwertyuiopasdfghjklQWE";
  public static final String TOPOLOGY_NAME = "topology_name";
  public static final long EXTERNAL_TASK_ID = 2_070_373_127_078_497_810L;
  private static final int OCCURRENCES = 2;


  private TestObjectFactory() {
  }

  /**
   * Create dummy workflow
   *
   * @return the created workflow
   */
  public static Workflow createWorkflowObject() {
    Workflow workflow = new Workflow();
    workflow.setDatasetId(Integer.toString(DATASETID));
    OaipmhHarvestPluginMetadata oaipmhHarvestPluginMetadata = new OaipmhHarvestPluginMetadata();
    oaipmhHarvestPluginMetadata.setUrl("http://example.com");
    oaipmhHarvestPluginMetadata.setEnabled(true);
    ValidationExternalPluginMetadata validationExternalPluginMetadata = new ValidationExternalPluginMetadata();
    validationExternalPluginMetadata.setEnabled(true);
    TransformationPluginMetadata transformationPluginMetadata = new TransformationPluginMetadata();
    transformationPluginMetadata.setEnabled(true);
    ValidationInternalPluginMetadata validationInternalPluginMetadata = new ValidationInternalPluginMetadata();
    validationInternalPluginMetadata.setEnabled(true);
    NormalizationPluginMetadata normalizationPluginMetadata = new NormalizationPluginMetadata();
    normalizationPluginMetadata.setEnabled(true);
    LinkCheckingPluginMetadata linkCheckingPluginMetadata = new LinkCheckingPluginMetadata();
    linkCheckingPluginMetadata.setEnabled(true);
    EnrichmentPluginMetadata enrichmentPluginMetadata = new EnrichmentPluginMetadata();
    enrichmentPluginMetadata.setEnabled(true);

    List<AbstractExecutablePluginMetadata> abstractMetisPluginMetadata = new ArrayList<>();
    abstractMetisPluginMetadata.add(oaipmhHarvestPluginMetadata);
    abstractMetisPluginMetadata.add(validationExternalPluginMetadata);
    abstractMetisPluginMetadata.add(transformationPluginMetadata);
    abstractMetisPluginMetadata.add(validationInternalPluginMetadata);
    abstractMetisPluginMetadata.add(normalizationPluginMetadata);
    abstractMetisPluginMetadata.add(linkCheckingPluginMetadata);
    abstractMetisPluginMetadata.add(enrichmentPluginMetadata);
    workflow.setMetisPluginsMetadata(abstractMetisPluginMetadata);

    return workflow;
  }

  /**
   * Create dummy workflow execution
   *
   * @return the created workflow execution
   */
  public static WorkflowExecution createWorkflowExecutionObject() {
    Dataset dataset = createDataset(DATASETNAME);
    ArrayList<AbstractMetisPlugin> abstractMetisPlugins = new ArrayList<>();
    AbstractMetisPlugin oaipmhHarvestPlugin = ExecutablePluginFactory
        .createPlugin(new OaipmhHarvestPluginMetadata());
    abstractMetisPlugins.add(oaipmhHarvestPlugin);
    AbstractMetisPlugin validationExternalPlugin = ExecutablePluginFactory
        .createPlugin(new ValidationExternalPluginMetadata());
    abstractMetisPlugins.add(validationExternalPlugin);

    WorkflowExecution workflowExecution = new WorkflowExecution(dataset, abstractMetisPlugins, 0);
    workflowExecution.setWorkflowStatus(WorkflowStatus.INQUEUE);
    workflowExecution.setCreatedDate(new Date());

    return workflowExecution;
  }

  private static WorkflowExecution createWorkflowExecutionObject(Dataset dataset) {
    WorkflowExecution workflowExecution = new WorkflowExecution(dataset, new ArrayList<>(), 0);
    workflowExecution.setWorkflowStatus(WorkflowStatus.INQUEUE);
    workflowExecution.setCreatedDate(new Date());

    return workflowExecution;
  }

  /**
   * Create a list of dummy workflow executions. The dataset name will have a suffix number for each
   * dataset.
   *
   * @param size the number of dummy workflow executions to create
   * @return the created list
   */
  public static List<WorkflowExecution> createListOfWorkflowExecutions(int size) {
    return createExecutionsWithDatasets(size).stream().map(ExecutionDatasetPair::getExecution)
        .collect(Collectors.toList());
  }

  /**
   * Create a list of dummy execution overviews. The dataset name will have a suffix number for each
   * dataset.
   *
   * @param size the number of dummy execution overviews to create
   * @return the created list
   */
  public static List<ExecutionDatasetPair> createExecutionsWithDatasets(int size) {
    final List<ExecutionDatasetPair> result = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      Dataset dataset = createDataset(String.format("%s%s", DATASETNAME, i));
      dataset.setId(new ObjectId(new Date(i)));
      dataset.setDatasetId(Integer.toString(DATASETID + i));
      WorkflowExecution workflowExecution = createWorkflowExecutionObject(dataset);
      workflowExecution.setId(new ObjectId());
      result.add(new ExecutionDatasetPair(dataset, workflowExecution));
    }
    return result;
  }

  /**
   * Create a dummy scheduled workflow
   *
   * @return the created scheduled workflow
   */
  public static ScheduledWorkflow createScheduledWorkflowObject() {
    ScheduledWorkflow scheduledWorkflow = new ScheduledWorkflow();
    scheduledWorkflow.setDatasetId(Integer.toString(DATASETID));
    scheduledWorkflow.setPointerDate(new Date());
    scheduledWorkflow.setScheduleFrequence(ScheduleFrequence.ONCE);
    scheduledWorkflow.setWorkflowPriority(0);
    return scheduledWorkflow;
  }

  /**
   * Create a list of dummy scheduled workflows. The dataset name will have a suffix number for each
   * dataset.
   *
   * @param size the number of dummy scheduled workflows to create
   * @return the created list
   */
  public static List<ScheduledWorkflow> createListOfScheduledWorkflows(int size) {
    List<ScheduledWorkflow> scheduledWorkflows = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      ScheduledWorkflow scheduledWorkflow = createScheduledWorkflowObject();
      scheduledWorkflow.setId(new ObjectId());
      scheduledWorkflow.setDatasetId(Integer.toString(DATASETID + i));
      scheduledWorkflows.add(scheduledWorkflow);
    }
    return scheduledWorkflows;
  }

  /**
   * Create a list of dummy scheduled workflows with pointer date and frequency. The dataset name
   * will have a suffix number for each dataset.
   *
   * @param size the number of dummy scheduled workflows to create
   * @param date the pointer date
   * @param scheduleFrequence the schedule frequence
   * @return the created list
   */
  public static List<ScheduledWorkflow> createListOfScheduledWorkflowsWithDateAndFrequence(
      int size, Date date, ScheduleFrequence scheduleFrequence) {
    List<ScheduledWorkflow> scheduledWorkflows = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      ScheduledWorkflow scheduledWorkflow = createScheduledWorkflowObject();
      scheduledWorkflow.setId(new ObjectId());
      scheduledWorkflow.setDatasetId(Integer.toString(DATASETID + i));
      scheduledWorkflow.setPointerDate(date);
      scheduledWorkflow.setScheduleFrequence(scheduleFrequence);
      scheduledWorkflows.add(scheduledWorkflow);
    }
    return scheduledWorkflows;
  }

  /**
   * Create a dummy dataset
   *
   * @param datasetName the dataset name to be used
   * @return the created dataset
   */
  public static Dataset createDataset(String datasetName) {
    Dataset ds = new Dataset();
    ds.setEcloudDatasetId("NOT_CREATED_YET-f525f64c-fea0-44bf-8c56-88f30962734c");
    ds.setDatasetId(Integer.toString(DATASETID));
    ds.setDatasetName(datasetName);
    final String organizationId = "1234567890";
    ds.setOrganizationId(organizationId);
    ds.setOrganizationName("OrganizationName");
    ds.setProvider(organizationId);
    ds.setIntermediateProvider(organizationId);
    ds.setDataProvider(organizationId);
    ds.setCreatedByUserId("userId");
    ds.setCreatedDate(new Date());
    ds.setUpdatedDate(new Date());
    ds.setReplacedBy("replacedBy");
    ds.setReplaces("12345");
    ds.setCountry(Country.GREECE);
    ds.setLanguage(Language.AR);
    ds.setDescription("description");
    ds.setUnfitForPublication(false);
    ds.setNotes("Notes");
    return ds;
  }

  /**
   * Create a dummy metis user
   *
   * @param email the email for the dummy user
   * @return the created metis user
   */
  public static MetisUser createMetisUser(String email) {
    MetisUser metisUser = spy(new MetisUser());
    doReturn(email).when(metisUser).getEmail();
    doReturn(AccountRole.EUROPEANA_DATA_OFFICER).when(metisUser).getAccountRole();
    doReturn("Organization_12345").when(metisUser).getOrganizationId();
    doReturn("OrganizationName").when(metisUser).getOrganizationName();
    doReturn(true).when(metisUser).isMetisUserFlag();
    doReturn("FirstName").when(metisUser).getFirstName();
    doReturn("LastName").when(metisUser).getLastName();
    doReturn("User_12345").when(metisUser).getUserId();
    return metisUser;
  }

  /**
   * Create a dummy sub task info
   *
   * @return the created sub task info
   */
  public static List<SubTaskInfo> createListOfSubTaskInfo() {
    SubTaskInfo subTaskInfo1 = new SubTaskInfo(1, "some_resource_id1", RecordState.SUCCESS, "",
        "Sensitive Information");
    final int resourceNum = 2;
    SubTaskInfo subTaskInfo2 = new SubTaskInfo(resourceNum, "some_resource_id1", RecordState.SUCCESS, "",
        "Sensitive Information");
    ArrayList<SubTaskInfo> subTaskInfos = new ArrayList<>();
    subTaskInfos.add(subTaskInfo1);
    subTaskInfos.add(subTaskInfo2);
    return subTaskInfos;
  }

  /**
   * Create a task errors info object, which contains a list of {@link TaskErrorInfo} objects.
   *
   * @param numberOfErrorTypes the number of dummy error types
   * @return the created task errors info
   */
  public static TaskErrorsInfo createTaskErrorsInfoListWithoutIdentifiers(int numberOfErrorTypes) {
    ArrayList<TaskErrorInfo> taskErrorInfos = new ArrayList<>();
    for (int i = 0; i < numberOfErrorTypes; i++) {
      TaskErrorInfo taskErrorInfo = new TaskErrorInfo("be39ef50-f77d-11e7-af0f-fa163e77119a",
          String.format("Error%s", i), OCCURRENCES);
      taskErrorInfos.add(taskErrorInfo);
    }
    return new TaskErrorsInfo(EXTERNAL_TASK_ID, taskErrorInfos);
  }

  /**
   * Create a task errors info object, which contains a list of {@link TaskErrorInfo} objects. These
   * will also contain a list of {@link ErrorDetails} that in turn contain dummy identifiers.
   *
   * @param numberOfErrorTypes the number of dummy error types
   * @return the created task errors info
   */
  public static TaskErrorsInfo createTaskErrorsInfoListWithIdentifiers(int numberOfErrorTypes) {
    ArrayList<TaskErrorInfo> taskErrorInfos = new ArrayList<>();
    for (int i = 0; i < numberOfErrorTypes; i++) {
      TaskErrorInfo taskErrorInfo = new TaskErrorInfo("be39ef50-f77d-11e7-af0f-fa163e77119a",
          String.format("Error%s", i), OCCURRENCES);
      ArrayList<ErrorDetails> errorDetails = new ArrayList<>();
      errorDetails.add(new ErrorDetails("identifier1", "error1"));
      errorDetails.add(new ErrorDetails("identifier2", "error2"));
      taskErrorInfo.setErrorDetails(errorDetails);
      taskErrorInfos.add(taskErrorInfo);
    }
    return new TaskErrorsInfo(EXTERNAL_TASK_ID, taskErrorInfos);
  }

  /**
   * Create a task errors info object, which contains a list of {@link TaskErrorInfo} objects. These
   * will also contain a list of {@link ErrorDetails} that in turn contain dummy identifiers.
   *
   * @param errorType the error type to be used for the internal {@link TaskErrorInfo}
   * @param message the message type to be used for the internal {@link TaskErrorInfo}
   * @return the created task errors info
   */
  public static TaskErrorsInfo createTaskErrorsInfoWithIdentifiers(String errorType,
      String message) {
    ArrayList<ErrorDetails> errorDetails = new ArrayList<>();
    errorDetails.add(new ErrorDetails("identifier1", "error1"));
    errorDetails.add(new ErrorDetails("identifier2", "error2"));
    TaskErrorInfo taskErrorInfo1 = new TaskErrorInfo(errorType,
        message, OCCURRENCES, errorDetails);
    ArrayList<TaskErrorInfo> taskErrorInfos = new ArrayList<>();
    taskErrorInfos.add(taskErrorInfo1);

    return new TaskErrorsInfo(EXTERNAL_TASK_ID, taskErrorInfos);
  }

  /**
   * Create a dummy {@link StatisticsReport}
   *
   * @return the created report
   */
  public static StatisticsReport createTaskStatisticsReport() {
    List<NodeStatistics> nodeStatistics = new ArrayList<>();
    nodeStatistics.add(new NodeStatistics("parentpath1", "path1", "value1", 1));
    nodeStatistics.add(new NodeStatistics("parentpath2", "path2", "value2", OCCURRENCES));
    return new StatisticsReport(EXTERNAL_TASK_ID, nodeStatistics);
  }

  /**
   * Create a dummy dataset xslt. The xslt copies a record.
   *
   * @param dataset the dataset to be used for the creation of the {@link DatasetXslt}
   * @return the created dataset xslt
   */
  public static DatasetXslt createXslt(Dataset dataset) {
    DatasetXslt datasetXslt = new DatasetXslt(dataset.getDatasetId(),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<xsl:stylesheet version=\"2.0\"\n"
            + "xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n"
            + "<xsl:template match=\"/\">\n"
            + "<xsl:copy-of select=\"node()\"/>\n"
            + "</xsl:template>\n"
            + "</xsl:stylesheet>");
    datasetXslt.setId(new ObjectId());
    return datasetXslt;
  }

  /**
   * Create a dummy list of {@link Record}s
   *
   * @param numberOfRecords the number of records to create
   * @return the created list of records
   */
  public static List<Record> createListOfRecords(int numberOfRecords) {
    List<Record> records = new ArrayList<>(numberOfRecords);
    for (int i = 0; i < numberOfRecords; i++) {
      String domain = String.format("http://some.domain.com/id/path/%s", i);
      records.add(new Record(UUID.randomUUID().toString(),
          "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
              + "<rdf:RDF xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
              + "\txmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:edm=\"http://www.europeana.eu/schemas/edm/\">\n"
              + "\t<edm:ProvidedCHO rdf:about=\"" + domain + "\">\n"
              + "\t</edm:ProvidedCHO>\n"
              + "</rdf:RDF>\n"));
    }
    return records;
  }

}
