package com.wise.baba.entity;

import org.litepal.crud.DataSupport;

/**
 *@author honesty
 **/
public class CollectionData extends DataSupport{
	private int id;
	private String Cust_id;
	private String favorite_id;
	private String name;
	private String address;
	private String tel;
	private String lon;
	private String lat;
	private String Content;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCust_id() {
		return Cust_id;
	}
	public void setCust_id(String cust_id) {
		Cust_id = cust_id;
	}
	public String getFavorite_id() {
		return favorite_id;
	}
	public void setFavorite_id(String favorite_id) {
		this.favorite_id = favorite_id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getTel() {
		return tel;
	}
	public void setTel(String tel) {
		this.tel = tel;
	}
	public String getLon() {
		return lon;
	}
	public void setLon(String lon) {
		this.lon = lon;
	}
	public String getLat() {
		return lat;
	}
	public void setLat(String lat) {
		this.lat = lat;
	}
	public String getContent() {
		return Content;
	}
	public void setContent(String content) {
		Content = content;
	}
	@Override
	public String toString() {
		return "TB_Collection [id=" + id + ", Cust_id=" + Cust_id
				+ ", favorite_id=" + favorite_id + ", name=" + name
				+ ", address=" + address + ", tel=" + tel + ", lon=" + lon
				+ ", lat=" + lat + ", Content=" + Content + "]";
	}	
}