package com.wise.car;

import com.wise.baba.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 常用地址
 *
 */
public class AddressActivity extends Activity {
	TextView tv_home, tv_company;
	ImageView ad_delete, ad_delete_1;
	ListView addListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.adress_activity);

		tv_home = (TextView) findViewById(R.id.tv_home);
		tv_company = (TextView) findViewById(R.id.tv_company);

		tv_home.setOnClickListener(onClickListener);
		tv_company.setOnClickListener(onClickListener);

		ad_delete = (ImageView) findViewById(R.id.ad_delete);
		ad_delete.setOnClickListener(onClickListener);
		ad_delete_1 = (ImageView) findViewById(R.id.ad_delete_1);
		ad_delete_1.setOnClickListener(onClickListener);

		findViewById(R.id.address_add).setOnClickListener(onClickListener);
		findViewById(R.id.iv_back).setOnClickListener(onClickListener);

		SharedPreferences preferences = getSharedPreferences("search_name",
				Activity.MODE_PRIVATE);
		String name = preferences.getString("name", "");
		String company = preferences.getString("company", "");

		if (name.equals("") || name == null) {
			tv_home.setText("家");
		} else {
			tv_home.setText("家\n" + name);
		}
		if (company.equals("") || company == null) {
			tv_company.setText("公司");
		} else {
			tv_company.setText("公司\n" + company);
		}

		addListView = (ListView) findViewById(R.id.my_address_add);

	}

	private static final int HOME = 1;
	private static final int COMPANY = 2;
	private static final int ADD = 3;
	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				AddressActivity.this.finish();
				break;
			case R.id.ad_delete:
				tv_home.setText("家");
				ad_delete.setVisibility(View.GONE);
				SharedPreferences home = getSharedPreferences("search_name",
						Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = home.edit();
				editor.remove("name");
				editor.remove("homeLat");
				editor.remove("homeLon");
				editor.commit();
				break;
			case R.id.ad_delete_1:
				tv_company.setText("公司");
				ad_delete_1.setVisibility(View.GONE);
				SharedPreferences company = getSharedPreferences("search_name",
						Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor_1 = company.edit();
				editor_1.remove("company");
				editor_1.remove("companyLat");
				editor_1.remove("companyLon");
				editor_1.commit();
				break;
			case R.id.tv_home:
				startActivityForResult(new Intent(AddressActivity.this,
						ChooseAddressActivity.class), HOME);
				break;
			case R.id.tv_company:
				startActivityForResult(new Intent(AddressActivity.this,
						ChooseAddressActivity.class), COMPANY);
				break;
			case R.id.address_add:
				// startActivityForResult(new Intent(AddressActivity.this,
				// ChooseAddressActivity.class), ADD);
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
					ad_delete.setVisibility(View.VISIBLE);
					editor.putString("homeLat", String.valueOf(latitude));
					editor.putString("homeLon", String.valueOf(longitude));
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
					ad_delete_1.setVisibility(View.VISIBLE);
					editor.putString("companyLat", String.valueOf(latitude));
					editor.putString("companyLon", String.valueOf(longitude));
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

	// class AddressAdapter extends BaseAdapter {
	//
	// @Override
	// public int getCount() {
	// return 0;
	// }
	//
	// @Override
	// public Object getItem(int position) {
	// return null;
	// }
	//
	// @Override
	// public long getItemId(int position) {
	// return 0;
	// }
	//
	// @Override
	// public View getView(int position, View convertView, ViewGroup parent) {
	// return null;
	// }
	//
	// class Holder {
	// TextView addressName;
	// ImageView addressDelete;
	// }

	// }
}
