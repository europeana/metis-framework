package eu.europeana.indexing;

import java.io.IOException;
import java.util.regex.Pattern;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.solr.EdmLabel;

public class DatasetRemover {

  private final EdmMongoServer mongoServer;
  private final SolrClient solrServer;

  /**
   * Constructor.
   * 
   * @param mongoServer The Mongo server connection.
   * @param solrServer The Solr server connection.
   */
  DatasetRemover(EdmMongoServer mongoServer, SolrClient solrServer) {
    this.mongoServer = mongoServer;
    this.solrServer = solrServer;
  }

  public void removeDataset(String datasetId) throws IndexingException {
    try {
      removeDatasetFromMongo(datasetId);
      solrServer
          .deleteByQuery(EdmLabel.EUROPEANA_COLLECTIONNAME.toString() + ":" + datasetId + "_*");
    } catch (SolrServerException | IOException | RuntimeException e) {
      throw new IndexingException("Could not remove dataset with ID '" + datasetId + "'.", e);
    }
  }

  private void removeDatasetFromMongo(String datasetId) {

    // TODO JOCHEN use generic query building (with class instances) instead of string collection
    // names.
    // TODO JOCHEN create some string constants
    DBCollection records = mongoServer.getDatastore().getDB().getCollection("record");
    DBCollection proxies = mongoServer.getDatastore().getDB().getCollection("Proxy");
    DBCollection physicalThing = mongoServer.getDatastore().getDB().getCollection("PhysicalThing");
    DBCollection providedCHOs = mongoServer.getDatastore().getDB().getCollection("ProvidedCHO");
    DBCollection aggregations = mongoServer.getDatastore().getDB().getCollection("Aggregation");
    DBCollection europeanaAggregations =
        mongoServer.getDatastore().getDB().getCollection("EuropeanaAggregation");
    DBObject query = new BasicDBObject("about", Pattern.compile("^/" + datasetId + "/"));
    DBObject proxyQuery =
        new BasicDBObject("about", Pattern.compile("^/proxy/provider/" + datasetId + "/"));
    DBObject europeanaProxyQuery =
        new BasicDBObject("about", Pattern.compile("^/proxy/europeana/" + datasetId + "/"));

    DBObject providedCHOQuery = new BasicDBObject("about", Pattern.compile("^/" + datasetId + "/"));
    DBObject aggregationQuery =
        new BasicDBObject("about", Pattern.compile("^/aggregation/provider/" + datasetId + "/"));
    DBObject europeanaAggregationQuery =
        new BasicDBObject("about", Pattern.compile("^/aggregation/europeana/" + datasetId + "/"));

    // TODO JOCHEN what about other collections (place, timespan, etc.)?
    // TODO JOCHEN should we check which objects are still used by other beans?
    // TODO JOCHEN what does this write concern do and do we want it? Change to WriteConcern.W2 (I
    // think).
    // TODO JOCHEN remove physicalThing as that doesn't exist anymore. Other removals/additions?
    // What about WebResources?
    // TODO JOCHEN Write concern still valid? Why wait for two replicas? Maybe we don't have them?
    europeanaAggregations.remove(europeanaAggregationQuery, WriteConcern.REPLICAS_SAFE);
    records.remove(query, WriteConcern.REPLICAS_SAFE);
    proxies.remove(europeanaProxyQuery, WriteConcern.REPLICAS_SAFE);
    proxies.remove(proxyQuery, WriteConcern.REPLICAS_SAFE);
    physicalThing.remove(proxyQuery, WriteConcern.REPLICAS_SAFE);
    physicalThing.remove(europeanaProxyQuery, WriteConcern.REPLICAS_SAFE);
    providedCHOs.remove(providedCHOQuery, WriteConcern.REPLICAS_SAFE);
    aggregations.remove(aggregationQuery, WriteConcern.REPLICAS_SAFE);
  }
}
