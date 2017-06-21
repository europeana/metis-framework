/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.metis.core.test;

import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.OrganizationDao;
import eu.europeana.metis.core.dao.ZohoMockClient;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.NoOrganizationFoundException;
import eu.europeana.metis.core.exceptions.OrganizationAlreadyExistsException;
import eu.europeana.metis.core.organization.Organization;
import eu.europeana.metis.core.search.service.MetisSearchService;
import eu.europeana.metis.core.service.OrganizationService;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.solr.client.solrj.SolrServerException;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;


public class TestOrganizationService {

  private OrganizationDao organizationDaoMock;
  private DatasetDao datasetDaoMock;
  private OrganizationService service;

  private Organization org;
  private MetisSearchService searchServiceMock;
  private ZohoMockClient zohoMock;


  @Before
  public void prepare() {
    organizationDaoMock = Mockito.mock(OrganizationDao.class);
    datasetDaoMock = Mockito.mock(DatasetDao.class);
    searchServiceMock = Mockito.mock(MetisSearchService.class);
    zohoMock = Mockito.mock(ZohoMockClient.class);
    service = new OrganizationService(organizationDaoMock, datasetDaoMock, zohoMock, searchServiceMock);

    org = createOrganization("myOrg");
  }

  private Organization createOrganization(String orgName) {
    Organization org = new Organization();
    org.setId(new ObjectId("1f1f1f1f1f1f1f1f1f1f1f1f"));
    org.setName(orgName);
    org.setOrganizationId("orgId");
    org.setDatasetNames(new TreeSet<String>());
    org.setOrganizationUri("testUri");
    org.setOptInIIIF(true);
    return org;
  }

  @Test
  public void testOrganizationCreation() throws IOException, SolrServerException {
      service.createOrganization(org);

      ArgumentCaptor<Organization> organizationArgumentCaptor = ArgumentCaptor.forClass(Organization.class);
      verify(organizationDaoMock, times(1)).create(organizationArgumentCaptor.capture());
      verify(searchServiceMock,times(1)).addOrganizationForSearch(eq("1f1f1f1f1f1f1f1f1f1f1f1f"),
          eq("orgId"), eq("myOrg"), anyListOf(String.class));

      assertSame(org, organizationArgumentCaptor.getValue());
  }

  @Test
  public void testOrganizationUpdate() throws IOException, SolrServerException {

    String id = "2f2f2f2f2f2f2f2f2f2f2f2f";
    when(searchServiceMock.findSolrIdByOrganizationId("orgId")).thenReturn(id);

    service.updateOrganization(org);

    ArgumentCaptor<Organization> organizationArgumentCaptor = ArgumentCaptor.forClass(Organization.class);
    verify(organizationDaoMock, times(1)).update(organizationArgumentCaptor.capture());
    verify(searchServiceMock,times(1)).addOrganizationForSearch(eq(id),
        eq("orgId"), eq("myOrg"), anyListOf(String.class));

    assertSame(org, organizationArgumentCaptor.getValue());
  }

  @Test
  public void testOrganizationDelete() throws IOException, SolrServerException {
    String id = "2f2f2f2f2f2f2f2f2f2f2f2f";
    org.setId(new ObjectId(id));

    service.deleteOrganization(org);
    verify(organizationDaoMock, times(1)).delete(org);
    verify(searchServiceMock, times(1)).deleteFromSearch("2f2f2f2f2f2f2f2f2f2f2f2f");
  }

  @Test
  public void testOrganizationDeleteById() throws IOException, SolrServerException {
    String id = "2f2f2f2f2f2f2f2f2f2f2f2f";

    service.deleteOrganizationByOrganizationId(id);
    verify(organizationDaoMock, times(1)).deleteByOrganizationId(id);
    verify(searchServiceMock, times(1)).deleteFromSearchByOrganizationId("2f2f2f2f2f2f2f2f2f2f2f2f");
  }

