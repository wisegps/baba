package com.wise.baba.db;
import com.wise.baba.app.Const;
import com.wise.baba.ui.fragment.FragmentHome;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class ShareCards {

	SharedPreferences sharedPreferences;
	public String initFlag = "|";
	public ShareCards(Context context) {
		sharedPreferences = context.getSharedPreferences("cardNames",
				Activity.MODE_PRIVATE);
	}
	

	/**
	 * 
	 * @param 增加一个
	 */
	public void put(String cardName) {
		//检查参数是否为空
		if(cardName==null || cardName.length()<1){
			return;
		}
		
		//本地为空直接增加
		String strCardNames = sharedPreferences.getString("cardNames", "");
		if(strCardNames.equals("")){
			sharedPreferences.edit().putString("cardNames", cardName);
			return;
		}
		
		//本地不为空，传进来的参数正确
		String[] cardNames = get();
		for(int i=0;i<cardNames.length;i++){
			if(cardNames.equals(cardName)){
				//已经存在，不予添加
				return;
			}
		}
		strCardNames += ","+cardName.trim();
		Editor editor = sharedPreferences.edit();//获取编辑器
		editor.putString("cardNames", strCardNames.trim());
		editor.commit();
	}
	/**
	 * 
	 * @param 增加
	 */
	public void put(String[] cardNames) {
		if(cardNames==null || cardNames.length<1){
			//Put为空 = 删除所有
			clear();
			return;
		}
		Editor editor = sharedPreferences.edit();//获取编辑器
		editor.clear();
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(initFlag);
		for(int i=0;i<cardNames.length;i++){
			strBuilder.append(cardNames[i].trim()+",");
		}
		//去掉最后一个逗号
		strBuilder.replace(strBuilder.lastIndexOf(","), strBuilder.length(), "");
		editor.putString("cardNames", strBuilder.toString().trim());
		editor.commit();
	}
	
	
	/**
	 * 
	 * @param 删除
	 */
	public void delete(String cardNames) {
	}
	
	/**
	 * 
	 * @param 清空
	 */
	public void clear(){
		Editor editor = sharedPreferences.edit();//获取编辑器
		editor.clear();
		editor.putString("cardNames", initFlag);
		editor.commit();
	}

	/**
	 * 
	 * @param 获取全部
	 */
	public String[] get() {

		/**检查是否初始化过，否则增加两个值*/
		String strCardNames = sharedPreferences.getString("cardNames", "");
		if(strCardNames.equals("")){
			String initCardNames[] = {Const.TAG_POI,Const.TAG_CAR,Const.TAG_SPEED,Const.TAG_NEWS,Const.TAG_WEATHER,Const.TAG_SERVICE,Const.TAG_NAV,Const.TAG_AIR};
			put(initCardNames);
		}
		
		/**开始查询，去掉初始化标示符“|” */
		strCardNames = sharedPreferences.getString("cardNames", "");
		
		
		/**卡片为空*/
		if(strCardNames.equals(initFlag)){
			return new String[0];
		}
		
		
		/**卡片不为空*/
		strCardNames = strCardNames.substring(1, strCardNames.length());
		//以逗号为分隔符
		String[] cardNames= strCardNames.trim().split(",");
		return cardNames;
	}

}
