package eu.europeana.indexing;

import com.mongodb.MongoClientException;
import com.mongodb.MongoConfigurationException;
import com.mongodb.MongoIncompatibleDriverException;
import com.mongodb.MongoInternalException;
import com.mongodb.MongoInterruptedException;
import com.mongodb.MongoSecurityException;
import com.mongodb.MongoSocketException;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.common.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.common.exception.IndexingException;
import eu.europeana.indexing.common.exception.RecordRelatedIndexingException;
import eu.europeana.indexing.common.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.common.fullbean.RdfToFullBeanConverter;
import eu.europeana.indexing.record.v2.FullBeanUpdater;
import eu.europeana.indexing.redirect.v2.RecordRedirectsUtil;
import eu.europeana.indexing.search.v2.IndexerForSearchingV2;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.SolrUtils;
import eu.europeana.metis.mongo.dao.RecordDao;
import eu.europeana.metis.mongo.dao.RecordRedirectDao;
import java.util.Collections;
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

  private static final String MONGO_SERVER_PUBLISH_ERROR = "Could not publish to Mongo server.";

  private final Supplier<RdfToFullBeanConverter> fullBeanConverterSupplier;

  private final RecordDao recordDao;
  private final RecordDao tombstoneRecordDao;
  private final SolrClient solrClient;
  private final boolean preserveUpdateAndCreateTimesFromRdf;
  private final RecordRedirectDao recordRedirectDao;

  private final IndexerForSearchingV2 indexerForSearchingV2;

  /**
   * Constructor.
   *
   * @param recordDao The Mongo persistence.
   * @param tombstoneRecordDao The mongo tombstone persistence.
   * @param recordRedirectDao The record redirect dao
   * @param solrClient The searchable persistence.
   * @param preserveUpdateAndCreateTimesFromRdf This determines whether this publisher will use the updated and created times from
   * the incoming RDFs, or whether it computes its own.
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
   * @param preserveUpdateAndCreateTimesFromRdf This determines whether this publisher will use the updated and created times from
   * the incoming RDFs, or whether it computes its own.
   * @param fullBeanConverterSupplier Supplies an instance of {@link RdfToFullBeanConverter} used to parse strings to instances of
   * {@link FullBeanImpl}. Will be called once during every publish.
   */
  FullBeanPublisher(RecordDao recordDao, RecordDao tombstoneRecordDao, RecordRedirectDao recordRedirectDao,
      SolrClient solrClient, boolean preserveUpdateAndCreateTimesFromRdf,
      Supplier<RdfToFullBeanConverter> fullBeanConverterSupplier) {
    this.recordDao = recordDao;
    this.tombstoneRecordDao = tombstoneRecordDao;
    this.recordRedirectDao = recordRedirectDao;
    this.solrClient = solrClient;
    this.fullBeanConverterSupplier = fullBeanConverterSupplier;
    this.preserveUpdateAndCreateTimesFromRdf = preserveUpdateAndCreateTimesFromRdf;
    this.indexerForSearchingV2 = new IndexerForSearchingV2(solrClient,
        preserveUpdateAndCreateTimesFromRdf, fullBeanConverterSupplier);
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

    final FullBeanImpl fullBean = convertRDFToFullBean(rdf);

    final List<Pair<String, Date>> recordsForRedirection = performRedirection(rdf,
        recordDate, datasetIdsToRedirectFrom, performRedirects);

    final FullBeanImpl savedFullBean = publishToRecordMongo(recordDate, fullBean,
        recordsForRedirection);

    this.indexerForSearchingV2.publishToSolrFinal(rdf, savedFullBean);
  }

  /**
   * Publishes an RDF only to mongo server
   *
   * @param rdf RDF to publish.
   * @param recordDate The date that would represent the created/updated date of a record
   * @throws IndexingException which can be one of:
   * <ul>
   * <li>{@link IndexerRelatedIndexingException} In case an error occurred during publication.</li>
   * <li>{@link SetupRelatedIndexingException} in case an error occurred during indexing setup</li>
   * <li>{@link RecordRelatedIndexingException} in case an error occurred related to record
   * contents</li>
   * </ul>
   */
  public void publishMongo(RdfWrapper rdf, Date recordDate) throws IndexingException {
    final FullBeanImpl fullBean = convertRDFToFullBean(rdf);

    publishToRecordMongo(recordDate, fullBean, Collections.emptyList());
  }

  /**
   * Publishes an RDF only to tombstone mongo.
   * @param fullBean Fullbean to publish.
   * @param recordDate the data that would represent the created/updated date of a record
   * @throws IndexingException which can be one of:
   * <ul>
   * <li>{@link IndexerRelatedIndexingException} In case an error occurred during publication.</li>
   * <li>{@link SetupRelatedIndexingException} in case an error occurred during indexing setup</li>
   * <li>{@link RecordRelatedIndexingException} in case an error occurred related to record
   * contents</li>
   * </ul>
   */
  public void publishTombstone(FullBeanImpl fullBean, Date recordDate) throws IndexingException {
    publishToTombstoneMongo(recordDate, fullBean, Collections.emptyList());
  }

  private FullBeanImpl publishToRecordMongo(Date recordDate, FullBeanImpl fullBean,
      List<Pair<String, Date>> recordsForRedirection)
      throws SetupRelatedIndexingException, IndexerRelatedIndexingException, RecordRelatedIndexingException {
    return publishToMongo(recordDate, fullBean, recordsForRedirection, recordDao);
  }

  private FullBeanImpl publishToTombstoneMongo(Date recordDate, FullBeanImpl fullBean,
      List<Pair<String, Date>> recordsForRedirection)
      throws SetupRelatedIndexingException, IndexerRelatedIndexingException, RecordRelatedIndexingException {
    return publishToMongo(recordDate, fullBean, recordsForRedirection, tombstoneRecordDao);
  }

  private FullBeanImpl publishToMongo(Date recordDate, FullBeanImpl fullBean,
      List<Pair<String, Date>> recordsForRedirection, RecordDao tombstoneRecordDao)
      throws SetupRelatedIndexingException, IndexerRelatedIndexingException, RecordRelatedIndexingException {
    final FullBeanImpl savedFullBean;
    try {
      final Date recordCreationDate = recordsForRedirection.stream().map(Pair::getValue)
          .min(Comparator.naturalOrder()).orElse(null);
      savedFullBean = new FullBeanUpdater(this.preserveUpdateAndCreateTimesFromRdf)
          .update(fullBean, recordDate, recordCreationDate, tombstoneRecordDao);
    } catch (MongoIncompatibleDriverException | MongoConfigurationException | MongoSecurityException e) {
      throw new SetupRelatedIndexingException(MONGO_SERVER_PUBLISH_ERROR, e);
    } catch (MongoSocketException | MongoClientException | MongoInternalException | MongoInterruptedException e) {
      throw new IndexerRelatedIndexingException(MONGO_SERVER_PUBLISH_ERROR, e);
    } catch (RuntimeException e) {
      throw new RecordRelatedIndexingException(MONGO_SERVER_PUBLISH_ERROR, e);
    }
    return savedFullBean;
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

  private FullBeanImpl convertRDFToFullBean(RdfWrapper rdf) {
    // Convert RDF to Full Bean.
    final RdfToFullBeanConverter fullBeanConverter = fullBeanConverterSupplier.get();
    return fullBeanConverter.convertRdfToFullBean(rdf);
  }
}
