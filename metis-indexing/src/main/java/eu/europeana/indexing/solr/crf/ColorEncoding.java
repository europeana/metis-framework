package eu.europeana.indexing.solr.crf;

import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * This enum contains all supported colors along with the code that they are assigned.
 */
public enum ColorEncoding {

  COLOR_1("F0F8FF", 1),
  COLOR_2("FAEBD7", 2),
  COLOR_3("00FFFF", 3),
  COLOR_4("7FFFD4", 4),
  COLOR_5("F0FFFF", 5),
  COLOR_6("F5F5DC", 6),
  COLOR_7("FFE4C4", 7),
  COLOR_8("000000", 8),
  COLOR_9("FFEBCD", 9),
  COLOR_10("0000FF", 10),
  COLOR_11("8A2BE2", 11),
  COLOR_12("A52A2A", 12),
  COLOR_13("DEB887", 13),
  COLOR_14("5F9EA0", 14),
  COLOR_15("7FFF00", 15),
  COLOR_16("D2691E", 16),
  COLOR_17("FF7F50", 17),
  COLOR_18("6495ED", 18),
  COLOR_19("FFF8DC", 19),
  COLOR_20("DC143C", 20),
  COLOR_21("00008B", 21),
  COLOR_22("008B8B", 22),
  COLOR_23("B8860B", 23),
  COLOR_24("A9A9A9", 24),
  COLOR_25("006400", 25),
  COLOR_26("BDB76B", 26),
  COLOR_27("8B008B", 27),
  COLOR_28("556B2F", 28),
  COLOR_29("FF8C00", 29),
  COLOR_30("9932CC", 30),
  COLOR_31("8B0000", 31),
  COLOR_32("E9967A", 32),
  COLOR_33("8FBC8F", 33),
  COLOR_34("483D8B", 34),
  COLOR_35("2F4F4F", 35),
  COLOR_36("00CED1", 36),
  COLOR_37("9400D3", 37),
  COLOR_38("FF1493", 38),
  COLOR_39("00BFFF", 39),
  COLOR_40("696969", 40),
  COLOR_41("1E90FF", 41),
  COLOR_42("B22222", 42),
  COLOR_43("FFFAF0", 43),
  COLOR_44("228B22", 44),
  COLOR_45("FF00FF", 45),
  COLOR_46("DCDCDC", 46),
  COLOR_47("F8F8FF", 47),
  COLOR_48("FFD700", 48),
  COLOR_49("DAA520", 49),
  COLOR_50("808080", 50),
  COLOR_51("008000", 51),
  COLOR_52("ADFF2F", 52),
  COLOR_53("F0FFF0", 53),
  COLOR_54("FF69B4", 54),
  COLOR_55("CD5C5C", 55),
  COLOR_56("4B0082", 56),
  COLOR_57("FFFFF0", 57),
  COLOR_58("F0E68C", 58),
  COLOR_59("E6E6FA", 59),
  COLOR_60("FFF0F5", 60),
  COLOR_61("7CFC00", 61),
  COLOR_62("FFFACD", 62),
  COLOR_63("ADD8E6", 63),
  COLOR_64("F08080", 64),
  COLOR_65("E0FFFF", 65),
  COLOR_66("FAFAD2", 66),
  COLOR_67("D3D3D3", 67),
  COLOR_68("90EE90", 68),
  COLOR_69("FFB6C1", 69),
  COLOR_70("FFA07A", 70),
  COLOR_71("20B2AA", 71),
  COLOR_72("87CEFA", 72),
  COLOR_73("778899", 73),
  COLOR_74("B0C4DE", 74),
  COLOR_75("FFFFE0", 75),
  COLOR_76("00FF00", 76),
  COLOR_77("32CD32", 77),
  COLOR_78("FAF0E6", 78),
  COLOR_79("800000", 79),
  COLOR_80("66CDAA", 80),
  COLOR_81("0000CD", 81),
  COLOR_82("BA55D3", 82),
  COLOR_83("9370DB", 83),
  COLOR_84("3CB371", 84),
  COLOR_85("7B68EE", 85),
  COLOR_86("00FA9A", 86),
  COLOR_87("48D1CC", 87),
  COLOR_88("C71585", 88),
  COLOR_89("191970", 89),
  COLOR_90("F5FFFA", 90),
  COLOR_91("FFE4E1", 91),
  COLOR_92("FFE4B5", 92),
  COLOR_93("FFDEAD", 93),
  COLOR_94("000080", 94),
  COLOR_95("FDF5E6", 95),
  COLOR_96("808000", 96),
  COLOR_97("6B8E23", 97),
  COLOR_98("FFA500", 98),
  COLOR_99("FF4500", 99),
  COLOR_100("DA70D6", 100),
  COLOR_101("EEE8AA", 101),
  COLOR_102("98FB98", 102),
  COLOR_103("AFEEEE", 103),
  COLOR_104("DB7093", 104),
  COLOR_105("FFEFD5", 105),
  COLOR_106("FFDAB9", 106),
  COLOR_107("CD853F", 107),
  COLOR_108("FFC0CB", 108),
  COLOR_109("DDA0DD", 109),
  COLOR_110("B0E0E6", 110),
  COLOR_111("800080", 111),
  COLOR_112("FF0000", 112),
  COLOR_113("BC8F8F", 113),
  COLOR_114("4169E1", 114),
  COLOR_115("8B4513", 115),
  COLOR_116("FA8072", 116),
  COLOR_117("F4A460", 117),
  COLOR_118("2E8B57", 118),
  COLOR_119("FFF5EE", 119),
  COLOR_120("A0522D", 120),
  COLOR_121("C0C0C0", 121),
  COLOR_122("87CEEB", 122),
  COLOR_123("6A5ACD", 123),
  COLOR_124("708090", 124),
  COLOR_125("FFFAFA", 125),
  COLOR_126("00FF7F", 126),
  COLOR_127("4682B4", 127),
  COLOR_128("D2B48C", 128),
  COLOR_129("008080", 129),
  COLOR_130("D8BFD8", 130),
  COLOR_131("FF6347", 131),
  COLOR_132("40E0D0", 132),
  COLOR_133("EE82EE", 133),
  COLOR_134("F5DEB3", 134),
  COLOR_135("FFFFFF", 135),
  COLOR_136("F5F5F5", 136),
  COLOR_137("FFFF00", 137),
  COLOR_138("9ACD32", 138);
  
