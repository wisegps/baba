package com.wise.baba.net;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class HttpVolley {

	private Context context;
	private Handler handler;
	private RequestQueue mQueue;
	
	public HttpVolley(Context context,Handler handler) {
		super();
		this.context = context;
		this.handler = handler;
	}
	
	/**
	 * 根据url发送get请求 返回json字符串
	 * @param url
	 */
	public void request(String url,final int what){
		 mQueue = Volley.newRequestQueue(context);
		Listener listener = new Response.Listener<String>(){
			public void onResponse(String response) {
				Message msg = new Message();
				msg.what = what;
				msg.obj = response;
				System.out.println("url response "+response);
				handler.sendMessage(msg);
			}
		};
		
		ErrorListener errorListener = new ErrorListener(){
			@Override
			public void onErrorResponse(VolleyError error) {
				System.out.println("url error "+error.getMessage());
			}
		};
		Request request = new StringRequest(url, listener, errorListener);
		mQueue.add(request);
		mQueue.start();
	}
	
	public void cancle(){
		if(mQueue != null){
			mQueue.cancelAll(context);
		}
	}
	
}
