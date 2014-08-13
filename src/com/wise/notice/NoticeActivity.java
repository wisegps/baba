package com.wise.notice;

import com.wise.baba.R;
import com.wise.notice.NoticeFragment.BtnListener;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;

/**通知**/
public class NoticeActivity extends FragmentActivity{
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_notice);
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		NoticeFragment noticeFragment = new NoticeFragment();
        transaction.add(R.id.ll_content, noticeFragment); 
        transaction.commit();
        noticeFragment.SetBtnListener(new BtnListener() {			
			@Override
			public void Back() {
				finish();
			}
		});
	}
}