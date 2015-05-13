/**
 * 
 */
package com.wise.baba.app;

/**
 *
 * @author c
 * @desc   常量
 * @date   2015-4-2
 *
 */
public class Const {

	/**
	 * 首页所有卡片fragment名字
	 */
	
	public static final String TAG_SERVICE = "fragmentService";//服务商
	public static final String TAG_POI = "fragmetnHomePOI";//周边
	public static final String TAG_CAR = "fragmentCarInfo";//汽车
	public static final String TAG_SPEED = "fragmentSpeed";//速度
	public static final String TAG_SHOOP = "fragmentShop";//我的店铺
	public static final String TAG_NAV = "fragmentNavigation";//快速导航
	public static final String TAG_WEATHER = "fragmentWeather";//天气
	public static final String TAG_NEWS = "fragmentHotNews";//新闻
	
	/**
	 * 速度卡片，各参数类型
	 */
	//1=电源，2=进气，节气门，3=怠速，4=冷却，水温，5=排放，
	public static final int TYPE_DY = 1;
	public static final int TYPE_JQM = 2;
	public static final int TYPE_DS = 3;
	public static final int TYPE_SW = 4;
	public static final int TYPE_PF = 5;
	 
	
	/**
	 * 体检标示
	 */
	//-2=检测中返回单个项目结果  -1=未检测 0=未绑定终端，1= 检测中，2=历史检测纪录，3=检测
	public static final int DETECT_PROGRESS_RESULT = -2;
	public static final int DETECT_NOT_DETECTED = -1;
	public static final int DETECT_NO_DEVICE = 0;
	public static final int DETECT_IN_PROGRESS= 1;
	public static final int DETECT_HISTORY = 2;
	public static final int DETECT_RESULT = 3;
	
	
	
	
	
}
