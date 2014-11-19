package com.wise.setting;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.umeng.analytics.MobclickAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.CollectionActivity;
import com.wise.baba.ManageActivity;
import com.wise.baba.MoreActivity;
import com.wise.baba.R;
import com.wise.notice.NoticeActivity;
import com.wise.remind.RemindListActivity;
import com.wise.violation.TrafficActivity;
import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.JsonData;
import pubclas.NetThread;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 登录界面
 * 
 * @author honesty
 * 
 */
public class LoginActivity extends Activity implements PlatformActionListener,
		TagAliasCallback {
	private static final String TAG = "LoginActivity";

	private final static int login_account = 1;
	private static final int get_data = 3;
	private final static int login = 4;
	private final static int login_sso = 5;

	TextView tv_note;
	EditText et_account, et_pwd;
	Platform platformQQ;
	Platform platformSina;
	String platform;
	ProgressBar progressBar;
	Button bt_login;
	String account;
	String pwd;

	AppApplication app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ManageActivity.getActivityInstance().addActivity(LoginActivity.this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);
		app = (AppApplication) getApplication();
		JPushInterface.init(getApplicationContext());
		ShareSDK.initSDK(this);
		tv_note = (TextView) findViewById(R.id.tv_note);
		et_account = (EditText) findViewById(R.id.et_account);
		et_pwd = (EditText) findViewById(R.id.et_pwd);
		bt_login = (Button) findViewById(R.id.bt_login);
		bt_login.setOnClickListener(onClickListener);
		TextView tv_register = (TextView) findViewById(R.id.tv_register);
		tv_register.setOnClickListener(onClickListener);
		TextView tv_rest_pwd = (TextView) findViewById(R.id.tv_rest_pwd);
		tv_rest_pwd.setOnClickListener(onClickListener);
		ImageView iv_qq = (ImageView) findViewById(R.id.iv_qq);
		iv_qq.setOnClickListener(onClickListener);
		ImageView iv_sina = (ImageView) findViewById(R.id.iv_sina);
		iv_sina.setOnClickListener(onClickListener);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		platformQQ = ShareSDK.getPlatform(LoginActivity.this, QZone.NAME);
		platformSina = ShareSDK.getPlatform(LoginActivity.this, SinaWeibo.NAME);
		SharedPreferences preferences = getSharedPreferences(
				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		String sp_account = preferences.getString(Constant.sp_account, "");
		et_account.setText(sp_account);

		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setVisibility(View.GONE);

		findViewById(R.id.btn_show).setOnClickListener(onClickListener);
		app.isTest = false;
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.tv_register:
				Intent intent = new Intent(LoginActivity.this,
						RegisterActivity.class);
				intent.putExtra("mark", 0);
				intent.putExtra("fastTrack", true);
				startActivity(intent);
				break;
			case R.id.tv_rest_pwd:
				Intent intent1 = new Intent(LoginActivity.this,
						RegisterActivity.class);
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

			case R.id.btn_show:
				app.isTest = true;
				Login();
				break;
			}
		}
	};

	Handler handler = new Handler() {
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
			case get_data:
				app.carDatas.clear();
				app.carDatas.addAll(JsonData.jsonCarInfo(msg.obj.toString()));
				// 发广播
				Intent intent = new Intent(Constant.A_RefreshHomeCar);
				sendBroadcast(intent);
				// TODO 做出相应的跳转
				Intent intent1 = LoginActivity.this.getIntent();
				getActivityState(intent1);
				break;
			}
		}
	};

	private void Login() {
		if(GetSystem.isNetworkAvailable(LoginActivity.this)){
			if (app.isTest) {
				account = "demo@bibibaba.cn";
				pwd = "demo123";
			} else {
				account = et_account.getText().toString().trim();
				pwd = et_pwd.getText().toString().trim();
				if (account.equals("") || pwd.equals("")) {
					Toast.makeText(LoginActivity.this, "请输入账号密码",
							Toast.LENGTH_SHORT).show();
					return;
				}
				bt_login.setEnabled(false);
				progressBar.setVisibility(View.VISIBLE);
			}
			String url = Constant.BaseUrl + "user_login?account=" + account
					+ "&password=" + GetSystem.getM5DEndo(pwd);
			new NetThread.GetDataThread(handler, url, login_account).start();
		}else{
			AlertDialog.Builder dialog =new AlertDialog.Builder(LoginActivity.this);
			dialog.setTitle("提示");  
			dialog.setMessage("当前网络未连接"); 
			dialog.setPositiveButton("去打开", new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					   startActivity(new Intent("android.settings.WIFI_SETTINGS"));  
				}
			});
			dialog.setNegativeButton("取消", null);
			dialog.show();
		}		
	}

	private void jsonLogin(String str) {
		progressBar.setVisibility(View.GONE);
		bt_login.setEnabled(true);
		try {
			JSONObject jsonObject = new JSONObject(str);
			if (jsonObject.getString("status_code").equals("0")) {
				app.cust_id = jsonObject.getString("cust_id");
				app.auth_code = jsonObject.getString("auth_code");
				if (!app.isTest) {
					// 保存账号密码
					SharedPreferences preferences = getSharedPreferences(
							Constant.sharedPreferencesName,
							Context.MODE_PRIVATE);
					Editor editor = preferences.edit();
					editor.putString(Constant.sp_account, account);
					editor.putString(Constant.sp_pwd, GetSystem.getM5DEndo(pwd));
					editor.commit();
				}
				setJpush();
				getData();
				setResult(1);
				finish();

			} else {
				tv_note.setVisibility(View.VISIBLE);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	// 页面跳转方法，根据登录前传过来的跳转类型进行相应界面的跳转
	private void getActivityState(Intent i) {
		int state = i.getIntExtra("ActivityState", 0);
		switch (state) {
		case MoreActivity.SMS:
			startActivity(new Intent(LoginActivity.this, NoticeActivity.class));
			break;
		case MoreActivity.COLLCETION:
			startActivity(new Intent(LoginActivity.this,
					CollectionActivity.class));
			break;
		case MoreActivity.REMIND:
			startActivity(new Intent(LoginActivity.this,
					RemindListActivity.class));
			break;
		case MoreActivity.TRAFFIC:
			startActivity(new Intent(LoginActivity.this, TrafficActivity.class));
			break;
		}
	}

	private void SQLogin() {
		if (platform.equals("qq")) {
			Platform platformQQ = ShareSDK.getPlatform(LoginActivity.this,
					QZone.NAME);
			String login_id = platformQQ.getDb().getUserId();
			String cust_name = platformQQ.getDb().getUserName();
			String logo = platformQQ.getDb().getUserIcon();
			try {
				String url = Constant.BaseUrl + "sso_login?login_id="
						+ login_id + "&cust_name="
						+ URLEncoder.encode(cust_name, "UTF-8") + "&provice="
						+ URLEncoder.encode(app.Province, "UTF-8") + "&city="
						+ URLEncoder.encode(app.City, "UTF-8") + "&logo="
						+ logo + "&remark=";
				new NetThread.GetDataThread(handler, url, login_sso).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (platform.equals("sina")) {
			Platform platformSina = ShareSDK.getPlatform(LoginActivity.this,
					SinaWeibo.NAME);
			String login_id = platformSina.getDb().getUserId();
			String cust_name = platformSina.getDb().getUserName();
			String logo = platformSina.getDb().getUserIcon();
			try {
				String url = Constant.BaseUrl + "sso_login?login_id="
						+ login_id + "&cust_name="
						+ URLEncoder.encode(cust_name, "UTF-8")
						+ "&provice=&city=&logo=" + logo + "&remark=";
				new Thread(new NetThread.GetDataThread(handler, url, login_sso))
						.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void jsonSQLogin(String str) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			if (jsonObject.getString("status_code").equals("1")) {// 需要绑定账号
				new AlertDialog.Builder(LoginActivity.this)
						.setTitle("提示")
						.setMessage("如果您的账号已注册，请绑定，没有请注册")
						.setPositiveButton("去绑定",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// 绑定
										Intent intent = new Intent(
												LoginActivity.this,
												BindActivity.class);
										intent.putExtra("platform", platform);
										startActivityForResult(intent, 1);
									}
								})
						.setNegativeButton("去注册",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {// 注册
										Intent intent1 = new Intent(
												LoginActivity.this,
												RegisterActivity.class);
										intent1.putExtra("mark", 2);
										intent1.putExtra("platform", platform);
										intent1.putExtra("fastTrack", true);
										startActivity(intent1);
									}
								}).show();
			} else {
				Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT)
						.show();
				// TODO 登录成功
				app.cust_id = jsonObject.getString("cust_id");
				app.auth_code = jsonObject.getString("auth_code");
				JPushInterface.resumePush(getApplicationContext());
				setJpush();
				getData();
				setResult(1);
				finish();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/** 获取车辆信息 **/
	private void getData() {
		String url = Constant.BaseUrl + "customer/" + app.cust_id
				+ "/vehicle?auth_code=" + app.auth_code;
		new Thread(new NetThread.GetDataThread(handler, url, get_data)).start();
	}

	@Override
	public void onCancel(Platform arg0, int arg1) {
	}

	@Override
	public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {
		Message message = new Message();
		message.what = login;
		handler.sendMessage(message);
		SharedPreferences preferences = getSharedPreferences(
				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(Constant.platform, platform);
		editor.commit();
	}

	@Override
	public void onError(Platform arg0, int arg1, Throwable arg2) {
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1) {
			setResult(1);
			finish();
		}
	}

	@Override
	public void gotResult(int arg0, String arg1, Set<String> arg2) {
		GetSystem.myLog(TAG, "arg0 = " + arg0 + " , arg1 = " + arg1);
	}

	private void setJpush() {
		GetSystem.myLog(TAG, "设置推送");
		Set<String> tagSet = new LinkedHashSet<String>();
		tagSet.add(app.cust_id);
		// 调用JPush API设置Tag
		JPushInterface.setAliasAndTags(getApplicationContext(), null, tagSet,
				this);
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