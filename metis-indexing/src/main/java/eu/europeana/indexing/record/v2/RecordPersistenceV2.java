package eu.europeana.indexing.record.v2;

import dev.morphia.query.filters.Filters;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.common.contract.RecordPersistence;
import eu.europeana.indexing.common.exception.IndexingException;
import eu.europeana.indexing.common.fullbean.RdfToFullBeanConverter;
import eu.europeana.indexing.utils.RDFDeserializer;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.mongo.dao.RecordDao;
import eu.europeana.metis.schema.jibx.RDF;
import java.util.Date;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * This class implements record persistence using the record MongoDB V2.
 */
public class RecordPersistenceV2 implements RecordPersistence<FullBeanImpl> {

  private static final String ABOUT_FIELD = "about";

  private static final String NULL_RECORD_MESSAGE = "record is null";

  private final RecordDao liveRecordDao;

  private final Supplier<RdfToFullBeanConverter> fullBeanConverterSupplier;
  private final RDFDeserializer rdfDeserializer = new RDFDeserializer();

  /**
   * Constructor.
   *
   * @param liveRecordDao The Mongo persistence for live records.
   */
  public RecordPersistenceV2(RecordDao liveRecordDao) {
    this.liveRecordDao = liveRecordDao;
    this.fullBeanConverterSupplier = RdfToFullBeanConverter::new;
  }

  private FullBeanImpl convertRDFToFullBean(RdfWrapper rdf) {
    final RdfToFullBeanConverter fullBeanConverter = fullBeanConverterSupplier.get();
    return fullBeanConverter.convertRdfToFullBean(rdf);
  }

  @Override
  public FullBeanImpl getRecord(String rdfAbout) {
    return liveRecordDao.getDatastore().find(FullBeanImpl.class)
        .filter(Filters.eq(ABOUT_FIELD, rdfAbout)).first();
  }

  @Override
  public ComputedDates indexForPersistence(String rdfRecord) throws IndexingException {
    Objects.requireNonNull(rdfRecord, NULL_RECORD_MESSAGE);
    if (liveRecordDao == null) {
      throw new UnsupportedOperationException();
    }
    return indexForPersistence(rdfDeserializer.convertToRdf(rdfRecord));
  }

  @Override
  public ComputedDates indexForPersistence(RDF rdfRecord) throws IndexingException {
    Objects.requireNonNull(rdfRecord, NULL_RECORD_MESSAGE);
    if (liveRecordDao == null) {
      throw new UnsupportedOperationException();
    }
    return indexForPersistence(new RdfWrapper(rdfRecord), false, new Date(), null);
  }

  @Override
  public ComputedDates indexForPersistence(RdfWrapper rdf, boolean preserveUpdateAndCreateTimesFromRdf,
      Date recordDate, Date createdDate) throws IndexingException {
    Objects.requireNonNull(rdf, NULL_RECORD_MESSAGE);
    if (liveRecordDao == null) {
      throw new UnsupportedOperationException();
    }
    return PublishToMongoDBUtils.publishRecord(recordDate, createdDate, convertRDFToFullBean(rdf),
        liveRecordDao, preserveUpdateAndCreateTimesFromRdf);
  }
}
