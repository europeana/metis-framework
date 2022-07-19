package eu.europeana.normalization.dates.edtf;

/**
 * This class contains all the necessary data for represent an EDTF date with compliance level 1 See the specification of EDTF at
 * https://www.loc.gov/standards/datetime/
 */
public abstract class TemporalEntity {

  public String serialize() {
    return new EdtfSerializer().serialize(this);
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

  public abstract void switchDayMonth();

  public abstract TemporalEntity copy();

  public abstract Instant getFirstDay();

  public abstract Instant getLastDay();

  public abstract void removeTime();

}
