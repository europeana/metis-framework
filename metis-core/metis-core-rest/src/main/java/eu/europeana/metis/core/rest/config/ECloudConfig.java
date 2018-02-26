package eu.europeana.metis.core.rest.config;

import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.client.uis.rest.CloudException;
import eu.europeana.cloud.client.uis.rest.UISClient;
import eu.europeana.cloud.common.exceptions.ProviderDoesNotExistException;
import eu.europeana.cloud.common.model.DataProviderProperties;
import eu.europeana.cloud.mcs.driver.DataSetServiceClient;
import eu.europeana.cloud.mcs.driver.FileServiceClient;
import eu.europeana.cloud.mcs.driver.RecordServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-11-22
 */
@Configuration
@ComponentScan(basePackages = {"eu.europeana.metis.core.rest"})
@PropertySource({"classpath:metis.properties"})
public class ECloudConfig extends WebMvcConfigurerAdapter implements InitializingBean {
  private static final Logger LOGGER = LoggerFactory.getLogger(ECloudConfig.class);

  @Value("${ecloud.baseUrl}")
  private String ecloudBaseUrl;
  @Value("${ecloud.dps.baseUrl}")
  private String ecloudDpsBaseUrl;
  @Value("${ecloud.provider}")
  private String ecloudProvider;
  @Value("${ecloud.username}")
  private String ecloudUsername;
  @Value("${ecloud.password}")
  private String ecloudPassword;

  @Override
  public void afterPropertiesSet() throws Exception {
    UISClient uisClient = new UISClient(ecloudBaseUrl, ecloudUsername, ecloudPassword);
    try {
      uisClient.getDataProvider(ecloudProvider);
    } catch (CloudException e) {
      if (e.getCause() instanceof ProviderDoesNotExistException) {
        LOGGER.warn("Ecloud provider does not exist in Ecloud", e);
        DataProviderProperties dataProviderProperties = new DataProviderProperties();
        dataProviderProperties.setOrganisationName("Eurpeana Foundation");
        uisClient.createProvider(ecloudProvider, dataProviderProperties);
      }
    }
  }

  @Bean
  DataSetServiceClient dataSetServiceClient() {
    return new DataSetServiceClient(ecloudBaseUrl, ecloudUsername, ecloudPassword);
  }

  @Bean
  RecordServiceClient recordServiceClient() {
    return new RecordServiceClient(ecloudBaseUrl, ecloudUsername, ecloudPassword);
  }

  @Bean
  FileServiceClient fileServiceClient() {
    return new FileServiceClient(ecloudBaseUrl, ecloudUsername, ecloudPassword);
  }

  @Bean
  DpsClient dpsClient() {
    return new DpsClient(ecloudDpsBaseUrl, ecloudUsername, ecloudPassword);
  }
}
