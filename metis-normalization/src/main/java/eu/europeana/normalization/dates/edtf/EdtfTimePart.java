package eu.europeana.normalization.dates.edtf;

import java.io.Serializable;

/**
 * A data class for representing the hours, minutes, seconds, miliseconds and timezone part of an EDTF date
 */
public class EdtfTimePart implements Serializable {

  private static final long serialVersionUID = 7275202766728354307L;

  private Integer hour;
  private Integer minute;
  private Integer second;
  private Integer millisecond;
  private Integer timezone;

  public EdtfTimePart() {
  }

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

}
