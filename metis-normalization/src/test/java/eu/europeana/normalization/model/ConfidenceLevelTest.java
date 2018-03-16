package eu.europeana.normalization.model;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ConfidenceLevelTest {

  @Test
  public void testGetForConfidence() {
    assertEquals(ConfidenceLevel.CERTAIN, ConfidenceLevel.getForConfidence(1.0F));
    assertEquals(ConfidenceLevel.VERY_HIGH, ConfidenceLevel.getForConfidence(0.99999F));
    assertEquals(ConfidenceLevel.HIGH, ConfidenceLevel.getForConfidence(ConfidenceLevel.HIGH.getMin()));
    assertEquals(ConfidenceLevel.GUESS, ConfidenceLevel.getForConfidence(0.0F));
    assertEquals(ConfidenceLevel.GUESS, ConfidenceLevel.getForConfidence(-0.0F));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetForNegativeConfidence() {
    ConfidenceLevel.getForConfidence(-0.00001F);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetForConfidenceAboveOne() {
    ConfidenceLevel.getForConfidence(1.00001F);
  }
}
