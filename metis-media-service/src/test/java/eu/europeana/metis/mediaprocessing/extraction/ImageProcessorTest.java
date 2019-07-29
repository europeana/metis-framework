package eu.europeana.metis.mediaprocessing.extraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResultImpl;
import eu.europeana.metis.mediaprocessing.model.ResourceImpl;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import eu.europeana.metis.mediaprocessing.model.ThumbnailImpl;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    reset(thumbnailGenerator);
  }

  @Test
  void testProcessing() throws MediaExtractionException, IOException {

    // Define input
    final String url = "testUrl";
    final File content = new File("content file");
    final RdfResourceEntry rdfResourceEntry = new RdfResourceEntry("testUrl",
        Collections.singletonList(UrlType.IS_SHOWN_BY));
    final ResourceImpl resource = spy(
        new ResourceImpl(rdfResourceEntry, null, null, URI.create("http://www.test.com")));
    final String detectedMimeType = "detected mime type";
    doReturn(true).when(resource).hasContent();
    doReturn(1234L).when(resource).getContentSize();
    doReturn(content).when(resource).getContentFile();

    // Define thumbnails
    final ThumbnailImpl thumbnail1 = mock(ThumbnailImpl.class);
    doReturn("thumbnail 1").when(thumbnail1).getTargetName();
    final ThumbnailImpl thumbnail2 = mock(ThumbnailImpl.class);
    doReturn("thumbnail 2").when(thumbnail1).getTargetName();

    // Define output and mock thumbnail generator - resource type for which metadata is generated.
    final ImageMetadata imageMetadata = new ImageMetadata(123, 321, "sRGB",
        Arrays.asList("123456", "654321"));
    final Pair<ImageMetadata, List<Thumbnail>> thumbnailsAndMetadata = new ImmutablePair<>(
        imageMetadata, Arrays.asList(thumbnail1, thumbnail2));
    doReturn(thumbnailsAndMetadata).when(thumbnailGenerator)
        .generateThumbnails(url, detectedMimeType, content);

    // Call method
    final ResourceExtractionResultImpl result = imageProcessor.extractMetadata(resource, detectedMimeType);

    // Verify result metadata general properties
    assertTrue(result.getOriginalMetadata() instanceof ImageResourceMetadata);
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
    assertEquals(imageMetadata.getDominantColors().stream().map(color -> "#" + color)
        .collect(Collectors.toList()), metadata.getDominantColors());

    // Verify result thumbnails
    assertEquals(thumbnailsAndMetadata.getRight(), result.getThumbnails());

    // Check for resource with no content
    doReturn(false).when(resource).hasContent();
    assertThrows(MediaExtractionException.class,
        () -> imageProcessor.extractMetadata(resource, detectedMimeType));
    doReturn(true).when(resource).hasContent();

    // Check for resource with IO exception
    doThrow(new IOException()).when(resource).hasContent();
    assertThrows(MediaExtractionException.class,
        () -> imageProcessor.extractMetadata(resource, detectedMimeType));
    doReturn(true).when(resource).hasContent();
    doThrow(new IOException()).when(resource).getContentSize();
    assertThrows(MediaExtractionException.class,
        () -> imageProcessor.extractMetadata(resource, detectedMimeType));
    doReturn(1234L).when(resource).getContentSize();

    // Check that all is well again.
    assertNotNull(imageProcessor.extractMetadata(resource, detectedMimeType));
  }
}
