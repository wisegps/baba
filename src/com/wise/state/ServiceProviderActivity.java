package com.wise.state;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.Judge;
import pubclas.NetThread;
import com.wise.baba.AppApplication;
import com.wise.baba.MoreActivity;
import com.wise.baba.R;
import com.wise.car.BarcodeActivity;
import com.wise.notice.FriendAddActivity;
import com.wise.notice.LetterActivity;
import com.wise.notice.ServiceProviderInfoActivity;
import com.wise.notice.SmsActivity;
import com.wise.setting.SetActivity;

import customView.CircleImageView;
import customView.WaitLinearLayout.OnFinishListener;
import xlist.XListView;
import xlist.XListView.IXListViewListener;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 服务商主界面
 *@author honesty
 **/
public class ServiceProviderActivity extends Activity implements IXListViewListener{
	
	private final int getNotice = 1;
	private final int refreshNotice = 3;
	private final int getFriendImage = 2;
	
	NoticeAdapter noticeAdapter;
	BtnListener btnListener;
	XListView lv_notice,lv_friend;
	List<NoticeData> noticeDatas = new ArrayList<NoticeData>();
	AppApplication app;
	ImageView iv_fm_back,iv_add;
	Button bt_info,bt_friend;
	
	MyBroadCastReceiver myBroadCastReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.fragement_notice);
		app = (AppApplication)getApplication();
		iv_add = (ImageView) findViewById(R.id.iv_add);
		iv_add.setOnClickListener(onClickListener);
		iv_fm_back = (ImageView)findViewById(R.id.iv_fm_back);
		iv_fm_back.setOnClickListener(onClickListener);
		bt_info = (Button)findViewById(R.id.bt_info);
		bt_info.setOnClickListener(onClickListener);
		bt_friend = (Button)findViewById(R.id.bt_friend);
		bt_friend.setOnClickListener(onClickListener);
		Button bt_set = (Button)findViewById(R.id.bt_set);
		bt_set.setOnClickListener(onClickListener);
		setFriendDatas();
		lv_friend = (XListView)findViewById(R.id.lv_friend);
		FriendAdapter friendAdapter = new FriendAdapter();
		lv_friend.setAdapter(friendAdapter);
		lv_friend.setPullLoadEnable(false);
		lv_friend.setPullRefreshEnable(false);
		lv_friend.setOnItemClickListener(onItemClickListener);
		
		lv_notice = (XListView)findViewById(R.id.lv_notice);
		lv_notice.setOnFinishListener(onFinishListener);
		lv_notice.setPullLoadEnable(false);
		lv_notice.setPullRefreshEnable(true);
		lv_notice.setXListViewListener(this);
		noticeAdapter = new NoticeAdapter();
		lv_notice.setAdapter(noticeAdapter);
		
		myBroadCastReceiver = new MyBroadCastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constant.A_ChangeCustomerType);
		registerReceiver(myBroadCastReceiver, intentFilter);
	}
	
	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_fm_back:
				btnListener.Back();
				break;
			case R.id.bt_info:
				lv_notice.setVisibility(View.VISIBLE);
				lv_friend.setVisibility(View.GONE);
				break;
			case R.id.bt_friend:
				lv_notice.setVisibility(View.GONE);
				lv_friend.setVisibility(View.VISIBLE);
				break;		
			case R.id.bt_set:
				startActivity(new Intent(ServiceProviderActivity.this, SetActivity.class));
				break;
			case R.id.iv_add:
				showMenu();
				break;
			case R.id.tv_letter:
				//TODO 处理数据
				startActivity(new Intent(ServiceProviderActivity.this, FriendAddActivity.class));
				break;
			case R.id.tv_Comments:
				startActivityForResult(new Intent(ServiceProviderActivity.this,
						BarcodeActivity.class), 0);
				break;
			}
		}
	};
	
	OnFinishListener onFinishListener = new OnFinishListener() {		
		@Override
		public void OnFinish(int index) {
			jsonData(refresh);
			noticeAdapter.notifyDataSetChanged();
			onLoadOver();
		}
	};

	public void SetBtnListener(BtnListener btnListener) {
		this.btnListener = btnListener;
	}
	boolean isVisible = false;
	public void setBackButtonVISIBLE(){
		isVisible = true;
	}

	public interface BtnListener {
		public void Back();
	}
	/**清空通知**/
	public void ClearNotice(){
		noticeDatas.clear();
		noticeAdapter.notifyDataSetChanged();
	}
	/**刷新通知**/
	public void ResetNotice(){
		noticeDatas.clear();
		getData();
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case getNotice:
				jsonData(msg.obj.toString());
				noticeAdapter.notifyDataSetChanged();
				break;
			case refreshNotice:
				refresh = msg.obj.toString();
				lv_notice.runFast(0);
				break;

			case getFriendImage:
				noticeAdapter.notifyDataSetChanged();
				break;
			}
		}
	};
	
	private void itemClick(int position){
		NoticeData noticeData = noticeDatas.get(position);
		switch (noticeData.friend_type) {
		case 1://秀爱车				
			break;
		case 2://秀服务
			break;
		case 3://问答
			break;
		case 4://通知
			clickSms(position);
			break;
		case 99://私信
			clickLetter(position);
			break;
		}
	}
	/**点击通知**/
	private void clickSms(int position){
		Intent intent = new Intent(ServiceProviderActivity.this, SmsActivity.class);
		intent.putExtra("type", noticeDatas.get(position).getType());
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
	}
	/**点击私信**/
	private void clickLetter(int position){
		NoticeData noticeData = noticeDatas.get(position);
		Intent intent = new Intent(ServiceProviderActivity.this, LetterActivity.class);
		intent.putExtra("cust_id", noticeData.getFriend_id());
		intent.putExtra("cust_name", noticeData.getFriend_name());
		intent.putExtra("logo", noticeData.getLogo());
		startActivity(intent);
	}

	private void getData() {
		String url = Constant.BaseUrl + "customer/" + app.cust_id
				+ "/get_relations?auth_code=" + app.auth_code;
		new NetThread.GetDataThread(handler, url, getNotice).start();
	}
	/**解析通知**/
	private void jsonData(String result) {
		noticeDatas.clear();
		try {
			JSONArray jsonArray = new JSONArray(result);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				if(jsonObject.opt("friend_type") != null){
					NoticeData noticeData = new NoticeData();
					noticeData.setType(jsonObject.getInt("type"));
					noticeData.setFriend_id(jsonObject.getString("friend_id"));
					int friend_type = jsonObject.getInt("friend_type");
					noticeData.setFriend_type(friend_type);
					noticeData.setContent(jsonObject.getString("content"));
					noticeData.setFriend_name(jsonObject.getString("friend_name"));
					noticeData.setSend_time(jsonObject.getString("send_time").replace("T", " ").substring(0, 19));
					//如果是私信的话,把头像url取下
					if(friend_type == 99){
						noticeData.setLogo(jsonObject.getString("logo"));
						//判断是文本还是图片
						int type = jsonObject.getInt("type");
						if(type == 1){
							noticeData.setContent("[图片]");
						}else if(type == 2){
							noticeData.setContent("[语音]");
						}
					}
					noticeDatas.add(noticeData);
				}				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private class FriendData{
		private String name;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}		
	}
	OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			//去介绍界面
			Intent intent = new Intent(ServiceProviderActivity.this, ServiceProviderInfoActivity.class);
			intent.putExtra("name", friendDatas.get(arg2 - 1).getName());
			startActivity(intent);
		}
	};
	List<FriendData> friendDatas = new ArrayList<FriendData>();
	private void setFriendDatas(){
		for(int i = 0 ; i < 12 ; i++){
			FriendData friendData = new FriendData();
			friendData.setName("客户经理"+i);
			friendDatas.add(friendData);
		}
	}
	class FriendAdapter extends BaseAdapter{
		LayoutInflater inflater = LayoutInflater.from(ServiceProviderActivity.this);
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
			holder.tv_name.setText(friendData.getName());
			return convertView;
		}
		private class ViewHolder {
			TextView tv_name;
			CircleImageView iv_image;
		}
	}

	class NoticeAdapter extends BaseAdapter {
		LayoutInflater inflater = LayoutInflater.from(ServiceProviderActivity.this);

		@Override
		public int getCount() {
			return noticeDatas.size();
		}

		@Override
		public Object getItem(int position) {
			return noticeDatas.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.item_notice, null);
				holder = new ViewHolder();
				holder.tv_type = (TextView) convertView.findViewById(R.id.tv_type);
				holder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
				holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
				holder.iv_image = (CircleImageView) convertView.findViewById(R.id.iv_image);
				holder.ll_fm_notice = (LinearLayout) convertView.findViewById(R.id.ll_fm_notice);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			NoticeData noticeData = noticeDatas.get(position);
			holder.tv_content.setText(noticeData.getContent());
			holder.tv_type.setText(noticeData.getFriend_name());
			holder.ll_fm_notice.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					itemClick(position);
				}
			});
			//间隔时间
			String create_time =GetSystem.ChangeTimeZone(noticeData.getSend_time());
			int spacingData = GetSystem.spacingNowTime(create_time);			
			holder.tv_time.setText(GetSystem.showData(spacingData, create_time));
						
			switch (noticeData.friend_type) {
			case 1://秀爱车	
				holder.iv_image.setImageResource(R.drawable.icon_xx_notice);
				break;
			case 2://秀服务
				holder.iv_image.setImageResource(R.drawable.icon_xx_notice);
				break;
			case 3://问答
				holder.iv_image.setImageResource(R.drawable.icon_xx_notice);
				break;
			case 4://通知
				holder.iv_image.setImageResource(R.drawable.icon_xx_notice);
				break;
			case 99://私信
				//读取用户对应的图片
				if(new File(Constant.userIconPath + noticeData.getFriend_id() + ".png").exists()){
					Bitmap image = BitmapFactory.decodeFile(Constant.userIconPath + noticeData.getFriend_id() + ".png");
					holder.iv_image.setImageBitmap(image);
				}else{
					holder.iv_image.setImageResource(R.drawable.icon_people_no);
				}
				break;
			}
			
			return convertView;
		}

		private class ViewHolder {
			TextView tv_type;
			TextView tv_content;
			TextView tv_time;
			CircleImageView iv_image;
			LinearLayout ll_fm_notice;
		}
	}

	class NoticeData {
		/** 好友id 1: 秀爱车 2：秀服务 3：问答 4: 通知 >99: 私信id区段 **/
		String friend_id;
		/** 好友类型 1: 秀爱车 2：秀服务 3：问答 4: 通知 99: 私信 **/
		int friend_type;
		/** 排序id 1: 秀爱车 2：秀服务 3：问答 4: 通知 99: 私信 **/
		int order_id;
		int type;
		String content;
		String send_time;
		String friend_name;
		String logo;
		
		public int getType() {
			return type;
		}
		public void setType(int type) {
			this.type = type;
		}
		public String getFriend_id() {
			return friend_id;
		}
		public void setFriend_id(String friend_id) {
			this.friend_id = friend_id;
		}
		public int getFriend_type() {
			return friend_type;
		}
		public void setFriend_type(int friend_type) {
			this.friend_type = friend_type;
		}
		public int getOrder_id() {
			return order_id;
		}
		public void setOrder_id(int order_id) {
			this.order_id = order_id;
		}
		public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}
		public String getSend_time() {
			return send_time;
		}
		public void setSend_time(String send_time) {
			this.send_time = send_time;
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
		
		@Override
		public String toString() {
			return "NoticeData [friend_id=" + friend_id + ", friend_type="
					+ friend_type + ", order_id=" + order_id + ", content="
					+ content + ", send_time=" + send_time
					+ ", friend_name=" + friend_name + ", logo=" + logo + "]";
		}		
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
				getPersionImage();
				break;
			}
		}
		
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
		}
	};
	
	/**获取显示区域的图片**/
	private void getPersionImage(){
		int start = lv_notice.getFirstVisiblePosition();
		int stop = lv_notice.getLastVisiblePosition();		
		for(int i = start ; i <= stop ; i++){
			NoticeData noticeData = noticeDatas.get(i);
			if(noticeData.getFriend_type() == 99){
				//判断图片是否存在
				if(new File(Constant.userIconPath + noticeData.getFriend_id() + ".png").exists()){
					
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
	class ImageThread extends Thread{
		int position;
		public ImageThread(int position){
			this.position = position;
		}
		@Override
		public void run() {
			super.run();
			Bitmap bitmap = GetSystem.getBitmapFromURL(noticeDatas.get(position).getLogo());
			if(bitmap != null){
				GetSystem.saveImageSD(bitmap, Constant.userIconPath, noticeDatas.get(position).getFriend_id() + ".png",100);
			}
			for (int i = 0; i < photoThreadId.size(); i++) {
				if (photoThreadId.get(i) == position) {
					photoThreadId.remove(i);
					break;
				}
			}
			Message message = new Message();
			message.what = getFriendImage;
			handler.sendMessage(message);
		}
	}
	String refresh = "";

	@Override
	public void onRefresh() {
		refresh = "";
		String url = Constant.BaseUrl + "customer/" + app.cust_id
				+ "/get_relations?auth_code=" + app.auth_code;
		new NetThread.GetDataThread(handler, url, refreshNotice).start();
	}
	@Override
	public void onLoadMore() {}
	private void onLoadOver() {
		lv_notice.refreshHeaderView();
		lv_notice.refreshBottomView();
		lv_notice.stopRefresh();
		lv_notice.stopLoadMore();
		lv_notice.setRefreshTime(GetSystem.GetNowTime());
	}
	PopupWindow mPopupWindow;
	private void showMenu() {
		LayoutInflater mLayoutInflater = LayoutInflater
				.from(ServiceProviderActivity.this);
		View popunwindwow = mLayoutInflater.inflate(
				R.layout.item_menu_vertical, null);
		TextView tv_letter = (TextView) popunwindwow
				.findViewById(R.id.tv_letter);
		tv_letter.setText("添加朋友");
		tv_letter.setOnClickListener(onClickListener);
		TextView tv_Comments = (TextView) popunwindwow
				.findViewById(R.id.tv_Comments);
		tv_Comments.setText("扫一扫");
		tv_Comments.setOnClickListener(onClickListener);
		mPopupWindow = new PopupWindow(popunwindwow, 320,
				LayoutParams.WRAP_CONTENT);
		mPopupWindow.setAnimationStyle(R.style.PopupAnimation);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopupWindow.setFocusable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.showAsDropDown(ServiceProviderActivity.this.findViewById(R.id.iv_add), 0, 0);
	}
	class MyBroadCastReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(Constant.A_ChangeCustomerType)){
				//类型改变，关闭界面
				finish();
			}
		}}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(myBroadCastReceiver);
	}
	@Override
	protected void onResume() {
		super.onResume();
		if(Judge.isLogin(app)){
			getData();
		}
	}
	long waitTime = 2000;
	long touchTime = 0;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		long currentTime = System.currentTimeMillis();
		if (touchTime == 0 || (currentTime - touchTime) >= waitTime) {
			Toast.makeText(this, "再按一次退出客户端", Toast.LENGTH_SHORT)
					.show();
			touchTime = currentTime;
		} else {
			finish();
		}
		return true;
	}
	
}