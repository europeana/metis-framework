package eu.europeana.metis.mediaprocessing.extraction;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static eu.europeana.metis.mediaprocessing.extraction.ImageTestUtils.assertThumbnail;
import static eu.europeana.metis.mediaprocessing.extraction.ImageTestUtils.createImageWithRedLeftHalf;
import static eu.europeana.metis.mediaprocessing.model.RdfResourceKind.IIIF;
import static eu.europeana.metis.mediaprocessing.model.RdfResourceKind.STANDARD;
import static eu.europeana.metis.mediaprocessing.model.UrlType.HAS_VIEW;
import static eu.europeana.metis.mediaprocessing.model.UrlType.IS_SHOWN_AT;
import static eu.europeana.metis.mediaprocessing.model.UrlType.IS_SHOWN_BY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import eu.europeana.metis.mediaprocessing.MediaExtractor;
import eu.europeana.metis.mediaprocessing.MediaProcessorFactory;
import eu.europeana.metis.mediaprocessing.RdfConverterFactory;
import eu.europeana.metis.mediaprocessing.RdfDeserializer;
import eu.europeana.metis.mediaprocessing.model.ImageResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.Media3dResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.RdfResourceKind;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResultImpl;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MediaExtractorImplITTest {

  private static final String FIRST_IIIF_IMAGE = "/image/iiif/zw031pj2507/zw031pj2507_0001";
  private static final String SECOND_IIIF_IMAGE = "/image/iiif/zw031pj2507/zw031pj2507_0002";
  private static final String XML_RESOURCE = "/file/zw031pj2507/zw031pj2507_0002.xml";
  private static final String HTML_RESOURCE = "/zw031pj2507";
  private static final String XHTML_RESOURCE = "/Skara_Brae/landing/sb_h10_lidar.html";

  @RegisterExtension
  static WireMockExtension wireMock = WireMockExtension.newInstance()
                                                       .options(wireMockConfig().dynamicPort())
                                                       .build();

  private final RdfDeserializer rdfDeserializer = new RdfConverterFactory().createRdfDeserializer();

  @BeforeEach
  void stubResources() throws IOException {
    final BufferedImage preview = createImageWithRedLeftHalf(400, 200, false);
    final ByteArrayOutputStream encodedPreview = new ByteArrayOutputStream();
    try {
      assertTrue(ImageIO.write(preview, "jpeg", encodedPreview));
    } finally {
      preview.flush();
    }
    for (String servicePath : List.of(FIRST_IIIF_IMAGE, SECOND_IIIF_IMAGE)) {
      stub(servicePath + "/info.json", "application/json", """
          {
            "@context": "http://iiif.io/api/image/2/context.json",
            "@id": "%s%s",
            "protocol": "http://iiif.io/api/image",
            "width": 800,
            "height": 400,
            "profile": ["http://iiif.io/api/image/2/level2.json"]
          }
          """.formatted(wireMock.baseUrl(), servicePath).getBytes(StandardCharsets.UTF_8));
      stub(servicePath + "/full/!400,400/0/default.jpg", "image/jpeg", encodedPreview.toByteArray());
    }
    stub(XML_RESOURCE, "application/xml", """
        <?xml version="1.0" encoding="UTF-8"?>
        <metadata><title>Local XML resource</title></metadata>
        """.getBytes(StandardCharsets.UTF_8));
    stub(HTML_RESOURCE, "text/html", """
        <!DOCTYPE html><html><head><title>Local HTML resource</title></head><body>Fixture</body></html>
        """.getBytes(StandardCharsets.UTF_8));
    stub(XHTML_RESOURCE, "application/xhtml+xml", """
        <?xml version="1.0" encoding="UTF-8"?>
        <html xmlns="http://www.w3.org/1999/xhtml"><head><title>Local XHTML resource</title></head><body>Fixture</body></html>
        """.getBytes(StandardCharsets.UTF_8));
    stub("/3d/cube.gltf", "model/gltf+json", readFile("models/cube.gltf"));
    stub("/3d/cube-binary.ply", "model/ply", readFile("models/cube-binary.ply"));
  }

  private static Stream<Arguments> testMediaExtraction_ResourceAndMimeType() {
    return Stream.of(
        Arguments.of("rdf/rdf_with_iiif_sample.xml", Set.of(
            new ExtractionParameters(FIRST_IIIF_IMAGE + "/full/full/0/default.jpg", "image/jpeg", Set.of(IS_SHOWN_BY), IIIF),
            new ExtractionParameters(XML_RESOURCE, "application/xml", Set.of(HAS_VIEW), STANDARD),
            new ExtractionParameters(SECOND_IIIF_IMAGE + "/full/full/0/default.jpg", "image/jpeg", Set.of(HAS_VIEW), IIIF),
            new ExtractionParameters(HTML_RESOURCE, "text/html", Set.of(IS_SHOWN_AT), STANDARD))),
        Arguments.of("rdf/rdf_with_3d_gltf.xml", Set.of(
            new ExtractionParameters(XHTML_RESOURCE, "application/xhtml+xml", Set.of(IS_SHOWN_BY), STANDARD),
            new ExtractionParameters("/3d/cube.gltf", "model/gltf+json", Set.of(IS_SHOWN_AT), STANDARD))),
        Arguments.of("rdf/rdf_with_3d_ply.xml", Set.of(
            new ExtractionParameters(XHTML_RESOURCE, "application/xhtml+xml", Set.of(IS_SHOWN_BY), STANDARD),
            new ExtractionParameters("/3d/cube-binary.ply", "model/ply", Set.of(IS_SHOWN_AT), STANDARD)))
    );
  }

  @ParameterizedTest(name = "{index} => record={0}")
  @MethodSource
  void testMediaExtraction_ResourceAndMimeType(String resourcePath,
      Set<ExtractionParameters> expectedExtractionParameters) throws Exception {
    final String rdf = new String(readFile(resourcePath), StandardCharsets.UTF_8)
        .replace("https://stacks.stanford.edu", wireMock.baseUrl())
        .replace("https://purl.stanford.edu", wireMock.baseUrl())
        .replace("http://cmcassociates.co.uk", wireMock.baseUrl())
        .replace("http://www.cmcassociates.co.uk", wireMock.baseUrl())
        .replace("https://raw.githubusercontent.com/europeana/metis-framework/refs/heads/develop/"
            + "metis-media-service/src/test/resources/models", wireMock.baseUrl() + "/3d");
    final Set<ExtractionParameters> actualExtractionParameters = new HashSet<>();
    try (InputStream inputRdf = new ByteArrayInputStream(rdf.getBytes(StandardCharsets.UTF_8));
        MediaExtractor mediaExtractor = new MediaProcessorFactory().createMediaExtractor()) {
      final List<RdfResourceEntry> resourceEntries = rdfDeserializer.getRemainingResourcesForMediaExtraction(inputRdf);
      for (RdfResourceEntry resourceEntry : resourceEntries) {
        assertTrue(resourceEntry.getResourceUrl().startsWith(wireMock.baseUrl() + "/"));
        if (resourceEntry.getServiceReference() != null) {
          assertTrue(resourceEntry.getServiceReference().startsWith(wireMock.baseUrl() + "/"));
        }
        try (ResourceExtractionResult resourceExtractionResult = mediaExtractor.performMediaExtraction(resourceEntry, true)) {
          assertNotNull(resourceExtractionResult);
          assertNotNull(resourceExtractionResult.getMetadata());
          assertEquals(resourceEntry.getResourceUrl(), resourceExtractionResult.getMetadata().getResourceUrl());
          actualExtractionParameters.add(
              new ExtractionParameters(URI.create(resourceExtractionResult.getMetadata().getResourceUrl()).getPath(),
                  resourceExtractionResult.getMetadata().getMimeType(), Set.copyOf(resourceEntry.getUrlTypes()),
                  resourceEntry.getResourceKind()));
          if (resourceEntry.getResourceKind() == IIIF) {
            assertIIIFExtraction(resourceEntry, resourceExtractionResult);
          } else if (resourceExtractionResult.getMetadata().getMimeType().startsWith("model/")) {
            assertInstanceOf(Media3dResourceMetadata.class,
                assertInstanceOf(ResourceExtractionResultImpl.class, resourceExtractionResult).getOriginalMetadata());
            assertNull(resourceExtractionResult.getThumbnails());
          }
        }
      }
    }
    assertEquals(expectedExtractionParameters, actualExtractionParameters);
  }

  private void assertIIIFExtraction(RdfResourceEntry resourceEntry, ResourceExtractionResult result) throws IOException {
    final ImageResourceMetadata metadata = assertInstanceOf(ImageResourceMetadata.class,
        assertInstanceOf(ResourceExtractionResultImpl.class, result).getOriginalMetadata());
    assertEquals(800, metadata.getWidth());
    assertEquals(400, metadata.getHeight());
    assertTrue(metadata.getDominantColors().containsAll(List.of("#FF0000", "#0000FF")));
    assertNotNull(result.getThumbnails());
    assertEquals(2, result.getThumbnails().size());
    assertEquals(2, metadata.getThumbnailTargetNames().size());
    final List<Integer> expectedWidths = List.of(200, 400);
    for (int i = 0; i < expectedWidths.size(); i++) {
      final Thumbnail thumbnail = result.getThumbnails().get(i);
      assertEquals(resourceEntry.getResourceUrl(), thumbnail.getResourceUrl());
      final int width = expectedWidths.get(i);
      final BufferedImage decoded = assertThumbnail(thumbnail, width, width / 2);
      decoded.flush();
    }
    final String servicePath = URI.create(resourceEntry.getServiceReference()).getPath();
    wireMock.verify(getRequestedFor(urlEqualTo(servicePath + "/info.json")));
    wireMock.verify(getRequestedFor(urlEqualTo(servicePath + "/full/!400,400/0/default.jpg")));
    wireMock.verify(0, getRequestedFor(urlEqualTo(URI.create(resourceEntry.getResourceUrl()).getPath())));
  }

  private void stub(String path, String mimeType, byte[] content) {
    wireMock.stubFor(get(urlEqualTo(path)).willReturn(aResponse()
        .withHeader("Content-Type", mimeType)
        .withBody(content)));
  }

  private byte[] readFile(String path) throws IOException {
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
      assertNotNull(inputStream, "Missing fixture: " + path);
      return inputStream.readAllBytes();
    }
  }

  private record ExtractionParameters(String resourcePath, String mimeType, Set<UrlType> urlTypes, RdfResourceKind resourceKind) {

  }
}
