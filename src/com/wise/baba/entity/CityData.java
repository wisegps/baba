package com.wise.baba.entity;

import java.io.Serializable;

public class CityData implements Serializable {
	private String cityName; // 违章城市
	private String cityCode; // 违章城市代码
	private String province; // 省份
	private int engine; // 是否需要发动机号
	private int engineno; // 需要发动机号多少位
	private int frame; // 是否需要车架号
	private int frameno; // 需要车架号多少位
	private int regist; // 是否需要登记证号
	private int registno; // 需要登记证号多少位
	private boolean isCheck;
	private int oilPrice;// 加油油价

	public int getOilPrice() {
		return oilPrice;
	}

	public void setOilPrice(int oilPrice) {
		this.oilPrice = oilPrice;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getCityCode() {
		return cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	public boolean isCheck() {
		return isCheck;
	}

	public void setCheck(boolean isCheck) {
		this.isCheck = isCheck;
	}

	public int getEngine() {
		return engine;
	}

	public void setEngine(int engine) {
		this.engine = engine;
	}

	public int getEngineno() {
		return engineno;
	}

	public void setEngineno(int engineno) {
		this.engineno = engineno;
	}

	public int getFrame() {
		return frame;
	}

	public void setFrame(int frame) {
		this.frame = frame;
	}

	public int getFrameno() {
		return frameno;
	}

	public void setFrameno(int frameno) {
		this.frameno = frameno;
	}

	public int getRegist() {
		return regist;
	}

	public void setRegist(int regist) {
		this.regist = regist;
	}

	public int getRegistno() {
		return registno;
	}

	public void setRegistno(int registno) {
		this.registno = registno;
	}

	@Override
	public String toString() {
		return "CityData [cityName=" + cityName + ", cityCode=" + cityCode
				+ ", engine=" + engine + ", engineno=" + engineno + ", frame="
				+ frame + ", frameno=" + frameno + ", regist=" + regist
				+ ", registno=" + registno + ", isCheck=" + isCheck + "]";
	}
}