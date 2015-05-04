package com.wise.state;

import java.util.ArrayList;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wise.baba.R;
import com.wise.baba.ui.fragment.FragmentFault;
import com.wise.baba.ui.fragment.FragmentLocation;
import com.wise.baba.ui.fragment.FragmentPersionInfo;
import com.wise.baba.ui.fragment.FragmentRemind;
import com.wise.baba.util.DensityUtil;


/**
 * 服务商管理界面
 * 
 * @author honesty
 **/
public class ServiceManageActivity extends FragmentActivity {

	LinearLayout ll_bottom;
	ViewPager vp_manage;
	
	ArrayList<Fragment> fragments = new ArrayList<Fragment>();
	int FriendId = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_service_manage);
		FriendId = getIntent().getIntExtra("FriendId", 0);
		ll_bottom = (LinearLayout) findViewById(R.id.ll_bottom);
		vp_manage = (ViewPager)findViewById(R.id.vp_manage);
		vp_manage.setOffscreenPageLimit(3);
		setData();
		vp_manage.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager()));
		vp_manage.setOnPageChangeListener(new MyOnPageChangeListener());
	}
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case 0:
				vp_manage.setCurrentItem(0);
				break;
			case 1:
				vp_manage.setCurrentItem(1);
				break;
			case 2:
				vp_manage.setCurrentItem(2);
				break;
			case 3:
				vp_manage.setCurrentItem(3);
				break;
			}
		}
	};
	
	public class MyOnPageChangeListener implements OnPageChangeListener{
		@Override
		public void onPageScrollStateChanged(int arg0) {}
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {}
		@Override
		public void onPageSelected(int arg0) {
			setBackground();
			ll_bottom.getChildAt(arg0).setBackgroundColor(getResources().getColor(R.color.yellow_dete_press));
		}		
	}
	
	private void setBackground(){
		for(int i = 0 ; i < ll_bottom.getChildCount() ; i++){
			ll_bottom.getChildAt(i).setBackgroundResource(R.drawable.bg_yellow);
		}
	}

	private void setData() {
		String[] names = {"车况","车务信息","客户信息","位置"};
		for(int i = 0 ; i < names.length ; i++){
			TextView textView = new TextView(getApplicationContext());
			textView.setText(names[i]);
			textView.setLayoutParams(new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
			textView.setBackgroundResource(R.drawable.bg_yellow);
			textView.setTextSize(20);
			textView.setPadding(0,DensityUtil.dip2px(getApplicationContext(), 10),0,DensityUtil.dip2px(getApplicationContext(), 10));
			textView.setGravity(Gravity.CENTER);
			textView.setTextColor(Color.WHITE);
			textView.setId(i);
			textView.setOnClickListener(onClickListener);
			ll_bottom.addView(textView);
		}	
		fragments.add(FragmentFault.newInstance(FriendId));
		fragments.add(FragmentRemind.newInstance(FriendId,true));
		fragments.add(FragmentPersionInfo.newInstance(FriendId,true));
		fragments.add(new FragmentLocation());
	}
	
	class MyFragmentPagerAdapter extends FragmentPagerAdapter{
		public MyFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
		}
		@Override
		public Fragment getItem(int arg0) {
			return fragments.get(arg0);
		}
		@Override
		public int getCount() {
			return fragments.size();
		}		
	}
}