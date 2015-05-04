package com.wise.setting;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qzone.QZone;
import com.umeng.analytics.MobclickAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.net.NetThread;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class BindActivity extends Activity{
	
	private static final int login_account = 1;
	private static final int bind = 2;
	
	EditText et_account,et_pwd;
	String account,pwd,platform;
	AppApplication app;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_bind);
		app = (AppApplication)getApplication();
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		Button bt_bind = (Button)findViewById(R.id.bt_bind);
		bt_bind.setOnClickListener(onClickListener);
		et_account = (EditText)findViewById(R.id.et_account);
		et_pwd = (EditText)findViewById(R.id.et_pwd);
		platform = getIntent().getStringExtra("platform");
	}
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.bt_bind:
				Login();
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

			case bind:
				setResult(0);
				finish();				
				break;
			}
		}		
	};
	private void Login(){
		account = et_account.getText().toString().trim();
		pwd = et_pwd.getText().toString().trim();
		if(account.equals("") || pwd.equals("")){
			Toast.makeText(BindActivity.this, "请输入账号密码", Toast.LENGTH_SHORT).show();
			return;
		}
		String url = Constant.BaseUrl + "user_login?account=" + account + "&password=" + GetSystem.getM5DEndo(pwd);
		new Thread(new NetThread.GetDataThread(handler, url, login_account)).start();
	}
	private void jsonLogin(String str){
		try {
			JSONObject jsonObject = new JSONObject(str);
			if(jsonObject.getString("status_code").equals("0")){
				app.cust_id = jsonObject.getString("cust_id");
				app.auth_code = jsonObject.getString("auth_code");
				//TODO 保存账号密码
				SharedPreferences preferences = getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		        Editor editor = preferences.edit();
		        editor.putString(Constant.sp_account, account);
		        editor.putString(Constant.sp_pwd, GetSystem.getM5DEndo(pwd));
		        editor.commit();
				if(platform.equals("qq")){
					Platform platformQQ = ShareSDK.getPlatform(BindActivity.this, QZone.NAME);
					String url = Constant.BaseUrl + "customer/" + app.cust_id + 
							"/bind_qq?auth_code=" + app.auth_code;
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("qq_login_id", platformQQ.getDb().getUserId()));
					new Thread(new NetThread.putDataThread(handler, url, params, bind)).start();
				}else{
					Platform platformSina = ShareSDK.getPlatform(BindActivity.this, SinaWeibo.NAME);
					String url = Constant.BaseUrl + "customer/" + app.cust_id + 
							"/bind_sina?auth_code=" + app.auth_code;
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("sina_login_id", platformSina.getDb().getUserId()));
					new Thread(new NetThread.putDataThread(handler, url, params, bind)).start();
				}				
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}