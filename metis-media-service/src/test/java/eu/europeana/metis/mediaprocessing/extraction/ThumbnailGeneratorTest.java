package eu.europeana.metis.mediaprocessing.extraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import eu.europeana.metis.mediaprocessing.model.ThumbnailImpl;
import eu.europeana.metis.mediaprocessing.model.ThumbnailKind;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ThumbnailGeneratorTest {

  private ThumbnailGenerator generator;

  @BeforeEach
  void setUp() {
    generator = new ThumbnailGenerator();
  }

  @ParameterizedTest
  @CsvSource({"image/png,image/png", "image/jpeg,image/jpeg", "other,image/jpeg"})
  void preparesNamedThumbnails(String detectedMimeType, String expectedMimeType) throws Exception {
    final String url = "http://images.is.ed.ac.uk/MediaManager/srvr?mediafile=/Size3/UoEcar-4-NA/1007/0012127c.jpg";
    final String md5 = "6d27e9f0dcdbf33afc07d952cc5c2833";
    final EnumMap<ThumbnailKind, ThumbnailImpl> thumbnails = generator.prepareThumbnails(url, detectedMimeType);
    try {
      assertEquals(List.of(ThumbnailKind.MEDIUM, ThumbnailKind.LARGE), new ArrayList<>(thumbnails.keySet()));
      for (ThumbnailKind kind : ThumbnailKind.values()) {
        final ThumbnailImpl thumbnail = thumbnails.get(kind);
        assertNotNull(thumbnail);
        assertEquals(md5 + (kind == ThumbnailKind.MEDIUM ? "-MEDIUM" : "-LARGE"), thumbnail.getTargetName());
        assertEquals(url, thumbnail.getResourceUrl());
        assertEquals(expectedMimeType, thumbnail.getMimeType());
        assertFalse(thumbnail.hasContent());
        assertEquals(0L, thumbnail.getContentSize());
      }
    } finally {
      thumbnails.values().forEach(ThumbnailImpl::close);
    }
  }

}
