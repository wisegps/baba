/**
 * 
 */
package com.wise.baba.biz;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

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
public class HttpServiceProvider {
	private Handler handler;
	private RequestQueue mQueue;
	private AppApplication app;
	private Context context;

	public HttpServiceProvider(Context context, Handler handler) {
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
		String url = Constant.BaseUrl + "/customer/178/total?auth_code="+app.auth_code;
		Log.i("HttpService", url);
		Listener listener = new Response.Listener<String>() {
			public void onResponse(String response) {
				Message msg = new Message();
				msg.what = Msg.Get_Customer_Total;
				int[] totals = parseJsonString(response);
				msg.obj = totals;
				handler.sendMessage(msg);
			}
		};

		ErrorListener errorListener = new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.i("HttpServiceProvider", error.getMessage());
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
	public int[] parseJsonString(String strJson) {
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(strJson);
			int customer_total = jsonObject.getInt("customer_total"); 
			int vehicle_total = jsonObject.getInt("vehicle_total"); 
			int unread_message = jsonObject.getInt("unread_message"); 
			int alert_total = jsonObject.getInt("alert_total"); 
			int fault_total = jsonObject.getInt("fault_total"); 
			int overdue_total = jsonObject.getInt("overdue_total"); 
			return new int[]{customer_total,vehicle_total,unread_message,alert_total,fault_total,overdue_total};
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
		
		
	}
	
	public void cancle() {
		if (mQueue != null) {
			mQueue.cancelAll(context);
		}
	}


}
