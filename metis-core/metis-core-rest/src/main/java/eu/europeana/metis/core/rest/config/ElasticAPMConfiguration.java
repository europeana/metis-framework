package eu.europeana.metis.core.rest.config;

import co.elastic.apm.attach.ElasticApmAttacher;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Elastic apm configuration
 */
@Configuration
@ConfigurationProperties(prefix = "elastic")
@ConditionalOnProperty(value = "elastic.apm.enabled", havingValue = "true")
public class ElasticAPMConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(ElasticAPMConfiguration.class);
  private Map<String, String> apmProperties;

  /**
   * The name of this method has to be as the key in the application.yml elastic: apm: ...
   *
   * @return map of elastic apm agent properties
   */
  public Map<String, String> getApm() {
    return SerializationUtils.clone(new HashMap<>(apmProperties));
  }

  public void setApm(Map<String, String> apmProperties) {
    this.apmProperties = SerializationUtils.clone(new HashMap<>(apmProperties));
  }

  /**
   * Attaches the apm agent to the jvm on {@link PostConstruct}.
   */
  @PostConstruct
  public void attachApmAgent() {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("{}", StringUtils.join(apmProperties));
    }
    ElasticApmAttacher.attach(this.apmProperties);
  }
}
