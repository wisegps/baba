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
	
	public static final String TAG_AIR = "fragmentHomeAir";//空气质量
	
	
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
	
	//搜索建议类型
	public static final int Type_History = 0;
	public static final int Type_Suggestion = 1;
	public static final int Type_Clear_History = 2;
	
	
	//空气净化器设置运行模式,手工，智能，定时
		public static final int AIR_MODE_MANUL = 0;
		public static final int AIR_MODE_SMART = 1;
		public static final int AIR_MODE_TIMER = 2;
	
}
