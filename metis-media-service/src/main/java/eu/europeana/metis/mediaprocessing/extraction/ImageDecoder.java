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

/** Reads original image metadata and decodes pixels at a suitable resolution for thumbnails. */
class ImageDecoder {

  // Keep some resolution for the final resampling instead of sampling directly to thumbnail size.
  private static final int SUBSAMPLING_OVERSAMPLE = 2;

  /**
   * Decodes the first image or renders an SVG. The caller must close the returned image.
   *
   * @param content The original image file.
   * @param maximumWidth The largest requested thumbnail width.
   * @return Decoded pixels together with the original dimensions and colour space.
   * @throws IOException If no reader supports the input, or reading or rendering fails.
   */
  DecodedImage decode(File content, int maximumWidth) throws IOException {
    BufferedImage decoded = null;
    // A file-backed stream keeps compressed data off the heap. With unknown stream length,
    // TwelveMonkeys uses its absolute allocation limit instead of a compression-ratio limit
    // that rejects valid, very compact images (for example, solid-colour lossless WebP).
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
          // Use Batik directly for rendering: the ImageIO adapter rounds fractional viewBoxes.
          final int renderWidth = maximumWidth * SUBSAMPLING_OVERSAMPLE;
          final int renderHeight = Math.max(1, (int) Math.round((double) originalHeight * renderWidth / originalWidth));
          decoded = new SvgRasterizer().render(content, renderWidth, renderHeight);
        } else {
          final ImageReadParam readParam = reader.getDefaultReadParam();
          final int subsampling = Math.max(1, originalWidth / (maximumWidth * SUBSAMPLING_OVERSAMPLE));
          readParam.setSourceSubsampling(subsampling, subsampling, 0, 0);
          decoded = reader.read(0, readParam);
        }
        return new DecodedImage(decoded, originalWidth, originalHeight, colorSpace);
      } finally {
        reader.dispose();
      }
    } catch (IOException | RuntimeException e) {
      // Ownership only passes to the caller after the reader and streams have closed successfully.
      if (decoded != null) {
        decoded.flush();
      }
      throw e;
    }
  }

  /** Owns the decoded image buffer; its dimensions may differ from the original dimensions. */
  record DecodedImage(BufferedImage image, int originalWidth, int originalHeight, ColorSpaceType colorSpace)
      implements AutoCloseable {

    @Override
    public void close() {
      image.flush();
    }
  }
}
