package eu.europeana.indexing.record.v2;

import com.mongodb.client.MongoClient;
import dev.morphia.query.filters.Filters;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.common.contract.RecordPersistence;
import eu.europeana.indexing.common.exception.IndexingException;
import eu.europeana.indexing.common.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.common.fullbean.RdfToFullBeanConverter;
import eu.europeana.indexing.utils.RDFDeserializer;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.mongo.connection.MongoClientProvider;
import eu.europeana.metis.mongo.dao.RecordDao;
import eu.europeana.metis.schema.jibx.RDF;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

/**
 * This class implements record persistence using the record MongoDB V2.
 */
public class RecordPersistenceV2 implements RecordPersistence<FullBeanImpl> {

  private static final String ABOUT_FIELD = "about";

  private static final String NULL_RECORD_MESSAGE = "record is null";

  private final MongoClient mongoClientToClose;
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
    this.mongoClientToClose = mongoClientProvider.createMongoClient();
    this.recordDao = new RecordDao(this.mongoClientToClose, mongoDatabaseName);
  }

  /**
   * Constructor.
   *
   * @param recordDao DAO object for records. Note: this instance will not take
   *                  responsibility for closing this client.
   */
  public RecordPersistenceV2(RecordDao recordDao) {
    this.mongoClientToClose = null;
    this.recordDao = recordDao;
  }

  private FullBeanImpl convertRDFToFullBean(RdfWrapper rdf) {
    final RdfToFullBeanConverter fullBeanConverter = new RdfToFullBeanConverter();
    return fullBeanConverter.convertRdfToFullBean(rdf);
  }

  @Override
  public FullBeanImpl getRecord(String rdfAbout) {
    return recordDao.getDatastore().find(FullBeanImpl.class)
        .filter(Filters.eq(ABOUT_FIELD, rdfAbout)).first();
  }

  @Override
  public ComputedDates indexForPersistence(String rdfRecord) throws IndexingException {
    Objects.requireNonNull(rdfRecord, NULL_RECORD_MESSAGE);
    if (recordDao == null) {
      throw new UnsupportedOperationException();
    }
    return indexForPersistence(rdfDeserializer.convertToRdf(rdfRecord));
  }

  @Override
  public ComputedDates indexForPersistence(RDF rdfRecord) throws IndexingException {
    Objects.requireNonNull(rdfRecord, NULL_RECORD_MESSAGE);
    if (recordDao == null) {
      throw new UnsupportedOperationException();
    }
    return indexForPersistence(new RdfWrapper(rdfRecord), false, new Date(), null);
  }

  @Override
  public ComputedDates indexForPersistence(RdfWrapper rdf, boolean preserveUpdateAndCreateTimesFromRdf,
      Date recordDate, Date createdDate) throws IndexingException {
    Objects.requireNonNull(rdf, NULL_RECORD_MESSAGE);
    if (recordDao == null) {
      throw new UnsupportedOperationException();
    }
    return PublishToMongoDBUtils.publishRecord(recordDate, createdDate, convertRDFToFullBean(rdf),
        recordDao, preserveUpdateAndCreateTimesFromRdf);
  }

  @Override
  public void close() throws IOException {
    if (mongoClientToClose != null) {
      this.mongoClientToClose.close();
    }
  }
}
