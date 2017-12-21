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
import eu.europeana.metis.core.dao.ScheduledWorkflowDao;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.DatasetAlreadyExistsException;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
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
  private final WorkflowExecutionDao workflowExecutionDao;
  private final ScheduledWorkflowDao scheduledWorkflowDao;

  @Autowired
  public DatasetService(DatasetDao datasetDao,
      WorkflowExecutionDao workflowExecutionDao,
      ScheduledWorkflowDao scheduledWorkflowDao) {
    this.datasetDao = datasetDao;
    this.workflowExecutionDao = workflowExecutionDao;
    this.scheduledWorkflowDao = scheduledWorkflowDao;
  }

  public void createDataset(Dataset dataset) {
    datasetDao.create(dataset);
  }

  public void createDatasetForOrganization(Dataset dataset, String organizationId)
      throws DatasetAlreadyExistsException, BadContentException {
    checkRestrictionsOnCreate(dataset, organizationId);
    dataset.setOrganizationId(organizationId);
    dataset.setCreatedDate(new Date());
    //Add fake ecloudDatasetId to avoid null errors in the database
    dataset.setEcloudDatasetId(String.format("NOT_CREATED_YET-%s", UUID.randomUUID().toString()));
    createDataset(dataset);
  }

  public void updateDataset(Dataset dataset) {
    datasetDao.update(dataset);
  }

  public void updateDatasetByDatasetName(Dataset dataset, String datasetName)
      throws BadContentException, NoDatasetFoundException {
    if (workflowExecutionDao.existsAndNotCompleted(datasetName) != null) {
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
    if (workflowExecutionDao.existsAndNotCompleted(datasetName) != null) {
      throw new BadContentException(
          String.format("User workflow execution is active for datasteName %s", datasetName));
    }
    datasetDao.updateDatasetName(datasetName, newDatasetName);
    scheduledWorkflowDao.updateAllDatasetNames(datasetName, newDatasetName);
    workflowExecutionDao.updateAllDatasetNames(datasetName, newDatasetName);
  }

  public void deleteDatasetByDatasetName(String datasetName)
      throws NoDatasetFoundException, BadContentException {
    if (workflowExecutionDao.existsAndNotCompleted(datasetName) != null) {
      throw new BadContentException(
          String.format("User workflow execution is active for datasteName %s", datasetName));
    }
    datasetDao.deleteDatasetByDatasetName(datasetName);

    //Clean up dataset leftovers
    workflowExecutionDao.deleteAllByDatasetName(datasetName);
    scheduledWorkflowDao.deleteAllByDatasetName(datasetName);
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
      throws BadContentException, DatasetAlreadyExistsException {
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