  @Test
  public void testRetrieveOrgByOrgId_withExistingOrg_returnsOrganization() throws NoOrganizationFoundException {
    String id = "2f2f2f2f2f2f2f2f2f2f2f2f";
    Organization org = new Organization();
    when(organizationDaoMock.getOrganizationByOrganizationId(id)).thenReturn(org);

    Organization retrievedOrg = service.getOrganizationByOrganizationId(id);

    assertSame(org, retrievedOrg);
  }

  @Test(expected = NoOrganizationFoundException.class)
  public void testRetrieveOrgByOrgId_withNonExistingOrg_throwsException() throws NoOrganizationFoundException {
    String id = "2f2f2f2f2f2f2f2f2f2f2f2f";
    when(organizationDaoMock.getOrganizationByOrganizationId(id)).thenReturn(null);
    Organization retrievedOrg = service.getOrganizationByOrganizationId(id);
  }

  @Test
  public void testRetrieveOptin_withExistingOrg_returnsOrganization() throws NoOrganizationFoundException {
    String id = "2f2f2f2f2f2f2f2f2f2f2f2f";
    Organization org = new Organization();
    org.setOptInIIIF(true);
    when(organizationDaoMock.getOrganizationOptInIIIFByOrganizationId(id)).thenReturn(org);

    boolean optedIn = service.isOptedInIIIF("2f2f2f2f2f2f2f2f2f2f2f2f");

    assertSame(true, optedIn);
  }

  @Test(expected = NoOrganizationFoundException.class)
  public void testRetrieveOptin_withNonExistingOrg_throwsException()
      throws NoOrganizationFoundException {
    String id = "2f2f2f2f2f2f2f2f2f2f2f2f";

    org.setOptInIIIF(true);
    when(organizationDaoMock.getOrganizationOptInIIIFByOrganizationId(id)).thenReturn(null);

    boolean optedIn = service.isOptedInIIIF("2f2f2f2f2f2f2f2f2f2f2f2f");
  }

  @Test
  public void testRetrieveOrgByCountry() {
    List<Organization> orgs = new ArrayList<>();
    orgs.add(org);
    orgs.add(org);
    orgs.add(org);
    when(organizationDaoMock.getAllOrganizationsByCountry(Country.ALBANIA, null))
        .thenReturn(orgs);
    List<Organization> orgRet = service.getAllOrganizationsByCountry(Country.ALBANIA, null);
    Assert.assertEquals(orgs, orgRet);
  }

  @Test
  public void testRetrieveAll() {
    List<Organization> orgs = new ArrayList<>();
    orgs.add(org);
    orgs.add(org);
    orgs.add(org);
    Mockito.when(organizationDaoMock.getAllOrganizations(null)).thenReturn(orgs);
    List<Organization> orgRet = service.getAllOrganizations(null);
    Assert.assertEquals(orgs, orgRet);
  }

  @Test
  public void testGetOrganizationByIdFromCRM_withExistingOrgId_returnOrg()
      throws IOException, ParseException, NoOrganizationFoundException {
    String id = "2f2f2f2f2f2f2f2f2f2f2f2f";

    Organization org = new Organization();
    when(zohoMock.getOrganizationById(id)).thenReturn(org);
    Organization orgRet = service.getOrganizationByIdFromCRM(id);

    assertSame(org, orgRet);
  }

  @Test(expected = NoOrganizationFoundException.class)
  public void testGetOrganizationByIdFromCRM_withNonExistingOrgId_throwsException()
      throws IOException, ParseException, NoOrganizationFoundException {
    String id = "2f2f2f2f2f2f2f2f2f2f2f2f";

    when(zohoMock.getOrganizationById(id)).thenReturn(null);
    service.getOrganizationByIdFromCRM(id);
  }

  @Test(expected = BadContentException.class)
  public void checkRestrictionsOnCreate_emptyOrgID_throwsException()
      throws BadContentException, OrganizationAlreadyExistsException {

    Organization org = new Organization();
    org.setOrganizationId(null);
    service.checkRestrictionsOnCreate(org);
  }

