package eu.europeana.normalization.dates.edtf;

/**
 * Part of an EDTF date that represents a point in time with various degrees of precision.
 * <p>An instance of this class should contain a non-null value of {@link EdtfDatePart} with optional {@link DateQualification}.
 * Or it should instead contain a {@link DateEdgeType} that is not {@link DateEdgeType#DECLARED}.</p>
 */
public class InstantEdtfDate extends AbstractEdtfDate {

  public static final int THRESHOLD_4_DIGITS_YEAR = 9999;
  private final EdtfDatePart edtfDatePart;
  private DateQualification dateQualification = DateQualification.EMPTY;
  private DateEdgeType dateEdgeType = DateEdgeType.DECLARED;

  private InstantEdtfDate(DateEdgeType dateEdgeType) {
    this.edtfDatePart = null;
    this.dateEdgeType = dateEdgeType;
  }

  public InstantEdtfDate(EdtfDatePart edtfDatePart) {
    this(edtfDatePart, DateQualification.EMPTY);
  }

  public InstantEdtfDate(EdtfDatePart edtfDatePart, DateQualification dateQualification) {
    this.edtfDatePart = edtfDatePart;
    this.dateQualification = dateQualification;
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
    return edtfDatePart != null && edtfDatePart.getYearPrecision() != null;
  }

  @Override
  public boolean isOpen() {
    return dateEdgeType == DateEdgeType.OPEN;
  }

  public DateEdgeType getDateEdgeType() {
    return dateEdgeType;
  }

  public static InstantEdtfDate getUnknownInstance() {
    return new InstantEdtfDate(DateEdgeType.UNKNOWN);
  }

  public static InstantEdtfDate getOpenInstance() {
    return new InstantEdtfDate(DateEdgeType.OPEN);
  }

  @Override
  public InstantEdtfDate getFirstDay() {
    InstantEdtfDate firstDay = null;
    if (dateEdgeType == DateEdgeType.DECLARED) {
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
    if (dateEdgeType == DateEdgeType.DECLARED) {
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
    if (dateEdgeType == DateEdgeType.OPEN || dateEdgeType == DateEdgeType.UNKNOWN) {
      stringBuilder.append("..");
    } else {
      stringBuilder.append(this.edtfDatePart.toString());
      if (dateQualification != null && dateQualification != DateQualification.EMPTY) {
        stringBuilder.append(dateQualification.getCharacter());
      }
    }
    return stringBuilder.toString();

  }
}
