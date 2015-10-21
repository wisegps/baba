/**
 * 
 */
package com.wise.baba.biz;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wise.baba.AppApplication;
import com.wise.baba.app.Constant;
import com.wise.baba.app.Msg;
import com.wise.baba.entity.AQIEntity;
import com.wise.baba.entity.Air;
import com.wise.baba.entity.Info;
import com.wise.baba.ui.fragment.FragmentCarInfo;

/**
 * 
 * @author c
 * @desc 获取速度卡片信息
 * @date 2015-4-3
 * 
 */
public class HttpGetObdData {

	private Context context;
	private Handler uiHandler;
	private Handler workHandler;
	private HandlerThread handlerThread = null;
	private RequestQueue mQueue;
	private AppApplication app;
	private String deviceId;
	private String brand;

	public HttpGetObdData(Context context, Handler uiHandler) {
		super();
		this.context = context;
		this.uiHandler = uiHandler;
		app = (AppApplication) ((Activity) context).getApplication();
		mQueue = HttpUtil.getRequestQueue(context);
		
		handlerThread = new HandlerThread("HttpGetObdData");
		handlerThread.start();

		Looper looper = handlerThread.getLooper();
		workHandler = new Handler(looper, handleCallBack);
		
	}

	
	
	/**
	 * 工作子线程回调函数：
	 * 主线程把网络请求数据发送到该工作子线程，子线程解析完毕，发送通知到ui主线程跟新界面
	 */
	public Handler.Callback handleCallBack = new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case Msg.Get_OBD_Data:
				// 解析后提交ui线程更新数据
				Bundle bundle = parse(msg.obj.toString());
				Message m = uiHandler.obtainMessage();
				m.what = msg.what;
				m.setData(bundle);
				uiHandler.sendMessage(m);
				break;
			}
			return false;
		}

	};

	/**
	 * 根据url发送get请求 返回json字符串,并解析
	 * 
	 * @param url
	 */
	public void request() {
		request(app.currentCarIndex);
	}

	/**
	 * 根据url发送get请求 返回json字符串,并解析
	 * 
	 * @param url
	 */
	public void request(int index) {
		if( app.carDatas == null || index >= app.carDatas.size()){
			return;
		}

		deviceId = app.carDatas.get(index).getDevice_id();
		brand = app.carDatas.get(index).getCar_brand();
		try {
			brand = URLEncoder.encode(brand, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String url = Constant.BaseUrl + "device/" + deviceId + "?auth_code="
				+ app.auth_code + "&brand" + brand;
		Log.i("HttpGetData", "aaa " + url);
		
		Listener<String> listener = new Response.Listener<String>() {
			public void onResponse(String response) {

				Log.i("HttpGetData", "response " + response);
				Message msg = workHandler.obtainMessage();
				msg.what = Msg.Get_OBD_Data;
				msg.obj = response;
				workHandler.sendMessage(msg);
			}
		};

		ErrorListener errorListener = new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {

				// Log.i("HttpGetData", error.getMessage());
			}
		};
		Request request = new StringRequest(url, listener, errorListener);
		request.setShouldCache(false);
		mQueue.add(request);
	}
	
	/**
	 * 查询空气净化信息
	 * @param index
	 */
	public void requestAir(int index){
		request(index);
	}
	/**
	 * 
	 * 解析返回字符串
	 * 
	 * @param response
	 * @return
	 */
	private Bundle parse(String response) {
		Bundle budle = new Bundle();
		try {

			Boolean isStart = false;
			JSONObject gpsData = new JSONObject(response)
					.getJSONObject("active_gps_data");
			JSONArray uni_status = gpsData.getJSONArray("uni_status");
			for (int i = 0; i < uni_status.length(); i++) {
				if (((Integer) uni_status.get(i)) == Info.CarStartStatus) {
					isStart = true;
					break;
				}
			}

			JSONObject jsonObject = new JSONObject(response)
					.optJSONObject("active_obd_data");

			if(jsonObject != null){
				
				/*
				 * 速度卡片相关信息
				 */
				String ss = $(jsonObject, "ss");
				String fdjzs = $(jsonObject, "fdjzs");
				String dpdy = $(jsonObject, "dpdy");
				String sw = $(jsonObject, "sw");
				String fdjfz = $(jsonObject, "fdjfz");
				String jqmkd = $(jsonObject, "jqmkd");
				String syyl = $(jsonObject, "syyl");

				budle.putString("ss", ss);
				budle.putString("fdjzs", fdjzs);
				budle.putString("dpdy", dpdy);
				budle.putString("sw", sw);
				budle.putString("fdjfz", fdjfz);
				budle.putString("jqmkd", jqmkd);
				budle.putString("syyl", syyl);
				budle.putBoolean("isStart", isStart);
				
			}
			

			/*
			 * 空气净化
			 */

			int air = gpsData.optInt("air");

			budle.putInt("air", air);
			

			JSONObject jsonParams = new JSONObject(response)
					.getJSONObject("params");

			int vSwitch =jsonParams.optInt("switch");

			budle.putInt("switch", vSwitch);
			
			
			int air_mode = jsonParams.optInt("air_mode");

			budle.putInt("air_mode", air_mode);
			
			int airDuration = jsonParams.optInt("air_duration");

			budle.putInt("airDuration", airDuration);
			
			String air_time = jsonParams.optString("air_time");

			budle.putString("air_time", air_time);
			
			
			Log.i("HttpGetData", "strSwitch "+vSwitch);
		} catch (JSONException e) {
			Log.i("HttpGetData", "exception " + e.getMessage());
			e.printStackTrace();
		}

		return budle;

	}

	// 把json对象为空判断 转化为int
	private String $(JSONObject json, String key) {

		Object obj = null;
		try {
			obj = json.get(key);
		} catch (JSONException e) {
			Log.i("HttpGetData", "exception1 " + e.getMessage());
			e.printStackTrace();
			return "--";
		}

		if (obj.equals(null)) {
			return "--";
		} else {
			Double value = Double.parseDouble(obj.toString());
			int i = value.intValue();
			Log.i("HttpGetData", "intValue " + key + " :" + i);
			return value.intValue() + "";
		}
	}
	
	
	

}
