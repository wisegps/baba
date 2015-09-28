package com.wise.baba.entity;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 车辆信息(车牌，车标)
 * 
 * @author keven.cheng
 */
public class CarData implements Serializable{
	
	
	private String car_city;
	
	public String getCar_city() {
		return car_city;
	}

	public void setCar_city(String car_city) {
		this.car_city = car_city;
	}

	private int obj_id;
	/** 品牌 **/
	private String car_brand;
	private String car_brand_id;
	/** 车型 **/
	private String car_series;
	private String car_series_id;
	/** 车款 **/
	private String car_type;
	private String car_type_id;

	private String engine_no = "";// 发送机
	private String frame_no = "";// 车架号
	private String regNo = "";// 登记证
	private String gas_no;
	/** 保险电话 **/
	private String insurance_tel;
	private String maintain_tel;
	/** 昵称 **/
	private String nick_name;
	private String insurance_no;
	/** 限行 **/
	private String limit;
	/**
	 * 保险公司
	 */
	private String insurance_company;
	/**
	 * 保险时间
	 */
	private String insurance_date;
	private String geofence;
	/**
	 * 年检日期
	 */
	private String annual_inspect_date;
	/**
	 * 保养
	 */
	private String maintain_company;
	private String maintain_last_mileage;
	private String maintain_next_mileage;
	private String buy_date;
	/** 车牌 **/
	public String obj_name;
	private String maintain_last_date;
	/** 车logo **/
	private String logoPath;
	/** 车对应的device id **/
	private String device_id;
	/** 序列号 **/
	private String serial;
	private int Type; // 布局控制
	private String Adress; // 车辆位置
	private String gps_time; // 定位时间
	private String rcv_time;// 最后上传数据时间
	/** 位置 **/
	private double Lat;
	private double Lon;
	/** 违章城市 **/
	private ArrayList<String> vio_citys;
	private ArrayList<String> vio_citys_code;
	private ArrayList<String> province;
	/** 油价 **/
	private double fuel_price;

	private boolean isStop;// 是否启动状态

	private boolean ifAir;//是否有空气净化器

	public boolean isStop() {
		return isStop;
	}

	public void setStop(boolean isStop) {
		this.isStop = isStop;
	}

	public double getFuel_price() {
		return fuel_price;
	}

	public void setFuel_price(double fuel_price) {
		this.fuel_price = fuel_price;
	}
	public String getRcv_time() {
		return rcv_time;
	}

	public void setRcv_time(String rcv_time) {
		this.rcv_time = rcv_time;
	}

	/** 震动灵敏度 **/
	private int sensitivity;

	public int getSensitivity() {
		return sensitivity;
	}

	public void setSensitivity(int sensitivity) {
		this.sensitivity = sensitivity;
	}

	public String getCar_brand_id() {
		return car_brand_id;
	}

	public void setCar_brand_id(String car_brand_id) {
		this.car_brand_id = car_brand_id;
	}

	public String getCar_series_id() {
		return car_series_id;
	}

	public void setCar_series_id(String car_series_id) {
		this.car_series_id = car_series_id;
	}

	public String getCar_type_id() {
		return car_type_id;
	}

	public void setCar_type_id(String car_type_id) {
		this.car_type_id = car_type_id;
	}

	public String getGas_no() {
		return gas_no;
	}

	public void setGas_no(String gas_no) {
		this.gas_no = gas_no;
	}

	public String getInsurance_tel() {
		return insurance_tel;
	}

	public void setInsurance_tel(String insurance_tel) {
		this.insurance_tel = insurance_tel;
	}

	public String getMaintain_tel() {
		return maintain_tel;
	}

	public void setMaintain_tel(String maintain_tel) {
		this.maintain_tel = maintain_tel;
	}

	public int getType() {
		return Type;
	}

	public void setType(int type) {
		Type = type;
	}

	public String getLogoPath() {
		return logoPath;
	}

	public void setLogoPath(String logoPath) {
		this.logoPath = logoPath;
	}

	public int getObj_id() {
		return obj_id;
	}

	public void setObj_id(int obj_id) {
		this.obj_id = obj_id;
	}

	public String getObj_name() {
		return obj_name;
	}

	public void setObj_name(String obj_name) {
		this.obj_name = obj_name;
	}

	public String getCar_brand() {
		return car_brand;
	}

	public void setCar_brand(String car_brand) {
		this.car_brand = car_brand;
	}

	public String getCar_series() {
		return car_series;
	}

	public void setCar_series(String car_series) {
		this.car_series = car_series;
	}

	public String getCar_type() {
		return car_type;
	}

	public void setCar_type(String car_type) {
		this.car_type = car_type;
	}

	public String getEngine_no() {
		return engine_no;
	}

	public void setEngine_no(String engine_no) {
		this.engine_no = engine_no;
	}

	public String getFrame_no() {
		return frame_no;
	}

	public void setFrame_no(String frame_no) {
		this.frame_no = frame_no;
	}

