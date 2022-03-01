package eu.europeana.indexing.tiers.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.europeana.metis.schema.jibx.AltLabel;
import eu.europeana.metis.schema.jibx.Alternative;
import eu.europeana.metis.schema.jibx.Concept;
import eu.europeana.metis.schema.jibx.Coverage;
import eu.europeana.metis.schema.jibx.Description;
import eu.europeana.metis.schema.jibx.EuropeanaType.Choice;
import eu.europeana.metis.schema.jibx.Format;
import eu.europeana.metis.schema.jibx.HasPart;
import eu.europeana.metis.schema.jibx.IsPartOf;
import eu.europeana.metis.schema.jibx.IsReferencedBy;
import eu.europeana.metis.schema.jibx.LiteralType;
import eu.europeana.metis.schema.jibx.LiteralType.Lang;
import eu.europeana.metis.schema.jibx.Medium;
import eu.europeana.metis.schema.jibx.PlaceType;
import eu.europeana.metis.schema.jibx.PrefLabel;
import eu.europeana.metis.schema.jibx.Provenance;
import eu.europeana.metis.schema.jibx.References;
import eu.europeana.metis.schema.jibx.Relation;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType.Resource;
import eu.europeana.metis.schema.jibx.Rights;
import eu.europeana.metis.schema.jibx.Source;
import eu.europeana.metis.schema.jibx.Spatial;
import eu.europeana.metis.schema.jibx.Subject;
import eu.europeana.metis.schema.jibx.TableOfContents;
import eu.europeana.metis.schema.jibx.Temporal;
import eu.europeana.metis.schema.jibx.TimeSpanType;
import eu.europeana.metis.schema.jibx.Title;
import eu.europeana.metis.schema.jibx.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.Test;

class LanguageTagStatisticsTest {

  @Test
  void testConstruction() {

    // Define some about values
    final String about1 = "about1";
    final String about2 = "about2";
    final String about3 = "about3";
    final String about4 = "about4";

    // Create contextual classes for later use. They are all valid to begin with.
    final PlaceType place1 = new PlaceType();
    place1.setPrefLabelList(Collections.singletonList(createPrefLabel("lang1", "value1")));
    place1.setAbout(about1);
    final PlaceType place2 = new PlaceType();
    place2.setPrefLabelList(Collections.singletonList(createPrefLabel("lang2", "value2")));
    place2.setAbout(about2);
    final TimeSpanType timeSpan = new TimeSpanType();
    timeSpan.setPrefLabelList(Collections.singletonList(createPrefLabel("lang3", "value3")));
    timeSpan.setAbout(about3);
    final Concept concept = new Concept();
    final Concept.Choice choiceA = new Concept.Choice();
    choiceA.setPrefLabel(createPrefLabel("lang4a", "value4a"));
    final Concept.Choice choiceB = new Concept.Choice();
    choiceB.setPrefLabel(createPrefLabel("lang4b", "value4b"));
    concept.setChoiceList(Arrays.asList(choiceA, choiceB));
    concept.setAbout(about4);

    // Test that they are all represented.
    final LanguageTagStatistics statistics1 = new LanguageTagStatistics(
        Arrays.asList(place1, place2), Collections.singletonList(timeSpan),
        Collections.singletonList(concept));
    assertEquals(new HashSet<>(Arrays.asList(about1, about2, about3, about4)),
        statistics1.getContextualClassesWithLanguage());
    assertTrue(statistics1.containsContextualClass(about1));
    assertTrue(statistics1.containsContextualClass(about2));
    assertTrue(statistics1.containsContextualClass(about3));
    assertTrue(statistics1.containsContextualClass(about4));

    // Now make some preflabels invalid.
    place1.getPrefLabelList().get(0).setLang(new Lang());
    place2.getPrefLabelList().get(0).setLang(null);
    timeSpan.getPrefLabelList().get(0).setString(" ");
    concept.getChoiceList().get(0).getPrefLabel().getLang().setLang(" ");
    final LanguageTagStatistics statistics2 = new LanguageTagStatistics(
        Arrays.asList(place1, place2), Collections.singletonList(timeSpan),
        Collections.singletonList(concept));
    assertEquals(Collections.singleton(about4), statistics2.getContextualClassesWithLanguage());
    concept.getChoiceList().get(1).getPrefLabel().setString(null);
    final LanguageTagStatistics statistics3 = new LanguageTagStatistics(
        Arrays.asList(place1, place2), Collections.singletonList(timeSpan),
        Collections.singletonList(concept));
    assertEquals(Collections.emptySet(), statistics3.getContextualClassesWithLanguage());

    // Now make some preflabel collections invalid
    place1.setPrefLabelList(Collections.emptyList());
    place2.setPrefLabelList(null);
    concept.getChoiceList().forEach(Concept.Choice::clearChoiceListSelect);
    concept.getChoiceList().get(0).setAltLabel(new AltLabel());
    concept.getChoiceList().get(0).getAltLabel().setString("altLabelValue");
    concept.getChoiceList().get(0).getAltLabel().setLang(createLang("altLabelLanguage"));
    concept.getChoiceList().get(1).setAltLabel(null);
    final LanguageTagStatistics statistics4 = new LanguageTagStatistics(
        Arrays.asList(place1, place2), Collections.singletonList(timeSpan),
        Collections.singletonList(concept));
    assertEquals(Collections.emptySet(), statistics4.getContextualClassesWithLanguage());
    concept.setChoiceList(null);
    final LanguageTagStatistics statistics5 = new LanguageTagStatistics(
        Arrays.asList(place1, place2), Collections.singletonList(timeSpan),
        Collections.singletonList(concept));
    assertEquals(Collections.emptySet(), statistics5.getContextualClassesWithLanguage());
  }

