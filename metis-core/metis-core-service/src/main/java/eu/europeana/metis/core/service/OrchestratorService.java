package eu.europeana.metis.core.service;

import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.UserWorkflowDao;
import eu.europeana.metis.core.dao.UserWorkflowExecutionDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoUserWorkflowExecutionFoundException;
import eu.europeana.metis.core.exceptions.NoUserWorkflowFoundException;
import eu.europeana.metis.core.exceptions.UserWorkflowExecutionAlreadyExistsException;
import eu.europeana.metis.core.workflow.UserWorkflow;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
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

  private final Logger LOGGER = LoggerFactory.getLogger(OrchestratorService.class);

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

    new Thread(userWorkflowExecutorManager).start();
  }

  public void createUserWorkflow(UserWorkflow userWorkflow) throws BadContentException {
    checkRestrictionsOnUserWorkflowCreate(userWorkflow);
    userWorkflowDao.create(userWorkflow);
  }

  public void deleteUserWorkflow(String owner, String workflowName) {
    userWorkflowDao.deleteUserWorkflow(owner, workflowName);
  }

  public UserWorkflow getUserWorkflow(String owner, String workflowName) {
    return userWorkflowDao.getUserWorkflow(owner, workflowName);
  }

  public UserWorkflowExecution getRunningUserWorkflowExecution(String datasetName, String owner, String workflowName) {
    return userWorkflowExecutionDao.getRunningUserWorkflowExecution(datasetName, owner, workflowName);
  }

  public void addUserWorkflowInQueueOfUserWorkflowExecutions(String datasetName,
      String owner, String workflowName, int priority)
      throws NoDatasetFoundException, NoUserWorkflowFoundException, UserWorkflowExecutionAlreadyExistsException {

    Dataset dataset = datasetDao.getDatasetByDatasetName(datasetName);
    if (dataset == null) {
      throw new NoDatasetFoundException(
          "No dataset found with datasetName: " + datasetName + " in METIS");
    }
    UserWorkflow userWorkflow = userWorkflowDao
        .getUserWorkflow(owner, workflowName);
    if (userWorkflow == null) {
      throw new NoUserWorkflowFoundException(
          "No user workflow found with owner: " + owner + " and workflowName: " + workflowName
              + " in METIS");
    }

    UserWorkflowExecution userWorkflowExecution = new UserWorkflowExecution(dataset, userWorkflow,
        priority);
    userWorkflowExecution.setWorkflowStatus(WorkflowStatus.INQUEUE);
    String storedUserWorkflowExecutionId = userWorkflowExecutionDao
        .existsAndNotCompleted(datasetName);
    if (storedUserWorkflowExecutionId != null) {
      throw new UserWorkflowExecutionAlreadyExistsException(
          "User workflow execution already exists with id " + storedUserWorkflowExecutionId
              + " and is not completed");
    }
    userWorkflowExecution.setCreatedDate(new Date());
    String objectId = userWorkflowExecutionDao.create(userWorkflowExecution);
    userWorkflowExecution.setId(new ObjectId(objectId));
    userWorkflowExecutorManager.addUserWorkflowExecutionToQueue(userWorkflowExecution);
    LOGGER.info("UserWorkflowExecution with id: " + objectId + " added to execution queue");
  }

  public void cancelUserWorkflowExecution(String datasetName,
      String owner, String workflowName)
      throws NoUserWorkflowExecutionFoundException, ExecutionException {

    UserWorkflowExecution userWorkflowExecution = userWorkflowExecutionDao
        .getRunningOrInQueueExecution(datasetName, owner, workflowName);
    if (userWorkflowExecution != null) {
      userWorkflowExecutorManager.cancelUserWorkflowExecution(userWorkflowExecution);
    } else {
      throw new NoUserWorkflowExecutionFoundException(
          "UserworkflowExecution with datasetName: " + datasetName + ", owner: " + owner
              + ", workflowName: " + workflowName + " does not exist");
    }
  }

  private void checkRestrictionsOnUserWorkflowCreate(UserWorkflow userWorkflow)
      throws BadContentException {

    if (workflowExists(userWorkflow)) {
      throw new BadContentException(
          "UserWorkflow with owner: " + userWorkflow.getOwner() + " and workflowName: "
              + userWorkflow
              .getWorkflowName() + " already exists");
    }
  }

  private boolean workflowExists(UserWorkflow userWorkflow) {
    return userWorkflowDao.exists(userWorkflow);
  }

  public int getUserWorkflowExecutionsPerRequest()
  {
    return userWorkflowExecutionDao.getUserWorkflowExecutionsPerRequest();
  }

  public List<UserWorkflowExecution> getAllUserWorkflowExecutions(String datasetName, String owner,
      String workflowName,
      WorkflowStatus workflowStatus, String nextPage) {
    return userWorkflowExecutionDao.getAllUserWorkflowExecutions(datasetName, owner, workflowName, workflowStatus, nextPage);
  }

  /**
   * Execute all scheduled jobs that have not started. This is updated every one hour.
   */
