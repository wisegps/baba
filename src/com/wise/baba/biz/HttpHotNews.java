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
	private Handler handler;
	private RequestQueue mQueue;
	private AppApplication app;
	private Context context;

	public HttpHotNews(Context context, Handler handler) {
		super();
		this.context = context;
		this.handler = handler;
		app = (AppApplication) ((Activity) context).getApplication();
	
		mQueue = Volley.newRequestQueue(context);
	}

	public void request() {
		
		if (app.carDatas.size() < 1 ) {
			return ;
		}
		String city  = "";
		try {
			city = URLEncoder.encode(app.City, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String url = Constant.BaseUrl + "base/hot_news/5?auth_code="+app.auth_code+"&city=" +city;
		Log.i("HttpHotNews", url);
		Listener listener = new Response.Listener<String>() {
			public void onResponse(String response) {
				Message msg = new Message();
				msg.what = Msg.Get_News_List;
				List<News> newsList = parseJsonString(response);
				msg.obj = newsList;
				handler.sendMessage(msg);
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
		mQueue.start();
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
			newsList = gson.fromJson(strJson,
					new TypeToken<List<News>>() {
					}.getType());
		}

		return newsList;
	}
	
	public void cancle() {
		if (mQueue != null) {
			mQueue.cancelAll(context);
		}
	}


}
