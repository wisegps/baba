package com.wise.baba;

import java.util.List;
import org.json.JSONObject;
import com.wise.notice.SmsActivity;
import com.wise.remind.RemindListActivity;
import com.wise.violation.TrafficActivity;
import cn.jpush.android.api.JPushInterface;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class SmsReceiver extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent intent) {
		    
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
        	System.out.println("JPush用户注册成功");
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
        	System.out.println("接受到推送下来的自定义消息");
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
        	//TODO 确认消息发送，上传到自己服务器       
        	System.out.println("确认消息发送，上传到自己服务器 ");
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
        	init(context,intent.getExtras());
        } else {
        	//System.out.println("Unhandled intent - " + intent.getAction());
        }
	}
	
	private void init(Context context,Bundle bundle){
		boolean isOpen = false;
    	ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> Infos = am.getRunningTasks(20);
		for(int i = 0 ; i < Infos.size() ; i++){
			if(Infos.get(i).topActivity.getPackageName().equals("com.wise.baba")){
				isOpen = true;
				//打开
				receivingNotification(context,bundle);
				break;
			}
		}
		if(!isOpen){
			Intent intent = new Intent(context, WelcomeActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("isSpecify", true);
			intent.putExtras(bundle);
	        context.startActivity(intent);
		}
	}
	
	
	private void receivingNotification(Context context, Bundle bundle){
        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
        try {
			JSONObject jsonObject = new JSONObject(extras);
			int msg_type = jsonObject.getInt("msg_type");
			if(msg_type == 1){//提醒界面
		        Intent intent = new Intent(context, RemindListActivity.class);
		        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        context.startActivity(intent);
			}else if(msg_type == 4){//违章界面
				Intent intent = new Intent(context, TrafficActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        context.startActivity(intent);
			}else{//消息界面
				Intent intent = new Intent(context, SmsActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        context.startActivity(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}