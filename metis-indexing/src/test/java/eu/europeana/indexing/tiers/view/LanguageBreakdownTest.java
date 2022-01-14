package eu.europeana.indexing.tiers.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.europeana.indexing.tiers.metadata.LanguageTagStatistics.PropertyType;
import eu.europeana.indexing.tiers.model.MetadataTier;
import java.util.List;
import org.junit.jupiter.api.Test;

class LanguageBreakdownTest {

  @Test
  void objectCreationTest() {
    final List<String> qualifiedElementsWithoutLanguage = List.of(PropertyType.DC_COVERAGE.name(),
        PropertyType.DC_DESCRIPTION.name());
    final MetadataTier metadataTier = MetadataTier.TC;
    final LanguageBreakdown languageBreakdown = new LanguageBreakdown(2, qualifiedElementsWithoutLanguage, metadataTier);
    assertEquals(2, languageBreakdown.getQualifiedElements());
    assertEquals(0, languageBreakdown.getQualifiedElementsWithLanguage());
    assertEquals(0F, languageBreakdown.getQualifiedElementsWithLanguagePercentage());
    assertEquals(qualifiedElementsWithoutLanguage.size(), languageBreakdown.getQualifiedElementsWithoutLanguage());
    assertEquals(qualifiedElementsWithoutLanguage, languageBreakdown.getQualifiedElementsWithoutLanguageList());
    assertEquals(metadataTier, languageBreakdown.getMetadataTier());
  }

}