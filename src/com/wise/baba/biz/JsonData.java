package com.wise.baba.biz;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.wise.baba.entity.CarData;


public class JsonData {
	public static List<CarData> jsonCarInfo(String str) {
		List<CarData> carDatas = new ArrayList<CarData>();
		try {
			JSONArray jsonArray = new JSONArray(str);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				CarData carData = new CarData();
				carData.setObj_id(jsonObject.getInt("obj_id"));
				
				if(jsonObject.has("nick_name")){//2016-11-11 修复 返回数据没有
					if(!jsonObject.isNull("nick_name"))
					carData.setNick_name(jsonObject.getString("nick_name"));
				}else{
					carData.setNick_name("");
				}
				
				if(jsonObject.has("device_id")){
					if(!jsonObject.isNull("device_id")){
						carData.setDevice_id(jsonObject.getString("device_id"));
						Log.e("Welcome","=== " + carData.getDevice_id());
					}else{
						carData.setDevice_id("");
					}
				}else{
					carData.setDevice_id("");
					Log.e("Welcome","=== " + carData.getDevice_id());
				}
			
				
				if(jsonObject.has("obj_name")){
					if(!jsonObject.isNull("obj_name"))
					carData.setObj_name(jsonObject.getString("obj_name"));
				}else{
					carData.setCar_brand("");
				}
				if(jsonObject.has("car_brand")){
					if(!jsonObject.isNull("car_brand"))
					carData.setCar_brand(jsonObject.getString("car_brand"));
				}else{
					carData.setCar_brand("");
				}
				if(jsonObject.has("car_brand_id")){
					if(!jsonObject.isNull("car_brand_id"))
					carData.setCar_brand_id(jsonObject.getString("car_brand_id"));
				}else{
					carData.setCar_brand_id("0");
				}
				
				if(jsonObject.has("car_series")){
					if(!jsonObject.isNull("car_series"))
					carData.setCar_series(jsonObject.getString("car_series"));
				}else{
					carData.setCar_series("");
				}
				
				if(jsonObject.has("car_series_id")){
					if(!jsonObject.isNull("car_series_id"))
					carData.setCar_series_id(jsonObject.getString("car_series_id"));
				}else{
					carData.setCar_series_id("0");
				}
				
				if(jsonObject.has("car_type")){
					if(!jsonObject.isNull("car_type"))
					carData.setCar_type(jsonObject.getString("car_type"));
				}else{
					carData.setCar_type("");
				}
				
				if(jsonObject.has("car_type_id")){
					if(!jsonObject.isNull("car_type_id"))
					carData.setCar_type_id(jsonObject.getString("car_type_id"));
				}else{
					carData.setCar_type_id("0");
				}
				
				
				
				if (jsonObject.opt("annual_inspect_date") != null) {
					carData.setAnnual_inspect_date(jsonObject.getString(
							"annual_inspect_date").substring(0, 10));
				}
				if (jsonObject.opt("buy_date") != null) {
					carData.setBuy_date(jsonObject.getString("buy_date")
							.substring(0, 10));
				}
				if (jsonObject.opt("engine_no") != null) {
					carData.setEngine_no(jsonObject.getString("engine_no"));
				}
				if (jsonObject.opt("frame_no") != null) {
					carData.setFrame_no(jsonObject.getString("frame_no"));
				}
				if (jsonObject.opt("reg_no") != null) {
					carData.setRegNo(jsonObject.getString("reg_no"));
				}
				if (jsonObject.opt("gas_no") != null) {
					carData.setGas_no(jsonObject.getString("gas_no"));
				}
				if (jsonObject.opt("insurance_company") != null) {
					carData.setInsurance_company(jsonObject
							.getString("insurance_company"));
				}
				if (jsonObject.opt("insurance_date") != null) {
					carData.setInsurance_date(jsonObject.getString(
							"insurance_date").substring(0, 10));
				}
				if (jsonObject.opt("insurance_tel") != null) {
					carData.setInsurance_tel(jsonObject
							.getString("insurance_tel"));
				}
				if (jsonObject.opt("insurance_no") != null) {
					carData.setInsurance_no(jsonObject
							.getString("insurance_no"));
				}
				if (jsonObject.opt("maintain_company") != null) {
					carData.setMaintain_company(jsonObject
							.getString("maintain_company"));
				}
				
				// 2016-4-16 修复bug 登陆黑屏卡住
				
				Log.e("Welcome", "修复bug：" + jsonObject.isNull("maintain_last_date"));
				
				if (!jsonObject.isNull("maintain_last_date")) {
					carData.setMaintain_last_date(jsonObject.getString(
							"maintain_last_date").substring(0, 10));
				}
				
				if (jsonObject.opt("maintain_last_mileage") != null) {
					carData.setMaintain_last_mileage(jsonObject
							.getString("maintain_last_mileage"));
				}
				if (jsonObject.opt("maintain_tel") != null) {
					carData.setMaintain_tel(jsonObject
							.getString("maintain_tel"));
				}
				if (jsonObject.opt("geofence") != null) {
					carData.setGeofence(jsonObject.getString("geofence"));
				} else {

				}
				if (jsonObject.opt("fuel_price") != null) {
					carData.setFuel_price(jsonObject.getDouble("fuel_price"));
				} else {
					carData.setFuel_price(0);
				}
				if (jsonObject.opt("if_air") != null) {
					carData.setIfAir(jsonObject.getBoolean("if_air"));
				} else {
					carData.setIfAir(false);
				}
//				
				JSONArray jsonArray2 = new JSONArray(
						jsonObject.getString("vio_citys"));
				ArrayList<String> vio_citys = new ArrayList<String>();
				ArrayList<String> vio_citys_code = new ArrayList<String>();
				ArrayList<String> provinces = new ArrayList<String>();
				for (int j = 0; j < jsonArray2.length(); j++) {
					JSONObject jsonObject2 = jsonArray2.getJSONObject(j);
					String vio_city_name = jsonObject2
							.getString("vio_city_name");
					String vio_location = jsonObject2.getString("vio_location");
					String province = "";
					if (jsonObject2.opt("province") == null) {

					} else {
						province = jsonObject2.getString("province");
					}
					vio_citys.add(vio_city_name);
					vio_citys_code.add(vio_location);
					provinces.add(province);
				}
				carData.setVio_citys(vio_citys);
				carData.setVio_citys_code(vio_citys_code);
				carData.setProvince(provinces);
				carData.setSensitivity(0);
				carDatas.add(carData);
			}
			return carDatas;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return carDatas;
	}
}