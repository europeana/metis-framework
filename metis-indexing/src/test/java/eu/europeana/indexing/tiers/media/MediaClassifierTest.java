package eu.europeana.indexing.tiers.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import eu.europeana.corelib.definitions.jibx.EdmType;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.utils.RdfWrapper;

public class MediaClassifierTest {

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
    final MediaTier audioTier = MediaTier.T1;
    final MediaTier imageTier = MediaTier.T2;
    final MediaTier textTier = MediaTier.T3;
    final MediaTier videoTier = MediaTier.T4;
    final MediaTier threeDTier = MediaTier.T2;
    doReturn(audioTier).when(audioClassifier).classify(entity);
    doReturn(imageTier).when(imageClassifier).classify(entity);
    doReturn(textTier).when(textClassifier).classify(entity);
    doReturn(videoTier).when(videoClassifier).classify(entity);
    doReturn(threeDTier).when(threeDClassifier).classify(entity);

    // Test audio
    doReturn(EdmType.SOUND).when(entity).getEdmType();
    assertEquals(audioTier, classifier.classify(entity));
    verify(audioClassifier, times(1)).classify(entity);
    verify(imageClassifier, times(0)).classify(entity);
    verify(textClassifier, times(0)).classify(entity);
    verify(videoClassifier, times(0)).classify(entity);
    verify(threeDClassifier, times(0)).classify(entity);

    // Test image
    doReturn(EdmType.IMAGE).when(entity).getEdmType();
    assertEquals(imageTier, classifier.classify(entity));
    verify(audioClassifier, times(1)).classify(entity);
    verify(imageClassifier, times(1)).classify(entity);
    verify(textClassifier, times(0)).classify(entity);
    verify(videoClassifier, times(0)).classify(entity);
    verify(threeDClassifier, times(0)).classify(entity);

    // Test text
    doReturn(EdmType.TEXT).when(entity).getEdmType();
    assertEquals(textTier, classifier.classify(entity));
    verify(audioClassifier, times(1)).classify(entity);
    verify(imageClassifier, times(1)).classify(entity);
    verify(textClassifier, times(1)).classify(entity);
    verify(videoClassifier, times(0)).classify(entity);
    verify(threeDClassifier, times(0)).classify(entity);

    // Test video
    doReturn(EdmType.VIDEO).when(entity).getEdmType();
    assertEquals(videoTier, classifier.classify(entity));
    verify(audioClassifier, times(1)).classify(entity);
    verify(imageClassifier, times(1)).classify(entity);
    verify(textClassifier, times(1)).classify(entity);
    verify(videoClassifier, times(1)).classify(entity);
    verify(threeDClassifier, times(0)).classify(entity);

    // Test 3D
    doReturn(EdmType._3_D).when(entity).getEdmType();
    assertEquals(threeDTier, classifier.classify(entity));
    verify(audioClassifier, times(1)).classify(entity);
    verify(imageClassifier, times(1)).classify(entity);
    verify(textClassifier, times(1)).classify(entity);
    verify(videoClassifier, times(1)).classify(entity);
    verify(threeDClassifier, times(1)).classify(entity);

    // Test unknown
    doReturn(null).when(entity).getEdmType();
    assertEquals(MediaTier.T0, classifier.classify(entity));
    verify(audioClassifier, times(1)).classify(entity);
    verify(imageClassifier, times(1)).classify(entity);
    verify(textClassifier, times(1)).classify(entity);
    verify(videoClassifier, times(1)).classify(entity);
    verify(threeDClassifier, times(1)).classify(entity);
  }
}
