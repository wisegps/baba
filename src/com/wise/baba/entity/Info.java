package com.wise.baba.entity;

/**
 * @author honesty
 **/
public class Info {
	/** intent传递好友状态 **/
	public static String FriendStatusKey = "FriendStatusKey";

	/** 好友页面状态 **/
	public static enum FriendStatus {
		/**
		 * 已是好友关系
		 */
		FriendInfo,
		/**
		 * 通过id添加好友
		 */
		FriendAddFromId,
		/**
		 * 通过账户添加好友
		 */
		FriendAddFromName
	}

	/** 服务商 **/
	public static int ServiceProvider = 2;
	/**车辆启动状态**/
	public static int CarStartStatus = 8196;
}