//    @Scheduled(fixedDelay = 3600000)
//    public void executeScheduled() {
//        List<Execution> executions = getExecutions(null, null, null, null, null, new Date(), null, false,null,null);
//        if (executions != null && executions.size() > 0) {
//            for (Execution execution : executions) {
//                AbstractMetisPlugin workflow = (AbstractMetisPlugin) registry.getPluginFor(execution.getWorkflow());
//                UpdateOperations<Execution> ops = executionDao.createUpdateOperations();
//                ops.set("startedAt", new Date());
//                ops.set("updatedAt", new Date());
//                ops.set("active", true);
//                ops.set("statisticsUrl", "/" + execution.getWorkflow() + "-" + execution.getId().toString());
//                workflow.execute();
//                executionDao.update(executionDao.createQuery().filter("id", execution.getId()), ops);
//            }
//        }
//    }

//  /**
//   * Get all active executions (not finished or cancelled)
//   */
//  public List<Execution> getActiveExecutions(String operatorEmail) {
//    return getExecutions(null, null, null, null, null, null, true, false, null, operatorEmail);
//  }
//

  /**
   * Update the execution statistics. Currently set to every 10 seconds
   */
//    @Scheduled(fixedDelay = 10000)
//    private void updateActiveExecutions() {
//        List<Execution> activeExecutions = getActiveExecutions(null);
//        if (activeExecutions != null && activeExecutions.size() > 0) {
//            for (Execution activeExecution : activeExecutions) {
//                UpdateOperations<Execution> ops = executionDao.createUpdateOperations();
//                ops.set("updatedAt", new Date());
//                ExecutionStatistics stats = activeExecution.getStatistics();
//                if (stats == null) {
//                    stats = new ExecutionStatistics();
//                }
//                AbstractMetisPlugin workflow=  (AbstractMetisPlugin)registry.getPluginFor(activeExecution.getWorkflow());
//                CloudStatistics statistics =workflow.monitor(activeExecution.getDatasetId());
//                stats.setProcessed(statistics.getProcessed());
//                stats.setCreated(statistics.getCreated());
//                stats.setDeleted(statistics.getDeleted());
//                stats.setUpdated(statistics.getUpdated());
//                ops.set("statistics", stats);
//                if(StringUtils.equals(stats.getStatus(),"finished")){
//                    ops.set("finishedAt",new Date());
//                    ops.set("active",false);
//                }
//                executionDao.update(executionDao.createQuery().filter("id", activeExecution.getId()), ops);
//                if(statistics.getFailedRecords().size()>0){
//                    FailedRecords failedRecords = getFailed(activeExecution.getId().toString());
//                    if(failedRecords == null) {
//                        failedRecords=new FailedRecords();
//                    }
//                    failedRecords.setRecords(statistics.getFailedRecords());
//                    failedRecords.setExecutionId(activeExecution.getId().toString());
//                    failedRecords.setId(new ObjectId());
//                    failedRecordsDao.save(failedRecords);
//                }
//            }
//        }
//    }
//  private FailedRecords getFailed(String s) {
//    return failedRecordsDao.findOne("executionId", s);
//  }
//
//  /**
//   * Get the executions based on specific criterias
//   *
//   * @param datasetId The dataset identifier
//   * @param offset The offset
//   * @param limit The number of results to retrieve
//   * @param start The start date
//   * @param end The end date
//   * @param scheduled The scheduled date
//   * @param active If the execution is active or not
//   * @param cancelled If the execution is cancelled or not
//   * @return The list of exectuions that conform to this criteria
//   */
//  private List<Execution> getExecutions(String datasetId, Integer offset, Integer limit, Date start,
//      Date end, Date scheduled, Boolean active,
//      Boolean cancelled, String workflow, String operatorEmail) {
//    Query<Execution> query = executionDao.createQuery();
//    if (offset != null) {
//      query.offset(offset);
//    }
//    if (limit != null) {
//      query.limit(limit);
//    }
//    if (StringUtils.isNotEmpty(datasetId)) {
//      query.filter("datasetId", datasetId);
//    }
//    if (start != null) {
//      query.field("startedAt").greaterThanOrEq(start);
//    }
//    if (end != null) {
//      query.field("finishedAt").lessThanOrEq(end);
//    }
//    if (scheduled != null) {
//      query.field("scheduledAt").lessThanOrEq(scheduled);
//      query.filter("active", false);
//      query.field("finishedAt").doesNotExist();
//    }
//    if (active != null) {
//      query.filter("active", active);
//    }
//    if (cancelled != null) {
//      query.filter("cancelled", cancelled);
//    }
//    if (workflow != null) {
//      query.filter("workflow", workflow);
//    }
//
//    if (operatorEmail != null) {
//      query.filter("operatorEmail", operatorEmail);
//    }
//    return executionDao.find(query).asList();
//  }
//
//  /**
//   * Schedule a workflow execution for a dataset
//   *
//   * @param datasetId The dataset identifier
//   * @param name The name of the operation to execute
//   * @param params The parameters of the execution of the workflow
//   * @param milliseconds The number of milliseconds after which the execution will be triggered
//   * @return The List of executions that correspond to the query
//   */
//  public String schedule(String datasetId, String name, String operatorMail,
//      Map<String, List<String>> params, long milliseconds) throws NoDatasetFoundException {
//    //  if(datasetService.exists(datasetId)) {
//    if (true) {
//      Execution execution = new Execution();
//      execution.setId(new ObjectId());
//      execution.setDatasetId(datasetId);
//      execution.setWorkflow(name);
//      execution.setOperatorEmail(operatorMail);
//      long when = new Date().getTime() + milliseconds;
//      execution.setScheduledAt(new Date(when));
//      execution.setActive(false);
//      execution.setStatisticsUrl("/" + name + "-" + execution.getId().toString());
//      execution.setExecutionParameters(params);
//      executionDao.save(execution);
//      return execution.getStatisticsUrl();
//    }
//    throw new NoDatasetFoundException(datasetId);
//  }
//
//  /**
//   * Cancel a task
//   *
//   * @param id The execution id to cancel
//   */
//  public void cancel(String id) {
//    UpdateOperations<Execution> ops = executionDao.createUpdateOperations();
//    ops.set("cancelled", true);
//    ops.set("finishedAt", new Date());
//    //TODO set cancelled in Europeana Cloud
//    executionDao.update(executionDao.createQuery().filter("id", new ObjectId(id)), ops);
//  }
//
//  /**
//   * Get the failed records for an executionId
//   *
//   * @param executionId The execution identifier
//   * @param offset The offset of records
//   * @param limit The limit of records
//   * @return A list of failed records for a dataset or empty
//   */
//  public List<String> getFailedRecords(String executionId, int offset, int limit) {
//    Query<FailedRecords> query = failedRecordsDao.createQuery();
//    query.filter("executionId", executionId);
//    query.project("records", new ArraySlice(offset, limit));
//    FailedRecords records = failedRecordsDao.findOne(query);
//    if (records != null) {
//      return records.getRecords();
//    }
//    return Collections.emptyList();
//  }
}
