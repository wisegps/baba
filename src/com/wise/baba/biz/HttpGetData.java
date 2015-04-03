/**
 * 
 */
package com.wise.baba.biz;

import org.json.JSONException;
import org.json.JSONObject;

import pubclas.Constant;
import android.app.Activity;
import android.content.Context;
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

	public HttpGetData(Context context, Handler handler) {
		super();
		this.context = context;
		this.handler = handler;
		app = (AppApplication) ((Activity) context).getApplication();
		deviceId = app.carDatas.get(0).getDevice_id();
		mQueue = Volley.newRequestQueue(context);
	}

	/**
	 * 根据url发送get请求 返回json字符串,并解析
	 * 
	 * @param url
	 */
	public void request(final int type) {
		String url = Constant.BaseUrl + "device/" + deviceId
				+ "/obd_data?auth_code=" + app.auth_code + "&type=" + type;
		Log.i("HttpGetData", url);
		Listener listener = new Response.Listener<String>() {
			public void onResponse(String response) {
				try {
					Message msg = new Message();
					msg.what = type;
					JSONObject jsonObject;
					jsonObject = new JSONObject(response);
					Double realValue = (Double) jsonObject.getDouble("real_value");
					msg.obj = String.format("%.0f", realValue);
					Log.i("HttpGetData", realValue+"");
					handler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		};

		ErrorListener errorListener = new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
			}
		};
		Request request = new StringRequest(url, listener, errorListener);
		mQueue.add(request);
		mQueue.start();
	}


	public void cancle() {
		if (mQueue != null) {
			mQueue.cancelAll(context);
		}
	}

}
