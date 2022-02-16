package eu.europeana.metis.dereference.vocimport.utils;

import eu.europeana.metis.dereference.vocimport.exception.VocabularyImportException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DereferenceValidationUtilsTest {

    @Test
    public void isDirectoryValid_withValidValues_expectSuccess() throws VocabularyImportException {
        DereferenceValidationUtils dereferenceValidationUtils = new DereferenceValidationUtils(
                "src/test/resources/dereferencing-with-valid-directories.properties");
        assertTrue(dereferenceValidationUtils.isDirectoryValid("https://validdirectory/test/call"));
    }

    @Test
    public void isDirectoryValid_withoutValidValues_expectFail() throws VocabularyImportException {
        DereferenceValidationUtils dereferenceValidationUtils = new DereferenceValidationUtils(
                "src/test/resources/dereferencing-with-valid-directories.properties");
        assertFalse(dereferenceValidationUtils.isDirectoryValid("http://wrongdirectory/test/call"));
    }

    @Test
    public void isDirectoryValid_withEmptyValues_expectFail() throws VocabularyImportException {
        DereferenceValidationUtils dereferenceValidationUtils = new DereferenceValidationUtils(
                "src/test/resources/dereferencing-with-empty-values.properties");
        assertFalse(dereferenceValidationUtils.isDirectoryValid("https://randomdirectory"));
    }

    @Test
    public void isDirectoryValid_withStarValue_expectSuccess() throws VocabularyImportException {
        DereferenceValidationUtils dereferenceValidationUtils = new DereferenceValidationUtils(
                "src/test/resources/dereferencing-with-star-value.properties");
        assertTrue(dereferenceValidationUtils.isDirectoryValid("https://randomdirectory"));
    }

}
