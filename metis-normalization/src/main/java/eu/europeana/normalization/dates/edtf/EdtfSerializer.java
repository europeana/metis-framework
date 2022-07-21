package eu.europeana.normalization.dates.edtf;

import static java.lang.String.format;

/**
 * This class implements the serialisation of instances of EDTF objects.
 */
public final class EdtfSerializer {

  public static final int THRESHOLD_4_DIGITS_YEAR = 9999;

  // TODO: 19/07/2022 Shouldn't the methods of this class be in fact the toString methods of the relevant classes??

  private EdtfSerializer() {
  }

  public static String serialize(AbstractEdtfDate edtfDate) {
    if (edtfDate instanceof InstantEdtfDate) {
      return serializeInstant((InstantEdtfDate) edtfDate);
    }
    return serializeInstant(((IntervalEdtfDate) edtfDate).getStart()) + "/" + serializeInstant(
        ((IntervalEdtfDate) edtfDate).getEnd());
  }

  private static String serializeInstant(InstantEdtfDate edtfDate) {
    StringBuilder stringBuilder = new StringBuilder();
    //Date part serialization
    if (edtfDate.getEdtfDatePart() != null) {
      if (edtfDate.getEdtfDatePart().isUnknown()) {
        stringBuilder.append("");
      } else if (edtfDate.getEdtfDatePart().isUnspecified()) {
        stringBuilder.append("..");
      } else {
        stringBuilder.append(serializeDatePart(edtfDate));
      }
    }
    stringBuilder.append(serializeTimePart(edtfDate));
    return stringBuilder.toString();
  }

  private static String serializeDatePart(InstantEdtfDate edtfDate) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(serializeYear(edtfDate.getEdtfDatePart()));
    //If the year is below or above threshold(prefixed with Y) we stop
    if (edtfDate.getEdtfDatePart().getYear() < -THRESHOLD_4_DIGITS_YEAR
        || edtfDate.getEdtfDatePart().getYear() > THRESHOLD_4_DIGITS_YEAR) {
      return stringBuilder.toString();
    }

    //Append Month and day
    if (edtfDate.getEdtfDatePart().getMonth() != null && edtfDate.getEdtfDatePart().getMonth() > 0) {
      stringBuilder.append("-").append(zeroPadding(edtfDate.getEdtfDatePart().getMonth(), 2));
      if (edtfDate.getEdtfDatePart().getDay() != null && edtfDate.getEdtfDatePart().getDay() > 0) {
        stringBuilder.append("-").append(zeroPadding(edtfDate.getEdtfDatePart().getDay(), 2));
      }
    }
    //Append approximate/uncertain
    if (edtfDate.getEdtfDatePart().isApproximate() && edtfDate.getEdtfDatePart().isUncertain()) {
      stringBuilder.append("%");
    } else if (edtfDate.getEdtfDatePart().isApproximate()) {
      stringBuilder.append("~");
    } else if (edtfDate.getEdtfDatePart().isUncertain()) {
      stringBuilder.append("?");
    }
    return stringBuilder.toString();
  }


  private static String serializeTimePart(InstantEdtfDate edtfDate) {
    StringBuilder stringBuilder = new StringBuilder();
    // TODO: 20/07/2022 Why the hour,minute,second has to be != 0 ??
    //  In fact checking the value and then nullity probably will cause an issue if it was null.
    if (edtfDate.getEdtfTimePart() != null && (edtfDate.getEdtfTimePart().getHour() != 0
        || edtfDate.getEdtfTimePart().getMinute() != 0 || edtfDate.getEdtfTimePart().getSecond() != 0)) {
      stringBuilder.append("T").append(zeroPadding(edtfDate.getEdtfTimePart().getHour(), 2));
      if (edtfDate.getEdtfTimePart().getMinute() != null) {
        stringBuilder.append(":").append(zeroPadding(edtfDate.getEdtfTimePart().getMinute(), 2));
        if (edtfDate.getEdtfTimePart().getSecond() != null) {
          stringBuilder.append(":").append(zeroPadding(edtfDate.getEdtfTimePart().getSecond(), 2));
          if (edtfDate.getEdtfTimePart().getMillisecond() != null) {
            stringBuilder.append(".").append(zeroPadding(edtfDate.getEdtfTimePart().getMillisecond(), 3));
          }
        }
      }
    }
    return stringBuilder.toString();
  }


  private static String serializeYear(EdtfDatePart edtfDatePart) {
    final String serializedYear;
    if (edtfDatePart.getYear() < -THRESHOLD_4_DIGITS_YEAR || edtfDatePart.getYear() > THRESHOLD_4_DIGITS_YEAR) {
      serializedYear = "Y" + edtfDatePart.getYear();
    } else {
      final String paddedYear = zeroPadding(Math.abs(edtfDatePart.getYear()), 4);
      final String prefix = edtfDatePart.getYear() < 0 ? "-" : "";
      serializedYear = prefix + getYearWithPrecisionApplied(edtfDatePart, paddedYear);
    }
    return serializedYear;
  }

  private static String getYearWithPrecisionApplied(EdtfDatePart edtfDatePart, String paddedYear) {
    final String yearWithAppliedPrecision;
    if (edtfDatePart.getYearPrecision() == null) {
      yearWithAppliedPrecision = paddedYear;
    } else {
      switch (edtfDatePart.getYearPrecision()) {
        case MILLENNIUM:
          // TODO: 19/07/2022 What happens if a year was first padded and then substringed in this cases?
          yearWithAppliedPrecision = paddedYear.charAt(0) + "XXX";
          break;
        case CENTURY:
          yearWithAppliedPrecision = paddedYear.substring(0, 2) + "XX";
          break;
        case DECADE:
        default:
          yearWithAppliedPrecision = paddedYear.substring(0, 3) + "X";
          break;
      }
    }
    return yearWithAppliedPrecision;
  }

  private static String zeroPadding(int value, int paddingLength) {
    final String paddingFormat = "%0" + paddingLength + "d";
    return format(paddingFormat, value);
  }
}
