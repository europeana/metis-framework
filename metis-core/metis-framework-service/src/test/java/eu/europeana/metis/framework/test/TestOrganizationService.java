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
package eu.europeana.metis.framework.test;

import eu.europeana.metis.framework.common.Country;
import eu.europeana.metis.framework.common.HarvestingMetadata;
import eu.europeana.metis.framework.dao.OrganizationDao;
import eu.europeana.metis.framework.dataset.Dataset;
import eu.europeana.metis.framework.exceptions.NoOrganizationExceptionFound;
import eu.europeana.metis.framework.mongo.MongoProvider;
import eu.europeana.metis.framework.organization.Organization;
import eu.europeana.metis.framework.service.OrganizationService;
import eu.europeana.metis.search.service.MetisSearchService;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by ymamakis on 2/19/16.
 */

public class TestOrganizationService {

    private MongoProvider mongoProvider;
    private OrganizationDao organizationDao;
    private OrganizationService service;
    private Datastore datastore;
    private Organization org;
    private MetisSearchService searchService;

    @Before
    public void prepare() {
        mongoProvider = Mockito.mock(MongoProvider.class);
        organizationDao = Mockito.mock(OrganizationDao.class);
        ReflectionTestUtils.setField(organizationDao, "provider", mongoProvider);
        service = new OrganizationService();
        datastore = Mockito.mock(Datastore.class);
        searchService=Mockito.mock(MetisSearchService.class);
        ReflectionTestUtils.setField(service, "orgDao", organizationDao);
        ReflectionTestUtils.setField(service, "searchService",searchService);
        org = new Organization();
        org.setId(new ObjectId());
        org.setOrganizationId("orgId");
        org.setDatasets(new ArrayList<Dataset>());
        org.setOrganizationUri("testUri");
        org.setHarvestingMetadata(new HarvestingMetadata());
        org.setOptInIIIF(true);


    }

    @Test
    public void testOrganizationCreation() {
        Mockito.when(mongoProvider.getDatastore()).thenReturn(datastore);
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
            }).when(searchService).addOrganizationForSearch(Mockito.anyString(),Mockito.anyString(),Mockito.anyList());
            service.createOrganization(org);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testOrganizationUpdate() {
        Mockito.when(mongoProvider.getDatastore()).thenReturn(datastore);

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
            }).when(searchService).addOrganizationForSearch(Mockito.anyString(),Mockito.anyString(),Mockito.anyList());
            service.updateOrganization(org);
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testOrganizationDelete() {
        Mockito.when(mongoProvider.getDatastore()).thenReturn(datastore);

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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRetrieveOrgByOrgId() {
        Mockito.when(mongoProvider.getDatastore()).thenReturn(datastore);
        Mockito.when(organizationDao.getByOrganizationId("string")).thenReturn(org);

        try {
            Organization orgRet = service.getOrganizationByOrganizationId("string");
            Assert.assertEquals(org, orgRet);
        } catch (NoOrganizationExceptionFound e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRetrieveOptin() {
        Mockito.when(mongoProvider.getDatastore()).thenReturn(datastore);
        Mockito.when(organizationDao.getById("string")).thenReturn(org);
            boolean optedIn = service.isOptedInForIIIF("string");
            Assert.assertTrue(optedIn);
    }

    @Test
    public void testRetrieveOrgById() {
        Mockito.when(mongoProvider.getDatastore()).thenReturn(datastore);
        Mockito.when(organizationDao.getById("string")).thenReturn(org);
        try {
            Organization orgRet = service.getOrganizationById("string");
            Assert.assertEquals(org, orgRet);
        } catch (NoOrganizationExceptionFound e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRetrieveOrgByCountry(){
        Mockito.when(mongoProvider.getDatastore()).thenReturn(datastore);
        List<Organization> orgs = new ArrayList<>();
        orgs.add(org);
        orgs.add(org);
        orgs.add(org);
        Mockito.when(organizationDao.getAllByCountry(Country.ALBANIA)).thenReturn(orgs);
        try {
            List<Organization> orgRet = service.getAllOrganizationsByCountry(Country.ALBANIA);
            Assert.assertEquals(orgs, orgRet);
        } catch (NoOrganizationExceptionFound e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRetrieveAll() {

        List<Organization> orgs = new ArrayList<>();
        orgs.add(org);
        orgs.add(org);
        orgs.add(org);
        Mockito.when(mongoProvider.getDatastore()).thenReturn(datastore);
        Mockito.when(organizationDao.getAll()).thenReturn(orgs);
        try {
            List<Organization> orgRet = service.getAllOrganizations();
            Assert.assertEquals(orgs, orgRet);
        } catch (NoOrganizationExceptionFound e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testRetrieveDatasets() {

        List<Dataset> datasets = new ArrayList<>();
        datasets.add(new Dataset());
        datasets.add(new Dataset());
        datasets.add(new Dataset());
        org.setDatasets(datasets);
        Mockito.when(mongoProvider.getDatastore()).thenReturn(datastore);
        try {
            Mockito.when(organizationDao.getAllDatasetsByOrganization("string")).thenReturn(org.getDatasets());

            List<Dataset> datasetsRet = service.getDatasetsByOrganization("string");
            Assert.assertEquals(datasets, datasetsRet);
        } catch (NoOrganizationExceptionFound e) {
            e.printStackTrace();
        }
    }
}
