package eu.europeana.indexing.tiers.view;

import eu.europeana.indexing.utils.LicenseType;
import eu.europeana.indexing.utils.SolrTier;
import eu.europeana.metis.schema.model.MediaType;
import java.util.Collections;
import java.util.List;

// TODO: 22/12/2021 Remove this when actual implementation ready
public class FakeTierCalculationProvider {

  private FakeTierCalculationProvider() {
  }

  public static RecordTierCalculationDto getFakeObject() {

    final RecordTierCalculationSummary recordTierCalculationSummary = new RecordTierCalculationSummary();
    recordTierCalculationSummary.setEuropeanaRecordId("europeanaRecordId");
    recordTierCalculationSummary.setProviderRecordId("providerRecordId");
    recordTierCalculationSummary.setContentTier(SolrTier.CONTENT_TIER_4.getTierValue());
    recordTierCalculationSummary.setMetadataTier(SolrTier.METADATA_TIER_C.getTierValue());
    recordTierCalculationSummary.setPortalLink("https://example.com");
    recordTierCalculationSummary.setHarvestedRecordLink("https://example.com");

    final ContentTierBreakdown contentTierBreakdown = new ContentTierBreakdown();
    contentTierBreakdown.setRecordType(MediaType.AUDIO);
    contentTierBreakdown.setLicenseType(LicenseType.OPEN);
    contentTierBreakdown.setThumbnailAvailable(true);
    contentTierBreakdown.setLandingPageAvailable(true);
    contentTierBreakdown.setEmbeddableMediaAvailable(true);
    final MediaResourceTechnicalMetadata mediaResourceTechnicalMetadata = new MediaResourceTechnicalMetadata();
    mediaResourceTechnicalMetadata.setResourceUrl("https://example.com");
    mediaResourceTechnicalMetadata.setMediaType("image/jpeg");
    mediaResourceTechnicalMetadata.setElementLinkType("edm:isShownBy");
    mediaResourceTechnicalMetadata.setImageResolution("91.12 megapixel");
    mediaResourceTechnicalMetadata.setVerticalResolution("480 pixels");
    mediaResourceTechnicalMetadata.setLicenceType(LicenseType.RESTRICTED);
    contentTierBreakdown.setMediaResourceTechnicalMetadataList(Collections.singletonList(mediaResourceTechnicalMetadata));
    final ProcessingError processingError1 = new ProcessingError();
    processingError1.setErrorMessage("Error1");
    processingError1.setErrorCode(404);
    final ProcessingError processingError2 = new ProcessingError();
    processingError2.setErrorMessage("Error2");
    processingError2.setErrorCode(500);
    contentTierBreakdown.setProcessingErrorsList(List.of(processingError1, processingError2));

    final MetadataTierBreakdown metadataTierBreakdown = new MetadataTierBreakdown();
    final LanguageBreakdown languageBreakdown = new LanguageBreakdown();
    languageBreakdown.setPotentialLanguageQualifiedElements(42);
    languageBreakdown.setActualLanguageQualifiedElements(34);
    languageBreakdown.setActualLanguageQualifiedElementsPercentage(81);
    languageBreakdown.setActualLanguageUnqualifiedElements(8);
    languageBreakdown.setActualLanguageUnqualifiedElementsList(List.of("dc:creator", "edm:currentLocation"));
    languageBreakdown.setMetadataTier(SolrTier.METADATA_TIER_C.getTierValue());
    metadataTierBreakdown.setLanguageBreakdown(languageBreakdown);
    final EnablingElements enablingElements = new EnablingElements();
    enablingElements.setDistinctEnablingElements(7);
    enablingElements.setDistinctEnablingElementsList(List.of("dc:creator", "edm:currentLocation"));
    enablingElements.setMetadataGroups(2);
    enablingElements.setMetadataGroupsList(List.of("Agent, Place"));
    enablingElements.setMetadataTier(SolrTier.METADATA_TIER_C.getTierValue());
    metadataTierBreakdown.setEnablingElements(enablingElements);
    final ContextualClasses contextualClasses = new ContextualClasses();
    contextualClasses.setCompleteContextualResources(5);
    contextualClasses.setDistinctClassesOfCompleteContextualResources(2);
    contextualClasses.setDistinctClassesList(List.of("edm:TimeSpan", "edm:Place"));
    contextualClasses.setMetadataTier(SolrTier.METADATA_TIER_C.getTierValue());
    metadataTierBreakdown.setContextualClasses(contextualClasses);

    final RecordTierCalculationDto recordTierCalculationDto = new RecordTierCalculationDto();
    recordTierCalculationDto.setRecordTierCalculationSummary(recordTierCalculationSummary);
    recordTierCalculationDto.setContentTierBreakdown(contentTierBreakdown);
    recordTierCalculationDto.setMetadataTierBreakdown(metadataTierBreakdown);

    return recordTierCalculationDto;
  }

}
