package eu.europeana.metis.mediaprocessing.extraction;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import org.apache.batik.bridge.EmbededExternalResourceSecurity;
import org.apache.batik.bridge.ExternalResourceSecurity;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.ParsedURL;

/**
 * Renders SVG directly to a bounded raster while retaining fractional viewBox coordinates.
 */
class SvgRasterizer {

  /**
   * Renders an SVG file into a rasterized {@link BufferedImage} with the specified width and height.
   *
   * @param content The SVG file to be rendered.
   * @param width The desired width of the rendered image, in pixels.
   * @param height The desired height of the rendered image, in pixels.
   * @return A {@link BufferedImage} representing the rendered SVG.
   * @throws IOException If an error occurs while reading the input file or rendering the SVG.
   */
  BufferedImage render(File content, int width, int height) throws IOException {
    final BufferedImageTranscoder transcoder = new BufferedImageTranscoder();
    transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, (float) width);
    transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, (float) height);
    try (InputStream input = Files.newInputStream(content.toPath())) {
      transcoder.transcode(new TranscoderInput(input), null);
      return transcoder.image;
    } catch (TranscoderException e) {
      throw new IOException("Could not render SVG", e);
    }
  }

  /**
   * A specialized implementation of {@link ImageTranscoder} that generates a {@link BufferedImage}. This class is used for
   * rendering scalable vector graphics (SVG) into rasterized images.
   */
  @SuppressWarnings("java:S110")
  // Batik's ImageTranscoder extension point already has a deep inheritance hierarchy.
  private static class BufferedImageTranscoder extends ImageTranscoder {

    private BufferedImage image;

    @Override
    public BufferedImage createImage(int width, int height) {
      return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    public void writeImage(BufferedImage image, TranscoderOutput output) {
      this.image = image;
    }

    @Override
    protected UserAgent createUserAgent() {
      return new SVGAbstractTranscoderUserAgent() {
        @Override
        public ExternalResourceSecurity getExternalResourceSecurity(ParsedURL resourceUrl, ParsedURL documentUrl) {
          // Match the ImageIO SVG reader: allow embedded images without fetching external resources.
          return new EmbededExternalResourceSecurity(resourceUrl);
        }
      };
    }
  }
}
