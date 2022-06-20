package eu.europeana.normalization.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ConfidenceLevelTest {

  @Test
  void testGetForConfidence() {
    assertEquals(ConfidenceLevel.CERTAIN, ConfidenceLevel.getForConfidence(1.0F));
    assertEquals(ConfidenceLevel.VERY_HIGH, ConfidenceLevel.getForConfidence(0.99999F));
    assertEquals(ConfidenceLevel.HIGH,
        ConfidenceLevel.getForConfidence(ConfidenceLevel.HIGH.getMin()));
    assertEquals(ConfidenceLevel.GUESS, ConfidenceLevel.getForConfidence(0.0F));
    assertEquals(ConfidenceLevel.GUESS, ConfidenceLevel.getForConfidence(-0.0F));
  }

  @Test
  void testGetForNegativeConfidence() {
    assertThrows(IllegalArgumentException.class, () -> ConfidenceLevel.getForConfidence(-0.00001F));
  }

  @Test
  void testGetForConfidenceAboveOne() {
    assertThrows(IllegalArgumentException.class, () -> ConfidenceLevel.getForConfidence(1.00001F));
  }
}
