package eu.europeana.normalization.dates.edtf;

/**
 * An abstract class that contains the template that an EDTF date with compliance level 1 should implement.
 * <p>See more in the specification of <a href="https://www.loc.gov/standards/datetime/">EDTF</a></p>
 */
public abstract class AbstractEdtfDate {

  public String serialize() {
    return EdtfSerializer.serialize(this);
  }

  @Override
  public String toString() {
    return EdtfSerializer.serialize(this);
  }

  public abstract void setApproximate(boolean approx);

  public abstract boolean isApproximate();

  public abstract void setUncertain(boolean uncertain);

  public abstract boolean isUncertain();

  public abstract boolean isTimeOnly();

  public abstract void switchDayAndMonth();

  public abstract AbstractEdtfDate copy();

  public abstract InstantEdtfDate getFirstDay();

  public abstract InstantEdtfDate getLastDay();

  public abstract void removeTime();

}
