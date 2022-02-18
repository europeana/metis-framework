package eu.europeana.metis.dereference.vocimport.utils;

import eu.europeana.metis.dereference.vocimport.exception.VocabularyImportException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Utils class focused on validating values used in dereferencing
 */
public class DereferenceValidationUtils {

    private static final String PROPERTY_FIELD_NAME = "valid.directories.values";
    private final List<String> validDirectoriesAsList;

    /**
     * Constructor of Utils class
     */
    public DereferenceValidationUtils(String[] validDirectories){
        validDirectoriesAsList = Arrays.asList(validDirectories);
    }

    // This constructor is used for testing purposes
    DereferenceValidationUtils(String dereferencingPropertiesLocation) throws VocabularyImportException {
        validDirectoriesAsList = Arrays.asList(readProperty(dereferencingPropertiesLocation));
    }

    /**
     * Method that verifies if the given directory is valid
     * @param directoryToEvaluate The directory to evaluate as a string
     * @return True if is valid; False otherwise
     */
    public boolean isDirectoryValid(String directoryToEvaluate) {
        boolean result;

        if (CollectionUtils.isEmpty(validDirectoriesAsList)) {
            result = false;
        } else {
            result = validDirectoriesAsList.stream().anyMatch(directoryToEvaluate::startsWith);
        }
        return result;
    }

    //This method is used for testing purposes
    private String[] readProperty(String dereferencingPropertiesLocation) throws VocabularyImportException {
        Properties properties = new Properties();
        try (InputStream propertiesFile = new FileInputStream(dereferencingPropertiesLocation)) {
            properties.load(propertiesFile);
        } catch (IOException e){
            throw new VocabularyImportException("A problem occurred while reading properties file", e);
        }
        String propertiesWithoutSpaces = properties.getProperty(PROPERTY_FIELD_NAME).replaceAll("\\s+", "");
        return StringUtils.isBlank(propertiesWithoutSpaces) ? new String[0] : propertiesWithoutSpaces.split(",");
    }

}
