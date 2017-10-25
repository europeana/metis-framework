package eu.europeana.metis.core.test.utils;

import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.core.common.Language;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetStatus;
import eu.europeana.metis.core.dataset.OaipmhHarvestingMetadata;
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledUserWorkflow;
import eu.europeana.metis.core.workflow.UserWorkflow;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.VoidDereferencePluginMetadata;
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

  public static final String DATASETNAME = "datasetName";
  public static final String WORKFLOWOWNER = "workflowOwner";
  public static final String WORKFLOWNAME = "workflowName";

  private TestObjectFactory() {
  }

  public static UserWorkflow createUserWorkflowObject() {
    UserWorkflow userWorkflow = new UserWorkflow();
    userWorkflow.setHarvestPlugin(true);
    userWorkflow.setTransformPlugin(false);
    userWorkflow.setWorkflowOwner(WORKFLOWOWNER);
    userWorkflow.setWorkflowName(WORKFLOWNAME);

    ArrayList<String> dereferenceParameters = new ArrayList<>();
    dereferenceParameters.add("dereference_parameter_a");
    dereferenceParameters.add("dereference_parameter_b");
    HashMap<String, List<String>> dereferenceParameterGroups = new HashMap<>();
    dereferenceParameterGroups.put("GroupA", dereferenceParameters);
    dereferenceParameterGroups.put("GroupB", dereferenceParameters);
    VoidDereferencePluginMetadata voidDereferencePluginMetadata = new VoidDereferencePluginMetadata(
        dereferenceParameterGroups);

    List<AbstractMetisPluginMetadata> abstractMetisPluginMetadata = new ArrayList<>();
    abstractMetisPluginMetadata.add(voidDereferencePluginMetadata);
    userWorkflow.setMetisPluginsMetadata(abstractMetisPluginMetadata);

    return userWorkflow;
  }

  public static List<UserWorkflow> createListOfUserWorkflowsSameOwner(String workflowOwner,
      int size) {
    List<UserWorkflow> userWorkflows = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      UserWorkflow userWorkflow = createUserWorkflowObject();
      userWorkflow.setId(new ObjectId());
      userWorkflow.setWorkflowOwner(workflowOwner);
      userWorkflow.setWorkflowName(String.format("%s%s", WORKFLOWNAME, i));
      userWorkflows.add(userWorkflow);
    }
    return userWorkflows;
  }

  public static UserWorkflowExecution createUserWorkflowExecutionObject() {
    UserWorkflow userWorkflow = createUserWorkflowObject();
    Dataset dataset = createDataset(DATASETNAME);

    UserWorkflowExecution userWorkflowExecution = new UserWorkflowExecution(dataset,
        userWorkflow, 0);
    userWorkflowExecution.setWorkflowStatus(WorkflowStatus.INQUEUE);
    userWorkflowExecution.setCreatedDate(new Date());

    return userWorkflowExecution;
  }

  public static UserWorkflowExecution createUserWorkflowExecutionObject(Dataset dataset,
      UserWorkflow userWorkflow) {
    UserWorkflowExecution userWorkflowExecution = new UserWorkflowExecution(dataset,
        userWorkflow, 0);
    userWorkflowExecution.setWorkflowStatus(WorkflowStatus.INQUEUE);
    userWorkflowExecution.setCreatedDate(new Date());

    return userWorkflowExecution;
  }

  public static List<UserWorkflowExecution> createListOfUserWorkflowExecutions(int size) {
    List<UserWorkflowExecution> userWorkflowExecutions = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      UserWorkflow userWorkflow = createUserWorkflowObject();
      userWorkflow.setId(new ObjectId());
      userWorkflow.setWorkflowName(String.format("%s%s", WORKFLOWNAME, i));
      Dataset dataset = createDataset(String.format("%s%s", DATASETNAME, i));
      UserWorkflowExecution userWorkflowExecution = createUserWorkflowExecutionObject(dataset,
          userWorkflow);
      userWorkflowExecution.setId(new ObjectId());
      userWorkflowExecutions.add(userWorkflowExecution);
    }
    return userWorkflowExecutions;
  }

  public static void updateListOfUserWorkflowExecutionsWithWorkflowStatus(
      List<UserWorkflowExecution> userWorkflowExecutions, WorkflowStatus workflowStatus) {
    for (UserWorkflowExecution userWorkflowExecution : userWorkflowExecutions) {
      userWorkflowExecution.setWorkflowStatus(workflowStatus);
    }
  }

  public static ScheduledUserWorkflow createScheduledUserWorkflowObject() {
    ScheduledUserWorkflow scheduledUserWorkflow = new ScheduledUserWorkflow();
    scheduledUserWorkflow.setDatasetName(DATASETNAME);
    scheduledUserWorkflow.setWorkflowOwner(WORKFLOWOWNER);
    scheduledUserWorkflow.setWorkflowName(WORKFLOWNAME);
    scheduledUserWorkflow.setPointerDate(new Date());
    scheduledUserWorkflow.setScheduleFrequence(ScheduleFrequence.ONCE);
    scheduledUserWorkflow.setWorkflowPriority(0);
    return scheduledUserWorkflow;
  }

  public static List<ScheduledUserWorkflow> createListOfScheduledUserWorkflows(int size) {
    List<ScheduledUserWorkflow> scheduledUserWorkflows = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      ScheduledUserWorkflow scheduledUserWorkflow = createScheduledUserWorkflowObject();
      scheduledUserWorkflow.setId(new ObjectId());
      scheduledUserWorkflow.setDatasetName(String.format("%s%s", DATASETNAME, i));
      scheduledUserWorkflows.add(scheduledUserWorkflow);
    }
    return scheduledUserWorkflows;
  }

  public static List<ScheduledUserWorkflow> createListOfScheduledUserWorkflowsWithDateAndFrequence(int size, Date date, ScheduleFrequence scheduleFrequence) {
    List<ScheduledUserWorkflow> scheduledUserWorkflows = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      ScheduledUserWorkflow scheduledUserWorkflow = createScheduledUserWorkflowObject();
      scheduledUserWorkflow.setId(new ObjectId());
      scheduledUserWorkflow.setDatasetName(String.format("%s%s", DATASETNAME, i));
      scheduledUserWorkflow.setPointerDate(date);
      scheduledUserWorkflow.setScheduleFrequence(scheduleFrequence);
      scheduledUserWorkflows.add(scheduledUserWorkflow);
    }
    return scheduledUserWorkflows;
  }

  public static Dataset createDataset(String datasetName) {
    Dataset ds = new Dataset();
    ds.setAccepted(true);
    ds.setCountry(Country.ALBANIA);
    ds.setCreatedDate(new Date(1000));
    ds.setDataProvider("prov");
    ds.setDeaSigned(true);
    ds.setDescription("Test description");
    List<String> dqa = new ArrayList<>();
    dqa.add("test DQA");
    ds.setDqas(dqa);
    ds.setFirstPublished(new Date(1000));
    ds.setHarvestedAt(new Date(1000));
    ds.setLanguage(Language.AR);
    ds.setLastPublished(new Date(1000));
    ds.setHarvestingMetadata(new OaipmhHarvestingMetadata());
    ds.setDatasetName(datasetName);
    ds.setNotes("test Notes");
    ds.setPublishedRecords(100);
    ds.setSubmittedRecords(199);
    ds.setReplacedBy("replacedBY");
    List<String> sources = new ArrayList<>();
    sources.add("testSource");
    ds.setSources(sources);
    List<String> subjects = new ArrayList<>();
    subjects.add("testSubject");
    ds.setSubjects(subjects);
    ds.setSubmissionDate(new Date(1000));
    ds.setUpdatedDate(new Date(1000));
    ds.setDatasetStatus(DatasetStatus.ACCEPTANCE);
    return ds;
  }
}
