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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.core.common.Language;
import eu.europeana.metis.core.common.OrganizationRole;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.OrganizationDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetStatus;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.organization.Organization;
import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongodb.morphia.Datastore;

public class TestOrganizationDao {

  private static Organization org;
  private static Dataset ds;
  private static OrganizationDao orgDao;
  private static DatasetDao dsDao;
  private static EmbeddedLocalhostMongo embeddedLocalhostMongo;
  private static MorphiaDatastoreProvider provider;

  @BeforeClass
  public static void prepare() throws IOException {
    embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
    embeddedLocalhostMongo.start();
    String mongoHost = embeddedLocalhostMongo.getMongoHost();
    int mongoPort = embeddedLocalhostMongo.getMongoPort();
    ServerAddress address = new ServerAddress(mongoHost, mongoPort);
    MongoClient mongoClient = new MongoClient(address);
    provider = new MorphiaDatastoreProvider(mongoClient, "test");

    orgDao = new OrganizationDao(provider);
    orgDao.setOrganizationsPerRequest(5);

    dsDao = new DatasetDao(provider);
    dsDao.setDatasetsPerRequest(5);
  }

  @AfterClass
  public static void destroy() {
    embeddedLocalhostMongo.stop();
  }

  @Before
  public void setup() {
    org = createOrganization("orgId");
    ds = createDataset();
  }

  @After
  public void CleanUp() {
    Datastore datastore = provider.getDatastore();

    datastore.delete(datastore.createQuery(Organization.class));
    datastore.delete(datastore.createQuery(Dataset.class));
  }

  private static Dataset createDataset() {
    Dataset ds = new Dataset();
    ds.setOrganizationId(org.getOrganizationId());
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
//    ds.setHarvestingMetadata(new OaipmhHarvestingDatasetMetadata());
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
    ds.setDatasetStatus(DatasetStatus.ACCEPTANCE);
    return ds;
  }



  private static Organization createOrganization(String orgId) {
    Organization org = new Organization();
    org.setOrganizationId(orgId);
    org.setDatasetNames(new TreeSet<String>());
    org.setOrganizationUri("testUri");
    org.setCountry(Country.ALBANIA);
    return org;
  }

  @Test
  public void testCreateRetrieveOrg() {
    dsDao.create(ds);
    Set<String> datasets = new TreeSet<>();
    datasets.add(ds.getDatasetName());
    org.setDatasetNames(datasets);
    orgDao.create(org);
    Organization retOrg = orgDao.getOrganizationByOrganizationId(org.getOrganizationId());
    assertEquals(org.getOrganizationId(), retOrg.getOrganizationId());
    assertEquals(org.getOrganizationUri(), retOrg.getOrganizationUri());
    assertEquals(org.getDatasetNames().size(), retOrg.getDatasetNames().size());
  }


  @Test
  public void testDeleteOrganization() {
    dsDao.create(ds);
    Set<String> datasets = new TreeSet<>();
    datasets.add(ds.getDatasetName());
    org.setDatasetNames(datasets);
    orgDao.create(org);
    orgDao.delete(org);
    Assert.assertNull(orgDao.getOrganizationByOrganizationId(org.getOrganizationId()));
  }

  @Test
  public void testDatasets() {
    dsDao.create(ds);
    Set<String> datasets = new TreeSet<>();
    datasets.add(ds.getDatasetName());
    org.setDatasetNames(datasets);
    orgDao.create(org);

    List<Dataset> allDatasetsByOrganizationId = dsDao
        .getAllDatasetsByOrganizationId(org.getOrganizationId(), null);
    Assert.assertTrue(allDatasetsByOrganizationId.size() == 1);
  }

  @Test
  public void testGetAll() {
    dsDao.create(ds);
    Set<String> datasets = new TreeSet<>();
    datasets.add(ds.getDatasetName());
    org.setDatasetNames(datasets);
    orgDao.create(org);

    List<Organization> getAll = orgDao.getAllOrganizations(null);

    Assert.assertTrue(getAll.size() == 1);
  }

  @Test
  public void testGetAllByCountry() {
    dsDao.create(ds);
    Set<String> datasets = new TreeSet<>();
    datasets.add(ds.getDatasetName());
    org.setDatasetNames(datasets);
    orgDao.create(org);
    List<Organization> getAll = orgDao.getAllOrganizationsByCountry(Country.ALBANIA, null);
    Assert.assertTrue(getAll.size() == 1);
  }

  @Test
  public void testUpdate() {
    dsDao.create(ds);
    Set<String> datasets = new TreeSet<>();
    datasets.add(ds.getDatasetName());
    org.setDatasetNames(datasets);
    orgDao.create(org);

    org.setOrganizationUri("testNew");
    org.setName("name");
    org.setModified(new Date());
    org.setCreated(new Date());
    List<OrganizationRole> organizationRoles = new ArrayList<>();
    org.setOrganizationRoles(organizationRoles);
    org.setAcronym("acronym");
    orgDao.update(org);
    Organization organization = orgDao.getOrganizationByOrganizationId(org.getOrganizationId());

    Assert.assertTrue(StringUtils.equals(organization.getOrganizationUri(), "testNew"));
  }

