package com.wise.baba.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
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
import com.wise.baba.biz.GetSystem;
import com.wise.baba.db.dao.FriendData;
import com.wise.baba.net.NetThread;
import com.wise.notice.LetterActivity;
import com.wise.notice.SetCompetActivity;


/**
 *@author honesty
 **/
public class FragmentPersionInfo extends Fragment{
	
	private static final int get_customer = 1;
	private static final int add_friend = 2;
	
	Button bt_add_friend,bt_send_message;
	ImageView iv_logo,iv_sex,iv_service,iv_menu;
	TextView tv_name,tv_area;
	
	RequestQueue mQueue;
	AppApplication app;
	static boolean isService;
	static int FriendId = 0;
	String FriendName;
	String name = "honesty";
	
	public static FragmentPersionInfo newInstance(int Friend_id , boolean is_Service){
		FragmentPersionInfo fragmentPersionInfo = new FragmentPersionInfo();
		FriendId = Friend_id;
		isService = is_Service;
		return fragmentPersionInfo;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.activity_friend_info, container, false);
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		app = (AppApplication)getActivity().getApplication();
		mQueue = Volley.newRequestQueue(getActivity());
		ImageView iv_back = (ImageView)getActivity().findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		iv_menu = (ImageView)getActivity().findViewById(R.id.iv_menu);
		iv_menu.setOnClickListener(onClickListener);
		iv_logo = (ImageView)getActivity().findViewById(R.id.iv_logo);
		iv_sex = (ImageView)getActivity().findViewById(R.id.iv_sex);
		iv_service = (ImageView)getActivity().findViewById(R.id.iv_service);
		tv_name = (TextView)getActivity().findViewById(R.id.tv_name);
		tv_area = (TextView)getActivity().findViewById(R.id.tv_area);
		bt_add_friend = (Button)getActivity().findViewById(R.id.bt_add_friend);
		bt_add_friend.setOnClickListener(onClickListener);
		bt_send_message = (Button)getActivity().findViewById(R.id.bt_send_message);
		bt_send_message.setOnClickListener(onClickListener);
		System.out.println("isService = " + isService);
		if(isService){
			iv_back.setVisibility(View.INVISIBLE);
			getFriendInfoId();
		}else{
			iv_back.setVisibility(View.VISIBLE);
			if(FriendId == 0){
				//通过搜索跳转
				getFriendInfoName(name);
			}else{
				//扫描二维码跳转
				getFriendInfoId();
				judgeIsAddFriend();
			}
		}		
	}
	OnClickListener onClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				//finish();
				break;
			case R.id.bt_add_friend:
				addFriend();
				break;
			case R.id.bt_send_message:
				Intent intent = new Intent(getActivity(), LetterActivity.class);
				intent.putExtra("cust_id", ""+ FriendId);
				intent.putExtra("cust_name", FriendName);
				startActivity(intent);
				break;
			case R.id.iv_menu:
				showMenu();
				break;
			case R.id.tv_compet:
				startActivity(new Intent(getActivity(), SetCompetActivity.class));
				mPopupWindow.dismiss();
				break;
			}
		}		
	};
	Handler handler = new Handler(){
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
			}
		}		
	};
	/**判断好友是否已经添加**/
	private void judgeIsAddFriend(){
		if(app.cust_id.equals(String.valueOf(FriendId))){
			//自己
			bt_add_friend.setVisibility(View.GONE);
			bt_send_message.setVisibility(View.GONE);
			return;
		}
		for(FriendData friendData : app.friendDatas){
			if(friendData.getFriend_id() == FriendId){
				//好友已存在
				bt_add_friend.setVisibility(View.GONE);
				bt_send_message.setVisibility(View.VISIBLE);
				return;
			}
		}
		//好友不存在，可以添加好友
		bt_add_friend.setVisibility(View.VISIBLE);
		bt_send_message.setVisibility(View.GONE);
	}
	private void addFriend(){
		String url = Constant.BaseUrl + "customer/" + app.cust_id
				+ "/send_friend_request?auth_code=" + app.auth_code;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("friend_id", String.valueOf(FriendId)));
		params.add(new BasicNameValuePair("cust_name", app.cust_name));
		new NetThread.postDataThread(handler, url, params, add_friend).start();
	}
	private void jsonAddFriend(String result){
		//TODO 添加好友
		System.out.println(result);
		try {
			JSONObject jsonObject = new JSONObject(result);
			if(jsonObject.getInt("status_code") == 0){
				Toast.makeText(getActivity(), "添加成功，等待对方确认!", Toast.LENGTH_SHORT).show();
				getActivity().setResult(2);
				//finish();
			}else{
				Toast.makeText(getActivity(), "添加好友失败，请重试", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**通过名称获取好友信息**/
	private void getFriendInfoName(String name){
		String url = Constant.BaseUrl + "customer/search?auth_code=" + app.auth_code
				+ "&account=" + name;
		new NetThread.GetDataThread(handler, url, get_customer).start();
	}
	/**通过id获取要添加的好友信息**/
	private void getFriendInfoId(){
		String url = Constant.BaseUrl + "customer/" + FriendId
				+ "?auth_code=" + app.auth_code;
		new NetThread.GetDataThread(handler, url, get_customer).start();
	}
	/**解析好友信息**/
	private void jsonFriendInfo(String result){
		try {
			JSONObject jsonObject = new JSONObject(result);	
			FriendId = jsonObject.getInt("cust_id");
			judgeIsAddFriend();
			FriendName = jsonObject.getString("cust_name");
			tv_name.setText(FriendName);
			tv_area.setText(jsonObject.getString("province") + "    " + jsonObject.getString("city"));
	        Bitmap bimage = BitmapFactory.decodeFile(Constant.userIconPath + FriendId + ".png");
	        if(bimage != null){
	        	iv_logo.setImageBitmap(bimage);
	        }
	        String sex = jsonObject.getString("sex");
	        if(sex.equals("0")){
		        iv_sex.setImageResource(R.drawable.icon_man);
	        }else{
	        	iv_sex.setImageResource(R.drawable.icon_woman);
	        }
	        int cust_type = jsonObject.getInt("cust_type");
	        //如果是服务商显示标志
	        if(cust_type == 2){
	        	iv_service.setVisibility(View.VISIBLE);
	        	iv_menu.setVisibility(View.VISIBLE);
	        }else{
	        	iv_service.setVisibility(View.GONE);
	        	iv_menu.setVisibility(View.GONE);
	        }
	        String logo = jsonObject.getString("logo");
	        if(logo == null || logo.equals("")){
	        	
	        }else{
	        	mQueue.add(new ImageRequest(logo, new Response.Listener<Bitmap>() {
					@Override
					public void onResponse(Bitmap response) {
						GetSystem.saveImageSD(response, Constant.userIconPath, FriendId + ".png",100);
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
		LayoutInflater mLayoutInflater = LayoutInflater
				.from(getActivity());
		View popunwindwow = mLayoutInflater.inflate(
				R.layout.pop_friend_info, null);
		TextView tv_compet = (TextView) popunwindwow
				.findViewById(R.id.tv_compet);
		tv_compet.setOnClickListener(onClickListener);
		mPopupWindow = new PopupWindow(popunwindwow, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		mPopupWindow.setAnimationStyle(R.style.PopupAnimation);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopupWindow.setFocusable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.showAsDropDown(getActivity().findViewById(R.id.iv_menu), 0, 0);
	}
}