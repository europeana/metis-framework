/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.metis.dereference.rest.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import eu.europeana.enrichment.rest.client.EnrichmentDriver;
import eu.europeana.metis.RedisProvider;
import eu.europeana.metis.dereference.service.MongoDereferenceService;
import eu.europeana.metis.dereference.service.MongoDereferencingManagementService;
import eu.europeana.metis.dereference.service.dao.CacheDao;
import eu.europeana.metis.dereference.service.dao.EntityDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import eu.europeana.metis.dereference.service.utils.RdfRetriever;
import eu.europeana.metis.utils.PivotalCloudFoundryServicesReader;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Spring configuration class
 * Created by ymamakis on 12-2-16.
 */
@Configuration
@ComponentScan(basePackages = {"eu.europeana.metis.dereference.rest",
    "eu.europeana.metis.dereference.rest.exceptions"})
@PropertySource("classpath:dereferencing.properties")
@EnableWebMvc
@EnableSwagger2
public class Application extends WebMvcConfigurerAdapter implements InitializingBean {
  //Redis
  @Value("${redis.host}")
  private String redisHost;
  @Value("${redis.port}")
  private int redisPort;
  @Value("${redis.password}")
  private String redisPassword;

  //Mongo
  @Value("${mongo.host}")
  private String mongoHost;
  @Value("${mongo.port}")
  private int mongoPort;
  @Value("${mongo.username}")
  private String mongoUsername;
  @Value("${mongo.password}")
  private String mongoPassword;
  @Value("${mongo.db}")
  private String mongoDb;
  @Value("${entity.db}")
  private String entityDb;
  @Value("${vocabulary.db}")
  private String vocabularyDb;

  @Value("${enrichment.url}")
  private String enrichmentUrl;
  private MongoClientURI mongoClientURI;
  private RedisProvider redisProvider;

  /**
   * Used for overwriting properties if cloud foundry environment is used
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    if (System.getenv().get("VCAP_SERVICES") != null) {
      PivotalCloudFoundryServicesReader vcapServices = new PivotalCloudFoundryServicesReader(
          System.getenv().get("VCAP_SERVICES"));

      mongoClientURI = vcapServices.getMongoClientUriFromService();
      vocabularyDb = mongoClientURI.getDatabase();
      entityDb = mongoClientURI.getDatabase();
      redisProvider = vcapServices.getRedisProviderFromService();
    }
  }

  @Bean
  EnrichmentDriver getEnrichmentDriver() {
    return new EnrichmentDriver(enrichmentUrl);
  }

  @Bean
  MongoClient getMongo() {
    if (mongoClientURI != null) {
      return new MongoClient(mongoClientURI);
    } else {
      ServerAddress address = new ServerAddress(mongoHost, mongoPort);
      MongoCredential mongoCredential;
      MongoClient mongoClient;
      if (StringUtils.isNotEmpty(mongoUsername) && StringUtils.isNotEmpty(mongoPassword)) {
        mongoCredential = MongoCredential
            .createCredential(mongoUsername, mongoDb, mongoPassword.toCharArray());
        mongoClient = new MongoClient(address, Arrays.asList(mongoCredential));
      } else {
        mongoClient = new MongoClient(address);
      }

      return mongoClient;
    }
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(new MappingJackson2HttpMessageConverter());

    super.configureMessageConverters(converters);
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("swagger-ui.html")
        .addResourceLocations("classpath:/META-INF/resources/");
    registry.addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/");
  }

  @Bean
  RdfRetriever getRdfRetriever() {
    return new RdfRetriever();
  }

  @Bean
  CacheDao getCacheDao() {
    return new CacheDao(getRedisProvider().getJedis());
  }

  @Bean
  RedisProvider getRedisProvider() {
    if (redisProvider != null)
      return redisProvider;
    else
      return new RedisProvider(redisHost, redisPort, redisPassword);
  }

  @Bean
  EntityDao getEntityDao() {
    return new EntityDao(getMongo(), entityDb);
  }

  @Bean
  VocabularyDao getVocabularyDao() {
    return new VocabularyDao(getMongo(), vocabularyDb);
  }

  @Bean
  MongoDereferenceService getMongoDereferenceService() {
    return new MongoDereferenceService();
  }

  @Bean
  MongoDereferencingManagementService getMongoDereferencingManagementService() {
    return new MongoDereferencingManagementService();
  }

  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.any())
        .paths(PathSelectors.regex("/.*"))
        .build()
        .apiInfo(apiInfo());
  }

  private ApiInfo apiInfo() {
    ApiInfo apiInfo = new ApiInfo(
        "Dereference REST API",
        "Dereference REST API for Europeana",
        "v1",
        "API TOS",
        "development@europeana.eu",
        "EUPL Licence v1.1",
        ""
    );
    return apiInfo;
  }
}
