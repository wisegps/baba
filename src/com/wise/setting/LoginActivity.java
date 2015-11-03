package com.wise.setting;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import org.json.JSONObject;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qzone.QZone;
import com.umeng.analytics.MobclickAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.CollectionActivity;
import com.wise.baba.ManageActivity;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.biz.JsonData;
import com.wise.baba.net.NetThread;
import com.wise.notice.NoticeActivity;
import com.wise.remind.RemindListActivity;
import com.wise.violation.TrafficActivity;

/**
 * 登录界面
 * 
 * @author honesty
 * 
 */
public class LoginActivity extends Activity implements PlatformActionListener, TagAliasCallback {
	private static final String TAG = "LoginActivity";
	/** 账号密码登录 **/
	private final static int accountLogin = 1;
	/** 获取用户信息 **/
	private final static int getCustomer = 2;
	/** 获取车辆信息 **/
	private static final int getCarData = 3;
	/** 第三方登录授权成功 **/
	private final static int shareSdkAuthorize = 4;
	/** 第三方在服务器验证成功 **/
	private final static int authorizeLogin = 5;

	TextView tv_note;
	EditText et_account, et_pwd;
	Platform platformQQ;
	Platform platformSina;
	String platform;
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
		JPushInterface.init(getApplicationContext());// 推送初始化
		ShareSDK.initSDK(this);// 分享初始化
		tv_note = (TextView) findViewById(R.id.tv_note);
		et_account = (EditText) findViewById(R.id.et_account);
		et_account.addTextChangedListener(textWatcher);
		et_pwd = (EditText) findViewById(R.id.et_pwd);
		et_pwd.addTextChangedListener(textWatcher);
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
		platformQQ = ShareSDK.getPlatform(LoginActivity.this, QZone.NAME);// QQ平台
		platformSina = ShareSDK.getPlatform(LoginActivity.this, SinaWeibo.NAME);// 微博平台
		// 始终显示账号，如果有
		SharedPreferences preferences = getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		String sp_account = preferences.getString(Constant.sp_account, "");
		et_account.setText(sp_account);

