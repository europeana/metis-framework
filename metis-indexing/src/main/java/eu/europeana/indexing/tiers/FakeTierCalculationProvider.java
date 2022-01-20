package eu.europeana.indexing.tiers;

import eu.europeana.indexing.tiers.metadata.EnablingElement;
import eu.europeana.indexing.tiers.metadata.EnablingElement.EnablingElementGroup;
import eu.europeana.indexing.tiers.metadata.LanguageTagStatistics.PropertyType;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.view.ContentTierBreakdown;
import eu.europeana.indexing.tiers.view.ContextualClassesBreakdown;
import eu.europeana.indexing.tiers.view.EnablingElementsBreakdown;
import eu.europeana.indexing.tiers.view.LanguageBreakdown;
import eu.europeana.indexing.tiers.view.MediaResourceTechnicalMetadata;
import eu.europeana.indexing.tiers.view.MediaResourceTechnicalMetadata.MediaResourceTechnicalMetadataBuilder;
import eu.europeana.indexing.tiers.view.MetadataTierBreakdown;
import eu.europeana.indexing.tiers.view.ProcessingError;
import eu.europeana.indexing.tiers.view.RecordTierCalculationSummary;
import eu.europeana.indexing.tiers.view.RecordTierCalculationView;
import eu.europeana.indexing.utils.LicenseType;
import eu.europeana.indexing.utils.WebResourceLinkType;
import eu.europeana.metis.schema.jibx.PlaceType;
import eu.europeana.metis.schema.jibx.TimeSpanType;
import eu.europeana.metis.schema.model.MediaType;
import java.util.Collections;
import java.util.List;
import java.util.Set;

// TODO: 22/12/2021 Remove this when actual implementation ready
public class FakeTierCalculationProvider {

  private FakeTierCalculationProvider() {
  }

  public static RecordTierCalculationView getFakeObject() {

    final RecordTierCalculationSummary recordTierCalculationSummary = new RecordTierCalculationSummary();
    recordTierCalculationSummary.setEuropeanaRecordId("europeanaRecordId");
    recordTierCalculationSummary.setProviderRecordId("providerRecordId");
    recordTierCalculationSummary.setContentTier(MediaTier.T4);
    recordTierCalculationSummary.setMetadataTier(MetadataTier.TC);
    recordTierCalculationSummary.setPortalRecordLink("https://example.com");
    recordTierCalculationSummary.setHarvestedRecordLink("https://example.com");

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
    final ContentTierBreakdown contentTierBreakdown = new ContentTierBreakdown(MediaType.AUDIO, LicenseType.OPEN, true,
        true, true, Collections.singletonList(mediaResourceTechnicalMetadata));
    final ProcessingError processingError1 = new ProcessingError();
    processingError1.setErrorMessage("Error1");
    processingError1.setErrorCode(404);
    final ProcessingError processingError2 = new ProcessingError();
    processingError2.setErrorMessage("Error2");
    processingError2.setErrorCode(500);
    contentTierBreakdown.setProcessingErrorsList(List.of(processingError1, processingError2));

    final LanguageBreakdown languageBreakdown = new LanguageBreakdown(42,
        List.of(PropertyType.DC_COVERAGE.name(), PropertyType.DC_DESCRIPTION.name()),
        MetadataTier.TC);
    final EnablingElementsBreakdown enablingElementsBreakdown = new EnablingElementsBreakdown(
        List.of(EnablingElement.DC_CREATOR.name(), EnablingElement.EDM_CURRENT_LOCATION.name()),
        List.of(EnablingElementGroup.PERSONAL.name(), EnablingElementGroup.GEOGRAPHICAL.name()), MetadataTier.TC);
    final ContextualClassesBreakdown contextualClassesBreakdown = new ContextualClassesBreakdown(5,
        List.of(TimeSpanType.class.getSimpleName(), PlaceType.class.getSimpleName()), MetadataTier.TC);

    final MetadataTierBreakdown metadataTierBreakdown = new MetadataTierBreakdown(languageBreakdown, enablingElementsBreakdown,
        contextualClassesBreakdown);

    return new RecordTierCalculationView(recordTierCalculationSummary,
        contentTierBreakdown, metadataTierBreakdown);
  }

}
