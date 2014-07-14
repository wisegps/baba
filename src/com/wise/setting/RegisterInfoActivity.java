package com.wise.setting;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.NetThread;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qzone.QZone;

import com.wise.baba.AppApplication;
import com.wise.baba.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

/**
 * 注册信息界面
 * 
 * @author Administrator
 * 
 */
public class RegisterInfoActivity extends Activity {
	private static final int save = 1;
	
	private String[] items = { "销售", "售后", "保险", "理赔", "代办", "维修", "保养" };
	LinearLayout ll_models, ll_type;
	Spinner s_type,s_birth;
	boolean isPhone = true;
	String pwd = "";
	String account = "";
	String cust_type = "0";
	String sex = "0";
	String platform = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		AppApplication.getActivityInstance().addActivity(this);
		setContentView(R.layout.activity_register_info);
		Button bt_enter = (Button) findViewById(R.id.bt_enter);
		bt_enter.setOnClickListener(onClickListener);
		RadioGroup rg_identity = (RadioGroup) findViewById(R.id.rg_identity);
		rg_identity.setOnCheckedChangeListener(onCheckedChangeListener);
		RadioGroup rg_sex = (RadioGroup) findViewById(R.id.rg_sex);
		rg_sex.setOnCheckedChangeListener(onCheckedChangeListener);
		ll_models = (LinearLayout) findViewById(R.id.ll_models);
		ll_type = (LinearLayout) findViewById(R.id.ll_type);
		s_type = (Spinner) findViewById(R.id.s_type);
		s_birth = (Spinner) findViewById(R.id.s_birth);
		ArrayAdapter<String> type = new ArrayAdapter<String>(
				RegisterInfoActivity.this,
				android.R.layout.simple_spinner_dropdown_item, items);
		s_type.setAdapter(type);
		getYear();
		Intent intent = getIntent();
		isPhone = intent.getBooleanExtra("isPhone", true);
		pwd = intent.getStringExtra("pwd");
		account = intent.getStringExtra("account");
		platform = intent.getStringExtra("platform");
	}
	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.bt_enter:
				Save();
				break;
			}
		}
	};
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case save:
				jsonSave(msg.obj.toString());
				break;
			}
		}		
	};
	OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (group.getCheckedRadioButtonId()) {
			case R.id.rb_no_car:
				ll_models.setVisibility(View.GONE);
				ll_type.setVisibility(View.GONE);
				cust_type = "0";
				break;
			case R.id.rb_car:
				ll_models.setVisibility(View.VISIBLE);
				ll_type.setVisibility(View.GONE);
				cust_type = "1";
				break;
			case R.id.rb_service:
				ll_models.setVisibility(View.GONE);
				ll_type.setVisibility(View.VISIBLE);
				cust_type = "2";
				break;
			}
		}
	};
	OnCheckedChangeListener onSexChangeListener = new OnCheckedChangeListener() {		
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (group.getCheckedRadioButtonId()) {
			case R.id.rb_woman:
				sex = "1";
				break;
			case R.id.rb_man:
				sex = "0";
				break;
			}
		}
	};

	private void getYear() {
		Time time = new Time();
		time.setToNow();
		int year = time.year;
		List<Integer> years = new ArrayList<Integer>();
		for (int i = 0; i < 100; i++) {
			years.add(year - i);
		}
		ArrayAdapter<Integer> arrayAdapter = new ArrayAdapter<Integer>(
				RegisterInfoActivity.this,
				android.R.layout.simple_spinner_item, years);
		s_birth.setAdapter(arrayAdapter);
	}
	private String GetBirth(){
		Time time = new Time();
		time.setToNow();
		int year = time.year;
		return (year - s_birth.getSelectedItemPosition()) +"-01-01";
	}
	private void Save(){
		String url = Constant.BaseUrl + "customer/register?auth_code=127a154df2d7850c4232542b4faa2c3d";
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        if(isPhone){
            params.add(new BasicNameValuePair("mobile", account));
            params.add(new BasicNameValuePair("email", ""));
        }else{
        	params.add(new BasicNameValuePair("mobile", ""));
            params.add(new BasicNameValuePair("email", account));
        }
        params.add(new BasicNameValuePair("password", GetSystem.getM5DEndo(pwd)));
        params.add(new BasicNameValuePair("cust_type", cust_type));
        params.add(new BasicNameValuePair("sex", sex));
        params.add(new BasicNameValuePair("birth", GetBirth()));
        params.add(new BasicNameValuePair("province", ""));
        params.add(new BasicNameValuePair("city", ""));
        params.add(new BasicNameValuePair("car_brand", ""));
        params.add(new BasicNameValuePair("car_series", ""));
        params.add(new BasicNameValuePair("service_type", String.valueOf(s_type.getSelectedItemPosition())));
        if(platform == null || platform.equals("")){
            params.add(new BasicNameValuePair("cust_name", ""));
            params.add(new BasicNameValuePair("qq_login_id", ""));
            params.add(new BasicNameValuePair("sina_login_id", ""));
            params.add(new BasicNameValuePair("logo", ""));
        }else{
        	if(platform.equals("qq")){
        		Platform platformQQ = ShareSDK.getPlatform(RegisterInfoActivity.this, QZone.NAME);
    			params.add(new BasicNameValuePair("qq_login_id", platformQQ.getDb().getUserId()));
                params.add(new BasicNameValuePair("sina_login_id", ""));
                params.add(new BasicNameValuePair("cust_name", platformQQ.getDb().getUserName()));
                params.add(new BasicNameValuePair("logo", platformQQ.getDb().getUserIcon()));
        	}else{
        		Platform platformSina = ShareSDK.getPlatform(RegisterInfoActivity.this, SinaWeibo.NAME);
    			params.add(new BasicNameValuePair("qq_login_id", ""));
                params.add(new BasicNameValuePair("sina_login_id", platformSina.getDb().getUserId()));
                params.add(new BasicNameValuePair("cust_name", platformSina.getDb().getUserName()));
                params.add(new BasicNameValuePair("logo", platformSina.getDb().getUserIcon()));
        	}
        }
        params.add(new BasicNameValuePair("remark", ""));
        new Thread(new NetThread.postDataThread(handler, url, params, save)).start();
	}
	private void jsonSave(String str){
		System.out.println("str = " + str);
		try {
			JSONObject jsonObject = new JSONObject(str);
			String status_code = jsonObject.getString("status_code");
			if(status_code.equals("0")){
				Toast.makeText(RegisterInfoActivity.this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
				AppApplication.getActivityInstance().exit();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}		
	}
}