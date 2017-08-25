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
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import eu.europeana.corelib.storage.impl.MongoProviderImpl;
import eu.europeana.enrichment.rest.client.EnrichmentClient;
import eu.europeana.metis.cache.redis.RedisProvider;
import eu.europeana.metis.dereference.service.MongoDereferenceService;
import eu.europeana.metis.dereference.service.MongoDereferencingManagementService;
import eu.europeana.metis.dereference.service.dao.CacheDao;
import eu.europeana.metis.dereference.service.dao.EntityDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import eu.europeana.metis.dereference.service.utils.RdfRetriever;
import eu.europeana.metis.utils.PivotalCloudFoundryServicesReader;
import javax.annotation.PreDestroy;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
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
  @Value("${mongo.hosts}")
  private String mongoHosts;
  @Value("${mongo.port}")
  private int mongoPort;
  @Value("${mongo.username}")
  private String mongoUsername;
  @Value("${mongo.password}")
  private String mongoPassword;
  @Value("${entity.db}")
  private String entityDb;
  @Value("${vocabulary.db}")
  private String vocabularyDb;

  @Value("${enrichment.url}")
  private String enrichmentUrl;
  private MongoProviderImpl mongoProviderEntity;
  private MongoProviderImpl mongoProviderVocabulary;
  private RedisProvider redisProvider;

  /**
   * Used for overwriting properties if cloud foundry environment is used
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    String vcapServicesJson = System.getenv().get("VCAP_SERVICES");
    if (StringUtils.isNotEmpty(vcapServicesJson) && !StringUtils.equals(vcapServicesJson, "{}")) {
      PivotalCloudFoundryServicesReader vcapServices = new PivotalCloudFoundryServicesReader(
          vcapServicesJson);

      MongoClientURI mongoClientURI = vcapServices.getMongoClientUriFromService();
      if (mongoClientURI != null) {
        String mongoHostAndPort = mongoClientURI.getHosts().get(0);
        mongoHosts = mongoHostAndPort.substring(0, mongoHostAndPort.lastIndexOf(":"));
        mongoPort = Integer
            .parseInt(mongoHostAndPort.substring(mongoHostAndPort.lastIndexOf(":") + 1));
        mongoUsername = mongoClientURI.getUsername();
        mongoPassword = String.valueOf(mongoClientURI.getPassword());
        vocabularyDb = mongoClientURI.getDatabase();
        entityDb = mongoClientURI.getDatabase();
      }

      RedisProvider redisProviderFromService = vcapServices.getRedisProviderFromService();
      if (redisProviderFromService != null) {
        redisProvider = redisProviderFromService;
      }
    }

    String[] mongoHostsArray = mongoHosts.split(",");
    StringBuilder mongoPorts = new StringBuilder();
    for (int i = 0; i < mongoHostsArray.length; i++) {
      mongoPorts.append(mongoPort + ",");
    }
    mongoPorts.replace(mongoPorts.lastIndexOf(","), mongoPorts.lastIndexOf(","), "");
    MongoClientOptions.Builder options = MongoClientOptions.builder();
    options.socketKeepAlive(true);
    mongoProviderEntity = new MongoProviderImpl(mongoHosts, mongoPorts.toString(), entityDb,
        mongoUsername,
        mongoPassword, options);
    mongoProviderVocabulary = new MongoProviderImpl(mongoHosts, mongoPorts.toString(), vocabularyDb,
        mongoUsername,
        mongoPassword, options);

    if (redisProvider == null) {
      redisProvider = new RedisProvider(redisHost, redisPort, redisPassword);
    }
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("swagger-ui.html")
        .addResourceLocations("classpath:/META-INF/resources/");
    registry.addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/");
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addRedirectViewController("/", "swagger-ui.html");
  }

  @Bean
  EnrichmentClient getEnrichmentClient() {
    return new EnrichmentClient(enrichmentUrl);
  }

  MongoClient getEntityMongoClient() {
    return mongoProviderEntity.getMongo();
  }

  MongoClient getVocabularyMongoClient() {
    return mongoProviderVocabulary.getMongo();
  }

  @Bean
  CacheDao getCacheDao() {
    return new CacheDao(getRedisProvider().getJedis());
  }

  @Bean
  RedisProvider getRedisProvider() {
    return redisProvider;
  }

  @Bean
  EntityDao getEntityDao() {
    return new EntityDao(getEntityMongoClient(), entityDb);
  }

  @Bean
  VocabularyDao getVocabularyDao() {
    return new VocabularyDao(getVocabularyMongoClient(), vocabularyDb);
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
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

  @PreDestroy
  public void close()
  {
    if (mongoProviderVocabulary != null)
      mongoProviderVocabulary.close();
    if (mongoProviderEntity != null)
      mongoProviderEntity.close();
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
