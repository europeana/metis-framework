package eu.europeana.indexing.solr.property;

import eu.europeana.corelib.definitions.edm.entity.QualityAnnotation;
import eu.europeana.indexing.solr.EdmLabel;
import eu.europeana.indexing.utils.RdfTier;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.solr.common.SolrInputDocument;

/**
 * Property Solr Creator for 'dqv:QualityAnnotation' tags.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2019-06-13
 */
public class QualityAnnotationSolrCreator implements PropertySolrCreator<QualityAnnotation> {

  private enum SolrTier {

    METADATA_TIER_0(RdfTier.METADATA_TIER_0, EdmLabel.METADATA_TIER, "0"),
    METADATA_TIER_A(RdfTier.METADATA_TIER_A, EdmLabel.METADATA_TIER, "A"),
    METADATA_TIER_B(RdfTier.METADATA_TIER_B, EdmLabel.METADATA_TIER, "B"),
    METADATA_TIER_C(RdfTier.METADATA_TIER_C, EdmLabel.METADATA_TIER, "C"),

    CONTENT_TIER_0(RdfTier.CONTENT_TIER_0, EdmLabel.CONTENT_TIER, "0"),
    CONTENT_TIER_1(RdfTier.CONTENT_TIER_1, EdmLabel.CONTENT_TIER, "1"),
    CONTENT_TIER_2(RdfTier.CONTENT_TIER_2, EdmLabel.CONTENT_TIER, "2"),
    CONTENT_TIER_3(RdfTier.CONTENT_TIER_3, EdmLabel.CONTENT_TIER, "3"),
    CONTENT_TIER_4(RdfTier.CONTENT_TIER_4, EdmLabel.CONTENT_TIER, "4");

    private final RdfTier tier;
    private final EdmLabel tierLabel;
    private final String tierValue;

    SolrTier(RdfTier tier, EdmLabel tierLabel, String tierValue) {
      this.tier = tier;
      this.tierLabel = tierLabel;
      this.tierValue = tierValue;
    }

    public RdfTier getTier() {
      return tier;
    }

    public EdmLabel getTierLabel() {
      return tierLabel;
    }

    public String getTierValue() {
      return tierValue;
    }
  }

  private static Map<RdfTier, SolrTier> tiers = Collections.unmodifiableMap(
      Stream.of(SolrTier.values())
          .collect(Collectors.toMap(SolrTier::getTier, Function.identity())));

  @Override
  public void addToDocument(SolrInputDocument doc, QualityAnnotation qualityAnnotation) {
    Optional.of(qualityAnnotation).map(RdfTier::getTier).map(tiers::get).ifPresent(
        tier -> SolrPropertyUtils.addValue(doc, tier.getTierLabel(), tier.getTierValue())
    );
  }
}
