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
import eu.europeana.metis.framework.dao.DatasetDao;
import eu.europeana.metis.framework.dataset.Dataset;
import eu.europeana.metis.framework.dataset.OAIDatasetMetadata;
import eu.europeana.metis.framework.dataset.WorkflowStatus;
import eu.europeana.metis.framework.mongo.MongoProvider;
import eu.europeana.metis.framework.organization.Organization;
import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Created by ymamakis on 2/19/16.
 */
public class TestDatasetDao {
    private static DatasetDao dsDao;
    private static Organization org;
    private static Dataset ds;
    private static EmbeddedLocalhostMongo embeddedLocalhostMongo;

    @BeforeClass
    public static void prepare() throws IOException {
        embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
        embeddedLocalhostMongo.start();
        String mongoHost = embeddedLocalhostMongo.getMongoHost();
        int mongoPort = embeddedLocalhostMongo.getMongoPort();
        try {
            MongoProvider provider = new MongoProvider(mongoHost, mongoPort, "test",null,null);
            dsDao = new DatasetDao();
            org = new Organization();
            org.setOrganizationId("orgId");
            org.setDatasets(new ArrayList<Dataset>());
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
            ReflectionTestUtils.setField(dsDao,"provider",provider);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testCreateRetrieveDataset(){
        dsDao.createDatasetForOrganization(org,ds);
        Dataset dsRet = dsDao.getByName(ds.getName());
        Assert.assertEquals(ds.getName(),dsRet.getName());
        Assert.assertEquals(ds.getAssignedToLdapId(),dsRet.getAssignedToLdapId());
        Assert.assertEquals(ds.getCountry(),dsRet.getCountry());
        Assert.assertEquals(ds.getCreated(),dsRet.getCreated());
        Assert.assertEquals(ds.getCreatedByLdapId(),dsRet.getCreatedByLdapId());
        Assert.assertEquals(ds.getDataProvider(),dsRet.getDataProvider());
        Assert.assertEquals(ds.getDQA(),dsRet.getDQA());
        Assert.assertEquals(ds.getDescription(),ds.getDescription());
        Assert.assertEquals(ds.getFirstPublished(),dsRet.getFirstPublished());
        Assert.assertEquals(ds.getLanguage(),dsRet.getLanguage());
        Assert.assertEquals(ds.getLastPublished(),dsRet.getLastPublished());
        Assert.assertEquals(ds.getNotes(),dsRet.getNotes());
        Assert.assertEquals(ds.getRecordsPublished(),ds.getRecordsPublished());
        Assert.assertEquals(ds.getRecordsSubmitted(),ds.getRecordsSubmitted());
        Assert.assertEquals(ds.getReplacedBy(),ds.getReplacedBy());
        Assert.assertEquals(ds.getSource(),dsRet.getSource());
        Assert.assertEquals(ds.getSubject(),dsRet.getSubject());
        Assert.assertEquals(ds.getSubmittedAt(), dsRet.getSubmittedAt());
        Assert.assertEquals(ds.getUpdated(), dsRet.getUpdated());
        Assert.assertEquals(ds.getWorkflowStatus(), dsRet.getWorkflowStatus());
    }


    @Test
    public void testUpdateRetrieveDataset(){
        dsDao.createDatasetForOrganization(org,ds);
        ds.setWorkflowStatus(WorkflowStatus.CREATED);
        dsDao.update(ds);
        Dataset dsRet = dsDao.getByName(ds.getName());
        Assert.assertEquals(ds.getName(),dsRet.getName());
        Assert.assertEquals(ds.getAssignedToLdapId(),dsRet.getAssignedToLdapId());
        Assert.assertEquals(ds.getCountry(),dsRet.getCountry());
        Assert.assertEquals(ds.getCreated(),dsRet.getCreated());
        Assert.assertEquals(ds.getCreatedByLdapId(),dsRet.getCreatedByLdapId());
        Assert.assertEquals(ds.getDataProvider(),dsRet.getDataProvider());
        Assert.assertEquals(ds.getDQA(),dsRet.getDQA());
        Assert.assertEquals(ds.getDescription(),ds.getDescription());
        Assert.assertEquals(ds.getFirstPublished(),dsRet.getFirstPublished());
        Assert.assertEquals(ds.getLanguage(),dsRet.getLanguage());
        Assert.assertEquals(ds.getLastPublished(),dsRet.getLastPublished());
        Assert.assertEquals(ds.getNotes(),dsRet.getNotes());
        Assert.assertEquals(ds.getRecordsPublished(),ds.getRecordsPublished());
        Assert.assertEquals(ds.getRecordsSubmitted(),ds.getRecordsSubmitted());
        Assert.assertEquals(ds.getReplacedBy(),ds.getReplacedBy());
        Assert.assertEquals(ds.getSource(),dsRet.getSource());
        Assert.assertEquals(ds.getSubject(),dsRet.getSubject());
        Assert.assertEquals(ds.getSubmittedAt(), dsRet.getSubmittedAt());
        Assert.assertEquals(ds.getUpdated(), dsRet.getUpdated());
        Assert.assertEquals(ds.getWorkflowStatus(), dsRet.getWorkflowStatus());
    }
    @Test
    public void testDeleteDataset(){
        dsDao.createDatasetForOrganization(org,ds);
        Dataset dsRet = dsDao.getByName(ds.getName());
        dsDao.delete(dsRet);
        dsRet = dsDao.getByName(ds.getName());
        Assert.assertNull(dsRet);
    }
    @AfterClass
    public static void destroy(){
        embeddedLocalhostMongo.stop();
    }
}
