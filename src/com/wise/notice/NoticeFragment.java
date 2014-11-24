package com.wise.notice;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.Judge;
import pubclas.NetThread;
import xlist.XListView;
import xlist.XListView.IXListViewListener;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.car.BarcodeActivity;
import com.wise.setting.SetActivity;
import customView.CircleImageView;
import customView.WaitLinearLayout.OnFinishListener;
import data.FriendData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
/**
 * 通知列表
 * @author honesty
 *
 */
public class NoticeFragment extends Fragment implements IXListViewListener{

	private final static int getNotice = 1;
	private final static int getFriendImage = 2;
	private final static int refreshNotice = 3;
	private final static int get_all_friend = 4;
	private final static int get_friend_logo = 5;
	
	NoticeAdapter noticeAdapter;
	BtnListener btnListener;
	XListView lv_notice,lv_friend;
	List<NoticeData> noticeDatas = new ArrayList<NoticeData>();
	AppApplication app;
	ImageView iv_fm_back,iv_add;
	Button bt_info,bt_friend,bt_set;
	FriendAdapter friendAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragement_notice, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		app = (AppApplication)getActivity().getApplication();
		iv_add = (ImageView) getActivity().findViewById(R.id.iv_add);
		iv_add.setOnClickListener(onClickListener);
		iv_fm_back = (ImageView) getActivity().findViewById(R.id.iv_fm_back);
		iv_fm_back.setOnClickListener(onClickListener);
		if(isVisible){
			iv_fm_back.setVisibility(View.VISIBLE);
		}
		bt_info = (Button)getActivity().findViewById(R.id.bt_info);
		bt_info.setOnClickListener(onClickListener);
		bt_friend = (Button)getActivity().findViewById(R.id.bt_friend);
		bt_friend.setOnClickListener(onClickListener);
		bt_set = (Button)getActivity().findViewById(R.id.bt_set);
		bt_set.setOnClickListener(onClickListener);
		lv_friend = (XListView)getActivity().findViewById(R.id.lv_friend);
		friendAdapter = new FriendAdapter();
		lv_friend.setAdapter(friendAdapter);
		lv_friend.setPullLoadEnable(false);
		lv_friend.setPullRefreshEnable(false);
		lv_friend.setOnItemClickListener(onItemClickListener);
		lv_friend.setOnScrollListener(onScrollListener);
		getFriendData();
		
