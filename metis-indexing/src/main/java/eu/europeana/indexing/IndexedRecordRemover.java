package eu.europeana.indexing;

import java.io.IOException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.mongodb.morphia.query.Query;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;
import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.indexing.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.solr.EdmLabel;

/**
 * This class provides functionality for removing records that are already indexed from the Mongo
 * and the Solr data stores.
 */
public class IndexedRecordRemover {

  private final EdmMongoServer mongoServer;
  private final SolrClient solrServer;

  /**
   * Constructor.
   * 
   * @param mongoServer The Mongo server connection.
   * @param solrServer The Solr server connection.
   */
  IndexedRecordRemover(EdmMongoServer mongoServer, SolrClient solrServer) {
    this.mongoServer = mongoServer;
    this.solrServer = solrServer;
  }

  /**
   * Removes all records that belong to a given dataset. This method also removes the associated
   * objects (i.e. those objects that are always part of only one record and the removal of which
   * can not invalidate references from other records):
   * <ul>
   * <li>Aggregation</li>
   * <li>EuropeanaAggregation</li>
   * <li>ProvidedCHO</li>
   * <li>Proxy</li>
   * </ul>
   * This does not remove any records that are potentially shared (like web resources, places,
   * concepts etc.).
   * 
   * @param datasetId The ID of the dataset to clear. Is not null.
   * @return The number of records that were removed.
   * @throws IndexerRelatedIndexingException In case something went wrong.
   */
  public int removeDataset(String datasetId) throws IndexerRelatedIndexingException {
    final int mongoCount;
    try {
      mongoCount = removeDatasetFromMongo(datasetId);
      final String queryValue = ClientUtils.escapeQueryChars(datasetId + "_") + "*";
      solrServer.deleteByQuery(EdmLabel.EUROPEANA_COLLECTIONNAME.toString() + ":" + queryValue);
    } catch (SolrServerException | IOException | RuntimeException e) {
      throw new IndexerRelatedIndexingException(
          "Could not remove dataset with ID '" + datasetId + "'.", e);
    }
    return mongoCount;
  }

  private int removeDatasetFromMongo(String datasetId) {

    // First remove the records
    final int result = removeDocumentsFromMongo(FullBeanImpl.class, "/" + datasetId + "/");

    // Then remove the private properties
    removeDocumentsFromMongo(AggregationImpl.class, "/aggregation/provider/" + datasetId + "/");
    removeDocumentsFromMongo(EuropeanaAggregationImpl.class,
        "/aggregation/europeana/" + datasetId + "/");
    removeDocumentsFromMongo(ProvidedCHOImpl.class, "/" + datasetId + "/");
    removeDocumentsFromMongo(ProxyImpl.class, "/proxy/provider/" + datasetId + "/");
    removeDocumentsFromMongo(ProxyImpl.class, "/proxy/europeana/" + datasetId + "/");

    // Done
    return result;
  }

  private int removeDocumentsFromMongo(Class<?> documentType, String aboutPrefix) {
    final Query<?> query =
        mongoServer.getDatastore().find(documentType).field("about").startsWith(aboutPrefix);
    return mongoServer.getDatastore().delete(query).getN();
  }
}
