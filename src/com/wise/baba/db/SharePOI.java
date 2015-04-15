/**
 * 
 */
package com.wise.baba.db;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 *
 * @author c
 * @desc   保存周边信息 家  公司
 * @date   2015-4-13
 *
 */
public class SharePOI {

	private Context context;
	public SharePOI(Context context) {
		super();
		this.context = context;
		// TODO Auto-generated constructor stub
	}

	public double[]  getHomeLocation(){
		SharedPreferences preferences = context.getSharedPreferences(
				"search_name", Activity.MODE_PRIVATE);
		double homeLat = Double.valueOf(preferences.getString(
				"homeLat", "0"));
		double homeLon = Double.valueOf(preferences.getString(
				"homeLon", "0"));
		return new double[]{homeLat,homeLon};
	}
	
	public void putHomeLocation(){
		
	}
	
	public double[]  getCompanyLocation(){
		SharedPreferences preferences = context.getSharedPreferences(
				"search_name", Activity.MODE_PRIVATE);
		double companyLat = Double.valueOf(preferences.getString(
				"companyLat", "0"));
		double companyLon = Double.valueOf(preferences.getString(
				"companyLon", "0"));
		return new double[]{companyLat,companyLon};
	}
	
	public void putCompanyLocation(){
		
	}
	
}
