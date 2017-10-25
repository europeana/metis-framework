package eu.europeana.metis.core.rest;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.europeana.metis.core.api.MetisKey;
import eu.europeana.metis.core.api.Options;
import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.core.common.OrganizationRole;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.exceptions.OrganizationAlreadyExistsException;
import eu.europeana.metis.core.organization.Organization;
import eu.europeana.metis.core.rest.exception.RestResponseExceptionHandler;
import eu.europeana.metis.core.search.common.OrganizationSearchBean;
import eu.europeana.metis.core.service.DatasetService;
import eu.europeana.metis.core.service.MetisAuthorizationService;
import eu.europeana.metis.core.service.OrganizationService;
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

/**
 * Created by erikkonijnenburg on 16/06/2017.
 */
public class TestOrganizationController {

  private OrganizationService organizationServiceMock;
  private DatasetService datasetServiceMock;
  private MetisAuthorizationService metisAuthorizationServiceMock;
  private MockMvc organizationControllerMock;

  @Before
  public void setUp() throws Exception {
    organizationServiceMock = mock(OrganizationService.class);
    datasetServiceMock = mock(DatasetService.class);
    metisAuthorizationServiceMock = mock(MetisAuthorizationService.class);

    OrganizationController organizationController = new OrganizationController(organizationServiceMock,
        datasetServiceMock, metisAuthorizationServiceMock);
    organizationControllerMock = MockMvcBuilders
        .standaloneSetup(organizationController)
        .setControllerAdvice(new RestResponseExceptionHandler())
        .build();
  }

  @Test
  public void createOrganization() throws Exception {
     prepareAuthorizationMockWithValidKey("myApiKey", Options.WRITE);

     Organization organization = new Organization();
     organizationControllerMock.perform(post("/organizations")
         .param("organizationId", "myOrg").param("apikey", "myApiKey")
         .contentType(TestUtils.APPLICATION_JSON_UTF8)
         .content(TestUtils.convertObjectToJsonBytes(organization)))
         .andExpect(status().is(201))
         .andExpect(content().string(""));

    verify(metisAuthorizationServiceMock, times(1)).getKeyFromId("myApiKey");
    verify(organizationServiceMock, times(1)).checkRestrictionsOnCreate(any(Organization.class));
    verify(organizationServiceMock, times(1)).createOrganization(any(Organization.class));
    verifyNoMoreInteractions(metisAuthorizationServiceMock, datasetServiceMock,organizationServiceMock);

  }

