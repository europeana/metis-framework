package eu.europeana.metis.authentication.rest.config;

import com.zoho.oauth.common.ZohoOAuthException;
import eu.europeana.metis.authentication.dao.PsqlMetisUserDao;
import eu.europeana.metis.authentication.user.MetisZohoOAuthToken;
import eu.europeana.metis.authentication.utils.MetisZohoOAuthPSQLHandler;
import eu.europeana.metis.utils.CustomTruststoreAppender.TrustStoreConfigurationException;
import eu.europeana.metis.zoho.ZohoAccessClient;
import eu.europeana.metis.authentication.service.AuthenticationService;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.authentication.user.MetisUserAccessToken;
import eu.europeana.metis.utils.CustomTruststoreAppender;
import java.util.List;
import javax.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
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
@ComponentScan(basePackages = {"eu.europeana.metis.authentication.rest"})
@PropertySource("classpath:authentication.properties")
@EnableWebMvc
@EnableScheduling
public class Application implements WebMvcConfigurer, InitializingBean {

  //Custom trustore
  @Value("${truststore.path}")
  private String truststorePath;
  @Value("${truststore.password}")
  private String truststorePassword;

  @Value("${zoho.initial.grant.token}")
  private String zohoInitialGrantToken;
  @Value("${metis.access.token.expire.time.in.mins}")
  private int metisAccessTokenExpireTimeInMins;
  @Value("${allowed.cors.hosts}")
  private String[] allowedCorsHosts;

  private SessionFactory sessionFactory;
  private AuthenticationService authenticationService;

  /**
   * Used for overwriting properties if cloud foundry environment is used
   */
  @Override
  public void afterPropertiesSet() throws TrustStoreConfigurationException {
    if (StringUtils.isNotEmpty(truststorePath) && StringUtils.isNotEmpty(truststorePassword)) {
      CustomTruststoreAppender.appendCustomTrustoreToDefault(truststorePath, truststorePassword);
    }
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
  public ZohoAccessClient getZohoAccessClient() throws ZohoOAuthException {
    return new ZohoAccessClient(zohoInitialGrantToken);
  }

  @Bean
  public SessionFactory getSessionFactory() {
    org.hibernate.cfg.Configuration configuration = new org.hibernate.cfg.Configuration();
    configuration.addAnnotatedClass(MetisUser.class);
    configuration.addAnnotatedClass(MetisUserAccessToken.class);
    configuration.addAnnotatedClass(MetisZohoOAuthToken.class);
    configuration.configure();
    ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(
        configuration.getProperties()).build();
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
