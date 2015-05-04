package com.wise.baba.entity;

import java.io.Serializable;

public class FriendSearch implements Serializable {
	/**
	 * 根据名字搜索好友，好友实体
	 */
	private static final long serialVersionUID = 1L;
	private int cust_id;
	private String cust_name;
	private int cust_type;
	private String logo;
	private String province;
	private String city;
	private String sex;
	public int getCust_id() {
		return cust_id;
	}
	public void setCust_id(int cust_id) {
		this.cust_id = cust_id;
	}
	public String getCust_name() {
		return cust_name;
	}
	public void setCust_name(String cust_name) {
		this.cust_name = cust_name;
	}
	public int getCust_type() {
		return cust_type;
	}
	public void setCust_type(int cust_type) {
		this.cust_type = cust_type;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	
	
}
