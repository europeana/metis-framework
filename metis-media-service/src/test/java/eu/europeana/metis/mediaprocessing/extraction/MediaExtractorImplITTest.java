package eu.europeana.metis.mediaprocessing.extraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import eu.europeana.metis.mediaprocessing.MediaExtractor;
import eu.europeana.metis.mediaprocessing.MediaProcessorFactory;
import eu.europeana.metis.mediaprocessing.RdfConverterFactory;
import eu.europeana.metis.mediaprocessing.RdfDeserializer;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MediaExtractorImplITTest {

  private RdfDeserializer rdfDeserializer;
  private MediaExtractor mediaExtractor;

  @Disabled("enable imagemagick first on metis-actions for the build.")
  @Test
  void testIIFExtraction() throws Exception {

    rdfDeserializer = new RdfConverterFactory().createRdfDeserializer();
    mediaExtractor = new MediaProcessorFactory().createMediaExtractor();
    InputStream inputRdf = getClass().getClassLoader().getResourceAsStream("__files/rdf_with_iiif_sample.xml");
    List<RdfResourceEntry> resourceEntryList = rdfDeserializer.getRemainingResourcesForMediaExtraction(inputRdf);
    for (RdfResourceEntry resourceEntry : resourceEntryList) {

      ResourceExtractionResult extractionResult = mediaExtractor.performMediaExtraction(resourceEntry, true);
      assertNotNull(extractionResult.getMetadata());
    }
  }

  @Disabled("enable imagemagick first on metis-actions for the build.")
  @ParameterizedTest(name = "{index} => record={0}, expected={1}")
  @MethodSource
  void testCube3dExtraction_MimeType(String resourcePath, Set<String> expectedMimeTypes) throws Exception {
    rdfDeserializer = new RdfConverterFactory().createRdfDeserializer();
    mediaExtractor = new MediaProcessorFactory().createMediaExtractor();
    InputStream inputRdf = getClass().getClassLoader().getResourceAsStream(resourcePath);
    List<RdfResourceEntry> resourceEntryList = rdfDeserializer.getRemainingResourcesForMediaExtraction(inputRdf);
    Set<String> actualMimeTypes = HashSet.newHashSet(2);
    for (RdfResourceEntry resourceEntry : resourceEntryList) {
      ResourceExtractionResult extractionResult = mediaExtractor.performMediaExtraction(resourceEntry, true);
      assertNotNull(extractionResult.getMetadata());
      actualMimeTypes.add(extractionResult.getMetadata().getMimeType());
    }
    assertEquals(expectedMimeTypes, actualMimeTypes);
  }

  private static Stream<Arguments> testCube3dExtraction_MimeType() {
    return Stream.of(Arguments.of("__files/record-MET-6921.xml", Set.of("application/xhtml+xml", "model/gltf+json")),
        Arguments.of("__files/record-MET-6991.xml", Set.of("application/xhtml+xml", "model/ply")));
  }
}
