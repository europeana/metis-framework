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
package eu.europeana.metis.core.dao;

import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetIdSequence;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.FindOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * DAO for the datasets Created by ymamakis on 2/17/16.
 */
@Repository
public class DatasetDao implements MetisDao<Dataset, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatasetDao.class);
  private static final String DATASET_NAME = "datasetName";
  private static final String DATASET_ID = "datasetId";
  private int datasetsPerRequest = 5;

  private final MorphiaDatastoreProvider morphiaDatastoreProvider;

  @Autowired
  public DatasetDao(MorphiaDatastoreProvider morphiaDatastoreProvider) {
    this.morphiaDatastoreProvider = morphiaDatastoreProvider;
  }

  @Override
  public String create(Dataset dataset) {
    Key<Dataset> datasetKey = morphiaDatastoreProvider.getDatastore().save(dataset);
    LOGGER.debug(
        "Dataset with datasetId: '{}', datasetName: '{}' and OrganizationId: '{}' created in Mongo",
        dataset.getDatasetId(), dataset.getDatasetName(), dataset.getOrganizationId());
    return datasetKey.getId().toString();
  }

  @Override
  public String update(Dataset dataset) {
    Key<Dataset> datasetKey = morphiaDatastoreProvider.getDatastore().save(dataset);
    LOGGER.debug(
        "Dataset with datasetId: '{}', datasetName: '{}' and OrganizationId: '{}' updated in Mongo",
        dataset.getDatasetId(), dataset.getDatasetName(), dataset.getOrganizationId());
    return datasetKey.getId().toString();
  }

  @Override
  public Dataset getById(String id) {
    return morphiaDatastoreProvider.getDatastore().find(Dataset.class)
        .filter("_id", new ObjectId(id)).get();
  }

  @Override
  public boolean delete(Dataset dataset) {
    morphiaDatastoreProvider.getDatastore().delete(
        morphiaDatastoreProvider.getDatastore().createQuery(Dataset.class).field(DATASET_ID)
            .equal(dataset.getDatasetId()));
    LOGGER
        .debug(
            "Dataset with datasetId: '{}', datasetName: '{}' and OrganizationId: '{}' deleted in Mongo",
            dataset.getDatasetId(), dataset.getDatasetName(), dataset.getOrganizationId());
    return true;
  }

  public boolean deleteByDatasetId(int datasetId) {
    Dataset dataset = new Dataset();
    dataset.setDatasetId(datasetId);
    return delete(dataset);
  }

  public Dataset getDatasetByDatasetName(String datasetName) {
    return morphiaDatastoreProvider.getDatastore().find(Dataset.class).field(DATASET_NAME)
        .equal(datasetName).get();
  }

  public Dataset getDatasetByDatasetId(int datasetId) {
    return morphiaDatastoreProvider.getDatastore().find(Dataset.class)
        .filter(DATASET_ID, datasetId).get();
  }

  public Dataset getDatasetByOrganizationIdAndDatasetName(String organizationId,
      String datasetName) {
    return morphiaDatastoreProvider.getDatastore().find(Dataset.class).field("organizationId")
        .equal(organizationId).field(DATASET_NAME)
        .equal(datasetName).get();
  }

  public boolean existsDatasetByDatasetName(String datasetName) {
    return morphiaDatastoreProvider.getDatastore().find(Dataset.class).field(DATASET_NAME)
        .equal(datasetName)
        .project("_id", true).get() != null;
  }

  public List<Dataset> getAllDatasetsByProvider(String provider, String nextPage) {
    Query<Dataset> query = morphiaDatastoreProvider.getDatastore().createQuery(Dataset.class);
    query.field("provider").equal(provider).order("_id");
    if (StringUtils.isNotEmpty(nextPage)) {
      query.field("_id").greaterThan(new ObjectId(nextPage));
    }
    return query.asList(new FindOptions().limit(datasetsPerRequest));
  }

  public List<Dataset> getAllDatasetsByIntermidiateProvider(String intermediateProvider,
      String nextPage) {
    Query<Dataset> query = morphiaDatastoreProvider.getDatastore().createQuery(Dataset.class);
    query.field("intermediateProvider").equal(intermediateProvider).order("_id");
    if (StringUtils.isNotEmpty(nextPage)) {
      query.field("_id").greaterThan(new ObjectId(nextPage));
    }
    return query.asList(new FindOptions().limit(datasetsPerRequest));
  }

  public List<Dataset> getAllDatasetsByDataProvider(String dataProvider, String nextPage) {
    Query<Dataset> query = morphiaDatastoreProvider.getDatastore().createQuery(Dataset.class);
    query.field("dataProvider").equal(dataProvider).order("_id");
    if (StringUtils.isNotEmpty(nextPage)) {
      query.field("_id").greaterThan(new ObjectId(nextPage));
    }
    return query.asList(new FindOptions().limit(datasetsPerRequest));
  }

  public List<Dataset> getAllDatasetsByOrganizationId(String organizationId, String nextPage) {
    Query<Dataset> query = morphiaDatastoreProvider.getDatastore().createQuery(Dataset.class);
    query.field("organizationId").equal(organizationId).order("_id");
    if (StringUtils.isNotEmpty(nextPage)) {
      query.field("_id").greaterThan(new ObjectId(nextPage));
    }
    return query.asList(new FindOptions().limit(datasetsPerRequest));
  }

  public List<Dataset> getAllDatasetsByOrganizationName(String organizationName, String nextPage) {
    Query<Dataset> query = morphiaDatastoreProvider.getDatastore().createQuery(Dataset.class);
    query.field("organizationName").equal(organizationName).order("_id");
    if (StringUtils.isNotEmpty(nextPage)) {
      query.field("_id").greaterThan(new ObjectId(nextPage));
    }
    return query.asList(new FindOptions().limit(datasetsPerRequest));
  }

  public int getDatasetsPerRequest() {
    return datasetsPerRequest;
  }

  public void setDatasetsPerRequest(int datasetsPerRequest) {
    this.datasetsPerRequest = datasetsPerRequest;
  }

  public int findNextInSequenceDatasetId() {
    Dataset dataset;
    DatasetIdSequence datasetIdSequence = morphiaDatastoreProvider.getDatastore()
        .find(DatasetIdSequence.class).get();
    do {
      datasetIdSequence.setSequence(datasetIdSequence.getSequence() + 1);
      dataset = this.getDatasetByDatasetId(datasetIdSequence.getSequence());
    } while (dataset != null);
    Query<DatasetIdSequence> updateQuery = morphiaDatastoreProvider.getDatastore().createQuery(DatasetIdSequence.class).field("_id").equal(datasetIdSequence.getId());
    UpdateOperations<DatasetIdSequence> updateOperations = morphiaDatastoreProvider.getDatastore()
        .createUpdateOperations(DatasetIdSequence.class).set("sequence", datasetIdSequence.getSequence());
    morphiaDatastoreProvider.getDatastore().update(updateQuery, updateOperations);
    return datasetIdSequence.getSequence();
  }
}
