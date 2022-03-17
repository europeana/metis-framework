//package eu.europeana.metis.dereference.vocimport;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.nio.file.Paths;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//class VocabularyCollectionImporterFactoryTest {
//
//  private VocabularyCollectionImporterFactory factory;
//
//  @BeforeEach
//  void setUp() {
//    factory = new VocabularyCollectionImporterFactory();
//  }
//
//  @Test
//  void createImporterWithUri_expectSuccess() throws URISyntaxException {
//    VocabularyCollectionImporter result = factory.createImporter(new URI("https://validprefix/test/call"));
//    assertEquals("https://validprefix/test/call", result.getDirectoryLocation().toString());
//  }
//
//  @Test
//  void createImporterWithPath_expectSuccess() {
//    VocabularyCollectionImporter result = factory.createImporter(Paths.get("/path/test/random"));
//    assertEquals("/path/test/random", result.getDirectoryLocation().toString());
//  }
//
//}
