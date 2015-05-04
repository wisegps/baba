package com.wise.baba.entity;

import java.util.List;

public class ProvinceData {
	private String provinceName = null;
	private String provinceLetter = null;
	private List<CityData> IllegalCityList = null;
	public String getProvinceName() {
		return provinceName;
	}
	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}
	public String getProvinceLetter() {
		return provinceLetter;
	}
	public void setProvinceLetter(String provinceLetter) {
		this.provinceLetter = provinceLetter;
	}
	public List<CityData> getIllegalCityList() {
		return IllegalCityList;
	}
	public void setIllegalCityList(List<CityData> illegalCityList) {
		IllegalCityList = illegalCityList;
	}
	@Override
	public String toString() {
		return "ProvinceModel [provinceName=" + provinceName
				+ ", provinceLetter=" + provinceLetter + "]";
	}
}
