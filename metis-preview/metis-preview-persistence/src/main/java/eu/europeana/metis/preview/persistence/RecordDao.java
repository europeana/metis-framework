package eu.europeana.metis.preview.persistence;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.WriteConcern;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.entity.Aggregation;
import eu.europeana.corelib.definitions.edm.entity.EuropeanaAggregation;
import eu.europeana.corelib.definitions.edm.entity.ProvidedCHO;
import eu.europeana.corelib.definitions.edm.entity.Proxy;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.exceptions.MongoRuntimeException;
import eu.europeana.corelib.edm.exceptions.MongoUpdateException;
import eu.europeana.corelib.edm.utils.construct.FullBeanHandler;
import eu.europeana.corelib.edm.utils.construct.SolrDocumentHandler;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;

/**
 * Record persistence DAO
 * Created by ymamakis on 9/2/16.
 */
@Service
public class RecordDao {

  public static final String ABOUT_STRING = "about";
  private final SolrServer solrServer;
  private final EdmMongoServer mongoServer;
  private final FullBeanHandler mongoBeanHandler;
  private final SolrDocumentHandler solrDocumentHandler;

  /**
   * Constructor with required fields.
   *
   * @param mongoBeanHandler {@link FullBeanHandler}
   * @param solrDocumentHandler {@link SolrDocumentHandler}
   * @param solrServer {@link SolrServer}
   * @param mongoServer {@link EdmMongoServer}
   */
  @Autowired
  public RecordDao(FullBeanHandler mongoBeanHandler,
      SolrDocumentHandler solrDocumentHandler, SolrServer solrServer,
      EdmMongoServer mongoServer) {
    this.mongoBeanHandler = mongoBeanHandler;
    this.solrDocumentHandler = solrDocumentHandler;
    this.solrServer = solrServer;
    this.mongoServer = mongoServer;
  }

  /**
   * Persist a record in mongo and solr
   *
   * @param fBean The record
 * @throws MongoUpdateException 
   */
  public void createRecord(FullBean fBean)
      throws SolrServerException, NoSuchMethodException, IllegalAccessException, InvocationTargetException,
      IOException, MongoDBException, MongoRuntimeException, MongoUpdateException {
    SolrInputDocument doc = solrDocumentHandler.generate((FullBeanImpl) fBean);
    doc.setField("europeana_id", fBean.getAbout());
    solrServer.add(doc);
    FullBean fixed = fixIdentifiers(fBean);

    mongoBeanHandler
        .saveEdmClasses((FullBeanImpl) fixed, mongoServer.getFullBean(fBean.getAbout()) == null);
    mongoServer.getDatastore().save(fixed);

  }

  private FullBean fixIdentifiers(FullBean fBean) {

    Aggregation aggr = fBean.getAggregations().get(0);
    aggr.setAbout("/aggregation/provider" + fBean.getAbout());
    List<Aggregation> aggregations = new ArrayList<>();
    aggregations.add(aggr);
    fBean.setAggregations(aggregations);
    ProvidedCHO cho = fBean.getProvidedCHOs().get(0);
    List<ProvidedCHO> chos = new ArrayList<>();
    cho.setAbout("/item" + fBean.getAbout());
    chos.add(cho);
    fBean.setProvidedCHOs(chos);
    EuropeanaAggregation euAggr = fBean.getEuropeanaAggregation();
    euAggr.setAbout("/aggregation/europeana" + fBean.getAbout());
    fBean.setEuropeanaAggregation(euAggr);
    List<Proxy> proxies = new ArrayList<>(fBean.getProxies().size());
    for (Proxy proxy : fBean.getProxies()) {
      if (proxy.isEuropeanaProxy()) {
        proxy.setAbout("/proxy/europeana" + fBean.getAbout());
      } else {
        proxy.setAbout("/proxy/provider" + fBean.getAbout());
      }

      proxies.add(proxy);
    }
    fBean.setProxies(proxies);
    return fBean;
  }


  /**
   * Delete a record using its id
   *
   * @param recordId The id of the record to remove
   */
  public void deleteRecord(String recordId) {
    mongoBeanHandler.removeRecordById(solrServer, recordId);
  }

  public void deleteCollection(String collectionName) throws IOException, SolrServerException {
    solrServer.deleteByQuery("edm_datasetName:" + collectionName + "*");
    clearData(collectionName);
  }

  public void commit() throws IOException, SolrServerException {
    solrServer.commit();
  }

