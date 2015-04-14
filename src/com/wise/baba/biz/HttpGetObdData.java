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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
		cancle();
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
		Log.i("HttpGetData", "aaa " + url);

		Listener<String> listener = new Response.Listener<String>() {
			public void onResponse(String response) {
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

				// Log.i("HttpGetData", error.getMessage());
			}
		};
		Request request = new StringRequest(url, listener, errorListener);
		request.setShouldCache(false);
		mQueue.add(request);
		mQueue.start();
	}

	/**
	 * 
	 * 解析返回字符串
	 * 
	 * @param response
	 * @return
	 */
	public Bundle parse(String response) {
		Bundle budle = new Bundle();
		try {
			JSONObject jsonObject = new JSONObject(response)
					.getJSONObject("active_obd_data");

			int ss = $(jsonObject, "ss");
			int fdjfz = $(jsonObject, "fdjfz");
			int dpdy = $(jsonObject, "dpdy");
			int sw = $(jsonObject, "sw");
			int jqmkd = $(jsonObject, "jqmkd");
			int syyl = $(jsonObject, "syyl");

			budle.putInt("ss", ss);
			budle.putInt("fdjfz", fdjfz);
			budle.putInt("dpdy", dpdy);
			budle.putInt("sw", sw);
			budle.putInt("jqmkd", jqmkd);
			budle.putInt("syyl", syyl);

		} catch (JSONException e) {
			Log.i("HttpGetData", "exception " + e.getMessage());
			e.printStackTrace();
		}

		return budle;

	}

	// 把json对象为空判断 转化为int
	public int $(JSONObject json, String key) {

		Object obj = null;
		try {
			obj = json.get(key);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (obj.equals(null)) {
			return 0;
		} else {
			Double value = Double.parseDouble(obj.toString());
			return value.intValue();
		}
	}

	public void cancle() {
		if (mQueue != null) {
			mQueue.cancelAll(context);
		}
	}

}
