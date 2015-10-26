/**
 * 
 */
package com.wise.baba.app;

/**
 * 
 * @author c
 * @desc Handler message what 的常量
 * @date 2015-4-3
 * 
 */
public class Msg {

	/**
	 * DialView
	 * 
	 */
	public static final int Dial_Refresh_Value = 8;

	/**
	 * FragmentCarInfo
	 */

	public static final int Get_Car_Device = 30;// 获取Device信息
	public static final int Get_Car_GPS = 31;// 获取驾驶指数
	public static final int Get_Car_Drive = 32;// 获取驾驶指数
	public static final int Get_Car_Health = 33;// 获取健康体检信息
	public static final int Get_Car_Limit = 34; // 获取限行
	public static final int Get_Car_Month_Data = 35;// 获取当月油耗信息

	/**
	 * FragmentHomeSpeed
	 */

	public static final int Get_OBD_Data = 2;

	/**
	 * FragmentHomeHotNews
	 */

	public static final int Get_News_List = 6;

	/**
	 * FragmentService
	 */

	public static final int Get_Customer_Total = 9;

	/**
	 * FragmentFriendList
	 */

	public static final int GetFriendList = 10;

	/**
	 * CarManage
	 */
	public static final int Delete_Car = 11;
	public static final int Delete_Car_Success = 12;
	public static final int Unbind = 13;
	public static final int Unbind_Clear_Data = 14;

	/**
	 * FragmentHomeAir
	 */

	public static final int Get_OBD_AIR = 15;

	public static final int Set_Air_Response = 20;
	public static final int Set_Air_Power_Fail = 16;
	public static final int Set_Air_Auto_Fail = 17;
	public static final int Set_Air_Setting_Fail = 18;
	public static final int Set_Air_Level_Fail = 19;

	public static final int Get_Air_AQI = 21;
	public static final int Get_Weather = 22;
	public static final int Get_Air_Value = 23;
	
	/**
	 * 获取所有车辆信息
	 */

	public static final int Get_Car_Info_List = 40;

}
