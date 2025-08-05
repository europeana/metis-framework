package eu.europeana.indexing.record.v2;

import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.common.contract.IndexerForPersistence;
import eu.europeana.indexing.common.exception.IndexingException;
import eu.europeana.indexing.common.fullbean.RdfToFullBeanConverter;
import eu.europeana.indexing.utils.RDFDeserializer;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.mongo.dao.RecordDao;
import eu.europeana.metis.schema.jibx.RDF;
import java.util.Date;
import java.util.Objects;
import java.util.function.Supplier;

public class IndexerForPersistenceV2 implements IndexerForPersistence {

  private final RecordDao liveRecordDao;

  private final Supplier<RdfToFullBeanConverter> fullBeanConverterSupplier;
  private final RDFDeserializer rdfDeserializer = new RDFDeserializer();

  /**
   * Constructor.
   *
   * @param liveRecordDao The Mongo persistence for live records.
   */
  public IndexerForPersistenceV2(RecordDao liveRecordDao) {
    this.liveRecordDao = liveRecordDao;
    this.fullBeanConverterSupplier = RdfToFullBeanConverter::new;
  }

  private FullBeanImpl convertRDFToFullBean(RdfWrapper rdf) {
    final RdfToFullBeanConverter fullBeanConverter = fullBeanConverterSupplier.get();
    return fullBeanConverter.convertRdfToFullBean(rdf);
  }

  @Override
  public ComputedDates indexForPersistence(String rdfRecord) throws IndexingException {
    Objects.requireNonNull(rdfRecord, "record is null");
    if (liveRecordDao == null) {
      throw new UnsupportedOperationException();
    }
    return indexForPersistence(rdfDeserializer.convertToRdf(rdfRecord));
  }

  @Override
  public ComputedDates indexForPersistence(RDF rdfRecord) throws IndexingException {
    Objects.requireNonNull(rdfRecord, "record is null");
    if (liveRecordDao == null) {
      throw new UnsupportedOperationException();
    }
    return indexForPersistence(new RdfWrapper(rdfRecord), false, new Date(), null);
  }

  @Override
  public ComputedDates indexForPersistence(RdfWrapper rdf, boolean preserveUpdateAndCreateTimesFromRdf,
      Date recordDate, Date createdDate) throws IndexingException {
    Objects.requireNonNull(rdf, "record is null");
    if (liveRecordDao == null) {
      throw new UnsupportedOperationException();
    }
    return PublishToMongoDBUtils.publishRecord(recordDate, createdDate, convertRDFToFullBean(rdf),
        liveRecordDao, preserveUpdateAndCreateTimesFromRdf);
  }
}
