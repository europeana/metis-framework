package eu.europeana.metis.core.service;

import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.ScheduledUserWorkflowDao;
import eu.europeana.metis.core.dao.UserWorkflowDao;
import eu.europeana.metis.core.dao.UserWorkflowExecutionDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoScheduledUserWorkflowFoundException;
import eu.europeana.metis.core.exceptions.NoUserWorkflowExecutionFoundException;
import eu.europeana.metis.core.exceptions.NoUserWorkflowFoundException;
import eu.europeana.metis.core.exceptions.ScheduledUserWorkflowAlreadyExistsException;
import eu.europeana.metis.core.exceptions.UserWorkflowAlreadyExistsException;
import eu.europeana.metis.core.exceptions.UserWorkflowExecutionAlreadyExistsException;
import eu.europeana.metis.core.execution.UserWorkflowExecutorManager;
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledUserWorkflow;
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

  private final UserWorkflowExecutionDao userWorkflowExecutionDao;
  private final UserWorkflowDao userWorkflowDao;
  private final ScheduledUserWorkflowDao scheduledUserWorkflowDao;
  private final DatasetDao datasetDao;
  private final UserWorkflowExecutorManager userWorkflowExecutorManager;

  @Autowired
  public OrchestratorService(UserWorkflowDao userWorkflowDao,
      UserWorkflowExecutionDao userWorkflowExecutionDao,
      ScheduledUserWorkflowDao scheduledUserWorkflowDao,
      DatasetDao datasetDao,
      UserWorkflowExecutorManager userWorkflowExecutorManager) throws IOException {
    this.userWorkflowDao = userWorkflowDao;
    this.userWorkflowExecutionDao = userWorkflowExecutionDao;
    this.scheduledUserWorkflowDao = scheduledUserWorkflowDao;
    this.datasetDao = datasetDao;
    this.userWorkflowExecutorManager = userWorkflowExecutorManager;

    this.userWorkflowExecutorManager.initiateConsumer();
  }

  public void createUserWorkflow(Workflow workflow)
      throws UserWorkflowAlreadyExistsException {
    checkRestrictionsOnUserWorkflowCreate(workflow);
    userWorkflowDao.create(workflow);
  }

  public void updateUserWorkflow(Workflow workflow) throws NoUserWorkflowFoundException {
    String storedId = checkRestrictionsOnUserWorkflowUpdate(workflow);
    workflow.setId(new ObjectId(storedId));
    userWorkflowDao.update(workflow);
  }

  public void deleteUserWorkflow(String workflowOwner, String workflowName) {
    userWorkflowDao.deleteUserWorkflow(workflowOwner, workflowName);
  }

  public Workflow getUserWorkflow(String workflowOwner, String workflowName) {
    return userWorkflowDao.getUserWorkflow(workflowOwner, workflowName);
  }

  public List<Workflow> getAllUserWorkflows(String workflowOwner, String nextPage) {
    return userWorkflowDao.getAllUserWorkflows(workflowOwner, nextPage);
  }

  public WorkflowExecution getRunningUserWorkflowExecution(String datasetName) {
    return userWorkflowExecutionDao
        .getRunningUserWorkflowExecution(datasetName);
  }

  public void addUserWorkflowInQueueOfUserWorkflowExecutions(String datasetName,
      String workflowOwner, String workflowName, int priority)
      throws NoDatasetFoundException, NoUserWorkflowFoundException, UserWorkflowExecutionAlreadyExistsException {

    Dataset dataset = checkDatasetExistence(datasetName);
    Workflow workflow = checkUserWorkflowExistence(workflowOwner, workflowName);

    WorkflowExecution workflowExecution = new WorkflowExecution(dataset, workflow,
        priority);
    workflowExecution.setWorkflowStatus(WorkflowStatus.INQUEUE);
    String storedUserWorkflowExecutionId = userWorkflowExecutionDao
        .existsAndNotCompleted(datasetName);
    if (storedUserWorkflowExecutionId != null) {
      throw new UserWorkflowExecutionAlreadyExistsException(
          String.format("User workflow execution already exists with id %s and is not completed",
              storedUserWorkflowExecutionId));
    }
    workflowExecution.setCreatedDate(new Date());
    String objectId = userWorkflowExecutionDao.create(workflowExecution);
    userWorkflowExecutorManager.addUserWorkflowExecutionToQueue(objectId, priority);
    LOGGER.info("WorkflowExecution with id: {}, added to execution queue", objectId);
  }

  //Used for direct, on the fly provided, execution of a Workflow
  public void addUserWorkflowInQueueOfUserWorkflowExecutions(String datasetName,
      Workflow workflow, int priority)
      throws UserWorkflowExecutionAlreadyExistsException, NoDatasetFoundException, UserWorkflowAlreadyExistsException {
    Dataset dataset = checkDatasetExistence(datasetName);
    //Generate uuid workflowName for user and check if by any chance it exists.
    workflow.setWorkflowName(new ObjectId().toString());
    checkRestrictionsOnUserWorkflowCreate(workflow);

    WorkflowExecution workflowExecution = new WorkflowExecution(dataset, workflow,
        priority);
    workflowExecution.setWorkflowStatus(WorkflowStatus.INQUEUE);
    String storedUserWorkflowExecutionId = userWorkflowExecutionDao
        .existsAndNotCompleted(datasetName);
    if (storedUserWorkflowExecutionId != null) {
      throw new UserWorkflowExecutionAlreadyExistsException(
          String.format(
              "User workflow execution for datasetName: %s, already exists with id: %s, and is not completed",
              datasetName, storedUserWorkflowExecutionId));
    }
    workflowExecution.setCreatedDate(new Date());
    String objectId = userWorkflowExecutionDao.create(workflowExecution);
    userWorkflowExecutorManager.addUserWorkflowExecutionToQueue(objectId, priority);
    LOGGER.info("WorkflowExecution with id: %s, added to execution queue", objectId);
  }

  public void cancelUserWorkflowExecution(String datasetName)
      throws NoUserWorkflowExecutionFoundException {

    WorkflowExecution workflowExecution = userWorkflowExecutionDao
        .getRunningOrInQueueExecution(datasetName);
    if (workflowExecution != null) {
      userWorkflowExecutorManager.cancelUserWorkflowExecution(workflowExecution);
    } else {
      throw new NoUserWorkflowExecutionFoundException(String.format(
          "Running userworkflowExecution with datasetName: %s, does not exist or not running",
          datasetName));
    }
  }

  public void removeActiveUserWorkflowExecutionsFromList(
      List<WorkflowExecution> workflowExecutions) {
    userWorkflowExecutionDao
        .removeActiveExecutionsFromList(workflowExecutions,
            userWorkflowExecutorManager.getMonitorCheckIntervalInSecs());
  }

  public void addUserWorkflowExecutionToQueue(String userWorkflowExecutionObjectId,
      int priority) {
    userWorkflowExecutorManager
        .addUserWorkflowExecutionToQueue(userWorkflowExecutionObjectId, priority);
  }

  private void checkRestrictionsOnUserWorkflowCreate(Workflow workflow)
      throws UserWorkflowAlreadyExistsException {

    if (StringUtils.isNotEmpty(userWorkflowExists(workflow))) {
      throw new UserWorkflowAlreadyExistsException(String.format(
          "Workflow with workflowOwner: %s, and workflowName: %s, already exists",
          workflow.getWorkflowOwner(), workflow.getWorkflowName()));
    }
  }

  private String checkRestrictionsOnUserWorkflowUpdate(Workflow workflow)
      throws NoUserWorkflowFoundException {

    String storedId = userWorkflowExists(workflow);
    if (StringUtils.isEmpty(storedId)) {
      throw new NoUserWorkflowFoundException(String.format(
          "Workflow with workflowOwner: %s, and workflowName: %s, not found",
          workflow.getWorkflowOwner(),
          workflow
              .getWorkflowName()));
    }

    return storedId;
  }

  private String userWorkflowExists(Workflow workflow) {
    return userWorkflowDao.exists(workflow);
  }

  public int getUserWorkflowExecutionsPerRequest() {
    return userWorkflowExecutionDao.getUserWorkflowExecutionsPerRequest();
  }

  public int getScheduledUserWorkflowsPerRequest() {
    return scheduledUserWorkflowDao.getScheduledUserWorkflowPerRequest();
  }

  public int getUserWorkflowsPerRequest() {
    return userWorkflowDao.getUserWorkflowsPerRequest();
  }

  public List<WorkflowExecution> getAllUserWorkflowExecutions(String datasetName,
      String workflowOwner,
      String workflowName,
      WorkflowStatus workflowStatus, String nextPage) {
    return userWorkflowExecutionDao
        .getAllUserWorkflowExecutions(datasetName, workflowOwner, workflowName, workflowStatus,
            nextPage);
  }

  public List<WorkflowExecution> getAllUserWorkflowExecutions(WorkflowStatus workflowStatus,
      String nextPage) {
    return userWorkflowExecutionDao.getAllUserWorkflowExecutions(workflowStatus, nextPage);
  }

  public ScheduledUserWorkflow getScheduledUserWorkflowByDatasetName(String datasetName) {
    return scheduledUserWorkflowDao.getScheduledUserWorkflowByDatasetName(datasetName);
  }

  public void scheduleUserWorkflow(ScheduledUserWorkflow scheduledUserWorkflow)
      throws NoDatasetFoundException, NoUserWorkflowFoundException, BadContentException, ScheduledUserWorkflowAlreadyExistsException {
    checkRestrictionsOnScheduleUserWorkflow(scheduledUserWorkflow);
    scheduledUserWorkflowDao.create(scheduledUserWorkflow);
  }

  public List<ScheduledUserWorkflow> getAllScheduledUserWorkflows(
      ScheduleFrequence scheduleFrequence, String nextPage) {
    return scheduledUserWorkflowDao.getAllScheduledUserWorkflows(scheduleFrequence, nextPage);
  }

  public List<ScheduledUserWorkflow> getAllScheduledUserWorkflowsByDateRangeONCE(
      LocalDateTime lowerBound,
      LocalDateTime upperBound, String nextPage) {
    return scheduledUserWorkflowDao
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
      throws NoUserWorkflowFoundException {
    Workflow workflow = userWorkflowDao
        .getUserWorkflow(workflowOwner, workflowName);
    if (workflow == null) {
      throw new NoUserWorkflowFoundException(String.format(
          "No user workflow found with workflowOwner: %s, and workflowName: %s, in METIS",
          workflowOwner, workflowName));
    }
    return workflow;
  }

  private void checkScheduledUserWorkflowExistenceForDatasetName(String datasetName)
      throws ScheduledUserWorkflowAlreadyExistsException {
    String id = scheduledUserWorkflowDao.existsForDatasetName(datasetName);
    if (id != null) {
      throw new ScheduledUserWorkflowAlreadyExistsException(String.format(
          "ScheduledUserWorkflow for datasetName: %s with id %s, already exists",
          datasetName, id));
    }
  }

  public void updateScheduledUserWorkflow(ScheduledUserWorkflow scheduledUserWorkflow)
      throws NoScheduledUserWorkflowFoundException, BadContentException, NoUserWorkflowFoundException {
    String storedId = checkRestrictionsOnScheduledUserWorkflowUpdate(scheduledUserWorkflow);
    scheduledUserWorkflow.setId(new ObjectId(storedId));
    scheduledUserWorkflowDao.update(scheduledUserWorkflow);
  }

  private void checkRestrictionsOnScheduleUserWorkflow(ScheduledUserWorkflow scheduledUserWorkflow)
      throws NoUserWorkflowFoundException, NoDatasetFoundException, ScheduledUserWorkflowAlreadyExistsException, BadContentException {
    checkDatasetExistence(scheduledUserWorkflow.getDatasetName());
    checkUserWorkflowExistence(scheduledUserWorkflow.getWorkflowOwner(),
        scheduledUserWorkflow.getWorkflowName());
    checkScheduledUserWorkflowExistenceForDatasetName(scheduledUserWorkflow.getDatasetName());
    if (scheduledUserWorkflow.getPointerDate() == null) {
      throw new BadContentException("PointerDate cannot be null");
    }
    if (scheduledUserWorkflow.getScheduleFrequence() == null
        || scheduledUserWorkflow.getScheduleFrequence() == ScheduleFrequence.NULL) {
      throw new BadContentException("NULL or null is not a valid scheduleFrequence");
    }
  }

  private String checkRestrictionsOnScheduledUserWorkflowUpdate(
      ScheduledUserWorkflow scheduledUserWorkflow)
      throws NoScheduledUserWorkflowFoundException, BadContentException, NoUserWorkflowFoundException {
    checkUserWorkflowExistence(scheduledUserWorkflow.getWorkflowOwner(),
        scheduledUserWorkflow.getWorkflowName());
    String storedId = scheduledUserWorkflowDao
        .existsForDatasetName(scheduledUserWorkflow.getDatasetName());
    if (StringUtils.isEmpty(storedId)) {
      throw new NoScheduledUserWorkflowFoundException(String.format(
          "Workflow with datasetName: %s, not found", scheduledUserWorkflow.getDatasetName()));
    }
    if (scheduledUserWorkflow.getPointerDate() == null) {
      throw new BadContentException("PointerDate cannot be null");
    }
    if (scheduledUserWorkflow.getScheduleFrequence() == null
        || scheduledUserWorkflow.getScheduleFrequence() == ScheduleFrequence.NULL) {
      throw new BadContentException("NULL or null is not a valid scheduleFrequence");
    }
    return storedId;
  }

  public void deleteScheduledUserWorkflow(String datasetName) {
    scheduledUserWorkflowDao.deleteScheduledUserWorkflow(datasetName);
  }
}
