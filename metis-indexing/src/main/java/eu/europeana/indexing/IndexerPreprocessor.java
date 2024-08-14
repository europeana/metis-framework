package eu.europeana.indexing;

import eu.europeana.indexing.exception.IndexingException;
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
    if (properties.isPerformTierCalculation() && properties.getTypesEnabledForTierCalculation()
                                                           .contains(rdfWrapper.getEdmType())) {
      final TierResults tierCalculationsResultProvidedData = new TierResults(
          mediaClassifier.classify(rdfWrapper), metadataClassifier.classify(rdfWrapper));
      RdfTierUtils.setTier(rdf, tierCalculationsResultProvidedData.getMediaTier());
      RdfTierUtils.setTier(rdf, tierCalculationsResultProvidedData.getMetadataTier());

      final TierResults tierCalculationsResultEuropeana = new TierResults(
          mediaClassifier.classify(rdfWrapper), metadataClassifierEuropeana.classify(rdfWrapper));
      RdfTierUtils.setTierEuropeana(rdf, tierCalculationsResultEuropeana.getMediaTier());
      RdfTierUtils.setTierEuropeana(rdf, tierCalculationsResultEuropeana.getMetadataTier());

      return tierCalculationsResultProvidedData;
    }

    return new TierResults();
  }
}
