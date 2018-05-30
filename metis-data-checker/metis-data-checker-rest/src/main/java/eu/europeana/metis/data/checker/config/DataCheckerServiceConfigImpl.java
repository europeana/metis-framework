package eu.europeana.metis.data.checker.config;

import eu.europeana.metis.data.checker.service.DataCheckerServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

/**
 * Created by erikkonijnenburg on 06/07/2017.
 */
@Service()
@PropertySource("classpath:data.checker.properties")
public class DataCheckerServiceConfigImpl implements DataCheckerServiceConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataCheckerServiceConfigImpl.class);
  private static final int DEFAULT_EXECUTOR_THREAD_COUNT = 10;

  @Value("${data.checker.portal.url}")
  private String dataCheckerPortalUrl;
  @Value("${data.checker.executor.threadCount:10}")
  private String executorThreadCount;

  @Override
  public String getDataCheckerUrl() {
    return dataCheckerPortalUrl;
  }

  @Override
  public int getThreadCount() {
    try {
      int value = Integer.parseInt(executorThreadCount);
      LOGGER.info(
          "Using data.checker.executor.threadCount with value {}", value);
      return value;
    } catch (NumberFormatException ex) {
      LOGGER.warn(
          "Failed to parse data.checker.executor.threadCount with value '{}'. Taking 10 as default",
          executorThreadCount);
      return DEFAULT_EXECUTOR_THREAD_COUNT;
    }
  }
}