  /**
   * Create records
   *
   * @param fBeans The records to create
 * @throws MongoUpdateException 
   */
  public void createRecords(List<FullBean> fBeans)
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, SolrServerException,
      IOException, MongoDBException, MongoRuntimeException, MongoUpdateException {
    for (FullBean fBean : fBeans) {
      createRecord(fBean);
    }
  }

  /**
   * Delete records using their ids
   *
   * @param recordIds The ids of the records to remove
   */
  public void deleteRecords(List<String> recordIds) {
    for (String recordId : recordIds) {
      deleteRecord(recordId);
    }
  }

  /**
   * Delete the records persisted over the last 24h
   */
  public void deleteRecordIdsByTimestamp() throws SolrServerException, IOException {
    SolrQuery query = new SolrQuery();
    query.setQuery("*:*");
    solrServer.deleteByQuery(query.getQuery());
    solrServer.commit();
    clearAll();
  }

  private void clearAll() {
    this.mongoServer.getDatastore().getDB().getCollection("record")
        .remove(new BasicDBObject());
    this.mongoServer.getDatastore().getDB().getCollection("Proxy")
        .remove(new BasicDBObject());
    this.mongoServer.getDatastore().getDB().getCollection("Aggregation")
        .remove(new BasicDBObject());
    this.mongoServer.getDatastore().getDB().getCollection("EuropeanaAggregation")
        .remove(new BasicDBObject());
    this.mongoServer.getDatastore().getDB().getCollection("PhysicalThing")
        .remove(new BasicDBObject());
    this.mongoServer.getDatastore().getDB().getCollection("Agent")
        .remove(new BasicDBObject());
    this.mongoServer.getDatastore().getDB().getCollection("Concept")
        .remove(new BasicDBObject());
    this.mongoServer.getDatastore().getDB().getCollection("Place")
        .remove(new BasicDBObject());
    this.mongoServer.getDatastore().getDB().getCollection("Timespan")
        .remove(new BasicDBObject());
    this.mongoServer.getDatastore().getDB().getCollection("WebResource")
        .remove(new BasicDBObject());
    this.mongoServer.getDatastore().getDB().getCollection("Service")
        .remove(new BasicDBObject());
    this.mongoServer.getDatastore().getDB().getCollection("License")
        .remove(new BasicDBObject());
  }

  private void clearData(String collection) {
    DBCollection records = this.mongoServer.getDatastore().getDB().getCollection("record");
    DBCollection proxies = this.mongoServer.getDatastore().getDB().getCollection("Proxy");
    DBCollection physicalThing = this.mongoServer.getDatastore().getDB()
        .getCollection("PhysicalThing");
    DBCollection providedCHOs = this.mongoServer.getDatastore().getDB()
        .getCollection("ProvidedCHO");
    DBCollection aggregations = this.mongoServer.getDatastore().getDB()
        .getCollection("Aggregation");
    DBCollection europeanaAggregations = this.mongoServer.getDatastore().getDB()
        .getCollection("EuropeanaAggregation");
    BasicDBObject query = new BasicDBObject(ABOUT_STRING, Pattern.compile("^/" + collection + "/"));
    BasicDBObject proxyQuery = new BasicDBObject(ABOUT_STRING,
        Pattern.compile("^/proxy/provider/" + collection + "/"));
    BasicDBObject europeanaProxyQuery = new BasicDBObject(ABOUT_STRING,
        Pattern.compile("^/proxy/europeana/" + collection + "/"));
    BasicDBObject providedCHOQuery = new BasicDBObject(ABOUT_STRING,
        Pattern.compile("^/item/" + collection + "/"));
    BasicDBObject aggregationQuery = new BasicDBObject(ABOUT_STRING,
        Pattern.compile("^/aggregation/provider/" + collection + "/"));
    BasicDBObject europeanaAggregationQuery = new BasicDBObject(ABOUT_STRING,
        Pattern.compile("^/aggregation/europeana/" + collection + "/"));
    europeanaAggregations.remove(europeanaAggregationQuery, WriteConcern.JOURNALED);
    records.remove(query, WriteConcern.JOURNALED);
    proxies.remove(europeanaProxyQuery, WriteConcern.JOURNALED);
    proxies.remove(proxyQuery, WriteConcern.JOURNALED);
    physicalThing.remove(proxyQuery, WriteConcern.JOURNALED);
    physicalThing.remove(europeanaProxyQuery, WriteConcern.JOURNALED);
    providedCHOs.remove(providedCHOQuery, WriteConcern.JOURNALED);
    aggregations.remove(aggregationQuery, WriteConcern.JOURNALED);
  }
}
