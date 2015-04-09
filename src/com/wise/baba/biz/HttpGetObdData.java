/**
 * 
 */
package com.wise.baba.biz;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import pubclas.Constant;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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
import com.wise.baba.AppApplication;
import com.wise.baba.app.Msg;

/**
 * 
 * @author c
 * @desc 获取速度卡片信息
 * @date 2015-4-3
 * 
 */
public class HttpGetObdData {

	private Context context;
	private Handler handler;
	private RequestQueue mQueue;
	private AppApplication app;
	private String deviceId;
	private String brand;

	public HttpGetObdData(Context context, Handler handler) {
		super();
		this.context = context;
		this.handler = handler;
		app = (AppApplication) ((Activity) context).getApplication();
		mQueue = Volley.newRequestQueue(context);
		
	}

	/**
	 * 根据url发送get请求 返回json字符串,并解析
	 * 
	 * @param url
	 */
	public void request() {
		if (app.carDatas.size() < 1) {
			return;
		}
		
		deviceId = app.carDatas.get(0).getDevice_id();
		brand = app.carDatas.get(0).getCar_brand();
		try {
			brand = URLEncoder.encode(brand, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String url = Constant.BaseUrl + "device/" + deviceId + "?auth_code="
				+ app.auth_code + "&brand" + brand;
		
		Listener<String> listener = new Response.Listener<String>() {
			public void onResponse(String response) {
				Log.i("HttpGetData", "fffff" + response);
				Message msg = new Message();
				msg.what = Msg.Get_OBD_Data;
				Bundle bundle = parse(response);
				msg.setData(bundle);
				handler.sendMessage(msg);
			}
		};

		ErrorListener errorListener = new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {

				Log.i("HttpGetData", error.getMessage());
			}
		};
		Request request = new StringRequest(url, listener, errorListener);
		mQueue.add(request);
		mQueue.start();
	}

	
	/**
	 * 
	 * 解析返回字符串
	 * @param response
	 * @return
	 */
	public Bundle parse(String response) {
		Bundle budle = new Bundle();
		try {
			JSONObject jsonObject = new JSONObject(response)
					.getJSONObject("active_obd_data");
			int ss = $(jsonObject.getDouble("ss"));
			int fdjfz = $(jsonObject.getDouble("fdjfz"));
			int dpdy = $(jsonObject.getDouble("dpdy"));
			int sw = $(jsonObject.getDouble("sw"));
			int jqmkd = $(jsonObject.getDouble("jqmkd"));
			int syyl = $(jsonObject.getDouble("syyl"));
			
			budle.putInt("ss", ss);
			budle.putInt("fdjfz", fdjfz);
			budle.putInt("dpdy", dpdy);
			budle.putInt("sw", sw);
			budle.putInt("jqmkd", jqmkd);
			budle.putInt("syyl", syyl);
			
		} catch (JSONException e) {
			Log.i("HttpGetData", e.getMessage());
			e.printStackTrace();
		}


		return budle;

	}
	
	public int  $(Double value){
		Log.i("HttpGetData", value+" obj");
		if(value == null){
			return 0;
		}else {
			return value.intValue();
		}
	}

	public void cancle() {
		if (mQueue != null) {
			mQueue.cancelAll(context);
		}
	}

}
