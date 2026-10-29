package eu.europeana.metis.mediaprocessing.extraction.image;

import static eu.europeana.metis.mediaprocessing.extraction.image.PaletteAnalyzer.OPAQUE_ALPHA;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

public class PaletteAnalyzerTest {

  private static final int RGB_MASK = 0x00FFFFFF;
  private static final Color GRAY_100 = new Color(100, 100, 100);
  private static final int DIM_GRAY_INTENSITY = 105;

  private static final int OPAQUE_RED = Color.RED.getRGB();
  private static final int OPAQUE_BLACK = Color.BLACK.getRGB();
  private static final int OPAQUE_BLUE = Color.BLUE.getRGB();
  private static final int TRANSPARENT_BLACK = withAlpha(Color.BLACK, 0);
  private static final int TRANSPARENT_BLUE = withAlpha(Color.BLUE, 0);
  private static final int HALF_OPAQUE_RED = withAlpha(Color.RED, 128);
  private static final int QUARTER_OPAQUE_RED = withAlpha(Color.RED, 64);
  private static final int FAINT_RED = withAlpha(Color.RED, 1);

  public static final String RED_HEX = "FF0000";
  public static final String BLUE_HEX = "0000FF";

  private static final String GREEN_HEX = "00FF00";
  private static final String BLACK_HEX = "000000";
  private static final String WHITE_HEX = "FFFFFF";
  private static final String YELLOW_HEX = "FFFF00";
  private static final String CYAN_HEX = "00FFFF";
  private static final String MAGENTA_HEX = "FF00FF";
  private static final String DIM_GRAY_HEX = "696969";

  private final PaletteAnalyzer analyzer = new PaletteAnalyzer();

