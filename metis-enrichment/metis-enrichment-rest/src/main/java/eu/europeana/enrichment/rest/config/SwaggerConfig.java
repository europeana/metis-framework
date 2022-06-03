package eu.europeana.enrichment.rest.config;

import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Config for Swagger documentation.
 */
// TODO: 03/06/2022 Disable swagger temporarily that is because
//  the rest application is based on webmvc while the entity client on webflux.
//  Both libs need to be in the classpath therefore swagger tries to load two controllers and they collide.
//  Somehow we need to selectively exclude the Swagger2ControllerWebFlux from loading
//@Configuration
//@EnableSwagger2
public class SwaggerConfig implements WebMvcConfigurer {

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addRedirectViewController("/", "/swagger-ui/index.html");
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/swagger-ui/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
            .resourceChain(false);
  }

  /**
   * Initialize Swagger Documentation
   *
   * @return Swagger Docket for this API
   */
  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
        .useDefaultResponseMessages(false)
        .select()
        .apis(RequestHandlerSelectors.any())
        .paths(PathSelectors.regex("/.*"))
        .build()
        .apiInfo(apiInfo());
  }

  private ApiInfo apiInfo() {
    Contact contact = new Contact("Europeana", "http:\\www.europeana.eu",
        "development@europeana.eu");

    return new ApiInfo(
        "Enrichment REST API",
        "Enrichment REST API for Europeana",
        "v1",
        "API TOS",
        contact,
        "EUPL Licence v1.2",
        "https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12", Collections.emptyList());
  }
}
