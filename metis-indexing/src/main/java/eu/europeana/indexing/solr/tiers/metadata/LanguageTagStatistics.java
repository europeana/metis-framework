package eu.europeana.indexing.solr.tiers.metadata;

import eu.europeana.corelib.definitions.jibx.Concept;
import eu.europeana.corelib.definitions.jibx.LiteralType;
import eu.europeana.corelib.definitions.jibx.PlaceType;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
import eu.europeana.corelib.definitions.jibx.TimeSpanType;
import eu.europeana.indexing.utils.RdfWrapper;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

class LanguageTagStatistics {

  enum PropertyType {
    DC_COVERAGE,
    DC_DESCRIPTION,
    DC_FORMAT,
    DC_RELATION,
    DC_RIGHTS,
    DC_SOURCE,
    DC_SUBJECT,
    DC_TITLE,
    DC_TYPE,
    DCTERMS_ALTERNATIVE,
    DCTERMS_HAS_PART,
    DCTERMS_IS_PART_OF,
    DCTERMS_IS_REFERENCED_BY,
    DCTERMS_MEDIUM,
    DCTERMS_PROVENANCE,
    DCTERMS_REFERENCES,
    DCTERMS_SPATIAL,
    DCTERMS_TABLE_OF_CONTENTS,
    DCTERMS_TEMPORAL,
    EDM_CURRENT_LOCATION,
    EDM_HAS_TYPE,
    EDM_IS_RELATED_TO
  }

  private final Set<String> contextualClassesWithValidLanguages = new HashSet<>();
  private final EnumSet<PropertyType> propertiesInEntity = EnumSet.noneOf(PropertyType.class);
  private final EnumSet<PropertyType> propertiesWithLanguage = EnumSet.noneOf(PropertyType.class);

  LanguageTagStatistics(RdfWrapper entity) {
    entity.getPlaces().stream().filter(this::hasValidLanguage).map(PlaceType::getAbout)
        .forEach(contextualClassesWithValidLanguages::add);
    entity.getTimeSpans().stream().filter(this::hasValidLanguage).map(TimeSpanType::getAbout)
        .forEach(contextualClassesWithValidLanguages::add);
    entity.getConcepts().stream().filter(this::hasValidLanguage).map(Concept::getAbout)
        .forEach(contextualClassesWithValidLanguages::add);
  }

  private boolean hasValidLanguage(PlaceType place) {
    return place.getPrefLabelList().stream().anyMatch(this::hasValidLanguage);
  }

  private boolean hasValidLanguage(TimeSpanType timespan) {
    return timespan.getPrefLabelList().stream().anyMatch(this::hasValidLanguage);
  }

  private boolean hasValidLanguage(Concept concept) {
    return concept.getChoiceList().stream().filter(Concept.Choice::ifPrefLabel)
        .map(Concept.Choice::getPrefLabel).anyMatch(this::hasValidLanguage);
  }

  private boolean hasValidLanguage(LiteralType literal) {
    return StringUtils.isNotBlank(literal.getString()) && literal.getLang() != null && StringUtils
        .isNotBlank(literal.getLang().getLang());
  }

  void addToStatistics(LiteralType property, PropertyType type) {
    if (StringUtils.isNotBlank(property.getString())) {
      // If the literal has a value, check whether the language is not empty.
      propertiesInEntity.add(type);
      if (property.getLang() != null && StringUtils.isNotBlank(property.getLang().getLang())) {
        propertiesWithLanguage.add(type);
      }
    }
  }

  void addToStatistics(ResourceOrLiteralType property, PropertyType type) {
    if (StringUtils.isNotBlank(property.getString())) {
      // If the literal has a value, check whether the language is not empty.
      propertiesInEntity.add(type);
      if (property.getLang() != null && StringUtils.isNotBlank(property.getLang().getLang())) {
        propertiesWithLanguage.add(type);
      }
    } else if (property.getResource() != null && StringUtils
        .isNotBlank(property.getResource().getResource())) {
      propertiesInEntity.add(type);
      if (contextualClassesWithValidLanguages.contains(property.getResource().getResource())) {
        propertiesWithLanguage.add(type);
      }
    }
  }

  void addToStatistics(List<? extends ResourceOrLiteralType> properties, PropertyType type) {
    Optional.ofNullable(properties).orElse(Collections.emptyList())
        .forEach(property -> addToStatistics(property, type));
  }

  double getPropertyWithLanguageRatio() {
    return ((double) propertiesWithLanguage.size()) / propertiesInEntity.size();
  }
}
