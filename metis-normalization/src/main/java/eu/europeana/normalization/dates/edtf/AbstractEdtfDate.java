package eu.europeana.normalization.dates.edtf;

/**
 * An abstract class that contains the template that an EDTF date with compliance level 1 should implement.
 * <p>See more in the specification of <a href="https://www.loc.gov/standards/datetime/">EDTF</a></p>
 * <p>The date can contain a label, but can also be null</p>
 */
public abstract class AbstractEdtfDate {

  private final String label;

  protected AbstractEdtfDate() {
    this.label = null;
  }

  protected AbstractEdtfDate(String label) {
    this.label = label;
  }

  /**
   * Overwrite the date qualification, mainly used for pre-sanitized values.
   *
   * @param dateQualification the date qualification
   */
  public abstract void overwriteQualification(DateQualification dateQualification);

  public String getLabel() {
    return label;
  }

  public abstract DateQualification getDateQualification();

  public abstract boolean isOpen();

  public abstract InstantEdtfDate getFirstDay();

  public abstract InstantEdtfDate getLastDay();

}
