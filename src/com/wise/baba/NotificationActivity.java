package com.wise.baba;

import org.json.JSONObject;
import cn.jpush.android.api.JPushInterface;
import com.wise.notice.LetterActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class NotificationActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//判断app状况
		Bundle bundle = getIntent().getExtras();
		boolean isTask = getIntent().getBooleanExtra("isTask", true);
		if(isTask){
			receivingNotification(bundle);
		}else{
			Intent intent = new Intent(NotificationActivity.this, WelcomeActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("isSpecify", true);
			intent.putExtras(bundle);
	        startActivity(intent);
		}
		finish();
	}
		
	private void receivingNotification(Bundle bundle){
        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
        try {
			JSONObject jsonObject = new JSONObject(extras);
			int msg_type = jsonObject.getInt("msg_type");
			if(msg_type == 0){//私信界面
				Intent intent = new Intent(NotificationActivity.this, LetterActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("cust_id", jsonObject.getString("friend_id"));
		        startActivity(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}