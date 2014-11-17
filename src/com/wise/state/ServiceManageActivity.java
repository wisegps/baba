package com.wise.state;

import java.util.ArrayList;
import pubclas.DensityUtil;
import com.wise.baba.R;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 服务商管理界面
 * 
 * @author honesty
 **/
public class ServiceManageActivity extends Activity {

	LinearLayout ll_bottom;
	ViewPager vp_manage;
	
	ArrayList<View> pageViews = new ArrayList<View>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_service_manage);
		ll_bottom = (LinearLayout) findViewById(R.id.ll_bottom);
		vp_manage = (ViewPager)findViewById(R.id.vp_manage);
		setData();
		vp_manage.setAdapter(new ServicePageAdapter());
	}
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.bt_odb_data:
				
				break;

			case R.id.bt_odb_fault:
				
				break;
			}
		}
	};

	private void setData() {
		for(int i = 0 ; i < 2 ; i++){
			TextView textView = new TextView(getApplicationContext());
			textView.setText("客户功能");
			textView.setLayoutParams(new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
			textView.setBackgroundResource(R.drawable.bg_yellow);
			textView.setTextSize(20);
			textView.setPadding(0,DensityUtil.dip2px(getApplicationContext(), 10),0,DensityUtil.dip2px(getApplicationContext(), 10));
			textView.setGravity(Gravity.CENTER);
			textView.setTextColor(Color.WHITE);
			ll_bottom.addView(textView);
		}
		View view_offline_down1 = LayoutInflater.from(this).inflate(
				R.layout.pv_service_obd, null);
		Button bt_odb_data = (Button)view_offline_down1.findViewById(R.id.bt_odb_data);
		bt_odb_data.setOnClickListener(onClickListener);
		Button bt_odb_fault = (Button)view_offline_down1.findViewById(R.id.bt_odb_fault);
		bt_odb_fault.setOnClickListener(onClickListener);
		pageViews.add(view_offline_down1);
		View view_offline_down = LayoutInflater.from(this).inflate(
				R.layout.pv_service_remind, null);
		pageViews.add(view_offline_down);
	}
	
	class ServicePageAdapter extends PagerAdapter {
		@Override
		public int getCount() {
			return pageViews.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView(pageViews.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			((ViewPager) container).addView(pageViews.get(position));
			return pageViews.get(position);
		}
	}
}