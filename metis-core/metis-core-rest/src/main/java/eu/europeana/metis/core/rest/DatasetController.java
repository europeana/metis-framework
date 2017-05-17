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

import static eu.europeana.metis.RestEndpoints.DATASETS;
import static eu.europeana.metis.RestEndpoints.DATASETS_DATASETNAME;
import static eu.europeana.metis.RestEndpoints.DATASET_BYPROVIDER;

import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.core.api.MetisKey;
import eu.europeana.metis.core.api.Options;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetListWrapper;
import eu.europeana.metis.core.dto.OrgDatasetDTO;
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
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The dataset controller
 * Created by ymamakis on 2/18/16.
 */
@Controller
@Api("/")
public class DatasetController {

  private final Logger LOGGER = LoggerFactory.getLogger(DatasetController.class);

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

  /**
   * Delete a dataset
   */
  @RequestMapping(value = DATASETS, method = RequestMethod.DELETE, consumes = "application/json")
  @ResponseBody
  @ApiOperation(value = "Delete a dataset from an organization")
  public ResponseEntity<Void> deleteDataset(@ApiParam @RequestBody OrgDatasetDTO dto) {
    datasetService.deleteDataset(dto.getOrganization(), dto.getDataset());
    return ResponseEntity.noContent().build();
  }

  /**
   * Get a dataset by name
   *
   * @param name The name of the dataset
   * @return The Dataset
   */
  @RequestMapping(value = DATASETS_DATASETNAME, method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  @ApiOperation(value = "Retrieve a dataset by name", response = Dataset.class)
  public Dataset getByName(@ApiParam("name") @PathVariable("name") String name)
      throws NoDatasetFoundException {
    return datasetService.getDatasetByName(name);
  }

  @RequestMapping(value = DATASET_BYPROVIDER, method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  @ApiOperation(value = "Retrieve datasets by data providers", response = DatasetListWrapper.class)
  public DatasetListWrapper getByDataProviderId(
      @PathVariable("dataProviderId") String dataProviderId) {
    DatasetListWrapper lst = new DatasetListWrapper();
    lst.setDatasets(datasetService.getDatasetsByDataProviderId(dataProviderId));
    return lst;
  }
}
