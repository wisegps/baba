/**
 * 
 */
package com.wise.baba.biz;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wise.baba.AppApplication;
import com.wise.baba.app.Constant;
import com.wise.baba.app.Msg;

/**
 * 
 * @author c
 * @desc 空气净化
 * @date 2015-4-3
 * 
 */
public class HttpAir {

	private Context context;
	private Handler handler;
	private RequestQueue mQueue;
	private AppApplication app;
	private String deviceId;
	private String brand;
	
	public static int COMMAND_SWITCH = 0x4043; //开关指令
	public static int COMMAND_AIR_MODE = 0x4044; //设置净化模式指令
	public static int COMMAND_AIR_SPEED = 0x4045; //设置净化速度指令
	
	public final int POWER_ON = 1;
	public final int POWER_OFF = 0;

	public HttpAir(Context context, Handler handler) {
		super();
		this.context = context;
		this.handler = handler;
		app = (AppApplication) ((Activity) context).getApplication();
		mQueue = Volley.newRequestQueue(context);
	}


	/**
	 * 根据url发送get请求 返回json字符串,并解析
	 * 
	 * @param url
	 */
	public void request(String deviceId,final int command,String params) {

			String url = Constant.BaseUrl + "command?auth_code=" + app.auth_code;
			
			String data = "{device_id:"+deviceId+",cmd_type:" + COMMAND_SWITCH+",params:" +params+"}";

			Log.i("HttpAir", data);
			JSONObject jsonObject = null;
			try {
				jsonObject = new JSONObject(data);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			Request request = new JsonObjectRequest(Method.POST, url, jsonObject,
					new Response.Listener<JSONObject>() {
						@Override
						public void onResponse(JSONObject response) {
							Log.i("HttpAir", response.toString());
							
							handler.sendEmptyMessage(Msg.Set_Air_Response);
							
//							//以下情况，处理失败
//							if(response.optInt("status_code") != 15){
//								return;
//							}
//							
//							if(command == COMMAND_SWITCH  ){
//								handler.sendEmptyMessage(Msg.Set_Air_Power_Fail);
//							}
//							
//							if(command == COMMAND_AIR_MODE ){
//								handler.sendEmptyMessage(Msg.Set_Air_Auto_Fail);
//							}
//							
//							if(command == COMMAND_AIR_SPEED  ){
//								handler.sendEmptyMessage(Msg.Set_Air_Level_Fail);
//							}
//							
							
						}
					}, new Response.ErrorListener() {

						@Override
						public void onErrorResponse(VolleyError error) {
							Log.i("HttpAir", error.getMessage());
							handler.sendEmptyMessage(Msg.Set_Air_Response);
							
						}
					});

			
			request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 1, 1.0f));
			mQueue.add(request);

	}
	

	
	public void setPower(String deviceId,boolean power){
		int command = COMMAND_SWITCH;
		int value  = power?1:0;
		String params = "{switch: "+value+"}";
		request(deviceId, command, params);
	}

	public void cancle() {
		if (mQueue != null) {
			mQueue.cancelAll(context);
		}
	}

}
