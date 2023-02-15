package eu.europeana.normalization.dates.edtf;

import eu.europeana.normalization.dates.edtf.EdtfDatePart.EdtfDatePartBuilder;

/**
 * Part of an EDTF date that represents a point in time with various degrees of precision
 */
public class InstantEdtfDate extends AbstractEdtfDate {

  public static final int THRESHOLD_4_DIGITS_YEAR = 9999;
  private EdtfDatePart edtfDatePart;
  private DateQualification dateQualification = DateQualification.EMPTY;

  public InstantEdtfDate(EdtfDatePart edtfDatePart) {
    this.edtfDatePart = edtfDatePart;
  }

  public InstantEdtfDate(EdtfDatePart edtfDatePart, DateQualification dateQualification) {
    this.edtfDatePart = edtfDatePart;
    this.dateQualification = dateQualification;
  }

  /**
   * Copy constructor.
   *
   * @param instantEdtfDate the internal instant date to copy
   */
  public InstantEdtfDate(InstantEdtfDate instantEdtfDate) {
    this(new EdtfDatePartBuilder(instantEdtfDate.getEdtfDatePart()).build(false));
  }

  public EdtfDatePart getEdtfDatePart() {
    return edtfDatePart;
  }

  public void setDateQualification(DateQualification dateQualification) {
    this.dateQualification = dateQualification;
  }

  public DateQualification getDateQualification() {
    return dateQualification;
  }

  @Override
  public boolean isYearPrecision() {
    return edtfDatePart.getYearPrecision() != null;
  }

  @Override
  public boolean isUnspecified() {
    return edtfDatePart.isUnspecified();
  }

  @Override
  public InstantEdtfDate getFirstDay() {
    InstantEdtfDate firstDay = null;
    if (getEdtfDatePart() != null && !getEdtfDatePart().isUnknown() && !getEdtfDatePart().isUnspecified()) {

      // TODO: 25/07/2022 What about > THRESHOLD_4_DIGITS_YEAR??
      //The part where > THRESHOLD_4_DIGITS_YEAR is not possible because it's in the future, so we don't have to check it.
      //Verify though that the contents of this class are always considered valid before the call of this method.
      if (getEdtfDatePart().getYear().getValue() < -THRESHOLD_4_DIGITS_YEAR) {
        final EdtfDatePart newEdtfDatePart = new EdtfDatePart.EdtfDatePartBuilder(
            getEdtfDatePart().getYear().getValue()).build(false);
        firstDay = new InstantEdtfDate(newEdtfDatePart);
      } else {
        EdtfDatePart firstDayNew = getEdtfDatePart().firstDayOfYearDatePart();
        firstDay = new InstantEdtfDate(firstDayNew);
      }
    }

    return firstDay;
  }

  @Override
  public InstantEdtfDate getLastDay() {
    InstantEdtfDate lastDay = null;
    if (getEdtfDatePart() != null && !getEdtfDatePart().isUnknown() && !getEdtfDatePart().isUnspecified()) {
      if (getEdtfDatePart().getYear().getValue() < -THRESHOLD_4_DIGITS_YEAR) {
        final EdtfDatePart newEdtfDatePart = new EdtfDatePart.EdtfDatePartBuilder(
            getEdtfDatePart().getYear().getValue()).build(false);
        lastDay = new InstantEdtfDate(newEdtfDatePart);
      } else {
        EdtfDatePart lastDayNew = getEdtfDatePart().lastDayOfYearDatePart();
        lastDay = new InstantEdtfDate(lastDayNew);
      }
    }
    return lastDay;
  }

  public Integer getCentury() {
    return edtfDatePart.getCentury();
  }

  @Override
  public String toString() {
    final StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(this.edtfDatePart.toString());
    if (dateQualification != null && dateQualification != DateQualification.EMPTY) {
      stringBuilder.append(dateQualification.getCharacter());
    }
    return stringBuilder.toString();

  }
}
