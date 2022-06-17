package eu.europeana.indexing.tiers.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Arrays;
import java.util.Comparator;
import org.junit.jupiter.api.Test;

class MediaTierTest {

  @Test
  void checkValues() {
    final MediaTier[] tiersOrderedByLevel = Arrays.stream(MediaTier.values())
                                                  .sorted(Comparator.comparingInt(MediaTier::getLevel)).toArray(MediaTier[]::new);
    assertEquals(5, tiersOrderedByLevel.length);

    final MediaTier[] expectedOrder =
        new MediaTier[]{MediaTier.T0, MediaTier.T1, MediaTier.T2, MediaTier.T3, MediaTier.T4};
    assertArrayEquals(expectedOrder, tiersOrderedByLevel);

    //Check comparators
    assertSame(MediaTier.T1, Tier.max(MediaTier.T0, MediaTier.T1));
    assertSame(MediaTier.T1, Tier.max(MediaTier.T1, MediaTier.T0));
    assertSame(MediaTier.T0, Tier.max(MediaTier.T0, MediaTier.T0));

    assertEquals("0", MediaTier.T0.toString());
    assertEquals("1", MediaTier.T1.toString());
    assertEquals("2", MediaTier.T2.toString());
    assertEquals("3", MediaTier.T3.toString());
    assertEquals("4", MediaTier.T4.toString());
  }
}
