package eu.europeana.normalization.dates.edtf;

/**
 * This class implements the serialisation of instances of TemporalEntity as EDTF strings.
 */
public class EDTFSerializer {

  public static String serialize(AbstractEDTFDate edtf) {
    if (edtf instanceof InstantEDTFDate) {
      return serializeInstant((InstantEDTFDate) edtf);
    }
    return serializeInstant(((IntervalEDTFDate) edtf).getStart()) + "/" + serializeInstant(((IntervalEDTFDate) edtf).getEnd());
  }

  private static String serializeInstant(InstantEDTFDate edtf) {
    StringBuffer buf = new StringBuffer();
    if (edtf.getEdtfDatePart() != null) {
      if (edtf.getEdtfDatePart().isUnknown()) {
        buf.append("");
      } else if (edtf.getEdtfDatePart().isUnspecified()) {
        buf.append("..");
      } else {
        buf.append(serializeYear(edtf.getEdtfDatePart()));
        if (edtf.getEdtfDatePart().getYear() < -9999 || edtf.getEdtfDatePart().getYear() > 9999) {
          return buf.toString();
        }
        if (edtf.getEdtfDatePart().getMonth() != null && edtf.getEdtfDatePart().getMonth() > 0) {
          buf.append("-").append(padInt(edtf.getEdtfDatePart().getMonth(), 2));
          if (edtf.getEdtfDatePart().getDay() != null && edtf.getEdtfDatePart().getDay() > 0) {
            buf.append("-").append(padInt(edtf.getEdtfDatePart().getDay(), 2));
          }
        }
        if (edtf.getEdtfDatePart().isApproximate() && edtf.getEdtfDatePart().isUncertain()) {
          buf.append("%");
        } else if (edtf.getEdtfDatePart().isApproximate()) {
          buf.append("~");
        } else if (edtf.getEdtfDatePart().isUncertain()) {
          buf.append("?");
        }
      }
    }
    if (edtf.getEdtfTimePart() != null && (edtf.getEdtfTimePart().getHour() != 0 || edtf.getEdtfTimePart().getMinute() != 0
        || edtf.getEdtfTimePart().getSecond() != 0)) {
      buf.append("T").append(padInt(edtf.getEdtfTimePart().getHour(), 2));
      if (edtf.getEdtfTimePart().getMinute() != null) {
        buf.append(":").append(padInt(edtf.getEdtfTimePart().getMinute(), 2));
        if (edtf.getEdtfTimePart().getSecond() != null) {
          buf.append(":").append(padInt(edtf.getEdtfTimePart().getSecond(), 2));
          if (edtf.getEdtfTimePart().getMillisecond() != null) {
            buf.append(".").append(padInt(edtf.getEdtfTimePart().getMillisecond(), 3));
          }
        }
      }
    }
    return buf.toString();
  }

  private static String serializeYear(EDTFDatePart edtfDatePart) {
    if (edtfDatePart.getYear() < -9999 || edtfDatePart.getYear() > 9999) {
      return "Y" + edtfDatePart.getYear();
    }
    String yearStr = padInt(Math.abs(edtfDatePart.getYear()), 4);
    String prefix = edtfDatePart.getYear() < 0 ? "-" : "";
    if (edtfDatePart.getYearPrecision() != null) {
      switch (edtfDatePart.getYearPrecision()) {
        case MILLENNIUM:
          return prefix + yearStr.substring(0, 1) + "XXX";
        case CENTURY:
          return prefix + yearStr.substring(0, 2) + "XX";
        case DECADE:
          return prefix + yearStr.substring(0, 3) + "X";
      }
    }
    return prefix + yearStr;
  }

  private static String padInt(int value, int paddingLength) {
    return String.format("%0" + paddingLength + "d", value);
  }
}
