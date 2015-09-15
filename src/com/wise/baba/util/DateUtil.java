/**
 * 
 */
package com.wise.baba.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @author c
 * @desc baba
 * @date 2015-5-21
 * 
 */
public class DateUtil {

	/**
	 * 
	 * @param date
	 *            参数 “2015-5-21” 转化为 ："2015年5月21日 星期三";
	 */
	public String toChineseDate(String paramDate) {
		SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date date = mFormat.parse(paramDate);
			SimpleDateFormat chineseFormat = new SimpleDateFormat("yyyy年M月d日 E");

			String chineseDate = chineseFormat.format(date);
			chineseDate = chineseDate.replace("周", "星期");
			return chineseDate;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return paramDate;

	}

	/**
	 * 
	 * @param chineseDate
	 *            参数 "2015年5月21日 星期三" 转化为 ： “2015-5-21”;
	 */
	public String toParamDate(String chineseDate) {
		SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat chineseFormat = new SimpleDateFormat("yyyy年M月d日 E");
		try {
			Date date = chineseFormat.parse(chineseDate);
			String paramDate = mFormat.format(date);
			return paramDate;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return chineseDate;

	}

	/**
	 * 获取当前时间
	 * @param  hour 几小时前
	 */
	public static String getCurrentTime(int hour) {
		long cur = System.currentTimeMillis() ;
		long dur = hour * 60 * 60 *1000;
		SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date(cur - dur);
		String paramDate = mFormat.format(date);
		System.out.println(paramDate);
		return paramDate;
	}
	
	
	/**
	 * 转换时间格式
	 * @param time 
	 */
	public static String getTime(String time) {
		time = time.substring(0, time.length() - 5)
				.replace("T", " ");
		SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			date = mFormat.parse(time);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Date newDate = new Date(date.getTime()+8*60*60*1000);
		return mFormat.format(newDate);
	}
	
	

}
