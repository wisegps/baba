package com.wise.state;

import pubclas.Constant;
import pubclas.FaceConversionUtil;
import pubclas.GetSystem;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

import com.umeng.update.UmengUpdateAgent;
import com.wise.baba.MoreActivity;
import com.wise.baba.R;

import fragment.FragmentFriend;
import fragment.FragmentHome;
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
		intentFilter.addAction(Constant.A_ChangeCustomerType);
		registerReceiver(myBroadCastReceiver, intentFilter);

		// 从通知栏跳转
		boolean isSpecify = getIntent().getBooleanExtra("isSpecify", false);
		if (isSpecify) {
			Intent intent = new Intent(MainActivity.this, MoreActivity.class);
			intent.putExtra("isSpecify", isSpecify);
			intent.putExtras(getIntent().getExtras());
			startActivityForResult(intent, 5);
		}

		UmengUpdateAgent.update(MainActivity.this);
		/** 开启线程初始化表情 **/
		new Thread(new Runnable() {
			@Override
			public void run() {
				FaceConversionUtil.getInstace().getFileText(getApplication());
			}
		}).start();
	}

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
				startActivityForResult(new Intent(MainActivity.this, MoreActivity.class), 5);
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
				GetSystem.myLog(TAG, "A_RefreshHomeCar");
				if (fragmentHome != null) {
					fragmentHome.resetAllView();
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
				// try {
				// app.City = intent.getStringExtra("City");
				// app.Province = intent.getStringExtra("Province");
				// SharedPreferences preferences =
				// getActivity().getSharedPreferences(Constant.sharedPreferencesName,
				// Context.MODE_PRIVATE);
				// Editor editor = preferences.edit();
				// editor.putString(Constant.sp_city, app.City);
				// editor.putString(Constant.sp_province, app.Province);
				// editor.commit();
				// } catch (Exception e) {
				// e.printStackTrace();
				// }
			} else if (action.equals(Constant.A_ChangeCustomerType)) {
				// 类型改变，关闭界面
				finish();
			}
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		startActivityForResult(new Intent(MainActivity.this, MoreActivity.class), 5);
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
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(myBroadCastReceiver);
	}
}
