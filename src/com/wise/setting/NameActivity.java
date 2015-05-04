package com.wise.setting;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import com.umeng.analytics.MobclickAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.net.NetThread;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class NameActivity extends Activity {
	private static final int update_name = 1;
	private static final int get_customer = 2;
	private static final int exist = 3;

	EditText et_name;
	AppApplication app;    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_name);
		app = (AppApplication)getApplication();
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		Button bt_sure = (Button) findViewById(R.id.bt_sure);
		bt_sure.setOnClickListener(onClickListener);
		String name = getIntent().getStringExtra("name");
		et_name = (EditText) findViewById(R.id.et_name);
		et_name.setText(name);
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.bt_sure:
				updateName();
				break;
			}
		}
	};
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case update_name:
				String url = Constant.BaseUrl + "customer/" + app.cust_id
						+ "?auth_code=" + app.auth_code;
				new Thread(new NetThread.GetDataThread(handler, url,
						get_customer)).start();
				break;
			case get_customer:
				jsonCustomer(msg.obj.toString());
				break;
			case exist:
				updateName(msg.obj.toString());
				break;
			}
		}
	};

	String name_1;

	private void updateName() {
		name_1 = et_name.getText().toString().trim();
		if (name_1.equals("")) {
			Toast.makeText(NameActivity.this, "昵称不能为空", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		String url = Constant.BaseUrl + "exists?query_type=5&value=" + name_1;
		new Thread(new NetThread.GetDataThread(handler, url, exist)).start();
	}

	private void updateName(String str) {
		try {
			JSONObject json = new JSONObject(str);
			if (!json.getBoolean("exist")) {
				Intent data = new Intent();
				data.putExtra("name", name_1);
				setResult(1, data);
				String url = Constant.BaseUrl + "customer/" + app.cust_id
						+ "/field?auth_code=" + app.auth_code;
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("field_name", "cust_name"));
				params.add(new BasicNameValuePair("field_type", "String"));
				params.add(new BasicNameValuePair("field_value", name_1));
				new Thread(new NetThread.putDataThread(handler, url, params,
						update_name)).start();
				finish();
			} else {
				Toast.makeText(NameActivity.this, "昵称已存在", Toast.LENGTH_SHORT)
						.show();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	/** 获取个人信息 **/
	private void jsonCustomer(String str) {
		SharedPreferences preferences1 = getSharedPreferences(
				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		Editor editor1 = preferences1.edit();
		editor1.putString(Constant.sp_customer + app.cust_id, str);
		editor1.commit();
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
