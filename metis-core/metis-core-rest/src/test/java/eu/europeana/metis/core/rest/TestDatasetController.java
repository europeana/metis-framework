package eu.europeana.metis.core.rest;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.DatasetAlreadyExistsException;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.rest.exception.RestResponseExceptionHandler;
import eu.europeana.metis.core.service.DatasetService;
import eu.europeana.metis.core.test.utils.TestUtils;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class TestDatasetController {

    private DatasetService datasetServiceMock;
    private MockMvc datasetControllerMock;

    @Before
    public void setUp() throws Exception {
        datasetServiceMock = mock(DatasetService.class);
        DatasetController datasetController = new DatasetController(datasetServiceMock);
        datasetControllerMock = MockMvcBuilders
            .standaloneSetup(datasetController)
            .setControllerAdvice(new RestResponseExceptionHandler())
            .build();
    }

    @Test
    public void createDatasetForOrganization() throws Exception {
        Dataset dataset = new Dataset();

        datasetControllerMock.perform(post("/datasets")
                .param("organizationId", "myOrg").param("apikey", "myApiKey")
                .contentType(TestUtils.APPLICATION_JSON_UTF8)
                .content(TestUtils.convertObjectToJsonBytes(dataset)))
                .andExpect(status().is(201))
                .andExpect(content().string(""));

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

        verify(datasetServiceMock, times(1)).createDatasetForOrganization(any(Dataset.class), argument.capture());

        assertEquals("myOrg", argument.getValue());
    }

    @Test
    public void createDatasetForOrganization_BadContentException_Returns406() throws Exception {
        Dataset dataset = new Dataset();
        doThrow(new BadContentException("Bad"))
            .when(datasetServiceMock).createDatasetForOrganization(any(Dataset.class), any(String.class));

        datasetControllerMock.perform(post("/datasets")
            .param("organizationId", "myOrg").param("apikey", "myApiKey")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(dataset)))
            .andExpect(status().is(406))
            .andExpect(jsonPath("$.errorMessage", is("Bad")));
    }

    @Test
    public void createDatasetForOrganization_DatasetAlreadyExistsException_Returns409() throws Exception {
        Dataset dataset = new Dataset();

        doThrow(new DatasetAlreadyExistsException("Conflict"))
            .when(datasetServiceMock).createDatasetForOrganization(any(Dataset.class), any(String.class));

        datasetControllerMock.perform(post("/datasets")
            .param("organizationId", "myOrg").param("apikey", "myApiKey")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(dataset)))
            .andExpect(status().is(409))
            .andExpect(jsonPath("$.errorMessage", is("Dataset with name Conflict already exists")));
    }

    @Test
    public void updateDataset_withValidData_Returns204() throws Exception {
        Dataset dataset = new Dataset();
        datasetControllerMock.perform(put("/datasets/myDataset")
            .param("apikey", "myApiKey")

            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(dataset)))
            .andExpect(status().is(204))
            .andExpect(content().string(""));

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

        verify(datasetServiceMock, times(1)).updateDatasetByDatasetName(any(Dataset.class), argument.capture());

        assertEquals("myDataset", argument.getValue());
    }

    @Test
    public void updateDataset_withNotExistingName_Returns404() throws Exception {
        Dataset dataset = new Dataset();
        doThrow(new NoDatasetFoundException("NoExisting"))
            .when(datasetServiceMock).updateDatasetByDatasetName(any(Dataset.class), any(String.class));

        datasetControllerMock.perform(put("/datasets/myDataset")
            .param("apikey", "myApiKey")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(dataset)))
            .andExpect(status().is(404))
            .andExpect(jsonPath("$.errorMessage", is("NoExisting")));
    }

    @Test
    public void updateDatasetName() throws Exception {
        datasetControllerMock.perform(put("/datasets/myDataset/updateName")
            .param("apikey", "myApiKey")
            .param("newDatasetName", "newName")
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(null)))
            .andExpect(status().is(204))
            .andExpect(content().string(""));

        ArgumentCaptor<String> oldNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> newNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(datasetServiceMock, times(1)).updateDatasetName(oldNameCaptor.capture(), newNameCaptor.capture());

        assertEquals("myDataset", oldNameCaptor.getValue());
        assertEquals("newName", newNameCaptor.getValue());
    }

    @Test
    public void deleteDataset() throws Exception {
        datasetControllerMock.perform(delete("/datasets/myDataset")
            .param("apikey", "myApiKey")
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(null)))
            .andExpect(status().is(204))
            .andExpect(content().string(""));

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

        verify(datasetServiceMock, times(1)).deleteDatasetByDatasetName(argument.capture());

        assertEquals("myDataset", argument.getValue());
    }

    @Test
    public void getByDatasetName() throws Exception {
        Dataset dataSet = new Dataset();
        dataSet.setCountry(Country.ALBANIA);
        dataSet.setAcceptanceStep(Boolean.TRUE);
        dataSet.setDatasetName("myDataset");
        dataSet.setDataProvider("erik");

        when(datasetServiceMock.getDatasetByDatasetName("myDataset")).thenReturn(dataSet);
        datasetControllerMock.perform(get("/datasets/myDataset")
            .param("apikey", "myApiKey")
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(null)))
            .andExpect(status().is(200))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.datasetName", is("myDataset")))
            .andExpect(jsonPath("$.dataProvider", is("erik")));

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

        verify(datasetServiceMock, times(1)).getDatasetByDatasetName(argument.capture());

        assertEquals("myDataset", argument.getValue());
    }

    @Test
    public void getAllDatasetsByDataProvider() throws Exception {
        List<Dataset> list = getDatasets();

        when(datasetServiceMock.getAllDatasetsByDataProvider("myDataProvider", "3")).thenReturn(list);

        datasetControllerMock.perform(get("/datasets/data_provider/myDataProvider")
            .param("apikey", "myApiKey")
            .param("nextPage" , "3")
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(null)))
            .andExpect(status().is(200))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.results", hasSize(2)))
            .andExpect(jsonPath("$.results[0].id", is("1f1f1f1f1f1f1f1f1f1f1f1f")))
            .andExpect(jsonPath("$.results[0].datasetName", is("name1")))
            .andExpect(jsonPath("$.results[1].id", is("2f2f2f2f2f2f2f2f2f2f2f2f")))
            .andExpect(jsonPath("$.results[1].datasetName", is("name2")));

        ArgumentCaptor<String> provider = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> page = ArgumentCaptor.forClass(String.class);
        verify(datasetServiceMock, times(1)).getAllDatasetsByDataProvider(provider.capture(), page.capture());

        assertEquals("myDataProvider", provider.getValue());
        assertEquals("3", page.getValue());
    }

    private List<Dataset> getDatasets() {
        List<Dataset> list = new ArrayList<>();
        Dataset s1 = new Dataset();

        s1.setId( new ObjectId("1f1f1f1f1f1f1f1f1f1f1f1f"));
        s1.setDatasetName("name1");
        s1.setCountry(Country.ALBANIA);
        list.add(s1);

        Dataset s2 = new Dataset();
        s2.setId( new ObjectId("2f2f2f2f2f2f2f2f2f2f2f2f"));
        s2.setDatasetName("name2");
        s1.setCountry(Country.ANDORRA);
        list.add(s2);

        return list;
    }


}