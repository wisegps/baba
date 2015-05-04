package com.wise.notice;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;
import cn.jpush.android.api.JPushInterface;

import com.wise.baba.R;
import com.wise.baba.ui.fragment.FragmentNotice;
import com.wise.baba.ui.fragment.FragmentNotice.BtnListener;


/** 通知 **/
public class NoticeActivity extends FragmentActivity {
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_notice);
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		FragmentNotice noticeFragment = new FragmentNotice();
		transaction.add(R.id.ll_content, noticeFragment);
		transaction.commit();
		noticeFragment.SetBtnListener(new BtnListener() {
			@Override
			public void Back() {
				finish();
			}
		});
		noticeFragment.setBackButtonVISIBLE();

		Intent intent1 = getIntent();
		boolean isSpecify = intent1.getBooleanExtra("isSpecify", false);
		if (isSpecify) {
			String extras = intent1.getExtras().getString(JPushInterface.EXTRA_EXTRA);
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(extras);
				int msg_type = jsonObject.getInt("msg_type");
				if (jsonObject.opt("friend_id") == null) {
					Intent intent = new Intent(NoticeActivity.this, SmsActivity.class);
					intent.putExtra("type", msg_type);
					startActivity(intent);
				} else {
					Intent intent = new Intent(NoticeActivity.this, LetterActivity.class);
					intent.putExtra("cust_id", jsonObject.getString("friend_id"));
					startActivity(intent);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}