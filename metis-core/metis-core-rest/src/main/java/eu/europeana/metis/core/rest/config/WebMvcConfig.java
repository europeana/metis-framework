package eu.europeana.metis.core.rest.config;

import eu.europeana.metis.core.rest.config.properties.MetisCoreConfigurationProperties;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  private final MetisCoreConfigurationProperties metisCoreConfigurationProperties;

  /**
   * Constructor.
   *
   * @param metisCoreConfigurationProperties The properties.
   */
  public WebMvcConfig(MetisCoreConfigurationProperties metisCoreConfigurationProperties) {
    this.metisCoreConfigurationProperties = metisCoreConfigurationProperties;
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**").allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedOrigins(metisCoreConfigurationProperties.getAllowedCorsHosts());
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(new MappingJackson2HttpMessageConverter());
    converters.add(new MappingJackson2XmlHttpMessageConverter());
    converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
  }
}
