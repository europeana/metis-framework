package eu.europeana.metis.core.rest.config;

import eu.europeana.cloud.client.uis.rest.CloudException;
import eu.europeana.cloud.client.uis.rest.UISClient;
import eu.europeana.cloud.common.exceptions.ProviderDoesNotExistException;
import eu.europeana.cloud.common.model.DataProviderProperties;
import eu.europeana.cloud.mcs.driver.DataSetServiceClient;
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
  @Value("${ecloud.baseUrl}")
  private String ecloudBaseUrl;
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
}