  private PrefLabel createPrefLabel(String language, String value) {
    final PrefLabel label = new PrefLabel();
    label.setLang(createLang(language));
    label.setString(value);
    return label;
  }

  private Lang createLang(String language) {
    final Lang lang = new Lang();
    lang.setLang(language);
    return lang;
  }

  @Test
  void testAddToStatisticsForLiteralType() {

    // Create the mock.
    final LanguageTagStatistics statistics = spy(new LanguageTagStatistics(null, null, null));

    // Create literal types
    final LiteralType validLiteral = createLiteralType("lang", "value");
    final LiteralType invalidLiteral1 = createLiteralType(" ", "value1");
    final LiteralType invalidLiteral2 = createLiteralType(null, "value2");
    final LiteralType invalidLiteral3 = createLiteralType("lang3", null);
    final LiteralType invalidLiteral4 = createLiteralType("lang4", "");
    invalidLiteral2.setLang(null);

    // Create property types
    final PropertyType propertyType1 = PropertyType.DC_FORMAT;
    final PropertyType propertyType2 = PropertyType.EDM_CURRENT_LOCATION;
    final PropertyType propertyType3 = PropertyType.DC_TYPE;
    final PropertyType propertyType4 = PropertyType.EDM_HAS_TYPE;
    final PropertyType propertyType5 = PropertyType.DC_COVERAGE;

    // Test sanity checks
    assertThrows(IllegalArgumentException.class,
        () -> statistics.addToStatistics(validLiteral, null));
    assertThrows(IllegalArgumentException.class,
        () -> statistics.addToStatistics((LiteralType) null, null));
    statistics.addToStatistics((LiteralType) null, propertyType1);
    assertTrue(statistics.getQualifiedProperties().isEmpty());
    assertTrue(statistics.getQualifiedPropertiesWithLanguage().isEmpty());

    // Test for invalid literal types: empty value means not registered at all.
    statistics.addToStatistics(invalidLiteral1, propertyType1);
    statistics.addToStatistics(invalidLiteral2, propertyType2);
    statistics.addToStatistics(invalidLiteral3, propertyType3);
    statistics.addToStatistics(invalidLiteral4, propertyType4);
    assertEquals(EnumSet.of(propertyType1, propertyType2), statistics.getQualifiedProperties());
    assertTrue(statistics.getQualifiedPropertiesWithLanguage().isEmpty());

    // Test for valid literal type: add new type.
    statistics.addToStatistics(validLiteral, propertyType5);
    assertEquals(EnumSet.of(propertyType1, propertyType2, propertyType5),
        statistics.getQualifiedProperties());
    assertEquals(EnumSet.of(propertyType5), statistics.getQualifiedPropertiesWithLanguage());

    // Test for valid literal type: add to already registered type with language.
    statistics.addToStatistics(validLiteral, propertyType5);
    assertEquals(EnumSet.of(propertyType1, propertyType2, propertyType5),
        statistics.getQualifiedProperties());
    assertEquals(EnumSet.of(propertyType5), statistics.getQualifiedPropertiesWithLanguage());

    // Test for valid literal type: add to already registered type without language.
    statistics.addToStatistics(validLiteral, propertyType2);
    assertEquals(EnumSet.of(propertyType1, propertyType2, propertyType5),
        statistics.getQualifiedProperties());
    assertEquals(EnumSet.of(propertyType2, propertyType5),
        statistics.getQualifiedPropertiesWithLanguage());
  }

