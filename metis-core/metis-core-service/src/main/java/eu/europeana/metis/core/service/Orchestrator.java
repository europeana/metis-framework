package eu.europeana.metis.core.service;

import eu.europeana.metis.core.dao.ExecutionDao;
import eu.europeana.metis.core.dao.FailedRecordsDao;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.workflow.AbstractMetisWorkflow;
import eu.europeana.metis.core.workflow.CloudStatistics;
import eu.europeana.metis.core.workflow.Execution;
import eu.europeana.metis.core.workflow.ExecutionStatistics;
import eu.europeana.metis.core.workflow.FailedRecords;
import eu.europeana.metis.core.workflow.WorkflowParameters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.query.ArraySlice;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Orchestrator implementation for Metis
 * Created by ymamakis on 11/15/16.
 */
@Service
public class Orchestrator {

    @Autowired
    private ExecutionDao executionDao;
    @Autowired
    private DatasetService datasetService;
    @Autowired
    private FailedRecordsDao failedRecordsDao;
    @Autowired
    @Qualifier("abstractMetisWorkflowRegistry")
    private OrderAwarePluginRegistry registry;

    /**
     * Execute the worklow that supports the selected operation
     * @param datasetId The dataset Id for which to create the workflow execution
     * @param name The operation to be performed. This parameter is required to filter out the Workflow
     * @param params The execution parameters
     * @return The URL that the execution is available through
     */
    public String execute(String datasetId, String name, String operatorMail, Map<String, List<String>> params) throws NoDatasetFoundException {
       // if(datasetService.exists(datasetId)) {
        if(true){
            AbstractMetisWorkflow workflow = (AbstractMetisWorkflow) registry.getPluginFor(name);
            Execution execution = new Execution();
            execution.setId(new ObjectId());
            execution.setDatasetId(datasetId);
            execution.setWorkflow(name);
            execution.setStartedAt(new Date());
            execution.setUpdatedAt(new Date());
            execution.setActive(true);
            execution.setOperatorEmail(operatorMail);
            if(params==null){
                params = new HashMap<>();
            }
            List<String> paramList = new ArrayList<>();
            paramList.add(datasetId);
            params.put(WorkflowParameters.DATASET,paramList);
            List<String> executionParams = new ArrayList<>();
            executionParams.add(execution.getId().toString());
            params.put(WorkflowParameters.EXECUTION,executionParams);
            workflow.setParameters(params);
            execution.setStatisticsUrl("/" + execution.getId().toString());
            execution.setExecutionParameters(params);
            executionDao.save(execution);
            workflow.execute();
            return execution.getStatisticsUrl();
        }
            throw new NoDatasetFoundException(datasetId);
    }

    /**
     * Execute all scheduled jobs that have not started. This is updated every one hour.
     */
    @Scheduled(fixedDelay = 3600000)
    public void executeScheduled() {
        List<Execution> executions = getExecutions(null, null, null, null, null, new Date(), null, false,null,null);
        if (executions != null && executions.size() > 0) {
            for (Execution execution : executions) {
                AbstractMetisWorkflow workflow = (AbstractMetisWorkflow) registry.getPluginFor(execution.getWorkflow());
                UpdateOperations<Execution> ops = executionDao.createUpdateOperations();
                ops.set("startedAt", new Date());
                ops.set("updatedAt", new Date());
                ops.set("active", true);
                ops.set("statisticsUrl", "/" + execution.getWorkflow() + "-" + execution.getId().toString());
                workflow.execute();
                executionDao.update(executionDao.createQuery().filter("id", execution.getId()), ops);
            }
        }
    }

    /**
     * Get all active executions (not finished or cancelled)
     * @return
     */
    public List<Execution> getActiveExecutions(String operatorEmail) {
        return getExecutions(null, null, null, null, null, null, true, false,null,operatorEmail);
    }
    /**
     * Get all cancelled executions
     * @return
     */
    public List<Execution> getCancelledExecutions(String operatorEmail) {
        return getExecutions(null, null, null, null, null, null, false, true,null,operatorEmail);
    }

