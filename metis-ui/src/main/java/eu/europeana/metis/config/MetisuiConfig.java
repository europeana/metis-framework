package eu.europeana.metis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * The configuration is for DB access (MongoDB), where the user account data such as Skype account,
 * Country, Organization, etc. is stored.
 *
 * @author alena
 */
@Configuration
@PropertySource("classpath:metisui.properties")
public class MetisuiConfig {

  @Value("${metisui.cssroot}")
  private String cssRoot;

  @Value("${metisui.scriptroot}")
  private String scriptRoot;

  @Value("${metisui.contextroot}")
  private String contextRoot;

  public String getCssRoot() {
    return cssRoot;
  }

  public String getScriptRoot() {
    return scriptRoot;
  }

  public String getContextRoot() { return contextRoot; }

  @Bean
  public MetisuiConfig metisuiConfig(){
    return this;
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }
}
