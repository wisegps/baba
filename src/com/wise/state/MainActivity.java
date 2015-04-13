package com.wise.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
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
import com.wise.baba.ui.widget.NavigationLayout;
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
public class MainActivity extends FragmentActivity implements listener.OnTabChangedListener{
	private static final String TAG = "MainActivity";
	private FragmentManager fragmentManager;
	MyBroadCastReceiver myBroadCastReceiver;
	// FragmentHome fragmentHome;
	// FragmentNotice fragmentNotice;
	// FragmentFriend fragmentFriend;
	// FragmentMore fragmentMore;
	HashMap<String, Fragment> fragments = new HashMap<String, Fragment>();

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		NavigationLayout navigationLayout = (NavigationLayout) findViewById(R.id.navigationLayout);
		navigationLayout.setOnTabChangedListener(this);
		Log.i("MainActivity", "onCreate");
		fragmentManager = getSupportFragmentManager();
		showFragment("home");
		// fragmentHome.setOnExitListener(new OnExitListener() {
		// @Override
		// public void exit() {
		// finish();
		// }
		// });

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
			String extras = getIntent().getExtras().getString(
					JPushInterface.EXTRA_EXTRA);
			try {
				JSONObject jsonObject = new JSONObject(extras);
				int msg_type = jsonObject.getInt("msg_type");
				if (msg_type == 1) {// 提醒界面
					Intent intent = new Intent(MainActivity.this,
							RemindListActivity.class);
					startActivity(intent);
				} else if (msg_type == 4) {// 违章界面
					Intent intent = new Intent(MainActivity.this,
							TrafficActivity.class);
					startActivity(intent);
				} else {// 跳转到通知界面
					Intent nIntent = new Intent(MainActivity.this,
							NoticeActivity.class);
					nIntent.putExtra("isSpecify", isSpecify);
					nIntent.putExtras(getIntent().getExtras());
					startActivity(nIntent);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		/** 定位 **/
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
		BaiduNaviManager.getInstance().initEngine(this, getSdcardDir(),
				mNaviEngineInitListener, new LBSAuthManagerListener() {
					@Override
					public void onAuthResult(int status, String msg) {

					}
				});
	}

	private String getSdcardDir() {
		if (Environment.getExternalStorageState().equalsIgnoreCase(
				Environment.MEDIA_MOUNTED)) {
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

	/**
	 * 
	 * @param 显示某个fragment
	 */
	public void showFragment(String name) {
		Log.i("MainActivity", "显示fragment: " + name);
		// 开启一个事务

		FragmentTransaction trans;
		// 添加相应的fragment
		Fragment fragment = fragments.get(name);

		if (fragment == null) {
			Log.i("MainActivity", "显示fragment  为空");
			if (name.equals("home")) {// 首页
				fragment = new FragmentHome();
				OnExitListener exitListener = new OnExitListener() {
					@Override
					public void exit() {
						finish();
					}
				};
				((FragmentHome) fragment).setOnExitListener(exitListener);
			} else if (name.equals("message")) {// 信息
				fragment = new FragmentNotice();
			} else if (name.equals("friend")) {// 好友
				fragment = new FragmentFriend();
			} else if (name.equals("setting")) {// 设置
				fragment = new FragmentMore();
				OnFinishListener finishListener = new OnFinishListener() {
					@Override
					public void onFinish() {
						finish();
					}
				};
				((FragmentMore) fragment).setOnFinishListener(finishListener);
			}

			fragments.put(name, fragment);
			Fragment last = fragmentManager.findFragmentByTag(name);
			trans = fragmentManager.beginTransaction();
			if(last != null && last.isAdded()){
				Log.i("MainActivity", "删除一个fragment");
				trans.remove(last).commit();
			}
			trans = fragmentManager.beginTransaction();
			trans.add(R.id.content, fragment, name).commit();
		}

		

		// 先隐藏所有fragment
		Iterator<String> keys = fragments.keySet().iterator();
		while (keys.hasNext()) {
			trans = fragmentManager.beginTransaction();
			String key = (String) keys.next();
			Fragment value = fragments.get(key);
			if (value != null) {
				trans.hide(value);
				trans.commit();
			}
		}
		

		trans = fragmentManager.beginTransaction();
		// 然后显示所要显示的fragment
		trans.show(fragment);
		trans.commit();

	}

	class MyBroadCastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			GetSystem.myLog(TAG, action);
			FragmentHome fragmentHome = (FragmentHome) fragments.get("home");
			FragmentNotice fragmentNotice = (FragmentNotice) fragments.get("message");
			FragmentFriend fragmentFriend = (FragmentFriend) fragments.get("friend");
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
					//fragmentHome.isChangeCards();
				}
			}
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		showFragment("setting");
		return true;
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
	public void finish() {
		Log.i("MainActivity", "finish");
		// TODO Auto-generated method stub
		super.finish();

		// 删除所有fragment
		FragmentTransaction trans = null;
		String name[] = {"home","msg","friend","setting"};
		for(int i=0;i<name.length;i++){
			Fragment last = fragmentManager.findFragmentByTag(name[i]);
			if(last !=null){
				trans = fragmentManager.beginTransaction();
				trans.remove(last).commit();
			}
			
		}
		

	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
		unregisterReceiver(myBroadCastReceiver);
	}

	/* (non-Javadoc)
	 * @see listener.OnTabChangedListener#onTabClick(int)
	 */
	@Override
	public void onTabClick(int index) {
		switch (index) {
		case 0:
			showFragment("home");
			break;

		case 1:
			showFragment("message");
			break;
		case 2:
			showFragment("friend");
			break;
		case 3:
			showFragment("setting");
			break;
		}
		
	}

}
