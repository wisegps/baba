package com.wise.notice;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pubclas.Constant;
import pubclas.Info;
import pubclas.MyVolley;
import pubclas.NetThread;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.wise.baba.AppApplication;
import com.wise.baba.R;

import data.FriendSearch;

/**
 * @author honesty
 **/
public class FriendAddActivity extends Activity implements Callback {
	private EditText et_name;
	private AppApplication app;
	private Handler handler;
	private MyVolley volley;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_add_friend);
		et_name = (EditText) findViewById(R.id.et_name);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		ImageView iv_search = (ImageView) findViewById(R.id.iv_search);
		iv_search.setOnClickListener(onClickListener);
		app = (AppApplication) getApplication();
		handler = new Handler(this);
		volley = new MyVolley(this, handler);
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.iv_search:
				String name = et_name.getText().toString().trim();
				if (name.equals("")) {
					toast("不能为空哦");
					return;
				}
				searchByName(name);
				break;
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1 && resultCode == 2) {
			// TODO 添加好友返回
			finish();
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		parseJsonString((String) msg.obj);
		return true;
	}

	public void parseJsonString(String strJson) {
		Gson gson = new Gson();
		if (strJson.startsWith("[]")) {
			et_name.setText("");
			toast("搜索结果为空！");
			return;
		} else if (strJson.startsWith("{")) {// 只有一条数据
			FriendSearch friend = gson.fromJson(strJson, FriendSearch.class);
			//toast("一条数据" + friend.getCust_name());
			Intent intent = new Intent(this,
					FriendDetailActivity.class);
			intent.putExtra(Info.FriendStatusKey,
					Info.FriendStatus.FriendAddFromName);
			intent.putExtra("friend", (Serializable)friend);
			startActivityForResult(intent, 1);
			
			
		} else {// 搜索到多个好友
			List<FriendSearch> friends = new LinkedList<FriendSearch>();
			friends = gson.fromJson(strJson,
					new TypeToken<List<FriendSearch>>() {
					}.getType());
			Intent intent = new Intent(FriendAddActivity.this,
					FriendListActivity.class);
			intent.putExtra("friends", (Serializable) friends);
			startActivityForResult(intent, 1);
		}

	}

	/** 通过名称获取好友信息 **/
	private void searchByName(String name) {
		try {
			name = URLEncoder.encode(name, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String url = Constant.BaseUrl + "customer/search?auth_code="
				+ app.auth_code + "&account=" + name;
		volley.request(url);
	}

	public void toast(String info) {
		Toast toast = Toast.makeText(FriendAddActivity.this, info,
				Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	@Override
	protected void onStop() {
		//取消网络请求
		volley.cancle();
		super.onStop();
	}
	
	
}