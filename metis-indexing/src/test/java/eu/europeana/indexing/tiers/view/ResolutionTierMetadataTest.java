package eu.europeana.indexing.tiers.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.view.ResolutionTierMetadata.ResolutionTierMetadataBuilder;
import org.junit.jupiter.api.Test;

class ResolutionTierMetadataTest {

  @Test
  void objectCreationTest() {
    final ResolutionTierMetadataBuilder resolutionTierMetadataBuilder = new ResolutionTierMetadataBuilder();
    ResolutionTierMetadata resolutionTierMetadata = resolutionTierMetadataBuilder.build();
    assertNull(resolutionTierMetadata.getImageResolution());
    assertNull(resolutionTierMetadata.getImageResolutionTier());
    assertNull(resolutionTierMetadata.getVerticalResolution());
    assertNull(resolutionTierMetadata.getVerticalResolutionTier());

    //Non null image resolution tier
    resolutionTierMetadataBuilder.setImageResolution(10L);
    resolutionTierMetadataBuilder.setImageResolutionTier(MediaTier.T0);
    resolutionTierMetadata = resolutionTierMetadataBuilder.build();
    assertNotNull(resolutionTierMetadata.getImageResolution());
    assertNotNull(resolutionTierMetadata.getImageResolutionTier());
    assertNull(resolutionTierMetadata.getVerticalResolution());
    assertNull(resolutionTierMetadata.getVerticalResolutionTier());

    //Non null vertical resolution tier
    resolutionTierMetadataBuilder.setImageResolution(null);
    resolutionTierMetadataBuilder.setImageResolutionTier(null);
    resolutionTierMetadataBuilder.setVerticalResolution(10L);
    resolutionTierMetadataBuilder.setVerticalResolutionTier(MediaTier.T0);
    resolutionTierMetadata = resolutionTierMetadataBuilder.build();
    assertNull(resolutionTierMetadata.getImageResolution());
    assertNull(resolutionTierMetadata.getImageResolutionTier());
    assertNotNull(resolutionTierMetadata.getVerticalResolution());
    assertNotNull(resolutionTierMetadata.getVerticalResolutionTier());

    //Check nullification if zero value
    resolutionTierMetadataBuilder.setImageResolution(0L);
    resolutionTierMetadataBuilder.setImageResolutionTier(null);
    resolutionTierMetadataBuilder.setVerticalResolution(0L);
    resolutionTierMetadataBuilder.setVerticalResolutionTier(MediaTier.T0);
    resolutionTierMetadata = resolutionTierMetadataBuilder.build();
    assertNull(resolutionTierMetadata.getImageResolution());
    assertNull(resolutionTierMetadata.getImageResolutionTier());
    assertNull(resolutionTierMetadata.getVerticalResolution());
    assertNotNull(resolutionTierMetadata.getVerticalResolutionTier());

    //Fail if both tiers set
    resolutionTierMetadataBuilder.setImageResolution(null);
    resolutionTierMetadataBuilder.setImageResolutionTier(MediaTier.T0);
    resolutionTierMetadataBuilder.setVerticalResolution(0L);
    resolutionTierMetadataBuilder.setVerticalResolutionTier(MediaTier.T0);
    assertThrows(IllegalArgumentException.class, resolutionTierMetadataBuilder::build);

    //Check copy object
    resolutionTierMetadataBuilder.setImageResolution(null);
    resolutionTierMetadataBuilder.setImageResolutionTier(null);
    resolutionTierMetadataBuilder.setVerticalResolution(10L);
    resolutionTierMetadataBuilder.setVerticalResolutionTier(MediaTier.T0);
    resolutionTierMetadata = resolutionTierMetadataBuilder.build();
    final ResolutionTierMetadata resolutionTierMetadataCopy = new ResolutionTierMetadata(resolutionTierMetadata);
    assertEquals(resolutionTierMetadata.getImageResolution(), resolutionTierMetadataCopy.getImageResolution());
    assertEquals(resolutionTierMetadata.getImageResolutionTier(), resolutionTierMetadataCopy.getImageResolutionTier());
    assertEquals(resolutionTierMetadata.getVerticalResolution(), resolutionTierMetadataCopy.getVerticalResolution());
    assertEquals(resolutionTierMetadata.getVerticalResolutionTier(), resolutionTierMetadataCopy.getVerticalResolutionTier());
  }

}