package com.wise.baba;

import org.json.JSONObject;
import pubclas.GetSystem;
import pubclas.Judge;
import cn.jpush.android.api.JPushInterface;
import com.wise.notice.NoticeActivity;
import com.wise.remind.RemindListActivity;
import com.wise.setting.LoginActivity;
import com.wise.setting.SetActivity;
import com.wise.violation.TrafficActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class MoreActivity extends Activity{
	private static final String TAG = "MoreActivity";
	ImageView iv_noti,iv_vio;
	
	public static final int SMS = 1;//传递信息页面跳转类型
	public static final int COLLCETION = 2;//收藏
	public static final int TRAFFIC = 3;//违章
	public static final int REMIND = 4;//提醒
//	public static final int SET = 5;//设置
	AppApplication app;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		app = (AppApplication)getApplication();
		setContentView(R.layout.activity_more);		
		TextView tv_set = (TextView)findViewById(R.id.tv_set);
		tv_set.setOnClickListener(onClickListener);
		TextView tv_sms = (TextView)findViewById(R.id.tv_sms);
		tv_sms.setOnClickListener(onClickListener);
		TextView tv_collection = (TextView)findViewById(R.id.tv_collection);
		tv_collection.setOnClickListener(onClickListener);
		TextView tv_remind = (TextView)findViewById(R.id.tv_remind);
		tv_remind.setOnClickListener(onClickListener);
		TextView tv_traffic = (TextView)findViewById(R.id.tv_traffic);
		tv_traffic.setOnClickListener(onClickListener);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		iv_noti = (ImageView)findViewById(R.id.iv_noti);
		iv_vio = (ImageView)findViewById(R.id.iv_vio);
		
		Intent intent1 = getIntent();		
		boolean isSpecify = intent1.getBooleanExtra("isSpecify", false);
		if(isSpecify){
			String extras = intent1.getExtras().getString(JPushInterface.EXTRA_EXTRA);
			try {
				JSONObject jsonObject = new JSONObject(extras);
				int msg_type = jsonObject.getInt("msg_type");
				if(msg_type == 1){//提醒界面
			        Intent intent = new Intent(MoreActivity.this, RemindListActivity.class);
			        startActivity(intent);
				}else if(msg_type == 4){//违章界面
					Intent intent = new Intent(MoreActivity.this, TrafficActivity.class);
			        startActivity(intent);
				}else{//跳转到通知界面
					Intent nIntent = new Intent(MoreActivity.this, NoticeActivity.class);
					nIntent.putExtra("isSpecify", isSpecify);
					nIntent.putExtras(intent1.getExtras());
					startActivity(nIntent);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	OnClickListener onClickListener = new OnClickListener() {	
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.tv_sms:				
				if(Judge.isLogin(app)){
					app.noti_count = 0;
					startActivity(new Intent(MoreActivity.this, NoticeActivity.class));
				}else{
					//TODO 传送类型跳转类型
					Intent intent = new Intent(MoreActivity.this, LoginActivity.class);
					intent.putExtra("ActivityState", SMS);
					startActivity(intent);
				}
				break;
			case R.id.tv_collection:
				if(Judge.isLogin(app)){
					startActivity(new Intent(MoreActivity.this, CollectionActivity.class));
				}else{
					Intent intent = new Intent(MoreActivity.this, LoginActivity.class);
					intent.putExtra("ActivityState", COLLCETION);
					startActivity(intent);
				}
				break;
			case R.id.tv_remind:
				if(Judge.isLogin(app)){
					startActivity(new Intent(MoreActivity.this, RemindListActivity.class));
				}else{
					Intent intent = new Intent(MoreActivity.this, LoginActivity.class);
					intent.putExtra("ActivityState", REMIND);
					startActivity(intent);
				}
				break;
			case R.id.tv_traffic:
				if(Judge.isLogin(app)){
					app.vio_count = 0;
					startActivity(new Intent(MoreActivity.this, TrafficActivity.class));
				}else{
					Intent intent = new Intent(MoreActivity.this, LoginActivity.class);
					intent.putExtra("ActivityState", TRAFFIC);
					startActivity(intent);
				}
				break;
			case R.id.tv_set:
				startActivityForResult(new Intent(MoreActivity.this, SetActivity.class), 1);
				break;
			}
		}
	};
	
	@Override
	protected void onResume() {
		super.onResume();
		setNotiView();
	}
	/**设置提醒**/
	private void setNotiView(){
		if(app.noti_count == 0){
			iv_noti.setVisibility(View.GONE);
		}else{
			iv_noti.setVisibility(View.VISIBLE);
		}
		if(app.vio_count == 0){
			iv_vio.setVisibility(View.GONE);
		}else{
			iv_vio.setVisibility(View.VISIBLE);
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 1 && resultCode == 1){
			GetSystem.myLog(TAG, "登录返回");
			finish();
		}
	}
}