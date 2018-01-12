package eu.europeana.metis.core.test.utils;

import eu.europeana.metis.authentication.user.AccountRole;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.authentication.user.MetisUserAccessToken;
import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.core.common.Language;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetStatus;
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledWorkflow;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.EnrichmentPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPluginMetadata;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.bson.types.ObjectId;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-04
 */
public class TestObjectFactory {

  public static final int DATASETID = 100;
  public static final String DATASETNAME = "datasetName";
  public static final String WORKFLOWOWNER = "workflowOwner";
  public static final String WORKFLOWNAME = "workflowName";
  public static final String EMAIL = "user.metis@europeana.eu";
  public static final String AUTHORIZATION_HEADER = "Bearer qwerty12345";

  private TestObjectFactory() {
  }

  public static Workflow createUserWorkflowObject() {
    Workflow workflow = new Workflow();
    workflow.setHarvestPlugin(true);
    workflow.setTransformPlugin(false);
    workflow.setWorkflowOwner(WORKFLOWOWNER);
    workflow.setWorkflowName(WORKFLOWNAME);

    ArrayList<String> dereferenceParameters = new ArrayList<>();
    dereferenceParameters.add("dereference_parameter_a");
    dereferenceParameters.add("dereference_parameter_b");
    HashMap<String, List<String>> dereferenceParameterGroups = new HashMap<>();
    dereferenceParameterGroups.put("GroupA", dereferenceParameters);
    dereferenceParameterGroups.put("GroupB", dereferenceParameters);
    EnrichmentPluginMetadata enrichmentPluginMetadata = new EnrichmentPluginMetadata(true,
        dereferenceParameterGroups);

    List<AbstractMetisPluginMetadata> abstractMetisPluginMetadata = new ArrayList<>();
    abstractMetisPluginMetadata.add(enrichmentPluginMetadata);
    workflow.setMetisPluginsMetadata(abstractMetisPluginMetadata);

    return workflow;
  }

