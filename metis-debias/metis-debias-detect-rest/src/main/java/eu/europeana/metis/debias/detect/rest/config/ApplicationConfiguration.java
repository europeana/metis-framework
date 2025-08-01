package eu.europeana.metis.debias.detect.rest.config;

import eu.europeana.metis.debias.detect.client.DeBiasClient;
import eu.europeana.metis.debias.detect.service.BiasDetectService;
import eu.europeana.metis.utils.CustomTruststoreAppender;
import eu.europeana.metis.utils.CustomTruststoreAppender.TrustStoreConfigurationException;
import eu.europeana.metis.utils.apm.ElasticAPMConfiguration;
import java.lang.invoke.MethodHandles;
import eu.europeana.metis.common.config.properties.TruststoreConfigurationProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * The type Application configuration.
 */
@Configuration
@EnableConfigurationProperties({ElasticAPMConfiguration.class, TruststoreConfigurationProperties.class})
@ComponentScan(basePackages = {
    "eu.europeana.metis.debias.detect.rest"})
public class ApplicationConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Value("${debias.simple.client.detect-url}")
  private String detectUrl;

  @Value("${debias.simple.client.connect-timeout}")
  private int connectTimeOut;

  @Value("${debias.simple.client.request-timeout}")
  private int requestTimeOut;

  /**
   * Instantiates a new Application configuration.
   *
   * @param truststoreConfigurationProperties the truststore configuration properties
   * @throws TrustStoreConfigurationException the trust store configuration exception
   */
  @Autowired
  public ApplicationConfiguration(TruststoreConfigurationProperties truststoreConfigurationProperties)
      throws TrustStoreConfigurationException {
    ApplicationConfiguration.initializeTruststore(truststoreConfigurationProperties);
  }

  /**
   * Detect service detect service.
   *
   * @return the detect service
   */
  @Bean
  public BiasDetectService detectService() {
    return new DeBiasClient(this.detectUrl, this.connectTimeOut, this.requestTimeOut);
  }

  /**
   * Initialize truststore.
   *
   * @param truststoreConfigurationProperties the truststore configuration properties
   * @throws TrustStoreConfigurationException the trust store configuration exception
   */
  static void initializeTruststore(TruststoreConfigurationProperties truststoreConfigurationProperties)
      throws TrustStoreConfigurationException {
    if (StringUtils.isNotEmpty(truststoreConfigurationProperties.getPath()) && StringUtils
        .isNotEmpty(truststoreConfigurationProperties.getPassword())) {
      CustomTruststoreAppender
          .appendCustomTruststoreToDefault(truststoreConfigurationProperties.getPath(),
              truststoreConfigurationProperties.getPassword());
      LOGGER.info("Custom truststore appended to default truststore");
    }
  }
}
