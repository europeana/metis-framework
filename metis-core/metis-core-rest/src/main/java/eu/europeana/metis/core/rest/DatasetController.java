/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.metis.core.rest;

import eu.europeana.metis.CommonStringValues;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.authentication.rest.client.AuthenticationClient;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.core.common.Language;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.DatasetAlreadyExistsException;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.service.DatasetService;
import eu.europeana.metis.exception.UserUnauthorizedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class DatasetController {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatasetController.class);

  private final DatasetService datasetService;
  private final AuthenticationClient authenticationClient;

  @Autowired
  public DatasetController(DatasetService datasetService,
      AuthenticationClient authenticationClient) {
    this.datasetService = datasetService;
    this.authenticationClient = authenticationClient;
  }

  @RequestMapping(value = RestEndpoints.DATASETS, method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public Dataset createDataset(@RequestHeader("Authorization") String authorization,
      @RequestBody Dataset dataset)
      throws DatasetAlreadyExistsException, BadContentException, UserUnauthorizedException {

    MetisUser metisUser = authenticationClient
        .getUserByAccessTokenInHeader(authorization);
    if (metisUser == null) {
      throw new BadContentException(CommonStringValues.WRONG_ACCESS_TOKEN);
    }

    Dataset createdDataset = datasetService.createDataset(metisUser, dataset);
    LOGGER.info("Dataset with datasetId: {}, datasetName: {} and organizationId {} created",
        createdDataset.getDatasetId(), createdDataset.getDatasetName(),
        createdDataset.getOrganizationId());
    return createdDataset;
  }

  @RequestMapping(value = RestEndpoints.DATASETS, method = RequestMethod.PUT, consumes = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateDataset(@RequestHeader("Authorization") String authorization,
      @RequestBody Dataset dataset)
      throws NoDatasetFoundException, BadContentException, UserUnauthorizedException {

    MetisUser metisUser = authenticationClient
        .getUserByAccessTokenInHeader(authorization);
    if (metisUser == null) {
      throw new BadContentException(CommonStringValues.WRONG_ACCESS_TOKEN);
    }

    datasetService.updateDataset(metisUser, dataset);
    LOGGER.info("Dataset with datasetId {} updated", dataset.getDatasetId());
  }

  @RequestMapping(value = RestEndpoints.DATASETS_DATASETID, method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteDataset(@RequestHeader("Authorization") String authorization,
      @PathVariable("datasetId") int datasetId)
      throws BadContentException, UserUnauthorizedException, NoDatasetFoundException {

    MetisUser metisUser = authenticationClient
        .getUserByAccessTokenInHeader(authorization);
    if (metisUser == null) {
      throw new BadContentException(CommonStringValues.WRONG_ACCESS_TOKEN);
    }

    datasetService.deleteDatasetByDatasetId(metisUser, datasetId);
    LOGGER.info("Dataset with datasetId '{}' deleted", datasetId);
  }

  @RequestMapping(value = RestEndpoints.DATASETS_DATASETID, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Dataset getByDatasetId(@RequestHeader("Authorization") String authorization,
      @PathVariable("datasetId") int datasetId)
      throws NoDatasetFoundException, UserUnauthorizedException, BadContentException {

    MetisUser metisUser = authenticationClient
        .getUserByAccessTokenInHeader(authorization);
    if (metisUser == null) {
      throw new BadContentException(CommonStringValues.WRONG_ACCESS_TOKEN);
    }

    Dataset storedDataset = datasetService.getDatasetByDatasetId(metisUser, datasetId);
    LOGGER.info("Dataset with datasetId '{}' found", datasetId);
    return storedDataset;
  }

  @RequestMapping(value = RestEndpoints.DATASETS_DATASETNAME, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Dataset getByDatasetName(@RequestHeader("Authorization") String authorization,
      @PathVariable("datasetName") String datasetName)
      throws NoDatasetFoundException, BadContentException, UserUnauthorizedException {

    MetisUser metisUser = authenticationClient
        .getUserByAccessTokenInHeader(authorization);
    if (metisUser == null) {
      throw new BadContentException(CommonStringValues.WRONG_ACCESS_TOKEN);
    }

    Dataset dataset = datasetService.getDatasetByDatasetName(metisUser, datasetName);
    LOGGER.info("Dataset with datasetName '{}' found", datasetName);
    return dataset;
  }

  @RequestMapping(value = RestEndpoints.DATASETS_PROVIDER, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseListWrapper<Dataset> getAllDatasetsByProvider(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("provider") String provider,
      @RequestParam(value = "nextPage", required = false) String nextPage)
      throws BadContentException, UserUnauthorizedException {

    MetisUser metisUser = authenticationClient
        .getUserByAccessTokenInHeader(authorization);
    if (metisUser == null) {
      throw new BadContentException(CommonStringValues.WRONG_ACCESS_TOKEN);
    }

    ResponseListWrapper<Dataset> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper
        .setResultsAndLastPage(
            datasetService.getAllDatasetsByProvider(metisUser, provider, nextPage),
            datasetService.getDatasetsPerRequestLimit());
    LOGGER.info(CommonStringValues.BATCH_OF_DATASETS_RETURNED,
        responseListWrapper.getListSize(), nextPage);
    return responseListWrapper;
  }

  @RequestMapping(value = RestEndpoints.DATASETS_INTERMEDIATE_PROVIDER, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseListWrapper<Dataset> getAllDatasetsByIntermediateProvider(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("intermediateProvider") String intermediateProvider,
      @RequestParam(value = "nextPage", required = false) String nextPage)
      throws BadContentException, UserUnauthorizedException {

    MetisUser metisUser = authenticationClient
        .getUserByAccessTokenInHeader(authorization);
    if (metisUser == null) {
      throw new BadContentException(CommonStringValues.WRONG_ACCESS_TOKEN);
    }

    ResponseListWrapper<Dataset> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper
        .setResultsAndLastPage(
            datasetService.getAllDatasetsByIntermediateProvider(metisUser, intermediateProvider, nextPage),
            datasetService.getDatasetsPerRequestLimit());
    LOGGER.info(CommonStringValues.BATCH_OF_DATASETS_RETURNED,
        responseListWrapper.getListSize(), nextPage);
    return responseListWrapper;
  }

  @RequestMapping(value = RestEndpoints.DATASETS_DATAPROVIDER, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseListWrapper<Dataset> getAllDatasetsByDataProvider(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("dataProvider") String dataProvider,
      @RequestParam(value = "nextPage", required = false) String nextPage)
      throws BadContentException, UserUnauthorizedException {

    MetisUser metisUser = authenticationClient
        .getUserByAccessTokenInHeader(authorization);
    if (metisUser == null) {
      throw new BadContentException(CommonStringValues.WRONG_ACCESS_TOKEN);
    }

    ResponseListWrapper<Dataset> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper
        .setResultsAndLastPage(
            datasetService.getAllDatasetsByDataProvider(metisUser, dataProvider, nextPage),
            datasetService.getDatasetsPerRequestLimit());
    LOGGER.info(CommonStringValues.BATCH_OF_DATASETS_RETURNED,
        responseListWrapper.getListSize(), nextPage);
    return responseListWrapper;
  }

  @RequestMapping(value = RestEndpoints.DATASETS_ORGANIZATION_ID, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseListWrapper<Dataset> getAllDatasetsByOrganizationId(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("organizationId") String organizationId,
      @RequestParam(value = "nextPage", required = false) String nextPage)
      throws BadContentException, UserUnauthorizedException {

    MetisUser metisUser = authenticationClient
        .getUserByAccessTokenInHeader(authorization);
    if (metisUser == null) {
      throw new BadContentException(CommonStringValues.WRONG_ACCESS_TOKEN);
    }

    ResponseListWrapper<Dataset> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper
        .setResultsAndLastPage(
            datasetService.getAllDatasetsByOrganizationId(metisUser, organizationId, nextPage),
            datasetService.getDatasetsPerRequestLimit());
    LOGGER.info(CommonStringValues.BATCH_OF_DATASETS_RETURNED,
        responseListWrapper.getListSize(), nextPage);
    return responseListWrapper;
  }

  @RequestMapping(value = RestEndpoints.DATASETS_ORGANIZATION_NAME, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseListWrapper<Dataset> getAllDatasetsByOrganizationName(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("organizationName") String organizationName,
      @RequestParam(value = "nextPage", required = false) String nextPage)
      throws BadContentException, UserUnauthorizedException {

    MetisUser metisUser = authenticationClient
        .getUserByAccessTokenInHeader(authorization);
    if (metisUser == null) {
      throw new BadContentException(CommonStringValues.WRONG_ACCESS_TOKEN);
    }

    ResponseListWrapper<Dataset> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper
        .setResultsAndLastPage(
            datasetService.getAllDatasetsByOrganizationName(metisUser, organizationName, nextPage),
            datasetService.getDatasetsPerRequestLimit());
    LOGGER.info(CommonStringValues.BATCH_OF_DATASETS_RETURNED,
        responseListWrapper.getListSize(), nextPage);
    return responseListWrapper;
  }

  @RequestMapping(value = RestEndpoints.DATASETS_COUNTRIES, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<Country> getDatasetsCountries(@RequestHeader("Authorization") String authorization)
      throws BadContentException {

    MetisUser metisUser = authenticationClient
        .getUserByAccessTokenInHeader(authorization);
    if (metisUser == null) {
      throw new BadContentException(CommonStringValues.WRONG_ACCESS_TOKEN);
    }

    return new ArrayList<>(Arrays.asList(Country.values()));
  }

  @RequestMapping(value = RestEndpoints.DATASETS_LANGUAGES, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<Language> getDatasetsLanguages(@RequestHeader("Authorization") String authorization)
      throws BadContentException {

    MetisUser metisUser = authenticationClient
        .getUserByAccessTokenInHeader(authorization);
    if (metisUser == null) {
      throw new BadContentException(CommonStringValues.WRONG_ACCESS_TOKEN);
    }

    return new ArrayList<>(Arrays.asList(Language.values()));
  }
}
