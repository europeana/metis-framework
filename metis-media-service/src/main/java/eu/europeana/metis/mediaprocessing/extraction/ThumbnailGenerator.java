package eu.europeana.metis.mediaprocessing.extraction;

import com.twelvemonkeys.image.ResampleOp;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import eu.europeana.metis.mediaprocessing.model.ThumbnailImpl;
import eu.europeana.metis.mediaprocessing.model.ThumbnailKind;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageOutputStream;

/**
 * Resizes images and encodes thumbnails with ImageIO and TwelveMonkeys. The caller owns input images and returned thumbnails, and
 * partially generated thumbnails are closed here on failure.
 */
public class ThumbnailGenerator {

  private static final String PNG_MIME_TYPE = "image/png";
  private static final String JPEG_MIME_TYPE = "image/jpeg";

  /**
   * Generates thumbnails for a given PDF page. Thumbnails are derived from the provided
   * PDF page image and resized based on predefined types. Each thumbnail is encoded
   * based on the detected MIME type of the source URL.
   *
   * @param url The URL of the resource from which the PDF page is derived.
   * @param pdfPage A {@code BufferedImage} representation of the PDF page to generate thumbnails from.
   *                If the PDF page contains alpha transparency, it will be composited onto a white background.
   * @return A {@code List} of {@code Thumbnail} objects representing the generated thumbnails.
   *         Each thumbnail contains the resized image encoded in the appropriate format.
   * @throws MediaExtractionException If an error occurs during thumbnail preparation, resizing, encoding,
   *                                  or due to any other unexpected runtime issue.
   */
  List<Thumbnail> generatePdfThumbnails(String url, BufferedImage pdfPage) throws MediaExtractionException {
    final EnumMap<ThumbnailKind, ThumbnailImpl> thumbnails = prepareThumbnails(url, PNG_MIME_TYPE);
    BufferedImage source = pdfPage;
    try {
      if (pdfPage.getColorModel().hasAlpha()) {
        source = onWhite(pdfPage);
      }
      for (var entry : thumbnails.entrySet()) {
        final BufferedImage resizedBufferedImage = resize(source, pdfPage.getWidth(), pdfPage.getHeight(), entry.getKey());
        try {
          encodeThumbnail(resizedBufferedImage, entry.getValue());
        } finally {
          resizedBufferedImage.flush();
        }
      }
      return new ArrayList<>(thumbnails.values());
    } catch (IOException | RuntimeException e) {
      thumbnails.values().forEach(ThumbnailImpl::close);
      throw new MediaExtractionException("Could not generate PDF thumbnails", e);
    } finally {
      if (source != pdfPage) {
        source.flush();
      }
    }
  }

  BufferedImage resize(BufferedImage image, int originalWidth, int originalHeight, ThumbnailKind thumbnailKind) {
    final int width = thumbnailKind.getImageSize();
    final int height = Math.max(1, (int) Math.round((double) originalHeight * width / originalWidth));
    return new ResampleOp(width, height, ResampleOp.FILTER_LANCZOS).filter(image, null);
  }

  void encodeThumbnail(BufferedImage image, ThumbnailImpl thumbnail) throws IOException {
    if (PNG_MIME_TYPE.equals(thumbnail.getMimeType())) {
      thumbnail.markAsWithContent(encodeImage(image, "png"));
      return;
    }
    // JPEG writers require an RGB image. Composite transparency on white for opaque output.
    final BufferedImage rgb = onWhite(image);
    try {
      thumbnail.markAsWithContent(encodeImage(rgb, "jpeg"));
    } finally {
      rgb.flush();
    }
  }

  private BufferedImage onWhite(BufferedImage image) {
    final BufferedImage rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
    final Graphics2D graphics = rgb.createGraphics();
    try {
      graphics.setColor(Color.WHITE);
      graphics.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
      graphics.drawImage(image, 0, 0, null);
    } finally {
      graphics.dispose();
    }
    return rgb;
  }

  byte[] encodeImage(BufferedImage image, String format) throws IOException {
    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    // Use an explicit memory cache: ImageIO's default OutputStream adapter may cache on disk.
    try (MemoryCacheImageOutputStream imageOutput = new MemoryCacheImageOutputStream(output)) {
      if (!ImageIO.write(image, format, imageOutput)) {
        throw new IOException("No ImageIO writer for " + format);
      }
    }
    return output.toByteArray();
  }

  EnumMap<ThumbnailKind, ThumbnailImpl> prepareThumbnails(String url, String detectedMimeType) throws MediaExtractionException {
    final String thumbnailMimeType = PNG_MIME_TYPE.equals(detectedMimeType) ? PNG_MIME_TYPE : JPEG_MIME_TYPE;
    final String md5 = md5Hex(url);
    final EnumMap<ThumbnailKind, ThumbnailImpl> thumbnails = new EnumMap<>(ThumbnailKind.class);
    for (ThumbnailKind thumbnailKind : ThumbnailKind.values()) {
      final ThumbnailImpl thumbnail = new ThumbnailImpl(url, thumbnailMimeType, md5 + thumbnailKind.getNameSuffix());
      thumbnails.put(thumbnailKind, thumbnail);
    }
    return thumbnails;
  }

  /**
   * Calculates the MD5 hash of the provided string and returns it as a hexadecimal string.
   * <p>
   * Note: we have no choice but to use MD5, this is agreed upon with the API implementation.
   * <p>
   * The data used are not private and are considered safe.
   *
   * @param s The input string for which the MD5 hash needs to be calculated.
   * @return The calculated MD5 hash of the input string in hexadecimal format.
   * @throws MediaExtractionException If an error occurs during hash computation, such as unsupported encoding or unavailability
   * of the MD5 algorithm.
   */
  @SuppressWarnings({"findsecbugs:WEAK_MESSAGE_DIGEST_MD5", "java:S4790"})
  public static String md5Hex(String s) throws MediaExtractionException {
    try {
      byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
      byte[] md5bytes = MessageDigest.getInstance("MD5").digest(bytes);
      return String.format("%032x", new BigInteger(1, md5bytes));
    } catch (NoSuchAlgorithmException e) {
      throw new MediaExtractionException("Could not compute md5 hash", e);
    }
  }
}
