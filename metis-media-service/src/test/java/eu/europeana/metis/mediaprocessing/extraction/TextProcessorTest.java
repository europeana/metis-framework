package eu.europeana.metis.mediaprocessing.extraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.extraction.TextProcessor.PdfCharacteristics;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import eu.europeana.metis.mediaprocessing.model.ResourceImpl;
import eu.europeana.metis.mediaprocessing.model.TextResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import eu.europeana.metis.mediaprocessing.model.ThumbnailImpl;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TextProcessorTest {

  private static ThumbnailGenerator thumbnailGenerator;
  private static TextProcessor textProcessor;

  @BeforeAll
  static void prepare() {
    thumbnailGenerator = mock(ThumbnailGenerator.class);
    textProcessor = spy(new TextProcessor(thumbnailGenerator));
  }

  @BeforeEach
  void resetMocks() {
    reset(thumbnailGenerator);
    doReturn(true).when(textProcessor).shouldExtractMetadata(notNull());
  }

  @Test
  void testProcessForRegularText() throws IOException, MediaExtractionException {

    // Define input
    final RdfResourceEntry rdfResourceEntry = new RdfResourceEntry("testUrl",
        Collections.singletonList(UrlType.IS_SHOWN_BY));
    final ResourceImpl resource = spy(new ResourceImpl(rdfResourceEntry, "mime type",
        URI.create("http://www.test.com")));
    final String detectedMimeType = "detected mime type";
    doReturn(true).when(resource).hasContent();
    doReturn(1234L).when(resource).getContentSize();

    // Call method
    final ResourceExtractionResult result = textProcessor.process(resource, detectedMimeType);

    // Verify result metadata general properties
    assertTrue(result.getOriginalMetadata() instanceof TextResourceMetadata);
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

    // Check for resource link type for which we should not extract metadata at all
    doReturn(false).when(textProcessor).shouldExtractMetadata(notNull());
    assertNull(textProcessor.process(resource, detectedMimeType));
    doReturn(true).when(textProcessor).shouldExtractMetadata(notNull());

    // Check for resource with no content
    doReturn(false).when(resource).hasContent();
    assertThrows(MediaExtractionException.class,
        () -> textProcessor.process(resource, detectedMimeType));
    doReturn(true).when(resource).hasContent();

    // Check for resource with IO exception
    doThrow(new IOException()).when(resource).hasContent();
    assertThrows(MediaExtractionException.class,
        () -> textProcessor.process(resource, detectedMimeType));
    doReturn(true).when(resource).hasContent();
    doThrow(new IOException()).when(resource).getContentSize();
    assertThrows(MediaExtractionException.class,
        () -> textProcessor.process(resource, detectedMimeType));
    doReturn(1234L).when(resource).getContentSize();

    // Check that all is well again.
    assertNotNull(textProcessor.process(resource, detectedMimeType));
  }

  @Test
  void testProcessForPdf() throws IOException, MediaExtractionException {

    // Define input
    final String url = "testUrl";
    final File content = new File("content file");
    final RdfResourceEntry rdfResourceEntry = new RdfResourceEntry("testUrl",
        Collections.singletonList(UrlType.IS_SHOWN_BY));
    final ResourceImpl resource = spy(new ResourceImpl(rdfResourceEntry, "mime type",
        URI.create("http://www.test.com")));
    final String detectedMimeType = "application/pdf";
    doReturn(true).when(resource).hasContent();
    doReturn(1234L).when(resource).getContentSize();
    doReturn(content).when(resource).getContentFile();

    // Define thumbnails
    final ThumbnailImpl thumbnail1 = mock(ThumbnailImpl.class);
    doReturn(Paths.get("File 1")).when(thumbnail1).getContentPath();
    doReturn("thumbnail 1").when(thumbnail1).getTargetName();
    final ThumbnailImpl thumbnail2 = mock(ThumbnailImpl.class);
    doReturn(Paths.get("File 2")).when(thumbnail2).getContentPath();
    doReturn("thumbnail 2").when(thumbnail1).getTargetName();

    // Define output and mock thumbnail generator - resource type for which metadata is generated.
    final ImageMetadata imageMetadata = new ImageMetadata(123, 321, "sRGB",
        Arrays.asList("123456", "654321"));
    final Pair<ImageMetadata, List<Thumbnail>> thumbnailsAndMetadata = new ImmutablePair<>(
        imageMetadata, Arrays.asList(thumbnail1, thumbnail2));
    doReturn(thumbnailsAndMetadata).when(thumbnailGenerator)
        .generateThumbnails(url, ResourceType.TEXT, content);

    // define PDF analysis results and mock the method performing it.
    final PdfCharacteristics pdfCharacteristics = new PdfCharacteristics(true, 1);
    doReturn(pdfCharacteristics).when(textProcessor).findPdfCharacteristics(content);

    // Call method
    final ResourceExtractionResult result = textProcessor.process(resource, detectedMimeType);

    // Verify result metadata general properties
    assertTrue(result.getOriginalMetadata() instanceof TextResourceMetadata);
    final TextResourceMetadata metadata = (TextResourceMetadata) result.getOriginalMetadata();
    assertEquals(rdfResourceEntry.getResourceUrl(), metadata.getResourceUrl());
    assertEquals(detectedMimeType, metadata.getMimeType());
    assertEquals(2, metadata.getThumbnailTargetNames().size());
    assertTrue(metadata.getThumbnailTargetNames().contains(thumbnail1.getTargetName()));
    assertTrue(metadata.getThumbnailTargetNames().contains(thumbnail2.getTargetName()));
    assertEquals(resource.getContentSize(), metadata.getContentSize());

    // Verify result metadata image specific properties
    assertEquals(pdfCharacteristics.containsText(), metadata.containsText());
    assertEquals(pdfCharacteristics.getResolution(), metadata.getResolution());

    // Verify result thumbnails
    assertEquals(thumbnailsAndMetadata.getRight(), result.getThumbnails());
  }
}
