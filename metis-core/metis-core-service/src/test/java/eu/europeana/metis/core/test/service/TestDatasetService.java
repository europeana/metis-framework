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
package eu.europeana.metis.core.test.service;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertSame;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.europeana.cloud.common.model.DataSet;
import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.core.common.Language;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.OrganizationDao;
import eu.europeana.metis.core.dao.ScheduledUserWorkflowDao;
import eu.europeana.metis.core.dao.UserWorkflowExecutionDao;
import eu.europeana.metis.core.dao.ecloud.EcloudDatasetDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetStatus;
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.DatasetAlreadyExistsException;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoOrganizationFoundException;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mongodb.morphia.Datastore;

public class TestDatasetService {

  private OrganizationDao organizationDao;
  private OrganizationService organizationService;
  private DatasetDao datasetDao;
  private UserWorkflowExecutionDao userWorkflowExecutionDao;
  private ScheduledUserWorkflowDao scheduledUserWorkflowDao;
  private EcloudDatasetDao ecloudDatasetDao;
  private DatasetService datasetService;
  private Datastore datastore;
  private Organization org;
  private Dataset ds;

  @Before
  public void prepare() {
    organizationService = Mockito.mock(OrganizationService.class);

    organizationDao = Mockito.mock(OrganizationDao.class);
    datasetDao = Mockito.mock(DatasetDao.class);
    userWorkflowExecutionDao = Mockito.mock(UserWorkflowExecutionDao.class);
    scheduledUserWorkflowDao = Mockito.mock(ScheduledUserWorkflowDao.class);
    ecloudDatasetDao = Mockito.mock(EcloudDatasetDao.class);

    datasetService = new DatasetService(datasetDao, organizationDao, userWorkflowExecutionDao, scheduledUserWorkflowDao);
    datastore = Mockito.mock(Datastore.class);

    org = createOrganization();
//        org.setHarvestingMetadata(new HarvestingMetadata());
    ds = createDataset();
  }

  private Dataset createDataset() {
    Dataset d = new Dataset();

    d.setOrganizationId("orgId");
    d.setAccepted(true);
    d.setAssignedToLdapId("Lemmy");
    d.setCountry(Country.ALBANIA);
    d.setCreatedDate(new Date(1000));
    d.setCreatedByLdapId("Lemmy");
    d.setDataProvider("prov");
    d.setDeaSigned(true);
    d.setDescription("Test description");
    List<String> DQA = new ArrayList<>();
    DQA.add("test DQA");
    d.setDqas(DQA);
    d.setFirstPublished(new Date(1000));
    d.setHarvestedAt(new Date(1000));
    d.setLanguage(Language.AR);
    d.setLastPublished(new Date(1000));
//        ds.setHarvestingMetadata(new OaipmhHarvestingDatasetMetadata());
    d.setDatasetName("testName");
    d.setNotes("test Notes");
    d.setPublishedRecords(100);
    d.setSubmittedRecords(199);
    d.setReplacedBy("replacedBY");
    List<String> sources = new ArrayList<>();
    sources.add("testSource");
    d.setSources(sources);
    List<String> subjects = new ArrayList<>();
    subjects.add("testSubject");
    d.setSubjects(subjects);
    d.setSubmissionDate(new Date(1000));
    d.setUpdatedDate(new Date(1000));
    d.setDatasetStatus(DatasetStatus.ACCEPTANCE);
    return d;
  }

  private Organization createOrganization() {
    Organization o = new Organization();
    o.setOrganizationId("orgId");
    o.setDatasetNames(new TreeSet<String>());
    o.setOrganizationUri("testUri");
    return o;
  }

