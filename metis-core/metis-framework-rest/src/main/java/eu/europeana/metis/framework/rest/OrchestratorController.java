package eu.europeana.metis.framework.rest;

import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.framework.api.MetisKey;
import eu.europeana.metis.framework.api.Options;
import eu.europeana.metis.framework.dataset.Dataset;
import eu.europeana.metis.framework.exceptions.NoDatasetFoundException;
import eu.europeana.metis.framework.organization.Organization;
import eu.europeana.metis.framework.service.*;
import eu.europeana.metis.framework.workflow.Execution;
import eu.europeana.metis.ui.mongo.domain.DBUser;
import eu.europeana.metis.ui.mongo.domain.OrganizationRole;
import eu.europeana.metis.ui.mongo.domain.UserDTO;
import eu.europeana.metis.ui.mongo.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Path;
import java.util.ArrayList;
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
    @Autowired
    private MetisAuthorizationService authorizationService;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private DatasetService datasetService;

    @Autowired
    private eu.europeana.metis.ui.mongo.service.UserService userService;

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = RestEndpoints.ORCHESTRATION_TRIGGER_OPERATION, consumes = "application/json")
    public String execute(@PathVariable("datasetId") String datasetId, @PathVariable("operation") String operation,
                          @RequestBody(required = false) Map<String, List<String>> params) throws NoDatasetFoundException {
        return orchestrator.execute(datasetId, operation, params);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = RestEndpoints.ORCHESTRATION_SCHEDULE, consumes = "application/json")
    public String schedule(@PathVariable("datasetId") String datasetId, @PathVariable("operation") String operation,
                           @RequestBody(required = false) Map<String, List<String>> params, @PathVariable("millis") long millis) throws NoDatasetFoundException {
        return orchestrator.schedule(datasetId, operation, params, millis);
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.GET, value = RestEndpoints.ORCHESTRATION_SCHEDULED)
    public void executeScheduled() {
        orchestrator.executeScheduled();
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = RestEndpoints.ORCHESTRATION_ACTIVE)
    public List<Execution> getActiveExecutions() {
        return orchestrator.getActiveExecutions();
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = RestEndpoints.ORCHESTRATION_BYID)
    public Execution getExecution(@PathVariable("executionId") String executionId) {
        return orchestrator.getExecution(executionId);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = RestEndpoints.ORCHESTRATION_RANGE)
    public List<Execution> getExecutionsByDate(@RequestParam("start") long start, @RequestParam("end") long end) {
        return orchestrator.getExecutionsByDates(new Date(start), new Date(end));
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = RestEndpoints.ORCHESTRATION_EXECUTIONS)
    public List<Execution> getExecutions(@RequestParam("offset") int offset, @RequestParam("limit") int limit) {
        return orchestrator.getAllExecutions(offset, limit);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = RestEndpoints.ORCHESTRATION_DATASET)
    public List<Execution> getExecutionsForDataset(@PathVariable("datasetId") String datasetId,
                                                   @RequestParam("offset") int offset, @RequestParam("limit") int limit) throws NoDatasetFoundException {
        return orchestrator.getAllExecutionsForDataset(datasetId, offset, limit);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = RestEndpoints.ORCHESTRATION_RANGE_DATASET)
    public List<Execution> getExecutionsForDatasetByDate(@PathVariable("datasetId") String datasetId,
                                                         @RequestParam("offset") int offset, @RequestParam("limit") int limit,
                                                         @RequestParam("start") long start, @RequestParam("end") long end) throws NoDatasetFoundException {
        return orchestrator.getAllExecutionsForDatasetByDates(datasetId, offset, limit, new Date(start), new Date(end));
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

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = RestEndpoints.ORCHESTRATION_UPLOAD)
    public boolean uploadRecordsToCloud(@RequestBody List<String> records,
                                        @PathVariable("datasetkey") String datasetKey,
                                        @PathVariable("apikey") String apikey) {
        return checkKeysValidity(datasetKey, apikey) && orchestrator.uploadRecords(records);
    }

    private boolean checkKeysValidity(String datasetKey, String apikey) {
        MetisKey metisKey = authorizationService.getKeyFromId(apikey);
        if (metisKey == null || metisKey.getOptions().equals(Options.READ)) {
            return false;
        }

        String userMail = metisKey.getUserEmail();
        try {
            Dataset ds = datasetService.getByDatasetkey(datasetKey);
            if (ds == null) {
                return false;
            }
            UserDTO user = userService.getUser(userMail);
            if (user == null) {
                return false;
            }
            List<OrganizationRole> userRoles = user.getDbUser().getOrganizationRoles();
            if (userRoles == null) {
                return false;
            }
            Organization org = organizationService.getOrganizationForDataset(ds.getId().toString());
            if (org == null) {
                return false;
            }
            for (OrganizationRole role : userRoles) {
                if (StringUtils.equals(role.getOrganizationId(), org.getOrganizationId())) {
                    return true;
                }
            }
        } catch (NoDatasetFoundException e) {
            return false;
        }
        return false;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.DELETE, value = RestEndpoints.ORCHESTRATION_DELETE)
    public boolean deleteRecordsToCloud(@RequestBody List<String> records,
                                        @PathVariable("datasetkey") String datasetKey,
                                        @PathVariable("apikey") String apikey) {
        return checkKeysValidity(datasetKey, apikey) && orchestrator.deleteRecords(records);
    }

}
