package eu.europeana.metis.dereference.vocimport.utils;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;


public class DereferenceValidationUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DereferenceValidationUtils.class);

    private final List<String> validUriValues;

    public DereferenceValidationUtils() {
        validUriValues = Arrays.asList(readProperty());
    }

    public boolean isDirectoryValid(String directoryToEvaluate){
        if(CollectionUtils.isEmpty(validUriValues)){
            return false;
        } else if (validUriValues.size() == 1 && StringUtils.equals(validUriValues.get(0), "*")){
            return true;
        } else {
            return validUriValues.stream().anyMatch(directoryToEvaluate::contains);
        }
    }

    private String[] readProperty() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("metis-dereference/metis-dereference-rest/src/main/resources/dereferencing.properties"));
        } catch (IOException e) {
            LOGGER.error("Error while reading valid directories");
            //TODO: throw an exception?
        }
        return properties.getProperty("valid.uri.values").split(", ");
    }

}
