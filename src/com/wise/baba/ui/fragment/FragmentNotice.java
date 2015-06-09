package com.wise.baba.ui.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import xlist.XListView;
import xlist.XListView.IXListViewListener;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.biz.Judge;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.widget.CircleImageView;
import com.wise.baba.ui.widget.WaitLinearLayout.OnFinishListener;
import com.wise.baba.util.FaceConversionUtil;
import com.wise.notice.LetterActivity;
import com.wise.notice.SmsActivity;


/**
 * 通知列表
 * 
 * @author honesty
 * 
 */
public class FragmentNotice extends Fragment implements IXListViewListener {

	private final static int getNotice = 1;
	/** 获取通知图片 **/
	private final static int getNoticeImage = 2;
	private final static int refreshNotice = 3;

	NoticeAdapter noticeAdapter;
	BtnListener btnListener;
	XListView lv_notice;
	List<NoticeData> noticeDatas = new ArrayList<NoticeData>();
	AppApplication app;
	private View rootView;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(rootView == null){
			rootView = inflater.inflate(R.layout.fragment_notice, container,
					false);
		}else{
			ViewGroup parent = (ViewGroup) rootView.getParent();
			if (null != parent) {
				parent.removeView(rootView);
			}
		}
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		app = (AppApplication) getActivity().getApplication();
		lv_notice = (XListView) getActivity().findViewById(R.id.lv_notice);
		lv_notice.setOnFinishListener(onFinishListener);
		lv_notice.setPullLoadEnable(false);
		lv_notice.setPullRefreshEnable(true);
		lv_notice.setXListViewListener(this);
		noticeAdapter = new NoticeAdapter();
		lv_notice.setAdapter(noticeAdapter);
		lv_notice.setOnScrollListener(onScrollListener);
		SharedPreferences preferences = getActivity().getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		String notice = preferences.getString(Constant.sp_notice + app.cust_id, "");
		jsonData(notice);
		noticeAdapter.notifyDataSetChanged();
	}

	
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		/** 开启线程初始化表情 **/
		new Thread(new Runnable() {
			@Override
			public void run() {
				FaceConversionUtil.getInstace().getFileText(app);
			}
		}).start();
	}



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

	public void setBackButtonVISIBLE() {
		isVisible = true;
	}

	public interface BtnListener {
		public void Back();
	}

	/** 清空通知 **/
	public void ClearNotice() {
		noticeDatas.clear();
		noticeAdapter.notifyDataSetChanged();
		app.friendDatas.clear();
		// friendAdapter.notifyDataSetChanged();
	}

	/** 刷新通知 **/
	public void ResetNotice() {
		noticeDatas.clear();
		getData();
		app.friendDatas.clear();
		// getFriendData();
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case getNotice:
				saveSp(msg.obj.toString());
				jsonData(msg.obj.toString());
				noticeAdapter.notifyDataSetChanged();
				getNoticeImage();
				break;
			case refreshNotice:
				refresh = msg.obj.toString();
				saveSp(refresh);
				lv_notice.runFast(0);
				getNoticeImage();
				break;

			case getNoticeImage:
				removeNoticeThreadMark(msg.arg1);
				noticeAdapter.notifyDataSetChanged();
				break;
			}
		}
	};
	/**存储最新的通知**/
	private void saveSp(String result){
		SharedPreferences preferences = getActivity().getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(Constant.sp_notice + app.cust_id, result);
		editor.commit();
	}

	private void itemClick(int position) {
		NoticeData noticeData = noticeDatas.get(position);
		switch (noticeData.friend_type) {
		case 1:// 秀爱车
			break;
		case 2:// 秀服务
			break;
		case 3:// 问答
			break;
		case 4:// 通知
			clickSms(position);
			break;
		case 99:// 私信
			clickLetter(position);
			break;
		}
	}

	/** 点击通知 **/
	private void clickSms(int position) {
		Intent intent = new Intent(getActivity(), SmsActivity.class);
		intent.putExtra("type", noticeDatas.get(position).getType());
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	/** 点击私信 **/
	private void clickLetter(int position) {
		NoticeData noticeData = noticeDatas.get(position);
		Intent intent = new Intent(getActivity(), LetterActivity.class);
		intent.putExtra("cust_id", noticeData.getFriend_id());
		intent.putExtra("cust_name", noticeData.getFriend_name());
		intent.putExtra("logo", noticeData.getLogo());
		startActivity(intent);
	}

	private void getData() {
		String url = Constant.BaseUrl + "customer/" + app.cust_id + "/get_relations?auth_code=" + app.auth_code;
		new NetThread.GetDataThread(handler, url, getNotice).start();
	}

	/** 解析通知 **/
	private void jsonData(String result) {
		noticeDatas.clear();
		try {
			JSONArray jsonArray = new JSONArray(result);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				if (jsonObject.opt("friend_type") != null) {
					NoticeData noticeData = new NoticeData();
					noticeData.setUnread_count(jsonObject.getInt("unread_count"));
					noticeData.setType(jsonObject.getInt("type"));
					noticeData.setFriend_id(jsonObject.getString("friend_id"));
					int friend_type = jsonObject.getInt("friend_type");
					noticeData.setFriend_type(friend_type);
					noticeData.setContent(jsonObject.getString("content"));
					noticeData.setFriend_name(jsonObject.getString("friend_name"));
					noticeData.setSend_time(jsonObject.getString("send_time").replace("T", " ").substring(0, 19));
					// 如果是私信的话,把头像url取下
					if (friend_type == 99) {
						noticeData.setLogo(jsonObject.getString("logo"));
						// 判断是文本还是图片
						int type = jsonObject.getInt("type");
						if (type == 1) {
							noticeData.setContent("[图片]");
						} else if (type == 2) {
							noticeData.setContent("[语音]");
						} else if (type == 3) {
							noticeData.setContent("[文件]");
						} else if (type == 4) {
							noticeData.setContent("[位置]");
						}
					}
					noticeDatas.add(noticeData);
				}
			}
			getNoticeImage();
		} catch (Exception e) {
			e.printStackTrace();
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
				holder.tv_noti_number = (TextView) convertView.findViewById(R.id.tv_noti_number);
				holder.iv_image = (CircleImageView) convertView.findViewById(R.id.iv_image);
				holder.ll_fm_notice = (LinearLayout) convertView.findViewById(R.id.ll_fm_notice);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			NoticeData noticeData = noticeDatas.get(position);
			if (noticeData.getUnread_count() == 0) {
				holder.tv_noti_number.setVisibility(View.GONE);
			} else {
				holder.tv_noti_number.setVisibility(View.VISIBLE);
				holder.tv_noti_number.setText("" + noticeData.getUnread_count());
			}
			holder.tv_content.setText(getFaceImage(noticeData.getContent()));
			holder.tv_type.setText(noticeData.getFriend_name());
			holder.ll_fm_notice.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					itemClick(position);
				}
			});
			// 间隔时间
			String create_time = GetSystem.ChangeTimeZone(noticeData.getSend_time());
			int spacingData = GetSystem.spacingNowTime(create_time);
			holder.tv_time.setText(GetSystem.showData(spacingData, create_time));

			switch (noticeData.friend_type) {
			case 1:// 秀爱车
				holder.iv_image.setImageResource(R.drawable.icon_xx_notice);
				break;
			case 2:// 秀服务
				holder.iv_image.setImageResource(R.drawable.icon_xx_notice);
				break;
			case 3:// 问答
				holder.iv_image.setImageResource(R.drawable.icon_xx_notice);
				break;
			case 4:// 通知
				holder.iv_image.setImageResource(R.drawable.icon_xx_notice);
				break;
			case 99:// 私信
				// 读取用户对应的图片
				if (new File(Constant.userIconPath + GetSystem.getM5DEndo(noticeData.getLogo()) + ".png").exists()) {
					Bitmap image = BitmapFactory.decodeFile(Constant.userIconPath + GetSystem.getM5DEndo(noticeData.getLogo()) + ".png");
					holder.iv_image.setImageBitmap(image);
				} else {
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
			TextView tv_noti_number;
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
		int unread_count;

		public int getUnread_count() {
			return unread_count;
		}

		public void setUnread_count(int unread_count) {
			this.unread_count = unread_count;
		}

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
			return "NoticeData [friend_id=" + friend_id + ", friend_type=" + friend_type + ", order_id=" + order_id + ", content=" + content + ", send_time="
					+ send_time + ", friend_name=" + friend_name + ", logo=" + logo + "]";
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
				getNoticeImage();
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		}
	};

	public SpannableString getFaceImage(String faceContent) {
		return FaceConversionUtil.getInstace().getExpressionString(getActivity(), faceContent);
	}

	/** 获取显示区域通知的图片 **/
	private void getNoticeImage() {
		int start = lv_notice.getFirstVisiblePosition();
		int stop = lv_notice.getLastVisiblePosition();
		for (int i = start; i < stop; i++) {
			if (i >= noticeDatas.size()) {
				return;
			}
			NoticeData noticeData = noticeDatas.get(i);
			if (noticeData.getFriend_type() == 99) {
				// 判断图片是否存在
				if (noticeData.getLogo() == null || noticeData.getLogo().equals("")) {

				} else {

					if (new File(Constant.userIconPath + GetSystem.getM5DEndo(noticeData.getLogo()) + ".png").exists()) {

					} else {
						if (isNoticeThreadRun(i)) {
							// 如果图片正在读取则跳过
						} else {
							noticeThreadId.add(i);
							new NoticeImageThread(i).start();
						}
					}
				}
			}
		}
	}

	List<Integer> noticeThreadId = new ArrayList<Integer>();

	/** 判断图片是否开启了线程正在读图 **/
	private boolean isNoticeThreadRun(int positon) {
		for (int i = 0; i < noticeThreadId.size(); i++) {
			if (positon == noticeThreadId.get(i)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 删除通知列表里正在下载的线程标识
	 * 
	 * @param position
	 */
	private void removeNoticeThreadMark(int position) {
		for (int i = 0; i < noticeThreadId.size(); i++) {
			if (noticeThreadId.get(i) == position) {
				noticeThreadId.remove(i);
				break;
			}
		}
	}

	class NoticeImageThread extends Thread {
		int position;

		public NoticeImageThread(int position) {
			this.position = position;
		}

		@Override
		public void run() {
			super.run();
			try {
				Bitmap bitmap = GetSystem.getBitmapFromURL(noticeDatas.get(position).getLogo());
				if (bitmap != null) {
					String logo = noticeDatas.get(position).getLogo();
					if (logo == null || logo.equals("")) {

					} else {
						GetSystem.saveImageSD(bitmap, Constant.userIconPath, GetSystem.getM5DEndo(logo) + ".png", 100);
					}
				}
				Message message = new Message();
				message.what = getNoticeImage;
				message.arg1 = position;
				handler.sendMessage(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	String refresh = "";

	@Override
	public void onRefresh() {
		refresh = "";
		String url = Constant.BaseUrl + "customer/" + app.cust_id + "/get_relations?auth_code=" + app.auth_code;
		new NetThread.GetDataThread(handler, url, refreshNotice).start();
	}

	@Override
	public void onLoadMore() {
	}

	private void onLoadOver() {
		lv_notice.refreshHeaderView();
		lv_notice.refreshBottomView();
		lv_notice.stopRefresh();
		lv_notice.stopLoadMore();
		lv_notice.setRefreshTime(GetSystem.GetNowTime());
	}

	@Override
	public void onResume() {
		super.onResume();
		if (Judge.isLogin(app)) {
			getData();
		}
	}
}
