package com.game.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateFormatUtil {


	private static final ThreadLocal<DateFormatList> dateFormats = ThreadLocal.withInitial(DateFormatList::new);

	public static String formatYMD(Date date) {
		return dateFormats.get().ymd.format(date);
	}

	public static String formatYMDHM(Date date) {
		return dateFormats.get().ymdhm.format(date);
	}

	public static String formatYMDHMChinese(Date date) {
		return dateFormats.get().ymdhmChinese.format(date);
	}

	public static String formatYMDHMS(Date date) {
		return dateFormats.get().ymdhms.format(date);
	}

	public static String formatYMDChinese(Date date) {
		return dateFormats.get().ymdChinese.format(date);
	}

	public static String formatYMDSlash(Date date) {
		return dateFormats.get().ymdSlash.format(date);
	}

	public static Date parseYMD(String dateStr) {
		return parse(dateFormats.get().ymd, dateStr);
	}

	public static Date parseYMDHMS(String dateStr) {
		return parse(dateFormats.get().ymdhms, dateStr);
	}

	private static Date parse(SimpleDateFormat format, String dateString) {
		try {
			format.setLenient(false);
			Date d = format.parse(dateString);
			// mysql date range : '1000-01-01' to '9999-12-31'
			Calendar c = Calendar.getInstance();
			c.setTime(d);
			int year = c.get(Calendar.YEAR);
			if (year >= 1000 && year <=9999){
				return d;
			}
			else {
				throw new IllegalArgumentException(dateString + ", '1000-01-01' to '9999-12-31'");
			}
		}
		catch (ParseException ex) {
			throw new IllegalArgumentException(dateString, ex);
		}
	}
}

class DateFormatList {
	public final SimpleDateFormat ymdhm = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	public final SimpleDateFormat ymdhmChinese = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
	public final SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
	public final SimpleDateFormat ymdhms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public final SimpleDateFormat ymdChinese = new SimpleDateFormat("yyyy年MM月dd");
	public final SimpleDateFormat ymdSlash = new SimpleDateFormat("yyyy/MM/dd");
}