  @Test(expected = OrganizationAlreadyExistsException.class)
  public void checkRestrictionsOnCreate_existingOrgId_throwsException()
      throws BadContentException, OrganizationAlreadyExistsException {
    String orgId = "myOrg";

    when(organizationDaoMock.existsOrganizationByOrganizationId(orgId)).thenReturn(true);

    Organization org = new Organization();
    org.setOrganizationId(orgId);
    service.checkRestrictionsOnCreate(org);
  }

  @Test(expected = BadContentException.class)
  public void checkRestrictionsOnCreate_emptyDatasets_throwsException()
      throws BadContentException, OrganizationAlreadyExistsException {
    String orgId = "myOrg";

    when(organizationDaoMock.existsOrganizationByOrganizationId(orgId)).thenReturn(false);

    Organization org = new Organization();
    org.setOrganizationId(orgId);
    Set<String> set = new HashSet<>();
    set.add("TooMuch");
    org.setDatasetNames(set);
    service.checkRestrictionsOnCreate(org);
  }

  @Test(expected = BadContentException.class)
  public void checkRestrictionsOnUpdate_nonMatchingOrgIds_throwsException()
      throws BadContentException, NoOrganizationFoundException {
    String orgId = "myOrg";

    Organization org = new Organization();
    org.setOrganizationId(orgId);
    Set<String> set = new HashSet<>();
    set.add("TooMuch");
    org.setDatasetNames(set);
    service.checkRestrictionsOnUpdate(org, "nonMatchingOrgId");
  }

  @Test(expected = BadContentException.class)
  public void checkRestrictionsOnUpdate_emptyDatasets_throwsException()
      throws BadContentException, NoOrganizationFoundException {
    String orgId = "myOrg";

    Organization org = new Organization();
    org.setOrganizationId(orgId);
    Set<String> set = new HashSet<>();
    set.add("TooMuch");
    org.setDatasetNames(set);
    service.checkRestrictionsOnUpdate(org, orgId);
  }

  @Test(expected = NoOrganizationFoundException.class)
  public void checkRestrictionsOnUpdate_nonExistingOrganisation_throwsException()
      throws BadContentException, NoOrganizationFoundException {
    String orgId = "myOrg";
    Organization org = new Organization();
    org.setOrganizationId(orgId);

    when(organizationDaoMock.getOrganizationByOrganizationId(orgId)).thenReturn(null);
    service.checkRestrictionsOnUpdate(org, orgId);
  }

  @Test
  public void testRetrieveDatasets_withExistingOrg_returnsDataSets() throws NoOrganizationFoundException {

    List<Dataset> datasets = new ArrayList<>();
    datasets.add(new Dataset());
    datasets.add(new Dataset());
    datasets.add(new Dataset());

    Organization org = new Organization();

    String id = "string";
    Mockito.when(organizationDaoMock.getOrganizationByOrganizationId(id)).thenReturn(org);
    Mockito.when(datasetDaoMock.getAllDatasetsByOrganizationId(id, null)).thenReturn(datasets);

    List<Dataset> datasetsRet = service.getAllDatasetsByOrganizationId(id, null);
    verify(organizationDaoMock, times(1)).getOrganizationByOrganizationId(id);
    verify(datasetDaoMock, times(1)).getAllDatasetsByOrganizationId(id, null);

    Assert.assertEquals(datasets, datasetsRet);
  }

  @Test(expected = NoOrganizationFoundException.class)
  public void testRetrieveDatasets_withNonExistingOrg_throwsException() throws NoOrganizationFoundException {

    String id = "string";
    Mockito.when(organizationDaoMock.getOrganizationByOrganizationId(id)).thenReturn(null);

    List<Dataset> datasetsRet = service.getAllDatasetsByOrganizationId(id, null);
  }
}