  @Test
  public void testCreateDataset() {
    Dataset ds = createDataset();
    ds.setDatasetName("datasetName");
    ds.setOrganizationId("myOrgId");
    ds.setCreatedDate(null);
    ds.setUpdatedDate(null);
    ds.setFirstPublished(null);
    ds.setLastPublished(null);
    ds.setHarvestedAt(null);
    ds.setSubmissionDate(null);
    ds.setSubmittedRecords(0);
    ds.setPublishedRecords(0);

    when(ecloudDatasetDao.getEcloudProvider()).thenReturn("ecloudProviderId");

    datasetService.createDataset(ds, "myOrgId");

    ArgumentCaptor<DataSet> ecloudDataSetCapture = ArgumentCaptor.forClass(DataSet.class);
    ArgumentCaptor<Dataset> datasetArgumentCaptor = ArgumentCaptor.forClass(Dataset.class);

//    verify(ecloudDatasetDao, times(1)).
//        create(ecloudDataSetCapture.capture());
    verify(datasetDao, times(1)).
        create(datasetArgumentCaptor.capture());
    verify(organizationDao, times(1)).
        updateOrganizationDatasetNamesList("myOrgId", "datasetName");
  }

  @Test
  public void testDelete()
      throws NoDatasetFoundException, NoOrganizationFoundException, BadContentException {
    when(datasetDao.getDatasetByDatasetName(Mockito.any(String.class))).thenReturn(ds);
//        when(datasetDao.deleteDatasetByDatasetName(Mockito.any(String.class))).thenReturn(true);
//        when(organizationService.getOrganizationByOrganizationId(Mockito.any(String.class))).thenReturn(null);
//        Mockito.doNothing().when(organizationService).removeOrganizationDatasetNameFromList(Mockito.any(String.class), Mockito.any(String.class));
//
    ds.setOrganizationId("myOrgId");
    ds.setDatasetName("myDatasetId");
    when(datasetDao.getDatasetByDatasetName(any(String.class))).thenReturn(ds);
    when(userWorkflowExecutionDao.existsAndNotCompleted(any(String.class))).thenReturn(null);
    when(datasetDao.deleteDatasetByDatasetName(any(String.class))).thenReturn(true);
    when(organizationService.getOrganizationByOrganizationId(Mockito.any(String.class)))
        .thenReturn(null);
    when(userWorkflowExecutionDao.deleteAllByDatasetName(any(String.class))).thenReturn(true);
    when(scheduledUserWorkflowDao.deleteAllByDatasetName(any(String.class))).thenReturn(true);

    datasetService.deleteDatasetByDatasetName("myDatasetId");

    verify(datasetDao, times(1)).getDatasetByDatasetName(any(String.class));
    verify(datasetDao, times(1)).deleteDatasetByDatasetName(ds.getDatasetName());
    verify(organizationDao, times(1)).getOrganizationByOrganizationId("myOrgId");
    verify(organizationDao, times(1))
        .removeOrganizationDatasetNameFromList("myOrgId", "myDatasetId");
    verify(userWorkflowExecutionDao, times(1)).deleteAllByDatasetName(any(String.class));
    verify(scheduledUserWorkflowDao, times(1)).deleteAllByDatasetName(any(String.class));
//    verify(ecloudDatasetDao, times(1)).delete(any(DataSet.class));
  }

  @Test
  public void testUpdate() {
    ds.setEcloudDatasetId("myEcloudID");
    datasetService.updateDataset(ds);

    ArgumentCaptor<DataSet> ecloudDataSetCapture = ArgumentCaptor.forClass(DataSet.class);

//    verify(ecloudDatasetDao, times(1)).update(ecloudDataSetCapture.capture());
    verify(datasetDao, times(1)).update(ds);

//    assertEquals("myEcloudID", ecloudDataSetCapture.getValue().getId());
  }

  @Test
  public void testRetrieve() throws NoDatasetFoundException {
    when(datasetDao.getDatasetByDatasetName("name")).thenReturn(ds);

    Dataset retDataset = datasetService.getDatasetByDatasetName("name");
    Assert.assertEquals(ds, retDataset);
  }


  @Test(expected = NoDatasetFoundException.class)
  public void testRetrieve_NotFound() throws NoDatasetFoundException {
    when(datasetDao.getDatasetByDatasetName("name")).thenReturn(null);

    datasetService.getDatasetByDatasetName("name");
  }


