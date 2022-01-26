package eu.europeana.indexing.tiers.view;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import eu.europeana.indexing.tiers.metadata.ContextualClassGroup;
import eu.europeana.indexing.tiers.metadata.EnablingElement;
import eu.europeana.indexing.tiers.metadata.LanguageTagStatistics.PropertyType;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.view.ResolutionTierMetadata.ResolutionTierMetadataBuilder;
import eu.europeana.indexing.utils.LicenseType;
import eu.europeana.indexing.utils.WebResourceLinkType;
import eu.europeana.metis.schema.jibx.PlaceType;
import eu.europeana.metis.schema.jibx.TimeSpanType;
import eu.europeana.metis.schema.model.MediaType;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RecordTierCalculationViewTest {

  @Test
  void objectCreationTest() {
    final LanguageBreakdown languageBreakdown = new LanguageBreakdown(2, Set.of(PropertyType.DC_COVERAGE.name(),
        PropertyType.DC_DESCRIPTION.name()), MetadataTier.TC);
    EnablingElementsBreakdown enablingElementsBreakdown = new EnablingElementsBreakdown(Set.of(EnablingElement.DC_CREATOR.name(),
        EnablingElement.EDM_CURRENT_LOCATION.name()), Set.of(ContextualClassGroup.PERSONAL.name(),
        ContextualClassGroup.GEOGRAPHICAL.name()),
        MetadataTier.TC);
    final Set<String> distinctClassesList = Set.of(TimeSpanType.class.getSimpleName(), PlaceType.class.getSimpleName());
    final int completeContextualResources = 5;
    ContextualClassesBreakdown contextualClassesBreakdown = new ContextualClassesBreakdown(completeContextualResources,
        distinctClassesList, MetadataTier.TC);

    final ResolutionTierMetadata resolutionTierData = new ResolutionTierMetadataBuilder().build();
    final MediaResourceTechnicalMetadata mediaResourceTechnicalMetadata =
        new MediaResourceTechnicalMetadata.MediaResourceTechnicalMetadataBuilder(resolutionTierData)
            .setResourceUrl("https://example.com")
            .setMediaType(MediaType.getMediaType("image/jpeg"))
            .setMimeType("image/jpeg")
            .setElementLinkTypes(Set.of(WebResourceLinkType.IS_SHOWN_AT))
            .setLicenseType(LicenseType.RESTRICTED)
            .setMediaTier(MediaTier.T1)
            .build();

    final RecordTierCalculationView recordTierCalculationView = new RecordTierCalculationView(new RecordTierCalculationSummary(),
        new ContentTierBreakdown(MediaType.AUDIO, LicenseType.OPEN, true,
            true, true, Collections.singletonList(mediaResourceTechnicalMetadata)),
        new MetadataTierBreakdown(languageBreakdown, enablingElementsBreakdown,
            contextualClassesBreakdown));

    assertNotNull(recordTierCalculationView.getRecordTierCalculationSummary());
    assertNotNull(recordTierCalculationView.getContentTierBreakdown());
    assertNotNull(recordTierCalculationView.getMetadataTierBreakdown());
  }

}