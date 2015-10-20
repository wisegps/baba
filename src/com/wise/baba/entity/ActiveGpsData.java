package com.wise.baba.entity;

import java.io.Serializable;

/**
 * gps所有数据
 *@author honesty
 **/
public class ActiveGpsData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String serial;
	private GpsData active_gps_data;
	private Params params;
	public String getSerial() {
		return serial;
	}
	public void setSerial(String serial) {
		this.serial = serial;
	}
	public GpsData getActive_gps_data() {
		return active_gps_data;
	}
	public void setActive_gps_data(GpsData active_gps_data) {
		this.active_gps_data = active_gps_data;
	}	
	public Params getParams() {
		return params;
	}
	public void setParams(Params params) {
		this.params = params;
	}
	public static class Params{
		private int sensitivity;

		public int getSensitivity() {
			return sensitivity;
		}
		public void setSensitivity(int sensitivity) {
			this.sensitivity = sensitivity;
		}
		@Override
		public String toString() {
			return "Params [sensitivity=" + sensitivity + "]";
		}		
	}
}