/**
 * 
 */
package com.wise.baba.biz;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.wise.baba.AppApplication;
import com.wise.baba.app.Constant;

/**
 * 
 * @author c
 * @desc baba
 * @date 2015-4-21
 * 
 */
public class HttpCarInfo {
	private Context context;
	private Handler handler;
	private RequestQueue mQueue;
	private AppApplication app;
	private String deviceId;

	public HttpCarInfo(Context context, Handler handler) {
		super();
		this.context = context;
		this.handler = handler;
		app = (AppApplication) ((Activity) context).getApplication();
		mQueue = HttpUtil.getRequestQueue(context);
	}

	public void putStealthMode(int mode) {

		if (app.carDatas.size() < 1) {
			return;
		}

		Log.i("HttpCarInfo", "carrent car index " + app.currentCarIndex);
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

		Listener<JSONObject> listener = new Response.Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject response) {
				Log.i("HttpCarInfo", response.toString());
			}
		};
		ErrorListener errorListener = new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.i("HttpCarInfo", error.toString());
			}
		};
		JsonObjectRequest request = new JsonObjectRequest(Method.PUT, url,
				json, listener, errorListener);
		mQueue.add(request);
	}

}
