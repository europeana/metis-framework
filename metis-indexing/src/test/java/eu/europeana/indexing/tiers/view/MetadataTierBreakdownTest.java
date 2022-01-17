package eu.europeana.indexing.tiers.view;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import eu.europeana.indexing.tiers.metadata.LanguageTagStatistics.PropertyType;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.metis.schema.jibx.PlaceType;
import eu.europeana.metis.schema.jibx.TimeSpanType;
import java.util.List;
import org.junit.jupiter.api.Test;

class MetadataTierBreakdownTest {

  @Test
  void objectCreationTest() {
    MetadataTierBreakdown metadataTierBreakdown = new MetadataTierBreakdown(null, null, null);
    assertNull(metadataTierBreakdown.getLanguageBreakdown());
    assertNull(metadataTierBreakdown.getEnablingElements());
    assertNull(metadataTierBreakdown.getContextualClasses());

    final LanguageBreakdown languageBreakdown = new LanguageBreakdown(2, List.of(PropertyType.DC_COVERAGE.name(),
        PropertyType.DC_DESCRIPTION.name()), MetadataTier.TC);
    EnablingElementsBreakdown enablingElementsBreakdown = new EnablingElementsBreakdown(null, null,
        MetadataTier.TC);
    final ContextualClassesBreakdown contextualClassesBreakdown = new ContextualClassesBreakdown(5,
        List.of(TimeSpanType.class.getSimpleName(), PlaceType.class.getSimpleName()), MetadataTier.TC);

    metadataTierBreakdown = new MetadataTierBreakdown(languageBreakdown, enablingElementsBreakdown, contextualClassesBreakdown);
    assertNotNull(metadataTierBreakdown.getLanguageBreakdown());
    assertNotNull(metadataTierBreakdown.getEnablingElements());
    assertNotNull(metadataTierBreakdown.getContextualClasses());
  }

}