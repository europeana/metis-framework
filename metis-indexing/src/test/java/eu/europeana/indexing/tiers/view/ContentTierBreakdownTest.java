package eu.europeana.indexing.tiers.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.view.MediaResourceTechnicalMetadata.MediaResourceTechnicalMetadataBuilder;
import eu.europeana.indexing.tiers.view.ResolutionTierMetadata.ResolutionTierMetadataBuilder;
import eu.europeana.indexing.utils.LicenseType;
import eu.europeana.metis.schema.model.MediaType;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;

class ContentTierBreakdownTest {

  @Test
  void objectCreationTest_Without_Processing_Errors_Parameter() {
    final MediaType recordType = MediaType.IMAGE;
    final LicenseType licenseType = LicenseType.OPEN;
    final boolean thumbnailAvailable = true;
    final boolean landingPageAvailable = true;
    final boolean mediaResource3DAvailable = true;
    final boolean embeddableMediaAvailable = true;
    final MediaResourceTechnicalMetadata mediaResourceTechnicalMetadata1 = new MediaResourceTechnicalMetadataBuilder(
        new ResolutionTierMetadataBuilder().build()).setResourceUrl("resourceUrl").setMediaType(MediaType.IMAGE)
                                                    .setElementLinkTypes(
                                                        Collections.emptySet()).setLicenseType(LicenseType.OPEN)
                                                    .setMediaTier(MediaTier.T4)
                                                    .setMediaTierBeforeLicenseCorrection(MediaTier.T3).build();
    final MediaResourceTechnicalMetadata mediaResourceTechnicalMetadata2 = new MediaResourceTechnicalMetadataBuilder(
        new ResolutionTierMetadataBuilder().build()).setResourceUrl("resourceUrl").setMediaType(MediaType.IMAGE)
                                                    .setElementLinkTypes(
                                                        Collections.emptySet()).setLicenseType(LicenseType.OPEN)
                                                    .setMediaTier(MediaTier.T4)
                                                    .setMediaTierBeforeLicenseCorrection(MediaTier.T3).build();
    final List<MediaResourceTechnicalMetadata> mediaResourceTechnicalMetadataList = List.of(
        mediaResourceTechnicalMetadata1, mediaResourceTechnicalMetadata2);

    final ContentTierBreakdown contentTierBreakdown = new ContentTierBreakdown.Builder()
            .setRecordType(recordType)
            .setLicenseType(licenseType)
            .setThumbnailAvailable(thumbnailAvailable)
            .setLandingPageAvailable(landingPageAvailable)
            .setMediaResource3DAvailable(mediaResource3DAvailable)
            .setEmbeddableMediaAvailable(embeddableMediaAvailable)
            .setMediaResourceTechnicalMetadataList(mediaResourceTechnicalMetadataList)
            .setMediaTierBeforeLicenseCorrection(MediaTier.T3)
            .build();

    assertEquals(recordType, contentTierBreakdown.getRecordType());
    assertEquals(licenseType, contentTierBreakdown.getLicenseType());
    assertEquals(thumbnailAvailable, contentTierBreakdown.isThumbnailAvailable());
    assertEquals(landingPageAvailable, contentTierBreakdown.isLandingPageAvailable());
    assertEquals(mediaResource3DAvailable, contentTierBreakdown.isMediaResource3DAvailable());
    assertEquals(embeddableMediaAvailable, contentTierBreakdown.isEmbeddableMediaAvailable());
    assertEquals(MediaTier.T3, contentTierBreakdown.getMediaTierBeforeLicenseCorrection());
    assertNotSame(mediaResourceTechnicalMetadataList, contentTierBreakdown.getMediaResourceTechnicalMetadataList());
    assertTrue(CollectionUtils.isEqualCollection(mediaResourceTechnicalMetadataList,
        contentTierBreakdown.getMediaResourceTechnicalMetadataList()));

    //Extend now with processing errors
    final ProcessingError processingError1 = new ProcessingError("errorMessage1", "stackTrace1");
    final ProcessingError processingError2 = new ProcessingError("errorMessage2", "stackTrace2");
    final List<ProcessingError> processingErrorList = List.of(processingError1, processingError2);
    final ContentTierBreakdown contentTierBreakdownWithErrors = new ContentTierBreakdown(contentTierBreakdown,
        processingErrorList);
    assertTrue(CollectionUtils.isEqualCollection(processingErrorList, contentTierBreakdownWithErrors.getProcessingErrorsList()));

    //Recheck other fields
    assertEquals(recordType, contentTierBreakdown.getRecordType());
    assertEquals(licenseType, contentTierBreakdown.getLicenseType());
    assertEquals(thumbnailAvailable, contentTierBreakdown.isThumbnailAvailable());
    assertEquals(landingPageAvailable, contentTierBreakdown.isLandingPageAvailable());
    assertEquals(mediaResource3DAvailable, contentTierBreakdown.isMediaResource3DAvailable());
    assertEquals(embeddableMediaAvailable, contentTierBreakdown.isEmbeddableMediaAvailable());
    assertNotSame(mediaResourceTechnicalMetadataList, contentTierBreakdown.getMediaResourceTechnicalMetadataList());
    assertTrue(CollectionUtils.isEqualCollection(mediaResourceTechnicalMetadataList,
        contentTierBreakdown.getMediaResourceTechnicalMetadataList()));

  }
}
