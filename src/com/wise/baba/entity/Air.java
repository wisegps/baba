package com.wise.baba.entity;

import java.io.Serializable;

public class Air implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 空气质量指数
	 */
	public int air;
	
	/**
	 * 开关
	 */
	public int airSwitch;
	/**
	 * 模式
	 */
	public int airMode;
	/**
	 * 定时时间
	 */
	public String airTime;
	/**
	 * 定时时长
	 */
	public int airDuration;

	public int getAir() {
		return air;
	}

	public void setAir(int air) {
		this.air = air;
	}

	public int getAirSwitch() {
		return airSwitch;
	}

	public void setAirSwitch(int airSwitch) {
		this.airSwitch = airSwitch;
	}

	public int getAirMode() {
		return airMode;
	}

	public void setAirMode(int airMode) {
		this.airMode = airMode;
	}

	public String getAirTime() {
		return airTime;
	}

	public void setAirTime(String airTime) {
		this.airTime = airTime;
	}

	public int getAirDuration() {
		return airDuration;
	}

	public void setAirDuration(int airDuration) {
		this.airDuration = airDuration;
	}

}
