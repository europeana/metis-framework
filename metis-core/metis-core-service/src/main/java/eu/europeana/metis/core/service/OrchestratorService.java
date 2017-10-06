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
import eu.europeana.metis.core.workflow.UserWorkflow;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
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
      UserWorkflowExecutorManager userWorkflowExecutorManager) {
    this.userWorkflowDao = userWorkflowDao;
    this.userWorkflowExecutionDao = userWorkflowExecutionDao;
    this.scheduledUserWorkflowDao = scheduledUserWorkflowDao;
    this.datasetDao = datasetDao;
    this.userWorkflowExecutorManager = userWorkflowExecutorManager;

    this.userWorkflowExecutorManager.initiateConsumer();
  }

  public void createUserWorkflow(UserWorkflow userWorkflow)
      throws UserWorkflowAlreadyExistsException {
    checkRestrictionsOnUserWorkflowCreate(userWorkflow);
    userWorkflowDao.create(userWorkflow);
  }

  public void updateUserWorkflow(UserWorkflow userWorkflow) throws NoUserWorkflowFoundException {
    String storedId = checkRestrictionsOnUserWorkflowUpdate(userWorkflow);
    userWorkflow.setId(new ObjectId(storedId));
    userWorkflowDao.update(userWorkflow);
  }

  public void deleteUserWorkflow(String workflowOwner, String workflowName) {
    userWorkflowDao.deleteUserWorkflow(workflowOwner, workflowName);
  }

  public UserWorkflow getUserWorkflow(String workflowOwner, String workflowName) {
    return userWorkflowDao.getUserWorkflow(workflowOwner, workflowName);
  }

  public List<UserWorkflow> getAllUserWorkflows(String workflowOwner, String nextPage) {
    return userWorkflowDao.getAllUserWorkflows(workflowOwner, nextPage);
  }

  public UserWorkflowExecution getRunningUserWorkflowExecution(String datasetName,
      String workflowOwner,
      String workflowName) {
    return userWorkflowExecutionDao
        .getRunningUserWorkflowExecution(datasetName, workflowOwner, workflowName);
  }

  public void addUserWorkflowInQueueOfUserWorkflowExecutions(String datasetName,
      String workflowOwner, String workflowName, int priority)
      throws NoDatasetFoundException, NoUserWorkflowFoundException, UserWorkflowExecutionAlreadyExistsException {

    Dataset dataset = checkDatasetExistence(datasetName);
    UserWorkflow userWorkflow = checkUserWorkflowExistence(workflowOwner, workflowName);

    UserWorkflowExecution userWorkflowExecution = new UserWorkflowExecution(dataset, userWorkflow,
        priority);
    userWorkflowExecution.setWorkflowStatus(WorkflowStatus.INQUEUE);
    String storedUserWorkflowExecutionId = userWorkflowExecutionDao
        .existsAndNotCompleted(datasetName);
    if (storedUserWorkflowExecutionId != null) {
      throw new UserWorkflowExecutionAlreadyExistsException(
          String.format("User workflow execution already exists with id %s and is not completed",
              storedUserWorkflowExecutionId));
    }
    userWorkflowExecution.setCreatedDate(new Date());
    String objectId = userWorkflowExecutionDao.create(userWorkflowExecution);
    userWorkflowExecutorManager.addUserWorkflowExecutionToQueue(objectId, priority);
    LOGGER.info("UserWorkflowExecution with id: {}, added to execution queue", objectId);
  }

  //Used for direct, on the fly provided, execution of a UserWorkflow
  public void addUserWorkflowInQueueOfUserWorkflowExecutions(String datasetName,
      UserWorkflow userWorkflow, int priority)
      throws UserWorkflowExecutionAlreadyExistsException, NoDatasetFoundException, UserWorkflowAlreadyExistsException {
    Dataset dataset = checkDatasetExistence(datasetName);
    //Generate uuid workflowName for user and check if by any chance it exists.
    userWorkflow.setWorkflowName(new ObjectId().toString());
    checkRestrictionsOnUserWorkflowCreate(userWorkflow);

    UserWorkflowExecution userWorkflowExecution = new UserWorkflowExecution(dataset, userWorkflow,
        priority);
    userWorkflowExecution.setWorkflowStatus(WorkflowStatus.INQUEUE);
    String storedUserWorkflowExecutionId = userWorkflowExecutionDao
        .existsAndNotCompleted(datasetName);
    if (storedUserWorkflowExecutionId != null) {
      throw new UserWorkflowExecutionAlreadyExistsException(
          String.format(
              "User workflow execution for datasetName: %s, already exists with id: %s, and is not completed",
              datasetName, storedUserWorkflowExecutionId));
    }
    userWorkflowExecution.setCreatedDate(new Date());
    String objectId = userWorkflowExecutionDao.create(userWorkflowExecution);
    userWorkflowExecutorManager.addUserWorkflowExecutionToQueue(objectId, priority);
    LOGGER.info("UserWorkflowExecution with id: %s, added to execution queue", objectId);
  }

  public void cancelUserWorkflowExecution(String datasetName)
      throws NoUserWorkflowExecutionFoundException {

    UserWorkflowExecution userWorkflowExecution = userWorkflowExecutionDao
        .getRunningOrInQueueExecution(datasetName);
    if (userWorkflowExecution != null) {
      userWorkflowExecutorManager.cancelUserWorkflowExecution(userWorkflowExecution);
    } else {
      throw new NoUserWorkflowExecutionFoundException(String.format(
          "Running userworkflowExecution with datasetName: %s, does not exist or not running",
          datasetName));
    }
  }

  public void removeActiveUserWorkflowExecutionsFromList(
      List<UserWorkflowExecution> userWorkflowExecutions) {
    userWorkflowExecutionDao
        .removeActiveExecutionsFromList(userWorkflowExecutions,
            userWorkflowExecutorManager.getMonitorCheckIntervalInSecs());
  }

  public void addUserWorkflowExecutionToQueue(String userWorkflowExecutionObjectId,
      int priority) {
    userWorkflowExecutorManager
        .addUserWorkflowExecutionToQueue(userWorkflowExecutionObjectId, priority);
  }

  private void checkRestrictionsOnUserWorkflowCreate(UserWorkflow userWorkflow)
      throws UserWorkflowAlreadyExistsException {

    if (StringUtils.isNotEmpty(userWorkflowExists(userWorkflow))) {
      throw new UserWorkflowAlreadyExistsException(String.format(
          "UserWorkflow with workflowOwner: %s, and workflowName: %s, already exists",
          userWorkflow.getWorkflowOwner(), userWorkflow.getWorkflowName()));
    }
  }

  private String checkRestrictionsOnUserWorkflowUpdate(UserWorkflow userWorkflow)
      throws NoUserWorkflowFoundException {

    String storedId = userWorkflowExists(userWorkflow);
    if (StringUtils.isEmpty(storedId)) {
      throw new NoUserWorkflowFoundException(String.format(
          "UserWorkflow with workflowOwner: %s, and workflowName: %s, not found",
          userWorkflow.getWorkflowOwner(),
          userWorkflow
              .getWorkflowName()));
    }

    return storedId;
  }

  private String userWorkflowExists(UserWorkflow userWorkflow) {
    return userWorkflowDao.exists(userWorkflow);
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

  public List<UserWorkflowExecution> getAllUserWorkflowExecutions(String datasetName,
      String workflowOwner,
      String workflowName,
      WorkflowStatus workflowStatus, String nextPage) {
    return userWorkflowExecutionDao
        .getAllUserWorkflowExecutions(datasetName, workflowOwner, workflowName, workflowStatus,
            nextPage);
  }

  public List<UserWorkflowExecution> getAllUserWorkflowExecutions(WorkflowStatus workflowStatus,
      String nextPage) {
    return userWorkflowExecutionDao.getAllUserWorkflowExecutions(workflowStatus, nextPage);
  }

  public ScheduledUserWorkflow getScheduledUserWorkflowByDatasetName(String datasetName) {
    return scheduledUserWorkflowDao.getScheduledUserWorkflowByDatasetName(datasetName);
  }

  public void scheduleUserWorkflow(ScheduledUserWorkflow scheduledUserWorkflow)
      throws NoDatasetFoundException, NoUserWorkflowFoundException, BadContentException, ScheduledUserWorkflowAlreadyExistsException {

    checkDatasetExistence(scheduledUserWorkflow.getDatasetName());
    checkUserWorkflowExistence(scheduledUserWorkflow.getWorkflowOwner(),
        scheduledUserWorkflow.getWorkflowName());
    checkScheduledUserWorkflowExistenceForDatasetName(scheduledUserWorkflow.getDatasetName());
    if (scheduledUserWorkflow.getPointerDate() == null) {
      throw new BadContentException("PointerDate cannot be null");
    }
    if (scheduledUserWorkflow.getScheduleFrequence() == ScheduleFrequence.NULL) {
      throw new BadContentException("NULL is not a valid scheduleFrequence");
    }
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

  private UserWorkflow checkUserWorkflowExistence(String workflowOwner, String workflowName)
      throws NoUserWorkflowFoundException {
    UserWorkflow userWorkflow = userWorkflowDao
        .getUserWorkflow(workflowOwner, workflowName);
    if (userWorkflow == null) {
      throw new NoUserWorkflowFoundException(String.format(
          "No user workflow found with workflowOwner: %s, and workflowName: %s, in METIS",
          workflowOwner, workflowName));
    }
    return userWorkflow;
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

  private String checkRestrictionsOnScheduledUserWorkflowUpdate(
      ScheduledUserWorkflow scheduledUserWorkflow)
      throws NoScheduledUserWorkflowFoundException, BadContentException, NoUserWorkflowFoundException {
    checkUserWorkflowExistence(scheduledUserWorkflow.getWorkflowOwner(),
        scheduledUserWorkflow.getWorkflowName());
    String storedId = scheduledUserWorkflowDao
        .existsForDatasetName(scheduledUserWorkflow.getDatasetName());
    if (StringUtils.isEmpty(storedId)) {
      throw new NoScheduledUserWorkflowFoundException(String.format(
          "UserWorkflow with datasetName: %s, not found", scheduledUserWorkflow.getDatasetName()));
    }
    if (scheduledUserWorkflow.getPointerDate() == null) {
      throw new BadContentException("PointerDate cannot be null");
    }
    if (scheduledUserWorkflow.getScheduleFrequence() == ScheduleFrequence.NULL) {
      throw new BadContentException("NULL is not a valid scheduleFrequence");
    }
    return storedId;
  }

  public void deleteScheduledUserWorkflow(String datasetName, String workflowOwner,
      String workflowName) {
    scheduledUserWorkflowDao.deleteScheduledUserWorkflow(datasetName, workflowOwner, workflowName);
  }
}
