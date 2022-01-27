package eu.europeana.indexing.tiers.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.europeana.indexing.tiers.metadata.PropertyType;
import eu.europeana.indexing.tiers.model.MetadataTier;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LanguageBreakdownTest {

  @Test
  void objectCreationTest() {
    final Set<String> qualifiedElementsWithoutLanguage = Set.of(PropertyType.DC_COVERAGE.name(),
        PropertyType.DC_DESCRIPTION.name());
    final MetadataTier metadataTier = MetadataTier.TC;
    assertThrows(IllegalArgumentException.class, () -> new LanguageBreakdown(0, qualifiedElementsWithoutLanguage, metadataTier));

    LanguageBreakdown languageBreakdown = new LanguageBreakdown(2, null, metadataTier);
    assertEquals(0, languageBreakdown.getQualifiedElementsWithoutLanguageList().size());

    languageBreakdown = new LanguageBreakdown(2, qualifiedElementsWithoutLanguage, metadataTier);
    assertEquals(2, languageBreakdown.getQualifiedElements());
    assertEquals(0, languageBreakdown.getQualifiedElementsWithLanguage());
    assertEquals(0F, languageBreakdown.getQualifiedElementsWithLanguagePercentage());
    assertEquals(qualifiedElementsWithoutLanguage, languageBreakdown.getQualifiedElementsWithoutLanguageList());
    assertEquals(metadataTier, languageBreakdown.getTier());
  }

}