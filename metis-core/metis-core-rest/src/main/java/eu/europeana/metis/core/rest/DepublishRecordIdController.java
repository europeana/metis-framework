package eu.europeana.metis.core.rest;

import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.authentication.rest.client.AuthenticationClient;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.service.DepublishRecordIdService;
import eu.europeana.metis.core.util.SortDirection;
import eu.europeana.metis.core.util.DepublishedRecordSortField;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.GenericMetisException;
import eu.europeana.metis.exception.UserUnauthorizedException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller for calls related to depublish record ids.
 */
@Controller
public class DepublishRecordIdController {

  private static final Logger LOGGER = LoggerFactory.getLogger(DepublishRecordIdController.class);

  private final DepublishRecordIdService depublishRecordIdService;
  private final AuthenticationClient authenticationClient;

  /**
   * Autowired constructor with all required parameters.
   *
   * @param depublishRecordIdService the service for depublished records.
   * @param authenticationClient the java client to communicate with the external authentication
   * service
   */
  @Autowired
  public DepublishRecordIdController(DepublishRecordIdService depublishRecordIdService,
      AuthenticationClient authenticationClient) {
    this.depublishRecordIdService = depublishRecordIdService;
    this.authenticationClient = authenticationClient;
  }

  /**
   * Adds a list of record ids to be depublished for the dataset - the version for a simple text
   * body.
   *
   * @param authorization the HTTP Authorization header, in the form of a Bearer Access Token.
   * @param datasetId The dataset ID to which the depublished records belong.
   * @param recordIdsInSeparateLines The string containing the record IDs in separate lines.
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoDatasetFoundException} if the dataset for datasetId was not found.</li>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized</li>
   * <li>{@link BadContentException} if some content or the operation were invalid</li>
   * </ul>
   */
  @PostMapping(value = RestEndpoints.DEPUBLISH_RECORDIDS_DATASETID, consumes = {
      MediaType.TEXT_PLAIN_VALUE})
  @ResponseStatus(HttpStatus.CREATED)
  public void createRecordIdsToBeDepublished(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("datasetId") String datasetId,
      @RequestBody String recordIdsInSeparateLines
  ) throws GenericMetisException {
    final MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    final int added = depublishRecordIdService
        .addRecordIdsToBeDepublished(metisUser, datasetId, recordIdsInSeparateLines);
    LOGGER.info("{} Depublished records added to dataset with datasetId: {}", added, datasetId);
  }

  /**
   * Adds a list of record ids to be depublished for the dataset - the version for a multipart
   * file.
   *
   * @param authorization the HTTP Authorization header, in the form of a Bearer Access Token.
   * @param datasetId The dataset ID to which the depublished records belong.
   * @param recordIdsFile The file containing the record IDs in separate lines.
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoDatasetFoundException} if the dataset for datasetId was not found.</li>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized</li>
   * <li>{@link BadContentException} if some content or the operation were invalid</li>
   * </ul>
   */
  @PostMapping(value = RestEndpoints.DEPUBLISH_RECORDIDS_DATASETID, consumes = {
      MediaType.MULTIPART_FORM_DATA_VALUE})
  @ResponseStatus(HttpStatus.CREATED)
  public void createRecordIdsToBeDepublished(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("datasetId") String datasetId,
      @RequestPart("depublicationFile") MultipartFile recordIdsFile
  ) throws GenericMetisException, IOException {
    createRecordIdsToBeDepublished(authorization, datasetId,
        new String(recordIdsFile.getBytes(), StandardCharsets.UTF_8));
  }

  /**
   * Retrieve the list of depublish record ids for a specific dataset.
   *
   * @param authorization the HTTP Authorization header, in the form of a Bearer Access Token.
   * @param datasetId The ID of the dataset for which to retrieve the records.
   * @param page The page to retrieve.
   * @param sortField The field on which to sort.
   * @param sortAscending The direction in which to sort.
   * @param searchQuery Search query for the record ID.
   * @return A list of records.
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoDatasetFoundException} if the dataset for datasetId was not found.</li>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.DEPUBLISH_RECORDIDS_DATASETID, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseListWrapper<DepublishedRecordView> getDepublishRecordIds(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("datasetId") String datasetId,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "sortField", required = false) DepublishedRecordSortField sortField,
      @RequestParam(value = "sortAscending", defaultValue = "" + true) boolean sortAscending,
      @RequestParam(value = "searchQuery", required = false) String searchQuery
  ) throws GenericMetisException {
    final MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    return depublishRecordIdService.getDepublishRecordIds(metisUser, datasetId, page,
        sortField == null ? DepublishedRecordSortField.RECORD_ID : sortField,
        sortAscending ? SortDirection.ASCENDING : SortDirection.DESCENDING, searchQuery);
  }

  /**
   * Does checking, prepares and adds a WorkflowExecution with a single Depublish step in the queue.
   * That means it updates the status of the WorkflowExecution to {@link
   * eu.europeana.metis.core.workflow.WorkflowStatus#INQUEUE}, adds it to the database and also it's
   * identifier goes into the distributed queue of WorkflowExecutions.
   *
   * @param authorization the authorization header with the access token
   * @param datasetId the dataset identifier for which the execution will take place
   * @param datasetDepublish true for dataset depublication, false for record depublication
   * @param priority the priority of the execution in case the system gets overloaded, 0 lowest, 10
   * highest
   * @return the WorkflowExecution object that was generated
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link BadContentException} if the workflow is empty or no plugin enabled</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoDatasetFoundException} if the dataset
   * identifier provided does not exist</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authenticated or authorized to perform this operation</li>
   * <li>{@link eu.europeana.metis.exception.ExternalTaskException} if there was an exception when
   * contacting the external resource(ECloud)</li>
   * <li>{@link eu.europeana.metis.core.exceptions.PluginExecutionNotAllowed} if the execution of
   * the first plugin was not allowed, because a valid source plugin could not be found</li>
   * <li>{@link eu.europeana.metis.core.exceptions.WorkflowExecutionAlreadyExistsException} if a
   * workflow execution for the generated execution identifier already exists, almost impossible to
   * happen since ids are UUIDs</li>
   * </ul>
   */
  @PostMapping(value = RestEndpoints.DEPUBLISH_EXECUTE_DATASETID, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.CREATED)
  public WorkflowExecution addDepublishWorkflowInQueueOfWorkflowExecutions(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("datasetId") String datasetId,
      @RequestParam(value = "datasetDepublish", defaultValue = "" + true) boolean datasetDepublish,
      @RequestParam(value = "priority", defaultValue = "0") int priority)
      throws GenericMetisException {
    MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    return depublishRecordIdService.createAndAddInQueueDepublishWorkflowExecution(metisUser, datasetId,
        datasetDepublish, priority);
  }
}
