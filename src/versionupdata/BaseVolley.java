package versionupdata;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


/**
 * 本类目的是全局共用一个RequestQueue，统一网络请求入口 
 * 不同实例对象用不同的handler处理返回结果
 * @author wu
 */
public class BaseVolley {
	
	public static RequestQueue mQueue;

	
	public BaseVolley(){
		super();
	}
	
	
	/**
	 * 在 Activity 中 初始化一下
	 * 
	 * @param context
	 */
	public static void init(Context context) {
		if (mQueue == null) {
			mQueue = Volley.newRequestQueue(context.getApplicationContext());
		}
	}
	
	/**
	 * @param url 根据Wistorm API请求规则拼接好的url
	 * @param onSuccess 连接成功回调
	 * @param onError   连接失败回调
	 */
	public void request(String url, Response.Listener<String> onSuccess, Response.ErrorListener onError) {
		if(mQueue==null){
			Log.i("BaseVolley", "RequestQueue还木有初始化，请先调用init方法");
			return ;
		}
		Request request = new StringRequest(url, onSuccess, onError);
		request.setShouldCache(false);
		mQueue.add(request);
	}
	
}
