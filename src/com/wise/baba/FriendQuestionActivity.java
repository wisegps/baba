package com.wise.baba;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class FriendQuestionActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friend_ask);
		Button bt_reply = (Button)findViewById(R.id.bt_reply);
		bt_reply.setOnClickListener(onClickListener);
	}
	OnClickListener onClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.bt_reply:
				startActivity(new Intent(FriendQuestionActivity.this, ReplyActivity.class));
				break;

			default:
				break;
			}
		}		
	};
}
