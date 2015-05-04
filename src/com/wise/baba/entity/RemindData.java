package com.wise.baba.entity;

import java.io.Serializable;

public class RemindData implements Serializable{
	
	String create_time;
	String remind_time;
	String content;
	int repeat_type;
	int remind_way;
	int cur_mileage;
	int mileages;
	int obj_id;
	int remind_type;
	String reminder_id;
	String count_time;
	String url;
	
	public String getCreate_time() {
		return create_time;
	}
	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}
	public String getRemind_time() {
		return remind_time;
	}
	public void setRemind_time(String remind_time) {
		this.remind_time = remind_time;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getRepeat_type() {
		return repeat_type;
	}
	public void setRepeat_type(int repeat_type) {
		this.repeat_type = repeat_type;
	}
	public int getRemind_way() {
		return remind_way;
	}
	public void setRemind_way(int remind_way) {
		this.remind_way = remind_way;
	}
	public int getCur_mileage() {
		return cur_mileage;
	}
	public void setCur_mileage(int cur_mileage) {
		this.cur_mileage = cur_mileage;
	}
	public int getMileages() {
		return mileages;
	}
	public void setMileages(int mileages) {
		this.mileages = mileages;
	}	
	public int getObj_id() {
		return obj_id;
	}
	public void setObj_id(int obj_id) {
		this.obj_id = obj_id;
	}
	public int getRemind_type() {
		return remind_type;
	}
	public void setRemind_type(int remind_type) {
		this.remind_type = remind_type;
	}
	public String getReminder_id() {
		return reminder_id;
	}
	public void setReminder_id(String reminder_id) {
		this.reminder_id = reminder_id;
	}	
	public String getCount_time() {
		return count_time;
	}
	public void setCount_time(String count_time) {
		this.count_time = count_time;
	}	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	@Override
	public String toString() {
		return "RemindData [create_time=" + create_time + ", remind_time="
				+ remind_time + ", content=" + content + ", repeat_type="
				+ repeat_type + ", remind_way=" + remind_way + ", cur_mileage="
				+ cur_mileage + ", mileages=" + mileages + ", obj_id=" + obj_id
				+ ", remind_type=" + remind_type + ", reminder_id="
				+ reminder_id + ", count_time=" + count_time + ", url=" + url
				+ "]";
	}	
}