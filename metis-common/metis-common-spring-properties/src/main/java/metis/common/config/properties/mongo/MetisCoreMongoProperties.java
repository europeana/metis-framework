package metis.common.config.properties.mongo;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "metis-core.mongo")
public class MetisCoreMongoProperties extends MongoProperties {

}
