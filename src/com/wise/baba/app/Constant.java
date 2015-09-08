package com.wise.baba.app;

import android.os.Environment;

import com.wise.baba.R;

/**
 * 常量
 * 
 * @author honesty
 */

public class Constant {
	public static boolean isLog = true;
	/** 在阿里云上url **/
	public static String oss_url = "http://img.bibibaba.cn/";
	// public static String oss_url =
	// "http://baba-img.oss-cn-hangzhou.aliyuncs.com/photo/";
	/** 图片oss路径 **/
	public static String oss_path = "baba-img/photo";
	/** accessId **/
	public static String oss_accessId = "eJ3GLV07j9DD4LY5";
	/** accessKey **/
	public static String oss_accessKey = "iAxAHoAuG1ZYwjIRLfyYKK2oF9WcCe";

	/** url,42.121.109.221:8002 **/
	// public static String BaseUrl = "http://183.12.66.164:8002/";
	public static String BaseUrl = "http://api.bibibaba.cn/";
	/** 图片地址 **/
	public static String ImageUrl = "http://img.wisegps.cn/logo/";
	/** 图片路径存储地址 **/
	public static String BasePath = Environment.getExternalStorageDirectory().getPath() + "/baba/";
	/** 车品牌logo **/
	public static String VehicleLogoPath = BasePath + "vehicleLogo/";
	/** 存放用户头像 **/
	public static String userIconPath = BasePath + "userIcon/";
	/** 存放秀爱车图片 **/
	public static String VehiclePath = BasePath + "vehicle/";
	/** 秀爱车拍照存放地方 **/
	public static String TemporaryImage = "0.png";
	/** 临时图片 **/
	public static String TemporaryMapImage = BasePath + "map.png";
	/** 获取版本信息用到 **/
	public static String PackageName = "com.wise.baba";

	/**
	 * 违章推送
	 */
	public static String againstPush_key = "againstPush";
	/**
	 * 故障推送
	 */
	public static String faultPush_key = "faultPush";
	/**
	 * 车务提醒
	 */
	public static String remaindPush_key = "remaindPush";

	/**
	 * 默认定位中心
	 */
	public static String defaultCenter_key = "defaultCenter";
	/**
	 * SharedPreferences数据共享名称
	 */
	public static final String sharedPreferencesName = "userData";

	public static final String DefaultCity = "DefaultCity";
	/**
	 * 城市编码
	 */
	public static final String LocationCityCode = "LocationCityCode";

	/** 存放城市 **/
	public static final String sp_city = "sp_city";
	/** 存放省份 **/
	public static final String sp_province = "sp_province";
	/** 存放体检 **/
	public static final String sp_health_score = "sp_health_score";
	/** 存放驾驶信息 **/
	public static final String sp_drive_score = "sp_drive_score";
	/** 存放个人信息 **/
	public static final String sp_customer = "sp_customer";
	/**存放通知消息**/
	public static final String sp_notice = "sp_notice";
	/**
	 * 油价
	 */
	public static final String LocationCityFuel = "LocationCityFuel";
	/**
	 * 获取指定城市4s店所需参数
	 */
	public static final String FourShopParmeter = "FourShopParmeter";
	public static final String sp_login_type1 = "sp_login_type";
	public static final String sp_account = "sp_account";
	public static final String sp_pwd = "sp_pwd";
	/**
	 * 用户id
	 */
	public static final String sp_cust_id = "sp_cust_id";
	/**
	 * 用户auth_code
	 */
	public static final String sp_auth_code = "sp_auth_code";
	/**
	 * sp车辆所在列表位置
	 */
	public static final String DefaultVehicleID = "DefaultVehicleID";
	/**
	 * sp登录平台
	 */
	public static final String platform = "platform";
	/**
	 * 定位成功发送广播，选择城市用到
	 */
	public static String A_City = "com.wise.wawc.city";
	/**
	 * 提交订单广播
	 */
	public static String A_Order = "com.wise.wawc.order";
	/**
	 * 更新or修改车辆
	 */
	public static String A_UpdateCar = "com.wise.wawc.update_car";
	/** 更新首页信息 **/
	public static String A_RefreshHomeCar = "com.wise.baba.refresh_home_car";
	/**修改首页卡片广播**/
	public static String A_ChangeCards = "com.wise.baba.change_cards";
	/** 登录广播，首页获取车辆用到 */
	public static String A_Login = "com.wise.baba.login";
	/** 注销账号广播 **/
	public static String A_LoginOut = "com.wise.baba.login_out";
	/** 收到私信通知 **/
	public static String A_ReceiverLetter = "com.wise.baba.letter";
	/**
	 * 我的爱车logo刷新
	 */
	public static String updataMyVehicleLogoAction = "com.wise.wawc.update_logo";
	/**
	 * 添加终端
	 */
	public static String A_UpdateDevice = "com.wise.wawc.update_device";

	/**
	 * 水平滑动选择logo的宽度
	 */
	public static int ImageWidth = 120;

	public static String smallImage = "small_pic";
	public static String bigImage = "big_pic";

	public static String Maintain = "maintain"; // 数据库4s店表 标题（由maintain/城市/品牌构成）

	public static String[] items_note_type = { "驾照换证", "车辆年检", "车辆保养", "车辆续保", "车辆理赔", "通用提醒" };
	public static int[] items_note_type_image = { R.drawable.icon_cw_niansheng, R.drawable.icon_cw_nianjian, R.drawable.icon_cw_baoyang,
			R.drawable.icon_cw_xuxian, R.drawable.icon_xx_notice, R.drawable.icon_xx_notice };
	public static String[] items_note_mode = { "不重复", "按月重复", "按周重复", "按日重复", "末月按周重复，其他按月重复" };
	//卡片管理

	public static int[] picture = { R.drawable.ico_small_poi,R.drawable.ico_small_car,
			R.drawable.ico_small_speed , R.drawable.ico_small_news, R.drawable.ico_small_weather,R.drawable.ico_small_shop,R.drawable.ico_small_nav,R.drawable.ico_small_air};
	public static String[] title = { "周边","车况","仪表盘", "本地资讯","天气","我的店铺","快速导航","空气质量" };
	public static String[] content = { "周边","查看体检信息，驾驶指数","获取终端obd信息", "最新新闻更新和内容" ,"今天天气概况","服务商专用","导航到其他常用操作","空气净化器"};
	//public static String[] cards = { "service","weather", "hotNews" };
}
