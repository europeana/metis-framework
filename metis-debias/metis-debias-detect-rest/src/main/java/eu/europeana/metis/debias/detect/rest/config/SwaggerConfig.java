package eu.europeana.metis.debias.detect.rest.config;

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
    return new OpenAPI()
        .info(new Info()
            .title("DeBias REST API")
            .description("DeBias REST API for Europeana")
            .version("v1")
            .license(new License()
                .name("EUPL License v1.2")
                .url("https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12")));
  }
}
