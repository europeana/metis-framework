package eu.europeana.metis.core.rest.config;

import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.client.uis.rest.UISClient;
import eu.europeana.cloud.mcs.driver.DataSetServiceClient;
import eu.europeana.cloud.mcs.driver.FileServiceClient;
import eu.europeana.cloud.mcs.driver.RecordServiceClient;
import eu.europeana.metis.core.rest.config.properties.MetisCoreConfigurationProperties;
import javax.annotation.PreDestroy;
import metis.common.config.properties.ecloud.EcloudConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@EnableConfigurationProperties({
    MetisCoreConfigurationProperties.class, EcloudConfigurationProperties.class})
@ComponentScan(basePackages = {"eu.europeana.metis.core.rest.controller"})
public class ECloudConfig implements WebMvcConfigurer {

  private DataSetServiceClient dataSetServiceClient;
  private RecordServiceClient recordServiceClient;
  private FileServiceClient fileServiceClient;
  private DpsClient dpsClient;
  private UISClient uisClient;


  @Bean
  DataSetServiceClient dataSetServiceClient(
      MetisCoreConfigurationProperties metisCoreConfigurationProperties,
      EcloudConfigurationProperties ecloudConfigurationProperties) {
    dataSetServiceClient = new DataSetServiceClient(
        ecloudConfigurationProperties.getBaseUrl(),
        ecloudConfigurationProperties.getUsername(),
        ecloudConfigurationProperties.getPassword(),
        metisCoreConfigurationProperties.getDpsConnectTimeoutInMilliseconds(),
        metisCoreConfigurationProperties.getDpsReadTimeoutInMilliseconds());
    return dataSetServiceClient;
  }

  @Bean
  RecordServiceClient recordServiceClient(
      MetisCoreConfigurationProperties metisCoreConfigurationProperties,
      EcloudConfigurationProperties ecloudConfigurationProperties) {
    recordServiceClient = new RecordServiceClient(
        ecloudConfigurationProperties.getBaseUrl(),
        ecloudConfigurationProperties.getUsername(),
        ecloudConfigurationProperties.getPassword(),
        metisCoreConfigurationProperties.getDpsConnectTimeoutInMilliseconds(),
        metisCoreConfigurationProperties.getDpsReadTimeoutInMilliseconds());
    return recordServiceClient;
  }

  @Bean
  FileServiceClient fileServiceClient(
      MetisCoreConfigurationProperties metisCoreConfigurationProperties,
      EcloudConfigurationProperties ecloudConfigurationProperties) {
    fileServiceClient = new FileServiceClient(
        ecloudConfigurationProperties.getBaseUrl(),
        ecloudConfigurationProperties.getUsername(),
        ecloudConfigurationProperties.getPassword(),
        metisCoreConfigurationProperties.getDpsConnectTimeoutInMilliseconds(),
        metisCoreConfigurationProperties.getDpsReadTimeoutInMilliseconds());
    return fileServiceClient;
  }

  @Bean
  DpsClient dpsClient(
      MetisCoreConfigurationProperties metisCoreConfigurationProperties,
      EcloudConfigurationProperties ecloudConfigurationProperties) {
    dpsClient = new DpsClient(
        ecloudConfigurationProperties.getDpsBaseUrl(),
        ecloudConfigurationProperties.getUsername(),
        ecloudConfigurationProperties.getPassword(),
        metisCoreConfigurationProperties.getDpsConnectTimeoutInMilliseconds(),
        metisCoreConfigurationProperties.getDpsReadTimeoutInMilliseconds());
    return dpsClient;
  }

  @Bean
  UISClient uisClient(
      MetisCoreConfigurationProperties metisCoreConfigurationProperties,
      EcloudConfigurationProperties ecloudConfigurationProperties) {
    uisClient = new UISClient(
        ecloudConfigurationProperties.getBaseUrl(),
        ecloudConfigurationProperties.getUsername(),
        ecloudConfigurationProperties.getPassword(),
        metisCoreConfigurationProperties.getDpsConnectTimeoutInMilliseconds(),
        metisCoreConfigurationProperties.getDpsReadTimeoutInMilliseconds());
    return uisClient;
  }

  /**
   * Close all open clients.
   */
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
