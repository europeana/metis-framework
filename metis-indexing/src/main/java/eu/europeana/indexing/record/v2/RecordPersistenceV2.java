package eu.europeana.indexing.record.v2;

import com.mongodb.client.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.DeleteOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.common.contract.QueryableRecordPersistence;
import eu.europeana.indexing.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.common.fullbean.RdfToFullBeanConverter;
import eu.europeana.indexing.utils.RDFDeserializer;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.RecordUtils;
import eu.europeana.metis.mongo.connection.MongoClientProvider;
import eu.europeana.metis.mongo.dao.RecordDao;
import eu.europeana.metis.schema.jibx.RDF;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterators;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This class implements record persistence using the record MongoDB V2.
 */
public class RecordPersistenceV2 implements QueryableRecordPersistence<FullBeanImpl> {

  private static final String ID_FIELD = "_id";
  private static final String ABOUT_FIELD = "about";

  private static final String NULL_RECORD_MESSAGE = "record is null";

  private final MongoClient mongoClient;
  private final RecordDao recordDao;

  private final RDFDeserializer rdfDeserializer = new RDFDeserializer();

  /**
   * Constructor.
   *
   * @param mongoClientProvider Provider for record persistence. Clients that are provided from this
   *                            object will be closed when this instance's {@link #close()} method
   *                            is called.
   * @param mongoDatabaseName   The specific Mongo database to connect to.
   * @throws SetupRelatedIndexingException In the case of setup issues.
   */
  public RecordPersistenceV2(MongoClientProvider<SetupRelatedIndexingException> mongoClientProvider,
      String mongoDatabaseName) throws SetupRelatedIndexingException {
    this.mongoClient = mongoClientProvider.createMongoClient();
    this.recordDao = new RecordDao(this.mongoClient, mongoDatabaseName);
  }

  /**
   * Constructor.
   *
   * @param recordDao DAO object for records. Note: this instance will not take
   *                  responsibility for closing this client.
   */
  public RecordPersistenceV2(RecordDao recordDao) {
    this.mongoClient = null;
    this.recordDao = recordDao;
  }

  private static FullBeanImpl convertRDFToFullBean(RdfWrapper rdf) {
    final RdfToFullBeanConverter fullBeanConverter = new RdfToFullBeanConverter();
    return fullBeanConverter.convertRdfToFullBean(rdf);
  }

  private Query<FullBeanImpl> createMongoQuery(String datasetId, Date maxUpdatedDate) {
    final Pattern pattern = Pattern.compile("^" + Pattern.quote(RecordUtils.getRecordIdPrefix(datasetId)));
    final Query<FullBeanImpl> query = recordDao.getDatastore().find(FullBeanImpl.class);
    query.filter(Filters.regex(ABOUT_FIELD, pattern));
    if (maxUpdatedDate != null) {
      query.filter(Filters.lt("timestampUpdated", maxUpdatedDate));
    }
    return query;
  }


  @Override
  public FullBeanImpl getRecord(String rdfAbout) {
    return recordDao.getDatastore().find(FullBeanImpl.class)
        .filter(Filters.eq(ABOUT_FIELD, rdfAbout)).first();
  }

  @Override
  public ComputedDates saveRecord(String rdfRecord) throws IndexingException {
    Objects.requireNonNull(rdfRecord, NULL_RECORD_MESSAGE);
    if (recordDao == null) {
      throw new UnsupportedOperationException();
    }
    return saveRecord(rdfDeserializer.convertToRdf(rdfRecord));
  }

  @Override
  public ComputedDates saveRecord(RDF rdfRecord) throws IndexingException {
    Objects.requireNonNull(rdfRecord, NULL_RECORD_MESSAGE);
    if (recordDao == null) {
      throw new UnsupportedOperationException();
    }
    return saveRecord(new RdfWrapper(rdfRecord), false, new Date(), null);
  }

  @Override
  public ComputedDates saveRecord(RdfWrapper rdf, boolean preserveUpdateAndCreateTimesFromRdf,
      Date recordDate, Date createdDate) throws IndexingException {
    Objects.requireNonNull(rdf, NULL_RECORD_MESSAGE);
    if (recordDao == null) {
      throw new UnsupportedOperationException();
    }
    return PublishToMongoDBUtils.publishRecord(recordDate, createdDate, convertRDFToFullBean(rdf),
        recordDao, preserveUpdateAndCreateTimesFromRdf);
  }

  @Override
  public long countRecords(String datasetId, Date maxUpdatedDate)
      throws IndexerRelatedIndexingException {
    try {
      return createMongoQuery(datasetId, maxUpdatedDate).count();
    } catch (RuntimeException e) {
      throw new IndexerRelatedIndexingException("Could not count records in dataset '" + datasetId + "'.", e);
    }
  }

  @Override
  public Stream<String> getRecordIds(String datasetId, Date maxUpdatedDate)
      throws IndexerRelatedIndexingException {
    try {
      final FindOptions findOptions = new FindOptions()
          .projection().exclude(ID_FIELD)
          .projection().include(ABOUT_FIELD);
      final Iterator<FullBeanImpl> resultIterator = createMongoQuery(datasetId, maxUpdatedDate)
          .iterator(findOptions);
      return StreamSupport.stream(Spliterators.spliteratorUnknownSize(resultIterator, 0), false)
          .map(FullBeanImpl::getAbout);
    } catch (RuntimeException e) {
      throw new IndexerRelatedIndexingException("Could not get IDs for dataset '" + datasetId + "'.", e);
    }
  }

  @Override
  public boolean removeRecord(String rdfAbout) throws IndexerRelatedIndexingException {
    try {

      // Obtain the Mongo record
      final Datastore datastore = recordDao.getDatastore();
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
      throw new IndexerRelatedIndexingException("Could not remove record '" + rdfAbout + "'.", e);
    }
  }

  @Override
  public long removeDataset(String datasetId, Date maxUpdatedDate)
      throws IndexerRelatedIndexingException {
    try {
      return createMongoQuery(datasetId, maxUpdatedDate).delete(new DeleteOptions().multi(true))
          .getDeletedCount();
    } catch (RuntimeException e) {
      throw new IndexerRelatedIndexingException("Could not remove dataset '" + datasetId + "'.", e);
    }
  }

  @Override
  public void triggerFlushOfPendingChanges(boolean blockUntilComplete)
      throws IndexerRelatedIndexingException {
    // Nothing to do.
  }

  @Override
  public void close() throws IOException {
    if (mongoClient != null) {
      this.mongoClient.close();
    }
  }
}
