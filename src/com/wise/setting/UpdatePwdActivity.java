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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class UpdatePwdActivity extends Activity{
	private static final int update_pwd = 1;
	EditText et_pwd,et_new_pwd,et_new_pwd_again;
	String new_pwd;
	AppApplication app;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_update_pwd);
		app = (AppApplication)getApplication();
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		ImageView iv_sure = (ImageView)findViewById(R.id.iv_sure);
		iv_sure.setOnClickListener(onClickListener);
		et_pwd = (EditText)findViewById(R.id.et_pwd);
		et_new_pwd = (EditText)findViewById(R.id.et_new_pwd);
		et_new_pwd_again = (EditText)findViewById(R.id.et_new_pwd_again);
	}
	OnClickListener onClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.iv_sure:				
				updatePwd();
				break;
			}
		}		
	};
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case update_pwd:
				jsonUpdatePwd(msg.obj.toString());
				break;
			}
		}		
	};
	
	private void updatePwd(){
		String pwd = et_pwd.getText().toString().trim();
		new_pwd = et_new_pwd.getText().toString().trim();
		String new_pwd_again = et_new_pwd_again.getText().toString().trim();
		
		if(pwd.equals("") || new_pwd.equals("") || new_pwd_again.equals("")){
			Toast.makeText(UpdatePwdActivity.this, "请填写完整信息", Toast.LENGTH_SHORT).show();
			return;
		}
		
		if(!new_pwd.equals(new_pwd_again)){
			Toast.makeText(UpdatePwdActivity.this, "2次输入的密码不一致", Toast.LENGTH_SHORT).show();
			return;
		}
		
		SharedPreferences preferences = getSharedPreferences(
                Constant.sharedPreferencesName, Context.MODE_PRIVATE);
        String sp_pwd = preferences.getString(Constant.sp_pwd, "");
		
        if(!sp_pwd.equals(GetSystem.getM5DEndo(pwd))){
        	Toast.makeText(UpdatePwdActivity.this, "密码错误，请重新输入", Toast.LENGTH_SHORT).show();
        	return ;
        }
        
		String url = Constant.BaseUrl + "customer/" + app.cust_id + "/field?auth_code=" + app.auth_code;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("field_name", "password"));
        params.add(new BasicNameValuePair("field_type", "String"));
        params.add(new BasicNameValuePair("field_value", GetSystem.getM5DEndo(new_pwd)));
        new Thread(new NetThread.putDataThread(handler, url, params, update_pwd)).start();
	}
	private void jsonUpdatePwd(String str){
		try {
			JSONObject jsonObject = new JSONObject(str);
			if(jsonObject.getString("status_code").equals("0")){
				SharedPreferences preferences = getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		        Editor editor = preferences.edit();
		        editor.putString(Constant.sp_pwd, GetSystem.getM5DEndo(new_pwd));
		        editor.commit();
		        Toast.makeText(UpdatePwdActivity.this, "密码修改成功", Toast.LENGTH_SHORT).show();
		        finish();
			}else{
				Toast.makeText(UpdatePwdActivity.this, "密码修改失败", Toast.LENGTH_SHORT).show();
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
