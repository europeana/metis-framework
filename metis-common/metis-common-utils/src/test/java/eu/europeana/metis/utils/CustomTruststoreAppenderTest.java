package eu.europeana.metis.utils;

import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.europeana.metis.utils.CustomTruststoreAppender.TrustStoreConfigurationException;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link CustomTruststoreAppender}
 */
class CustomTruststoreAppenderTest {

  @Test
  void appendCustomTrustoreToDefault() throws TrustStoreConfigurationException {
    CustomTruststoreAppender.appendCustomTrustoreToDefault(getClass().getClassLoader().getResource("custom.jks").getPath(),
        "europeana");
  }

  @Test
  void appendCustomTrustoreToDefaultException() {
    assertThrows(TrustStoreConfigurationException.class, () -> CustomTruststoreAppender.appendCustomTrustoreToDefault(
        getClass().getClassLoader().getResource("custom.jks").getPath(), "euro"));
  }
}