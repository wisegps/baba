package com.wise.baba.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.Judge;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.widget.AlwaysMarqueeTextView;
import com.wise.baba.ui.widget.NoticeScrollTextView;
import com.wise.car.CarActivity;
import com.wise.car.CarAddActivity;
import com.wise.car.CarUpdateActivity;
import com.wise.notice.NoticeActivity;
import com.wise.setting.LoginActivity;


/**
 * 滚动消息
 * 
 * @author honesty
 **/
public class FragmentScrollMessage extends Fragment {

	/** 获取滚动消息 **/
	private static final int getMessage = 5;
	/** 定时滚动消息 **/
	private static final int Nstv = 6;

	NoticeScrollTextView nstv_message;
	/** 滚动消息数据 **/
	List<NsData> nsDatas = new ArrayList<NsData>();
	AppApplication app;
	String noticeUrl = "";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_scroll_message, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		app = (AppApplication) getActivity().getApplication();
		nstv_message = (NoticeScrollTextView) getActivity().findViewById(R.id.nstv_message);
		getScrollMessage();
		new CycleNstvThread().start();
	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.tv_message:
				nstvClick();
				break;
			}
		}
	};

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case getMessage:
				setMessageView(msg.obj.toString());
				break;
			case Nstv:
				ScrollMessage();
				break;
			}
		}

	};

	/** 获取滚动消息 **/
	public void getScrollMessage() {
		if (Judge.isLogin(app)) {// 已登录
			noticeUrl = Constant.BaseUrl + "customer/" + app.cust_id + "/tips?auth_code=" + app.auth_code;
		} else {
			noticeUrl = Constant.BaseUrl + "customer/0/tips";
		}
		getMessage(noticeUrl);
	}

	/** 获取滚动消息通知 **/
	private void getMessage(String url) {
		new NetThread.GetDataThread(handler, url, getMessage).start();
	}

	/** 当前显示消息数目 **/
	int index_message = 0;

	/** 滚动消息点击 **/
	private void nstvClick() {
		if (nsDatas == null || nsDatas.size() == 0) {
			// TODO 没有数据不考虑
		} else {
			if (index_message < nsDatas.size()) {
				// 防止数组越界
				int type = nsDatas.get(index_message).getType();
				switch (type) {
				case 0:
					// 注册用户
					startActivity(new Intent(getActivity(), LoginActivity.class));
					break;
				case 1:
					// 注册车辆
					startActivity(new Intent(getActivity(), CarAddActivity.class));
					break;
				case 2:
					// 修改车辆
					int index = getIndexFromId(nsDatas.get(index_message).getObj_id());
					if (index == -1) {
						// 没有在列表找到对应的车
						startActivity(new Intent(getActivity(), CarActivity.class));
					} else {
						Intent intent = new Intent(getActivity(), CarUpdateActivity.class);
						intent.putExtra("index", index);
						startActivityForResult(intent, 2);
					}
					break;
				case 3:
					// 绑定终端
					startActivity(new Intent(getActivity(), CarActivity.class));
					break;
				case 4:
					// 消息
					startActivity(new Intent(getActivity(), NoticeActivity.class));
					break;
				case 5:
					// 问答
					break;
				case 6:
					// 私信
					break;
				}
			}
		}
	}

	boolean isCycle = true;

	// 定时调整滚动消息
	class CycleNstvThread extends Thread {
		@Override
		public void run() {
			super.run();
			while (isCycle) {
				try {
					Thread.sleep(10000);
					Message message = new Message();
					message.what = Nstv;
					handler.sendMessage(message);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/** 根据obj_id返回对应列表的位置 **/
	private int getIndexFromId(int obj_id) {
		for (int i = 0; i < app.carDatas.size(); i++) {
			if (app.carDatas.get(i).getObj_id() == obj_id) {
				return i;
			}
		}
		return -1;
	}

	/** 解析滚动消息并绑定 **/
	private void setMessageView(String str) {
		nsDatas.clear();
		try {
			JSONArray jsonArray = new JSONArray(str);
			if (jsonArray.length() > 0) {
				nstv_message.setVisibility(View.VISIBLE);
			} else {
				nstv_message.setVisibility(View.GONE);
			}
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String content = jsonObject.getString("content");
				View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_nstv, null);
				AlwaysMarqueeTextView tv_message = (AlwaysMarqueeTextView) v.findViewById(R.id.tv_message);
				tv_message.setText(content);
				tv_message.setOnClickListener(onClickListener);
				nstv_message.addView(v);
				NsData nsData = new NsData();
				nsData.setType(jsonObject.getInt("type"));
				if (jsonObject.opt("obj_id") == null) {
					nsData.setObj_id(0);
				} else {
					nsData.setObj_id(jsonObject.getInt("obj_id"));
				}
				nsDatas.add(nsData);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/** 定时滚动 **/
	private void ScrollMessage() {
		index_message++;
		if (index_message >= nsDatas.size()) {
			index_message = 0;
		}
		nstv_message.snapToScreen(index_message);
	}

	/** 滚动消息 **/
	class NsData {
		int type;
		int obj_id;

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public int getObj_id() {
			return obj_id;
		}

		public void setObj_id(int obj_id) {
			this.obj_id = obj_id;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		isCycle = false;
	}
}
