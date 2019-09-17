package eu.europeana.indexing;

import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.solr.EdmLabel;
import eu.europeana.metis.CommonStringValues;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

/**
 * <p>
 * This class provides functionality for removing records that are already indexed from the Mongo
 * and the Solr data stores.
 * </p>
 * <p>
 * When removing entities from the Mongo database, it also removes any associated entities (i.e.
 * those entities that are always part of only one record and the removal of which can not
 * invalidate references from other records):
 * <ul>
 * <li>Aggregation</li>
 * <li>EuropeanaAggregation</li>
 * <li>ProvidedCHO</li>
 * <li>Proxy (both provider and Europeana proxies)</li>
 * </ul>
 * However, entities that are potentially shared (like web resources, places, concepts etc.) are not
 * removed.
 * </p>
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
   * Removes the record with the given rdf:about value.
   *
   * @param rdfAbout The about value of the record to remove. Is not null.
   * @return Whether or not the record was removed.
   * @throws IndexerRelatedIndexingException In case something went wrong.
   */
  public boolean removeRecord(String rdfAbout) throws IndexerRelatedIndexingException {
    try {

      // Remove Solr record
      final String queryValue = ClientUtils.escapeQueryChars(rdfAbout);
      solrServer.deleteByQuery(EdmLabel.EUROPEANA_ID.toString() + ":" + queryValue);

      // Obtain the Mongo record
      final Datastore datastore = mongoServer.getDatastore();
      final FullBeanImpl recordToDelete = datastore.find(FullBeanImpl.class).field("about")
          .equal(rdfAbout).get();

      // Remove mongo record and dependencies
      if (recordToDelete != null) {
        datastore.delete(recordToDelete);
        recordToDelete.getAggregations().forEach(datastore::delete);
        datastore.delete(recordToDelete.getEuropeanaAggregation());
        recordToDelete.getProvidedCHOs().forEach(datastore::delete);
        recordToDelete.getProxies().forEach(datastore::delete);
      }

      // Done
      return recordToDelete != null;

    } catch (SolrServerException | IOException | RuntimeException e) {
      throw new IndexerRelatedIndexingException("Could not remove record '" + rdfAbout + "'.", e);
    }
  }

  /**
   * Removes all records that belong to a given dataset. <b>NOTE</b> that the rdf:about is used to
   * find the dependencies, rather than the actual references in the records. While this is a
   * reasonably safe way to go for now, eventually a more generic way along the lines of {@link
   * #removeRecord(String)} should be found, in which the exact composition of the rdf:about is
   * taken out of the equation.
   *
   * @param datasetId The ID of the dataset to clear. Is not null.
   * @param maxRecordDate The date that all records that have lower timestampUpdated than that date
   * would be removed. If null is provided then all records from that dataset will be removed.
   * @return The number of records that were removed.
   * @throws IndexerRelatedIndexingException In case something went wrong.
   */
  public int removeDataset(String datasetId, Date maxRecordDate)
      throws IndexerRelatedIndexingException {
    final int mongoCount;
    try {
      mongoCount = removeDatasetFromMongo(datasetId, maxRecordDate);
      removeDatasetFromSolr(datasetId, maxRecordDate);
    } catch (SolrServerException | IOException | RuntimeException e) {
      throw new IndexerRelatedIndexingException(
          "Could not remove dataset with ID '" + datasetId + "'.", e);
    }
    return mongoCount;
  }

  private void removeDatasetFromSolr(String datasetId, Date recordDate)
      throws SolrServerException, IOException {
    final StringBuilder solrQuery = new StringBuilder();

    final String datasetIdRegexEscaped = ClientUtils.escapeQueryChars(datasetId + "_") + "*";
    solrQuery.append(EdmLabel.EUROPEANA_COLLECTIONNAME.toString()).append(":")
        .append(datasetIdRegexEscaped);

    if (recordDate != null) {
      DateFormat dateFormat = new SimpleDateFormat(CommonStringValues.DATE_FORMAT, Locale.US);
      solrQuery.append(" AND ").append(EdmLabel.TIMESTAMP_UPDATED.toString()).append(":")
          .append("[* TO ").append(dateFormat.format(recordDate)).append("}");
    }
    solrServer.deleteByQuery(solrQuery.toString());
  }

  private int removeDatasetFromMongo(String datasetId, Date recordDate) {
    return removeDocumentsFromMongo(FullBeanImpl.class, "/" + datasetId + "/",
        recordDate);
  }

  private int removeDocumentsFromMongo(Class<?> documentType, String aboutPrefix, Date recordDate) {
    final Query<?> query = mongoServer.getDatastore().createQuery(documentType);
    query.field("about").startsWith(aboutPrefix);
    if (recordDate != null) {
      query.field("timestampUpdated").lessThan(recordDate);
    }
    return mongoServer.getDatastore().delete(query).getN();
  }
}
