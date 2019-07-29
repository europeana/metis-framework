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
import eu.europeana.metis.mediaprocessing.extraction.MediaExtractorImpl.ProcessingMode;
import eu.europeana.metis.mediaprocessing.http.ResourceDownloadClient;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResultImpl;
import eu.europeana.metis.utils.MediaType;
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

  private static ResourceDownloadClient fullProcessingDownloadClient;
  private static ResourceDownloadClient reducedProcessingDownloadClient;
  private static CommandExecutor commandExecutor;
  private static Tika tika;

  private static ImageProcessor imageProcessor;
  private static AudioVideoProcessor audioVideoProcessor;
  private static TextProcessor textProcessor;

  private static MediaExtractorImpl mediaExtractor;

  @BeforeAll
  static void prepare() {
    fullProcessingDownloadClient = mock(ResourceDownloadClient.class);
    reducedProcessingDownloadClient = mock(ResourceDownloadClient.class);
    commandExecutor = mock(CommandExecutor.class);
    tika = mock(Tika.class);
    imageProcessor = mock(ImageProcessor.class);
    audioVideoProcessor = mock(AudioVideoProcessor.class);
    textProcessor = mock(TextProcessor.class);
    mediaExtractor = spy(new MediaExtractorImpl(fullProcessingDownloadClient,
        reducedProcessingDownloadClient, tika, imageProcessor, audioVideoProcessor, textProcessor));
  }

  @BeforeEach
  void resetMocks() {
    reset(fullProcessingDownloadClient, reducedProcessingDownloadClient, commandExecutor, tika,
        imageProcessor, audioVideoProcessor, textProcessor, mediaExtractor);
  }

  @Test
  void testDetectAndVerifyMimeType() throws IOException, MediaExtractionException, URISyntaxException {

    // Create resource
    final URI actualLocation = new URI("http://resource.actual.location.test.com");
    final Path contentPath = Paths.get("content path");
    final Resource resource = mock(Resource.class);
    doReturn("resource url").when(resource).getResourceUrl();
    doReturn(actualLocation).when(resource).getActualLocation();
    doReturn("mime type").when(resource).getProvidedMimeType();
    doReturn(contentPath).when(resource).getContentPath();

    // Register mime types
    final String detectedMimeTypeNoContent = "detected mime type no content";
    doReturn(false).when(mediaExtractor).shouldDownloadForFullProcessing(detectedMimeTypeNoContent);
    final String detectedMimeTypeWithContent = "detected mime type with content";
    doReturn(true).when(mediaExtractor).shouldDownloadForFullProcessing(detectedMimeTypeWithContent);

    // Test case where there is no content
    doReturn(false).when(resource).hasContent();
    doReturn(detectedMimeTypeNoContent).when(tika).detect(actualLocation.toURL());
    assertEquals(detectedMimeTypeNoContent, mediaExtractor.detectAndVerifyMimeType(resource, ProcessingMode.FULL));

    // Test case where there is content
    doReturn(true).when(resource).hasContent();
    doReturn(detectedMimeTypeWithContent).when(tika).detect(contentPath);
    assertEquals(detectedMimeTypeWithContent, mediaExtractor.detectAndVerifyMimeType(resource, ProcessingMode.FULL));

    // Test case where there is no content, but there should be.
    doReturn(false).when(resource).hasContent();
    doReturn(detectedMimeTypeWithContent).when(tika).detect(actualLocation.toURL());
    assertThrows(MediaExtractionException.class, () -> mediaExtractor.detectAndVerifyMimeType(resource, ProcessingMode.FULL));

    // Test when tika throws exception
    doThrow(IOException.class).when(tika).detect(contentPath);
    assertThrows(MediaExtractionException.class, () -> mediaExtractor.detectAndVerifyMimeType(resource, ProcessingMode.FULL));
    doReturn(detectedMimeTypeWithContent).when(tika).detect(contentPath);

    // Check what happens if resource.hasContent() throws an exception.
    doThrow(IOException.class).when(resource).hasContent();
    assertThrows(MediaExtractionException.class, () -> mediaExtractor.detectAndVerifyMimeType(resource, ProcessingMode.FULL));
    doReturn(true).when(resource).hasContent();
  }

  @Test
  void testChooseMediaProcessor() {
    assertSame(imageProcessor, mediaExtractor.chooseMediaProcessor(MediaType.IMAGE));
    assertSame(audioVideoProcessor, mediaExtractor.chooseMediaProcessor(MediaType.AUDIO));
    assertSame(audioVideoProcessor, mediaExtractor.chooseMediaProcessor(MediaType.VIDEO));
    assertSame(textProcessor, mediaExtractor.chooseMediaProcessor(MediaType.TEXT));
    assertNull(mediaExtractor.chooseMediaProcessor(MediaType.OTHER));
  }

  @Test
  void testProcessResource() throws MediaExtractionException {

    // Create resource
    final Resource resource = mock(Resource.class);

    // Set detected mime type
    final String detectedMimeType = "detected mime type";
    doReturn(detectedMimeType).when(mediaExtractor).detectAndVerifyMimeType(resource, ProcessingMode.FULL);

    // Set processor.
    doReturn(audioVideoProcessor)
        .when(mediaExtractor).chooseMediaProcessor(MediaType.getMediaType(detectedMimeType));
    final ResourceExtractionResultImpl result = new ResourceExtractionResultImpl(null, null);
    doReturn(result).when(audioVideoProcessor).extractMetadata(resource, detectedMimeType);

    // Make the call.
    assertSame(result, mediaExtractor.performProcessing(resource, ProcessingMode.FULL));
  }

  @Test
  void testPerformMediaExtraction() throws IOException, MediaExtractionException {

    // Create objects and mock.
    final RdfResourceEntry entry = new RdfResourceEntry("resource url", Collections.emptyList());
    final Resource resource = mock(Resource.class);
    doReturn(ProcessingMode.FULL).when(mediaExtractor).getMode(entry);
    doReturn(resource).when(fullProcessingDownloadClient).download(entry);
    final ResourceExtractionResultImpl result = new ResourceExtractionResultImpl(null, null);
    doReturn(result).when(mediaExtractor).performProcessing(resource, ProcessingMode.FULL);

    // Make the call and verify that the resource is closed.
    assertEquals(result, mediaExtractor.performMediaExtraction(entry));
    verify(resource).close();

    // Check exception from downloading.
    doThrow(IOException.class).when(fullProcessingDownloadClient).download(entry);
    assertThrows(MediaExtractionException.class,
        () -> mediaExtractor.performMediaExtraction(entry));
    doThrow(RuntimeException.class).when(fullProcessingDownloadClient).download(entry);
    assertThrows(MediaExtractionException.class,
        () -> mediaExtractor.performMediaExtraction(entry));
    doReturn(resource).when(fullProcessingDownloadClient).download(entry);
  }

  @Test
  void testClose() throws IOException {
    mediaExtractor.close();
    verify(fullProcessingDownloadClient).close();
    verify(reducedProcessingDownloadClient).close();
  }

  @Test
  void testShouldDownloadForFullProcessing() {
    doReturn(true).when(imageProcessor).downloadResourceForFullProcessing();
    doReturn(true).when(textProcessor).downloadResourceForFullProcessing();
    doReturn(false).when(audioVideoProcessor).downloadResourceForFullProcessing();
    assertTrue(mediaExtractor.shouldDownloadForFullProcessing("image/unknown_type"));
    assertTrue(mediaExtractor.shouldDownloadForFullProcessing("text/unknown_type"));
    assertFalse(mediaExtractor.shouldDownloadForFullProcessing("audio/unknown_type"));
    assertFalse(mediaExtractor.shouldDownloadForFullProcessing("video/unknown_type"));
    assertFalse(mediaExtractor.shouldDownloadForFullProcessing("unknown_type"));
  }
}
