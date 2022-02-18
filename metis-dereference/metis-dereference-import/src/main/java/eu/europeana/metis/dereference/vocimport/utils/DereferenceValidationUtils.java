package eu.europeana.metis.dereference.vocimport.utils;

import org.apache.commons.collections.CollectionUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Utils class focused on validating values used in dereferencing
 */
public class DereferenceValidationUtils {

    private static final String PROPERTY_FIELD_NAME = "valid.directories.values";
    private final List<String> validDirectoriesAsList;

    /**
     * Constructor of Utils class
     *
     * @param validDirectories The list of directories that are valid.
     */
    public DereferenceValidationUtils(String[] validDirectories){
        validDirectoriesAsList = Arrays.asList(validDirectories);
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

}
