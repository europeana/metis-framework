package eu.europeana.normalization.dates;

import java.util.EnumSet;

/**
 * Identifies the cleaning operation that was done on a input value being normalised. This information is only of interest for the
 * DatesNormalizer
 */
public enum CleanOperationId {
  INITIAL_TEXT,
  ENDING_TEXT,
  SQUARE_BRACKETS,
  CIRCA,
  SQUARE_BRACKETS_AND_CIRCA,
  SQUARE_BRACKET_END,
  PARENTHESES_FULL_VALUE,
  PARENTHESES_FULL_VALUE_AND_CIRCA;

  private static final EnumSet<CleanOperationId> APPROXIMATE_CLEAN_OPERATION_IDS_FOR_DATE_PROPERTY = EnumSet.of(
      CleanOperationId.CIRCA, CleanOperationId.SQUARE_BRACKETS_AND_CIRCA, CleanOperationId.PARENTHESES_FULL_VALUE_AND_CIRCA);

  private static final EnumSet<CleanOperationId> APPROXIMATE_CLEAN_OPERATION_IDS_FOR_GENERIC_PROPERTY = EnumSet.of(
      CleanOperationId.CIRCA, CleanOperationId.SQUARE_BRACKETS_AND_CIRCA);

  public static boolean isApproximateCleanOperationIdForDateProperty(CleanOperationId cleanOperationId) {
    return APPROXIMATE_CLEAN_OPERATION_IDS_FOR_DATE_PROPERTY.contains(cleanOperationId);
  }

  public static boolean isApproximateCleanOperationIdForGenericProperty(CleanOperationId cleanOperationId) {
    return APPROXIMATE_CLEAN_OPERATION_IDS_FOR_GENERIC_PROPERTY.contains(cleanOperationId);
  }


}
