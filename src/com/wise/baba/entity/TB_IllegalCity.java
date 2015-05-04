package com.wise.baba.entity;
/**
 *@author honesty
 **/
public class TB_IllegalCity {
	private int id;
	private String json_data;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getJson_data() {
		return json_data;
	}
	public void setJson_data(String json_data) {
		this.json_data = json_data;
	}
	@Override
	public String toString() {
		return "TB_IllegalCity [id=" + id + ", json_data=" + json_data + "]";
	}	
}