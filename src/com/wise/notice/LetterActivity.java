package com.wise.notice;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.NetThread;
import pubclas.Variable;
import xlist.XListView;
import xlist.XListView.IXListViewListener;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.wise.baba.R;
import customView.CircleImageView;
import customView.WaitLinearLayout.OnFinishListener;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**私信**/
public class LetterActivity extends Activity implements IXListViewListener{
		
	private static final int send_letter = 1;
	private static final int get_data = 2;
	private static final int refresh_data = 3;
	private static final int get_friend_info = 4;
	TextView tv_friend;
	XListView lv_letter;
	List<LetterData> letterDatas = new ArrayList<LetterData>();
	LetterAdapter letterAdapter;
	EditText et_content;
	RequestQueue mQueue;
	Bitmap imageFriend = null;
	Bitmap imageMe = null;
	String cust_id;
	String cust_name;
	String logo;
	MyBroadCastReceiver myBroadCastReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_letter);
		mQueue = Volley.newRequestQueue(this);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		lv_letter = (XListView)findViewById(R.id.lv_letter);
		lv_letter.setPullLoadEnable(false);
		lv_letter.setPullRefreshEnable(true);
		lv_letter.setXListViewListener(this);
		letterAdapter = new LetterAdapter();
		lv_letter.setAdapter(letterAdapter);
		lv_letter.setOnFinishListener(onFinishListener);
		et_content = (EditText)findViewById(R.id.et_content);
		tv_friend = (TextView)findViewById(R.id.tv_friend);
		TextView tv_send = (TextView)findViewById(R.id.tv_send);
		tv_send.setOnClickListener(onClickListener);
		cust_id = getIntent().getStringExtra("cust_id");
		cust_name = getIntent().getStringExtra("cust_name");
		logo = getIntent().getStringExtra("logo");
		tv_friend.setText(cust_name);
		//读取朋友对应的图片
		if(new File(Constant.userIconPath + cust_id + ".png").exists()){
			imageFriend = BitmapFactory.decodeFile(Constant.userIconPath + cust_id + ".png");
		}
		//读取自己对应的图片
		if(new File(Constant.userIconPath + Variable.cust_id + ".png").exists()){
			imageMe = BitmapFactory.decodeFile(Constant.userIconPath + Variable.cust_id + ".png");
		}
		getFristData();
		getLogo();//判断是否有需要从网上读取的图片
		myBroadCastReceiver = new MyBroadCastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constant.A_ReceiverLetter);
		registerReceiver(myBroadCastReceiver, intentFilter);
		getFriendInfo();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(myBroadCastReceiver);
	}
	@Override
	protected void onResume() {
		super.onResume();
	}	
	@Override
	protected void onPause() {
		super.onPause();
	}
	/**如果没有好友信息则获取**/
	private void getFriendInfo(){
		if(cust_name == null || cust_name.equals("") || logo == null || logo.equals("")){
			String url = Constant.BaseUrl + "customer/" + cust_id + "?auth_code=" + Variable.auth_code;
			new NetThread.GetDataThread(handler, url, get_friend_info).start();
		}
	}
	/**解析好友信息**/
	private void jsonFriendInfo(String result){
		try {
			JSONObject jsonObject = new JSONObject(result);
			cust_name = jsonObject.getString("cust_name");
			tv_friend.setText(cust_name);
			getLogo();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.tv_send:
				send();
				break;
			case R.id.iv_back:
				finish();
				break;
			}
		}
	};
	String refresh = "";
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case send_letter:
				jsonSendLetter(msg.obj.toString());
				break;

			case get_data:
				List<LetterData> lDatas = jsonData(msg.obj.toString());
				letterDatas.addAll(lDatas);
				letterAdapter.notifyDataSetChanged();
				lv_letter.setSelection(lv_letter.getBottom());
				break;
			case refresh_data:
				refresh = msg.obj.toString();
				lv_letter.runFast(1);
				break;
			case get_friend_info:
				jsonFriendInfo(msg.obj.toString());
				break;
			}
		}		
	};
	OnFinishListener onFinishListener = new OnFinishListener() {		
		@Override
		public void OnFinish(int index) {
			// Auto-generated method stub
			List<LetterData> lDatas = jsonData(refresh);
			letterDatas.addAll(0,lDatas);
			letterAdapter.notifyDataSetChanged();
			lv_letter.setSelection(lDatas.size());
			onLoadOver();
		}
	};
	private void send(){
		String content = et_content.getText().toString().trim();
		if(content.equals("")){
			Toast.makeText(LetterActivity.this, "发送内容不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		String url = Constant.BaseUrl + "customer/" + Variable.cust_id + "/send_chat?auth_code=" + Variable.auth_code;
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("cust_name", cust_name));
		pairs.add(new BasicNameValuePair("friend_id", cust_id));
		pairs.add(new BasicNameValuePair("type", "0"));
		pairs.add(new BasicNameValuePair("url", ""));
		pairs.add(new BasicNameValuePair("content", content));
		new NetThread.postDataThread(handler, url, pairs, send_letter).start();
		et_content.setText("");
		//添加显示
		LetterData letterData = new LetterData();
		letterData.setContent(content);
		letterData.setType(1);
		letterData.setSend_time(GetSystem.GetNowTime());
		letterDatas.add(letterData);
		letterAdapter.notifyDataSetChanged();
		lv_letter.setSelection(lv_letter.getBottom());
	}
	/**判断发送状态**/
	private void jsonSendLetter(String result){
		try {
			JSONObject jsonObject = new JSONObject(result);
			if(jsonObject.getInt("status_code") == 0){
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void getFristData(){
		String url = Constant.BaseUrl + "customer/" + Variable.cust_id + "/get_chats?auth_code=" +
						Variable.auth_code + "&friend_id=" + cust_id;
		new NetThread.GetDataThread(handler, url, get_data).start();
	}
	private List<LetterData> jsonData(String result){
		List<LetterData> lDatas = new ArrayList<LetterData>();
		try {
			JSONArray jsonArray = new JSONArray(result);
			for(int i = (jsonArray.length() - 1) ; i >= 0 ; i--){
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				LetterData letterData = new LetterData();
				letterData.setChat_id(jsonObject.getInt("chat_id"));
				letterData.setContent(jsonObject.getString("content"));
				String sender_id = jsonObject.getString("sender_id");
				String send_time = GetSystem.ChangeTimeZone(jsonObject.getString("send_time").substring(0, 19).replace("T", " "));
				letterData.setSend_time(send_time);
				if(sender_id.equals(cust_id)){//好友
					letterData.setType(0);
				}else{
					letterData.setType(1);
				}
				lDatas.add(letterData);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return lDatas;
	}
	/**获取头像**/
	private void getLogo(){
		if(imageFriend == null){
			if(logo == null || logo.equals("")){
				
			}else{
				//获取用户头像
				mQueue.add(new ImageRequest(logo, new Response.Listener<Bitmap>() {
					@Override
					public void onResponse(Bitmap response) {
						GetSystem.saveImageSD(response, Constant.userIconPath, cust_id + ".png",100);
						imageFriend = response;
						letterAdapter.notifyDataSetChanged();
					}
				}, 0, 0, Config.RGB_565, null));
			}			
		}
		if(imageMe == null){
			String meLogo = "";
			SharedPreferences preferences = getSharedPreferences(
					Constant.sharedPreferencesName, Context.MODE_PRIVATE);
			String customer = preferences.getString(Constant.sp_customer + Variable.cust_id, "");
			try {
				JSONObject jsonObject = new JSONObject(customer);
				meLogo = jsonObject.getString("logo");
			} catch (Exception e) {
				e.printStackTrace();
			}
			//获取自己头像
			mQueue.add(new ImageRequest(meLogo, new Response.Listener<Bitmap>() {
				@Override
				public void onResponse(Bitmap response) {
					GetSystem.saveImageSD(response, Constant.userIconPath,Variable.cust_id + ".png",100);
					imageMe = response;
					letterAdapter.notifyDataSetChanged();
				}
			}, 0, 0, Config.RGB_565, null));
		}
	}
	
	
	class LetterAdapter extends BaseAdapter{
		LayoutInflater inflater = LayoutInflater.from(LetterActivity.this);
		@Override
		public int getCount() {
			return letterDatas.size();
		}
		@Override
		public Object getItem(int position) {
			return letterDatas.get(position);
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			int Type = getItemViewType(position);
			ViewFriend viewFriend = null;
			ViewMe viewMe = null;
			
			if(convertView == null){
				if(Type == 0){//朋友
					convertView = inflater.inflate(R.layout.item_letter_friend, null);
					viewFriend = new ViewFriend();
					viewFriend.iv_friend = (CircleImageView)convertView.findViewById(R.id.iv_friend);
					viewFriend.tv_friend_content = (TextView)convertView.findViewById(R.id.tv_friend_content);
					viewFriend.tv_time = (TextView)convertView.findViewById(R.id.tv_time);
					convertView.setTag(viewFriend);
				}else{
					convertView = inflater.inflate(R.layout.item_letter_me, null);
					viewMe = new ViewMe();
					viewMe.iv_me = (CircleImageView)convertView.findViewById(R.id.iv_me);
					viewMe.tv_me_content = (TextView)convertView.findViewById(R.id.tv_me_content);
					viewMe.tv_time = (TextView)convertView.findViewById(R.id.tv_time);
					convertView.setTag(viewMe);
				}
			}else{
				if(Type == 0 ){
					viewFriend = (ViewFriend) convertView.getTag();
				}else{
					viewMe = (ViewMe)convertView.getTag();
				}
			}
			LetterData letterData = letterDatas.get(position);
			if((position + 1)>=  letterDatas.size()){
				//最后一条
				String last_time = letterData.getSend_time();
				//得到间隔分钟
				int min = GetSystem.spacingNowTime(last_time);
				if(min >= 5){
					if(Type == 0){//朋友
						viewFriend.tv_time.setVisibility(View.VISIBLE);
						viewFriend.tv_time.setText(last_time.substring(5, 16));
					}else{
						viewMe.tv_time.setVisibility(View.VISIBLE);
						viewMe.tv_time.setText(last_time.substring(5, 16));
					}
				}else{
					if(Type == 0){//朋友
						viewFriend.tv_time.setVisibility(View.GONE);
					}else{
						viewMe.tv_time.setVisibility(View.GONE);
					}
				}
			}else{
				String last_time = letterData.getSend_time();
				String next_time = letterDatas.get(position + 1).getSend_time();
				//得到间隔分钟
				int min = GetSystem.spacingTime(last_time, next_time)/60;
				if(min >= 5){
					if(Type == 0){//朋友
						viewFriend.tv_time.setVisibility(View.VISIBLE);
						viewFriend.tv_time.setText(last_time.substring(5, 16));
					}else{
						viewMe.tv_time.setVisibility(View.VISIBLE);
						viewMe.tv_time.setText(last_time.substring(5, 16));
					}
				}else{
					if(Type == 0){//朋友
						viewFriend.tv_time.setVisibility(View.GONE);
					}else{
						viewMe.tv_time.setVisibility(View.GONE);
					}
				}
			}
			if(Type == 0){//朋友
				viewFriend.tv_friend_content.setText(letterData.getContent());
				//读取朋友对应的图片
				if(imageFriend != null){
					viewFriend.iv_friend.setImageBitmap(imageFriend);
				}else{
					viewFriend.iv_friend.setImageResource(R.drawable.icon_people_no);
				}
			}else{
				viewMe.tv_me_content.setText(letterData.getContent());
				//读取自己对应的图片
				if(imageMe != null){
					viewMe.iv_me.setImageBitmap(imageMe);
				}else{
					viewMe.iv_me.setImageResource(R.drawable.icon_people_no);
				}
			}
			return convertView;
		}
		
		@Override
		public int getItemViewType(int position) {
			LetterData letterData = letterDatas.get(position);
			return letterData.getType();
		}
		@Override
		public int getViewTypeCount() {
			return 2;
		}
		class ViewFriend{
			CircleImageView iv_friend;
			TextView tv_friend_content;
			TextView tv_time;
		}
		class ViewMe{
			CircleImageView iv_me;
			TextView tv_me_content;
			TextView tv_time;
		}
	}	
	
	class LetterData{
		int type;
		String content;
		int friend_id;
		String friend_name;
		String logo;
		String send_time;
		int relat_id;
		int chat_id;		
		
		public String getSend_time() {
			return send_time;
		}
		public void setSend_time(String send_time) {
			this.send_time = send_time;
		}
		public int getChat_id() {
			return chat_id;
		}
		public void setChat_id(int chat_id) {
			this.chat_id = chat_id;
		}
		public int getType() {
			return type;
		}
		public void setType(int type) {
			this.type = type;
		}
		public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}
		public int getFriend_id() {
			return friend_id;
		}
		public void setFriend_id(int friend_id) {
			this.friend_id = friend_id;
		}
		public String getFriend_name() {
			return friend_name;
		}
		public void setFriend_name(String friend_name) {
			this.friend_name = friend_name;
		}
		public String getLogo() {
			return logo;
		}
		public void setLogo(String logo) {
			this.logo = logo;
		}
		public int getRelat_id() {
			return relat_id;
		}
		public void setRelat_id(int relat_id) {
			this.relat_id = relat_id;
		}		
	}
	
	
	
	class MyBroadCastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Constant.A_ReceiverLetter)) {
				String extras = intent.getStringExtra("extras");
	            try {
					JSONObject jsonObject = new JSONObject(extras);
					if(cust_id.equals(jsonObject.getString("friend_id"))){
						String content = jsonObject.getString("msg");
						//如果是当前朋友发来的私信则显示
						LetterData letterData = new LetterData();
						letterData.setContent(content);
						letterData.setType(0);
						letterData.setSend_time(GetSystem.GetNowTime());
						letterDatas.add(letterData);
						letterAdapter.notifyDataSetChanged();
						lv_letter.setSelection(lv_letter.getBottom());
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} 
		}
	}
	
	private void onLoadOver() {
		lv_letter.refreshHeaderView();
		lv_letter.refreshBottomView();
		lv_letter.stopRefresh();
		lv_letter.stopLoadMore();
		lv_letter.setRefreshTime(GetSystem.GetNowTime());
	}
	@Override
	public void onRefresh() {
		refresh = "";
		int Chat_id = letterDatas.get(0).getChat_id();
		String url = Constant.BaseUrl + "customer/" + Variable.cust_id + "/get_chats?auth_code=" +
				Variable.auth_code + "&friend_id=" + cust_id + "&max_id=" + Chat_id;
		new NetThread.GetDataThread(handler, url, refresh_data).start();
		lv_letter.startHeaderWheel();
	}
	@Override
	public void onLoadMore() {}	
	
}