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

/**
 * 
 * @author c
 * @desc 获取速度卡片信息
 * @date 2015-4-3
 * 
 */
public class HttpGetData {

	private Context context;
	private Handler handler;
	private RequestQueue mQueue;
	private AppApplication app;
	private String deviceId;
	private String brand;

	public HttpGetData(Context context, Handler handler) {
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

		Log.i("HttpGetData", "11");
		String url = Constant.BaseUrl + "device/" + deviceId + "?auth_code="
				+ app.auth_code + "&brand" + brand;
		// http://api.bibibaba.cn/device/819?auth_code=a166883973f608f2d22085038a79998a&brand=%E6%A0%87%E8%87%B4
		// String url = Constant.BaseUrl + "device/" + deviceId
		// + "/obd_data?auth_code=" + app.auth_code + "&type=" + type;
		Log.i("HttpGetData", url);
		Listener listener = new Response.Listener<String>() {
			public void onResponse(String response) {
				Log.i("HttpGetData", "fffff" + response);
				Message msg = new Message();
				msg.what = 1;
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

	public Bundle parse(String response) {
		Bundle budle = new Bundle();
		try {
			JSONObject jsonObject = new JSONObject(response)
					.getJSONObject("active_obd_data");
			budle.putInt("ss", jsonObject.getInt("ss"));
			budle.putInt("fdjfz", jsonObject.getInt("fdjfz"));
			budle.putInt("dpdy", jsonObject.getInt("dpdy"));
			budle.putInt("sw", jsonObject.getInt("sw"));
			budle.putInt("jqmkd", jsonObject.getInt("jqmkd"));
			budle.putInt("syyl", jsonObject.getInt("syyl"));
		} catch (JSONException e) {
			Log.i("HttpGetData", e.getMessage());
			e.printStackTrace();
		}

		// Double realValue = (Double) jsonObject.getDouble("real_value");
		// msg.obj = String.format("%.0f", realValue);
		// Log.i("HttpGetData", realValue+"");
		// Bundle bundle = new Bundle();
		// msg.setData(data);

		return budle;

	}

	public void cancle() {
		if (mQueue != null) {
			mQueue.cancelAll(context);
		}
	}

}
