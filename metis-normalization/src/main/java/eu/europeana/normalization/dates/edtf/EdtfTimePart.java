package eu.europeana.normalization.dates.edtf;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * A data class for representing the hours, minutes, seconds, milliseconds and timezone part of an EDTF date
 */
public class EdtfTimePart implements Serializable {

  private static final long serialVersionUID = 7275202766728354307L;

  private Integer hour;
  private Integer minute;
  private Integer second;
  private Integer millisecond;
  private Integer timezone;

  public Integer getHour() {
    return hour;
  }

  public void setHour(Integer hour) {
    this.hour = hour;
  }

  public Integer getMinute() {
    return minute;
  }

  public void setMinute(Integer minute) {
    this.minute = minute;
  }

  public Integer getSecond() {
    return second;
  }

  public void setSecond(Integer second) {
    this.second = second;
  }

  public Integer getMillisecond() {
    return millisecond;
  }

  public void setMillisecond(Integer millisecond) {
    this.millisecond = millisecond;
  }

  public Integer getTimezone() {
    return timezone;
  }

  public void setTimezone(Integer timezone) {
    this.timezone = timezone;
  }

  @Override
  public String toString() {
    final DecimalFormat decimalFormat = new DecimalFormat("00");
    final DecimalFormat millisFormat = new DecimalFormat("000");
    StringBuilder stringBuilder = new StringBuilder();

    if (hour != null) {
      stringBuilder.append("T").append(decimalFormat.format(hour));
    }
    // A child value can only exist if the parent value exists.
    // TODO should the nested checks below not themselves be nested inside the hour!=null check above?
    //   You can't have a minute if there is no hour, right?
    if (minute != null) {
      stringBuilder.append(":").append(decimalFormat.format(minute));
      if (second != null) {
        stringBuilder.append(":").append(decimalFormat.format(second));
        if (millisecond != null) {
          stringBuilder.append(".").append(millisFormat.format(millisecond));
        }
      }
    }
    return stringBuilder.toString();
  }
}
