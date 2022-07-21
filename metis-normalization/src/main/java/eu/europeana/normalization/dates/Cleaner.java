package eu.europeana.normalization.dates;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Class for the steps of the normalisations process related with the detection of punctuation marks and abbreviations that signal
 * uncertain and approximate dates. This class also detects some patterns frequently used for adding notes to the dates, and
 * removes such notes.
 */
public class Cleaner {

  public class CleanResult {

    CleanOperationId cleanOperation;
    String cleanedValue;

    public CleanResult(CleanOperationId cleanOperation, String cleanedValue) {
      super();
      this.cleanOperation = cleanOperation;
      this.cleanedValue = cleanedValue;
    }

    public CleanOperationId getCleanOperation() {
      return cleanOperation;
    }

    public String getCleanedValue() {
      return cleanedValue;
    }
  }

  Pattern patInitialTextA = Pattern.compile("^\\s*[^\\s:]+:\\s*");
  Pattern patInitialTextB = Pattern.compile("^\\s*\\([^\\)]+\\)\\s*");
  Pattern patEndingText = Pattern.compile("\\s*\\(.+\\)\\s*$");
  Pattern patEndingDot = Pattern.compile("\\s*\\.\\s*$");
  Pattern patEndingSquareBracket = Pattern.compile("\\s*\\]\\s*$");
  Pattern patSquareBrackets = Pattern.compile("\\[([^\\]]+)\\]");
  Pattern patParenthesesFullValue = Pattern.compile("\\s*\\(([^\\(\\)]+)\\)\\s*");
  Pattern patParenthesesFullValueAndCa = Pattern.compile("\\s*\\((circa|CA\\.?|C\\.)([^\\(\\)]+)\\)\\s*");
  Pattern patCa = Pattern.compile("^\\s*(circa|CA\\.?|C\\.)\\s*", Pattern.CASE_INSENSITIVE);
  Pattern patSquareBracketsAndCa = Pattern.compile("\\[(circa|CA\\.?|C\\.)\\s*([^\\]]+)\\]",
      Pattern.CASE_INSENSITIVE);
  Pattern patEndingTextSquareBrackets = Pattern.compile("\\s*\\[.+\\]\\s*$");

  public CleanResult clean1stTime(String value) {
    Matcher m = patInitialTextA.matcher(value);
    if (m.find()) {
      String cleanedVal = m.replaceFirst("");
      if (!StringUtils.isEmpty(cleanedVal)) {
        return new CleanResult(CleanOperationId.INITIAL_TEXT, cleanedVal);
      }
    }
    m = patInitialTextB.matcher(value);
    if (m.find()) {
      String cleanedVal = m.replaceFirst("");
      if (!StringUtils.isEmpty(cleanedVal)) {
        return new CleanResult(CleanOperationId.INITIAL_TEXT, cleanedVal);
      }
    }
    m = patEndingText.matcher(value);
    if (m.find()) {
      String cleanedVal = m.replaceFirst("");
      if (!StringUtils.isEmpty(cleanedVal)) {
        return new CleanResult(CleanOperationId.ENDING_TEXT, cleanedVal);
      }
    }
    m = patSquareBracketsAndCa.matcher(value);
    if (m.find()) {
      return new CleanResult(CleanOperationId.SQUARE_BRACKETS_AND_CIRCA, m.replaceAll("$2"));
    }
    m = patSquareBrackets.matcher(value);
    if (m.find()) {
      return new CleanResult(CleanOperationId.SQUARE_BRACKETS, m.replaceAll("$1"));
    }
    m = patCa.matcher(value);
    if (m.find()) {
      return new CleanResult(CleanOperationId.CIRCA, m.replaceAll(""));
    }
    m = patEndingSquareBracket.matcher(value);
    if (m.find()) {
      return new CleanResult(CleanOperationId.SQUARE_BRACKET_END, m.replaceAll(""));
    }
    m = patEndingDot.matcher(value);
    if (m.find()) {
      String cleanedVal = m.replaceFirst("");
      if (!StringUtils.isEmpty(cleanedVal)) {
        return new CleanResult(CleanOperationId.ENDING_TEXT, cleanedVal);
      }
    }
    return null;
  }

  public CleanResult clean2ndTime(String value) {
    Matcher m = patEndingTextSquareBrackets.matcher(value);
    if (m.find()) {
      return new CleanResult(CleanOperationId.ENDING_TEXT, m.replaceFirst(""));
    }
    m = patParenthesesFullValueAndCa.matcher(value);
    if (m.matches()) {
      return new CleanResult(CleanOperationId.PARENTHESES_FULL_VALUE_AND_CIRCA, m.replaceAll("$1"));
    }
    m = patParenthesesFullValue.matcher(value);
    if (m.matches()) {
      return new CleanResult(CleanOperationId.PARENTHESES_FULL_VALUE, m.replaceAll("$1"));
    }
    return null;
  }

  public CleanResult cleanGenericProperty(String value) {
    Matcher m = patSquareBracketsAndCa.matcher(value);
    if (m.find()) {
      return new CleanResult(CleanOperationId.SQUARE_BRACKETS_AND_CIRCA, m.replaceAll("$2"));
    }
    m = patSquareBrackets.matcher(value);
    if (m.find()) {
      return new CleanResult(CleanOperationId.SQUARE_BRACKETS, m.replaceAll("$1"));
    }
    m = patCa.matcher(value);
    if (m.find()) {
      return new CleanResult(CleanOperationId.CIRCA, m.replaceAll(""));
    }
    m = patEndingDot.matcher(value);
    if (m.find()) {
      String cleanedVal = m.replaceFirst("");
      if (!StringUtils.isEmpty(cleanedVal)) {
        return new CleanResult(CleanOperationId.ENDING_TEXT, cleanedVal);
      }
    }
    return null;
  }

}
