package com.wise.notice;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.net.NetThread;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 确认添加好友界面
 * 
 * @author honesty
 **/
public class SureFriendActivity extends Activity {
	private static final int i_launch = 1; 
	private static final int y_launch = 2; 
	private static final int sure_add_friend = 3; 
	private static final int getImage = 4; 
	
	AppApplication app;
	ListView lv_new_friend;
	List<FriendData> friendDatas = new ArrayList<FriendData>();
	NewFriendAdapter newFriendAdapter;
	int cust_id;
	/**我发起的数据是否获取完毕**/
	private boolean isILaunchResult = false;
	/**别人发起的数据是否获取完毕**/
	private boolean isYLaunchResult = false;
	/**确认添加好友需要刷新**/
	boolean isChange = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_sure_friend);
		app = (AppApplication) getApplication();
		cust_id = Integer.valueOf(app.cust_id);
		getILaunch();
		getYLaunch();
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		lv_new_friend = (ListView)findViewById(R.id.lv_new_friend);
		newFriendAdapter = new NewFriendAdapter();
		lv_new_friend.setAdapter(newFriendAdapter);
		lv_new_friend.setOnScrollListener(onScrollListener);
	}
	
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				initFinish();
				break;
			}
		}
	};
	
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case i_launch:
				friendDatas.addAll(0, jsonLaunch(msg.obj.toString()));
				newFriendAdapter.notifyDataSetChanged();
				isILaunchResult = true;
				getFristImage();
				break;
			case y_launch:
				friendDatas.addAll(jsonLaunch(msg.obj.toString()));
				newFriendAdapter.notifyDataSetChanged();
				isYLaunchResult = true;
				getFristImage();
				break;
			case sure_add_friend:
				jsonAddFriend(msg.obj.toString(),msg.arg1);
				break;
			case getImage:
				removeThreadMark(msg.arg1);
				newFriendAdapter.notifyDataSetChanged();
				break;
			}
			
		}		
	};
	
	class NewFriendAdapter extends BaseAdapter{
		LayoutInflater inflater = LayoutInflater.from(SureFriendActivity.this);
		@Override
		public int getCount() {
			return friendDatas.size();
		}
		@Override
		public Object getItem(int arg0) {
			return friendDatas.get(arg0);
		}
		@Override
		public long getItemId(int arg0) {
			return arg0;
		}
		@Override
		public View getView(final int arg0, View arg1, ViewGroup arg2) {
			ViewHolder holder;
			if(arg1 == null){
				arg1 = inflater.inflate(R.layout.item_new_friend, null);
				holder = new ViewHolder();
				holder.iv_logo = (ImageView)arg1.findViewById(R.id.iv_logo);
				holder.tv_name = (TextView)arg1.findViewById(R.id.tv_name);
				holder.tv_status = (TextView)arg1.findViewById(R.id.tv_status);
				arg1.setTag(holder);
			}else{
				holder = (ViewHolder) arg1.getTag();
			}
			final FriendData friendData = friendDatas.get(arg0);			
			if(friendData.getUser_id() == cust_id){//我发起添加的
				holder.tv_name.setText(friendData.getFriend_name());
				if(new File(Constant.userIconPath + GetSystem.getM5DEndo(friendData.getLogo()) + ".png").exists()){
					Bitmap image = BitmapFactory.decodeFile(Constant.userIconPath + GetSystem.getM5DEndo(friendData.getLogo())  + ".png");
					holder.iv_logo.setImageBitmap(image);
				}else{
					holder.iv_logo.setImageResource(R.drawable.icon_people_no);
				}
				if(friendData.getStatus() == 0){
					holder.tv_status.setText("等待对方添加");
					holder.tv_status.setTextColor(getResources().getColor(R.color.gray));
				}else if(friendData.getStatus() == 1){
					holder.tv_status.setText("已添加");
					holder.tv_status.setTextColor(getResources().getColor(R.color.gray));
				}
			}else{//别人添加我
				holder.tv_name.setText(friendData.getCust_name());
				if(new File(Constant.userIconPath + GetSystem.getM5DEndo(friendData.getLogo()) + ".png").exists()){
					Bitmap image = BitmapFactory.decodeFile(Constant.userIconPath + GetSystem.getM5DEndo(friendData.getLogo()) + ".png");
					holder.iv_logo.setImageBitmap(image);
				}else{
					holder.iv_logo.setImageResource(R.drawable.icon_people_no);
				}
				if(friendData.getStatus() == 0){
					holder.tv_status.setText("接受");
					holder.tv_status.setTextColor(getResources().getColor(R.color.color_navy));
					holder.tv_status.setOnClickListener(new OnClickListener() {						
						@Override
						public void onClick(View v) {
							sureAddFriend(friendData.getUser_id(),arg0);
						}
					});
				}else if(friendData.getStatus() == 1){
					holder.tv_status.setText("已添加");
					holder.tv_status.setTextColor(getResources().getColor(R.color.gray));
				}
			}
			return arg1;
		}	
		private class ViewHolder{
			ImageView iv_logo;
			TextView tv_name,tv_status;
		} 
	}
	ProgressDialog myDialog = null;
	/**接受好友**/
	private void sureAddFriend(int friend_id,int position){
		myDialog = ProgressDialog.show(SureFriendActivity.this,"提示", "接受中...");
        myDialog.setCancelable(true);
		String url = Constant.BaseUrl + "customer/" + app.cust_id
				+ "/auth_friend_request?auth_code=" + app.auth_code;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("friend_id", String.valueOf(friend_id)));
		params.add(new BasicNameValuePair("status", "1"));
		new NetThread.postDataThread(handler, url, params, sure_add_friend,position).start();
		System.out.println(url);
		System.out.println("friend_id = "+String.valueOf(friend_id));
	}
	/**确认添加结果**/
	private void jsonAddFriend(String result,int position){
		isChange = true;
		if(myDialog != null){
			myDialog.dismiss();
		}
		try {
			JSONObject jsonObject = new JSONObject(result);
			if(jsonObject.getInt("status_code") == 0){
				friendDatas.get(position).setStatus(1);
				newFriendAdapter.notifyDataSetChanged();
			}else{
				Toast.makeText(SureFriendActivity.this, "添加好友失败，请重试", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(SureFriendActivity.this, "添加好友失败，请重试", Toast.LENGTH_SHORT).show();
		}
		
	}
	/** 我发起添加的好友 **/
	private void getILaunch() {
		String url = Constant.BaseUrl + "customer/" + app.cust_id
				+ "/get_my_requests?auth_code=" + app.auth_code;
		new NetThread.GetDataThread(handler, url, i_launch).start();
	}
	/** 别人发起添加的好友 **/
	private void getYLaunch() {
		String url = Constant.BaseUrl + "customer/" + app.cust_id
				+ "/get_friend_requests?auth_code=" + app.auth_code;
		new NetThread.GetDataThread(handler, url, y_launch).start();
	}
	/**解析返回好友信息**/
	private List<FriendData> jsonLaunch(String result){
		List<FriendData> fDatas = new ArrayList<FriendData>();
		try {
			JSONArray jsonArray = new JSONArray(result);
			for(int i = 0 ; i < jsonArray.length() ; i++){
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				FriendData friendData = new FriendData();
				friendData.setFriend_id(jsonObject.getInt("friend_id"));
				friendData.setFriend_name(jsonObject.getString("friend_name"));
				friendData.setLogo(jsonObject.getString("logo"));
				friendData.setRequest_id(jsonObject.getInt("request_id"));
				friendData.setUser_id(jsonObject.getInt("user_id"));
				friendData.setStatus(jsonObject.getInt("status"));
				friendData.setCust_name(jsonObject.getString("cust_name"));
				fDatas.add(friendData);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fDatas;
	}
	
	OnScrollListener onScrollListener = new OnScrollListener() {		
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL://触摸状态
				break;
			case OnScrollListener.SCROLL_STATE_FLING://滑动状态				
				break;
			case OnScrollListener.SCROLL_STATE_IDLE://停止
				//读取图片
				getImage();
				break;
			}
		}		
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
		}
	};
	/**滚动才会获取图片，所以一开始需要手动加载图片**/
	private void getFristImage(){
		//2种请求加载完毕后才开始加载图片
		if(isILaunchResult && isYLaunchResult){
			getImage();
		}
	}
	/**读取图片**/
	private void getImage(){
		int start = lv_new_friend.getFirstVisiblePosition();
		int stop = lv_new_friend.getLastVisiblePosition();	
		for(int i = start ; i <= stop ; i++){
			FriendData friendData = friendDatas.get(i);
			if(friendData.getLogo() != null && (!friendData.getLogo().equals(""))){
				//判断图片是否存在
				if(new File(Constant.userIconPath + GetSystem.getM5DEndo(friendData.getLogo()) + ".png").exists()){
					
				}else{
					if(isThreadRun(i)){
						//如果图片正在读取则跳过
					}else{
						photoThreadId.add(i);
						new ImageThread(i).start();
					}
				}
			}					
		}
	}
	class ImageThread extends Thread{
		int position;
		public ImageThread(int position){
			this.position = position;
		}
		@Override
		public void run() {
			super.run();
			Bitmap bitmap = GetSystem.getBitmapFromURL(friendDatas.get(position).getLogo());
			if(bitmap != null){
				GetSystem.saveImageSD(bitmap, Constant.userIconPath, GetSystem.getM5DEndo(friendDatas.get(position).getLogo()) + ".png",100);
			}
			Message message = new Message();
			message.what = getImage;
			message.arg1 = position;
			handler.sendMessage(message);
		}
	}
	/**
	 * 删除列表里正在下载的线程标识
	 * @param position
	 */
	private void removeThreadMark(int position){
		for (int i = 0; i < photoThreadId.size(); i++) {
			if (photoThreadId.get(i) == position) {
				photoThreadId.remove(i);
				break;
			}
		}
	}
	List<Integer> photoThreadId = new ArrayList<Integer>();
	/**判断图片是否开启了线程正在读图**/
	private boolean isThreadRun(int positon){
		for(int i = 0 ; i < photoThreadId.size() ; i++){
			if(positon == photoThreadId.get(i)){
				return true;
			}
		}
		return false;
	}
	
	class FriendData{
		private String logo;
		private String friend_name;
		private int friend_id;
		private int request_id;
		private int user_id;
		private String cust_name;
		private int status;
				
		public String getCust_name() {
			return cust_name;
		}
		public void setCust_name(String cust_name) {
			this.cust_name = cust_name;
		}
		public String getLogo() {
			return logo;
		}
		public void setLogo(String logo) {
			this.logo = logo;
		}
		public String getFriend_name() {
			return friend_name;
		}
		public void setFriend_name(String friend_name) {
			this.friend_name = friend_name;
		}
		public int getFriend_id() {
			return friend_id;
		}
		public void setFriend_id(int friend_id) {
			this.friend_id = friend_id;
		}
		public int getRequest_id() {
			return request_id;
		}
		public void setRequest_id(int request_id) {
			this.request_id = request_id;
		}
		public int getUser_id() {
			return user_id;
		}
		public void setUser_id(int user_id) {
			this.user_id = user_id;
		}
		public int getStatus() {
			return status;
		}
		public void setStatus(int status) {
			this.status = status;
		}			
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {	
			initFinish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	private void initFinish(){
		if(isChange){
			//如果改变状态需要通知上一页面刷新
			setResult(2);
		}
		finish();
	}
}