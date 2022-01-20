package eu.europeana.indexing.tiers.view;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import eu.europeana.indexing.tiers.metadata.EnablingElement;
import eu.europeana.indexing.tiers.metadata.EnablingElement.EnablingElementGroup;
import eu.europeana.indexing.tiers.metadata.LanguageTagStatistics.PropertyType;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.view.MediaResourceTechnicalMetadata.MediaResourceTechnicalMetadataBuilder;
import eu.europeana.indexing.utils.LicenseType;
import eu.europeana.indexing.utils.WebResourceLinkType;
import eu.europeana.metis.schema.jibx.PlaceType;
import eu.europeana.metis.schema.jibx.TimeSpanType;
import eu.europeana.metis.schema.model.MediaType;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RecordTierCalculationViewTest {

  @Test
  void objectCreationTest() {
    final LanguageBreakdown languageBreakdown = new LanguageBreakdown(2, List.of(PropertyType.DC_COVERAGE.name(),
        PropertyType.DC_DESCRIPTION.name()), MetadataTier.TC);
    EnablingElementsBreakdown enablingElementsBreakdown = new EnablingElementsBreakdown(List.of(EnablingElement.DC_CREATOR.name(),
        EnablingElement.EDM_CURRENT_LOCATION.name()), List.of(EnablingElementGroup.PERSONAL.name(),
        EnablingElementGroup.GEOGRAPHICAL.name()),
        MetadataTier.TC);
    final List<String> distinctClassesList = List.of(TimeSpanType.class.getSimpleName(), PlaceType.class.getSimpleName());
    final int completeContextualResources = 5;
    ContextualClassesBreakdown contextualClassesBreakdown = new ContextualClassesBreakdown(completeContextualResources,
        distinctClassesList, MetadataTier.TC);

    final MediaResourceTechnicalMetadata mediaResourceTechnicalMetadata =
        new MediaResourceTechnicalMetadataBuilder().setResourceUrl("https://example.com")
                                                   .setMediaType(MediaType.getMediaType("image/jpeg"))
                                                   .setMimeType("image/jpeg")
                                                   .setElementLinkTypes(Set.of(WebResourceLinkType.IS_SHOWN_AT))
                                                   .setImageResolution(
                                                       100_000L).setImageResolutionTier(MediaTier.T1)
                                                   .setVerticalResolution(0L)
                                                   .setVerticalResolutionTier(null)
                                                   .setLicenseType(
                                                       LicenseType.RESTRICTED)
                                                   .setMediaTier(
                                                       MediaTier.T1)
                                                   .createMediaResourceTechnicalMetadata();

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