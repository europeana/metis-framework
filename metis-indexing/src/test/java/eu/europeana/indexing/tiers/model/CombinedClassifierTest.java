package eu.europeana.indexing.tiers.model;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import eu.europeana.indexing.utils.RdfWrapper;

public class CombinedClassifierTest {

  @Test
  void testEmptyClassifier() {
    assertThrows(IllegalArgumentException.class, () -> new CombinedClassifier<Tier>(null));
    assertThrows(IllegalArgumentException.class,
        () -> new CombinedClassifier<Tier>(Collections.emptyList()));
  }

  @Test
  void testClassify() {

    // The entity under consideration
    final RdfWrapper testEntity = mock(RdfWrapper.class);

    // The tiers
    final Tier lowTier = () -> 1;
    final Tier middleTier = () -> 2;
    final Tier highTier = () -> 3;

    // The classifiers
    final TierClassifier<Tier> lowClassifier = entity -> lowTier;
    final TierClassifier<Tier> middleClassifier = entity -> middleTier;
    final TierClassifier<Tier> highClassifier = entity -> highTier;

    // Test the combined classifier
    assertSame(highTier,
        new CombinedClassifier<>(Arrays.asList(highClassifier)).classify(testEntity));
    assertSame(highTier, new CombinedClassifier<>(Arrays.asList(highClassifier, highClassifier))
        .classify(testEntity));
    assertSame(highTier,
        new CombinedClassifier<>(Arrays.asList(highClassifier)).classify(testEntity));
    assertSame(middleTier, new CombinedClassifier<>(Arrays.asList(middleClassifier, highClassifier))
        .classify(testEntity));
    assertSame(middleTier, new CombinedClassifier<>(Arrays.asList(highClassifier, middleClassifier))
        .classify(testEntity));
    assertSame(middleTier,
        new CombinedClassifier<>(Arrays.asList(middleClassifier, middleClassifier, highClassifier))
            .classify(testEntity));
    assertSame(lowTier,
        new CombinedClassifier<>(Arrays.asList(lowClassifier, middleClassifier, highClassifier))
            .classify(testEntity));
  }
}
