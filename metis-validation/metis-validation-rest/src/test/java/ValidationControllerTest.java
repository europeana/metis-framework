import eu.europeana.metis.RestEndpoints;
import eu.europeana.validation.rest.ValidationController;
import eu.europeana.validation.rest.exceptions.ServerException;
import eu.europeana.validation.rest.exceptions.exceptionmappers.HttpMessageNotReadableExceptionMapper;
import eu.europeana.validation.rest.exceptions.exceptionmappers.ServerExceptionMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

/**
 * Created by pwozniak on 12/7/17
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestApplication.class)
@WebAppConfiguration
public class ValidationControllerTest {

    private MockMvc mockMvc;

    @Before
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(new ValidationController(null))
                .setControllerAdvice(new HttpMessageNotReadableExceptionMapper(), new ServerExceptionMapper())
                .build();
    }

    @Test
    public void exceptionShouldBeThrownForMalformedXmlFile() throws Exception {

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post(RestEndpoints.SCHEMA_VALIDATE,"sampleSchema","sampleVersion")
                .contentType(MediaType.APPLICATION_JSON)
                .content("malformed content"))
                .andExpect(MockMvcResultMatchers.status().is(400))
                .andReturn();

        Assert.assertTrue(result.getResolvedException() instanceof HttpMessageNotReadableException);
    }

    @Test
    public void exceptionShouldBeThrownForMalformedZipFile() throws Exception {

        MockMultipartFile firstFile = new MockMultipartFile("file", "filename.txt", "text/plain", "some xml".getBytes());


        MvcResult result =  mockMvc.perform(MockMvcRequestBuilders
                .fileUpload(RestEndpoints.SCHEMA_BATCH_VALIDATE,"sampleSchema","sampleVersion")
                .file(firstFile))
                .andExpect(MockMvcResultMatchers.status().is(500))
                .andReturn();

        Assert.assertTrue(result.getResolvedException() instanceof ServerException);
    }

    @Test
    public void exceptionShouldBeThrownForMalformedXmlFiles() throws Exception {

        MvcResult result =  mockMvc.perform(MockMvcRequestBuilders
                .post(RestEndpoints.SCHEMA_RECORDS_BATCH_VALIDATE,"sampleSchema","sampleVersion")
                .contentType(MediaType.APPLICATION_JSON)
                .content("malformed content"))
                .andExpect(MockMvcResultMatchers.status().is(400))
                .andReturn();

        Assert.assertTrue(result.getResolvedException() instanceof HttpMessageNotReadableException);
    }
}