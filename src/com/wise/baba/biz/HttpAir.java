/**
 * 
 */
package com.wise.baba.biz;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.wise.baba.AppApplication;
import com.wise.baba.app.Const;
import com.wise.baba.app.Constant;
import com.wise.baba.app.Msg;
import com.wise.baba.entity.AQIEntity;
import com.wise.baba.entity.Air;
import com.wise.baba.util.DateUtil;

/**
 * 
 * @author c
 * @desc 空气净化
 * @date 2015-4-3
 * 
 */
public class HttpAir {

	private Context context;
	private Handler uiHandler;
	private Handler workHandler;
	private HandlerThread handlerThread = null;

	private RequestQueue mQueue;
	private AppApplication app;
	private String deviceId;
	private String brand;

	public static int COMMAND_SWITCH = 0x4043; // 开关指令
	public static int COMMAND_AIR_MODE = 0x4044; // 设置净化模式指令
	public static int COMMAND_AIR_SPEED = 0x4045; // 设置净化速度指令

	public final int POWER_ON = 1;
	public final int POWER_OFF = 0;

	public HttpAir(Context context, Handler uiHandler) {
		super();
		this.context = context;
		this.uiHandler = uiHandler;
		app = (AppApplication) ((Activity) context).getApplication();
		mQueue = HttpUtil.getRequestQueue(context);

		handlerThread = new HandlerThread("HttpAir");
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
			case Msg.Get_Air_Value:
				// 解析后提交ui线程更新数据
				Air air = paseAir(msg.obj.toString());
				Message m = uiHandler.obtainMessage();
				m.what = msg.what;
				m.obj = air;
				uiHandler.sendMessage(m);
				break;
			case Msg.Get_Air_AQI:
				List<AQIEntity> list = parseAQI(msg.obj.toString());
				m = uiHandler.obtainMessage();
				m.what = msg.what;
				m.obj = list;
				uiHandler.sendMessage(m);
				break;
			}
			return false;
		}

	};

	/**
	 * 发送postt请求 返回json字符串,并解析
	 * 
	 * @param url
	 */
	public void request(String deviceId, final int command, String params) {

		String url = Constant.BaseUrl + "command?auth_code=" + app.auth_code;

		String data = "{device_id:" + deviceId + ",cmd_type:" + command
				+ ",params:" + params + "}";

		Log.i("HttpAir", data);

		JSONObject jsonObject = null;

		try {
			jsonObject = new JSONObject(data);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Request request = new JsonObjectRequest(Method.POST, url, jsonObject,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						uiHandler.sendEmptyMessage(Msg.Set_Air_Response);
						Log.i("HttpAir", "response " + response.toString());
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						uiHandler.sendEmptyMessage(Msg.Set_Air_Response);
					}
				});

		request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 1, 1.0f));
		mQueue.add(request);

	}

	/**
	 * 请求空气质量指数
	 */
	public void requestAQI(String deviceId) {

		String startTime = DateUtil.getCurrentTime(3);
		String endTime = DateUtil.getCurrentTime(0);
		try {
			startTime = URLEncoder.encode(startTime, "gbk");
			endTime = URLEncoder.encode(endTime, "gbk");

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		String url = Constant.BaseUrl + "device/" + deviceId
				+ "/air_data?auth_code=" + app.auth_code + "&start_time="
				+ startTime + "&end_time=" + endTime;

		Listener<String> listener = new Response.Listener<String>() {
			public void onResponse(String response) {
				//返回数据，发送到工作子线程去解析
				Log.i("HttpAir", response);
				Message msg = workHandler.obtainMessage();
				msg.what = Msg.Get_Air_AQI;
				msg.obj = response;
				workHandler.sendMessage(msg);
			}
		};

		ErrorListener errorListener = new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {

			}
		};
		Request request = new StringRequest(url, listener, errorListener);
		request.setShouldCache(false);
		mQueue.add(request);

	}

	/**
	 * 请求空气质量指数,提交工作线程解析
	 */
	public void requestAir(int index) {
		if (app.carDatas == null || index >= app.carDatas.size()) {
			return;
		}
		deviceId = app.carDatas.get(index).getDevice_id();

		String url = Constant.BaseUrl + "device/" + deviceId
				+ "/active_gps_data?auth_code=" + app.auth_code;

		Log.i("HttpAir", url.toString());
		Listener<String> listener = new Response.Listener<String>() {
			public void onResponse(String response) {
				//返回数据，发送到工作子线程去解析
				Message msg = workHandler.obtainMessage();
				msg.what = Msg.Get_Air_Value;
				msg.obj = response;
				workHandler.sendMessage(msg);
			}
		};

		ErrorListener errorListener = new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {

			}
		};
		Request request = new StringRequest(url, listener, errorListener);
		mQueue.add(request);

	}

	private Air paseAir(String response) {
		Air mAir = new Air();

		int airValue = 0;
		int airSwitch = 0, airDuration = 0, airMode = 0;
		String airTime = "";

		try {
			JSONObject obj = new JSONObject(response);
			JSONObject data = obj.optJSONObject("active_gps_data");
			JSONObject params = obj.optJSONObject("params");
			airValue = data.optInt("air");
			airSwitch = params.optInt("switch");
			airMode = params.optInt("air_mode");
			airTime = params.optString("air_time");
			airDuration = params.optInt("air_duration");

		} catch (JSONException e) {
			e.printStackTrace();
		}

		mAir.setAir(airValue);
		mAir.setAirSwitch(airSwitch);
		mAir.setAirMode(airMode);
		mAir.setAirDuration(airDuration);
		mAir.setAirTime(airTime);
		return mAir;

	}

	private List<AQIEntity> parseAQI(String response) {

		Log.i("AirQualityIndexActivity", "parseAQI ID: "
				+ Thread.currentThread().getId());
		List<AQIEntity> list = new ArrayList<AQIEntity>();
		try {
			JSONArray jsonArray = new JSONArray(response);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject obj = jsonArray.getJSONObject(i);
				String time = obj.getString("rcv_time");
				time = DateUtil.getTime(time);
				int air = obj.getInt("air");
				AQIEntity aqi = new AQIEntity();
				aqi.setTime(time);
				aqi.setAir(air);
				list.add(aqi);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return list;
	}

	public void setPower(String deviceId, boolean power) {
		int command = COMMAND_SWITCH;
		int value = power ? 1 : 0;
		// value = 1;
		String params = "{switch: " + value + "}";
		request(deviceId, command, params);
	}

	public void setMode(String deviceId, int mode, String time, int duration) {
		int command = COMMAND_AIR_MODE;
		String params = "{air_mode: " + mode + "}";
		if (mode == Const.AIR_MODE_TIMER) {
			params = "{air_mode: " + mode + ",air_time:'" + time
					+ "',air_duration:" + duration + "}";
		}
		request(deviceId, command, params);
	}

}
