package eu.europeana.metis.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.europeana.metis.utils.CustomTruststoreAppender.TrustStoreConfigurationException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link CustomTruststoreAppender}
 *
 * @author Jorge Ortiz
 * @since 31-01-2022
 */
class CustomTruststoreAppenderTest {

  @Test
  void appendCustomTrustoreToDefault() throws URISyntaxException {
    final String customTruststorePath = Paths.get(this.getClass().getClassLoader().getResource("custom.jks").toURI()).toString();
    assertDoesNotThrow(() ->
        CustomTruststoreAppender.appendCustomTrustoreToDefault(customTruststorePath, "europeana")
    );
  }

  @Test
  void appendCustomTrustoreToDefaultException() throws URISyntaxException {
    final String customTruststorePath = Paths.get(this.getClass().getClassLoader().getResource("custom.jks").toURI()).toString();
    assertThrows(TrustStoreConfigurationException.class, () ->
        CustomTruststoreAppender.appendCustomTrustoreToDefault(customTruststorePath, "euro")
    );
  }
}