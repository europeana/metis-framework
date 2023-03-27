package eu.europeana.indexing.utils;

import eu.europeana.indexing.exception.SetupRelatedIndexingException;

/**
 * The type Indexing settings utils.
 */
public class IndexingSettingsUtils {

  /**
   * Non null field name t.
   *
   * @param <T> the type parameter
   * @param value the value
   * @param fieldName the field name
   * @return the t
   * @throws SetupRelatedIndexingException the setup related indexing exception
   */
  public static <T> T nonNullFieldName(T value, String fieldName) throws SetupRelatedIndexingException {
    if (value == null) {
      throw new SetupRelatedIndexingException(
          String.format("Value '%s' cannot be null.", fieldName));
    }
    return value;
  }

  /**
   * Non null message t.
   *
   * @param <T> the type parameter
   * @param value the value
   * @param message the message
   * @return the t
   * @throws SetupRelatedIndexingException the setup related indexing exception
   */
  public static <T> T nonNullMessage(T value, String message) throws SetupRelatedIndexingException {
    if (value == null) {
      throw new SetupRelatedIndexingException(message);
    }
    return value;
  }

  /**
   * Non null illegal t.
   *
   * @param <T> the type parameter
   * @param value the value
   * @param message the message
   * @return the t
   * @throws SetupRelatedIndexingException the setup related indexing exception
   */
  public static <T> T nonNullIllegal(T value, String message) throws SetupRelatedIndexingException {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }
}
