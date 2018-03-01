package eu.europeana.metis.core.dao;

import com.mongodb.WriteResult;
import eu.europeana.metis.core.dataset.Xslt;
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
public class XsltsDao implements MetisDao<Xslt, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(XsltsDao.class);
  private static final String DATASET_ID = "datasetId";

  private final MorphiaDatastoreProvider morphiaDatastoreProvider;

  /**
   * Constructs the DAO
   *
   * @param morphiaDatastoreProvider {@link MorphiaDatastoreProvider} used to access Mongo
   */
  @Autowired
  public XsltsDao(MorphiaDatastoreProvider morphiaDatastoreProvider) {
    this.morphiaDatastoreProvider = morphiaDatastoreProvider;
  }

  @Override
  public String create(Xslt xslt) {
    Key<Xslt> datasetKey = morphiaDatastoreProvider.getDatastore().save(xslt);
    LOGGER.debug("Xslt for datasetId: '{}'created in Mongo", xslt.getDatasetId());
    return datasetKey.getId().toString();
  }

  @Override
  public String update(Xslt xslt) {
    Key<Xslt> datasetKey = morphiaDatastoreProvider.getDatastore().save(xslt);
    LOGGER.debug("Xslt for datasetId: '{}' updated in Mongo", xslt.getDatasetId());
    return datasetKey.getId().toString();
  }

  @Override
  public Xslt getById(String id) {
    return morphiaDatastoreProvider.getDatastore().find(Xslt.class)
        .filter("_id", new ObjectId(id)).get();
  }

  @Override
  public boolean delete(Xslt xslt) {
    morphiaDatastoreProvider.getDatastore().delete(
        morphiaDatastoreProvider.getDatastore().createQuery(Xslt.class).field("_id")
            .equal(xslt.getId()));
    LOGGER.debug("Xslt with objectId: '{}', datasetId: '{}'deleted in Mongo", xslt.getId(),
        xslt.getDatasetId());
    return true;
  }

  public boolean deleteAllByDatasetId(int datasetId) {
    Query<Xslt> query = morphiaDatastoreProvider.getDatastore()
        .createQuery(Xslt.class);
    query.field(DATASET_ID).equal(datasetId);
    WriteResult delete = morphiaDatastoreProvider.getDatastore().delete(query);
    LOGGER.debug("Xslts with datasetId: {}, deleted from Mongo", datasetId);
    return delete.getN() >= 1;
  }

  public Xslt getLatestDefaultXslt() {
    return morphiaDatastoreProvider.getDatastore().find(Xslt.class)
        .filter("datasetId", -1).order(Sort.descending("createdDate")).get();
  }
}
