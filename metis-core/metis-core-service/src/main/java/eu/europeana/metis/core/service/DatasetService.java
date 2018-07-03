package eu.europeana.metis.core.service;

import eu.europeana.metis.CommonStringValues;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.DatasetXsltDao;
import eu.europeana.metis.core.dao.ScheduledWorkflowDao;
import eu.europeana.metis.core.dao.WorkflowDao;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetXslt;
import eu.europeana.metis.core.exceptions.DatasetAlreadyExistsException;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoXsltFoundException;
import eu.europeana.metis.core.exceptions.XsltSetupException;
import eu.europeana.metis.core.rest.Record;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.core.workflow.plugins.TransformationPlugin;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.GenericMetisException;
import eu.europeana.metis.exception.UserUnauthorizedException;
import eu.europeana.metis.transformation.service.EuropeanaGeneratedIdsMap;
import eu.europeana.metis.transformation.service.EuropeanaIdCreator;
import eu.europeana.metis.transformation.service.EuropeanaIdException;
import eu.europeana.metis.transformation.service.TransformationException;
import eu.europeana.metis.transformation.service.XsltTransformer;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Contains business logic of how to manipulate datasets in the system using several components. The
 * functionality in this class is checked for user authentication.
 */
@Service
public class DatasetService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatasetService.class);
  private static final String DATASET_CREATION_LOCK = "datasetCreationLock";

  private final Authorizer authorizer;
  private final DatasetDao datasetDao;
  private final DatasetXsltDao datasetXsltDao;
  private final WorkflowDao workflowDao;
  private final WorkflowExecutionDao workflowExecutionDao;
  private final ScheduledWorkflowDao scheduledWorkflowDao;
  private final RedissonClient redissonClient;
  private String metisCoreUrl; //Initialize with setter

  /**
   * Constructs the service.
   *
   * @param datasetDao the Dao instance to access the Dataset database
   * @param datasetXsltDao the Dao instance to access the DatasetXslt database
   * @param workflowDao the Dao instance to access the Workflow database
   * @param workflowExecutionDao the Dao instance to access the WorkflowExecution database
   * @param scheduledWorkflowDao the Dao instance to access the ScheduledWorkflow database
   * @param redissonClient the redisson client used for distributed locks
   * @param authorizer the authorizer for this service
   */
  @Autowired
  public DatasetService(DatasetDao datasetDao, DatasetXsltDao datasetXsltDao,
      WorkflowDao workflowDao, WorkflowExecutionDao workflowExecutionDao,
      ScheduledWorkflowDao scheduledWorkflowDao, RedissonClient redissonClient,
      Authorizer authorizer) {
    this.datasetDao = datasetDao;
    this.datasetXsltDao = datasetXsltDao;
    this.workflowDao = workflowDao;
    this.workflowExecutionDao = workflowExecutionDao;
    this.scheduledWorkflowDao = scheduledWorkflowDao;
    this.redissonClient = redissonClient;
    this.authorizer = authorizer;
  }

  /**
   * Creates a dataset for a specific {@link MetisUser}
   *
   * @param metisUser the user used to create the dataset
   * @param dataset the dataset to be created
   * @return the created {@link Dataset} including the extra fields generated from the system
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link DatasetAlreadyExistsException} if the dataset for the same organizationId and datasetName already exists in the system.</li>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized</li>
   * </ul>
   */
  public Dataset createDataset(MetisUser metisUser, Dataset dataset)
      throws GenericMetisException {
    authorizer.authorizeWriteNewDataset(metisUser);

    dataset.setOrganizationId(metisUser.getOrganizationId());
    dataset.setOrganizationName(metisUser.getOrganizationName());

    //Lock required for find in the next empty datasetId
    RLock lock = redissonClient.getFairLock(DATASET_CREATION_LOCK);
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
      dataset.setCreatedByUserId(metisUser.getUserId());
      dataset.setId(null);
      dataset.setUpdatedDate(null);

      dataset.setCreatedDate(new Date());
      //Add fake ecloudDatasetId to avoid null errors in the database
      dataset.setEcloudDatasetId(String.format("NOT_CREATED_YET-%s", UUID.randomUUID().toString()));

      int nextInSequenceDatasetId = datasetDao.findNextInSequenceDatasetId();
      dataset.setDatasetId(Integer.toString(nextInSequenceDatasetId));
      datasetObjectId = datasetDao.getById(datasetDao.create(dataset));
    } finally {
      lock.unlock();
    }
    return datasetObjectId;
  }

  /**
   * Update an already existent dataset.
   *
   * @param metisUser the {@link MetisUser} to authorize with
   * @param dataset the provided dataset with the changes and the datasetId included in the {@link Dataset}
   * @param xsltString the text of the String representation
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoDatasetFoundException} if the dataset for datasetId was not found.</li>
   * <li>{@link BadContentException} if the dataset has an execution running.</li>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized.</li>
   * <li>{@link DatasetAlreadyExistsException} if the request contains a datasetName change and that datasetName already exists for organizationId of metisUser.</li>
   * </ul>
   */
  public void updateDataset(MetisUser metisUser, Dataset dataset, String xsltString)
      throws GenericMetisException {

    // Find existing dataset and check authentication.
    Dataset storedDataset = authorizer
        .authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId());

    // Check that the new dataset name does not already exist.
    final String newDatasetName = dataset.getDatasetName();
    if (!storedDataset.getDatasetName().equals(newDatasetName)
        && datasetDao.getDatasetByOrganizationIdAndDatasetName(metisUser.getOrganizationId(),
        newDatasetName) != null) {
      throw new DatasetAlreadyExistsException(String.format(
          "Trying to change dataset with datasetName: %s but dataset with organizationId: %s and datasetName: %s already exists",
          storedDataset.getDatasetName(), metisUser.getOrganizationId(), newDatasetName));
    }

    // Check that there is no workflow execution pending for the given dataset.
    if (workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId()) != null) {
      throw new BadContentException(
          String.format("Workflow execution is active for datasteId %s", dataset.getDatasetId()));
    }

    // Set/overwrite dataset properties that the user may not determine.
    dataset.setOrganizationId(metisUser.getOrganizationId());
    dataset.setOrganizationName(metisUser.getOrganizationName());
    dataset.setCreatedByUserId(storedDataset.getCreatedByUserId());
    dataset.setEcloudDatasetId(storedDataset.getEcloudDatasetId());
    dataset.setCreatedDate(storedDataset.getCreatedDate());
    dataset.setOrganizationId(storedDataset.getOrganizationId());
    dataset.setOrganizationName(storedDataset.getOrganizationName());
    dataset.setCreatedByUserId(storedDataset.getCreatedByUserId());
    dataset.setId(storedDataset.getId());
    if (xsltString != null) {
      dataset.setXsltId(new ObjectId(
          datasetXsltDao.create(new DatasetXslt(dataset.getDatasetId(), xsltString))));
    } else {
      dataset.setXsltId(storedDataset.getXsltId());
    }

    // Update the dataset
    dataset.setUpdatedDate(new Date());
    datasetDao.update(dataset);
  }

  /**
   * Delete a dataset from the system
   *
   * @param metisUser the {@link MetisUser} to authorize with
   * @param datasetId the identifier to find the dataset with
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link BadContentException} if the dataset is has an execution running.</li>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized.</li>
   * <li>{@link NoDatasetFoundException} if the dataset was not found.</li>
   * </ul>
   */
  public void deleteDatasetByDatasetId(MetisUser metisUser, String datasetId)
      throws GenericMetisException {

    // Find existing dataset and check authentication.
    authorizer.authorizeWriteExistingDatasetById(metisUser, datasetId);

    // Check that there is no workflow execution pending for the given dataset.
    if (workflowExecutionDao.existsAndNotCompleted(datasetId) != null) {
      throw new BadContentException(
          String.format("Workflow execution is active for datasteId %s", datasetId));
    }

    // Delete the dataset.
    datasetDao.deleteByDatasetId(datasetId);

    // Clean up dataset leftovers
    datasetXsltDao.deleteAllByDatasetId(datasetId);
    workflowDao.deleteWorkflow(datasetId);
    workflowExecutionDao.deleteAllByDatasetId(datasetId);
    scheduledWorkflowDao.deleteAllByDatasetId(datasetId);
  }

  /**
   * Get a dataset from the system using a datasetName
   *
   * @param metisUser the {@link MetisUser} to authorize with
   * @param datasetName the string used to find the dataset with
   * @return {@link Dataset}
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoDatasetFoundException} if the dataset is not found in the system.</li>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized.</li>
   * </ul>
   */
  public Dataset getDatasetByDatasetName(MetisUser metisUser, String datasetName)
      throws GenericMetisException {
    return authorizer.authorizeReadExistingDatasetByName(metisUser, datasetName);
  }

  /**
   * Get a dataset from the system using a datasetId.
   *
   * @param metisUser the {@link MetisUser} to authorize with
   * @param datasetId the identifier to find the dataset with
   * @return {@link Dataset}
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoDatasetFoundException} if the dataset was not found.</li>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized.</li>
   * </ul>
   */
  public Dataset getDatasetByDatasetId(MetisUser metisUser, String datasetId)
      throws GenericMetisException {
    return authorizer.authorizeReadExistingDatasetById(metisUser, datasetId);
  }

  /**
   * Get the xslt object containing the escaped xslt string using a dataset identifier.
   *
   * @param metisUser the {@link MetisUser} to authorize with
   * @param datasetId the identifier to find the xslt with
   * @return the {@link DatasetXslt} object containing the xslt as an escaped string
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoXsltFoundException} if the xslt was not found.</li>
   * <li>{@link NoDatasetFoundException} if the dataset was not found.</li>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized.</li>
   * </ul>
   */
  public DatasetXslt getDatasetXsltByDatasetId(MetisUser metisUser,
      String datasetId) throws GenericMetisException {
    Dataset dataset = authorizer.authorizeReadExistingDatasetById(metisUser, datasetId);
    DatasetXslt datasetXslt =
        datasetXsltDao.getById(dataset.getXsltId() == null ? null : dataset.getXsltId().toString());
    if (datasetXslt == null) {
      throw new NoXsltFoundException(String.format(
          "No datasetXslt found for dataset with datasetId: '%s' and xsltId: '%s' in METIS",
          datasetId, dataset.getXsltId()));
    }
    return datasetXslt;
  }

  /**
   * Get the xslt object containing the escaped xslt string using an xslt identifier.
   * <p>
   * It is a method that does not require authentication.
   * </p>
   *
   * @param xsltId the identifier to find the xslt with
   * @return the {@link DatasetXslt} object containing the xslt as an escaped string
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoXsltFoundException} if the xslt was not found.</li>
   * </ul>
   */
  public DatasetXslt getDatasetXsltByXsltId(String xsltId) throws GenericMetisException {
    DatasetXslt datasetXslt = datasetXsltDao.getById(xsltId);
    if (datasetXslt == null) {
      throw new NoXsltFoundException(
          String.format("No datasetXslt found with xsltId: '%s' in METIS", xsltId));
    }
    return datasetXslt;
  }

  /**
   * Create a new default xslt in the database.
   * <p>
   * Each dataset can have it's own custom xslt but a default xslt should always be available.
   * Creating a new default xslt will create a new {@link DatasetXslt} object and the older one will
   * still be available. The created {@link DatasetXslt} will have
   * {@link DatasetXslt#getDatasetId()} equal to -1 to indicate that it is not related to a specific
   * dataset.
   * </p>
   *
   * @param metisUser the {@link MetisUser} to authorize with
   * @param xsltString the text of the String representation non escaped
   * @return the created {@link DatasetXslt}
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized.</li>
   * </ul>
   */
  public DatasetXslt createDefaultXslt(MetisUser metisUser, String xsltString)
      throws GenericMetisException {
    authorizer.authorizeWriteDefaultXslt(metisUser);
    DatasetXslt datasetXslt = null;
    if (xsltString != null) {
      datasetXslt = datasetXsltDao.getById(
          datasetXsltDao.create(new DatasetXslt(DatasetXsltDao.DEFAULT_DATASET_ID, xsltString)));
    }
    return datasetXslt;
  }

  /**
   * Get the latest xslt for a datasetId. Use -1 to get the default xslt.
   * <p>
   * It is an method that does not require authentication and it is meant to be used from
   * external service to download the corresponding xslt. At the point of writing, ECloud
   * transformation topology is using it. {@link TransformationPlugin}
   * </p>
   *
   * @param datasetId the dataset identifier defined to which dataset an xslt belongs
   * @return the text representation of the String xslt non escaped
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoXsltFoundException} if the xslt was not found.</li>
   * </ul>
   */
  public DatasetXslt getLatestXsltForDatasetId(String datasetId) throws GenericMetisException {
    DatasetXslt datasetXslt = datasetXsltDao.getLatestXsltForDatasetId(datasetId);
    if (datasetXslt == null) {
      throw new NoXsltFoundException("No default datasetXslt found");
    }
    return datasetXslt;
  }

  /**
   * Transform a list of xmls using the latest default xslt stored.
   * <p>
   * This method can be used, for example, after a response from
   * {@link ProxiesService#getListOfFileContentsFromPluginExecution(MetisUser, String, PluginType, String, int)}
   * to try a transformation on a list of xmls just after validation external to preview an example
   * result.
   * </p>
   *
   * @param metisUser the {@link MetisUser} to authorize with
   * @param datasetId the dataset identifier, it is required for authentication and for the dataset
   * fields xslt injection
   * @param records the list of {@link Record} for which {@link Record#getXmlRecord()} returns a
   * non-null value
   * @return a list of {@link Record}s with {@link Record#getXmlRecord()} returning the transformed
   * XML
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the authorization header is un-parsable or the
   * user cannot be authorized.</li>
   * <li>{@link NoDatasetFoundException} if the dataset was not found.</li>
   * <li>{@link NoXsltFoundException} if there is no xslt found</li>
   * <li>{@link XsltSetupException} if the XSL transform could not be set up</li>
   * </ul>
   */
  public List<Record> transformRecordsUsingLatestDefaultXslt(MetisUser metisUser, String datasetId,
      List<Record> records) throws GenericMetisException {
    //Used for authentication and dataset existence
    Dataset dataset = authorizer.authorizeWriteExistingDatasetById(metisUser, datasetId);

    //Using default dataset identifier
    DatasetXslt datasetXslt = datasetXsltDao
        .getLatestXsltForDatasetId(DatasetXsltDao.DEFAULT_DATASET_ID);
    if (datasetXslt == null) {
      throw new NoXsltFoundException("Could not find default xslt");
    }

    String xsltUrl;
    synchronized (this) {
      xsltUrl = metisCoreUrl + RestEndpoints
          .resolve(RestEndpoints.DATASETS_XSLT_XSLTID, datasetXslt.getId().toString());
    }

    return transformRecords(dataset, records, xsltUrl);
  }

  /**
   * Transform a list of xmls using the latest dataset xslt stored.
   * <p>
   * This method can be used, for example, after a response from
   * {@link ProxiesService#getListOfFileContentsFromPluginExecution(MetisUser, String, PluginType, String, int)}
   * to try a transformation on a list of xmls just after validation external to preview an example
   * result.
   * </p>
   *
   * @param metisUser the {@link MetisUser} to authorize with
   * @param datasetId the dataset identifier, it is required for authentication and for the dataset
   * fields xslt injection
   * @param records the list of {@link Record} for which {@link Record#getXmlRecord()} returns a
   * non-null value
   * @return a list of {@link Record}s with {@link Record#getXmlRecord()} returning the transformed
   * XML
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the authorization header is un-parsable or the
   * user cannot be authorized.</li>
   * <li>{@link NoDatasetFoundException} if the dataset was not found.</li>
   * <li>{@link NoXsltFoundException} if there is no xslt found</li>
   * <li>{@link XsltSetupException} if the XSL transform could not be set up</li>
   * </ul>
   */
  public List<Record> transformRecordsUsingLatestDatasetXslt(MetisUser metisUser, String datasetId,
      List<Record> records) throws GenericMetisException {
    //Used for authentication and dataset existence
    Dataset dataset = authorizer.authorizeWriteExistingDatasetById(metisUser, datasetId);
    if (dataset.getXsltId() == null) {
      throw new NoXsltFoundException(
          String.format("Could not find xslt for datasetId %s", datasetId));
    }
    DatasetXslt datasetXslt = datasetXsltDao.getById(dataset.getXsltId().toString());

    String xsltUrl;
    synchronized (this) {
      xsltUrl = metisCoreUrl + RestEndpoints
          .resolve(RestEndpoints.DATASETS_XSLT_XSLTID, datasetXslt.getId().toString());
    }

    return transformRecords(dataset, records, xsltUrl);
  }

  private List<Record> transformRecords(Dataset dataset, List<Record> records, String xsltUrl)
      throws XsltSetupException {

    // Set up transformer.
    final XsltTransformer transformer;
    final EuropeanaIdCreator europeanIdCreator;
    try {
      transformer = new XsltTransformer(xsltUrl, dataset.getDatasetName(),
          dataset.getCountry().getName(), dataset.getLanguage().name());
      europeanIdCreator = new EuropeanaIdCreator();
    } catch (TransformationException e) {
      LOGGER.info("Transformation setup failed.", e);
      throw new XsltSetupException("Could not setup XSL transformation.", e);
    } catch (EuropeanaIdException e) {
      LOGGER.info(CommonStringValues.EUROPEANA_ID_CREATOR_INITIALIZATION_FAILED, e);
      throw new XsltSetupException(CommonStringValues.EUROPEANA_ID_CREATOR_INITIALIZATION_FAILED, e);
    }

    // Transform the records.
    return records.stream().map(record -> {
      try {
        EuropeanaGeneratedIdsMap europeanaGeneratedIdsMap = europeanIdCreator
            .constructEuropeanaId(record.getXmlRecord(), dataset.getDatasetId());
        return new Record(record.getEcloudId(), transformer
            .transform(record.getXmlRecord().getBytes(StandardCharsets.UTF_8),
                europeanaGeneratedIdsMap).toString());
      } catch (TransformationException e) {
        LOGGER.info("Record from list failed transformation", e);
        return new Record(record.getEcloudId(), e.getMessage());
      } catch (EuropeanaIdException e) {
        LOGGER.info(CommonStringValues.EUROPEANA_ID_CREATOR_INITIALIZATION_FAILED, e);
        return new Record(record.getEcloudId(), e.getMessage());
      }
    }).collect(Collectors.toList());
  }

  /**
   * Get all datasets using the provider field.
   *
   * @param metisUser the {@link MetisUser} to authorize with
   * @param provider the provider string used to find the datasets
   * @param nextPage the nextPage token or -1
   * @return {@link List} of {@link Dataset}
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized</li>
   * </ul>
   */
  public List<Dataset> getAllDatasetsByProvider(
      MetisUser metisUser, String provider, int nextPage)
      throws GenericMetisException {
    authorizer.authorizeReadAllDatasets(metisUser);
    return datasetDao.getAllDatasetsByProvider(provider, nextPage);
  }

  /**
   * Get all datasets using the intermediateProvider field.
   *
   * @param metisUser the {@link MetisUser} to authorize with
   * @param intermediateProvider the intermediateProvider string used to find the datasets
   * @param nextPage the nextPage token or -1
   * @return {@link List} of {@link Dataset}
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized</li>
   * </ul>
   */
  public List<Dataset> getAllDatasetsByIntermediateProvider(
      MetisUser metisUser, String intermediateProvider,
      int nextPage) throws GenericMetisException {
    authorizer.authorizeReadAllDatasets(metisUser);
    return datasetDao.getAllDatasetsByIntermediateProvider(intermediateProvider, nextPage);
  }

  /**
   * Get all datasets using the dataProvider field.
   *
   * @param metisUser the {@link MetisUser} to authorize with
   * @param dataProvider the dataProvider string used to find the datasets
   * @param nextPage the nextPage token or -1
   * @return {@link List} of {@link Dataset}
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized</li>
   * </ul>
   */
  public List<Dataset> getAllDatasetsByDataProvider(
      MetisUser metisUser, String dataProvider,
      int nextPage) throws GenericMetisException {
    authorizer.authorizeReadAllDatasets(metisUser);
    return datasetDao.getAllDatasetsByDataProvider(dataProvider, nextPage);
  }

  /**
   * Get all datasets using the organizationId field.
   *
   * @param metisUser the {@link MetisUser} to authorize with
   * @param organizationId the organizationId string used to find the datasets
   * @param nextPage the nextPage number or -1
   * @return {@link List} of {@link Dataset}
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized</li>
   * </ul>
   */
  public List<Dataset> getAllDatasetsByOrganizationId(
      MetisUser metisUser, String organizationId, int nextPage)
      throws GenericMetisException {
    authorizer.authorizeReadAllDatasets(metisUser);
    return datasetDao.getAllDatasetsByOrganizationId(organizationId, nextPage);
  }

  /**
   * Get all datasets using the organizationName field.
   *
   * @param metisUser the {@link MetisUser} to authorize with
   * @param organizationName the organizationName string used to find the datasets
   * @param nextPage the nextPage number or -1
   * @return {@link List} of {@link Dataset}
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized</li>
   * </ul>
   */
  public List<Dataset> getAllDatasetsByOrganizationName(
      MetisUser metisUser, String organizationName, int nextPage)
      throws GenericMetisException {
    authorizer.authorizeReadAllDatasets(metisUser);
    return datasetDao.getAllDatasetsByOrganizationName(organizationName, nextPage);
  }

  public int getDatasetsPerRequestLimit() {
    return datasetDao.getDatasetsPerRequest();
  }

  public void setMetisCoreUrl(String metisCoreUrl) {
    synchronized (this) {
      this.metisCoreUrl = metisCoreUrl;
    }
  }
}
