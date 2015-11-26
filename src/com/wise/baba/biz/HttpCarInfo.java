/**
 * 
 */
package com.wise.baba.biz;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.android.volley.Request.Method;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.wise.baba.AppApplication;
import com.wise.baba.app.Constant;
import com.wise.baba.app.Msg;
import com.wise.baba.entity.ActiveGpsData;
import com.wise.baba.entity.CarData;
import com.wise.baba.entity.GpsData;

/**
 * 
 * @author c
 * @desc baba
 * @date 2015-4-21
 * 
 */
public class HttpCarInfo {
	private Handler uiHandler;
	private Handler workHandler;
	private HandlerThread handlerThread = null;
	private RequestQueue mQueue;
	private AppApplication app;
	private String deviceId;
	private String TAG = "HttpCarInfo";
	private Context context;

	public HttpCarInfo(Context context, Handler uiHandler) {
		super();
		this.context = context;
		this.uiHandler = uiHandler;
		handlerThread = new HandlerThread("HttpCarInfo");
		handlerThread.start();
		Looper looper = handlerThread.getLooper();
		workHandler = new Handler(looper, handleCallBack);
		app = (AppApplication) ((Activity) context).getApplication();
		mQueue = HttpUtil.getRequestQueue(context);
	}

	/**
	 * 工作子线程回调函数： 主线程把网络请求数据发送到该工作子线程，子线程解析完毕，发送通知到ui主线程跟新界面
	 */
	private Callback handleCallBack = new Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			Bundle data = null;
			String response = msg.obj.toString();
			switch (msg.what) {
			case Msg.Get_Car_Device:
				data = parseDevice(response);
				break;
			case Msg.Get_Car_Month_Data:
				data = parseMothData(response);
				break;
			case Msg.Get_Car_GPS:
				data = parseGps(response);
				break;
			case Msg.Get_Car_Health:
				data = parseHealth(response);
				break;
			case Msg.Get_Car_Drive:
				data = parseDrive(response);
				break;
			case Msg.Get_Car_Limit:
				data = parseCarLimit(response);
				break;
			default:
				break;
			}
			// 通知ui线程更新数据
			Message m = uiHandler.obtainMessage();
			m.what = msg.what;
			m.setData(data);
			uiHandler.sendMessage(m);
			return false;
		}

	};

	private ErrorListener errorListener = new ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError error) {
		}
	};

	/**
	 * 获取车辆数据 requestAllData
	 */
	public void requestAllData() {
		Log.i("HttpCarInfo", "获取车辆数据");
		// 1,车辆不能为空
		if (app.carDatas == null || app.carDatas.size() == 0) {
			return;
		}
		int index = app.currentCarIndex;
		// 2,防止删除车辆后数组越界
		if (index >= app.carDatas.size()) {
			return;
		}

		CarData carData = app.carDatas.get(index);
		String device_id = carData.getDevice_id();
		// 3,无设备id
		if (device_id == null || device_id.equals("")) {
			return;
		}

		String brand = carData.getCar_brand();
		/*
		 * 请求设备信息
		 */
		requestDevice(device_id, brand);

		String gasNo = "";
		if (carData.getGas_no() == null || carData.getGas_no().equals("")) {
			gasNo = "93#(92#)";
		} else {
			gasNo = carData.getGas_no();
		}
		/*
		 * 请求当月数据
		 */
		requestMonthData(device_id, gasNo);

		/*
		 * 获取gps信息
		 */
		requestGps(device_id);

		/*
		 * 获取体检信息
		 */
		requestHealth(device_id, brand);

		/*
		 * 获取驾驶信息
		 */

		requestDrive(device_id, gasNo);

		String objName = carData.getObj_name();
		/*
		 * 获取限行信息
		 */
		requestCarLimit(device_id, objName);
	}

	/**
	 * 获取设备信息 requestDevice
	 */
	public void requestDevice(String device_id, String brand) {
		// Log.i("HttpCarInfo", "获取设备信息");
		// 获取设备信息
		String deviceUrl = "";
		try {
			deviceUrl = Constant.BaseUrl + "device/" + device_id
					+ "?auth_code=" + app.auth_code + "&brand="
					+ URLEncoder.encode(brand, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		Listener<String> listener = new Response.Listener<String>() {
			public void onResponse(String response) {
				// 返回数据，发送到工作子线程去解析
				Message msg = workHandler.obtainMessage();
				msg.what = Msg.Get_Car_Device;
				msg.obj = response;
				workHandler.sendMessage(msg);
			}
		};

		Request request = new StringRequest(deviceUrl, listener, errorListener);
		mQueue.add(request);
	}

	/**
	 * 解析设备信息
	 */
	private Bundle parseDevice(String response) {
		// Log.i("HttpCarInfo", " 解析设备信息");
		try {
			// Log.i("FragmentCarInfo", json);
			JSONObject jsonObject = new JSONObject(response);
			// SIM卡总流量，单位M
			Double total_traffic = jsonObject.getDouble("total_traffic");
			// SIM卡剩余流量，单位M
			Double remain_traffic = jsonObject.getDouble("remain_traffic");

			// 车辆状态 0: 静止 1：运行 2：设防 3：报警
			int device_flag = jsonObject.getInt("device_flag");

			// 信号强度 0 离线(灰色)，1 差(蓝色1格)，2中(蓝色2格)，3优(蓝色3格)
			int signal_level = jsonObject.getInt("signal_level");
			// 是否在线
			boolean is_online = jsonObject.getBoolean("is_online");

			// 是否隐身 1：隐身 0：不隐身
			int stealthMode = jsonObject.getInt("stealth_mode");

			Bundle bundle = new Bundle();
			bundle.putDouble("total_traffic", total_traffic);
			bundle.putDouble("remain_traffic", remain_traffic);
			bundle.putInt("device_flag", device_flag);
			bundle.putInt("signal_level", signal_level);
			bundle.putBoolean("is_online", is_online);
			bundle.putInt("stealthMode", stealthMode);
			return bundle;
		} catch (Exception e) {

		}
		return null;
	}

	/**
	 * 获取当月数据 requestMonthData
	 */
	public void requestMonthData(String device_id, String gasNo) {
		// Log.i("HttpCarInfo", "获取当月数据");
		String Month = GetSystem.GetNowMonth().getMonth();
		String startMonth = Month + "-01";
		String endMonth = GetSystem.getMonthLastDay(Month);

		String url = "";
		try {
			url = Constant.BaseUrl + "device/" + device_id
					+ "/total?auth_code=" + app.auth_code + "&start_day="
					+ startMonth + "&end_day=" + endMonth + "&city="
					+ URLEncoder.encode(app.City, "UTF-8") + "&gas_no=" + gasNo;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		Listener<String> listener = new Response.Listener<String>() {
			public void onResponse(String response) {
				// 返回数据，发送到工作子线程去解析
				Message msg = workHandler.obtainMessage();
				msg.what = Msg.Get_Car_Month_Data;
				msg.obj = response;
				workHandler.sendMessage(msg);
			}
		};
		Request request = new StringRequest(url, listener, errorListener);
		mQueue.add(request);

	}

	/**
	 * 解析当月数据
	 */
	private Bundle parseMothData(String response) {
		try {

			Bundle bundle = new Bundle();

			JSONObject jsonObject = new JSONObject(response);
			if (jsonObject.toString() == null
					|| jsonObject.toString().equals("")) {
				return null;
			}
			String total_fee = String.format("%.1f",
					jsonObject.getDouble("total_fee"));
			bundle.putString("total_fee", total_fee);

			String total_fuel = String.format("%.1f",
					jsonObject.getDouble("total_fuel"));
			bundle.putString("total_fuel", total_fuel);

			// 剩余里程显示
			if ((jsonObject.getString("left_distance")).equals("null")) {
				bundle.putString("left_distance", String.format("%.0f", 0.0));
			} else if (jsonObject.getDouble("left_distance") == 0) {
				String left_distance = String.format("%.1f",
						jsonObject.getDouble("total_distance"));
				bundle.putString("left_distance", left_distance);
			} else {
				String left_distance = String.format("%.1f",
						jsonObject.getDouble("left_distance"));
				bundle.putString("left_distance", left_distance);
			}

			return bundle;
		} catch (Exception e) {

		}
		return null;
	}

	/**
	 * 获取gps requestGps
	 */
	public void requestGps(String device_id) {

		String gpsUrl = GetUrl.getCarGpsData(device_id, app.auth_code);

		Listener<String> listener = new Response.Listener<String>() {
			public void onResponse(String response) {
				// 返回数据，发送到工作子线程去解析
				Message msg = workHandler.obtainMessage();
				msg.what = Msg.Get_Car_GPS;
				msg.obj = response;
				workHandler.sendMessage(msg);
			}
		};
		Request request = new StringRequest(gpsUrl, listener, errorListener);
		mQueue.add(request);

	}

	/**
	 * 解析gps信息
	 */
	private Bundle parseGps(String response) {
		try {

			Bundle bundle = new Bundle();

			Gson gson = new Gson();
			ActiveGpsData activeGpsData = gson.fromJson(response,
					ActiveGpsData.class);
			if (activeGpsData == null) {
				return null;
			}
			GpsData gpsData = activeGpsData.getActive_gps_data();
			if (gpsData != null) {
				bundle.putDouble("lat", gpsData.getLat());
				bundle.putDouble("lon", gpsData.getLon());
				String rcv_time = GetSystem.ChangeTimeZone(gpsData
						.getRcv_time().substring(0, 19).replace("T", " "));
				bundle.putString("rcv_time", rcv_time);
			}
			if (activeGpsData.getParams() != null) {
				bundle.putInt("sensitivity", activeGpsData.getParams()
						.getSensitivity());
			}

			return bundle;
		} catch (Exception e) {

		}
		return null;
	}

	/**
	 * 获取体检信息 requestHealth
	 */
	public void requestHealth(String device_id, String brand) {

		// 从服务器获取体检信息
		String url = "";
		try {
			url = Constant.BaseUrl + "device/" + device_id
					+ "/health_exam?auth_code=" + app.auth_code + "&brand="
					+ URLEncoder.encode(brand, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		Listener<String> listener = new Response.Listener<String>() {
			public void onResponse(String response) {
				// 返回数据，发送到工作子线程去解析
				Message msg = workHandler.obtainMessage();
				msg.what = Msg.Get_Car_Health;
				msg.obj = response;
				workHandler.sendMessage(msg);
			}
		};
		Request request = new StringRequest(url, listener, errorListener);
		mQueue.add(request);

	}

	/**
	 * 解析体检信息
	 */
	private Bundle parseHealth(String response) {
		try {

			Bundle bundle = new Bundle();

			JSONObject jsonObject = new JSONObject(response);

			// 健康指数
			int health_score = jsonObject.getInt("health_score");
			bundle.putInt("health_score", health_score);

			saveResHealth(response);

			return bundle;
		} catch (Exception e) {

		}
		return null;
	}

	/**
	 * 
	 * saveResHealth 保存体检结果
	 * 
	 * @param response
	 */
	private void saveResHealth(String response) {
		// 体检结果存起来
		SharedPreferences preferences = context.getSharedPreferences(
				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(
				Constant.sp_health_score
						+ app.carDatas.get(app.currentCarIndex).getObj_id(),
				response);
		editor.commit();
	}

	/**
	 * 获取驾驶信息 requestDrive
	 */
	public void requestDrive(String device_id, String gasNo) {

		String url = "";
		try {
			url = Constant.BaseUrl + "device/" + device_id
					+ "/day_drive?auth_code=" + app.auth_code + "&day="
					+ GetSystem.GetNowMonth().getDay() + "&city="
					+ URLEncoder.encode(app.City, "UTF-8") + "&gas_no=" + gasNo;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		Listener<String> listener = new Response.Listener<String>() {
			public void onResponse(String response) {
				// 返回数据，发送到工作子线程去解析
				Message msg = workHandler.obtainMessage();
				msg.what = Msg.Get_Car_Drive;
				msg.obj = response;
				workHandler.sendMessage(msg);
			}
		};

		Request request = new StringRequest(url, listener, errorListener);
		mQueue.add(request);

	}

	/**
	 * 解析驾驶信息
	 */
	private Bundle parseDrive(String response) {
		try {
			Bundle bundle = new Bundle();
			JSONObject jsonObject = new JSONObject(response);
			int drive_score = jsonObject.getInt("drive_score");
			bundle.putInt("drive_score", drive_score);
			saveResDrive(response);
			return bundle;
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * 
	 * saveResDrive 保存驾驶结果
	 * 
	 * @param response
	 */
	private void saveResDrive(String response) {

		// 存在本地
		SharedPreferences preferences = context.getSharedPreferences(
				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(
				Constant.sp_drive_score
						+ app.carDatas.get(app.currentCarIndex).getObj_id(),
				response);
		editor.commit();
	}

	/**
	 * 获取限行信息 requestCarLimit
	 */
	public void requestCarLimit(String device_id, String objName) {
		if (app.City == null || objName == null || app.City.equals("")
				|| objName.equals("")) {
			return;

		}
		String url = "";
		try {
			url = Constant.BaseUrl + "base/ban?city="
					+ URLEncoder.encode(app.City, "UTF-8") + "&obj_name="
					+ URLEncoder.encode(objName, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		Listener<String> listener = new Response.Listener<String>() {
			public void onResponse(String response) {
				// 返回数据，发送到工作子线程去解析
				Message msg = workHandler.obtainMessage();
				msg.what = Msg.Get_Car_Limit;
				msg.obj = response;
				workHandler.sendMessage(msg);
			}
		};

		Request request = new StringRequest(url, listener, errorListener);
		mQueue.add(request);

	}

	/**
	 * 解析限行信息
	 */
	private Bundle parseCarLimit(String response) {
		try {

			Bundle bundle = new Bundle();
			if (response == null || response.equals("")) {
				bundle.putString("limit", "不限");
			} else {
				JSONObject jsonObject = new JSONObject(response);
				String limit = jsonObject.getString("limit");
				bundle.putString("limit", limit);
			}
			return bundle;
		} catch (Exception e) {

		}
		return null;
	}

	/**
	 * 设置隐身模式 putStealthMode
	 * 
	 * @param mode
	 */
	public void putStealthMode(int mode) {

		if (app.carDatas.size() < 1) {
			return;
		}

		// Log.i("HttpCarInfo", "carrent car index " + app.currentCarIndex);
		deviceId = app.carDatas.get(app.currentCarIndex).getDevice_id();
		String url = Constant.BaseUrl + "device/" + deviceId
				+ "/stealth_mode?auth_code=" + app.auth_code;
		Log.i("HttpCarInfo", url);
		JSONObject json = new JSONObject();
		try {
			json.put("stealth_mode", mode);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Listener listener = new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
			}
		};
		JsonObjectRequest request = new JsonObjectRequest(Method.PUT, url,
				json, listener, errorListener);
		mQueue.add(request);
	}

}
