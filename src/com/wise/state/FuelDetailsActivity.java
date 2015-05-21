package com.wise.state;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import xlist.XListView;
import xlist.XListView.IXListViewListener;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.entity.CarData;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.widget.WaitLinearLayout;
import com.wise.baba.ui.widget.WaitLinearLayout.OnFinishListener;


/** 油耗明细列表 **/
public class FuelDetailsActivity extends Activity implements IXListViewListener {

	private static final int getData = 1;
	private static final int Load = 2;
	private static final int refresh_data = 3;

	List<FuelData> fuelDatas = new ArrayList<FuelData>();
	FuelAdapter fuelAdapter;
	WaitLinearLayout ll_wait;
	XListView lv_fuel;
	TextView tv_title_fee;
	CarData carData;

	String NowYear = "";
	AppApplication app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_fuel_details);
		app = (AppApplication) getApplication();
		ll_wait = (WaitLinearLayout) findViewById(R.id.ll_wait);
		ll_wait.setOnFinishListener(onFinishListener);
		TextView tv_name = (TextView) findViewById(R.id.tv_name);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		tv_title_fee = (TextView) findViewById(R.id.tv_title_fee);
		lv_fuel = (XListView) findViewById(R.id.lv_fuel);
		lv_fuel.setPullRefreshEnable(true);
		lv_fuel.setPullLoadEnable(true);
		lv_fuel.setBottomFinishListener(onFinishListener);
		lv_fuel.setOnFinishListener(onFinishListener);
		lv_fuel.setXListViewListener(this);
		fuelAdapter = new FuelAdapter();
		lv_fuel.setAdapter(fuelAdapter);
		carData = (CarData)getIntent().getSerializableExtra("carData");
		if(carData == null && app.carDatas.size()>0){
			carData = app.carDatas.get(app.currentCarIndex);
		}
		tv_name.setText(carData.getNick_name());
		NowYear = GetNowYear();
		getData();
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;

			default:
				break;
			}
		}
	};
	OnFinishListener onFinishListener = new OnFinishListener() {
		@Override
		public void OnFinish(int index) {
			if (index == 0) {
				fuelAdapter.notifyDataSetChanged();
				ll_wait.setVisibility(View.GONE);
				lv_fuel.setVisibility(View.VISIBLE);
			} else if (index == 2) {
				jsonData(load, false);
				fuelAdapter.notifyDataSetChanged();
				onLoad();
			} else if (index == 3) {
				jsonData(refresh, true);
				fuelAdapter.notifyDataSetChanged();
				onLoad();
			}
		}
	};
	String load = "";
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case getData:
				ll_wait.runFast(0);
				jsonData(msg.obj.toString(), true);
				break;
			case Load:
				lv_fuel.runBottomFast(2);
				load = msg.obj.toString();
				break;
			case refresh_data:
				lv_fuel.runFast(3);
				refresh = msg.obj.toString();
				break;
			}
		}
	};

	private void getData() {
		try {
			String url = Constant.BaseUrl + "device/"
					+ carData.getDevice_id()
					+ "/fee_detail?auth_code=" + app.auth_code + "&city="
					+ URLEncoder.encode(app.City, "UTF-8") + "&gas_no=#93(#92)";
			new NetThread.GetDataThread(handler, url, getData).start();
			ll_wait.startWheel();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	String month = "";
	double money = 0;
	FuelData fuelData2 = null;

	private void jsonData(String str, boolean isRefresh) {
		try {
			if (isRefresh) {
				month = "";
				money = 0;
				fuelDatas.clear();
			}
			JSONArray jsonArray = new JSONArray(str);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String Time = GetSystem.ChangeTimeZone(
						jsonObject.getString("rcv_day").substring(0, 19)
								.replace("T", " ")).substring(0, 10);
				if (!month.equals(Time.substring(0, 7))) {
					fuelData2 = new FuelData();
					fuelData2.setType(0);
					fuelData2.setRcv_day(Time);
					fuelDatas.add(fuelData2);
					month = Time.substring(0, 7);
					money = 0;
				}
				FuelData fuelData = new FuelData();
				try {
					fuelData.setAvg_fuel(String.format("%.2f",
							jsonObject.getDouble("avg_fuel")));
				} catch (Exception e) {
					fuelData.setAvg_fuel("0");
				}
				fuelData.setRcv_day(Time);
				fuelData.setTotal_distance(jsonObject
						.getString("total_distance"));
				fuelData.setTotal_fee(String.format("%.1f",
						jsonObject.getDouble("total_fee")));
				fuelData.setType(1);
				if (jsonObject.opt("day_trip_id") == null) {
					fuelData.setDay_trip_id("0");
				} else {
					fuelData.setDay_trip_id(jsonObject.getString("day_trip_id"));
				}
				fuelDatas.add(fuelData);
				money += jsonObject.getDouble("total_fee");
				if (fuelData2 != null) {
					fuelData2.setTotal_fee(String.format("%.1f", money));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	int index = 0;

	class FuelAdapter extends BaseAdapter {
		private static final int VALUE_TITLE = 0;
		private static final int VALUE_HOLDER = 1;
		LayoutInflater mInflater = LayoutInflater
				.from(FuelDetailsActivity.this);

		@Override
		public int getCount() {
			return fuelDatas.size();
		}

		@Override
		public Object getItem(int position) {
			return fuelDatas.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			int Type = getItemViewType(position);
			ViewTitle Title = null;
			ViewHolder holder = null;
			if (convertView == null) {
				if (Type == VALUE_TITLE) {
					convertView = mInflater.inflate(R.layout.item_fuel_title,
							null);
					Title = new ViewTitle();
					Title.tv_title_month = (TextView) convertView
							.findViewById(R.id.tv_title_month);
					Title.tv_title_fee = (TextView) convertView
							.findViewById(R.id.tv_title_fee);
					convertView.setTag(Title);
				} else {
					convertView = mInflater.inflate(R.layout.item_fuel, null);
					holder = new ViewHolder();
					holder.tv_day = (TextView) convertView
							.findViewById(R.id.tv_day);
					holder.tv_fuel = (TextView) convertView
							.findViewById(R.id.tv_fuel);
					holder.total_distance = (TextView) convertView
							.findViewById(R.id.total_distance);
					holder.tv_total_fee = (TextView) convertView
							.findViewById(R.id.tv_total_fee);
					convertView.setTag(holder);
				}
			} else {
				if (Type == VALUE_TITLE) {
					Title = (ViewTitle) convertView.getTag();
				} else {
					holder = (ViewHolder) convertView.getTag();
				}
			}

			FuelData fuelData = fuelDatas.get(position);
			String day = fuelData.getRcv_day();
			if (Type == VALUE_TITLE) {
				if (isNowYear(day)) {
					Title.tv_title_month.setText(ChineseMonth(Integer
							.valueOf(day.substring(5, 7))) + "花费: ");
					Title.tv_title_fee.setText(fuelData.getTotal_fee());
				} else {
					// 不是则显示年月
					Title.tv_title_month.setText(day.substring(0, 7) + "花费: ");
					Title.tv_title_fee.setText(fuelData.getTotal_fee());
				}
			} else {
				holder.tv_day.setText(day.substring(day.length() - 2,
						day.length())
						+ "日");
				holder.tv_fuel.setText(fuelData.getAvg_fuel());
				holder.total_distance.setText(fuelData.getTotal_distance());
				holder.tv_total_fee.setText(fuelData.getTotal_fee());
			}
			return convertView;
		}

		@Override
		public int getItemViewType(int position) {
			FuelData fuelData = fuelDatas.get(position);
			return fuelData.getType();
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		private class ViewHolder {
			TextView tv_day, tv_fuel, total_distance, tv_total_fee;
		}

		private class ViewTitle {
			TextView tv_title_fee, tv_title_month;
		}
	}

	class FuelData {
		private int Type;
		private String total_distance;
		private String avg_fuel;
		private String rcv_day;
		private String total_fee;
		private String day_trip_id;

		public int getType() {
			return Type;
		}

		public void setType(int type) {
			Type = type;
		}

		public String getTotal_distance() {
			return total_distance;
		}

		public void setTotal_distance(String total_distance) {
			this.total_distance = total_distance;
		}

		public String getAvg_fuel() {
			return avg_fuel;
		}

		public void setAvg_fuel(String avg_fuel) {
			this.avg_fuel = avg_fuel;
		}

		/** 2014-07-10 **/
		public String getRcv_day() {
			return rcv_day;
		}

		public void setRcv_day(String rcv_day) {
			this.rcv_day = rcv_day;
		}

		public String getTotal_fee() {
			return total_fee;
		}

		public void setTotal_fee(String total_fee) {
			this.total_fee = total_fee;
		}

		public String getDay_trip_id() {
			return day_trip_id;
		}

		public void setDay_trip_id(String day_trip_id) {
			this.day_trip_id = day_trip_id;
		}

		@Override
		public String toString() {
			return "FuelData [Type=" + Type + ", total_distance="
					+ total_distance + ", avg_fuel=" + avg_fuel + ", rcv_day="
					+ rcv_day + ", total_fee=" + total_fee + ", day_trip_id="
					+ day_trip_id + "]";
		}
	}

	String refresh = "";

	@Override
	public void onRefresh() {
		try {
			refresh = "";
			String url = Constant.BaseUrl + "device/"
					+ carData.getDevice_id()
					+ "/fee_detail?auth_code=" + app.auth_code + "&city="
					+ URLEncoder.encode(app.City, "UTF-8") + "&gas_no=#93(#92)";
			new NetThread.GetDataThread(handler, url, refresh_data).start();
			lv_fuel.startHeaderWheel();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onLoadMore() {
		try {
			load = "";
			String url = Constant.BaseUrl + "device/"
					+ carData.getDevice_id()
					+ "/fee_detail?auth_code=" + app.auth_code + "&city="
					+ URLEncoder.encode(app.City, "UTF-8") + "&min_id="
					+ fuelDatas.get(fuelDatas.size() - 1).getDay_trip_id()
					+ "&gas_no=#93(#92)";
			new NetThread.GetDataThread(handler, url, Load).start();
			lv_fuel.startBottomWheel();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void onLoad() {
		lv_fuel.refreshHeaderView();
		lv_fuel.refreshBottomView();
		lv_fuel.stopRefresh();
		lv_fuel.stopLoadMore();
		lv_fuel.setRefreshTime(GetSystem.GetNowTime());
	}

	/** 判断日期是否是当前年 **/
	private boolean isNowYear(String date) {
		if (date.indexOf(NowYear) >= 0) {
			return true;
		} else {
			return false;
		}
	}

	/** 获取当前年份 **/
	private String GetNowYear() {
		Time time = new Time();
		time.setToNow();
		return String.valueOf(time.year);
	}

	private String ChineseMonth(int month) {
		String Cmonth = "";
		switch (month) {
		case 1:
			Cmonth = "一月";
			break;
		case 2:
			Cmonth = "二月";
			break;
		case 3:
			Cmonth = "三月";
			break;
		case 4:
			Cmonth = "四月";
			break;
		case 5:
			Cmonth = "五月";
			break;
		case 6:
			Cmonth = "六月";
			break;
		case 7:
			Cmonth = "七月";
			break;
		case 8:
			Cmonth = "八月";
			break;
		case 9:
			Cmonth = "九月";
			break;
		case 10:
			Cmonth = "十月";
			break;
		case 11:
			Cmonth = "十一月";
			break;
		case 12:
			Cmonth = "十二月";
			break;
		}
		return Cmonth;
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}