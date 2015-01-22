package com.wise.state;

import listener.OnFinishListener;

import org.json.JSONObject;

import pubclas.Constant;
import pubclas.FaceConversionUtil;
import pubclas.GetLocation;
import pubclas.GetSystem;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import cn.jpush.android.api.JPushInterface;

import com.baidu.lbsapi.auth.LBSAuthManagerListener;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.BNaviEngineManager.NaviEngineInitListener;
import com.umeng.update.UmengUpdateAgent;
import com.wise.baba.R;
import com.wise.notice.NoticeActivity;
import com.wise.remind.RemindListActivity;
import com.wise.violation.TrafficActivity;

import fragment.FragmentFriend;
import fragment.FragmentHome;
import fragment.FragmentMore;
import fragment.FragmentHome.OnExitListener;
import fragment.FragmentNotice;

/**
 * 主界面
 * 
 * @author honesty
 **/
public class MainActivity extends FragmentActivity {
	private static final String TAG = "MainActivity";
	private FragmentManager fragmentManager;
	MyBroadCastReceiver myBroadCastReceiver;
	FragmentHome fragmentHome;
	FragmentNotice fragmentNotice;
	FragmentFriend fragmentFriend;
	FragmentMore fragmentMore;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		Button bt_home = (Button) findViewById(R.id.bt_home);
		bt_home.setOnClickListener(onClickListener);
		Button bt_info = (Button) findViewById(R.id.bt_info);
		bt_info.setOnClickListener(onClickListener);
		Button bt_friend = (Button) findViewById(R.id.bt_friend);
		bt_friend.setOnClickListener(onClickListener);
		Button bt_set = (Button) findViewById(R.id.bt_set);
		bt_set.setOnClickListener(onClickListener);

		System.out.println("onCreate");
		fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		fragmentHome = new FragmentHome();
		transaction.add(R.id.ll_content, fragmentHome);
		transaction.commit();
		fragmentHome.setOnExitListener(new OnExitListener() {
			@Override
			public void exit() {
				finish();
			}
		});

		myBroadCastReceiver = new MyBroadCastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constant.A_RefreshHomeCar);
		intentFilter.addAction(Constant.A_Login);
		intentFilter.addAction(Constant.A_LoginOut);
		intentFilter.addAction(Constant.A_ChangeCards);
		registerReceiver(myBroadCastReceiver, intentFilter);

		// 从通知栏跳转
		boolean isSpecify = getIntent().getBooleanExtra("isSpecify", false);
		if (isSpecify) {
			String extras = getIntent().getExtras().getString(JPushInterface.EXTRA_EXTRA);
			try {
				JSONObject jsonObject = new JSONObject(extras);
				int msg_type = jsonObject.getInt("msg_type");
				if(msg_type == 1){//提醒界面
			        Intent intent = new Intent(MainActivity.this, RemindListActivity.class);
			        startActivity(intent);
				}else if(msg_type == 4){//违章界面
					Intent intent = new Intent(MainActivity.this, TrafficActivity.class);
			        startActivity(intent);
				}else{//跳转到通知界面
					Intent nIntent = new Intent(MainActivity.this, NoticeActivity.class);
					nIntent.putExtra("isSpecify", isSpecify);
					nIntent.putExtras(getIntent().getExtras());
					startActivity(nIntent);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		/**定位**/
		new GetLocation(this);
		UmengUpdateAgent.update(MainActivity.this);
		/** 开启线程初始化表情 **/
		new Thread(new Runnable() {
			@Override
			public void run() {
				FaceConversionUtil.getInstace().getFileText(getApplication());
			}
		}).start();
		// 初始化导航，必须
		BaiduNaviManager.getInstance().initEngine(this, getSdcardDir(), mNaviEngineInitListener, new LBSAuthManagerListener() {
			@Override
			public void onAuthResult(int status, String msg) {
				
			}
		});
	}

	private String getSdcardDir() {
		if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStorageDirectory().toString();
		}
		return null;
	}

	private NaviEngineInitListener mNaviEngineInitListener = new NaviEngineInitListener() {
		public void engineInitSuccess() {

		}

		public void engineInitStart() {
		}

		public void engineInitFail() {
		}
	};
	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.bt_home: {
				FragmentTransaction transaction = fragmentManager.beginTransaction();
				hideFragments(transaction);
				if (fragmentHome == null) {
					fragmentHome = new FragmentHome();
					transaction.add(R.id.ll_content, fragmentHome);
					transaction.commit();
				} else {
					transaction.show(fragmentHome);
					transaction.commit();
				}
			}

				break;

			case R.id.bt_info: {
				FragmentTransaction transaction = fragmentManager.beginTransaction();
				hideFragments(transaction);
				if (fragmentNotice == null) {
					fragmentNotice = new FragmentNotice();
					transaction.add(R.id.ll_content, fragmentNotice);
					transaction.commit();
				} else {
					transaction.show(fragmentNotice);
					transaction.commit();
				}
			}

				break;
			case R.id.bt_friend: {
				FragmentTransaction transaction = fragmentManager.beginTransaction();
				hideFragments(transaction);
				if (fragmentFriend == null) {
					fragmentFriend = new FragmentFriend();
					transaction.add(R.id.ll_content, fragmentFriend);
					transaction.commit();
				} else {
					transaction.show(fragmentFriend);
					transaction.commit();
				}
			}

				break;
			case R.id.bt_set:
				showMore();
				break;
			}
		}
	};

	private void hideFragments(FragmentTransaction transaction) {
		if (fragmentHome != null) {
			transaction.hide(fragmentHome);
		}
		if (fragmentNotice != null) {
			transaction.hide(fragmentNotice);
		}
		if (fragmentFriend != null) {
			transaction.hide(fragmentFriend);
		}
	}

	class MyBroadCastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			GetSystem.myLog(TAG, action);
			if (action.equals(Constant.A_RefreshHomeCar)) {
				if (fragmentHome != null) {
					fragmentHome.refreshCarInfo();
				}
			} else if (action.equals(Constant.A_Login)) {// 登录
				if (fragmentHome != null) {
					fragmentHome.resetAllView();//
				}
				if (fragmentNotice != null) {
					fragmentNotice.ResetNotice();// 重置消息
				}
				if (fragmentFriend != null) {
					fragmentFriend.getFriendData();// 获取好友
				}
			} else if (action.equals(Constant.A_LoginOut)) {// 注销账号
				if (fragmentHome != null) {
					fragmentHome.setLoginOutView();// 通知首页账号注销
				}
				if (fragmentNotice != null) {
					fragmentNotice.ClearNotice(); // 清除通知信息
				}
			} else if (action.equals(Constant.A_City)) {
			} else if (action.equals(Constant.A_ChangeCards)) {
				// 卡片有可能改变
				if (fragmentHome != null) {
					fragmentHome.isChangeCards();
				}
			}
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		showMore();
		return true;
	}
	private void showMore(){
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		hideFragments(transaction);
		if (fragmentMore == null) {
			fragmentMore = new FragmentMore();
			fragmentMore.setOnFinishListener(new OnFinishListener() {
				@Override
				public void onFinish() {
					finish();
				}
			});
			
			transaction.add(R.id.ll_content, fragmentMore);
			transaction.commit();
		} else {
			transaction.show(fragmentMore);
			transaction.commit();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/** 更多页面点击退出系统 **/
		if (requestCode == 5 && resultCode == 1) {
			finish();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			startActivity(intent);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(myBroadCastReceiver);
	}
}
