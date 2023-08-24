package metis.common.config.properties.solr;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Class using {@link ConfigurationProperties} loading.
 */
@ConfigurationProperties(prefix = "solr")
public class SolrZookeeperProperties {

  private String[] hosts;

  @NestedConfigurationProperty
  //Keep the name as is(zookeeper) for the spring mapping.
  private ZookeeperProperties zookeeper;

  public String[] getHosts() {
    return hosts == null ? null : hosts.clone();
  }

  public void setHosts(String[] hosts) {
    this.hosts = hosts == null ? null : hosts.clone();
  }

  public ZookeeperProperties getZookeeper() {
    return zookeeper;
  }

  public void setZookeeper(ZookeeperProperties zookeeper) {
    this.zookeeper = zookeeper;
  }
}
