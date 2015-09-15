package com.wise.baba.entity;

import java.io.Serializable;

/**
 * 空气质量指数实体类
 * @author c
 *
 */
public class AQIEntity implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String id; //id
	
	private String time;//接收时间
	
	private int air;//空气质量指数

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

	public int getAir() {
		return air;
	}

	public void setAir(int air) {
		this.air = air;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
	
	
}
