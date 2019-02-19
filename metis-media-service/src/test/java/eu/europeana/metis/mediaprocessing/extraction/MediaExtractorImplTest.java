package eu.europeana.metis.mediaprocessing.extraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.http.ResourceDownloadClient;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import org.apache.tika.Tika;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MediaExtractorImplTest {

  private static ResourceDownloadClient resourceDownloadClient;
  private static CommandExecutor commandExecutor;
  private static Tika tika;

  private static ImageProcessor imageProcessor;
  private static AudioVideoProcessor audioVideoProcessor;
  private static TextProcessor textProcessor;

  private static MediaExtractorImpl mediaExtractor;

  @BeforeAll
  static void prepare() {
    resourceDownloadClient = mock(ResourceDownloadClient.class);
    commandExecutor = mock(CommandExecutor.class);
    tika = mock(Tika.class);
    imageProcessor = mock(ImageProcessor.class);
    audioVideoProcessor = mock(AudioVideoProcessor.class);
    textProcessor = mock(TextProcessor.class);
    mediaExtractor = spy(
        new MediaExtractorImpl(resourceDownloadClient, commandExecutor, tika, imageProcessor,
            audioVideoProcessor, textProcessor));
  }

  @BeforeEach
  void resetMocks() {
    reset(resourceDownloadClient, commandExecutor, tika, imageProcessor, audioVideoProcessor,
        textProcessor, mediaExtractor);
  }

  @Test
  void testVerifyMimeType() throws IOException, MediaExtractionException, URISyntaxException {

    // Create resource
    final URI actualLocation = new URI("http://resource.actual.location.test.com");
    final Path contentPath = Paths.get("content path");
    final Resource resource = mock(Resource.class);
    doReturn("resource url").when(resource).getResourceUrl();
    doReturn(actualLocation).when(resource).getActualLocation();
    doReturn("mime type").when(resource).getMimeType();
    doReturn(contentPath).when(resource).getContentPath();

    // Test case where there is no content
    final String detectedMimeTypeNoContent = "detected mime type no content";
    doReturn(false).when(resource).hasContent();
    doReturn(detectedMimeTypeNoContent).when(tika).detect(actualLocation.toURL());
    assertEquals(detectedMimeTypeNoContent, mediaExtractor.verifyMimeType(resource));

    // Test case where there is content
    final String detectedMimeTypeWithContent = "detected mime type with content";
    doReturn(true).when(resource).hasContent();
    doReturn(detectedMimeTypeWithContent).when(tika).detect(contentPath);
    assertEquals(detectedMimeTypeWithContent, mediaExtractor.verifyMimeType(resource));

    // Test when tika throws exception
    doThrow(IOException.class).when(tika).detect(contentPath);
    assertThrows(MediaExtractionException.class, () -> mediaExtractor.verifyMimeType(resource));
  }

  @Test
  void testChooseMediaProcessor() {
    assertSame(imageProcessor, mediaExtractor.chooseMediaProcessor(ResourceType.IMAGE));
    assertSame(audioVideoProcessor, mediaExtractor.chooseMediaProcessor(ResourceType.AUDIO));
    assertSame(audioVideoProcessor, mediaExtractor.chooseMediaProcessor(ResourceType.VIDEO));
    assertSame(textProcessor, mediaExtractor.chooseMediaProcessor(ResourceType.TEXT));
    assertNull(mediaExtractor.chooseMediaProcessor(ResourceType.UNKNOWN));
  }

  @Test
  void testProcessResourceNotDownloaded() throws IOException, MediaExtractionException {

    // Create resource
    final Resource resource = mock(Resource.class);
    doReturn(false).when(resource).hasContent();

    // Use media type that should not be downloaded.
    final String detectedMimeType = "video/unknown_type";
    assertFalse(ResourceType.shouldDownloadMimetype(detectedMimeType));
    doReturn(detectedMimeType).when(mediaExtractor).verifyMimeType(resource);

    // Set processor.
    doReturn(audioVideoProcessor)
        .when(mediaExtractor).chooseMediaProcessor(ResourceType.getResourceType(detectedMimeType));
    final ResourceExtractionResult result = new ResourceExtractionResult(null, null);
    doReturn(result).when(audioVideoProcessor).process(resource, detectedMimeType);

    // Make the call.
    assertSame(result, mediaExtractor.processResource(resource));

    // Check what happens if resource.hasContent() throws an exception.
    doThrow(IOException.class).when(resource).hasContent();
    assertThrows(MediaExtractionException.class, () -> mediaExtractor.processResource(resource));
  }

  @Test
  void testProcessResourceDownloaded() throws IOException, MediaExtractionException {

    // Create resource
    final Resource resource = mock(Resource.class);
    doReturn(true).when(resource).hasContent();

    // Use media type that should be downloaded.
    final String detectedMimeType = "image/unknown_type";
    assertTrue(ResourceType.shouldDownloadMimetype(detectedMimeType));
    doReturn(detectedMimeType).when(mediaExtractor).verifyMimeType(resource);

    // Set processor.
    doReturn(imageProcessor)
        .when(mediaExtractor).chooseMediaProcessor(ResourceType.getResourceType(detectedMimeType));
    final ResourceExtractionResult result = new ResourceExtractionResult(null, null);
    doReturn(result).when(imageProcessor).process(resource, detectedMimeType);

    // Make the call.
    assertSame(result, mediaExtractor.processResource(resource));

    // Check what happens if the resource was not downloaded even though it should have been
    doReturn(false).when(resource).hasContent();
    assertThrows(MediaExtractionException.class, () -> mediaExtractor.processResource(resource));
  }

  @Test
  void testPerformMediaExtraction() throws IOException, MediaExtractionException {

    // Create objects and mock.
    final RdfResourceEntry entry = new RdfResourceEntry("resource url", Collections.emptyList());
    final Resource resource = mock(Resource.class);
    doReturn(resource).when(resourceDownloadClient).download(entry);
    final ResourceExtractionResult result = new ResourceExtractionResult(null, null);
    doReturn(result).when(mediaExtractor).processResource(resource);

    // Make the call and verify that the resource is closed.
    assertEquals(result, mediaExtractor.performMediaExtraction(entry));
    verify(resource).close();

    // Check exception from downloading.
    doThrow(IOException.class).when(resourceDownloadClient).download(entry);
    assertThrows(MediaExtractionException.class,
        () -> mediaExtractor.performMediaExtraction(entry));
    doThrow(RuntimeException.class).when(resourceDownloadClient).download(entry);
    assertThrows(MediaExtractionException.class,
        () -> mediaExtractor.performMediaExtraction(entry));
    doReturn(resource).when(resourceDownloadClient).download(entry);
  }

  @Test
  void testClose() throws IOException {
    mediaExtractor.close();
    verify(commandExecutor).close();
    verify(resourceDownloadClient).close();
  }
}
