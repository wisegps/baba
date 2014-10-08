package com.wise.baba;

import org.litepal.LitePalApplication;
import com.baidu.mapapi.SDKInitializer;
import pubclas.GetSystem;

/**
 * 初始化地图
 * @author honesty
 */
public class AppApplication extends LitePalApplication {
    private static final String TAG = "AppApplication";	
	@Override
    public void onCreate() {
	    super.onCreate();
		GetSystem.myLog(TAG, "onCreate");
		//百度地图初始化
		SDKInitializer.initialize(getApplicationContext());
	}
}