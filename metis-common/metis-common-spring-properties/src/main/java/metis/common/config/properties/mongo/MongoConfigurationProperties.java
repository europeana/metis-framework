package metis.common.config.properties.mongo;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Class using {@link ConfigurationProperties} loading.
 */
@ConfigurationProperties(prefix = "mongo")
public class MongoConfigurationProperties {

    private String[] hosts;
    private int[] ports;
    private String username;
    private String password;
    private String authenticationDatabase;
    private String database;
    private boolean enableSsl;
    private String applicationName;

    public void setHosts(String[] hosts) {
        this.hosts = hosts == null ? null : hosts.clone();
    }

    public void setPorts(int[] ports) {
        this.ports = ports == null ? null : ports.clone();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAuthenticationDatabase(String authenticationDatabase) {
        this.authenticationDatabase = authenticationDatabase;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setEnableSsl(boolean enableSsl) {
        this.enableSsl = enableSsl;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String[] getHosts() {
        return hosts == null ? null : hosts.clone();
    }

    public int[] getPorts() {
        return ports == null ? null : ports.clone();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getAuthenticationDatabase() {
        return authenticationDatabase;
    }

    public String getDatabase() {
        return database;
    }

    public boolean isEnableSsl() {
        return enableSsl;
    }

    public String getApplicationName() {
        return applicationName;
    }
}
