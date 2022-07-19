package eu.europeana.normalization.dates.edtf;

/**
 * This class implements the serialisation of instances of TemporalEntity as EDTF strings.
 */
public class EdtfSerializer {

  public static String serialize(TemporalEntity edtf) {
    if (edtf instanceof Instant) {
      return serializeInstant((Instant) edtf);
    }
    return serializeInstant(((Interval) edtf).getStart()) + "/" + serializeInstant(((Interval) edtf).getEnd());
  }

  private static String serializeInstant(Instant edtf) {
    StringBuffer buf = new StringBuffer();
    if (edtf.getDate() != null) {
      if (edtf.getDate().isUnkown()) {
        buf.append("");
      } else if (edtf.getDate().isUnspecified()) {
        buf.append("..");
      } else {
        buf.append(serializeYear(edtf.getDate()));
        if (edtf.getDate().getYear() < -9999 || edtf.getDate().getYear() > 9999) {
          return buf.toString();
        }
        if (edtf.getDate().getMonth() != null && edtf.getDate().getMonth() > 0) {
          buf.append("-").append(padInt(edtf.getDate().getMonth(), 2));
          if (edtf.getDate().getDay() != null && edtf.getDate().getDay() > 0) {
            buf.append("-").append(padInt(edtf.getDate().getDay(), 2));
          }
        }
        if (edtf.getDate().isApproximate() && edtf.getDate().isUncertain()) {
          buf.append("%");
        } else if (edtf.getDate().isApproximate()) {
          buf.append("~");
        } else if (edtf.getDate().isUncertain()) {
          buf.append("?");
        }
      }
    }
    if (edtf.getTime() != null && (edtf.getTime().getHour() != 0 || edtf.getTime().getMinute() != 0
        || edtf.getTime().getSecond() != 0)) {
      buf.append("T").append(padInt(edtf.getTime().getHour(), 2));
      if (edtf.getTime().getMinute() != null) {
        buf.append(":").append(padInt(edtf.getTime().getMinute(), 2));
        if (edtf.getTime().getSecond() != null) {
          buf.append(":").append(padInt(edtf.getTime().getSecond(), 2));
          if (edtf.getTime().getMillisecond() != null) {
            buf.append(".").append(padInt(edtf.getTime().getMillisecond(), 3));
          }
        }
      }
    }
    return buf.toString();
  }

  private static String serializeYear(Date date) {
    if (date.getYear() < -9999 || date.getYear() > 9999) {
      return "Y" + date.getYear();
    }
    String yearStr = padInt(Math.abs(date.getYear()), 4);
    String prefix = date.getYear() < 0 ? "-" : "";
    if (date.getYearPrecision() != null) {
      switch (date.getYearPrecision()) {
        case MILLENIUM:
          return prefix + yearStr.substring(0, 1) + "XXX";
        case CENTURY:
          return prefix + yearStr.substring(0, 2) + "XX";
        case DECADE:
          return prefix + yearStr.substring(0, 3) + "X";
      }
    }
    return prefix + yearStr;
  }
  //	private String serializeTwoDigits(Integer number) {
  //		assert number>=0 && number<100;
  //		if(number<10)
  //			return "0"+number;
  //		return String.valueOf(number);
  //	}
  //	private String serializeFourDigits(Integer number) {
  //		assert number>=10000 && number<10000;
  //		switch (key) {
  //		case value:
  //
  //			break;
  //
  //		default:
  //			break;
  //		}
  //		if(number<10)
  //			return "0"+number;
  //		return String.valueOf(number);
  //	}

  private static String padInt(int value, int paddingLength) {
    return String.format("%0" + paddingLength + "d", value);
  }

  public static void main(String[] args) throws Exception {
    System.out.println(padInt(-1000, 4));
  }
}
