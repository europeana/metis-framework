package eu.europeana.metis.core.rest;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
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

import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.DatasetAlreadyExistsException;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.rest.exception.RestResponseExceptionHandler;
import eu.europeana.metis.core.service.DatasetService;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.test.utils.TestUtils;
import java.util.ArrayList;
import java.util.List;
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
  public void createDataset() throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(datasetServiceMock.createDataset(any(Dataset.class))).thenReturn(dataset);

    datasetControllerMock.perform(post("/datasets")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .accept(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(dataset)))
        .andExpect(status().is(201))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.datasetName", is(TestObjectFactory.DATASETNAME)));
    verify(datasetServiceMock, times(1)).createDataset(any(Dataset.class));
  }

  @Test
  public void createDataset_DatasetAlreadyExistsException_Returns409() throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);

    doThrow(new DatasetAlreadyExistsException("Conflict"))
        .when(datasetServiceMock).createDataset(any(Dataset.class));

    datasetControllerMock.perform(post("/datasets")
        .accept(MediaType.APPLICATION_JSON_UTF8)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(dataset)))
        .andExpect(status().is(409))
        .andExpect(jsonPath("$.errorMessage", is("Conflict")));
  }

  @Test
  public void updateDataset_withValidData_Returns204() throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    datasetControllerMock.perform(put("/datasets")
        .accept(MediaType.APPLICATION_JSON_UTF8)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(dataset)))
        .andExpect(status().is(204))
        .andExpect(content().string(""));

    verify(datasetServiceMock, times(1)).updateDataset(any(Dataset.class));
  }

  @Test
  public void updateDataset_noDatasetFound_Returns404() throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    doThrow(new NoDatasetFoundException("Does not exist")).when(datasetServiceMock)
        .updateDataset(any(Dataset.class));
    datasetControllerMock.perform(put("/datasets")
        .accept(MediaType.APPLICATION_JSON_UTF8)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(dataset)))
        .andExpect(status().is(404))
        .andExpect(jsonPath("$.errorMessage", is("Does not exist")));

    verify(datasetServiceMock, times(1)).updateDataset(any(Dataset.class));
  }

  @Test
  public void updateDataset_BadContentException_Returns406() throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    doThrow(new BadContentException("Bad Content")).when(datasetServiceMock)
        .updateDataset(any(Dataset.class));
    datasetControllerMock.perform(put("/datasets")
        .accept(MediaType.APPLICATION_JSON_UTF8)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(dataset)))
        .andExpect(status().is(406))
        .andExpect(jsonPath("$.errorMessage", is("Bad Content")));

    verify(datasetServiceMock, times(1)).updateDataset(any(Dataset.class));
  }

  @Test
  public void deleteDataset() throws Exception {
    datasetControllerMock.perform(delete(String.format("/datasets/%s", TestObjectFactory.DATASETID))
        .accept(MediaType.APPLICATION_JSON_UTF8)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(204))
        .andExpect(content().string(""));

    ArgumentCaptor<String> datasetIdArgumentCaptor = ArgumentCaptor.forClass(String.class);

    verify(datasetServiceMock, times(1))
        .deleteDatasetByDatasetId(datasetIdArgumentCaptor.capture());

    assertEquals("datasetId", datasetIdArgumentCaptor.getValue());
  }

  @Test
  public void deleteDataset_BadContentException_Returns406() throws Exception {
    doThrow(new BadContentException("Bad Content")).when(datasetServiceMock)
        .deleteDatasetByDatasetId(TestObjectFactory.DATASETID);
    datasetControllerMock.perform(delete(String.format("/datasets/%s", TestObjectFactory.DATASETID))
        .accept(MediaType.APPLICATION_JSON_UTF8)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(406))
        .andExpect(jsonPath("$.errorMessage", is("Bad Content")));
  }


  @Test
  public void getByDatasetId() throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);

    when(datasetServiceMock.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(dataset);
    datasetControllerMock.perform(get(String.format("/datasets/%s", TestObjectFactory.DATASETID))
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(200))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.datasetName", is(TestObjectFactory.DATASETNAME)))
        .andExpect(jsonPath("$.datasetId", is(TestObjectFactory.DATASETID)));

    ArgumentCaptor<String> datasetIdArgumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(datasetServiceMock, times(1)).getDatasetByDatasetId(datasetIdArgumentCaptor.capture());
    assertEquals(TestObjectFactory.DATASETID, datasetIdArgumentCaptor.getValue());
  }

  @Test
  public void getByDatasetId_noDatasetFound_Returns404() throws Exception {
    when(datasetServiceMock.getDatasetByDatasetId(TestObjectFactory.DATASETID))
        .thenThrow(new NoDatasetFoundException("Does not exist"));
    datasetControllerMock.perform(get(String.format("/datasets/%s", TestObjectFactory.DATASETID))
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(status().is(404))
        .andExpect(jsonPath("$.errorMessage", is("Does not exist")));
  }

  @Test
  public void getByDatasetName() throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);

    when(datasetServiceMock.getDatasetByDatasetName(TestObjectFactory.DATASETNAME))
        .thenReturn(dataset);
    datasetControllerMock
        .perform(get(String.format("/datasets/dataset_name/%s", TestObjectFactory.DATASETNAME))
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(200))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.datasetName", is(TestObjectFactory.DATASETNAME)))
        .andExpect(jsonPath("$.datasetId", is(TestObjectFactory.DATASETID)));

    ArgumentCaptor<String> datasetNameArgumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(datasetServiceMock, times(1))
        .getDatasetByDatasetName(datasetNameArgumentCaptor.capture());
    assertEquals(TestObjectFactory.DATASETNAME, datasetNameArgumentCaptor.getValue());
  }

  @Test
  public void getByDatasetName_noDatasetFound_Returns404() throws Exception {
    when(datasetServiceMock.getDatasetByDatasetName(TestObjectFactory.DATASETNAME))
        .thenThrow(new NoDatasetFoundException("Does not exist"));
    datasetControllerMock
        .perform(get(String.format("/datasets/dataset_name/%s", TestObjectFactory.DATASETNAME))
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(status().is(404))
        .andExpect(jsonPath("$.errorMessage", is("Does not exist")));
  }

  @Test
  public void getAllDatasetsByDataProvider() throws Exception {
    List<Dataset> datasetList = getDatasets();

    when(datasetServiceMock.getAllDatasetsByDataProvider("myDataProvider", "3")).thenReturn(datasetList);
    when(datasetServiceMock.getDatasetsPerRequestLimit()).thenReturn(5);

    datasetControllerMock.perform(get("/datasets/data_provider/myDataProvider")
        .param("nextPage", "3")
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(200))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.results", hasSize(2)))
        .andExpect(jsonPath("$.results[0].datasetId", is(TestObjectFactory.DATASETID+1)))
        .andExpect(jsonPath("$.results[1].datasetId", is(TestObjectFactory.DATASETID+2)));

    ArgumentCaptor<String> provider = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> page = ArgumentCaptor.forClass(String.class);
    verify(datasetServiceMock, times(1))
        .getAllDatasetsByDataProvider(provider.capture(), page.capture());

    assertEquals("myDataProvider", provider.getValue());
    assertEquals("3", page.getValue());
  }

  private List<Dataset> getDatasets() {
    List<Dataset> datasetList = new ArrayList<>();
    Dataset dataset1 = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset1.setDatasetId(TestObjectFactory.DATASETID + 1);
    datasetList.add(dataset1);

    Dataset dataset2 = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset2.setDatasetId(TestObjectFactory.DATASETID + 2);
    datasetList.add(dataset2);

    return datasetList;
  }


}