  private LiteralType createLiteralType(String language, String value) {
    final LiteralType result = new LiteralType();
    result.setString(value);
    result.setLang(createLang(language));
    return result;
  }

  @Test
  void testAddToStatisticsForResourceOrLiteralType() {

    // Create the mock.
    final LanguageTagStatistics statistics = spy(new LanguageTagStatistics(null, null, null));

    // Create valid resource or literal types
    final String resource1 = "resource1";
    final String resource2 = "resource2";
    final String resource3 = "resource3";
    final String resource4 = "resource4";
    doReturn(true).when(statistics).containsContextualClass(resource1);
    doReturn(true).when(statistics).containsContextualClass(resource2);
    doReturn(true).when(statistics).containsContextualClass(resource3);
    doReturn(false).when(statistics).containsContextualClass(resource4);
    final ResourceOrLiteralType valid1 = createResourceOrLiteralType("lang", null, "value1");
    final ResourceOrLiteralType valid2 = createResourceOrLiteralType("lang", resource2, null);
    final ResourceOrLiteralType valid3 = createResourceOrLiteralType("lang", resource3, "value3");
    final ResourceOrLiteralType valid4 = createResourceOrLiteralType("lang", resource4, "value4");

    // Create invalid resource or literal types
    final ResourceOrLiteralType invalid1 = createResourceOrLiteralType(" ", "resourceA", "valueA");
    final ResourceOrLiteralType invalid2 = createResourceOrLiteralType(null, "resourceB", "valueB");
    final ResourceOrLiteralType invalid3 = createResourceOrLiteralType("lang3", null, null);
    final ResourceOrLiteralType invalid4 = createResourceOrLiteralType("lang4", "", " ");
    final ResourceOrLiteralType invalid5 = createResourceOrLiteralType("lang4", resource4, " ");
    invalid2.setLang(null);
    invalid3.setResource(null);

    // Create property types
    final PropertyType propertyType1 = PropertyType.DC_FORMAT;
    final PropertyType propertyType2 = PropertyType.EDM_CURRENT_LOCATION;
    final PropertyType propertyType3 = PropertyType.DC_TYPE;
    final PropertyType propertyType4 = PropertyType.EDM_HAS_TYPE;
    final PropertyType propertyType5 = PropertyType.DC_COVERAGE;
    final PropertyType propertyType6 = PropertyType.DC_DESCRIPTION;

    // Test sanity checks
    assertThrows(IllegalArgumentException.class, () -> statistics.addToStatistics(valid1, null));
    assertThrows(IllegalArgumentException.class,
        () -> statistics.addToStatistics((ResourceOrLiteralType) null, null));
    statistics.addToStatistics((ResourceOrLiteralType) null, propertyType1);
    assertTrue(statistics.getQualifiedProperties().isEmpty());
    assertTrue(statistics.getQualifiedPropertiesWithLanguage().isEmpty());

    // Test for invalid literal types: empty value means not registered at all.
    statistics.addToStatistics(invalid1, propertyType1);
    statistics.addToStatistics(invalid2, propertyType2);
    statistics.addToStatistics(invalid3, propertyType3);
    statistics.addToStatistics(invalid4, propertyType4);
    statistics.addToStatistics(invalid5, propertyType5);
    assertEquals(EnumSet.of(propertyType1, propertyType2, propertyType5),
        statistics.getQualifiedProperties());
    assertTrue(statistics.getQualifiedPropertiesWithLanguage().isEmpty());

    // Test for valid literal type: add new type.
    statistics.addToStatistics(valid1, propertyType3);
    statistics.addToStatistics(valid2, propertyType4);
    statistics.addToStatistics(valid3, propertyType6);
    assertEquals(EnumSet.of(propertyType1, propertyType2, propertyType3, propertyType4,
        propertyType5, propertyType6), statistics.getQualifiedProperties());
    assertEquals(EnumSet.of(propertyType3, propertyType4, propertyType6),
        statistics.getQualifiedPropertiesWithLanguage());

    // Test for valid literal type: add to already registered type with language.
    statistics.addToStatistics(valid4, propertyType3);
    assertEquals(EnumSet.of(propertyType1, propertyType2, propertyType3, propertyType4,
        propertyType5, propertyType6), statistics.getQualifiedProperties());
    assertEquals(EnumSet.of(propertyType3, propertyType4, propertyType6),
        statistics.getQualifiedPropertiesWithLanguage());

    // Test for valid literal type: add to already registered type without language.
    statistics.addToStatistics(valid4, propertyType2);
    assertEquals(EnumSet.of(propertyType1, propertyType2, propertyType3, propertyType4,
        propertyType5, propertyType6), statistics.getQualifiedProperties());
    assertEquals(EnumSet.of(propertyType2, propertyType3, propertyType4, propertyType6),
        statistics.getQualifiedPropertiesWithLanguage());
  }

