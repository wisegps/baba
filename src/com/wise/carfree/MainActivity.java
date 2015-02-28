package com.wise.carfree;

import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.MKGeneralListener;
import com.baidu.lbsapi.panoramaview.PanoramaView;
import com.wise.baba.R;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.Toast;
/**
 * 1.解决箭头出不来问题：可能是jar问题
 * 2.解决不使用application退出再进不显示问题
 * @author honesty
 *
 */
public class MainActivity extends Activity {
	PanoramaView mPanoView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		DemoApplication app = (DemoApplication) this.getApplication();  
//		if (app.mBMapManager == null) {  
//		    app.mBMapManager = new BMapManager(app);  
//		 
//		    app.mBMapManager.init(new DemoApplication.MyGeneralListener());  
//		}
		BMapManager mBMapManager = new BMapManager(getApplicationContext());
		mBMapManager.init(new MKGeneralListener() {
			
			@Override
			public void onGetPermissionState(int iError) {
				//非零值表示key验证未通过
	            if (iError != 0) {
	                //授权Key错误：
	                Toast.makeText(MainActivity.this, 
	                        "请在AndoridManifest.xml中输入正确的授权Key,并检查您的网络连接是否正常！error: "+iError, Toast.LENGTH_LONG).show();
	            }
	            else{
	            	Toast.makeText(MainActivity.this, 
	                        "key认证成功", Toast.LENGTH_LONG).show();
	            }
			}
		});
		setContentView(R.layout.activity_main);
		mPanoView = (PanoramaView)findViewById(R.id.panorama);
		mPanoView.setShowTopoLink(true);
		mPanoView.setPanorama("0100220000130817164838355J5");
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mPanoView.onPause();
	}
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		mPanoView.onResume();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mPanoView.destroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
