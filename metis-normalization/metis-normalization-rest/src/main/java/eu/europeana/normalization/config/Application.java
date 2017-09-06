/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.normalization.config;


import eu.europeana.normalization.NormalizationService;
import eu.europeana.normalization.cleaning.DuplicateStatementCleaning;
import eu.europeana.normalization.cleaning.MarkupTagsCleaning;
import eu.europeana.normalization.cleaning.TrimAndEmptyValueCleaning;
import eu.europeana.normalization.language.LanguageNormalizer;
import eu.europeana.normalization.language.LanguageNormalizer.SupportedOperations;
import eu.europeana.normalization.language.LanguagesVocabulary;
import eu.europeana.normalization.normalizers.ChainedNormalization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Configuration file for Spring MVC
 */
@ComponentScan(basePackages = {"eu.europeana.normalization" })
@EnableWebMvc
@EnableSwagger2
@Configuration
@EnableScheduling
public class Application extends WebMvcConfigurerAdapter implements InitializingBean{

  private final Logger LOGGER = LoggerFactory.getLogger(Application.class);


  /**
   * Used for overwriting properties if cloud foundry environment is used
   */
  @Override
  public void afterPropertiesSet() throws Exception {

  }

  @Bean
  public NormalizationService normalizationService(NormalizationConfig config) {

    LOGGER.info("Using LanguageVocabulary '{}' and ConfidenceLevel '{}'", config.getNormalizationVocabulary(), config.getNormalizationConfidence());

    LanguageNormalizer languageNorm = new LanguageNormalizer(LanguagesVocabulary.valueOf(config.getNormalizationVocabulary()), config.getNormalizationConfidence());
    languageNorm.setOperations(SupportedOperations.ALL);

    TrimAndEmptyValueCleaning spacesCleaner=new TrimAndEmptyValueCleaning();
    DuplicateStatementCleaning dupStatementsCleaner=new DuplicateStatementCleaning();
    MarkupTagsCleaning markupStatementsCleaner=new MarkupTagsCleaning();

    ChainedNormalization chainedNormalizer = new ChainedNormalization(spacesCleaner.toEdmRecordNormalizer(), markupStatementsCleaner.toEdmRecordNormalizer(), dupStatementsCleaner, languageNorm.toEdmRecordNormalizer());

    return new NormalizationService(chainedNormalizer);

  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("swagger-ui.html")
        .addResourceLocations("classpath:/META-INF/resources/");
    registry.addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/");
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addRedirectViewController("/", "swagger-ui.html");
  }

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
    ApiInfo apiInfo = new ApiInfo(
        "EDM Record Normalization plugin for Metis",
        "Applies a preset list of data cleaning and normalization operations to metadata records in EDM.",
        "0.2",
        "API TOS",
        "development@europeana.eu",
        "EUPL Licence v1.1",
        ""
    );
    return apiInfo;
  }


}
