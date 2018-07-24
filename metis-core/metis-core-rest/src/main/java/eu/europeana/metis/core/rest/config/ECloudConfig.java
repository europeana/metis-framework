package eu.europeana.metis.core.rest.config;

import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.mcs.driver.DataSetServiceClient;
import eu.europeana.cloud.mcs.driver.FileServiceClient;
import eu.europeana.cloud.mcs.driver.RecordServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-11-22
 */
@Configuration
@ComponentScan(basePackages = {"eu.europeana.metis.core.rest"})
public class ECloudConfig extends WebMvcConfigurerAdapter {

  private final ConfigurationPropertiesHolder propertiesHolder;

  @Autowired
  public ECloudConfig(ConfigurationPropertiesHolder propertiesHolder) {
    this.propertiesHolder = propertiesHolder;
  }

  @Bean
  DataSetServiceClient dataSetServiceClient() {
    return new DataSetServiceClient(propertiesHolder.getEcloudBaseUrl(), propertiesHolder.getEcloudUsername(),
        propertiesHolder.getEcloudPassword());
  }

  @Bean
  RecordServiceClient recordServiceClient() {
    return new RecordServiceClient(propertiesHolder.getEcloudBaseUrl(), propertiesHolder.getEcloudUsername(),
        propertiesHolder.getEcloudPassword());
  }

  @Bean
  FileServiceClient fileServiceClient() {
    return new FileServiceClient(propertiesHolder.getEcloudBaseUrl(), propertiesHolder.getEcloudUsername(),
        propertiesHolder.getEcloudPassword());
  }

  @Bean
  DpsClient dpsClient() {
    return new DpsClient(propertiesHolder.getEcloudDpsBaseUrl(),
        propertiesHolder.getEcloudUsername(), propertiesHolder.getEcloudPassword(),
        propertiesHolder.getDpsConnectTimeoutInMillisecs(),
        propertiesHolder.getDpsReadTimeoutInMillisecs());
  }
}
