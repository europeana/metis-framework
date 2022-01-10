package eu.europeana.indexing.tiers;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.view.ContentTierBreakdown;
import eu.europeana.indexing.tiers.view.ContextualClasses;
import eu.europeana.indexing.tiers.view.EnablingElements;
import eu.europeana.indexing.tiers.view.LanguageBreakdown;
import eu.europeana.indexing.tiers.view.MediaResourceTechnicalMetadata;
import eu.europeana.indexing.tiers.view.MetadataTierBreakdown;
import eu.europeana.indexing.tiers.view.ProcessingError;
import eu.europeana.indexing.tiers.view.RecordTierCalculationSummary;
import eu.europeana.indexing.tiers.view.RecordTierCalculationView;
import eu.europeana.indexing.utils.LicenseType;
import eu.europeana.metis.schema.model.MediaType;
import java.util.Collections;
import java.util.List;

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
    mediaResourceTechnicalMetadata.setLicenseType(LicenseType.RESTRICTED);
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
    languageBreakdown.setActualLanguageQualifiedElementsPercentage(81F);
    languageBreakdown.setActualLanguageUnqualifiedElements(8);
    languageBreakdown.setActualLanguageUnqualifiedElementsList(List.of("dc:creator", "edm:currentLocation"));
    languageBreakdown.setMetadataTier(MetadataTier.TC);
    metadataTierBreakdown.setLanguageBreakdown(languageBreakdown);
    final EnablingElements enablingElements = new EnablingElements();
    enablingElements.setDistinctEnablingElements(7);
    enablingElements.setDistinctEnablingElementsList(List.of("dc:creator", "edm:currentLocation"));
    enablingElements.setMetadataGroups(2);
    enablingElements.setMetadataGroupsList(List.of("Agent, Place"));
    enablingElements.setMetadataTier(MetadataTier.TC);
    metadataTierBreakdown.setEnablingElements(enablingElements);
    final ContextualClasses contextualClasses = new ContextualClasses();
    contextualClasses.setCompleteContextualResources(5);
    contextualClasses.setDistinctClassesOfCompleteContextualResources(2);
    contextualClasses.setDistinctClassesList(List.of("edm:TimeSpan", "edm:Place"));
    contextualClasses.setMetadataTier(MetadataTier.TC);
    metadataTierBreakdown.setContextualClasses(contextualClasses);

    final RecordTierCalculationView recordTierCalculationDto = new RecordTierCalculationView();
    recordTierCalculationDto.setRecordTierCalculationSummary(recordTierCalculationSummary);
    recordTierCalculationDto.setContentTierBreakdown(contentTierBreakdown);
    recordTierCalculationDto.setMetadataTierBreakdown(metadataTierBreakdown);

    return recordTierCalculationDto;
  }

}
