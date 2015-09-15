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
import com.wise.baba.app.Constant;
import com.wise.baba.app.Msg;
import com.wise.baba.entity.AQIEntity;
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
	private Handler handler;
	private RequestQueue mQueue;
	private AppApplication app;
	private String deviceId;
	private String brand;

	public static int COMMAND_SWITCH = 0x4043; // 开关指令
	public static int COMMAND_AIR_MODE = 0x4044; // 设置净化模式指令
	public static int COMMAND_AIR_SPEED = 0x4045; // 设置净化速度指令

	public final int POWER_ON = 1;
	public final int POWER_OFF = 0;

	public HttpAir(Context context, Handler handler) {
		super();
		this.context = context;
		this.handler = handler;
		app = (AppApplication) ((Activity) context).getApplication();
		mQueue = Volley.newRequestQueue(context);
	}

	/**
	 * 发送postt请求 返回json字符串,并解析
	 * 
	 * @param url
	 */
	public void request(String deviceId, final int command, String params) {

		String url = Constant.BaseUrl + "command?auth_code=" + app.auth_code;

		String data = "{device_id:" + deviceId + ",cmd_type:" + COMMAND_SWITCH
				+ ",params:" + params + "}";

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
						handler.sendEmptyMessage(Msg.Set_Air_Response);
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						handler.sendEmptyMessage(Msg.Set_Air_Response);

					}
				});

		request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 1, 1.0f));
		mQueue.add(request);

	}

	/**
	 * 请求空气质量指数
	 */
	public void requestAQI(String deviceId) {

		String startTime = DateUtil.getCurrentTime(1);
		Log.i("HttpAir", "startTime: " + startTime);
		String endTime = DateUtil.getCurrentTime(0);
		Log.i("HttpAir", "endTime: " + endTime);
		try {
			startTime = URLEncoder.encode(startTime, "gbk");
			endTime = URLEncoder.encode(endTime, "gbk");

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		String url = Constant.BaseUrl + "device/" + deviceId
				+ "/air_data?auth_code=" + app.auth_code + "&start_time="
				+ startTime + "&end_time=" + endTime;
		Log.i("HttpAir", "air_data url: " + url);
		Listener<String> listener = new Response.Listener<String>() {
			public void onResponse(String response) {

				Message msg = new Message();
				msg.what = Msg.Get_Air_AQI;
				msg.obj = parseAQI(response);
				handler.sendMessage(msg);
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
		mQueue.start();

	}

	public List parseAQI(String response){
		List<AQIEntity> list = new ArrayList<AQIEntity>();
		try {
			JSONArray jsonArray = new JSONArray(response);
			for(int i =0 ;i<jsonArray.length();i++){
				JSONObject  obj = jsonArray.getJSONObject(i);
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
		String params = "{switch: " + value + "}";
		request(deviceId, command, params);
	}

	public void setMode(String deviceId, int mode, String time, int duration) {
		int command = COMMAND_AIR_MODE;
		String params = "{air_mode: " + mode + "}";

		if (mode == 0) {
			params = "{air_mode: " + mode + ",air_time:'" + time
					+ "',air_duration:" + duration + "}";
		}
		request(deviceId, command, params);
	}

	public void cancle() {
		if (mQueue != null) {
			mQueue.cancelAll(context);
		}
	}

}
