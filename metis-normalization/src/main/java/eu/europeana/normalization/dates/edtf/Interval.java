package eu.europeana.normalization.dates.edtf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * An EDTF date that represents a period of time specified by a start and end date with various degrees of precision
 */
public class Interval extends TemporalEntity implements Serializable {

  Instant start;
  Instant end;

  public Interval(Instant start, Instant end) {
    super();
    this.start = start;
    this.end = end;
  }

  @Override
  public boolean isTimeOnly() {
    return (start == null || start.isTimeOnly()) && (end == null || end.isTimeOnly());
  }

  public Instant getStart() {
    return start;
  }

  public void setStart(Instant start) {
    this.start = start;
  }

  public Instant getEnd() {
    return end;
  }

  public void setEnd(Instant end) {
    this.end = end;
  }

  @Override
  public void setApproximate(boolean approx) {
    if (start != null && start.date != null) {
      start.date.setApproximate(approx);
    }
    if (end != null && end.date != null) {
      end.date.setApproximate(approx);
    }
  }

  @Override
  public void setUncertain(boolean uncertain) {
    if (start != null && start.date != null) {
      start.date.setUncertain(uncertain);
    }
    if (end != null && end.date != null) {
      end.date.setUncertain(uncertain);
    }
  }

  @Override
  public boolean isApproximate() {
    return (start != null && start.isApproximate()) || (end != null && end.isApproximate());
  }

  @Override
  public boolean isUncertain() {
    return (start != null && start.isUncertain()) || (end != null && end.isUncertain());
  }

  @Override
  public void switchDayMonth() {
    if (start != null) {
      start.switchDayMonth();
    }
    if (end != null) {
      end.switchDayMonth();
    }
  }

  @Override
  public TemporalEntity copy() {
    try {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(bytes);
      out.writeObject(this);
      out.close();
      ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
      Interval copy = (Interval) in.readObject();
      return copy;
    } catch (ClassNotFoundException | IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public Instant getFirstDay() {
    return start == null ? null : start.getFirstDay();
  }

  @Override
  public Instant getLastDay() {
    return end == null ? null : end.getLastDay();
  }

  @Override
  public void removeTime() {
    if (start != null) {
      start.removeTime();
    }
    if (end != null) {
      end.removeTime();
    }
  }
}
