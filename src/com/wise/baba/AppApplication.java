package com.wise.baba;

import java.util.LinkedList;
import java.util.List;
import pubclas.GetSystem;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;

import com.baidu.mapapi.BMapManager;

/**
 * 初始化地图
 * @author honesty
 */
public class AppApplication extends Application {
    private static final String TAG = "AppApplication";
    private List<Activity> activityList = new LinkedList<Activity>();
    private static AppApplication instance;
	
    private static AppApplication mInstance = null;

    /**
     * 百度地图key
     */
    //测试
    public static final String strKey = "zwIFsm9hVHYmroq923Psz3xv";
    //正式
    //public static final String strKey = "S9V4G1qyDIWyU1eVF8MYHfKP";
    BMapManager mBMapManager = null;
	
	@Override
    public void onCreate() {
	    super.onCreate();
		mInstance = this;
		GetSystem.myLog(TAG, "onCreate");
		initEngineManager(this);
	}
	
	public void initEngineManager(Context context) {
        if (mBMapManager == null) {
        	GetSystem.myLog(TAG, "mBMapManager实例化");
            mBMapManager = new BMapManager(context);
        }
        if (!mBMapManager.init(strKey,null)) {
            Toast.makeText(AppApplication.getInstance().getApplicationContext(), 
                    "BMapManager  初始化错误!", Toast.LENGTH_LONG).show();
        }
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