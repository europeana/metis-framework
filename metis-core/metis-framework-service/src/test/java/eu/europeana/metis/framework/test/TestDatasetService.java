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

import eu.europeana.cloud.common.model.DataSet;
import eu.europeana.metis.framework.common.Country;
import eu.europeana.metis.framework.common.HarvestingMetadata;
import eu.europeana.metis.framework.common.Language;
import eu.europeana.metis.framework.dao.DatasetDao;
import eu.europeana.metis.framework.dao.OrganizationDao;
import eu.europeana.metis.framework.dao.ecloud.EcloudDatasetDao;
import eu.europeana.metis.framework.dataset.Dataset;
import eu.europeana.metis.framework.dataset.OAIDatasetMetadata;
import eu.europeana.metis.framework.dataset.WorkflowStatus;
import eu.europeana.metis.framework.exceptions.NoDatasetFoundException;
import eu.europeana.metis.framework.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.framework.organization.Organization;
import eu.europeana.metis.framework.service.DatasetService;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mongodb.morphia.Datastore;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Created by ymamakis on 2/19/16.
 */
public class TestDatasetService {
    private MorphiaDatastoreProvider morphiaDatastoreProvider;
    private OrganizationDao organizationDao;
    private DatasetDao datasetDao;
    private EcloudDatasetDao ecloudDatasetDao;
    private DatasetService service;
    private Datastore datastore;
    private Organization org;
    private Dataset ds;

    @Before
    public void prepare(){
        morphiaDatastoreProvider = Mockito.mock(MorphiaDatastoreProvider.class);
        organizationDao = Mockito.mock(OrganizationDao.class);
        datasetDao = Mockito.mock(DatasetDao.class);
        ecloudDatasetDao = Mockito.mock(EcloudDatasetDao.class);
        ReflectionTestUtils.setField(organizationDao,"provider", morphiaDatastoreProvider);
        ReflectionTestUtils.setField(datasetDao,"provider", morphiaDatastoreProvider);
        service = new DatasetService();
        datastore = Mockito.mock(Datastore.class);
        ReflectionTestUtils.setField(service,"orgDao",organizationDao);
        ReflectionTestUtils.setField(service,"dsDao",datasetDao);
        ReflectionTestUtils.setField(service,"ecloudDatasetDao",ecloudDatasetDao);
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
    }
    @Test
    public void testCreate(){

        Mockito.when(morphiaDatastoreProvider.getDatastore()).thenReturn(datastore);

        Mockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when(datasetDao).createDatasetForOrganization(org,ds);
        Mockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when(organizationDao).update(org);
        Mockito.when(ecloudDatasetDao.create(Mockito.any(DataSet.class))).thenReturn(null);
        service.createDataset(org,ds);
    }

    @Test
    public  void testDelete(){
        Mockito.when(morphiaDatastoreProvider.getDatastore()).thenReturn(datastore);

        Mockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when(datasetDao).delete(ds);
        Mockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when(organizationDao).update(org);
        Mockito.when(ecloudDatasetDao.delete(Mockito.any(DataSet.class))).thenReturn(true);
        service.deleteDataset(org,ds);
    }

    @Test
    public  void testUpdate(){
        Mockito.when(morphiaDatastoreProvider.getDatastore()).thenReturn(datastore);

        Mockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when(datasetDao).update(ds);
        Mockito.when(ecloudDatasetDao.update(Mockito.any(DataSet.class))).thenReturn(null);
        service.updateDataset(ds);
    }

    @Test
    public  void testRetrieve(){
        Mockito.when(morphiaDatastoreProvider.getDatastore()).thenReturn(datastore);

        Mockito.when(datasetDao.getByName("name")).thenReturn(ds);

        try {
            Dataset retDataset = service.getByName("name");
            Assert.assertEquals(ds, retDataset);
        } catch (NoDatasetFoundException e){
            e.printStackTrace();
        }
    }
}
