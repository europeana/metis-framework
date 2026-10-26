package eu.europeana.metis.mediaprocessing.extraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.extraction.pdf.PdfProcessor;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.RdfResourceKind;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResultImpl;
import eu.europeana.metis.mediaprocessing.model.ResourceImpl;
import eu.europeana.metis.mediaprocessing.model.TextResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TextProcessorTest {

  private static PdfProcessor pdfProcessor;
  private static TextProcessor textProcessor;

  @BeforeAll
  static void createMocks() {
    pdfProcessor = mock(PdfProcessor.class);
    textProcessor = new TextProcessor(pdfProcessor);
  }

  @BeforeEach
  void resetMocks() {
    reset(pdfProcessor);
  }

  @Test
  void testDownloadResourceForFullProcessing() {
    assertTrue(textProcessor.downloadResourceForFullProcessing());
  }

  @Test
  void testCopy() {

    // Create resource
    final Resource resource = mock(Resource.class);
    final Long fileSize = 12345L;
    final String url = "test url";
    doReturn(fileSize).when(resource).getProvidedFileSize();
    doReturn(url).when(resource).getResourceUrl();

    // Make call
    final String mediaType = "text type";
    final ResourceExtractionResultImpl result = textProcessor.copyMetadata(resource, mediaType);

    // Verify
    assertNotNull(result);
    assertNotNull(result.getOriginalMetadata());
    assertInstanceOf(TextResourceMetadata.class, result.getOriginalMetadata());
    assertEquals(mediaType, result.getOriginalMetadata().getMimeType());
    assertEquals(fileSize, result.getOriginalMetadata().getContentSize());
    assertEquals(url, result.getOriginalMetadata().getResourceUrl());
    assertNull(result.getThumbnails());
    assertTrue(result.getOriginalMetadata().getThumbnailTargetNames().isEmpty());
    assertNull(((TextResourceMetadata)result.getOriginalMetadata()).getResolution());
    assertFalse(((TextResourceMetadata)result.getOriginalMetadata()).containsText());
  }

  @Test
  void testExtractForRegularText() throws IOException, MediaExtractionException {

    // Define input
    final RdfResourceEntry rdfResourceEntry = new RdfResourceEntry("testUrl",
        Collections.singletonList(UrlType.IS_SHOWN_BY), RdfResourceKind.STANDARD, null);
    final ResourceImpl resource = spy(
        new ResourceImpl(rdfResourceEntry, null, null, null, URI.create("http://www.test.com")));
    final String detectedMimeType = "detected mime type";
    doReturn(true).when(resource).hasContent();
    doReturn(1234L).when(resource).getContentSize();

    // Call method
    final ResourceExtractionResultImpl result = textProcessor.extractMetadata(resource, detectedMimeType, true);

    // Verify result metadata general properties
    assertInstanceOf(TextResourceMetadata.class, result.getOriginalMetadata());
    final TextResourceMetadata metadata = (TextResourceMetadata) result.getOriginalMetadata();
    assertEquals(rdfResourceEntry.getResourceUrl(), metadata.getResourceUrl());
    assertEquals(detectedMimeType, metadata.getMimeType());
    assertEquals(0, metadata.getThumbnailTargetNames().size());
    assertEquals(resource.getContentSize(), metadata.getContentSize());

    // Verify result metadata image specific properties
    assertFalse(metadata.containsText());
    assertNull(metadata.getResolution());

    // Verify result thumbnails
    assertNull(result.getThumbnails());

    // Check for resource with no content
    doReturn(false).when(resource).hasContent();
    assertThrows(MediaExtractionException.class,
        () -> textProcessor.extractMetadata(resource, detectedMimeType, true));
    doReturn(true).when(resource).hasContent();

    // Check for resource with IO exception
    doThrow(new IOException()).when(resource).hasContent();
    assertThrows(MediaExtractionException.class,
        () -> textProcessor.extractMetadata(resource, detectedMimeType, true));
    doReturn(true).when(resource).hasContent();
    doThrow(new IOException()).when(resource).getContentSize();
    assertThrows(MediaExtractionException.class,
        () -> textProcessor.extractMetadata(resource, detectedMimeType, true));
    doReturn(1234L).when(resource).getContentSize();

    // Check that all is well again.
    assertNotNull(textProcessor.extractMetadata(resource, detectedMimeType, true));
  }

  @Test
  void testExtractForPdf() throws IOException, MediaExtractionException {
    final Resource resource = pdfResource();
    final Thumbnail thumbnail1 = mock(Thumbnail.class);
    final Thumbnail thumbnail2 = mock(Thumbnail.class);
    doReturn("thumbnail 1").when(thumbnail1).getTargetName();
    doReturn("thumbnail 2").when(thumbnail2).getTargetName();
    final List<Thumbnail> thumbnails = Arrays.asList(thumbnail1, thumbnail2);
    doReturn(new PdfProcessor.PdfResult(true, 300, thumbnails)).when(pdfProcessor)
        .process(Path.of("document.pdf"), "https://example.org/document.pdf", true);

    final ResourceExtractionResultImpl result = textProcessor.extractMetadata(resource, "application/pdf", false);
    final TextResourceMetadata metadata = assertInstanceOf(TextResourceMetadata.class, result.getOriginalMetadata());
    assertEquals(resource.getResourceUrl(), metadata.getResourceUrl());
    assertEquals("application/pdf", metadata.getMimeType());
    assertEquals(1234L, metadata.getContentSize());
    assertTrue(metadata.containsText());
    assertEquals(300, metadata.getResolution());
    assertEquals(2, metadata.getThumbnailTargetNames().size());
    assertTrue(metadata.getThumbnailTargetNames().containsAll(List.of("thumbnail 1", "thumbnail 2")));
    assertEquals(thumbnails, result.getThumbnails());
  }

  @Test
  void extractsPdfMetadataWithoutRenderingWhenMainThumbnailExists() throws Exception {
    final Resource resource = pdfResource();
    doReturn(new PdfProcessor.PdfResult(false, null, null)).when(pdfProcessor)
        .process(Path.of("document.pdf"), "https://example.org/document.pdf", false);

    final var result = textProcessor.extractMetadata(resource, "application/pdf", true);
    final var metadata = (TextResourceMetadata) result.getOriginalMetadata();
    assertFalse(metadata.containsText());
    assertNull(metadata.getResolution());
    assertNull(result.getThumbnails());
    verify(pdfProcessor).process(Path.of("document.pdf"), "https://example.org/document.pdf", false);
  }

  @Test
  void propagatesPdfProcessingFailure() throws Exception {
    final Resource resource = pdfResource();
    doThrow(new MediaExtractionException("Invalid PDF")).when(pdfProcessor)
        .process(Path.of("document.pdf"), "https://example.org/document.pdf", true);
    assertThrows(MediaExtractionException.class,
        () -> textProcessor.extractMetadata(resource, "application/pdf", false));
  }

  @Test
  void readsContentSizeBeforeProcessingPdf() throws Exception {
    final Resource resource = pdfResource();
    doThrow(new IOException("Cannot read size")).when(resource).getContentSize();
    assertThrows(MediaExtractionException.class,
        () -> textProcessor.extractMetadata(resource, "application/pdf", false));
    verifyNoInteractions(pdfProcessor);
  }

  private Resource pdfResource() throws IOException {
    final Resource resource = mock(Resource.class);
    doReturn(true).when(resource).hasContent();
    doReturn(1234L).when(resource).getContentSize();
    doReturn(Path.of("document.pdf")).when(resource).getContentPath();
    doReturn("https://example.org/document.pdf").when(resource).getResourceUrl();
    doReturn(Collections.singleton(UrlType.IS_SHOWN_BY)).when(resource).getUrlTypes();
    return resource;
  }
}
