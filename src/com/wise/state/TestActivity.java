package com.wise.state;

import com.wise.baba.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class TestActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		Intent intent = getIntent();
		System.out.println(intent.getAction());
		System.out.println(intent.getType());
		boolean isSTREAM =  intent.hasExtra("android.intent.extra.STREAM");
		boolean isTEXT = intent.hasExtra("android.intent.extra.TEXT");
		System.out.println("isSTREAM = " + isSTREAM + " , isTEXT = " + isTEXT);
		System.out.println(intent.getStringExtra("android.intent.extra.TEXT"));
		System.out.println(intent.getStringExtra(Intent.EXTRA_TEXT));
	}
}
