package eu.europeana.normalization.dates.edtf;

/**
 * An abstract class that contains the template that an EDTF date with compliance level 1 should implement.
 * <p>See more in the specification of <a href="https://www.loc.gov/standards/datetime/">EDTF</a></p>
 */
public abstract class AbstractEDTFDate {

  public String serialize() {
    return new EDTFSerializer().serialize(this);
  }

  @Override
  public String toString() {
    return EDTFSerializer.serialize(this);
  }

  public abstract void setApproximate(boolean approx);

  public abstract boolean isApproximate();

  public abstract void setUncertain(boolean uncertain);

  public abstract boolean isUncertain();

  public abstract boolean isTimeOnly();

  public abstract void switchDayMonth();

  public abstract AbstractEDTFDate copy();

  public abstract InstantEDTFDate getFirstDay();

  public abstract InstantEDTFDate getLastDay();

  public abstract void removeTime();

}
