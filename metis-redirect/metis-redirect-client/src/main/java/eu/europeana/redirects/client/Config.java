package eu.europeana.redirects.client;

import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration class for the Redirects REST client
 * Created by ymamakis on 1/15/16.
 */
public class Config {

  private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);
  private String redirectsPath;

  public Config() throws IOException {
    Properties props = new Properties();
    props.load(Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("redirect.properties"));
    redirectsPath = props.getProperty("redirect.path");
  }

  public String getRedirectsPath() {
    return redirectsPath;
  }
}
