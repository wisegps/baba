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
import pubclas.Variable;
import xlist.XListView;
import xlist.XListView.IXListViewListener;
import com.wise.baba.R;
import customView.CircleImageView;
import customView.WaitLinearLayout.OnFinishListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NoticeFragment extends Fragment implements IXListViewListener{

	private final int getNotice = 1;
	private final int refreshNotice = 3;
	private final int getFriendImage = 2;
	
	NoticeAdapter noticeAdapter;
	BtnListener btnListener;
	XListView lv_notice;
	List<NoticeData> noticeDatas = new ArrayList<NoticeData>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragement_notice, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		ImageView iv_fm_back = (ImageView) getActivity().findViewById(R.id.iv_fm_back);
		iv_fm_back.setOnClickListener(onClickListener);
		lv_notice = (XListView) getActivity().findViewById(R.id.lv_notice);
		lv_notice.setOnFinishListener(onFinishListener);
		lv_notice.setPullLoadEnable(false);
		lv_notice.setPullRefreshEnable(true);
		lv_notice.setXListViewListener(this);
		noticeAdapter = new NoticeAdapter();
		lv_notice.setAdapter(noticeAdapter);
		if(Judge.isLogin()){
			getData();
		}
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_fm_back:
				btnListener.Back();
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
		String url = Constant.BaseUrl + "customer/" + Variable.cust_id
				+ "/get_relations?auth_code=" + Variable.auth_code;
		new Thread(new NetThread.GetDataThread(handler, url, getNotice)).start();
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
					}
					noticeDatas.add(noticeData);
				}				
			}
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
				holder.iv_image = (CircleImageView) convertView.findViewById(R.id.iv_image);
				holder.ll_fm_notice = (RelativeLayout) convertView.findViewById(R.id.ll_fm_notice);
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
					//TODO 点击
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
					holder.iv_image.setImageResource(R.drawable.icon_xx_notice);
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
			RelativeLayout ll_fm_notice;
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
			GetSystem.saveImageSD(bitmap, Constant.userIconPath, noticeDatas.get(position).getFriend_id() + ".png",100);
			photoThreadId.remove(position);
			Message message = new Message();
			message.what = getFriendImage;
			handler.sendMessage(message);
		}
	}
	String refresh = "";
	@Override
	public void onRefresh() {
		refresh = "";
		String url = Constant.BaseUrl + "customer/" + Variable.cust_id
				+ "/get_relations?auth_code=" + Variable.auth_code;
		new Thread(new NetThread.GetDataThread(handler, url, refreshNotice)).start();
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
}