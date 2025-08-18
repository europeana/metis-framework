package eu.europeana.indexing;

import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.tiers.ClassifierFactory;
import eu.europeana.indexing.tiers.TierCalculationMode;
import eu.europeana.indexing.tiers.metadata.ClassifierMode;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.model.TierClassifier;
import eu.europeana.indexing.tiers.model.TierClassifier.TierClassification;
import eu.europeana.indexing.tiers.model.TierResults;
import eu.europeana.indexing.tiers.view.ContentTierBreakdown;
import eu.europeana.indexing.tiers.view.MetadataTierBreakdown;
import eu.europeana.indexing.utils.RdfTierUtils;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.jibx.RDF;

/**
 * The type Indexer preprocessor.
 */
public final class IndexerPreprocessor {

  private static final TierClassifier<MediaTier, ContentTierBreakdown> mediaClassifier =
      ClassifierFactory.getMediaClassifier();
  private static final TierClassifier<MetadataTier, MetadataTierBreakdown> metadataClassifier =
      ClassifierFactory.getMetadataClassifier(ClassifierMode.PROVIDER_PROXIES);
  private static final TierClassifier<MetadataTier, MetadataTierBreakdown> metadataClassifierEuropeana =
      ClassifierFactory.getMetadataClassifier(ClassifierMode.ALL_PROXIES);

  private IndexerPreprocessor() {
  }

  /**
   * Preprocess record tier results.
   *
   * @param rdf the rdf
   * @param properties the properties
   * @return the tier results for the provider data (i.e. {@link ClassifierMode#PROVIDER_PROXIES}).
   * @throws IndexingException the indexing exception
   */
  public static TierResults preprocessRecord(RDF rdf, IndexingProperties properties)
      throws IndexingException {

    // Perform the tier classification
    final RdfWrapper rdfWrapper = new RdfWrapper(rdf);

    if (!properties.getTierCalculationMode().equals(TierCalculationMode.SKIP)
        && properties.getTypesEnabledForTierCalculation().contains(rdfWrapper.getEdmType())) {
      final TierClassification<MediaTier, ContentTierBreakdown> mediaTierClassification =
          mediaClassifier.classify(rdfWrapper);
      final TierResults tierCalculationsResultProvidedData = new TierResults(
          mediaTierClassification, metadataClassifier.classify(rdfWrapper));
      final TierResults tierCalculationsResultEuropeana = new TierResults(
          mediaTierClassification, metadataClassifierEuropeana.classify(rdfWrapper));

      if (properties.getTierCalculationMode().equals(TierCalculationMode.INITIALISE)) {
          RdfTierUtils.setTierIfAbsent(rdf, tierCalculationsResultProvidedData.getMediaTier());
          RdfTierUtils.setTierIfAbsent(rdf, tierCalculationsResultProvidedData.getMetadataTier());
          RdfTierUtils.setTierEuropeanaIfAbsent(rdf, tierCalculationsResultEuropeana.getMediaTier());
          RdfTierUtils.setTierEuropeanaIfAbsent(rdf, tierCalculationsResultEuropeana.getMetadataTier());
      } else if (properties.getTierCalculationMode().equals(TierCalculationMode.OVERWRITE)) {
        RdfTierUtils.setTier(rdf, tierCalculationsResultProvidedData.getMediaTier());
        RdfTierUtils.setTier(rdf, tierCalculationsResultProvidedData.getMetadataTier());
        RdfTierUtils.setTierEuropeana(rdf, tierCalculationsResultEuropeana.getMediaTier());
        RdfTierUtils.setTierEuropeana(rdf, tierCalculationsResultEuropeana.getMetadataTier());
      }

      return tierCalculationsResultProvidedData;
    }
    return new TierResults();
  }
}