  @ParameterizedTest
  @CsvSource({"1,1", "1,17", "17,1", "3,5", "400,200", "200,400"})
  void countsEveryOpaquePixelAtFullWeightOnRectangularAndSinglePixelImages(int width, int height) {
    final Color[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.BLACK, Color.WHITE};
    final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    final Map<String, Long> expected = new HashMap<>();
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        final Color color = colors[(x + y) % colors.length];
        bufferedImage.setRGB(x, y, color.getRGB());
        expected.merge(String.format(Locale.ROOT, "%06X", color.getRGB() & RGB_MASK), (long) OPAQUE_ALPHA, Long::sum);
      }
    }
    final int[] original = bufferedImage.getRGB(0, 0, width, height, null, 0, width);
    assertEquals(expected, analyzer.histogram(bufferedImage));
    assertArrayEquals(original, bufferedImage.getRGB(0, 0, width, height, null, 0, width));
  }

  @Test
  void ranksByCountAndLimitsToSix() {
    final Color[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.WHITE};
    final BufferedImage bufferedImage = new BufferedImage(28, 1, BufferedImage.TYPE_INT_RGB);
    int x = 0;
    for (int i = 0; i < 7; i++) {
      for (int j = 0; j <= i; j++) {
        bufferedImage.setRGB(x++, 0, colors[i].getRGB());
      }
    }
    assertEquals(List.of(WHITE_HEX, MAGENTA_HEX, CYAN_HEX, YELLOW_HEX, BLUE_HEX, GREEN_HEX),
        analyzer.dominantColors(bufferedImage));
  }

  @Test
  void breaksEqualFrequencyTiesDeterministically() {
    final BufferedImage bufferedImage = new BufferedImage(2, 1, BufferedImage.TYPE_INT_RGB);
    bufferedImage.setRGB(0, 0, Color.BLUE.getRGB());
    bufferedImage.setRGB(1, 0, Color.RED.getRGB());
    assertEquals(List.of(RED_HEX, BLUE_HEX), analyzer.dominantColors(bufferedImage));
  }

  @ParameterizedTest
  @ValueSource(ints = {BufferedImage.TYPE_BYTE_GRAY, BufferedImage.TYPE_USHORT_GRAY})
  void countsEncodedGrayIntensitiesWithoutApplyingAnExtraGammaConversion(int imageType) {
    final BufferedImage bufferedImage = new BufferedImage(40, 20, imageType);
    final int value = (imageType == BufferedImage.TYPE_BYTE_GRAY) ? DIM_GRAY_INTENSITY : DIM_GRAY_INTENSITY * (65535 / OPAQUE_ALPHA);
    for (int y = 0; y < bufferedImage.getHeight(); y++) {
      for (int x = 0; x < bufferedImage.getWidth(); x++) {
        bufferedImage.getRaster().setSample(x, y, 0, value);
      }
    }
    assertEquals(List.of(DIM_GRAY_HEX), analyzer.dominantColors(bufferedImage));
  }

  @Test
  void excludesTransparentPixelsRegardlessOfTheirHiddenColor() {
    final BufferedImage bufferedImage = new BufferedImage(4, 1, BufferedImage.TYPE_INT_ARGB);
    bufferedImage.setRGB(0, 0, OPAQUE_RED);
    bufferedImage.setRGB(1, 0, OPAQUE_BLACK);
    bufferedImage.setRGB(2, 0, TRANSPARENT_BLACK);
    bufferedImage.setRGB(3, 0, TRANSPARENT_BLUE);
    assertEquals(Map.of(RED_HEX, (long) OPAQUE_ALPHA, BLACK_HEX, (long) OPAQUE_ALPHA), analyzer.histogram(bufferedImage));
  }

  @ParameterizedTest
  @ValueSource(ints = {BufferedImage.TYPE_INT_ARGB, BufferedImage.TYPE_INT_ARGB_PRE,
      BufferedImage.TYPE_4BYTE_ABGR, BufferedImage.TYPE_4BYTE_ABGR_PRE})
  void weightsPartialTransparencyWithoutChangingThePixelColor(int imageType) {
    final BufferedImage bufferedImage = new BufferedImage(4, 1, imageType);
    bufferedImage.setRGB(0, 0, HALF_OPAQUE_RED);
    bufferedImage.setRGB(1, 0, QUARTER_OPAQUE_RED);
    bufferedImage.setRGB(2, 0, FAINT_RED);
    bufferedImage.setRGB(3, 0, OPAQUE_BLUE);
    final int[] original = bufferedImage.getRGB(0, 0, 4, 1, null, 0, 4);
    assertEquals(Map.of(RED_HEX, 193L, BLUE_HEX, (long) OPAQUE_ALPHA), analyzer.histogram(bufferedImage));
    assertEquals(List.of(BLUE_HEX, RED_HEX), analyzer.dominantColors(bufferedImage));
    assertArrayEquals(original, bufferedImage.getRGB(0, 0, 4, 1, null, 0, 4));
  }

  @Test
  void returnsNoDominantColorsForFullyTransparentImage() {
    final BufferedImage bufferedImage = new BufferedImage(4, 3, BufferedImage.TYPE_INT_ARGB);
    assertEquals(Map.of(), analyzer.histogram(bufferedImage));
    assertEquals(List.of(), analyzer.dominantColors(bufferedImage));
  }

  @Test
  void mapsUniformColorWithoutIntroducingDitherColors() {
    final BufferedImage bufferedImage = new BufferedImage(4, 3, BufferedImage.TYPE_INT_RGB);
    for (int y = 0; y < bufferedImage.getHeight(); y++) {
      for (int x = 0; x < bufferedImage.getWidth(); x++) {
        bufferedImage.setRGB(x, y, GRAY_100.getRGB());
      }
    }
    assertEquals(Map.of(DIM_GRAY_HEX, 12L * OPAQUE_ALPHA), analyzer.histogram(bufferedImage));
  }

  private static int withAlpha(Color color, int alpha) {
    return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha).getRGB();
  }
}
