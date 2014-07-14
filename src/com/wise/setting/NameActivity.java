package com.wise.setting;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import pubclas.Constant;
import pubclas.NetThread;
import pubclas.Variable;
import com.wise.baba.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class NameActivity extends Activity{
	private static final int update_name = 1;
	EditText et_name;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_name);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		Button bt_sure = (Button)findViewById(R.id.bt_sure);
		bt_sure.setOnClickListener(onClickListener);
		et_name = (EditText)findViewById(R.id.et_name);
	}
	OnClickListener onClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.bt_sure:				
				updateName();
				finish();
				break;
			}
		}		
	};
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case update_name:
				System.out.println("update name : " + msg.obj.toString());
				break;

			default:
				break;
			}
		}		
	};
	private void updateName(){
		String name = et_name.getText().toString().trim();
		if(name.equals("")){
			return;
		}
		Intent data = new Intent();
		data.putExtra("name", name);
		setResult(0, data);
		String url = Constant.BaseUrl + "customer/" + Variable.cust_id + "/field?auth_code=" + Variable.auth_code;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("field_name", "cust_name"));
        params.add(new BasicNameValuePair("field_type", "String"));
        params.add(new BasicNameValuePair("field_value", name));
        new Thread(new NetThread.putDataThread(handler, url, params, update_name)).start();
	}
}
