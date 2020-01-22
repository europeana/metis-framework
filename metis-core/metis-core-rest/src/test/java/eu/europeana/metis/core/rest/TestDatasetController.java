package eu.europeana.metis.core.rest;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
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

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import eu.europeana.metis.CommonStringValues;
import eu.europeana.metis.authentication.rest.client.AuthenticationClient;
import eu.europeana.metis.authentication.user.AccountRole;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.core.common.Language;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetSearchView;
import eu.europeana.metis.core.dataset.DatasetXslt;
import eu.europeana.metis.core.dataset.DatasetXsltStringWrapper;
import eu.europeana.metis.core.exceptions.DatasetAlreadyExistsException;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoXsltFoundException;
import eu.europeana.metis.core.rest.exception.RestResponseExceptionHandler;
import eu.europeana.metis.core.service.DatasetService;
import eu.europeana.metis.core.utils.TestObjectFactory;
import eu.europeana.metis.utils.TestUtils;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.UserUnauthorizedException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class TestDatasetController {

  private static DatasetService datasetServiceMock;
  private static AuthenticationClient authenticationClient;
  private static MockMvc datasetControllerMock;

  @BeforeAll
  static void setUp() {
    datasetServiceMock = mock(DatasetService.class);
    authenticationClient = mock(AuthenticationClient.class);
    DatasetController datasetController = new DatasetController(datasetServiceMock,
        authenticationClient);
    datasetControllerMock = MockMvcBuilders
        .standaloneSetup(datasetController)
        .setControllerAdvice(new RestResponseExceptionHandler())
        .setMessageConverters(new MappingJackson2HttpMessageConverter(),
            new MappingJackson2XmlHttpMessageConverter(),
            new StringHttpMessageConverter(StandardCharsets.UTF_8))
        .build();
  }

  @AfterEach
  void cleanUp() {
    reset(datasetServiceMock);
    reset(authenticationClient);
  }

  @Test
  void createDataset() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    when(datasetServiceMock.createDataset(any(MetisUser.class), any(Dataset.class)))
        .thenReturn(dataset);

    datasetControllerMock.perform(post("/datasets")
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .accept(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(dataset)))
        .andExpect(status().is(201))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.datasetName", is(TestObjectFactory.DATASETNAME)));
    verify(datasetServiceMock, times(1)).createDataset(any(MetisUser.class), any(Dataset.class));
  }

  @Test
  void createDatasetInvalidUser() throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED));

    datasetControllerMock.perform(post("/datasets")
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .accept(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(dataset)))
        .andExpect(status().is(401))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));
    verify(datasetServiceMock, times(0)).createDataset(any(MetisUser.class), any(Dataset.class));
  }

  @Test
  void createDataset_DatasetAlreadyExistsException_Returns409() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);

    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    doThrow(new DatasetAlreadyExistsException("Conflict"))
        .when(datasetServiceMock).createDataset(any(MetisUser.class), any(Dataset.class));

    datasetControllerMock.perform(post("/datasets")
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .accept(MediaType.APPLICATION_JSON_UTF8)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(dataset)))
        .andExpect(status().is(409))
        .andExpect(jsonPath("$.errorMessage", is("Conflict")));
  }

  @Test
  void updateDataset_withValidData_Returns204() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    DatasetXsltStringWrapper datasetXsltStringWrapper = new DatasetXsltStringWrapper(dataset,
        "<xslt attribute:\"value\"></xslt>");
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    datasetControllerMock.perform(put("/datasets")
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .accept(MediaType.APPLICATION_JSON_UTF8)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(datasetXsltStringWrapper)))
        .andExpect(status().is(204))
        .andExpect(content().string(""));

    verify(datasetServiceMock, times(1))
        .updateDataset(any(MetisUser.class), any(Dataset.class), anyString());
  }

  @Test
  void updateDataset_InvalidUser() throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED));
    datasetControllerMock.perform(put("/datasets")
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .accept(MediaType.APPLICATION_JSON_UTF8)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(dataset)))
        .andExpect(status().is(401))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));

    verify(datasetServiceMock, times(0))
        .updateDataset(any(MetisUser.class), any(Dataset.class), anyString());
  }

  @Test
  void updateDataset_noDatasetFound_Returns404() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    DatasetXsltStringWrapper datasetXsltStringWrapper = new DatasetXsltStringWrapper(dataset,
        "<xslt attribute:\"value\"></xslt>");
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    doThrow(new NoDatasetFoundException("Does not exist")).when(datasetServiceMock)
        .updateDataset(any(MetisUser.class), any(Dataset.class), anyString());
    datasetControllerMock.perform(put("/datasets")
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .accept(MediaType.APPLICATION_JSON_UTF8)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(datasetXsltStringWrapper)))
        .andExpect(status().is(404))
        .andExpect(jsonPath("$.errorMessage", is("Does not exist")));

    verify(datasetServiceMock, times(1))
        .updateDataset(any(MetisUser.class), any(Dataset.class), anyString());
  }

  @Test
  void updateDataset_BadContentException_Returns406() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    DatasetXsltStringWrapper datasetXsltStringWrapper = new DatasetXsltStringWrapper(dataset,
        "<xslt attribute:\"value\"></xslt>");
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    doThrow(new BadContentException("Bad Content")).when(datasetServiceMock)
        .updateDataset(any(MetisUser.class), any(Dataset.class), anyString());
    datasetControllerMock.perform(put("/datasets")
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .accept(MediaType.APPLICATION_JSON_UTF8)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(datasetXsltStringWrapper)))
        .andExpect(status().is(406))
        .andExpect(jsonPath("$.errorMessage", is("Bad Content")));

    verify(datasetServiceMock, times(1))
        .updateDataset(any(MetisUser.class), any(Dataset.class), anyString());
  }

  @Test
  void deleteDataset() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    datasetControllerMock.perform(
        delete(String.format("/datasets/%s", Integer.toString(TestObjectFactory.DATASETID)))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(204))
        .andExpect(content().string(""));

    ArgumentCaptor<String> datasetIdArgumentCaptor = ArgumentCaptor.forClass(String.class);

    verify(datasetServiceMock, times(1))
        .deleteDatasetByDatasetId(any(MetisUser.class), datasetIdArgumentCaptor.capture());

    assertEquals(Integer.toString(TestObjectFactory.DATASETID), datasetIdArgumentCaptor.getValue());
  }

  @Test
  void deleteDatasetInvalidUser() throws Exception {
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED));
    datasetControllerMock.perform(
        delete(String.format("/datasets/%s", Integer.toString(TestObjectFactory.DATASETID)))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(401))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));
    verify(datasetServiceMock, times(0))
        .deleteDatasetByDatasetId(any(MetisUser.class), anyString());
  }

  @Test
  void deleteDataset_BadContentException_Returns406() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    doThrow(new BadContentException("Bad Content")).when(datasetServiceMock)
        .deleteDatasetByDatasetId(metisUser, Integer.toString(TestObjectFactory.DATASETID));
    datasetControllerMock.perform(
        delete(String.format("/datasets/%s", Integer.toString(TestObjectFactory.DATASETID)))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(406))
        .andExpect(jsonPath("$.errorMessage", is("Bad Content")));
  }


  @Test
  void getByDatasetId() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);

    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    when(datasetServiceMock
        .getDatasetByDatasetId(metisUser, Integer.toString(TestObjectFactory.DATASETID)))
        .thenReturn(dataset);
    datasetControllerMock
        .perform(get(String.format("/datasets/%s", Integer.toString(TestObjectFactory.DATASETID)))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(200))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.datasetName", is(TestObjectFactory.DATASETNAME)))
        .andExpect(jsonPath("$.datasetId", is(Integer.toString(TestObjectFactory.DATASETID))));

    ArgumentCaptor<String> datasetIdArgumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(datasetServiceMock, times(1))
        .getDatasetByDatasetId(any(MetisUser.class), datasetIdArgumentCaptor.capture());
    assertEquals(Integer.toString(TestObjectFactory.DATASETID), datasetIdArgumentCaptor.getValue());
  }

  @Test
  void getByDatasetIdInvalidUser() throws Exception {
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED));
    datasetControllerMock
        .perform(get(String.format("/datasets/%s", Integer.toString(TestObjectFactory.DATASETID)))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(401))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));

    verify(datasetServiceMock, times(0))
        .getDatasetByDatasetId(any(MetisUser.class), anyString());
  }

  @Test
  void getByDatasetId_noDatasetFound_Returns404() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    when(datasetServiceMock
        .getDatasetByDatasetId(metisUser, Integer.toString(TestObjectFactory.DATASETID)))
        .thenThrow(new NoDatasetFoundException("Does not exist"));
    datasetControllerMock
        .perform(get(String.format("/datasets/%s", Integer.toString(TestObjectFactory.DATASETID)))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(status().is(404))
        .andExpect(jsonPath("$.errorMessage", is("Does not exist")));
  }

  @Test
  void getDatasetXsltByDatasetId() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    DatasetXslt xsltObject = new DatasetXslt(dataset.getDatasetId(),
        "<xslt attribute:\"value\"></xslt>");

    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    when(datasetServiceMock
        .getDatasetXsltByDatasetId(metisUser, Integer.toString(TestObjectFactory.DATASETID)))
        .thenReturn(xsltObject);
    datasetControllerMock
        .perform(
            get(String.format("/datasets/%s/xslt", Integer.toString(TestObjectFactory.DATASETID)))
                .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(200))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.xslt", is(xsltObject.getXslt())))
        .andExpect(jsonPath("$.datasetId", is(Integer.toString(TestObjectFactory.DATASETID))));

    ArgumentCaptor<String> datasetIdArgumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(datasetServiceMock, times(1))
        .getDatasetXsltByDatasetId(any(MetisUser.class), datasetIdArgumentCaptor.capture());
    assertEquals(Integer.toString(TestObjectFactory.DATASETID), datasetIdArgumentCaptor.getValue());
  }

  @Test
  void getDatasetXsltByDatasetId_noDatasetFound_Returns404() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);

    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    when(datasetServiceMock
        .getDatasetXsltByDatasetId(metisUser, Integer.toString(TestObjectFactory.DATASETID)))
        .thenThrow(new NoDatasetFoundException("Does not exist"));
    datasetControllerMock
        .perform(
            get(String.format("/datasets/%s/xslt", Integer.toString(TestObjectFactory.DATASETID)))
                .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(404))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.errorMessage", is("Does not exist")));
  }

  @Test
  void getDatasetXsltByDatasetId_noXsltFound_Returns404() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);

    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    when(datasetServiceMock
        .getDatasetXsltByDatasetId(metisUser, Integer.toString(TestObjectFactory.DATASETID)))
        .thenThrow(new NoXsltFoundException("Does not exist"));
    datasetControllerMock
        .perform(
            get(String.format("/datasets/%s/xslt", Integer.toString(TestObjectFactory.DATASETID)))
                .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(404))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.errorMessage", is("Does not exist")));
  }

  @Test
  void getDatasetXsltByDatasetIdInvalidUser() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    DatasetXslt xsltObject = new DatasetXslt(dataset.getDatasetId(),
        "<xslt attribute:\"value\"></xslt>");

    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED));
    when(datasetServiceMock
        .getDatasetXsltByDatasetId(metisUser, Integer.toString(TestObjectFactory.DATASETID)))
        .thenReturn(xsltObject);
    datasetControllerMock
        .perform(
            get(String.format("/datasets/%s/xslt", Integer.toString(TestObjectFactory.DATASETID)))
                .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(401))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));

    verify(datasetServiceMock, times(0))
        .getDatasetXsltByDatasetId(any(MetisUser.class), anyString());
  }

  @Test
  void getXsltByXsltId() throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    DatasetXslt xsltObject = new DatasetXslt(dataset.getDatasetId(),
        "<xslt attribute:\"value\"></xslt>");

    when(datasetServiceMock.getDatasetXsltByXsltId(TestObjectFactory.XSLTID))
        .thenReturn(xsltObject);
    datasetControllerMock
        .perform(get(String.format("/datasets/xslt/%s", TestObjectFactory.XSLTID))
            .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(200))
        .andExpect(content().contentType(
            new MediaType(MediaType.TEXT_PLAIN.getType(), MediaType.TEXT_PLAIN.getSubtype(),
                StandardCharsets.UTF_8)))
        .andExpect(content().string(xsltObject.getXslt()));

    ArgumentCaptor<String> xsltIdArgumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(datasetServiceMock, times(1))
        .getDatasetXsltByXsltId(xsltIdArgumentCaptor.capture());
    assertEquals(TestObjectFactory.XSLTID, xsltIdArgumentCaptor.getValue());
  }

  @Test
  void getXsltByXsltId_NoXsltFound_404() throws Exception {
    when(datasetServiceMock.getDatasetXsltByXsltId(TestObjectFactory.XSLTID))
        .thenThrow(new NoXsltFoundException("No xslt found"));
    datasetControllerMock
        .perform(get(String.format("/datasets/xslt/%s", TestObjectFactory.XSLTID))
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(404));

    ArgumentCaptor<String> xsltIdArgumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(datasetServiceMock, times(1))
        .getDatasetXsltByXsltId(xsltIdArgumentCaptor.capture());
    assertEquals(TestObjectFactory.XSLTID, xsltIdArgumentCaptor.getValue());
  }

  @Test
  void createDefaultXslt() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.METIS_ADMIN);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    DatasetXslt xsltObject = new DatasetXslt(dataset.getDatasetId(),
        "<xslt attribute:\"value\"></xslt>");
    xsltObject.setId(new ObjectId(TestObjectFactory.XSLTID));

    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);

    when(datasetServiceMock.createDefaultXslt(any(MetisUser.class), anyString()))
        .thenReturn(xsltObject);
    datasetControllerMock
        .perform(post("/datasets/xslt/default", TestObjectFactory.XSLTID)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.TEXT_PLAIN)
            .content(TestUtils.convertObjectToJsonBytes(xsltObject.getXslt())))
        .andExpect(status().is(201))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.xslt", is(xsltObject.getXslt())))
        .andExpect(jsonPath("$.datasetId", is(Integer.toString(TestObjectFactory.DATASETID))));
  }

  @Test
  void createDefaultXslt_Unauthorized() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    DatasetXslt xsltObject = new DatasetXslt(dataset.getDatasetId(),
        "<xslt attribute:\"value\"></xslt>");
    xsltObject.setId(new ObjectId(TestObjectFactory.XSLTID));

    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);

    when(datasetServiceMock.createDefaultXslt(any(MetisUser.class), anyString()))
        .thenThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED));
    datasetControllerMock
        .perform(post("/datasets/xslt/default", TestObjectFactory.XSLTID)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.TEXT_PLAIN)
            .content(TestUtils.convertObjectToJsonBytes(xsltObject.getXslt())))
        .andExpect(status().is(401))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));
  }

  @Test
  void getLatestDefaultXslt() throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    DatasetXslt xsltObject = new DatasetXslt(dataset.getDatasetId(),
        "<xslt attribute:\"value\"></xslt>");

    when(datasetServiceMock.getLatestDefaultXslt()).thenReturn(xsltObject);
    datasetControllerMock.perform(get("/datasets/xslt/default")
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(200))
        .andExpect(content().contentType(
            new MediaType(MediaType.TEXT_PLAIN.getType(), MediaType.TEXT_PLAIN.getSubtype(),
                StandardCharsets.UTF_8)))
        .andExpect(content().string(xsltObject.getXslt()));

    verify(datasetServiceMock, times(1)).getLatestDefaultXslt();
  }

  @Test
  void getLatestDefaultXslt_NoXsltFound_404() throws Exception {
    when(datasetServiceMock.getLatestDefaultXslt())
        .thenThrow(new NoXsltFoundException("No xslt found"));
    datasetControllerMock.perform(get("/datasets/xslt/default")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(404));

    verify(datasetServiceMock, times(1)).getLatestDefaultXslt();
  }

  @Test
  void transformRecordsUsingLatestDatasetXslt() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    List<Record> listOfRecords = TestObjectFactory.createListOfRecords(5);
    when(datasetServiceMock
        .transformRecordsUsingLatestDatasetXslt(any(MetisUser.class), anyString(), anyList()))
        .thenReturn(listOfRecords);
    datasetControllerMock
        .perform(post("/datasets/{datasetId}/xslt/transform",
            Integer.toString(TestObjectFactory.DATASETID))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.convertObjectToJsonBytes(listOfRecords)))
        .andExpect(status().is(200))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$", hasSize(5)));
  }

  @Test
  void transformRecordsUsingLatestDatasetXslt_UserUnauthorizedException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenThrow(new UserUnauthorizedException(CommonStringValues.WRONG_ACCESS_TOKEN));
    datasetControllerMock
        .perform(post("/datasets/{datasetId}/xslt/transform",
            Integer.toString(TestObjectFactory.DATASETID))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.convertObjectToJsonBytes(TestObjectFactory.createListOfRecords(5))))
        .andExpect(status().is(401))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.WRONG_ACCESS_TOKEN)));
  }

  @Test
  void transformRecordsUsingLatestDefaultXslt() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    List<Record> listOfRecords = TestObjectFactory.createListOfRecords(5);
    when(datasetServiceMock
        .transformRecordsUsingLatestDefaultXslt(any(MetisUser.class), anyString(), anyList()))
        .thenReturn(listOfRecords);
    datasetControllerMock
        .perform(post("/datasets/{datasetId}/xslt/transform/default",
            Integer.toString(TestObjectFactory.DATASETID))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.convertObjectToJsonBytes(listOfRecords)))
        .andExpect(status().is(200))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$", hasSize(5)));
  }

  @Test
  void transformRecordsUsingLatestDefaultXslt_UserUnauthorizedException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenThrow(new UserUnauthorizedException(CommonStringValues.WRONG_ACCESS_TOKEN));
    datasetControllerMock
        .perform(post("/datasets/{datasetId}/xslt/transform/default",
            Integer.toString(TestObjectFactory.DATASETID))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.convertObjectToJsonBytes(TestObjectFactory.createListOfRecords(5))))
        .andExpect(status().is(401))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.WRONG_ACCESS_TOKEN)));
  }

  @Test
  void getByDatasetName() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);

    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    when(datasetServiceMock.getDatasetByDatasetName(metisUser, TestObjectFactory.DATASETNAME))
        .thenReturn(dataset);
    datasetControllerMock
        .perform(get(String.format("/datasets/dataset_name/%s", TestObjectFactory.DATASETNAME))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(200))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.datasetName", is(TestObjectFactory.DATASETNAME)))
        .andExpect(jsonPath("$.datasetId", is(Integer.toString(TestObjectFactory.DATASETID))));

    ArgumentCaptor<String> datasetNameArgumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(datasetServiceMock, times(1))
        .getDatasetByDatasetName(any(MetisUser.class), datasetNameArgumentCaptor.capture());
    assertEquals(TestObjectFactory.DATASETNAME, datasetNameArgumentCaptor.getValue());
  }

  @Test
  void getByDatasetNameInvalidUser() throws Exception {
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED));
    datasetControllerMock
        .perform(get(String.format("/datasets/dataset_name/%s", TestObjectFactory.DATASETNAME))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(401))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));

    verify(datasetServiceMock, times(0))
        .getDatasetByDatasetName(any(MetisUser.class), anyString());
  }


  @Test
  void getByDatasetName_noDatasetFound_Returns404() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    when(datasetServiceMock.getDatasetByDatasetName(metisUser, TestObjectFactory.DATASETNAME))
        .thenThrow(new NoDatasetFoundException("Does not exist"));
    datasetControllerMock
        .perform(get(String.format("/datasets/dataset_name/%s", TestObjectFactory.DATASETNAME))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(status().is(404))
        .andExpect(jsonPath("$.errorMessage", is("Does not exist")));
  }

  @Test
  void getAllDatasetsByProvider() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    List<Dataset> datasetList = getDatasets();

    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    when(datasetServiceMock.getAllDatasetsByProvider(metisUser, "myProvider", 3))
        .thenReturn(datasetList);
    when(datasetServiceMock.getDatasetsPerRequestLimit()).thenReturn(5);

    datasetControllerMock.perform(get("/datasets/provider/myProvider")
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .param("nextPage", "3")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(200))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.results", hasSize(2)))
        .andExpect(jsonPath("$.results[0].datasetId",
            is(Integer.toString(TestObjectFactory.DATASETID + 1))))
        .andExpect(jsonPath("$.results[1].datasetId",
            is(Integer.toString(TestObjectFactory.DATASETID + 2))));

    ArgumentCaptor<String> provider = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Integer> page = ArgumentCaptor.forClass(Integer.class);
    verify(datasetServiceMock, times(1))
        .getAllDatasetsByProvider(any(MetisUser.class), provider.capture(), page.capture());

    assertEquals("myProvider", provider.getValue());
    assertEquals(3, page.getValue().intValue());
  }

  @Test
  void getAllDatasetsByProviderNegativeNextPage() throws Exception {
    datasetControllerMock.perform(get("/datasets/provider/myProvider")
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .param("nextPage", "-1")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(406));
  }

  @Test
  void getAllDatasetsByProviderInvalidUser() throws Exception {
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED));

    datasetControllerMock.perform(get("/datasets/provider/myProvider")
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .param("nextPage", "3")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(401))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));

    verify(datasetServiceMock, times(0))
        .getAllDatasetsByProvider(any(MetisUser.class), anyString(), anyInt());
  }

  @Test
  void getAllDatasetsByIntermediateProvider() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    List<Dataset> datasetList = getDatasets();

    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    when(datasetServiceMock
        .getAllDatasetsByIntermediateProvider(metisUser, "myIntermediateProvider", 3))
        .thenReturn(datasetList);
    when(datasetServiceMock.getDatasetsPerRequestLimit()).thenReturn(5);

    datasetControllerMock.perform(get("/datasets/intermediate_provider/myIntermediateProvider")
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .param("nextPage", "3")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(200))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.results", hasSize(2)))
        .andExpect(jsonPath("$.results[0].datasetId",
            is(Integer.toString(TestObjectFactory.DATASETID + 1))))
        .andExpect(jsonPath("$.results[1].datasetId",
            is(Integer.toString(TestObjectFactory.DATASETID + 2))));

    ArgumentCaptor<String> provider = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Integer> page = ArgumentCaptor.forClass(Integer.class);
    verify(datasetServiceMock, times(1))
        .getAllDatasetsByIntermediateProvider(any(MetisUser.class), provider.capture(),
            page.capture());

    assertEquals("myIntermediateProvider", provider.getValue());
    assertEquals(3, page.getValue().intValue());
  }

  @Test
  void getAllDatasetsByIntermediateProviderNegativeNextPage() throws Exception {
    datasetControllerMock.perform(get("/datasets/intermediate_provider/myIntermediateProvider")
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .param("nextPage", "-1")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(406));
  }

  @Test
  void getAllDatasetsByIntermediateProviderInvalidUser() throws Exception {
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED));

    datasetControllerMock.perform(get("/datasets/intermediate_provider/myIntermediateProvider")
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .param("nextPage", "3")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(401))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));

    verify(datasetServiceMock, times(0))
        .getAllDatasetsByIntermediateProvider(any(MetisUser.class), anyString(), anyInt());
  }

  @Test
  void getAllDatasetsByDataProvider() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    List<Dataset> datasetList = getDatasets();

    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    when(datasetServiceMock.getAllDatasetsByDataProvider(metisUser, "myDataProvider", 3))
        .thenReturn(datasetList);
    when(datasetServiceMock.getDatasetsPerRequestLimit()).thenReturn(5);

    datasetControllerMock.perform(get("/datasets/data_provider/myDataProvider")
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .param("nextPage", "3")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(200))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.results", hasSize(2)))
        .andExpect(jsonPath("$.results[0].datasetId",
            is(Integer.toString(TestObjectFactory.DATASETID + 1))))
        .andExpect(jsonPath("$.results[1].datasetId",
            is(Integer.toString(TestObjectFactory.DATASETID + 2))));

    ArgumentCaptor<String> provider = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Integer> page = ArgumentCaptor.forClass(Integer.class);
    verify(datasetServiceMock, times(1))
        .getAllDatasetsByDataProvider(any(MetisUser.class), provider.capture(), page.capture());

    assertEquals("myDataProvider", provider.getValue());
    assertEquals(3, page.getValue().intValue());
  }

  @Test
  void getAllDatasetsByDataProviderNegativeNextPage() throws Exception {
    datasetControllerMock.perform(get("/datasets/data_provider/myDataProvider")
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .param("nextPage", "-1")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(406));
  }

  @Test
  void getAllDatasetsByDataProviderInvalidUser() throws Exception {
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED));

    datasetControllerMock.perform(get("/datasets/data_provider/myDataProvider")
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .param("nextPage", "3")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(401))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));

    verify(datasetServiceMock, times(0))
        .getAllDatasetsByDataProvider(any(MetisUser.class), anyString(), anyInt());
  }

  @Test
  void getAllDatasetsByOrganizationId() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    List<Dataset> datasetList = getDatasets();

    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    when(datasetServiceMock.getAllDatasetsByOrganizationId(metisUser, "myOrganizationId", 3))
        .thenReturn(datasetList);
    when(datasetServiceMock.getDatasetsPerRequestLimit()).thenReturn(5);

    datasetControllerMock.perform(get("/datasets/organization_id/myOrganizationId")
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .param("nextPage", "3")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(200))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.results", hasSize(2)))
        .andExpect(jsonPath("$.results[0].datasetId",
            is(Integer.toString(TestObjectFactory.DATASETID + 1))))
        .andExpect(jsonPath("$.results[1].datasetId",
            is(Integer.toString(TestObjectFactory.DATASETID + 2))));

    ArgumentCaptor<String> provider = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Integer> page = ArgumentCaptor.forClass(Integer.class);
    verify(datasetServiceMock, times(1))
        .getAllDatasetsByOrganizationId(any(MetisUser.class), provider.capture(), page.capture());

    assertEquals("myOrganizationId", provider.getValue());
    assertEquals(3, page.getValue().intValue());
  }

  @Test
  void getAllDatasetsByOrganizationIdNegativeNextPage() throws Exception {
    datasetControllerMock.perform(get("/datasets/organization_id/myOrganizationId")
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .param("nextPage", "-1")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(406));
  }

  @Test
  void getAllDatasetsByOrganizationIdInvalidUser() throws Exception {
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED));

    datasetControllerMock.perform(get("/datasets/organization_id/myOrganizationId")
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .param("nextPage", "3")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(401))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));

    verify(datasetServiceMock, times(0))
        .getAllDatasetsByOrganizationId(any(MetisUser.class), anyString(), anyInt());
  }

  @Test
  void getAllDatasetsByOrganizationName() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    List<Dataset> datasetList = getDatasets();

    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    when(datasetServiceMock.getAllDatasetsByOrganizationName(metisUser, "myOrganizationName", 3))
        .thenReturn(datasetList);
    when(datasetServiceMock.getDatasetsPerRequestLimit()).thenReturn(5);

    datasetControllerMock.perform(get("/datasets/organization_name/myOrganizationName")
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .param("nextPage", "3")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(200))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.results", hasSize(2)))
        .andExpect(jsonPath("$.results[0].datasetId",
            is(Integer.toString(TestObjectFactory.DATASETID + 1))))
        .andExpect(jsonPath("$.results[1].datasetId",
            is(Integer.toString(TestObjectFactory.DATASETID + 2))));

    ArgumentCaptor<String> provider = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Integer> page = ArgumentCaptor.forClass(Integer.class);
    verify(datasetServiceMock, times(1))
        .getAllDatasetsByOrganizationName(any(MetisUser.class), provider.capture(), page.capture());

    assertEquals("myOrganizationName", provider.getValue());
    assertEquals(3, page.getValue().intValue());
  }

  @Test
  void getAllDatasetsByOrganizationNameNegativeNextPage() throws Exception {
    datasetControllerMock.perform(get("/datasets/organization_name/myOrganizationName")
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .param("nextPage", "-1")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(406));
  }

  @Test
  void getAllDatasetsByOrganizationNameInvalidUser() throws Exception {
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED));

    datasetControllerMock.perform(get("/datasets/organization_name/myOrganizationName")
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .param("nextPage", "3")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(401))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));

    verify(datasetServiceMock, times(0))
        .getAllDatasetsByOrganizationName(any(MetisUser.class), anyString(), anyInt());
  }

  @Test
  void getDatasetsCountries() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);

    MvcResult mvcResult = datasetControllerMock.perform(get("/datasets/countries")
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(""))
        .andExpect(status().is(200))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)).andReturn();

    String resultListOfCountries = mvcResult.getResponse().getContentAsString();
    Object document = Configuration.defaultConfiguration().jsonProvider()
        .parse(resultListOfCountries);

    List<Map<String, Object>> mapListOfCountries = JsonPath.read(document, "$[*]");
    assertEquals(Country.values().length, mapListOfCountries.size());
    assertEquals(mapListOfCountries.get(22).get("enum"), Country.values()[22].name());
    assertEquals(mapListOfCountries.get(22).get("name"), Country.values()[22].getName());
    assertEquals(mapListOfCountries.get(22).get("isoCode"), Country.values()[22].getIsoCode());
  }

  @Test
  void getDatasetsCountriesInvalidUser() throws Exception {
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED));

    datasetControllerMock.perform(get("/datasets/countries")
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(""))
        .andExpect(status().is(401))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));
  }

  @Test
  void getDatasetsLanguages() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(
        authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);

    MvcResult mvcResult = datasetControllerMock.perform(get("/datasets/languages")
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(""))
        .andExpect(status().is(200))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)).andReturn();

    String resultListOfLanguages = mvcResult.getResponse().getContentAsString();
    Object document = Configuration.defaultConfiguration().jsonProvider()
        .parse(resultListOfLanguages);

    List<Map<String, Object>> mapListOfLanguages = JsonPath.read(document, "$[*]");
    assertEquals(Language.values().length, mapListOfLanguages.size());
    assertEquals(mapListOfLanguages.get(10).get("enum"),
        Language.getLanguageListSortedByName().get(10).name());
    assertEquals(mapListOfLanguages.get(10).get("name"),
        Language.getLanguageListSortedByName().get(10).getName());
  }

  @Test
  void getDatasetsLanguagesInvalidUser() throws Exception {
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED));

    datasetControllerMock.perform(get("/datasets/languages")
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(""))
        .andExpect(status().is(401))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));
  }

  @Test
  void getDatasetSearch() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);

    when(datasetServiceMock.searchDatasetsBasedOnSearchString(metisUser, "test", 3))
        .thenReturn(getDatasetSearchViews());
    when(datasetServiceMock.getDatasetsPerRequestLimit()).thenReturn(5);

    datasetControllerMock.perform(get("/datasets/search")
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .param("searchString", "test")
        .param("nextPage", "3")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(200))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.results", hasSize(2)))
        .andExpect(jsonPath("$.results[0].datasetId",
            is(Integer.toString(TestObjectFactory.DATASETID + 1))))
        .andExpect(jsonPath("$.results[1].datasetId",
            is(Integer.toString(TestObjectFactory.DATASETID + 2))));

    ArgumentCaptor<String> searchString = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Integer> page = ArgumentCaptor.forClass(Integer.class);
    verify(datasetServiceMock, times(1))
        .searchDatasetsBasedOnSearchString(any(MetisUser.class), searchString.capture(), page.capture());

    assertEquals("test", searchString.getValue());
    assertEquals(3, page.getValue().intValue());
  }

  private List<DatasetSearchView> getDatasetSearchViews() {
    List<DatasetSearchView> datasetSearchViews = new ArrayList<>(2);
    final DatasetSearchView datasetSearchView1 = new DatasetSearchView();
    datasetSearchView1.setDatasetId(Integer.toString(TestObjectFactory.DATASETID + 1));
    datasetSearchView1.setDatasetName(TestObjectFactory.DATASETNAME + 1);
    datasetSearchView1.setProvider("provider1");
    datasetSearchView1.setDataProvider("dataProvider1");
    datasetSearchView1.setLastExecutionDate(new Date());
    datasetSearchViews.add(datasetSearchView1);

    final DatasetSearchView datasetSearchView2 = new DatasetSearchView();
    datasetSearchView2.setDatasetId(Integer.toString(TestObjectFactory.DATASETID + 2));
    datasetSearchView2.setDatasetName(TestObjectFactory.DATASETNAME + 2);
    datasetSearchView2.setProvider("provider2");
    datasetSearchView2.setDataProvider("dataProvider2");
    datasetSearchView2.setLastExecutionDate(new Date());
    datasetSearchViews.add(datasetSearchView2);

    return datasetSearchViews;
  }

  private List<Dataset> getDatasets() {
    List<Dataset> datasetList = new ArrayList<>();
    Dataset dataset1 = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset1.setDatasetId(Integer.toString(TestObjectFactory.DATASETID + 1));
    datasetList.add(dataset1);

    Dataset dataset2 = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset2.setDatasetId(Integer.toString(TestObjectFactory.DATASETID + 2));
    datasetList.add(dataset2);

    return datasetList;
  }


}