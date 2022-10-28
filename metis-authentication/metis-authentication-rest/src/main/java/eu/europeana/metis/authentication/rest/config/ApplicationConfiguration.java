package eu.europeana.metis.authentication.rest.config;

import static eu.europeana.metis.utils.SonarqubeNullcheckAvoidanceUtils.performAction;

import eu.europeana.metis.authentication.dao.PsqlMetisUserDao;
import eu.europeana.metis.authentication.service.AuthenticationService;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.authentication.user.MetisUserAccessToken;
import eu.europeana.metis.authentication.utils.MetisZohoOAuthPSQLHandler;
import eu.europeana.metis.utils.CustomTruststoreAppender;
import eu.europeana.metis.utils.CustomTruststoreAppender.TrustStoreConfigurationException;
import eu.europeana.metis.zoho.ZohoAccessClient;
import eu.europeana.metis.zoho.ZohoException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Entry class with configuration fields and beans initialization for the application.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-27
 */
@Configuration
@ComponentScan(basePackages = {"eu.europeana.metis.authentication.rest.controller"})
@PropertySource("classpath:authentication.properties")
@EnableWebMvc
@EnableScheduling
public class ApplicationConfiguration implements WebMvcConfigurer {

  @Value("classpath:create_tables.sql")
  private Resource createTablesSqlResource;

  //Custom trustore
  @Value("${truststore.path}")
  private String truststorePath;
  @Value("${truststore.password}")
  private String truststorePassword;
  @Value("${metis.access.token.expire.time.in.mins}")
  private int metisAccessTokenExpireTimeInMins;
  @Value("${allowed.cors.hosts}")
  private String[] allowedCorsHosts;

  //Zoho configuration
  @Value("${zoho.initial.grant.token}")
  private String zohoInitialGrantToken;
  @Value("${zoho.refresh.token}")
  private String zohoRefreshToken;
  @Value("${zoho.current.user.email}")
  private String zohoCurrentUserEmail;
  @Value("${zoho.client.id}")
  private String zohoClientId;
  @Value("${zoho.client.secret}")
  private String zohoClientSecret;
  @Value("${zoho.redirect.uri}")
  private String zohoRedirectUri;

  private SessionFactory sessionFactory;
  private AuthenticationService authenticationService;

  @PostConstruct
  private void postConstruct() throws TrustStoreConfigurationException, IOException {
    if (StringUtils.isNotEmpty(truststorePath) && StringUtils.isNotEmpty(truststorePassword)) {
      CustomTruststoreAppender.appendCustomTrustoreToDefault(truststorePath, truststorePassword);
    }

    //Read the sql file
    String createTablesSql;
    Reader reader = new InputStreamReader(createTablesSqlResource.getInputStream(),
        StandardCharsets.UTF_8);
    createTablesSql = FileCopyUtils.copyToString(reader);

    //Execute sql and create tables if needed
    org.hibernate.cfg.Configuration configuration = new org.hibernate.cfg.Configuration();
    configuration.configure();
    ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
        .applySettings(configuration.getProperties()).build();
    sessionFactory = configuration.buildSessionFactory(serviceRegistry);
    try (Session dbSession = sessionFactory.openSession()) {
      performAction(dbSession, session -> {
        Transaction tx = session.beginTransaction();
        session.createSQLQuery(createTablesSql).executeUpdate();
        tx.commit();
      });
    } finally {
      sessionFactory.close();
    }
    //Initialize Zoho handler
    MetisZohoOAuthPSQLHandler
        .initializeWithRefreshToken(zohoCurrentUserEmail, zohoRefreshToken, zohoClientId,
            zohoClientSecret);
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**").allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedOrigins(allowedCorsHosts);
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

  @Bean
  public ZohoAccessClient getZohoAccessClient() throws ZohoException {
    final MetisZohoOAuthPSQLHandler metisZohoOAuthPSQLHandler = new MetisZohoOAuthPSQLHandler();
    final ZohoAccessClient zohoAccessClient = new ZohoAccessClient(metisZohoOAuthPSQLHandler,
        zohoCurrentUserEmail, zohoClientId, zohoClientSecret, zohoInitialGrantToken,
        zohoRedirectUri);
    //Make a call to zoho so that the grant token will generate the first pair of access/refresh tokens
    zohoAccessClient.getZohoRecordContactByEmail("");
    return zohoAccessClient;
  }

  @Bean
  public SessionFactory getSessionFactory() {
    org.hibernate.cfg.Configuration configuration = new org.hibernate.cfg.Configuration();
    configuration.addAnnotatedClass(MetisUser.class);
    configuration.addAnnotatedClass(MetisUserAccessToken.class);
    configuration.configure();
    ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
        .applySettings(configuration.getProperties()).build();
    sessionFactory = configuration.buildSessionFactory(serviceRegistry);
    return sessionFactory;
  }

  /**
   * Get the DAO for metis users.
   *
   * @param sessionFactory the session factory required to initialize the DAO
   * @return the DAO instance for accessing user information
   */
  @Bean
  public PsqlMetisUserDao getPsqlMetisUserDao(SessionFactory sessionFactory) {
    PsqlMetisUserDao psqlMetisUserDao = new PsqlMetisUserDao(sessionFactory);
    psqlMetisUserDao.setAccessTokenExpireTimeInMins(metisAccessTokenExpireTimeInMins);
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
    //Close static session factory
    MetisZohoOAuthPSQLHandler.close();
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(new MappingJackson2HttpMessageConverter());
    converters.add(new MappingJackson2XmlHttpMessageConverter());
  }
}
