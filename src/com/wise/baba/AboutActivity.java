package com.wise.baba;

import org.json.JSONObject;
import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.NetThread;
import pubclas.UpdateManager;
import com.umeng.update.UmengUpdateAgent;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutActivity extends Activity{
	
	private static final int get_version = 1;
	
	ProgressDialog Dialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_about);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		TextView tv_check_update = (TextView)findViewById(R.id.tv_check_update);
		tv_check_update.setOnClickListener(onClickListener);
	}
	OnClickListener onClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.tv_check_update:
				//Dialog = ProgressDialog.show(AboutActivity.this,"提示","号码提交中",true);
				//Dialog.setCancelable(true);
				//getVersion();
				UmengUpdateAgent.forceUpdate(AboutActivity.this);
				break;
			}
		}
		
	};
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case get_version:
				if(Dialog != null){
					Dialog.dismiss();
				}
				jsonVersion(msg.obj.toString());
				break;

			default:
				break;
			}
		}
		
	};
	/**获取最新版本**/
	private void getVersion(){
		String url = Constant.BaseUrl + "upgrade/android/baba";
        new NetThread.GetDataThread(handler, url,get_version).start();
	}
	private void jsonVersion(String result) {
        try {
            double Version = Double.valueOf(GetSystem.GetVersion(AboutActivity.this, Constant.PackageName));
            double logVersion = Double.valueOf(new JSONObject(result).getString("version"));
            String VersonUrl = new JSONObject(result).getString("app_path");
            String logs = new JSONObject(result).getString("logs");
            if (logVersion > Version) {
            	UpdateManager mUpdateManager = new UpdateManager(
                        AboutActivity.this, VersonUrl, logs, Version);
                mUpdateManager.checkUpdateInfo();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
