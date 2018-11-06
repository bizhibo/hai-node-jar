package com.hai.log.collect.common;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @描述 : 时间格式化工具类
 * @创建者：liuss
 * @创建时间： 2014-6-4下午2:47:05
 */
public class DateUtils {
	private static final SimpleDateFormat DATE = new SimpleDateFormat(
			"yyyy-MM-dd");
	private static final SimpleDateFormat DATE_TIME = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat();

	/**
	 * @描述 : 将date转换为string,格式yyyy-MM-dd
	 * @创建者：liuss
	 * @创建时间： 2014-5-5下午5:15:51
	 *
	 * @param date
	 * @return
	 */
	public static String formatDate(Date date) {
		if (date == null) {
			return null;
		} else {
			return DATE.format(date);
		}
	}

	/**
	 * @描述 : 将date转换为string,格式yyyy-MM-dd HH:mm:ss
	 * @创建者：liuss
	 * @创建时间： 2014-5-5下午5:16:06
	 *
	 * @param date
	 * @return
	 */
	public static String formatDateTime(Date date) {
		if (date == null) {
			return null;
		} else {
			return DATE_TIME.format(date);
		}
	}

	/**
	 * @描述 : 将date转换为string,格式pattern自定义的格式
	 * @创建者：liuss
	 * @创建时间： 2014-6-4下午2:55:17
	 *
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String format(Date date, String pattern) {
		if (date == null || pattern == null) {
			return null;
		} else {
			DATE_FORMAT.applyPattern(pattern);
			return DATE_FORMAT.format(date);
		}
	}

	/**
	 * @描述 : 将string转换为date,格式yyyy-MM-dd hh:mm:ss
	 * @创建者：liuss
	 * @创建时间： 2014-5-5下午5:16:17
	 *
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	public static Date parseDateTime(String date) throws RuntimeException {
		try {
			if (date == null) {
				return null;
			} else {
				return DATE_TIME.parse(date);
			}
		} catch (ParseException e) {
			LogUtils.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * @描述 : 将string转换为date,格式yyyy-MM-dd
	 * @创建者：liuss
	 * @创建时间： 2014-5-5下午5:16:27
	 *
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	public static Date parseDate(String date) throws RuntimeException {
		try {
			if (date == null) {
				return null;
			} else {
				return DATE.parse(date);
			}
		} catch (ParseException e) {
			LogUtils.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * @描述 : 将string转换为date,格式pattern自定义格式
	 * @创建者：liuss
	 * @创建时间： 2014-6-4下午3:27:47
	 *
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static Date parse(String date, String pattern)
			throws RuntimeException {
		try {
			if (date == null || pattern == null) {
				return null;
			} else {
				DATE_FORMAT.applyPattern(pattern);
				return DATE_FORMAT.parse(date);
			}
		} catch (ParseException e) {
			LogUtils.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * @描述 : 计算相隔天数 保留一位小数
	 * @创建者：liuss
	 * @创建时间： 2016年11月18日上午11:28:22
	 *
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static String getIntervalDays(Date startDate, Date endDate)
			throws RuntimeException {
		try {
			if (startDate == null || endDate == null) {
				return null;
			}
			BigDecimal startMilli = new BigDecimal(startDate.getTime());
			BigDecimal endMilli = new BigDecimal(endDate.getTime());
			BigDecimal intervalDays = endMilli.subtract(startMilli).divide(
					new BigDecimal(24 * 60 * 60 * 1000), 1,
					BigDecimal.ROUND_HALF_UP);
			return intervalDays.toString();
		} catch (Exception e) {
			LogUtils.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
}
