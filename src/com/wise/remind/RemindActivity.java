package com.wise.remind;

import pubclas.Constant;
import pubclas.NetThread;
import pubclas.Variable;
import com.wise.baba.R;
import data.RemindData;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
/**
 * 提醒明细
 * @author honesty
 */
public class RemindActivity extends Activity{
	private static final int delete = 1;
	int resultCode = 0;
	RemindData remindData;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_remind);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		Button bt_edit = (Button)findViewById(R.id.bt_edit);
		bt_edit.setOnClickListener(onClickListener);
		Button bt_delete = (Button)findViewById(R.id.bt_delete);
		bt_delete.setOnClickListener(onClickListener);
		Button bt_share = (Button)findViewById(R.id.bt_share);
		bt_share.setOnClickListener(onClickListener);
		Button bt_place = (Button)findViewById(R.id.bt_place);
		bt_place.setOnClickListener(onClickListener);
		remindData = (RemindData) getIntent().getSerializableExtra("remindData");
	}
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				setResult(resultCode);
				finish();
				break;
			case R.id.bt_edit:
				Intent intent = new Intent(RemindActivity.this, CarRemindUpdateActivity.class);
				intent.putExtra("remindData", remindData);
				startActivityForResult(intent, 2);
				break;
			case R.id.bt_delete:
				Delete();
				break;

			case R.id.bt_place:
				//ToDealAdress
				break;
			}
		}
	};
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case delete:				
				System.out.println(msg.obj.toString());
				setResult(2);
				finish();
				break;

			default:
				break;
			}
		}		
	};
	private void ToDealAdress() {
        Intent intent = new Intent(RemindActivity.this,
                DealAddressActivity.class);
		if(remindData.getRemind_type() == 1){
	        intent.putExtra("Title", getString(R.string.inspection_title));
	        intent.putExtra("Type", "1");
		}else if(remindData.getRemind_type() == 0){
	        intent.putExtra("Title", "驾照换证");
	        intent.putExtra("Type", "0");
		}
        startActivity(intent);
    }
	private void Delete(){
		String url = Constant.BaseUrl + "reminder/" + remindData.getReminder_id() + "?auth_code=" + Variable.auth_code;
		new Thread(new NetThread.DeleteThread(handler, url, delete)).start();
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == 2){
			resultCode = 2;
			setResult(resultCode);
			finish();
		}
	}
}
