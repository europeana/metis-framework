package eu.europeana.metis.mediaprocessing.extraction.image;

import com.twelvemonkeys.image.ImageUtil;
import com.twelvemonkeys.image.InverseColorMapIndexColorModel;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Maps thumbnails to the fixed palette using TwelveMonkeys, then counts the colours.
 */
public class PaletteAnalyzer {

  private static final int MAX_COLORS = 6;
  public static final int OPAQUE_ALPHA = 255;

  //Note: Perhaps we can use an adaptive palette instead. That might improve the results.
  // The 138 distinct RGB colours from the former colormap.png, in the same sorted order.
  private static final int[] PALETTE_COLORS = {
      0x000000, 0x000080, 0x00008B, 0x0000CD, 0x0000FF, 0x006400, 0x008000, 0x008080,
      0x008B8B, 0x00BFFF, 0x00CED1, 0x00FA9A, 0x00FF00, 0x00FF7F, 0x00FFFF, 0x191970,
      0x1E90FF, 0x20B2AA, 0x228B22, 0x2E8B57, 0x2F4F4F, 0x32CD32, 0x3CB371, 0x40E0D0,
      0x4169E1, 0x4682B4, 0x483D8B, 0x48D1CC, 0x4B0082, 0x556B2F, 0x5F9EA0, 0x6495ED,
      0x66CDAA, 0x696969, 0x6A5ACD, 0x6B8E23, 0x708090, 0x778899, 0x7B68EE, 0x7CFC00,
      0x7FFF00, 0x7FFFD4, 0x800000, 0x800080, 0x808000, 0x808080, 0x87CEEB, 0x87CEFA,
      0x8A2BE2, 0x8B0000, 0x8B008B, 0x8B4513, 0x8FBC8F, 0x90EE90, 0x9370DB, 0x9400D3,
      0x98FB98, 0x9932CC, 0x9ACD32, 0xA0522D, 0xA52A2A, 0xA9A9A9, 0xADD8E6, 0xADFF2F,
      0xAFEEEE, 0xB0C4DE, 0xB0E0E6, 0xB22222, 0xB8860B, 0xBA55D3, 0xBC8F8F, 0xBDB76B,
      0xC0C0C0, 0xC71585, 0xCD5C5C, 0xCD853F, 0xD2691E, 0xD2B48C, 0xD3D3D3, 0xD8BFD8,
      0xDA70D6, 0xDAA520, 0xDB7093, 0xDC143C, 0xDCDCDC, 0xDDA0DD, 0xDEB887, 0xE0FFFF,
      0xE6E6FA, 0xE9967A, 0xEE82EE, 0xEEE8AA, 0xF08080, 0xF0E68C, 0xF0F8FF, 0xF0FFF0,
      0xF0FFFF, 0xF4A460, 0xF5DEB3, 0xF5F5DC, 0xF5F5F5, 0xF5FFFA, 0xF8F8FF, 0xFA8072,
      0xFAEBD7, 0xFAF0E6, 0xFAFAD2, 0xFDF5E6, 0xFF0000, 0xFF00FF, 0xFF1493, 0xFF4500,
      0xFF6347, 0xFF69B4, 0xFF7F50, 0xFF8C00, 0xFFA07A, 0xFFA500, 0xFFB6C1, 0xFFC0CB,
      0xFFD700, 0xFFDAB9, 0xFFDEAD, 0xFFE4B5, 0xFFE4C4, 0xFFE4E1, 0xFFEBCD, 0xFFEFD5,
      0xFFF0F5, 0xFFF5EE, 0xFFF8DC, 0xFFFACD, 0xFFFAF0, 0xFFFAFA, 0xFFFF00, 0xFFFFE0,
      0xFFFFF0, 0xFFFFFF
  };

  private final IndexColorModel palette = new InverseColorMapIndexColorModel(Byte.SIZE, PALETTE_COLORS.length,
      PALETTE_COLORS, 0, false, -1, DataBuffer.TYPE_BYTE);

  /**
   * Finds up to six palette colors with the greatest opacity-weighted pixel counts. Ties are ordered by descending hexadecimal
   * color code; fully transparent pixels are ignored.
   *
   * @param bufferedImage The image to analyze. The caller retains ownership of its buffer.
   * @return An unmodifiable list of uppercase {@code RRGGBB} color codes, ordered by decreasing weight.
   */
  public List<String> dominantColors(BufferedImage bufferedImage) {
    return histogram(bufferedImage).entrySet().stream()
                           .sorted(Comparator.<Map.Entry<String, Long>>comparingLong(Map.Entry::getValue).reversed()
                                             .thenComparing(Map.Entry::getKey, Comparator.reverseOrder()))
                           .limit(MAX_COLORS).map(Map.Entry::getKey).toList();
  }

  /**
   * Calculates an opacity-weighted color histogram by mapping image colors to a predefined palette. Fully transparent pixels
   * contribute nothing; opaque pixels contribute {@value #OPAQUE_ALPHA} and partial transparency contributes proportionally.
   * These integer weights avoid rounding away faint pixels.
   * <p>
   * Diffusion dithering introduces extra colours and randomized errors. For dominant-colour analysis, direct mapping is
   * deterministic and stays closer to the legacy histograms.
   *
   * @param image The input image for which the histogram is computed. The image is converted to indexed color using a predefined
   * palette before calculating the histogram.
   * @return A map where the keys are color codes (hexadecimal RGB values in the format of "RRGGBB") from the palette, and the
   * values are the summed alpha weights for each color.
   */
  Map<String, Long> histogram(BufferedImage image) {
    final BufferedImage rgb = ImageUtil.toBuffered(image, BufferedImage.TYPE_INT_RGB);
    final BufferedImage indexed = ImageUtil.createIndexed(rgb, palette, null, ImageUtil.DITHER_NONE);
    final boolean hasAlpha = image.getColorModel().hasAlpha();
    final long[] counts = new long[palette.getMapSize()];
    for (int y = 0; y < indexed.getHeight(); y++) {
      for (int x = 0; x < indexed.getWidth(); x++) {
        final int weight = hasAlpha ? (image.getRGB(x, y) >>> 24) : OPAQUE_ALPHA;
        counts[indexed.getRaster().getSample(x, y, 0)] += weight;
      }
    }
    final Map<String, Long> histogram = new HashMap<>();
    for (int i = 0; i < counts.length; i++) {
      if (counts[i] != 0) {
        histogram.put(String.format(Locale.ROOT, "%06X", palette.getRGB(i) & 0xffffff), counts[i]);
      }
    }
    return histogram;
  }
}
