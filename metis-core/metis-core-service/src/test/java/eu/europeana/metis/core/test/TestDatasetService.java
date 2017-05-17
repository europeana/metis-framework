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

import eu.europeana.cloud.common.model.DataSet;
import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.core.common.HarvestingMetadata;
import eu.europeana.metis.core.common.Language;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.OrganizationDao;
import eu.europeana.metis.core.dao.ecloud.EcloudDatasetDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.OAIDatasetMetadata;
import eu.europeana.metis.core.dataset.WorkflowStatus;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.organization.Organization;
import eu.europeana.metis.core.service.DatasetService;
import eu.europeana.metis.core.service.OrganizationService;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mongodb.morphia.Datastore;
import org.springframework.test.util.ReflectionTestUtils;

public class TestDatasetService {
    private MorphiaDatastoreProvider morphiaDatastoreProvider;
    private OrganizationDao organizationDao;
    private OrganizationService organizationService;
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
        organizationService = Mockito.mock(OrganizationService.class);
        datasetDao = Mockito.mock(DatasetDao.class);
        ecloudDatasetDao = Mockito.mock(EcloudDatasetDao.class);
        ReflectionTestUtils.setField(organizationDao,"provider", morphiaDatastoreProvider);
        ReflectionTestUtils.setField(datasetDao,"provider", morphiaDatastoreProvider);
        service = new DatasetService(datasetDao, ecloudDatasetDao, organizationService);
        datastore = Mockito.mock(Datastore.class);
        ReflectionTestUtils.setField(service,"organizationService",organizationService);
        ReflectionTestUtils.setField(service,"datasetDao",datasetDao);
        ReflectionTestUtils.setField(service,"ecloudDatasetDao",ecloudDatasetDao);
        org = new Organization();
        org.setOrganizationId("orgId");
        org.setDatasetNames(new TreeSet<String>());
        org.setOrganizationUri("testUri");
        org.setHarvestingMetadata(new HarvestingMetadata());
        ds = new Dataset();
        ds.setOrganizationId("orgId");
        ds.setAccepted(true);
        ds.setAssignedToLdapId("Lemmy");
        ds.setCountry(Country.ALBANIA);
        ds.setCreatedDate(new Date(1000));
        ds.setCreatedByLdapId("Lemmy");
        ds.setDataProvider("prov");
        ds.setDeaSigned(true);
        ds.setDescription("Test description");
        List<String> DQA = new ArrayList<>();
        DQA.add("test DQA");
        ds.setDqas(DQA);
        ds.setFirstPublished(new Date(1000));
        ds.setHarvestedAt(new Date(1000));
        ds.setLanguage(Language.AR);
        ds.setLastPublished(new Date(1000));
        ds.setMetadata(new OAIDatasetMetadata());
        ds.setDatasetName("testName");
        ds.setNotes("test Notes");
        ds.setPublishedRecords(100);
        ds.setSubmittedRecords(199);
        ds.setReplacedBy("replacedBY");
        List<String> sources = new ArrayList<>();
        sources.add("testSource");
        ds.setSources(sources);
        List<String> subjects = new ArrayList<>();
        subjects.add("testSubject");
        ds.setSubjects(subjects);
        ds.setSubmissionDate(new Date(1000));
        ds.setUpdatedDate(new Date(1000));
        ds.setWorkflowStatus(WorkflowStatus.ACCEPTANCE);
    }
    @Test
    public void testCreate(){
        Mockito.when(morphiaDatastoreProvider.getDatastore()).thenReturn(datastore);
        Mockito.when(datasetDao.create(Mockito.any(Dataset.class))).thenReturn(null);
        Mockito.when(organizationDao.updateOrganizationDatasetNamesList(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(null);
        Mockito.when(ecloudDatasetDao.create(Mockito.any(DataSet.class))).thenReturn(null);
        service.createDataset(ds, org.getOrganizationId());
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

        Mockito.when(datasetDao.getDatasetByDatasetName("name")).thenReturn(ds);

        try {
            Dataset retDataset = service.getDatasetByName("name");
            Assert.assertEquals(ds, retDataset);
        } catch (NoDatasetFoundException e){
            e.printStackTrace();
        }
    }
}
