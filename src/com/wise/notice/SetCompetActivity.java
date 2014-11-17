package com.wise.notice;

import com.wise.baba.R;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * 设置好友权限
 *@author honesty
 **/
public class SetCompetActivity extends Activity{
	
	CheckBox cb_obd_data,cb_obd_fault;
	CheckBox cb_location;
	LinearLayout ll_location;
	ScrollView sv_compet;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_set_compet);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		ll_location = (LinearLayout)findViewById(R.id.ll_location);
		cb_location = (CheckBox)findViewById(R.id.cb_location);
		cb_location.setOnCheckedChangeListener(onCheckedChangeListener);
		sv_compet = (ScrollView)findViewById(R.id.sv_compet);
	}
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			default:
				break;
			}
		}
	};
	OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(isChecked){
				ll_location.setVisibility(View.VISIBLE);
				new Handler().postDelayed(new Runnable() {					
					@Override
					public void run() {
						sv_compet.fullScroll(ScrollView.FOCUS_DOWN);						
					}
				}, 50);
			}else{
				ll_location.setVisibility(View.GONE);
			}
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		
	}	
}