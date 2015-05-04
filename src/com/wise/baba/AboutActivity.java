package com.wise.baba;


import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends Activity{
	TextView tv_version;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_about);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		TextView tv_check_update = (TextView)findViewById(R.id.tv_check_update);
		tv_check_update.setOnClickListener(onClickListener);
		tv_version = (TextView)findViewById(R.id.tv_version);
		setVersion();
		UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {			
			@Override
			public void onUpdateReturned(int arg0, UpdateResponse arg1) {
				switch (arg0) {
				case UpdateStatus.No:
					Toast.makeText(AboutActivity.this, "无更新", Toast.LENGTH_SHORT).show();
					break;
				} 
			}
		});
	}
	OnClickListener onClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.tv_check_update:
				UmengUpdateAgent.forceUpdate(AboutActivity.this);
				break;
			}
		}
		
	};
	private void setVersion(){
		//Alpha Beta
		tv_version.setText("叭叭V"+ GetSystem.GetVersion(AboutActivity.this, Constant.PackageName));
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		UmengUpdateAgent.setUpdateListener(null);
	}
}