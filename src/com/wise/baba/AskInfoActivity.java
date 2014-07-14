package com.wise.baba;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class AskInfoActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ask_search);
		LinearLayout ll_ask = (LinearLayout)findViewById(R.id.ll_ask);
		ll_ask.setOnClickListener(onClickListener);
		LinearLayout ll_friend_ask = (LinearLayout)findViewById(R.id.ll_friend_ask);
		ll_friend_ask.setOnClickListener(onClickListener);
		Button bt_ask_friend = (Button)findViewById(R.id.bt_ask_friend);
		bt_ask_friend.setOnClickListener(onClickListener);
	}
	OnClickListener onClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.ll_ask:
				//startActivity(new Intent(AskInfoActivity.this, RegisterInfoActivity.class));
				break;
			case R.id.ll_friend_ask:
				startActivity(new Intent(AskInfoActivity.this, FriendQuestionActivity.class));
				break;
			case R.id.bt_ask_friend:
				startActivity(new Intent(AskInfoActivity.this, QuestionActivity.class));
				break;
			}
		}		
	};
}