    /**
     * Get all finished executions
     * @return
     */
    public List<Execution> getFinishedExecutions(String operatorEmail) {
        return getExecutions(null, null, null, null, new Date(), null, false, null,null, operatorEmail);
    }
    /**
     * Get all executions by workflow
     * @return
     */
    public List<Execution> getByWorkflowExecutions(String workflow,String operatorEmail) {
        return getExecutions(null, null, null, null, null, null, false, null,workflow, operatorEmail);
    }


    /**
     * Get a specific execution
     * @param id The id of the execution
     * @return The execution along with its current execution statistics
     */
    public Execution getExecution(String id) {
        return executionDao.findOne("id", new ObjectId(id));
    }

    /**
     * Get executions by date range
     * @param start The starting date
     * @param end The final date
     * @return The list of executions that matches the search criteria
     */
    public List<Execution> getExecutionsByDates(Date start, Date end, String operatorEmail) {
        return getExecutions(null, null, null, start, end, null, null, null,null,operatorEmail);
    }

    /**
     * Get executions paginated
     * @param offset The offset of the search
     * @param limit The number of results to retrieve
     * @return The List of executions that correspond to the query
     */
    public List<Execution> getAllExecutions(int offset, int limit,String operatorEmail) {
        return getExecutions(null, offset, limit, null, null, null, null, null,null,operatorEmail);
    }

    /**
     * Get all the executions for a dataset
     * @param datasetId The dataset identifier
     * @param offset The offset of the retrieval results
     * @param limit The number of results to retrieve
     * @return The List of executions that correspond to the query
     */
    public List<Execution> getAllExecutionsForDataset(String datasetId, int offset, int limit,String operatorEmail) throws NoDatasetFoundException {
        //if(datasetService.exists(datasetId)) {
        if(true){
            return getExecutions(datasetId, offset, limit, null, null, null, null, null,null, operatorEmail);
        }
        throw new NoDatasetFoundException(datasetId);
    }

    /**
     * Get all the execution ids for a given dataset on given dates
     * @param datasetId The dataset identifier
     * @param offset The offset of the retrieval results
     * @param limit The number of results to retrieve
     * @param start The start date
     * @param end The end date
     * @return The List of executions that correspond to the query
     */
    public List<Execution> getAllExecutionsForDatasetByDates(String datasetId, int offset, int limit, Date start, Date end,String operatorEmail) throws NoDatasetFoundException {
        if(datasetService.exists(datasetId)) {
            return getExecutions(datasetId, offset, limit, start, end, null, null, null,null, operatorEmail);
        }
        throw new NoDatasetFoundException(datasetId);
    }

    /**
     * Update the execution statistics. Currently set to every 10 seconds
     */
    @Scheduled(fixedDelay = 10000)
    private void updateActiveExecutions() {
        List<Execution> activeExecutions = getActiveExecutions(null);
        if (activeExecutions != null && activeExecutions.size() > 0) {
            for (Execution activeExecution : activeExecutions) {
                UpdateOperations<Execution> ops = executionDao.createUpdateOperations();
                ops.set("updatedAt", new Date());
                ExecutionStatistics stats = activeExecution.getStatistics();
                if (stats == null) {
                    stats = new ExecutionStatistics();
                }
                AbstractMetisWorkflow workflow=  (AbstractMetisWorkflow)registry.getPluginFor(activeExecution.getWorkflow());
                CloudStatistics statistics =workflow.monitor(activeExecution.getDatasetId());
                stats.setProcessed(statistics.getProcessed());
                stats.setCreated(statistics.getCreated());
                stats.setDeleted(statistics.getDeleted());
                stats.setUpdated(statistics.getUpdated());
                ops.set("statistics", stats);
                if(StringUtils.equals(stats.getStatus(),"finished")){
                    ops.set("finishedAt",new Date());
                    ops.set("active",false);
                }
                executionDao.update(executionDao.createQuery().filter("id", activeExecution.getId()), ops);
                if(statistics.getFailedRecords().size()>0){
                    FailedRecords failedRecords = getFailed(activeExecution.getId().toString());
                    if(failedRecords == null) {
                        failedRecords=new FailedRecords();
                    }
                    failedRecords.setRecords(statistics.getFailedRecords());
                    failedRecords.setExecutionId(activeExecution.getId().toString());
                    failedRecords.setId(new ObjectId());
                    failedRecordsDao.save(failedRecords);
                }
            }
        }
    }

