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

import eu.europeana.cloud.common.model.DataSet;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.ecloud.EcloudDatasetDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.DatasetAlreadyExistsException;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoOrganizationFoundException;
import eu.europeana.metis.core.organization.Organization;
import java.util.Date;
import java.util.List;
import java.util.Set;
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
    organizationService.updateOrganizationDatasetNamesList(organizationId, dataset.getName());

    //Create in ECloud
//        DataSet ecloudDataset = new DataSet();
//        ecloudDataset.setId(dataset.getName());
//        ecloudDataset.setProviderId(ecloudDatasetDao.getEcloudProvider());
//        ecloudDataset.setDescription(dataset.getDescription());
//        ecloudDatasetDao.create(ecloudDataset);
  }

  public void createDatasetForOrganization(Dataset dataset, String organizationId)
      throws DatasetAlreadyExistsException, BadContentException, NoOrganizationFoundException {
    checkRestrictionsOnCreate(dataset, organizationId);
    dataset.setOrganizationId(organizationId);
    dataset.setCreated(new Date());
    createDataset(dataset, organizationId);
  }

  /**
   * Update a dataset
   *
   * @param ds The dataset to update
   */
  public void updateDataset(Dataset ds) {
    datasetDao.update(ds);

    //Update in ECloud
    DataSet ecloudDataset = new DataSet();
    ecloudDataset.setId(ds.getName());
    ecloudDataset.setProviderId(ecloudDatasetDao.getEcloudProvider());
    ecloudDataset.setDescription(ds.getDescription());
    ecloudDatasetDao.update(ecloudDataset);
  }

  /**
   * Delete a dataset
   *
   * @param org The organization the dataset is assigned to
   * @param ds The dataset to delete
   */
  public void deleteDataset(Organization org, Dataset ds) {
    datasetDao.delete(ds);
    Set<String> datasetSet = org.getDatasetNames();
    datasetSet.remove(ds.getName());
    org.setDatasetNames(datasetSet);
//        organizationService.update(org);

    //Delete from ECloud
    DataSet ecloudDataset = new DataSet();
    ecloudDataset.setId(ds.getName());
    ecloudDataset.setProviderId(ecloudDatasetDao.getEcloudProvider());
    ecloudDataset.setDescription(ds.getDescription());
    ecloudDatasetDao.delete(ecloudDataset);
  }

  public Dataset getDatasetByName(String name) throws NoDatasetFoundException {
    Dataset dataset = datasetDao.getDatasetByName(name);
    if (dataset == null) {
      throw new NoDatasetFoundException(
          "No dataset found with name: " + name + " in METIS");
    }
    return dataset;
  }

  /**
   * Retrieve the datasets an organization is a data provider for
   *
   * @param dataProviderId The data provider id
   * @return The list of datasets the provider is a data provider for
   */
  public List<Dataset> getDatasetsByDataProviderId(String dataProviderId) {
    return datasetDao.getByDataProviderId(dataProviderId);
  }

  public void checkRestrictionsOnCreate(Dataset dataset, String organizationId)
      throws BadContentException, DatasetAlreadyExistsException, NoOrganizationFoundException {
    if (StringUtils.isEmpty(dataset.getName())) {
      throw new BadContentException("Dataset field 'name' cannot be empty");
    } else if (dataset.getCreated() != null || dataset.getUpdated() != null
        || dataset.getFirstPublished() != null || dataset.getLastPublished() != null
        || dataset.getHarvestedAt() != null || dataset.getSubmissionDate() != null) {
      throw new BadContentException(
          "Dataset fields 'created', 'updated', 'firstPublished', 'lastPublished', 'harvestedAt', 'submittedAt' should be empty");
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
    if (existsDatasetByDatasetName(dataset.getName())) {
      throw new DatasetAlreadyExistsException(dataset.getName());
    }
    LOGGER.info("Dataset not found, so it can be created");
  }

  public boolean existsDatasetByDatasetName(String datasetName) {
    return datasetDao.existsDatasetByName(datasetName);
  }

  public int getDatasetsPerRequestLimit() {
    return datasetDao.getDatasetsPerRequest();
  }
}
