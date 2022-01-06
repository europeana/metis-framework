package eu.europeana.indexing.tiers;

import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.exception.TierCalculationException;
import eu.europeana.indexing.fullbean.RdfToFullBeanConverter;
import eu.europeana.indexing.solr.EdmLabel;
import eu.europeana.indexing.tiers.view.RecordTierCalculationSummary;
import eu.europeana.indexing.tiers.view.RecordTierCalculationView;
import eu.europeana.indexing.utils.RdfTierUtils;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.SolrTier;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.convert.SerializationException;
import eu.europeana.metis.schema.jibx.RDF;
import java.util.List;
import java.util.stream.Collectors;

public class RecordTierCalculationViewGenerator {

  private final String europeanaId;
  private final String providerId;
  private final String stringRdf;
  private final String portalRecordLink;
  private final String providerRecordLink;

  public RecordTierCalculationViewGenerator(String europeanaId, String providerId, String stringRdf, String portalRecordLink,
      String providerRecordLink) {
    this.europeanaId = europeanaId;
    this.providerId = providerId;
    this.stringRdf = stringRdf;
    this.portalRecordLink = portalRecordLink;
    this.providerRecordLink = providerRecordLink;
  }

  public RecordTierCalculationView generate() {
    //Create an object that has predefined "fake" values
    RecordTierCalculationView recordTierCalculationView = FakeTierCalculationProvider.getFakeObject();
    final RecordTierCalculationSummary recordTierCalculationSummary = recordTierCalculationView.getRecordTierCalculationSummary();
    final TierValues tierValues = calculateTierValues(stringRdf);

    //Update only the summary which is what we need for now
    // TODO: 04/01/2022 Update the remaining values
    recordTierCalculationSummary.setEuropeanaRecordId(europeanaId);
    recordTierCalculationSummary.setProviderRecordId(providerId);
    recordTierCalculationSummary.setContentTier(tierValues.getContentTier());
    recordTierCalculationSummary.setMetadataTier(tierValues.getMetadataTier());
    recordTierCalculationSummary.setPortalRecordLink(portalRecordLink);
    recordTierCalculationSummary.setHarvestedRecordLink(providerRecordLink);

    return recordTierCalculationView;
  }

  private TierValues calculateTierValues(String xml) {
    final RDF rdf;
    try {
      // Perform the tier classification
      rdf = RdfConversionUtils.convertStringToRdf(xml);
      final RdfWrapper rdfWrapper = new RdfWrapper(rdf);
      RdfTierUtils.setTier(rdf, ClassifierFactory.getMediaClassifier().classify(rdfWrapper));
      RdfTierUtils.setTier(rdf, ClassifierFactory.getMetadataClassifier().classify(rdfWrapper));

      final RdfToFullBeanConverter fullBeanConverter = new RdfToFullBeanConverter();
      final FullBeanImpl fullBean = fullBeanConverter.convertRdfToFullBean(rdfWrapper);
      final List<SolrTier> solrTiers = fullBean.getQualityAnnotations().stream().map(RdfTierUtils::getTier)
          .map(RdfTierUtils::getSolrTier).collect(
              Collectors.toList());
      final String contentTier = solrTiers.stream().filter(solrTier -> solrTier.getTierLabel() == EdmLabel.CONTENT_TIER)
          .findFirst().map(SolrTier::getTierValue).orElse(null);
      final String metadataTier = solrTiers.stream().filter(solrTier -> solrTier.getTierLabel() == EdmLabel.METADATA_TIER)
          .findFirst().map(SolrTier::getTierValue).orElse(null);
      return new TierValues(contentTier, metadataTier);
    } catch (SerializationException | IndexingException e) {
      throw new TierCalculationException("Error during calculation of tiers", e);
    }
  }

  static class TierValues {

    private final String contentTier;
    private final String metadataTier;

    public TierValues(String contentTier, String metadataTier) {
      this.contentTier = contentTier;
      this.metadataTier = metadataTier;
    }

    public String getContentTier() {
      return contentTier;
    }

    public String getMetadataTier() {
      return metadataTier;
    }
  }
}
