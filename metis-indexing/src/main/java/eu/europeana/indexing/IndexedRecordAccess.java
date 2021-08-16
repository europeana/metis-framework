package eu.europeana.indexing;

import dev.morphia.Datastore;
import dev.morphia.DeleteOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filters;
import eu.europeana.metis.mongo.dao.RecordDao;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.solr.EdmLabel;
import eu.europeana.metis.utils.CommonStringValues;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Spliterators;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.util.ClientUtils;

/**
 * This class provides functionality for accessing records that are already indexed from the Mongo
 * and the Solr data stores. Note that this class does <b>NOT</b> contain functionality for indexing
 * records.
 */
public class IndexedRecordAccess {

  private static final String ID_FIELD = "_id";
  private static final String ABOUT_FIELD = "about";

  private final RecordDao mongoServer;
  private final SolrClient solrServer;

  /**
   * Constructor.
   *
   * @param mongoServer The Mongo server connection.
   * @param solrServer The Solr server connection.
   */
  IndexedRecordAccess(RecordDao mongoServer, SolrClient solrServer) {
    this.mongoServer = mongoServer;
    this.solrServer = solrServer;
  }

  /**
   * Counts the records in a given dataset. The criteria of whether a record belongs to a certain
   * dataset is the same as that used in the method {@link #removeDataset(String, Date)}, i.e. it is
   * based on the <code>rdf:about</code> values.
   *
   * @param datasetId The ID of the dataset of which to count the records. Is not null.
   * @return The number of records encountered for the given dataset.
   */
  public long countRecords(String datasetId) {
    return countRecords(datasetId, null);
  }

  /**
   * Removes the record with the given rdf:about value. Also removes any associated entities (i.e.
   * those entities that are always part of only one record and the removal of which can not
   * invalidate references from other records):
   * <ul>
   * <li>Aggregation</li>
   * <li>EuropeanaAggregation</li>
   * <li>ProvidedCHO</li>
   * <li>Proxy (both provider and Europeana proxies)</li>
   * </ul>
   * However, entities that are potentially shared (like web resources, places, concepts etc.) are
   * not removed.
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
      final FullBeanImpl recordToDelete = datastore.find(FullBeanImpl.class)
          .filter(Filters.eq(ABOUT_FIELD, rdfAbout)).first();

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
   * <p>Removes all records that belong to a given dataset. For details on what parts of the record
   * are removed, see the documentation of {@link #removeRecord(String)}.</p>
   * <p><b>NOTE</b> that the rdf:about is
   * used to find the dependencies, rather than the actual references in the records. While this is
   * a reasonably safe way to go for now, eventually a more generic way along the lines of {@link
   * #removeRecord(String)} should be found, in which the exact composition of the rdf:about is
   * taken out of the equation.</p>
   *
   * @param datasetId The ID of the dataset to clear. Is not null.
   * @param maxRecordDate The cutoff date: all records that have a lower timestampUpdated than this
   * date will be removed. If null is provided then all records from that dataset will be removed.
   * @return The number of records that were removed.
   * @throws IndexerRelatedIndexingException In case something went wrong.
   */
  public long removeDataset(String datasetId, Date maxRecordDate)
      throws IndexerRelatedIndexingException {
    final long mongoCount;
    try {
      mongoCount = removeDatasetFromMongo(datasetId, maxRecordDate);
      removeDatasetFromSolr(datasetId, maxRecordDate);
    } catch (SolrServerException | IOException | RuntimeException e) {
      throw new IndexerRelatedIndexingException(
          "Could not remove dataset with ID '" + datasetId + "'.", e);
    }
    return mongoCount;
  }

  /**
   * Return all record IDs that belong to the given dataset. For implementation details see {@link
   * #removeDataset(String, Date)} as the selection is to be performed analogously.
   *
   * @param datasetId The ID of the dataset to search. Is not null.
   * @param maxRecordDate The cutoff date: all records that have a lower timestampUpdated than this
   * date will be included. If null is provided then all records from that dataset are included.
   * @return The record IDs in a stream.
   */
  public Stream<String> getRecordIds(String datasetId, Date maxRecordDate) {
    final FindOptions findOptions = new FindOptions()
            .projection().exclude(ID_FIELD)
            .projection().include(ABOUT_FIELD);
    final Iterator<FullBeanImpl> resultIterator = createMongoQuery(datasetId, maxRecordDate)
            .iterator(findOptions);
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(resultIterator, 0), false)
            .map(FullBeanImpl::getAbout);
  }

  /**
   * Count all records that belong to the given dataset. For implementation details see {@link
   * #removeDataset(String, Date)} as the selection is to be performed analogously.
   *
   * @param datasetId The ID of the dataset to search. Is not null.
   * @param maxRecordDate The cutoff date: all records that have a lower timestampUpdated than this
   * date will be counted. If null is provided then all records from that dataset will be counted.
   * @return The record IDs in a stream.
   */
  public long countRecords(String datasetId, Date maxRecordDate) {
    return createMongoQuery(datasetId, maxRecordDate).count();
  }

  private void removeDatasetFromSolr(String datasetId, Date maxRecordDate)
      throws SolrServerException, IOException {
    final StringBuilder solrQuery = new StringBuilder();

    final String datasetIdRegexEscaped =
        ClientUtils.escapeQueryChars(getRecordIdPrefix(datasetId)) + "*";
    solrQuery.append(EdmLabel.EUROPEANA_ID).append(':').append(datasetIdRegexEscaped);

    if (maxRecordDate != null) {
      //Set date format properly for Solr, the timezone has to be added
      DateFormat dateFormat = new SimpleDateFormat(CommonStringValues.DATE_FORMAT_Z, Locale.US);
      dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      solrQuery.append(" AND ").append(EdmLabel.TIMESTAMP_UPDATED).append(":[* TO ")
          .append(dateFormat.format(maxRecordDate)).append('}');
    }
    solrServer.deleteByQuery(solrQuery.toString());
  }

  private long removeDatasetFromMongo(String datasetId, Date maxRecordDate) {
    return createMongoQuery(datasetId, maxRecordDate).delete(new DeleteOptions().multi(true))
            .getDeletedCount();
  }

  private Query<FullBeanImpl> createMongoQuery(String datasetId, Date maxRecordDate) {
    final Pattern pattern = Pattern.compile("^" + Pattern.quote(getRecordIdPrefix(datasetId)));
    final Query<FullBeanImpl> query = mongoServer.getDatastore().find(FullBeanImpl.class);
    query.filter(Filters.regex(ABOUT_FIELD).pattern(pattern));
    if (maxRecordDate != null) {
      query.filter(Filters.lt("timestampUpdated", maxRecordDate));
    }
    return query;
  }

  private static String getRecordIdPrefix(String datasetId) {
    return "/" + datasetId + "/";
  }
}
