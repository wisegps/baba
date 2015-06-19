package com.wise.notice;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.db.dao.FriendData;
import com.wise.baba.entity.FriendSearch;
import com.wise.baba.net.NetThread;


/**
 * 添加好友界面
 * 
 * @author cyy
 **/
public class FriendDetailActivity extends Activity implements OnClickListener,Callback{

	private FriendSearch friend;// 通过搜索查找到的好友信息
	private RequestQueue mQueue;
	private ImageView imgBack, imgMenu, imgLogo, imgSex, imgService;
	private TextView textService, textName, textArea;
	private Button btnAddFriend,btnManagement, btnLocation, btnSendMsg;
	private AppApplication app;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_friend_info);
		app = (AppApplication) getApplication();
		mQueue = Volley.newRequestQueue(this);
		Intent intent = this.getIntent();
		friend = (FriendSearch) intent.getSerializableExtra("friend");
		initView();
	};

	/**
	 * 初始化界面控件
	 */
	public void initView() {

		imgBack = (ImageView) findViewById(R.id.iv_back);
		imgMenu = (ImageView) findViewById(R.id.iv_menu);
		imgLogo = (ImageView) findViewById(R.id.iv_logo);
		imgSex = (ImageView) findViewById(R.id.iv_sex);
		imgService = (ImageView) findViewById(R.id.iv_service);
		textName = (TextView) findViewById(R.id.tv_name);
		textArea = (TextView) findViewById(R.id.tv_area);
		btnAddFriend = (Button) findViewById(R.id.bt_add_friend);
		btnLocation = (Button) findViewById(R.id.bt_find_location);
		btnSendMsg = (Button) findViewById(R.id.bt_send_message);
		btnManagement = (Button) findViewById(R.id.bt_management);

		// 设置一些无关控件不可见
		imgMenu.setVisibility(View.GONE);
		btnLocation.setVisibility(View.GONE);
		btnSendMsg.setVisibility(View.GONE);
		btnAddFriend.setVisibility(View.VISIBLE);
		btnManagement.setVisibility(View.GONE);
		// 设置从上个页面传过来的值
		String sex = friend.getSex();
		String name = friend.getCust_name();
		int type = friend.getCust_type();
		final int id = friend.getCust_id();
		String city = friend.getCity();
		String logo = friend.getLogo();
		String province = friend.getProvince();

		textName.setText(name);
		textArea.setText(province + "    " + city);

		if (sex.equals("0")) {
			imgSex.setImageResource(R.drawable.icon_man);
		} else {
			imgSex.setImageResource(R.drawable.icon_woman);
		}
		// 如果是服务商显示标志
		if (type == 2) {
			imgService.setVisibility(View.VISIBLE);
		} else {
			imgService.setVisibility(View.GONE);
		}
		
		//设置头像图片
		if (new File(Constant.userIconPath + id + ".png").exists()) {
			Bitmap image = BitmapFactory.decodeFile(Constant.userIconPath + id + ".png");
			imgLogo.setImageBitmap(image);
		}else if (logo.startsWith("http://")) {
			Listener<Bitmap> listener = new Response.Listener<Bitmap>() {
				@Override
				public void onResponse(Bitmap response) {
					GetSystem.saveImageSD(response, Constant.userIconPath, id
							+ ".png", 100);
					imgLogo.setImageBitmap(response);
				}
			};

			ErrorListener errorListener = new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					error.printStackTrace();
				}
			};
			ImageRequest imgRequest = new ImageRequest(logo, listener, 0, 0,
					Config.RGB_565, errorListener);
			mQueue.add(imgRequest);
		}
		
		btnAddFriend.setOnClickListener(this);
		imgBack.setOnClickListener(this);
		
		judgeIsAddFriend();
	}
	
	private void addFriend() {
		String url = Constant.BaseUrl + "customer/" + app.cust_id
				+ "/send_friend_request?auth_code=" + app.auth_code;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("friend_id", String.valueOf(friend.getCust_id())));
		params.add(new BasicNameValuePair("cust_name", app.cust_name));
		new NetThread.postDataThread(new Handler(this), url, params, 2).start();
	}

	private void jsonAddFriend(String result) {
		// TODO 添加好友
		System.out.println(result);
		try {
			JSONObject jsonObject = new JSONObject(result);
			if (jsonObject.getInt("status_code") == 0) {
				Toast.makeText(FriendDetailActivity.this, "添加成功，等待对方确认!",
						Toast.LENGTH_SHORT).show();
				setResult(2);
				finish();
			} else {
				Toast.makeText(FriendDetailActivity.this, "添加好友失败，请重试",
						Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/** 判断好友是否已经添加 **/
	private void judgeIsAddFriend() {
		int id = friend.getCust_id();
		if (app.cust_id.equals(String.valueOf(id))) {
			// 自己
			btnAddFriend.setVisibility(View.GONE);
			return;
		}
		for (FriendData friendData : app.friendDatas) {
			
			if (friendData.getFriend_id()!=null &&  friendData.getFriend_id() == id) {
				// 好友已存在
				btnAddFriend.setVisibility(View.GONE);
				return;
			}
		}
		// 好友不存在，可以添加好友
		btnAddFriend.setVisibility(View.VISIBLE);
		
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {
		case R.id.iv_back:
			this.finish();
			break;
		case R.id.bt_add_friend:
			addFriend();
			break;

		}

	}

	@Override
	public boolean handleMessage(Message msg) {
		jsonAddFriend(msg.obj.toString());
		
		return true;
	}
}