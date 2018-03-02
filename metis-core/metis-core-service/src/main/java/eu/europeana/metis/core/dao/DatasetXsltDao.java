package eu.europeana.metis.core.dao;

import com.mongodb.WriteResult;
import eu.europeana.metis.core.dataset.DatasetXslt;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-02-27
 */
@Repository
public class DatasetXsltDao implements MetisDao<DatasetXslt, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatasetXsltDao.class);
  private static final String ID = "_id";
  private static final String DATASET_ID = "datasetId";
  public static final int DEFAULT_DATASET_ID = -1;

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
    Key<DatasetXslt> datasetKey = morphiaDatastoreProvider.getDatastore().save(datasetXslt);
    LOGGER.debug("DatasetXslt for datasetId: '{}'created in Mongo", datasetXslt.getDatasetId());
    return datasetKey.getId().toString();
  }

  @Override
  public String update(DatasetXslt datasetXslt) {
    Key<DatasetXslt> datasetKey = morphiaDatastoreProvider.getDatastore().save(datasetXslt);
    LOGGER.debug("DatasetXslt for datasetId: '{}' updated in Mongo", datasetXslt.getDatasetId());
    return datasetKey.getId().toString();
  }

  @Override
  public DatasetXslt getById(String id) {
    return morphiaDatastoreProvider.getDatastore().find(DatasetXslt.class)
        .filter(ID, new ObjectId(id)).get();
  }

  @Override
  public boolean delete(DatasetXslt datasetXslt) {
    morphiaDatastoreProvider.getDatastore().delete(
        morphiaDatastoreProvider.getDatastore().createQuery(DatasetXslt.class).field(ID)
            .equal(datasetXslt.getId()));
    LOGGER.debug("DatasetXslt with objectId: '{}', datasetId: '{}'deleted in Mongo", datasetXslt.getId(),
        datasetXslt.getDatasetId());
    return true;
  }

  /**
   * Delete All Xslts but using a dataset identifier.
   * @param datasetId the dataset identifier
   * @return true if something was found and deleted or false
   */
  public boolean deleteAllByDatasetId(int datasetId) {
    Query<DatasetXslt> query = morphiaDatastoreProvider.getDatastore()
        .createQuery(DatasetXslt.class);
    query.field(DATASET_ID).equal(datasetId);
    WriteResult delete = morphiaDatastoreProvider.getDatastore().delete(query);
    LOGGER.debug("Xslts with datasetId: {}, deleted from Mongo", datasetId);
    return delete.getN() >= 1;
  }

  /**
   * Fet latest stored xslt using a dataset identifier.
   * <p>Use -1 to get the default xslt that is not related to a dataset</p>
   * @param datasetId the dataset identifier
   * @return the {@link DatasetXslt} object
   */
  public DatasetXslt getLatestXsltForDatasetId(int datasetId) {
    return morphiaDatastoreProvider.getDatastore().find(DatasetXslt.class)
        .filter(DATASET_ID, datasetId).order(Sort.descending("createdDate")).get();
  }
}
