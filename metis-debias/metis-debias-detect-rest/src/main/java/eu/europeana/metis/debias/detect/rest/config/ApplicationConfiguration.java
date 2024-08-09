package eu.europeana.metis.debias.detect.rest.config;

import eu.europeana.metis.utils.apm.ElasticAPMConfiguration;
import metis.common.config.properties.TruststoreConfigurationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
    ElasticAPMConfiguration.class, TruststoreConfigurationProperties.class})
public class ApplicationConfiguration {

  @Value("debias.simple.client.detect.url")
  private String debiasAPIUrl;

  @Value("debias.simple.client.connect.timeout")
  private String connectTimeOut;

  @Value("debias.simple.client.read.timeout")
  private String readTimeOut;
}
