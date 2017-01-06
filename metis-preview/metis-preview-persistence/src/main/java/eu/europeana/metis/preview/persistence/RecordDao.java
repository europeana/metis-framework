package eu.europeana.metis.preview.persistence;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.WriteConcern;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.entity.Aggregation;
import eu.europeana.corelib.definitions.edm.entity.EuropeanaAggregation;
import eu.europeana.corelib.definitions.edm.entity.ProvidedCHO;
import eu.europeana.corelib.definitions.edm.entity.Proxy;
import eu.europeana.corelib.edm.utils.construct.FullBeanHandler;
import eu.europeana.corelib.edm.utils.construct.SolrDocumentHandler;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Record persistence DAO
 * Created by ymamakis on 9/2/16.
 */
public class RecordDao {

    @Autowired
    private FullBeanHandler beanHandler;

    @Autowired
    private SolrDocumentHandler solrDocumentHandler;

    @Autowired
    private SolrServer server;

    @Autowired
    private EdmMongoServer mongoServer;

    /**
     * Persist a record in mongo and solr
     * @param fBean The record
     * @throws SolrServerException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public void createRecord(FullBean fBean) throws SolrServerException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
        SolrInputDocument doc =solrDocumentHandler.generate((FullBeanImpl) fBean);
        doc.setField("europeana_id",fBean.getAbout());

        server.add(doc);
        FullBean fixed = fixIdentifiers(fBean);
        beanHandler.saveEdmClasses((FullBeanImpl)fixed,true);
        mongoServer.getDatastore().save(fixed);

    }

    private FullBean fixIdentifiers(FullBean fBean) {

        Aggregation aggr = fBean.getAggregations().get(0);
        aggr.setAbout("/aggregation/provider"+fBean.getAbout());
        List<Aggregation> aggregations = new ArrayList<>();
        aggregations.add(aggr);
        fBean.setAggregations(aggregations);
        ProvidedCHO cho = fBean.getProvidedCHOs().get(0);
        List<ProvidedCHO> chos = new ArrayList<>();
        cho.setAbout("/item"+fBean.getAbout());
        chos.add(cho);
        fBean.setProvidedCHOs(chos);
        EuropeanaAggregation euAggr = fBean.getEuropeanaAggregation();
        euAggr.setAbout("/aggregation/europeana"+fBean.getAbout());
        fBean.setEuropeanaAggregation(euAggr);
        List<Proxy> proxies = new ArrayList<>();
        List<ProxyImpl> beanProxies = (List<ProxyImpl>)fBean.getProxies();
        for(Proxy proxy:beanProxies) {
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
     * @param recordId The id of the record to remove
     */
    public void deleteRecord(String recordId){
        beanHandler.removeRecordById(server,recordId);
    }

    public void deleteCollection(String collectionName) throws IOException, SolrServerException {
        server.deleteByQuery("edm_datasetName:"+collectionName+"*");
        clearData(collectionName);
    }

    public void commit() throws IOException, SolrServerException {
        server.commit();
    }
    /**
     * Create records
     * @param fBeans The records to create
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws SolrServerException
     */
    public void createRecords(List<FullBean> fBeans, String collectionName) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, SolrServerException, IOException {
        for(FullBean fBean:fBeans){
            createRecord(fBean);
        }
    }

    /**
     * Delete records using their ids
     * @param recordIds The ids of the records to remove
     */
    public void deleteRecords(List<String> recordIds){
        for (String recordId:recordIds){
            deleteRecord(recordId);
        }
    }

    /**
     * Delete the records persisted over the last 24h
     * @throws SolrServerException
     * @throws IOException
     */
    public void deleteRecordIdsByTimestamp() throws SolrServerException, IOException {
        ModifiableSolrParams params = new ModifiableSolrParams();
        params.add("q","*:*");
        server.deleteByQuery(params.toString());
        server.commit();
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
        DBCollection physicalThing = this.mongoServer.getDatastore().getDB().getCollection("PhysicalThing");
        DBCollection providedCHOs = this.mongoServer.getDatastore().getDB().getCollection("ProvidedCHO");
        DBCollection aggregations = this.mongoServer.getDatastore().getDB().getCollection("Aggregation");
        DBCollection europeanaAggregations = this.mongoServer.getDatastore().getDB().getCollection("EuropeanaAggregation");
        BasicDBObject query = new BasicDBObject("about", Pattern.compile("^/" + collection + "/"));
        BasicDBObject proxyQuery = new BasicDBObject("about", Pattern.compile("^/proxy/provider/" + collection + "/"));
        BasicDBObject europeanaProxyQuery = new BasicDBObject("about", Pattern.compile("^/proxy/europeana/" + collection + "/"));
        BasicDBObject providedCHOQuery = new BasicDBObject("about", Pattern.compile("^/item/" + collection + "/"));
        BasicDBObject aggregationQuery = new BasicDBObject("about", Pattern.compile("^/aggregation/provider/" + collection + "/"));
        BasicDBObject europeanaAggregationQuery = new BasicDBObject("about", Pattern.compile("^/aggregation/europeana/" + collection + "/"));
        europeanaAggregations.remove(europeanaAggregationQuery, WriteConcern.FSYNC_SAFE);
        records.remove(query, WriteConcern.FSYNC_SAFE);
        proxies.remove(europeanaProxyQuery, WriteConcern.FSYNC_SAFE);
        proxies.remove(proxyQuery, WriteConcern.FSYNC_SAFE);
        physicalThing.remove(proxyQuery, WriteConcern.FSYNC_SAFE);
        physicalThing.remove(europeanaProxyQuery, WriteConcern.FSYNC_SAFE);
        providedCHOs.remove(providedCHOQuery, WriteConcern.FSYNC_SAFE);
        aggregations.remove(aggregationQuery, WriteConcern.FSYNC_SAFE);
    }
}
