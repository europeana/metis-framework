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
import eu.europeana.metis.utils.NetworkUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Created by ymamakis on 2/19/16.
 */
public class TestOrganizationDao {
    private static Organization org;
    private static Dataset ds;
    private static OrganizationDao orgDao;
    private static DatasetDao dsDao;
    private static eu.europeana.metis.mongo.MongoProvider mongoProvider;
    private static int port;

    @BeforeClass
    public static void prepare() throws IOException {
        port = NetworkUtil.getAvailableLocalPort();
        mongoProvider = new eu.europeana.metis.mongo.MongoProvider();
        mongoProvider.start(port);
        try {
            MongoProvider provider = new MongoProvider("localhost",port, "test",null,null);
            orgDao = new OrganizationDao();
            ReflectionTestUtils.setField(orgDao,"provider",provider);

            org = new Organization();
            org.setOrganizationId("orgId");
            org.setDatasets(new ArrayList<Dataset>());
            org.setOrganizationUri("testUri");
            org.setHarvestingMetadata(new HarvestingMetadata());
            org.setCountry(Country.ALBANIA);
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
    public void testGetAllByCountry(){
        dsDao.create(ds);
        List<Dataset> datasets = new ArrayList<>();
        datasets.add(dsDao.getByName(ds.getName()));
        org.setDatasets(datasets);
        orgDao.create(org);
        List<Organization> getAll = orgDao.getAllByCountry(Country.ALBANIA);
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
    @AfterClass
    public static void destroy(){
        mongoProvider.stop();
    }
}
