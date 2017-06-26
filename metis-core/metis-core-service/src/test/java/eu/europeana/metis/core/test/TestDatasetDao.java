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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.core.common.Language;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetStatus;
import eu.europeana.metis.core.dataset.OaipmhHarvestingMetadata;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.organization.Organization;
import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import org.bson.types.ObjectId;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

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

    ServerAddress address = new ServerAddress(mongoHost, mongoPort);
    MongoClient mongoClient = new MongoClient(address);
    MorphiaDatastoreProvider provider = new MorphiaDatastoreProvider(mongoClient, "test");

    dsDao = new DatasetDao(provider);
    dsDao.setDatasetsPerRequest(5);

    org = createOrganization();
    ds = createDataset("testName");
  }

  @AfterClass
  public static void destroy() {
    embeddedLocalhostMongo.stop();
  }

  private static Organization createOrganization() {
    Organization org = new Organization();
    org.setOrganizationId("orgId");
    org.setDatasetNames(new TreeSet<String>());
    org.setOrganizationUri("testUri");
    return org;
  }

  private static Dataset createDataset(String datasetName) {
    Dataset ds = new Dataset();
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
    ds.setHarvestingMetadata(new OaipmhHarvestingMetadata());
    ds.setDatasetName(datasetName);
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


  @Test
  public void testCreateRetrieveDataset() {
    dsDao.create(ds);
    Dataset dsRet = dsDao.getDatasetByDatasetName(ds.getDatasetName());
    assertEquals(ds.getDatasetName(), dsRet.getDatasetName());
    assertEquals(ds.getAssignedToLdapId(), dsRet.getAssignedToLdapId());
    assertEquals(ds.getCountry(), dsRet.getCountry());
    assertEquals(ds.getCreatedDate(), dsRet.getCreatedDate());
    assertEquals(ds.getCreatedByLdapId(), dsRet.getCreatedByLdapId());
    assertEquals(ds.getDataProvider(), dsRet.getDataProvider());
    assertEquals(ds.getDqas(), dsRet.getDqas());
    assertEquals(ds.getDescription(), ds.getDescription());
    assertEquals(ds.getFirstPublished(), dsRet.getFirstPublished());
    assertEquals(ds.getLanguage(), dsRet.getLanguage());
    assertEquals(ds.getLastPublished(), dsRet.getLastPublished());
    assertEquals(ds.getNotes(), dsRet.getNotes());
    assertEquals(ds.getPublishedRecords(), ds.getPublishedRecords());
    assertEquals(ds.getSubmittedRecords(), ds.getSubmittedRecords());
    assertEquals(ds.getReplacedBy(), ds.getReplacedBy());
    assertEquals(ds.getSources(), dsRet.getSources());
    assertEquals(ds.getSubjects(), dsRet.getSubjects());
    assertEquals(ds.getSubmissionDate(), dsRet.getSubmissionDate());
    assertEquals(ds.getUpdatedDate(), dsRet.getUpdatedDate());
    assertEquals(ds.getDatasetStatus(), dsRet.getDatasetStatus());
  }


  @Test
  public void testUpdateRetrieveDataset() {
    dsDao.create(ds);
    ds.setDatasetStatus(DatasetStatus.CREATED);
    dsDao.update(ds);
    Dataset dsRet = dsDao.getDatasetByDatasetName(ds.getDatasetName());
    assertEquals(ds.getDatasetName(), dsRet.getDatasetName());
    assertEquals(ds.getAssignedToLdapId(), dsRet.getAssignedToLdapId());
    assertEquals(ds.getCountry(), dsRet.getCountry());
    assertEquals(ds.getCreatedDate(), dsRet.getCreatedDate());
    assertEquals(ds.getCreatedByLdapId(), dsRet.getCreatedByLdapId());
    assertEquals(ds.getDataProvider(), dsRet.getDataProvider());
    assertEquals(ds.getDqas(), dsRet.getDqas());
    assertEquals(ds.getDescription(), ds.getDescription());
    assertEquals(ds.getFirstPublished(), dsRet.getFirstPublished());
    assertEquals(ds.getLanguage(), dsRet.getLanguage());
    assertEquals(ds.getLastPublished(), dsRet.getLastPublished());
    assertEquals(ds.getNotes(), dsRet.getNotes());
    assertEquals(ds.getPublishedRecords(), ds.getPublishedRecords());
    assertEquals(ds.getSubmittedRecords(), ds.getSubmittedRecords());
    assertEquals(ds.getReplacedBy(), ds.getReplacedBy());
    assertEquals(ds.getSources(), dsRet.getSources());
    assertEquals(ds.getSubjects(), dsRet.getSubjects());
    assertEquals(ds.getSubmissionDate(), dsRet.getSubmissionDate());
    assertEquals(ds.getUpdatedDate(), dsRet.getUpdatedDate());
    assertEquals(ds.getDatasetStatus(), dsRet.getDatasetStatus());
  }

  @Test
  public void testDeleteDataset() {
    dsDao.create(ds);
    Dataset dsRet = dsDao.getDatasetByDatasetName(ds.getDatasetName());
    dsDao.delete(dsRet);
    dsRet = dsDao.getDatasetByDatasetName(ds.getDatasetName());
    Assert.assertNull(dsRet);
  }

  @Test
  public void testUpdateDatasetName() {
    String key = dsDao.create(ds);
    String nameBefore = ds.getDatasetName();

    Dataset storedBefore = dsDao.getById(key);

    assertEquals(ds.getDatasetName(), storedBefore.getDatasetName());
    dsDao.updateDatasetName(nameBefore, "someNewName");

    Dataset stored = dsDao.getById(key);
    assertEquals("someNewName", stored.getDatasetName());
  }

  @Test
  public void testDeleteDatasetByDatasetName() {
    String key = dsDao.create(ds);

    Dataset storedBefore = dsDao.getById(key);
    assertTrue(dsDao.deleteDatasetByDatasetName(storedBefore.getDatasetName()));
    Dataset deleted = dsDao.getById(key);

    assertNull(deleted);
    assertFalse(dsDao.deleteDatasetByDatasetName(storedBefore.getDatasetName()));
  }

  @Test
  public void testExistsDatasetByDatasetName() {
    String key = dsDao.create(ds);

    assertTrue(dsDao.existsDatasetByDatasetName(ds.getDatasetName()));

    dsDao.deleteDatasetByDatasetName(ds.getDatasetName());

    assertFalse(dsDao.existsDatasetByDatasetName(ds.getDatasetName()));
  }

  @Test
  public void testGetAllDatasetByOrganization() {
    Dataset ds1 = createDataset("dataset1");
    //add some required fields (indexed)
    ds1.setDataProvider("myProvider");
    ds1.setId(new ObjectId("1f2f2f2f2f2f2f2f2f2f2f2f"));
    ds1.setEcloudDatasetId("id1");

    dsDao.create(ds1);

    Dataset ds2 = createDataset("dataset2");
    //add some required fields (indexed)
    ds2.setId(new ObjectId("2f2f2f2f2f2f2f2f2f2f2f2f"));
    ds2.setDataProvider("myProvider");
    ds2.setEcloudDatasetId("id2");

    dsDao.create(ds2);

    Dataset ds3 = createDataset("dataset3");
    //add some required fields (indexed)
    ds3.setId(new ObjectId("3f2f2f2f2f2f2f2f2f2f2f2f"));
    ds3.setDataProvider("otherProvider");
    ds3.setEcloudDatasetId("id3");

    dsDao.create(ds3);

    List<Dataset> datasets = dsDao.getAllDatasetsByDataProvider("myProvider", null);

    assertEquals(2, datasets.size());

    assertTrue(containsDatasetWithName(datasets, "dataset1"));
    assertTrue(containsDatasetWithName(datasets, "dataset2"));
  }

  private Boolean containsDatasetWithName(List<Dataset> datasets, String datasetName) {
    Boolean found = false;
    for(Dataset d: datasets) {
      if(d.getDatasetName().equals(datasetName)) {
        found=true;
      }
    }
    return found;
  }


}
