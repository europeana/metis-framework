package eu.europeana.normalization.dates;

/**
 * Identifies the cleaning operation that was done on a input value being normalised. This information is only of interest for the
 * DatesNormalizer
 */
public enum CleanId {
	INITIAL_TEXT, ENDING_TEXT, SQUARE_BRACKETS, CIRCA, SQUARE_BRACKETS_AND_CIRCA, SQUARE_BRACKET_END,
	PARENTHESES_FULL_VALUE, PARENTHESES_FULL_VALUE_AND_CIRCA
}
