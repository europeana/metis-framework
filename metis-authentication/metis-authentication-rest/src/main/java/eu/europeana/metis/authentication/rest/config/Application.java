package eu.europeana.metis.authentication.rest.config;

import eu.europeana.metis.authentication.dao.PsqlMetisUserDao;
import eu.europeana.metis.authentication.dao.ZohoAccessClientDao;
import eu.europeana.metis.authentication.service.AuthenticationService;
import eu.europeana.metis.authentication.user.MetisUser;
import java.util.List;
import javax.annotation.PreDestroy;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-27
 */
@Configuration
@ComponentScan(basePackages = {"eu.europeana.metis.authentication.rest"})
@PropertySource("classpath:authentication.properties")
@EnableWebMvc
//@EnableSwagger2
public class Application extends WebMvcConfigurerAdapter {

  @Value("${zoho.base.url}")
  private String zohoBaseUrl;
  @Value("${zoho.authentication.token}")
  private String zohoAuthenticationToken;

  private SessionFactory sessionFactory;

  @Bean
  public AuthenticationService getAuthenticationService(ZohoAccessClientDao zohoAccessClientDao,
      PsqlMetisUserDao psqlMetisUserDao) {
    return new AuthenticationService(zohoAccessClientDao, psqlMetisUserDao);
  }

  @Bean
  public ZohoAccessClientDao getZohoAccessClientDao() {
    return new ZohoAccessClientDao(zohoBaseUrl, zohoAuthenticationToken);
  }

  @Bean
  public SessionFactory getSessionFactory() {
    org.hibernate.cfg.Configuration configuration = new org.hibernate.cfg.Configuration();
    configuration.addAnnotatedClass(MetisUser.class);
    configuration.configure();
    ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(
        configuration.getProperties()).build();
    sessionFactory = configuration.buildSessionFactory(serviceRegistry);
    return sessionFactory;
  }

  @Bean
  public PsqlMetisUserDao getPsqlMetisUserDao(SessionFactory sessionFactory) {
    return new PsqlMetisUserDao(sessionFactory);
  }

  @PreDestroy
  public void close() {
    if (sessionFactory != null && !sessionFactory.isClosed()) {
      sessionFactory.close();
    }
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(new MappingJackson2HttpMessageConverter());
    converters.add(new MappingJackson2XmlHttpMessageConverter());
    super.configureMessageConverters(converters);
  }

//  @Bean
//  public View json() {
//    MappingJackson2JsonView view = new MappingJackson2JsonView();
//    view.setPrettyPrint(true);
//    view.setObjectMapper(new CustomObjectMapper());
//    return view;
//  }
//
//  @Override
//  public void addViewControllers(ViewControllerRegistry registry) {
//    registry.addRedirectViewController("/", "swagger-ui.html");
//  }
//
//  @Override
//  public void addResourceHandlers(ResourceHandlerRegistry registry) {
//    registry.addResourceHandler("swagger-ui.html")
//        .addResourceLocations("classpath:/META-INF/resources/");
//    registry.addResourceHandler("/webjars/**")
//        .addResourceLocations("classpath:/META-INF/resources/webjars/");
//  }
//
//  @Bean
//  public Docket api() {
//    return new Docket(DocumentationType.SWAGGER_2)
//        .select()
//        .apis(RequestHandlerSelectors.any())
//        .paths(PathSelectors.regex("/.*"))
//        .build()
//        .directModelSubstitute(ObjectId.class, String.class)
//        .useDefaultResponseMessages(false)
//        .apiInfo(apiInfo());
//  }
//
//  private ApiInfo apiInfo() {
//    return new ApiInfo(
//        "Metis authentication REST API",
//        "Metis authentication REST API for Europeana",
//        "v1",
//        "API TOS",
//        new Contact("development", "europeana.eu", "development@europeana.eu"),
//        "EUPL Licence v1.1",
//        ""
//    );
//  }
}
