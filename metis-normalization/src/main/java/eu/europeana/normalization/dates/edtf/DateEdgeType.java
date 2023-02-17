package eu.europeana.normalization.dates.edtf;


/**
 * Enum that indicates what type of a date is in an interval.
 * <p>
 * A date in an interval can be:
 *   <ul>
 *     <li>{@link #DECLARED}. Indicates whether there is a value representing an actual date</li>
 *     <li>{@link #OPEN}. Indicates whether the date is open, represented by '..' (e.g. if the input EDTF-compliant date interval string was equal to
 *         '<code>1900/..</code>'). Other character examples that this can be used to identify an open interval range is '?' or '-'</li>
 *     <li>{@link #UNKNOWN} Indicates whether the date is unknown, represented by an empty string ''
 *          (e.g. if the input EDTF-compliant date interval string was equal to '<code>1900/</code>').</li>
 *   </ul>
 * </p>
 */
public enum DateEdgeType {
  DECLARED(""), OPEN(".."), UNKNOWN("");

  private String stringRepresentation;

  DateEdgeType(String stringRepresentation) {
    this.stringRepresentation = stringRepresentation;
  }

  public String getStringRepresentation() {
    return stringRepresentation;
  }
}
