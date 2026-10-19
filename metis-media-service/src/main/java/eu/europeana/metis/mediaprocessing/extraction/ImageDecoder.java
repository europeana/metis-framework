package eu.europeana.metis.mediaprocessing.extraction;

import eu.europeana.metis.schema.jibx.ColorSpaceType;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileCacheImageInputStream;
import javax.imageio.stream.ImageInputStream;

/**
 * Reads original image metadata and decodes pixels at a suitable resolution for thumbnails.
 */
class ImageDecoder {

  private static final int THUMBNAIL_OVERSAMPLE_FACTOR = 2;

  /**
   * Decodes the image or renders an SVG.
   * <p>
   * Note: The caller must close the returned image.
   *
   * @param content The original image file.
   * @param maximumWidth The largest requested thumbnail width.
   * @return Decoded pixels together with the original dimensions and colour space.
   * @throws IOException If no reader supports the input, or reading or rendering fails.
   */
  @SuppressWarnings("java:S2093")
  // Ownership of the returned image passes to the caller, who should close it properly.
  DecodedImageAndMetadata decode(File content, int maximumWidth) throws IOException {
    BufferedImage decodedBufferedImage = null;
    try (InputStream source = Files.newInputStream(content.toPath());
        ImageInputStream input = new FileCacheImageInputStream(source, null)) {
      final Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
      if (!readers.hasNext()) {
        throw new IOException("No ImageIO reader supports image: " + content);
      }
      final ImageReader reader = readers.next();
      try {
        reader.setInput(input);
        final int originalWidth = reader.getWidth(0);
        final int originalHeight = reader.getHeight(0);
        final ColorSpaceType colorSpace = new ImageColorSpaceReader().read(reader);
        if ("svg".equalsIgnoreCase(reader.getFormatName())) {
          final int renderWidth = maximumWidth * THUMBNAIL_OVERSAMPLE_FACTOR;
          final int renderHeight = Math.max(1, (int) Math.round((double) originalHeight * renderWidth / originalWidth));
          decodedBufferedImage = new SvgRasterizer().render(content, renderWidth, renderHeight);
        } else {
          final ImageReadParam readParam = reader.getDefaultReadParam();
          final int subsampling = Math.max(1, originalWidth / (maximumWidth * THUMBNAIL_OVERSAMPLE_FACTOR));
          readParam.setSourceSubsampling(subsampling, subsampling, 0, 0);
          decodedBufferedImage = reader.read(0, readParam);
        }
        return new DecodedImageAndMetadata(decodedBufferedImage, originalWidth, originalHeight, colorSpace);
      } finally {
        reader.dispose();
      }
    } catch (IOException | RuntimeException e) {
      // Ownership only passes to the caller after the reader and streams have closed successfully.
      if (decodedBufferedImage != null) {
        decodedBufferedImage.flush();
      }
      throw e;
    }
  }

  /**
   * Owns the decoded image buffer. Its dimensions may differ from the original dimensions.
   */
  record DecodedImageAndMetadata(BufferedImage image, int originalWidth, int originalHeight, ColorSpaceType colorSpace)
      implements AutoCloseable {

    @Override
    public void close() {
      image.flush();
    }
  }
}
