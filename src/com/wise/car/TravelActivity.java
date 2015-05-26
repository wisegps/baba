package com.wise.car;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.entity.AdressData;
import com.wise.baba.entity.CollectionData;
import com.wise.baba.net.NetThread;


/**
 * 车辆行程列表
 * 
 * @author meng
 */
public class TravelActivity extends Activity {

	private static final String TAG = "TravelActivity";
	private static final int get_data = 1;
	private static final int deleteTravel = 2;
	private static final int renameTravel = 3;
	private static final int actAvgFuel = 4;
	private static final int collectAdress = 5;
	private static final int getIsCollect = 6;

	ImageView iv_activity_travel_data_next;
	TextView tv_travel_date, tv_distance, tv_fuel, tv_hk_fuel, tv_money;
	ListView lv_activity_travel;
	List<TravelData> travelDatas = new ArrayList<TravelData>();
	TravelAdapter travelAdapter;
	String Date;
	private GeoCoder mGeoCoder = null;
	String device_id = "0";
	String Gas_no = "93#(92#)";
	AppApplication app;
	ProgressDialog dialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_travel);
		app = (AppApplication) getApplication();
		mGeoCoder = GeoCoder.newInstance();
		mGeoCoder.setOnGetGeoCodeResultListener(listener);
		tv_travel_date = (TextView) findViewById(R.id.tv_travel_date);
		tv_distance = (TextView) findViewById(R.id.tv_distance);
		tv_fuel = (TextView) findViewById(R.id.tv_fuel);
		tv_hk_fuel = (TextView) findViewById(R.id.tv_hk_fuel);
		tv_money = (TextView) findViewById(R.id.tv_money);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		iv_activity_travel_data_next = (ImageView) findViewById(R.id.iv_activity_travel_data_next);
		iv_activity_travel_data_next.setOnClickListener(onClickListener);
		ImageView iv_activity_travel_data_previous = (ImageView) findViewById(R.id.iv_activity_travel_data_previous);
		iv_activity_travel_data_previous.setOnClickListener(onClickListener);
		lv_activity_travel = (ListView) findViewById(R.id.lv_activity_travel);

		device_id = getIntent().getStringExtra("device_id");
		Gas_no = getIntent().getStringExtra("Gas_no");
		if(Gas_no == null){
			Gas_no = "93#(92#)";
		}
		String iDate = getIntent().getStringExtra("Date");
		if (iDate != null) {
			Date = iDate;
		} else {
			Date = GetSystem.GetNowDay();
		}
		tv_travel_date.setText(Date);
		judgeNowData(Date);
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
				judgeNowData(Date);
				break;
			case R.id.iv_activity_travel_data_previous:// 上一日
				Date = GetSystem.GetNextData(Date, -1);
				tv_travel_date.setText(Date);
				GetDataTrip();
				iv_activity_travel_data_next.setVisibility(View.VISIBLE);
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
			case deleteTravel:
				jsonDeleteTravel(msg.obj.toString());
				break;
			case renameTravel:
				jsonRenameTravel(msg.obj.toString());
				break;
			case actAvgFuel:
				jsonActAvgFuel(msg.obj.toString());
				break;
			case collectAdress:
				jsonCollectAdress(msg.obj.toString());
				break;
			case getIsCollect:
				jsonCollect(msg.obj.toString(), msg.arg1);
				break;
			}
		}
	};

	/** 判断到日期是今天的话，不能下一日 **/
	private void judgeNowData(String Date) {
		boolean isMax = GetSystem.maxTime(Date + " 00:00:00", GetSystem.GetNowMonth().getDay() + " 00:00:00");
		if (isMax) {
			iv_activity_travel_data_next.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * 解析数据
	 * 
	 * @param result
	 */
	private void jsonData(String result) {
		try {
			travelDatas.clear();
			JSONObject jsonObject = new JSONObject(result);
			String distance = "行驶总里程：" + jsonObject.getString("total_distance") + "KM";
			tv_distance.setText(distance);
			String fuel = "油耗：" + jsonObject.getString("total_fuel") + "L";
			tv_fuel.setText(fuel);
			String hk_fuel = "百公里油耗：" + jsonObject.getString("avg_fuel") + "L";
			tv_hk_fuel.setText(hk_fuel);
			String fee = "花费：" + jsonObject.getString("total_fee") + "元";
			tv_money.setText(fee);
			String adressName = "";
			JSONArray jsonArray = jsonObject.getJSONArray("data");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject2 = jsonArray.getJSONObject(i);
				if (jsonObject2.opt("start_time") == null) {

				} else {
					TravelData travelData = new TravelData();
					travelData.setCollect(false);
					travelData.setTrip_id(jsonObject2.getInt("trip_id"));
					if (jsonObject2.opt("act_avg_fuel") == null) {
						travelData.setAct_avg_fuel(0);
					} else {
						travelData.setAct_avg_fuel(Float.valueOf(jsonObject2.getString("act_avg_fuel")));
					}
					if (jsonObject2.opt("trip_name") == null) {
						travelData.setTrip_name("");
					} else {
						String trip_name = jsonObject2.getString("trip_name");
						travelData.setTrip_name(trip_name);
						adressName += trip_name + ",";
					}
					travelData.setStartTime(GetSystem.ChangeTimeZone(jsonObject2.getString("start_time").replace("T", " ").substring(0, 19)));
					travelData.setStopTime(GetSystem.ChangeTimeZone(jsonObject2.getString("end_time").replace("T", " ").substring(0, 19)));

					travelData.setSpacingTime(GetSystem.ProcessTime(GetSystem.spacingTime(travelData.getStartTime(), travelData.getStopTime())));

					travelData.setStart_lat(jsonObject2.getString("start_lat"));
					travelData.setStart_lon(jsonObject2.getString("start_lon"));
					travelData.setEnd_lat(jsonObject2.getString("end_lat"));
					travelData.setEnd_lon(jsonObject2.getString("end_lon"));
					travelData.setStart_place("起始位置");
					travelData.setEnd_place("结束位置");
					travelData.setSpacingDistance(jsonObject2.getString("cur_distance"));
					travelData.setAverageOil("百公里油耗：" + jsonObject2.getString("avg_fuel") + "L");
					travelData.setOil("油耗：" + jsonObject2.getString("cur_fuel") + "L");
					travelData.setSpeed("平均速度：" + jsonObject2.getString("avg_speed") + "km/h");
					travelData.setCost("花费：" + jsonObject2.getString("cur_fee") + "元");
					travelDatas.add(travelData);
				}
			}
			judgeCollect(adressName, 1);
			travelAdapter.notifyDataSetChanged();
			if (travelDatas.size() > 0) {
				i = 0;
				isFrist = true;
				double lat = Double.valueOf(travelDatas.get(i).getStart_lat());
				double lon = Double.valueOf(travelDatas.get(i).getStart_lon());
				LatLng latLng = new LatLng(lat, lon);
				if (mGeoCoder != null) {
					mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
				}
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
		if(device_id == null || device_id .equals("0")){
			return;
		}
		String url;
		try {
			url = Constant.BaseUrl + "device/" + device_id + "/trip?auth_code=" + app.auth_code + "&day=" + Date + "&city="
					+ URLEncoder.encode(app.City, "UTF-8") + "&gas_no=" + Gas_no;
			
			Log.i("TravelActivity", url);
			new Thread(new NetThread.GetDataThread(handler, url, get_data)).start();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}

	/** 删除的记录在列表中的位置 **/
	int deleteTravelPosition;

	/** 删除行程 **/
	private void deleteTravel(int position) {
		dialog = ProgressDialog.show(TravelActivity.this, "提示", "行程删除中");
		dialog.setCancelable(true);
		deleteTravelPosition = position;
		int trip_id = travelDatas.get(position).getTrip_id();
		String url = Constant.BaseUrl + "device/trip/" + trip_id + "?auth_code=" + app.auth_code;
		new NetThread.DeleteThread(handler, url, deleteTravel).start();
	}

	/** 解析删除行程 **/
	private void jsonDeleteTravel(String result) {
		if (dialog != null) {
			dialog.dismiss();
		}
		try {
			JSONObject jsonObject = new JSONObject(result);
			if (jsonObject.getInt("status_code") == 0) {
				if (deleteTravelPosition < travelDatas.size()) {// 防止数组越界
					travelDatas.remove(deleteTravelPosition);
				}
				travelAdapter.notifyDataSetChanged();
			} else {
				Toast.makeText(TravelActivity.this, "行程删除失败", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			Toast.makeText(TravelActivity.this, "行程删除失败", Toast.LENGTH_SHORT).show();
		}
	}

	int renameTravelPosition;
	String rename;

	/** 重命名 **/
	private void renameTravel(int position, String name, boolean isProgressDialog) {
		try {
			if (isProgressDialog) {
				dialog = ProgressDialog.show(TravelActivity.this, "提示", "行程重命名中");
				dialog.setCancelable(true);
			}
			renameTravelPosition = position;
			rename = name;
			int trip_id = travelDatas.get(position).getTrip_id();
			String url = Constant.BaseUrl + "device/trip/" + trip_id + "/name?auth_code=" + app.auth_code;
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			// params.add(new BasicNameValuePair("trip_name",
			// URLEncoder.encode(name, "UTF-8")));
			params.add(new BasicNameValuePair("trip_name", name));
			new NetThread.putDataThread(handler, url, params, renameTravel).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 解析重命名 **/
	private void jsonRenameTravel(String result) {
		if (dialog != null) {
			dialog.dismiss();
		}
		try {
			JSONObject jsonObject = new JSONObject(result);
			if (jsonObject.getInt("status_code") == 0) {
				travelDatas.get(renameTravelPosition).setTrip_name(rename);
				travelAdapter.notifyDataSetChanged();
			} else {
				Toast.makeText(TravelActivity.this, "行程重命名失败", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			Toast.makeText(TravelActivity.this, "行程重命名失败", Toast.LENGTH_SHORT).show();
		}
	}

	int actAvgFuelPosition;
	float act_fuel;

	/** 录入实际油耗 **/
	private void actAvgFuel(int position, float act_avg_fuel) {
		dialog = ProgressDialog.show(TravelActivity.this, "提示", "实际油耗提交中");
		dialog.setCancelable(true);
		actAvgFuelPosition = position;
		act_fuel = act_avg_fuel;
		int trip_id = travelDatas.get(position).getTrip_id();
		String url = Constant.BaseUrl + "device/trip/" + trip_id + "?auth_code=" + app.auth_code;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("act_avg_fuel", String.valueOf(act_avg_fuel)));
		new NetThread.putDataThread(handler, url, params, actAvgFuel).start();
	}

	/** 解析录入实际油耗 **/
	private void jsonActAvgFuel(String result) {
		if (dialog != null) {
			dialog.dismiss();
		}
		try {
			JSONObject jsonObject = new JSONObject(result);
			if (jsonObject.getInt("status_code") == 0) {
				travelDatas.get(actAvgFuelPosition).setAct_avg_fuel(act_fuel);
				travelAdapter.notifyDataSetChanged();
			} else {
				Toast.makeText(TravelActivity.this, "实际油耗提交失败", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(TravelActivity.this, "实际油耗提交失败", Toast.LENGTH_SHORT).show();
		}
	}

	CollectionData collectionData;
	int collectPosition;
	String collectName = "";
	String collectAdres = "";
	String collectLon = "";
	String collectLat = "";

	/** 收藏地址 **/
	private void collectAdress(final int position) {
		// 判断该行程是否设置trip_name
		final TravelData travelData = travelDatas.get(position);
		String trip_name = travelData.getTrip_name();
		collectAdres = travelData.getEnd_place();
		collectLat = travelData.getEnd_lat();
		collectLon = travelData.getEnd_lon();
		if (trip_name == null || trip_name.equals("")) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(TravelActivity.this);
			LayoutInflater inflater = (LayoutInflater) TravelActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.builder_rename, null);
			dialog.setView(layout);
			final EditText et_rename = (EditText) layout.findViewById(R.id.et_rename);
			et_rename.setHint("请输入收藏的名称");
			dialog.setTitle("提示");
			dialog.setNegativeButton("取消", null);
			dialog.setPositiveButton("收藏", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String name = et_rename.getText().toString().trim();
					if (name.equals("")) {
						Toast.makeText(TravelActivity.this, "收藏的名称不能为空", Toast.LENGTH_SHORT).show();
					} else {
						collectPosition = position;
						collectName = name;
						// TODO 判断收藏的名称是否重复
						judgeCollect(name, 0);
					}
				}
			});
			dialog.show();
		} else {
			sureCollectAdress(trip_name, collectAdres, collectLon, collectLat);
			collectCommonAdress(collectName, collectAdres, Double.valueOf(collectLat), Double.valueOf(collectLon));
		}
	}

	/** 收藏地址 **/
	private void sureCollectAdress(String name, String adress, String lon, String lat) {
		dialog = ProgressDialog.show(TravelActivity.this, "提示", "地址收藏中");
		dialog.setCancelable(true);
		String url = Constant.BaseUrl + "favorite?auth_code=" + app.auth_code;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("cust_id", app.cust_id));
		params.add(new BasicNameValuePair("name", name));
		params.add(new BasicNameValuePair("address", adress));
		params.add(new BasicNameValuePair("tel", ""));
		params.add(new BasicNameValuePair("lon", lon));
		params.add(new BasicNameValuePair("lat", lat));
		new NetThread.postDataThread(handler, url, params, collectAdress).start();

		collectionData = new CollectionData();
		collectionData.setCust_id(app.cust_id);
		collectionData.setName(name);
		collectionData.setAddress(adress);
		collectionData.setTel("");
		collectionData.setLon(lon);
		collectionData.setLat(lat);
	}

	/** 解析收藏 **/
	private void jsonCollectAdress(String result) {
		if (dialog != null) {
			dialog.dismiss();
		}
		try {
			JSONObject jsonObject = new JSONObject(result);
			if (jsonObject.getString("status_code").equals("0")) {
				collectionData.setFavorite_id(jsonObject.getString("favorite_id"));
				collectionData.save();
			} else {

			}
		} catch (Exception e) {
			// handle exception
		}
	}

	/**
	 * 判断是否收藏 type = 0;判断新的收藏的名称是否重复 type = 1;判断所有的行程结果，确定是否收藏
	 * **/
	private void judgeCollect(String result, int type) {
		try {
			String url = Constant.BaseUrl + "favorite/is_collect?auth_code=" + app.auth_code + "&names=" + URLEncoder.encode(result, "UTF-8") + "&cust_id="
					+ app.cust_id;
			new Thread(new NetThread.GetDataThread(handler, url, getIsCollect, type)).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jsonCollect(String result, int type) {
		try {
			JSONArray jsonArray = new JSONArray(result);
			if (type == 1) {// 判断所有行程的是否收藏
				for (int i = 0; i < jsonArray.length(); i++) {
					String name = jsonArray.getJSONObject(i).getString("name");
					for (TravelData travelData : travelDatas) {
						if (travelData.getTrip_name().equals(name)) {
							travelData.setCollect(true);
							break;
						}
					}
				}
			} else {// 判断新的收藏名称是否重复
				if (jsonArray.length() == 0) {// 未重复,开始收藏
					sureCollectAdress(collectName, collectAdres, collectLon, collectLat);
					renameTravel(collectPosition, collectName, false);
					collectCommonAdress(collectName, collectAdres, Double.valueOf(collectLat), Double.valueOf(collectLon));
				} else {
					Toast.makeText(TravelActivity.this, "收藏的名称重复", Toast.LENGTH_SHORT).show();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	List<AdressData> adressDatas = new ArrayList<AdressData>();

	/** 收藏的同时，添加到常用地址 **/
	private void collectCommonAdress(String nameMark, String addressName, double latitude, double longitude) {
		getJsonData();
		SharedPreferences preferences2 = getSharedPreferences("address_add", Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor2 = preferences2.edit();
		JSONObject object = new JSONObject();
		JSONArray addJsonArray = new JSONArray();
		for (AdressData adress : adressDatas) {
			if (adress.getName().equals(nameMark) && adress.getAdress().equals(addressName)) {
				//如果名称和地址都相同则，不添加到常用地址
				return;
			}
		}
		try {
			for (AdressData adress : adressDatas) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("nameMark", adress.getName());
				jsonObject.put("addressName", adress.getAdress());
				jsonObject.put("addressLat", adress.getLat());
				jsonObject.put("addressLon", adress.getLon());
				addJsonArray.put(jsonObject);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		AdressData adressData = new AdressData();
		adressData.setName(nameMark);
		adressData.setAdress(addressName);
		adressData.setLat(latitude);
		adressData.setLon(longitude);
		adressDatas.add(adressData);
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("nameMark", nameMark);
			jsonObject.put("addressName", addressName);
			jsonObject.put("addressLat", latitude);
			jsonObject.put("addressLon", longitude);
			addJsonArray.put(jsonObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			object.put("addJsonArray", addJsonArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		editor2.putString("addJsonArray", object.toString());
		editor2.commit();
	}
	/**获取本地常用地址**/
	private void getJsonData() {
		SharedPreferences preferences = getSharedPreferences("address_add", Activity.MODE_PRIVATE);
		String addJsonArray = preferences.getString("addJsonArray", "");
		if (addJsonArray == null || addJsonArray.equals("")) {
			return;
		}
		try {
			JSONObject jsonObject = new JSONObject(addJsonArray);
			JSONArray jsonArray = jsonObject.getJSONArray("addJsonArray");
			for (int i = 0; i < jsonArray.length(); i++) {
				AdressData adressData = new AdressData();
				JSONObject object = jsonArray.getJSONObject(i);
				if(object.opt("nameMark") == null){
					adressData.setName("");
				}else{
					adressData.setName(object.getString("nameMark"));
				}
				String addressName = object.getString("addressName");
				double addressLat = object.getDouble("addressLat");
				double addressLon = object.getDouble("addressLon");
				adressData.setAdress(addressName);
				adressData.setLat(addressLat);
				adressData.setLon(addressLon);
				adressDatas.add(adressData);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void showMore(final int position) {
		final boolean isCollect = travelDatas.get(position).isCollect;
		String collect = "收藏终点";
		if (isCollect) {
			collect = "已收藏";
		}
		AlertDialog.Builder builder = new Builder(TravelActivity.this);
		builder.setTitle("更多");
		builder.setItems(new String[] { collect, "删除行程", "重命名", "实际油耗" }, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					if (!isCollect) {
						collectAdress(position);
					}
					break;
				case 1:
					deleteTravel(position);
					break;
				case 2:
					AlertDialog.Builder dialog1 = new AlertDialog.Builder(TravelActivity.this);
					LayoutInflater inflater = (LayoutInflater) TravelActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.builder_rename, null);
					dialog1.setView(layout);
					final EditText et_rename = (EditText) layout.findViewById(R.id.et_rename);
					String trip_name = travelDatas.get(position).getTrip_name();
					if (trip_name == null || trip_name.equals("")) {

					} else {
						et_rename.setText(trip_name);
					}
					dialog1.setTitle("提示");
					dialog1.setNegativeButton("取消", null);
					dialog1.setPositiveButton("更改", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String name = et_rename.getText().toString().trim();
							if (!name.equals("")) {
								renameTravel(position, name, true);
							}
						}
					});
					dialog1.show();
					break;
				case 3:
					AlertDialog.Builder dialog2 = new AlertDialog.Builder(TravelActivity.this);
					View view = (LayoutInflater.from(TravelActivity.this)).inflate(R.layout.item_travel_record, null);
					final EditText et_travel_record = (EditText) view.findViewById(R.id.et_travel_record);
					float avg_fuel = travelDatas.get(position).getAct_avg_fuel();
					if (avg_fuel != 0) {
						et_travel_record.setText(String.valueOf(avg_fuel));
					}
					dialog2.setTitle("行程油耗录入");
					dialog2.setView(view);
					dialog2.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String record = et_travel_record.getText().toString().trim();
							if (record.equals("")) {
								Toast.makeText(TravelActivity.this, "实际油耗不能为空", Toast.LENGTH_SHORT).show();
							} else {
								actAvgFuel(position, Float.valueOf(record));
							}
						}
					}).setNegativeButton("取消", null).show();
					break;
				}
			}
		});
		builder.setNegativeButton("确定", null);
		builder.show();
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
		public View getView(final int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_travel, null);
				holder = new ViewHolder();
				holder.tv_trip_name = (TextView) convertView.findViewById(R.id.tv_trip_name);
				holder.tv_item_travel_startTime = (TextView) convertView.findViewById(R.id.tv_item_travel_startTime);
				holder.tv_item_travel_stopTime = (TextView) convertView.findViewById(R.id.tv_item_travel_stopTime);
				holder.tv_item_travel_startPlace = (TextView) convertView.findViewById(R.id.tv_item_travel_startPlace);
				holder.tv_item_travel_stopPlace = (TextView) convertView.findViewById(R.id.tv_item_travel_stopPlace);
				holder.tv_item_travel_spacingDistance = (TextView) convertView.findViewById(R.id.tv_item_travel_spacingDistance);
				holder.tv_item_travel_averageOil = (TextView) convertView.findViewById(R.id.tv_item_travel_averageOil);
				holder.tv_item_travel_oil = (TextView) convertView.findViewById(R.id.tv_item_travel_oil);
				holder.tv_item_travel_speed = (TextView) convertView.findViewById(R.id.tv_item_travel_speed);
				holder.tv_item_travel_cost = (TextView) convertView.findViewById(R.id.tv_item_travel_cost);
				holder.iv_item_travel_map = (ImageView) convertView.findViewById(R.id.iv_item_travel_map);
				holder.iv_item_travel_share = (ImageView) convertView.findViewById(R.id.iv_item_travel_share);
				holder.iv_item_travel_recordShow = (ImageView) convertView.findViewById(R.id.iv_item_travel_recordShow);
				holder.iv_nav_start = (ImageView) convertView.findViewById(R.id.iv_nav_start);
				holder.iv_nav_stop = (ImageView) convertView.findViewById(R.id.iv_nav_stop);
				holder.iv_item_travel_more = (ImageView) convertView.findViewById(R.id.iv_item_travel_more);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final TravelData travelData = travelDatas.get(position);
			if (travelData.getAct_avg_fuel() == 0) {
				holder.iv_item_travel_recordShow.setVisibility(View.INVISIBLE);
			} else {
				holder.iv_item_travel_recordShow.setVisibility(View.VISIBLE);
			}
			if (travelData.getTrip_name() == null || travelData.getTrip_name().equals("")) {
				holder.tv_trip_name.setVisibility(View.GONE);
			} else {
				holder.tv_trip_name.setVisibility(View.VISIBLE);
				holder.tv_trip_name.setText(travelData.getTrip_name());
			}
			holder.tv_item_travel_startTime.setText(travelData.getStartTime().substring(10, 16));
			holder.tv_item_travel_stopTime.setText(travelData.getStopTime().substring(10, 16));
			holder.tv_item_travel_startPlace.setText("起点：" + travelData.getStart_place());
			holder.tv_item_travel_stopPlace.setText("终点：" + travelData.getEnd_place());
			holder.tv_item_travel_spacingDistance.setText("共" + travelData.getSpacingDistance() + "公里\\" + travelData.getSpacingTime());
			holder.tv_item_travel_averageOil.setText(travelData.getAverageOil());
			holder.tv_item_travel_oil.setText(travelData.getOil());
			holder.tv_item_travel_speed.setText(travelData.getSpeed());
			holder.tv_item_travel_cost.setText(travelData.getCost());
			holder.iv_item_travel_share.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					StringBuffer sb = new StringBuffer();
					sb.append("【行程】 ");
					sb.append(travelData.getStartTime().substring(5, 16));
					sb.append(" 从" + travelData.getStart_place());
					sb.append("到" + travelData.getEnd_place());
					sb.append("，共行驶" + travelData.getSpacingDistance());
					sb.append("公里，耗时" + travelData.getSpacingTime());
					sb.append("，" + travelData.getOil());
					sb.append("，" + travelData.getCost());
					sb.append("，" + travelData.getAverageOil());
					sb.append("，" + travelData.getSpeed());
					GetSystem.share(TravelActivity.this, sb.toString(), "", 0, 0, "行程", "");
				}
			});
			holder.iv_item_travel_map.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(TravelActivity.this, TravelMapActivity.class);
					intent.putExtra("StartTime", travelData.getStartTime());
					intent.putExtra("StopTime", travelData.getStopTime());
					intent.putExtra("Start_place", travelData.getStart_place());
					intent.putExtra("End_place", travelData.getEnd_place());
					intent.putExtra("SpacingDistance", travelData.getSpacingDistance());
					intent.putExtra("SpacingTime", travelData.getSpacingTime());
					intent.putExtra("AverageOil", travelData.getAverageOil());
					intent.putExtra("Oil", travelData.getOil());
					intent.putExtra("Speed", travelData.getSpeed());
					intent.putExtra("Cost", travelData.getCost());
					intent.putExtra("device_id", device_id);
					intent.putExtra("Lat", travelData.getStart_lat());
					intent.putExtra("Lon", travelData.getStart_lon());
					TravelActivity.this.startActivity(intent);
				}
			});
			holder.iv_item_travel_recordShow.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String content = "实际录入百公里油耗:" + travelData.getAct_avg_fuel() + "L";
					Toast.makeText(TravelActivity.this, content, Toast.LENGTH_SHORT).show();
				}
			});
			holder.iv_nav_start.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					LatLng pt1 = new LatLng(app.Lat, app.Lon);
					LatLng pt2 = new LatLng(Double.valueOf(travelData.getStart_lat()), Double.valueOf(travelData.getStart_lon()));
					GetSystem.FindCar(TravelActivity.this, pt1, pt2, "point", "point1");
				}
			});
			holder.iv_nav_stop.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					LatLng pt1 = new LatLng(app.Lat, app.Lon);
					LatLng pt2 = new LatLng(Double.valueOf(travelData.getEnd_lat()), Double.valueOf(travelData.getEnd_lon()));
					GetSystem.FindCar(TravelActivity.this, pt1, pt2, "point", "point1");
				}
			});
			holder.iv_item_travel_more.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showMore(position);
				}
			});
			return convertView;
		}

		private class ViewHolder {
			TextView tv_item_travel_startTime, tv_item_travel_stopTime, tv_item_travel_startPlace, tv_item_travel_stopPlace, tv_item_travel_spacingDistance,
					tv_item_travel_averageOil, tv_item_travel_oil, tv_item_travel_speed, tv_item_travel_cost, tv_trip_name;
			ImageView iv_item_travel_map, iv_item_travel_share, iv_item_travel_recordShow, iv_nav_start, iv_nav_stop, iv_item_travel_more;
		}
	}

	private class TravelData {
		int trip_id;
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
		String trip_name;
		float act_avg_fuel;
		boolean isCollect;

		public boolean isCollect() {
			return isCollect;
		}

		public void setCollect(boolean isCollect) {
			this.isCollect = isCollect;
		}

		public float getAct_avg_fuel() {
			return act_avg_fuel;
		}

		public void setAct_avg_fuel(float act_avg_fuel) {
			this.act_avg_fuel = act_avg_fuel;
		}

		public int getTrip_id() {
			return trip_id;
		}

		public void setTrip_id(int trip_id) {
			this.trip_id = trip_id;
		}

		public String getTrip_name() {
			return trip_name;
		}

		public void setTrip_name(String trip_name) {
			this.trip_name = trip_name;
		}

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
			try {
				if (!isDestory) {
					String strInfo = "";
					if (arg0 == null || arg0.error != SearchResult.ERRORNO.NO_ERROR) {
						
					} else {
						strInfo = arg0.getAddress();
						strInfo = strInfo.substring((strInfo.indexOf("市") + 1), strInfo.length());
					}
					if (isFrist) {// 起点位置取完，在取结束位置
						travelDatas.get(i).setStart_place(strInfo);
						isFrist = false;
						double lat = Double.valueOf(travelDatas.get(i).getEnd_lat());
						double lon = Double.valueOf(travelDatas.get(i).getEnd_lon());
						LatLng latLng = new LatLng(lat, lon);
						mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
						i++;
					} else {
						travelDatas.get(i - 1).setEnd_place(strInfo);
						if (travelDatas.size() == i) {

						} else {
							isFrist = true;
							double lat = Double.valueOf(travelDatas.get(i).getStart_lat());
							double lon = Double.valueOf(travelDatas.get(i).getStart_lon());
							LatLng latLng = new LatLng(lat, lon);
							mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
						}
					}
					travelAdapter.notifyDataSetChanged();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	boolean isDestory = false;

	@Override
	protected void onDestroy() {
		super.onDestroy();
		isDestory = true;
		mGeoCoder.destroy();
		mGeoCoder = null;
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
