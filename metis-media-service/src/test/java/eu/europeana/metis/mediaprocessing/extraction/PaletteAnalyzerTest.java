package eu.europeana.metis.mediaprocessing.extraction;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class PaletteAnalyzerTest {

  private final PaletteAnalyzer analyzer = new PaletteAnalyzer();

  @ParameterizedTest
  @CsvSource({"1,1", "1,17", "17,1", "3,5", "400,200", "200,400"})
  void countsEveryPixelExactlyOnceOnRectangularAndSinglePixelImages(int width, int height) {
    final int[] colors = {0xff0000, 0x00ff00, 0x0000ff, 0x000000, 0xffffff};
    final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    final Map<String, Long> expected = new HashMap<>();
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        final int color = colors[(x + y) % colors.length];
        bufferedImage.setRGB(x, y, color);
        expected.merge(String.format(Locale.ROOT, "%06X", color), 1L, Long::sum);
      }
    }
    final int[] original = bufferedImage.getRGB(0, 0, width, height, null, 0, width);
    assertEquals(expected, analyzer.histogram(bufferedImage));
    assertArrayEquals(original, bufferedImage.getRGB(0, 0, width, height, null, 0, width));
  }

  @Test
  void ranksByCountAndLimitsToSix() {
    final int[] colors = {0xff0000, 0x00ff00, 0x0000ff, 0xffff00, 0x00ffff, 0xff00ff, 0xffffff};
    final BufferedImage bufferedImage = new BufferedImage(28, 1, BufferedImage.TYPE_INT_RGB);
    int x = 0;
    for (int i = 0; i < 7; i++) {
      for (int j = 0; j <= i; j++) {
        bufferedImage.setRGB(x++, 0, colors[i]);
      }
    }
    assertEquals(List.of("FFFFFF", "FF00FF", "00FFFF", "FFFF00", "0000FF", "00FF00"),
        analyzer.dominantColors(bufferedImage));
  }

  @Test
  void breaksEqualFrequencyTiesDeterministically() {
    final BufferedImage bufferedImage = new BufferedImage(2, 1, BufferedImage.TYPE_INT_RGB);
    bufferedImage.setRGB(0, 0, 0x0000ff);
    bufferedImage.setRGB(1, 0, 0xff0000);
    assertEquals(List.of("FF0000", "0000FF"), analyzer.dominantColors(bufferedImage));
  }

  @ParameterizedTest
  @ValueSource(ints = {BufferedImage.TYPE_BYTE_GRAY, BufferedImage.TYPE_USHORT_GRAY})
  void countsEncodedGrayIntensitiesWithoutApplyingAnExtraGammaConversion(int imageType) {
    final BufferedImage bufferedImage = new BufferedImage(40, 20, imageType);
    final int value = (imageType == BufferedImage.TYPE_BYTE_GRAY) ? 105 : 105 * 257; //(65535 / 255)
    for (int y = 0; y < bufferedImage.getHeight(); y++) {
      for (int x = 0; x < bufferedImage.getWidth(); x++) {
        bufferedImage.getRaster().setSample(x, y, 0, value);
      }
    }
    assertEquals(List.of("696969"), analyzer.dominantColors(bufferedImage));
  }

  @Test
  void preservesExistingTreatmentOfTransparentPixelColors() {
    final BufferedImage bufferedImage = new BufferedImage(3, 1, BufferedImage.TYPE_INT_ARGB);
    bufferedImage.setRGB(0, 0, 0xffff0000);
    bufferedImage.setRGB(1, 0, 0x80ff0000);
    bufferedImage.setRGB(2, 0, 0x00000000);
    assertEquals(Map.of("FF0000", 2L, "000000", 1L), analyzer.histogram(bufferedImage));
  }

  @Test
  void mapsUniformColorWithoutIntroducingDitherColors() {
    final BufferedImage bufferedImage = new BufferedImage(4, 3, BufferedImage.TYPE_INT_RGB);
    for (int y = 0; y < bufferedImage.getHeight(); y++) {
      for (int x = 0; x < bufferedImage.getWidth(); x++) {
        bufferedImage.setRGB(x, y, 0x646464);
      }
    }
    assertEquals(Map.of("696969", 12L), analyzer.histogram(bufferedImage));
  }
}
