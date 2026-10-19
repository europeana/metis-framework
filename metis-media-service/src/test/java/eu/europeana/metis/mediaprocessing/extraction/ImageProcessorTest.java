package eu.europeana.metis.mediaprocessing.extraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.model.ImageResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.RdfResourceKind;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResultImpl;
import eu.europeana.metis.mediaprocessing.model.ResourceImpl;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ImageProcessorTest {

  private static ThumbnailGenerator thumbnailGenerator;
  private static ImageProcessor imageProcessor;

  @BeforeAll
  static void createMocks() {
    thumbnailGenerator = mock(ThumbnailGenerator.class);
    imageProcessor = spy(new ImageProcessor(thumbnailGenerator));
  }

  @BeforeEach
  void resetMocks() {
    reset(thumbnailGenerator, imageProcessor);
  }

  @Test
  void testDownloadResourceForFullProcessing() {
    assertTrue(imageProcessor.downloadResourceForFullProcessing());
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
    final ResourceExtractionResultImpl result = imageProcessor.copyMetadata(resource, mediaType);

    // Verify
    assertNotNull(result);
    assertNotNull(result.getOriginalMetadata());
    assertTrue(result.getOriginalMetadata() instanceof ImageResourceMetadata);
    assertEquals(mediaType, result.getOriginalMetadata().getMimeType());
    assertEquals(fileSize, result.getOriginalMetadata().getContentSize());
    assertEquals(url, result.getOriginalMetadata().getResourceUrl());
    assertNull(result.getThumbnails());
    assertTrue(result.getOriginalMetadata().getThumbnailTargetNames().isEmpty());
    assertTrue(((ImageResourceMetadata) result.getOriginalMetadata()).getDominantColors().isEmpty());
    assertNull(((ImageResourceMetadata) result.getOriginalMetadata()).getColorSpace());
    assertNull(((ImageResourceMetadata) result.getOriginalMetadata()).getHeight());
    assertNull(((ImageResourceMetadata) result.getOriginalMetadata()).getWidth());
  }

  @Test
  void testExtract() throws MediaExtractionException, IOException {

    // Define input
    final String url = "testUrl";
    final File content = new File("content file");
    final RdfResourceEntry rdfResourceEntry = new RdfResourceEntry("testUrl",
        Collections.singletonList(UrlType.IS_SHOWN_BY), RdfResourceKind.STANDARD, null);
    final ResourceImpl resource = spy(
        new ResourceImpl(rdfResourceEntry, null, null, null, URI.create("http://www.test.com")));
    final String detectedMimeType = "detected mime type";
    doReturn(true).when(resource).hasContent();
    doReturn(1234L).when(resource).getContentSize();
    doReturn(content).when(resource).getContentFile();

    // The processing method already returns the complete extraction result.
    final ResourceExtractionResultImpl expected = mock(ResourceExtractionResultImpl.class);
    doReturn(expected).when(imageProcessor).processImage(url, detectedMimeType, content, 1234L);

    final ResourceExtractionResultImpl resourceExtractionResult = imageProcessor.extractMetadata(resource, detectedMimeType,
        true);
    assertSame(expected, resourceExtractionResult);

    // Check for resource with no content
    doReturn(false).when(resource).hasContent();
    assertThrows(MediaExtractionException.class,
        () -> imageProcessor.extractMetadata(resource, detectedMimeType, true));
    doReturn(true).when(resource).hasContent();

    // Check for resource with IO exception
    doThrow(new IOException()).when(resource).hasContent();
    assertThrows(MediaExtractionException.class,
        () -> imageProcessor.extractMetadata(resource, detectedMimeType, true));
    doReturn(true).when(resource).hasContent();
    doThrow(new IOException()).when(resource).getContentSize();
    assertThrows(MediaExtractionException.class,
        () -> imageProcessor.extractMetadata(resource, detectedMimeType, true));
    doReturn(1234L).when(resource).getContentSize();

    // Check that all is well again.
    assertNotNull(imageProcessor.extractMetadata(resource, detectedMimeType, true));
  }

  @Test
  void rejectsNullContent() {
    assertThrows(MediaExtractionException.class,
        () -> imageProcessor.processImage("test", "image/png", null, null));
  }

  @Test
  void rejectsNonImageInputs() {
    assertThrows(MediaExtractionException.class,
        () -> imageProcessor.processImage("test", "application/pdf", new File("unused"), null));
  }

  @ParameterizedTest
  @ValueSource(strings = {"image/vnd.djvu", "image/x-djvu", "image/x.djvu"})
  void rejectsDjvuThroughUnsupportedReader(String mimeType, @TempDir Path directory) throws IOException {
    final Path content = directory.resolve("image.djvu");
    Files.write(content, new byte[]{'A', 'T', '&', 'T', 'F', 'O', 'R', 'M', 0, 0, 0, 4, 'D', 'J', 'V', 'U'});
    final ImageProcessor processor = new ImageProcessor(new ThumbnailGenerator());
    final MediaExtractionException exception = assertThrows(MediaExtractionException.class,
        () -> processor.processImage("test", mimeType, content.toFile(), Files.size(content)));
    final IOException cause = assertInstanceOf(IOException.class, exception.getCause());
    assertTrue(cause.getMessage().startsWith("No ImageIO reader supports image:"));
  }
}
