package com.wise.notice;

import com.wise.baba.R;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * 服务信息界面
 *@author honesty
 **/
public class ServiceProviderInfoActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_service_provider);
		TextView tv_service_name = (TextView)findViewById(R.id.tv_service_name);
		tv_service_name.setText(getIntent().getStringExtra("name"));
	}
}