package eu.europeana.indexing.tiers;

import eu.europeana.indexing.exception.TierCalculationException;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.Tier;
import eu.europeana.indexing.tiers.model.TierClassifier.TierClassification;
import eu.europeana.indexing.tiers.view.ContentTierBreakdown;
import eu.europeana.indexing.tiers.view.MetadataTierBreakdown;
import eu.europeana.indexing.tiers.view.RecordTierCalculationSummary;
import eu.europeana.indexing.tiers.view.RecordTierCalculationView;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.convert.SerializationException;
import eu.europeana.metis.schema.jibx.RDF;

/**
 * Generator of tier statistics view {@link RecordTierCalculationView}
 */
public class RecordTierCalculationViewGenerator {

  private final String europeanaId;
  private final String providerId;
  private final String stringRdf;
  private final String portalRecordLink;
  private final String providerRecordLink;

  /**
   * Parameter constructor
   *
   * @param europeanaId the europeana id
   * @param providerId the provider id
   * @param stringRdf the rdf in string representation
   * @param portalRecordLink the portal record link
   * @param providerRecordLink the provider record link
   */
  public RecordTierCalculationViewGenerator(String europeanaId, String providerId, String stringRdf, String portalRecordLink,
      String providerRecordLink) {
    this.europeanaId = europeanaId;
    this.providerId = providerId;
    this.stringRdf = stringRdf;
    this.portalRecordLink = portalRecordLink;
    this.providerRecordLink = providerRecordLink;
  }

  /**
   * Generates the {@link RecordTierCalculationView} for the current object and its parameters
   *
   * @return the record tier calculation view
   */
  public RecordTierCalculationView generate() {
    return tierClassification(stringRdf);
  }

  private RecordTierCalculationView tierClassification(final String xml) {
    final RDF rdf;
    try {
      // Perform the tier classification
      rdf = RdfConversionUtils.convertStringToRdf(xml);
      final RdfWrapper rdfWrapper = new RdfWrapper(rdf);
      final TierClassification<MediaTier, ContentTierBreakdown> mediaTierClassification = ClassifierFactory.getMediaClassifier()
                                                                                                           .classify(rdfWrapper);
      final TierClassification<Tier, MetadataTierBreakdown> metadataTierClassification = ClassifierFactory.getMetadataClassifier()
                                                                                                          .classify(
                                                                                                              rdfWrapper);
      RecordTierCalculationSummary recordTierCalculationSummary = new RecordTierCalculationSummary();
      recordTierCalculationSummary.setEuropeanaRecordId(europeanaId);
      recordTierCalculationSummary.setProviderRecordId(providerId);
      recordTierCalculationSummary.setContentTier(mediaTierClassification.getTier());
      recordTierCalculationSummary.setMetadataTier(metadataTierClassification.getTier());
      recordTierCalculationSummary.setPortalRecordLink(portalRecordLink);
      recordTierCalculationSummary.setHarvestedRecordLink(providerRecordLink);

      final RecordTierCalculationView recordTierCalculationView = new RecordTierCalculationView(recordTierCalculationSummary,
          mediaTierClassification.getClassification(), metadataTierClassification.getClassification());

      // TODO: 04/01/2022 Update the remaining values with the upcoming tickets MET-4157 and MET-4158
      //Create an object that has predefined "fake" values
      RecordTierCalculationView fakeView = FakeTierCalculationProvider.getFakeObject();
      recordTierCalculationView.setContentTierBreakdown(fakeView.getContentTierBreakdown());

      return recordTierCalculationView;
    } catch (SerializationException e) {
      throw new TierCalculationException("Error during calculation of tiers", e);
    }
  }
}
