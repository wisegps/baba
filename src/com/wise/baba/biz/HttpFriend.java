package com.wise.baba.biz;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.CheckBox;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wise.baba.AppApplication;
import com.wise.baba.app.Constant;
import com.wise.baba.entity.FriendSearch;


/**
 * 
 * @author ccc 从网络获取好友信息
 * 
 */
public class HttpFriend {

	private Context context;
	private Handler handler;
	private RequestQueue mQueue;
	private AppApplication app;

	public HttpFriend(Context context, Handler handler) {
		super();
		this.context = context;
		this.handler = handler;
		app = (AppApplication) ((Activity) context).getApplication();
		mQueue = Volley.newRequestQueue(context);
	}

	/** 通过名称获取好友信息 **/
	public  void searchByName(String name) {
		try {
			name = URLEncoder.encode(name, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		String url = Constant.BaseUrl + "customer/search?auth_code="
				+ app.auth_code + "&account=" + name;
		request(url);
	}

	/** 通过ID获取好友信息 **/
	public void searchById(String friendId) {
		String url = Constant.BaseUrl + "customer/" + friendId + "?auth_code="
				+ app.auth_code;
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
				msg.what = 6;
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
		List<FriendSearch> friends = new LinkedList<FriendSearch>();
		Gson gson = new Gson();
		if (strJson.startsWith("[]")) {
		} else if (strJson.startsWith("{")) {// 只有一条数据
			FriendSearch friend = gson.fromJson(strJson, FriendSearch.class);
			friends.add(friend);
		} else {// 搜索到多个好友
			friends = gson.fromJson(strJson,
					new TypeToken<List<FriendSearch>>() {
					}.getType());
		}

		return friends;
	}
	
	/**
	 * 获取好友权限
	 * @param url
	 */
	public void getAuthCode(String url){
		Listener listener = new Response.Listener<String>() {
			public void onResponse(String response) {
				try {
					JSONObject jsonObject = new JSONObject(response);
					JSONArray values = jsonObject.getJSONArray("rights");
					int[] authCode = new int[values.length()];
					for(int i =0;i<values.length();i++){
						int code = values.getInt(i);
						authCode[i] = code;
						Message msg = new Message();
						int get_authToMe = 2;
						msg.what = get_authToMe;
						msg.obj = authCode;
						handler.sendMessage(msg);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}			
				System.out.println("url request " + response);
			}
		};
		ErrorListener errorListener = new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				System.out.println("url error " + error.getMessage());
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
