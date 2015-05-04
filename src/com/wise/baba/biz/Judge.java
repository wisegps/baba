package com.wise.baba.biz;

import com.wise.baba.AppApplication;

public class Judge {
	/**
	 * 判断用户是否登录
	 * @return true 已登录
	 */
	public static boolean isLogin(AppApplication app){
		if(app.cust_id == null || app.cust_id.equals("0")){
			return false;
		}
		return true;
	}
}
