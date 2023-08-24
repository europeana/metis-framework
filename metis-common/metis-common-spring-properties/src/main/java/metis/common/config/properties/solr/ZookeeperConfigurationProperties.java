package metis.common.config.properties.solr;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Class using {@link ConfigurationProperties} loading.
 */
@ConfigurationProperties(prefix = "zookeeper")
public class ZookeeperConfigurationProperties {

    private String[] hosts;
    private int[] ports;
    private String chroot;
    private String defaultCollection;

    public void setHosts(String[] hosts) {
        this.hosts = hosts == null ? null : hosts.clone();
    }

    public void setPorts(int[] ports) {
        this.ports = ports == null ? null : ports.clone();
    }

    public void setChroot(String chroot) {
        this.chroot = chroot;
    }

    public void setDefaultCollection(String defaultCollection) {
        this.defaultCollection = defaultCollection;
    }

    public String[] getHosts() {
        return hosts == null ? null : hosts.clone();
    }

    public int[] getPorts() {
        return ports == null ? null : ports.clone();
    }

    public String getChroot() {
        return chroot;
    }

    public String getDefaultCollection() {
        return defaultCollection;
    }
}