  @Test
  public void testcreateDatasetForOrganization()
      throws DatasetAlreadyExistsException, BadContentException, NoOrganizationFoundException {
    Dataset ds = createDataset();
    ds.setDatasetName("datasetName");
    ds.setOrganizationId("myOrgId");
    ds.setCreatedDate(null);
    ds.setUpdatedDate(null);
    ds.setFirstPublished(null);
    ds.setLastPublished(null);
    ds.setHarvestedAt(null);
    ds.setSubmissionDate(null);
    ds.setSubmittedRecords(0);
    ds.setPublishedRecords(0);

    when(datasetDao.getDatasetByDatasetName("datasetName")).thenReturn(null);
    Organization org = new Organization();

    when(organizationDao.getOrganizationByOrganizationId("myOrgId")).
        thenReturn(org);

    datasetService.createDatasetForOrganization(ds, "myOrgId");

    ArgumentCaptor<DataSet> ecloudDataSetCapture = ArgumentCaptor.forClass(DataSet.class);
    ArgumentCaptor<Dataset> datasetArgumentCaptor = ArgumentCaptor.forClass(Dataset.class);

//    verify(ecloudDatasetDao, times(1)).
//        create(ecloudDataSetCapture.capture());
    verify(datasetDao, times(1)).
        create(datasetArgumentCaptor.capture());

    verify(organizationDao, times(1)).
        updateOrganizationDatasetNamesList("myOrgId", "datasetName");

    assertEquals("myOrgId", datasetArgumentCaptor.getValue().getOrganizationId());
    assertNotNull(datasetArgumentCaptor.getValue().getCreatedDate());
  }

  @Test
  public void testUpdateDatasetByDatasetName()
      throws BadContentException, NoDatasetFoundException {

    Dataset ds = createDataset();
    ds.setEcloudDatasetId("myEcloudid");
    ds.setDatasetName(null);
    ds.setCreatedDate(null);
    ds.setUpdatedDate(null);

    when(datasetDao.getDatasetByDatasetName("dataSetName")).thenReturn(ds);
//    when(ecloudDatasetDao.getEcloudProvider()).thenReturn("myEcloudprovider");
    when(userWorkflowExecutionDao.existsAndNotCompleted("dataSetName")).thenReturn(null);

    datasetService.updateDatasetByDatasetName(ds, "dataSetName");

    //Beware : DataSet and Dataset
//    ArgumentCaptor<DataSet> ecloudDataSetCapture = ArgumentCaptor.forClass(DataSet.class);
    ArgumentCaptor<Dataset> datasetCapture = ArgumentCaptor.forClass(Dataset.class);

//    verify(ecloudDatasetDao, times(1)).update(ecloudDataSetCapture.capture());
    verify(datasetDao, times(1)).update(datasetCapture.capture());

//    assertEquals(ds.getDescription(), ecloudDataSetCapture.getValue().getDescription());
//
//    assertEquals("myEcloudprovider", ecloudDataSetCapture.getValue().getProviderId());
//    assertEquals("myEcloudid", ecloudDataSetCapture.getValue().getId());

    assertEquals(ds.getDescription(), datasetCapture.getValue().getDescription());
    assertEquals("myEcloudid", datasetCapture.getValue().getEcloudDatasetId());
    assertSame(ds, datasetCapture.getValue());
  }

  @Test
  public void testUpdateDatasetName() throws NoDatasetFoundException, BadContentException {

    Dataset ds = createDataset();
    ds.setDatasetName(null);
    ds.setCreatedDate(null);
    ds.setUpdatedDate(null);
    ds.setOrganizationId("myOrgId");

    when(datasetDao.getDatasetByDatasetName("datasetName")).thenReturn(ds);

    datasetService.updateDatasetName("datasetName", "newDatasetName");

    verify(datasetDao, times(1)).
        updateDatasetName("datasetName", "newDatasetName");
    verify(organizationDao, times(1)).
        removeOrganizationDatasetNameFromList("myOrgId", "datasetName");
    verify(organizationDao, times(1)).
        updateOrganizationDatasetNamesList("myOrgId", "newDatasetName");

  }

