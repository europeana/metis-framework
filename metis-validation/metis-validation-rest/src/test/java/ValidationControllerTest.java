import com.github.tomakehurst.wiremock.junit.WireMockRule;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.validation.model.ValidationResult;
import eu.europeana.validation.model.ValidationResultList;
import eu.europeana.validation.rest.ValidationController;
import eu.europeana.validation.rest.exceptions.ServerException;
import eu.europeana.validation.rest.exceptions.ValidationException;
import eu.europeana.validation.rest.exceptions.exceptionmappers.HttpMessageNotReadableExceptionMapper;
import eu.europeana.validation.rest.exceptions.exceptionmappers.ServerExceptionMapper;
import eu.europeana.validation.rest.exceptions.exceptionmappers.ValidationExceptionController;
import eu.europeana.validation.service.ValidationExecutionService;
import org.apache.commons.io.IOUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * Created by pwozniak on 12/7/17
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestApplication.class)
@WebAppConfiguration
public class ValidationControllerTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(9999));

    @Autowired
    ValidationExecutionService validationExecutionService;

    private static MockMvc mockMvc;

    private static boolean isOneTime = true;

    @Before
    public void setup() throws Exception {
        if (isOneTime) {
            mockMvc = MockMvcBuilders
                    .standaloneSetup(new ValidationController(validationExecutionService))
                    .setControllerAdvice(new HttpMessageNotReadableExceptionMapper(), new ServerExceptionMapper(), new ValidationExceptionController())
                    .build();
        }
        isOneTime = false;
    }


    @Ignore
    @Test
    public void exceptionShouldBeThrownForMalformedXmlFile() throws Exception {

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post(RestEndpoints.SCHEMA_VALIDATE, "sampleSchema")
                .contentType(MediaType.APPLICATION_XML)
                .content("malformed content"))
                .andExpect(MockMvcResultMatchers.status().is(422))
                .andReturn();
        Assert.assertTrue(result.getResolvedException() instanceof ValidationException);

    }

    @Ignore
    @Test
    public void shouldValidateJSONRecordAgainstEDMInternal() throws Exception {
        wireMockRule.resetAll();
        wireMockRule.stubFor(get(urlEqualTo("/schema.zip"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(2000)
                        .withBodyFile("test_schema.zip")));

        String xmlContent = prepareXMLRequest();
        mockMvc.perform(MockMvcRequestBuilders
                .post(RestEndpoints.SCHEMA_VALIDATE, "EDM-INTERNAL")
                .param("rootFileLocation", "EDM-INTERNAL.xsd")
                .contentType(MediaType.APPLICATION_XML)
                .content(xmlContent))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
    }

    @Test
    public void exceptionShouldBeThrownForMalformedXmlFile() throws Exception {

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post(RestEndpoints.SCHEMA_VALIDATE, "sampleSchema")
                .param("rootFileLocation","sampleRootFileLocation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("malformed content"))
                .andExpect(MockMvcResultMatchers.status().is(400))
                .andReturn();
        Assert.assertTrue(result.getResolvedException() instanceof HttpMessageNotReadableException);

    }

    @Test
    public void exceptionShouldBeThrownForUndefinedSchema() throws Exception {
        String xmlContent = prepareXMLRequest();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post(RestEndpoints.SCHEMA_VALIDATE, "UNDEFINED_SCHEMA")
                .param("rootFileLocation","EDM-INTERNAL.xsd")
                .contentType(MediaType.APPLICATION_XML)
                .content(xmlContent)).andExpect(MockMvcResultMatchers.status().is(422))
                .andReturn();
        Assert.assertTrue(result.getResolvedException().getMessage().contains("MalformedURLException"));
        Assert.assertTrue(result.getResolvedException() instanceof ValidationException);
    }


    @Test
    public void shouldValidateZipFileRecordsAgainstEDMInternal() throws Exception {

        wireMockRule.resetAll();
        wireMockRule.stubFor(get(urlEqualTo("/schema.zip"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("test_schema.zip")));

        MockMultipartFile file = new MockMultipartFile("file", "filename.txt", "text/plain", new FileInputStream("src/test/resources/test.zip"));
        mockMvc.perform(MockMvcRequestBuilders
                .fileUpload(RestEndpoints.SCHEMA_BATCH_VALIDATE, "EDM-INTERNAL")
                .file(file)
                .param("rootFileLocation","EDM-INTERNAL.xsd"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
    }

    @Test
    public void TestValidationFailure() throws Exception {

        wireMockRule.resetAll();
        wireMockRule.stubFor(get(urlEqualTo("/schema.zip"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("test_schema.zip")));

        MockMultipartFile file = new MockMultipartFile("file", "filename.txt", "text/plain", new FileInputStream("src/test/resources/test_wrong.zip"));
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .fileUpload(RestEndpoints.SCHEMA_BATCH_VALIDATE, "EDM-INTERNAL")
                .file(file)
                .param("rootFileLocation","EDM-INTERNAL.xsd"))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
        ValidationResultList validationResultList = unmarshalXMLToValidationResultSet(result);
        Assert.assertTrue(validationResultList.isSuccess());
        List<ValidationResult> validationResults = validationResultList.getResultList();
        for (ValidationResult validationResult : validationResults) {
            Assert.assertFalse(validationResult.isSuccess());
        }
    }

    @Test
    public void ShouldReturnResultSetOfExceptionsForUndefinedSchemaForZipFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "filename.txt", "text/plain", new FileInputStream("src/test/resources/test.zip"));
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .fileUpload(RestEndpoints.SCHEMA_BATCH_VALIDATE, "UNDEFINED_SCHEMA")
                .file(file)
                .param("rootFileLocation","EDM-INTERNAL.xsd"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        ValidationResultList validationResultList = unmarshalXMLToValidationResultSet(result);
        Assert.assertTrue(validationResultList.isSuccess());
        List<ValidationResult> validationResults = validationResultList.getResultList();
        for (ValidationResult validationResult : validationResults) {
            Assert.assertTrue(validationResult.getMessage().contains("MalformedURLException"));
        }


    }

    @Test
    public void exceptionShouldBeThrownForMalformedZipFile() throws Exception {

        MockMultipartFile firstFile = new MockMultipartFile("file", "filename.txt", "text/plain", "some xml".getBytes());


        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .fileUpload(RestEndpoints.SCHEMA_BATCH_VALIDATE, "sampleSchema")
                .file(firstFile)
                .param("rootFileLocation","EDM-INTERNAL.xsd"))
                .andExpect(MockMvcResultMatchers.status().is(500))
                .andReturn();

        Assert.assertTrue(result.getResolvedException() instanceof ServerException);
    }

    private ValidationResultList unmarshalXMLToValidationResultSet(MvcResult result) throws JAXBException, UnsupportedEncodingException {
        JAXBContext jaxbContext = JAXBContext.newInstance(ValidationResultList.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (ValidationResultList) jaxbUnmarshaller.unmarshal(new StreamSource(new StringReader(result.getResponse().getContentAsString())));
    }


    private String prepareXMLRequest() throws IOException {
        return IOUtils.toString(new FileInputStream("src/test/resources/Item_35834473_test.xml"));
    }


    @Ignore
    @Test
    public void exceptionShouldBeThrownForMalformedZipFile() throws Exception {

        MockMultipartFile firstFile = new MockMultipartFile("file", "filename.txt", "text/plain", "some xml".getBytes());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .fileUpload(RestEndpoints.SCHEMA_BATCH_VALIDATE, "sampleSchema")
                .file(firstFile))
                .andExpect(MockMvcResultMatchers.status().is(500))
                .andReturn();

        Assert.assertTrue(result.getResolvedException() instanceof ServerException);
    }
}