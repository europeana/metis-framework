package eu.europeana.metis.authentication.rest.config;

import static eu.europeana.metis.utils.SonarqubeNullcheckAvoidanceUtils.performAction;

import eu.europeana.metis.authentication.dao.PsqlMetisUserDao;
import eu.europeana.metis.authentication.rest.config.properties.MetisAuthenticationConfigurationProperties;
import eu.europeana.metis.authentication.service.AuthenticationService;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.authentication.user.MetisUserAccessToken;
import eu.europeana.metis.authentication.user.MetisZohoOAuthToken;
import eu.europeana.metis.authentication.utils.MetisZohoOAuthPSQLHandler;
import eu.europeana.metis.utils.CustomTruststoreAppender;
import eu.europeana.metis.utils.CustomTruststoreAppender.TrustStoreConfigurationException;
import eu.europeana.metis.utils.apm.ElasticAPMConfiguration;
import eu.europeana.metis.zoho.ZohoAccessClient;
import eu.europeana.metis.zoho.ZohoException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.annotation.PreDestroy;
import metis.common.config.properties.TruststoreConfigurationProperties;
import metis.common.config.properties.postgres.HibernateConfigurationProperties;
import metis.common.config.properties.zoho.ZohoConfigurationProperties;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Entry class with configuration fields and beans initialization for the application.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-27
 */
@Configuration
@EnableConfigurationProperties({
    ElasticAPMConfiguration.class, TruststoreConfigurationProperties.class,
    HibernateConfigurationProperties.class, ZohoConfigurationProperties.class,
    MetisAuthenticationConfigurationProperties.class})
