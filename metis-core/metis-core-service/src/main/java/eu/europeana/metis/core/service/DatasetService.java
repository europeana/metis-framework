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
import eu.europeana.metis.core.dataset.DatasetStatus;
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.DatasetAlreadyExistsException;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatasetService {

  private final DatasetDao datasetDao;
  private final WorkflowExecutionDao workflowExecutionDao;
  private final ScheduledWorkflowDao scheduledWorkflowDao;
  private final RedissonClient redissonClient;

  @Autowired
  public DatasetService(DatasetDao datasetDao,
      WorkflowExecutionDao workflowExecutionDao,
      ScheduledWorkflowDao scheduledWorkflowDao, RedissonClient redissonClient) {
    this.datasetDao = datasetDao;
    this.workflowExecutionDao = workflowExecutionDao;
    this.scheduledWorkflowDao = scheduledWorkflowDao;
    this.redissonClient = redissonClient;
  }

  public Dataset createDataset(Dataset dataset) throws DatasetAlreadyExistsException {
    //Lock required for findin the next empty datasetId
    final String executionCheckLock = "datasetCreationLock";
    RLock lock = redissonClient.getFairLock(executionCheckLock);
    lock.lock();

    Dataset datasetObjectId;
    try {
      Dataset storedDataset = datasetDao
          .getDatasetByOrganizationIdAndDatasetName(dataset.getOrganizationId(),
              dataset.getDatasetName());
      if (storedDataset != null) {
        lock.unlock();
        throw new DatasetAlreadyExistsException(String
            .format("Dataset with organizationId: %s and datasetName: %s already exists..",
                dataset.getOrganizationId(), dataset.getDatasetName()));
      }
      dataset.setFirstPublishedDate(null);
      dataset.setLastPublishedDate(null);
      dataset.setPublishedRecords(0);
      dataset.setHarvestedDate(null);
      dataset.setHarvestedRecords(0);
      dataset.setId(null);
      dataset.setUpdatedDate(null);

      dataset.setCreatedDate(new Date());
      dataset.setDatasetStatus(DatasetStatus.CREATED);
      //Add fake ecloudDatasetId to avoid null errors in the database
      dataset.setEcloudDatasetId(String.format("NOT_CREATED_YET-%s", UUID.randomUUID().toString()));

      int nextInSequenceDatasetId = datasetDao.findNextInSequenceDatasetId();
      dataset.setDatasetId(nextInSequenceDatasetId);
      datasetObjectId = datasetDao.getById(datasetDao.create(dataset));
    }
    finally {
      lock.unlock();
    }
    return datasetObjectId;
  }

  public void updateDataset(Dataset dataset) throws NoDatasetFoundException, BadContentException {
    if (workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId()) != null) {
      throw new BadContentException(
          String.format("Workflow execution is active for datasteId %s", dataset.getDatasetId()));
    }
    Dataset storedDataset = datasetDao.getDatasetByDatasetId(dataset.getDatasetId());
    if (storedDataset == null)
      throw new NoDatasetFoundException(String.format("Dataset with datasetId: %s does NOT exist", dataset.getDatasetId()));
    dataset.setEcloudDatasetId(storedDataset.getEcloudDatasetId());
    dataset.setCreatedDate(storedDataset.getCreatedDate());
    dataset.setOrganizationId(storedDataset.getOrganizationId());
    dataset.setOrganizationName(storedDataset.getOrganizationName());
    dataset.setFirstPublishedDate(storedDataset.getFirstPublishedDate());
    dataset.setLastPublishedDate(storedDataset.getLastPublishedDate());
    dataset.setPublishedRecords(storedDataset.getPublishedRecords());
    dataset.setHarvestedDate(storedDataset.getHarvestedDate());
    dataset.setHarvestedRecords(storedDataset.getHarvestedRecords());
    dataset.setDatasetStatus(storedDataset.getDatasetStatus());
    dataset.setCreatedByUserId(storedDataset.getCreatedByUserId());
    dataset.setId(storedDataset.getId());

    dataset.setUpdatedDate(new Date());
    datasetDao.update(dataset);
  }

  public void deleteDatasetByDatasetId(int datasetId) throws BadContentException {
    if (workflowExecutionDao.existsAndNotCompleted(datasetId) != null) {
      throw new BadContentException(
          String.format("Workflow execution is active for datasteId %s", datasetId));
    }
    datasetDao.deleteByDatasetId(datasetId);

    //Clean up dataset leftovers
    workflowExecutionDao.deleteAllByDatasetId(datasetId);
    scheduledWorkflowDao.deleteAllByDatasetId(datasetId);
  }

  public Dataset getDatasetByDatasetName(String datasetName) throws NoDatasetFoundException {
    Dataset dataset = datasetDao.getDatasetByDatasetName(datasetName);
    if (dataset == null) {
      throw new NoDatasetFoundException(
          String.format("No dataset found with datasetName: '%s' in METIS", datasetName));
    }
    return dataset;
  }

  public Dataset getDatasetByDatasetId(int datasetId) throws NoDatasetFoundException {
    Dataset dataset = datasetDao.getDatasetByDatasetId(datasetId);
    if (dataset == null) {
      throw new NoDatasetFoundException(
          String.format("No dataset found with datasetId: '%s' in METIS", datasetId));
    }
    return dataset;
  }

  public List<Dataset> getAllDatasetsByProvider(String provider, String nextPage) {
    return datasetDao.getAllDatasetsByProvider(provider, nextPage);
  }

  public List<Dataset> getAllDatasetsByIntermidiateProvider(String intermediateProvider,
      String nextPage) {
    return datasetDao.getAllDatasetsByIntermidiateProvider(intermediateProvider, nextPage);
  }

  public List<Dataset> getAllDatasetsByDataProvider(String dataProvider, String nextPage) {
    return datasetDao.getAllDatasetsByDataProvider(dataProvider, nextPage);
  }

  public List<Dataset> getAllDatasetsByOrganizationId(String organizationId, String nextPage) {
    return datasetDao.getAllDatasetsByOrganizationId(organizationId, nextPage);
  }

  public List<Dataset> getAllDatasetsByOrganizationName(String organizationName, String nextPage) {
    return datasetDao.getAllDatasetsByOrganizationName(organizationName, nextPage);
  }

  public boolean existsDatasetByDatasetName(String datasetName) {
    return datasetDao.existsDatasetByDatasetName(datasetName);
  }

  public int getDatasetsPerRequestLimit() {
    return datasetDao.getDatasetsPerRequest();
  }
}
