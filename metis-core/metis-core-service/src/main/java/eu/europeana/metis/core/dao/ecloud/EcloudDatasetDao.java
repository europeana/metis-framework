package eu.europeana.metis.core.dao.ecloud;

import eu.europeana.cloud.common.model.DataSet;
import eu.europeana.cloud.mcs.driver.DataSetServiceClient;
import eu.europeana.cloud.service.mcs.exception.MCSException;
import eu.europeana.metis.core.dao.MetisDao;
import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-02-20
 */
public class EcloudDatasetDao implements MetisDao<DataSet, String> {

  private final Logger LOGGER = LoggerFactory.getLogger(EcloudDatasetDao.class);

  @Autowired
  private DataSetServiceClient dataSetServiceClient;

  @Value("${ecloud.provider}")
  private String ecloudProvider;

  @Override
  public String create(DataSet dataSet) {
    try {
      URI datasetUri = dataSetServiceClient
          .createDataSet(ecloudProvider, dataSet.getId(), dataSet.getDescription());
      LOGGER.info("Dataset '" + dataSet.getId() + "' created with Provider '" + ecloudProvider + "' and Description '" + dataSet.getDescription() + "' in ECloud");
      return datasetUri.toString();
    } catch (MCSException ex) {
      LOGGER.error("Provider '" + ecloudProvider + "'could not create Dataset '" + dataSet.getId()
          + "'in ECloud", ex);
    }
    return null;
  }

  @Override
  public String update(DataSet dataSet) {
    try {
      dataSetServiceClient
          .updateDescriptionOfDataSet(ecloudProvider, dataSet.getId(), dataSet.getDescription());
      LOGGER.info("Dataset '" + dataSet.getId() + "' updated with Provider '" + ecloudProvider + "' and Description '" + dataSet.getDescription() + "' in ECloud");
      return dataSet.getId();
    } catch (MCSException ex) {
      LOGGER.error(
          "Provider '" + ecloudProvider + "' could not update description of Dataset '" + dataSet.getId()
              + "' in ECloud", ex);
    }
    return null;
  }

  @Override
  public DataSet getById(String dataSetId) {
    // TODO: 20-2-17 Update when ecloud has direct call to retrieve dataset by id
    List<DataSet> dataSetsForProvider = null;
    try {
      dataSetsForProvider = dataSetServiceClient
          .getDataSetsForProvider(ecloudProvider);
    } catch (MCSException ex) {
      LOGGER.error("Could not retrieve datasets of Provider '" + ecloudProvider + "' from ECloud", ex);
    }
    if(dataSetsForProvider != null) {
      for (DataSet dataSetInEcloud : dataSetsForProvider) {
        if (dataSetInEcloud.getId().equals(dataSetId))
          return dataSetInEcloud;
      }
    }
    return null;
  }

  @Override
  public boolean delete(DataSet dataSet) {
    try {
      dataSetServiceClient.deleteDataSet(ecloudProvider, dataSet.getId());
      LOGGER.info("Dataset '" + dataSet.getId() + "' deleted with Provider '" + ecloudProvider + "' from ECloud");
      return true;
    } catch (MCSException ex) {
      LOGGER.error("Provider '" + ecloudProvider + "' could not delete Dataset '" + dataSet.getId()
          + "' in ECloud", ex);
    }
    return false;
  }

  public boolean exists(String dataSetId) {
    // TODO: 20-2-17 Update when ecloud has direct call to exist or retrieve dataset by id
    List<DataSet> dataSetsForProvider = null;
    try {
      dataSetsForProvider = dataSetServiceClient
          .getDataSetsForProvider(ecloudProvider);
    } catch (MCSException ex) {
      LOGGER.error("Could not retrieve datasets of Provider '" + ecloudProvider + "' from ECloud",
          ex);
    }
    if (dataSetsForProvider != null) {
      for (DataSet dataSetInEcloud : dataSetsForProvider) {
        if (dataSetInEcloud.getId().equals(dataSetId))
          return true;
      }
    }
    return false;
  }

  public String getEcloudProvider() {
    return ecloudProvider;
  }

  public void setEcloudProvider(String ecloudProvider) {
    this.ecloudProvider = ecloudProvider;
  }
}