  private ResourceOrLiteralType createResourceOrLiteralType(String language, String resource,
      String value) {
    final ResourceOrLiteralType result = new ResourceOrLiteralType();
    result.setString(value);
    result.setLang(new ResourceOrLiteralType.Lang());
    result.getLang().setLang(language);
    result.setResource(new Resource());
    result.getResource().setResource(resource);
    return result;
  }

  @Test
  void testAddToStatisticsForResourceOrLiteralTypes() {

    // Create the mock.
    final LanguageTagStatistics statistics = spy(new LanguageTagStatistics(null, null, null));

    // Create some constants
    final ResourceOrLiteralType valid1 = createResourceOrLiteralType("lang", null, "value1");
    final ResourceOrLiteralType valid2 = createResourceOrLiteralType("lang", "resource2", null);
    final PropertyType propertyType = PropertyType.DC_FORMAT;

    // Test sanity check
    assertThrows(IllegalArgumentException.class,
        () -> statistics.addToStatistics(Arrays.asList(valid1, valid2), null));
    assertThrows(IllegalArgumentException.class,
        () -> statistics.addToStatistics((List<ResourceOrLiteralType>) null, null));
    statistics.addToStatistics((List<ResourceOrLiteralType>) null, propertyType);
    verify(statistics, never()).addToStatistics(any(ResourceOrLiteralType.class), any());

    // Test passing on the data
    statistics.addToStatistics(Arrays.asList(valid1, valid2), propertyType);
    verify(statistics, times(1)).addToStatistics(valid1, propertyType);
    verify(statistics, times(1)).addToStatistics(valid2, propertyType);
    verify(statistics, times(2)).addToStatistics(any(ResourceOrLiteralType.class), any());
  }