		findViewById(R.id.btn_show).setOnClickListener(onClickListener);// 演示账号登录
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
				Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
				intent.putExtra("mark", 0);
				intent.putExtra("fastTrack", true);
				startActivity(intent);
				break;
			case R.id.tv_rest_pwd:
				Intent intent1 = new Intent(LoginActivity.this, RegisterActivity.class);
				intent1.putExtra("mark", 1);
				startActivity(intent1);
				break;
			case R.id.iv_qq:// QQ登录，结果在回调里
				platformQQ.setPlatformActionListener(LoginActivity.this);
				platformQQ.showUser(null);
				platform = "qq";
				break;
			case R.id.iv_sina:// 微博登录，结果在回调里
				platformSina.setPlatformActionListener(LoginActivity.this);
				platformSina.showUser(null);
				platformSina.SSOSetting(true);
				platform = "sina";
				break;
			case R.id.bt_login:
				accountLogin();
				break;
			case R.id.btn_show:// 演示登录
				app.isTest = true;
				accountLogin();
				break;
			}
		}
	};

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case shareSdkAuthorize:
				authorizeLogin();
				break;
			case accountLogin:
				jsonAccountLogin(msg.obj.toString());
				break;
			case getCustomer:
				jsonCustomer(msg.obj.toString());
				break;
			case authorizeLogin:
				jsonAuthorizeLogin(msg.obj.toString());
				break;
			case getCarData:
				jsonCarData(msg.obj.toString());
				break;
			}
		}
	};

	/** 账号密码登录 **/
	private void accountLogin() {
		if (GetSystem.isNetworkAvailable(LoginActivity.this)) {
			if (app.isTest) {
				account = "demo@bibibaba.cn";
				pwd = "demo123";
			} else {
				account = et_account.getText().toString().trim();
				pwd = et_pwd.getText().toString().trim();
				if (account.equals("") || pwd.equals("")) {
					Toast.makeText(LoginActivity.this, "请输入账号密码", Toast.LENGTH_SHORT).show();
					return;
				}
			}
			bt_login.setEnabled(false);
			bt_login.setText("登录中...");
			String url = Constant.BaseUrl + "user_login?account=" + account + "&password=" + GetSystem.getM5DEndo(pwd);
			Log.i("LoginActivity", url);
			new NetThread.GetDataThread(handler, url, accountLogin).start();
		} else {
			AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
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

	/** 解析登录结果 **/
	private void jsonAccountLogin(String str) {
		
		try {
			JSONObject jsonObject = new JSONObject(str);
			if (jsonObject.getString("status_code").equals("0")) {
				app.cust_id = jsonObject.getString("cust_id");
				app.auth_code = jsonObject.getString("auth_code");
				if (!app.isTest) {
					// 不是演示账号要保存账号密码
					SharedPreferences preferences = getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
					Editor editor = preferences.edit();
					editor.putString(Constant.sp_account, account);
					editor.putString(Constant.sp_pwd, GetSystem.getM5DEndo(pwd));
					editor.commit();
				}
				setJpush();
				getCustomer();
			} else {
				tv_note.setVisibility(View.VISIBLE);
				bt_login.setEnabled(true);
				bt_login.setText("登录");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 获取用户信息 **/
	private void getCustomer() {
		String url = Constant.BaseUrl + "customer/" + app.cust_id + "?auth_code=" + app.auth_code;
		
		new NetThread.GetDataThread(handler, url, getCustomer).start();
	}

	/** 解析个人信息 **/
	private void jsonCustomer(String str) {
		SharedPreferences preferences = getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		// 按sp_customer+id的格式保存，可以保存多个登录的信息
		Editor editor = preferences.edit();
		editor.putString(Constant.sp_customer + app.cust_id, str);
		editor.commit();
		try {
			JSONObject jsonObject = new JSONObject(str);
			if (jsonObject.opt("status_code") == null) {
				int cust_type = jsonObject.getInt("cust_type");
				app.cust_type = cust_type;
				getCarData();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 获取车辆信息 **/
	private void getCarData() {
		String url = Constant.BaseUrl + "customer/" + app.cust_id + "/vehicle?auth_code=" + app.auth_code;
		Log.i("LoginActivity", " 获取车辆信息: "+url);
		new Thread(new NetThread.GetDataThread(handler, url, getCarData)).start();
	}

	/** 解析车辆信息 **/
	private void jsonCarData(String str) {
		app.carDatas.clear();
		app.carDatas.addAll(JsonData.jsonCarInfo(str));
		// 发广播
		Intent intent = new Intent(Constant.A_Login);
		sendBroadcast(intent);
		// 判断进入那个页面
		getActivityState(LoginActivity.this.getIntent());
		setResult(1);
		finish();
	}
	
	
	
	
	
	@Override
	public void finish() {
		super.finish();
//		bt_login.setText("登录");
//		bt_login.setEnabled(true);
	}

	public static final int SMS = 1;// 传递信息页面跳转类型
	public static final int COLLCETION = 2;// 收藏
	public static final int TRAFFIC = 3;// 违章
	public static final int REMIND = 4;// 提醒
	// 页面跳转方法，根据登录前传过来的跳转类型进行相应界面的跳转
	// 如果未登录点击通知，收藏等，需要先跳转到登录页面，登录后在跳转到对应的界面
	private void getActivityState(Intent i) {
		int state = i.getIntExtra("ActivityState", 0);
		switch (state) {
		case SMS:
			startActivity(new Intent(LoginActivity.this, NoticeActivity.class));
			break;
		case COLLCETION:
			startActivity(new Intent(LoginActivity.this, CollectionActivity.class));
			break;
		case REMIND:
			startActivity(new Intent(LoginActivity.this, RemindListActivity.class));
			break;
		case TRAFFIC:
			Intent intent = new Intent(LoginActivity.this, TrafficActivity.class);
			intent.putExtra("isService", false);
			startActivity(intent);
			break;
		}
	}

	/** 第三方账户在服务器验证 **/
	private void authorizeLogin() {
		if (platform.equals("qq")) {
			Platform platformQQ = ShareSDK.getPlatform(LoginActivity.this, QZone.NAME);
			String login_id = platformQQ.getDb().getUserId();
			String cust_name = platformQQ.getDb().getUserName();
			String logo = platformQQ.getDb().getUserIcon();
			try {
				String url = Constant.BaseUrl + "sso_login?login_id=" + login_id + "&cust_name=" + URLEncoder.encode(cust_name, "UTF-8") + "&provice="
						+ URLEncoder.encode(app.Province, "UTF-8") + "&city=" + URLEncoder.encode(app.City, "UTF-8") + "&logo=" + logo + "&remark=";
				new NetThread.GetDataThread(handler, url, authorizeLogin).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (platform.equals("sina")) {
			Platform platformSina = ShareSDK.getPlatform(LoginActivity.this, SinaWeibo.NAME);
			String login_id = platformSina.getDb().getUserId();
			String cust_name = platformSina.getDb().getUserName();
			String logo = platformSina.getDb().getUserIcon();
			try {
				String url = Constant.BaseUrl + "sso_login?login_id=" + login_id + "&cust_name=" + URLEncoder.encode(cust_name, "UTF-8")
						+ "&provice=&city=&logo=" + logo + "&remark=";
				new Thread(new NetThread.GetDataThread(handler, url, authorizeLogin)).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/** 解析第三方在服务器上返回结果 **/
	private void jsonAuthorizeLogin(String str) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			if (jsonObject.getString("status_code").equals("1")) {// 需要绑定账号
				new AlertDialog.Builder(LoginActivity.this).setTitle("提示").setMessage("如果您的账号已注册，请绑定，没有请注册")
						.setPositiveButton("去绑定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// 绑定
								Intent intent = new Intent(LoginActivity.this, BindActivity.class);
								intent.putExtra("platform", platform);
								startActivityForResult(intent, 1);
							}
						}).setNegativeButton("去注册", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {// 注册
								Intent intent1 = new Intent(LoginActivity.this, RegisterActivity.class);
								intent1.putExtra("mark", 2);
								intent1.putExtra("platform", platform);
								intent1.putExtra("fastTrack", true);
								startActivity(intent1);
							}
						}).show();
			} else {
				Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
				app.cust_id = jsonObject.getString("cust_id");
				app.auth_code = jsonObject.getString("auth_code");
				JPushInterface.resumePush(getApplicationContext());
				setJpush();
				getCustomer();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCancel(Platform arg0, int arg1) {
	}

	@Override
	public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {// 第三方登录成功
		Message message = new Message();
		message.what = shareSdkAuthorize;
		handler.sendMessage(message);
		SharedPreferences preferences = getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(Constant.platform, platform);// 记下登录的平台，欢迎界面自动登录用到
		editor.commit();
	}

	@Override
	public void onError(Platform arg0, int arg1, Throwable arg2) {// 第三方登录失败
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1) {
			setResult(1);
			finish();
		}
	}

	/** 设置推送 **/
	private void setJpush() {
		Set<String> tagSet = new LinkedHashSet<String>();
		tagSet.add(app.cust_id);
		// 调用JPush API设置Tag
		JPushInterface.setAliasAndTags(getApplicationContext(), null, tagSet, this);
	}

	@Override
	public void gotResult(int arg0, String arg1, Set<String> arg2) {
		// 设置推送成功的标志
		GetSystem.myLog(TAG, "arg0 = " + arg0 + " , arg1 = " + arg1);
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

	/** 在文本框输入内容时隐藏提示信息 **/
	TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			tv_note.setVisibility(View.INVISIBLE);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};
}