package com.wise.setting;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import com.umeng.analytics.MobclickAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.ManageActivity;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.net.NetThread;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

/**
 * 注册验证码界面
 * 
 * @author Administrator
 * 
 */
public class CaptchaActivity extends Activity {
	private static final int get_captcha = 1;
	private static final int update_account = 2;
	private static final int reset_pwd = 3;

	EditText et_captcha, et_pwd, et_pwd_again;

	boolean isPhone = true;
	String account;
	String valid_code = "";
	/**
	 * 0 注册 ， 1 重置，2,第三方注册 ,3修改手机,4修改邮箱
	 */
	int mark = 0;
	String platform = "";
	boolean fastTrack = false;
	AppApplication app;
	boolean remove = false;
	boolean device_update = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		ManageActivity.getActivityInstance().addActivity(this);
		setContentView(R.layout.activity_captcha);
		app = (AppApplication) getApplication();
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		et_captcha = (EditText) findViewById(R.id.et_captcha);
		et_pwd = (EditText) findViewById(R.id.et_pwd);
		et_pwd_again = (EditText) findViewById(R.id.et_pwd_again);
		Button bt_Submit = (Button) findViewById(R.id.bt_Submit);
		bt_Submit.setOnClickListener(onClickListener);
		TextView tv_send_captcha = (TextView) findViewById(R.id.tv_send_captcha);
		tv_send_captcha.setOnClickListener(onClickListener);
		TextView tv_account = (TextView) findViewById(R.id.tv_account);
		Intent intent = getIntent();
		isPhone = intent.getBooleanExtra("isPhone", true);
		account = intent.getStringExtra("account");
		mark = intent.getIntExtra("mark", 0);
		platform = intent.getStringExtra("platform");
		fastTrack = intent.getBooleanExtra("fastTrack", false);
		remove = intent.getBooleanExtra("remove", false);
		device_update = intent.getBooleanExtra("device_update", false);

		tv_account.setText(account);
		GetCaptcha();
		if (mark == 0 || mark == 2) {
			bt_Submit.setText("下一步");
		} else if (mark == 1) {
			if (remove) {
				et_pwd.setVisibility(View.GONE);
				et_pwd_again.setVisibility(View.GONE);
				bt_Submit.setText("解除绑定");
			} else if (device_update) {
				et_pwd.setVisibility(View.GONE);
				et_pwd_again.setVisibility(View.GONE);
				bt_Submit.setText("修改终端");
			} else {
				bt_Submit.setText("重置密码");
			}
		} else if (mark == 3 || mark == 4) {
			bt_Submit.setText("修改");
			et_pwd_again.setVisibility(View.GONE);
		}
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.bt_Submit:
				if (mark == 3 || mark == 4) {
					SubmitPhone();
				} else {
					Submit();
				}
				break;
			case R.id.tv_send_captcha:
				// GetCaptcha();
				break;
			case R.id.iv_back:
				finish();
				break;
			}
		}
	};
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case get_captcha:
				jsonCaptcha(msg.obj.toString());
				break;
			case update_account:
				jsonPhone(msg.obj.toString());
				break;
			case reset_pwd:
				jsonReset(msg.obj.toString());
				break;
			}
		}
	};

	private void GetCaptcha() {
		String url;
		if (isPhone) {
			url = Constant.BaseUrl + "valid_code?mobile=" + account + "&type=1";
		} else {
			url = Constant.BaseUrl + "valid_code/email?email=" + account
					+ "&type=1";
		}
		new Thread(new NetThread.GetDataThread(handler, url, get_captcha))
				.start();
	}

	private void jsonCaptcha(String str) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			valid_code = jsonObject.getString("valid_code");
			System.out.println("valid_code = " + valid_code);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void SubmitPhone() {
		String captcha = et_captcha.getText().toString().trim();
		String pwd = et_pwd.getText().toString().trim();
		SharedPreferences preferences = getSharedPreferences(
				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		String sp_pwd = preferences.getString(Constant.sp_pwd, "");

		if (!sp_pwd.equals(GetSystem.getM5DEndo(pwd))) {
			Toast.makeText(CaptchaActivity.this, "密码错误，请重新输入",
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (!captcha.equals(valid_code)) {
			Toast.makeText(CaptchaActivity.this, "验证码错误，请重新输入",
					Toast.LENGTH_SHORT).show();
			return;
		}

		if (mark == 3) {
			String url = Constant.BaseUrl + "customer/" + app.cust_id
					+ "/field?auth_code=" + app.auth_code;
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("field_name", "mobile"));
			params.add(new BasicNameValuePair("field_type", "String"));
			params.add(new BasicNameValuePair("field_value", account));
			new Thread(new NetThread.putDataThread(handler, url, params,
					update_account)).start();
		} else {
			String url = Constant.BaseUrl + "customer/" + app.cust_id
					+ "/field?auth_code=" + app.auth_code;
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("field_name", "email"));
			params.add(new BasicNameValuePair("field_type", "String"));
			params.add(new BasicNameValuePair("field_value", account));
			new Thread(new NetThread.putDataThread(handler, url, params,
					update_account)).start();
		}
	}

	private void jsonReset(String str) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			if (jsonObject.getString("status_code").equals("0")) {
				setResult(2);
				finish();
			} else {
				Toast.makeText(CaptchaActivity.this, "修改失败", Toast.LENGTH_SHORT)
						.show();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void jsonPhone(String str) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			if (jsonObject.getString("status_code").equals("0")) {

				SharedPreferences preferences = getSharedPreferences(
						Constant.sharedPreferencesName, Context.MODE_PRIVATE);
				Editor editor = preferences.edit();
				editor.putString(Constant.sp_account, account);
				editor.commit();

				Intent intent = new Intent();
				intent.putExtra("isPhone", isPhone);
				intent.putExtra("account", account);
				setResult(2, intent);
				finish();

				// TODO
			} else {
				Toast.makeText(CaptchaActivity.this, "修改失败", Toast.LENGTH_SHORT)
						.show();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void Submit() {
		String captcha = et_captcha.getText().toString().trim();
		String pwd = et_pwd.getText().toString().trim();
		String pwdAgain = et_pwd_again.getText().toString().trim();
		if (!pwd.equals(pwdAgain)) {
			Toast.makeText(CaptchaActivity.this, "2次输入的密码不一致，请重新输入",
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (!captcha.equals(valid_code)) {
			Toast.makeText(CaptchaActivity.this, "验证码错误，请重新输入",
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (mark == 0 || mark == 2) {
			Intent intent = new Intent(CaptchaActivity.this,
					RegisterInfoActivity.class);
			intent.putExtra("pwd", pwd);
			intent.putExtra("account", account);
			intent.putExtra("isPhone", isPhone);
			intent.putExtra("platform", platform);
			intent.putExtra("fastTrack", fastTrack);
			startActivity(intent);
		} else if (mark == 1) {
			if (remove) {
				setResult(RegisterActivity.Result_Device_Remove);
				finish();
			} else if (device_update) {
				setResult(RegisterActivity.Result_Device_Update);
				finish();
			} else {
				// TODO 重置密码
				String url = Constant.BaseUrl
						+ "customer/password/reset?account=" + account
						+ "&password=" + GetSystem.getM5DEndo(pwd);
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				new Thread(new NetThread.putDataThread(handler, url, params,
						reset_pwd)).start();
			}
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