package com.wise.baba;


import versionupdata.VersionUpdate;

import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;
import com.wise.baba.app.Config;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
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
//		UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {			
//			@Override
//			public void onUpdateReturned(int arg0, UpdateResponse arg1) {
//				switch (arg0) {
//				case UpdateStatus.No:
////					Toast.makeText(AboutActivity.this, "无更新", Toast.LENGTH_SHORT).show();
//					break;
//				} 
//			}
//		});
	}
	OnClickListener onClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.tv_check_update:
//				UmengUpdateAgent.forceUpdate(AboutActivity.this);
				update(Config.updateApkUrl);
				break;
			}
		}
		
	};
	
	
	
	private void update(String url){
		 VersionUpdate updata = new VersionUpdate(this);
		 
		 
		 Log.e("UPDATE_TEST", "onCreate................................................");
		 
        updata.check(url, new VersionUpdate.UpdateListener() {
            @Override
            public void hasNewVersion(boolean isHad, String updateMsg, String apkUrl) {
           	 Log.e("UPDATE_TEST", "是否有更新...................................." + isHad);
           	 if(!isHad){
           		Toast.makeText(AboutActivity.this, "已是最新版本", Toast.LENGTH_SHORT).show();
           	 }
            }
        });
	}
	
	private void setVersion(){
		//Alpha Beta
		tv_version.setText("叭叭V"+ GetSystem.GetVersion(AboutActivity.this, Constant.PackageName));
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
//		UmengUpdateAgent.setUpdateListener(null);
	}
}