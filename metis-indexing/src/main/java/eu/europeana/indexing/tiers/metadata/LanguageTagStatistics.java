package eu.europeana.indexing.tiers.metadata;

import eu.europeana.metis.schema.jibx.AboutType;
import eu.europeana.metis.schema.jibx.Concept;
import eu.europeana.metis.schema.jibx.EuropeanaType.Choice;
import eu.europeana.metis.schema.jibx.LiteralType;
import eu.europeana.metis.schema.jibx.PlaceType;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType;
import eu.europeana.metis.schema.jibx.TimeSpanType;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

/**
 * This class is able to compute statistics on language usage in an entity. It counts for which of the defined property types a
 * property is present that:
 * <ol><li>
 * has a language qualification itself, or,
 * <li>refers to a contextual entity (edm:Place, edm:TimeSpan or skos:concept) that has a language
 * qualification (in a skos:prefLabel).</li>
 * </li></ol>
 * It is then able to return for how many of the known property types a language qualification is
 * present.
 */
public class LanguageTagStatistics {

  private final Set<String> contextualClassesWithLanguage = new HashSet<>();
  private final EnumSet<PropertyType> qualifiedProperties = EnumSet.noneOf(PropertyType.class);
  private final EnumSet<PropertyType> qualifiedPropertiesWithLanguage = EnumSet.noneOf(PropertyType.class);

  /**
   * Constructor.
   *
   * @param places The place objects that may be referenced. Can be null or empty, but can't contain null values.
   * @param timeSpans The time span objects that may be referenced. Can be null or empty, but can't contain null values.
   * @param concepts The concept objects that may be referenced. Can be null or empty, but can't contain null values.
   */
  LanguageTagStatistics(List<PlaceType> places, List<TimeSpanType> timeSpans,
      List<Concept> concepts) {
    getStream(places).filter(LanguageTagStatistics::hasValidLanguage).map(AboutType::getAbout)
                     .forEach(contextualClassesWithLanguage::add);
    getStream(timeSpans).filter(LanguageTagStatistics::hasValidLanguage).map(AboutType::getAbout)
                        .forEach(contextualClassesWithLanguage::add);
    getStream(concepts).filter(LanguageTagStatistics::hasValidLanguage).map(AboutType::getAbout)
                       .forEach(contextualClassesWithLanguage::add);
  }

  private static boolean hasValidLanguage(Concept concept) {
    return getStream(concept.getChoiceList()).filter(Concept.Choice::ifPrefLabel)
                                             .map(Concept.Choice::getPrefLabel).anyMatch(LanguageTagStatistics::hasValidLanguage);
  }

  Set<String> getContextualClassesWithLanguage() {
    return Collections.unmodifiableSet(contextualClassesWithLanguage);
  }

  Set<PropertyType> getQualifiedProperties() {
    return Collections.unmodifiableSet(qualifiedProperties);
  }

  Set<PropertyType> getQualifiedPropertiesWithLanguage() {
    return Collections.unmodifiableSet(qualifiedPropertiesWithLanguage);
  }

  boolean containsContextualClass(String about) {
    return contextualClassesWithLanguage.contains(about);
  }

  private static <T> Stream<T> getStream(List<T> list) {
    return Optional.ofNullable(list).stream().flatMap(Collection::stream);
  }

  private static boolean hasValidLanguage(PlaceType place) {
    return getStream(place.getPrefLabelList()).anyMatch(LanguageTagStatistics::hasValidLanguage);
  }

  private static boolean hasValidLanguage(TimeSpanType timespan) {
    return getStream(timespan.getPrefLabelList()).anyMatch(LanguageTagStatistics::hasValidLanguage);
  }

  private static boolean hasValidLanguage(LiteralType literal) {
    return StringUtils.isNotBlank(literal.getString()) && literal.getLang() != null && StringUtils
        .isNotBlank(literal.getLang().getLang());
  }

  /**
   * Adds a property occurrence to the statistics.
   *
   * @param property The value of the property.
   * @param type The type of the property.
   */
  void addToStatistics(LiteralType property, PropertyType type) {
    if (type == null) {
      // Sanity check
      throw new IllegalArgumentException();
    }
    if (property == null) {
      // sanity check.
      return;
    }
    if (StringUtils.isNotBlank(property.getString())) {
      qualifiedProperties.add(type);
      // If the literal has a value, check whether the language is not empty.
      if (property.getLang() != null && StringUtils.isNotBlank(property.getLang().getLang())) {
        qualifiedPropertiesWithLanguage.add(type);
      }
    }
  }

  /**
   * Adds a property occurrence to the statistics.
   *
   * @param property The value of the property.
   * @param type The type of the property.
   */
  void addToStatistics(ResourceOrLiteralType property, PropertyType type) {
    if (type == null) {
      // Sanity check
      throw new IllegalArgumentException();
    }
    if (property == null) {
      // sanity check.
      return;
    }
    if (StringUtils.isNotBlank(property.getString())) {
      qualifiedProperties.add(type);
      // If the property has a value, check whether the language is not empty.
      if (property.getLang() != null && StringUtils.isNotBlank(property.getLang().getLang())) {
        qualifiedPropertiesWithLanguage.add(type);
      }
    }
    if (property.getResource() != null && StringUtils
        .isNotBlank(property.getResource().getResource())) {
      qualifiedProperties.add(type);
      // If the property has a resource link, check whether the link is a contextual class.
      if (containsContextualClass(property.getResource().getResource())) {
        qualifiedPropertiesWithLanguage.add(type);
      }
    }
  }

  /**
   * Adds a property occurrence to the statistics.
   *
   * @param properties The values of the property.
   * @param type The type of the property.
   */
  void addToStatistics(List<? extends ResourceOrLiteralType> properties, PropertyType type) {
    if (type == null) {
      // Sanity check
      throw new IllegalArgumentException();
    }
    if (properties == null) {
      // sanity check.
      return;
    }
    getStream(properties).forEach(property -> addToStatistics(property, type));
  }

  /**
   * Adds a property occurrence to the statistics.
   *
   * @param choice The choice list containing the property value (s). The type of the property is determined based on which
   * element of the choice is set.
   */
  void addToStatistics(Choice choice) {
    if (choice == null) {
      // sanity check.
      return;
    }
    for (ProxyChoiceKind kind : ProxyChoiceKind.values()) {
      kind.valueProcessing.accept(choice, this);
    }
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

    protected final BiConsumer<Choice, LanguageTagStatistics> valueProcessing;

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

  /**
   * Computes for how many of the known property types a language qualification is present. This is returned as a ratio (i.e. a
   * fraction of the total number of known property types). If no properties were added, this method returns 0.
   *
   * @return The ratio.
   */
  double getPropertiesWithLanguageRatio() {
    final Set<PropertyType> addedProperties = getQualifiedProperties();
    if (addedProperties.isEmpty()) {
      return 0;
    }
    return ((double) getQualifiedPropertiesWithLanguage().size()) / addedProperties.size();
  }
}
