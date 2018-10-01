package eu.europeana.metis.core.dao;

import eu.europeana.cloud.mcs.driver.DataSetServiceClient;
import eu.europeana.cloud.service.mcs.exception.DataSetAlreadyExistsException;
import eu.europeana.cloud.service.mcs.exception.MCSException;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetIdSequence;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.rest.RequestLimits;
import eu.europeana.metis.core.workflow.OrderField;
import eu.europeana.metis.exception.ExternalTaskException;
import eu.europeana.metis.utils.ExternalRequestUtil;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;
import org.apache.commons.lang3.StringUtils;
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
 * Dataset Access Object for datasets using Mongo. It also contains the {@link DataSetServiceClient}
 * which is used to access functionality of the ECloud datasets.
 */
@Repository
public class DatasetDao implements MetisDao<Dataset, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatasetDao.class);
  private static final String DATASET_NAME = "datasetName";
  private static final String DATASET_ID = "datasetId";
  private int datasetsPerRequest = RequestLimits.DATASETS_PER_REQUEST.getLimit();

  private final MorphiaDatastoreProvider morphiaDatastoreProvider;
  private final DataSetServiceClient ecloudDataSetServiceClient;
  private String ecloudProvider; // Use getter and setter for this field!

  /**
   * Constructs the DAO
   * <p>Initialize {@link #ecloudProvider} using the setter class.
   * Use setter for {@link #setDatasetsPerRequest(int)} to overwrite the default value</p>
   *
   * @param morphiaDatastoreProvider {@link MorphiaDatastoreProvider} used to access Mongo
   * @param ecloudDataSetServiceClient {@link DataSetServiceClient} to access the ecloud dataset
   * functionality
   */
  @Autowired
  public DatasetDao(MorphiaDatastoreProvider morphiaDatastoreProvider,
      DataSetServiceClient ecloudDataSetServiceClient) {
    this.morphiaDatastoreProvider = morphiaDatastoreProvider;
    this.ecloudDataSetServiceClient = ecloudDataSetServiceClient;
  }

  /**
   * Create a dataset in the database
   *
   * @param dataset {@link Dataset} to be created
   * @return the {@link ObjectId} as String
   */
  @Override
  public String create(Dataset dataset) {
    Key<Dataset> datasetKey = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(() -> morphiaDatastoreProvider.getDatastore().save(dataset));
    LOGGER.debug(
        "Dataset with datasetId: '{}', datasetName: '{}' and OrganizationId: '{}' created in Mongo",
        dataset.getDatasetId(), dataset.getDatasetName(), dataset.getOrganizationId());
    return datasetKey != null ? datasetKey.getId().toString() : null;
  }

  /**
   * Update a dataset in the database
   *
   * @param dataset {@link Dataset} to be updated
   * @return the {@link ObjectId} as String
   */
  @Override
  public String update(Dataset dataset) {
    Key<Dataset> datasetKey = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(() -> morphiaDatastoreProvider.getDatastore().save(dataset));
    LOGGER.debug(
        "Dataset with datasetId: '{}', datasetName: '{}' and OrganizationId: '{}' updated in Mongo",
        dataset.getDatasetId(), dataset.getDatasetName(), dataset.getOrganizationId());
    return datasetKey != null ? datasetKey.getId().toString() : null;
  }

  /**
   * Get a dataset by {@link ObjectId} String.
   *
   * @param id the {@link ObjectId} String to search with
   * @return {@link Dataset}
   */
  @Override
  public Dataset getById(String id) {
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(
        () -> morphiaDatastoreProvider.getDatastore().find(Dataset.class)
            .filter("_id", new ObjectId(id)).get());
  }

  /**
   * Delete a dataset using its datasetId.
   *
   * @param dataset {@link Dataset} containing the datasetId to be used for delete
   * @return always true
   */
  @Override
  public boolean delete(Dataset dataset) {
    ExternalRequestUtil
        .retryableExternalRequestConnectionReset(() -> morphiaDatastoreProvider.getDatastore().delete(
            morphiaDatastoreProvider.getDatastore().createQuery(Dataset.class).field(DATASET_ID)
                .equal(dataset.getDatasetId())));
    LOGGER.debug(
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
  public boolean deleteByDatasetId(String datasetId) {
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
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(
        () -> morphiaDatastoreProvider.getDatastore().find(Dataset.class).field(DATASET_NAME)
            .equal(datasetName).get());
  }

  /**
   * Get a dataset using a datasetId
   *
   * @param datasetId the String to search for
   * @return {@link Dataset} or null
   */
  public Dataset getDatasetByDatasetId(String datasetId) {
    return ExternalRequestUtil
        .retryableExternalRequestConnectionReset(() -> morphiaDatastoreProvider.getDatastore().find(Dataset.class)
            .filter(DATASET_ID, datasetId).get());
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
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(
        () -> morphiaDatastoreProvider.getDatastore().find(Dataset.class).field("organizationId")
            .equal(organizationId).field(DATASET_NAME).equal(datasetName).get());
  }

  /**
   * Check if a dataset exists using a datasetName.
   *
   * @param datasetName the datasetName
   * @return true if exist or false if it does not exist
   */
  public boolean existsDatasetByDatasetName(String datasetName) {
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(
        () -> morphiaDatastoreProvider.getDatastore().find(Dataset.class).field(DATASET_NAME)
            .equal(datasetName).project("_id", true).get()) != null;
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
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(() -> query.asList(
        new FindOptions().skip(nextPage * getDatasetsPerRequest()).limit(getDatasetsPerRequest())));
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
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(() -> query.asList(
        new FindOptions().skip(nextPage * getDatasetsPerRequest()).limit(getDatasetsPerRequest())));
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
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(() -> query.asList(
        new FindOptions().skip(nextPage * getDatasetsPerRequest()).limit(getDatasetsPerRequest())));
  }

  /**
   * Get all datasets using the organizationId field, using pagination.
   *
   * @param organizationId the organizationId string used to find the datasets
   * @param nextPage the nextPage positive number
   * @return {@link List} of {@link Dataset}
   */
  public List<Dataset> getAllDatasetsByOrganizationId(String organizationId, int nextPage) {
    return getAllDatasetsByOrganizationId(organizationId,
        options -> options.skip(nextPage * getDatasetsPerRequest()).limit(getDatasetsPerRequest()));
  }

  /**
   * Get all datasets using the organizationId field.
   *
   * @param organizationId the organizationId string used to find the datasets
   * @return {@link List} of {@link Dataset}
   */
  public List<Dataset> getAllDatasetsByOrganizationId(String organizationId) {
    return getAllDatasetsByOrganizationId(organizationId, UnaryOperator.identity());
  }

  private List<Dataset> getAllDatasetsByOrganizationId(String organizationId,
      UnaryOperator<FindOptions> options) {
    Query<Dataset> query = morphiaDatastoreProvider.getDatastore().createQuery(Dataset.class);
    query.field("organizationId").equal(organizationId);
    query.order(OrderField.ID.getOrderFieldName());
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(() -> query.asList(options.apply(new FindOptions())));
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
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(() -> query.asList(
        new FindOptions().skip(nextPage * getDatasetsPerRequest()).limit(getDatasetsPerRequest())));
  }

  public int getDatasetsPerRequest() {
    synchronized (this) {
      return datasetsPerRequest;
    }
  }

  public void setDatasetsPerRequest(int datasetsPerRequest) {
    synchronized (this) {
      this.datasetsPerRequest = datasetsPerRequest;
    }
  }

  /**
   * Find the next in sequence identifier that can be used as a datasetId for a {@link Dataset}.
   * <p>It will bypass any existing datasetId's in the system and will give the first available
   * after that, otherwise it's simply an incremental identifier</p>
   *
   * @return the available identifier to be used further for a creation of a {@link Dataset}
   */
  public int findNextInSequenceDatasetId() {
    Dataset dataset;
    DatasetIdSequence datasetIdSequence = morphiaDatastoreProvider.getDatastore()
        .find(DatasetIdSequence.class).get();
    do {
      datasetIdSequence.setSequence(datasetIdSequence.getSequence() + 1);
      dataset = this.getDatasetByDatasetId(Integer.toString(datasetIdSequence.getSequence()));
    } while (dataset != null);
    Query<DatasetIdSequence> updateQuery = morphiaDatastoreProvider.getDatastore()
        .createQuery(DatasetIdSequence.class).field("_id").equal(datasetIdSequence.getId());
    UpdateOperations<DatasetIdSequence> updateOperations = morphiaDatastoreProvider.getDatastore()
        .createUpdateOperations(DatasetIdSequence.class)
        .set("sequence", datasetIdSequence.getSequence());
    ExternalRequestUtil.retryableExternalRequestConnectionReset(
        () -> morphiaDatastoreProvider.getDatastore().update(updateQuery, updateOperations));
    return datasetIdSequence.getSequence();
  }

  /**
   * Checks if the ecloud dataset identifier already exists in ECloud and if it does not, it will
   * try to create a new one and add the identifier inside the metis Dataset object and store.
   * <p>This is an exception method that uses the {@link DataSetServiceClient} to communicate with
   * the external dataset resource in ECloud</p>
   *
   * @param dataset the Datase object to check
   * @return the ECloud dataset identifier
   * @throws ExternalTaskException if an error occurred during the creation of the dataset
   * identifier on ECloud
   */
  public String checkAndCreateDatasetInEcloud(Dataset dataset) throws ExternalTaskException {
    if (StringUtils.isEmpty(dataset.getEcloudDatasetId()) || dataset.getEcloudDatasetId()
        .startsWith("NOT_CREATED_YET")) {
      final String uuid = UUID.randomUUID().toString();
      dataset.setEcloudDatasetId(uuid);
      try {
        ecloudDataSetServiceClient.createDataSet(getEcloudProvider(), uuid,
            "Metis generated dataset");
        update(dataset);
      } catch (DataSetAlreadyExistsException e) {
        LOGGER.info("Dataset already exist, not recreating", e);
        throw new ExternalTaskException("Dataset already exist, not recreating", e);
      } catch (MCSException e) {
        LOGGER.error("An error has occurred during ecloud dataset creation.", e);
        throw new ExternalTaskException("An error has occurred during ecloud dataset creation.", e);
      }
    } else {
      LOGGER
          .info("Dataset with datasetId {} already has a dataset initialized in Ecloud with id {}",
              dataset.getDatasetId(), dataset.getEcloudDatasetId());
    }
    return dataset.getEcloudDatasetId();
  }

  public void setEcloudProvider(String ecloudProvider) {
    synchronized (this) {
      this.ecloudProvider = ecloudProvider;
    }
  }

  private String getEcloudProvider() {
    synchronized (this) {
      return this.ecloudProvider;
    }
  }
}
