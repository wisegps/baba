package com.wise.violation;

import com.wise.baba.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/** 省份简称 **/
public class ShortProvincesActivity extends Activity {
	TextView car_title;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_short_provinces);
		GridView gv_provices = (GridView) findViewById(R.id.gv_provices);
		gv_provices.setAdapter(new ProvincesAdapter());
		gv_provices.setOnItemClickListener(onItemClickListener);

		car_title = (TextView) findViewById(R.id.car_number_name);
		car_title.setText("请选择车辆地区");
		findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ShortProvincesActivity.this.finish();
			}
		});
	}

	OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			String province = provinces[arg2];
			if(province.equals("")){
				
			}else{
				Intent intent = new Intent();
				intent.putExtra("province", province);
				ShortProvincesActivity.this.setResult(6, intent);
				ShortProvincesActivity.this.finish();
			}
		}
	};

	private String[] provinces = { "京", "津", "沪", "渝", "冀", "豫", "云", "辽", "黑",
			"湘", "皖", "鲁", "新", "苏", "浙", "赣", "鄂", "桂", "甘", "晋", "蒙", "陕",
			"吉", "闽", "贵", "粤", "青", "藏", "川", "宁", "琼", "" };

	class ProvincesAdapter extends BaseAdapter {
		LayoutInflater inflater = LayoutInflater
				.from(ShortProvincesActivity.this);

		@Override
		public int getCount() {
			return provinces.length;
		}

		@Override
		public Object getItem(int position) {
			return provinces[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.item_short_province,
						null);
				holder = new ViewHolder();
				holder.tv_province = (TextView) convertView
						.findViewById(R.id.tv_province);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tv_province.setBackgroundColor(getResources().getColor(
					R.color.white));
			holder.tv_province.setText(provinces[position]);
			return convertView;
		}

		class ViewHolder {
			TextView tv_province;
		}
	}
}