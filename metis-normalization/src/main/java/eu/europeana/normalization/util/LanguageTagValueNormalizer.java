package eu.europeana.normalization.util;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import com.ibm.icu.text.Transliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * This class parses a language tag value and then normalizes it for indexing or checking against
 * the index. This consists of the following actions, in the order given here:
 * <ol>
 * <li>Removing all punctuation (replacing them with spaces)</li>
 * <li>Cleaning surplus space characters (so that only single spaces remain, and no spaces are
 * found at the beginning or end of the input)</li>
 * <li>Splitting the value up into several languages (words) and each language into a language
 * identifier and optional subtag.</li>
 * </ol>
 * On the language names/labels and identifiers we perform additional normalization:
 * <ol>
 * <li>Transliterating all Cyrillic and Greek characters to Latin, removing all accents</li>
 * <li>Transforming all characters to lower case</li>
 * </ol>
 * Note that we don't perform further normalization on the subtags: they are left intact.
 * </p>
 * <p>
 * The Greek-to-Latin transliteration follows the standard UNGEGN, which is based in the older
 * standard ELOT 743, in use in Greek passports, for example.
 * </p>
 * <p>
 * The functionality in this class does not maintain state and is thread-safe.
 * </p>
 */
public final class LanguageTagValueNormalizer {

  /**
   * This character separates a language identifier from the subtag that may follow it.
   **/
  private static final char SUBTAG_SEPARATOR = '-';

  /**
   * This regular expression matches any sequence of space characters.
   **/
  private static final Pattern CLEAN_SURPLUS_SPACES = Pattern.compile("\\s+");

  /**
   * This regular expression matches all punctuation so that it may be removed. U+2013 is a dash
   * that may appear in subject heading and {Punct} was not removing it. We need to keep the subtag
   * separator, though.
   **/
  private static final Pattern CLEAN_PUNCTUATION_PATTERN = Pattern.compile(
      "[\\p{Punct}\\u2013&&[^" + Pattern.quote(Character.toString(SUBTAG_SEPARATOR)) + "]]+");

  /**
   * The transliterator for language identifiers.
   **/
  private static final Transliterator TRANSLITERATOR = Transliterator.getInstance(
      "Greek-Latin/UNGEGN; Cyrillic-Latin; nfd; [:Nonspacing Mark:] remove; nfc; Lower");

  private LanguageTagValueNormalizer() {
    // static methods only - hide constructor
  }

  /**
   * Normalizes a string
   *
   * @param input The string to normalize
   * @return The normalized string
   */
  public static List<LanguageTag> normalize(String input) {

    // Clean punctuation, replace any punctuation with a space.
    String normalizedInput = CLEAN_PUNCTUATION_PATTERN.matcher(input).replaceAll(" ");

    // Remove duplicate space characters as well as spaces before and after the text.
    normalizedInput = CLEAN_SURPLUS_SPACES.matcher(normalizedInput).replaceAll(" ").trim();

    // Split by spaces. We should have non-empty words.
    final String[] words = normalizedInput.split("\\s+");

    // Normalize the individual words
    return Stream.of(words).map(LanguageTagValueNormalizer::normalizeWord).filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  /**
   * Normalizes a word.
   *
   * @param word The word to normalize. The word cannot contain spaces.
   * @return The normalized word.
   */
  static LanguageTag normalizeWord(String word) {

    // Trim the word.
    final String trimmedWord = trimSeparators(word);
    if (trimmedWord.isEmpty()) {
      return null;
    }

    // Split the word to find the subtag.
    final int subtagStart = trimmedWord.indexOf(SUBTAG_SEPARATOR);
    final String code = subtagStart < 0 ? trimmedWord : trimmedWord.substring(0, subtagStart);
    final String subTag = subtagStart < 0 ? "" : trimmedWord.substring(subtagStart + 1);

    // Normalize the three values
    final String normalizedWord = TRANSLITERATOR.transliterate(word);
    final String normalizedCode = TRANSLITERATOR.transliterate(code);
    final String normalizedSubtag = subTag.isEmpty() ? null : (SUBTAG_SEPARATOR + subTag);

    // Done
    return new LanguageTag(normalizedWord, normalizedCode, normalizedSubtag);
  }

  private static String trimSeparators(String word) {

    // Find the left boundary. Could range from 0 to word.length().
    int left = 0;
    while(left < word.length() && word.charAt(left) == SUBTAG_SEPARATOR) {
      left++;
    }

    // Find the right boundary. Could range from left to word.length().
    int right = word.length();
    while(left < right && word.charAt(right - 1) == SUBTAG_SEPARATOR) {
      right--;
    }

    // Return what's between left and right.
    return word.substring(left, right);
  }
}
