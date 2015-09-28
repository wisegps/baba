package com.wise.baba.biz;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
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
import com.wise.baba.entity.Weather;
import com.wise.baba.net.NetThread;
import com.wise.baba.util.DateUtil;

public class HttpWeather {

	private Context context;
	private Handler handler;
	private RequestQueue mQueue;
	private AppApplication app;

	public HttpWeather(Context context, Handler handler) {
		super();
		this.context = context;
		this.handler = handler;
		mQueue = Volley.newRequestQueue(context);
		app = (AppApplication) ((Activity) context).getApplication();
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
				msg.obj = jsonWeather(response);
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
