package eu.europeana.indexing.utils;

import eu.europeana.indexing.exception.SetupRelatedIndexingException;

public class IndexingSettingsUtils {
  public static <T> T nonNullFieldName(T value, String fieldName) throws SetupRelatedIndexingException {
    if (value == null) {
      throw new SetupRelatedIndexingException(
          String.format("Value '%s' cannot be null.", fieldName));
    }
    return value;
  }
  public static <T> T nonNullMessage(T value, String message) throws SetupRelatedIndexingException {
    if (value == null) {
      throw new SetupRelatedIndexingException(message);
    }
    return value;
  }
  public static <T> T nonNullIllegal(T value, String message) throws SetupRelatedIndexingException {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }
}
