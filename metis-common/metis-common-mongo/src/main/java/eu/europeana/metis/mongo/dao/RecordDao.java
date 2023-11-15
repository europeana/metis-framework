package eu.europeana.metis.mongo.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.Mapper;
import dev.morphia.mapping.MappingException;
import dev.morphia.query.filters.Filters;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.exceptions.MongoRuntimeException;
import eu.europeana.corelib.edm.model.metainfo.WebResourceMetaInfoImpl;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.derived.AttributionSnippet;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.BasicProxyImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.ConceptSchemeImpl;
import eu.europeana.corelib.solr.entity.DatasetImpl;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;
import eu.europeana.corelib.solr.entity.EventImpl;
import eu.europeana.corelib.solr.entity.LicenseImpl;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.corelib.solr.entity.PhysicalThingImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.solr.entity.QualityAnnotationImpl;
import eu.europeana.corelib.solr.entity.ServiceImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.corelib.solr.entity.WebResourceImpl;
import eu.europeana.corelib.web.exception.EuropeanaException;
import eu.europeana.corelib.web.exception.ProblemType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connection for accessing Europeana records.
 */
public class RecordDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(RecordDao.class);
  private final Datastore datastore;

  /**
   * Constructor to initialize the mongo mappings/collections and the {@link Datastore} connection.
   * This constructor is meant to be used when the database is already available.
   *
   * @param mongoClient the mongo client connection
   * @param databaseName the database name of the record redirect database
   */
  public RecordDao(MongoClient mongoClient, String databaseName) {
    this(mongoClient, databaseName, false);
  }

  /**
   * Constructor to initialize the mongo mappings/collections and the {@link Datastore} connection.
   * This constructor is meant to be used mostly for when the creation of the database is required.
   *
   * @param mongoClient the mongo client connection
   * @param databaseName the database name of the record redirect database
   * @param createIndexes flag that initiates the database/indices
   */
  public RecordDao(MongoClient mongoClient, String databaseName, boolean createIndexes) {
    this.datastore = createDatastore(mongoClient, databaseName);
    if (createIndexes) {
      LOGGER.info("Initializing database indices");
      datastore.ensureIndexes();
    }
  }

  private Datastore createDatastore(MongoClient mongoClient, String databaseName) {
    final Datastore morphiaDatastore = Morphia.createDatastore(mongoClient, databaseName);
    final Mapper mapper = morphiaDatastore.getMapper();
    mapper.getEntityModel(FullBeanImpl.class);
    mapper.getEntityModel(ProvidedCHOImpl.class);
    mapper.getEntityModel(AgentImpl.class);
    mapper.getEntityModel(AggregationImpl.class);
    mapper.getEntityModel(OrganizationImpl.class);
    mapper.getEntityModel(ConceptImpl.class);
    mapper.getEntityModel(ProxyImpl.class);
    mapper.getEntityModel(PlaceImpl.class);
    mapper.getEntityModel(TimespanImpl.class);
    mapper.getEntityModel(WebResourceImpl.class);
    mapper.getEntityModel(EuropeanaAggregationImpl.class);
    mapper.getEntityModel(EventImpl.class);
    mapper.getEntityModel(PhysicalThingImpl.class);
    mapper.getEntityModel(ConceptSchemeImpl.class);
    mapper.getEntityModel(BasicProxyImpl.class);
    mapper.getEntityModel(WebResourceMetaInfoImpl.class);
    mapper.getEntityModel(LicenseImpl.class);
    mapper.getEntityModel(ServiceImpl.class);
    mapper.getEntityModel(QualityAnnotationImpl.class);
    mapper.getEntityModel(AttributionSnippet.class);
    mapper.getEntityModel(DatasetImpl.class);
    LOGGER.info("Datastore initialized");

    return morphiaDatastore;
  }

  public Datastore getDatastore() {
    return this.datastore;
  }

  /**
   * Get a full bean using an identifier matching it's {@code about} field.
   *
   * @param id the identifier of the fullbean
   * @return the matched full bean
   * @throws EuropeanaException if anything when wrong with the request
   */
  public FullBean getFullBean(String id) throws EuropeanaException {
    try {
      long start = 0;
      if (LOGGER.isDebugEnabled()) {
        start = System.currentTimeMillis();
      }
      FullBeanImpl result = datastore.find(FullBeanImpl.class).filter(Filters.eq("about", id))
          .first();
      LOGGER.debug("Mongo query find fullbean {} finished in {} ms", id,
          (System.currentTimeMillis() - start));
      return result;
    } catch (RuntimeException re) {
      if (re.getCause() != null && (re.getCause() instanceof MappingException || re
          .getCause() instanceof ClassCastException)) {
        throw new MongoDBException(ProblemType.RECORD_RETRIEVAL_ERROR, re);
      } else {
        throw new MongoRuntimeException(ProblemType.MONGO_UNREACHABLE, re);
      }
    }
  }

  /**
   * Find Web resource metadata matches using a list of hash codes.
   *
   * @param hashCodes the hash codes
   * @return a map of the web resource metadata id and the metadata corresponding to that id
   */
  public Map<String, WebResourceMetaInfoImpl> retrieveWebMetaInfos(List<String> hashCodes) {
    Map<String, WebResourceMetaInfoImpl> metaInfos = new HashMap<>();

    final BasicDBObject basicObject = new BasicDBObject("$in", hashCodes);
    long start = 0;
    if (LOGGER.isDebugEnabled()) {
      start = System.currentTimeMillis();
    }
    List<WebResourceMetaInfoImpl> metaInfoList = getDatastore().find(WebResourceMetaInfoImpl.class)
        .disableValidation().filter(Filters.eq("_id", basicObject)).iterator().toList();
    LOGGER.debug("Mongo query find metainfo for {} webresources done in {} ms", hashCodes.size(),
        (System.currentTimeMillis() - start));

    start = System.currentTimeMillis();
    metaInfoList.forEach(cursor -> {
      String id = cursor.getId();
      metaInfos.put(id, cursor);
    });
    LOGGER.debug("Mongo cursor done in {} ms", (System.currentTimeMillis() - start));
    return metaInfos;

  }

  @Override
  public String toString() {
    return "{ datastore=" + datastore.getDatabase().getName() + " }";
  }

  /**
   * Get a document using a class type and an about value.
   *
   * @param clazz the class representing type
   * @param about the about value
   * @param <T> the type
   * @return the object found
   */
  public <T> T searchByAbout(Class<T> clazz, String about) {
    return datastore.find(clazz).filter(Filters.eq("about", about)).first();
  }
}
