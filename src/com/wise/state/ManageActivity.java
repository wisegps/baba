package com.wise.state;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
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

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.JsonData;
import com.wise.baba.biz.LruImageCache;
import com.wise.baba.entity.CarData;
import com.wise.baba.net.NetThread;
import com.wise.car.CarUpdateActivity;
import com.wise.car.TravelActivity;
import com.wise.remind.RemindListActivity;
import com.wise.violation.TrafficActivity;

/**
 * 服务商管理界面
 * 
 * @author honesty
 **/
public class ManageActivity extends Activity {
	private static final int getAllCarData = 1;

	ExpandableListView elv_cars;

	/** 好友id **/
	private String friendId;
	AppApplication app;
	/** 好友下的车辆信息 **/
	public static List<CarData> carDatas = new ArrayList<CarData>();
	List<List<String>> details = new ArrayList<List<String>>();
	ManageAdapter manageAdapter;
	private int[] authToMe;
	
	private int index = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_manage);
		app = (AppApplication) getApplication();
		friendId = "" + getIntent().getIntExtra("FriendId", 0);
		authToMe = getIntent().getIntArrayExtra("authToMe");
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
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
				manageAdapter = new ManageAdapter();
				elv_cars.setAdapter(manageAdapter);
				elv_cars.expandGroup(0);// 默认展开第一个
				break;
			}
		}

	};

	public void startAnimation(final int groupPosition, boolean open) {

		new Handler().post(new Runnable() {
			@Override
			public void run() {
				ImageView ivIndicate = (ImageView) manageAdapter.getGroupview(
						groupPosition).findViewById(R.id.iv_indicate);
				Matrix m=new Matrix();
				m.postRotate(-90f,ivIndicate.getWidth()/2,ivIndicate.getHeight()/2);
				ivIndicate.setImageMatrix(m);
//				RotateAnimation animation = new RotateAnimation(0f, -90f,
//						Animation.RELATIVE_TO_SELF, 0.5f,
//						Animation.RELATIVE_TO_SELF, 0.5f);
//				animation.setDuration(100);// 设置动画持续时间
//				animation.setFillAfter(true);
//				ivIndicate.startAnimation(animation);
			}
		});

	}

	OnGroupExpandListener onGroupExpandListener = new OnGroupExpandListener() {

		@Override
		public void onGroupExpand(int groupPosition) {
			index = groupPosition;
			Log.i("ManageActivity", " groupPosition "+groupPosition);
			for (int i = 0; i < carDatas.size(); i++) {
				if (groupPosition != i) {
					elv_cars.collapseGroup(i);// 关闭其他展开项目
				}
			}
		}
	};

	/**
	 * 获取好友车辆数据
	 */
	private void getAllCarData() {
		String url = Constant.BaseUrl + "customer/" + friendId
				+ "/vehicle?auth_code=" + app.auth_code;
		new NetThread.GetDataThread(handler, url, getAllCarData).start();
	}

	private int RIGHT_OBD_DATA = 0x6001; // 访问OBD标准数据（服务商）
	private int RIGHT_ODB_ERR = 0x6002; // 访问OBD故障码数据（服务商）
	private int RIGHT_EVENT = 0x6003; // 访问车务提醒（服务商）
	private int RIGHT_VIOLATION = 0x6004; // 访问车辆违章（服务商）
	private int RIGHT_LOCATION = 0x6005; // 访问车辆实时位置（个人好友及服务商）
	private int RIGHT_TRIP = 0x6006; // 访问车辆行程（个人好友及服务商）
	private int RIGHT_FUEL = 0x6007; // 访问车辆油耗明细（服务商）
	private int RIGHT_DRIVESTAT = 0x6008; // 访问车辆驾驶习惯数据（服务商）

	private void jsonAllCarData(String result) {
		carDatas.addAll(JsonData.jsonCarInfo(result));
		for (CarData carData : carDatas) {
			List<String> strs = new ArrayList<String>();
			if (authToMe != null) {
				for (int i = 0; i < authToMe.length; i++) {
					System.out.println("authToMe = " + authToMe[i]);
					if (RIGHT_LOCATION == authToMe[i]) {

					} else if (RIGHT_TRIP == authToMe[i]) {
						strs.add("行程");
					} else if (RIGHT_OBD_DATA == authToMe[i]) {
						strs.add("车况");
					} else if (RIGHT_ODB_ERR == authToMe[i]) {

					} else if (RIGHT_EVENT == authToMe[i]) {
						strs.add("车务");
					} else if (RIGHT_VIOLATION == authToMe[i]) {
						strs.add("违章");
					} else if (RIGHT_FUEL == authToMe[i]) {
						strs.add("油耗");
					} else if (RIGHT_DRIVESTAT == authToMe[i]) {
						strs.add("驾驶");
					}
				}
				// strs.add("信息");
			}
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
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			return getChildView(groupPosition, childPosition);
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			List<String> strs = details.get(groupPosition);
			if (strs.size() == 0) {
				return 0;
			} else {
				return 1;
			}
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
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
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
			View viewChild = LayoutInflater.from(ManageActivity.this).inflate(
					R.layout.item_child_cars, null);
			GridView gv_compet = (GridView) viewChild
					.findViewById(R.id.gv_compet);
			gv_compet.setSelector(new ColorDrawable(Color.TRANSPARENT));// 取消GridView中Item选中时默认的背景色
//			System.out.println("details.get(groupPosition).size() = "
//					+ details.get(groupPosition).size());
//			int px = (int) TypedValue.applyDimension(
//					TypedValue.COMPLEX_UNIT_DIP, 40, getResources()
//							.getDisplayMetrics());
//			LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,
//					myMathCeil(details.get(groupPosition).size(), 4) * px);
//			gv_compet.setLayoutParams(params);
			gv_compet.setAdapter(new CompetAdapter(details.get(groupPosition)));
			gv_compet.setOnItemClickListener(onItemClickListener);
			return viewChild;
		}

		private View getGroupview(int groupPosition) {
			Log.i("ManageActivity", " Adapter getGroupview 刷新界面");
			
			
			View viewChild = LayoutInflater.from(ManageActivity.this).inflate(
					R.layout.item_group_cars, null);
			TextView tv_serial = (TextView) viewChild
					.findViewById(R.id.tv_serial);
			TextView tv_name = (TextView) viewChild.findViewById(R.id.tv_name);
			ImageView iv_icon = (ImageView) viewChild
					.findViewById(R.id.iv_icon);
			CarData carData = carDatas.get(groupPosition);
			tv_serial.setText(carData.getCar_series());
			if (new File(Constant.VehicleLogoPath + carData.getCar_brand_id()
					+ ".png").exists()) {
				Bitmap image = BitmapFactory
						.decodeFile(Constant.VehicleLogoPath
								+ carData.getCar_brand_id() + ".png");
				iv_icon.setImageBitmap(image);
			} else {
				iv_icon.setImageResource(R.drawable.icon_car_moren);
			}
			tv_name.setText(carData.getObj_name());
			
			
			ImageView iv_indicate = (ImageView) viewChild
					.findViewById(R.id.iv_indicate);
			iv_indicate.setImageResource(R.drawable.ico_right);
			if(index == groupPosition && elv_cars.isGroupExpanded(groupPosition)){
				RotateAnimation animation = new RotateAnimation(0f, 90f,
						Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				animation.setDuration(100);// 设置动画持续时间
				animation.setFillAfter(true);
				iv_indicate.startAnimation(animation);
			}
			
			return viewChild;
		}
	}

	/** 向上取整 **/
	public int myMathCeil(int a, int b) {
		if (a % b == 0) {
			return a / b;
		} else {
			return a / b + 1;
		}
	}

	int groupIndex = 0;
	public static final int FEE = 2;// 费用
	OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			System.out.println("position = " + position);
			if(position>=details.get(groupIndex).size()){
				return;
			}
			// groupIndex 对应的那个车 , position ，对应的车里的某个权限
			CarData carData = carDatas.get(groupIndex);

			List<String> strDetails = details.get(groupIndex);
			String detail = strDetails.get(position);
			// 信息
			if (detail.equals("信息")) {
				Intent intent = new Intent(ManageActivity.this,
						CarUpdateActivity.class);
				intent.putExtra("isService", true);
				intent.putExtra("index", groupIndex);
				startActivityForResult(intent, 2);
				return;
			}
			// 行程
			if (detail.equals("行程")) {
				Intent intent = new Intent(ManageActivity.this,
						TravelActivity.class);
				intent.putExtra("device_id", carData.getDevice_id());
				String Gas_no = "93#(92#)";
				;
				if (carData.getGas_no() != null) {
					Gas_no = carData.getGas_no();
				}
				intent.putExtra("Gas_no", Gas_no);
				startActivity(intent);
				return;
			}
			// 车况
			if (detail.equals("车况")) {
				Intent intent = new Intent(ManageActivity.this,
						FaultDetectionActivity.class);
				intent.putExtra("carDatas", (Serializable) carDatas);
				intent.putExtra("index", groupIndex);
				startActivity(intent);
				return;
			}
			// 油耗
			if (detail.equals("油耗")) {
				Intent intent = new Intent(ManageActivity.this,
						FuelActivity.class);
				intent.putExtra("carData", carData);
				intent.putExtra("type", FEE);
				startActivity(intent);
				return;
			}
			// 驾驶
			if (detail.equals("驾驶")) {
				Intent intent = new Intent(ManageActivity.this,
						DriveActivity.class);
				intent.putExtra("isNearData", true);
				intent.putExtra("carData", carData);
				startActivity(intent);
				return;
			}
			// 车务提醒
			if (detail.equals("车务")) {
				Intent intent = new Intent(ManageActivity.this,
						RemindListActivity.class);
				intent.putExtra("cust_id", friendId);
				intent.putExtra("carDatas", (Serializable) carDatas);
				startActivity(intent);
				return;
			}
			// 违章
			if (detail.equals("违章")) {
				Intent intent = new Intent(ManageActivity.this,
						TrafficActivity.class);
				intent.putExtra("isService", true);
				intent.putExtra("index", groupIndex);
				startActivity(intent);
				return;
			}
		}
	};

	private void ind() {

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 2 && resultCode == 3) {
			// 修改车辆信息返回
			manageAdapter.notifyDataSetChanged();
		}
	}

	public class CompetAdapter extends BaseAdapter {
		LayoutInflater mInflater = LayoutInflater.from(ManageActivity.this);
		List<String> compets;

		public CompetAdapter(List<String> compets) {
			this.compets = compets;
		}

		@Override
		public int getCount() {
			return 8;
		}

		@Override
		public Object getItem(int position) {
			if(position<compets.size()){
				return compets.get(position);
			}else{
				return null;
			}
			
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_car_manage, null);
			}
			TextView tv_item_manage = (TextView) convertView
					.findViewById(R.id.tv_item_manage);
			
			if(position<compets.size()){
				tv_item_manage.setText(compets.get(position));
			}else{
				tv_item_manage.setText(" ");
			}
			
			
			return convertView;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// static 文件手动删除
		carDatas.clear();
	}
}
