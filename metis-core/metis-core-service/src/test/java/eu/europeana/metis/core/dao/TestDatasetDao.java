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
package eu.europeana.metis.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetStatus;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.organization.Organization;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;
import java.io.IOException;
import java.util.List;
import java.util.TreeSet;
import org.bson.types.ObjectId;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestDatasetDao {

  private static DatasetDao datasetDao;
  private static Organization organization;
  private static Dataset dataset;
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

    datasetDao = new DatasetDao(provider);
    datasetDao.setDatasetsPerRequest(5);

    organization = createOrganization();
    dataset = TestObjectFactory.createDataset("testName");
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


  @Test
  public void testCreateRetrieveDataset() {
    datasetDao.create(dataset);
    Dataset dsRet = datasetDao.getDatasetByDatasetName(dataset.getDatasetName());
    assertEquals(dataset.getDatasetName(), dsRet.getDatasetName());
    assertEquals(dataset.getCountry(), dsRet.getCountry());
    assertEquals(dataset.getCreatedDate(), dsRet.getCreatedDate());
    assertEquals(dataset.getDataProvider(), dsRet.getDataProvider());
    assertEquals(dataset.getDqas(), dsRet.getDqas());
    assertEquals(dataset.getDescription(), dataset.getDescription());
    assertEquals(dataset.getFirstPublished(), dsRet.getFirstPublished());
    assertEquals(dataset.getLanguage(), dsRet.getLanguage());
    assertEquals(dataset.getLastPublished(), dsRet.getLastPublished());
    assertEquals(dataset.getNotes(), dsRet.getNotes());
    assertEquals(dataset.getPublishedRecords(), dataset.getPublishedRecords());
    assertEquals(dataset.getSubmittedRecords(), dataset.getSubmittedRecords());
    assertEquals(dataset.getReplacedBy(), dataset.getReplacedBy());
    assertEquals(dataset.getSources(), dsRet.getSources());
    assertEquals(dataset.getSubjects(), dsRet.getSubjects());
    assertEquals(dataset.getSubmissionDate(), dsRet.getSubmissionDate());
    assertEquals(dataset.getUpdatedDate(), dsRet.getUpdatedDate());
    assertEquals(dataset.getDatasetStatus(), dsRet.getDatasetStatus());
  }


  @Test
  public void testUpdateRetrieveDataset() {
    datasetDao.create(dataset);
    dataset.setDatasetStatus(DatasetStatus.CREATED);
    datasetDao.update(dataset);
    Dataset dsRet = datasetDao.getDatasetByDatasetName(dataset.getDatasetName());
    assertEquals(dataset.getDatasetName(), dsRet.getDatasetName());
    assertEquals(dataset.getCountry(), dsRet.getCountry());
    assertEquals(dataset.getCreatedDate(), dsRet.getCreatedDate());
    assertEquals(dataset.getDataProvider(), dsRet.getDataProvider());
    assertEquals(dataset.getDqas(), dsRet.getDqas());
    assertEquals(dataset.getDescription(), dataset.getDescription());
    assertEquals(dataset.getFirstPublished(), dsRet.getFirstPublished());
    assertEquals(dataset.getLanguage(), dsRet.getLanguage());
    assertEquals(dataset.getLastPublished(), dsRet.getLastPublished());
    assertEquals(dataset.getNotes(), dsRet.getNotes());
    assertEquals(dataset.getPublishedRecords(), dataset.getPublishedRecords());
    assertEquals(dataset.getSubmittedRecords(), dataset.getSubmittedRecords());
    assertEquals(dataset.getReplacedBy(), dataset.getReplacedBy());
    assertEquals(dataset.getSources(), dsRet.getSources());
    assertEquals(dataset.getSubjects(), dsRet.getSubjects());
    assertEquals(dataset.getSubmissionDate(), dsRet.getSubmissionDate());
    assertEquals(dataset.getUpdatedDate(), dsRet.getUpdatedDate());
    assertEquals(dataset.getDatasetStatus(), dsRet.getDatasetStatus());
  }

  @Test
  public void testDeleteDataset() {
    datasetDao.create(dataset);
    Dataset dsRet = datasetDao.getDatasetByDatasetName(dataset.getDatasetName());
    datasetDao.delete(dsRet);
    dsRet = datasetDao.getDatasetByDatasetName(dataset.getDatasetName());
    Assert.assertNull(dsRet);
  }

  @Test
  public void testUpdateDatasetName() {
    String key = datasetDao.create(dataset);
    String nameBefore = dataset.getDatasetName();

    Dataset storedBefore = datasetDao.getById(key);

    assertEquals(dataset.getDatasetName(), storedBefore.getDatasetName());
    datasetDao.updateDatasetName(nameBefore, "someNewName");

    Dataset stored = datasetDao.getById(key);
    assertEquals("someNewName", stored.getDatasetName());
  }

  @Test
  public void testDeleteDatasetByDatasetName() {
    String key = datasetDao.create(dataset);

    Dataset storedBefore = datasetDao.getById(key);
    assertTrue(datasetDao.deleteDatasetByDatasetName(storedBefore.getDatasetName()));
    Dataset deleted = datasetDao.getById(key);

    assertNull(deleted);
    assertFalse(datasetDao.deleteDatasetByDatasetName(storedBefore.getDatasetName()));
  }

  @Test
  public void testExistsDatasetByDatasetName() {
    String key = datasetDao.create(dataset);

    assertTrue(datasetDao.existsDatasetByDatasetName(dataset.getDatasetName()));

    datasetDao.deleteDatasetByDatasetName(dataset.getDatasetName());

    assertFalse(datasetDao.existsDatasetByDatasetName(dataset.getDatasetName()));
  }

  @Test
  public void testGetAllDatasetByOrganization() {
    Dataset ds1 = TestObjectFactory.createDataset("dataset1");
    //add some required fields (indexed)
    ds1.setDataProvider("myProvider");
    ds1.setId(new ObjectId("1f2f2f2f2f2f2f2f2f2f2f2f"));
    ds1.setEcloudDatasetId("id1");

    datasetDao.create(ds1);

    Dataset ds2 = TestObjectFactory.createDataset("dataset2");
    //add some required fields (indexed)
    ds2.setId(new ObjectId("2f2f2f2f2f2f2f2f2f2f2f2f"));
    ds2.setDataProvider("myProvider");
    ds2.setEcloudDatasetId("id2");

    datasetDao.create(ds2);

    Dataset ds3 = TestObjectFactory.createDataset("dataset3");
    //add some required fields (indexed)
    ds3.setId(new ObjectId("3f2f2f2f2f2f2f2f2f2f2f2f"));
    ds3.setDataProvider("otherProvider");
    ds3.setEcloudDatasetId("id3");

    datasetDao.create(ds3);

    List<Dataset> datasets = datasetDao.getAllDatasetsByDataProvider("myProvider", null);

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
