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
import eu.europeana.metis.core.rest.RequestLimits;
import eu.europeana.metis.core.workflow.OrderField;
import java.util.List;
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
 * Dataset Access Object for datasets using Mongo.
 */
@Repository
public class DatasetDao implements MetisDao<Dataset, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatasetDao.class);
  private static final String DATASET_NAME = "datasetName";
  private static final String DATASET_ID = "datasetId";
  private int datasetsPerRequest = RequestLimits.DATASETS_PER_REQUEST.getLimit();

  private final MorphiaDatastoreProvider morphiaDatastoreProvider;

  /**
   * Constructs the DAO
   *
   * @param morphiaDatastoreProvider {@link MorphiaDatastoreProvider} used to access Mongo
   */
  @Autowired
  public DatasetDao(MorphiaDatastoreProvider morphiaDatastoreProvider) {
    this.morphiaDatastoreProvider = morphiaDatastoreProvider;
  }

  /**
   * Create a dataset in the database
   *
   * @param dataset {@link Dataset} to be created
   * @return the {@link ObjectId} as String
   */
  @Override
  public String create(Dataset dataset) {
    Key<Dataset> datasetKey = morphiaDatastoreProvider.getDatastore().save(dataset);
    LOGGER.debug(
        "Dataset with datasetId: '{}', datasetName: '{}' and OrganizationId: '{}' created in Mongo",
        dataset.getDatasetId(), dataset.getDatasetName(), dataset.getOrganizationId());
    return datasetKey.getId().toString();
  }

  /**
   * Update a dataset in the database
   *
   * @param dataset {@link Dataset} to be updated
   * @return the {@link ObjectId} as String
   */
  @Override
  public String update(Dataset dataset) {
    Key<Dataset> datasetKey = morphiaDatastoreProvider.getDatastore().save(dataset);
    LOGGER.debug(
        "Dataset with datasetId: '{}', datasetName: '{}' and OrganizationId: '{}' updated in Mongo",
        dataset.getDatasetId(), dataset.getDatasetName(), dataset.getOrganizationId());
    return datasetKey.getId().toString();
  }

  /**
   * Get a dataset by {@link ObjectId} String.
   *
   * @param id the {@link ObjectId} String to search with
   * @return {@link Dataset}
   */
  @Override
  public Dataset getById(String id) {
    return morphiaDatastoreProvider.getDatastore().find(Dataset.class)
        .filter("_id", new ObjectId(id)).get();
  }

  /**
   * Delete a dataset using its datasetId.
   *
   * @param dataset {@link Dataset} containing the datasetId to be used for delete
   * @return always true
   */
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

  /**
   * Delete a dataset using a datasetId.
   *
   * @param datasetId the identifier used to delete a dataset from the database
   * @return always true
   */
  public boolean deleteByDatasetId(int datasetId) {
    Dataset dataset = new Dataset();
    dataset.setDatasetId(datasetId);
    return delete(dataset);
  }

  /**
   * Get a dataset using a datasetName
   *
   * @param datasetName the String to search for
   * @return {@link Dataset} or null
   */
  public Dataset getDatasetByDatasetName(String datasetName) {
    return morphiaDatastoreProvider.getDatastore().find(Dataset.class).field(DATASET_NAME)
        .equal(datasetName).get();
  }

  /**
   * Get a dataset using a datasetId
   *
   * @param datasetId the String to search for
   * @return {@link Dataset} or null
   */
  public Dataset getDatasetByDatasetId(int datasetId) {
    return morphiaDatastoreProvider.getDatastore().find(Dataset.class)
        .filter(DATASET_ID, datasetId).get();
  }

  /**
   * Get a dataset using an organizationId and datasetName
   *
   * @param organizationId the organizationId
   * @param datasetName the datasetName
   * @return {@link Dataset} or null
   */
  public Dataset getDatasetByOrganizationIdAndDatasetName(String organizationId,
      String datasetName) {
    return morphiaDatastoreProvider.getDatastore().find(Dataset.class).field("organizationId")
        .equal(organizationId).field(DATASET_NAME)
        .equal(datasetName).get();
  }

  /**
   * Check if a dataset exists using a datasetName.
   *
   * @param datasetName the datasetName
   * @return true if exist or false if it does not exist
   */
  public boolean existsDatasetByDatasetName(String datasetName) {
    return morphiaDatastoreProvider.getDatastore().find(Dataset.class).field(DATASET_NAME)
        .equal(datasetName)
        .project("_id", true).get() != null;
  }

  /**
   * Get all datasets using the provider field.
   *
   * @param provider the provider string used to find the datasets
   * @param nextPage the nextPage positive number
   * @return {@link List} of {@link Dataset}
   */
  public List<Dataset> getAllDatasetsByProvider(String provider, int nextPage) {
    Query<Dataset> query = morphiaDatastoreProvider.getDatastore().createQuery(Dataset.class);
    query.field("provider").equal(provider);
    query.order(OrderField.ID.getOrderFieldName());
    return query.asList(new FindOptions().skip(nextPage * datasetsPerRequest)
        .limit(datasetsPerRequest));
  }

  /**
   * Get all datasets using the intermediateProvider field.
   *
   * @param intermediateProvider the intermediateProvider string used to find the datasets
   * @param nextPage the nextPage positive number
   * @return {@link List} of {@link Dataset}
   */
  public List<Dataset> getAllDatasetsByIntermediateProvider(String intermediateProvider,
      int nextPage) {
    Query<Dataset> query = morphiaDatastoreProvider.getDatastore().createQuery(Dataset.class);
    query.field("intermediateProvider").equal(intermediateProvider);
    query.order(OrderField.ID.getOrderFieldName());
    return query.asList(new FindOptions().skip(nextPage * datasetsPerRequest)
        .limit(datasetsPerRequest));
  }

  /**
   * Get all datasets using the dataProvider field.
   *
   * @param dataProvider the dataProvider string used to find the datasets
   * @param nextPage the nextPage positive number
   * @return {@link List} of {@link Dataset}
   */
  public List<Dataset> getAllDatasetsByDataProvider(String dataProvider, int nextPage) {
    Query<Dataset> query = morphiaDatastoreProvider.getDatastore().createQuery(Dataset.class);
    query.field("dataProvider").equal(dataProvider);
    query.order(OrderField.ID.getOrderFieldName());
    return query.asList(new FindOptions().skip(nextPage * datasetsPerRequest)
        .limit(datasetsPerRequest));
  }

  /**
   * Get all datasets using the organizationId field.
   *
   * @param organizationId the organizationId string used to find the datasets
   * @param nextPage the nextPage positive number
   * @return {@link List} of {@link Dataset}
   */
  public List<Dataset> getAllDatasetsByOrganizationId(String organizationId, int nextPage) {
    Query<Dataset> query = morphiaDatastoreProvider.getDatastore().createQuery(Dataset.class);
    query.field("organizationId").equal(organizationId);
    query.order(OrderField.ID.getOrderFieldName());
    return query.asList(new FindOptions().skip(nextPage * datasetsPerRequest)
        .limit(datasetsPerRequest));
  }

  /**
   * Get all datasets using the organizationName field.
   *
   * @param organizationName the organizationName string used to find the datasets
   * @param nextPage the nextPage positive number
   * @return {@link List} of {@link Dataset}
   */
  public List<Dataset> getAllDatasetsByOrganizationName(String organizationName, int nextPage) {
    Query<Dataset> query = morphiaDatastoreProvider.getDatastore().createQuery(Dataset.class);
    query.field("organizationName").equal(organizationName);
    query.order(OrderField.ID.getOrderFieldName());
    return query.asList(new FindOptions().skip(nextPage * datasetsPerRequest)
        .limit(datasetsPerRequest));
  }

  public int getDatasetsPerRequest() {
    return datasetsPerRequest;
  }

  public void setDatasetsPerRequest(int datasetsPerRequest) {
    this.datasetsPerRequest = datasetsPerRequest;
  }

  /**
   * Find the next in sequence identifier that can be used as a datasetId for a {@link Dataset}.
   * <p>It will bypass any existing datasetId's in the system and will give the first available after that, otherwise it's simply an incremental identifier</p>
   *
   * @return the available identifier to be used further for a creation of a {@link Dataset}
   */
  public int findNextInSequenceDatasetId() {
    Dataset dataset;
    DatasetIdSequence datasetIdSequence = morphiaDatastoreProvider.getDatastore()
        .find(DatasetIdSequence.class).get();
    do {
      datasetIdSequence.setSequence(datasetIdSequence.getSequence() + 1);
      dataset = this.getDatasetByDatasetId(datasetIdSequence.getSequence());
    } while (dataset != null);
    Query<DatasetIdSequence> updateQuery = morphiaDatastoreProvider.getDatastore()
        .createQuery(DatasetIdSequence.class).field("_id").equal(datasetIdSequence.getId());
    UpdateOperations<DatasetIdSequence> updateOperations = morphiaDatastoreProvider.getDatastore()
        .createUpdateOperations(DatasetIdSequence.class)
        .set("sequence", datasetIdSequence.getSequence());
    morphiaDatastoreProvider.getDatastore().update(updateQuery, updateOperations);
    return datasetIdSequence.getSequence();
  }
}
