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

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.core.common.HarvestingMetadata;
import eu.europeana.metis.core.common.Language;
import eu.europeana.metis.core.common.OrganizationRole;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.OrganizationDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.OAIDatasetMetadata;
import eu.europeana.metis.core.dataset.WorkflowStatus;
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
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class TestOrganizationDao {

  private static Organization org;
  private static Dataset ds;
  private static OrganizationDao orgDao;
  private static DatasetDao dsDao;
  private static EmbeddedLocalhostMongo embeddedLocalhostMongo;

  @BeforeClass
  public static void prepare() throws IOException {
    embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
    embeddedLocalhostMongo.start();
    String mongoHost = embeddedLocalhostMongo.getMongoHost();
    int mongoPort = embeddedLocalhostMongo.getMongoPort();
    ServerAddress address = new ServerAddress(mongoHost, mongoPort);
    MongoClient mongoClient = new MongoClient(address);
    MorphiaDatastoreProvider provider = new MorphiaDatastoreProvider(mongoClient, "test");
    orgDao = new OrganizationDao(provider);
    orgDao.setOrganizationsPerRequest(5);
    ReflectionTestUtils.setField(orgDao, "provider", provider);

    org = new Organization();
    org.setOrganizationId("orgId");
    org.setDatasetNames(new TreeSet<String>());
    org.setOrganizationUri("testUri");
    org.setHarvestingMetadata(new HarvestingMetadata());
    org.setCountry(Country.ALBANIA);
    ds = new Dataset();
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

    dsDao = new DatasetDao(provider);
    dsDao.setDatasetsPerRequest(5);
    ReflectionTestUtils.setField(dsDao, "provider", provider);

  }

  @Test
  public void testCreateRetrieveOrg() {
    dsDao.create(ds);
    Set<String> datasets = new TreeSet<>();
    datasets.add(ds.getDatasetName());
    org.setDatasetNames(datasets);
    orgDao.create(org);
    Organization retOrg = orgDao.getOrganizationByOrganizationId(org.getOrganizationId());
    Assert.assertEquals(org.getOrganizationId(), retOrg.getOrganizationId());
    Assert.assertEquals(org.getOrganizationUri(), retOrg.getOrganizationUri());
    Assert.assertEquals(org.getDatasetNames().size(), retOrg.getDatasetNames().size());
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

  @AfterClass
  public static void destroy() {
    embeddedLocalhostMongo.stop();
  }
}
