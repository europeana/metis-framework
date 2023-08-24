package properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zookeeper")
public class ZookeeperProperties {

    private String[] hosts;
    private int[] ports;
    private String chroot;
    private String defaultCollection;

    public void setHosts(String[] hosts) {
        this.hosts = hosts;
    }

    public void setPorts(int[] ports) {
        this.ports = ports;
    }

    public void setChroot(String chroot) {
        this.chroot = chroot;
    }

    public void setDefaultCollection(String defaultCollection) {
        this.defaultCollection = defaultCollection;
    }

    public String[] getHosts() {
        return hosts;
    }

    public int[] getPorts() {
        return ports;
    }

    public String getChroot() {
        return chroot;
    }

    public String getDefaultCollection() {
        return defaultCollection;
    }
}
