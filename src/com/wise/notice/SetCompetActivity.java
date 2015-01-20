package com.wise.notice;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pubclas.GetSystem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.wise.baba.AppApplication;
import com.wise.baba.R;

/**
 * 设置好友权限
 * 
 * @author honesty
 **/
public class SetCompetActivity extends Activity implements OnClickListener {

	private int friendId;
	private boolean isService;
	private CheckBox chkOBDStandard, chkOBDFault, chkNotice, chkViolation,
			chkLocation, chkTrip, chkFuel, chkDriving;

	private LinearLayout llytList;

	private int RIGHT_OBD_DATA = 0x6001; // 访问OBD标准数据（服务商）
	private int RIGHT_ODB_ERR = 0x6002; // 访问OBD故障码数据（服务商）
	private int RIGHT_EVENT = 0x6003; // 访问车务提醒（服务商）
	private int RIGHT_VIOLATION = 0x6004; // 访问车辆违章（服务商）
	private int RIGHT_LOCATION = 0x6005; // 访问车辆实时位置（个人好友及服务商）
	private int RIGHT_TRIP = 0x6006; // 访问车辆行程（个人好友及服务商）
	private int RIGHT_FUEL = 0x6007; // 访问车辆油耗明细（服务商）
	private int RIGHT_DRIVESTAT = 0x6008; // 访问车辆驾驶习惯数据（服务商）
	private AppApplication app;
	private RequestQueue mQueue;
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_set_compet);
		app = (AppApplication) getApplication();
		mQueue = Volley.newRequestQueue(this);
		Intent intent = getIntent();
		friendId = intent.getIntExtra("friendId", 0);
		isService = intent.getBooleanExtra("isService", false);
		handler = new Handler();
		initView();
		

	}

	/**
	 * 初始化界面
	 */
	public void initView() {
		llytList = (LinearLayout) findViewById(R.id.llytList);
		chkOBDStandard = (CheckBox) findViewById(R.id.chkOBDStandard);
		chkOBDFault = (CheckBox) findViewById(R.id.chkOBDFault);
		chkNotice = (CheckBox) findViewById(R.id.chkNotice);
		chkViolation = (CheckBox) findViewById(R.id.chkViolation);
		chkLocation = (CheckBox) findViewById(R.id.chkLocation);
		chkTrip = (CheckBox) findViewById(R.id.chkTrip);
		chkFuel = (CheckBox) findViewById(R.id.chkFuel);
		chkDriving = (CheckBox) findViewById(R.id.chkDriving);

		setServiceMode(isService);
		getAuthorization();
		setAuthorization();
	}

	public void get(String url) {
		Listener listener = new Response.Listener<String>() {
			public void onResponse(String response) {
				try {
					JSONObject jsonObject = new JSONObject(response);
					JSONArray values = jsonObject.getJSONArray("rights");
					int code[] = {RIGHT_OBD_DATA,RIGHT_ODB_ERR,RIGHT_EVENT,RIGHT_VIOLATION,RIGHT_LOCATION,RIGHT_TRIP,RIGHT_FUEL,RIGHT_DRIVESTAT};
					final CheckBox[] cb = {chkOBDStandard,chkOBDFault,chkNotice,chkViolation,chkLocation,chkTrip,chkFuel,chkDriving};
					for(int i =0;i<values.length();i++){
						int authCode = values.getInt(i);
						for(int j=0;j<code.length;j++){
							if(authCode == code[j]){
								final int z = j;
								handler.post(new Runnable(){
									@Override
									public void run() {
										cb[z].setChecked(true);
									}
								});
								
							}
						}
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

	public void post(String url, Map map) {
		Listener listener = new Response.Listener<String>() {
			public void onResponse(String response) {
				System.out.println("url request " + response);
			}

		};
		ErrorListener errorListener = new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				System.out.println("url error " + error.getMessage());
			}
		};

		JSONObject jsonRequest = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		System.out.println(RIGHT_LOCATION);
		jsonArray.put(RIGHT_LOCATION);
		try {
			jsonRequest.put("rights", jsonArray);
		} catch (JSONException e) {
			System.out.println("JJJJJJ");
			e.printStackTrace();
		}
		Request request = new JsonObjectRequest(Method.POST, url, jsonRequest,
				listener, errorListener);

		mQueue.add(request);
		mQueue.start();
	}

	/**
	 * 获取有哪些权限
	 */
	public void getAuthorization() {
		String id = app.cust_id;
		String url = "http://api.bibibaba.cn/customer/" + friendId + "/friend/"
				+ id + "/rights?auth_code=" + app.auth_code;
		get(url);
	}

	/**
	 * 设置权限
	 */
	public void setAuthorization() {
		String id = app.cust_id;
		final String url = "http://api.bibibaba.cn/customer/" + id + "/friend/"
				+ friendId + "/rights?auth_code=" + app.auth_code;
		
		
		StringBuilder  authBuilder = new StringBuilder("[");
		
		int code[] = {RIGHT_OBD_DATA,RIGHT_ODB_ERR,RIGHT_EVENT,RIGHT_VIOLATION,RIGHT_LOCATION,RIGHT_TRIP,RIGHT_FUEL,RIGHT_DRIVESTAT};
		CheckBox[] cb = {chkOBDStandard,chkOBDFault,chkNotice,chkViolation,chkLocation,chkTrip,chkFuel,chkDriving};
		for(int i =0 ;i<cb.length;i++){
			if(cb[i].isChecked()){
				authBuilder.append(code[i]);
				authBuilder.append(",");
			}
		}
		authBuilder.append("]");
		
		String authString = authBuilder.toString();
		if(authString.lastIndexOf(",") == authString.length()-2){
			
		}
		authString = authString.replace(",]", "]");
		System.out.println(authString);
		put(url,authString);

	}

	// 设置是否是服务商界面
	public void setServiceMode(boolean isService) {
		if (!isService) {

			for (int i = 0; i < llytList.getChildCount(); i++) {
				View view = llytList.getChildAt(i);
				int id = view.getId();
				if (id != R.id.rlytLocation && id != R.id.rlytTrip
						&& id != R.id.viewLine) {
					view.setVisibility(View.GONE);
				}
			}
			// chkOBDStandard.setVisibility(View.GONE);
			// chkOBDFault.setVisibility(View.GONE);
			// chkNotice.setVisibility(View.GONE);
			// chkViolation.setVisibility(View.GONE);
			// chkFuel.setVisibility(View.GONE);
			// chkDriving.setVisibility(View.GONE);
		}

	}

	public void put(String url,String authCode) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("rights", authCode);
		JSONObject	jsonObject = new JSONObject(params);
		JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(
				Method.PUT, url, jsonObject,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						System.out.println("volley response " + response + "");
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						System.out.println("error response " + error.getMessage()
								+ "");
					}
				}) {
			@Override
			public Map<String, String> getHeaders() {
				HashMap<String, String> headers = new HashMap<String, String>();
				headers.put("Accept", "application/json");
				headers.put("Content-Type", "application/json; charset=UTF-8");

				return headers;
			}
		};
		mQueue.add(jsonRequest);
		mQueue.start();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_back:
			finish();
			break;
		case R.id.iv_add:
			setAuthorization();
			break;
		default:
			break;
		}

	}
}