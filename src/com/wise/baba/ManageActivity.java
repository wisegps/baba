package com.wise.baba;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;

/**
 *@author honesty
 **/
public class ManageActivity {
	private List<Activity> activityList = new LinkedList<Activity>();
    private static ManageActivity instance;
	public static ManageActivity getActivityInstance(){
        if(null == instance){
            instance = new ManageActivity();
        }
        return instance;
    }
    public void addActivity(Activity activity){
        activityList.add(activity);
    }
    public void exit(){
        for(Activity activity : activityList){
            activity.finish();
        }
    }
}
