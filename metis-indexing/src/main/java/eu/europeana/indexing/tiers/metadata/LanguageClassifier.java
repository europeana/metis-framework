package eu.europeana.indexing.tiers.metadata;

import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.indexing.tiers.metadata.LanguageTagStatistics.PropertyType;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.model.TierClassifier;
import eu.europeana.indexing.utils.RdfWrapper;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

/**
 * Classifier for language tag completeness.
 */
public class LanguageClassifier implements TierClassifier<MetadataTier> {

  private static final float MIN_RATE_FOR_T1 = 0.25F;
  private static final float MIN_RATE_FOR_T2 = 0.5F;
  private static final float MIN_RATE_FOR_T3 = 0.75F;


  @Override
  public MetadataTier classify(RdfWrapper entity) {

    // Compute the language tag ratio
    final double ratio = createLanguageTagStatistics(entity).getPropertyWithLanguageRatio();

    // Convert to tier
    final MetadataTier result;
    if (ratio < MIN_RATE_FOR_T1) {
      result = MetadataTier.T0;
    } else if (ratio < MIN_RATE_FOR_T2) {
      result = MetadataTier.TA;
    } else if (ratio < MIN_RATE_FOR_T3) {
      result = MetadataTier.TB;
    } else {
      result = MetadataTier.TC;
    }

    // Done
    return result;
  }

  LanguageTagStatistics createLanguageTagStatistics(RdfWrapper entity) {
    final LanguageTagStatistics statistics = new LanguageTagStatistics(entity.getPlaces(),
        entity.getTimeSpans(), entity.getConcepts());
    entity.getProviderProxies().stream().filter(Objects::nonNull)
        .forEach(proxy -> addProxyToStatistics(proxy, statistics));
    return statistics;
  }

  void addProxyToStatistics(ProxyType proxy, LanguageTagStatistics statistics) {
    Optional.of(proxy).map(ProxyType::getChoiceList).orElseGet(Collections::emptyList)
        .forEach(statistics::addToStatistics);
    statistics.addToStatistics(proxy.getCurrentLocation(), PropertyType.EDM_CURRENT_LOCATION);
    statistics.addToStatistics(proxy.getHasTypeList(), PropertyType.EDM_HAS_TYPE);
    statistics.addToStatistics(proxy.getIsRelatedToList(), PropertyType.EDM_IS_RELATED_TO);
  }
}
