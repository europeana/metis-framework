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
public class IntervalEdtfDate extends AbstractEdtfDate implements Serializable {

  private static final long serialVersionUID = -8754610674192759880L;
  private InstantEdtfDate start;
  private InstantEdtfDate end;

  public IntervalEdtfDate(InstantEdtfDate start, InstantEdtfDate end) {
    super();
    this.start = start;
    this.end = end;
  }

  @Override
  public boolean isTimeOnly() {
    return (start == null || start.isTimeOnly()) && (end == null || end.isTimeOnly());
  }

  public InstantEdtfDate getStart() {
    return start;
  }

  public void setStart(InstantEdtfDate start) {
    this.start = start;
  }

  public InstantEdtfDate getEnd() {
    return end;
  }

  public void setEnd(InstantEdtfDate end) {
    this.end = end;
  }

  @Override
  public void setApproximate(boolean approx) {
    if (start != null && start.getEdtfDatePart() != null) {
      start.getEdtfDatePart().setApproximate(approx);
    }
    if (end != null && end.getEdtfDatePart() != null) {
      end.getEdtfDatePart().setApproximate(approx);
    }
  }

  @Override
  public void setUncertain(boolean uncertain) {
    if (start != null && start.getEdtfDatePart() != null) {
      start.getEdtfDatePart().setUncertain(uncertain);
    }
    if (end != null && end.getEdtfDatePart() != null) {
      end.getEdtfDatePart().setUncertain(uncertain);
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
  public void switchDayAndMonth() {
    if (start != null) {
      start.switchDayAndMonth();
    }
    if (end != null) {
      end.switchDayAndMonth();
    }
  }

  @Override
  public AbstractEdtfDate copy() {
    try {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(bytes);
      out.writeObject(this);
      out.close();
      ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
      return (IntervalEdtfDate) in.readObject();
    } catch (ClassNotFoundException | IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public InstantEdtfDate getFirstDay() {
    return start == null ? null : start.getFirstDay();
  }

  @Override
  public InstantEdtfDate getLastDay() {
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
