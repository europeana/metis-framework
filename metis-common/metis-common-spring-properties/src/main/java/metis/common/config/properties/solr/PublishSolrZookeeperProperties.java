package metis.common.config.properties.solr;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "publish.solr")
public class PublishSolrZookeeperProperties extends SolrZookeeperProperties {

}