  public static List<Workflow> createListOfUserWorkflowsSameOwner(String workflowOwner,
      int size) {
    List<Workflow> workflows = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      Workflow workflow = createUserWorkflowObject();
      workflow.setId(new ObjectId());
      workflow.setWorkflowOwner(workflowOwner);
      workflow.setWorkflowName(String.format("%s%s", WORKFLOWNAME, i));
      workflows.add(workflow);
    }
    return workflows;
  }

  public static WorkflowExecution createUserWorkflowExecutionObject() {
    Workflow workflow = createUserWorkflowObject();
    Dataset dataset = createDataset(DATASETNAME);

    WorkflowExecution workflowExecution = new WorkflowExecution(dataset,
        workflow, 0);
    workflowExecution.setWorkflowStatus(WorkflowStatus.INQUEUE);
    workflowExecution.setCreatedDate(new Date());

    return workflowExecution;
  }

  public static WorkflowExecution createUserWorkflowExecutionObject(Dataset dataset,
      Workflow workflow) {
    WorkflowExecution workflowExecution = new WorkflowExecution(dataset,
        workflow, 0);
    workflowExecution.setWorkflowStatus(WorkflowStatus.INQUEUE);
    workflowExecution.setCreatedDate(new Date());

    return workflowExecution;
  }

  public static List<WorkflowExecution> createListOfUserWorkflowExecutions(int size) {
    List<WorkflowExecution> workflowExecutions = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      Workflow workflow = createUserWorkflowObject();
      workflow.setId(new ObjectId());
      workflow.setWorkflowName(String.format("%s%s", WORKFLOWNAME, i));
      Dataset dataset = createDataset(String.format("%s%s", DATASETNAME, i));
      dataset.setDatasetId(DATASETID+i);
      WorkflowExecution workflowExecution = createUserWorkflowExecutionObject(dataset,
          workflow);
      workflowExecution.setId(new ObjectId());
      workflowExecutions.add(workflowExecution);
    }
    return workflowExecutions;
  }

  public static void updateListOfUserWorkflowExecutionsWithWorkflowStatus(
      List<WorkflowExecution> workflowExecutions, WorkflowStatus workflowStatus) {
    for (WorkflowExecution workflowExecution : workflowExecutions) {
      workflowExecution.setWorkflowStatus(workflowStatus);
    }
  }

  public static ScheduledWorkflow createScheduledUserWorkflowObject() {
    ScheduledWorkflow scheduledWorkflow = new ScheduledWorkflow();
    scheduledWorkflow.setDatasetId(DATASETID);
    scheduledWorkflow.setWorkflowOwner(WORKFLOWOWNER);
    scheduledWorkflow.setWorkflowName(WORKFLOWNAME);
    scheduledWorkflow.setPointerDate(new Date());
    scheduledWorkflow.setScheduleFrequence(ScheduleFrequence.ONCE);
    scheduledWorkflow.setWorkflowPriority(0);
    return scheduledWorkflow;
  }

  public static List<ScheduledWorkflow> createListOfScheduledUserWorkflows(int size) {
    List<ScheduledWorkflow> scheduledWorkflows = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      ScheduledWorkflow scheduledWorkflow = createScheduledUserWorkflowObject();
      scheduledWorkflow.setId(new ObjectId());
      scheduledWorkflow.setDatasetId(DATASETID + i);
      scheduledWorkflows.add(scheduledWorkflow);
    }
    return scheduledWorkflows;
  }

  public static List<ScheduledWorkflow> createListOfScheduledUserWorkflowsWithDateAndFrequence(int size, Date date, ScheduleFrequence scheduleFrequence) {
    List<ScheduledWorkflow> scheduledWorkflows = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      ScheduledWorkflow scheduledWorkflow = createScheduledUserWorkflowObject();
      scheduledWorkflow.setId(new ObjectId());
      scheduledWorkflow.setDatasetId(DATASETID + i);
      scheduledWorkflow.setPointerDate(date);
      scheduledWorkflow.setScheduleFrequence(scheduleFrequence);
      scheduledWorkflows.add(scheduledWorkflow);
    }
    return scheduledWorkflows;
  }

  public static Dataset createDataset(String datasetName) {
    Dataset ds = new Dataset();
    ds.setEcloudDatasetId("NOT_CREATED_YET-f525f64c-fea0-44bf-8c56-88f30962734c");
    ds.setDatasetId(DATASETID);
    ds.setDatasetName(datasetName);
    ds.setOrganizationId("1234567890");
    ds.setOrganizationName("OrganizationName");
    ds.setProvider("1234567890");
    ds.setIntermediateProvider("1234567890");
    ds.setDataProvider("1234567890");
    ds.setCreatedByUserId("userId");
    ds.setCreatedDate(new Date());
    ds.setUpdatedDate(new Date());
    ds.setDatasetStatus(DatasetStatus.CREATED);
    ds.setReplacedBy("replacedBy");
    ds.setReplaces("12345");
    ds.setCountry(Country.GREECE);
    ds.setLanguage(Language.AR);
    ds.setDescription("description");
    ds.setNotes("Notes");
    ds.setFirstPublishedDate(new Date());
    ds.setLastPublishedDate(new Date());
    ds.setPublishedRecords(100);
    ds.setHarvestedDate(new Date());
    ds.setHarvestedRecords(100);
    ds.setHarvestingMetadata(new OaipmhHarvestPluginMetadata());
    return ds;
  }

  public static MetisUser createMetisUser(String email)
  {
    MetisUserAccessToken metisUserAccessToken = new MetisUserAccessToken();
    metisUserAccessToken.setAccessToken("AccessToken_12345");
    metisUserAccessToken.setTimestamp(new Date());

    MetisUser metisUser = new MetisUser();
    metisUser.setEmail(email);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    metisUser.setOrganizationId("Organization_12345");
    metisUser.setOrganizationName("OrganizationName");
    metisUser.setMetisUserFlag(true);
    metisUser.setFirstName("FirstName");
    metisUser.setLastName("LastName");
    metisUser.setUserId("User_12345");
    metisUser.setMetisUserAccessToken(metisUserAccessToken);

    return metisUser;
  }
}

