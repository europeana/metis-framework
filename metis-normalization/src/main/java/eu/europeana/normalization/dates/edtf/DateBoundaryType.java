package eu.europeana.normalization.dates.edtf;


/**
 * Enum that indicates what type of date is in an interval.
 * <p>
 * A date in an interval can be:
 *   <ul>
 *     <li>{@link #DECLARED}. Indicates whether there is a value representing an actual date. Not meant to be (de)serialized</li>
 *     <li>{@link #OPEN}. Indicates whether the date is open, represented by '..' (e.g. if the input EDTF-compliant date interval string was equal to
 *         '<code>1900/..</code>').</li>
 *     <li>{@link #UNKNOWN} Indicates whether the date is unknown, represented by an empty string ''(deserialization) and '..'(serialization
 *          (e.g. if the input EDTF-compliant date interval string was equal to '<code>1900/</code>').</li>
 *   </ul>
 * </p>
 */
public enum DateBoundaryType {
  DECLARED(null, null),
  OPEN(DateBoundaryType.DEFAULT_OPEN_STRING, DateBoundaryType.DEFAULT_OPEN_STRING),
  UNKNOWN("", DateBoundaryType.DEFAULT_OPEN_STRING);

  public static final String DEFAULT_OPEN_STRING = "..";
  private final String deserializedRepresentation;
  private final String serializedRepresentation;

  DateBoundaryType(String deserializedRepresentation, String serializedRepresentation) {
    this.deserializedRepresentation = deserializedRepresentation;
    this.serializedRepresentation = serializedRepresentation;
  }

  public String getDeserializedRepresentation() {
    return deserializedRepresentation;
  }

  public String getSerializedRepresentation() {
    return serializedRepresentation;
  }
}
