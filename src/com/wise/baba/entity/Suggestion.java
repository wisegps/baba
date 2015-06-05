/**
 * 
 */
package com.wise.baba.entity;

import java.io.Serializable;

/**
 * 
 * @author c
 * @desc 百度地图搜索建议
 * @date 2015-6-4
 * 
 */
public class Suggestion implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int type; // 类型，历史记录，搜索建议
	private String key;// 关键字
	private String city;// 城市名
	private String district;// 地区名

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}


	public final static int Type_History = 1;
	public final static int Type_Suggestion = 2;
	
}
