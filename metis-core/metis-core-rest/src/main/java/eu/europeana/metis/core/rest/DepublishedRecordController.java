package eu.europeana.metis.core.rest;

import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.authentication.rest.client.AuthenticationClient;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.service.DepublishedRecordService;
import eu.europeana.metis.core.util.SortDirection;
import eu.europeana.metis.core.util.DepublishedRecordSortField;
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
 * Controller for calls related to depublished records.
 */
@Controller
public class DepublishedRecordController {

  private static final Logger LOGGER = LoggerFactory.getLogger(DepublishedRecordController.class);

  private final DepublishedRecordService depublishedRecordService;
  private final AuthenticationClient authenticationClient;

  /**
   * Autowired constructor with all required parameters.
   *
   * @param depublishedRecordService the service for depublished records.
   * @param authenticationClient the java client to communicate with the external authentication
   * service
   */
  @Autowired
  public DepublishedRecordController(DepublishedRecordService depublishedRecordService,
          AuthenticationClient authenticationClient) {
    this.depublishedRecordService = depublishedRecordService;
    this.authenticationClient = authenticationClient;
  }

  /**
   * Adds a list of depublished records to the dataset - the version for a simple text body.
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
  @PostMapping(value = RestEndpoints.DEPUBLISHED_RECORDS_DATASETID, consumes = {
          MediaType.TEXT_PLAIN_VALUE})
  @ResponseStatus(HttpStatus.CREATED)
  public void createDepublishedRecords(
          @RequestHeader("Authorization") String authorization,
          @PathVariable("datasetId") String datasetId,
          @RequestBody String recordIdsInSeparateLines
  ) throws GenericMetisException {
    final MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    final int added = depublishedRecordService
            .addRecordsToBeDepublished(metisUser, datasetId, recordIdsInSeparateLines);
    LOGGER.info("{} Depublished records added to dataset with datasetId: {}", added, datasetId);
  }

  /**
   * Adds a list of depublished records to the dataset - the version for a multipart file.
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
  @PostMapping(value = RestEndpoints.DEPUBLISHED_RECORDS_DATASETID, consumes = {
          MediaType.MULTIPART_FORM_DATA_VALUE})
  @ResponseStatus(HttpStatus.CREATED)
  public void createDepublishedRecords(
          @RequestHeader("Authorization") String authorization,
          @PathVariable("datasetId") String datasetId,
          @RequestPart("depublicationFile") MultipartFile recordIdsFile
  ) throws GenericMetisException, IOException {
    createDepublishedRecords(authorization, datasetId,
            new String(recordIdsFile.getBytes(), StandardCharsets.UTF_8));
  }

  /**
   * Retrieve the list of depublished records for a specific dataset.
   *
   * @param authorization the HTTP Authorization header, in the form of a Bearer Access Token.
   * @param datasetId The ID of the dataset for which to retrieve the records.
   * @param page The page to retrieve.
   * @param sortFieldString The field on which to sort.
   * @param sortDirectionString The direction in which to sort.
   * @param searchQuery Search query for the record ID.
   * @return A list of records.
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoDatasetFoundException} if the dataset for datasetId was not found.</li>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.DEPUBLISHED_RECORDS_DATASETID, produces = {
          MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseListWrapper<DepublishedRecordView> getDepublishedRecords(
          @RequestHeader("Authorization") String authorization,
          @PathVariable("datasetId") String datasetId,
          @RequestParam(value = "page", defaultValue = "0") int page,
          @RequestParam(value = "sortField", defaultValue = "") String sortFieldString,
          @RequestParam(value = "sortDirection", defaultValue = "") String sortDirectionString,
          @RequestParam(value = "searchQuery", required = false) String searchQuery
  ) throws GenericMetisException {

    // Get the user
    final MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);

    // Get the sort field
    final DepublishedRecordSortField sortField;
    switch (sortFieldString.toLowerCase()) {
      case "depublicationstatus":
        sortField = DepublishedRecordSortField.DEPUBLICATION_STATE;
        break;
      case "depublicationdate":
        sortField = DepublishedRecordSortField.DEPUBLICATION_DATE;
        break;
      default:
        sortField = DepublishedRecordSortField.RECORD_ID;
        break;
    }

    // Get the sort direction
    final SortDirection sortDirection = sortDirectionString.equalsIgnoreCase("desc") ?
            SortDirection.DESCENDING : SortDirection.ASCENDING;

    // Perform the query
    return depublishedRecordService.getDepublishedRecords(metisUser, datasetId, page, sortField,
            sortDirection, searchQuery);
  }
}
