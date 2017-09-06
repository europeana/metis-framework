package eu.europeana.normalization.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

/**
 * Created by erikkonijnenburg on 06/07/2017.
 */
@Service()
@PropertySource("classpath:normalization.properties")

public class NormalizationConfigImpl implements NormalizationConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(NormalizationConfig.class);

  @Value("${normalization.language.target.vocabulary}")
  private String normalizationVocabulary;
  @Value("${normalization.language.target.confidence}")
  private Float normalizationConfidence;

  @Override
  public String getNormalizationVocabulary() {
    return normalizationVocabulary;
  }

  @Override
  public Float getNormalizationConfidence() {
    return normalizationConfidence;
  }

}
