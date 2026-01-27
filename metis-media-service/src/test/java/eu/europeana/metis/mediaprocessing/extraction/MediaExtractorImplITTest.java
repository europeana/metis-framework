package eu.europeana.metis.mediaprocessing.extraction;

import static eu.europeana.metis.mediaprocessing.model.RdfResourceKind.IIIF;
import static eu.europeana.metis.mediaprocessing.model.RdfResourceKind.STANDARD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import eu.europeana.metis.mediaprocessing.MediaExtractor;
import eu.europeana.metis.mediaprocessing.MediaProcessorFactory;
import eu.europeana.metis.mediaprocessing.RdfConverterFactory;
import eu.europeana.metis.mediaprocessing.RdfDeserializer;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.RdfResourceKind;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MediaExtractorImplITTest {

  private RdfDeserializer rdfDeserializer;
  private MediaExtractor mediaExtractor;

  private static Stream<Arguments> testMediaExtraction_ResourceAndMimeType() {
    return Stream.of(
        Arguments.of("__files/rdf_with_iiif_sample.xml",
            Set.of(Pair.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/full/0/default.jpg", IIIF),
                Pair.of("https://stacks.stanford.edu/file/zw031pj2507/zw031pj2507_0002.xml", STANDARD),
                Pair.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0002/full/full/0/default.jpg", IIIF),
                Pair.of("https://purl.stanford.edu/zw031pj2507", STANDARD)),
            Set.of("application/xml", "text/html", "image/jpeg")),
        Arguments.of("__files/rdf_with_3d_gltf.xml",
            Set.of(Pair.of("http://cmcassociates.co.uk/Skara_Brae/landing/sb_h10_lidar.html", STANDARD),
                Pair.of("https://raw.githubusercontent.com/europeana/metis-framework/refs/heads/develop/metis-media-service/src/test/resources/__files/3d/cube.gltf", STANDARD)),
            Set.of("application/xhtml+xml", "model/gltf+json")),
        Arguments.of("__files/rdf_with_3d_ply.xml",
            Set.of(Pair.of("http://cmcassociates.co.uk/Skara_Brae/landing/sb_h10_lidar.html", STANDARD),
                Pair.of("https://raw.githubusercontent.com/europeana/metis-framework/refs/heads/develop/metis-media-service/src/test/resources/__files/3d/cube-binary.ply", STANDARD)),
            Set.of("application/xhtml+xml", "model/ply"))
    );
  }

  @Disabled("Enable media-processing tools (image-magick, ghostscript, etc) first on metis-actions for the build.")
  @ParameterizedTest(name = "{index} => record={0}, expected-resource{1}, expected-mimetype={2}")
  @MethodSource
  void testMediaExtraction_ResourceAndMimeType(String resourcePath,
      Set<Pair<String, RdfResourceKind>> expectedResource,
      Set<String> expectedMimeTypes) throws Exception {
    // Given
    rdfDeserializer = new RdfConverterFactory().createRdfDeserializer();
    mediaExtractor = new MediaProcessorFactory().createMediaExtractor();
    InputStream inputRdf = getClass().getClassLoader().getResourceAsStream(resourcePath);
    List<RdfResourceEntry> resourceEntryList = rdfDeserializer.getRemainingResourcesForMediaExtraction(inputRdf);
    Set<String> actualMimeTypes = new HashSet<>();
    Set<Pair<String, RdfResourceKind>> actualResource = new HashSet<>();

    for (RdfResourceEntry resourceEntry : resourceEntryList) {
      // When
      ResourceExtractionResult extractionResult = mediaExtractor.performMediaExtraction(resourceEntry, true);

      // Then
      assertNotNull(extractionResult.getMetadata());
      assertEquals(resourceEntry.getResourceUrl(), extractionResult.getMetadata().getResourceUrl());
      actualMimeTypes.add(extractionResult.getMetadata().getMimeType());
      actualResource.add(Pair.of(extractionResult.getMetadata().getResourceUrl(), resourceEntry.getResourceKind()));
    }
    assertEquals(expectedMimeTypes, actualMimeTypes);
    assertEquals(expectedResource, actualResource);
  }
}
