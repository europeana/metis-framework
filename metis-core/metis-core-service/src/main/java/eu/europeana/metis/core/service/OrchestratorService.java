package eu.europeana.metis.core.service;

import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.UserWorkflowDao;
import eu.europeana.metis.core.dao.UserWorkflowExecutionDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoUserWorkflowExecutionFoundException;
import eu.europeana.metis.core.exceptions.NoUserWorkflowFoundException;
import eu.europeana.metis.core.exceptions.UserWorkflowAlreadyExistsException;
import eu.europeana.metis.core.exceptions.UserWorkflowExecutionAlreadyExistsException;
import eu.europeana.metis.core.execution.UserWorkflowExecutorManager;
import eu.europeana.metis.core.workflow.UserWorkflow;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
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
  private final DatasetDao datasetDao;
  private final UserWorkflowExecutorManager userWorkflowExecutorManager;

  @Autowired
  public OrchestratorService(UserWorkflowDao userWorkflowDao,
      UserWorkflowExecutionDao userWorkflowExecutionDao,
      DatasetDao datasetDao,
      UserWorkflowExecutorManager userWorkflowExecutorManager) {
    this.userWorkflowDao = userWorkflowDao;
    this.userWorkflowExecutionDao = userWorkflowExecutionDao;
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

  public void deleteUserWorkflow(String owner, String workflowName) {
    userWorkflowDao.deleteUserWorkflow(owner, workflowName);
  }

  public UserWorkflow getUserWorkflow(String owner, String workflowName) {
    return userWorkflowDao.getUserWorkflow(owner, workflowName);
  }

  public List<UserWorkflow> getAllUserWorkflows(String owner, String nextPage) {
    return userWorkflowDao.getAllUserWorkflows(owner, nextPage);
  }

  public UserWorkflowExecution getRunningUserWorkflowExecution(String datasetName, String owner,
      String workflowName) {
    return userWorkflowExecutionDao
        .getRunningUserWorkflowExecution(datasetName, owner, workflowName);
  }

  public void addUserWorkflowInQueueOfUserWorkflowExecutions(String datasetName,
      String owner, String workflowName, int priority)
      throws NoDatasetFoundException, NoUserWorkflowFoundException, UserWorkflowExecutionAlreadyExistsException {

    Dataset dataset = datasetDao.getDatasetByDatasetName(datasetName);
    if (dataset == null) {
      throw new NoDatasetFoundException(
          String.format("No dataset found with datasetName: %s, in METIS", datasetName));
    }
    UserWorkflow userWorkflow = userWorkflowDao
        .getUserWorkflow(owner, workflowName);
    if (userWorkflow == null) {
      throw new NoUserWorkflowFoundException(String.format(
          "No user workflow found with owner: %s, and workflowName: %s, in METIS", owner,
          workflowName));
    }

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

  public void addUserWorkflowInQueueOfUserWorkflowExecutions(String datasetName,
      UserWorkflow userWorkflow, int priority)
      throws UserWorkflowAlreadyExistsException, NoDatasetFoundException, UserWorkflowExecutionAlreadyExistsException {
    Dataset dataset = datasetDao.getDatasetByDatasetName(datasetName);
    if (dataset == null) {
      throw new NoDatasetFoundException(
          String.format("No dataset found with datasetName: %s, in METIS", datasetName));
    }
    //Generate workflowName for user.
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
      throws NoUserWorkflowExecutionFoundException, ExecutionException {

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

  private void checkRestrictionsOnUserWorkflowCreate(UserWorkflow userWorkflow)
      throws UserWorkflowAlreadyExistsException {

    if (StringUtils.isNotEmpty(workflowExists(userWorkflow))) {
      throw new UserWorkflowAlreadyExistsException(String.format(
          "UserWorkflow with owner: %s, and workflowName: %s, already exists",
          userWorkflow.getOwner(), userWorkflow.getWorkflowName()));
    }
  }

  private String checkRestrictionsOnUserWorkflowUpdate(UserWorkflow userWorkflow)
      throws NoUserWorkflowFoundException {

    String storedId = workflowExists(userWorkflow);
    if (StringUtils.isEmpty(storedId)) {
      throw new NoUserWorkflowFoundException(String.format(
          "UserWorkflow with owner: %s, and workflowName: %s, not found", userWorkflow.getOwner(),
          userWorkflow
              .getWorkflowName()));
    }

    return storedId;
  }

  private String workflowExists(UserWorkflow userWorkflow) {
    return userWorkflowDao.exists(userWorkflow);
  }

  public int getUserWorkflowExecutionsPerRequest() {
    return userWorkflowExecutionDao.getUserWorkflowExecutionsPerRequest();
  }

  public int getUserWorkflowsPerRequest() {
    return userWorkflowDao.getUserWorkflowsPerRequest();
  }

  public List<UserWorkflowExecution> getAllUserWorkflowExecutions(String datasetName, String owner,
      String workflowName,
      WorkflowStatus workflowStatus, String nextPage) {
    return userWorkflowExecutionDao
        .getAllUserWorkflowExecutions(datasetName, owner, workflowName, workflowStatus, nextPage);
  }

  public List<UserWorkflowExecution> getAllUserWorkflowExecutions(WorkflowStatus workflowStatus,
      String nextPage) {
    return userWorkflowExecutionDao.getAllUserWorkflowExecutions(workflowStatus, nextPage);
  }
}
