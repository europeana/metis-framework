package eu.europeana.indexing;

import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.common.contract.IndexerForPersistence;
import eu.europeana.indexing.common.contract.IndexerForPersistence.ComputedDates;
import eu.europeana.indexing.common.contract.IndexerForSearch;
import eu.europeana.indexing.common.contract.IndexerForTombstones;
import eu.europeana.indexing.common.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.common.exception.IndexingException;
import eu.europeana.indexing.common.exception.RecordRelatedIndexingException;
import eu.europeana.indexing.common.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.common.fullbean.RdfToFullBeanConverter;
import eu.europeana.indexing.record.v2.IndexerForPersistenceV2;
import eu.europeana.indexing.record.v2.IndexerForTombstonesV2;
import eu.europeana.indexing.redirect.v2.RecordRedirectsUtil;
import eu.europeana.indexing.search.v2.IndexerForSearchV2;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.SolrUtils;
import eu.europeana.metis.mongo.dao.RecordDao;
import eu.europeana.metis.mongo.dao.RecordRedirectDao;
import eu.europeana.metis.utils.DepublicationReason;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.solr.client.solrj.SolrClient;

/**
 * Publisher for Full Beans (instances of {@link FullBeanImpl}) that makes them accessible and searchable for external agents.
 *
 * @author jochen
 */
public class FullBeanPublisher {

  private static final String REDIRECT_PUBLISH_ERROR = "Could not publish the redirection changes.";

  private final Supplier<RdfToFullBeanConverter> fullBeanConverterSupplier;

  private final SolrClient solrClient;
  private final RecordRedirectDao recordRedirectDao;

  private final boolean preserveUpdateAndCreateTimesFromRdf;

  private final IndexerForPersistence indexerForPersistence;
  private final IndexerForTombstones indexerForTombstones;
  private final IndexerForSearch indexerForSearch;

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
    this(recordDao, tombstoneRecordDao, recordRedirectDao, solrClient, preserveUpdateAndCreateTimesFromRdf,
        RdfToFullBeanConverter::new);
  }

  /**
   * Constructor for testing purposes.
   *
   * @param recordDao The Mongo persistence.
   * @param tombstoneRecordDao The Mongo persistence.
   * @param recordRedirectDao The record redirect dao
   * @param solrClient The searchable persistence.
   * @param preserveUpdateAndCreateTimesFromRdf This regulates whether we should preserve (use) the
   * updated and created dates that are set in the input record or if they should be recomputed
   * using any equivalent record that is currently in the database.
   * @param fullBeanConverterSupplier Supplies an instance of {@link RdfToFullBeanConverter} used to parse strings to instances of
   * {@link FullBeanImpl}. Will be called once during every publish.
   */
  FullBeanPublisher(RecordDao recordDao, RecordDao tombstoneRecordDao, RecordRedirectDao recordRedirectDao,
      SolrClient solrClient, boolean preserveUpdateAndCreateTimesFromRdf,
      Supplier<RdfToFullBeanConverter> fullBeanConverterSupplier) {
    this.recordRedirectDao = recordRedirectDao;
    this.solrClient = solrClient;
    this.fullBeanConverterSupplier = fullBeanConverterSupplier;
    this.preserveUpdateAndCreateTimesFromRdf = preserveUpdateAndCreateTimesFromRdf;
    this.indexerForPersistence = new IndexerForPersistenceV2(recordDao);
    this.indexerForTombstones = new IndexerForTombstonesV2(recordDao, tombstoneRecordDao);
    this.indexerForSearch = new IndexerForSearchV2(solrClient, fullBeanConverterSupplier);
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

    final List<Pair<String, Date>> recordsForRedirection = performRedirection(rdf,
        recordDate, datasetIdsToRedirectFrom, performRedirects);
    final Date createdDate = recordsForRedirection.stream().map(Pair::getValue)
        .min(Comparator.naturalOrder()).orElse(null);

    final ComputedDates computedDates = this.indexerForPersistence.indexForPersistence(rdf,
        this.preserveUpdateAndCreateTimesFromRdf, recordDate, createdDate);

    this.indexerForSearch.indexForSearch(rdf, computedDates.updatedDate(),
        computedDates.createdDate());
  }

  public boolean publishTombstone(String rdfAbout, DepublicationReason reason) throws IndexingException {
    return this.indexerForTombstones.indexTombstoneForLiveRecord(rdfAbout, reason);
  }

  private List<Pair<String, Date>> performRedirection(RdfWrapper rdf, Date recordDate, List<String> datasetIdsToRedirectFrom,
      boolean performRedirects) throws IndexingException {
    // Perform redirection
    final List<Pair<String, Date>> recordsForRedirection;
    try {
      recordsForRedirection = RecordRedirectsUtil.checkAndApplyRedirects(recordRedirectDao, rdf,
          recordDate, datasetIdsToRedirectFrom, performRedirects,
          queryParams -> SolrUtils.getSolrDocuments(solrClient, queryParams));
    } catch (RuntimeException e) {
      throw new RecordRelatedIndexingException(REDIRECT_PUBLISH_ERROR, e);
    }
    return recordsForRedirection;
  }
}
