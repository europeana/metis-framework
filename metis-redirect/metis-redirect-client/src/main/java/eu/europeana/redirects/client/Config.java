package eu.europeana.redirects.client;

import java.io.IOException;
import java.util.Properties;

/**
 * Configuration class for the Redirects REST client
 * Created by ymamakis on 1/15/16.
 */
public class Config {
    private static String redirectsPath;

    public Config(){
        Properties props = new Properties();
        try {
            props.load(Config.class.getClassLoader().getResourceAsStream("redirect.properties"));
            redirectsPath= props.getProperty("redirect.path");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  String getRedirectsPath() {

        return redirectsPath;
    }
}
