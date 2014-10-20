package com.wise.car;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.NetThread;
import pubclas.Variable;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.wise.baba.R;

/**
 * 车辆行程列表
 * 
 * @author meng
 */
public class TravelActivity extends Activity {

	private static final String TAG = "TravelActivity";
	private static final int get_data = 1;
	TextView tv_travel_date, tv_distance, tv_fuel, tv_hk_fuel, tv_money;
	ListView lv_activity_travel;
	List<TravelData> travelDatas = new ArrayList<TravelData>();
	TravelAdapter travelAdapter;
	String Date;
	String device_id = "3";
	private GeoCoder mGeoCoder = null;
	int index;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_travel);
		mGeoCoder = GeoCoder.newInstance();
		mGeoCoder.setOnGetGeoCodeResultListener(listener);
		tv_travel_date = (TextView) findViewById(R.id.tv_travel_date);
		tv_distance = (TextView) findViewById(R.id.tv_distance);
		tv_fuel = (TextView) findViewById(R.id.tv_fuel);
		tv_hk_fuel = (TextView) findViewById(R.id.tv_hk_fuel);
		tv_money = (TextView) findViewById(R.id.tv_money);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		ImageView iv_activity_travel_data_next = (ImageView) findViewById(R.id.iv_activity_travel_data_next);
		iv_activity_travel_data_next.setOnClickListener(onClickListener);
		ImageView iv_activity_travel_data_previous = (ImageView) findViewById(R.id.iv_activity_travel_data_previous);
		iv_activity_travel_data_previous.setOnClickListener(onClickListener);
		lv_activity_travel = (ListView) findViewById(R.id.lv_activity_travel);

		index = getIntent().getIntExtra("index", 0);
		Date = GetSystem.GetNowDay();
		tv_travel_date.setText(Date);
		GetDataTrip();

		travelAdapter = new TravelAdapter();
		lv_activity_travel.setAdapter(travelAdapter);
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;

			case R.id.iv_activity_travel_data_next:// 下一日
				Date = GetSystem.GetNextData(Date, 1);
				tv_travel_date.setText(Date);
				GetDataTrip();
				break;
			case R.id.iv_activity_travel_data_previous:// 上一日
				Date = GetSystem.GetNextData(Date, -1);
				tv_travel_date.setText(Date);
				GetDataTrip();
				break;
			}
		}
	};
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case get_data:
				jsonData(msg.obj.toString());
				break;
			}
		}
	};

	/**
	 * 解析数据
	 * 
	 * @param result
	 */
	private void jsonData(String result) {
		try {
			travelDatas.clear();
			JSONObject jsonObject = new JSONObject(result);
			String distance = "行驶总里程:" + jsonObject.getString("total_distance")
					+ "KM";
			tv_distance.setText(distance);
			String fuel = "油耗:" + jsonObject.getString("avg_fuel") + "L";
			tv_fuel.setText(fuel);
			String hk_fuel = "百公里油耗:" + jsonObject.getString("total_fuel")
					+ "L";
			tv_hk_fuel.setText(hk_fuel);
			String fee = "花费:" + jsonObject.getString("total_fee") + "元";
			tv_money.setText(fee);

			JSONArray jsonArray = jsonObject.getJSONArray("data");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject2 = jsonArray.getJSONObject(i);
				if (jsonObject2.opt("start_time") == null) {

				} else {
					TravelData travelData = new TravelData();
					travelData.setStartTime(GetSystem
							.ChangeTimeZone(jsonObject2.getString("start_time")
									.replace("T", " ").substring(0, 19)));
					travelData.setStopTime(GetSystem.ChangeTimeZone(jsonObject2
							.getString("end_time").replace("T", " ")
							.substring(0, 19)));

					travelData.setSpacingTime(GetSystem.ProcessTime(GetSystem
							.spacingTime(travelData.getStartTime(),
									travelData.getStopTime())));

					travelData.setStart_lat(jsonObject2.getString("start_lat"));
					travelData.setStart_lon(jsonObject2.getString("start_lon"));
					travelData.setEnd_lat(jsonObject2.getString("end_lat"));
					travelData.setEnd_lon(jsonObject2.getString("end_lon"));
					travelData.setStart_place("起始位置");
					travelData.setEnd_place("结束位置");
					travelData.setSpacingDistance(jsonObject2
							.getString("cur_distance"));
					travelData.setAverageOil("百公里油耗："
							+ jsonObject2.getString("cur_fuel") + "L");
					travelData.setOil("油耗：" + jsonObject2.getString("avg_fuel")
							+ "L");
					travelData.setSpeed("平均速度："
							+ jsonObject2.getString("avg_speed") + "km/h");
					travelData.setCost("花费：" + jsonObject2.getString("cur_fee")
							+ "元");
					travelDatas.add(travelData);
				}
			}
			travelAdapter.notifyDataSetChanged();
			if (travelDatas.size() > 0) {
				i = 0;
				isFrist = true;
				double lat = Double.valueOf(travelDatas.get(i).getStart_lat());
				double lon = Double.valueOf(travelDatas.get(i).getStart_lon());
				LatLng latLng = new LatLng(lat, lon);
				System.out.println("获取位置");
				mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption()
						.location(latLng));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	int i = 0;
	boolean isFrist = true;

	/**
	 * 从服务器上获取数据
	 */
	private void GetDataTrip() {
		String url;
		String Gas_no;
		if (Variable.carDatas.get(index).getGas_no() == null
				|| Variable.carDatas.get(index).getGas_no().equals("")) {
			Gas_no = "93#(92#)";
		} else {
			Gas_no = Variable.carDatas.get(index).getGas_no();
		}
		try {
			url = Constant.BaseUrl + "device/" + Variable.carDatas.get(index).getDevice_id() + "/trip?auth_code="
					+ Variable.auth_code + "&day=" + Date + "&city="
					+ URLEncoder.encode(Variable.City, "UTF-8") + "&gas_no="
					+ Gas_no;
			new Thread(new NetThread.GetDataThread(handler, url, get_data))
					.start();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}

	private class TravelAdapter extends BaseAdapter {
		LayoutInflater mInflater = LayoutInflater.from(TravelActivity.this);

		@Override
		public int getCount() {
			return travelDatas.size();
		}

		@Override
		public Object getItem(int position) {
			return travelDatas.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_travel, null);
				holder = new ViewHolder();
				holder.tv_item_travel_startTime = (TextView) convertView
						.findViewById(R.id.tv_item_travel_startTime);
				holder.tv_item_travel_stopTime = (TextView) convertView
						.findViewById(R.id.tv_item_travel_stopTime);
				holder.tv_item_travel_startPlace = (TextView) convertView
						.findViewById(R.id.tv_item_travel_startPlace);
				holder.tv_item_travel_stopPlace = (TextView) convertView
						.findViewById(R.id.tv_item_travel_stopPlace);
				holder.tv_item_travel_spacingDistance = (TextView) convertView
						.findViewById(R.id.tv_item_travel_spacingDistance);
				holder.tv_item_travel_averageOil = (TextView) convertView
						.findViewById(R.id.tv_item_travel_averageOil);
				holder.tv_item_travel_oil = (TextView) convertView
						.findViewById(R.id.tv_item_travel_oil);
				holder.tv_item_travel_speed = (TextView) convertView
						.findViewById(R.id.tv_item_travel_speed);
				holder.tv_item_travel_cost = (TextView) convertView
						.findViewById(R.id.tv_item_travel_cost);
				holder.iv_item_travel_map = (ImageView) convertView
						.findViewById(R.id.iv_item_travel_map);
				holder.iv_item_travel_share = (ImageView) convertView
						.findViewById(R.id.iv_item_travel_share);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final TravelData travelData = travelDatas.get(position);
			holder.tv_item_travel_startTime.setText(travelData.getStartTime()
					.substring(10, 16));
			holder.tv_item_travel_stopTime.setText(travelData.getStopTime()
					.substring(10, 16));
			holder.tv_item_travel_startPlace.setText(travelData
					.getStart_place());
			holder.tv_item_travel_stopPlace.setText(travelData.getEnd_place());
			// TODO
			holder.tv_item_travel_spacingDistance.setText("共"
					+ travelData.getSpacingDistance() + "公里\\"
					+ travelData.getSpacingTime());
			holder.tv_item_travel_averageOil
					.setText(travelData.getAverageOil());
			holder.tv_item_travel_oil.setText(travelData.getOil());
			holder.tv_item_travel_speed.setText(travelData.getSpeed());
			holder.tv_item_travel_cost.setText(travelData.getCost());
			holder.iv_item_travel_share
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							StringBuffer sb = new StringBuffer();
							sb.append("【行程】 ");
							sb.append(travelData.getStartTime()
									.substring(5, 16));
							sb.append(" 从" + travelData.getStart_place());
							sb.append("到" + travelData.getEnd_place());
							sb.append("，共行驶" + travelData.getSpacingDistance());
							sb.append("公里，耗时" + travelData.getSpacingTime());
							sb.append("，" + travelData.getOil());
							sb.append("，" + travelData.getCost());
							sb.append("，" + travelData.getAverageOil());
							sb.append("，" + travelData.getSpeed());
							System.out.println(sb.toString());
							GetSystem.share(TravelActivity.this, sb.toString(),
									"", 0, 0, "行程", "");
						}
					});
			holder.iv_item_travel_map.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(TravelActivity.this,
							TravelMapActivity.class);
					intent.putExtra("StartTime", travelDatas.get(position)
							.getStartTime());
					intent.putExtra("StopTime", travelDatas.get(position)
							.getStopTime());
					intent.putExtra("Start_place", travelDatas.get(position)
							.getStart_place());
					intent.putExtra("End_place", travelDatas.get(position)
							.getEnd_place());
					intent.putExtra("SpacingDistance", travelDatas
							.get(position).getSpacingDistance());
					intent.putExtra("SpacingTime", travelDatas.get(position)
							.getSpacingTime());
					intent.putExtra("AverageOil", travelDatas.get(position)
							.getAverageOil());
					intent.putExtra("Oil", travelDatas.get(position).getOil());
					intent.putExtra("Speed", travelDatas.get(position)
							.getSpeed());
					intent.putExtra("Cost", travelDatas.get(position).getCost());
					intent.putExtra("index", index);
					TravelActivity.this.startActivity(intent);
				}
			});
			return convertView;
		}

		private class ViewHolder {
			TextView tv_item_travel_startTime, tv_item_travel_stopTime,
					tv_item_travel_startPlace, tv_item_travel_stopPlace,
					tv_item_travel_spacingDistance, tv_item_travel_averageOil,
					tv_item_travel_oil, tv_item_travel_speed,
					tv_item_travel_cost;
			ImageView iv_item_travel_map, iv_item_travel_share;
		}
	}

	private class TravelData {
		String startTime;
		String stopTime;
		String spacingTime;
		String start_lat;
		String start_lon;
		String start_place;
		String end_lat;
		String end_lon;
		String end_place;
		String spacingDistance;
		String oil;
		String averageOil;
		String speed;
		String cost;

		public String getStartTime() {
			return startTime;
		}

		public void setStartTime(String startTime) {
			this.startTime = startTime;
		}

		public String getStopTime() {
			return stopTime;
		}

		public void setStopTime(String stopTime) {
			this.stopTime = stopTime;
		}

		public String getSpacingTime() {
			return spacingTime;
		}

		public void setSpacingTime(String spacingTime) {
			this.spacingTime = spacingTime;
		}

		public String getStart_lat() {
			return start_lat;
		}

		public void setStart_lat(String start_lat) {
			this.start_lat = start_lat;
		}

		public String getStart_lon() {
			return start_lon;
		}

		public void setStart_lon(String start_lon) {
			this.start_lon = start_lon;
		}

		public String getEnd_lat() {
			return end_lat;
		}

		public void setEnd_lat(String end_lat) {
			this.end_lat = end_lat;
		}

		public String getEnd_lon() {
			return end_lon;
		}

		public void setEnd_lon(String end_lon) {
			this.end_lon = end_lon;
		}

		public String getStart_place() {
			return start_place;
		}

		public void setStart_place(String start_place) {
			this.start_place = start_place;
		}

		public String getEnd_place() {
			return end_place;
		}

		public void setEnd_place(String end_place) {
			this.end_place = end_place;
		}

		public String getSpacingDistance() {
			return spacingDistance;
		}

		public void setSpacingDistance(String spacingDistance) {
			this.spacingDistance = spacingDistance;
		}

		public String getOil() {
			return oil;
		}

		public void setOil(String oil) {
			this.oil = oil;
		}

		public String getAverageOil() {
			return averageOil;
		}

		public void setAverageOil(String averageOil) {
			this.averageOil = averageOil;
		}

		public String getSpeed() {
			return speed;
		}

		public void setSpeed(String speed) {
			this.speed = speed;
		}

		public String getCost() {
			return cost;
		}

		public void setCost(String cost) {
			this.cost = cost;
		}
	}

	OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
		@Override
		public void onGetGeoCodeResult(GeoCodeResult arg0) {
		}

		@Override
		public void onGetReverseGeoCodeResult(ReverseGeoCodeResult arg0) {
			String strInfo = arg0.getAddress();
			strInfo = strInfo.substring((strInfo.indexOf("市") + 1),
					strInfo.length());
			if (isFrist) {// 起点位置取完，在取结束位置
				travelDatas.get(i).setStart_place("起点：" + strInfo);
				isFrist = false;
				double lat = Double.valueOf(travelDatas.get(i).getEnd_lat());
				double lon = Double.valueOf(travelDatas.get(i).getEnd_lon());
				LatLng latLng = new LatLng(lat, lon);
				mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption()
						.location(latLng));
				i++;
			} else {
				travelDatas.get(i - 1).setEnd_place("终点：" + strInfo);
				if (travelDatas.size() == i) {
					System.out.println("递归完毕");
				} else {
					isFrist = true;
					double lat = Double.valueOf(travelDatas.get(i)
							.getStart_lat());
					double lon = Double.valueOf(travelDatas.get(i)
							.getStart_lon());
					LatLng latLng = new LatLng(lat, lon);
					mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption()
							.location(latLng));
				}
			}
			travelAdapter.notifyDataSetChanged();
		}

	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mGeoCoder.destroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

}
