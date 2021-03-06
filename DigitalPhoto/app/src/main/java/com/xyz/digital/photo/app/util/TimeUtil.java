package com.xyz.digital.photo.app.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * 时间的处理类
 */
public class TimeUtil {
	public final static String FORMAT_TODAY = "今天 HH:mm";
	public final static String FORMAT_YESTERDAY = "昨天 HH:mm";
	public final static String FORMAT_THIS_YEAR = "M 月 d 日";
	public final static String FORMAT_OTHER_YEAR = "yyyy-MM-dd";
	public final static String FORMAT_YEAR_MONTH = "yyyy 年 M 月";
	public final static String FORMAT_FULL = "yyyy-MM-dd HH:mm:ss";
	public final static String FORMAT_M = "MM-dd HH:mm:ss";
	public final static String FORMAT_TO_M = "yyyy-MM-dd HH:mm";
	public final static String FORMAT_H_M_S = "HH:mm:ss";
	public final static String FORMAT_H_M = "HH:mm";
	public final static String FORMAT_M_S = "mm:ss";

	private final static int SECOND_MILLISECONDS = 1000;
	private final static int YEAR_BASE = 1900;
	// "2011-01-05T15:19:21+00:00" to long
	public static long parseStringTolong(String s) {
		long result = (long) 0;
		String s1 = s.replace("T", " ");
		String s2 = s1.replace("+", " ");
		System.out.println(s2);
		SimpleDateFormat sf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			result = sf1.parse(s2).getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 获取当前时间 小时:分;秒 HH:mm:ss
	 * 
	 * @return
	 */
	public static String getCurTimeH_M_S() {
		SimpleDateFormat formatter = new SimpleDateFormat(FORMAT_H_M_S);
		Date currentTime = new Date();
		String dateString = formatter.format(currentTime);
		return dateString;
	}
	/**
	 * 获取当前时间 小时:分;秒 YY-MM-DD HH:mm:ss
	 * 
	 * @return
	 */
	public static String getCurTimeToMin() {
		SimpleDateFormat formatter = new SimpleDateFormat(FORMAT_TO_M);
		Date currentTime = new Date();
		String dateString = formatter.format(currentTime);
		return dateString;
	}
	/**
	 * 获取当前时间 年月日
	 *
	 * @return
	 */
	public static String getCurToday() {
		SimpleDateFormat formatter = new SimpleDateFormat(FORMAT_OTHER_YEAR);
		Date currentTime = new Date();
		String dateString = formatter.format(currentTime);
		return dateString;
	}
	/**
	 * 获取当前时间 小时:分;秒 HH:mm:ss
	 * 
	 * @return
	 */
	public static String getCurTimeH_M() {
		SimpleDateFormat formatter = new SimpleDateFormat(FORMAT_H_M);
		Date currentTime = new Date();
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * 使用给定的formatter格式化时间
	 * 
	 * @param aSeconds
	 * @return
	 */
	public static String formatTime(long aSeconds, String aFormat) {
		SimpleDateFormat sdf = new SimpleDateFormat(aFormat);
		Date date = new Date();
		date.setTime(aSeconds);
		String formatDate = sdf.format(date);
		return formatDate;
	}

	/**
	 * 获取年数字
	 * 
	 * @param aSeconds
	 * @return
	 */
	public static int getYear(long aSeconds) {
		Date date = new Date();
		date.setTime(aSeconds * SECOND_MILLISECONDS);

		int year = date.getYear() + YEAR_BASE;

		return year;
	}

	/**
	 * 获取月数字
	 * 
	 * @param aSeconds
	 * @return
	 */
	public static int getMonth(long aSeconds) {
		Date date = new Date();
		date.setTime(aSeconds * SECOND_MILLISECONDS);

		int month = date.getMonth() + 1;

		return month;
	}

	/**
	 * 获取日数字
	 * 
	 * @param aSeconds
	 * @return
	 */
	public static int getDate(long aSeconds) {
		Date date = new Date();
		date.setTime(aSeconds * SECOND_MILLISECONDS);

		return date.getDate();
	}

	/**
	 * 获取小时数字
	 * 
	 * @param aSeconds
	 * @return
	 */
	public static int getHour(long aSeconds) {
		Date date = new Date();
		date.setTime(aSeconds * SECOND_MILLISECONDS);

		return date.getHours();
	}

	/**
	 * 获取分钟数字
	 * 
	 * @param aSeconds
	 * @return
	 */
	public static int getMinute(long aSeconds) {
		Date date = new Date();
		date.setTime(aSeconds * SECOND_MILLISECONDS);

		return date.getMinutes();
	}

	/**
	 * 获取秒数字
	 * 
	 * @param aSeconds
	 * @return
	 */
	public static int getSecond(long aSeconds) {
		Date date = new Date();
		date.setTime(aSeconds * SECOND_MILLISECONDS);

		return date.getSeconds();
	}

	/**
	 * 格式化时间
	 * 
	 * @param aSeconds
	 * @return
	 */
	public static String getFormattedDateString(long aSeconds) {
		Date date = new Date();
		date.setTime(aSeconds * SECOND_MILLISECONDS);
		String formatter = FORMAT_FULL;
		SimpleDateFormat sdf = new SimpleDateFormat(formatter);
		return sdf.format(date);
	}

	public static String getFormattedDateString(long aSeconds, String formatter) {
		Date date = new Date();
		date.setTime(aSeconds * SECOND_MILLISECONDS);
		SimpleDateFormat sdf = new SimpleDateFormat(formatter);
		return sdf.format(date);
	}

	/**
	 * 计算两个日期之间相差的天数
	 *
	 * @param smdate 较小的时间
	 * @param bdate  较大的时间
	 * @return 相差天数
	 * @throws ParseException
	 */
	public static int daysBetween(Date smdate, Date bdate) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			smdate = sdf.parse(sdf.format(smdate));
			bdate = sdf.parse(sdf.format(bdate));
			Calendar cal = Calendar.getInstance();
			cal.setTime(smdate);
			long time1 = cal.getTimeInMillis();
			cal.setTime(bdate);
			long time2 = cal.getTimeInMillis();
			long between_days = (time2 - time1) / (1000 * 3600 * 24);

			return Integer.parseInt(String.valueOf(between_days));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static int daysBetween(long smdate, long bdate) {
		return daysBetween(new Date(smdate), new Date(bdate));
	}

}
