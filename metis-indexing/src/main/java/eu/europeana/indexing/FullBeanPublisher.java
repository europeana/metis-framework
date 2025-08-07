package eu.europeana.indexing;

import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.common.contract.RecordPersistence;
import eu.europeana.indexing.common.contract.RecordPersistence.ComputedDates;
import eu.europeana.indexing.common.contract.RedirectPersistence;
import eu.europeana.indexing.common.contract.SearchPersistence;
import eu.europeana.indexing.common.contract.TombstonePersistence;
import eu.europeana.indexing.common.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.common.exception.IndexingException;
import eu.europeana.indexing.common.exception.RecordRelatedIndexingException;
import eu.europeana.indexing.common.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.record.v2.RecordPersistenceV2;
import eu.europeana.indexing.record.v2.TombstonePersistenceV2;
import eu.europeana.indexing.redirect.v2.RedirectPersistenceV2;
import eu.europeana.indexing.search.v2.SearchPersistenceV2;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.mongo.dao.RecordDao;
import eu.europeana.metis.mongo.dao.RecordRedirectDao;
import eu.europeana.metis.utils.DepublicationReason;
import java.util.Date;
import java.util.List;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 * Publisher for Full Beans (instances of {@link FullBeanImpl}) that makes them accessible and searchable for external agents.
 *
 * @author jochen
 */
public class FullBeanPublisher {

  private final boolean preserveUpdateAndCreateTimesFromRdf;

  private final RecordPersistence<FullBeanImpl> recordPersistence;
  private final TombstonePersistence tombstonePersistence;
  private final SearchPersistence<SolrDocument, SolrDocumentList> searchPersistence;
  private final RedirectPersistence redirectPersistence;

  /**
   * Constructor.
   *
   * @param recordDao The Mongo persistence.
   * @param tombstoneRecordDao The mongo tombstone persistence.
   * @param recordRedirectDao The record redirect dao
   * @param solrClient The searchable persistence.
   * @param preserveUpdateAndCreateTimesFromRdf This regulates whether we should preserve (use) the
   * updated and created dates that are set in the input record or if they should be recomputed
   * using any equivalent record that is currently in the database.
   */
  FullBeanPublisher(RecordDao recordDao, RecordDao tombstoneRecordDao, RecordRedirectDao recordRedirectDao,
      SolrClient solrClient, boolean preserveUpdateAndCreateTimesFromRdf) {
    this.preserveUpdateAndCreateTimesFromRdf = preserveUpdateAndCreateTimesFromRdf;
    this.recordPersistence = new RecordPersistenceV2(recordDao);
    this.tombstonePersistence = new TombstonePersistenceV2(tombstoneRecordDao, this.recordPersistence);
    this.searchPersistence = new SearchPersistenceV2(solrClient);
    this.redirectPersistence = new RedirectPersistenceV2(recordRedirectDao, this.searchPersistence);
  }

  /**
   * Publishes an RDF.
   *
   * @param rdf RDF to publish.
   * @param recordDate The date that would represent the created/updated date of a record
   * @param datasetIdsToRedirectFrom The dataset ids that their records need to be redirected
   * @throws IndexingException which can be one of:
   * <ul>
   * <li>{@link IndexerRelatedIndexingException} In case an error occurred during publication.</li>
   * <li>{@link SetupRelatedIndexingException} in case an error occurred during indexing setup</li>
   * <li>{@link RecordRelatedIndexingException} in case an error occurred related to record
   * contents</li>
   * </ul>
   */
  public void publishWithRedirects(RdfWrapper rdf, Date recordDate,
      List<String> datasetIdsToRedirectFrom) throws IndexingException {
    publish(rdf, recordDate, datasetIdsToRedirectFrom, true);
  }

  /**
   * Publishes an RDF.
   *
   * @param rdf RDF to publish.
   * @param recordDate The date that would represent the created/updated date of a record
   * @param datasetIdsToRedirectFrom The dataset ids that their records need to be redirected
   * @throws IndexingException which can be one of:
   * <ul>
   * <li>{@link IndexerRelatedIndexingException} In case an error occurred during publication.</li>
   * <li>{@link SetupRelatedIndexingException} in case an error occurred during indexing setup</li>
   * <li>{@link RecordRelatedIndexingException} in case an error occurred related to record
   * contents</li>
   * </ul>
   */
  public void publish(RdfWrapper rdf, Date recordDate, List<String> datasetIdsToRedirectFrom)
      throws IndexingException {
    publish(rdf, recordDate, datasetIdsToRedirectFrom, false);
  }

  /**
   * Publishes an RDF.
   *
   * @param rdf RDF to publish.
   * @param recordDate The date that would represent the created/updated date of a record
   * @param datasetIdsToRedirectFrom The dataset ids that their records need to be redirected
   * @param performRedirects flag that indicates if redirect should be performed
   * @throws IndexingException which can be one of:
   * <ul>
   * <li>{@link IndexerRelatedIndexingException} In case an error occurred during publication.</li>
   * <li>{@link SetupRelatedIndexingException} in case an error occurred during indexing setup</li>
   * <li>{@link RecordRelatedIndexingException} in case an error occurred related to record
   * contents</li>
   * </ul>
   */
  private void publish(RdfWrapper rdf, Date recordDate, List<String> datasetIdsToRedirectFrom,
      boolean performRedirects) throws IndexingException {

    final Date createdDate = performRedirects ? this.redirectPersistence
        .performRedirection(rdf, recordDate, datasetIdsToRedirectFrom) : null;

    final ComputedDates computedDates = this.recordPersistence.indexForPersistence(rdf,
        this.preserveUpdateAndCreateTimesFromRdf, recordDate, createdDate);

    this.searchPersistence.indexForSearch(rdf, computedDates.updatedDate(),
        computedDates.createdDate());
  }

  public boolean publishTombstone(String rdfAbout, DepublicationReason reason) throws IndexingException {
    return this.tombstonePersistence.indexTombstoneForLiveRecord(rdfAbout, reason);
  }
}
