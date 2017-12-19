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
package eu.europeana.validation.rest.config;

import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import eu.europeana.corelib.storage.impl.MongoProviderImpl;
import eu.europeana.corelib.web.socks.SocksProxy;
import eu.europeana.features.ObjectStorageClient;
import eu.europeana.features.S3ObjectStorageClient;
import eu.europeana.features.SwiftObjectStorageClient;
import eu.europeana.metis.utils.PivotalCloudFoundryServicesReader;
import eu.europeana.validation.model.Schema;
import eu.europeana.validation.service.AbstractLSResourceResolver;
import eu.europeana.validation.service.AbstractSchemaDao;
import eu.europeana.validation.service.ClasspathResourceResolver;
import eu.europeana.validation.service.ObjectStorageResourceResolver;
import eu.europeana.validation.service.ObjectStorageSchemaDao;
import eu.europeana.validation.service.SchemaDao;
import eu.europeana.validation.service.ValidationManagementService;
import eu.europeana.validation.utils.EnableSchemaStorage;
import java.util.List;
import javax.annotation.PreDestroy;
import org.apache.commons.lang.StringUtils;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
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
 * Configuration file for Jersey
 */
@ComponentScan(basePackages = {"eu.europeana.validation.rest",
    "eu.europeana.validation.rest.exceptions.exceptionmappers"})
@PropertySource("classpath:validation.properties")
@EnableWebMvc
@EnableSwagger2
@Configuration
public class Application extends WebMvcConfigurerAdapter implements InitializingBean {

  //Socks proxy
  @Value("${socks.proxy.enabled}")
  private boolean socksProxyEnabled;
  @Value("${socks.proxy.host}")
  private String socksProxyHost;
  @Value("${socks.proxy.port}")
  private String socksProxyPort;
  @Value("${socks.proxy.username}")
  private String socksProxyUsername;
  @Value("${socks.proxy.password}")
  private String socksProxyPassword;

  @Value("${mongo.hosts}")
  private String mongoHosts;
  @Value("${mongo.port}")
  private int mongoPort;
  @Value("${mongo.username}")
  private String mongoUsername;
  @Value("${mongo.password}")
  private String mongoPassword;
  @Value("${mongo.db}")
  private String mongoDb;

  @Value("${enable.schema.storage}")
  private String enableSchemaStorage;

  //S3 properties
  @Value("${schema.root.path}")
  private String schemaRootPath;
  @Value("${s3.client.key}")
  private String s3ClientKey;
  @Value("${s3.secret.key}")
  private String s3SecretKey;
  @Value("${s3.region}")
  private String s3Region;
  @Value("${s3.bucket}")
  private String s3Bucket;

  //Swift properties
  @Value("${swift.authentication.uri}")
  private String swiftAuthenticationUri;
  @Value("${swift.availability.zone}")
  private String swiftAvailabilityZone;
  @Value("${swift.tenantname}")
  private String swiftTenantname;
  @Value("${swift.username}")
  private String swiftUsername;
  @Value("${swift.password}")
  private String swiftPassword;

  private MongoProviderImpl mongoProvider;
  private AbstractSchemaDao abstractSchemaDao;
  private ObjectStorageClient objectStorageClient;

  /**
   * Used for overwriting properties if cloud foundry environment is used
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    if (socksProxyEnabled) {
      new SocksProxy(socksProxyHost, socksProxyPort, socksProxyUsername, socksProxyPassword).init();
    }

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
        mongoDb = mongoClientURI.getDatabase();
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
    mongoProvider = new MongoProviderImpl(mongoHosts, mongoPorts.toString(), mongoDb, mongoUsername,
        mongoPassword, options);
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addRedirectViewController("/", "swagger-ui.html");
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(new MappingJackson2HttpMessageConverter());
    converters.add(new StringHttpMessageConverter());
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
  public CommonsMultipartResolver multipartResolver() {
    CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
    commonsMultipartResolver.setDefaultEncoding("utf-8");
    commonsMultipartResolver.setMaxUploadSize(50000000);
    return commonsMultipartResolver;
  }


  @Bean(name = "abstractSchemaDao")
  @DependsOn(value = "objectStorageClient")
  public AbstractSchemaDao getAbstractSchemaDao() {
    Morphia morphia = new Morphia();
    morphia.map(Schema.class);
    Datastore datastore = morphia.createDatastore(mongoProvider.getMongo(), mongoDb);
    datastore.ensureIndexes();

    if (enableSchemaStorage.equals(EnableSchemaStorage.S3.name()) || enableSchemaStorage
        .equals(EnableSchemaStorage.SWIFT.name())) {
      abstractSchemaDao = new ObjectStorageSchemaDao(datastore, schemaRootPath);
      ((ObjectStorageSchemaDao) abstractSchemaDao).setClient(objectStorageClient);
    } else {
      abstractSchemaDao = new SchemaDao(datastore, schemaRootPath);
    }
    return abstractSchemaDao;
  }

  @Bean
  @DependsOn(value = "objectStorageClient")
  public AbstractLSResourceResolver getAbstractLSResourceResolver() {
    AbstractLSResourceResolver abstractLSResourceResolver;
    if (enableSchemaStorage.equals(EnableSchemaStorage.S3.name()) || enableSchemaStorage
        .equals(EnableSchemaStorage.SWIFT.name())) {
      abstractLSResourceResolver = new ObjectStorageResourceResolver();
      ((ObjectStorageResourceResolver) abstractLSResourceResolver).setClient(objectStorageClient);
    } else {
      abstractLSResourceResolver = new ClasspathResourceResolver();
    }
    return abstractLSResourceResolver;
  }

  @Bean(name = "objectStorageClient")
  public ObjectStorageClient getObjectStorageClient() {
    if (enableSchemaStorage.equals(EnableSchemaStorage.S3.name())) {
      objectStorageClient = new S3ObjectStorageClient(s3ClientKey, s3SecretKey, s3Region, s3Bucket);
    } else if (enableSchemaStorage.equals(EnableSchemaStorage.SWIFT.name())) {
      objectStorageClient = new SwiftObjectStorageClient(swiftAuthenticationUri, swiftUsername,
          swiftPassword, schemaRootPath, swiftAvailabilityZone, swiftTenantname);
    }
    return objectStorageClient;
  }

  @Bean
  @DependsOn(value = "abstractSchemaDao")
  ValidationManagementService getValidationManagementService() {
    return new ValidationManagementService(abstractSchemaDao);
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
    if (mongoProvider != null)
      mongoProvider.close();
  }

  private ApiInfo apiInfo() {
    ApiInfo apiInfo = new ApiInfo(
        "Validation REST API",
        "Validation REST API for Europeana",
        "v1",
        "API TOS",
        "development@europeana.eu",
        "EUPL Licence v1.1",
        ""
    );
    return apiInfo;
  }
}
