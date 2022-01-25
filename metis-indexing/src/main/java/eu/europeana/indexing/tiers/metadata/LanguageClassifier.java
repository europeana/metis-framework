package eu.europeana.indexing.tiers.metadata;

import eu.europeana.indexing.tiers.metadata.LanguageTagStatistics.PropertyType;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.model.TierClassifierBreakdown;
import eu.europeana.indexing.tiers.view.LanguageBreakdown;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.jibx.ProxyType;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * Classifier for language tag completeness.
 */
public class LanguageClassifier implements TierClassifierBreakdown<LanguageBreakdown> {

  private static final float MIN_RATE_FOR_T1 = 0.25F;
  private static final float MIN_RATE_FOR_T2 = 0.5F;
  private static final float MIN_RATE_FOR_T3 = 0.75F;

  private static <T> Set<T> differenceOfSets(final Set<T> setOne, final Set<T> setTwo) {
    Set<T> result = new HashSet<>(setOne);
    result.removeIf(setTwo::contains);
    return result;
  }

  @Override
  public LanguageBreakdown classifyBreakdown(RdfWrapper entity) {

    final LanguageTagStatistics languageTagStatistics = createLanguageTagStatistics(entity);
    final Set<PropertyType> qualifiedProperties = languageTagStatistics.getQualifiedProperties();
    final Set<PropertyType> qualifiedPropertiesWithLanguage = languageTagStatistics.getQualifiedPropertiesWithLanguage();
    final Set<PropertyType> qualifiedPropertiesWithoutLanguage = differenceOfSets(qualifiedProperties,
        qualifiedPropertiesWithLanguage);

    final MetadataTier metadataTier = calculateMetadataTier(languageTagStatistics.getPropertiesWithLanguageRatio());

    return new LanguageBreakdown(qualifiedProperties.size(),
        qualifiedPropertiesWithoutLanguage.stream().map(PropertyType::getTypedClass)
                                          .map(RdfConversionUtils::getQualifiedElementNameForClass).collect(Collectors.toSet()),
        metadataTier);
  }

  @NotNull
  private MetadataTier calculateMetadataTier(double propertiesWithLanguageRatio) {
    final MetadataTier metadataTier;
    if (propertiesWithLanguageRatio < MIN_RATE_FOR_T1) {
      metadataTier = MetadataTier.T0;
    } else if (propertiesWithLanguageRatio < MIN_RATE_FOR_T2) {
      metadataTier = MetadataTier.TA;
    } else if (propertiesWithLanguageRatio < MIN_RATE_FOR_T3) {
      metadataTier = MetadataTier.TB;
    } else {
      metadataTier = MetadataTier.TC;
    }
    return metadataTier;
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
