package com.wise.baba;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

public class AskActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ask);
		RelativeLayout rl_new_ask = (RelativeLayout)findViewById(R.id.rl_new_ask);
		rl_new_ask.setOnClickListener(onClickListener);
	}
	OnClickListener onClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.rl_new_ask:
				startActivity(new Intent(AskActivity.this, AskInfoActivity.class));
				break;
			}
		}		
	};
}
