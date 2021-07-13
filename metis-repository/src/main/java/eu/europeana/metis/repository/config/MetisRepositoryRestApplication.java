package eu.europeana.metis.repository.config;

import com.mongodb.client.MongoClient;
import eu.europeana.metis.mongo.connection.MongoClientProvider;
import eu.europeana.metis.mongo.connection.MongoProperties;
import eu.europeana.metis.mongo.connection.MongoProperties.ReadPreferenceValue;
import eu.europeana.metis.repository.dao.RecordDao;
import eu.europeana.metis.utils.CustomTruststoreAppender;
import eu.europeana.metis.utils.CustomTruststoreAppender.TrustStoreConfigurationException;
import java.util.Collections;
import javax.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * The web application making available the repository functionality. This provides all the
 * configuration and is the starting point for all injections and beans. It also performs the
 * required setup.
 */
@Configuration
@EnableWebMvc
@EnableSwagger2
@ComponentScan(basePackages = {"eu.europeana.metis.repository.config",
        "eu.europeana.metis.repository.rest"})
public class MetisRepositoryRestApplication implements WebMvcConfigurer, InitializingBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetisRepositoryRestApplication.class);

  private final ApplicationPropertiesHolder properties;

  private MongoClient mongoClientForEntities;

  /**
   * Constructor.
   *
   * @param properties The properties.
   */
  @Autowired
  public MetisRepositoryRestApplication(ApplicationPropertiesHolder properties) {
    this.properties = properties;
  }

  @Override
  public void afterPropertiesSet() throws TrustStoreConfigurationException {

    // Set the truststore.
    LOGGER.info("Append default truststore with custom truststore");
    if (StringUtils.isNotEmpty(properties.getTruststorePath())
            && StringUtils.isNotEmpty(properties.getTruststorePassword())) {
      CustomTruststoreAppender.appendCustomTrustoreToDefault(properties.getTruststorePath(),
              properties.getTruststorePassword());
    }

    // Create the mongo connection
    LOGGER.info("Creating Mongo connection");
    final MongoProperties<IllegalArgumentException> mongoProperties = new MongoProperties<>(
            IllegalArgumentException::new);
    mongoProperties
            .setAllProperties(properties.getMongoHosts(), new int[]{properties.getMongoPort()},
                    properties.getMongoAuthenticationDb(), properties.getMongoUsername(),
                    properties.getMongoPassword(), properties.isMongoEnableSsl(),
                    ReadPreferenceValue.PRIMARY_PREFERRED, properties.getMongoApplicationName());
    mongoClientForEntities = new MongoClientProvider<>(mongoProperties).createMongoClient();
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/swagger-ui/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
            .resourceChain(false);
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addRedirectViewController("/", "/swagger-ui/index.html");
  }

  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
            .useDefaultResponseMessages(false)
            .select()
            .apis(RequestHandlerSelectors.any())
            .paths(PathSelectors.any())
            .build()
            .apiInfo(apiInfo());
  }

  private ApiInfo apiInfo() {
    return new ApiInfo(
            "Metis Repository REST API",
            "Metis Repository REST API",
            "v1",
            "API TOS",
            new Contact("development", "europeana.eu", "development@europeana.eu"),
            "EUPL Licence v1.2",
            "https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12",
            Collections.emptyList());
  }

  @Bean
  public RecordDao getRecordDao() {
    return new RecordDao(mongoClientForEntities, properties.getMongoRecordDb());
  }

  /**
   * Closes any connections previous acquired.
   */
  @PreDestroy
  public void close() {
    if (mongoClientForEntities != null) {
      mongoClientForEntities.close();
    }
  }
}
