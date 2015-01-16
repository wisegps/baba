package com.wise.notice;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.Info;
import pubclas.Info.FriendStatus;
import pubclas.NetThread;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.wise.baba.AppApplication;
import com.wise.baba.R;

import data.FriendData;

/**
 * 个人信息界面
 * 
 * @author honesty
 **/
public class FriendInfoActivity extends Activity {

	private static final int get_customer = 1;
	private static final int add_friend = 2;
	private static final int delete_friend = 3;

	Button bt_add_friend, bt_send_message, bt_find_location, bt_management;
	ImageView iv_logo, iv_sex, iv_service, iv_menu;
	TextView tv_name, tv_area;

	RequestQueue mQueue;
	AppApplication app;
	int FriendId = 0;
	String FriendName;
	/** 判断当前页面的状态 **/
	FriendStatus friendStatus;

	enum abc {
		a, b
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_friend_info);
		app = (AppApplication) getApplication();
		mQueue = Volley.newRequestQueue(this);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		iv_menu = (ImageView) findViewById(R.id.iv_menu);
		iv_menu.setOnClickListener(onClickListener);
		iv_logo = (ImageView) findViewById(R.id.iv_logo);
		iv_sex = (ImageView) findViewById(R.id.iv_sex);
		iv_service = (ImageView) findViewById(R.id.iv_service);
		tv_name = (TextView) findViewById(R.id.tv_name);
		tv_area = (TextView) findViewById(R.id.tv_area);
		bt_add_friend = (Button) findViewById(R.id.bt_add_friend);
		bt_add_friend.setOnClickListener(onClickListener);
		bt_send_message = (Button) findViewById(R.id.bt_send_message);
		bt_send_message.setOnClickListener(onClickListener);
		bt_find_location = (Button) findViewById(R.id.bt_find_location);
		bt_find_location.setOnClickListener(onClickListener);
		bt_management = (Button) findViewById(R.id.bt_management);
		bt_management.setOnClickListener(onClickListener);
		Intent intent = getIntent();
		friendStatus = (FriendStatus) intent.getSerializableExtra(Info.FriendStatusKey);
		String Friendid = intent.getStringExtra("FriendId");
		String name = intent.getStringExtra("name");

		if (friendStatus == FriendStatus.FriendInfo) {
			FriendId = Integer.valueOf(Friendid);
			getFriendInfoId();
			bt_find_location.setVisibility(View.VISIBLE);
			iv_menu.setVisibility(View.VISIBLE);
		} else if (friendStatus == FriendStatus.FriendAddFromName) {
			iv_menu.setVisibility(View.GONE);
			bt_find_location.setVisibility(View.GONE);
			getFriendInfoName(name);

		} else if (friendStatus == FriendStatus.FriendAddFromId) {
			iv_menu.setVisibility(View.GONE);
			bt_find_location.setVisibility(View.GONE);
			FriendId = Integer.valueOf(Friendid);
			getFriendInfoId();
			judgeIsAddFriend();
		}
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.bt_add_friend:
				addFriend();
				break;
			case R.id.bt_send_message:
				Intent intent = new Intent(FriendInfoActivity.this, LetterActivity.class);
				intent.putExtra("cust_id", "" + FriendId);
				intent.putExtra("cust_name", FriendName);
				startActivity(intent);
				break;
			case R.id.bt_find_location:
				// TODO 查看好友位置信息
				Intent intentLocation = new Intent(FriendInfoActivity.this, FriendLocationActivity.class);
				intentLocation.putExtra("FriendId", FriendId);
				startActivity(intentLocation);
				break;
			case R.id.iv_menu:
				showMenu();
				break;
			case R.id.tv_compet:
				startActivity(new Intent(FriendInfoActivity.this, SetCompetActivity.class));
				mPopupWindow.dismiss();
				break;
			case R.id.tv_delete:
				// TODO 删除好友
				deleteFriend();
				mPopupWindow.dismiss();
				break;
			case R.id.bt_management:

