package eu.europeana.enrichment.rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import springfox.documentation.service.ApiInfo;

import java.util.Collections;

/**
 * Configures Swagger on all requests. Swagger Json file is availabe at <hostname>/v2/api-docs and
 * at <hostname/v3/api-docs. Swagger UI is available at <hostname>/swagger-ui/
 */
@EnableSwagger2
public class SwaggerConfig implements WebMvcConfigurer {

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


    // TODO - FIX swagger
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
}
