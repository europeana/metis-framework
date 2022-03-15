package eu.europeana.metis.dereference.vocimport;

import eu.europeana.metis.dereference.vocimport.exception.VocabularyImportException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VocabularyCollectionImporterFactoryTest {

    private VocabularyCollectionImporterFactory factory;

    @BeforeEach
    void setUp() {
        List<String> validUrlsPrefixes = new ArrayList<>();
        validUrlsPrefixes.add("https://validprefix");
        factory = new VocabularyCollectionImporterFactory(validUrlsPrefixes);
    }

    @Test
    void createImporterWithUri_expectSuccess() throws URISyntaxException, VocabularyImportException {
        VocabularyCollectionImporter result = factory.createImporter(new URI("https://validprefix/test/call"));
        assertEquals("https://validprefix/test/call", result.getDirectoryLocation().toString());
    }

    @Test
    void createImporterWithUri_expectFail() {
        assertThrows(VocabularyImportException.class,
                () -> factory.createImporter(new URI("https://anotherprefix/test/call")));
    }

    @Test
    void createImporterWithPath_expectSuccess() {
        VocabularyCollectionImporter result = factory.createImporter(Paths.get("/path/test/random"));
        assertEquals("/path/test/random", result.getDirectoryLocation().toString());
    }

}
