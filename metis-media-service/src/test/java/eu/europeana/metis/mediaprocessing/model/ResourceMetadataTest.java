package eu.europeana.metis.mediaprocessing.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ResourceMetadataTest {

  @Test
  void testAudioResource() {
    final AudioResourceMetadata sourceMetadata = new AudioResourceMetadata();
    final ResourceMetadata metadata = new ResourceMetadata(sourceMetadata);
    assertEquals(sourceMetadata, metadata.getMetaData());
    assertThrows(IllegalArgumentException.class,
        () -> new ResourceMetadata((AudioResourceMetadata) null));
  }

  @Test
  void testVideoResource() {
    final VideoResourceMetadata sourceMetadata = new VideoResourceMetadata();
    final ResourceMetadata metadata = new ResourceMetadata(sourceMetadata);
    assertEquals(sourceMetadata, metadata.getMetaData());
    assertThrows(IllegalArgumentException.class,
        () -> new ResourceMetadata((VideoResourceMetadata) null));
  }

  @Test
  void testTextResource() {
    final TextResourceMetadata sourceMetadata = new TextResourceMetadata();
    final ResourceMetadata metadata = new ResourceMetadata(sourceMetadata);
    assertEquals(sourceMetadata, metadata.getMetaData());
    assertThrows(IllegalArgumentException.class,
        () -> new ResourceMetadata((TextResourceMetadata) null));
  }

  @Test
  void testImageResource() {
    final ImageResourceMetadata sourceMetadata = new ImageResourceMetadata();
    final ResourceMetadata metadata = new ResourceMetadata(sourceMetadata);
    assertEquals(sourceMetadata, metadata.getMetaData());
    assertThrows(IllegalArgumentException.class,
        () -> new ResourceMetadata((ImageResourceMetadata) null));
  }

  @Test
  void testNoResource() {
    assertThrows(IllegalStateException.class, () -> new ResourceMetadata().getMetaData());
  }

  @Test
  void testGetters() {

    // Set up mocks
    final TextResourceMetadata sourceMetadata = mock(TextResourceMetadata.class);
    final ResourceMetadata metadata = new ResourceMetadata(sourceMetadata);

    // Test resource urls
    final String resourceUrl = "resourceUrl";
    doReturn(resourceUrl).when(sourceMetadata).getResourceUrl();
    assertEquals(resourceUrl, metadata.getResourceUrl());
    verify(sourceMetadata, times(1)).getResourceUrl();

    // Test mime type
    final String mimeType = "mime type";
    doReturn(mimeType).when(sourceMetadata).getMimeType();
    assertEquals(mimeType, metadata.getMimeType());
    verify(sourceMetadata, times(1)).getMimeType();

    // Test thumbnails
    final Set<String> thumbnailNames = new HashSet<>(Arrays.asList("thumbnail 1", "thumbnail 2"));
    doReturn(thumbnailNames).when(sourceMetadata).getThumbnailTargetNames();
    assertEquals(thumbnailNames, metadata.getThumbnailTargetNames());
    verify(sourceMetadata, times(1)).getThumbnailTargetNames();
  }
}
