package eu.europeana.metis.data.checker.config;

import eu.europeana.corelib.web.socks.SocksProxy;
import eu.europeana.indexing.AbstractConnectionProvider;
import eu.europeana.indexing.IndexingSettings;
import eu.europeana.indexing.SettingsConnectionProvider;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.metis.data.checker.service.ZipService;
import eu.europeana.metis.data.checker.service.executor.ValidationUtils;
import eu.europeana.metis.data.checker.service.persistence.RecordDao;
import eu.europeana.metis.transformation.service.XsltTransformer;
import eu.europeana.validation.client.ValidationClient;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Configuration file for Spring MVC
 */
@Configuration
@ComponentScan(basePackages = {"eu.europeana.metis.data.checker.rest",
    "eu.europeana.metis.data.checker.service", "eu.europeana.metis.data.checker"})
@PropertySource("classpath:data.checker.properties")
@EnableWebMvc
@EnableSwagger2
@EnableScheduling
public class Application extends WebMvcConfigurerAdapter implements InitializingBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

  private static final int MAX_UPLOAD_SIZE = 50_000_000;

  // Socks proxy
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
  private String[] mongoHosts;
  @Value("${mongo.port}")
  private int[] mongoPorts;
  @Value("${mongo.authentication.db}")
  private String mongoAuthenticationDb;
  @Value("${mongo.username}")
  private String mongoUsername;
  @Value("${mongo.password}")
  private String mongoPassword;
  @Value("${mongo.db}")
  private String mongoDb;
  @Value("${mongo.enableSSL}")
  private boolean mongoEnableSSL;

  @Value("${solr.hosts}")
  private String[] solrHosts;

  @Value("${zookeeper.hosts}")
  private String[] zookeeperHosts;
  @Value("${zookeeper.ports}")
  private int[] zookeeperPorts;
  @Value("${zookeeper.chroot}")
  private String zookeeperChroot;
  @Value("${zookeeper.default.collection}")
  private String zookeeperDefaultCollection;
  @Value("${zookeeper.timeout.in.secs}")
  private int zookeeperTimeoutInSecs;

  @Value("${validation.schema.before_transformation}")
  private String schemaBeforeTransformation;
  @Value("${validation.schema.after_transformation}")
  private String schemaAfterTransformation;

  @Value("${metis.core.uri}")
  private String metisCoreUri;

  @Value("${xslt.cache.expiration.in.sec}")
  private int xsltCacheExpirationInSec;
  @Value("${xslt.cache.unused.cleanup.in.min}")
  private int xsltCacheCleanupInMin;

  private AbstractConnectionProvider indexingConnection;

  /**
   * Used for overwriting properties if cloud foundry environment is used
   */
  @Override
  public void afterPropertiesSet() throws Exception {

    // Create the indexing settings
    final IndexingSettings settings = new IndexingSettings();

    // Set the Mongo properties
    for (InetSocketAddress address : getAddressesFromHostsAndPorts(mongoHosts, mongoPorts)) {
      settings.addMongoHost(address);
    }
    settings.setMongoDatabaseName(mongoDb);
    if (mongoEnableSSL) {
      settings.setMongoEnableSsl();
    }
    if (StringUtils.isNotBlank(mongoUsername) || StringUtils.isNotBlank(mongoPassword)
        || StringUtils.isNotBlank(mongoAuthenticationDb)) {
      settings.setMongoCredentials(mongoUsername, mongoPassword, mongoAuthenticationDb);
    }

    // Set Solr properties
    for (String host : solrHosts) {
      settings.addSolrHost(new URI(host));
    }

    // Set Zookeeper properties
    for (InetSocketAddress address : getAddressesFromHostsAndPorts(zookeeperHosts,
        zookeeperPorts)) {
      settings.addZookeeperHost(address);
    }
    if (StringUtils.isNotBlank(zookeeperChroot)) {
      settings.setZookeeperChroot(zookeeperChroot);
    }
    if (StringUtils.isNotBlank(zookeeperDefaultCollection)) {
      settings.setZookeeperDefaultCollection(zookeeperDefaultCollection);
    }
    settings.setZookeeperTimeoutInSecs(zookeeperTimeoutInSecs);

    // Create the indexing connection
    indexingConnection = new SettingsConnectionProvider(settings);

    // Configure the socks proxy.
    if (socksProxyEnabled) {
      new SocksProxy(socksProxyHost, socksProxyPort, socksProxyUsername, socksProxyPassword).init();
    }

    // Configure the xslt cache
    XsltTransformer.setExpirationTime(Duration.ZERO.plusSeconds(xsltCacheExpirationInSec));
    XsltTransformer.setLenientWithReloads(true);
    
    // Schedule cache cleaning
    final Duration since = Duration.ofMinutes(xsltCacheCleanupInMin);
    final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    scheduler.scheduleWithFixedDelay(() -> XsltTransformer.removeItemsNotAccessedSince(since),
        xsltCacheCleanupInMin, xsltCacheCleanupInMin, TimeUnit.MINUTES);
  }

  private static List<InetSocketAddress> getAddressesFromHostsAndPorts(String[] hosts,
      int[] ports) {
    final List<InetSocketAddress> result = new ArrayList<>();
    if (hosts.length != ports.length && ports.length != 1) {
      throw new IllegalArgumentException("Hosts and ports do not match.");
    }
    for (int i = 0; i < hosts.length; i++) {
      final int port = ports.length == 1 ? ports[0] : ports[i];
      result.add(new InetSocketAddress(hosts[i], port));
    }
    return result;
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
  ValidationClient validationClient() throws IOException {
    return new ValidationClient();
  }

  @Bean
  ZipService zipService() {
    return new ZipService();
  }

  @Bean()
  AbstractConnectionProvider getIndexingConnection() {
    return indexingConnection;
  }

  @Bean
  RecordDao recordDao() throws IndexingException {
    return new RecordDao(indexingConnection);
  }

  @Bean
  public CommonsMultipartResolver multipartResolver() {
    CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
    commonsMultipartResolver.setDefaultEncoding("utf-8");
    commonsMultipartResolver.setMaxUploadSize(MAX_UPLOAD_SIZE);
    return commonsMultipartResolver;
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.any())
        .paths(PathSelectors.regex("/.*")).build().apiInfo(apiInfo());
  }

  @Bean
  public ValidationUtils getValidationUtils() throws IOException, IndexingException {
    return new ValidationUtils(validationClient(), recordDao(), schemaBeforeTransformation,
        schemaAfterTransformation, metisCoreUri);
  }
  
  @PreDestroy
  public void close() throws IOException {
    LOGGER.info("Closing connections..");
    if (indexingConnection != null) {
      indexingConnection.close();
    }
  }

  private ApiInfo apiInfo() {
    return new ApiInfo("Data Checker REST API", "Data Checker REST API for Europeana", "v1",
        "API TOS", new Contact("development", "europeana.eu", "development@europeana.eu"),
        "EUPL Licence v1.1", "");
  }
}
