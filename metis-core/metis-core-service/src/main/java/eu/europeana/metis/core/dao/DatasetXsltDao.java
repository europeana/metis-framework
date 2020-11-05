package eu.europeana.metis.core.dao;

import static eu.europeana.metis.core.common.DaoFieldNames.DATASET_ID;
import static eu.europeana.metis.core.common.DaoFieldNames.ID;
import static eu.europeana.metis.network.ExternalRequestUtil.retryableExternalRequestForNetworkExceptions;

import com.mongodb.client.result.DeleteResult;
import dev.morphia.DeleteOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.Sort;
import dev.morphia.query.experimental.filters.Filters;
import eu.europeana.metis.core.dataset.DatasetXslt;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Dataset Access Object for xslts using Mongo
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-02-27
 */
@Repository
public class DatasetXsltDao implements MetisDao<DatasetXslt, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatasetXsltDao.class);

  private final MorphiaDatastoreProvider morphiaDatastoreProvider;

  /**
   * Constructs the DAO
   *
   * @param morphiaDatastoreProvider {@link MorphiaDatastoreProvider} used to access Mongo
   */
  @Autowired
  public DatasetXsltDao(MorphiaDatastoreProvider morphiaDatastoreProvider) {
    this.morphiaDatastoreProvider = morphiaDatastoreProvider;
  }

  @Override
  public String create(DatasetXslt datasetXslt) {
    final ObjectId objectId = Optional.ofNullable(datasetXslt.getId()).orElseGet(ObjectId::new);
    datasetXslt.setId(objectId);
    DatasetXslt datasetSaved = retryableExternalRequestForNetworkExceptions(
        () -> morphiaDatastoreProvider.getDatastore().save(datasetXslt));
    LOGGER.debug("DatasetXslt for datasetId: '{}'created in Mongo", datasetXslt.getDatasetId());
    return datasetSaved == null ? null : datasetSaved.getId().toString();
  }

  @Override
  public String update(DatasetXslt datasetXslt) {
    DatasetXslt datasetXsltSaved = retryableExternalRequestForNetworkExceptions(
        () -> morphiaDatastoreProvider.getDatastore().save(datasetXslt));
    LOGGER.debug("DatasetXslt for datasetId: '{}' updated in Mongo", datasetXslt.getDatasetId());
    return datasetXsltSaved == null ? null : datasetXsltSaved.getId().toString();
  }

  @Override
  public DatasetXslt getById(String id) {
    return retryableExternalRequestForNetworkExceptions(
        () -> morphiaDatastoreProvider.getDatastore().find(DatasetXslt.class)
            .filter(Filters.eq(ID.getFieldName(), new ObjectId(id))).first());
  }

  @Override
  public boolean delete(DatasetXslt datasetXslt) {
    retryableExternalRequestForNetworkExceptions(
        () -> morphiaDatastoreProvider.getDatastore().find(DatasetXslt.class)
            .filter(Filters.eq(ID.getFieldName(), datasetXslt.getId())).delete());
    LOGGER.debug("DatasetXslt with objectId: '{}', datasetId: '{}'deleted in Mongo",
        datasetXslt.getId(), datasetXslt.getDatasetId());
    return true;
  }

  /**
   * Delete All Xslts but using a dataset identifier.
   *
   * @param datasetId the dataset identifier
   * @return true if something was found and deleted or false
   */
  public boolean deleteAllByDatasetId(String datasetId) {
    Query<DatasetXslt> query = morphiaDatastoreProvider.getDatastore().find(DatasetXslt.class);
    query.filter(Filters.eq(DATASET_ID.getFieldName(), datasetId));
    DeleteResult deleteResult = retryableExternalRequestForNetworkExceptions(
        () -> query.delete(new DeleteOptions().multi(true)));
    LOGGER.debug("Xslts with datasetId: {}, deleted from Mongo", datasetId);
    return (deleteResult == null ? 0 : deleteResult.getDeletedCount()) >= 1;
  }

  /**
   * Fet latest stored xslt using a dataset identifier.
   *
   * @param datasetId the dataset identifier
   * @return the {@link DatasetXslt} object
   */
  DatasetXslt getLatestXsltForDatasetId(String datasetId) {
    return retryableExternalRequestForNetworkExceptions(
        () -> morphiaDatastoreProvider.getDatastore().find(DatasetXslt.class)
            .filter(Filters.eq(DATASET_ID.getFieldName(), datasetId))
            .first(new FindOptions().sort(Sort.descending("createdDate"))));
  }

  /**
   * Fet latest stored default xslt.
   *
   * @return the {@link DatasetXslt} object
   */
  public DatasetXslt getLatestDefaultXslt() {
    return getLatestXsltForDatasetId(DatasetXslt.DEFAULT_DATASET_ID);
  }
}
