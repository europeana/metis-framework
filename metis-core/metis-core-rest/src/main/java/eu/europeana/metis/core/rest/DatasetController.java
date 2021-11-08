package eu.europeana.metis.core.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.europeana.metis.utils.CommonStringValues;
import eu.europeana.metis.utils.RestEndpoints;
import eu.europeana.metis.authentication.rest.client.AuthenticationClient;
import eu.europeana.metis.authentication.user.MetisUserView;
import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.core.common.Language;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetSearchView;
import eu.europeana.metis.core.dataset.DatasetXslt;
import eu.europeana.metis.core.dataset.DatasetXsltStringWrapper;
import eu.europeana.metis.core.exceptions.DatasetAlreadyExistsException;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoXsltFoundException;
import eu.europeana.metis.core.exceptions.XsltSetupException;
import eu.europeana.metis.core.service.DatasetService;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.TransformationPlugin;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.GenericMetisException;
import eu.europeana.metis.exception.UserUnauthorizedException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Contains all the calls that are related to Datasets.
 * <p>The {@link DatasetService} has control on how to manipulate a dataset</p>
 */
@Controller
public class DatasetController {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatasetController.class);

  private final DatasetService datasetService;
  private final AuthenticationClient authenticationClient;

  /**
   * Autowired constructor with all required parameters.
   *
   * @param datasetService the datasetService
   * @param authenticationClient the java client to communicate with the external authentication
   * service
   */
  @Autowired
  public DatasetController(DatasetService datasetService,
      AuthenticationClient authenticationClient) {
    this.datasetService = datasetService;
    this.authenticationClient = authenticationClient;
  }

  /**
   * Create a provided dataset.
   * <p>Dataset is provided as json or xml.</p>
   *
   * @param authorization the String provided by an HTTP Authorization header <p> The expected input
   * should follow the rule Bearer accessTokenHere </p>
   * @param dataset the provided dataset to be created
   * @return the dataset created including all other fields that are auto generated
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link DatasetAlreadyExistsException} if the dataset already exists for the organizationId and datasetName.</li>
   * <li>{@link UserUnauthorizedException} if the authorization header is un-parsable or the user cannot be authenticated or authorized or the user is unauthorized.</li>
   * </ul>
   */
  @PostMapping(value = RestEndpoints.DATASETS, consumes = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public Dataset createDataset(@RequestHeader("Authorization") String authorization,
      @RequestBody Dataset dataset)
      throws GenericMetisException {

    MetisUserView metisUserView = authenticationClient.getUserByAccessTokenInHeader(authorization);

    Dataset createdDataset = datasetService.createDataset(metisUserView, dataset);
    LOGGER.info("Dataset with datasetId: {}, datasetName: {} and organizationId {} created",
        createdDataset.getDatasetId(), createdDataset.getDatasetName(),
        createdDataset.getOrganizationId());
    return createdDataset;
  }

  /**
   * Update a provided dataset including an xslt string.
   * <p>
   * Non allowed fields, to be manually updated, will be ignored. Updating a dataset with a new xslt
   * will only overwrite the {@code Dataset#xsltId} and a new {@link DatasetXslt} object will be
   * stored. The older {@link DatasetXslt} will still be accessible.
   * </p>
   *
   * @param authorization the String provided by an HTTP Authorization header <p> The expected input
   * should follow the rule Bearer accessTokenHere </p>
   * @param datasetXsltStringWrapper {@link DatasetXsltStringWrapper}
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoDatasetFoundException} if the dataset was not found for the datasetId.</li>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized.</li>
   * <li>{@link DatasetAlreadyExistsException} if a datasetName change is requested and the datasetName for that organizationId already exists.</li>
   * </ul>
   */
  @PutMapping(value = RestEndpoints.DATASETS, consumes = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateDataset(@RequestHeader("Authorization") String authorization,
      @RequestBody DatasetXsltStringWrapper datasetXsltStringWrapper)
      throws GenericMetisException {

    MetisUserView metisUserView = authenticationClient.getUserByAccessTokenInHeader(authorization);

    datasetService
        .updateDataset(metisUserView, datasetXsltStringWrapper.getDataset(), datasetXsltStringWrapper
            .getXslt());
    LOGGER.info("Dataset with datasetId {} updated",
        datasetXsltStringWrapper.getDataset().getDatasetId());
  }

  /**
   * Delete a dataset using a datasetId.
   *
   * @param authorization the String provided by an HTTP Authorization header <p> The expected input
   * should follow the rule Bearer accessTokenHere </p>
   * @param datasetId the identifier used to find and delete the dataset
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized.</li>
   * <li>{@link NoDatasetFoundException} if the dataset was not found for datasetId</li>
   * </ul>
   */
  @DeleteMapping(value = RestEndpoints.DATASETS_DATASETID)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteDataset(@RequestHeader("Authorization") String authorization,
      @PathVariable("datasetId") String datasetId)
      throws GenericMetisException {

    MetisUserView metisUserView = authenticationClient.getUserByAccessTokenInHeader(authorization);

    datasetService.deleteDatasetByDatasetId(metisUserView, datasetId);
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Dataset with datasetId '{}' deleted",
          datasetId.replaceAll(CommonStringValues.REPLACEABLE_CRLF_CHARACTERS_REGEX, ""));
    }
  }

  /**
   * Get a dataset based on its datasetId
   *
   * @param authorization the String provided by an HTTP Authorization header <p> The expected input
   * should follow the rule Bearer accessTokenHere </p>
   * @param datasetId the identifier used to find a dataset
   * @return {@link Dataset}
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoDatasetFoundException} if the dataset was not found.</li>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized.</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.DATASETS_DATASETID, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Dataset getByDatasetId(@RequestHeader("Authorization") String authorization,
      @PathVariable("datasetId") String datasetId)
      throws GenericMetisException {

    MetisUserView metisUserView = authenticationClient.getUserByAccessTokenInHeader(authorization);

    Dataset storedDataset = datasetService.getDatasetByDatasetId(metisUserView, datasetId);
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Dataset with datasetId '{}' found",
          datasetId.replaceAll(CommonStringValues.REPLACEABLE_CRLF_CHARACTERS_REGEX, ""));
    }
    return storedDataset;
  }

  /**
   * Get the xslt object containing the escaped xslt string using a dataset identifier.
   *
   * @param authorization the String provided by an HTTP Authorization header <p> The expected input
   * should follow the rule Bearer accessTokenHere </p>
   * @param datasetId the identifier used to find a dataset
   * @return the {@link DatasetXslt} object containing the xslt as an escaped string
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoXsltFoundException} if the xslt was not found.</li>
   * <li>{@link NoDatasetFoundException} if the dataset was not found.</li>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized.</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.DATASETS_DATASETID_XSLT, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public DatasetXslt getDatasetXsltByDatasetId(@RequestHeader("Authorization") String authorization,
      @PathVariable("datasetId") String datasetId) throws GenericMetisException {

    MetisUserView metisUserView = authenticationClient.getUserByAccessTokenInHeader(authorization);

    DatasetXslt datasetXslt = datasetService.getDatasetXsltByDatasetId(metisUserView, datasetId);
    LOGGER.info("Dataset XSLT with datasetId '{}' and xsltId: '{}' found", datasetId,
        datasetXslt.getId());
    return datasetXslt;
  }

  /**
   * Get the xslt string as non escaped text using an xslt identifier.
   * <p>
   * It is a method that does not require authentication and it is meant to be used from external
   * service to download the corresponding xslt. At the point of writing, ECloud transformation
   * topology is using it. {@link TransformationPlugin}
   * </p>
   *
   * @param xsltId the xslt identifier
   * @return the text non escaped representation of the xslt string
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoXsltFoundException} if the xslt was not found.</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.DATASETS_XSLT_XSLTID, produces = {
      MediaType.TEXT_PLAIN_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public String getXsltByXsltId(@PathVariable("xsltId") String xsltId)
      throws GenericMetisException {
    DatasetXslt datasetXslt = datasetService.getDatasetXsltByXsltId(xsltId);
    LOGGER.info("XSLT with xsltId '{}' found", datasetXslt.getId());
    return datasetXslt.getXslt();
  }

  /**
   * Create a new default xslt in the database.
   * <p>
   * Each dataset can have it's own custom xslt but a default xslt should always be available.
   * Creating a new default xslt will create a new {@link DatasetXslt} object and the older one will
   * still be available. The created {@link DatasetXslt} will have it's {@code
   * DatasetXslt#datasetId} as -1 to indicate that it is not related to a specific dataset.
   * </p>
   *
   * @param authorization the String provided by an HTTP Authorization header <p> The expected input
   * should follow the rule Bearer accessTokenHere </p>
   * @param xsltString the text of the String representation non escaped
   * @return the created {@link DatasetXslt}
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized.</li>
   * </ul>
   */
  @PostMapping(value = RestEndpoints.DATASETS_XSLT_DEFAULT, consumes = {
      MediaType.TEXT_PLAIN_VALUE}, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public DatasetXslt createDefaultXslt(@RequestHeader("Authorization") String authorization,
      @RequestBody String xsltString)
      throws GenericMetisException {

    MetisUserView metisUserView = authenticationClient.getUserByAccessTokenInHeader(authorization);
    DatasetXslt defaultDatasetXslt = datasetService.createDefaultXslt(metisUserView, xsltString);
    LOGGER.info("New default xslt created with xsltId: {}", defaultDatasetXslt.getId());
    return defaultDatasetXslt;
  }

  /**
   * Get the latest created default xslt.
   * <p>
   * It is an method that does not require authentication and it is meant to be used from external
   * service to download the corresponding xslt. At the point of writing, ECloud transformation
   * topology is using it. {@link TransformationPlugin}
   * </p>
   *
   * @return the text representation of the String xslt non escaped
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoXsltFoundException} if the xslt was not found.</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.DATASETS_XSLT_DEFAULT, produces = {
      MediaType.TEXT_PLAIN_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public String getLatestDefaultXslt() throws GenericMetisException {
    DatasetXslt datasetXslt = datasetService.getLatestDefaultXslt();
    LOGGER.info("Default XSLT with xsltId '{}' found", datasetXslt.getId());
    return datasetXslt.getXslt();
  }

  /**
   * Transform a list of xmls using the latest dataset xslt stored.
   * <p>
   * This method is meant to be used after a response from {@link ProxiesController#getListOfFileContentsFromPluginExecution(String,
   * String, ExecutablePluginType, String)} to try a transformation on a list of xmls just after
   * validation external to preview an example result.
   * </p>
   *
   * @param authorization the String provided by an HTTP Authorization header <p> The expected input
   * should follow the rule Bearer accessTokenHere </p>
   * @param datasetId the dataset identifier, it is required for authentication and for the dataset
   * fields xslt injection
   * @param records the list of {@link Record} that contain the xml fields {@code
   * Record#xmlRecord}.
   * @return a list of {@link Record}s with the field {@code Record#xmlRecord} containing the
   * transformed xml
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the authorization header is un-parsable or the user cannot be
   * authenticated or authorized.</li>
   * <li>{@link NoDatasetFoundException} if the dataset was not found.</li>
   * <li>{@link NoXsltFoundException} if there is no xslt found</li>
   * <li>{@link XsltSetupException} if the XSL transform could not be set up</li>
   * </ul>
   */
  @PostMapping(value = RestEndpoints.DATASETS_DATASETID_XSLT_TRANSFORM, consumes = {
      MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<Record> transformRecordsUsingLatestDatasetXslt(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("datasetId") String datasetId,
      @RequestBody List<Record> records) throws GenericMetisException {
    MetisUserView metisUserView = authenticationClient.getUserByAccessTokenInHeader(authorization);
    return datasetService.transformRecordsUsingLatestDatasetXslt(metisUserView, datasetId, records);
  }

  /**
   * Transform a list of xmls using the latest default xslt stored.
   * <p>
   * This method is meant to be used after a response from {@link ProxiesController#getListOfFileContentsFromPluginExecution(String,
   * String, ExecutablePluginType, String)} to try a transformation on a list of xmls just after
   * validation external to preview an example result.
   * </p>
   *
   * @param authorization the String provided by an HTTP Authorization header <p> The expected input
   * should follow the rule Bearer accessTokenHere </p>
   * @param datasetId the dataset identifier, it is required for authentication and for the dataset
   * fields xslt injection
   * @param records the list of {@link Record} that contain the xml fields {@code
   * Record#xmlRecord}.
   * @return a list of {@link Record}s with the field {@code Record#xmlRecord} containing the
   * transformed xml
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the authorization header is un-parsable or the user cannot be
   * authenticated or authorized.</li>
   * <li>{@link NoDatasetFoundException} if the dataset was not found.</li>
   * <li>{@link NoXsltFoundException} if there is no xslt found</li>
   * <li>{@link XsltSetupException} if the XSL transform could not be set up</li>
   * </ul>
   */
  @PostMapping(value = RestEndpoints.DATASETS_DATASETID_XSLT_TRANSFORM_DEFAULT, consumes = {
      MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<Record> transformRecordsUsingLatestDefaultXslt(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("datasetId") String datasetId,
      @RequestBody List<Record> records) throws GenericMetisException {
    MetisUserView metisUserView = authenticationClient.getUserByAccessTokenInHeader(authorization);
    return datasetService.transformRecordsUsingLatestDefaultXslt(metisUserView, datasetId, records);
  }

  /**
   * Get a dataset based on its datasetName
   *
   * @param authorization the String provided by an HTTP Authorization header <p> The expected input
   * should follow the rule Bearer accessTokenHere </p>
   * @param datasetName the name of the dataset used to find a dataset
   * @return {@link Dataset}
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoDatasetFoundException} if the dataset was not found.</li>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized.</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.DATASETS_DATASETNAME, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Dataset getByDatasetName(@RequestHeader("Authorization") String authorization,
      @PathVariable("datasetName") String datasetName)
      throws GenericMetisException {

    MetisUserView metisUserView = authenticationClient.getUserByAccessTokenInHeader(authorization);

    Dataset dataset = datasetService.getDatasetByDatasetName(metisUserView, datasetName);
    LOGGER.info("Dataset with datasetName '{}' found", dataset.getDatasetName());
    return dataset;
  }

  /**
   * Get a list of all the datasets using the provider field for lookup.
   * <p>The results are paged and wrapped around {@link ResponseListWrapper}</p>
   *
   * @param authorization the String provided by an HTTP Authorization header <p> The expected input
   * should follow the rule Bearer accessTokenHere </p>
   * @param provider the provider used to search
   * @param nextPage the nextPage number or -1
   * @return {@link ResponseListWrapper}
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized.</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.DATASETS_PROVIDER, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseListWrapper<Dataset> getAllDatasetsByProvider(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("provider") String provider,
      @RequestParam(value = "nextPage", required = false, defaultValue = "0") int nextPage)
      throws GenericMetisException {
    if (nextPage < 0) {
      throw new BadContentException(CommonStringValues.NEXT_PAGE_CANNOT_BE_NEGATIVE);
    }

    MetisUserView metisUserView = authenticationClient.getUserByAccessTokenInHeader(authorization);

    ResponseListWrapper<Dataset> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper
        .setResultsAndLastPage(
            datasetService.getAllDatasetsByProvider(metisUserView, provider, nextPage),
            datasetService.getDatasetsPerRequestLimit(), nextPage);
    LOGGER.info(CommonStringValues.BATCH_OF_DATASETS_RETURNED,
        responseListWrapper.getListSize(), nextPage);
    return responseListWrapper;
  }

  /**
   * Get a list of all the datasets using the intermediateProvider field for lookup.
   * <p>The results are paged and wrapped around {@link ResponseListWrapper}</p>
   *
   * @param authorization the String provided by an HTTP Authorization header <p> The expected input
   * should follow the rule Bearer accessTokenHere </p>
   * @param intermediateProvider the intermediateProvider used to search
   * @param nextPage the nextPage number or -1
   * @return {@link ResponseListWrapper}
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized.</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.DATASETS_INTERMEDIATE_PROVIDER, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseListWrapper<Dataset> getAllDatasetsByIntermediateProvider(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("intermediateProvider") String intermediateProvider,
      @RequestParam(value = "nextPage", required = false, defaultValue = "0") int nextPage)
      throws GenericMetisException {
    if (nextPage < 0) {
      throw new BadContentException(CommonStringValues.NEXT_PAGE_CANNOT_BE_NEGATIVE);
    }

    MetisUserView metisUserView = authenticationClient.getUserByAccessTokenInHeader(authorization);

    ResponseListWrapper<Dataset> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper
        .setResultsAndLastPage(
            datasetService
                .getAllDatasetsByIntermediateProvider(metisUserView, intermediateProvider, nextPage),
            datasetService.getDatasetsPerRequestLimit(), nextPage);
    LOGGER.info(CommonStringValues.BATCH_OF_DATASETS_RETURNED,
        responseListWrapper.getListSize(), nextPage);
    return responseListWrapper;
  }

  /**
   * Get a list of all the datasets using the dataProvider field for lookup.
   * <p>The results are paged and wrapped around {@link ResponseListWrapper}</p>
   *
   * @param authorization the String provided by an HTTP Authorization header <p> The expected input
   * should follow the rule Bearer accessTokenHere </p>
   * @param dataProvider the dataProvider used to search
   * @param nextPage the nextPage number or -1
   * @return {@link ResponseListWrapper}
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized.</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.DATASETS_DATAPROVIDER, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseListWrapper<Dataset> getAllDatasetsByDataProvider(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("dataProvider") String dataProvider,
      @RequestParam(value = "nextPage", required = false, defaultValue = "0") int nextPage)
      throws GenericMetisException {
    if (nextPage < 0) {
      throw new BadContentException(CommonStringValues.NEXT_PAGE_CANNOT_BE_NEGATIVE);
    }

    MetisUserView metisUserView = authenticationClient.getUserByAccessTokenInHeader(authorization);

    ResponseListWrapper<Dataset> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper
        .setResultsAndLastPage(
            datasetService.getAllDatasetsByDataProvider(metisUserView, dataProvider, nextPage),
            datasetService.getDatasetsPerRequestLimit(), nextPage);
    LOGGER.info(CommonStringValues.BATCH_OF_DATASETS_RETURNED,
        responseListWrapper.getListSize(), nextPage);
    return responseListWrapper;
  }

  /**
   * Get a list of all the datasets using the organizationId field for lookup.
   * <p>The results are paged and wrapped around {@link ResponseListWrapper}</p>
   *
   * @param authorization the String provided by an HTTP Authorization header <p> The expected input
   * should follow the rule Bearer accessTokenHere </p>
   * @param organizationId the organizationId used to search
   * @param nextPage the nextPage number or -1
   * @return {@link ResponseListWrapper}
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized.</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.DATASETS_ORGANIZATION_ID, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseListWrapper<Dataset> getAllDatasetsByOrganizationId(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("organizationId") String organizationId,
      @RequestParam(value = "nextPage", required = false, defaultValue = "0") int nextPage)
      throws GenericMetisException {
    if (nextPage < 0) {
      throw new BadContentException(CommonStringValues.NEXT_PAGE_CANNOT_BE_NEGATIVE);
    }

    MetisUserView metisUserView = authenticationClient.getUserByAccessTokenInHeader(authorization);

    ResponseListWrapper<Dataset> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper
        .setResultsAndLastPage(
            datasetService.getAllDatasetsByOrganizationId(metisUserView, organizationId, nextPage),
            datasetService.getDatasetsPerRequestLimit(), nextPage);
    LOGGER.info(CommonStringValues.BATCH_OF_DATASETS_RETURNED,
        responseListWrapper.getListSize(), nextPage);
    return responseListWrapper;
  }

  /**
   * Get a list of all the datasets using the organizationName field for lookup.
   * <p>The results are paged and wrapped around {@link ResponseListWrapper}</p>
   *
   * @param authorization the String provided by an HTTP Authorization header <p> The expected input
   * should follow the rule Bearer accessTokenHere </p>
   * @param organizationName the organizationName used to search
   * @param nextPage the nextPage number or -1
   * @return {@link ResponseListWrapper}
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized.</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.DATASETS_ORGANIZATION_NAME, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseListWrapper<Dataset> getAllDatasetsByOrganizationName(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("organizationName") String organizationName,
      @RequestParam(value = "nextPage", required = false, defaultValue = "0") int nextPage)
      throws GenericMetisException {
    if (nextPage < 0) {
      throw new BadContentException(CommonStringValues.NEXT_PAGE_CANNOT_BE_NEGATIVE);
    }

    MetisUserView metisUserView = authenticationClient.getUserByAccessTokenInHeader(authorization);

    ResponseListWrapper<Dataset> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper
        .setResultsAndLastPage(
            datasetService.getAllDatasetsByOrganizationName(metisUserView, organizationName, nextPage),
            datasetService.getDatasetsPerRequestLimit(), nextPage);
    LOGGER.info(CommonStringValues.BATCH_OF_DATASETS_RETURNED,
        responseListWrapper.getListSize(), nextPage);
    return responseListWrapper;
  }

  /**
   * Get all available countries that can be used.
   * <p>The list is retrieved based on an internal enum</p>
   *
   * @param authorization the String provided by an HTTP Authorization header <p> The expected input
   * should follow the rule Bearer accessTokenHere </p>
   * @return The list of countries that are serialized based on {@link eu.europeana.metis.core.common.CountrySerializer}
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized.</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.DATASETS_COUNTRIES, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<CountryView> getDatasetsCountries(
      @RequestHeader("Authorization") String authorization) throws GenericMetisException {
    authenticationClient.getUserByAccessTokenInHeader(authorization);
    return Arrays.stream(Country.values()).map(CountryView::new).collect(Collectors.toList());
  }

  /**
   * Get all available languages that can be used.
   * <p>The list is retrieved based on an internal enum</p>
   *
   * @param authorization the String provided by an HTTP Authorization header
   * <p> The expected input should follow the rule Bearer accessTokenHere </p>
   * @return The list of countries that are serialized based on {@link eu.europeana.metis.core.common.LanguageSerializer}
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized.</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.DATASETS_LANGUAGES, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<LanguageView> getDatasetsLanguages(
      @RequestHeader("Authorization") String authorization) throws GenericMetisException {
    authenticationClient.getUserByAccessTokenInHeader(authorization);
    return Language.getLanguageListSortedByName().stream().map(LanguageView::new)
        .collect(Collectors.toList());
  }

  /**
   * Get the list of of matching DatasetSearch using dataset
   *
   * @param authorization the String provided by an HTTP Authorization header
   * <p> The expected input should follow the rule Bearer accessTokenHere </p>
   * @param searchString a string that may contain multiple words separated by spaces.
   * <p>The search will be performed on the fields datasetId, datasetName, provider, dataProvider.
   * The words that start with a numeric character will be considered as part of the datasetId
   * search and that field is searched as a "starts with" operation. All words that from a certain
   * length threshold and above e.g. 3 will be used, as AND operations, for searching the fields
   * datasetName, provider, dataProvider</p>
   * @param nextPage the nextPage number, must be positive
   * @return a list with the dataset search view results
   * @throws GenericMetisException which can be one of:
   * <ul>
   *   <li>{@link BadContentException} if the parameters provided are invalid.</li>
   *   <li>{@link UserUnauthorizedException} if the user is unauthorized.</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.DATASETS_SEARCH, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseListWrapper<DatasetSearchView> getDatasetSearch(
      @RequestHeader("Authorization") String authorization,
      @RequestParam(value = "searchString") String searchString,
      @RequestParam(value = "nextPage", required = false, defaultValue = "0") int nextPage)
      throws GenericMetisException {
    if (nextPage < 0) {
      throw new BadContentException(CommonStringValues.NEXT_PAGE_CANNOT_BE_NEGATIVE);
    }

    final MetisUserView metisUserView = authenticationClient
        .getUserByAccessTokenInHeader(authorization);
    ResponseListWrapper<DatasetSearchView> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper.setResultsAndLastPage(
        datasetService.searchDatasetsBasedOnSearchString(metisUserView, searchString, nextPage),
        datasetService.getDatasetsPerRequestLimit(), nextPage);
    LOGGER.info(CommonStringValues.BATCH_OF_DATASETS_RETURNED, responseListWrapper.getListSize(),
        nextPage);
    return responseListWrapper;
  }

  private static class CountryView {

    @JsonProperty("enum")
    private final String enumName;
    @JsonProperty
    private final String name;
    @JsonProperty
    private final String isoCode;

    CountryView(Country country) {
      this.enumName = country.name();
      this.name = country.getName();
      this.isoCode = country.getIsoCode();
    }
  }

  private static class LanguageView {

    @JsonProperty("enum")
    private final String enumName;
    @JsonProperty
    private final String name;

    LanguageView(Language language) {
      this.enumName = language.name();
      this.name = language.getName();
    }
  }
}
