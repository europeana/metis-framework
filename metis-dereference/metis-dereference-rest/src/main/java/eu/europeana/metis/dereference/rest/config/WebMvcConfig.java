package eu.europeana.metis.dereference.rest.config;

import eu.europeana.metis.dereference.rest.config.properties.MetisDereferenceConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  private final MetisDereferenceConfigurationProperties metisDereferenceConfigurationProperties;

  /**
   * Constructor.
   *
   * @param metisDereferenceConfigurationProperties The properties.
   */
  @Autowired
  public WebMvcConfig(MetisDereferenceConfigurationProperties metisDereferenceConfigurationProperties) {
    this.metisDereferenceConfigurationProperties = metisDereferenceConfigurationProperties;
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addRedirectViewController("/", "/swagger-ui/index.html");
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**").allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedOrigins(metisDereferenceConfigurationProperties.allowedCorsHosts().toArray(String[]::new));
  }
}
