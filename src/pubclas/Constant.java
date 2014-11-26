package pubclas;

import com.wise.baba.R;
import android.os.Environment;

/**
 * 常量
 * 
 * @author honesty
 */
public class Constant {
	public static boolean isLog = true;
	/**在阿里云上url**/
	public static String oss_url = "http://img.bibibaba.cn/";
	// public static String oss_url = "http://baba-img.oss-cn-hangzhou.aliyuncs.com/photo/";
	/** 图片oss路径 **/
	public static String oss_path = "baba-img/photo";
	/** accessId **/
	public static String oss_accessId = "eJ3GLV07j9DD4LY5";
	/** accessKey **/
	public static String oss_accessKey = "iAxAHoAuG1ZYwjIRLfyYKK2oF9WcCe";

	/** url,42.121.109.221:8002 **/
	//public static String BaseUrl = "http://183.12.66.164:8002/";
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
	/**存放驾驶信息**/
	public static final String sp_drive_score = "sp_drive_score";
	/** 存放个人信息 **/
	public static final String sp_customer = "sp_customer";
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
	 * 基础表
	 */
	//public static String TB_Base = "TB_Base";
	/**
	 * 我的爱车
	 */
	//public static String TB_Vehicle = "TB_Vehicle";
	/**
	 * 我的终端
	 */
	//public static String TB_Devices = "TB_Devices";
	/**
	 * 我的收藏
	 */
	//public static String TB_Collection = "TB_Collection";
	/**
	 * 我的消息
	 */
	//public static String TB_Sms = "TB_Sms";
	/**
	 * 违章城市表
	 */
	//public static String TB_IllegalCity = "TB_IllegalCity";
	/**
	 * 定位成功发送广播，选择城市用到
	 */
	public static String A_City = "com.wise.wawc.city";
	/**
	 * 登录广播，首页获取车辆用到
	 */
	public static String A_Login = "com.wise.wawc.login";
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
	/** 注销广播 **/
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
	 * 收货人
	 */
	public static String Consignee = "Consignee";
	/**
	 * 收获地址
	 */
	public static String Adress = "Adress";
	/**
	 * 收货人手机
	 */
	public static String Phone = "Phone";

	public static int start1 = 0; // 开始页
	public static int pageSize1 = 10; // 每页数量
	public static int totalPage1 = 0; // 数据总量
	public static int currentPage1 = 0; // 当前页
	public static String UserIconUrl = null;
	/**
	 * 水平滑动选择logo的宽度
	 */
	public static int ImageWidth = 120;

	public static String smallImage = "small_pic";
	public static String bigImage = "big_pic";

	public static String Maintain = "maintain"; // 数据库4s店表 标题（由maintain/城市/品牌构成）

	public static String[] items_note_type = { "驾照换证", "车辆年检", "车辆保养", "车辆续保",
			"车辆理赔", "通用提醒" };
	public static int[] items_note_type_image = { R.drawable.icon_cw_niansheng,
			R.drawable.icon_cw_nianjian, R.drawable.icon_cw_baoyang,
			R.drawable.icon_cw_xuxian, R.drawable.icon_xx_notice,
			R.drawable.icon_xx_notice };
	public static String[] items_note_mode = { "不重复", "按月重复", "按周重复", "按日重复",
			"末月按周重复，其他按月重复" };

	public static String provinces[][] = { 
			{ "北京", "京" }, 
			{ "天津", "津" }, 
			{ "河北", "冀" },
			{ "山西", "晋" }, 
			{ "内蒙古", "蒙" }, 
			{ "辽宁", "辽" }, 
			{ "吉林", "吉" },
			{ "黑龙江", "黑" }, 
			{ "上海", "沪" }, 
			{ "江苏", "苏" }, 
			{ "浙江", "浙" },
			{ "安徽", "皖" }, 
			{ "福建", "闽" }, 
			{ "江西", "赣" }, 
			{ "山东", "鲁" },
			{ "河南", "豫" }, 
			{ "湖北", "鄂" }, 
			{ "湖南", "湘" }, 
			{ "广东", "粤" },
			{ "广西", "桂" }, 
			{ "海南", "琼" }, 
			{ "重庆", "渝" }, 
			{ "四川", "川" },
			{ "贵州", "贵" },
			{ "云南", "云" }, 
			{ "西藏", "藏" }, 
			{ "陕西", "陕" },
			{ "甘肃", "甘" }, 
			{ "青海", "青" },
			{ "宁夏", "宁" },
			{ "新疆", "新" },
			{ "", "" }};//占位符
}
