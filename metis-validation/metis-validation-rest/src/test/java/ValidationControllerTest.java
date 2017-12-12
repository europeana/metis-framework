import eu.europeana.metis.RestEndpoints;
import eu.europeana.validation.model.Record;
import eu.europeana.validation.model.ValidationResult;
import eu.europeana.validation.model.ValidationResultList;
import eu.europeana.validation.rest.ValidationController;
import eu.europeana.validation.rest.exceptions.ServerException;
import eu.europeana.validation.rest.exceptions.ValidationException;
import eu.europeana.validation.rest.exceptions.exceptionmappers.HttpMessageNotReadableExceptionMapper;
import eu.europeana.validation.rest.exceptions.exceptionmappers.ServerExceptionMapper;
import eu.europeana.validation.rest.exceptions.exceptionmappers.ValidationExceptionController;
import eu.europeana.validation.service.ValidationExecutionService;
import eu.europeana.validation.service.ValidationManagementService;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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

/**
 * Created by pwozniak on 12/7/17
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestApplication.class)
@WebAppConfiguration
public class ValidationControllerTest {

    @Autowired
    ValidationExecutionService validationExecutionService;

    @Autowired
    ValidationManagementService service;

    private static MockMvc mockMvc;

    private static boolean isOneTime = true;

    @Before
    public void setup() throws Exception {
        if (isOneTime) {
            mockMvc = MockMvcBuilders
                    .standaloneSetup(new ValidationController(validationExecutionService))
                    .setControllerAdvice(new HttpMessageNotReadableExceptionMapper(), new ServerExceptionMapper(), new ValidationExceptionController())
                    .build();
            service.createSchema("EDM-EXTERNAL", "EDM.xsd", "schematron/schematron.xsl", "undefined", this.getClass().getClassLoader()
                    .getResourceAsStream("test_schema.zip"));
            service.createSchema("EDM-INTERNAL", "EDM-INTERNAL.xsd", "schematron/schematron-internal.xsl", "undefined", this.getClass().getClassLoader()
                    .getResourceAsStream("test_schema.zip"));
        }
        isOneTime = false;
    }


    @Test
    public void exceptionShouldBeThrownForMalformedXmlFile() throws Exception {

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post(RestEndpoints.SCHEMA_VALIDATE, "sampleSchema")
                .contentType(MediaType.APPLICATION_JSON)
                .content("malformed content"))
                .andExpect(MockMvcResultMatchers.status().is(400))
                .andReturn();
        Assert.assertTrue(result.getResolvedException() instanceof HttpMessageNotReadableException);

    }

    @Test
    public void shouldValidateJSONRecordAgainstEDMInternal() throws Exception {
        String requestJson = prepareJsonRequest();
        mockMvc.perform(MockMvcRequestBuilders
                .post(RestEndpoints.SCHEMA_VALIDATE, "EDM-INTERNAL")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
    }

    @Test
    public void exceptionShouldBeThrownForUndefinedSchema() throws Exception {
        String requestJson = prepareJsonRequest();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post(RestEndpoints.SCHEMA_VALIDATE, "UNDEFINED_SCHEMA")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)).andExpect(MockMvcResultMatchers.status().is(422))
                .andReturn();
        Assert.assertEquals(result.getResolvedException().getMessage(), "Specified schema does not exist");
        Assert.assertTrue(result.getResolvedException() instanceof ValidationException);
    }


    @Test
    public void shouldValidateZipFileRecordsAgainstEDMInternal() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "filename.txt", "text/plain", new FileInputStream("src/test/resources/test.zip"));
        mockMvc.perform(MockMvcRequestBuilders
                .fileUpload(RestEndpoints.SCHEMA_BATCH_VALIDATE, "EDM-INTERNAL")
                .file(file))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
    }

    @Test
    public void shouldThrowExceptionForWrongZipFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "filename.txt", "text/plain", new FileInputStream("src/test/resources/test_wrong.zip"));
        MvcResult result =  mockMvc.perform(MockMvcRequestBuilders
                .fileUpload(RestEndpoints.SCHEMA_BATCH_VALIDATE, "EDM-INTERNAL")
                .file(file))
                .andExpect(MockMvcResultMatchers.status().is(500))
                .andReturn();
        Assert.assertTrue(result.getResolvedException() instanceof ServerException);
    }

    @Test
    public void ShouldReturnResultSetOfExceptionsForUndefinedSchemaForZipFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "filename.txt", "text/plain", new FileInputStream("src/test/resources/test.zip"));
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .fileUpload(RestEndpoints.SCHEMA_BATCH_VALIDATE, "UNDEFINED_SCHEMA")
                .file(file))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        ValidationResultList validationResultList = unmarshalXMLToValidationResultSet(result);
        Assert.assertTrue(validationResultList.isSuccess());
        List<ValidationResult> validationResults = validationResultList.getResultList();
        for (ValidationResult validationResult : validationResults) {
            Assert.assertEquals(validationResult.getMessage(), "Specified schema does not exist");
        }


    }

    private ValidationResultList unmarshalXMLToValidationResultSet(MvcResult result) throws JAXBException, UnsupportedEncodingException {
        JAXBContext jaxbContext = JAXBContext.newInstance(ValidationResultList.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (ValidationResultList) jaxbUnmarshaller.unmarshal(new StreamSource(new StringReader(result.getResponse().getContentAsString())));
    }


    private String prepareJsonRequest() throws IOException {
        String fileToValidate = IOUtils.toString(new FileInputStream("src/test/resources/Item_35834473_test.xml"));
        Record record = new Record();
        record.setRecord(fileToValidate);
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(record);
    }


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