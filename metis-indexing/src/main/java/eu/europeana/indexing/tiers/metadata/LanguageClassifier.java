package eu.europeana.indexing.tiers.metadata;

import eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
import eu.europeana.indexing.tiers.metadata.LanguageTagStatistics.PropertyType;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.model.TierClassifier;
import eu.europeana.indexing.utils.RdfWrapper;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Classifier for language tag completeness.
 */
public class LanguageClassifier implements TierClassifier<MetadataTier> {

  private static final float MIN_RATE_FOR_T1 = 0.25F;
  private static final float MIN_RATE_FOR_T2 = 0.5F;
  private static final float MIN_RATE_FOR_T3 = 0.75F;


  @Override
  public MetadataTier classify(RdfWrapper entity) {

    // Analyze the provider proxies.
    final LanguageTagStatistics statistics = new LanguageTagStatistics(entity);
    entity.getProviderProxies().stream().filter(Objects::nonNull)
        .forEach(proxy -> analyzeProxy(proxy, statistics));

    // Compute the tier.
    final double ratio = statistics.getPropertyWithLanguageRatio();
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

  private void analyzeProxy(ProxyType proxy, LanguageTagStatistics statistics) {
    Optional.of(proxy).map(ProxyType::getChoiceList).orElseGet(Collections::emptyList)
        .forEach(choice -> analyzeProxyChoice(choice, statistics));
    statistics.addToStatistics(proxy.getCurrentLocation(), PropertyType.EDM_CURRENT_LOCATION);
    statistics.addToStatistics(proxy.getHasTypeList(), PropertyType.EDM_HAS_TYPE);
    statistics.addToStatistics(proxy.getIsRelatedToList(), PropertyType.EDM_IS_RELATED_TO);
  }

  private enum ProxyChoiceKind {

    DC_COVERAGE(Choice::ifCoverage, Choice::getCoverage, PropertyType.DC_COVERAGE),
    DC_DESCRIPTION(Choice::ifDescription, Choice::getDescription, PropertyType.DC_DESCRIPTION),
    DC_FORMAT(Choice::ifFormat, Choice::getFormat, PropertyType.DC_FORMAT),
    DC_RELATION(Choice::ifRelation, Choice::getRelation, PropertyType.DC_RELATION),
    DC_RIGHTS(Choice::ifRights, Choice::getRights, PropertyType.DC_RIGHTS),
    DC_SOURCE(Choice::ifSource, Choice::getSource, PropertyType.DC_SOURCE),
    DC_SUBJECT(Choice::ifSubject, Choice::getSubject, PropertyType.DC_SUBJECT),
    DC_TITLE(Choice::ifTitle, Choice::getTitle, PropertyType.DC_TITLE,
        LanguageTagStatistics::addToStatistics),
    DC_TYPE(Choice::ifType, Choice::getType, PropertyType.DC_TYPE),
    DCTERMS_ALTERNATIVE(Choice::ifAlternative, Choice::getAlternative,
        PropertyType.DCTERMS_ALTERNATIVE, LanguageTagStatistics::addToStatistics),
    DCTERMS_HAS_PART(Choice::ifHasPart, Choice::getHasPart, PropertyType.DCTERMS_HAS_PART),
    DCTERMS_IS_PART_OF(Choice::ifIsPartOf, Choice::getIsPartOf, PropertyType.DCTERMS_IS_PART_OF),
    DCTERMS_IS_REFERENCED_BY(Choice::ifIsReferencedBy, Choice::getIsReferencedBy,
        PropertyType.DCTERMS_IS_REFERENCED_BY),
    DCTERMS_MEDIUM(Choice::ifMedium, Choice::getMedium, PropertyType.DCTERMS_MEDIUM),
    DCTERMS_PROVENANCE(Choice::ifProvenance, Choice::getProvenance,
        PropertyType.DCTERMS_PROVENANCE),
    DCTERMS_REFERENCES(Choice::ifReferences, Choice::getReferences,
        PropertyType.DCTERMS_REFERENCES),
    DCTERMS_SPATIAL(Choice::ifSpatial, Choice::getSpatial, PropertyType.DCTERMS_SPATIAL),
    DCTERMS_TABLE_OF_CONTENTS(Choice::ifTableOfContents, Choice::getTableOfContents,
        PropertyType.DCTERMS_TABLE_OF_CONTENTS),
    DCTERMS_TEMPORAL(Choice::ifTemporal, Choice::getTemporal, PropertyType.DCTERMS_TEMPORAL);

    private final BiConsumer<Choice, LanguageTagStatistics> valueProcessing;

    ProxyChoiceKind(Predicate<Choice> choiceSelection,
        Function<Choice, ResourceOrLiteralType> valueExtraction, PropertyType type) {
      this(choiceSelection, valueExtraction, type, LanguageTagStatistics::addToStatistics);
    }

    <T> ProxyChoiceKind(Predicate<Choice> choiceSelection, Function<Choice, T> valueExtraction,
        PropertyType type, InclusionInStatistics<T> inclusionInStatistics) {
      this.valueProcessing = (choice, statistics) -> {
        if (choiceSelection.test(choice)) {
          inclusionInStatistics
              .includeInStatistics(statistics, valueExtraction.apply(choice), type);
        }
      };
    }

    @FunctionalInterface
    interface InclusionInStatistics<T> {

      /**
       * Adds the value to the statistics.
       *
       * @param stats The statistics to which to add the value.
       * @param value The value to add.
       * @param type The type of the value to add.
       */
      void includeInStatistics(LanguageTagStatistics stats, T value, PropertyType type);
    }
  }

  private void analyzeProxyChoice(Choice choice, LanguageTagStatistics statistics) {
    for (ProxyChoiceKind kind : ProxyChoiceKind.values()) {
      kind.valueProcessing.accept(choice, statistics);
    }
  }
}
