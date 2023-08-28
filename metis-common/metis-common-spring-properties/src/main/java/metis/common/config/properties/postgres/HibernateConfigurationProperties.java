package metis.common.config.properties.postgres;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Class using {@link ConfigurationProperties} loading.
 */
@ConfigurationProperties(prefix = "hibernate")
public class HibernateConfigurationProperties {

  private String dialect;
  private Connection connection;
  private C3p0 c3p0;
  private Hbm2ddl hbm2ddl;

  public String getDialect() {
    return dialect;
  }

  public void setDialect(String dialect) {
    this.dialect = dialect;
  }

  public Connection getConnection() {
    return connection;
  }

  public void setConnection(Connection connection) {
    this.connection = connection;
  }

  public C3p0 getC3p0() {
    return c3p0;
  }

  public void setC3p0(C3p0 c3p0) {
    this.c3p0 = c3p0;
  }

  public Hbm2ddl getHbm2ddl() {
    return hbm2ddl;
  }

  public void setHbm2ddl(Hbm2ddl hbm2ddl) {
    this.hbm2ddl = hbm2ddl;
  }

  public static class Connection {

    private String driverClass;
    private String url;
    private String username;
    private String password;

    public void setDriverClass(String driverClass) {
      this.driverClass = driverClass;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public String getDriverClass() {
      return driverClass;
    }

    public String getUrl() {
      return url;
    }

    public String getUsername() {
      return username;
    }

    public String getPassword() {
      return password;
    }
  }

  public static class C3p0 {

    private String minSize;
    private String maxSize;
    private String timeout;
    private String maxStatements;

    public String getMinSize() {
      return minSize;
    }

    public void setMinSize(String minSize) {
      this.minSize = minSize;
    }

    public String getMaxSize() {
      return maxSize;
    }

    public void setMaxSize(String maxSize) {
      this.maxSize = maxSize;
    }

    public String getTimeout() {
      return timeout;
    }

    public void setTimeout(String timeout) {
      this.timeout = timeout;
    }

    public String getMaxStatements() {
      return maxStatements;
    }

    public void setMaxStatements(String maxStatements) {
      this.maxStatements = maxStatements;
    }
  }

  public static class Hbm2ddl {

    private String auto;

    public String getAuto() {
      return auto;
    }

    public void setAuto(String auto) {
      this.auto = auto;
    }
  }
}

