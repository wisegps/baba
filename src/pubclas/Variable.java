package pubclas;

import java.util.ArrayList;
import java.util.List;
import data.CarData;
import data.DevicesData;
import data.IllegalCity;
import data.ProvinceData;
import data.ProvinceModel;
/**
 * 变量
 * @author honesty
 */
public class Variable{
    /**auth_code**/
    public static String auth_code;
    /**cust_id**/
    public static String cust_id;
    /**用户名称**/
    public static String cust_name;
    /**通知数目**/
    public static int noti_count = 0;
    /**违章数目**/
    public static int vio_count = 0;
    /**
     * 当前位置
     */
    public static String Adress = "";
    /**
     * 当前定位城市
     */
    public static String City = "";
    public static String Province = "";
    /**
     * 当前经度
     */
    public static double Lat = 0;
    /**
     * 定位时间
     */
    public static String gpsTime = "";
    /**
     * 当前未读
     */
    public static double Lon = 0;
    
    public static List<CarData> carDatas = new ArrayList<CarData>();
    public static List<DevicesData> devicesDatas;
        
    public static int smallImageReqWidth = 0;
    public static int margins = 0;
    public static String MscKey = "5281f227";
    
    
    public static int distance = 10;  //附近车友距离（公里）
    
    
    public static  int articleAdapterImageWidth = 150;
    public static  int articleAdapterImageHeight = 150;
    
    public static List<IllegalCity> illegalList;
    
    public static List<ProvinceModel> illegalProvinceList;
    
    public static List<ProvinceData> provinceDatas;
}
