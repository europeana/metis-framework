package eu.europeana.validation.rest.config;

import eu.europeana.validation.service.ValidationServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

/**
 * Reads configuration file and provides methods to retrieve properties.
 */
@Service
@PropertySource("classpath:validation.properties")
public class ValidationServiceConfigImpl implements ValidationServiceConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(ValidationServiceConfigImpl.class);
  public static final int DEFAULT_THREADS_COUNT = 10;

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
      return DEFAULT_THREADS_COUNT;
    }
  }
}