		lv_notice = (XListView) getActivity().findViewById(R.id.lv_notice);
		lv_notice.setOnFinishListener(onFinishListener);
		lv_notice.setPullLoadEnable(false);
		lv_notice.setPullRefreshEnable(true);
		lv_notice.setXListViewListener(this);
		noticeAdapter = new NoticeAdapter();
		lv_notice.setAdapter(noticeAdapter);
		lv_notice.setOnScrollListener(onScrollListener);
		if(app.cust_type != 2){
			//用户
			bt_set.setVisibility(View.GONE);
		}
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
				startActivity(new Intent(getActivity(), SetActivity.class));
				break;
			case R.id.iv_add:
				showMenu();
				break;
			case R.id.tv_letter:
				startActivity(new Intent(getActivity(), FriendAddActivity.class));
				break;
			case R.id.tv_Comments:
				startActivityForResult(new Intent(getActivity(),
						BarcodeActivity.class), 1);
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
		app.friendDatas.clear();
		friendAdapter.notifyDataSetChanged();
	}
	/**刷新通知**/
	public void ResetNotice(){
		noticeDatas.clear();
		getData();
		app.friendDatas.clear();
		getFriendData();
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case getNotice:
				jsonData(msg.obj.toString());
				noticeAdapter.notifyDataSetChanged();
				System.out.println("getNotice");
				getPersionImage();
				break;
			case refreshNotice:
				refresh = msg.obj.toString();
				lv_notice.runFast(0);
				System.out.println("refreshNotice");
				getPersionImage();
				break;

			case getFriendImage:
				noticeAdapter.notifyDataSetChanged();
				break;
			case get_friend_logo:
				friendAdapter.notifyDataSetChanged();
				break;
			case get_all_friend:
				jsonFriendData(msg.obj.toString());
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
		Intent intent = new Intent(getActivity(), SmsActivity.class);
		intent.putExtra("type", noticeDatas.get(position).getType());
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
	}
	/**点击私信**/
	private void clickLetter(int position){
		NoticeData noticeData = noticeDatas.get(position);
		Intent intent = new Intent(getActivity(), LetterActivity.class);
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
						}else if(type == 3){
							noticeData.setContent("[文件]");
						}else if(type == 4){
							noticeData.setContent("[位置]");
						}
					}
					noticeDatas.add(noticeData);
				}				
			}
			getPersionImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**好友列表点击**/
	OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if(arg2 == 1){
				//新的朋友(我添加的和别人添加我的)
				startActivityForResult(new Intent(getActivity(), SureFriendActivity.class), 3);
			}else{
				//去介绍界面
				Intent intent = new Intent(getActivity(), FriendInfoActivity.class);
				intent.putExtra("FriendId", String.valueOf(app.friendDatas.get(arg2 - 1).getFriend_id()));
				intent.putExtra("name", app.friendDatas.get(arg2 - 1).getFriend_name());
				intent.putExtra("isShow", true);
				startActivity(intent);
			}
		}
	};
	/**获取好友数据**/
	private void getFriendData(){
		String url = Constant.BaseUrl + "customer/" + app.cust_id + "/get_friends?auth_code=" + app.auth_code;
		new NetThread.GetDataThread(handler, url, get_all_friend).start();
	}
	private void jsonFriendData(String result){
		try {
			app.friendDatas.clear();
			FriendData fData = new FriendData();
			fData.setFriend_name("新的朋友");
			app.friendDatas.add(fData);
			JSONArray jsonArray = new JSONArray(result);
			for(int i = 0 ; i < jsonArray.length() ; i++){
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				FriendData friendData = new FriendData();
				friendData.setSex(jsonObject.getInt("sex"));
				friendData.setLogo(jsonObject.getString("logo"));
				friendData.setFriend_name(jsonObject.getString("friend_name"));
				friendData.setFriend_type(jsonObject.getInt("friend_type"));
				friendData.setFriend_id(jsonObject.getInt("friend_id"));
				friendData.setUser_id(jsonObject.getInt("user_id"));
				friendData.setFriend_relat_id(jsonObject.getInt("friend_relat_id"));
				app.friendDatas.add(friendData);
			}
			friendAdapter.notifyDataSetChanged();
			getFriendLogo();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	class FriendAdapter extends BaseAdapter{
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		@Override
		public int getCount() {
			return app.friendDatas.size();
		}
		@Override
		public Object getItem(int position) {
			return app.friendDatas.get(position);
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
			FriendData friendData = app.friendDatas.get(position);
			holder.tv_name.setText(friendData.getFriend_name());
			if(position == 0){
				//第一项是新的朋友
				holder.iv_image.setImageResource(R.drawable.icon_people_no);
			}else{
				if(new File(Constant.userIconPath + friendData.getFriend_id() + ".png").exists()){
					Bitmap image = BitmapFactory.decodeFile(Constant.userIconPath + friendData.getFriend_id() + ".png");
					holder.iv_image.setImageBitmap(image);
				}else{
					holder.iv_image.setImageResource(R.drawable.icon_people_no);
				}
			}			
			return convertView;
		}
		private class ViewHolder {
			TextView tv_name;
			CircleImageView iv_image;
		}
	}

	class NoticeAdapter extends BaseAdapter {
		LayoutInflater inflater = LayoutInflater.from(getActivity());

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
				if(new File(Constant.userIconPath + GetSystem.getM5DEndo(noticeData.getLogo()) + ".png").exists()){
					Bitmap image = BitmapFactory.decodeFile(Constant.userIconPath + GetSystem.getM5DEndo(noticeData.getLogo()) + ".png");
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
				switch (view.getId()) {
				case R.id.lv_friend:
					getFriendLogo();
					break;
				case R.id.lv_notice:
					getPersionImage();
					break;
				}
				break;
			}
		}
		
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
		}
	};
	
	private void getFriendLogo(){
		int start = lv_friend.getFirstVisiblePosition();
		if(start == 0){
			start = 1;
		}
		int stop = lv_friend.getLastVisiblePosition();
		for(int i = start ; i < stop ; i++){
			if(i >= app.friendDatas.size()){
				return ;
			}
			FriendData friendData = app.friendDatas.get(i);
			if(friendData.getLogo() != null && (!friendData.getLogo().equals(""))){
				//判断图片是否存在
				if(new File(Constant.userIconPath + friendData.getFriend_id() + ".png").exists()){
					
				}else{
					if(isFriendThreadRun(i)){
						//如果图片正在读取则跳过
					}else{
						FriendId.add(i);
						new FriendThread(i).start();
					}
				}
			}					
		}
	}
	
	/**获取显示区域的图片**/
	private void getPersionImage(){
		int start = lv_notice.getFirstVisiblePosition();
		if(start != 0){
			start--;
		}
		int stop = lv_notice.getLastVisiblePosition();		
		for(int i = start ; i < stop ; i++){
			if(i >= noticeDatas.size()){
				return ;
			}
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
	
	List<Integer> FriendId = new ArrayList<Integer>();
	/**判断图片是否开启了线程正在读图**/
	private boolean isFriendThreadRun(int positon){
		for(int i = 0 ; i < FriendId.size() ; i++){
			if(positon == FriendId.get(i)){
				return true;
			}
		}
		return false;
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
	
	class FriendThread extends Thread{
		int position;
		public FriendThread(int position){
			this.position = position;
		}
		@Override
		public void run() {
			super.run();
			Bitmap bitmap = GetSystem.getBitmapFromURL(app.friendDatas.get(position).getLogo());
			if(bitmap != null){
				GetSystem.saveImageSD(bitmap, Constant.userIconPath, app.friendDatas.get(position).getFriend_id() + ".png",100);
			}
			for (int i = 0; i < FriendId.size(); i++) {
				if (FriendId.get(i) == position) {
					FriendId.remove(i);
					break;
				}
			}
			Message message = new Message();
			message.what = get_friend_logo;
			handler.sendMessage(message);
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
			Bitmap bitmap = GetSystem.getBitmapFromURL(noticeDatas.get(position).getLogo());
			if(bitmap != null){
				String logo = noticeDatas.get(position).getLogo();
				if(logo == null || logo.equals("")){
					
				}else{
					GetSystem.saveImageSD(bitmap, Constant.userIconPath, GetSystem.getM5DEndo(logo) + ".png",100);
				}
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
				.from(getActivity());
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
		mPopupWindow.showAsDropDown(getActivity().findViewById(R.id.iv_add), 0, 0);
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 1 && resultCode == 2){
			String FriendId = data.getStringExtra("result");
			//二维码扫描后跳转到用户信息界面
			Intent intent = new Intent(getActivity(), FriendInfoActivity.class);
			intent.putExtra("FriendId", FriendId);
			startActivityForResult(intent, 2);
			return;
		}if(requestCode == 2 && resultCode == 2){
			//TODO 添加朋友返回
		}if(requestCode == 3 && resultCode == 2){
			//确认接受朋友返回，刷新数据
			getFriendData();
		}
	}
	

	

	@Override
	public void onResume() {
		super.onResume();
		if(Judge.isLogin(app)){
			getData();
		}
	}
}