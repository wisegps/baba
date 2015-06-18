package com.wise.baba.biz;
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
import com.wise.baba.db.dao.FriendData;

/**
 * 
 * @author ccc 从网络获取好友列表
 * 
 */
public class HttpFriendList {

	private Handler handler;
	private RequestQueue mQueue;
	private AppApplication app;

	public HttpFriendList(Context context, Handler handler) {
		super();
		//this.context = context;
		this.handler = handler;
		app = (AppApplication) ((Activity) context).getApplication();
		mQueue = Volley.newRequestQueue(context);
	}
	
	public void request(){
		String url = Constant.BaseUrl + "customer/" + app.cust_id
				+ "/get_friends?auth_code=" + app.auth_code + "&friend_type=1";
		
		request(url);
	}

	/**
	 * 根据url发送get请求 返回json字符串,并解析
	 * 
	 * @param url
	 */
	public void request(String url) {
		Listener listener = new Response.Listener<String>() {
			public void onResponse(String response) {
				Message msg = new Message();
				msg.what = Msg.GetFriendList;
				List friends = parseJsonString(response);
				msg.obj = friends;
				handler.sendMessage(msg);

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

	/**
	 * 
	 * @param strJson
	 * @return 根据返回的字符串解析成朋友信息列表，也可能是一个的信息
	 */
	public List parseJsonString(String strJson) {
		Log.i("HttpFriendList",strJson);
		List<FriendData> friends = new LinkedList<FriendData>();
		Gson gson = new Gson();
		if (strJson.startsWith("[]")) {
		} else if (strJson.startsWith("{")) {// 只有一条数据
			FriendData friend = gson.fromJson(strJson, FriendData.class);
			friends.add(friend);
		} else {// 搜索到多个好友
			friends = gson.fromJson(strJson,
					new TypeToken<List<FriendData>>() {
					}.getType());
		}

		return friends;
	}

}
