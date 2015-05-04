package com.wise.baba.entity;

import org.litepal.crud.DataSupport;

/**
 *@author honesty
 **/
public class BaseData extends DataSupport{
	private int id;
	private String Cust_id; //用户id
	private String Title;   //标题
	private String Content; //内容
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
	public String getTitle() {
		return Title;
	}
	public void setTitle(String title) {
		Title = title;
	}
	public String getContent() {
		return Content;
	}
	public void setContent(String content) {
		Content = content;
	}
	@Override
	public String toString() {
		return "TB_Base [id=" + id + ", Cust_id=" + Cust_id + ", Title="
				+ Title + ", Content=" + Content + "]";
	}	
}