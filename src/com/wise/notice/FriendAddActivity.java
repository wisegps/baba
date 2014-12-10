package com.wise.notice;

import com.wise.baba.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * @author honesty
 **/
public class FriendAddActivity extends Activity {
	EditText et_name;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_add_friend);
		et_name = (EditText)findViewById(R.id.et_name);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		ImageView iv_search = (ImageView)findViewById(R.id.iv_search);
		iv_search.setOnClickListener(onClickListener);
		//et_name.setImeOptions(EditorInfo.IME_ACTION_SEND);
//		et_name.setOnEditorActionListener(new OnEditorActionListener() {
//			@Override
//			public boolean onEditorAction(TextView v, int actionId,
//					KeyEvent event) {
//				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//					((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
//							.hideSoftInputFromWindow(FriendAddActivity.this
//									.getCurrentFocus().getWindowToken(),
//									InputMethodManager.HIDE_NOT_ALWAYS);
//					Intent intent = new Intent(FriendAddActivity.this,
//							FriendInfoActivity.class);
//					intent.putExtra("name", et_name.getText().toString().trim());
//					startActivity(intent);
//				}
//				return false;
//			}
//		});
	}
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.iv_search:
				Intent intent = new Intent(FriendAddActivity.this,
						FriendInfoActivity.class);
				intent.putExtra("name", et_name.getText().toString().trim());
				startActivityForResult(intent, 1);
				break;
			}
		}
	};
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 1 && resultCode == 2){
			//TODO 添加好友返回
			finish();
		}
	}
}