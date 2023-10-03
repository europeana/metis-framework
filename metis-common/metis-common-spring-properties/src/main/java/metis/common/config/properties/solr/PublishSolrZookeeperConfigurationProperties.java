package metis.common.config.properties.solr;


import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Class using {@link ConfigurationProperties} loading.
 */
@ConfigurationProperties(prefix = "publish.solr")
public class PublishSolrZookeeperConfigurationProperties extends SolrZookeeperConfigurationProperties {

}
