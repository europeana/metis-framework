package eu.europeana.metis.mediaprocessing.extraction.thumbnail;

import static eu.europeana.metis.mediaprocessing.extraction.ImageTestUtils.assertThumbnail;
import static eu.europeana.metis.mediaprocessing.extraction.ImageTestUtils.createImageWithRedLeftHalf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import eu.europeana.metis.mediaprocessing.model.ThumbnailImpl;
import eu.europeana.metis.mediaprocessing.model.ThumbnailKind;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ThumbnailGeneratorTest {

  private final ThumbnailGenerator thumbnailGenerator = new ThumbnailGenerator();

  @ParameterizedTest
  @CsvSource({"image/png,image/png", "image/jpeg,image/jpeg", "image/tiff,image/jpeg"})
  void preparesNamedThumbnails(String detectedMimeType, String expectedMimeType) throws Exception {
    final String url = "https://example.org/image.jpg";
    final String md5 = "72a9a83711d4b28af561e815c0a0a523";
    final Map<ThumbnailKind, ThumbnailImpl> thumbnails = thumbnailGenerator.prepareThumbnails(url, detectedMimeType);
    try {
      assertEquals(List.of(ThumbnailKind.MEDIUM, ThumbnailKind.LARGE), new ArrayList<>(thumbnails.keySet()));
      assertEquals(md5 + "-MEDIUM", thumbnails.get(ThumbnailKind.MEDIUM).getTargetName());
      assertEquals(md5 + "-LARGE", thumbnails.get(ThumbnailKind.LARGE).getTargetName());
      for (ThumbnailKind thumbnailKind : ThumbnailKind.values()) {
        final Thumbnail thumbnail = thumbnails.get(thumbnailKind);
        assertEquals(url, thumbnail.getResourceUrl());
        assertEquals(expectedMimeType, thumbnail.getMimeType());
        assertFalse(thumbnail.hasContent());
        assertEquals(0L, thumbnail.getContentSize());
      }
    } finally {
      thumbnails.values().forEach(ThumbnailImpl::close);
    }
  }

  @ParameterizedTest(name = "{0} x {1} becomes 200 x {2}")
  @CsvSource({"800,400,100", "400,800,400", "3201,1600,100", "800,1,1"})
  void resizesUsingOriginalAspectRatio(int originalWidth, int originalHeight, int expectedHeight) {
    // The decoded pixels can have been subsampled, so use the supplied original dimensions.
    final BufferedImage bufferedImage = createImageWithRedLeftHalf((originalWidth + 3) / 4, (originalHeight + 3) / 4, false);
    final BufferedImage resizedBufferedImage = thumbnailGenerator.resize(bufferedImage, originalWidth, originalHeight, ThumbnailKind.MEDIUM);
    try {
      assertEquals(200, resizedBufferedImage.getWidth());
      assertEquals(expectedHeight, resizedBufferedImage.getHeight());
    } finally {
      resizedBufferedImage.flush();
      bufferedImage.flush();
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"png", "jpeg"})
  void encodesTransparencyAsPngOrWhiteJpegBackground(String format) throws Exception {
    final BufferedImage bufferedImage = createImageWithRedLeftHalf(100, 50, true);
    final Map<ThumbnailKind, ThumbnailImpl> thumbnails = thumbnailGenerator.prepareThumbnails("image", "image/" + format);
    try {
      final ThumbnailImpl thumbnail = thumbnails.get(ThumbnailKind.MEDIUM);
      thumbnailGenerator.encodeThumbnail(bufferedImage, thumbnail);
      final BufferedImage encoded = assertThumbnail(thumbnail, 100, 50);
      assertTrue(new Color(encoded.getRGB(25, 25)).getRed() > 240);
      if ("png".equals(format)) {
        assertTrue(encoded.getColorModel().hasAlpha());
        assertEquals(0, encoded.getRGB(75, 25) >>> 24);
      } else {
        assertFalse(encoded.getColorModel().hasAlpha());
        final Color background = new Color(encoded.getRGB(75, 25));
        assertTrue(background.getRed() > 240 && background.getGreen() > 240 && background.getBlue() > 240);
      }
      assertEquals(0, bufferedImage.getRGB(75, 25) >>> 24);
    } finally {
      thumbnails.values().forEach(ThumbnailImpl::close);
      bufferedImage.flush();
    }
  }

  @Test
  void encodesRgbImageDirectlyAsJpeg() throws Exception {
    final BufferedImage bufferedImage = createImageWithRedLeftHalf(100, 50, false);
    final Map<ThumbnailKind, ThumbnailImpl> thumbnails = thumbnailGenerator.prepareThumbnails("image", "image/jpeg");
    try {
      final ThumbnailImpl thumbnail = thumbnails.get(ThumbnailKind.MEDIUM);
      thumbnailGenerator.encodeThumbnail(bufferedImage, thumbnail);
      final BufferedImage encoded = assertThumbnail(thumbnail, 100, 50);
      assertTrue(new Color(encoded.getRGB(25, 25)).getRed() > 240);
      assertTrue(new Color(encoded.getRGB(75, 25)).getBlue() > 240);
    } finally {
      thumbnails.values().forEach(ThumbnailImpl::close);
      bufferedImage.flush();
    }
  }

  @ParameterizedTest
  @ValueSource(ints = {100, 800})
  void generatesOpaquePngPdfPreviewsIncludingSmallPages(int width) throws Exception {
    final BufferedImage bufferedImage = createImageWithRedLeftHalf(width, width / 2, true);
    final List<Thumbnail> thumbnails = thumbnailGenerator.generatePdfThumbnails("pdf", bufferedImage);
    try {
      assertEquals(2, thumbnails.size());
      for (int i = 0; i < thumbnails.size(); i++) {
        final Thumbnail thumbnail = thumbnails.get(i);
        assertEquals("image/png", thumbnail.getMimeType());
        final int thumbnailWidth = i == 0 ? 200 : 400;
        final BufferedImage encoded = assertThumbnail(thumbnail, thumbnailWidth, thumbnailWidth / 2);
        assertFalse(encoded.getColorModel().hasAlpha());
        assertEquals(Color.RED.getRGB(), encoded.getRGB(encoded.getWidth() / 4, encoded.getHeight() / 2));
        assertEquals(Color.WHITE.getRGB(), encoded.getRGB(encoded.getWidth() * 3 / 4, encoded.getHeight() / 2));
      }
      assertEquals(0, bufferedImage.getRGB(bufferedImage.getWidth() * 3 / 4, bufferedImage.getHeight() / 2) >>> 24);
    } finally {
      thumbnails.forEach(Thumbnail::close);
      bufferedImage.flush();
    }
  }

  @Test
  void clearsPartiallyEncodedPdfThumbnailsOnFailure() throws Exception {
    final ThumbnailGenerator failingGenerator = spy(new ThumbnailGenerator());
    final Map<ThumbnailKind, ThumbnailImpl> thumbnails = failingGenerator.prepareThumbnails("pdf", "image/png");
    doReturn(thumbnails).when(failingGenerator).prepareThumbnails("pdf", "image/png");
    final IOException ioException = new IOException("Encoding failed");
    doCallRealMethod().doThrow(ioException).when(failingGenerator).encodeImage(any(BufferedImage.class), anyString());
    final BufferedImage page = createImageWithRedLeftHalf(800, 400, false);
    try {
      final MediaExtractionException mediaExtractionException = assertThrows(MediaExtractionException.class,
          () -> failingGenerator.generatePdfThumbnails("pdf", page));
      assertSame(ioException, mediaExtractionException.getCause());
      assertTrue(thumbnails.values().stream().noneMatch(ThumbnailImpl::hasContent));
    } finally {
      thumbnails.values().forEach(ThumbnailImpl::close);
      page.flush();
    }
  }
}
