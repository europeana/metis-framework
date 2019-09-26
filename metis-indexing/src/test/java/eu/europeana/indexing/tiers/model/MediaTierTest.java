package eu.europeana.indexing.tiers.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import java.util.Arrays;
import java.util.Comparator;
import org.junit.jupiter.api.Test;

public class MediaTierTest {

  @Test
  void verifyOrder() {
    final MediaTier[] tiersOrderedByLevel = Arrays.stream(MediaTier.values())
        .sorted(Comparator.comparingInt(MediaTier::getLevel)).toArray(MediaTier[]::new);
    final MediaTier[] expectedOrder =
        new MediaTier[] {MediaTier.T0, MediaTier.T1, MediaTier.T2, MediaTier.T3, MediaTier.T4};
    assertArrayEquals(expectedOrder, tiersOrderedByLevel);
  }
}