  private static final Integer VALUE_UNKNOWN = 0;

  private static BiMap<String, Integer> colorMap;

  private final String hexString;
  private final int code;

  ColorEncoding(String hexString, int code) {
    this.hexString = hexString;
    this.code = code;
  }

  private static synchronized BiMap<String, Integer> getColorMap() {
    if (colorMap == null) {
      colorMap = HashBiMap.create(ColorEncoding.values().length);
      for (ColorEncoding encoding : ColorEncoding.values()) {
        colorMap.put(encoding.hexString, encoding.code);
      }
    }
    return colorMap;
  }

  /**
   * 
   * @return The hexadecimal string representing this color.
   */
  String getHexString() {
    return hexString;
  }

  /**
   * 
   * @return The code (unshifted) that is assigned to this color.
   */
  int getCode() {
    return code;
  }

  /**
   * Codifies the given color (but doesn't shift the code).
   * 
   * @param hexString The hexadecimal string representation of the color
   * @return The integer represantation of the color, or 0 if the color could not be found.
   */
  static Integer getColorCode(final String hexString) {
    final String hexStringWithoutHash;
    if (StringUtils.isBlank(hexString)) {
      hexStringWithoutHash = "";
    } else if (hexString.startsWith("#")) {
      hexStringWithoutHash = hexString.substring(1);
    } else {
      hexStringWithoutHash = hexString;
    }
    final Integer result = getColorMap().get(hexStringWithoutHash.trim().toUpperCase(Locale.ENGLISH));
    return result == null ? VALUE_UNKNOWN : result;
  }
}
