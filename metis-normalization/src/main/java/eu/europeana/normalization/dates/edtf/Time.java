package eu.europeana.normalization.dates.edtf;

import java.io.Serializable;

/**
 * A data class for representing the hours, minutes, seconds, miliseconds and timezone part of an EDTF date
 */
public class Time implements Serializable {

  Integer hour;
  Integer minute;
  Integer second;
  Integer millisecond;
  Integer timezone;

  public Time(Integer hour, Integer minute, Integer second, Integer millisecond, Integer timezone) {
    super();
    this.hour = hour;
    this.minute = minute;
    this.second = second;
    this.millisecond = millisecond;
    this.timezone = timezone;
  }

  public Time(Integer hour, Integer minute, Integer second) {
    super();
    this.hour = hour;
    this.minute = minute;
    this.second = second;
  }

  public Time(Integer hour, Integer minute) {
    super();
    this.hour = hour;
    this.minute = minute;
  }

  public Time() {
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
