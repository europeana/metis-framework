package eu.europeana.metis.mediaprocessing.extraction;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import javax.imageio.ImageIO;

public final class ImageTestUtils {

  private ImageTestUtils() {
  }

  public static BufferedImage createImageWithRedLeftHalf(int width, int height, boolean transparent) {
    final BufferedImage image = new BufferedImage(width, height,
        transparent ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
    final Graphics2D graphics = image.createGraphics();
    try {
      graphics.setColor(Color.RED);
      graphics.fillRect(0, 0, width / 2, height);
      if (!transparent) {
        graphics.setColor(Color.BLUE);
        graphics.fillRect(width / 2, 0, width - width / 2, height);
      }
    } finally {
      graphics.dispose();
    }
    return image;
  }

  public static Path writeImage(Path path, BufferedImage image, String format) throws IOException {
    assertTrue(ImageIO.write(image, format, path.toFile()), "No test image writer for " + format);
    return path;
  }

  public static Path copyFile(Path directory, String name) throws IOException {
    final Path path = directory.resolve(Path.of(name).getFileName());
    try (InputStream input = ImageTestUtils.class.getResourceAsStream("/images/" + name)) {
      assertNotNull(input, "Missing image fixture: " + name);
      Files.copy(input, path);
    }
    return path;
  }

  public static BufferedImage assertThumbnail(Thumbnail thumbnail, int width, int height) throws IOException {
    final byte[] bytes;
    try (InputStream input = thumbnail.getContentStream()) {
      bytes = input.readAllBytes();
    }
    assertTrue(Set.of("image/jpeg", "image/png").contains(thumbnail.getMimeType()));
    final byte[] signature = "image/png".equals(thumbnail.getMimeType())
        ? new byte[]{(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10}
        : new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff};
    assertArrayEquals(signature, Arrays.copyOf(bytes, signature.length), "Encoded format must match thumbnail MIME type");
    final BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
    assertNotNull(bufferedImage);
    assertEquals(width, bufferedImage.getWidth());
    assertEquals(height, bufferedImage.getHeight());
    return bufferedImage;
  }
}
