package eu.europeana.metis.core.rest.config;

import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.client.uis.rest.UISClient;
import eu.europeana.cloud.mcs.driver.DataSetServiceClient;
import eu.europeana.cloud.mcs.driver.FileServiceClient;
import eu.europeana.cloud.mcs.driver.RecordServiceClient;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ECloud configuration class.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-11-22
 */
@Configuration
@ComponentScan(basePackages = {"eu.europeana.metis.core.rest"})
public class ECloudConfig implements WebMvcConfigurer {

  private final ConfigurationPropertiesHolder propertiesHolder;
  private DataSetServiceClient dataSetServiceClient;
  private RecordServiceClient recordServiceClient;
  private FileServiceClient fileServiceClient;
  private DpsClient dpsClient;
  private UISClient uisClient;

  /**
   * Constructor.
   *
   * @param propertiesHolder Object containing the configuration properties.
   */
  @Autowired
  public ECloudConfig(ConfigurationPropertiesHolder propertiesHolder) {
    this.propertiesHolder = propertiesHolder;
  }

  @Bean
  DataSetServiceClient dataSetServiceClient() {
    dataSetServiceClient = new DataSetServiceClient(propertiesHolder.getEcloudBaseUrl(), null,
        propertiesHolder.getEcloudUsername(), propertiesHolder.getEcloudPassword(),
        propertiesHolder.getDpsConnectTimeoutInMillisecs(),
        propertiesHolder.getDpsReadTimeoutInMillisecs());
    return dataSetServiceClient;
  }

  @Bean
  RecordServiceClient recordServiceClient() {
    recordServiceClient = new RecordServiceClient(propertiesHolder.getEcloudBaseUrl(), null,
        propertiesHolder.getEcloudUsername(), propertiesHolder.getEcloudPassword(),
        propertiesHolder.getDpsConnectTimeoutInMillisecs(),
        propertiesHolder.getDpsReadTimeoutInMillisecs());
    return recordServiceClient;
  }

  @Bean
  FileServiceClient fileServiceClient() {
    fileServiceClient = new FileServiceClient(propertiesHolder.getEcloudBaseUrl(), null,
        propertiesHolder.getEcloudUsername(), propertiesHolder.getEcloudPassword(),
        propertiesHolder.getDpsConnectTimeoutInMillisecs(),
        propertiesHolder.getDpsReadTimeoutInMillisecs());
    return fileServiceClient;
  }

  @Bean
  DpsClient dpsClient() {
    dpsClient = new DpsClient(propertiesHolder.getEcloudDpsBaseUrl(),
        propertiesHolder.getEcloudUsername(), propertiesHolder.getEcloudPassword(),
        propertiesHolder.getDpsConnectTimeoutInMillisecs(),
        propertiesHolder.getDpsReadTimeoutInMillisecs());
    return dpsClient;
  }

  @Bean
  UISClient uisClient() {
    uisClient = new UISClient(propertiesHolder.getEcloudDpsBaseUrl(),
        propertiesHolder.getEcloudUsername(), propertiesHolder.getEcloudPassword(),
        propertiesHolder.getDpsConnectTimeoutInMillisecs(),
        propertiesHolder.getDpsReadTimeoutInMillisecs());
    return uisClient;
  }

  @PreDestroy
  public void close() {
    if (dataSetServiceClient != null) {
      dataSetServiceClient.close();
    }
    if (recordServiceClient != null) {
      recordServiceClient.close();
    }
    if (fileServiceClient != null) {
      fileServiceClient.close();
    }
    if (dpsClient != null) {
      dpsClient.close();
    }
    if (uisClient != null) {
      uisClient.close();
    }
  }
}