  @Test
  public void createOrganization_withInvalidKey_returns401() throws Exception {
    when(metisAuthorizationServiceMock.getKeyFromId("myApiKey")).thenReturn(null);

    Organization organization = new Organization();
    organizationControllerMock.perform(post("/organizations")
        .param("organizationId", "myOrg").param("apikey", "myApiKey")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(organization)))
        .andExpect(status().is(401))
        .andExpect(jsonPath("$.errorMessage", is("No API key found with name: myApiKey")));
  }

  @Test
  public void createOrganization_withExistingOrganization_returns409() throws Exception {
    prepareAuthorizationMockWithValidKey("myApiKey", Options.WRITE);

    doThrow(new OrganizationAlreadyExistsException("myOrgID"))
        .when(organizationServiceMock).checkRestrictionsOnCreate(any(Organization.class));

    Organization organization = new Organization();
    organizationControllerMock.perform(post("/organizations")
        .param("organizationId", "myOrg").param("apikey", "myApiKey")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(organization)))
        .andExpect(status().is(409))
        .andExpect(jsonPath("$.errorMessage", is("Organization with organizationId myOrgID already exists")));
  }

  @Test
  public void deleteOrganization_withValidData_returns204() throws Exception {
    prepareAuthorizationMockWithValidKey("myApiKey", Options.WRITE);

    organizationControllerMock.perform(delete("/organizations/myOrganisationId")
        .param("apikey", "myApiKey")
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(204))
        .andExpect(content().string(""));

    ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

    verify(metisAuthorizationServiceMock, times(1)).getKeyFromId("myApiKey");
    verify(organizationServiceMock, times(1)).deleteOrganizationByOrganizationId(argument.capture());
    verifyNoMoreInteractions(metisAuthorizationServiceMock, datasetServiceMock, organizationServiceMock);

    assertEquals("myOrganisationId", argument.getValue());

  }

  @Test
  public void updateOrganization_withValidData_returns204() throws Exception {
    prepareAuthorizationMockWithValidKey("myApiKey", Options.WRITE);

    Organization organization = new Organization();

    prepareAuthorizationMockWithValidKey("myApiKey", Options.WRITE);
    when(organizationServiceMock.getAllOrganizations("2")).thenReturn(getListOfOrginsations());

    organizationControllerMock.perform(put("/organizations/myOrgId")
        .param("apikey", "myApiKey")
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(organization)))
        .andExpect(status().is(204))
        .andExpect(content().string(""));

    verify(metisAuthorizationServiceMock, times(1)).getKeyFromId("myApiKey");
    verify(organizationServiceMock, times(1))
        .checkRestrictionsOnUpdate(any(Organization.class),eq("myOrgId"));
    verify(organizationServiceMock, times(1)).updateOrganization(any(Organization.class));

    verifyNoMoreInteractions(metisAuthorizationServiceMock, datasetServiceMock);
  }

  @Test
  public void getAllOrganizations_withValidData_returns200() throws Exception {

    Organization organization = new Organization();

    prepareAuthorizationMockWithValidKey("myApiKey", Options.WRITE);
    when(organizationServiceMock.getAllOrganizations("2")).thenReturn(getListOfOrginsations());

    organizationControllerMock.perform(get("/organizations")
        .param("apikey", "myApiKey")
        .param("nextPage", "2")
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(organization)))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.results", hasSize(2)))
        .andExpect(jsonPath("$.results[0].id", is("1f1f1f1f1f1f1f1f1f1f1f1f")))
        .andExpect(jsonPath("$.results[0].name", is("Org1")))
        .andExpect(jsonPath("$.results[1].id", is("2f2f2f2f2f2f2f2f2f2f2f2f")))
        .andExpect(jsonPath("$.results[1].name", is("Org2")));


    verify(metisAuthorizationServiceMock, times(1)).getKeyFromId("myApiKey");
    verify(organizationServiceMock, times(1)).getAllOrganizations("2");

    verifyNoMoreInteractions(metisAuthorizationServiceMock, datasetServiceMock);


  }

  @Test
  public void getOrganizationByOrganizationId_withValidData_returns200() throws Exception {
    prepareAuthorizationMockWithValidKey("myApiKey", Options.WRITE);

    Organization organization = new Organization();
    organization.setId(new ObjectId("1f1f1f1f1f1f1f1f1f1f1f1f"));

    prepareAuthorizationMockWithValidKey("myApiKey", Options.WRITE);
    when(organizationServiceMock.getOrganizationByOrganizationId("myOrgId")).
        thenReturn(organization);

    organizationControllerMock.perform(get("/organizations/myOrgId")
        .param("apikey", "myApiKey")
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(organization)))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.id", is("1f1f1f1f1f1f1f1f1f1f1f1f")));

    verify(metisAuthorizationServiceMock, times(1)).getKeyFromId("myApiKey");
    verify(organizationServiceMock, times(1))
        .getOrganizationByOrganizationId("myOrgId");

    verifyNoMoreInteractions(metisAuthorizationServiceMock, datasetServiceMock);
  }

  @Test
  public void getAllOrganizationsByCountryIsoCode_withValidData_returns200() throws Exception {
    Organization organization = new Organization();

    prepareAuthorizationMockWithValidKey("myApiKey", Options.WRITE);
    when(organizationServiceMock.getAllOrganizationsByCountry(Country.NETHERLANDS,"2"))
        .thenReturn(getListOfOrginsations());

    organizationControllerMock.perform(get("/organizations/country/NL")
        .param("apikey", "myApiKey")
        .param("nextPage", "2")
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(organization)))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.results", hasSize(2)))
        .andExpect(jsonPath("$.results[0].id", is("1f1f1f1f1f1f1f1f1f1f1f1f")))
        .andExpect(jsonPath("$.results[0].name", is("Org1")))
        .andExpect(jsonPath("$.results[1].id", is("2f2f2f2f2f2f2f2f2f2f2f2f")))
        .andExpect(jsonPath("$.results[1].name", is("Org2")));


    verify(metisAuthorizationServiceMock, times(1)).getKeyFromId("myApiKey");
    verify(organizationServiceMock, times(1)).
        getAllOrganizationsByCountry(Country.NETHERLANDS, "2");
  }

  @Test
  public void getAllOrganizationsByOrganizationRoles_withValidData_returns200() throws Exception {
    prepareAuthorizationMockWithValidKey("myApiKey", Options.WRITE);

    prepareAuthorizationMockWithValidKey("myApiKey", Options.WRITE);
    when(organizationServiceMock.getAllOrganizationsByOrganizationRole(anyListOf(OrganizationRole.class), eq("2")))
        .thenReturn(getListOfOrginsations());

    organizationControllerMock.perform(get("/organizations/roles")
        .param("apikey", "myApiKey")
        .param("nextPage", "2")
        .param("organizationRoles", "EUROPEANA,OTHER" )
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.results", hasSize(2)))
        .andExpect(jsonPath("$.results[0].id", is("1f1f1f1f1f1f1f1f1f1f1f1f")))
        .andExpect(jsonPath("$.results[0].name", is("Org1")))
        .andExpect(jsonPath("$.results[1].id", is("2f2f2f2f2f2f2f2f2f2f2f2f")))
        .andExpect(jsonPath("$.results[1].name", is("Org2")));


    verify(metisAuthorizationServiceMock, times(1)).getKeyFromId("myApiKey");
    verify(organizationServiceMock, times(1)).
        getAllOrganizationsByOrganizationRole(anyListOf(OrganizationRole.class), eq("2"));

  }

  @Test
  public void getAllOrganizationsByOrganizationRoles_withInvalidValidData_returns406() throws Exception {
    prepareAuthorizationMockWithValidKey("myApiKey", Options.WRITE);

    prepareAuthorizationMockWithValidKey("myApiKey", Options.WRITE);
    when(organizationServiceMock.getAllOrganizationsByOrganizationRole(anyListOf(OrganizationRole.class), eq("2")))
        .thenReturn(getListOfOrginsations());

    organizationControllerMock.perform(get("/organizations/roles")
        .param("apikey", "myApiKey")
        //.param("nextPage", "2")
        .param("organizationRoles", "" )
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(406))
        .andExpect(jsonPath("$.errorMessage", is("Organization roles malformed or empty")));

    verify(metisAuthorizationServiceMock, times(1)).getKeyFromId("myApiKey");
  }

  @Test
  public void suggestOrganizations_withValidData_returns200() throws Exception {
    prepareAuthorizationMockWithValidKey("myApiKey", Options.WRITE);

    when(organizationServiceMock.suggestOrganizations("ab"))
        .thenReturn(getListOfOrganizationSearchBeans());

    organizationControllerMock.perform(get("/organizations/suggest")
        .param("apikey", "myApiKey")
        .param("searchTerm", "ab")
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.Organizations", hasSize(2)))
        .andExpect(jsonPath("$.Organizations[0].organizationId", is("1f1f1f1f1f1f1f1f1f1f1f1f")))
        .andExpect(jsonPath("$.Organizations[0].engLabel", is("Org1")))
        .andExpect(jsonPath("$.Organizations[1].organizationId", is("2f2f2f2f2f2f2f2f2f2f2f2f")))
        .andExpect(jsonPath("$.Organizations[1].engLabel", is("Org2")));

    verify(metisAuthorizationServiceMock, times(1)).getKeyFromId("myApiKey");
    verify(organizationServiceMock, times(1)).suggestOrganizations( "ab");
  }

  @Test
  public void getAllDatasetsByOrganizationId_withValidData_return200() throws Exception {
    prepareAuthorizationMockWithValidKey("myApiKey", Options.WRITE);

    when(organizationServiceMock.getAllDatasetsByOrganizationId("myOrganizationId", "3"))
        .thenReturn(getListOfDataSets());

    organizationControllerMock.perform(get("/organizations/myOrganizationId/datasets")
        .param("apikey", "myApiKey")
        .param("nextPage", "3")
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.results", hasSize(2)))
        .andExpect(jsonPath("$.results[0].id", is("1f1f1f1f1f1f1f1f1f1f1f1f")))
        .andExpect(jsonPath("$.results[0].datasetName", is("Dataset 1")))
        .andExpect(jsonPath("$.results[1].id", is("2f2f2f2f2f2f2f2f2f2f2f2f")))
        .andExpect(jsonPath("$.results[1].datasetName", is("Dataset 2")));
  }


  @Test
  public void isOrganizationIdOptedIn() throws Exception {
    prepareAuthorizationMockWithValidKey("myApiKey", Options.WRITE);
    // /organizations/{organizationId}/optInIIIF
    // isOptedInIIIF

    when(organizationServiceMock.isOptedInIIIF("myOrganizationId"))
        .thenReturn(true);

    organizationControllerMock.perform(get("/organizations/myOrganizationId/optInIIIF")
        .param("apikey", "myApiKey")
        .param("nextPage", "3")
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(null)))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.results.optInIIIF", is(true)));
  }

  private void prepareAuthorizationMockWithValidKey(String keyName, Options write) {
    MetisKey key = new MetisKey();
    key.setOptions(write);
    when(metisAuthorizationServiceMock.getKeyFromId(keyName)).thenReturn(key);
  }


  private List<Dataset> getListOfDataSets() {
    List<Dataset> list = new ArrayList<>() ;

    Dataset s1 = new Dataset();
    s1.setId( new ObjectId("1f1f1f1f1f1f1f1f1f1f1f1f"));
    s1.setDatasetName("Dataset 1");
    list.add(s1);

    Dataset s2 = new Dataset();
    s2.setDatasetName("Dataset 2");
    s2.setId( new ObjectId("2f2f2f2f2f2f2f2f2f2f2f2f"));

    list.add(s2);

    return list;
  }

  private List<Organization> getListOfOrginsations() {
    List<Organization> list = new ArrayList<>() ;
    Organization o1 = new Organization();
    o1.setName("Org1");
    o1.setId( new ObjectId("1f1f1f1f1f1f1f1f1f1f1f1f"));
    list.add(o1);

    Organization o2 = new Organization();
    o2.setName("Org2");
    o2.setId( new ObjectId("2f2f2f2f2f2f2f2f2f2f2f2f"));
    list.add(o2);
    return list;
  }

  private List<OrganizationSearchBean> getListOfOrganizationSearchBeans() {
    List<OrganizationSearchBean> list = new ArrayList<>() ;
    OrganizationSearchBean o1 = new OrganizationSearchBean();

    o1.setEngLabel("Org1");
    o1.setOrganizationId("1f1f1f1f1f1f1f1f1f1f1f1f");
    list.add(o1);

    OrganizationSearchBean o2 = new OrganizationSearchBean();
    o2.setEngLabel("Org2");
    o2.setOrganizationId( "2f2f2f2f2f2f2f2f2f2f2f2f");
    list.add(o2);
    return list;
  }
}