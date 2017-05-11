package eu.europeana.metis.core.rest;

import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.service.Orchestrator;
import eu.europeana.metis.core.workflow.Execution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Orchestration REST Endpoint
 * Created by ymamakis on 11/15/16.
 */
@Controller
public class OrchestratorController {
    @Autowired
    private Orchestrator orchestrator;

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = RestEndpoints.ORCHESTRATION_TRIGGER_OPERATION, consumes = "application/json")
    public String execute(@PathVariable("datasetId") String datasetId, @PathVariable("operation") String operation,
                          @RequestBody(required = false) Map<String, List<String>> params,
                          @RequestParam("operatorEmail") String operatorEmail) throws NoDatasetFoundException {
        return orchestrator.execute(datasetId, operation,operatorEmail, params);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = RestEndpoints.ORCHESTRATION_SCHEDULE, consumes = "application/json")
    public String schedule(@PathVariable("datasetId") String datasetId, @PathVariable("operation") String operation,
                           @RequestBody(required = false) Map<String, List<String>> params, @PathVariable("millis") long millis,
                           @RequestParam("operatorEmail") String operatorEmail) throws NoDatasetFoundException {
        return orchestrator.schedule(datasetId, operation, operatorEmail, params, millis);
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.GET, value = RestEndpoints.ORCHESTRATION_SCHEDULED)
    public void executeScheduled() {
        orchestrator.executeScheduled();
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = RestEndpoints.ORCHESTRATION_ACTIVE)
    public List<Execution> getActiveExecutions(@RequestParam("operatorEmail") String operatorEmail) {
        return orchestrator.getActiveExecutions(operatorEmail);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = RestEndpoints.ORCHESTRATION_BYID)
    public Execution getExecution(@PathVariable("executionId") String executionId) {
        return orchestrator.getExecution(executionId);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = RestEndpoints.ORCHESTRATION_RANGE)
    public List<Execution> getExecutionsByDate(@RequestParam("start") long start, @RequestParam("end") long end,
                                               @RequestParam("operatorEmail") String operatorEmail) {
        return orchestrator.getExecutionsByDates(new Date(start), new Date(end), operatorEmail);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = RestEndpoints.ORCHESTRATION_EXECUTIONS)
    public List<Execution> getExecutions(@RequestParam("offset") int offset, @RequestParam("limit") int limit,
                                         @RequestParam("operatorEmail") String operatorEmail) {
        return orchestrator.getAllExecutions(offset, limit, operatorEmail);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = RestEndpoints.ORCHESTRATION_DATASET)
    public List<Execution> getExecutionsForDataset(@PathVariable("datasetId") String datasetId,
                                                   @RequestParam("offset") int offset, @RequestParam("limit") int limit,
                                                   @RequestParam("operatorEmail") String operatorEmail) throws NoDatasetFoundException {
        return orchestrator.getAllExecutionsForDataset(datasetId, offset, limit, operatorEmail);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = RestEndpoints.ORCHESTRATION_RANGE_DATASET)
    public List<Execution> getExecutionsForDatasetByDate(@PathVariable("datasetId") String datasetId,
                                                         @RequestParam("offset") int offset, @RequestParam("limit") int limit,
                                                         @RequestParam("start") long start, @RequestParam("end") long end,
                                                         @RequestParam("operatorEmail") String operatorEmail) throws NoDatasetFoundException {
        return orchestrator.getAllExecutionsForDatasetByDates(datasetId, offset, limit, new Date(start), new Date(end), operatorEmail);
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.DELETE, value = RestEndpoints.ORCHESTRATION_BYID)
    public void cancelExecution(@PathVariable("executionId") String executionId) {
        orchestrator.cancel(executionId);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = RestEndpoints.ORCHESTRATION_OPERATIONS)
    public List<String> getOperations() {
        return orchestrator.getAvailableWorkflows();
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = RestEndpoints.ORCHESTRATION_FAILED)
    public List<String> getFailedRecords(@PathVariable("executionId") String executionId, @RequestParam("offset") int offset, @RequestParam("limit") int limit) {
        return orchestrator.getFailedRecords(executionId, offset, limit);
    }
}
