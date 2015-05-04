package com.wise.baba.entity;

import android.R.string;

/**
 * 车辆信息(车牌，车标)
 * @author keven.cheng
 */
public class TimeData {
	public String Day;
	public String Year;
	public String Month;    
	
    public String getDay() {
		return Day;
	}
	public void setDay(String day) {
		Day = day;
	}
	public String getYear() {
        return Year;
    }
    public void setYear(String year) {
        Year = year;
    }
    public String getMonth() {
        return Month;
    }
    public void setMonth(String month) {
        Month = month;
    }	
}