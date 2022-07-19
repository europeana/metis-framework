package eu.europeana.normalization.dates.extraction;

import eu.europeana.normalization.dates.edtf.InstantEDTFDate;

/**
 * A time interval, representing a DCMI period. They may be open.
 * <p>
 * For further information on DCMI periods please refer to http://dublincore.org/documents/dcmi-period/
 */
public final class DcmiPeriod {

  private final InstantEDTFDate start;
  private final InstantEDTFDate end;
  private final String name;

  /**
   * Create a new period. To create an open interval you may set one of the boundaries null.
   */
  public DcmiPeriod(InstantEDTFDate start, InstantEDTFDate end) {
    this(start, end, null);
  }

  /**
   * Create a new period with an optional name. To create an open interval you may set one of the bounbaries null.
   */
  public DcmiPeriod(InstantEDTFDate start, InstantEDTFDate end, String name) {
    if (start == null && end == null) {
      throw new IllegalStateException("A period must be bounded at least at one end");
    }
    //    if (start != null && end != null && end.before(start))
    //      throw new IllegalStateException("The end date is before the start date");

    this.start = start;
    this.end = end;
    this.name = name;
  }

  /**
   * Returns the start date of the period or null, if it has only an upper bound.
   */
  public InstantEDTFDate getStart() {
    return start;
  }

  /**
   * Returns the end date of the period or null, if it has only a lower bound.
   */
  public InstantEDTFDate getEnd() {
    return end;
  }

  /**
   * Returns the optional name of the period.
   *
   * @return the name of the period or null
   */
  public String getName() {
    return name;
  }

  /**
   * Checks if the interval is closed.
   */
  public boolean isClosed() {
    return start != null && end != null;
  }

  /**
   * Checks if the interval has a start boundary.
   */
  public boolean hasStart() {
    return start != null;
  }

  /**
   * Checks if the interval has an end boundary.
   */
  public boolean hasEnd() {
    return end != null;
  }

  /**
   * Checks if the interval has a name.
   */
  public boolean hasName() {
    return name != null;
  }

  @Override
  public String toString() {
    return "DCMIPeriod{" + "start=" + (start != null ? start : "]") + ", end=" + (end != null ? end : "[")
        + (name != null ? ", name='" + name + '\'' : "") + '}';
  }

}