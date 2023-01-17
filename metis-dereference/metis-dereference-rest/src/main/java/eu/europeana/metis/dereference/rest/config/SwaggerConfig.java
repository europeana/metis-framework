package eu.europeana.metis.dereference.rest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Config for Swagger documentation
 */
@Configuration
public class SwaggerConfig {

  /**
   * The open api documentation docket.
   *
   * @return the docket configuration
   */
  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI().info(
        new Info().title("Dereference REST API")
                  .description("Dereference REST API for Europeana")
                  .version("v1")
                  .license(
                      new License().name("EUPL Licence v1.2")
                                   .url("https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12")));
  }
}
