package eu.europeana.indexing.tiers.model;

import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;
import eu.europeana.indexing.tiers.model.Tier;

public class TierTest {

  @Test
  void maxTest() {

    // create two tiers
    final Tier lowTier = () -> 1;
    final Tier highTier = () -> 2;

    // Test
    assertSame(highTier, Tier.max(lowTier, highTier));
    assertSame(highTier, Tier.max(highTier, lowTier));
    assertSame(lowTier, Tier.max(lowTier, lowTier));
  }

  @Test
  void minTest() {

    // create two tiers
    final Tier lowTier = () -> 1;
    final Tier highTier = () -> 2;

    // Test
    assertSame(lowTier, Tier.min(lowTier, highTier));
    assertSame(lowTier, Tier.min(highTier, lowTier));
    assertSame(highTier, Tier.min(highTier, highTier));
  }
}
