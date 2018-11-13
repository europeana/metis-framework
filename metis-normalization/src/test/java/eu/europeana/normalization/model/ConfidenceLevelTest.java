package eu.europeana.normalization.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ConfidenceLevelTest {

  @Test
  public void testGetForConfidence() {
    assertEquals(ConfidenceLevel.CERTAIN, ConfidenceLevel.getForConfidence(1.0F));
    assertEquals(ConfidenceLevel.VERY_HIGH, ConfidenceLevel.getForConfidence(0.99999F));
    assertEquals(ConfidenceLevel.HIGH,
        ConfidenceLevel.getForConfidence(ConfidenceLevel.HIGH.getMin()));
    assertEquals(ConfidenceLevel.GUESS, ConfidenceLevel.getForConfidence(0.0F));
    assertEquals(ConfidenceLevel.GUESS, ConfidenceLevel.getForConfidence(-0.0F));
  }

  public void testGetForNegativeConfidence() {
    assertThrows(IllegalArgumentException.class, () -> ConfidenceLevel.getForConfidence(-0.00001F));
  }

  public void testGetForConfidenceAboveOne() {
    assertThrows(IllegalArgumentException.class, () -> ConfidenceLevel.getForConfidence(1.00001F));
  }
}
