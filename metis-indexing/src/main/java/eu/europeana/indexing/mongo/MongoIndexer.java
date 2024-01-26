package eu.europeana.indexing.mongo;

import static eu.europeana.indexing.utils.IndexingSettingsUtils.nonNullIllegal;

import eu.europeana.indexing.AbstractConnectionProvider;
import eu.europeana.indexing.FullBeanPublisher;
import eu.europeana.indexing.IndexerImpl.IndexingSupplier;
import eu.europeana.indexing.IndexingProperties;
import eu.europeana.indexing.SimpleIndexer;
import eu.europeana.indexing.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.exception.RecordRelatedIndexingException;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.fullbean.StringToFullBeanConverter;
import eu.europeana.indexing.tiers.ClassifierFactory;
import eu.europeana.indexing.tiers.metadata.ClassifierMode;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.model.TierClassifier;
import eu.europeana.indexing.tiers.model.TierResults;
import eu.europeana.indexing.tiers.view.ContentTierBreakdown;
import eu.europeana.indexing.tiers.view.MetadataTierBreakdown;
import eu.europeana.indexing.utils.RdfTierUtils;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.jibx.RDF;
import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a record for indexing in Mongo
 */
public class MongoIndexer implements SimpleIndexer {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final AbstractConnectionProvider connectionProvider;
  private final IndexingSupplier<StringToFullBeanConverter> stringToRdfConverterSupplier;
  private final TierClassifier<MediaTier, ContentTierBreakdown> mediaClassifier = ClassifierFactory.getMediaClassifier();
  private final TierClassifier<MetadataTier, MetadataTierBreakdown> metadataClassifier = ClassifierFactory.getMetadataClassifier(
      ClassifierMode.PROVIDER_PROXIES);
  private final TierClassifier<MetadataTier, MetadataTierBreakdown> metadataClassifierEuropeana = ClassifierFactory.getMetadataClassifier(
      ClassifierMode.ALL_PROXIES);
  private final IndexingProperties indexingProperties;

  /**
   * Instantiates a new Mongo indexer.
   *
   * @param settings the mongo indexer settings
   * @throws SetupRelatedIndexingException the setup related indexing exception
   */
  public MongoIndexer(MongoIndexingSettings settings) throws SetupRelatedIndexingException {
    this.connectionProvider = new MongoConnectionProvider(settings);
    this.stringToRdfConverterSupplier = StringToFullBeanConverter::new;
    this.indexingProperties = settings.getIndexingProperties();
  }

  /**
   * Index to Mongo a rdf record object
   *
   * @param rdfRecord An RDF record object
   * @throws IndexingException which can be one of:
   * <ul>
   * <li>{@link IndexerRelatedIndexingException} In case an error occurred during publication.</li>
   * <li>{@link SetupRelatedIndexingException} in case an error occurred during indexing setup</li>
   * <li>{@link RecordRelatedIndexingException} in case an error occurred related to record
   * contents</li>
   * </ul>
   */
  @Override
  public void indexRecord(RDF rdfRecord) throws IndexingException {
    // Sanity checks
    rdfRecord = nonNullIllegal(rdfRecord, "Input RDF cannot be null.");

    LOGGER.info("Processing record {}", rdfRecord);
    final FullBeanPublisher publisher = connectionProvider.getFullBeanPublisher(false);
    preProcessRecord(rdfRecord);
    publisher.publishMongo(new RdfWrapper(rdfRecord), Date.from(Instant.now()));
  }

  private void preProcessRecord(RDF rdfRecord) throws IndexingException {
    final RdfWrapper rdfWrapper = new RdfWrapper(rdfRecord);
    TierResults tierCalculationsResult = new TierResults();
    if (indexingProperties.isPerformTierCalculation() && indexingProperties.getTypesEnabledForTierCalculation()
                                                           .contains(rdfWrapper.getEdmType())) {
      tierCalculationsResult = new TierResults(mediaClassifier.classify(rdfWrapper),
          metadataClassifier.classify(rdfWrapper));
      RdfTierUtils.setTier(rdfRecord, tierCalculationsResult.getMediaTier());
      RdfTierUtils.setTier(rdfRecord, tierCalculationsResult.getMetadataTier());

      tierCalculationsResult = new TierResults(mediaClassifier.classify(rdfWrapper),
          metadataClassifierEuropeana.classify(rdfWrapper));
      RdfTierUtils.setTierEuropeana(rdfRecord, tierCalculationsResult.getMediaTier());
      RdfTierUtils.setTierEuropeana(rdfRecord, tierCalculationsResult.getMetadataTier());
    }
  }

  /**
   * Index to Mongo a string rdf record
   *
   * @param stringRdfRecord A rdf record in string format
   * @throws IndexingException which can be one of:
   * <ul>
   * <li>{@link IndexerRelatedIndexingException} In case an error occurred during publication.</li>
   * <li>{@link SetupRelatedIndexingException} in case an error occurred during indexing setup</li>
   * <li>{@link RecordRelatedIndexingException} in case an error occurred related to record
   * contents</li>
   * </ul>
   */
  @Override
  public void indexRecord(String stringRdfRecord) throws IndexingException {
    final RDF rdfRecord = stringToRdfConverterSupplier.get().convertStringToRdf(stringRdfRecord);
    indexRecord(rdfRecord);
  }
}
