package eu.europeana.metis.dereference.rest.config;

import eu.europeana.metis.dereference.rest.config.properties.MetisDereferenceConfigurationProperties;
import eu.europeana.metis.dereference.service.dao.ProcessedEntityDao;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Configuration class for scheduling tasks and defining beans related to scheduling intervals.
 */
@Configuration
@EnableConfigurationProperties({MetisDereferenceConfigurationProperties.class})
public class ScheduledConfig {

  /**
   * Retrieves the purge all frequency from the provided MetisDereferenceConfigurationProperties.
   *
   * @param metisDereferenceConfigurationProperties Configuration properties.
   * @return The periodic failsafe check interval in milliseconds.
   */
  @Bean
  public String getPurgeAllFrequency(MetisDereferenceConfigurationProperties metisDereferenceConfigurationProperties) {
    return metisDereferenceConfigurationProperties.purgeAllFrequency();
  }

  /**
   * Retrieves the purge empty xml frequency from the provided MetisDereferenceConfigurationProperties.
   *
   * @param metisDereferenceConfigurationProperties Configuration properties.
   * @return The periodic failsafe check interval in milliseconds.
   */
  @Bean
  public String getPurgeEmptyXmlFrequency(MetisDereferenceConfigurationProperties metisDereferenceConfigurationProperties) {
    return metisDereferenceConfigurationProperties.purgeEmptyXmlFrequency();
  }

  /**
   * Inner class containing methods with @Scheduled annotation.
   * <p>
   * This is a helper class so that we can use shorter references on the SPEL on the @Scheduled annotation with values from
   * {@link ScheduledConfig}.
   */
  @Configuration
  @EnableScheduling
  static class ScheduledTasks {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ProcessedEntityDao processedEntityDao;

    @Autowired
    public ScheduledTasks(ProcessedEntityDao processedEntityDao) {
      this.processedEntityDao = processedEntityDao;
    }

    /**
     * Empty Cache with XML entries null or empty. This will remove entries with null or empty XML in the cache (Redis). If the same
     * redis instance/cluster is used for multiple services then the cache for other services is cleared as well. This task is
     * scheduled by a cron expression.
     */
    @Scheduled(cron = "#{@getPurgeEmptyXmlFrequency}")
    public void dereferenceCacheNullOrEmpty() {
      processedEntityDao.purgeByNullOrEmptyXml();
      LOGGER.debug("Processed entity dao purgeByNullOrEmptyXml finished.");
    }

    /**
     * Empty Cache. This will remove ALL entries in the cache (Redis). If the same redis instance/cluster is used for multiple
     * services then the cache for other services is cleared as well. This task is scheduled by a cron expression.
     */
    @Scheduled(cron = "#{@getPurgeAllFrequency}")
    public void dereferenceCachePurgeAll() {
      processedEntityDao.purgeAll();
      LOGGER.debug("Processed entity dao purgeAll finished.");
    }
  }
}
