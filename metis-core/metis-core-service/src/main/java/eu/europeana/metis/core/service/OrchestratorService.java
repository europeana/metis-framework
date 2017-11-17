package eu.europeana.metis.core.service;

import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.ScheduledWorkflowDao;
import eu.europeana.metis.core.dao.WorkflowDao;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoScheduledWorkflowFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowFoundException;
import eu.europeana.metis.core.exceptions.ScheduledWorkflowAlreadyExistsException;
import eu.europeana.metis.core.exceptions.WorkflowAlreadyExistsException;
import eu.europeana.metis.core.exceptions.WorkflowExecutionAlreadyExistsException;
import eu.europeana.metis.core.execution.WorkflowExecutorManager;
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledWorkflow;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-24
 */
@Service
public class OrchestratorService {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrchestratorService.class);

  private final WorkflowExecutionDao workflowExecutionDao;
  private final WorkflowDao workflowDao;
  private final ScheduledWorkflowDao scheduledWorkflowDao;
  private final DatasetDao datasetDao;
  private final WorkflowExecutorManager workflowExecutorManager;

  @Autowired
  public OrchestratorService(WorkflowDao workflowDao,
      WorkflowExecutionDao workflowExecutionDao,
      ScheduledWorkflowDao scheduledWorkflowDao,
      DatasetDao datasetDao,
      WorkflowExecutorManager workflowExecutorManager) throws IOException {
    this.workflowDao = workflowDao;
    this.workflowExecutionDao = workflowExecutionDao;
    this.scheduledWorkflowDao = scheduledWorkflowDao;
    this.datasetDao = datasetDao;
    this.workflowExecutorManager = workflowExecutorManager;

    this.workflowExecutorManager.initiateConsumer();
  }

  public void createUserWorkflow(Workflow workflow)
      throws WorkflowAlreadyExistsException {
    checkRestrictionsOnUserWorkflowCreate(workflow);
    workflowDao.create(workflow);
  }

  public void updateUserWorkflow(Workflow workflow) throws NoWorkflowFoundException {
    String storedId = checkRestrictionsOnUserWorkflowUpdate(workflow);
    workflow.setId(new ObjectId(storedId));
    workflowDao.update(workflow);
  }

  public void deleteUserWorkflow(String workflowOwner, String workflowName) {
    workflowDao.deleteUserWorkflow(workflowOwner, workflowName);
  }

  public Workflow getUserWorkflow(String workflowOwner, String workflowName) {
    return workflowDao.getUserWorkflow(workflowOwner, workflowName);
  }

  public List<Workflow> getAllUserWorkflows(String workflowOwner, String nextPage) {
    return workflowDao.getAllUserWorkflows(workflowOwner, nextPage);
  }

  public WorkflowExecution getRunningUserWorkflowExecution(String datasetName) {
    return workflowExecutionDao
        .getRunningUserWorkflowExecution(datasetName);
  }

  public void addUserWorkflowInQueueOfUserWorkflowExecutions(String datasetName,
      String workflowOwner, String workflowName, int priority)
      throws NoDatasetFoundException, NoWorkflowFoundException, WorkflowExecutionAlreadyExistsException {

    Dataset dataset = checkDatasetExistence(datasetName);
    Workflow workflow = checkUserWorkflowExistence(workflowOwner, workflowName);

    WorkflowExecution workflowExecution = new WorkflowExecution(dataset, workflow,
        priority);
    workflowExecution.setWorkflowStatus(WorkflowStatus.INQUEUE);
    String storedUserWorkflowExecutionId = workflowExecutionDao
        .existsAndNotCompleted(datasetName);
    if (storedUserWorkflowExecutionId != null) {
      throw new WorkflowExecutionAlreadyExistsException(
          String.format("User workflow execution already exists with id %s and is not completed",
              storedUserWorkflowExecutionId));
    }
    workflowExecution.setCreatedDate(new Date());
    String objectId = workflowExecutionDao.create(workflowExecution);
    workflowExecutorManager.addUserWorkflowExecutionToQueue(objectId, priority);
    LOGGER.info("WorkflowExecution with id: {}, added to execution queue", objectId);
  }

  //Used for direct, on the fly provided, execution of a Workflow
  public void addUserWorkflowInQueueOfUserWorkflowExecutions(String datasetName,
      Workflow workflow, int priority)
      throws WorkflowExecutionAlreadyExistsException, NoDatasetFoundException, WorkflowAlreadyExistsException {
    Dataset dataset = checkDatasetExistence(datasetName);
    //Generate uuid workflowName for user and check if by any chance it exists.
    workflow.setWorkflowName(new ObjectId().toString());
    checkRestrictionsOnUserWorkflowCreate(workflow);

    WorkflowExecution workflowExecution = new WorkflowExecution(dataset, workflow,
        priority);
    workflowExecution.setWorkflowStatus(WorkflowStatus.INQUEUE);
    String storedUserWorkflowExecutionId = workflowExecutionDao
        .existsAndNotCompleted(datasetName);
    if (storedUserWorkflowExecutionId != null) {
      throw new WorkflowExecutionAlreadyExistsException(
          String.format(
              "User workflow execution for datasetName: %s, already exists with id: %s, and is not completed",
              datasetName, storedUserWorkflowExecutionId));
    }
    workflowExecution.setCreatedDate(new Date());
    String objectId = workflowExecutionDao.create(workflowExecution);
    workflowExecutorManager.addUserWorkflowExecutionToQueue(objectId, priority);
    LOGGER.info("WorkflowExecution with id: %s, added to execution queue", objectId);
  }

  public void cancelUserWorkflowExecution(String datasetName)
      throws NoWorkflowExecutionFoundException {

    WorkflowExecution workflowExecution = workflowExecutionDao
        .getRunningOrInQueueExecution(datasetName);
    if (workflowExecution != null) {
      workflowExecutorManager.cancelUserWorkflowExecution(workflowExecution);
    } else {
      throw new NoWorkflowExecutionFoundException(String.format(
          "Running userworkflowExecution with datasetName: %s, does not exist or not running",
          datasetName));
    }
  }

  public void removeActiveUserWorkflowExecutionsFromList(
      List<WorkflowExecution> workflowExecutions) {
    workflowExecutionDao
        .removeActiveExecutionsFromList(workflowExecutions,
            workflowExecutorManager.getMonitorCheckIntervalInSecs());
  }

  public void addUserWorkflowExecutionToQueue(String userWorkflowExecutionObjectId,
      int priority) {
    workflowExecutorManager
        .addUserWorkflowExecutionToQueue(userWorkflowExecutionObjectId, priority);
  }

  private void checkRestrictionsOnUserWorkflowCreate(Workflow workflow)
      throws WorkflowAlreadyExistsException {

    if (StringUtils.isNotEmpty(userWorkflowExists(workflow))) {
      throw new WorkflowAlreadyExistsException(String.format(
          "Workflow with workflowOwner: %s, and workflowName: %s, already exists",
          workflow.getWorkflowOwner(), workflow.getWorkflowName()));
    }
  }

  private String checkRestrictionsOnUserWorkflowUpdate(Workflow workflow)
      throws NoWorkflowFoundException {

    String storedId = userWorkflowExists(workflow);
    if (StringUtils.isEmpty(storedId)) {
      throw new NoWorkflowFoundException(String.format(
          "Workflow with workflowOwner: %s, and workflowName: %s, not found",
          workflow.getWorkflowOwner(),
          workflow
              .getWorkflowName()));
    }

    return storedId;
  }

  private String userWorkflowExists(Workflow workflow) {
    return workflowDao.exists(workflow);
  }

  public int getUserWorkflowExecutionsPerRequest() {
    return workflowExecutionDao.getUserWorkflowExecutionsPerRequest();
  }

  public int getScheduledUserWorkflowsPerRequest() {
    return scheduledWorkflowDao.getScheduledUserWorkflowPerRequest();
  }

  public int getUserWorkflowsPerRequest() {
    return workflowDao.getUserWorkflowsPerRequest();
  }

  public List<WorkflowExecution> getAllUserWorkflowExecutions(String datasetName,
      String workflowOwner,
      String workflowName,
      WorkflowStatus workflowStatus, String nextPage) {
    return workflowExecutionDao
        .getAllUserWorkflowExecutions(datasetName, workflowOwner, workflowName, workflowStatus,
            nextPage);
  }

  public List<WorkflowExecution> getAllUserWorkflowExecutions(WorkflowStatus workflowStatus,
      String nextPage) {
    return workflowExecutionDao.getAllUserWorkflowExecutions(workflowStatus, nextPage);
  }

  public ScheduledWorkflow getScheduledUserWorkflowByDatasetName(String datasetName) {
    return scheduledWorkflowDao.getScheduledUserWorkflowByDatasetName(datasetName);
  }

  public void scheduleUserWorkflow(ScheduledWorkflow scheduledWorkflow)
      throws NoDatasetFoundException, NoWorkflowFoundException, BadContentException, ScheduledWorkflowAlreadyExistsException {
    checkRestrictionsOnScheduleUserWorkflow(scheduledWorkflow);
    scheduledWorkflowDao.create(scheduledWorkflow);
  }

  public List<ScheduledWorkflow> getAllScheduledUserWorkflows(
      ScheduleFrequence scheduleFrequence, String nextPage) {
    return scheduledWorkflowDao.getAllScheduledUserWorkflows(scheduleFrequence, nextPage);
  }

  public List<ScheduledWorkflow> getAllScheduledUserWorkflowsByDateRangeONCE(
      LocalDateTime lowerBound,
      LocalDateTime upperBound, String nextPage) {
    return scheduledWorkflowDao
        .getAllScheduledUserWorkflowsByDateRangeONCE(lowerBound, upperBound, nextPage);
  }

  private Dataset checkDatasetExistence(String datasetName) throws NoDatasetFoundException {
    Dataset dataset = datasetDao.getDatasetByDatasetName(datasetName);
    if (dataset == null) {
      throw new NoDatasetFoundException(
          String.format("No dataset found with datasetName: %s, in METIS", datasetName));
    }
    return dataset;
  }

  private Workflow checkUserWorkflowExistence(String workflowOwner, String workflowName)
      throws NoWorkflowFoundException {
    Workflow workflow = workflowDao
        .getUserWorkflow(workflowOwner, workflowName);
    if (workflow == null) {
      throw new NoWorkflowFoundException(String.format(
          "No user workflow found with workflowOwner: %s, and workflowName: %s, in METIS",
          workflowOwner, workflowName));
    }
    return workflow;
  }

  private void checkScheduledUserWorkflowExistenceForDatasetName(String datasetName)
      throws ScheduledWorkflowAlreadyExistsException {
    String id = scheduledWorkflowDao.existsForDatasetName(datasetName);
    if (id != null) {
      throw new ScheduledWorkflowAlreadyExistsException(String.format(
          "ScheduledWorkflow for datasetName: %s with id %s, already exists",
          datasetName, id));
    }
  }

  public void updateScheduledUserWorkflow(ScheduledWorkflow scheduledWorkflow)
      throws NoScheduledWorkflowFoundException, BadContentException, NoWorkflowFoundException {
    String storedId = checkRestrictionsOnScheduledUserWorkflowUpdate(scheduledWorkflow);
    scheduledWorkflow.setId(new ObjectId(storedId));
    scheduledWorkflowDao.update(scheduledWorkflow);
  }

  private void checkRestrictionsOnScheduleUserWorkflow(ScheduledWorkflow scheduledWorkflow)
      throws NoWorkflowFoundException, NoDatasetFoundException, ScheduledWorkflowAlreadyExistsException, BadContentException {
    checkDatasetExistence(scheduledWorkflow.getDatasetName());
    checkUserWorkflowExistence(scheduledWorkflow.getWorkflowOwner(),
        scheduledWorkflow.getWorkflowName());
    checkScheduledUserWorkflowExistenceForDatasetName(scheduledWorkflow.getDatasetName());
    if (scheduledWorkflow.getPointerDate() == null) {
      throw new BadContentException("PointerDate cannot be null");
    }
    if (scheduledWorkflow.getScheduleFrequence() == null
        || scheduledWorkflow.getScheduleFrequence() == ScheduleFrequence.NULL) {
      throw new BadContentException("NULL or null is not a valid scheduleFrequence");
    }
  }

  private String checkRestrictionsOnScheduledUserWorkflowUpdate(
      ScheduledWorkflow scheduledWorkflow)
      throws NoScheduledWorkflowFoundException, BadContentException, NoWorkflowFoundException {
    checkUserWorkflowExistence(scheduledWorkflow.getWorkflowOwner(),
        scheduledWorkflow.getWorkflowName());
    String storedId = scheduledWorkflowDao
        .existsForDatasetName(scheduledWorkflow.getDatasetName());
    if (StringUtils.isEmpty(storedId)) {
      throw new NoScheduledWorkflowFoundException(String.format(
          "Workflow with datasetName: %s, not found", scheduledWorkflow.getDatasetName()));
    }
    if (scheduledWorkflow.getPointerDate() == null) {
      throw new BadContentException("PointerDate cannot be null");
    }
    if (scheduledWorkflow.getScheduleFrequence() == null
        || scheduledWorkflow.getScheduleFrequence() == ScheduleFrequence.NULL) {
      throw new BadContentException("NULL or null is not a valid scheduleFrequence");
    }
    return storedId;
  }

  public void deleteScheduledUserWorkflow(String datasetName) {
    scheduledWorkflowDao.deleteScheduledUserWorkflow(datasetName);
  }
}
