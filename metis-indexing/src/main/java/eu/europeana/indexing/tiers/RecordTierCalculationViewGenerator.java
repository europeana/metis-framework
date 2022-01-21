package eu.europeana.indexing.tiers;

import eu.europeana.indexing.exception.TierCalculationException;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.Tier;
import eu.europeana.indexing.tiers.model.TierClassifier.TierClassification;
import eu.europeana.indexing.tiers.view.ContentTierBreakdown;
import eu.europeana.indexing.tiers.view.MetadataTierBreakdown;
import eu.europeana.indexing.tiers.view.ProcessingError;
import eu.europeana.indexing.tiers.view.RecordTierCalculationSummary;
import eu.europeana.indexing.tiers.view.RecordTierCalculationView;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.convert.SerializationException;
import eu.europeana.metis.schema.jibx.RDF;
import java.util.ArrayList;
import java.util.List;
import org.springframework.util.CollectionUtils;

/**
 * Generator of tier statistics view {@link RecordTierCalculationView}
 */
public class RecordTierCalculationViewGenerator {

  private final String europeanaId;
  private final String providerId;
  private final String stringRdf;
  private final String portalRecordLink;
  private final String providerRecordLink;
  private final List<ProcessingError> processingErrors;

  /**
   * Parameter constructor
   *
   * @param europeanaId the europeana id
   * @param providerId the provider id
   * @param stringRdf the rdf in string representation
   * @param portalRecordLink the portal record link
   * @param providerRecordLink the provider record link
   * @param processingErrors the processing errors for the record if any
   */
  public RecordTierCalculationViewGenerator(String europeanaId, String providerId, String stringRdf, String portalRecordLink,
      String providerRecordLink, List<ProcessingError> processingErrors) {
    this.europeanaId = europeanaId;
    this.providerId = providerId;
    this.stringRdf = stringRdf;
    this.portalRecordLink = portalRecordLink;
    this.providerRecordLink = providerRecordLink;
    this.processingErrors = CollectionUtils.isEmpty(processingErrors) ? new ArrayList<>() : new ArrayList<>(processingErrors);
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
                                                                                                          .classify(rdfWrapper);
      RecordTierCalculationSummary recordTierCalculationSummary = new RecordTierCalculationSummary();
      recordTierCalculationSummary.setEuropeanaRecordId(europeanaId);
      recordTierCalculationSummary.setProviderRecordId(providerId);
      recordTierCalculationSummary.setContentTier(mediaTierClassification.getTier());
      recordTierCalculationSummary.setMetadataTier(metadataTierClassification.getTier());
      recordTierCalculationSummary.setPortalRecordLink(portalRecordLink);
      recordTierCalculationSummary.setHarvestedRecordLink(providerRecordLink);

      final ContentTierBreakdown mediaTierClassificationWithErrors = new ContentTierBreakdown(
          mediaTierClassification.getClassification(),
          processingErrors);

      return new RecordTierCalculationView(recordTierCalculationSummary, mediaTierClassificationWithErrors,
          metadataTierClassification.getClassification());
    } catch (SerializationException e) {
      throw new TierCalculationException("Error during calculation of tiers", e);
    }
  }
}
