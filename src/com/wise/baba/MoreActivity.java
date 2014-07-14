package com.wise.baba;

import com.wise.remind.RemindListActivity;
import com.wise.setting.SetActivity;
import com.wise.violation.TrafficActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class MoreActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_more);
		TextView tv_set = (TextView)findViewById(R.id.tv_set);
		tv_set.setOnClickListener(onClickListener);
		TextView tv_sms = (TextView)findViewById(R.id.tv_sms);
		tv_sms.setOnClickListener(onClickListener);
		TextView tv_collection = (TextView)findViewById(R.id.tv_collection);
		tv_collection.setOnClickListener(onClickListener);
		TextView tv_remind = (TextView)findViewById(R.id.tv_remind);
		tv_remind.setOnClickListener(onClickListener);
		TextView tv_traffic = (TextView)findViewById(R.id.tv_traffic);
		tv_traffic.setOnClickListener(onClickListener);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
	}
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.tv_sms:
				startActivity(new Intent(MoreActivity.this, SmsActivity.class));
				break;
			case R.id.tv_collection:
				startActivity(new Intent(MoreActivity.this, CollectionActivity.class));
				break;
			case R.id.tv_remind:
				startActivity(new Intent(MoreActivity.this, RemindListActivity.class));
				break;
			case R.id.tv_traffic:
				startActivity(new Intent(MoreActivity.this, TrafficActivity.class));
				break;
			case R.id.tv_set:
				startActivity(new Intent(MoreActivity.this, SetActivity.class));
				break;
			}
		}
	};
}