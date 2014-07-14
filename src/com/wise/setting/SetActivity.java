package com.wise.setting;

import org.json.JSONException;
import org.json.JSONObject;
import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.NetThread;
import pubclas.Variable;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qzone.QZone;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.wise.baba.R;
import com.wise.car.CarActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * 设置界面
 * @author Administrator
 *
 */
public class SetActivity extends Activity{
	
	private static final String TAG = "SetActivity";
	private static final int login_account = 1;
	private static final int get_customer = 2;
	
	TextView tv_login;
	ImageView iv_logo;
	Button bt_login_out;
	RequestQueue mQueue;
	Platform platformQQ;
    Platform platformSina;
    Platform platformWhat;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_set);
		mQueue = Volley.newRequestQueue(this);
		ShareSDK.initSDK(this);
        platformQQ = ShareSDK.getPlatform(SetActivity.this, QZone.NAME);
        platformSina = ShareSDK.getPlatform(SetActivity.this, SinaWeibo.NAME);
		tv_login = (TextView)findViewById(R.id.tv_login);
		iv_logo = (ImageView)findViewById(R.id.iv_logo);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		tv_login.setOnClickListener(onClickListener);
		TextView tv_car = (TextView)findViewById(R.id.tv_car);
		tv_car.setOnClickListener(onClickListener);
		bt_login_out = (Button)findViewById(R.id.bt_login_out);
		bt_login_out.setOnClickListener(onClickListener);
		getLoginType();
	}
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.tv_login:
				if(Variable.cust_id == null){
					startActivityForResult(new Intent(SetActivity.this, LoginActivity.class), 1);
				}else{
					startActivity(new Intent(SetActivity.this, AccountActivity.class));
				}
				break;
			case R.id.tv_car:
				startActivity(new Intent(SetActivity.this, CarActivity.class));
				break;
			case R.id.bt_login_out:
				SharedPreferences preferences = getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		        Editor editor = preferences.edit();
		        editor.putString(Constant.sp_pwd, "");
			    editor.putString(Constant.sp_account, "");
		        editor.commit();
		        Variable.cust_id = null ;
		        Variable.auth_code = null;
		        bt_login_out.setVisibility(View.GONE);
		        tv_login.setText("登录/注册");
		        iv_logo.setBackgroundResource(R.drawable.icon_add);
				break;
			}
		}
	};
	
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case login_account:
				jsonLogin(msg.obj.toString());
				break;
			case get_customer:
				jsonCustomer(msg.obj.toString());
				break;
			}
		}		
	};
	
	/**
	 * 获取登录方式
	 */
	private void getLoginType(){
		SharedPreferences preferences = getSharedPreferences(
                Constant.sharedPreferencesName, Context.MODE_PRIVATE);
        String sp_account = preferences.getString(Constant.sp_account, "");
        String sp_pwd = preferences.getString(Constant.sp_pwd, "");
    	//登录
    	String url = Constant.BaseUrl + "user_login?account=" + sp_account + "&password=" + sp_pwd;
		new Thread(new NetThread.GetDataThread(handler, url, login_account)).start();
   }
	
	private void jsonLogin(String str){
		try {
			JSONObject jsonObject = new JSONObject(str);
			if(jsonObject.getString("status_code").equals("0")){
				Variable.cust_id = jsonObject.getString("cust_id");
				Variable.auth_code = jsonObject.getString("auth_code");
				Variable.cust_name = jsonObject.getString("cust_name");
				GetCustomer();				
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void GetCustomer() {
		String url = Constant.BaseUrl + "customer/" + Variable.cust_id
				+ "?auth_code=" + Variable.auth_code;
		new Thread(new NetThread.GetDataThread(handler, url, get_customer))
				.start();
	}
	
	private void jsonCustomer(String str) {
		try {
			bt_login_out.setVisibility(View.VISIBLE);
			JSONObject jsonObject = new JSONObject(str);
			String mobile = jsonObject.getString("mobile");
			String email = jsonObject.getString("email");
			String password = jsonObject.getString("password");
			
			SharedPreferences preferences = getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
	        Editor editor = preferences.edit();
	        editor.putString(Constant.sp_pwd, password);			
			if(mobile.equals("")){
		        editor.putString(Constant.sp_account, email);
			}else{
				editor.putString(Constant.sp_account, mobile);
			}
	        editor.commit();
	        
	        tv_login.setText(jsonObject.getString("cust_name"));
	        String logo = jsonObject.getString("logo");
			mQueue.add(new ImageRequest(logo, new Response.Listener<Bitmap>() {
				@Override
				public void onResponse(Bitmap response) {
					iv_logo.setImageBitmap(response);
				}
			}, 0, 0, Config.RGB_565, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					error.printStackTrace();
				}
			}));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
		
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);		
		GetSystem.Log(TAG, "requestCode = " + ",resultCode= " + resultCode);
		if(resultCode == 1){
			System.out.println("刷新界面");
			GetCustomer();
		}
	}
}