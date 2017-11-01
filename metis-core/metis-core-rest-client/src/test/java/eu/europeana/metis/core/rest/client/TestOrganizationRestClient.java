package eu.europeana.metis.core.rest.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.europeana.metis.core.common.Contact;
import eu.europeana.metis.common.model.OrganizationRole;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.organization.Organization;
import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.search.common.OrganizationSearchBean;
import eu.europeana.metis.core.search.common.OrganizationSearchListWrapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
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
 * Created by erikkonijnenburg on 21/06/2017.
 */
public class TestOrganizationRestClient {

  private RestTemplate templateMock;
  private OrganizationRestClient organizationRestClient;
  private ArgumentCaptor<String> uriCaptor;
  private ArgumentCaptor<HttpMethod> methodCaptor;
  private ArgumentCaptor<HttpEntity> entityCaptor;
  private ArgumentCaptor<Class> classCaptor;
  private final String hostUrl = "http://myhost";

  @Before
  public void setUp() throws Exception {
    templateMock = mock(RestTemplate.class);
    organizationRestClient = new OrganizationRestClient(templateMock, hostUrl, "myApiKey");
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

    organizationRestClient.createOrganization(myOrg);

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

    organizationRestClient.updateOrganization(myOrg);

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

    organizationRestClient.deleteOrganization(myOrg);

    String expectedUri = hostUrl + "/organizations/myOrgId?apikey=myApiKey";
    HttpMethod expectedMethod = HttpMethod.DELETE;
    Class<Organization> expectedClass = Organization.class;

    verifyTemplateMock(expectedUri, expectedMethod, expectedClass, excectedName);
  }

  @Test
  public void getAllOrganizations() throws Exception {
    setupRestTemplateMockForGetList();

    ResponseListWrapper<Organization> results = organizationRestClient.getAllOrganizations("myPage");
    String expectedUri = hostUrl + "/organizations?apikey=myApiKey&nextPage=myPage";
    verifyRestTemplateMockForGetList(expectedUri);
  }

  @Test
  public void getAllOrganizationsByIsoCode() throws Exception {
    setupRestTemplateMockForGetList();

    ResponseListWrapper<Organization> results = organizationRestClient
        .getAllOrganizationsByIsoCode("NL", "myPage");
    String expectedUri = hostUrl + "/organizations/country/NL?apikey=myApiKey&nextPage=myPage";
    verifyRestTemplateMockForGetList(expectedUri);
  }

  @Test
  public void getAllOrganizationsByRoles() throws Exception {
    setupRestTemplateMockForGetList();

    List<OrganizationRole> list = new ArrayList<>();
    list.add(OrganizationRole.CONSULTANT);
    list.add(OrganizationRole.EUROPEANA);

    ResponseListWrapper<Organization> results = organizationRestClient
        .getAllOrganizationsByRoles(list, "myPage");
    String expectedUri = hostUrl + "/organizations/roles?apikey=myApiKey&nextPage=myPage&organizationRoles=consultant,europeana";
    verifyRestTemplateMockForGetList(expectedUri);
  }

  @Test
  public void getDatasetsForOrganization() throws Exception {
    setupRestTemplateMockForGetListDataset();

    ResponseListWrapper<Dataset> results = organizationRestClient
        .getDatasetsForOrganization("myOrgID", "myPage");
    String expectedUri = hostUrl + "/organizations/myOrgID/datasets?apikey=myApiKey&nextPage=myPage";
    verifyRestTemplateMockForGetListDataset(expectedUri);
  }

//  @Test
//  public void getOrganizationById() throws Exception {
//    setupRestTemplateMockForGetObject();
//
//    Organization org = organizationRestClient.getOrganizationById("myOrgId");
//    String expectedUri = hostUrl + "/organizations/myOrgId?apikey=myApiKey";
//    verifyRestTemplateMockForGetObject(expectedUri);
//  }

  @Test
  public void getOrganizationByOrganizationId() throws Exception {
    setupRestTemplateMockForGetObject();

    Organization org = organizationRestClient.getOrganizationByOrganizationId("myOrgId");
    String expectedUri = hostUrl + "/organizations/myOrgId?apikey=myApiKey";
    verifyRestTemplateMockForGetObject(expectedUri);
  }

  @Test
  public void getOrganizationFromCrm() throws Exception {
    setupRestTemplateMockForGetObject();

    Organization org = organizationRestClient.getOrganizationFromCrm("myOrgId");
    String expectedUri = hostUrl + "/organizations/crm/myOrgId?apikey=myApiKey";
    verifyRestTemplateMockForGetObject(expectedUri);
  }

  @Test
  public void getOrganizationsFromCrm() throws Exception {
    setupRestTemplateMockForGetList();

    ResponseListWrapper<Organization> results = organizationRestClient.getOrganizationsFromCrm("myPage");
    String expectedUri = hostUrl + "/organizations/crm?apikey=myApiKey&nextPage=myPage";
    verifyRestTemplateMockForGetList(expectedUri);
  }

  @Test
  public void getUserByEmail() throws Exception {
    setupRestTemplateMockForGetContact();

    Contact org = organizationRestClient.getUserByEmail("my@email.com");

    String expectedUri = hostUrl + "/user/my@email.com?apikey=myApiKey";
    verifyRestTemplateMockForGetObjectContact(expectedUri);
  }

  @Test
  public void suggestOrganizations() throws Exception {

    setupRestTemplateMockForGetSuggestions();
    organizationRestClient.suggestOrganizations("begins");

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
    OrganizationSearchListWrapper organizationSearchListWrapper = new OrganizationSearchListWrapper(new ArrayList<OrganizationSearchBean>());

    when(templateMock.getForObject(any(String.class), eq(OrganizationSearchListWrapper.class)))
        .thenReturn(organizationSearchListWrapper);

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
    verify(templateMock, times(1)).getForObject(uriCaptor.capture(), eq(OrganizationSearchListWrapper.class));

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
    ResponseListWrapper<Dataset> response = new ResponseListWrapper();

    when(templateMock.getForObject(any(String.class), eq(ResponseListWrapper.class)))
        .thenReturn(response);

    uriCaptor = ArgumentCaptor.forClass(String.class);
  }

  private void verifyRestTemplateMockForGetListDataset(String expectedUri) {
    verify(templateMock, times(1)).getForObject(uriCaptor.capture(), eq(ResponseListWrapper.class));

    assertEquals(expectedUri, uriCaptor.getValue());
  }

  private void setupRestTemplateMockForGetList()
  {
    ResponseListWrapper<Organization> response = new ResponseListWrapper<>();

    when(templateMock.getForObject(any(String.class), eq(ResponseListWrapper.class)))
        .thenReturn(response);

    uriCaptor = ArgumentCaptor.forClass(String.class);
  }

  private void verifyRestTemplateMockForGetList(String expectedUri) {
    verify(templateMock, times(1)).getForObject(uriCaptor.capture(), eq(ResponseListWrapper.class));

    assertEquals(expectedUri, uriCaptor.getValue());
  }
}