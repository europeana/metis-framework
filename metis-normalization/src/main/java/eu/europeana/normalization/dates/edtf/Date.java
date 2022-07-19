package eu.europeana.normalization.dates.edtf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * A data class for representing the YYYY-MM-DD part of an EDTF date, supporting partial dates, including only centuries or
 * decades (e.g., 19XX)
 */
public class Date implements Serializable {

	public enum YearPrecision {
		MILLENIUM, CENTURY, DECADE
	}

	;

	public static final Date UNKNOWN = new Date() {
		{
			setUnkown(true);
		}
	};

	public static final Date UNSPECIFIED = new Date() {
		{
			setUnspecified(true);
		}
	};

	boolean uncertain;
	boolean approximate;
	boolean unkown;
	boolean unspecified;

	Integer year;
	Integer month;
	Integer day;

	YearPrecision yearPrecision;

	public boolean isUncertain() {
		return uncertain;
	}

	public void setUncertain(boolean uncertain) {
		this.uncertain = uncertain;
	}

	public boolean isApproximate() {
		return approximate;
	}

	public void setApproximate(boolean approximate) {
		this.approximate = approximate;
	}

	public boolean isUnkown() {
		return unkown;
	}

	public void setUnkown(boolean unkown) {
		this.unkown = unkown;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month == null || month == 0 ? null : month;
	}

	public Integer getDay() {
		return day;
	}

	public void setDay(Integer day) {
		this.day = day == null || day == 0 ? null : day;
	}

	public boolean isUnspecified() {
		return unspecified;
	}

	public void setUnspecified(boolean unspecified) {
		this.unspecified = unspecified;
	}

	public YearPrecision getYearPrecision() {
		return yearPrecision;
	}

	public void setYearPrecision(YearPrecision yearPrecision) {
		this.yearPrecision = yearPrecision;
	}

	public void switchDayMonth() {
		if (day != null) {
			int dayTmp = day;
			setDay(month);
			setMonth(dayTmp);
		}
	}

	public Date copy() {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bytes);
			out.writeObject(this);
			out.close();
			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
			Date copy = (Date) in.readObject();
			return copy;
		} catch (ClassNotFoundException | IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

}
