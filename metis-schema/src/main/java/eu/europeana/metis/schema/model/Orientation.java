package eu.europeana.metis.schema.model;

import java.util.Arrays;
import java.util.Locale;

/**
 * Enum for the permissible values of image orientation.
 */
public enum Orientation {

  PORTRAIT, LANDSCAPE;

  /**
   * Determines the orientation given a certain height and width.
   *
   * @param width The width of the image/page.
   * @param height The height of the image/page.
   * @return The orientation.
   */
  public static Orientation calculate(int width, int height) {
    return width > height ? Orientation.LANDSCAPE : Orientation.PORTRAIT;
  }

  /**
   * @return The name of this orientation, converted to lower case.
   */
  public String getNameLowercase() {
    return name().toLowerCase(Locale.ENGLISH);
  }

  /**
   * Find the orientation matching the given string. The match will be done ignoring the case.
   *
   * @param name The name of the orientation to find.
   * @return The orientation, or null if no orientation exists with the given name.
   */
  public static Orientation getFromNameCaseInsensitive(String name) {
    return Arrays.stream(values()).filter(orientation -> orientation.name().equalsIgnoreCase(name))
        .findAny().orElse(null);
  }
}
