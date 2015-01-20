package fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.HttpFriend;
import pubclas.Info;
import pubclas.NetThread;
import xlist.XListView;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.car.BarcodeActivity;
import com.wise.notice.FriendAddActivity;
import com.wise.notice.FriendDetailActivity;
import com.wise.notice.FriendInfoActivity;
import com.wise.notice.ServiceListActivity;
import com.wise.notice.SureFriendActivity;

import customView.CircleImageView;
import data.FriendData;
import data.FriendSearch;

/**
 * 好友列表
 * @author honesty
 **/
public class FragmentFriend extends Fragment {

	private final static int get_all_friend = 4;
	private final static int getFriendImage = 5;
	private final static int searchById = 6;

	ImageView iv_add;
	XListView lv_friend;
	FriendAdapter friendAdapter;
	AppApplication app;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragement_friend, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		app = (AppApplication) getActivity().getApplication();
		iv_add = (ImageView) getActivity().findViewById(R.id.iv_add);
		iv_add.setOnClickListener(onClickListener);
		lv_friend = (XListView) getActivity().findViewById(R.id.lv_friend);
		friendAdapter = new FriendAdapter();
		lv_friend.setAdapter(friendAdapter);
		lv_friend.setPullLoadEnable(false);
		lv_friend.setPullRefreshEnable(false);
		lv_friend.setOnItemClickListener(onItemClickListener);
		lv_friend.setOnScrollListener(onScrollListener);
		getFriendData();
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_add:
				showMenu();
				break;
			case R.id.tv_add_friend:
				startActivity(new Intent(getActivity(), FriendAddActivity.class));
				break;
			case R.id.tv_camera:
				startActivityForResult(new Intent(getActivity(), BarcodeActivity.class), 1);
				break;
			}
		}
	};

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case getFriendImage:
				removeFriendThreadMark(msg.arg1);
				friendAdapter.notifyDataSetChanged();
				break;
			case get_all_friend:
				jsonFriendData(msg.obj.toString());
				break;
			case searchById:
				System.out.println("searchById");
				List<FriendSearch> friends = (List<FriendSearch>) msg.obj;
				if(friends==null || friends.size()<1){
					Toast.makeText(FragmentFriend.this.getActivity(), "暂无记录", Toast.LENGTH_SHORT).show();
					break;
				}
				Intent intent = new Intent(getActivity(), FriendDetailActivity.class);
				intent.putExtra(Info.FriendStatusKey, Info.FriendStatus.FriendAddFromId);
				intent.putExtra("friend", friends.get(0));
				startActivityForResult(intent, 2);
				break;
			}
		}
	};

	PopupWindow mPopupWindow;

	private void showMenu() {
		LayoutInflater mLayoutInflater = LayoutInflater.from(getActivity());
		View popunwindwow = mLayoutInflater.inflate(R.layout.pop_add_friend, null);
		TextView tv_add_friend = (TextView) popunwindwow.findViewById(R.id.tv_add_friend);
		tv_add_friend.setOnClickListener(onClickListener);
		TextView tv_camera = (TextView) popunwindwow.findViewById(R.id.tv_camera);
		tv_camera.setOnClickListener(onClickListener);
		mPopupWindow = new PopupWindow(popunwindwow, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mPopupWindow.setAnimationStyle(R.style.PopupAnimation);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopupWindow.setFocusable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.showAsDropDown(getActivity().findViewById(R.id.iv_add), 0, 0);
	}

	/** 好友列表点击 **/
	OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			if (arg2 == 1) {
				// 新的朋友(我添加的和别人添加我的)
				startActivityForResult(new Intent(getActivity(), SureFriendActivity.class), 3);
			}else if (arg2 == 2) {
				// TODO 服务商
				startActivityForResult(new Intent(getActivity(),  ServiceListActivity.class), 3);
			} else {
				// 去介绍界面
				Intent intent = new Intent(getActivity(), FriendInfoActivity.class);
				intent.putExtra("FriendId", String.valueOf(app.friendDatas.get(arg2 - 1).getFriend_id()));
				intent.putExtra("name", app.friendDatas.get(arg2 - 1).getFriend_name());
				intent.putExtra(Info.FriendStatusKey, Info.FriendStatus.FriendInfo);
				startActivityForResult(intent, 4);
			}
		}
	};
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
			if (i >= app.friendDatas.size()) {
				return;
			}
			FriendData friendData = app.friendDatas.get(i);
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
			Bitmap bitmap = GetSystem.getBitmapFromURL(app.friendDatas.get(position).getLogo());
			if (bitmap != null) {
				GetSystem.saveImageSD(bitmap, Constant.userIconPath, GetSystem.getM5DEndo(app.friendDatas.get(position).getLogo()) + ".png", 100);
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

	/** 获取好友数据 **/
	public void getFriendData() {
		String url = Constant.BaseUrl + "customer/" + app.cust_id + "/get_friends?auth_code=" + app.auth_code +"&friend_type=1";
		new NetThread.GetDataThread(handler, url, get_all_friend).start();
	}

	private void jsonFriendData(String result) {
		System.out.println(result);
		try {
			app.friendDatas.clear();
			FriendData fData = new FriendData();
			fData.setFriend_name("新的朋友");
			app.friendDatas.add(fData);
			FriendData fData1 = new FriendData();
			fData1.setFriend_name("服务商");
			app.friendDatas.add(fData1);
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
				app.friendDatas.add(friendData);
			}
			friendAdapter.notifyDataSetChanged();
			getFriendLogo();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class FriendAdapter extends BaseAdapter {
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
			if (position == 0) {
				// 第一项是新的朋友
				holder.iv_image.setImageResource(R.drawable.icon_people_no);
			}else if (position == 1) {
				// 第二项是服务商
				holder.iv_image.setImageResource(R.drawable.icon_people_no);
			} else {
				if (new File(Constant.userIconPath + GetSystem.getM5DEndo(friendData.getLogo()) + ".png").exists()) {
					Bitmap image = BitmapFactory.decodeFile(Constant.userIconPath + GetSystem.getM5DEndo(friendData.getLogo()) + ".png");
					holder.iv_image.setImageBitmap(image);
				} else {
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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1 && resultCode == 2) {
			String FriendId = data.getStringExtra("result");
			// 二维码扫描后跳转到用户信息界面
			HttpFriend http = new HttpFriend(this.getActivity(),handler);
			http.searchById(FriendId);
			return;
		} else if (requestCode == 2 && resultCode == 2) {
			// TODO 添加朋友返回
		} else if (requestCode == 3 && resultCode == 2) {
			// 确认接受朋友返回，刷新数据
			getFriendData();
		} else if (requestCode == 4 && resultCode == 2) {
			// 删除好友返回
			int deleteFriendId = data.getIntExtra("FriendId", 0);
			// 删除好友
			for (FriendData friendData : app.friendDatas) {
				if (friendData.getFriend_id() == deleteFriendId) {
					app.friendDatas.remove(friendData);
					break;
				}
			}
			friendAdapter.notifyDataSetChanged();// 刷新本地数据
			// 重新获取服务器数据，确保一致
			getFriendData();
		}
	}
}