				break;
			}
		}
	};
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case get_customer:
				jsonFriendInfo(msg.obj.toString());
				break;
			case add_friend:
				jsonAddFriend(msg.obj.toString());
				break;
			case delete_friend:
				jsonDeleteFriend(msg.obj.toString());
				break;
			}
		}
	};

	/** 删除好友 **/
	private void deleteFriend() {
		String url = Constant.BaseUrl + "customer/" + app.cust_id + "/friend/" + FriendId + "?auth_code=" + app.auth_code;
		new NetThread.DeleteThread(handler, url, delete_friend).start();
	}

	private void jsonDeleteFriend(String result) {
		try {
			JSONObject jsonObject = new JSONObject(result);
			int status_code = jsonObject.getInt("status_code");
			if (status_code == 0) {
				Intent intent = new Intent();
				intent.putExtra("FriendId", FriendId);
				setResult(2, intent);
				finish();
			} else {
				Toast.makeText(FriendInfoActivity.this, "删除好友失败", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(FriendInfoActivity.this, "删除好友失败", Toast.LENGTH_SHORT).show();
		}
	}

	/** 判断好友是否已经添加 **/
	private void judgeIsAddFriend() {
		if (app.cust_id.equals(String.valueOf(FriendId))) {
			// 自己
			bt_add_friend.setVisibility(View.GONE);
			bt_send_message.setVisibility(View.GONE);
			return;
		}
		for (FriendData friendData : app.friendDatas) {
			if (friendData.getFriend_id() == FriendId) {
				// 好友已存在
				bt_add_friend.setVisibility(View.GONE);
				bt_send_message.setVisibility(View.VISIBLE);
				return;
			}
		}
		// 好友不存在，可以添加好友
		bt_add_friend.setVisibility(View.VISIBLE);
		bt_send_message.setVisibility(View.GONE);
	}

	private void addFriend() {
		String url = Constant.BaseUrl + "customer/" + app.cust_id + "/send_friend_request?auth_code=" + app.auth_code;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("friend_id", String.valueOf(FriendId)));
		params.add(new BasicNameValuePair("cust_name", app.cust_name));
		new NetThread.postDataThread(handler, url, params, add_friend).start();
	}

	private void jsonAddFriend(String result) {
		// TODO 添加好友
		System.out.println(result);
		try {
			JSONObject jsonObject = new JSONObject(result);
			if (jsonObject.getInt("status_code") == 0) {
				Toast.makeText(FriendInfoActivity.this, "添加成功，等待对方确认!", Toast.LENGTH_SHORT).show();
				setResult(2);
				finish();
			} else {
				Toast.makeText(FriendInfoActivity.this, "添加好友失败，请重试", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 通过名称获取好友信息 **/
	private void getFriendInfoName(String name) {
		String url = Constant.BaseUrl + "customer/search?auth_code=" + app.auth_code + "&account=" + name;
		new NetThread.GetDataThread(handler, url, get_customer).start();
	}

	/** 通过id获取要添加的好友信息 **/
	private void getFriendInfoId() {
		String url = Constant.BaseUrl + "customer/" + FriendId + "?auth_code=" + app.auth_code;
		new NetThread.GetDataThread(handler, url, get_customer).start();
	}

	/** 解析好友信息 **/
	private void jsonFriendInfo(String result) {
		try {
			System.out.println("解析好友信息列表" + result);
			JSONObject jsonObject = new JSONObject(result);

			FriendId = jsonObject.getInt("cust_id");
			judgeIsAddFriend();
			FriendName = jsonObject.getString("cust_name");
			tv_name.setText(FriendName);
			tv_area.setText(jsonObject.getString("province") + "    " + jsonObject.getString("city"));
			Bitmap bimage = BitmapFactory.decodeFile(Constant.userIconPath + FriendId + ".png");
			if (bimage != null) {
				iv_logo.setImageBitmap(bimage);
			}
			String sex = jsonObject.getString("sex");
			if (sex.equals("0")) {
				iv_sex.setImageResource(R.drawable.icon_man);
			} else {
				iv_sex.setImageResource(R.drawable.icon_woman);
			}
			int cust_type = jsonObject.getInt("cust_type");
			// 如果是服务商显示标志
			if (cust_type == 2) {
				iv_service.setVisibility(View.VISIBLE);
			} else {
				iv_service.setVisibility(View.GONE);
			}
			String logo = jsonObject.getString("logo");
			if (logo == null || logo.equals("")) {

			} else {
				mQueue.add(new ImageRequest(logo, new Response.Listener<Bitmap>() {
					@Override
					public void onResponse(Bitmap response) {
						GetSystem.saveImageSD(response, Constant.userIconPath, FriendId + ".png", 100);
						iv_logo.setImageBitmap(response);
					}
				}, 0, 0, Config.RGB_565, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						error.printStackTrace();
					}
				}));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	PopupWindow mPopupWindow;

	private void showMenu() {
		LayoutInflater mLayoutInflater = LayoutInflater.from(FriendInfoActivity.this);
		View popunwindwow = mLayoutInflater.inflate(R.layout.pop_friend_info, null);
		TextView tv_compet = (TextView) popunwindwow.findViewById(R.id.tv_compet);
		tv_compet.setOnClickListener(onClickListener);
		TextView tv_delete = (TextView) popunwindwow.findViewById(R.id.tv_delete);
		tv_delete.setOnClickListener(onClickListener);
		mPopupWindow = new PopupWindow(popunwindwow, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mPopupWindow.setAnimationStyle(R.style.PopupAnimation);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopupWindow.setFocusable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.showAsDropDown(FriendInfoActivity.this.findViewById(R.id.iv_menu), 0, 0);
	}
}