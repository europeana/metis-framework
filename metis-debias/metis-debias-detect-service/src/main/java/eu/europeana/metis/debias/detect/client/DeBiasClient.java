package eu.europeana.metis.debias.detect.client;

import eu.europeana.metis.debias.detect.exceptions.DeBiasBadRequestException;
import eu.europeana.metis.debias.detect.exceptions.DeBiasInternalServerException;
import eu.europeana.metis.debias.detect.model.DeBiasResult;
import eu.europeana.metis.debias.detect.model.error.ErrorDeBiasResult;
import eu.europeana.metis.debias.detect.model.request.BiasInputLiterals;
import eu.europeana.metis.debias.detect.model.response.DetectionDeBiasResult;
import eu.europeana.metis.debias.detect.service.BiasDetectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * The type DeBias client.
 */
public class DeBiasClient implements BiasDetectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final String apiURL;
    private final Integer connectTimeOut;
    private final Integer requestTimeout;

    /**
     * Instantiates a new DeBias client.
     *
     * @param apiURL         the api url
     * @param connectTimeOut the connect time out
     * @param requestTimeout the request time out
     */
    public DeBiasClient(String apiURL, int connectTimeOut, int requestTimeout) {
        this.apiURL = Objects.requireNonNull(apiURL, "api URL is required");
        this.connectTimeOut = connectTimeOut;
        this.requestTimeout = requestTimeout;
    }

    /**
     * Method to detect biased terms according to the input values provided
     *
     * @param biasInputLiterals {@link BiasInputLiterals} language and values
     * @return {@link DeBiasResult } containing metadata and values of the detection or error
     */
    @Override
    public DeBiasResult detect(BiasInputLiterals biasInputLiterals) {
        URI uri;
        try {
            uri = new URI(this.apiURL);
        } catch (URISyntaxException | RuntimeException e) {
            LOGGER.error("Error with API URL", e);
            throw new IllegalArgumentException("Not valid API url");
        }

        final HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectionRequestTimeout(this.connectTimeOut);
        clientHttpRequestFactory.setReadTimeout(this.requestTimeout);
        final RestClient restClient = RestClient.builder().requestFactory(clientHttpRequestFactory).build();

        final ResponseEntity<DeBiasResult> response = restClient
                .post()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(biasInputLiterals)
                .exchange(((clientRequest, clientResponse) -> {
                            ObjectMapper mapper = JsonMapper.builder()
                                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                                    .build();

                            var contentType = clientResponse.getHeaders().getContentType();
                            boolean isJson = contentType != null &&
                                    (MediaType.APPLICATION_JSON.isCompatibleWith(contentType)
                                            || MediaType.APPLICATION_PROBLEM_JSON.isCompatibleWith(contentType));

                            String body = new String(clientResponse.getBody().readAllBytes(), StandardCharsets.UTF_8);


                            if (clientResponse.getStatusCode().is2xxSuccessful()) {
                                DetectionDeBiasResult result = mapper.readValue(body, DetectionDeBiasResult.class);
                                return new ResponseEntity<>(result, clientResponse.getStatusCode());
                            } else {

                                if (isJson && !body.isBlank()) {
                                    ErrorDeBiasResult result = mapper.readValue(body, ErrorDeBiasResult.class);
                                    if (result.getDetailList() != null) {
                                        throw new DeBiasBadRequestException(clientResponse.getStatusCode() + " "
                                                + result.getDetailList().getFirst().getType() + " "
                                                + result.getDetailList().getFirst().getMsg());
                                    } else {
                                        throw new DeBiasBadRequestException(body);
                                    }
                                } else if (clientResponse.getStatusCode().is5xxServerError()) {
                                    throw new DeBiasInternalServerException(clientResponse.getStatusCode().value() + " " + clientResponse.getStatusText());
                                } else if (clientResponse.getStatusCode().is4xxClientError()) {
                                    throw new DeBiasBadRequestException(clientResponse.getStatusCode().value() + " " + clientResponse.getStatusText());
                                }
                            }
                            return new ResponseEntity<>(clientResponse.getStatusCode());
                        })
                );

        if (response.getStatusCode().is2xxSuccessful()) {
            LOGGER.info("Detection processed successfully!");
        } else {
            LOGGER.warn("Failed to process request. Response code: {}", response.getStatusCode().value());
        }
        return response.getBody();
    }

}
