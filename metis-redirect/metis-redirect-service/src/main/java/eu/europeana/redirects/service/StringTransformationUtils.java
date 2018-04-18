package eu.europeana.redirects.service;

import org.apache.commons.lang.StringUtils;

/**
 * Util class for handling StringUtils functionality
 *
 * Created by ymamakis on 1/14/16.
 */
public final class StringTransformationUtils {

  private StringTransformationUtils() {
  }

  /**
   * Apply transformations on a string.
   *
   * @param input The value to transform
   * @param transformations The transformations to apply. These expose the functionality of StringUtils functions
   * @return the transformed value
   */
  public static String applyTransformations(final String input, String transformations) {
    String result = input;
    if (transformations != null) {
      String[] transforms = null;
      if (transformations.contains(").")) {
        transforms = StringUtils.split(transformations, ").");
      } else if (!transformations.contains(").") && transformations.endsWith(")")) {
        transforms = new String[]{transformations};
      }
      if (transforms != null) {
        for (String transform : transforms) {
          StringBuilder stringBuilder = new StringBuilder(transform);
          if (!transform.endsWith(")")) {
            stringBuilder.append(")");
          }
          result = applyTransformation(result, stringBuilder.toString());
        }
      }
    }
    return result;
  }

  private static String applyTransformation(final String input, final String transform) {
    final String argument = StringUtils.substringBetween(transform, "(", ")");
    String result;
    if (StringUtils.startsWith(transform, "replace")) {
      String[] replacements = StringUtils.split(argument, ",");
      result = StringUtils.replace(input, replacements[0], replacements[1]);
    } else if (StringUtils.startsWith(transform, "substringBetween(")) {
      String[] replacements = StringUtils.split(argument, ",");
      result = StringUtils.substringBetween(input, replacements[0], replacements[1]);
    } else if (StringUtils.startsWith(transform, "substringBeforeFirst(")) {
      result = StringUtils.substringBefore(input, argument);
    } else if (StringUtils.startsWith(transform, "substringBeforeLast(")) {
      result = StringUtils.substringBeforeLast(input, argument);
    } else if (StringUtils.startsWith(transform, "substringAfterLast(")) {
      result = StringUtils.substringAfterLast(input, argument);
    } else if (StringUtils.startsWith(transform, "substringAfterFirst(")) {
      result = StringUtils.substringAfter(input, argument);
    } else if (StringUtils.startsWith(transform, "concatBefore(")) {
      result = argument + input;
    } else if (StringUtils.startsWith(transform, "concatAfter(")) {
      result = input + argument;
    } else {
      result = input;
    }
    return result;
  }
}
