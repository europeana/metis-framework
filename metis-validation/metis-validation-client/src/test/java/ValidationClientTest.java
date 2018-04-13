import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import eu.europeana.validation.client.ValidationClient;
import eu.europeana.validation.model.ValidationResult;
import eu.europeana.validation.model.ValidationResultList;
import java.io.File;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * Created by pwozniak on 1/17/18
 */
public class ValidationClientTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(9999));


    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionForMissingPropertiesFile() throws Exception {
        ValidationClient client = new ValidationClient();
    }

    @Test
    public void shouldCreateProperValidationResultForMalformedXml() {
        wireMockRule.resetAll();
        wireMockRule.stubFor(post(urlEqualTo("/schema/validate/EDM-INTERNAL"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type","application/json")
                        .withHeader("Accept","application/json")
                        .withBody("{\n" +
                                "  \"recordId\": null,\n" +
                                "  \"message\": null,\n" +
                                "  \"success\": false\n" +
                                "}")));


        ValidationClient client = new ValidationClient("http://127.0.0.1:9999");
        ValidationResult result = client.validateRecord("EDM-INTERNAL","malformedXml");
        Assert.assertFalse(result.isSuccess());
        Assert.assertNull(result.getMessage());
        Assert.assertNull(result.getRecordId());
    }


    @Test
    public void shouldCreateProperValidationResultForCorrectXml() {
        wireMockRule.resetAll();
        wireMockRule.stubFor(post(urlEqualTo("/schema/validate/EDM-INTERNAL"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type","application/json")
                        .withHeader("Accept","application/json")
                        .withBody("{\n" +
                                "  \"recordId\": null,\n" +
                                "  \"message\": null,\n" +
                                "  \"success\": true\n" +
                                "}")));


        ValidationClient client = new ValidationClient("http://127.0.0.1:9999");
        ValidationResult result = client.validateRecord("EDM-INTERNAL","wellFormedXml");
        Assert.assertTrue(result.isSuccess());
        Assert.assertNull(result.getMessage());
        Assert.assertNull(result.getRecordId());
    }

    @Test
    public void shouldCreateProperValidationResultForCorrectZipFile() {
        wireMockRule.resetAll();
        wireMockRule.stubFor(post(urlEqualTo("/schema/validate/batch/EDM-INTERNAL"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type","application/json")
                        .withHeader("Accept","application/json")
                        .withBody("{\n" +
                                "  \"resultList\": [],\n" +
                                "  \"success\": true\n" +
                                "}")));


        ValidationClient client = new ValidationClient("http://127.0.0.1:9999");
        ValidationResultList result = client.validateRecordsInFile("EDM-INTERNAL",new File("ok"));
        Assert.assertTrue(result.isSuccess());
        Assert.assertTrue(result.getResultList().size() == 0);
    }

    @Test
    public void shouldCreateProperValidationResultForWrongZipFile() {
        wireMockRule.resetAll();
        wireMockRule.stubFor(post(urlEqualTo("/schema/validate/batch/EDM-INTERNAL"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type","application/json")
                        .withHeader("Accept","application/json")
                        .withBody("{\n" +
                                "  \"resultList\": [\n" +
                                "    {\n" +
                                "      \"recordId\": \"sampleId\",\n" +
                                "      \"message\": \"sampleMessage\",\n" +
                                "      \"success\": false\n" +
                                "    }],\n" +
                                "  \"success\": true\n" +
                                "}")));


        ValidationClient client = new ValidationClient("http://127.0.0.1:9999");
        ValidationResultList result = client.validateRecordsInFile("EDM-INTERNAL",new File("ok"));
        Assert.assertTrue(result.isSuccess());
        Assert.assertTrue(result.getResultList().size() == 1);
        Assert.assertFalse(result.getResultList().get(0).isSuccess());
        Assert.assertEquals("sampleId",result.getResultList().get(0).getRecordId());
        Assert.assertEquals("sampleMessage",result.getResultList().get(0).getMessage());
    }
}