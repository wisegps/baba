package com.wise.show;

import java.io.Serializable;

public class ImageData implements Serializable{

	private boolean cust_praise;
	private String car_series;
	private String create_time;
	private int photo_id;
	private int praise_count;
	private String small_pic_url;
	private boolean sex;
	private String car_brand_id;	
	
	public boolean isSex() {
		return sex;
	}
	public void setSex(boolean sex) {
		this.sex = sex;
	}	
	public String getCar_brand_id() {
		return car_brand_id;
	}

	public void setCar_brand_id(String car_brand_id) {
		this.car_brand_id = car_brand_id;
	}

	public boolean isCust_praise() {
		return cust_praise;
	}

	public void setCust_praise(boolean cust_praise) {
		this.cust_praise = cust_praise;
	}

	public String getCar_series() {
		return car_series;
	}

	public void setCar_series(String car_series) {
		this.car_series = car_series;
	}

	public String getCreate_time() {
		return create_time;
	}

	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}

	public int getPhoto_id() {
		return photo_id;
	}

	public void setPhoto_id(int photo_id) {
		this.photo_id = photo_id;
	}

	public int getPraise_count() {
		return praise_count;
	}

	public void setPraise_count(int praise_count) {
		this.praise_count = praise_count;
	}

	public String getSmall_pic_url() {
		return small_pic_url;
	}

	public void setSmall_pic_url(String small_pic_url) {
		this.small_pic_url = small_pic_url;
	}
}
