package eu.europeana.metis.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.europeana.metis.utils.CustomTruststoreAppender.TrustStoreConfigurationException;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link CustomTruststoreAppender}
 *
 * @author Jorge Ortiz
 * @since 31-01-2022
 */
class CustomTruststoreAppenderTest {

  @Test
  void appendCustomTrustoreToDefault() {
    assertDoesNotThrow(() ->
        CustomTruststoreAppender.appendCustomTrustoreToDefault(
            this.getClass().getClassLoader().getResource("custom.jks").getPath(), "europeana")
    );
  }

  @Test
  void appendCustomTrustoreToDefaultException() {
    assertThrows(TrustStoreConfigurationException.class, () ->
        CustomTruststoreAppender.appendCustomTrustoreToDefault(
            this.getClass().getClassLoader().getResource("custom.jks").getPath(), "euro")
    );
  }
}