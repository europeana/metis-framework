package eu.europeana.metis.dereference.vocimport.utils;

import eu.europeana.metis.dereference.vocimport.exception.VocabularyImportException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DereferenceValidationUtilsTest {


    private DereferenceValidationUtils dereferenceValidationUtils;

    @BeforeEach
    public void setUp() throws VocabularyImportException {
        dereferenceValidationUtils = new DereferenceValidationUtils(
                "src/test/resources/dereferencing.properties");
    }

    @Test
    public void isDirectoryValid_expectSuccess(){
        assertTrue(dereferenceValidationUtils.isDirectoryValid("https://validdirectory/test/call"));
    }

    @Test
    public void isDirectoryValid_expectFail(){
        assertFalse(dereferenceValidationUtils.isDirectoryValid("http://wrongdirectory/test/call"));
    }

}
