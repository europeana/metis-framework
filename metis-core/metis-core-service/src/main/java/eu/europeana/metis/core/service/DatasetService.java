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
import eu.europeana.metis.core.dao.OrganizationDao;
import eu.europeana.metis.core.dao.ScheduledUserWorkflowDao;
import eu.europeana.metis.core.dao.UserWorkflowExecutionDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.DatasetAlreadyExistsException;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoOrganizationFoundException;
import eu.europeana.metis.core.organization.Organization;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for storing datasets Created by ymamakis on 2/17/16.
 */
@Service
public class DatasetService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatasetService.class);

  private final DatasetDao datasetDao;
  //  private final EcloudDatasetDao ecloudDatasetDao;
  private final OrganizationDao organizationDao;
  private final UserWorkflowExecutionDao userWorkflowExecutionDao;
  private final ScheduledUserWorkflowDao scheduledUserWorkflowDao;

  @Autowired
  public DatasetService(DatasetDao datasetDao,
      OrganizationDao organizationDao,
      UserWorkflowExecutionDao userWorkflowExecutionDao,
      ScheduledUserWorkflowDao scheduledUserWorkflowDao) {
    this.datasetDao = datasetDao;
    this.userWorkflowExecutionDao = userWorkflowExecutionDao;
    this.scheduledUserWorkflowDao = scheduledUserWorkflowDao;
    this.organizationDao = organizationDao;
  }

  public void createDataset(Dataset dataset, String organizationId) {

//    final String uuid = UUID.randomUUID().toString();
//    dataset.setEcloudDatasetId(uuid);

    //Create in ECloud
//    DataSet ecloudDataset = new DataSet();
//    ecloudDataset.setId(uuid);
//    ecloudDataset.setProviderId(ecloudDatasetDao.getEcloudProvider());
//    ecloudDataset.setDescription(dataset.getDescription());
//    ecloudDatasetDao.create(ecloudDataset);

    datasetDao.create(dataset);
    organizationDao
        .updateOrganizationDatasetNamesList(organizationId, dataset.getDatasetName());
  }

  public void createDatasetForOrganization(Dataset dataset, String organizationId)
      throws DatasetAlreadyExistsException, BadContentException, NoOrganizationFoundException {
    checkRestrictionsOnCreate(dataset, organizationId);
    dataset.setOrganizationId(organizationId);
    dataset.setCreatedDate(new Date());
    //Add fake ecloudDatasetId to avoid null errors in the database
    dataset.setEcloudDatasetId(String.format("NOT_CREATED_YET-%s", UUID.randomUUID().toString()));
    createDataset(dataset, organizationId);
  }

  public void updateDataset(Dataset dataset) {
    //Update in ECloud
//    DataSet ecloudDataset = new DataSet();
//    ecloudDataset.setId(dataset.getEcloudDatasetId());
//    ecloudDataset.setProviderId(ecloudDatasetDao.getEcloudProvider());
//    ecloudDataset.setDescription(dataset.getDescription());
//    ecloudDatasetDao.update(ecloudDataset);

    datasetDao.update(dataset);
  }

  public void updateDatasetByDatasetName(Dataset dataset, String datasetName)
      throws BadContentException, NoDatasetFoundException {
    if (userWorkflowExecutionDao.existsAndNotCompleted(datasetName) != null) {
      throw new BadContentException(
          String.format("User workflow execution is active for datasteName %s", datasetName));
    }
    checkRestrictionsOnUpdate(dataset, datasetName);
    dataset.setDatasetName(datasetName);
    dataset.setUpdatedDate(new Date());
    updateDataset(dataset);
  }

  public void updateDatasetName(String datasetName, String newDatasetName)
      throws NoDatasetFoundException, BadContentException {
    if (userWorkflowExecutionDao.existsAndNotCompleted(datasetName) != null) {
      throw new BadContentException(
          String.format("User workflow execution is active for datasteName %s", datasetName));
    }
    Dataset dataset = getDatasetByDatasetName(datasetName);
    datasetDao.updateDatasetName(datasetName, newDatasetName);
    organizationDao.removeOrganizationDatasetNameFromList(dataset.getOrganizationId(), datasetName);
    organizationDao.updateOrganizationDatasetNamesList(dataset.getOrganizationId(), newDatasetName);
    scheduledUserWorkflowDao.updateAllDatasetNames(datasetName, newDatasetName);
    userWorkflowExecutionDao.updateAllDatasetNames(datasetName, newDatasetName);
  }

  public void deleteDatasetByDatasetName(String datasetName)
      throws NoDatasetFoundException, BadContentException {
    Dataset dataset = getDatasetByDatasetName(datasetName);
    if (userWorkflowExecutionDao.existsAndNotCompleted(datasetName) != null) {
      throw new BadContentException(
          String.format("User workflow execution is active for datasteName %s", datasetName));
    }
    datasetDao.deleteDatasetByDatasetName(datasetName);
    Organization organization = organizationDao
        .getOrganizationByOrganizationId(dataset.getOrganizationId());
    if (organization == null) {
      LOGGER.warn(String.format(
          "Did not find organization with OrganizationId '%s' stated in dataset '%s' to be deleted",
          dataset.getOrganizationId(), datasetName));
    }
    organizationDao.removeOrganizationDatasetNameFromList(dataset.getOrganizationId(),
        dataset.getDatasetName());

    //Clean up dataset leftovers
    userWorkflowExecutionDao.deleteAllByDatasetName(datasetName);
    scheduledUserWorkflowDao.deleteAllByDatasetName(datasetName);

    //Delete from ECloud
//    DataSet ecloudDataset = new DataSet();
//    ecloudDataset.setId(dataset.getEcloudDatasetId());
//    ecloudDataset.setProviderId(ecloudDatasetDao.getEcloudProvider());
//    ecloudDatasetDao.delete(ecloudDataset);
  }

  public Dataset getDatasetByDatasetName(String datasetName) throws NoDatasetFoundException {
    Dataset dataset = datasetDao.getDatasetByDatasetName(datasetName);
    if (dataset == null) {
      throw new NoDatasetFoundException(
          String.format("No dataset found with datasetName: '%s' in METIS", datasetName));
    }
    return dataset;
  }

  public List<Dataset> getAllDatasetsByDataProvider(String dataProvider, String nextPage) {
    return datasetDao.getAllDatasetsByDataProvider(dataProvider, nextPage);
  }

  public List<Dataset> getAllDatasetsByOrganizationId(String organizationId, String nextPage) {
    return datasetDao.getAllDatasetsByOrganizationId(organizationId, nextPage);
  }

  public boolean existsDatasetByDatasetName(String datasetName) {
    return datasetDao.existsDatasetByDatasetName(datasetName);
  }

  public int getDatasetsPerRequestLimit() {
    return datasetDao.getDatasetsPerRequest();
  }


  private void checkRestrictionsOnCreate(Dataset dataset, String organizationId)
      throws BadContentException, DatasetAlreadyExistsException, NoOrganizationFoundException {
    if (StringUtils.isEmpty(dataset.getDatasetName())) {
      throw new BadContentException("Dataset field 'datasetName' cannot be empty");
    }
    if (dataset.getCreatedDate() != null || dataset.getUpdatedDate() != null
        || dataset.getFirstPublished() != null || dataset.getLastPublished() != null
        || dataset.getHarvestedAt() != null || dataset.getSubmissionDate() != null) {
      throw new BadContentException(
          "Dataset fields 'createdDate', 'updatedDate', 'firstPublished', 'lastPublished', 'harvestedAt', 'submittedAt' should be empty");
    }
    if (dataset.getSubmittedRecords() != 0 || dataset.getPublishedRecords() != 0) {
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
    Organization organization = organizationDao.getOrganizationByOrganizationId(organizationId);
    if (organization == null) {
      throw new NoOrganizationFoundException(
          "No organization found with organization id: " + organizationId + " in METIS");
    }
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
    if (dataset.getCreatedDate() != null || dataset.getUpdatedDate() != null) {
      throw new BadContentException(
          "Dataset fields 'createdDate', 'updatedDate' should be empty");
    }

    Dataset storedDataset = getDatasetByDatasetName(datasetName);
    dataset.setEcloudDatasetId(storedDataset.getEcloudDatasetId());
  }
}