  @Test
  public void testDeleteByOrganizationId(){
    Organization org =  createOrganization("12345");

    String key = orgDao.create(org);

    Organization retrieved = orgDao.getById(key);
    assertNotNull(retrieved);

    orgDao.deleteByOrganizationId("12345");

    Organization retrievedAfter = orgDao.getById(key);
    assertNull(retrievedAfter);
  }

  @Test
  public void testExistsOrganizationByOrganizationId(){

    String orgId = "44444";
    Organization org =  createOrganization(orgId);

    String key = orgDao.create(org);

    assertTrue(orgDao.existsOrganizationByOrganizationId(orgId));
    orgDao.deleteByOrganizationId(orgId);
    assertFalse(orgDao.existsOrganizationByOrganizationId(orgId));
  }

  @Test
  public void testGetOrganizationOptInIIIFByOrganizationId() {
    String orgId = "55555";
    Organization org =  createOrganization(orgId);
    org.setOptInIIIF(true);
    Organization orgRet1 = orgDao.getOrganizationOptInIIIFByOrganizationId("55555");
    assertNull(orgRet1);
    String key = orgDao.create(org);

    Organization orgRet2 = orgDao.getOrganizationOptInIIIFByOrganizationId("55555");
    assertNotNull(orgRet2);
    assertTrue(orgRet2.isOptInIIIF());

  }

  @Test
  public void testGetAllOrganizationsByOrganizationRole() {
    String org1Id = "666661";
    Organization org1 =  createOrganization(org1Id);
    List<OrganizationRole> roles1 = new ArrayList<>();
    roles1.add(OrganizationRole.CONSULTANT);
    org1.setOrganizationRoles(roles1);
    String org1Key = orgDao.create(org1);

    String org2Id = "666662";
    Organization org2 =  createOrganization(org2Id);
    List<OrganizationRole> roles2 = new ArrayList<>();
    roles2.add(OrganizationRole.CONSULTANT);
    roles2.add(OrganizationRole.CONTENT_PROVIDER);
    org2.setOrganizationRoles(roles2);
    String org2Key = orgDao.create(org2);

    String org3Id = "666663";
    Organization org3 =  createOrganization(org3Id);
    List<OrganizationRole> roles3 = new ArrayList<>();
    roles3.add(OrganizationRole.CONTENT_PROVIDER);
    org3.setOrganizationRoles(roles3);
    String org3Key = orgDao.create(org3);

    List<OrganizationRole> queryRoles = new ArrayList<>();
    queryRoles.add(OrganizationRole.CONSULTANT);
    List<Organization> result = orgDao.getAllOrganizationsByOrganizationRole(queryRoles, null);

    assertEquals(2, result.size());
    assertTrue(allOrganizationsContainsAtLeastOnOfTheRoles(result, queryRoles));
  }

  private boolean allOrganizationsContainsAtLeastOnOfTheRoles(List<Organization> result, List<OrganizationRole> queryRoles) {
    boolean contains = true;
    for (Organization o: result) {
      contains = contains || organizationContainsAtLeastOnOfTheRoles(o, queryRoles);
    }
    return contains;
  }

  private boolean organizationContainsAtLeastOnOfTheRoles( Organization o, List<OrganizationRole> queryRoles)
  {
    for (OrganizationRole role: queryRoles) {
      if (o.getOrganizationRoles().contains(role)) {
        return true;
      }
    }
    return false;
  }


  @Test
  public void testUpdateOrganizationDatasetNamesList() {
    String orgId = "55555";
    Organization org =  createOrganization(orgId);
    assertTrue(org.getDatasetNames().isEmpty());

    String key = orgDao.create(org);

    Organization orgRet = orgDao.getOrganizationByOrganizationId("55555");
    assertNull(orgRet.getDatasetNames());

    orgDao.updateOrganizationDatasetNamesList("55555", "datasetName1");
    orgDao.updateOrganizationDatasetNamesList("55555", "datasetName2");
    Organization orgRetAgain = orgDao.getOrganizationByOrganizationId("55555");


    assertEquals(2, orgRetAgain.getDatasetNames().size());
    assertTrue(orgRetAgain.getDatasetNames().contains("datasetName1"));
    assertTrue(orgRetAgain.getDatasetNames().contains("datasetName2"));
  }


  @Test
  public void testRemoveOrganizationDatasetNameFromList() {

    String orgId = "55555";
    Organization org =  createOrganization(orgId);
    org.getDatasetNames().add("datasetName1");
    org.getDatasetNames().add("datasetName2");

    assertEquals(2, org.getDatasetNames().size());
    String key = orgDao.create(org);

    Organization orgRet = orgDao.getOrganizationByOrganizationId("55555");
    assertEquals(2, orgRet.getDatasetNames().size());

    orgDao.removeOrganizationDatasetNameFromList("55555", "datasetName1");
    Organization orgRetAgain = orgDao.getOrganizationByOrganizationId("55555");
    assertEquals(1, orgRetAgain.getDatasetNames().size());
    assertTrue(org.getDatasetNames().contains("datasetName2"));

    orgDao.removeOrganizationDatasetNameFromList("55555", "datasetName2");
    Organization orgRetLast = orgDao.getOrganizationByOrganizationId("55555");
    assertTrue(orgRetLast.getDatasetNames().isEmpty());
  }



}
