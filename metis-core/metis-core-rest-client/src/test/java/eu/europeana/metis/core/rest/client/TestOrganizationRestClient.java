package eu.europeana.metis.core.rest.client;

import static org.junit.Assert.*;

import eu.europeana.metis.core.common.Contact;
import eu.europeana.metis.core.common.OrganizationRole;
import eu.europeana.metis.core.organization.Organization;

import eu.europeana.metis.core.search.common.OrganizationSearchBean;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.internal.matchers.Or;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.*;

/**
 * Created by erikkonijnenburg on 21/06/2017.
 */
public class TestOrganizationRestClient {

  private RestTemplate templateMock;
  private OrganizationRestClient orgRestClient;
  private ArgumentCaptor<String> uriCaptor;
  private ArgumentCaptor<HttpMethod> methodCaptor;
  private ArgumentCaptor<HttpEntity> entityCaptor;
  private ArgumentCaptor<Class> classCaptor;
  private final String hostUrl = "http://myhost";

  @Before
  public void setUp() throws Exception {
    templateMock = mock(RestTemplate.class);
    orgRestClient = new OrganizationRestClient(templateMock, hostUrl, "myApiKey");
  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void createOrganization() throws Exception {
    Organization myOrg = new Organization();
    String excectedName = "myOrg";
    myOrg.setName(excectedName);

    setupRestTemplateMockForExchange(myOrg, HttpStatus.CREATED);

    orgRestClient.createOrganization(myOrg);

    String expectedUri = hostUrl + "/organizations?apikey=myApiKey";
    HttpMethod expectedMethod = HttpMethod.POST;
    Class<Organization> expectedClass = Organization.class;

    verifyTemplateMock(expectedUri, expectedMethod, expectedClass, excectedName);
  }

  @Test
  public void updateOrganization() throws Exception {
    Organization myOrg = new Organization();
    String excectedName = "myOrg";
    myOrg.setName(excectedName);
    myOrg.setOrganizationId("myOrgId");

    setupRestTemplateMockForExchange(myOrg,  HttpStatus.NO_CONTENT);

    orgRestClient.updateOrganization(myOrg);

    String expectedUri = hostUrl + "/organizations/myOrgId?apikey=myApiKey";
    HttpMethod expectedMethod = HttpMethod.PUT;
    Class<Organization> expectedClass = Organization.class;

    verifyTemplateMock(expectedUri, expectedMethod, expectedClass, excectedName);
  }

  @Test
  public void deleteOrganization() throws Exception {
    Organization myOrg = new Organization();
    String excectedName = "myOrg";
    myOrg.setName(excectedName);
    myOrg.setOrganizationId("myOrgId");

    setupRestTemplateMockForExchange(myOrg,  HttpStatus.NO_CONTENT);

    orgRestClient.deleteOrganization(myOrg);

    String expectedUri = hostUrl + "/organizations/myOrgId?apikey=myApiKey";
    HttpMethod expectedMethod = HttpMethod.DELETE;
    Class<Organization> expectedClass = Organization.class;

    verifyTemplateMock(expectedUri, expectedMethod, expectedClass, excectedName);
  }

  @Test
  public void getAllOrganizations() throws Exception {
    setupRestTemplateMockForGetList();

    OrganizationListResponse results = orgRestClient.getAllOrganizations("myPage");
    String expectedUri = hostUrl + "/organizations?apikey=myApiKey&nextPage=myPage";
    verifyRestTemplateMockForGetList(expectedUri);
  }

  @Test
  public void getAllOrganizationsByIsoCode() throws Exception {
    setupRestTemplateMockForGetList();

    OrganizationListResponse results = orgRestClient.getAllOrganizationsByIsoCode("NL", "myPage");
    String expectedUri = hostUrl + "/organizations/country/NL?apikey=myApiKey&nextPage=myPage";
    verifyRestTemplateMockForGetList(expectedUri);
  }

  @Test
  public void getAllOrganizationsByRoles() throws Exception {
    setupRestTemplateMockForGetList();

    List<OrganizationRole> list = new ArrayList<>();
    list.add(OrganizationRole.CONSULTANT);
    list.add(OrganizationRole.EUROPEANA);

    OrganizationListResponse results = orgRestClient.getAllOrganizationsByRoles(list, "myPage");
    String expectedUri = hostUrl + "/organizations/roles?apikey=myApiKey&nextPage=myPage&organizationRoles=consultant,europeana";
    verifyRestTemplateMockForGetList(expectedUri);
  }

  @Test
  public void getDatasetsForOrganization() throws Exception {
    setupRestTemplateMockForGetListDataset();

    DatasetListResponse results = orgRestClient.getDatasetsForOrganization("myOrgID", "myPage");
    String expectedUri = hostUrl + "/organizations/myOrgID/datasets?apikey=myApiKey&nextPage=myPage";
    verifyRestTemplateMockForGetListDataset(expectedUri);
  }

  @Test
  public void getOrganizationById() throws Exception {
    setupRestTemplateMockForGetObject();

    Organization org = orgRestClient.getOrganizationById("myOrgId");
    String expectedUri = hostUrl + "/organizations/myOrgId?apikey=myApiKey";
    verifyRestTemplateMockForGetObject(expectedUri);
  }

  @Test
  public void getOrganizationByOrganizationId() throws Exception {
    setupRestTemplateMockForGetObject();

    Organization org = orgRestClient.getOrganizationByOrganizationId("myOrgId");
    String expectedUri = hostUrl + "/organizations?apikey=myApiKey&orgId=myOrgId";
    verifyRestTemplateMockForGetObject(expectedUri);
  }

  @Test
  public void getOrganizationFromCrm() throws Exception {
    setupRestTemplateMockForGetObject();

    Organization org = orgRestClient.getOrganizationFromCrm("myOrgId");
    String expectedUri = hostUrl + "/organizations/crm/myOrgId?apikey=myApiKey";
    verifyRestTemplateMockForGetObject(expectedUri);
  }

  @Test
  public void getOrganizationsFromCrm() throws Exception {
    setupRestTemplateMockForGetList();

    OrganizationListResponse results = orgRestClient.getOrganizationsFromCrm("myPage");
    String expectedUri = hostUrl + "/organizations/crm?apikey=myApiKey&nextPage=myPage";
    verifyRestTemplateMockForGetList(expectedUri);
  }

  @Test
  public void getUserByEmail() throws Exception {
    setupRestTemplateMockForGetContact();

    Contact org = orgRestClient.getUserByEmail("my@email.com");

    String expectedUri = hostUrl + "/user/my@email.com?apikey=myApiKey";
    verifyRestTemplateMockForGetObjectContact(expectedUri);
  }

  @Test
  public void suggestOrganizations() throws Exception {

    setupRestTemplateMockForGetSuggestions();
    orgRestClient.suggestOrganizations("begins");

    String expectedUri = hostUrl + "/organizations/suggest?apikey=myApiKey&searchTerm=begins";
    verifyRestTemplateMockForGetObjectSuggestion(expectedUri);

  }

  private void setupRestTemplateMockForExchange(Organization myOrg, HttpStatus returnStatus) {
    ResponseEntity responseEntity = new ResponseEntity<>(myOrg, returnStatus);
    Class<?> cl = responseEntity.getClass();

    when(templateMock.exchange(any(String.class), any(HttpMethod.class),
        Matchers.<HttpEntity<Organization>>any(),
        eq(cl))).thenReturn(responseEntity);

    uriCaptor = ArgumentCaptor.forClass(String.class);
    methodCaptor = ArgumentCaptor.forClass(HttpMethod.class);
    entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
    classCaptor = ArgumentCaptor.forClass(Class.class);
  }

  private void verifyTemplateMock(String expectedUri, HttpMethod expectedMethod, Class<Organization> expectedClass,
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
    assertEquals(excectedName, ((Organization) entityCaptor.getValue().getBody()).getName());
  }

  private void setupRestTemplateMockForGetSuggestions() {
    Suggestions suggestions = new Suggestions();
    suggestions.setSuggestions(new ArrayList<OrganizationSearchBean>());

    when(templateMock.getForObject(any(String.class), eq(Suggestions.class)))
        .thenReturn(suggestions);

    uriCaptor = ArgumentCaptor.forClass(String.class);
  }
  private void setupRestTemplateMockForGetContact() {
    Contact contact = new Contact();
    contact.setEmail("my@email.com");

    when(templateMock.getForObject(any(String.class), eq(Contact.class)))
        .thenReturn(contact);

    uriCaptor = ArgumentCaptor.forClass(String.class);
  }

  private void verifyRestTemplateMockForGetObjectSuggestion(String expectedUri) {
    verify(templateMock, times(1)).getForObject(uriCaptor.capture(), eq(Suggestions.class));

    assertEquals(expectedUri, uriCaptor.getValue());
  }

  private void verifyRestTemplateMockForGetObjectContact(String expectedUri) {
    verify(templateMock, times(1)).getForObject(uriCaptor.capture(), eq(Contact.class));

    assertEquals(expectedUri, uriCaptor.getValue());
  }

  private void setupRestTemplateMockForGetObject()
  {
    Organization response = new Organization();
    response.setName("ID");

    when(templateMock.getForObject(any(String.class), eq(Organization.class)))
        .thenReturn(response);

    uriCaptor = ArgumentCaptor.forClass(String.class);
  }

  private void verifyRestTemplateMockForGetObject(String expectedUri) {
    verify(templateMock, times(1)).getForObject(uriCaptor.capture(), eq(Organization.class));

    assertEquals(expectedUri, uriCaptor.getValue());
  }


  private void setupRestTemplateMockForGetListDataset()
  {
    DatasetListResponse response = new DatasetListResponse();

    when(templateMock.getForObject(any(String.class), eq(DatasetListResponse.class)))
        .thenReturn(response);

    uriCaptor = ArgumentCaptor.forClass(String.class);
  }

  private void verifyRestTemplateMockForGetListDataset(String expectedUri) {
    verify(templateMock, times(1)).getForObject(uriCaptor.capture(), eq(DatasetListResponse.class));

    assertEquals(expectedUri, uriCaptor.getValue());
  }

  private void setupRestTemplateMockForGetList()
  {
    OrganizationListResponse response = new OrganizationListResponse();

    when(templateMock.getForObject(any(String.class), eq(OrganizationListResponse.class)))
        .thenReturn(response);

    uriCaptor = ArgumentCaptor.forClass(String.class);
  }

  private void verifyRestTemplateMockForGetList(String expectedUri) {
    verify(templateMock, times(1)).getForObject(uriCaptor.capture(), eq(OrganizationListResponse.class));

    assertEquals(expectedUri, uriCaptor.getValue());
  }
}