package com.wise.baba.entity;

/**
 * 空气质量指数实体类
 * @author c
 *
 */
public class AQIEntity {
	private String id; //id
	
	private String time;//接收时间
	
	private String air;//空气质量指数

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getAir() {
		return air;
	}

	public void setAir(String air) {
		this.air = air;
	}
	
	
	
	
}
