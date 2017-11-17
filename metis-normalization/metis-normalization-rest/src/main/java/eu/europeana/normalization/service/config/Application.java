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
package eu.europeana.normalization.service.config;


import eu.europeana.normalization.common.cleaning.DuplicateStatementCleaning;
import eu.europeana.normalization.common.cleaning.MarkupTagsCleaning;
import eu.europeana.normalization.common.cleaning.TrimAndEmptyValueCleaning;
import eu.europeana.normalization.common.language.LanguageNormalizer;
import eu.europeana.normalization.common.language.LanguageNormalizer.SupportedOperations;
import eu.europeana.normalization.common.language.LanguagesVocabulary;
import eu.europeana.normalization.common.normalizers.ChainedNormalization;
import eu.europeana.normalization.service.NormalizationService;
import eu.europeana.normalization.service.NormalizationServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Configuration file for Spring MVC
 */
@ComponentScan(basePackages = {"eu.europeana.normalization"})
@EnableWebMvc
@Configuration
public class Application extends WebMvcConfigurerAdapter implements InitializingBean {

  private final Logger LOGGER = LoggerFactory.getLogger(Application.class);

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  /**
   * Used for overwriting properties if cloud foundry environment is used
   */
  @Override
  public void afterPropertiesSet() throws Exception {

  }

  @Bean
  public NormalizationService normalizationService(NormalizationConfig config) {

    LOGGER.info("Using LanguageVocabulary '{}' and ConfidenceLevel '{}'",
        config.getNormalizationVocabulary(), config.getNormalizationConfidence());

    LanguageNormalizer languageNorm = new LanguageNormalizer(
        LanguagesVocabulary.valueOf(config.getNormalizationVocabulary()),
        config.getNormalizationConfidence());
    languageNorm.setOperations(SupportedOperations.ALL);

    TrimAndEmptyValueCleaning spacesCleaner = new TrimAndEmptyValueCleaning();
    DuplicateStatementCleaning dupStatementsCleaner = new DuplicateStatementCleaning();
    MarkupTagsCleaning markupStatementsCleaner = new MarkupTagsCleaning();

    ChainedNormalization chainedNormalizer = new ChainedNormalization(
        spacesCleaner.toEdmRecordNormalizer(),
        markupStatementsCleaner.toEdmRecordNormalizer(),
        dupStatementsCleaner,
        languageNorm.toEdmRecordNormalizer());

    return new NormalizationServiceImpl(chainedNormalizer);

  }
}
