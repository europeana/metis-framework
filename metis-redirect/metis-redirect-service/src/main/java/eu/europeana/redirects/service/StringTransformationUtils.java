package eu.europeana.redirects.service;

import org.apache.commons.lang.StringUtils;

/**
 * Util class for handling StringUtils functionality
 *
 * Created by ymamakis on 1/14/16.
 */
public class StringTransformationUtils {

    /**
     * Apply transformations on a string
     * @param input The value to transform
     * @param transformations The transformations to apply. These expose the functionality of StringUtils functions
     * @return
     */
    public static String applyTransformations(final String input, String transformations) {
        if (transformations == null) {
            return input;
        }
        String[] transforms = null;
        if (transformations.contains(").")) {
            transforms = StringUtils.split(transformations, ").");
        } else if (!transformations.contains(").") && transformations.endsWith(")")) {
            transforms = new String[]{transformations};
        }
        String result = input;
        for (String transform : transforms) {
            if (!transform.endsWith(")")) {
                transform = transform + ")";
            }
            result = applyTransformation(result, transform);
        }
        return result;
    }
    
  private static String applyTransformation(final String input, final String transform) {
    final String argument = StringUtils.substringBetween(transform, "(", ")");
    final String result;
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
