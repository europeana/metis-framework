package eu.europeana.metis.dereference.vocimport.utils;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;


public class DereferenceValidationUtils {

    private static final String PROPERTY_FIELD_NAME = "valid.directories.values";
    private static final String DEREFERENCING_PROPERTIES_LOCATION =
            "metis-dereference/metis-dereference-rest/src/main/resources/dereferencing.properties";

    private final List<String> validDirectoriesValues;

    public DereferenceValidationUtils() throws IOException {
        validDirectoriesValues = Arrays.asList(readProperty());
    }

    public boolean isDirectoryValid(String directoryToEvaluate) {
        if (CollectionUtils.isEmpty(validDirectoriesValues)) {
            return false;
        } else if (validDirectoriesValues.size() == 1 && StringUtils.equals(validDirectoriesValues.get(0), "*")) {
            return true;
        } else {
            return validDirectoriesValues.stream().anyMatch(directoryToEvaluate::contains);
        }
    }

    private String[] readProperty() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(DEREFERENCING_PROPERTIES_LOCATION));
        String propertiesWithoutSpaces = properties.getProperty(PROPERTY_FIELD_NAME).replaceAll("\\s+", "");
        return propertiesWithoutSpaces.split(",");
    }

}
