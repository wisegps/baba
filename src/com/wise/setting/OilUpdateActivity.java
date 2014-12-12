package com.wise.setting;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;

import com.wise.baba.R;

public class OilUpdateActivity extends Activity {
	EditText et_oil_record;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_oil_update);

		et_oil_record = (EditText) findViewById(R.id.et_oil_record);
		findViewById(R.id.iv_back).setOnClickListener(onClickListener);
		findViewById(R.id.bt_oil_update).setOnClickListener(onClickListener);
		findViewById(R.id.bt_oil_add_1).setOnClickListener(onClickListener);
		findViewById(R.id.bt_oil_add_2).setOnClickListener(onClickListener);
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.bt_oil_update:// 车上有车行电脑，直接开始修正

				break;
			case R.id.bt_oil_add_1:// 无车行电脑，第一次加油

				break;
			case R.id.bt_oil_add_2:// 第二次加油，并录入

				break;
			}
		}
	};
}
