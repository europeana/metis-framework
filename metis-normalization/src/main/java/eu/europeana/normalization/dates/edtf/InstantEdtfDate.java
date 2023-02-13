package eu.europeana.normalization.dates.edtf;

import eu.europeana.normalization.dates.edtf.EdtfDatePart.EdtfDatePartBuilder;

/**
 * Part of an EDTF date that represents a point in time with various degrees of precision
 */
public class InstantEdtfDate extends AbstractEdtfDate {

  public static final int THRESHOLD_4_DIGITS_YEAR = 9999;
  private EdtfDatePart edtfDatePart;

  public InstantEdtfDate(EdtfDatePart edtfDatePart) {
    this.edtfDatePart = edtfDatePart;
  }

  /**
   * Copy constructor.
   *
   * @param instantEdtfDate the internal instant date to copy
   */
  public InstantEdtfDate(InstantEdtfDate instantEdtfDate) {
    this(new EdtfDatePartBuilder(instantEdtfDate.getEdtfDatePart()).build());
  }

  @Override
  public boolean isTimeOnly() {
    return edtfDatePart == null;
  }

  public EdtfDatePart getEdtfDatePart() {
    return edtfDatePart;
  }

  public void setEdtfDatePart(EdtfDatePart edtfDatePart) {
    this.edtfDatePart = edtfDatePart;
  }

  @Override
  public void setApproximate(boolean approximate) {
    edtfDatePart.setApproximate(approximate);
  }

  @Override
  public void setUncertain(boolean uncertain) {
    edtfDatePart.setUncertain(uncertain);
  }

  @Override
  public boolean isApproximate() {
    return edtfDatePart.isApproximate();
  }

  @Override
  public boolean isUncertain() {
    return edtfDatePart.isUncertain();
  }

  @Override
  public boolean isUnspecified() {
    return edtfDatePart.isUnspecified();
  }

  @Override
  public void switchDayAndMonth() {
    if (edtfDatePart != null) {
      edtfDatePart.switchDayAndMonth();
    }
  }

  @Override
  public InstantEdtfDate getFirstDay() {
    InstantEdtfDate firstDay = null;
    if (getEdtfDatePart() != null && !getEdtfDatePart().isUnknown() && !getEdtfDatePart().isUnspecified()) {
      EdtfDatePart firstDayNew = getEdtfDatePart().firstDayOfYearDatePart();
      firstDay = new InstantEdtfDate(firstDayNew);
    }

    // TODO: 25/07/2022 What about > THRESHOLD_4_DIGITS_YEAR??
    //The part where > THRESHOLD_4_DIGITS_YEAR is not possible because it's in the future, so we don't have to check it.
    //Verify though that the contents of this class are always considered valid before the call of this method.
    else if (getEdtfDatePart().getYear() != null && getEdtfDatePart().getYear() < -THRESHOLD_4_DIGITS_YEAR) {
      final EdtfDatePart newEdtfDatePart = new EdtfDatePart.EdtfDatePartBuilder(getEdtfDatePart().getYear()).build();
      firstDay = new InstantEdtfDate(newEdtfDatePart);
    }

    return firstDay;
  }

  @Override
  public InstantEdtfDate getLastDay() {
    InstantEdtfDate lastDay = null;
    if (getEdtfDatePart() != null && !getEdtfDatePart().isUnknown() && !getEdtfDatePart().isUnspecified()) {

      EdtfDatePart lastDayNew = getEdtfDatePart().lastDayOfYearDatePart();
      lastDay = new InstantEdtfDate(lastDayNew);
    } else if (getEdtfDatePart().getYear() < -THRESHOLD_4_DIGITS_YEAR) {
      final EdtfDatePart newEdtfDatePart = new EdtfDatePart.EdtfDatePartBuilder(getEdtfDatePart().getYear()).build();
      lastDay = new InstantEdtfDate(newEdtfDatePart);
    }
    return lastDay;
  }

  public Integer getCentury() {
    final int century;

    // TODO: 25/07/2022 getEdtfDatePart() or getEdtfDatePart().getYear() might be null??
    //Better to check both for nullity and if they are null we then throw an exception.
    if (getEdtfDatePart().getYear() < 0) {
      century = -1;
    } else if (getEdtfDatePart().getYearPrecision() == null) {
      int hundreds = getEdtfDatePart().getYear() / 100;
      int remainder = getEdtfDatePart().getYear() % 100;
      century = (remainder == 0) ? hundreds : (hundreds + 1);
    } else {
      int hundreds = getEdtfDatePart().getYear() / 100;
      century = hundreds + 1;
    }
    return century;
  }

  @Override
  public String toString() {
    return this.edtfDatePart.toString();
  }
}