	public String getInsurance_company() {
		return insurance_company;
	}

	public void setInsurance_company(String insurance_company) {
		this.insurance_company = insurance_company;
	}

	public String getInsurance_date() {
		return insurance_date;
	}

	public void setInsurance_date(String insurance_date) {
		this.insurance_date = insurance_date;
	}

	public String getAnnual_inspect_date() {
		return annual_inspect_date;
	}

	public void setAnnual_inspect_date(String annual_inspect_date) {
		this.annual_inspect_date = annual_inspect_date;
	}

	public String getMaintain_company() {
		return maintain_company;
	}

	public void setMaintain_company(String maintain_company) {
		this.maintain_company = maintain_company;
	}

	public String getMaintain_last_mileage() {
		return maintain_last_mileage;
	}

	public void setMaintain_last_mileage(String maintain_last_mileage) {
		this.maintain_last_mileage = maintain_last_mileage;
	}

	public String getMaintain_next_mileage() {
		return maintain_next_mileage;
	}

	public void setMaintain_next_mileage(String maintain_next_mileage) {
		this.maintain_next_mileage = maintain_next_mileage;
	}

	public String getBuy_date() {
		return buy_date;
	}

	public void setBuy_date(String buy_date) {
		this.buy_date = buy_date;
	}

	public String getMaintain_last_date() {
		return maintain_last_date;
	}

	public void setMaintain_last_date(String maintain_last_date) {
		this.maintain_last_date = maintain_last_date;
	}

	public String getAdress() {
		return Adress;
	}

	public void setAdress(String adress) {
		Adress = adress;
	}

	public double getLat() {
		return Lat;
	}

	public void setLat(double lat) {
		Lat = lat;
	}

	public double getLon() {
		return Lon;
	}

	public void setLon(double lon) {
		Lon = lon;
	}

	public String getRegNo() {
		return regNo;
	}

	public void setRegNo(String regNo) {
		this.regNo = regNo;
	}

	public String getDevice_id() {
		return device_id;
	}

	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public String getGps_time() {
		return gps_time;
	}

	public void setGps_time(String gps_time) {
		this.gps_time = gps_time;
	}

	public ArrayList<String> getVio_citys() {
		return vio_citys;
	}

	public void setVio_citys(ArrayList<String> vio_citys) {
		this.vio_citys = vio_citys;
	}

	public ArrayList<String> getVio_citys_code() {
		return vio_citys_code;
	}

	public void setVio_citys_code(ArrayList<String> vio_citys_code) {
		this.vio_citys_code = vio_citys_code;
	}

	public String getNick_name() {
		return nick_name;
	}

	public void setNick_name(String nick_name) {
		this.nick_name = nick_name;
	}

	public String getInsurance_no() {
		return insurance_no;
	}

	public void setInsurance_no(String insurance_no) {
		this.insurance_no = insurance_no;
	}

	public String getLimit() {
		return limit;
	}

	public void setLimit(String limit) {
		this.limit = limit;
	}

	public ArrayList<String> getProvince() {
		return province;
	}

	public void setProvince(ArrayList<String> province) {
		this.province = province;
	}

	public String getGeofence() {
		return geofence;
	}

	public void setGeofence(String geofence) {
		this.geofence = geofence;
	}

	public boolean isIfAir() {
		return ifAir;
	}

	public void setIfAir(boolean ifAir) {
		this.ifAir = ifAir;
	}

	@Override
	public String toString() {
		return "CarData [obj_id=" + obj_id + ", car_brand=" + car_brand + ", car_brand_id=" + car_brand_id + ", car_series=" + car_series + ", car_series_id="
				+ car_series_id + ", car_type=" + car_type + ", car_type_id=" + car_type_id + ", engine_no=" + engine_no + ", frame_no=" + frame_no
				+ ", regNo=" + regNo + ", gas_no=" + gas_no + ", insurance_tel=" + insurance_tel + ", maintain_tel=" + maintain_tel + ", nick_name="
				+ nick_name + ", insurance_no=" + insurance_no + ", limit=" + limit + ", insurance_company=" + insurance_company + ", insurance_date="
				+ insurance_date + ", geofence=" + geofence + ", annual_inspect_date=" + annual_inspect_date + ", maintain_company=" + maintain_company
				+ ", maintain_last_mileage=" + maintain_last_mileage + ", maintain_next_mileage=" + maintain_next_mileage + ", buy_date=" + buy_date
				+ ", obj_name=" + obj_name + ", maintain_last_date=" + maintain_last_date + ", logoPath=" + logoPath + ", device_id=" + device_id + ", serial="
				+ serial + ", Type=" + Type + ", Adress=" + Adress + ", gps_time=" + gps_time + ", Lat=" + Lat + ", Lon=" + Lon + ", vio_citys=" + vio_citys
				+ ", vio_citys_code=" + vio_citys_code + ", province=" + province + "]";
	}
}