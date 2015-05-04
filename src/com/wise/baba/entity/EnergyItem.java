package com.wise.baba.entity;

/**
 * 能耗 Bean
 * @author keven.cheng
 */
public class EnergyItem {
	public int date;	//时间值
	public float value;	//能量
	public String weekDate; //星期
	
	public EnergyItem() {
		super();
	}
	public EnergyItem(int date, float value,String weekDate) {
		super();
		this.date = date;
		this.value = value;
		this.weekDate = weekDate;
	}
	public int getDate(){
		return date;
	}
	public String getWeekDate(){
		return weekDate;
	}
	@Override
	public String toString() {
		return "EnergyItem [date=" + date + ", value=" + value + "]";
	}    	
}