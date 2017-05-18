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
import eu.europeana.metis.core.common.HarvestingMetadata;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.OrganizationDao;
import eu.europeana.metis.core.dao.ZohoMockClient;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.exceptions.NoOrganizationFoundException;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.organization.Organization;
import eu.europeana.metis.core.search.service.MetisSearchService;
import eu.europeana.metis.core.service.OrganizationService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import org.apache.solr.client.solrj.SolrServerException;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mongodb.morphia.Datastore;
import org.springframework.test.util.ReflectionTestUtils;

public class TestOrganizationService {

  private MorphiaDatastoreProvider morphiaDatastoreProvider;
  private OrganizationDao organizationDao;
  private DatasetDao datasetDao;
  private OrganizationService service;
  private Datastore datastore;
  private Organization org;
  private MetisSearchService searchService;

  @Before
  public void prepare() {
    morphiaDatastoreProvider = Mockito.mock(MorphiaDatastoreProvider.class);
    organizationDao = Mockito.mock(OrganizationDao.class);
    ReflectionTestUtils.setField(organizationDao, "provider", morphiaDatastoreProvider);
    datasetDao = Mockito.mock(DatasetDao.class);
    ReflectionTestUtils.setField(datasetDao, "provider", morphiaDatastoreProvider);
    service = new OrganizationService(organizationDao, datasetDao, new ZohoMockClient(),
        searchService);
    datastore = Mockito.mock(Datastore.class);
    searchService = Mockito.mock(MetisSearchService.class);
    ReflectionTestUtils.setField(service, "organizationDao", organizationDao);
    ReflectionTestUtils.setField(service, "searchService", searchService);
    org = new Organization();
    org.setId(new ObjectId());
    org.setOrganizationId("orgId");
    org.setDatasetNames(new TreeSet<String>());
    org.setOrganizationUri("testUri");
    org.setHarvestingMetadata(new HarvestingMetadata());
    org.setOptInIIIF(true);
  }

  @Test
  public void testOrganizationCreation() {
    Mockito.when(morphiaDatastoreProvider.getDatastore()).thenReturn(datastore);
    Mockito.doAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(organizationDao).create(org);

    try {
      Mockito.doAnswer(new Answer<Object>() {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
          return null;
        }
      }).when(searchService)
          .addOrganizationForSearch(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
              Mockito.anyList());
      service.createOrganization(org);
    } catch (IOException | SolrServerException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testOrganizationUpdate() {
    Mockito.when(morphiaDatastoreProvider.getDatastore()).thenReturn(datastore);

    Mockito.doAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(organizationDao).update(org);
    try {
      Mockito.doAnswer(new Answer<Object>() {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
          return null;
        }
      }).when(searchService)
          .addOrganizationForSearch(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
              Mockito.anyList());
      service.updateOrganization(org);
    } catch (SolrServerException | IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testOrganizationDelete() {
    Mockito.when(morphiaDatastoreProvider.getDatastore()).thenReturn(datastore);

    Mockito.doAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(organizationDao).delete(org);
    try {
      Mockito.doAnswer(new Answer<Object>() {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
          return null;
        }
      }).when(searchService).deleteFromSearch(Mockito.anyString());
      service.deleteOrganization(org);
    } catch (IOException | SolrServerException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testRetrieveOrgByOrgId() {
    Mockito.when(morphiaDatastoreProvider.getDatastore()).thenReturn(datastore);
    Mockito.when(organizationDao.getOrganizationByOrganizationId("string")).thenReturn(org);

    try {
      Organization orgRet = service.getOrganizationByOrganizationId("string");
      Assert.assertEquals(org, orgRet);
    } catch (NoOrganizationFoundException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testRetrieveOptin() throws NoOrganizationFoundException {
    Mockito.when(morphiaDatastoreProvider.getDatastore()).thenReturn(datastore);
    Mockito.when(organizationDao.getOrganizationOptInIIIFByOrganizationId("string"))
        .thenReturn(org);
    boolean optedIn = service.isOptedInIIIF("string");
    Assert.assertTrue(optedIn);
  }

  @Test
  public void testRetrieveOrgByCountry() {
    Mockito.when(morphiaDatastoreProvider.getDatastore()).thenReturn(datastore);
    List<Organization> orgs = new ArrayList<>();
    orgs.add(org);
    orgs.add(org);
    orgs.add(org);
    Mockito.when(organizationDao.getAllOrganizationsByCountry(Country.ALBANIA, null))
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
    Mockito.when(morphiaDatastoreProvider.getDatastore()).thenReturn(datastore);
    Mockito.when(organizationDao.getAllOrganizations(null)).thenReturn(orgs);
    List<Organization> orgRet = service.getAllOrganizations(null);
    Assert.assertEquals(orgs, orgRet);
  }

  @Test
  public void testRetrieveDatasets() {

    List<Dataset> datasets = new ArrayList<>();
    datasets.add(new Dataset());
    datasets.add(new Dataset());
    datasets.add(new Dataset());
    Mockito.when(morphiaDatastoreProvider.getDatastore()).thenReturn(datastore);
    try {
      Mockito.when(service.getOrganizationByOrganizationId("string"))
          .thenReturn(new Organization());
      Mockito.when(datasetDao.getAllDatasetsByOrganizationId("string", null)).thenReturn(datasets);

      List<Dataset> datasetsRet = service.getAllDatasetsByOrganizationId("string", null);
      Assert.assertEquals(datasets, datasetsRet);
    } catch (NoOrganizationFoundException e) {
      e.printStackTrace();
    }
  }
}
