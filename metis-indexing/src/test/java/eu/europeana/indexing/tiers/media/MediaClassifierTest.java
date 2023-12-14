package eu.europeana.indexing.tiers.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.TierClassifier.TierClassification;
import eu.europeana.indexing.tiers.view.ContentTierBreakdown;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.jibx.EdmType;
import java.util.Collections;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MediaClassifierTest {

  private static AudioClassifier audioClassifier;
  private static ImageClassifier imageClassifier;
  private static TextClassifier textClassifier;
  private static VideoClassifier videoClassifier;
  private static ThreeDClassifier threeDClassifier;
  private static MediaClassifier classifier;

  @BeforeAll
  static void setup() {
    audioClassifier = mock(AudioClassifier.class);
    imageClassifier = mock(ImageClassifier.class);
    textClassifier = mock(TextClassifier.class);
    videoClassifier = mock(VideoClassifier.class);
    threeDClassifier = mock(ThreeDClassifier.class);
    classifier = spy(new MediaClassifier(audioClassifier, imageClassifier, textClassifier,
        videoClassifier, threeDClassifier));
  }

  @BeforeEach
  void resetMocks() {
    reset(audioClassifier, imageClassifier, textClassifier, videoClassifier, threeDClassifier);
    reset(classifier);
  }

  @Test
  void testClassify() {

    // Create entity and tiers for various types
    final RdfWrapper entity = mock(RdfWrapper.class);
    final TierClassification<MediaTier, ContentTierBreakdown> audioTier =
        new TierClassification<>(MediaTier.T1, new ContentTierBreakdown.Builder()
            .setThumbnailAvailable(false)
            .setMediaTierBeforeLicenseCorrection(MediaTier.T1)
            .setLicenseType(entity.getLicenseType())
            .setMediaResourceTechnicalMetadataList(Collections.emptyList())
            .build());
    final TierClassification<MediaTier, ContentTierBreakdown> imageTier =
        new TierClassification<>(MediaTier.T2, new ContentTierBreakdown.Builder()
            .setThumbnailAvailable(false)
            .setMediaTierBeforeLicenseCorrection(MediaTier.T2)
            .setLicenseType(entity.getLicenseType())
            .setMediaResourceTechnicalMetadataList(Collections.emptyList())
            .build());
    final TierClassification<MediaTier, ContentTierBreakdown> textTier =
        new TierClassification<>(MediaTier.T3, new ContentTierBreakdown.Builder()
            .setThumbnailAvailable(false)
            .setMediaTierBeforeLicenseCorrection(MediaTier.T2)
            .setLicenseType(entity.getLicenseType())
            .setMediaResourceTechnicalMetadataList(Collections.emptyList())
            .build());
    final TierClassification<MediaTier, ContentTierBreakdown> videoTier =
        new TierClassification<>(MediaTier.T4, new ContentTierBreakdown.Builder()
            .setThumbnailAvailable(false)
            .setMediaTierBeforeLicenseCorrection(MediaTier.T2)
            .setLicenseType(entity.getLicenseType())
            .setMediaResourceTechnicalMetadataList(Collections.emptyList())
            .build());
    final TierClassification<MediaTier, ContentTierBreakdown> threeDTier =
        new TierClassification<>(MediaTier.T2, new ContentTierBreakdown.Builder()
            .setThumbnailAvailable(false)
            .setMediaTierBeforeLicenseCorrection(MediaTier.T2)
            .setLicenseType(entity.getLicenseType())
            .setMediaResourceTechnicalMetadataList(Collections.emptyList())
            .build());
    doReturn(audioTier).when(audioClassifier).classify(entity);
    doReturn(imageTier).when(imageClassifier).classify(entity);
    doReturn(textTier).when(textClassifier).classify(entity);
    doReturn(videoTier).when(videoClassifier).classify(entity);
    doReturn(threeDTier).when(threeDClassifier).classify(entity);

    // Test audio
    doReturn(EdmType.SOUND).when(entity).getEdmType();
    assertEquals(audioTier.getTier(), classifier.classify(entity).getTier());
    verify(audioClassifier, times(1)).classify(entity);
    verify(imageClassifier, times(0)).classify(entity);
    verify(textClassifier, times(0)).classify(entity);
    verify(videoClassifier, times(0)).classify(entity);
    verify(threeDClassifier, times(0)).classify(entity);

    // Test image
    doReturn(EdmType.IMAGE).when(entity).getEdmType();
    assertEquals(imageTier.getTier(), classifier.classify(entity).getTier());
    verify(audioClassifier, times(1)).classify(entity);
    verify(imageClassifier, times(1)).classify(entity);
    verify(textClassifier, times(0)).classify(entity);
    verify(videoClassifier, times(0)).classify(entity);
    verify(threeDClassifier, times(0)).classify(entity);

    // Test text
    doReturn(EdmType.TEXT).when(entity).getEdmType();
    assertEquals(textTier.getTier(), classifier.classify(entity).getTier());
    verify(audioClassifier, times(1)).classify(entity);
    verify(imageClassifier, times(1)).classify(entity);
    verify(textClassifier, times(1)).classify(entity);
    verify(videoClassifier, times(0)).classify(entity);
    verify(threeDClassifier, times(0)).classify(entity);

    // Test video
    doReturn(EdmType.VIDEO).when(entity).getEdmType();
    assertEquals(videoTier.getTier(), classifier.classify(entity).getTier());
    verify(audioClassifier, times(1)).classify(entity);
    verify(imageClassifier, times(1)).classify(entity);
    verify(textClassifier, times(1)).classify(entity);
    verify(videoClassifier, times(1)).classify(entity);
    verify(threeDClassifier, times(0)).classify(entity);

    // Test 3D
    doReturn(EdmType._3_D).when(entity).getEdmType();
    assertEquals(threeDTier.getTier(), classifier.classify(entity).getTier());
    verify(audioClassifier, times(1)).classify(entity);
    verify(imageClassifier, times(1)).classify(entity);
    verify(textClassifier, times(1)).classify(entity);
    verify(videoClassifier, times(1)).classify(entity);
    verify(threeDClassifier, times(1)).classify(entity);

    // Test unknown
    doReturn(null).when(entity).getEdmType();
    assertEquals(MediaTier.T0, classifier.classify(entity).getTier());
    verify(audioClassifier, times(1)).classify(entity);
    verify(imageClassifier, times(1)).classify(entity);
    verify(textClassifier, times(1)).classify(entity);
    verify(videoClassifier, times(1)).classify(entity);
    verify(threeDClassifier, times(1)).classify(entity);
  }
}
