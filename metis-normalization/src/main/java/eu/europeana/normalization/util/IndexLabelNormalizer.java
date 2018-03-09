package eu.europeana.normalization.util;

import java.util.regex.Pattern;
import com.ibm.icu.text.Transliterator;

/**
 * <p>
 * This class normalizes a label for indexing. This consists of the following actions, in the order
 * given here:
 * <ol>
 * <li>Removing all punctuation (replacing them with spaces)</li>
 * <li>Transliterating all Cyrillic and Greek characters to Latin, removing all accents</li>
 * <li>Transforming all characters to lower case</li>
 * <li>Cleaning surplus space characters (so that only single spaces remain)</li>
 * </ol>
 * </p>
 * <p>
 * The Greek-to-Latin transliteration follows the standard UNGEGN, which is based in the older
 * standard ELOT 743, in use in Greek passports, for example.
 * </p>
 * <p>
 * The functionality in this class does not maintain state and is thread-safe.
 * </p>
 */
public final class IndexLabelNormalizer {

  /** This regular expression matches any sequence of space characters. **/
  private static final Pattern CLEAN_SURPLUS_SPACES = Pattern.compile("\\s+");

  /**
   * This regular expression matches all punctuation so that it may be removed. U+2013 is a dash
   * that may appear in subject heading and {Punct} was not removing it
   **/
  private static final Pattern CLEAN_PUNCTUATION_PATTERN = Pattern.compile("[\\p{Punct}\\u2013]+");

  /** The transliterator. **/
  private static final Transliterator TRANSLITERATOR = Transliterator.getInstance(
      "Greek-Latin/UNGEGN; Cyrillic-Latin; nfd; [:Nonspacing Mark:] remove; nfc; Lower");

  private IndexLabelNormalizer() {
    // static methods only - hide constructor
  }

  /**
   * Normalizes a string
   *
   * @param indexLabel The string to normalize
   * @return The normalized string
   */
  public static String normalizeIndexLabel(String indexLabel) {

    // Clean punctuation, replace any punctuation with a space.
    String result = CLEAN_PUNCTUATION_PATTERN.matcher(indexLabel).replaceAll(" ");

    // Transliterate the text, also removing accents and converting to lower case.
    result = TRANSLITERATOR.transliterate(result);

    // Remove duplicate space characters.
    result = CLEAN_SURPLUS_SPACES.matcher(result).replaceAll(" ");

    // Trim and done.
    return result.trim();
  }
}
