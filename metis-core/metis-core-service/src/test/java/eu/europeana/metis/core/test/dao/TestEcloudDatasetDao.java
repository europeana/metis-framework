package eu.europeana.metis.core.test.dao;

import static org.mockito.Mockito.when;

import eu.europeana.cloud.common.model.DataSet;
import eu.europeana.metis.core.dao.ecloud.EcloudDatasetDao;
import java.net.URISyntaxException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-02-21
 */
public class TestEcloudDatasetDao {
  private static EcloudDatasetDao ecloudDatasetDao;
  private static DataSet dataSet;
  private static DataSet errorDataSet;
  private static DataSet datasetUpdated;
  private static String returnedUri = "returnedUriOfDatasetLocation";

  @BeforeClass
  public static void beforeTests() throws URISyntaxException {
    ecloudDatasetDao = Mockito.mock(EcloudDatasetDao.class);

    dataSet = new DataSet();
    dataSet.setId("sdataset1s");
    dataSet.setProviderId("test");
    dataSet.setDescription("test_description");

    datasetUpdated = new DataSet();
    datasetUpdated.setId(dataSet.getId());
    datasetUpdated.setProviderId(dataSet.getProviderId());
    String newDescription = "new_description";
    datasetUpdated.setDescription(newDescription);

    errorDataSet = new DataSet();
    errorDataSet.setId("serrordataset1s");

    when(ecloudDatasetDao.create(dataSet)).thenReturn(returnedUri);
    when(ecloudDatasetDao.update(datasetUpdated)).thenReturn(dataSet.getId());
    when(ecloudDatasetDao.delete(dataSet)).thenReturn(true);
    when(ecloudDatasetDao.getById(dataSet.getId())).thenReturn(dataSet);

    when(ecloudDatasetDao.create(errorDataSet)).thenReturn(null);
    when(ecloudDatasetDao.update(errorDataSet)).thenReturn(null);
    when(ecloudDatasetDao.delete(errorDataSet)).thenReturn(false);
    when(ecloudDatasetDao.getById(errorDataSet.getId())).thenReturn(null);
  }

  @Test
  public void testCreateDataset() {
    Assert.assertEquals(returnedUri, ecloudDatasetDao.create(dataSet));
    ecloudDatasetDao.delete(dataSet);
  }

  @Test
  public void testGetDatasetById() {
    ecloudDatasetDao.create(dataSet);
    DataSet retrievedDataset = ecloudDatasetDao.getById(dataSet.getId());
    Assert.assertEquals(dataSet.getId(), retrievedDataset.getId());
    Assert.assertEquals(dataSet.getProviderId(), retrievedDataset.getProviderId());
    Assert.assertEquals(dataSet.getDescription(), retrievedDataset.getDescription());
    ecloudDatasetDao.delete(dataSet);
  }

  @Test
  public void testUpdateDataset() {
    ecloudDatasetDao.create(dataSet);
    Assert.assertEquals(dataSet.getId(), ecloudDatasetDao.update(datasetUpdated));
    ecloudDatasetDao.delete(dataSet);
  }

  @Test
  public void testDeleteDataset() {
    ecloudDatasetDao.create(dataSet);
    Assert.assertEquals(true, ecloudDatasetDao.delete(dataSet));
  }

  //Errors
  @Test
  public void testCreateDatasetError() {
    Assert.assertNull(ecloudDatasetDao.create(errorDataSet));
  }

  @Test
  public void testGetDatasetByIdError() {
    ecloudDatasetDao.create(errorDataSet);
    Assert.assertNull(ecloudDatasetDao.getById(errorDataSet.getId()));
  }

  @Test
  public void testUpdateDatasetError() {
    ecloudDatasetDao.create(errorDataSet);
    Assert.assertNull(ecloudDatasetDao.update(errorDataSet));
  }

  @Test
  public void testDeleteDatasetError() {
    ecloudDatasetDao.create(errorDataSet);
    Assert.assertFalse(ecloudDatasetDao.delete(errorDataSet));
  }

}