  @Test
  public void testGetAllDatasetsByDataProvider() {
    List<Dataset> list = new ArrayList<>();
    String provider = "myProvider";
    String nextPage = "myNextPage";
    when(datasetDao.getAllDatasetsByDataProvider(provider, nextPage)).thenReturn(list);

    List<Dataset> retList = datasetService.getAllDatasetsByDataProvider(provider, nextPage);

    assertSame(list, retList);
  }

  @Test
  public void testGetAllDatasetsByOrganizationId() {

    List<Dataset> list = new ArrayList<>();
    String orgId = "myOrgId";
    String nextPage = "myNextPage";
    when(datasetDao.getAllDatasetsByOrganizationId(orgId, nextPage)).thenReturn(list);

    List<Dataset> retList = datasetService.getAllDatasetsByOrganizationId(orgId, nextPage);

    assertSame(list, retList);
  }

  @Test
  public void testExistsDatasetByDatasetName() {
    when(datasetDao.existsDatasetByDatasetName("test")).thenReturn(true);
    assertTrue(datasetDao.existsDatasetByDatasetName("test"));
  }

  @Test
  public void testGetDatasetsPerRequestLimit() {
    when(datasetDao.getDatasetsPerRequest()).thenReturn(5);
    assertEquals(5, datasetService.getDatasetsPerRequestLimit());
  }

  @Test(expected = BadContentException.class)
  public void test_createPreconditionCheck_datasetNameEmpty_throwsException()
      throws DatasetAlreadyExistsException, BadContentException, NoOrganizationFoundException {

    Dataset ds = prepareCreatePreConditions();
    ds.setDatasetName(null);
    datasetService.createDatasetForOrganization(ds, "myOrgId");
  }


  @Test(expected = BadContentException.class)
  public void test_createPreconditionCheck_updatedDateNotEmpty_throwsException()
      throws DatasetAlreadyExistsException, BadContentException, NoOrganizationFoundException {

    Dataset ds = prepareCreatePreConditions();
    ds.setUpdatedDate(new Date());
    datasetService.createDatasetForOrganization(ds, "myOrgId");
  }

  @Test(expected = BadContentException.class)
  public void test_createPreconditionCheck_firstPublishedNotEmpty_throwsException()
      throws DatasetAlreadyExistsException, BadContentException, NoOrganizationFoundException {

    Dataset ds = prepareCreatePreConditions();
    ds.setFirstPublished(new Date());
    datasetService.createDatasetForOrganization(ds, "myOrgId");
  }

  @Test(expected = BadContentException.class)
  public void test_createPreconditionCheck_lastPublishedNotEmpty_throwsException()
      throws DatasetAlreadyExistsException, BadContentException, NoOrganizationFoundException {

    Dataset ds = prepareCreatePreConditions();
    ds.setLastPublished(new Date());
    datasetService.createDatasetForOrganization(ds, "myOrgId");
  }

  @Test(expected = BadContentException.class)
  public void test_createPreconditionCheck_harvistedAtNotEmpty_throwsException()
      throws DatasetAlreadyExistsException, BadContentException, NoOrganizationFoundException {

    Dataset ds = prepareCreatePreConditions();
    ds.setHarvestedAt(new Date());
    datasetService.createDatasetForOrganization(ds, "myOrgId");
  }

  @Test(expected = BadContentException.class)
  public void test_createPreconditionCheck_submissionDateNotEmpty_throwsException()
      throws DatasetAlreadyExistsException, BadContentException, NoOrganizationFoundException {

    Dataset ds = prepareCreatePreConditions();
    ds.setSubmissionDate(new Date());
    datasetService.createDatasetForOrganization(ds, "myOrgId");
  }

  @Test(expected = BadContentException.class)
  public void test_createPreconditionCheck_createdDateNotEmpty_throwsException()
      throws DatasetAlreadyExistsException, BadContentException, NoOrganizationFoundException {

    Dataset ds = prepareCreatePreConditions();
    ds.setCreatedDate(new Date());
    datasetService.createDatasetForOrganization(ds, "myOrgId");
  }

