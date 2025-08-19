package eu.europeana.indexing.record.v2;

import static java.lang.String.format;

import com.mongodb.client.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.common.contract.QueryableRecordPersistence;
import eu.europeana.indexing.common.contract.QueryableTombstonePersistence;
import eu.europeana.indexing.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.common.fullbean.RdfToFullBeanConverter;
import eu.europeana.indexing.utils.RDFDeserializer;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.TombstoneUtil;
import eu.europeana.metis.mongo.connection.MongoClientProvider;
import eu.europeana.metis.mongo.dao.RecordDao;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.utils.DepublicationReason;
import java.io.IOException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements tombstone persistence using the tombstone MongoDB V2.
 */
public class TombstonePersistenceV2 implements QueryableTombstonePersistence<FullBeanImpl> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TombstonePersistenceV2.class);

  private static final String ABOUT_FIELD = "about";

  private final MongoClient mongoClientToClose;
  private final RecordDao tombstoneRecordDao;
  private final QueryableRecordPersistence<FullBeanImpl> recordPersistence;

  private final RDFDeserializer rdfDeserializer = new RDFDeserializer();

  /**
   * Constructor.
   *
   * @param mongoClientProvider  Provider for record persistence. Clients that are provided from
   *                             this object will be closed when this instance's {@link #close()}
   *                             method is called.
   * @param mongoTombstoneDBName The specific Mongo database to connect to.
   * @param recordPersistence    Persistence access for live records. Can be null, in which case
   *                             this object will not be suitable for creating tombstones for live
   *                             records. Note: this instance will not take responsibility for
   *                             closing this persistence object.
   * @throws SetupRelatedIndexingException In the case of setup issues.
   */
  public TombstonePersistenceV2(MongoClientProvider<SetupRelatedIndexingException> mongoClientProvider,
      String mongoTombstoneDBName, QueryableRecordPersistence<FullBeanImpl> recordPersistence)
      throws SetupRelatedIndexingException {
    this.mongoClientToClose = mongoClientProvider.createMongoClient();
    this.tombstoneRecordDao = new RecordDao(this.mongoClientToClose, mongoTombstoneDBName);
    this.recordPersistence = recordPersistence;
  }

  /**
   * Constructor.
   *
   * @param tombstoneRecordDao DAO object for record tombstones. Note: this instance will not take
   *                           responsibility for closing this client.
   * @param recordPersistence  Persistence access for live records. Can be null, in which case this
   *                           object will not be suitable for creating tombstones for live records.
   *                           Note: this instance will not take responsibility for closing this
   *                           persistence object.
   */
  public TombstonePersistenceV2(RecordDao tombstoneRecordDao,
      QueryableRecordPersistence<FullBeanImpl> recordPersistence) {
    this.mongoClientToClose = null;
    this.tombstoneRecordDao = tombstoneRecordDao;
    this.recordPersistence = recordPersistence;
  }

  private static FullBeanImpl convertRDFToFullBean(RdfWrapper rdf) {
    final RdfToFullBeanConverter fullBeanConverter = new RdfToFullBeanConverter();
    return fullBeanConverter.convertRdfToFullBean(rdf);
  }

  @Override
  public void saveTombstone(String rdfRecord, DepublicationReason reason) throws IndexingException {
    Objects.requireNonNull(rdfRecord, "record is null");
    if (tombstoneRecordDao == null) {
      throw new UnsupportedOperationException();
    }
    saveTombstone(rdfDeserializer.convertToRdf(rdfRecord), reason);
  }

  @Override
  public void saveTombstone(RDF rdfRecord, DepublicationReason reason) throws IndexingException {
    Objects.requireNonNull(rdfRecord, "record is null");
    if (tombstoneRecordDao == null) {
      throw new UnsupportedOperationException();
    }
    if (reasonUnsuitableForTombstoneCreation(reason)) {
      throw new IllegalArgumentException("Can't create tombstone for reason: " + reason);
    }
    saveTombstone(convertRDFToFullBean(new RdfWrapper(rdfRecord)), reason);
  }

  @Override
  public boolean saveTombstoneForLiveRecord(String rdfAbout, DepublicationReason reason)
      throws IndexingException {
    Objects.requireNonNull(rdfAbout, "rdfAbout is null");
    if (tombstoneRecordDao == null) {
      throw new UnsupportedOperationException();
    }
    if (reasonUnsuitableForTombstoneCreation(reason)) {
      LOGGER.warn(
          "Record {} Depublication reason {} disabled temporarily for tombstone indexing.",
          rdfAbout, reason);
      return true;
    }
    final FullBeanImpl publishedFullBean = this.recordPersistence.getRecord(rdfAbout);
    if (publishedFullBean != null) {
      saveTombstone(publishedFullBean, reason);
    }
    return publishedFullBean != null;
  }

  private void saveTombstone(FullBeanImpl fullBean, DepublicationReason reason)
      throws IndexingException {
    final FullBeanImpl tombstone = TombstoneUtil.prepareTombstoneFullbean(fullBean, reason);
    try {
      PublishToMongoDBUtils.publishRecord(null, null, tombstone, tombstoneRecordDao, true);
    } catch (IndexingException e) {
      throw new IndexerRelatedIndexingException("Could not create tombstone record '" + tombstone.getAbout() + "'.", e);
    }
  }

  private static boolean reasonUnsuitableForTombstoneCreation(DepublicationReason reason)
      throws IndexerRelatedIndexingException {
    switch (reason) {
      case DepublicationReason.LEGACY -> throw new IndexerRelatedIndexingException(
          format("Depublication reason %s, is not allowed", reason));
      case DepublicationReason.BROKEN_MEDIA_LINKS, DepublicationReason.GENERIC,
           DepublicationReason.REMOVED_DATA_AT_SOURCE -> {
        return false;
      }
      default -> {
        return true;
      }
    }
  }

  @Override
  public FullBeanImpl getTombstone(String rdfAbout) {
    final Datastore datastore = this.tombstoneRecordDao.getDatastore();
    return datastore.find(FullBeanImpl.class).filter(Filters.eq(ABOUT_FIELD, rdfAbout)).first();
  }

  @Override
  public boolean removeTombstone(String rdfAbout) throws IndexerRelatedIndexingException {
    try {

      // Obtain the Mongo record
      final Datastore datastore = this.tombstoneRecordDao.getDatastore();
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

    } catch (RuntimeException e) {
      throw new IndexerRelatedIndexingException("Could not remove tombstone '" + rdfAbout + "'.", e);
    }
  }

  @Override
  public void triggerFlushOfPendingChanges(boolean blockUntilComplete)
      throws IndexerRelatedIndexingException {
    // Nothing to do.
  }

  @Override
  public void close() throws IOException {
    if (mongoClientToClose != null) {
      this.mongoClientToClose.close();
    }
  }
}
