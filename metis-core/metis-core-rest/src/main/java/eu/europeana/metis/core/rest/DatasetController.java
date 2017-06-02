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

import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.core.api.MetisKey;
import eu.europeana.metis.core.api.Options;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.exceptions.ApiKeyNotAuthorizedException;
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.DatasetAlreadyExistsException;
import eu.europeana.metis.core.exceptions.NoApiKeyFoundException;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoOrganizationFoundException;
import eu.europeana.metis.core.service.DatasetService;
import eu.europeana.metis.core.service.MetisAuthorizationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@Api("/")
public class DatasetController {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatasetController.class);

  private final DatasetService datasetService;
  private final MetisAuthorizationService authorizationService;

  @Autowired
  public DatasetController(DatasetService datasetService,
      MetisAuthorizationService authorizationService) {
    this.datasetService = datasetService;
    this.authorizationService = authorizationService;
  }

  @RequestMapping(value = RestEndpoints.DATASETS, method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.CREATED)
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Successful response"),
      @ApiResponse(code = 401, message = "Api Key not authorized"),
      @ApiResponse(code = 406, message = "Bad content"),
      @ApiResponse(code = 409, message = "Dataset already exists")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "apikey", value = "ApiKey", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "organizationId", value = "organizationId for which the dataset will belong", dataType = "string", paramType = "query", required = true)
  })
  @ApiOperation(value = "Create a dataset for an organization Id")
  public void createDatasetForOrganization(@RequestBody Dataset dataset,
      @QueryParam("organizationId"
      ) String organizationId, @QueryParam("apikey") String apikey)
      throws BadContentException, DatasetAlreadyExistsException, NoOrganizationFoundException, ApiKeyNotAuthorizedException, NoApiKeyFoundException {
    MetisKey key = authorizationService.getKeyFromId(apikey);
    if (key != null) {
      if (key.getOptions().equals(Options.WRITE)) {
        datasetService.createDatasetForOrganization(dataset, organizationId);
        LOGGER.info("Dataset with name " + dataset.getDatasetName() + " for organizationId "
            + organizationId + " created");
      } else {
        throw new ApiKeyNotAuthorizedException(apikey);
      }
    } else {
      throw new NoApiKeyFoundException(apikey);
    }
  }

  @RequestMapping(value = RestEndpoints.DATASETS_DATASETNAME, method = RequestMethod.PUT, consumes = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Successful response"),
      @ApiResponse(code = 404, message = "Dataset not found"),
      @ApiResponse(code = 406, message = "Bad content")
  })
  @ApiImplicitParams({
      @ApiImplicitParam(name = "apikey", value = "ApiKey", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "datasetName", value = "datasetName", dataType = "string", paramType = "path", required = true)
  })
  @ApiOperation(value = "Update a dataset by dataset name")
  public void updateDataset(@RequestBody Dataset dataset,
      @PathVariable("datasetName"
      ) String datasetName, @QueryParam("apikey") String apikey)
      throws ApiKeyNotAuthorizedException, NoApiKeyFoundException, BadContentException, NoDatasetFoundException {
    MetisKey key = authorizationService.getKeyFromId(apikey);
    if (key != null) {
      if (key.getOptions().equals(Options.WRITE)) {
        datasetService.updateDatasetByDatasetName(dataset, datasetName);
        LOGGER.info("Dataset with datasetName " + datasetName + " updated");
      } else {
        throw new ApiKeyNotAuthorizedException(apikey);
      }
    } else {
      throw new NoApiKeyFoundException(apikey);
    }
  }

  @RequestMapping(value = RestEndpoints.DATASETS_DATASETNAME_UPDATENAME, method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Successful response"),
      @ApiResponse(code = 404, message = "Dataset not found"),
      @ApiResponse(code = 406, message = "Bad content")
  })
  @ApiImplicitParams({
      @ApiImplicitParam(name = "apikey", value = "ApiKey", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "datasetName", value = "datasetName", dataType = "string", paramType = "path", required = true),
      @ApiImplicitParam(name = "newDatasetName", value = "newDatasetName", dataType = "string", paramType = "query", required = true)
  })
  @ApiOperation(value = "Rename datasetName to newDatasetName")
  public void updateDatasetName(@PathVariable("datasetName"
  ) String datasetName, @QueryParam("newDatasetName") String newDatasetName,
      @QueryParam("apikey") String apikey)
      throws ApiKeyNotAuthorizedException, NoApiKeyFoundException, NoDatasetFoundException {
    MetisKey key = authorizationService.getKeyFromId(apikey);
    if (key != null) {
      if (key.getOptions().equals(Options.WRITE)) {
        datasetService.updateDatasetName(datasetName, newDatasetName);
        LOGGER
            .info(
                "Dataset with datasetName '" + datasetName + "' updated name to '" + newDatasetName
                    + "'");
      } else {
        throw new ApiKeyNotAuthorizedException(apikey);
      }
    } else {
      throw new NoApiKeyFoundException(apikey);
    }
  }

  @RequestMapping(value = RestEndpoints.DATASETS_DATASETNAME, method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Successful response"),
      @ApiResponse(code = 401, message = "Api Key not authorized")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "apikey", value = "ApiKey", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "datasetName", value = "datasetName", dataType = "string", paramType = "path", required = true)
  })
  @ApiOperation(value = "Delete a dataset by dataset name")
  public void deleteDataset(@PathVariable("datasetName"
  ) String datasetName, @QueryParam("apikey") String apikey)
      throws ApiKeyNotAuthorizedException, NoApiKeyFoundException, NoDatasetFoundException {
    MetisKey key = authorizationService.getKeyFromId(apikey);
    if (key != null) {
      if (key.getOptions().equals(Options.WRITE)) {
        datasetService.deleteDatasetByDatasetName(datasetName);
        LOGGER.info("Dataset with datasetName '" + datasetName + "' deleted");
      } else {
        throw new ApiKeyNotAuthorizedException(apikey);
      }
    } else {
      throw new NoApiKeyFoundException(apikey);
    }
  }

  @RequestMapping(value = RestEndpoints.DATASETS_DATASETNAME, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful response"),
      @ApiResponse(code = 401, message = "Api Key not authorized"),
      @ApiResponse(code = 404, message = "Dataset not found")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "apikey", value = "ApiKey", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "datasetName", value = "datasetName", dataType = "string", paramType = "path", required = true)
  })
  @ApiOperation(value = "Get a dataset by datasetName", response = Dataset.class)
  public Dataset getByDatasetName(@PathVariable("datasetName") String datasetName,
      @QueryParam("apikey") String apikey)
      throws NoDatasetFoundException, NoApiKeyFoundException, ApiKeyNotAuthorizedException {
    MetisKey key = authorizationService.getKeyFromId(apikey);
    if (key != null && (key.getOptions().equals(Options.WRITE) || key.getOptions()
        .equals(Options.READ))) {
      Dataset dataset = datasetService.getDatasetByDatasetName(datasetName);
      LOGGER.info("Dataset with datasetName '" + datasetName + "' found");
      return dataset;
    } else if (key == null) {
      throw new NoApiKeyFoundException(apikey);
    } else {
      throw new ApiKeyNotAuthorizedException(apikey);
    }
  }

  @RequestMapping(value = RestEndpoints.DATASETS_DATAPROVIDER, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful response"),
      @ApiResponse(code = 401, message = "Api Key not authorized")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "apikey", value = "ApiKey", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "dataProvider", value = "dataProvider", dataType = "string", paramType = "path", required = true),
      @ApiImplicitParam(name = "nextPage", value = "nextPage", dataType = "string", paramType = "query")
  })
  @ApiOperation(value = "Get all datasets by dataProvider", response = ResponseListWrapper.class)
  public ResponseListWrapper<Dataset> getAllDatasetsByDataProvider(@PathVariable("dataProvider") String dataProvider,
      @QueryParam("nextPage"
      ) String nextPage, @QueryParam("apikey") String apikey)
      throws NoApiKeyFoundException, ApiKeyNotAuthorizedException {
    MetisKey key = authorizationService.getKeyFromId(apikey);
    if (key != null && (key.getOptions().equals(Options.WRITE) || key.getOptions()
        .equals(Options.READ))) {
      ResponseListWrapper<Dataset> responseListWrapper = new ResponseListWrapper<>();
      responseListWrapper
          .setResultsAndLastPage(datasetService.getAllDatasetsByDataProvider(dataProvider, nextPage),
              datasetService.getDatasetsPerRequestLimit());
      LOGGER.info("Batch of: " + responseListWrapper.getListSize()
          + " datasets returned, using batch nextPage: " + nextPage);
      return responseListWrapper;
    } else if (key == null) {
      throw new NoApiKeyFoundException(apikey);
    } else {
      throw new ApiKeyNotAuthorizedException(apikey);
    }
  }
}