  @Test(expected = BadContentException.class)
  public void test_createPreconditionCheck_submittedRecordsNotZero_throwsException()
      throws DatasetAlreadyExistsException, BadContentException, NoOrganizationFoundException {

    Dataset ds = prepareCreatePreConditions();
    ds.setSubmittedRecords(10);
    datasetService.createDatasetForOrganization(ds, "myOrgId");
  }

  @Test(expected = BadContentException.class)
  public void test_createPreconditionCheck_publishedeRcordsNotZero_throwsException()
      throws DatasetAlreadyExistsException, BadContentException, NoOrganizationFoundException {

    Dataset ds = prepareCreatePreConditions();
    ds.setPublishedRecords(10);
    datasetService.createDatasetForOrganization(ds, "myOrgId");
  }


  @Test(expected = BadContentException.class)
  public void test_createPreconditionCheck_organisationIdNotMatching_throwsException()
      throws DatasetAlreadyExistsException, BadContentException, NoOrganizationFoundException {

    Dataset ds = prepareCreatePreConditions();
    ds.setOrganizationId("bla");
    datasetService.createDatasetForOrganization(ds, "myOrgId");
  }

  @Test(expected = NoOrganizationFoundException.class)
  public void test_createPreconditionCheck_organisationDoesNotExist_throwsException()
      throws DatasetAlreadyExistsException, BadContentException, NoOrganizationFoundException {

    Dataset ds = prepareCreatePreConditions();
    when(organizationDao.getOrganizationByOrganizationId("myOrgId")).thenReturn(null);
    datasetService.createDatasetForOrganization(ds, "myOrgId");
  }

  @Test(expected = DatasetAlreadyExistsException.class)
  public void test_createPreconditionCheck_dataSetAlreadyExist_throwsException()
      throws DatasetAlreadyExistsException, BadContentException, NoOrganizationFoundException {

    Dataset ds = prepareCreatePreConditions();
    when(datasetDao.existsDatasetByDatasetName("datasetName")).thenReturn(true);
    datasetService.createDatasetForOrganization(ds, "myOrgId");
  }


  @Test(expected = BadContentException.class)
  public void test_updatePreconditionCheck_datasetNameDoesNotMatch_throwsException()
      throws BadContentException, NoDatasetFoundException {
    Dataset ds = prepareCreatePreConditions();
    ds.setDatasetName("test");

    datasetService.updateDatasetByDatasetName(ds, "datasetName");
  }


  @Test(expected = BadContentException.class)
  public void test_updatePreconditionCheck_createdDateNotEmpty_throwsException()
      throws BadContentException, NoDatasetFoundException {
    Dataset ds = prepareCreatePreConditions();
    ds.setCreatedDate(new Date());

    datasetService.updateDatasetByDatasetName(ds, "datasetName");
  }

  @Test(expected = BadContentException.class)
  public void test_updatePreconditionCheck_updatedDateNotEmpty_throwsException()
      throws BadContentException, NoDatasetFoundException {
    Dataset ds = prepareCreatePreConditions();
    ds.setUpdatedDate(new Date());

    datasetService.updateDatasetByDatasetName(ds, "datasetName");
  }

  private Dataset prepareCreatePreConditions() {
    Dataset ds = createDataset();
    ds.setDatasetName("datasetName");
    ds.setOrganizationId("myOrgId");
    ds.setCreatedDate(null);
    ds.setUpdatedDate(null);
    ds.setFirstPublished(null);
    ds.setLastPublished(null);
    ds.setHarvestedAt(null);
    ds.setSubmissionDate(null);
    ds.setSubmittedRecords(0);
    ds.setPublishedRecords(0);

    when(datasetDao.getDatasetByDatasetName("datasetName")).thenReturn(null);
    Organization org = new Organization();

    when(organizationDao.getOrganizationByOrganizationId("myOrgId")).
        thenReturn(org);
    return ds;
  }
}

