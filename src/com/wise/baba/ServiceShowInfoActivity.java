package com.wise.baba;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ServiceShowInfoActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_service_show_info);
		Button bt_show = (Button)findViewById(R.id.bt_show);
		bt_show.setOnClickListener(onClickListener);
	}
	OnClickListener onClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.bt_show:
				startActivity(new Intent(ServiceShowInfoActivity.this, AddShowActivity.class));
				break;

			default:
				break;
			}
		}
	};
}
