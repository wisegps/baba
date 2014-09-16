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
import pubclas.Variable;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qzone.QZone;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.umeng.analytics.MobclickAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.car.CarAddActivity;
import com.wise.car.ModelsActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

/**
 * 注册信息界面
 * 
 * @author Administrator
 * 
 */
public class RegisterInfoActivity extends Activity {
	
	private static String TAG = "RegisterInfoActivity";
	private static final int save = 1;
	private static final int get_customer = 2;
	
	private String[] items = { "销售", "售后", "保险", "理赔", "代办", "维修", "保养" };
	LinearLayout ll_models, ll_type;
	Spinner s_type,s_birth;
	EditText et_cust_name;
	TextView tv_model;
	boolean isPhone = true;
	boolean fastTrack = false;
	String pwd = "";
	String account = "";
	String cust_type = "0";
	String sex = "0";
	String platform = "";	
	String device_id = "";	

	String carBrank = "";
	String carBrankId = "";
	String carSeries = "";
	String carSeriesId = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		AppApplication.getActivityInstance().addActivity(this);
		setContentView(R.layout.activity_register_info);
		et_cust_name = (EditText)findViewById(R.id.et_cust_name);
		tv_model = (TextView)findViewById(R.id.tv_model);
		tv_model.setOnClickListener(onClickListener);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
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
				android.R.layout.simple_spinner_item, items);
		type.setDropDownViewResource(R.layout.drop_down_item);
		s_type.setAdapter(type);
		getYear();
		Intent intent = getIntent();
		isPhone = intent.getBooleanExtra("isPhone", true);
		fastTrack = intent.getBooleanExtra("fastTrack", false);
		pwd = intent.getStringExtra("pwd");
		account = intent.getStringExtra("account");
		platform = intent.getStringExtra("platform");
		device_id = intent.getStringExtra("device_id");
		
		if(platform == null || platform.equals("")){
			
		}else{
			if(platform.equals("qq")){
        		Platform platformQQ = ShareSDK.getPlatform(RegisterInfoActivity.this, QZone.NAME);
        		et_cust_name.setText(platformQQ.getDb().getUserName());
        	}else{
        		Platform platformSina = ShareSDK.getPlatform(RegisterInfoActivity.this, SinaWeibo.NAME);
        		et_cust_name.setText(platformSina.getDb().getUserName());
        	}
		}
	}
	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.bt_enter:
				Save();
				break;
			case R.id.iv_back:
				finish();
				break;
			case R.id.tv_model:
				Intent intent = new Intent(RegisterInfoActivity.this, ModelsActivity.class);
				intent.putExtra("isNeedType", false);
				startActivityForResult(intent, 2);
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
			case get_customer:
				jsonCustomer(msg.obj.toString());
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
		arrayAdapter.setDropDownViewResource(R.layout.drop_down_item);
		s_birth.setAdapter(arrayAdapter);
	}
	private String GetBirth(){
		Time time = new Time();
		time.setToNow();
		int year = time.year;
		return (year - s_birth.getSelectedItemPosition()) +"-01-01";
	}
	private void Save(){
		String cust_name = et_cust_name.getText().toString().trim();
		if(cust_name.equals("")){
			Toast.makeText(RegisterInfoActivity.this, "昵称不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
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
        params.add(new BasicNameValuePair("province", Variable.Province));
        params.add(new BasicNameValuePair("city", Variable.City));
        params.add(new BasicNameValuePair("car_brand", carBrank));
        params.add(new BasicNameValuePair("car_series", carSeries));
        params.add(new BasicNameValuePair("service_type", String.valueOf(s_type.getSelectedItemPosition())));
        params.add(new BasicNameValuePair("cust_name", cust_name));
        if(platform == null || platform.equals("")){
            params.add(new BasicNameValuePair("qq_login_id", ""));
            params.add(new BasicNameValuePair("sina_login_id", ""));
            params.add(new BasicNameValuePair("logo", ""));
        }else{
        	if(platform.equals("qq")){
        		Platform platformQQ = ShareSDK.getPlatform(RegisterInfoActivity.this, QZone.NAME);
    			params.add(new BasicNameValuePair("qq_login_id", platformQQ.getDb().getUserId()));
                params.add(new BasicNameValuePair("sina_login_id", ""));
                params.add(new BasicNameValuePair("logo", platformQQ.getDb().getUserIcon()));
        	}else{
        		Platform platformSina = ShareSDK.getPlatform(RegisterInfoActivity.this, SinaWeibo.NAME);
    			params.add(new BasicNameValuePair("qq_login_id", ""));
                params.add(new BasicNameValuePair("sina_login_id", platformSina.getDb().getUserId()));
                params.add(new BasicNameValuePair("logo", platformSina.getDb().getUserIcon()));
        	}
        }
        params.add(new BasicNameValuePair("remark", ""));
        new NetThread.postDataThread(handler, url, params, save).start();
	}
	private void jsonSave(String str){
		try {
			GetSystem.myLog(TAG, str);
			JSONObject jsonObject = new JSONObject(str);
			String status_code = jsonObject.getString("status_code");
			if(status_code.equals("0")){
				if(fastTrack){
					//TODO 注册成功，把数据处理好
					Variable.cust_id = jsonObject.getString("cust_id");
					//存储账号密码
					SharedPreferences preferences = getSharedPreferences(
							Constant.sharedPreferencesName, Context.MODE_PRIVATE);
					Editor editor = preferences.edit();
					editor.putString(Constant.sp_account, account);
					editor.putString(Constant.sp_pwd, GetSystem.getM5DEndo(pwd));
					editor.commit();
					//设置
					String url = Constant.BaseUrl + "customer/" + Variable.cust_id
							+ "?auth_code=" + Variable.auth_code;
					new NetThread.GetDataThread(handler, url, get_customer).start();
					
					Intent intent = new Intent(RegisterInfoActivity.this, CarAddActivity.class);
					intent.putExtra("fastTrack", true);
					intent.putExtra("device_id", device_id);
					startActivity(intent);
				}else{
					Toast.makeText(RegisterInfoActivity.this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
					AppApplication.getActivityInstance().exit();
				}				
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}		
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == 1){
			carBrank = data.getStringExtra("brank");
			carBrankId = data.getStringExtra("brankId");
            carSeries = data.getStringExtra("series");
            carSeriesId = data.getStringExtra("seriesId");            
            tv_model.setText(carSeries);
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
	private void jsonCustomer(String str) {		
		SharedPreferences preferences = getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString(Constant.sp_customer + Variable.cust_id, str);
        editor.commit();	    
	}
}