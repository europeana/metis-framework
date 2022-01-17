package eu.europeana.indexing.tiers.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import eu.europeana.indexing.tiers.metadata.ContextualClassesClassifier;
import eu.europeana.indexing.tiers.metadata.EnablingElementsClassifier;
import eu.europeana.indexing.tiers.metadata.LanguageClassifier;
import eu.europeana.indexing.tiers.metadata.LanguageTagStatistics.PropertyType;
import eu.europeana.indexing.tiers.model.TierClassifier.TierClassification;
import eu.europeana.indexing.tiers.view.ContextualClasses;
import eu.europeana.indexing.tiers.view.EnablingElements;
import eu.europeana.indexing.tiers.view.LanguageBreakdown;
import eu.europeana.indexing.tiers.view.MetadataTierBreakdown;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.jibx.PlaceType;
import eu.europeana.metis.schema.jibx.TimeSpanType;
import java.util.Collections;
import java.util.List;
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
    final MetadataTier lowTier = MetadataTier.T0;
    final MetadataTier middleTier = MetadataTier.TA;
    final MetadataTier highTier = MetadataTier.TC;

    assertThrows(NullPointerException.class, () -> new MetadataClassifier(null, null, null));
    assertThrows(NullPointerException.class, () -> new MetadataClassifier(languageClassifier, null, null));
    assertThrows(NullPointerException.class, () -> new MetadataClassifier(languageClassifier, enablingElementsClassifier, null));

    when(languageClassifier.classify(testEntity)).thenReturn(new TierClassification<>(lowTier, new LanguageBreakdown(2,
        List.of(PropertyType.DC_COVERAGE.name(), PropertyType.DC_DESCRIPTION.name()), lowTier)));
    when(enablingElementsClassifier.classify(testEntity)).thenReturn(
        new TierClassification<>(middleTier,
            new EnablingElements(Collections.emptyList(), Collections.emptyList(), MetadataTier.TC)));
    when(contextualClassesClassifier.classify(testEntity)).thenReturn(
        new TierClassification<>(highTier, new ContextualClasses(5,
            List.of(TimeSpanType.class.getSimpleName(), PlaceType.class.getSimpleName()), MetadataTier.TC)));

    final TierClassification<MetadataTier, MetadataTierBreakdown> metadataTierClassification = new MetadataClassifier(
        languageClassifier,
        enablingElementsClassifier, contextualClassesClassifier).classify(testEntity);
    assertEquals(lowTier, metadataTierClassification.getTier());
  }
}