    private FailedRecords getFailed(String s) {
        return failedRecordsDao.findOne("executionId",s);
    }

    /**
     * Get the executions based on specific criterias
     * @param datasetId The dataset identifier
     * @param offset The offset
     * @param limit The number of results to retrieve
     * @param start The start date
     * @param end The end date
     * @param scheduled The scheduled date
     * @param active If the execution is active or not
     * @param cancelled If the execution is cancelled or not
     * @return The list of exectuions that conform to this criteria
     */
    private List<Execution> getExecutions(String datasetId, Integer offset, Integer limit, Date start, Date end, Date scheduled, Boolean active,
                                          Boolean cancelled,String workflow,String operatorEmail) {
        Query<Execution> query = executionDao.createQuery();
        if (offset != null) {
            query.offset(offset);
        }
        if (limit != null) {
            query.limit(limit);
        }
        if (StringUtils.isNotEmpty(datasetId)) {
            query.filter("datasetId", datasetId);
        }
        if (start != null) {
            query.field("startedAt").greaterThanOrEq(start);
        }
        if (end != null) {
            query.field("finishedAt").lessThanOrEq(end);
        }
        if (scheduled != null) {
            query.field("scheduledAt").lessThanOrEq(scheduled);
            query.filter("active", false);
            query.field("finishedAt").doesNotExist();
        }
        if (active != null) {
            query.filter("active", active);
        }
        if (cancelled != null) {
            query.filter("cancelled", cancelled);
        }
        if(workflow!=null){
            query.filter("workflow",workflow);
        }

        if(operatorEmail!=null){
            query.filter("operatorEmail",operatorEmail);
        }
        return executionDao.find(query).asList();
    }

    /**
     * Schedule a workflow execution for a dataset
     * @param datasetId The dataset identifier
     * @param name The name of the operation to execute
     * @param params The parameters of the execution of the workflow
     * @param milliseconds The number of milliseconds after which the execution will be triggered
     * @return The List of executions that correspond to the query
     */
    public String schedule(String datasetId, String name, String operatorMail, Map<String, List<String>> params, long milliseconds) throws NoDatasetFoundException {
      //  if(datasetService.exists(datasetId)) {
        if(true){
            Execution execution = new Execution();
            execution.setId(new ObjectId());
            execution.setDatasetId(datasetId);
            execution.setWorkflow(name);
            execution.setOperatorEmail(operatorMail);
            long when = new Date().getTime() + milliseconds;
            execution.setScheduledAt(new Date(when));
            execution.setActive(false);
            execution.setStatisticsUrl("/" + name + "-" + execution.getId().toString());
            execution.setExecutionParameters(params);
            executionDao.save(execution);
            return execution.getStatisticsUrl();
        }
        throw new NoDatasetFoundException(datasetId);
    }

    /**
     * Cancel a task
     * @param id The execution id to cancel
     */
    public void cancel(String id) {
        UpdateOperations<Execution> ops = executionDao.createUpdateOperations();
        ops.set("cancelled", true);
        ops.set("finishedAt", new Date());
        //TODO set cancelled in Europeana Cloud
        executionDao.update(executionDao.createQuery().filter("id", new ObjectId(id)), ops);
    }

    /**
     * Get all available workflows supported by Metis
     * @return The List of workflows
     */
    public List<String> getAvailableWorkflows() {
        List<AbstractMetisWorkflow> plugins = registry.getPlugins();
        List<String> operations = new ArrayList<>();
        for (AbstractMetisWorkflow plugin : plugins) {
            operations.add(plugin.getName());
        }
        return operations;
    }

    /**
     * Get the failed records for an executionId
     * @param executionId The execution identifier
     * @param offset The offset of records
     * @param limit The limit of records
     * @return A list of failed records for a dataset or empty
     */
    public List<String> getFailedRecords(String executionId, int offset, int limit){
        Query<FailedRecords> query = failedRecordsDao.createQuery();
        query.filter("executionId",executionId);
        query.project("records", new ArraySlice(offset,limit));
        FailedRecords records = failedRecordsDao.findOne(query);
        if(records!=null){
            return records.getRecords();
        }
        return Collections.emptyList();
    }
}
