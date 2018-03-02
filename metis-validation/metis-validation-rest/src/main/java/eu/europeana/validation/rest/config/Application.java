package eu.europeana.validation.rest.config;

import eu.europeana.corelib.web.socks.SocksProxy;
import eu.europeana.validation.service.PredefinedSchemas;
import eu.europeana.validation.service.PredefinedSchemasGenerator;
import eu.europeana.validation.service.SchemaProvider;
import eu.europeana.validation.service.ClasspathResourceResolver;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
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
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.annotation.Resource;
import java.util.List;
import java.util.Properties;

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

  public static final int MAX_UPLOAD_SIZE = 50_000_000;

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

  @Resource(name = "validationProperties")
  private Properties predefinedSchemasLocations;

  /**
   * Used for overwriting properties if cloud foundry environment is used
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    if (socksProxyEnabled) {
      new SocksProxy(socksProxyHost, socksProxyPort, socksProxyUsername, socksProxyPassword).init();
    }
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addRedirectViewController("/", "swagger-ui.html");
  }

  @Bean(name = "validationProperties")
  PropertiesFactoryBean mapper() {
    PropertiesFactoryBean bean = new PropertiesFactoryBean();
    bean.setLocation(new ClassPathResource(
            "validation.properties"));
    return bean;
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

    /**
     * Creates {@link org.springframework.web.multipart.MultipartResolver} for application context
     * @return
     */
  @Bean
  public CommonsMultipartResolver multipartResolver() {
    CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
    commonsMultipartResolver.setDefaultEncoding("utf-8");
    commonsMultipartResolver.setMaxUploadSize(MAX_UPLOAD_SIZE);
    return commonsMultipartResolver;
  }

  /**
   * Creates {@link SchemaProvider} for application context
   *
   * @return SchemaProvider instance
   */
  @Bean
  public SchemaProvider schemaManager() {
    PredefinedSchemas predefinedSchemas = PredefinedSchemasGenerator.generate(predefinedSchemasLocations);
    return new SchemaProvider(predefinedSchemas);
  }

  @Bean
  public ClasspathResourceResolver getLSResourceResolver() {
    return new ClasspathResourceResolver();
  }

    /**
     * Creates {@link PropertySourcesPlaceholderConfigurer} for application context
     *
     * @return PropertySourcesPlaceholderConfigurer instance
     */
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

  private ApiInfo apiInfo() {
    return new ApiInfo(
        "Validation REST API",
        "Validation REST API for Europeana",
        "v1",
        "API TOS",
        new Contact("Europeana","europeana.eu","development@europeana.eu"),
        "EUPL Licence v1.1",
        ""
    );
  }
}
