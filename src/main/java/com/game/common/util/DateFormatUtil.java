package com.game.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateFormatUtil {

	private static final DateTimeFormatter ymdhm = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	private static final DateTimeFormatter ymdhmChinese = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");
	private static final DateTimeFormatter ymd = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter ymdhms = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final DateTimeFormatter ymdChinese = DateTimeFormatter.ofPattern("yyyy年MM月dd");
	private static final DateTimeFormatter ymdSlash = DateTimeFormatter.ofPattern("yyyy/MM/dd");


	public static String formatYMD(LocalDateTime localDateTime) {
		return ymd.format(localDateTime);
	}

	public static String formatYMDHM(LocalDateTime localDateTime) {
		return ymdhm.format(localDateTime);
	}

	public static String formatYMDHMChinese(LocalDateTime localDateTime) {
		return ymdhmChinese.format(localDateTime);
	}

	public static String formatYMDHMS(LocalDateTime localDateTime) {
		return ymdhms.format(localDateTime);
	}

	public static String formatYMDChinese(LocalDateTime localDateTime) {
		return ymdChinese.format(localDateTime);
	}

	public static String formatYMDSlash(LocalDateTime localDateTime) {
		return ymdSlash.format(localDateTime);
	}

	public static LocalDateTime parseYMD(String formatString) {
		return LocalDateTime.parse(formatString, ymd);
	}

	public static LocalDateTime parseYMDHMS(String formatString) {
		return LocalDateTime.parse(formatString, ymdhms);
	}
}
