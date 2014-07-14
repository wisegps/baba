package pubclas;

import com.wise.baba.R;

import android.os.Environment;
/**
 * 常量
 * @author honesty
 */
public class Constant {
    /**
     * true,放在百度云上测试,需要默认自动登录
     * false 正式发布
     */
    public static boolean isTest = false;
    /**
     * 服务器地址："http://wiwc.api.wisegps.cn/"
     * http://baba.api.wisegps.cn/
     */
    public static String BaseUrl = "http://42.121.109.221:8002/";
	
    /**
     * 图片地址"http://img.wisegps.cn/logo/"
     */
    public static String ImageUrl = "http://img.wisegps.cn/logo/";
    /**
     * 图片路径存储地址
     */
    public static String BasePath = Environment.getExternalStorageDirectory().getPath() + "/wiwc/";
    /**
     * 个人头像和其他图片
     */
    public static String picPath = BasePath + "pic/";
    /**
     * 存放用户头像
     */
    public static String userIconPath = BasePath + "userIcon/";
    /**
     * 车友圈
     */
    public static String VehiclePath = BasePath + "vehicle/";
    /**
     * 车品牌logo
     */
    public static String VehicleLogoPath = BasePath + "vehicleLogo/";
    /**
     * 车品牌背景
     */
    public static String VehicleSpellPath = BasePath + "vehicleSpell/";
    /**
     * 个人头像
     */
    public static final String UserImage = "UserIcon.jpg";
    /**
     * 救援照片存储
     */
    public static final String ShareImage = "Share.jpg";
    /**
     * 首页背景图片
     */
    public static final String BgImage = "HomeBg.jpg";
	/**
	 * 获取版本信息用到
	 */
	public static String PackageName = "com.wise.wawc";
	
	/**
	 * 违章推送
	 */
	public static String againstPush_key ="againstPush";
	/**
	 * 故障推送
	 */
	public static String faultPush_key ="faultPush";
	/**
	 * 车务提醒
	 */
	public static String remaindPush_key ="remaindPush";
	
	/**
	 * 默认定位中心
	 */
	public static String defaultCenter_key ="defaultCenter";
	/**
	 * SharedPreferences数据共享名称
	 */
	public static final String sharedPreferencesName = "userData";
	
	public static final String DefaultCity = "DefaultCity";
	/**
	 * 定位城市，获取天气油价
	 */
	public static final String LocationCity = "LocationCity";
	/**
	 * 城市编码
	 */
	public static final String LocationCityCode = "LocationCityCode";
	/**
	 * 省份
	 */
	public static final String LocationProvince = "LocationProvince";
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
	public static String TB_Base = "TB_Base";
	/**
	 * 车友圈
	 */
	public static String TB_VehicleFriend = "TB_VehicleFriend";
	/**
	 * 车友圈类型
	 */
	public static String TB_VehicleFriendType = "TB_VehicleFriendType";
	
	/**
	 * 爱车故障
	 */
	public static String TB_Faults = "TB_Faults";
	/**
	 * 行程统计
	 */
	public static String TB_TripTotal = "TB_TripTotal";
	/**
	 * 行程记录
	 */
	public static String TB_TripList = "TB_TripList";
	/**
	 * 单次行程记录详细
	 */
	public static String TB_Trip = "TB_Trip";
	/**
	 * 我的爱车
	 */
	public static String TB_Vehicle = "TB_Vehicle";
	/**
	 * 我的终端
	 */
	public static String TB_Devices = "TB_Devices";
	/**
	 * 我的收藏
	 */
	public static String TB_Collection = "TB_Collection";
	/**
	 * 我的违章
	 */
	public static String TB_Traffic = "TB_Traffic";
	/**
	 * 我的账户
	 */
	public static String TB_Account = "TB_Account";
	/**
	 * 我的消息
	 */
	public static String TB_Sms = "TB_Sms";
	/**
	 * 违章城市表
	 */
	public static String TB_IllegalCity = "TB_IllegalCity";
	/**
	 * 定位成功发送广播，选择城市用到
	 */
	public static String A_City = "com.wise.wawc.city";
	/**
	 * 登录广播，首页获取车辆用到
	 */
	public static String A_Login = "com.wise.wawc.login";
	/**
	 * 注销广播
	 */
    public static String A_LoginOut = "com.wise.wawc.login_out";
    /**
     * 提交订单广播
     */
    public static String A_Order = "com.wise.wawc.order";
    /**
     * 更新or修改车辆
     */
    public static String A_UpdateCar = "com.wise.wawc.update_car";
    
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
	
	
	
	public static int start1 = 0;  // 开始页
	public static int pageSize1 = 10;   //每页数量
	public static int totalPage1 = 0;   //数据总量
	public static int currentPage1 = 0;  //当前页
	public static String UserIconUrl = null;
	/**
	 * 水平滑动选择logo的宽度
	 */
	public static int ImageWidth = 120;
	
	public static String smallImage = "small_pic";
	public static String bigImage = "big_pic";
	
	public static String Maintain = "maintain";   //数据库4s店表  标题（由maintain/城市/品牌构成）
	

	public static String[] items_note_type = {"驾照换证","车辆年检", "车辆保养", "车辆续保", "车辆理赔", "通用提醒"};
	public static int[] items_note_type_image = {R.drawable.ic_launcher,R.drawable.icon_cw_nianjian, R.drawable.icon_cw_baoyang, R.drawable.icon_cw_xuxian, R.drawable.icon_cw_niansheng, R.drawable.icon_xx_notice};
	public static String[] items_note_mode = {"不重复","按月重复", "按周重复", "按日重复", "末月按周重复，其他按周重复"};
}
