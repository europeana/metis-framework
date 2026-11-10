package eu.europeana.metis.mediaprocessing.extraction.image;

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
public class ImageDecoder {

  // Retain extra pixels at the working resolution to preserve detail during final thumbnail resampling.
  private static final int THUMBNAIL_OVERSAMPLE_FACTOR = 2;

  /**
   * Reads metadata from the first image or page, then decodes raster pixels, or renders an SVG for thumbnail generation.
   * <p>
   * Decoding follows the thumbnail generator's width-based sizing policy: height is derived from the original aspect ratio, with
   * no independent height limit. Pixels are retained at approximately twice the largest thumbnail width to provide detail for the
   * final resampling step. Raster subsampling uses integer steps, so its decoded width can be larger.
   * <p>
   * The caller must close the returned image to release its pixel buffer. This method disposes of the reader and closes its
   * streams and releases any decoded buffer if those operations fail before ownership passes to the caller.
   *
   * @param content The original image file.
   * @param maximumWidth The largest requested thumbnail width.
   * @return Decoded pixels together with the original dimensions and colour space.
   * @throws IOException If no reader supports the input, or reading or rendering fails.
   */
  @SuppressWarnings("java:S2093")
  // Ownership of the returned image passes to the caller, who should close it properly.
  public DecodedImageAndMetadata decode(File content, int maximumWidth) throws IOException {
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
        // Capture source metadata before decoding can change pixel dimensions or convert the colour space.
        final int originalWidth = reader.getWidth(0);
        final int originalHeight = reader.getHeight(0);
        final ColorSpaceType colorSpace = new ImageColorSpaceReader().read(reader);
        if ("svg".equalsIgnoreCase(reader.getFormatName())) {
          decodedBufferedImage = renderSvg(content, originalWidth, originalHeight, maximumWidth);
        } else {
          decodedBufferedImage = decodeRaster(reader, originalWidth, maximumWidth);
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
   * Renders vector content directly at the working resolution instead of rasterizing at its original dimensions first.
   */
  private BufferedImage renderSvg(File content, int originalWidth, int originalHeight, int maximumWidth) throws IOException {
    final int renderWidth = maximumWidth * THUMBNAIL_OVERSAMPLE_FACTOR;
    // Keep the original aspect ratio and at least one pixel of height for very wide images.
    final int renderHeight = Math.max(1, (int) Math.round((double) originalHeight * renderWidth / originalWidth));
    return new SvgRasterizer().render(content, renderWidth, renderHeight);
  }

  /**
   * Requests evenly spaced source pixels to reduce the working buffer while retaining detail for thumbnail resampling.
   */
  private BufferedImage decodeRaster(ImageReader reader, int originalWidth, int maximumWidth) throws IOException {
    final ImageReadParam readParam = reader.getDefaultReadParam();
    // Round down to retain at least the requested working width. A step of one preserves small rasters without upscaling.
    final int subsampling = Math.max(1, originalWidth / (maximumWidth * THUMBNAIL_OVERSAMPLE_FACTOR));
    // Use the same step on both axes to preserve proportions, apart from rounding to whole pixels at the image edges.
    readParam.setSourceSubsampling(subsampling, subsampling, 0, 0);
    return reader.read(0, readParam);
  }

  /**
   * Owns the decoded image buffer. Its dimensions may differ from the original dimensions, which describe the source image.
   * Closing this result releases the decoded pixel buffer.
   */
  public record DecodedImageAndMetadata(BufferedImage image, int originalWidth, int originalHeight, ColorSpaceType colorSpace)
      implements AutoCloseable {

    @Override
    public void close() {
      image.flush();
    }
  }
}
