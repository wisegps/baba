package com.wise.baba;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class ShowActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show);
		TextView tv_service = (TextView)findViewById(R.id.tv_service);
		tv_service.setOnClickListener(onClickListener);
		TextView tv_car = (TextView)findViewById(R.id.tv_car);
		tv_car.setOnClickListener(onClickListener);
	}
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.tv_service:
				startActivity(new Intent(ShowActivity.this, ServiceRankingActivity.class));
				break;
			case R.id.tv_car:
				startActivity(new Intent(ShowActivity.this, ShowCarActivity.class));
				break;
			}
		}
	};
}
