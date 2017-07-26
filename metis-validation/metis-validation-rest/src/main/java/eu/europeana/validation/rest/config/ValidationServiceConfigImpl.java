package eu.europeana.validation.rest.config;

import eu.europeana.validation.service.ValidationServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

/**
 * Created by erikkonijnenburg on 06/07/2017.
 */
@Service()
@PropertySource("classpath:preview.properties")
public class ValidationServiceConfigImpl implements ValidationServiceConfig {

  private final Logger LOGGER = LoggerFactory.getLogger(ValidationServiceConfig.class);


  @Value("${validation.executor.threadCount:10}")
  private String executorThreadCount;

  @Override
  public int getThreadCount() {
    try {
      int value = Integer.parseInt(executorThreadCount);
      LOGGER.info(
          "Using validation.executor.threadCount with value {}", value);
      return value;
    } catch (NumberFormatException ex) {
      LOGGER.warn(
          "Failed to parse validation.executor.threadCount with value '{}'. Taking 10 as default",
          executorThreadCount);
      return 10;
    }
  }
}
