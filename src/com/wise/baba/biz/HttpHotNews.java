/**
 * 
 */
package com.wise.baba.biz;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wise.baba.AppApplication;
import com.wise.baba.app.Constant;
import com.wise.baba.app.Msg;
import com.wise.baba.entity.News;

/**
 * 
 * @author c
 * @desc baba 获取新闻
 * @date 2015-4-21
 * 
 */
public class HttpHotNews {
	private Handler uiHandler;
	private Handler workHandler;
	private HandlerThread handlerThread = null;
	private RequestQueue mQueue;
	private AppApplication app;
	private Context context;

	public HttpHotNews(Context context, Handler uiHandler) {
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
			case Msg.Get_News_List:
				// 解析后提交ui线程更新数据
				Message m = uiHandler.obtainMessage();
				m.what = msg.what;
				m.obj = parseJsonString(msg.obj.toString());
				uiHandler.sendMessage(m);
				break;
			}
			return false;
		}

	};

	
	public void request() {

		if (app.carDatas.size() < 1) {
			return;
		}
		String city = "";
		try {
			city = URLEncoder.encode(app.City, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String url = Constant.BaseUrl + "base/hot_news/5?auth_code="
				+ app.auth_code + "&city=" + city;
		Log.i("HttpHotNews", url);
		Listener listener = new Response.Listener<String>() {
			public void onResponse(String response) {
				Message msg = workHandler.obtainMessage();
				msg.what = Msg.Get_News_List;
				msg.obj = response;
				workHandler.sendMessage(msg);
			}
		};

		ErrorListener errorListener = new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.i("HttpHotNews", error.getMessage());
			}
		};
		Request request = new StringRequest(url, listener, errorListener);
		mQueue.add(request);
	}

	/**
	 * 
	 * @param strJson
	 * @return 根据返回的字符串解析成新闻列表，也可能是一条新闻
	 */
	public List<News> parseJsonString(String strJson) {
		List<News> newsList = new LinkedList<News>();
		Gson gson = new Gson();
		if (strJson.startsWith("[]")) {
		} else if (strJson.startsWith("{")) {// 只有一条数据
			News news = gson.fromJson(strJson, News.class);
			newsList.add(news);
		} else {// 搜索到多个好友
			newsList = gson.fromJson(strJson, new TypeToken<List<News>>() {
			}.getType());
		}

		return newsList;
	}

}
