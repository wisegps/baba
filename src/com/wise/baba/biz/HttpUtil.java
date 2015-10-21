package com.wise.baba.biz;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RequestQueue.RequestFilter;
import com.android.volley.toolbox.Volley;

/**
 * HttpUtil 网络连接工具类
 * @author c
 * @date 2015-10-19
 */
public class HttpUtil {
	
	private static RequestQueue rquestQueue = null;

	/**
	 * 
	 *getRequestQueue 获取全局单实例Requestqueue
	 *@param ctx
	 *@return
	 */
	public static RequestQueue getRequestQueue(Context ctx) {
		if (rquestQueue == null) {
			rquestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
		}
		return rquestQueue;
	}
	
	public static void destoryVolley(){
		rquestQueue.cancelAll(new RequestFilter(){
			@Override
			public boolean apply(Request<?> request) {
				return true;
			}
		});
	}

}
