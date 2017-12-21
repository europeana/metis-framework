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
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.DatasetAlreadyExistsException;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.service.DatasetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class DatasetController {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatasetController.class);

  private final DatasetService datasetService;

  @Autowired
  public DatasetController(DatasetService datasetService) {
    this.datasetService = datasetService;
  }

  @RequestMapping(value = RestEndpoints.DATASETS, method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.CREATED)
  public void createDatasetForOrganization(@RequestBody Dataset dataset,
      @RequestParam("organizationId"
      ) String organizationId)
      throws BadContentException, DatasetAlreadyExistsException {

    datasetService.createDatasetForOrganization(dataset, organizationId);
    LOGGER.info("Dataset with name {} for organizationId {} created", dataset.getDatasetName(),
            organizationId);
  }

  @RequestMapping(value = RestEndpoints.DATASETS_DATASETNAME, method = RequestMethod.PUT, consumes = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateDataset(
      @RequestBody Dataset dataset,
      @PathVariable("datasetName") String datasetName)
      throws BadContentException, NoDatasetFoundException {

    datasetService.updateDatasetByDatasetName(dataset, datasetName);
    LOGGER.info("Dataset with datasetName {} updated", datasetName);
  }

  @RequestMapping(value = RestEndpoints.DATASETS_DATASETNAME_UPDATENAME, method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateDatasetName(
      @PathVariable("datasetName") String datasetName,
      @RequestParam("newDatasetName") String newDatasetName)
      throws NoDatasetFoundException, BadContentException {

    datasetService.updateDatasetName(datasetName, newDatasetName);
    LOGGER.info("Dataset with datasetName '{}' updated name to '{}'", datasetName,
            newDatasetName);
  }

  @RequestMapping(value = RestEndpoints.DATASETS_DATASETNAME, method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteDataset(
      @PathVariable("datasetName") String datasetName)
      throws NoDatasetFoundException, BadContentException {

    datasetService.deleteDatasetByDatasetName(datasetName);
    LOGGER.info("Dataset with datasetName '{}' deleted", datasetName);
  }

  @RequestMapping(value = RestEndpoints.DATASETS_DATASETNAME, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Dataset getByDatasetName(
      @PathVariable("datasetName") String datasetName)
      throws NoDatasetFoundException {

    Dataset dataset = datasetService.getDatasetByDatasetName(datasetName);
    LOGGER.info("Dataset with datasetName '{}' found", datasetName);
    return dataset;
  }

  @RequestMapping(value = RestEndpoints.DATASETS_DATAPROVIDER, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseListWrapper<Dataset> getAllDatasetsByDataProvider(
      @PathVariable("dataProvider") String dataProvider,
      @RequestParam(value = "nextPage", required = false) String nextPage) {

    ResponseListWrapper<Dataset> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper
        .setResultsAndLastPage(datasetService.getAllDatasetsByDataProvider(dataProvider, nextPage),
            datasetService.getDatasetsPerRequestLimit());
    LOGGER.info("Batch of: {} datasets returned, using batch nextPage: {}",
        responseListWrapper.getListSize(), nextPage);
    return responseListWrapper;
  }
}
