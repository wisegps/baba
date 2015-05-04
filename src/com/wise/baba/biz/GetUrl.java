package com.wise.baba.biz;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.wise.baba.app.Constant;

/**
 * @author honesty
 **/
public class GetUrl {
	/**
	 * 通过终端id获取最新gps信息
	 * 
	 * @param device_id
	 * @param auth_code
	 * @return
	 */
	public static String getCarGpsData(String device_id, String auth_code) {
		String gpsUrl = Constant.BaseUrl + "device/" + device_id + "?auth_code=" + auth_code + "&update_time=2014-01-01%2019:06:43";
		return gpsUrl;
	}
	/**
	 * 通过终端id获取最新gps信息
	 * @param device_id
	 * @param auth_code
	 * @return
	 */
	public static String getActivieGpsData(String device_id, String auth_code) {
		String gpsUrl = Constant.BaseUrl + "device/" + device_id + "/active_gps_data?auth_code=" + auth_code + "&update_time=2014-01-01%2019:06:43";
		return gpsUrl;
	}

	/**
	 * 通过终端id获取健康信息
	 * 
	 * @param device_id
	 * @param auth_code
	 * @param car_brand
	 * @return
	 * @throws Exception
	 */
	public static String getHealthData(String device_id, String auth_code, String car_brand) throws Exception {
		String url = Constant.BaseUrl + "device/" + device_id + "/health_exam?auth_code=" + auth_code + "&brand=" + URLEncoder.encode(car_brand, "UTF-8");
		return url;
	}
}
