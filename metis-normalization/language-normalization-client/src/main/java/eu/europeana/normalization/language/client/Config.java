package eu.europeana.normalization.language.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Config of the client
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 16/05/2016
 */
public class Config {

    private static String normalizationLanguageServiceUrl;

    public Config() {
        Properties props = new Properties();
        try {
            props.load(Config.class.getResourceAsStream("normalization-language-service.properties"));
            normalizationLanguageServiceUrl = props.getProperty("normalization.language.service.url");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNormalizationLanguageServiceUrl() {

        return normalizationLanguageServiceUrl;
    }

}
