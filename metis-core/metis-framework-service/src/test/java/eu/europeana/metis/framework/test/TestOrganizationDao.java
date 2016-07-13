package eu.europeana.metis.framework.test;

import eu.europeana.metis.framework.common.Country;
import eu.europeana.metis.framework.common.HarvestingMetadata;
import eu.europeana.metis.framework.common.Language;
import eu.europeana.metis.framework.common.Role;
import eu.europeana.metis.framework.dao.DatasetDao;
import eu.europeana.metis.framework.dao.OrganizationDao;
import eu.europeana.metis.framework.dataset.Dataset;
import eu.europeana.metis.framework.dataset.OAIDatasetMetadata;
import eu.europeana.metis.framework.dataset.WorkflowStatus;
import eu.europeana.metis.framework.exceptions.NoOrganizationExceptionFound;
import eu.europeana.metis.framework.mongo.MongoProvider;
import eu.europeana.metis.framework.organization.Organization;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ymamakis on 2/19/16.
 */
public class TestOrganizationDao {
    private Organization org;
    private Dataset ds;
    private OrganizationDao orgDao;
    private DatasetDao dsDao;
    @Before
    public void prepare() {
        MongoDBInstance.start();
        try {
            MongoProvider provider = new MongoProvider("localhost",10000, "test",null,null);
            orgDao = new OrganizationDao();
            ReflectionTestUtils.setField(orgDao,"provider",provider);

            org = new Organization();
            org.setOrganizationId("orgId");
            org.setDatasets(new ArrayList<>());
            org.setOrganizationUri("testUri");
            org.setHarvestingMetadata(new HarvestingMetadata());
            ds = new Dataset();
            ds.setAccepted(true);
            ds.setAssignedToLdapId("Lemmy");
            ds.setCountry(Country.ALBANIA);
            ds.setCreated(new Date(1000));
            ds.setCreatedByLdapId("Lemmy");
            ds.setDataProvider("prov");
            ds.setDeaSigned(true);
            ds.setDescription("Test description");
            List<String> DQA = new ArrayList<>();
            DQA.add("test DQA");
            ds.setDQA(DQA);
            ds.setFirstPublished(new Date(1000));
            ds.setHarvestedAt(new Date(1000));
            ds.setLanguage(Language.AR);
            ds.setLastPublished(new Date(1000));
            ds.setMetadata(new OAIDatasetMetadata());
            ds.setName("testName");
            ds.setNotes("test Notes");
            ds.setRecordsPublished(100);
            ds.setRecordsSubmitted(199);
            ds.setReplacedBy("replacedBY");
            List<String> sources = new ArrayList<>();
            sources.add("testSource");
            ds.setSource(sources);
            List<String> subjects = new ArrayList<>();
            subjects.add("testSubject");
            ds.setSubject(subjects);
            ds.setSubmittedAt(new Date(1000));
            ds.setUpdated(new Date(1000));
            ds.setWorkflowStatus(WorkflowStatus.ACCEPTANCE);

            dsDao = new DatasetDao();
            ReflectionTestUtils.setField(dsDao,"provider",provider);

        } catch (IOException e){
            e.printStackTrace();
        }

    }

    @Test
    public void testCreateRetrieveOrg(){
        dsDao.create(ds);
        List<Dataset> datasets = new ArrayList<>();
        datasets.add(dsDao.getByName(ds.getName()));
        org.setDatasets(datasets);
        orgDao.create(org);
        Organization retOrg = orgDao.getByOrganizationId(org.getOrganizationId());
        Assert.assertEquals(org.getOrganizationId(), retOrg.getOrganizationId());
        Assert.assertEquals(org.getOrganizationUri(),retOrg.getOrganizationUri());
        Assert.assertEquals(org.getDatasets().size(),retOrg.getDatasets().size());
    }


    @Test
    public void testDeleteOrganization(){
        dsDao.create(ds);
        List<Dataset> datasets = new ArrayList<>();
        datasets.add(dsDao.getByName(ds.getName()));
        org.setDatasets(datasets);
        orgDao.create(org);
        orgDao.delete(org);
        Assert.assertNull(orgDao.getByOrganizationId(org.getOrganizationId()));
    }

    @Test
    public void testDatasets(){
        dsDao.create(ds);
        List<Dataset> datasets = new ArrayList<>();
        datasets.add(dsDao.getByName(ds.getName()));
        org.setDatasets(datasets);
        orgDao.create(org);

        try {
            List<Dataset> dsRet = orgDao.getAllDatasetsByOrganization(org.getOrganizationId());
            Assert.assertTrue(dsRet.size() == 1);
        }catch (NoOrganizationExceptionFound e){
            e.printStackTrace();
        }
    }

    @Test
    public void testGetAll(){
        dsDao.create(ds);
        List<Dataset> datasets = new ArrayList<>();
        datasets.add(dsDao.getByName(ds.getName()));
        org.setDatasets(datasets);
        orgDao.create(org);

        List<Organization> getAll = orgDao.getAll();

        Assert.assertTrue(getAll.size()==1);
    }

    @Test
    public void testUpdate(){
        dsDao.create(ds);
        List<Dataset> datasets = new ArrayList<>();
        datasets.add(dsDao.getByName(ds.getName()));
        org.setDatasets(datasets);
        orgDao.create(org);

        org.setOrganizationUri("testNew");
        org.setName("name");
        org.setModified(new Date());
        org.setCreated(new Date());
        List<Role> roles = new ArrayList<>();
        org.setRoles(roles);
        org.setAcronym("acronym");
        orgDao.update(org);
        Organization organization = orgDao.getByOrganizationId(org.getOrganizationId());

        Assert.assertTrue(StringUtils.equals(organization.getOrganizationUri(),"testNew"));
    }
    @After
    public void destroy(){
        MongoDBInstance.stop();
    }
}
