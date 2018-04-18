package eu.europeana.metis.preview.config;

import eu.europeana.metis.preview.service.PreviewServiceConfig;
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
public class PreviewServiceConfigImpl implements PreviewServiceConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(PreviewServiceConfigImpl.class);
  private static final int DEFAULT_EXECUTOR_THREAD_COUNT = 10;

  @Value("${preview.portal.url}")
  private String previewPortalUrl;
  @Value("${preview.executor.threadCount:10}")
  private String executorThreadCount;

  @Override
  public String getPreviewUrl() {
    return previewPortalUrl;
  }

  @Override
  public int getThreadCount() {
    try {
      int value = Integer.parseInt(executorThreadCount);
      LOGGER.info(
          "Using preview.executor.threadCount with value {}", value);
      return value;
    } catch (NumberFormatException ex) {
      LOGGER.warn(
          "Failed to parse preview.executor.threadCount with value '{}'. Taking 10 as default",
          executorThreadCount);
      return DEFAULT_EXECUTOR_THREAD_COUNT;
    }
  }
}
