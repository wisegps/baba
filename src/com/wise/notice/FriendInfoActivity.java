package com.wise.notice;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
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
import com.wise.baba.app.Constant;
import com.wise.baba.biz.DBFriendAuth;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.biz.HttpFriend;
import com.wise.baba.db.dao.DaoMaster;
import com.wise.baba.db.dao.DaoSession;
import com.wise.baba.db.dao.FriendAuth;
import com.wise.baba.db.dao.FriendAuthDao;
import com.wise.baba.db.dao.FriendAuthDao.Properties;
import com.wise.baba.db.dao.FriendDataDao;
import com.wise.baba.db.dao.DaoMaster.DevOpenHelper;
import com.wise.baba.entity.Info;
import com.wise.baba.entity.Info.FriendStatus;
import com.wise.baba.net.NetThread;
import com.wise.state.ManageActivity;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * 个人信息界面
 * 
 * @author honesty
 **/
public class FriendInfoActivity extends Activity implements Callback {

	private static final int get_customer = 1;
	private static final int delete_friend = 3;
	private static final int get_authToMe = 2;
	private int RIGHT_LOCATION = 0x6005; // 访问车辆实时位置（个人好友及服务商）
	private Handler handler;
	private int[] authToMe;// 朋友授权给我的权限
	// private int[] authToFriend;//我授权给朋友的权限
	Button bt_send_message, bt_add_friend, bt_find_location, bt_management;
	ImageView iv_logo, iv_sex, iv_service, iv_menu;
	TextView tv_name, tv_area;

	RequestQueue mQueue;
	private HttpFriend httpFriend;
	AppApplication app;
	int FriendId = 0;
	String FriendName;

	enum abc {
		a, b
	};
	
	

	private DBFriendAuth friendAuthDB = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_friend_info);
		app = (AppApplication) getApplication();
		mQueue = Volley.newRequestQueue(this);
		handler = new Handler(this);
		httpFriend = new HttpFriend(this, handler);
		friendAuthDB = new DBFriendAuth(this);
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
		String Friendid = intent.getStringExtra("FriendId");
		cust_type = intent.getIntExtra("cust_type", 1);
		String name = intent.getStringExtra("name");

		FriendId = Integer.valueOf(Friendid);
		getFriendInfoId();
		bt_find_location.setVisibility(View.GONE);
		bt_management.setVisibility(View.GONE);
		iv_menu.setVisibility(View.VISIBLE);
		// 得到朋友给予我哪些权限，是否可以查看位置等
		if(cust_type != 2){//服务商不会授权给用户
			getAuthorization(app.cust_id, FriendId + "");
		}
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case get_customer:
			jsonFriendInfo(msg.obj.toString());
			break;
		case get_authToMe:
			authToMe = (int[]) msg.obj;
			friendAuthDB.saveAuthCode(authToMe, app.cust_id, FriendId+"");
			showViewByAuthCode();
			break;
		case delete_friend:
			jsonDeleteFriend(msg.obj.toString());
			break;
		}
		return false;
	}

	/**
	 * 根据权限设置一些按钮是否可见
	 */
	public void showViewByAuthCode() {
		if (authToMe != null) {
			if(app.cust_type != 2 && cust_type != 2){//用户对用户
				if(authToMe.length > 0){
					bt_find_location.setVisibility(View.VISIBLE);
				}
			}else if(app.cust_type == 2 && cust_type != 2){//服务商对用户
				if(authToMe.length > 0){
					bt_management.setVisibility(View.VISIBLE);
				}
			}			
		} else {
			bt_management.setVisibility(View.GONE);
			bt_find_location.setVisibility(View.GONE);
		}
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
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
				intent = new Intent(FriendInfoActivity.this, SetCompetActivity.class);
				intent.putExtra("friendId", FriendId);
				int visible = iv_service.getVisibility();
				boolean isService = visible == View.VISIBLE ? true : false;
				intent.putExtra("isService", isService);
				// intent.putExtra("authCode", authToMe);
				startActivity(intent);
				mPopupWindow.dismiss();
				break;
			case R.id.tv_delete:
				// TODO 删除好友
				deleteFriend();
				mPopupWindow.dismiss();
				break;
			case R.id.bt_management:
				Intent manageLocation = new Intent(FriendInfoActivity.this, ManageActivity.class);
				manageLocation.putExtra("FriendId", FriendId);
				manageLocation.putExtra("authToMe", authToMe);
				startActivity(manageLocation);
				break;
			}
		}
	};

	/**
	 * 获取有哪些权限
	 * 
	 */
	public void getAuthorization(String id, String friendId) {
		/*
		 * 先从数据库中获取
		 */
		authToMe =  friendAuthDB.queryAuthCode(id, friendId);
		/*
		 * 根据权限界面上调整控件显示
		 */
		showViewByAuthCode();
		
		/*
		 * 再从网络获取
		 */
		String url = "http://api.bibibaba.cn/customer/" + id + "/friend/" + friendId + "/rights?auth_code=" + app.auth_code;
		Log.i("FriendInfoActivity", "获取有哪些权限 url "+ url);
		
		httpFriend.getAuthCode(url);
		
		/*
		 * 网络获取之后再调整控件
		 */

	}

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

	/** 通过id获取要添加的好友信息 **/
	private void getFriendInfoId() {
		String url = Constant.BaseUrl + "customer/" + FriendId + "?auth_code=" + app.auth_code;
		
		Log.i("FriendInfoActivity", url);
		new NetThread.GetDataThread(handler, url, get_customer).start();
	}

	int cust_type = -1;

	/** 解析好友信息 **/
	private void jsonFriendInfo(String result) {
		try {
			System.out.println("解析好友信息列表" + result);
			JSONObject jsonObject = new JSONObject(result);
			FriendId = jsonObject.getInt("cust_id");
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
			cust_type = jsonObject.getInt("cust_type");
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