  @Test
  void testAddToStatisticsForChoice() {

    // Create the mock.
    final LanguageTagStatistics statistics = spy(new LanguageTagStatistics(null, null, null));

    // Test sanity check.
    statistics.addToStatistics(null);
    statistics.addToStatistics(new Choice());
    verify(statistics, never()).addToStatistics(any(ResourceOrLiteralType.class), any());
    verify(statistics, never()).addToStatistics(any(LiteralType.class), any());

    // Test actual values
    testAddToStatisticsForChoice(new Coverage(), Choice::setCoverage, PropertyType.DC_COVERAGE);
    testAddToStatisticsForChoice(new Description(), Choice::setDescription,
        PropertyType.DC_DESCRIPTION);
    testAddToStatisticsForChoice(new Format(), Choice::setFormat, PropertyType.DC_FORMAT);
    testAddToStatisticsForChoice(new Relation(), Choice::setRelation, PropertyType.DC_RELATION);
    testAddToStatisticsForChoice(new Rights(), Choice::setRights, PropertyType.DC_RIGHTS);
    testAddToStatisticsForChoice(new Source(), Choice::setSource, PropertyType.DC_SOURCE);
    testAddToStatisticsForChoice(new Subject(), Choice::setSubject, PropertyType.DC_SUBJECT);
    testAddToStatisticsForChoice(new Title(), Choice::setTitle, PropertyType.DC_TITLE);
    testAddToStatisticsForChoice(new Type(), Choice::setType, PropertyType.DC_TYPE);
    testAddToStatisticsForChoice(new Alternative(), Choice::setAlternative,
        PropertyType.DCTERMS_ALTERNATIVE);
    testAddToStatisticsForChoice(new HasPart(), Choice::setHasPart, PropertyType.DCTERMS_HAS_PART);
    testAddToStatisticsForChoice(new IsPartOf(), Choice::setIsPartOf,
        PropertyType.DCTERMS_IS_PART_OF);
    testAddToStatisticsForChoice(new IsReferencedBy(), Choice::setIsReferencedBy,
        PropertyType.DCTERMS_IS_REFERENCED_BY);
    testAddToStatisticsForChoice(new Medium(), Choice::setMedium, PropertyType.DCTERMS_MEDIUM);
    testAddToStatisticsForChoice(new Provenance(), Choice::setProvenance,
        PropertyType.DCTERMS_PROVENANCE);
    testAddToStatisticsForChoice(new References(), Choice::setReferences,
        PropertyType.DCTERMS_REFERENCES);
    testAddToStatisticsForChoice(new Spatial(), Choice::setSpatial, PropertyType.DCTERMS_SPATIAL);
    testAddToStatisticsForChoice(new TableOfContents(), Choice::setTableOfContents,
        PropertyType.DCTERMS_TABLE_OF_CONTENTS);
    testAddToStatisticsForChoice(new Temporal(), Choice::setTemporal,
        PropertyType.DCTERMS_TEMPORAL);
  }

  private <T extends ResourceOrLiteralType> void testAddToStatisticsForChoice(T value,
      BiConsumer<Choice, T> setter, PropertyType propertyType) {
    final LanguageTagStatistics statistics = spy(new LanguageTagStatistics(null, null, null));
    Choice choice = new Choice();
    setter.accept(choice, value);
    statistics.addToStatistics(choice);
    verify(statistics, times(1)).addToStatistics(value, propertyType);
    verify(statistics, times(1)).addToStatistics(any(ResourceOrLiteralType.class), any());
    verify(statistics, never()).addToStatistics(any(LiteralType.class), any());
  }

  private <T extends LiteralType> void testAddToStatisticsForChoice(T value,
      BiConsumer<Choice, T> setter, PropertyType propertyType) {
    final LanguageTagStatistics statistics = spy(new LanguageTagStatistics(null, null, null));
    Choice choice = new Choice();
    setter.accept(choice, value);
    statistics.addToStatistics(choice);
    verify(statistics, times(1)).addToStatistics(value, propertyType);
    verify(statistics, times(1)).addToStatistics(any(LiteralType.class), any());
    verify(statistics, never()).addToStatistics(any(ResourceOrLiteralType.class), any());
  }

  @Test
  void testGetPropertyWithLanguageRatio() {

    // Create mock
    final LanguageTagStatistics statistics = spy(new LanguageTagStatistics(null, null, null));
    doReturn(EnumSet.of(PropertyType.DC_COVERAGE, PropertyType.DC_TYPE, PropertyType.EDM_HAS_TYPE,
        PropertyType.DC_SOURCE)).when(statistics).getQualifiedProperties();

    // Test with non-zero values
    doReturn(EnumSet.noneOf(PropertyType.class)).when(statistics).getQualifiedPropertiesWithLanguage();
    assertEquals(0, statistics.getPropertiesWithLanguageRatio());
    doReturn(EnumSet.of(PropertyType.DC_COVERAGE)).when(statistics)
                                                  .getQualifiedPropertiesWithLanguage();
    assertEquals(0.25, statistics.getPropertiesWithLanguageRatio());
    doReturn(EnumSet.of(PropertyType.DC_COVERAGE, PropertyType.DC_TYPE)).when(statistics)
                                                                        .getQualifiedPropertiesWithLanguage();
    assertEquals(0.5, statistics.getPropertiesWithLanguageRatio());

    // Test case that nothing has been added.
    doReturn(EnumSet.noneOf(PropertyType.class)).when(statistics).getQualifiedProperties();
    doReturn(EnumSet.noneOf(PropertyType.class)).when(statistics).getQualifiedPropertiesWithLanguage();
    assertEquals(0, statistics.getPropertiesWithLanguageRatio());
  }
}
