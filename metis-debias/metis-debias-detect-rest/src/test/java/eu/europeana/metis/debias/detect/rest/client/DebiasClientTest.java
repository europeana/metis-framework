package eu.europeana.metis.debias.detect.rest.client;

import static org.junit.jupiter.api.Assertions.*;

import eu.europeana.metis.debias.detect.model.DetectionParameter;
import eu.europeana.metis.debias.detect.model.DetectionResult;
import java.util.List;
import org.junit.jupiter.api.Test;

class DebiasClientTest {

  @Test
  void detect() {
    final String apiURL = "https://debias-api.ails.ece.ntua.gr/simple";
    DebiasClient debiasClient = new DebiasClient(apiURL, 300,300);
    DetectionParameter detectionParameter = new DetectionParameter();
    detectionParameter.setLanguage("en");
    detectionParameter.setValues(List.of(
        "sample title of aboriginal and addict",
        "a second addict sample title",
        "this is a demo of master and slave branch"));

    DetectionResult detectionResult = debiasClient.detect(detectionParameter);

    assertNotNull(detectionResult);
    assertEquals("de-bias", detectionResult.getMetadata().getAnnotator());
    assertNull(null, detectionResult.getMetadata().getThesaurusVersion());
    assertNotNull(detectionResult.getMetadata().getDate());
    assertEquals(3, detectionResult.getDetections().size());
  }
}
