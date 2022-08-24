package eu.europeana.metis.core.common;

import eu.europeana.metis.exception.BadContentException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * This class contains functionality concerning the parsing and composing of depublish record IDs.
 */
public final class RecordIdUtils {

  private static final Pattern LINE_SEPARATION_PATTERN = Pattern.compile("\\R");
  private static final Pattern INVALID_CHAR_IN_RECORD_ID = Pattern.compile("[^a-zA-Z0-9_]");
  private static final Pattern FULL_RECORD_ID_PATTERN = Pattern.compile("^/([^/\\s]+)/([^/\\s]+)$");

  private RecordIdUtils() {
  }

  /**
   * Composes full record IDs.
   * @param datasetId The ID of the dataset.
   * @param recordIds The (simple) IDs of the records.
   * @return The full/qualified IDs of the records.
   */
  public static Set<String> composeFullRecordIds(String datasetId, Collection<String> recordIds) {
    return recordIds.stream().map(recordId -> composeFullRecordId(datasetId, recordId)).collect(
            Collectors.toSet());
  }

  /**
   * Composes a full record ID.
   *
   * @param datasetId The ID of the dataset.
   * @param recordId The (simple) ID of the record.
   * @return The full/qualified ID of the record.
   */
  public static String composeFullRecordId(String datasetId, String recordId) {
    return "/" + datasetId + "/" + recordId;
  }

  /**
   * Decomposes a full record ID into the dataset ID and the simple record ID part.
   *
   * @param fullRecordId The full record ID.
   * @return A String pair containing first the dataset ID, and second the simple record ID.
   */
  public static Pair<String, String> decomposeFullRecordId(String fullRecordId) {
    final Matcher matcher = FULL_RECORD_ID_PATTERN.matcher(fullRecordId);
    if (!matcher.find()) {
      return null;
    }
    return new ImmutablePair<>(matcher.group(1), matcher.group(2));
  }

  /**
   * This method checks/validates and normalizes incoming depublished record IDs for persistence.
   *
   * @param datasetId The dataset ID to which the depublished record belongs.
   * @param recordIdsInSeparateLines The unchecked and non-normalized record ID, in a
   * newline-separated string. The method accepts and ignores empty lines.
   * @return The checked and normalized record IDs.
   * @throws BadContentException In case any of the incoming record IDs does not validate.
   */
  public static Set<String> checkAndNormalizeRecordIds(String datasetId,
          String recordIdsInSeparateLines) throws BadContentException {
    final String[] recordIds = LINE_SEPARATION_PATTERN.split(recordIdsInSeparateLines);
    final Set<String> normalizedRecordIds = new HashSet<>(recordIds.length);
    for (String recordId : recordIds) {
      checkAndNormalizeRecordId(datasetId, recordId).ifPresent(normalizedRecordIds::add);
    }
    return normalizedRecordIds;
  }

  /**
   * This method checks/validates and normalizes an incoming depublished record ID for persistence.
   *
   * @param datasetId The dataset ID to which the depublished record belongs.
   * @param recordId The unchecked and non-normalized record ID.
   * @return The checked and normalized record ID. Or empty Optional if the incoming ID is empty.
   * @throws BadContentException In case the incoming record ID does not validate.
   */
  public static Optional<String> checkAndNormalizeRecordId(String datasetId, String recordId)
          throws BadContentException {

    // Trim and check that string is not empty. We allow empty record IDs, we return empty optional.
    final String recordIdTrimmed = recordId.trim();
    final Optional<String> result;
    if (recordIdTrimmed.isEmpty()) {
      result = Optional.empty();
    } else {
      result = Optional.of(validateNonEmptyRecordId(datasetId, recordIdTrimmed));
    }
    return result;
  }

  private static String validateNonEmptyRecordId(String datasetId, String recordIdTrimmed)
          throws BadContentException {

    // Check if it is a valid URI. This also checks for spaces. Relative URIs pass this test too.
    try {
      new URI(recordIdTrimmed);
    } catch (URISyntaxException e) {
      throw new BadContentException("Invalid record ID (is not a valid URI): " + recordIdTrimmed,
              e);
    }

    // Split in segments based on the slash - don't discard empty segments at the end.
    final String[] segments = recordIdTrimmed.split("/", -1);
    final String lastSegment = segments[segments.length - 1];
    final String penultimateSegment = segments.length > 1 ? segments[segments.length - 2] : "";

    // Check last segment: cannot be empty.
    if (lastSegment.isEmpty()) {
      throw new BadContentException("Invalid record ID (ends with '/'): " + recordIdTrimmed);
    }

    // Check last segment: cannot contain invalid characters
    if (INVALID_CHAR_IN_RECORD_ID.matcher(lastSegment).find()) {
      throw new BadContentException(
              "Invalid record ID (contains invalid characters): " + lastSegment);
    }

    // Check penultimate segment: if it is empty, it must be because it is the start of the ID.
    if (penultimateSegment.isEmpty() && segments.length > 2) {
      throw new BadContentException(
              "Invalid record ID (dataset ID seems to be missing): " + recordIdTrimmed);
    }

    // Check penultimate segment: if it is not empty, it must be equal to the dataset ID.
    if (!penultimateSegment.isEmpty() && !penultimateSegment.equals(datasetId)) {
      throw new BadContentException(
              "Invalid record ID (doesn't seem to belong to the correct dataset): "
                      + recordIdTrimmed);
    }

    // Return the last segment (the record ID without the dataset ID).
    return lastSegment;
  }
}
