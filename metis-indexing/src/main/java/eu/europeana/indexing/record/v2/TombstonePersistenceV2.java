package eu.europeana.indexing.record.v2;

import static java.lang.String.format;

import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.common.contract.RecordPersistence;
import eu.europeana.indexing.common.contract.TombstonePersistence;
import eu.europeana.indexing.common.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.common.exception.IndexingException;
import eu.europeana.indexing.common.fullbean.RdfToFullBeanConverter;
import eu.europeana.indexing.utils.RDFDeserializer;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.TombstoneUtil;
import eu.europeana.metis.mongo.dao.RecordDao;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.utils.DepublicationReason;
import java.util.Objects;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements tombstone persistence using the tombstone MongoDB V2.
 */
public class TombstonePersistenceV2 implements TombstonePersistence {

  private static final Logger LOGGER = LoggerFactory.getLogger(TombstonePersistenceV2.class);

  private final RecordDao tombstoneRecordDao;
  private final RecordPersistence<FullBeanImpl> recordPersistence;

  private final Supplier<RdfToFullBeanConverter> fullBeanConverterSupplier;
  private final RDFDeserializer rdfDeserializer = new RDFDeserializer();

  /**
   * Constructor.
   *
   * @param tombstoneRecordDao The Mongo persistence for tombstone records.
   * @param recordPersistence  Persistence access for live records. Can be null, in which case this
   *                           object will not be suitable for creating tombstones for live
   *                           records.
   */
  public TombstonePersistenceV2(RecordDao tombstoneRecordDao,
      RecordPersistence<FullBeanImpl> recordPersistence) {
    this.tombstoneRecordDao = tombstoneRecordDao;
    this.recordPersistence = recordPersistence;
    this.fullBeanConverterSupplier = RdfToFullBeanConverter::new;
  }

  private FullBeanImpl convertRDFToFullBean(RdfWrapper rdf) {
    final RdfToFullBeanConverter fullBeanConverter = fullBeanConverterSupplier.get();
    return fullBeanConverter.convertRdfToFullBean(rdf);
  }

  @Override
  public void indexTombstone(String rdfRecord, DepublicationReason reason) throws IndexingException {
    Objects.requireNonNull(rdfRecord, "record is null");
    if (tombstoneRecordDao == null) {
      throw new UnsupportedOperationException();
    }
    indexTombstone(rdfDeserializer.convertToRdf(rdfRecord), reason);
  }

  @Override
  public void indexTombstone(RDF rdfRecord, DepublicationReason reason) throws IndexingException {
    Objects.requireNonNull(rdfRecord, "record is null");
    if (tombstoneRecordDao == null) {
      throw new UnsupportedOperationException();
    }
    if (reasonUnsuitableForTombstoneCreation(reason)) {
      throw new IllegalArgumentException("Can't create tombstone for reason: " + reason);
    }
    indexTombstone(convertRDFToFullBean(new RdfWrapper(rdfRecord)), reason);
  }

  @Override
  public boolean indexTombstoneForLiveRecord(String rdfAbout, DepublicationReason reason)
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
      indexTombstone(publishedFullBean, reason);
    }
    return publishedFullBean != null;
  }

  private void indexTombstone(FullBeanImpl fullBean, DepublicationReason reason)
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
}
