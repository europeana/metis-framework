package eu.europeana.metis.core.rest.client;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.organization.Organization;
import java.sql.DataTruncation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Created by erikkonijnenburg on 22/06/2017.
 */
public class TestDatasetRestClient{

  private RestTemplate templateMock;
  private DatasetRestClient orgRestClient;
  private final String hostUrl = "http://myhost";
  private ArgumentCaptor<String> uriCaptor;
  private ArgumentCaptor<HttpMethod> methodCaptor;
  private ArgumentCaptor<HttpEntity> entityCaptor;
  private ArgumentCaptor<Class> classCaptor;

  @Before
  public void setUp() throws Exception {
    templateMock = mock(RestTemplate.class);
    orgRestClient = new DatasetRestClient(templateMock, hostUrl, "myApiKey");
  }

  @Test
  public void createDatasetForOrganization() throws Exception {
   // orgRestClient.createDatasetForOrganization();
  }

  @Test
  public void updateDatasetForName() throws Exception {
    Dataset myDataset = new Dataset();
    myDataset.setDatasetName("myDatasetName");

    setupRestTemplateMockForExchange(myDataset, HttpStatus.NO_CONTENT);

    orgRestClient.updateDataset(myDataset, "newName");
    String expectedUri = hostUrl + "/datasets/newName?apikey=myApiKey";
    verifyTemplateMock(expectedUri, HttpMethod.PUT, Dataset.class, "myDatasetName");
  }

  @Test
  public void updateDataset() throws Exception {
    Dataset myDataset = new Dataset();
    myDataset.setDatasetName("myDatasetName");

    setupRestTemplateMockForExchange(myDataset, HttpStatus.NO_CONTENT);

    orgRestClient.updateDataset(myDataset);
    String expectedUri = hostUrl + "/datasets/myDatasetName?apikey=myApiKey";
    verifyTemplateMock(expectedUri, HttpMethod.PUT, Dataset.class, "myDatasetName");
  }

  @Test
  public void updateDatasetName() throws Exception {

    Dataset myDataset = new Dataset();
    myDataset.setDatasetName("myDatasetName");

    setupRestTemplateMockForExchange(myDataset, HttpStatus.NO_CONTENT);

    orgRestClient.updateDatasetName("name", "newName");
    String expectedUri = hostUrl + "/datasets/name/updateName?apikey=myApiKey&newDatasetName=newName";

    verify(templateMock, times(1)).exchange(
        uriCaptor.capture(),
        methodCaptor.capture(),
        entityCaptor.capture(),
        classCaptor.capture());

    assertEquals(expectedUri, uriCaptor.getValue());
    assertEquals(HttpMethod.PUT, methodCaptor.getValue());
  }

  @Test
  public void deleteDataset() throws Exception {

    setupRestTemplateMockForExchange(null, HttpStatus.NO_CONTENT);

    orgRestClient.deleteDataset("myDatasetName");
    String expectedUri = hostUrl + "/datasets/myDatasetName?apikey=myApiKey";

    verify(templateMock, times(1)).exchange(
        uriCaptor.capture(),
        methodCaptor.capture(),
        entityCaptor.capture(),
        classCaptor.capture());

    assertEquals(expectedUri, uriCaptor.getValue());
    assertEquals(HttpMethod.DELETE, methodCaptor.getValue());
  }

  @Test
  public void getDatasetByName() throws Exception {
    setupRestTemplateMockForGetObject();

    orgRestClient.getDatasetByName("myDataset");

    String expectedUri = hostUrl + "/datasets/myDataset?apikey=myApiKey";
    verifyRestTemplateMockForGetObject(expectedUri);
  }

  @Test
  public void getAllDatasetsByDataProvider() throws Exception {
    setupRestTemplateMockForGetDatasetListResponse();
    orgRestClient.getAllDatasetsByDataProvider("myProvider", "myPage");
    String expectedUri = hostUrl + "/datasets/data_provider/myProvider?apikey=myApiKey&nextPage=myPage";
    verifyRestTemplateMockForGetDatasetListResponse(expectedUri);
  }


  private void setupRestTemplateMockForGetDatasetListResponse()
  {
    DatasetListResponse response = new DatasetListResponse();

    when(templateMock.getForObject(any(String.class), eq(DatasetListResponse.class)))
        .thenReturn(response);

    uriCaptor = ArgumentCaptor.forClass(String.class);
  }

  private void verifyRestTemplateMockForGetDatasetListResponse(String expectedUri) {
    verify(templateMock, times(1)).getForObject(uriCaptor.capture(), eq(DatasetListResponse.class));

    assertEquals(expectedUri, uriCaptor.getValue());
  }


  private void setupRestTemplateMockForGetObject()
  {
    Dataset response = new Dataset();
    response.setDatasetName("myDataset");

    when(templateMock.getForObject(any(String.class), eq(Dataset.class)))
        .thenReturn(response);

    uriCaptor = ArgumentCaptor.forClass(String.class);
  }

  private void verifyRestTemplateMockForGetObject(String expectedUri) {
    verify(templateMock, times(1)).getForObject(uriCaptor.capture(), eq(Dataset.class));

    assertEquals(expectedUri, uriCaptor.getValue());
  }

  private void setupRestTemplateMockForExchange(Dataset dataset, HttpStatus returnStatus) {
    ResponseEntity responseEntity = new ResponseEntity<>(dataset, returnStatus);
    Class<?> cl = responseEntity.getClass();

    when(templateMock.exchange(any(String.class), any(HttpMethod.class),
        Matchers.<HttpEntity<Organization>>any(),
        eq(cl))).thenReturn(responseEntity);

    uriCaptor = ArgumentCaptor.forClass(String.class);
    methodCaptor = ArgumentCaptor.forClass(HttpMethod.class);
    entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
    classCaptor = ArgumentCaptor.forClass(Class.class);
  }

  private void verifyTemplateMock(String expectedUri, HttpMethod expectedMethod, Class<Dataset> expectedClass,
      String excectedName) {
    verify(templateMock, times(1)).exchange(
        uriCaptor.capture(),
        methodCaptor.capture(),
        entityCaptor.capture(),
        classCaptor.capture());

    assertEquals(expectedUri, uriCaptor.getValue());
    assertEquals(expectedMethod, methodCaptor.getValue());
    assertNotNull(entityCaptor.getValue());
    assertEquals(expectedClass, entityCaptor.getValue().getBody().getClass());
    assertEquals(excectedName, ((Dataset) entityCaptor.getValue().getBody()).getDatasetName());
  }


}