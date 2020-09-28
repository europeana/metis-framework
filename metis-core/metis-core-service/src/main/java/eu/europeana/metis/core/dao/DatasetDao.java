package eu.europeana.metis.core.dao;

import static eu.europeana.metis.core.common.DaoFieldNames.DATASET_ID;
import static eu.europeana.metis.core.common.DaoFieldNames.DATASET_NAME;
import static eu.europeana.metis.core.common.DaoFieldNames.DATA_PROVIDER;
import static eu.europeana.metis.core.common.DaoFieldNames.ID;
import static eu.europeana.metis.core.common.DaoFieldNames.PROVIDER;
import static eu.europeana.metis.mongo.MorphiaUtils.getListOfQueryRetryable;

import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.Sort;
import dev.morphia.query.experimental.filters.Filter;
import dev.morphia.query.experimental.filters.Filters;
import dev.morphia.query.experimental.updates.UpdateOperator;
import dev.morphia.query.experimental.updates.UpdateOperators;
import eu.europeana.cloud.mcs.driver.DataSetServiceClient;
import eu.europeana.cloud.service.mcs.exception.DataSetAlreadyExistsException;
import eu.europeana.cloud.service.mcs.exception.MCSException;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetIdSequence;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.rest.RequestLimits;
import eu.europeana.metis.exception.ExternalTaskException;
import eu.europeana.metis.utils.ExternalRequestUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
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
    Dataset datasetSaved = ExternalRequestUtil
        .retryableExternalRequestForNetworkExceptions(
            () -> morphiaDatastoreProvider.getDatastore().save(dataset));
    LOGGER.debug(
        "Dataset with datasetId: '{}', datasetName: '{}' and OrganizationId: '{}' created in Mongo",
        dataset.getDatasetId(), dataset.getDatasetName(), dataset.getOrganizationId());
    return datasetSaved == null ? null : datasetSaved.getId().toString();
  }

  /**
   * Update a dataset in the database
   *
   * @param dataset {@link Dataset} to be updated
   * @return the {@link ObjectId} as String
   */
  @Override
  public String update(Dataset dataset) {
    Dataset datasetSaved = ExternalRequestUtil
        .retryableExternalRequestForNetworkExceptions(
            () -> morphiaDatastoreProvider.getDatastore().save(dataset));
    LOGGER.debug(
        "Dataset with datasetId: '{}', datasetName: '{}' and OrganizationId: '{}' updated in Mongo",
        dataset.getDatasetId(), dataset.getDatasetName(), dataset.getOrganizationId());
    return datasetSaved == null ? null : datasetSaved.getId().toString();
  }

  /**
   * Get a dataset by {@link ObjectId} String.
   *
   * @param id the {@link ObjectId} String to search with
   * @return {@link Dataset}
   */
  @Override
  public Dataset getById(String id) {
    return ExternalRequestUtil.retryableExternalRequestForNetworkExceptions(
        () -> morphiaDatastoreProvider.getDatastore().find(Dataset.class)
            .filter(Filters.eq(ID.getFieldName(), new ObjectId(id))).first());
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
        .retryableExternalRequestForNetworkExceptions(
            () -> morphiaDatastoreProvider.getDatastore().find(Dataset.class)
                .filter(Filters.eq(DATASET_ID.getFieldName(), dataset.getDatasetId())).delete());
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
    return ExternalRequestUtil.retryableExternalRequestForNetworkExceptions(
        () -> morphiaDatastoreProvider.getDatastore().find(Dataset.class)
            .filter(Filters.eq(DATASET_NAME.getFieldName(), datasetName)).first());
  }

  /**
   * Get a dataset using a datasetId
   *
   * @param datasetId the String to search for
   * @return {@link Dataset} or null
   */
  public Dataset getDatasetByDatasetId(String datasetId) {
    return ExternalRequestUtil
        .retryableExternalRequestForNetworkExceptions(
            () -> morphiaDatastoreProvider.getDatastore().find(Dataset.class)
                .filter(Filters.eq(DATASET_ID.getFieldName(), datasetId)).first());
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
    return ExternalRequestUtil.retryableExternalRequestForNetworkExceptions(
        () -> morphiaDatastoreProvider.getDatastore().find(Dataset.class)
            .filter(Filters.eq("organizationId", organizationId))
            .filter(Filters.eq(DATASET_NAME.getFieldName(), datasetName))).first();
  }

  /**
   * Check if a dataset exists using a datasetName.
   *
   * @param datasetName the datasetName
   * @return true if exist or false if it does not exist
   */
  boolean existsDatasetByDatasetName(String datasetName) {
    return ExternalRequestUtil.retryableExternalRequestForNetworkExceptions(
        () -> morphiaDatastoreProvider.getDatastore().find(Dataset.class)
            .filter(Filters.eq(DATASET_NAME.getFieldName(), datasetName))
            .first(new FindOptions().projection().include(ID.getFieldName()))) != null;
  }

  /**
   * Get all datasets using the provider field.
   *
   * @param provider the provider string used to find the datasets
   * @param nextPage the nextPage positive number
   * @return {@link List} of {@link Dataset}
   */
  public List<Dataset> getAllDatasetsByProvider(String provider, int nextPage) {
    Query<Dataset> query = morphiaDatastoreProvider.getDatastore().find(Dataset.class);
    query.filter(Filters.eq("provider", provider));
    final FindOptions findOptions = new FindOptions().skip(nextPage * getDatasetsPerRequest())
        .limit(getDatasetsPerRequest());
    return getListOfQueryRetryable(query, findOptions);
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
    Query<Dataset> query = morphiaDatastoreProvider.getDatastore().find(Dataset.class);
    query.filter(Filters.eq("intermediateProvider", intermediateProvider));
    final FindOptions findOptions = new FindOptions().skip(nextPage * getDatasetsPerRequest())
        .limit(getDatasetsPerRequest());
    return getListOfQueryRetryable(query, findOptions);
  }

  /**
   * Get all datasets using the dataProvider field.
   *
   * @param dataProvider the dataProvider string used to find the datasets
   * @param nextPage the nextPage positive number
   * @return {@link List} of {@link Dataset}
   */
  public List<Dataset> getAllDatasetsByDataProvider(String dataProvider, int nextPage) {
    Query<Dataset> query = morphiaDatastoreProvider.getDatastore().find(Dataset.class);
    query.filter(Filters.eq("dataProvider", dataProvider));
    final FindOptions findOptions = new FindOptions().skip(nextPage * getDatasetsPerRequest())
        .limit(getDatasetsPerRequest());
    return getListOfQueryRetryable(query, findOptions);
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
    Query<Dataset> query = morphiaDatastoreProvider.getDatastore().find(Dataset.class);
    query.filter(Filters.eq("organizationId", organizationId));
    return getListOfQueryRetryable(query, options.apply(new FindOptions()));
  }

  /**
   * Get all datasets using the organizationName field.
   *
   * @param organizationName the organizationName string used to find the datasets
   * @param nextPage the nextPage positive number
   * @return {@link List} of {@link Dataset}
   */
  public List<Dataset> getAllDatasetsByOrganizationName(String organizationName, int nextPage) {
    Query<Dataset> query = morphiaDatastoreProvider.getDatastore().find(Dataset.class);
    query.filter(Filters.eq("organizationName", organizationName));
    final FindOptions findOptions = new FindOptions().skip(nextPage * getDatasetsPerRequest())
        .limit(getDatasetsPerRequest());
    return getListOfQueryRetryable(query, findOptions);
  }

  /**
   * Get a list of datasets to redirect from
   *
   * @param datasetIdToRedirectFrom the dataset ids to redirect from
   * @return the list of dataset to redirect from
   */
  public List<Dataset> getAllDatasetsByDatasetIdsToRedirectFrom(String datasetIdToRedirectFrom) {
    Query<Dataset> query = morphiaDatastoreProvider.getDatastore().find(Dataset.class);
    query.filter(Filters.eq("datasetIdsToRedirectFrom", datasetIdToRedirectFrom));
    return getListOfQueryRetryable(query, new FindOptions());
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
    DatasetIdSequence datasetIdSequence = morphiaDatastoreProvider.getDatastore()
        .find(DatasetIdSequence.class).first();
    Dataset dataset;
    do {
      datasetIdSequence.setSequence(datasetIdSequence.getSequence() + 1);
      dataset = this.getDatasetByDatasetId(Integer.toString(datasetIdSequence.getSequence()));
    } while (dataset != null);
    Query<DatasetIdSequence> updateQuery = morphiaDatastoreProvider.getDatastore()
        .find(DatasetIdSequence.class)
        .filter(Filters.eq(ID.getFieldName(), datasetIdSequence.getId()));
    final UpdateOperator updateOperator = UpdateOperators
        .set("sequence", datasetIdSequence.getSequence());

    ExternalRequestUtil.retryableExternalRequestForNetworkExceptions(
        () -> updateQuery.update(updateOperator).execute());
    return datasetIdSequence.getSequence();
  }

  /**
   * Checks if the ecloud dataset identifier already exists in ECloud and if it does not, it will
   * try to create a new one and add the identifier inside the metis Dataset object and store.
   * <p>This is an exception method that uses the {@link DataSetServiceClient} to communicate with
   * the external dataset resource in ECloud</p>
   *
   * @param dataset the Dataset object to check
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
        throw new ExternalTaskException("Dataset already exist, not recreating", e);
      } catch (MCSException e) {
        throw new ExternalTaskException("An error has occurred during ecloud dataset creation.", e);
      }
    } else {
      LOGGER
          .info("Dataset with datasetId {} already has a dataset initialized in Ecloud with id {}",
              dataset.getDatasetId(), dataset.getEcloudDatasetId());
    }
    return dataset.getEcloudDatasetId();
  }

  /**
   * Get the list of of matching DatasetSearch using dataset
   *
   * @param datasetIdWords a list of words to be used for datasetId search, that field is searched
   * as a "starts with" operation
   * @param words a list of words to be used for datasetName, provider and dataProvider search.
   * Those words are considered as AND operation for each individual field.
   * @param nextPage the nextPage number, must be positive
   * @return a list with the datasets found
   */
  public List<Dataset> searchDatasetsBasedOnSearchString(List<String> datasetIdWords,
      List<String> words, int nextPage) {
    Query<Dataset> query = morphiaDatastoreProvider.getDatastore().find(Dataset.class);
    final List<Filter> datasetIdFilters = new ArrayList<>(datasetIdWords.size());
    final List<Filter> datasetNameFilters = new ArrayList<>(words.size());
    final List<Filter> providerIdFilters = new ArrayList<>(words.size());
    final List<Filter> dataProviderIdFilters = new ArrayList<>(words.size());

    //Search on datasetId, only words that start with a numeric character
    for (String datasetIdWord : datasetIdWords) {
      datasetIdFilters.add(
          Filters.regex(DATASET_ID.getFieldName())
              .pattern(Pattern.compile("^" + Pattern.quote(datasetIdWord))));
    }

    //Search on provider and dataProvider
    for (String word : words) {
      datasetNameFilters.add(Filters.regex(DATASET_NAME.getFieldName())
          .pattern(Pattern.compile(word, Pattern.CASE_INSENSITIVE)));
      providerIdFilters.add(Filters.regex(PROVIDER.getFieldName())
          .pattern(Pattern.compile(word, Pattern.CASE_INSENSITIVE)));
      dataProviderIdFilters.add(Filters.regex(DATA_PROVIDER.getFieldName())
          .pattern(Pattern.compile(word, Pattern.CASE_INSENSITIVE)));
    }
    final List<Filter> filterGroups = new ArrayList<>();
    if (!datasetIdFilters.isEmpty()) {
      filterGroups.add(Filters.or(datasetIdFilters.toArray(Filter[]::new)));
    }
    if (!datasetNameFilters.isEmpty()) {
      filterGroups.add(Filters.or(datasetNameFilters.toArray(Filter[]::new)));
    }
    if (!providerIdFilters.isEmpty()) {
      filterGroups.add(Filters.or(providerIdFilters.toArray(Filter[]::new)));
    }
    if (!dataProviderIdFilters.isEmpty()) {
      filterGroups.add(Filters.or(dataProviderIdFilters.toArray(Filter[]::new)));
    }

    if (!filterGroups.isEmpty()) {
      query.filter(Filters.or(filterGroups.toArray(Filter[]::new)));
    }

    final FindOptions findOptions = new FindOptions()
        .sort(Sort.ascending(DATASET_ID.getFieldName()))
        .skip(nextPage * getDatasetsPerRequest())
        .limit(getDatasetsPerRequest());
    return getListOfQueryRetryable(query, findOptions);
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
