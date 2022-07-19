package eu.europeana.normalization.dates.edtf;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * This class implements the deserialization of EDTF strings into the EDTF structure centred on the TemporalEntity class.
 */
public class EdtfParser {

	private static final Pattern DATE_PATTERN = Pattern
			.compile("((?<year1>\\-?\\d{4})-(?<month1>\\d{2})-(?<day1>\\d{2})|"
					+ "(?<year2>\\-?\\d{4})-(?<month2>\\d{2})|" + "(?<year3>\\-?\\d{4})" + ")(?<modifier>[\\?%~]?)");
	private static final Pattern TIME_PATTERN = Pattern
			.compile("((?<hour1>\\d{2}):(?<minute1>\\d{2}):(?<second1>\\d{2})|" + "(?<hour2>\\d{2}):(?<minute2>\\d{2})|"
					+ "(?<hour3>\\d{2})|"
					+ "(?<hour4>\\d{2}):(?<minute4>\\d{2}):(?<second4>\\d{2})(\\.(?<millis>\\d{1,3}))?(Z|[\\+\\-]\\d{2}:?\\d{0,2})?)");

	//TODO: The implementation of milisseconds and timezone is not implemented
	//TODO:millis  (\\.\\n{3})?
	//TODO:timezone			+ "(?<year>\\n{4})-(?<month>\\n{2})|(?<year>\\n{4}))(?<modifier>[\\?%~]?)");

	public TemporalEntity parse(String edtfString) throws ParseException {
		if (StringUtils.isEmpty(edtfString)) {
			throw new ParseException("Not input given", 0);
		}
		if (edtfString.contains("/")) {
			return parseInterval(edtfString);
		}
		return parseInstant(edtfString);
	}

	protected Instant parseInstant(String edtfString) throws ParseException {
		if (edtfString.contains("T")) {
			String datePart = edtfString.substring(0, edtfString.indexOf('T'));
			String timePart = edtfString.substring(edtfString.indexOf('T') + 1);
			if (datePart.isEmpty()) {
				return new Instant(parseTime(timePart));
			}
			return new Instant(parseDate(datePart), parseTime(timePart));
		} else if (edtfString.contains(":")) {
			return new Instant(parseTime(edtfString));
		} else {
			return new Instant(parseDate(edtfString));
		}
	}

	protected Interval parseInterval(String edtfString) throws ParseException {
		String startPart = edtfString.substring(0, edtfString.indexOf('/'));
		String endPart = edtfString.substring(edtfString.indexOf('/') + 1);
		Instant start = parseInstant(startPart);
		Instant end = parseInstant(endPart);
		if ((end.date.isUnkown() || end.date.isUnspecified()) && (start.date.isUnkown() || start.date.isUnspecified())) {
			throw new ParseException(edtfString, 0);
		}
		return new Interval(start, end);
	}

	protected Time parseTime(String edtfString) throws ParseException {
		Matcher m = TIME_PATTERN.matcher(edtfString);
		if (!m.matches()) {
			throw new ParseException("For input " + edtfString, 0);
		}
		Time t = new Time();
		if (!StringUtils.isEmpty(m.group("hour3"))) {
			t.setHour(Integer.parseInt(m.group("hour3")));
		} else if (!StringUtils.isEmpty(m.group("hour2"))) {
			t.setHour(Integer.parseInt(m.group("hour2")));
			t.setMinute(Integer.parseInt(m.group("minute2")));
		} else if (!StringUtils.isEmpty(m.group("hour1"))) {
			t.setHour(Integer.parseInt(m.group("hour1")));
			t.setMinute(Integer.parseInt(m.group("minute1")));
			t.setSecond(Integer.parseInt(m.group("second1")));
		} else {// if(!StringUtils.isEmpty(m.group("hour4"))) {
			t.setHour(Integer.parseInt(m.group("hour4")));
			t.setMinute(Integer.parseInt(m.group("minute4")));
			t.setSecond(Integer.parseInt(m.group("second4")));
		}
		return t;
	}

	protected Date parseDate(String edtfString) throws ParseException {
		if (edtfString.isEmpty()) {
			return Date.UNKNOWN;
		}
		if (edtfString.equals("..")) {
			return Date.UNSPECIFIED;
		}
		Date d = new Date();
		if (edtfString.startsWith("Y")) {
			d.setYear(Integer.parseInt(edtfString.substring(1)));
		} else {
			Matcher m = DATE_PATTERN.matcher(edtfString);
			if (!m.matches()) {
				throw new ParseException("For input '" + edtfString + "'", 0);
			}
			if (!StringUtils.isEmpty(m.group("year3"))) {
				d.setYear(Integer.parseInt(m.group("year3")));
			} else if (!StringUtils.isEmpty(m.group("year2"))) {
				d.setYear(Integer.parseInt(m.group("year2")));
				d.setMonth(Integer.parseInt(m.group("month2")));
			} else {// if(!StringUtils.isEmpty(m.group("year1"))) {
				d.setYear(Integer.parseInt(m.group("year1")));
				d.setMonth(Integer.parseInt(m.group("month1")));
				d.setDay(Integer.parseInt(m.group("day1")));
			}
			if (!StringUtils.isEmpty(m.group("modifier"))) {
				String modifier = m.group("modifier");
				if (modifier.equals("?")) {
					d.setUncertain(true);
				} else if (modifier.equals("~")) {
					d.setApproximate(true);
				} else if (modifier.equals("%")) {
					d.setApproximate(true);
					d.setUncertain(true);
				}
			}
		}
		return d;
	}

}
