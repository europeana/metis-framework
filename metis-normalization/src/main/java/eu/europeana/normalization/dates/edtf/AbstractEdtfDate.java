package eu.europeana.normalization.dates.edtf;

/**
 * An abstract class that contains the template that an EDTF date with compliance level 1 should implement.
 * <p>See more in the specification of <a href="https://www.loc.gov/standards/datetime/">EDTF</a></p>
 * <p>The date can contain a label, but can also be null</p>
 */
public abstract class AbstractEdtfDate {

  private final String label;

  public AbstractEdtfDate() {
    this.label = null;
  }

  public AbstractEdtfDate(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public abstract void setApproximate(boolean approx);

  public abstract boolean isApproximate();

  public abstract void setUncertain(boolean uncertain);

  public abstract boolean isUncertain();

  public abstract boolean isUnspecified();

  public abstract boolean isTimeOnly();

  public abstract void switchDayAndMonth();

  public abstract InstantEdtfDate getFirstDay();

  public abstract InstantEdtfDate getLastDay();

}