@ComponentScan(basePackages = {"eu.europeana.metis.authentication.rest.controller"})
@EnableScheduling
public class ApplicationConfiguration implements WebMvcConfigurer, ApplicationContextAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Value("classpath:create_tables.sql")
  private Resource createTablesSqlResource;

  private SessionFactory sessionFactory;
  private AuthenticationService authenticationService;
  private MetisZohoOAuthPSQLHandler metisZohoOAuthPSQLHandler;
  private ApplicationContext applicationContext;

  /**
   * Constructor.
   *
   * @param truststoreConfigurationProperties the truststore configuration properties
   * @throws CustomTruststoreAppender.TrustStoreConfigurationException if the configuration of the truststore failed
   */
  @Autowired
  public ApplicationConfiguration(TruststoreConfigurationProperties truststoreConfigurationProperties)
      throws TrustStoreConfigurationException {
    ApplicationConfiguration.initializeApplication(truststoreConfigurationProperties);
  }

  /**
   * Get the session factory.
   *
   * @param hibernateConfigurationProperties the hibernate configuration properties
   * @return the session factory
   * @throws IOException if an I/O error occurs during sql script initialization
   */
  @Bean
  public SessionFactory getSessionFactory(HibernateConfigurationProperties hibernateConfigurationProperties) throws IOException {

    org.hibernate.cfg.Configuration configuration = new org.hibernate.cfg.Configuration();
    configuration.addAnnotatedClass(MetisUser.class);
    configuration.addAnnotatedClass(MetisUserAccessToken.class);
    configuration.addAnnotatedClass(MetisZohoOAuthToken.class);

    //Apply code configuration to allow spring boot to handle the properties injection
    configuration.setProperty("hibernate.connection.driver_class",
        hibernateConfigurationProperties.getConnection().getDriverClass());
    configuration.setProperty("hibernate.connection.url", hibernateConfigurationProperties.getConnection().getUrl());
    configuration.setProperty("hibernate.connection.username", hibernateConfigurationProperties.getConnection().getUsername());
    configuration.setProperty("hibernate.connection.password", hibernateConfigurationProperties.getConnection().getPassword());
    configuration.setProperty("hibernate.dialect", hibernateConfigurationProperties.getDialect());
    configuration.setProperty("hibernate.c3p0.min_size", hibernateConfigurationProperties.getC3p0().getMinSize());
    configuration.setProperty("hibernate.c3p0.max_size", hibernateConfigurationProperties.getC3p0().getMaxSize());
    configuration.setProperty("hibernate.c3p0.timeout", hibernateConfigurationProperties.getC3p0().getTimeout());
    configuration.setProperty("hibernate.c3p0.max_statements", hibernateConfigurationProperties.getC3p0().getMaxStatements());
    configuration.setProperty("hibernate.hbm2ddl.auto", hibernateConfigurationProperties.getHbm2ddl().getAuto());

    ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
        .applySettings(configuration.getProperties()).build();
    sessionFactory = configuration.buildSessionFactory(serviceRegistry);

    //Read the sql file
    String createTablesSql;
    Reader reader = new InputStreamReader(createTablesSqlResource.getInputStream(),
        StandardCharsets.UTF_8);
    createTablesSql = FileCopyUtils.copyToString(reader);

    try (Session dbSession = sessionFactory.openSession()) {
      performAction(dbSession, session -> {
        Transaction tx = session.beginTransaction();
        session.createSQLQuery(createTablesSql).executeUpdate();
        tx.commit();
      });
    }

    return sessionFactory;
  }

  /**
   * Set the application context.
   *
   * @param applicationContext the application context
   * @throws BeansException if a beans exception occurs
   */
  @Override
  public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  /**
   * This method performs the initializing tasks for the application.
   *
   * @param truststoreConfigurationProperties The properties.
   * @throws CustomTruststoreAppender.TrustStoreConfigurationException In case a problem occurred with the truststore.
   */
  static void initializeApplication(TruststoreConfigurationProperties truststoreConfigurationProperties)
      throws CustomTruststoreAppender.TrustStoreConfigurationException {

    // Load the trust store file.
    if (StringUtils.isNotEmpty(truststoreConfigurationProperties.getPath()) && StringUtils
        .isNotEmpty(truststoreConfigurationProperties.getPassword())) {
      CustomTruststoreAppender
          .appendCustomTruststoreToDefault(truststoreConfigurationProperties.getPath(),
              truststoreConfigurationProperties.getPassword());
      LOGGER.info("Custom truststore appended to default truststore");
    }
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    MetisAuthenticationConfigurationProperties metisAuthenticationConfigurationProperties =
        applicationContext.getBean(MetisAuthenticationConfigurationProperties.class);
    registry.addMapping("/**").allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedOrigins(metisAuthenticationConfigurationProperties.getAllowedCorsHosts());
  }

  /**
   * Get the Service for authentication.
   *
   * @param psqlMetisUserDao the dao instance to access user information from the internal database
   * @param zohoAccessClient the zoho client
   * @return the authentication service instance
   */
  @Bean
  public AuthenticationService getAuthenticationService(PsqlMetisUserDao psqlMetisUserDao,
      ZohoAccessClient zohoAccessClient) {
    authenticationService = new AuthenticationService(psqlMetisUserDao, zohoAccessClient);
    return authenticationService;
  }

  /**
   * Get the zoho access client.
   *
   * @param sessionFactory the session factory
   * @param zohoConfigurationProperties the zoho configuration properties
   * @return the zoho access client
   * @throws ZohoException if a zoho configuration error occurred
   */
  @Bean
  public ZohoAccessClient getZohoAccessClient(SessionFactory sessionFactory,
      ZohoConfigurationProperties zohoConfigurationProperties) throws ZohoException {
    metisZohoOAuthPSQLHandler = new MetisZohoOAuthPSQLHandler(sessionFactory, zohoConfigurationProperties.getCurrentUserEmail(),
        zohoConfigurationProperties.getRefreshToken(),
        zohoConfigurationProperties.getClientId(), zohoConfigurationProperties.getClientSecret());

    final ZohoAccessClient zohoAccessClient = new ZohoAccessClient(metisZohoOAuthPSQLHandler,
        zohoConfigurationProperties.getCurrentUserEmail(), zohoConfigurationProperties.getClientId(),
        zohoConfigurationProperties.getClientSecret(), zohoConfigurationProperties.getInitialGrantToken(),
        zohoConfigurationProperties.getRedirectUri());
    //Make a call to zoho so that the grant token will generate the first pair of access/refresh tokens
    zohoAccessClient.getZohoRecordContactByEmail("");
    return zohoAccessClient;
  }

  /**
   * Get the DAO for metis users.
   *
   * @param sessionFactory the session factory required to initialize the DAO
   * @param metisAuthenticationConfigurationProperties the metis authentication configuration properties
   * @return the DAO instance for accessing user information
   */
  @Bean
  public PsqlMetisUserDao getPsqlMetisUserDao(SessionFactory sessionFactory,
      MetisAuthenticationConfigurationProperties metisAuthenticationConfigurationProperties) {
    PsqlMetisUserDao psqlMetisUserDao = new PsqlMetisUserDao(sessionFactory);
    psqlMetisUserDao.setAccessTokenExpireTimeInMins(
        metisAuthenticationConfigurationProperties.getAccessTokenExpireTimeInMinutes());
    return psqlMetisUserDao;
  }

  /**
   * Scheduled method that would expire access token periodically.
   */
  @Scheduled(fixedDelay = 60 * 1000, initialDelay = 60 * 1000) //1min
  public void expireAccessTokens() {
    authenticationService.expireAccessTokens();
  }

  /**
   * Closes connections to databases when the application stops.
   */
  @PreDestroy
  public void close() {
    if (sessionFactory != null && !sessionFactory.isClosed()) {
      sessionFactory.close();
    }
    if (metisZohoOAuthPSQLHandler != null) {
      metisZohoOAuthPSQLHandler.close();
    }
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(new MappingJackson2HttpMessageConverter());
    converters.add(new MappingJackson2XmlHttpMessageConverter());
  }
}
