package eu.europeana.metis.mediaprocessing.extraction;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.http.JvmProxyConfigurer;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.extraction.iiif.IIIFInfoJson;
import eu.europeana.metis.mediaprocessing.extraction.iiif.IIIFValidation;
import eu.europeana.metis.mediaprocessing.model.ImageResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.RdfResourceKind;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResultImpl;
import eu.europeana.metis.mediaprocessing.model.ResourceIIIFImpl;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import eu.europeana.metis.mediaprocessing.model.ThumbnailImpl;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class IIIFProcessorTest {

  private static ThumbnailGenerator thumbnailGenerator;
  private static IIIFProcessor iiifProcessor;
  private static WireMockServer wireMockServer;

  @BeforeAll
  static void createMocks() {
    thumbnailGenerator = mock(ThumbnailGenerator.class);
    iiifProcessor = spy(new IIIFProcessor(thumbnailGenerator));
    wireMockServer = new WireMockServer(wireMockConfig()
        .dynamicPort()
        .enableBrowserProxying(true)
        .notifier(new ConsoleNotifier(true)));
    wireMockServer.start();
    JvmProxyConfigurer.configureFor(wireMockServer);
  }

  @AfterAll
  static void tearDownWireMock() {
    wireMockServer.stop();
  }

  @BeforeEach
  void resetMocks() {
    reset(thumbnailGenerator);
  }

  @Test
  void testDownloadResourceForFullProcessing() {
    assertTrue(iiifProcessor.downloadResourceForFullProcessing());
  }

  @Test
  void testCopy() throws MediaExtractionException {

    // Create resource
    final Resource resource = mock(Resource.class);
    final Long fileSize = 12345L;
    final String url = "test url";
    doReturn(fileSize).when(resource).getProvidedFileSize();
    doReturn(url).when(resource).getResourceUrl();

    // Make call
    final String mediaType = "image type";
    final ResourceExtractionResultImpl result = iiifProcessor.copyMetadata(resource, mediaType);

    // Verify
    assertNotNull(result);
    assertNotNull(result.getOriginalMetadata());
    assertInstanceOf(ImageResourceMetadata.class, result.getOriginalMetadata());
    assertEquals(mediaType, result.getOriginalMetadata().getMimeType());
    assertEquals(fileSize, result.getOriginalMetadata().getContentSize());
    assertEquals(url, result.getOriginalMetadata().getResourceUrl());
    assertNull(result.getThumbnails());
    assertTrue(result.getOriginalMetadata().getThumbnailTargetNames().isEmpty());
    assertTrue(((ImageResourceMetadata)result.getOriginalMetadata()).getDominantColors().isEmpty());
    assertNull(((ImageResourceMetadata)result.getOriginalMetadata()).getColorSpace());
    assertNull(((ImageResourceMetadata)result.getOriginalMetadata()).getHeight());
    assertNull(((ImageResourceMetadata)result.getOriginalMetadata()).getWidth());
  }

  @Test
  void extractMetadata() throws MediaExtractionException, IOException {
    // Define input
    InputStream inputStreamInfoJson = getClass().getClassLoader().getResourceAsStream("__files/iiif_info_v2.json");
    String infoJson = new String(inputStreamInfoJson.readAllBytes());

    wireMockServer.stubFor(get(urlEqualTo("/image/iiif/zw031pj2507/zw031pj2507_0001/info.json"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(infoJson)
            .withStatus(HttpStatus.OK.value())));
    final String url = "http://localhost:" + wireMockServer.port() + "/image/iiif/zw031pj2507/zw031pj2507_0001/full/full/0/default.jpg";
    IIIFValidation iiifValidation = new IIIFValidation();
    final File content = new File("content file");
    final RdfResourceEntry rdfResourceEntry = new RdfResourceEntry(url,
        Collections.singletonList(UrlType.IS_SHOWN_BY), RdfResourceKind.STANDARD);
    final IIIFInfoJson iiifInfoJson = iiifValidation.fetchInfoJson(rdfResourceEntry);
    final ResourceIIIFImpl resource = spy(
        new ResourceIIIFImpl(rdfResourceEntry, null, null,
            URI.create(url), inputStreamInfoJson,
            iiifInfoJson));
    final String detectedMimeType = "image/jpeg";
    doReturn(true).when(resource).hasContent();
    doReturn(1234L).when(resource).getContentSize();
    doReturn(content).when(resource).getContentFile();

    // Define thumbnails
    final ThumbnailImpl thumbnail1 = mock(ThumbnailImpl.class);
    doReturn("thumbnail 1").when(thumbnail1).getTargetName();

    final ThumbnailImpl thumbnail2 = mock(ThumbnailImpl.class);
    doReturn("thumbnail 2").when(thumbnail2).getTargetName();

    // Define output and mock thumbnail generator - resource type for which metadata is generated.
    final ImageMetadata imageMetadata = new ImageMetadata(3710, 3088, "sRGB",
        Arrays.asList("123456", "654321"));
    final Pair<ImageMetadata, List<Thumbnail>> thumbnailsAndMetadata = new ImmutablePair<>(
        imageMetadata, Arrays.asList(thumbnail1, thumbnail2));
    doReturn(thumbnailsAndMetadata).when(thumbnailGenerator)
                                   .generateThumbnails(url, detectedMimeType, content, false);

    // Call method
    final ResourceExtractionResultImpl result = iiifProcessor.extractMetadata(resource, detectedMimeType, true);

    // Verify result metadata general properties
    assertInstanceOf(ImageResourceMetadata.class, result.getOriginalMetadata());
    final ImageResourceMetadata metadata = (ImageResourceMetadata) result.getOriginalMetadata();
    assertEquals(rdfResourceEntry.getResourceUrl(), metadata.getResourceUrl());
    assertEquals(detectedMimeType, metadata.getMimeType());
    assertEquals(2, metadata.getThumbnailTargetNames().size());
    assertTrue(metadata.getThumbnailTargetNames().contains(thumbnail1.getTargetName()));
    assertTrue(metadata.getThumbnailTargetNames().contains(thumbnail2.getTargetName()));
    assertEquals(resource.getContentSize(), metadata.getContentSize());

    // Verify result metadata image specific properties
    assertEquals(Integer.valueOf(imageMetadata.getWidth()), metadata.getWidth());
    assertEquals(Integer.valueOf(imageMetadata.getHeight()), metadata.getHeight());
    assertEquals(imageMetadata.getColorSpace(), metadata.getColorSpace().xmlValue());
    assertEquals(imageMetadata.getDominantColors().stream().map(color -> "#" + color).toList(), metadata.getDominantColors());


    // Check for resource with no content
    doReturn(false).when(resource).hasContent();
    assertThrows(MediaExtractionException.class,
        () -> iiifProcessor.extractMetadata(resource, detectedMimeType, true));
    doReturn(true).when(resource).hasContent();

    // Check for resource with IO exception
    doThrow(new IOException()).when(resource).hasContent();
    assertThrows(MediaExtractionException.class,
        () -> iiifProcessor.extractMetadata(resource, detectedMimeType, true));
    doReturn(true).when(resource).hasContent();
    doThrow(new IOException()).when(resource).getContentSize();
    assertThrows(MediaExtractionException.class,
        () -> iiifProcessor.extractMetadata(resource, detectedMimeType, true));
    doReturn(1234L).when(resource).getContentSize();

    // Check that all is well again.
    assertNotNull(iiifProcessor.extractMetadata(resource, detectedMimeType, true));
  }

}
