package eu.europeana.metis.mediaprocessing.extraction;

import static eu.europeana.metis.mediaprocessing.model.RdfResourceKind.IIIF;
import static eu.europeana.metis.mediaprocessing.model.RdfResourceKind.STANDARD;
import static eu.europeana.metis.mediaprocessing.model.UrlType.HAS_VIEW;
import static eu.europeana.metis.mediaprocessing.model.UrlType.IS_SHOWN_AT;
import static eu.europeana.metis.mediaprocessing.model.UrlType.IS_SHOWN_BY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import eu.europeana.metis.mediaprocessing.MediaExtractor;
import eu.europeana.metis.mediaprocessing.MediaProcessorFactory;
import eu.europeana.metis.mediaprocessing.RdfConverterFactory;
import eu.europeana.metis.mediaprocessing.RdfDeserializer;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.RdfResourceKind;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MediaExtractorImplITTest {

  private final RdfDeserializer rdfDeserializer;
  private final MediaExtractor mediaExtractor;

  private MediaExtractorImplITTest() throws MediaProcessorException {
    rdfDeserializer = new RdfConverterFactory().createRdfDeserializer();
    mediaExtractor = new MediaProcessorFactory().createMediaExtractor();
  }

  private static Stream<Arguments> testMediaExtraction_ResourceAndMimeType() {
    return Stream.of(
        Arguments.of("__files/rdf_with_iiif_sample.xml",
            Set.of(
                new ExtractionParameters(
                    "https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/full/0/default.jpg",
                    "image/jpeg", Set.of(IS_SHOWN_BY), IIIF),
                new ExtractionParameters("https://stacks.stanford.edu/file/zw031pj2507/zw031pj2507_0002.xml",
                    "application/xml", Set.of(HAS_VIEW), STANDARD),
                new ExtractionParameters(
                    "https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0002/full/full/0/default.jpg",
                    "image/jpeg", Set.of(HAS_VIEW), IIIF),
                new ExtractionParameters("https://purl.stanford.edu/zw031pj2507", "text/html", Set.of(IS_SHOWN_AT), STANDARD)
            )
        ),
        Arguments.of("__files/rdf_with_3d_gltf.xml",
            Set.of(
                new ExtractionParameters("http://cmcassociates.co.uk/Skara_Brae/landing/sb_h10_lidar.html",
                    "application/xhtml+xml", Set.of(IS_SHOWN_BY), STANDARD),
                new ExtractionParameters(
                    "https://raw.githubusercontent.com/europeana/metis-framework/refs/heads/develop/metis-media-service/src/test/resources/__files/3d/cube.gltf",
                    "model/gltf+json", Set.of(IS_SHOWN_AT), STANDARD)
            )
        ),
        Arguments.of("__files/rdf_with_3d_ply.xml",
            Set.of(
                new ExtractionParameters("http://cmcassociates.co.uk/Skara_Brae/landing/sb_h10_lidar.html",
                    "application/xhtml+xml", Set.of(IS_SHOWN_BY), STANDARD),
                new ExtractionParameters(
                    "https://raw.githubusercontent.com/europeana/metis-framework/refs/heads/develop/metis-media-service/src/test/resources/__files/3d/cube-binary.ply",
                    "model/ply", Set.of(IS_SHOWN_AT), STANDARD)
            )
        )
    );
  }

  /**
   * The type Extraction parameters.
   */
  record ExtractionParameters(String resourceURL, String mimeType, Set<UrlType> urlType, RdfResourceKind resourceKind) {
    //used internally for testing purposes
  }

  @Disabled("Enable media-processing tools (image-magick, ghostscript, etc) first on metis-actions for the build.")
  @ParameterizedTest(name = "{index} => record={0}, expected-resource{1}")
  @MethodSource
  void testMediaExtraction_ResourceAndMimeType(String resourcePath,
      Set<ExtractionParameters> expectedExtractionParameters) throws Exception {
    // Given
    InputStream inputRdf = getClass().getClassLoader().getResourceAsStream(resourcePath);
    List<RdfResourceEntry> resourceEntryList = rdfDeserializer.getRemainingResourcesForMediaExtraction(inputRdf);
    Set<ExtractionParameters> actualExtractionParameters = new HashSet<>();
    for (RdfResourceEntry resourceEntry : resourceEntryList) {
      // When
      ResourceExtractionResult extractionResult = mediaExtractor.performMediaExtraction(resourceEntry, true);

      // Then
      assertNotNull(extractionResult.getMetadata());
      assertEquals(resourceEntry.getResourceUrl(), extractionResult.getMetadata().getResourceUrl());
      actualExtractionParameters.add(new ExtractionParameters(extractionResult.getMetadata().getResourceUrl(),
          extractionResult.getMetadata().getMimeType(), resourceEntry.getUrlTypes(), resourceEntry.getResourceKind()));
    }
    assertEquals(expectedExtractionParameters, actualExtractionParameters);
  }
}
