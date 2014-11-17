package com.wise.car;

import com.wise.baba.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class AddressActivity extends Activity {
	TextView tv_home, tv_company;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.adress_activity);

		tv_home = (TextView) findViewById(R.id.tv_home);
		tv_company = (TextView) findViewById(R.id.tv_company);

		tv_home.setOnClickListener(onClickListener);
		tv_company.setOnClickListener(onClickListener);
		findViewById(R.id.iv_back).setOnClickListener(onClickListener);
		findViewById(R.id.ad_delete).setOnClickListener(onClickListener);
		findViewById(R.id.ad_delete_1).setOnClickListener(onClickListener);
	}

	private static final int HOME = 1;
	private static final int COMPANY = 2;
	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				AddressActivity.this.finish();
				break;
			case R.id.ad_delete:
				tv_home.setText("家");
				break;
			case R.id.ad_delete_1:
				tv_company.setText("公司");
				break;
			case R.id.tv_home:
				Intent i = new Intent(AddressActivity.this,
						ChooseAddressActivity.class);
				startActivityForResult(i, HOME);
				break;
			case R.id.tv_company:
				Intent i_1 = new Intent(AddressActivity.this,
						ChooseAddressActivity.class);
				startActivityForResult(i_1, COMPANY);
				break;
			}
		}
	};

	String name = "";

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == ChooseAddressActivity.ADDRESSCODE) {
			name = data.getExtras().getString("name");
			if (requestCode == HOME) {
				if (name != null && !name.equals("")) {
					tv_home.setText("家" + "\n" + name);
				} else {
					tv_home.setText("家");
				}
			} else if (requestCode == COMPANY) {
				if (name != null && !name.equals("")) {
					tv_company.setText("公司" + "\n" + name);
				} else {
					tv_company.setText("公司");
				}
			}
		}

	}
}
