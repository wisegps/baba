package com.wise.baba;

import java.util.ArrayList;
import java.util.List;

import org.litepal.LitePalApplication;


import android.content.Context;

import com.baidu.mapapi.SDKInitializer;
import com.wise.baba.app.App;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.db.dao.DaoMaster;
import com.wise.baba.db.dao.DaoMaster.OpenHelper;
import com.wise.baba.db.dao.DaoSession;
import com.wise.baba.db.dao.FriendData;
import com.wise.baba.entity.CarData;


/**
 * 初始化地图
 * 
 * @author honesty
 */
public class AppApplication extends LitePalApplication {
	private static final String TAG = "AppApplication";
	/** 演示true **/
	public boolean isTest = false;
	/** auth_code **/
	public String auth_code;
	/** 用户类别 **/
	public int cust_type = 0;
	/** cust_id **/
	public String cust_id;
	/** 用户名称 **/
	public String cust_name = "";
	/** 通知数目 **/
	public int noti_count = 0;
	/** 违章数目 **/
	public int vio_count = 0;
	/**
	 * 当前位置
	 */
	public String Adress = "";
	/**
	 * 当前定位城市
	 */
	public String City = "";
	public String Province = "";
	/**
	 * 当前经度
	 */
	public double Lat = 0;
	/**
	 * 当前未读
	 */
	public double Lon = 0;
	/** 车辆信息 **/
	public List<CarData> carDatas = new ArrayList<CarData>();
	
	public static int currentCarIndex = 0;//当前车辆
	
	
	
	/** 好友信息 **/
	public List<FriendData> friendDatas = new ArrayList<FriendData>();

	private static DaoMaster daoMaster;
	private static DaoSession daoSession;

	@Override
	public void onCreate() {
		super.onCreate();
		GetSystem.myLog(TAG, "onCreate");
		// 百度地图初始化
		SDKInitializer.initialize(getApplicationContext());
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());
	}
	
	/**
	 * 取得DaoMaster
	 *
	 * @param context
	 * @return
	 */
	public static DaoMaster getDaoMaster(Context context)
	{
	    if (daoMaster == null)
	    {
	        OpenHelper helper = new DaoMaster.DevOpenHelper(context, App.DATABASE_NAME, null);
	        daoMaster = new DaoMaster(helper.getWritableDatabase());
	    }
	    return daoMaster;
	}
	/**
	 * 取得DaoSession
	 *
	 * @param context
	 * @return
	 */
	public static DaoSession getDaoSession(Context context)
	{
	    if (daoSession == null)
	    {
	        if (daoMaster == null)
	        {
	            daoMaster = getDaoMaster(context);
	        }
	        daoSession = daoMaster.newSession();
	    }
	    return daoSession;
	}
	
}