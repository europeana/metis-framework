package eu.europeana.metis.framework.test;

import eu.europeana.metis.framework.common.HarvestingMetadata;
import eu.europeana.metis.framework.dao.OrganizationDao;
import eu.europeana.metis.framework.dataset.Dataset;
import eu.europeana.metis.framework.exceptions.NoOrganizationExceptionFound;
import eu.europeana.metis.framework.mongo.MongoProvider;
import eu.europeana.metis.framework.organization.Organization;
import eu.europeana.metis.framework.service.OrganizationService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mongodb.morphia.Datastore;
import org.springframework.test.util.ReflectionTestUtils;

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

    @Before
    public void prepare() {
        mongoProvider = Mockito.mock(MongoProvider.class);
        organizationDao = Mockito.mock(OrganizationDao.class);
        ReflectionTestUtils.setField(organizationDao, "provider", mongoProvider);
        service = new OrganizationService();
        datastore = Mockito.mock(Datastore.class);
        ReflectionTestUtils.setField(service, "orgDao", organizationDao);
        org = new Organization();
        org.setOrganizationId("orgId");
        org.setDatasets(new ArrayList<>());
        org.setOrganizationUri("testUri");
        org.setHarvestingMetadata(new HarvestingMetadata());


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
        service.createOrganization(org);
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
        service.updateOrganization(org);
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
        service.deleteOrganization(org);
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
