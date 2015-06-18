package com.wise.notice;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.db.dao.FriendData;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.widget.CircleImageView;

/**
 * 服务商列表
 *@author honesty
 **/
public class ServiceListActivity extends Activity{

	private final static int get_all_friend = 4;
	private final static int getFriendImage = 5;
	ListView lv_friend;
	AppApplication app;
	List<FriendData> friendDatas = new ArrayList<FriendData>();
	FriendAdapter friendAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_service_list);
		app = (AppApplication) getApplication();
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		lv_friend = (ListView) findViewById(R.id.lv_friend);
		friendAdapter = new FriendAdapter();
		lv_friend.setAdapter(friendAdapter);
		lv_friend.setOnScrollListener(onScrollListener);
		lv_friend.setOnItemClickListener(onItemClickListener);
		getFriendData();
	}
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			}
		}
	};
	/** 好友列表点击 **/
	OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			// 去介绍界面
			Intent intent = new Intent(ServiceListActivity.this, FriendInfoActivity.class);
			intent.putExtra("FriendId", String.valueOf(friendDatas.get(arg2).getFriend_id()));
			intent.putExtra("name", friendDatas.get(arg2).getFriend_name());
			intent.putExtra("cust_type", 2);
			startActivityForResult(intent, 4);
		}
	};
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case get_all_friend:
				jsonFriendData(msg.obj.toString());
				break;
			case getFriendImage:
				removeFriendThreadMark(msg.arg1);
				friendAdapter.notifyDataSetChanged();
				break;
			}
		}
		
	};
	/** 获取服务商数据 **/
	public void getFriendData() {
		String url = Constant.BaseUrl + "customer/" + app.cust_id + "/get_friends?auth_code=" + app.auth_code +"&friend_type=2";
		Log.i("ServiceListActivity", "获取服务商数据url "+url);
		new NetThread.GetDataThread(handler, url, get_all_friend).start();
	}
	private void jsonFriendData(String result) {
		System.out.println(result);
		try {
			friendDatas.clear();
			JSONArray jsonArray = new JSONArray(result);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				FriendData friendData = new FriendData();
				friendData.setSex(jsonObject.getInt("sex"));
				friendData.setLogo(jsonObject.getString("logo"));
				friendData.setFriend_name(jsonObject.getString("friend_name"));
				friendData.setFriend_type(jsonObject.getInt("friend_type"));
				friendData.setFriend_id(jsonObject.getInt("friend_id"));
				friendData.setUser_id(jsonObject.getInt("user_id"));
				friendData.setFriend_relat_id(jsonObject.getInt("friend_relat_id"));
				friendDatas.add(friendData);
			}
			friendAdapter.notifyDataSetChanged();
			getFriendLogo();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	class FriendAdapter extends BaseAdapter {
		LayoutInflater inflater = LayoutInflater.from(ServiceListActivity.this);

		@Override
		public int getCount() {
			return friendDatas.size();
		}

		@Override
		public Object getItem(int position) {
			return friendDatas.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.item_friend, null);
				holder = new ViewHolder();
				holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
				holder.iv_image = (CircleImageView) convertView.findViewById(R.id.iv_image);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			FriendData friendData = friendDatas.get(position);
			holder.tv_name.setText(friendData.getFriend_name());			
			if (new File(Constant.userIconPath + GetSystem.getM5DEndo(friendData.getLogo()) + ".png").exists()) {
				Bitmap image = BitmapFactory.decodeFile(Constant.userIconPath + GetSystem.getM5DEndo(friendData.getLogo()) + ".png");
				holder.iv_image.setImageBitmap(image);
			} else {
				holder.iv_image.setImageResource(R.drawable.icon_people_no);
			}
			return convertView;
		}

		private class ViewHolder {
			TextView tv_name;
			CircleImageView iv_image;
		}
	}
	OnScrollListener onScrollListener = new OnScrollListener() {
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:// 触摸状态
				break;
			case OnScrollListener.SCROLL_STATE_FLING:// 滑动状态
				break;
			case OnScrollListener.SCROLL_STATE_IDLE:// 停止
				// 读取图片
				getFriendLogo();
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		}
	};
	/** 获取好友列表图片 **/
	private void getFriendLogo() {
		int start = lv_friend.getFirstVisiblePosition();
		int stop = lv_friend.getLastVisiblePosition();
		for (int i = start; i < stop; i++) {
			if (i >= friendDatas.size()) {
				return;
			}
			FriendData friendData = friendDatas.get(i);
			if (friendData.getLogo() != null && (!friendData.getLogo().equals(""))) {
				// 判断图片是否存在
				if (new File(Constant.userIconPath + GetSystem.getM5DEndo(friendData.getLogo()) + ".png").exists()) {

				} else {
					if (isFriendThreadRun(i)) {
						// 如果图片正在读取则跳过
					} else {
						friendThreadId.add(i);
						new FriendImageThread(i).start();
					}
				}
			}
		}
	}

	List<Integer> friendThreadId = new ArrayList<Integer>();

	/** 判断图片是否开启了线程正在读图 **/
	private boolean isFriendThreadRun(int positon) {
		for (int i = 0; i < friendThreadId.size(); i++) {
			if (positon == friendThreadId.get(i)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 删除好友列表里正在下载的线程标识
	 * 
	 * @param position
	 */
	private void removeFriendThreadMark(int position) {
		for (int i = 0; i < friendThreadId.size(); i++) {
			if (friendThreadId.get(i) == position) {
				friendThreadId.remove(i);
				break;
			}
		}
	}
	class FriendImageThread extends Thread {
		int position;

		public FriendImageThread(int position) {
			this.position = position;
		}

		@Override
		public void run() {
			super.run();
			Bitmap bitmap = GetSystem.getBitmapFromURL(friendDatas.get(position).getLogo());
			if (bitmap != null) {
				GetSystem.saveImageSD(bitmap, Constant.userIconPath, GetSystem.getM5DEndo(friendDatas.get(position).getLogo()) + ".png", 100);
			}
			for (int i = 0; i < friendThreadId.size(); i++) {
				if (friendThreadId.get(i) == position) {
					friendThreadId.remove(i);
					break;
				}
			}
			Message message = new Message();
			message.what = getFriendImage;
			message.arg1 = position;
			handler.sendMessage(message);
		}
	}
}
