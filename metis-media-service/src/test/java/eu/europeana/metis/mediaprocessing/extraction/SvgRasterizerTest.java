package eu.europeana.metis.mediaprocessing.extraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SvgRasterizerTest {

  @TempDir
  Path directory;

  private final SvgRasterizer rasterizer = new SvgRasterizer();

  @ParameterizedTest(name = "{0} x {1} SVG with viewBox {2} x {3}")
  @CsvSource({"8000,4000,80,40,200", "125,75,12.5,7.5,240"})
  void rendersRequestedSizeWithTransparencyAndUnroundedViewBox(int width, int height,
      double viewBoxWidth, double viewBoxHeight, int renderHeight) throws Exception {
    final Path source = directory.resolve("source.svg");
    Files.writeString(source, """
        <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d" viewBox="0 0 %s %s">
          <rect width="%s" height="%s" fill="red"/>
        </svg>
        """.formatted(width, height, viewBoxWidth, viewBoxHeight, viewBoxWidth / 2, viewBoxHeight));
    final BufferedImage bufferedImage = rasterizer.render(source.toFile(), 400, renderHeight);
    try {
      assertEquals(400, bufferedImage.getWidth());
      assertEquals(renderHeight, bufferedImage.getHeight());
      assertEquals(Color.RED.getRGB(), bufferedImage.getRGB(100, renderHeight / 2));
      // Shifting gives the alpha value of the pixel.
      assertEquals(0, bufferedImage.getRGB(300, renderHeight / 2) >>> 24);
      // Fractional viewBox coordinates must not leave a transparent edge at the bottom.
      assertEquals(Color.RED.getRGB(), bufferedImage.getRGB(100, renderHeight - 1));
    } finally {
      bufferedImage.flush();
    }
  }

  @Test
  void wrapsMalformedSvgAsIOException() throws Exception {
    final Path source = directory.resolve("invalid.svg");
    Files.writeString(source, "<svg xmlns=\"http://www.w3.org/2000/svg\"><rect></svg>");
    assertThrows(IOException.class, () -> rasterizer.render(source.toFile(), 400, 200));
  }
}
