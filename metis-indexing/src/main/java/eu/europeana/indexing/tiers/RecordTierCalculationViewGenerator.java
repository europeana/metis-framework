package eu.europeana.indexing.tiers;

import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.exception.TierCalculationException;
import eu.europeana.indexing.fullbean.RdfToFullBeanConverter;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.model.Tier;
import eu.europeana.indexing.tiers.view.RecordTierCalculationSummary;
import eu.europeana.indexing.tiers.view.RecordTierCalculationView;
import eu.europeana.indexing.utils.RdfTier;
import eu.europeana.indexing.utils.RdfTierUtils;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.convert.SerializationException;
import eu.europeana.metis.schema.jibx.RDF;
import java.util.List;
import java.util.stream.Collectors;

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
   * @return the record tier calculation view
   */
  public RecordTierCalculationView generate() {
    //Create an object that has predefined "fake" values
    RecordTierCalculationView recordTierCalculationView = FakeTierCalculationProvider.getFakeObject();
    final RecordTierCalculationSummary recordTierCalculationSummary = recordTierCalculationView.getRecordTierCalculationSummary();
    final TierValues tierValues = calculateTierValues(stringRdf);

    //Update only the summary which is what we need for now
    // TODO: 04/01/2022 Update the remaining values with the upcoming tickets MET-4157 and MET-4158
    recordTierCalculationSummary.setEuropeanaRecordId(europeanaId);
    recordTierCalculationSummary.setProviderRecordId(providerId);
    recordTierCalculationSummary.setContentTier(tierValues.getContentTier());
    recordTierCalculationSummary.setMetadataTier(tierValues.getMetadataTier());
    recordTierCalculationSummary.setPortalRecordLink(portalRecordLink);
    recordTierCalculationSummary.setHarvestedRecordLink(providerRecordLink);

    return recordTierCalculationView;
  }

  private static TierValues calculateTierValues(String xml) {
    final RDF rdf;
    try {
      // Perform the tier classification
      rdf = RdfConversionUtils.convertStringToRdf(xml);
      final RdfWrapper rdfWrapper = new RdfWrapper(rdf);
      RdfTierUtils.setTier(rdf, ClassifierFactory.getMediaClassifier().classify(rdfWrapper));
      RdfTierUtils.setTier(rdf, ClassifierFactory.getMetadataClassifier().classify(rdfWrapper));

      final RdfToFullBeanConverter fullBeanConverter = new RdfToFullBeanConverter();
      final FullBeanImpl fullBean = fullBeanConverter.convertRdfToFullBean(rdfWrapper);
      final List<RdfTier> rdfTiers = fullBean.getQualityAnnotations().stream().map(RdfTierUtils::getTier).collect(
              Collectors.toList());
      final Tier contentTier = rdfTiers.stream().filter(rdfTier -> rdfTier.getTier() instanceof MediaTier)
          .findFirst().map(RdfTier::getTier).orElse(null);
      final Tier metadataTier = rdfTiers.stream().filter(rdfTier -> rdfTier.getTier() instanceof MetadataTier)
          .findFirst().map(RdfTier::getTier).orElse(null);
      return new TierValues(contentTier, metadataTier);
    } catch (SerializationException | IndexingException e) {
      throw new TierCalculationException("Error during calculation of tiers", e);
    }
  }

  static class TierValues {

    private final Tier contentTier;
    private final Tier metadataTier;

    public TierValues(Tier contentTier, Tier metadataTier) {
      this.contentTier = contentTier;
      this.metadataTier = metadataTier;
    }

    public Tier getContentTier() {
      return contentTier;
    }

    public Tier getMetadataTier() {
      return metadataTier;
    }
  }
}
