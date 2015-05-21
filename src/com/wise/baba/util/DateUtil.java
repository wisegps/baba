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
 * @desc   baba
 * @date   2015-5-21
 *
 */
public class DateUtil {

	/**
	 * 
	 * @param date   参数  “2015-5-21”  转化为  ："2015年5月21日 星期三"; 
	 */
	public String toChineseDate(String paramDate){
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
	 * @param chineseDate  参数  "2015年5月21日 星期三"  转化为  ：  “2015-5-21”; 
	 */
	public String toParamDate(String chineseDate){
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

}
