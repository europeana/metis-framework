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
public class IndexerPreprocessor {

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
   * @return the tier results
   * @throws IndexingException the indexing exception
   */
  public static TierResults preprocessRecord(RDF rdf, IndexingProperties properties)
      throws IndexingException {

    // Perform the tier classification
    final RdfWrapper rdfWrapper = new RdfWrapper(rdf);
    TierResults tierCalculationsResult = new TierResults();
    if (properties.isPerformTierCalculation() && properties.getTypesEnabledForTierCalculation()
                                                           .contains(rdfWrapper.getEdmType())) {
      tierCalculationsResult = new TierResults(mediaClassifier.classify(rdfWrapper),
          metadataClassifier.classify(rdfWrapper));
      RdfTierUtils.setTier(rdf, tierCalculationsResult.getMediaTier());
      RdfTierUtils.setTier(rdf, tierCalculationsResult.getMetadataTier());

      tierCalculationsResult = new TierResults(mediaClassifier.classify(rdfWrapper),
          metadataClassifierEuropeana.classify(rdfWrapper));
      RdfTierUtils.setTierEuropeana(rdf, tierCalculationsResult.getMediaTier());
      RdfTierUtils.setTierEuropeana(rdf, tierCalculationsResult.getMetadataTier());
    }

    return tierCalculationsResult;
  }

}
