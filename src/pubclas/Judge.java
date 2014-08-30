package pubclas;

public class Judge {
	/**
	 * 判断用户是否登录
	 * @return true 已登录
	 */
	public static boolean isLogin(){
		if(Variable.cust_id == null || Variable.cust_id.equals("0")){
			return false;
		}
		return true;
	}
}
