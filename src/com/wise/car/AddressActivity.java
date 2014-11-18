package com.wise.car;

import com.wise.baba.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

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

		SharedPreferences preferences = getSharedPreferences("search_name",
				Activity.MODE_PRIVATE);
		String name = preferences.getString("name", "");
		String company = preferences.getString("company", "");

		tv_home.setText("家\n" + name);
		tv_company.setText("公司\n" + company);

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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == ChooseAddressActivity.ADDRESSCODE) {
			SharedPreferences preferences = getSharedPreferences("search_name",
					Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = preferences.edit();
			String name = data.getExtras().getString("name");
			double latitude = data.getExtras().getDouble("latitude");
			double longitude = data.getExtras().getDouble("longitude");
			boolean myLocat = data.getExtras().getBoolean("myLoct");
			if (requestCode == HOME) {
				if (name != null && !name.equals("")) {
					tv_home.setText("家\n" + name);
					editor.putLong("homeLat", (long) latitude);
					editor.putLong("homeLon", (long) longitude);
					editor.putString("name", name);
				} else if (myLocat) {
					tv_home.setText("家");
					Toast.makeText(AddressActivity.this, "定位失败",
							Toast.LENGTH_SHORT).show();
				} else {
					tv_home.setText("家");
					Toast.makeText(AddressActivity.this, "未搜索到结果",
							Toast.LENGTH_SHORT).show();
				}
			} else if (requestCode == COMPANY) {
				if (name != null && !name.equals("")) {
					tv_company.setText("公司\n" + name);
					editor.putLong("companyLat", (long) latitude);
					editor.putLong("companyLon", (long) longitude);
					editor.putString("company", name);
				} else if (myLocat) {
					tv_company.setText("公司");
					Toast.makeText(AddressActivity.this, "定位失败",
							Toast.LENGTH_SHORT).show();
				} else {
					tv_company.setText("公司");
					Toast.makeText(AddressActivity.this, "未搜索到结果",
							Toast.LENGTH_SHORT).show();
				}
			}
			editor.commit();
		}

	}
}
