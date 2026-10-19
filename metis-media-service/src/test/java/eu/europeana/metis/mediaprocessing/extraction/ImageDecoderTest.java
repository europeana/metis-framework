package eu.europeana.metis.mediaprocessing.extraction;

import static eu.europeana.metis.mediaprocessing.extraction.ImageTestUtils.copyFixture;
import static eu.europeana.metis.mediaprocessing.extraction.ImageTestUtils.createImageWithRedLeftHalf;
import static eu.europeana.metis.mediaprocessing.extraction.ImageTestUtils.writeImage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.metis.mediaprocessing.extraction.ImageDecoder.DecodedImageAndMetadata;
import eu.europeana.metis.schema.jibx.ColorSpaceType;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ImageDecoderTest {

  @TempDir
  Path directory;

  private final ImageDecoder imageDecoder = new ImageDecoder();

  @ParameterizedTest(name = "maximum width {0}: decoded {1} x {2}")
  @CsvSource({"200,401,200", "400,801,400"})
  void subsamplesPixelsWhileKeepingOriginalDimensions(int maximumWidth, int decodedWidth, int decodedHeight)
      throws Exception {
    final Path source = writeImage(directory.resolve("large.jpeg"), createImageWithRedLeftHalf(3201, 1600, false), "jpeg");
    try (DecodedImageAndMetadata decoded = imageDecoder.decode(source.toFile(), maximumWidth)) {
      assertEquals(3201, decoded.originalWidth());
      assertEquals(1600, decoded.originalHeight());
      assertEquals(decodedWidth, decoded.image().getWidth());
      assertEquals(decodedHeight, decoded.image().getHeight());
    }
  }

  @Test
  void keepsSmallRasterAtItsOriginalSize() throws Exception {
    final Path source = writeImage(directory.resolve("small.png"), createImageWithRedLeftHalf(125, 75, true), "png");
    try (DecodedImageAndMetadata decoded = imageDecoder.decode(source.toFile(), 400)) {
      assertEquals(125, decoded.image().getWidth());
      assertEquals(75, decoded.image().getHeight());
      assertEquals(0, decoded.image().getRGB(100, 35) >>> 24);
    }
  }

  @Test
  void retainsCmykSourceMetadataWhenPixelsAreConvertedToRgb() throws Exception {
    final Path source = copyFixture(directory, "color-spaces/cmyk.jpeg");
    try (DecodedImageAndMetadata decoded = imageDecoder.decode(source.toFile(), 400)) {
      assertEquals(ColorSpaceType.CMYK, decoded.colorSpace());
      assertTrue(decoded.image().getColorModel().getColorSpace().isCS_sRGB());
    }
  }

  @ParameterizedTest
  @ValueSource(ints = {BufferedImage.TYPE_BYTE_GRAY, BufferedImage.TYPE_USHORT_GRAY})
  void readsGrayscaleWithoutLosingSourceBitDepth(int imageType) throws Exception {
    final BufferedImage bufferedImage = new BufferedImage(20, 10, imageType);
    final Path source = writeImage(directory.resolve("gray.png"), bufferedImage, "png");
    try (DecodedImageAndMetadata decoded = imageDecoder.decode(source.toFile(), 400)) {
      assertEquals(ColorSpaceType.GRAYSCALE, decoded.colorSpace());
      assertEquals(bufferedImage.getSampleModel().getSampleSize(0), decoded.image().getSampleModel().getSampleSize(0));
    }
  }

  @Test
  void rendersSvgAtBoundedSizeAndKeepsOriginalDimensions() throws Exception {
    final Path source = directory.resolve("source.svg");
    Files.writeString(source, """
        <svg xmlns="http://www.w3.org/2000/svg" width="8000" height="4000" viewBox="0 0 80 40">
          <rect width="80" height="40" fill="red"/>
        </svg>
        """);
    try (DecodedImageAndMetadata decoded = imageDecoder.decode(source.toFile(), 400)) {
      assertEquals(8000, decoded.originalWidth());
      assertEquals(4000, decoded.originalHeight());
      assertEquals(800, decoded.image().getWidth());
      assertEquals(400, decoded.image().getHeight());
      assertEquals(ColorSpaceType.S_RGB, decoded.colorSpace());
      assertEquals(Color.RED.getRGB(), decoded.image().getRGB(400, 200));
    }
  }

  @Test
  void readsOnlyFirstTiffPage() throws Exception {
    final Path source = directory.resolve("pages.tiff");
    final ImageWriter writer = ImageIO.getImageWritersByFormatName("tiff").next();
    try (ImageOutputStream output = ImageIO.createImageOutputStream(source.toFile())) {
      writer.setOutput(output);
      writer.prepareWriteSequence(null);
      writer.writeToSequence(new IIOImage(createImageWithRedLeftHalf(800, 400, false), null, null), null);
      writer.writeToSequence(new IIOImage(new BufferedImage(600, 900, BufferedImage.TYPE_INT_RGB), null, null), null);
      writer.endWriteSequence();
    } finally {
      writer.dispose();
    }
    try (DecodedImageAndMetadata decoded = imageDecoder.decode(source.toFile(), 400)) {
      assertEquals(800, decoded.originalWidth());
      assertEquals(400, decoded.originalHeight());
      assertEquals(Color.RED.getRGB(), decoded.image().getRGB(200, 200));
    }
  }

  @Test
  void rejectsTruncatedImage() throws Exception {
    final Path source = writeImage(directory.resolve("truncated.png"), createImageWithRedLeftHalf(80, 40, false), "png");
    Files.write(source, Arrays.copyOf(Files.readAllBytes(source), 24));
    assertThrows(IOException.class, () -> {
      try (DecodedImageAndMetadata decoded = imageDecoder.decode(source.toFile(), 400)) {
        assertNotNull(decoded.image());
      }
    });
  }

  @Test
  void rejectsInputWithoutAnImageReader() throws Exception {
    final Path source = directory.resolve("unsupported.image");
    Files.writeString(source, "not a supported image");
    assertThrows(IOException.class, () -> {
      try (DecodedImageAndMetadata decoded = imageDecoder.decode(source.toFile(), 400)) {
        assertNotNull(decoded.image());
      }
    });
  }
}
