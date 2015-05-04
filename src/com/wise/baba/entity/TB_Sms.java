package com.wise.baba.entity;
/**
 *@author honesty
 **/
public class TB_Sms {
	private int id;
	private String cust_id;
	private int noti_id;
	private String message;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCust_id() {
		return cust_id;
	}
	public void setCust_id(String cust_id) {
		this.cust_id = cust_id;
	}
	public int getNoti_id() {
		return noti_id;
	}
	public void setNoti_id(int noti_id) {
		this.noti_id = noti_id;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	@Override
	public String toString() {
		return "TB_Sms [id=" + id + ", cust_id=" + cust_id + ", noti_id="
				+ noti_id + ", message=" + message + "]";
	}	
}