package metis.common.config.properties.mongo;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Class using {@link ConfigurationProperties} loading.
 */
@ConfigurationProperties(prefix = "metis-core.mongo")
public class MetisCoreMongoConfigurationProperties extends MongoConfigurationProperties {

}
