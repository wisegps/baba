package com.wise.notice;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.NetThread;
import pubclas.Variable;
import com.wise.baba.R;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class NoticeFragment extends Fragment {

	private final int GET_SMS = 1;
	NoticeAdapter noticeAdapter;
	BtnListener btnListener;

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
		ListView lv_notice = (ListView) getActivity().findViewById(R.id.lv_notice);
		noticeAdapter = new NoticeAdapter();
		lv_notice.setAdapter(noticeAdapter);
		lv_notice.setOnItemClickListener(onItemClickListener);
		getData();
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

	public void SetBtnListener(BtnListener btnListener) {
		this.btnListener = btnListener;
	}

	public interface BtnListener {
		public void Back();
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GET_SMS:
				jsonData(msg.obj.toString());
				noticeAdapter.notifyDataSetChanged();
				break;

			default:
				break;
			}
		}
	};
	OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
			System.out.println("onItemClickListener");
			NoticeData noticeData = noticeDatas.get(arg2);
			System.out.println("friend_type = " + noticeData.friend_type);
			switch (noticeData.friend_type) {
			case 1://秀爱车				
				break;
			case 2://秀服务
				break;
			case 3://问答
				break;
			case 4://通知
				break;
			case 99://私信
				clickLetter(arg2);
				break;
			}
		}
	};
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
		new Thread(new NetThread.GetDataThread(handler, url, GET_SMS)).start();
	}

	private void jsonData(String result) {
		try {
			JSONArray jsonArray = new JSONArray(result);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				NoticeData noticeData = new NoticeData();
				int friend_type = jsonObject.getInt("friend_type");
				noticeData.setFriend_type(friend_type);
				noticeData.setContent(jsonObject.getString("content"));
				noticeData.setFriend_name(jsonObject.getString("friend_name"));
				noticeData.setCreate_time(jsonObject.getString("create_time").replace("T", " ").substring(0, 19));
				//如果是私信的话,把头像url取下
				if(friend_type == 99){
					noticeData.setLogo(jsonObject.getString("logo"));
				}
				noticeDatas.add(noticeData);
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
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.item_notice, null);
				holder = new ViewHolder();
				holder.tv_type = (TextView) convertView.findViewById(R.id.tv_type);
				holder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
				holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			NoticeData noticeData = noticeDatas.get(position);
			holder.tv_content.setText(noticeData.getContent());
			holder.tv_type.setText(noticeData.getFriend_name());
			
			//间隔时间
			String create_time =GetSystem.ChangeTimeZone(noticeData.getCreate_time());
			int spacingData = GetSystem.spacingNowTime(create_time);			
			holder.tv_time.setText(GetSystem.showData(spacingData, create_time));
			return convertView;
		}

		private class ViewHolder {
			TextView tv_type;
			TextView tv_content;
			TextView tv_time;
		}
	}

	class NoticeData {
		/** 好友id 1: 秀爱车 2：秀服务 3：问答 4: 通知 >99: 私信id区段 **/
		int friend_id;
		/** 好友类型 1: 秀爱车 2：秀服务 3：问答 4: 通知 99: 私信 **/
		int friend_type;
		/** 排序id 1: 秀爱车 2：秀服务 3：问答 4: 通知 99: 私信 **/
		int order_id;
		String content;
		String create_time;
		String friend_name;
		String logo;

		public int getFriend_id() {
			return friend_id;
		}
		public void setFriend_id(int friend_id) {
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
		public String getCreate_time() {
			return create_time;
		}
		public void setCreate_time(String create_time) {
			this.create_time = create_time;
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
	}
}