package com.wise.baba;

import java.util.List;
import org.json.JSONObject;

import pubclas.Constant;

import com.wise.notice.LetterActivity;
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
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("smsReceiver", intent.getAction());
		
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
        	//("JPush用户注册成功");
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
        	//("接受到推送下来的自定义消息");
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {        	
        	//TODO 确认消息发送，上传到自己服务器       
        	//("确认消息发送，上传到自己服务器 ");
        	Intent intent1 = new Intent(Constant.A_ReceiverLetter);
        	String extras = intent.getExtras().getString(JPushInterface.EXTRA_EXTRA);
        	intent1.putExtra("extras", extras);
        	context.sendBroadcast(intent1);
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
        	//在这里可以自己写代码去定义用户点击后的行为
        	init(context,intent.getExtras());
        } else {
        	
        }
        if(JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())){
        	//("自定义消息");
        }
        if(JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())){
        	 boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
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
			}else if(msg_type == 0){//私信界面
				Intent intent = new Intent(context, LetterActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("cust_id", jsonObject.getString("friend_id"));
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