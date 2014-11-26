package com.wise.baba;

import java.util.ArrayList;
import java.util.List;

import org.litepal.LitePalApplication;
import com.baidu.mapapi.SDKInitializer;

import data.CarData;
import pubclas.GetSystem;

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
	/**当前选着省份**/
	public String Province = "";
	/**当前定位省份**/
	public String LProvince = "";
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

	@Override
	public void onCreate() {
		super.onCreate();
		GetSystem.myLog(TAG, "onCreate");
		// 百度地图初始化
		SDKInitializer.initialize(getApplicationContext());
	}
}