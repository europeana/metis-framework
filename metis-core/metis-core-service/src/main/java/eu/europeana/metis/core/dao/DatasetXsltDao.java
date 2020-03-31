package eu.europeana.metis.core.dao;

import static eu.europeana.metis.core.common.DaoFieldNames.DATASET_ID;
import static eu.europeana.metis.core.common.DaoFieldNames.ID;

import com.mongodb.WriteResult;
import dev.morphia.Key;
import dev.morphia.query.Query;
import dev.morphia.query.Sort;
import eu.europeana.metis.core.dataset.DatasetXslt;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.utils.ExternalRequestUtil;
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
    Key<DatasetXslt> datasetKey = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(
            () -> morphiaDatastoreProvider.getDatastore().save(datasetXslt));
    LOGGER.debug("DatasetXslt for datasetId: '{}'created in Mongo", datasetXslt.getDatasetId());
    return datasetKey == null ? null : datasetKey.getId().toString();
  }

  @Override
  public String update(DatasetXslt datasetXslt) {
    Key<DatasetXslt> datasetKey = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(
            () -> morphiaDatastoreProvider.getDatastore().save(datasetXslt));
    LOGGER.debug("DatasetXslt for datasetId: '{}' updated in Mongo", datasetXslt.getDatasetId());
    return datasetKey == null ? null : datasetKey.getId().toString();
  }

  @Override
  public DatasetXslt getById(String id) {
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(
        () -> morphiaDatastoreProvider.getDatastore().find(DatasetXslt.class)
            .filter(ID.getFieldName(), new ObjectId(id)).first());
  }

  @Override
  public boolean delete(DatasetXslt datasetXslt) {
    ExternalRequestUtil.retryableExternalRequestConnectionReset(
        () -> morphiaDatastoreProvider.getDatastore().delete(
            morphiaDatastoreProvider.getDatastore().createQuery(DatasetXslt.class)
                .field(ID.getFieldName())
                .equal(datasetXslt.getId())));
    LOGGER.debug("DatasetXslt with objectId: '{}', datasetId: '{}'deleted in Mongo",
        datasetXslt.getId(),
        datasetXslt.getDatasetId());
    return true;
  }

  /**
   * Delete All Xslts but using a dataset identifier.
   *
   * @param datasetId the dataset identifier
   * @return true if something was found and deleted or false
   */
  public boolean deleteAllByDatasetId(String datasetId) {
    Query<DatasetXslt> query = morphiaDatastoreProvider.getDatastore()
        .createQuery(DatasetXslt.class);
    query.field(DATASET_ID.getFieldName()).equal(datasetId);
    WriteResult delete = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(
            () -> morphiaDatastoreProvider.getDatastore().delete(query));
    LOGGER.debug("Xslts with datasetId: {}, deleted from Mongo", datasetId);
    return (delete == null ? 0 : delete.getN()) >= 1;
  }

  /**
   * Fet latest stored xslt using a dataset identifier.
   *
   * @param datasetId the dataset identifier
   * @return the {@link DatasetXslt} object
   */
  DatasetXslt getLatestXsltForDatasetId(String datasetId) {
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(
        () -> morphiaDatastoreProvider.getDatastore().find(DatasetXslt.class)
            .filter(DATASET_ID.getFieldName(), datasetId).order(Sort.descending("createdDate"))
            .first());
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
