package com.wise.baba.biz;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.wise.baba.AppApplication;
import com.wise.baba.app.Constant;
import com.wise.baba.app.Msg;
import com.wise.baba.entity.AQIEntity;
import com.wise.baba.entity.Air;
import com.wise.baba.entity.Weather;
import com.wise.baba.net.NetThread;
import com.wise.baba.util.DateUtil;

public class HttpWeather {

	private Context context;
	private Handler uiHandler;
	private Handler workHandler;
	private HandlerThread handlerThread = null;
	private RequestQueue mQueue;
	private AppApplication app;

	public HttpWeather(Context context, Handler uiHandler) {
		super();
		this.context = context;
		this.uiHandler = uiHandler;
		mQueue = HttpUtil.getRequestQueue(context);
		app = (AppApplication) ((Activity) context).getApplication();
		handlerThread = new HandlerThread("HttpWeather");
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
			case Msg.Get_Weather:
				// 解析后提交ui线程更新数据
				Weather weather = jsonWeather(msg.obj.toString());
				Message m = uiHandler.obtainMessage();
				m.what = msg.what;
				m.obj = weather;
				uiHandler.sendMessage(m);
				break;
			}
			return false;
		}

	};
	/**
	 * 请求天气信息
	 */
	public void requestWeather() {

		String url = "";
		try {
			url = Constant.BaseUrl + "base/weather2?city="
					+ URLEncoder.encode(app.City, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		Log.i("HttpWeather", url);
		Listener<String> listener = new Response.Listener<String>() {
			public void onResponse(String response) {

				Message msg = new Message();
				msg.what = Msg.Get_Weather;
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
	 * 请求天气信息
	 */
	public void requestWeather(String city) {

		String url = "";
		try {
			url = Constant.BaseUrl + "base/weather2?city="
					+ URLEncoder.encode(city, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		Log.i("HttpWeather", url);
		Listener<String> listener = new Response.Listener<String>() {
			public void onResponse(String response) {

				Message msg = new Message();
				msg.what = Msg.Get_Weather;
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
	

	private Weather jsonWeather(String str) {
		JSONObject obj = null;
		try {
			obj = new JSONObject(str);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Weather weather = new Weather();
		weather.setCity(obj.optString("city"));
		weather.setQuality(obj.optString("quality"));
		return weather;
	}

}
