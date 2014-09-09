package pubclas;

import java.util.ArrayList;
import java.util.List;
import data.CarData;
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
    public static String cust_name = "";
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
     * 当前未读
     */
    public static double Lon = 0;
    /**车辆信息**/
    public static List<CarData> carDatas = new ArrayList<CarData>();
}
