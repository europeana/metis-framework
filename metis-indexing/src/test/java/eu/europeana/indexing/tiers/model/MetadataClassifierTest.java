package eu.europeana.indexing.tiers.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import eu.europeana.indexing.tiers.metadata.ContextualClassesClassifier;
import eu.europeana.indexing.tiers.metadata.EnablingElementsClassifier;
import eu.europeana.indexing.tiers.metadata.LanguageClassifier;
import eu.europeana.indexing.tiers.metadata.LanguageTagStatistics.PropertyType;
import eu.europeana.indexing.tiers.model.TierClassifier.TierClassification;
import eu.europeana.indexing.tiers.view.ContextualClassesBreakdown;
import eu.europeana.indexing.tiers.view.EnablingElementsBreakdown;
import eu.europeana.indexing.tiers.view.LanguageBreakdown;
import eu.europeana.indexing.tiers.view.MetadataTierBreakdown;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.jibx.PlaceType;
import eu.europeana.metis.schema.jibx.TimeSpanType;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class MetadataClassifierTest {

  @Test
  void testClassify() {

    // The entity under consideration
    final RdfWrapper testEntity = mock(RdfWrapper.class);
    final LanguageClassifier languageClassifier = mock(LanguageClassifier.class);
    final EnablingElementsClassifier enablingElementsClassifier = mock(EnablingElementsClassifier.class);
    final ContextualClassesClassifier contextualClassesClassifier = mock(ContextualClassesClassifier.class);

    // The tiers
    final Tier lowTier = MetadataTier.T0;
    final Tier middleTier = MetadataTier.TA;
    final Tier highTier = MetadataTier.TC;

    assertThrows(NullPointerException.class, () -> new MetadataClassifier(null, null, null));
    assertThrows(NullPointerException.class, () -> new MetadataClassifier(languageClassifier, null, null));
    assertThrows(NullPointerException.class, () -> new MetadataClassifier(languageClassifier, enablingElementsClassifier, null));

    when(languageClassifier.classifyBreakdown(testEntity)).thenReturn(new LanguageBreakdown(2,
        Set.of(PropertyType.DC_COVERAGE.name(), PropertyType.DC_DESCRIPTION.name()), lowTier));
    when(enablingElementsClassifier.classifyBreakdown(testEntity)).thenReturn(
        new EnablingElementsBreakdown(Collections.emptySet(), Collections.emptySet(), middleTier));
    when(contextualClassesClassifier.classifyBreakdown(testEntity)).thenReturn(
        new ContextualClassesBreakdown(5,
            Set.of(TimeSpanType.class.getSimpleName(), PlaceType.class.getSimpleName()), highTier));

    final TierClassification<Tier, MetadataTierBreakdown> metadataTierClassification = new MetadataClassifier(
        languageClassifier, enablingElementsClassifier, contextualClassesClassifier).classify(testEntity);
    assertEquals(lowTier, metadataTierClassification.getTier());
    assertNotNull(metadataTierClassification.getClassification());
  }
}
