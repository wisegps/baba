package com.wise.baba.entity;

import java.io.Serializable;
import java.util.List;

import com.wise.baba.biz.GetSystem;


/**
 *@author honesty
 **/
public class GpsData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double lat;
	private double lon;
	private String rcv_time;
	private List<Integer> uni_status;
	
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLon() {
		return lon;
	}
	public void setLon(double lon) {
		this.lon = lon;
	}	
	public String getRcv_time() {
		return rcv_time;
	}
	public void setRcv_time(String rcv_time) {
		this.rcv_time = rcv_time;
	}
	public List<Integer> getUni_status() {
		return uni_status;
	}
	public void setUni_status(List<Integer> uni_status) {
		this.uni_status = uni_status;
	}
	@Override
	public String toString() {
		return "GpsData [lat=" + lat + ", lon=" + lon + ", rcv_time=" + rcv_time + ", uni_status=" + uni_status + "]";
	}	
}