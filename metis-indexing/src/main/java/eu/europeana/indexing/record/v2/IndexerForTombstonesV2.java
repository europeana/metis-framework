package eu.europeana.indexing.record.v2;

import static java.lang.String.format;

import dev.morphia.query.filters.Filters;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.common.contract.IndexerForTombstones;
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

public class IndexerForTombstonesV2 implements IndexerForTombstones {

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexerForTombstonesV2.class);

  private final RecordDao liveRecordDao;
  private final RecordDao tombstoneRecordDao;

  private final Supplier<RdfToFullBeanConverter> fullBeanConverterSupplier;
  private final RDFDeserializer rdfDeserializer = new RDFDeserializer();

  /**
   * Constructor.
   *
   * @param liveRecordDao The Mongo persistence for live records. Can be null, in which case this
   *                      object will not be suitable for creating tombstones for live records.
   * @param tombstoneRecordDao The Mongo persistence for tombstone records.
   */
  public IndexerForTombstonesV2(RecordDao liveRecordDao, RecordDao tombstoneRecordDao) {
    this.liveRecordDao = liveRecordDao;
    this.tombstoneRecordDao = tombstoneRecordDao;
    this.fullBeanConverterSupplier = RdfToFullBeanConverter::new;
  }

  private FullBeanImpl convertRDFToFullBean(RdfWrapper rdf) {
    final RdfToFullBeanConverter fullBeanConverter = fullBeanConverterSupplier.get();
    return fullBeanConverter.convertRdfToFullBean(rdf);
  }

  private FullBeanImpl getLiveRecord(String rdfAbout) {
    return liveRecordDao.getDatastore().find(FullBeanImpl.class).filter(Filters.eq("about", rdfAbout)).first();
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
    indexTombstone(convertRDFToFullBean(new RdfWrapper(rdfRecord)), reason);
  }

  @Override
  public boolean indexTombstoneForLiveRecord(String rdfAbout, DepublicationReason reason)
      throws IndexingException {
    Objects.requireNonNull(rdfAbout, "rdfAbout is null");
    if (tombstoneRecordDao == null) {
      throw new UnsupportedOperationException();
    }
    switch (reason) {
      case DepublicationReason.LEGACY -> throw new IndexerRelatedIndexingException(
          format("Depublication reason %s, is not allowed", reason));
      case DepublicationReason.BROKEN_MEDIA_LINKS, DepublicationReason.GENERIC,
           DepublicationReason.REMOVED_DATA_AT_SOURCE -> {
        final FullBeanImpl publishedFullBean = getLiveRecord(rdfAbout);
        if (publishedFullBean != null) {
          indexTombstone(publishedFullBean, reason);
        }
        return publishedFullBean != null;
      }
      default -> {
        LOGGER.warn(
            "Record {} Depublication reason {} disabled temporarily for tombstone indexing.",
            rdfAbout, reason);
        return true;
      }
    }
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
}
