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
import eu.europeana.metis.core.workflow.plugins.VoidOaipmhHarvestPluginMetadata;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-04
 */
public class TestObjectFactory {

  public static final String DATASETNAME = "datasetName";
  public static final String WORKFLOWOWNER = "workflowOwner";
  public static final String WORKFLOWNAME = "workflowName";

  public static UserWorkflow createUserWorkflowObject() {
    UserWorkflow userWorkflow = new UserWorkflow();
    userWorkflow.setHarvestPlugin(true);
    userWorkflow.setTransformPlugin(false);
    userWorkflow.setWorkflowOwner(WORKFLOWOWNER);
    userWorkflow.setWorkflowName(WORKFLOWNAME);

    OaipmhHarvestingMetadata oaipmhHarvestingMetadata = new OaipmhHarvestingMetadata(
        "metadataFormat", "setSpec", "http://test.me.now");
    ArrayList<String> oaiParameters = new ArrayList<>();
    oaiParameters.add("oai_parameter_a");
    oaiParameters.add("oai_parameter_b");
    HashMap<String, List<String>> oaiParameterGroups = new HashMap<>();
    oaiParameterGroups.put("GroupA", oaiParameters);
    oaiParameterGroups.put("GroupB", oaiParameters);

    VoidOaipmhHarvestPluginMetadata voidOaipmhHarvestPluginMetadata = new VoidOaipmhHarvestPluginMetadata(
        oaipmhHarvestingMetadata, oaiParameterGroups);

    ArrayList<String> dereferenceParameters = new ArrayList<>();
    dereferenceParameters.add("dereference_parameter_a");
    dereferenceParameters.add("dereference_parameter_b");
    HashMap<String, List<String>> dereferenceParameterGroups = new HashMap<>();
    dereferenceParameterGroups.put("GroupA", dereferenceParameters);
    dereferenceParameterGroups.put("GroupB", dereferenceParameters);
    VoidDereferencePluginMetadata voidDereferencePluginMetadata = new VoidDereferencePluginMetadata(
        dereferenceParameterGroups);

    List<AbstractMetisPluginMetadata> abstractMetisPluginMetadata = new ArrayList<>();
    abstractMetisPluginMetadata.add(voidOaipmhHarvestPluginMetadata);
    abstractMetisPluginMetadata.add(voidDereferencePluginMetadata);
    userWorkflow.setMetisPluginsMetadata(abstractMetisPluginMetadata);

    return userWorkflow;
  }

  public static UserWorkflowExecution createUserWorkflowExecutionObject() {
    UserWorkflow userWorkflowObject = createUserWorkflowObject();
    Dataset dataset = createDataset(DATASETNAME);

    UserWorkflowExecution userWorkflowExecution = new UserWorkflowExecution(dataset,
        userWorkflowObject, 0);
    userWorkflowExecution.setWorkflowStatus(WorkflowStatus.INQUEUE);
    userWorkflowExecution.setCreatedDate(new Date());

    return userWorkflowExecution;
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

  public static Dataset createDataset(String datasetName) {
    Dataset ds = new Dataset();
    ds.setAccepted(true);
    ds.setAssignedToLdapId("Lemmy");
    ds.setCountry(Country.ALBANIA);
    ds.setCreatedDate(new Date(1000));
    ds.setCreatedByLdapId("Lemmy");
    ds.setDataProvider("prov");
    ds.setDeaSigned(true);
    ds.setDescription("Test description");
    List<String> DQA = new ArrayList<>();
    DQA.add("test DQA");
    ds.setDqas(DQA);
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
