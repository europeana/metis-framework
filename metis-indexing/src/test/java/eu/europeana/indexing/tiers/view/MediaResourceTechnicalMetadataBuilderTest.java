package eu.europeana.indexing.tiers.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.view.MediaResourceTechnicalMetadata.MediaResourceTechnicalMetadataBuilder;
import eu.europeana.indexing.tiers.view.ResolutionTierMetadata.ResolutionTierMetadataBuilder;
import eu.europeana.indexing.utils.LicenseType;
import eu.europeana.indexing.utils.WebResourceLinkType;
import eu.europeana.metis.schema.model.MediaType;
import java.util.Collections;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;

class MediaResourceTechnicalMetadataBuilderTest {

  @Test
  void objectCreationTest() {
    final ResolutionTierMetadata resolutionTierMetadata = new ResolutionTierMetadataBuilder().build();
    final MediaResourceTechnicalMetadataBuilder mediaResourceTechnicalMetadataBuilder = new MediaResourceTechnicalMetadataBuilder(
        resolutionTierMetadata);

    //Check field errors
    assertThrows(NullPointerException.class, mediaResourceTechnicalMetadataBuilder::build);
    mediaResourceTechnicalMetadataBuilder.setResourceUrl("");
    assertThrows(IllegalArgumentException.class, mediaResourceTechnicalMetadataBuilder::build);
    final String resourceUrl = "resourceUrl";
    mediaResourceTechnicalMetadataBuilder.setResourceUrl(resourceUrl);
    assertThrows(NullPointerException.class, mediaResourceTechnicalMetadataBuilder::build);
    final MediaType mediaType = MediaType.IMAGE;
    mediaResourceTechnicalMetadataBuilder.setMediaType(mediaType);
    final String mimeType = "mimeType";
    mediaResourceTechnicalMetadataBuilder.setMimeType(mimeType);
    assertThrows(NullPointerException.class, mediaResourceTechnicalMetadataBuilder::build);
    final LicenseType licenseType = LicenseType.OPEN;
    mediaResourceTechnicalMetadataBuilder.setLicenseType(licenseType);
    assertThrows(NullPointerException.class, mediaResourceTechnicalMetadataBuilder::build);
    final MediaTier mediaTier = MediaTier.T0;
    mediaResourceTechnicalMetadataBuilder.setMediaTier(mediaTier);

    //Object should be valid now
    assertDoesNotThrow(mediaResourceTechnicalMetadataBuilder::build);
    assertNotNull(mediaResourceTechnicalMetadataBuilder.build().getElementLinkTypes());
    //Check set null it should create an empty list
    mediaResourceTechnicalMetadataBuilder.setElementLinkTypes(null);
    MediaResourceTechnicalMetadata mediaResourceTechnicalMetadata = mediaResourceTechnicalMetadataBuilder.build();
    assertNotNull(mediaResourceTechnicalMetadata.getElementLinkTypes());
    assertTrue(CollectionUtils.isEqualCollection(Collections.emptySet(), mediaResourceTechnicalMetadata.getElementLinkTypes()));
    final Set<WebResourceLinkType> elementLinkTypes = Set.of(WebResourceLinkType.IS_SHOWN_AT, WebResourceLinkType.OBJECT);
    mediaResourceTechnicalMetadataBuilder.setElementLinkTypes(elementLinkTypes);

    mediaResourceTechnicalMetadata = mediaResourceTechnicalMetadataBuilder.build();
    assertEquals(resourceUrl, mediaResourceTechnicalMetadata.getResourceUrl());
    assertEquals(mediaType, mediaResourceTechnicalMetadata.getMediaType());
    assertEquals(mimeType, mediaResourceTechnicalMetadata.getMimeType());
    assertNotSame(elementLinkTypes, mediaResourceTechnicalMetadata.getElementLinkTypes());
    assertTrue(CollectionUtils.isEqualCollection(elementLinkTypes, mediaResourceTechnicalMetadata.getElementLinkTypes()));
    assertEquals(licenseType, mediaResourceTechnicalMetadata.getLicenseType());
    assertEquals(mediaTier, mediaResourceTechnicalMetadata.getMediaTier());
    assertNull(mediaResourceTechnicalMetadata.getImageResolution());
    assertNull(mediaResourceTechnicalMetadata.getImageResolutionTier());
    assertNull(mediaResourceTechnicalMetadata.getVerticalResolution());
    assertNull(mediaResourceTechnicalMetadata.getVerticalResolutionTier());
  }
}