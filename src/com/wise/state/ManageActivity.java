package com.wise.state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pubclas.Constant;
import pubclas.JsonData;
import pubclas.NetThread;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.car.CarUpdateActivity;
import com.wise.car.TravelActivity;
import com.wise.remind.RemindListActivity;
import com.wise.violation.TrafficActivity;

import data.CarData;

/**
 * 服务商管理界面
 * @author honesty
 **/
public class ManageActivity extends Activity {
	private static final int getAllCarData = 1;

	ExpandableListView elv_cars;

	/** 好友id **/
	private String friendId;
	AppApplication app;
	/** 好友下的车辆信息 **/
	List<CarData> carDatas = new ArrayList<CarData>();
	List<List<String>> details = new ArrayList<List<String>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_manage);
		app = (AppApplication) getApplication();
		friendId = ""+getIntent().getIntExtra("FriendId", 0);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		elv_cars = (ExpandableListView) findViewById(R.id.elv_cars);
		elv_cars.setGroupIndicator(null);
		elv_cars.setOnGroupExpandListener(onGroupExpandListener);
		getAllCarData();
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

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {

			case getAllCarData:
				jsonAllCarData(msg.obj.toString());
				ManageAdapter manageAdapter = new ManageAdapter();
				elv_cars.setAdapter(manageAdapter);
				elv_cars.expandGroup(0);//默认展开第一个
				break;
			}
		}

	};
	
	OnGroupExpandListener onGroupExpandListener = new OnGroupExpandListener() {
		
		@Override
		public void onGroupExpand(int groupPosition) {				
			for(int i = 0 ; i < carDatas.size() ; i++){
				if(groupPosition != i){
					elv_cars.collapseGroup(i);//关闭其他展开项目
				}
			}
		}
	};

	/**
	 * 获取好友车辆数据
	 */
	private void getAllCarData() {
		String url = Constant.BaseUrl + "customer/" + friendId + "/vehicle?auth_code=" + app.auth_code;
		new NetThread.GetDataThread(handler, url, getAllCarData).start();
	}

	private void jsonAllCarData(String result) {
		carDatas.addAll(JsonData.jsonCarInfo(result));
		for (CarData carData : carDatas) {
			List<String> strs = new ArrayList<String>();
			strs.add("信息");
			strs.add("行程");
			strs.add("车况");
			strs.add("油耗");
			strs.add("驾驶");
			strs.add("车务");
			strs.add("违章");
			details.add(strs);
		}
	}

	class ManageAdapter extends BaseExpandableListAdapter {
		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return details.get(groupPosition).get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			return getChildView(groupPosition, childPosition);
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return 1;
		}

		@Override
		public Object getGroup(int groupPosition) {
			return carDatas.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return carDatas.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			return getGroupview(groupPosition);
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		private View getChildView(int groupPosition, int position) {
			groupIndex = groupPosition;
			View viewChild = LayoutInflater.from(ManageActivity.this).inflate(R.layout.item_child_cars, null);
			GridView gv_compet = (GridView) viewChild.findViewById(R.id.gv_compet);
			System.out.println("details.get(groupPosition).size() = " + details.get(groupPosition).size());
			int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
			LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, myMathCeil(details.get(groupPosition).size(), 4) * px);
			gv_compet.setLayoutParams(params);
			gv_compet.setAdapter(new CompetAdapter(details.get(groupPosition)));
			gv_compet.setOnItemClickListener(onItemClickListener);
			return viewChild;
		}

		private View getGroupview(int groupPosition) {
			View viewChild = LayoutInflater.from(ManageActivity.this).inflate(R.layout.item_group_cars, null);
			TextView tv_serial = (TextView) viewChild.findViewById(R.id.tv_serial);
			TextView tv_name = (TextView) viewChild.findViewById(R.id.tv_name);
			ImageView iv_icon = (ImageView) viewChild.findViewById(R.id.iv_icon);
			CarData carData = carDatas.get(groupPosition);
			tv_serial.setText(carData.getCar_series());
			tv_name.setText(carData.getObj_name());
			return viewChild;
		}
	}
	/**向上取整**/
	public int myMathCeil(int a,int b){
		if(a%b == 0){
			return a/b;
		}else{
			return a/b + 1;
		}
	}
	
	int groupIndex = 0;
	public static final int FEE = 2;// 费用
	OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			System.out.println("position = " + position);
			//groupIndex 对应的那个车 , position ，对应的车里的某个权限
			CarData carData = carDatas.get(groupIndex);
			
			List<String> strDetails = details.get(groupIndex);
			String detail = strDetails.get(position);
			//信息
			if(detail.equals("信息")){
				Intent intent = new Intent(ManageActivity.this, CarUpdateActivity.class);
				startActivity(intent);
				return;
			}
			//行程
			if(detail.equals("行程")){
				Intent intent = new Intent(ManageActivity.this, TravelActivity.class);
				intent.putExtra("device_id", carData.getDevice_id());
				String Gas_no = "93#(92#)";;
				if(carData.getGas_no() != null){
					Gas_no = carData.getGas_no();
				}
				intent.putExtra("Gas_no", Gas_no);
				startActivity(intent);
				return;
			}
			//车务提醒
			if(detail.equals("车务")){
				Intent intent = new Intent(ManageActivity.this, RemindListActivity.class);
				intent.putExtra("cust_id", friendId);
				intent.putExtra("carDatas", (Serializable)carDatas);
				startActivity(intent);
				return;
			}
			if(detail.equals("违章")){
				Intent intent = new Intent(ManageActivity.this, TrafficActivity.class);
				startActivity(intent);
				return;
			}
			if(detail.equals("驾驶")){
				Intent intent = new Intent(ManageActivity.this, DriveActivity.class);
				intent.putExtra("isNearData", true);
				intent.putExtra("carData", carData);
				startActivity(intent);
				return;
			}
			if(detail.equals("油耗")){
				Intent intent = new Intent(ManageActivity.this, DriveActivity.class);
				intent.putExtra("carData", carData);
				intent.putExtra("type", FEE);
				startActivity(intent);
				return;
			}
			if(detail.equals("车况")){
				Intent intent = new Intent(ManageActivity.this, FaultDetectionActivity.class);
				intent.putExtra("carDatas", (Serializable)carDatas);
				intent.putExtra("index", groupIndex);
				startActivity(intent);
				return;
			}
		}
	};

	public class CompetAdapter extends BaseAdapter {
		LayoutInflater mInflater = LayoutInflater.from(ManageActivity.this);
		List<String> compets;

		public CompetAdapter(List<String> compets) {
			this.compets = compets;
		}

		@Override
		public int getCount() {
			return compets.size();
		}

		@Override
		public Object getItem(int position) {
			return compets.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_hot_city, null);
				holder = new ViewHolder();
				holder.tv_item_hot = (TextView) convertView.findViewById(R.id.tv_item_hot);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tv_item_hot.setText(compets.get(position));
			return convertView;
		}

		private class ViewHolder {
			TextView tv_item_hot;
		}
	}
}
