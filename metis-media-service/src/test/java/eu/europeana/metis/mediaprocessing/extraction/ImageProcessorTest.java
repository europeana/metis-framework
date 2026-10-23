package eu.europeana.metis.mediaprocessing.extraction;

import static eu.europeana.metis.mediaprocessing.extraction.ImageTestUtils.assertThumbnail;
import static eu.europeana.metis.mediaprocessing.extraction.ImageTestUtils.copyFile;
import static eu.europeana.metis.mediaprocessing.extraction.ImageTestUtils.createImageWithRedLeftHalf;
import static eu.europeana.metis.mediaprocessing.extraction.ImageTestUtils.writeImage;
import static eu.europeana.metis.mediaprocessing.extraction.PaletteAnalyzer.OPAQUE_ALPHA;
import static eu.europeana.metis.mediaprocessing.extraction.PaletteAnalyzerTest.BLUE_HEX;
import static eu.europeana.metis.mediaprocessing.extraction.PaletteAnalyzerTest.RED_HEX;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.model.ImageResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResultImpl;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import eu.europeana.metis.mediaprocessing.model.ThumbnailImpl;
import eu.europeana.metis.mediaprocessing.model.ThumbnailKind;
import eu.europeana.metis.schema.jibx.ColorSpaceType;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class ImageProcessorTest {

  @TempDir
  Path directory;

  private final ImageProcessor processor = new ImageProcessor(new ThumbnailGenerator());

  @Test
  void downloadsResourceForFullProcessing() {
    assertTrue(processor.downloadResourceForFullProcessing());
  }

  @Test
  void copiesOnlyProvidedMetadataWithoutProcessingPixels() throws Exception {
    final Resource resource = mock(Resource.class);
    doReturn(12345L).when(resource).getProvidedFileSize();
    doReturn("image-url").when(resource).getResourceUrl();
    try (ResourceExtractionResultImpl resourceExtractionResult = processor.copyMetadata(resource, "image/png")) {
      final ImageResourceMetadata imageResourceMetadata = assertInstanceOf(ImageResourceMetadata.class,
          resourceExtractionResult.getOriginalMetadata());
      assertEquals("image/png", imageResourceMetadata.getMimeType());
      assertEquals(12345L, imageResourceMetadata.getContentSize());
      assertEquals("image-url", imageResourceMetadata.getResourceUrl());
      assertNull(resourceExtractionResult.getThumbnails());
      assertTrue(imageResourceMetadata.getThumbnailTargetNames().isEmpty());
      assertTrue(imageResourceMetadata.getDominantColors().isEmpty());
      assertNull(imageResourceMetadata.getColorSpace());
      assertNull(imageResourceMetadata.getWidth());
      assertNull(imageResourceMetadata.getHeight());
    }
  }

  @ParameterizedTest(name = "{0} ({1})")
  @MethodSource("supportedImageFormats")
  void extractsOriginalMetadataAndGeneratesStandardThumbnails(String filename, String mimeType,
      int width, int height, String outputMimeType) throws Exception {
    final Path source = copyFile(directory, "processing/" + filename);
    try (ResourceExtractionResultImpl resourceExtractionResult = processor.extractMetadata(resource(source), mimeType, false)) {
      final ImageResourceMetadata imageResourceMetadata = assertInstanceOf(ImageResourceMetadata.class,
          resourceExtractionResult.getOriginalMetadata());
      assertEquals(width, imageResourceMetadata.getWidth());
      assertEquals(height, imageResourceMetadata.getHeight());
      assertEquals(Files.size(source), imageResourceMetadata.getContentSize());
      assertEquals(mimeType, imageResourceMetadata.getMimeType());
      assertEquals("image-url", imageResourceMetadata.getResourceUrl());
      assertEquals(ColorSpaceType.S_RGB, imageResourceMetadata.getColorSpace());
      assertEquals(2, resourceExtractionResult.getThumbnails().size());
      final Set<String> targetNames = new HashSet<>();
      for (int i = 0; i < 2; i++) {
        final Thumbnail thumbnail = resourceExtractionResult.getThumbnails().get(i);
        final int thumbnailWidth = Math.min(width, i == 0 ? 200 : 400);
        assertEquals(outputMimeType, thumbnail.getMimeType());
        final BufferedImage bufferedImage = assertThumbnail(thumbnail, thumbnailWidth,
            Math.max(1, (int) Math.round((double) height * thumbnailWidth / width)));
        assertTrue(new Color(bufferedImage.getRGB(bufferedImage.getWidth() / 4, bufferedImage.getHeight() / 2)).getRed() > 240);
        assertTrue(
            new Color(bufferedImage.getRGB(bufferedImage.getWidth() * 3 / 4, bufferedImage.getHeight() / 2)).getBlue() > 240);
        targetNames.add(thumbnail.getTargetName());
      }
      assertEquals(targetNames, imageResourceMetadata.getThumbnailTargetNames());
    }
  }

  private static Stream<Arguments> supportedImageFormats() {
    return Stream.of(
        Arguments.of("source.jpeg", "image/jpeg", 410, 205, "image/jpeg"),
        Arguments.of("source.png", "image/png", 410, 205, "image/png"),
        Arguments.of("source.gif", "image/gif", 410, 205, "image/jpeg"),
        Arguments.of("source.bmp", "image/bmp", 410, 205, "image/jpeg"),
        Arguments.of("source.bmp", "image/x-ms-bmp", 410, 205, "image/jpeg"),
        Arguments.of("source.tiff", "image/tiff", 410, 205, "image/jpeg"),
        Arguments.of("source.psd", "image/vnd.adobe.photoshop", 125, 62, "image/jpeg"),
        Arguments.of("source.pict", "image/x-pict", 410, 205, "image/jpeg"),
        Arguments.of("lossy.webp", "image/webp", 810, 405, "image/jpeg"),
        Arguments.of("lossless.webp", "image/webp", 810, 405, "image/jpeg"),
        Arguments.of("source.jp2", "image/jp2", 810, 405, "image/jpeg"),
        Arguments.of("source.j2k", "image/x-jp2-codestream", 810, 405, "image/jpeg")
    );
  }

  @ParameterizedTest(name = "{0} at width {1}")
  @CsvSource({"png,199", "png,200", "png,201", "png,399", "png,400",
      "jpeg,199", "jpeg,200", "jpeg,201", "jpeg,399", "jpeg,400"})
  void reusesMatchingOriginalBytesBelowThumbnailWidthWithoutUpscaling(String format, int width) throws Exception {
    final Path source = writeImage(directory.resolve("small." + format), createImageWithRedLeftHalf(width, 75, false), format);
    final byte[] original = Files.readAllBytes(source);
    try (ResourceExtractionResultImpl resourceExtractionResult = processImage(source, "image/" + format)) {
      for (int i = 0; i < 2; i++) {
        final Thumbnail thumbnail = resourceExtractionResult.getThumbnails().get(i);
        final int maximumWidth = i == 0 ? 200 : 400;
        final int thumbnailWidth = Math.min(width, maximumWidth);
        assertThumbnail(thumbnail, thumbnailWidth, (int) Math.round(75.0 * thumbnailWidth / width));
        if (width < maximumWidth) {
          try (var input = thumbnail.getContentStream()) {
            assertArrayEquals(original, input.readAllBytes());
          }
        }
      }
    }
  }

  @Test
  void closingOneThumbnailDoesNotClearSharedOriginalBytes() throws Exception {
    final Path source = writeImage(directory.resolve("small.png"), createImageWithRedLeftHalf(125, 75, false), "png");
    try (ResourceExtractionResultImpl resourceExtractionResult = processImage(source, "image/png")) {
      final Thumbnail medium = resourceExtractionResult.getThumbnails().getFirst();
      medium.close();
      assertFalse(medium.hasContent());
      assertThumbnail(resourceExtractionResult.getThumbnails().get(1), 125, 75);
    }
  }

  @ParameterizedTest
  @ValueSource(ints = {125, 300})
  void convertsSmallRasterToJpegWithoutUpscaling(int width) throws Exception {
    final Path source = writeImage(directory.resolve("small.tiff"), createImageWithRedLeftHalf(width, 75, false), "tiff");
    try (ResourceExtractionResultImpl resourceExtractionResult = processImage(source, "image/tiff")) {
      final Thumbnail medium = resourceExtractionResult.getThumbnails().get(0);
      final Thumbnail large = resourceExtractionResult.getThumbnails().get(1);
      assertEquals("image/jpeg", medium.getMimeType());
      assertEquals("image/jpeg", large.getMimeType());
      final int mediumWidth = Math.min(width, 200);
      assertThumbnail(medium, mediumWidth, (int) Math.round(75.0 * mediumWidth / width));
      assertThumbnail(large, width, 75);
    }
  }

  @ParameterizedTest
  @CsvSource({"small-lossless.webp,125,75", "small-lossy.webp,300,150"})
  void convertsSmallWebpToJpegWithoutUpscaling(String fixture, int width, int height) throws Exception {
    final Path source = copyFile(directory, "processing/" + fixture);
    try (ResourceExtractionResultImpl resourceExtractionResult = processImage(source, "image/webp")) {
      final Thumbnail medium = resourceExtractionResult.getThumbnails().get(0);
      final Thumbnail large = resourceExtractionResult.getThumbnails().get(1);
      assertEquals("image/jpeg", medium.getMimeType());
      assertEquals("image/jpeg", large.getMimeType());
      final int mediumWidth = Math.min(width, 200);
      assertThumbnail(medium, mediumWidth, (int) Math.round((double) height * mediumWidth / width));
      assertThumbnail(large, width, height);
    }
  }

  @ParameterizedTest(name = "{0} x {1} source")
  @CsvSource({"3201,1600,100,200", "1600,3201,400,800"})
  void preservesOriginalDimensionsAndAspectRatioForSubsampledImages(int width, int height,
      int mediumHeight, int largeHeight) throws Exception {
    final Path source = writeImage(directory.resolve("large.png"), createImageWithRedLeftHalf(width, height, false), "png");
    try (ResourceExtractionResultImpl resourceExtractionResult = processImage(source, "image/png")) {
      assertThumbnail(resourceExtractionResult.getThumbnails().get(0), 200, mediumHeight);
      assertThumbnail(resourceExtractionResult.getThumbnails().get(1), 400, largeHeight);
      final ImageResourceMetadata imageResourceMetadata = (ImageResourceMetadata) resourceExtractionResult.getOriginalMetadata();
      assertEquals(width, imageResourceMetadata.getWidth());
      assertEquals(height, imageResourceMetadata.getHeight());
    }
  }

  @ParameterizedTest
  @MethodSource("sourceColorSpaces")
  void reportsOriginalColorSpaceAlongsideEncodedThumbnails(String filename, String mimeType, ColorSpaceType expected)
      throws Exception {
    final Path source = copyFile(directory, "color-spaces/" + filename);
    try (ResourceExtractionResultImpl resourceExtractionResult = processImage(source, mimeType)) {
      final ImageResourceMetadata imageResourceMetadata = (ImageResourceMetadata) resourceExtractionResult.getOriginalMetadata();
      assertEquals(expected, imageResourceMetadata.getColorSpace());
      assertEquals("image/jpeg", resourceExtractionResult.getThumbnails().get(1).getMimeType());
      assertThumbnail(resourceExtractionResult.getThumbnails().get(1), 8, 4);
    }
  }

  private static Stream<Arguments> sourceColorSpaces() {
    return Stream.of(
        Arguments.of("linear-rgb.jpeg", "image/jpeg", ColorSpaceType.RGB),
        Arguments.of("cmyk.jpeg", "image/jpeg", ColorSpaceType.CMYK),
        Arguments.of("grayscale.tiff", "image/tiff", ColorSpaceType.GRAYSCALE)
    );
  }

  @ParameterizedTest
  @ValueSource(ints = {125, 800})
  void preservesPngTransparencyForReusedAndResizedImages(int width) throws Exception {
    final Path source = writeImage(directory.resolve("transparent.png"), createImageWithRedLeftHalf(width, 100, true), "png");
    try (ResourceExtractionResultImpl resourceExtractionResult = processImage(source, "image/png")) {
      final int thumbnailWidth = Math.min(width, 400);
      final BufferedImage bufferedImage = assertThumbnail(resourceExtractionResult.getThumbnails().get(1), thumbnailWidth,
          (int) Math.round(100.0 * thumbnailWidth / width));
      assertEquals(0, bufferedImage.getRGB(thumbnailWidth * 3 / 4, bufferedImage.getHeight() / 2) >>> 24);
      assertEquals(OPAQUE_ALPHA, bufferedImage.getRGB(thumbnailWidth / 4, bufferedImage.getHeight() / 2) >>> 24);
      final ImageResourceMetadata metadata = (ImageResourceMetadata) resourceExtractionResult.getOriginalMetadata();
      assertEquals("#" + RED_HEX, metadata.getDominantColors().getFirst());
    }
  }

  @Test
  void excludesInvisibleColorsFromPngMetadata() throws Exception {
    final BufferedImage bufferedImage = createImageWithRedLeftHalf(125, 100, true);
    final Path source = writeImage(directory.resolve("transparent.png"), bufferedImage, "png");
    try (ResourceExtractionResultImpl resourceExtractionResult = processImage(source, "image/png")) {
      final ImageResourceMetadata imageResourceMetadata = (ImageResourceMetadata) resourceExtractionResult.getOriginalMetadata();
      assertEquals(List.of("#" + RED_HEX), imageResourceMetadata.getDominantColors());
    }
  }

  @Test
  void flattensTransparentGifOnWhite() throws Exception {
    final Path path = writeImage(directory.resolve("transparent.gif"), createImageWithRedLeftHalf(125, 75, true), "gif");
    try (ResourceExtractionResultImpl result = processImage(path, "image/gif")) {
      final BufferedImage image = assertThumbnail(result.getThumbnails().get(1), 125, 75);
      final Color background = new Color(image.getRGB(100, 35));
      assertTrue(background.getRed() > 240 && background.getGreen() > 240 && background.getBlue() > 240);
      final ImageResourceMetadata imageResourceMetadata = (ImageResourceMetadata) result.getOriginalMetadata();
      assertEquals(List.of("#" + RED_HEX), imageResourceMetadata.getDominantColors());
    }
  }

  @Test
  void includesDominantColorsWithoutUpscalingTinyImageForAnalysis() throws Exception {
    final Path path = writeImage(directory.resolve("tiny.png"), createImageWithRedLeftHalf(2, 1, false), "png");
    try (ResourceExtractionResultImpl resourceExtractionResult = processImage(path, "image/png")) {
      final ImageResourceMetadata imageResourceMetadata = (ImageResourceMetadata) resourceExtractionResult.getOriginalMetadata();
      assertEquals(List.of("#" + RED_HEX, "#" + BLUE_HEX), imageResourceMetadata.getDominantColors());
    }
  }

  @Test
  void rasterizesSmallSvgIntoJpegThumbnails() throws Exception {
    final Path path = directory.resolve("small.svg");
    Files.writeString(path, """
        <svg xmlns="http://www.w3.org/2000/svg" width="125" height="75" viewBox="0 0 12.5 7.5">
          <rect width="12.5" height="7.5" fill="red"/>
        </svg>
        """);
    try (ResourceExtractionResultImpl resourceExtractionResult = processImage(path, "image/svg+xml")) {
      final ImageResourceMetadata imageResourceMetadata = (ImageResourceMetadata) resourceExtractionResult.getOriginalMetadata();
      assertEquals(125, imageResourceMetadata.getWidth());
      assertEquals(75, imageResourceMetadata.getHeight());
      assertEquals("image/jpeg", resourceExtractionResult.getThumbnails().get(1).getMimeType());
      assertThumbnail(resourceExtractionResult.getThumbnails().get(0), 200, 120);
      assertThumbnail(resourceExtractionResult.getThumbnails().get(1), 400, 240);
    }
  }

  @Test
  void clearsEncodedThumbnailsWhenLargestResizeFails() throws Exception {
    final Path path = copyFile(directory, "processing/source.png");
    final ThumbnailGenerator thumbnailGenerator = spy(new ThumbnailGenerator());
    final EnumMap<ThumbnailKind, ThumbnailImpl> thumbnails = thumbnailGenerator.prepareThumbnails("test", "image/png");
    doReturn(thumbnails).when(thumbnailGenerator).prepareThumbnails("test", "image/png");
    final IllegalStateException cause = new IllegalStateException("Resize failed");
    doThrow(cause).when(thumbnailGenerator).resize(any(BufferedImage.class), anyInt(), anyInt(), eq(ThumbnailKind.LARGE));
    final MediaExtractionException failure = assertThrows(MediaExtractionException.class,
        () -> {
          try (ResourceExtractionResultImpl resourceExtractionResult = new ImageProcessor(thumbnailGenerator).processImage("test",
              "image/png", path.toFile(), Files.size(path))) {
            assertNotNull(resourceExtractionResult);
          }
        });
    assertSame(cause, failure.getCause());
    assertTrue(thumbnails.values().stream().noneMatch(ThumbnailImpl::hasContent));
  }

  @Test
  void clearsEncodedThumbnailsWhenSecondEncodingFails() throws Exception {
    final Path path = copyFile(directory, "processing/source.png");
    final ThumbnailGenerator thumbnailGenerator = spy(new ThumbnailGenerator());
    final var thumbnails = thumbnailGenerator.prepareThumbnails("test", "image/png");
    doReturn(thumbnails).when(thumbnailGenerator).prepareThumbnails("test", "image/png");
    final IOException cause = new IOException("Encoding failed");
    doCallRealMethod().doThrow(cause).when(thumbnailGenerator).encodeImage(any(BufferedImage.class), anyString());
    final MediaExtractionException failure = assertThrows(MediaExtractionException.class,
        () -> {
          try (ResourceExtractionResultImpl resourceExtractionResult = new ImageProcessor(thumbnailGenerator).processImage("test",
              "image/png", path.toFile(), Files.size(path))) {
            assertNotNull(resourceExtractionResult);
          }
        });
    assertSame(cause, failure.getCause());
    assertTrue(thumbnails.values().stream().noneMatch(ThumbnailImpl::hasContent));
  }

  @Test
  void rejectsMissingContent() {
    assertThrows(MediaExtractionException.class, () -> {
          try (ResourceExtractionResultImpl resourceExtractionResult = processor.processImage("test", "image/png", null, null)) {
            assertNotNull(resourceExtractionResult);
          }
        }
    );
  }

  @Test
  void rejectsNonImageMimeType() {
    assertThrows(MediaExtractionException.class, () -> {
          try (ResourceExtractionResultImpl resourceExtractionResult = processor.processImage("test", "application/pdf",
              directory.resolve("unused").toFile(), null)) {
            assertNotNull(resourceExtractionResult);
          }
        }
    );
  }

  @Test
  void wrapsUnsupportedImageAsExtractionFailure() throws Exception {
    final Path path = directory.resolve("unsupported.image");
    Files.writeString(path, "not a supported image");
    final MediaExtractionException mediaExtractionException = assertThrows(MediaExtractionException.class, () -> {
          try (ResourceExtractionResultImpl resourceExtractionResult = processImage(path, "image/unknown")) {
            assertNotNull(resourceExtractionResult);
          }
        }
    );
    assertInstanceOf(IOException.class, mediaExtractionException.getCause());
  }

  @Test
  void rejectsResourceWithoutContent() {
    assertThrows(MediaExtractionException.class, () -> processor.extractMetadata(mock(Resource.class), "image/png", false));
  }

  @Test
  void wrapsResourceContentCheckFailure() throws Exception {
    final Resource resource = mock(Resource.class);
    final IOException cause = new IOException("Cannot inspect resource");
    doThrow(cause).when(resource).hasContent();
    final MediaExtractionException mediaExtractionException = assertThrows(MediaExtractionException.class,
        () -> processor.extractMetadata(resource, "image/png", false));
    assertSame(cause, mediaExtractionException.getCause());
  }

  @Test
  void wrapsResourceSizeFailure() throws Exception {
    final Resource resource = mock(Resource.class);
    doReturn(true).when(resource).hasContent();
    final IOException cause = new IOException("Cannot read resource size");
    doThrow(cause).when(resource).getContentSize();
    final MediaExtractionException mediaExtractionException = assertThrows(MediaExtractionException.class,
        () -> processor.extractMetadata(resource, "image/png", false));
    assertSame(cause, mediaExtractionException.getCause());
  }

  @Test
  void processesDifferentFormatsConcurrentlyWithOneProcessor() throws Exception {
    final List<Path> sources = List.of(
        copyFile(directory, "processing/source.png"),
        copyFile(directory, "processing/source.jpeg"),
        copyFile(directory, "processing/lossless.webp"),
        copyFile(directory, "processing/source.jp2"));
    final List<String> mimeTypes = List.of("image/png", "image/jpeg", "image/webp", "image/jp2");
    final List<Callable<Void>> jobs = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
      final int index = i % sources.size();
      final String url = "image-" + i;
      jobs.add(() -> {
        final Path path = sources.get(index);
        try (ResourceExtractionResultImpl resourceExtractionResult =
            processor.processImage(url, mimeTypes.get(index), path.toFile(), Files.size(path))) {
          assertEquals(url, resourceExtractionResult.getOriginalMetadata().getResourceUrl());
          assertThumbnail(resourceExtractionResult.getThumbnails().get(0), 200, 100);
          assertThumbnail(resourceExtractionResult.getThumbnails().get(1), 400, 200);
          final ImageResourceMetadata imageResourceMetadata = (ImageResourceMetadata) resourceExtractionResult.getOriginalMetadata();
          assertEquals(Set.of("#" + RED_HEX, "#" + BLUE_HEX), new HashSet<>(imageResourceMetadata.getDominantColors().subList(0, 2)));
        }
        return null;
      });
    }
    try (ExecutorService executorService = Executors.newFixedThreadPool(4)) {
      for (Future<Void> future : executorService.invokeAll(jobs)) {
        future.get();
      }
    }
  }

  private ResourceExtractionResultImpl processImage(Path source, String mimeType) throws Exception {
    return processor.processImage("test", mimeType, source.toFile(), Files.size(source));
  }

  private Resource resource(Path source) throws IOException {
    final Resource resource = mock(Resource.class);
    doReturn(true).when(resource).hasContent();
    doReturn(source.toFile()).when(resource).getContentFile();
    doReturn(Files.size(source)).when(resource).getContentSize();
    doReturn("image-url").when(resource).getResourceUrl();
    return resource;
  }
}
