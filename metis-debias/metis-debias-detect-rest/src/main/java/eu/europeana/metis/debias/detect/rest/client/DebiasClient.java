package eu.europeana.metis.debias.detect.rest.client;

import eu.europeana.metis.debias.detect.model.DetectionParameter;
import eu.europeana.metis.debias.detect.model.DetectionResult;
import eu.europeana.metis.debias.detect.service.DetectService;
import java.io.ByteArrayInputStream;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * The type DeBias client.
 */
public class DebiasClient implements DetectService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final String apiURL;
  private final Integer connectTimeOut;
  private final Integer readTimeOut;

  /**
   * Instantiates a new DeBias client.
   *
   * @param apiURL the api url
   * @param connectTimeOut the connect time out
   * @param readTimeOut the read time out
   */
  public DebiasClient(String apiURL, Integer connectTimeOut, Integer readTimeOut) {
    this.apiURL = apiURL;
    this.connectTimeOut = connectTimeOut;
    this.readTimeOut = readTimeOut;
  }

  /**
   * Method to detect biased terms according to the input values provided
   *
   * @param detectionParameter language and values
   * @return DetectionResult containing metadata and values of the detection
   */
  @Override
  public DetectionResult detect(DetectionParameter detectionParameter) {
    final URI uri;
    try {
      uri = new URI(this.apiURL);
    } catch (URISyntaxException e) {
      LOGGER.error("Error with API URL", e);
      throw new IllegalArgumentException("Not valid API url");
    }

    final HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    final HttpEntity<DetectionParameter> httpEntity = new HttpEntity<>(detectionParameter, headers);

    final RestTemplate restTemplate = new RestTemplateBuilder()
        .setConnectTimeout(Duration.ofSeconds(this.connectTimeOut))
        .setReadTimeout(Duration.ofSeconds(this.readTimeOut))
        .build();
    final ResponseEntity<DetectionResult> response = restTemplate.postForEntity(uri, httpEntity, DetectionResult.class);

    if (response.getStatusCode().is2xxSuccessful()) {
      LOGGER.info("Detection processed successfully!");
      return response.getBody();
    } else {
      LOGGER.warn("Failed to process request. Response code: {}", response.getStatusCode().value());
    }
    return null;
  }

}
