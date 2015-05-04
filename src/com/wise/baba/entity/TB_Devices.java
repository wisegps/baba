package com.wise.baba.entity;
/**
 *@author honesty
 **/
public class TB_Devices {
	private int id;
	private String Cust_id;
	private int DeviceID;
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
	public int getDeviceID() {
		return DeviceID;
	}
	public void setDeviceID(int deviceID) {
		DeviceID = deviceID;
	}
	public String getContent() {
		return Content;
	}
	public void setContent(String content) {
		Content = content;
	}
	@Override
	public String toString() {
		return "TB_Devices [id=" + id + ", Cust_id=" + Cust_id + ", DeviceID="
				+ DeviceID + ", Content=" + Content + "]";
	}	
}