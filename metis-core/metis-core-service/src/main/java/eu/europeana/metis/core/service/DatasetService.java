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
package eu.europeana.metis.core.service;

import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.ecloud.EcloudDatasetDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.DatasetAlreadyExistsException;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoOrganizationFoundException;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Service for storing datasets
 * Created by ymamakis on 2/17/16.
 */
@Component
public class DatasetService {

  private final Logger LOGGER = LoggerFactory.getLogger(DatasetService.class);

  private final DatasetDao datasetDao;
  private final EcloudDatasetDao ecloudDatasetDao;
  private final OrganizationService organizationService;

  @Autowired
  public DatasetService(DatasetDao datasetDao, EcloudDatasetDao ecloudDatasetDao,
      OrganizationService organizationService) {
    this.datasetDao = datasetDao;
    this.ecloudDatasetDao = ecloudDatasetDao;
    this.organizationService = organizationService;
  }

  public void createDataset(Dataset dataset, String organizationId) {
    datasetDao.create(dataset);
    organizationService
        .updateOrganizationDatasetNamesList(organizationId, dataset.getDatasetName());

    //Create in ECloud
//    DataSet ecloudDataset = new DataSet();
//    ecloudDataset.setId(dataset.getDatasetName());
//    ecloudDataset.setProviderId(ecloudDatasetDao.getEcloudProvider());
//    ecloudDataset.setDescription(dataset.getDescription());
//    ecloudDatasetDao.create(ecloudDataset);
  }

  public void createDatasetForOrganization(Dataset dataset, String organizationId)
      throws DatasetAlreadyExistsException, BadContentException, NoOrganizationFoundException {
    checkRestrictionsOnCreate(dataset, organizationId);
    dataset.setOrganizationId(organizationId);
    dataset.setCreatedDate(new Date());
    createDataset(dataset, organizationId);
  }

  public void updateDataset(Dataset dataset) {
    datasetDao.update(dataset);

    //Update in ECloud
//    DataSet ecloudDataset = new DataSet();
//    ecloudDataset.setId(ds.getDatasetName());
//    ecloudDataset.setProviderId(ecloudDatasetDao.getEcloudProvider());
//    ecloudDataset.setDescription(ds.getDescription());
//    ecloudDatasetDao.update(ecloudDataset);
  }

  public void updateDatasetByDatasetName(Dataset dataset, String datasetName)
      throws BadContentException, NoDatasetFoundException {
    checkRestrictionsOnUpdate(dataset, datasetName);
    dataset.setDatasetName(datasetName);
    dataset.setUpdatedDate(new Date());
    updateDataset(dataset);
  }

  public void updateDatasetName(String datasetName, String newDatasetName)
      throws NoDatasetFoundException {
    Dataset dataset = getDatasetByName(datasetName);
    datasetDao.updateDatasetName(datasetName, newDatasetName);
    organizationService.removeOrganizationDatasetNameFromList(dataset.getOrganizationId(), datasetName);
    organizationService.updateOrganizationDatasetNamesList(dataset.getOrganizationId(), newDatasetName);
  }

  public void deleteDatasetByDatasetName(String datasetName) throws NoDatasetFoundException {
    Dataset dataset = getDatasetByName(datasetName);
    datasetDao.deleteDatasetByDatasetName(datasetName);
    try {
      organizationService.getOrganizationByOrganizationId(dataset.getOrganizationId());
    } catch (NoOrganizationFoundException e) {
      LOGGER.warn("Did not find organization with OrganizationId '" + dataset.getOrganizationId() + "' stated in dataset '" + datasetName + "' to be deleted");
    }
    organizationService.removeOrganizationDatasetNameFromList(dataset.getOrganizationId(), dataset.getDatasetName());

    //Delete from ECloud
//    DataSet ecloudDataset = new DataSet();
//    ecloudDataset.setId(ds.getDatasetName());
//    ecloudDataset.setProviderId(ecloudDatasetDao.getEcloudProvider());
//    ecloudDataset.setDescription(ds.getDescription());
//    ecloudDatasetDao.delete(ecloudDataset);
  }

  public Dataset getDatasetByName(String name) throws NoDatasetFoundException {
    Dataset dataset = datasetDao.getDatasetByDatasetName(name);
    if (dataset == null) {
      throw new NoDatasetFoundException(
          "No dataset found with datasetName: " + name + " in METIS");
    }
    return dataset;
  }

  public List<Dataset> getAllDatasetsByDataProvider(String dataProvider, String nextPage)
      throws NoDatasetFoundException {
    List<Dataset> datasets = datasetDao.getAllDatasetsByDataProvider(dataProvider, nextPage);
    if ((datasets == null || datasets.size() == 0) && StringUtils.isEmpty(nextPage)) {
      throw new NoDatasetFoundException("No datasets found for dataProvider " + dataProvider);
    }
    return datasets;
  }

  private void checkRestrictionsOnCreate(Dataset dataset, String organizationId)
      throws BadContentException, DatasetAlreadyExistsException, NoOrganizationFoundException {
    if (StringUtils.isEmpty(dataset.getDatasetName())) {
      throw new BadContentException("Dataset field 'datasetName' cannot be empty");
    } else if (dataset.getCreatedDate() != null || dataset.getUpdatedDate() != null
        || dataset.getFirstPublished() != null || dataset.getLastPublished() != null
        || dataset.getHarvestedAt() != null || dataset.getSubmissionDate() != null) {
      throw new BadContentException(
          "Dataset fields 'createdDate', 'updatedDate', 'firstPublished', 'lastPublished', 'harvestedAt', 'submittedAt' should be empty");
    } else if (dataset.getSubmittedRecords() != 0 || dataset.getPublishedRecords() != 0) {
      throw new BadContentException(
          "Dataset fields 'submittedRecords', 'publishedRecords' should be 0");
    }
    if (StringUtils.isNotEmpty(dataset.getOrganizationId()) && !dataset
        .getOrganizationId().equals(organizationId)) {
      throw new BadContentException(
          "OrganinazationId in body " + dataset.getOrganizationId()
              + " is different from parameter " + organizationId);
    }
    //Check if organization exists first
    organizationService.getOrganizationByOrganizationId(organizationId);
    if (existsDatasetByDatasetName(dataset.getDatasetName())) {
      throw new DatasetAlreadyExistsException(dataset.getDatasetName());
    }
    LOGGER.info("Dataset not found, so it can be created");
  }

  private void checkRestrictionsOnUpdate(Dataset dataset, String datasetName)
      throws BadContentException, NoDatasetFoundException {
    if (StringUtils.isNotEmpty(dataset.getDatasetName()) && !dataset
        .getDatasetName().equals(datasetName)) {
      throw new BadContentException(
          "DatasetName in body " + dataset.getDatasetName()
              + " is different from parameter " + datasetName);
    }
    else if (dataset.getCreatedDate() != null || dataset.getUpdatedDate() != null) {
      throw new BadContentException(
          "Dataset fields 'createdDate', 'updatedDate' should be empty");
    }

    if (!existsDatasetByDatasetName(datasetName)) {
      throw new NoDatasetFoundException(datasetName);
    }
  }

  public boolean existsDatasetByDatasetName(String datasetName) {
    return datasetDao.existsDatasetByDatasetName(datasetName);
  }

  public int getDatasetsPerRequestLimit() {
    return datasetDao.getDatasetsPerRequest();
  }
}
