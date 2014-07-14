package com.wise.setting;

import java.net.URLEncoder;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.wise.baba.R;
import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.NetThread;
import pubclas.Variable;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qzone.QZone;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 登录界面
 * @author honesty
 *
 */
public class LoginActivity extends Activity implements PlatformActionListener{
	private final static int login_account = 1;
	private final static int login = 4;
	private final static int login_sso = 5;
	EditText et_account,et_pwd;
	Platform platformQQ;
	Platform platformSina;
	String platform;	

	String account;
	String pwd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);
		ShareSDK.initSDK(this);
		et_account = (EditText)findViewById(R.id.et_account);
		et_pwd = (EditText)findViewById(R.id.et_pwd);
		Button bt_login = (Button)findViewById(R.id.bt_login);
		bt_login.setOnClickListener(onClickListener);
		TextView tv_register = (TextView)findViewById(R.id.tv_register);
		tv_register.setOnClickListener(onClickListener);
		TextView tv_rest_pwd = (TextView)findViewById(R.id.tv_rest_pwd);
		tv_rest_pwd.setOnClickListener(onClickListener);
		ImageView iv_qq = (ImageView)findViewById(R.id.iv_qq);
		iv_qq.setOnClickListener(onClickListener);
		ImageView iv_sina = (ImageView)findViewById(R.id.iv_sina);
		iv_sina.setOnClickListener(onClickListener);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		platformQQ = ShareSDK.getPlatform(LoginActivity.this, QZone.NAME);
		platformSina = ShareSDK.getPlatform(LoginActivity.this, SinaWeibo.NAME);
	}
	OnClickListener onClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.tv_register:
				Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
				intent.putExtra("mark", 0);
				startActivity(intent);
				break;
			case R.id.tv_rest_pwd:
				Intent intent1 = new Intent(LoginActivity.this, RegisterActivity.class);
				intent1.putExtra("mark", 1);
				startActivity(intent1);
				break;
			case R.id.iv_qq:
				platformQQ.setPlatformActionListener(LoginActivity.this);
                platformQQ.showUser(null);
                platform = "qq";
				break;
			case R.id.iv_sina:
				platformSina.setPlatformActionListener(LoginActivity.this);
				platformSina.showUser(null);
				platformSina.SSOSetting(true);
                platform = "sina";
				break;
			case R.id.bt_login:
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
			case login:
				SQLogin();
				break;
			case login_account:
				jsonLogin(msg.obj.toString());
				break;
			case login_sso:
				jsonSQLogin(msg.obj.toString());
				break;
			}
		}		
	};
	private void Login(){
		account = et_account.getText().toString().trim();
		pwd = et_pwd.getText().toString().trim();
		if(account.equals("") || pwd.equals("")){
			Toast.makeText(LoginActivity.this, "请输入账号密码", Toast.LENGTH_SHORT).show();
			return;
		}
		String url = Constant.BaseUrl + "user_login?account=" + account + "&password=" + GetSystem.getM5DEndo(pwd);
		new Thread(new NetThread.GetDataThread(handler, url, login_account)).start();
	}
	private void jsonLogin(String str){
		try {
			JSONObject jsonObject = new JSONObject(str);
			if(jsonObject.getString("status_code").equals("0")){
				Variable.cust_id = jsonObject.getString("cust_id");
				Variable.auth_code = jsonObject.getString("auth_code");
				Variable.cust_name = jsonObject.getString("cust_name");
				//保存账号密码
				SharedPreferences preferences = getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		        Editor editor = preferences.edit();
		        editor.putString(Constant.sp_account, account);
		        editor.putString(Constant.sp_pwd, GetSystem.getM5DEndo(pwd));
		        editor.commit();
		        setResult(1);
				finish();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void SQLogin(){
		if(platform.equals("qq")){
			Platform platformQQ = ShareSDK.getPlatform(LoginActivity.this, QZone.NAME);
			String login_id = platformQQ.getDb().getUserId();
			String cust_name = platformQQ.getDb().getUserName();
			String logo = platformQQ.getDb().getUserIcon();
			try {
				String url = Constant.BaseUrl + "sso_login?login_id=" + login_id + 
						"&cust_name=" + URLEncoder.encode(cust_name, "UTF-8") + "&provice=&city=&logo=" + 
						logo + "&remark=";
				new Thread(new NetThread.GetDataThread(handler, url, login_sso)).start();
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
	}
	private void jsonSQLogin(String str){
		System.out.println("str = " + str);
		try {
			JSONObject jsonObject = new JSONObject(str);
			if(jsonObject.getString("status_code").equals("1")){//需要绑定账号
				
				new AlertDialog.Builder(LoginActivity.this)    
				.setTitle("提示")  
				.setMessage("如果您的账号已注册，请绑定，没有请注册")  
				.setPositiveButton("去绑定", new DialogInterface.OnClickListener() {						
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//TODO 绑定
						Intent intent = new Intent(LoginActivity.this, BindActivity.class);
						intent.putExtra("platform", platform);
						startActivityForResult(intent, 1);
					}
				}).setNegativeButton("去注册", new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {//TODO 注册
						Intent intent1 = new Intent(LoginActivity.this, RegisterActivity.class);
						intent1.putExtra("mark", 2);
						intent1.putExtra("platform", platform);
						startActivity(intent1);
					}
				}).show();				
			}else{
				Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
				//TODO 登录成功
				Variable.cust_id = jsonObject.getString("cust_id");
				Variable.auth_code = jsonObject.getString("auth_code");
				Variable.cust_name = jsonObject.getString("cust_name");
				setResult(1);
				finish();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onCancel(Platform arg0, int arg1) {}
	@Override
	public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {
		System.out.println("第三方登录成功");
		Message message = new Message();
        message.what = login;
        handler.sendMessage(message);
        SharedPreferences preferences = getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString(Constant.platform, platform);
        editor.commit();
	}
	@Override
	public void onError(Platform arg0, int arg1, Throwable arg2) {}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 1){
			setResult(1);
			finish();
		}
	}
}