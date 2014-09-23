package com.wise.baba;

import java.util.LinkedList;
import java.util.List;
import org.litepal.LitePalApplication;
import com.baidu.mapapi.SDKInitializer;
import pubclas.GetSystem;
import android.app.Activity;

/**
 * 初始化地图
 * @author honesty
 */
public class AppApplication extends LitePalApplication {
    private static final String TAG = "AppApplication";
    private List<Activity> activityList = new LinkedList<Activity>();
    private static AppApplication instance;
	
    private static AppApplication mInstance = null;
	
	@Override
    public void onCreate() {
	    super.onCreate();
		mInstance = this;
		GetSystem.myLog(TAG, "onCreate");
		//百度地图初始化
		SDKInitializer.initialize(getApplicationContext());
	}
	
	public static AppApplication getInstance() {
		return mInstance;
	}
	
	/**
	 * 关闭activity
	 * @return
	 */
	public static AppApplication getActivityInstance(){
        if(null == instance){
            instance = new AppApplication();
        }
        return instance;
    }
    public void addActivity(Activity activity){
        activityList.add(activity);
    }
    public void exit(){
        for(Activity activity : activityList){
            activity.finish();
        }
    }
	
}