package com.wise.baba.biz;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.wise.baba.AppApplication;
import com.wise.baba.app.Constant;
import com.wise.baba.app.Msg;
import com.wise.baba.entity.AQIEntity;
import com.wise.baba.entity.Air;

/**
 * HttpCarList,更新所有车辆信息
 * 
 * @author c
 * @date 2015-10-23
 */
public class HttpCarInfoList {
	private Context context;
	private Handler uiHandler;
	private Handler workHandler;
	private HandlerThread handlerThread = null;

	private RequestQueue mQueue;
	private AppApplication app;

	public HttpCarInfoList(Context context, Handler uiHandler) {
		super();
		this.context = context;
		this.uiHandler = uiHandler;
		app = (AppApplication) ((Activity) context).getApplication();
		mQueue = HttpUtil.getRequestQueue(context);

		handlerThread = new HandlerThread("HttpCarInfoList");
		handlerThread.start();

		Looper looper = handlerThread.getLooper();
		workHandler = new Handler(looper, handleCallBack);

	}

	/**
	 * 工作子线程回调函数： 主线程把网络请求数据发送到该工作子线程，子线程解析完毕，发送通知到ui主线程跟新界面
	 */
	public Handler.Callback handleCallBack = new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case Msg.Get_Car_Info_List:
				// 解析后提交ui线程更新数据
				String response = msg.obj.toString();
				app.carDatas.clear();
				app.carDatas.addAll(JsonData.jsonCarInfo(response));
				break;
			}
			return false;
		}

	};

	/**
	 * 获取设备信息 requestDevice
	 */
	public void request() {
		String url = Constant.BaseUrl + "customer/" + app.cust_id
				+ "/vehicle?auth_code=" + app.auth_code;
		Listener<String> listener = new Response.Listener<String>() {
			public void onResponse(String response) {
				// 返回数据，发送到工作子线程去解析
				Message msg = workHandler.obtainMessage();
				msg.what = Msg.Get_Car_Info_List;
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
		mQueue.add(request);
	}

}
