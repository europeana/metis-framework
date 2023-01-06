package eu.europeana.metis.dereference.rest.config;

import java.util.Collections;
import net.sf.saxon.s9api.streams.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Config for Swagger documentation
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

  /**
   * The swagger api documentation docket.
   *
   * @return the docket configuration
   */
  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
        .useDefaultResponseMessages(false)
        .select()
        .apis(Predicates.not(RequestHandlerSelectors.basePackage("org.springframework")))
        .build()
        .apiInfo(apiInfo());
  }

  private ApiInfo apiInfo() {
    return new ApiInfo(
        "Dereference REST API",
        "Dereference REST API for Europeana",
        "v1",
        "API TOS",
        new Contact("development", "europeana.eu", "development@europeana.eu"),
        "EUPL Licence v1.2",
        "https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12", Collections.emptyList());
  }
}
