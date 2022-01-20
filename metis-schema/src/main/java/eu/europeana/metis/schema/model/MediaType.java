package eu.europeana.metis.schema.model;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

/**
 * This class lists the supported media types.
 *
 * @author jochen
 */
public enum MediaType {

  /**
   * Audio
   **/
  AUDIO("AUDIO"),

  /**
   * Video
   **/
  VIDEO("VIDEO"),

  /**
   * Text (including PDFs)
   **/
  TEXT("TEXT"),

  /**
   * Graphical
   **/
  IMAGE("IMAGE"),

  /**
   * 3D
   */
  THREE_D("3D"),

  /**
   * Media that is not any of the other kinds.
   **/
  OTHER("OTHER");


  private static final String[] SUPPORTED_APPLICATION_TYPES_AS_TEXT = new String[]{
      "application/xml", "application/rtf", "application/epub", "application/pdf",
      "application/xhtml+xml"};

  private final String stringRepresentation;

  MediaType(String stringRepresentation) {
    this.stringRepresentation = stringRepresentation;
  }

  @Override
  @JsonValue
  public String toString() {
    return stringRepresentation;
  }

  /**
   * Obtains the media type of a given mime type. This method accepts media types with subtypes and/or parameters.
   *
   * @param mediaType The media type.
   * @return The media type to which the mime type belongs.
   */
  public static MediaType getMediaType(String mediaType) {
    final MediaType result;
    if (mediaType == null) {
      result = OTHER;
    } else if (mediaType.startsWith("image/")) {
      result = IMAGE;
    } else if (mediaType.startsWith("audio/")) {
      result = AUDIO;
    } else if (mediaType.startsWith("video/") || mediaType.startsWith("application/dash+xml")) {
      result = VIDEO;
    } else if (mediaType.startsWith("text/") || isApplicationMediaRepresentingText(mediaType)) {
      result = TEXT;
    } else {
      result = OTHER;
    }
    return result;
  }

  /**
   * Determines whether the supplied media base type matches the candidate media type. This method
   * takes possible subtypes into account: if the candidate has a subtype, the media type is
   * required to have it too, but if the candidate does not have a subtype, the media type is
   * accepted either with or without subtype.
   *
   * @param candidateType The candidate type. Does not have parameters.
   * @param mediaType The base type. Does not have parameters.
   */
  private static boolean mediaTypeMatchesCandidate(String candidateType, String mediaType) {

    // Check for the type itself as well as the addition of a possible subtype.
    return mediaType.equals(candidateType) || (!candidateType.contains("+") && mediaType
        .startsWith(candidateType + "+"));
  }

  /**
   * Determines whether the supplied media type is a text media type starting with 'application/'.
   * This method takes subtypes and parameters into consideration.
   *
   * @param mediaType The media type to categorize.
   * @return Whether the media type is a text media type starting with 'application/'.
   */
  private static boolean isApplicationMediaRepresentingText(String mediaType) {

    // Remove any parameters from the media type.
    final String baseType = mediaType.split(";", 2)[0];

    // Match against all possible application types that are accepted as test types.
    return Arrays.stream(SUPPORTED_APPLICATION_TYPES_AS_TEXT)
        .anyMatch(candidate -> mediaTypeMatchesCandidate(candidate, baseType));
  }
}
