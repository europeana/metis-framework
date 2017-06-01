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
import eu.europeana.metis.core.common.Language;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetStatus;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.organization.Organization;
import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

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
    org = new Organization();
    org.setOrganizationId("orgId");
    org.setDatasetNames(new TreeSet<String>());
    org.setOrganizationUri("testUri");
//    org.setHarvestingMetadata(new HarvestingMetadata());
    ds = new Dataset();
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
    ReflectionTestUtils.setField(dsDao, "provider", provider);
  }


  @Test
  public void testCreateRetrieveDataset() {
    dsDao.create(ds);
    Dataset dsRet = dsDao.getDatasetByDatasetName(ds.getDatasetName());
    Assert.assertEquals(ds.getDatasetName(), dsRet.getDatasetName());
    Assert.assertEquals(ds.getAssignedToLdapId(), dsRet.getAssignedToLdapId());
    Assert.assertEquals(ds.getCountry(), dsRet.getCountry());
    Assert.assertEquals(ds.getCreatedDate(), dsRet.getCreatedDate());
    Assert.assertEquals(ds.getCreatedByLdapId(), dsRet.getCreatedByLdapId());
    Assert.assertEquals(ds.getDataProvider(), dsRet.getDataProvider());
    Assert.assertEquals(ds.getDqas(), dsRet.getDqas());
    Assert.assertEquals(ds.getDescription(), ds.getDescription());
    Assert.assertEquals(ds.getFirstPublished(), dsRet.getFirstPublished());
    Assert.assertEquals(ds.getLanguage(), dsRet.getLanguage());
    Assert.assertEquals(ds.getLastPublished(), dsRet.getLastPublished());
    Assert.assertEquals(ds.getNotes(), dsRet.getNotes());
    Assert.assertEquals(ds.getPublishedRecords(), ds.getPublishedRecords());
    Assert.assertEquals(ds.getSubmittedRecords(), ds.getSubmittedRecords());
    Assert.assertEquals(ds.getReplacedBy(), ds.getReplacedBy());
    Assert.assertEquals(ds.getSources(), dsRet.getSources());
    Assert.assertEquals(ds.getSubjects(), dsRet.getSubjects());
    Assert.assertEquals(ds.getSubmissionDate(), dsRet.getSubmissionDate());
    Assert.assertEquals(ds.getUpdatedDate(), dsRet.getUpdatedDate());
    Assert.assertEquals(ds.getDatasetStatus(), dsRet.getDatasetStatus());
  }


  @Test
  public void testUpdateRetrieveDataset() {
    dsDao.create(ds);
    ds.setDatasetStatus(DatasetStatus.CREATED);
    dsDao.update(ds);
    Dataset dsRet = dsDao.getDatasetByDatasetName(ds.getDatasetName());
    Assert.assertEquals(ds.getDatasetName(), dsRet.getDatasetName());
    Assert.assertEquals(ds.getAssignedToLdapId(), dsRet.getAssignedToLdapId());
    Assert.assertEquals(ds.getCountry(), dsRet.getCountry());
    Assert.assertEquals(ds.getCreatedDate(), dsRet.getCreatedDate());
    Assert.assertEquals(ds.getCreatedByLdapId(), dsRet.getCreatedByLdapId());
    Assert.assertEquals(ds.getDataProvider(), dsRet.getDataProvider());
    Assert.assertEquals(ds.getDqas(), dsRet.getDqas());
    Assert.assertEquals(ds.getDescription(), ds.getDescription());
    Assert.assertEquals(ds.getFirstPublished(), dsRet.getFirstPublished());
    Assert.assertEquals(ds.getLanguage(), dsRet.getLanguage());
    Assert.assertEquals(ds.getLastPublished(), dsRet.getLastPublished());
    Assert.assertEquals(ds.getNotes(), dsRet.getNotes());
    Assert.assertEquals(ds.getPublishedRecords(), ds.getPublishedRecords());
    Assert.assertEquals(ds.getSubmittedRecords(), ds.getSubmittedRecords());
    Assert.assertEquals(ds.getReplacedBy(), ds.getReplacedBy());
    Assert.assertEquals(ds.getSources(), dsRet.getSources());
    Assert.assertEquals(ds.getSubjects(), dsRet.getSubjects());
    Assert.assertEquals(ds.getSubmissionDate(), dsRet.getSubmissionDate());
    Assert.assertEquals(ds.getUpdatedDate(), dsRet.getUpdatedDate());
    Assert.assertEquals(ds.getDatasetStatus(), dsRet.getDatasetStatus());
  }

  @Test
  public void testDeleteDataset() {
    dsDao.create(ds);
    Dataset dsRet = dsDao.getDatasetByDatasetName(ds.getDatasetName());
    dsDao.delete(dsRet);
    dsRet = dsDao.getDatasetByDatasetName(ds.getDatasetName());
    Assert.assertNull(dsRet);
  }

  @AfterClass
  public static void destroy() {
    embeddedLocalhostMongo.stop();
  }
}
