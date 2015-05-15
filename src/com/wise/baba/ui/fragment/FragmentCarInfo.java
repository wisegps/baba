package com.wise.baba.ui.fragment;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.google.gson.Gson;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Const;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.biz.GetUrl;
import com.wise.baba.biz.HttpCarInfo;
import com.wise.baba.entity.ActiveGpsData;
import com.wise.baba.entity.CarData;
import com.wise.baba.entity.CarView;
import com.wise.baba.entity.GpsData;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.adapter.OnCardMenuListener;
import com.wise.baba.ui.widget.DialView;
import com.wise.baba.ui.widget.HScrollLayout;
import com.wise.baba.ui.widget.OnViewChangeListener;
import com.wise.car.CarLocationActivity;
import com.wise.car.DevicesAddActivity;
import com.wise.setting.LoginActivity;
import com.wise.setting.OilUpdateActivity;
import com.wise.state.DriveActivity;
import com.wise.state.FaultDetectionActivity;
import com.wise.state.FuelActivity;


/**
 * 车辆信息卡片
 * 
 * @author honesty
 **/
public class FragmentCarInfo extends Fragment {
	private static final String TAG = "FragmentCarInfo";
	/** 获取油耗信息 **/
	private static final int getData = 1;
	/** 获取限行 **/
	private static final int Get_carLimit = 7;
	/** 获取健康体检信息 **/
	private static final int get_health = 11;
	/** 获取驾驶指数 **/
	private static final int get_drive = 12;
	/** 获取gps信息 **/
	private static final int get_gps = 10;
	
	/** 获取Device信息 **/
	private static final int get_device = 13;

	HScrollLayout hs_car;
	AppApplication app;
	/** 当前车在列表中位置 **/
	public  int index = 0;
	/** 获取油耗数据开始时间 **/
	String startMonth;
	/** 获取油耗数据结束时间 **/
	String endMonth;
	/** 仪表盘的间距 **/
	int completed;
	private GeoCoder mGeoCoder = null;
	
	private final int Stealth_Mode_True= 1,Stealth_Mode_False = 0;//是否隐身 1：隐身  0：不隐身
	private HttpCarInfo http;
	
	private OnCardMenuListener onCardMenuListener;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_car_info, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		app = (AppApplication) getActivity().getApplication();

		
		String Month = GetSystem.GetNowMonth().getMonth();
		startMonth = Month + "-01";
		endMonth = GetSystem.getMonthLastDay(Month);

		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int twoCompleted = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 280, getResources().getDisplayMetrics());
		completed = (width - twoCompleted) / 3;

		mGeoCoder = GeoCoder.newInstance();
		mGeoCoder.setOnGetGeoCodeResultListener(listener);
		
		
		hs_car = (HScrollLayout) getActivity().findViewById(R.id.hs_car);
		initDataView();
		hs_car.setOnViewChangeListener(new OnViewChangeListener() {
			@Override
			public void OnViewChange(int view, int duration) {
				if(index != view){
					index = view;
					app.currentCarIndex = view;
					Log.i("FragmentCarInfo", "当前车辆"+app.currentCarIndex);
					//等待滚动完毕后查询数据
					handler.postDelayed(new Runnable() {						
						@Override
						public void run() {
							getTotalData();
						}
					}, duration);
				}
			}
		});
		
		http = new HttpCarInfo(this.getActivity(),handler);
	}

	public void initTotalData(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (isGetAllData) {
					SystemClock.sleep(5 * 60000);
					getTotalData();
				}
			}
		}).start();
	}
	public void initLoaction(){
		// 30秒定位，显示当前位子
				new Thread(new Runnable() {
					@Override
					public void run() {
						while (!isDestroy) {
							if (isResume) {
								if (app.carDatas == null || app.carDatas.size() == 0) {

								} else {
									// 防止删除车辆后数组越界
									if (index < app.carDatas.size()) {
										CarData carData = app.carDatas.get(index);
										String device_id = carData.getDevice_id();
										if (device_id == null || device_id.equals("")) {

										} else {
											// 获取gps信息
											String gpsUrl = GetUrl.getCarGpsData(device_id, app.auth_code);
											new NetThread.GetDataThread(handler, gpsUrl, get_gps, index).start();
										}
									} else {
										Log.d(TAG, "刷新位置数组越界");
									}
								}
							}
							SystemClock.sleep(30000);
						}
					}
				}).start();
		
	}
	

	public void setOnCardMenuListener(OnCardMenuListener onCardMenuListener) {
		this.onCardMenuListener = onCardMenuListener;
	}
	
	
	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.dialHealthScore:
				GetSystem.myLog(TAG, "dialHealthScore : app.carDatas.size() = " + app.carDatas.size());
				if (app.carDatas != null && app.carDatas.size() != 0) {
					String Device_id = app.carDatas.get(index).getDevice_id();
					if (Device_id == null || Device_id.equals("")) {
						Intent intent = new Intent(getActivity(), DevicesAddActivity.class);
						intent.putExtra("car_id", app.carDatas.get(index).getObj_id());
						intent.putExtra("car_series_id", app.carDatas.get(index).getCar_series_id());
						intent.putExtra("car_series", app.carDatas.get(index).getCar_series());
						startActivityForResult(intent, 2);
					} else {
						Intent intent = new Intent(getActivity(), FaultDetectionActivity.class);
						intent.putExtra("carDatas", (Serializable)app.carDatas);
						intent.putExtra("index", index);
						startActivityForResult(intent, 1);
					}
				}
				break;
			case R.id.dialDriveScore:
				if (app.carDatas != null && app.carDatas.size() != 0) {
					String Device_id = app.carDatas.get(index).getDevice_id();
					if (Device_id == null || Device_id.equals("")) {
						Intent intent = new Intent(getActivity(), DevicesAddActivity.class);
						intent.putExtra("car_id", app.carDatas.get(index).getObj_id());
						intent.putExtra("car_series_id", app.carDatas.get(index).getCar_series_id());
						intent.putExtra("car_series", app.carDatas.get(index).getCar_series());
						startActivityForResult(intent, 2);
					} else {
						Intent intent = new Intent(getActivity(), DriveActivity.class);
						intent.putExtra("carData", app.carDatas.get(index));
						ViewGroup carLayout = (ViewGroup) hs_car.getChildAt(index);
						Boolean is_online = (Boolean) carLayout.findViewById(R.id.imgLocation).getTag();
						intent.putExtra("is_online", is_online);
						startActivityForResult(intent, 2);
					}
				}
				break;
			case R.id.iv_update_oil:
				String device_id = app.carDatas.get(index).getDevice_id();
				if (device_id == null || device_id.equals("")) {
					Toast.makeText(getActivity(), "您的车没有绑定终端，不能进行油耗修正", Toast.LENGTH_SHORT).show();
				} else {
					Intent intent_oil = new Intent(getActivity(), OilUpdateActivity.class);
					intent_oil.putExtra("index", index);
					startActivity(intent_oil);
				}
				break;
			// 油耗，花费，里程分别显示
			case R.id.Liner_distance:
				getDataOne(DISTANCE);
				break;
			case R.id.Liner_fuel:
				getDataOne(FUEL);
				break;
			case R.id.Liner_fee:
				getDataOne(FEE);
				break;
			case R.id.ll_adress:
				Intent intent = new Intent(getActivity(), CarLocationActivity.class);
				intent.putExtra("index", index);
				intent.putExtra("isHotLocation", true);
				startActivity(intent);
				break;
			case R.id.imgStealth:
				if(v.getTag().equals(Stealth_Mode_True)){//当前隐身模式，切换到非隐身模式
					((ImageView)v).setImageResource(R.drawable.ico_key);
					v.setTag(Stealth_Mode_False);
					http.putStealthMode(Stealth_Mode_False);
				}else{
					((ImageView)v).setImageResource(R.drawable.ico_key_close);
					v.setTag(Stealth_Mode_True);
					http.putStealthMode(Stealth_Mode_True);
				}
				
				break;	
				
			case R.id.iv_drive_menu:
				if (onCardMenuListener != null) {
					onCardMenuListener.showCarMenu(Const.TAG_CAR);
				}
				break;
				
			}
		}
	};
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (isDestroy) {// 关闭后直接跳出
				return;
			}
			super.handleMessage(msg);
			switch (msg.what) {
			
			case get_device:
				jsonDevice(msg.obj.toString(),msg.arg1);
				break;
			case getData:
				jsonData(msg.obj.toString(), msg.arg1);
				break;
			case Get_carLimit:
				jsonCarLinit(msg.obj.toString(), msg.arg1);
				break;
			case get_gps:
				jsonGps(msg.obj.toString(), msg.arg1);
				break;
			case get_health:
				// 显示体检信息
				try {
					JSONObject jsonObject = new JSONObject(msg.obj.toString());
					// 健康指数
					int health_score = jsonObject.getInt("health_score");
					carViews.get(msg.arg1).getDialHealthScore().initValue(health_score,handler);
					carViews.get(msg.arg1).getTv_score().setText(String.valueOf(health_score));
					carViews.get(msg.arg1).getTv_title().setText("健康指数");
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				SharedPreferences preferences = getActivity().getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
				Editor editor = preferences.edit();
				editor.putString(Constant.sp_health_score + app.carDatas.get(msg.arg1).getObj_id(), msg.obj.toString());
				editor.commit();
				break;
			case get_drive:
				try {
					JSONObject jsonObject = new JSONObject(msg.obj.toString());
					int drive_score = jsonObject.getInt("drive_score");
					if (drive_score != 0 ) {
						carViews.get(msg.arg1).getDialDriveScore().initValue(drive_score,handler);
						carViews.get(msg.arg1).getTv_drive().setText(String.valueOf(drive_score));
						// 存在本地
						SharedPreferences preferences1 = getActivity().getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
						Editor editor1 = preferences1.edit();
						editor1.putString(Constant.sp_drive_score + app.carDatas.get(msg.arg1).getObj_id(), msg.obj.toString());
						editor1.commit();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

				break;
			}
		}
	};
	
	@SuppressLint("ResourceAsColor")
	public void jsonDevice(String json,int childIndex){
			try {
				Log.i("FragmentCarInfo", json);
				JSONObject jsonObject = new JSONObject(json);
				//SIM卡总流量，单位M
				Double total_traffic = jsonObject.getDouble("total_traffic");
				//SIM卡剩余流量，单位M
				Double remain_traffic = jsonObject.getDouble("remain_traffic");	
				//车辆状态 0: 静止  1：运行  2：设防  3：报警
				int device_flag = jsonObject.getInt("device_flag");

				//信号强度 0 离线(灰色)，1 差(蓝色1格)，2中(蓝色2格)，3优(蓝色3格)
				int signal_level = jsonObject.getInt("signal_level");
				//是否在线
				boolean is_online = jsonObject.getBoolean("is_online");
				//是否隐身 1：隐身  0：不隐身
				int stealthMode = jsonObject.getInt("stealth_mode"); 
				
				
				ViewGroup carLayout = (ViewGroup) hs_car.getChildAt(childIndex);
				
				
				ImageView imgLocation = (ImageView) carLayout.findViewById(R.id.imgLocation);
				TextView textAddress = (TextView) carLayout.findViewById(R.id.textLocation);
				
				TextView textSIM = (TextView) carLayout.findViewById(R.id.textSIM);
				ImageView imgState = (ImageView) carLayout.findViewById(R.id.imgState);
				ImageView imgSingal = (ImageView) carLayout.findViewById(R.id.imgSingal);
				ImageView imgStealth = (ImageView) carLayout.findViewById(R.id.imgStealth);
				
				String simValue = "SIM:  "+remain_traffic.intValue()+"M/"+total_traffic.intValue()+"M";
				textSIM.setText(simValue);
				
				int[] stateDrawable = {R.drawable.ico_state_0,R.drawable.ico_state_1,R.drawable.ico_state_2,R.drawable.ico_state_3};
				imgState.setImageResource(stateDrawable[device_flag]);
				
				int[] signalDrawable = {R.drawable.ico_wifi_0,R.drawable.ico_wifi_1,R.drawable.ico_wifi_2,R.drawable.ico_wifi_3};
				imgSingal.setImageResource(signalDrawable[signal_level]);
				
				
				if(is_online){
					imgLocation.setImageResource(R.drawable.ico_location_on);
					imgLocation.setTag(is_online);
					textAddress.setTextColor(Color.parseColor("#50b7de"));
					textAddress.setAlpha(0.6f);
					//imgOnLine.setImageResource(R.drawable.ico_key);
				}else{
					//imgOnLine.setImageResource(R.drawable.ico_key_close);
					imgLocation.setImageResource(R.drawable.ico_location_off);
					imgLocation.setTag(is_online);
					textAddress.setTextColor(Color.BLACK);
					textAddress.setAlpha(0.3f);
				}
				
				
				//隐身的
				if(stealthMode == Stealth_Mode_True){
					imgStealth.setImageResource(R.drawable.ico_key_close);
					imgStealth.setTag(stealthMode);
				}else{
					//在线的
					imgStealth.setImageResource(R.drawable.ico_key);
					imgStealth.setTag(Stealth_Mode_False);
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			//int drive_score = jsonObject.getInt("drive_score");
			
	}
	

	/** 滑动车辆布局 **/
	public void initDataView() {// 布局
		// 删除车辆后重新布局，如果删除的是最后一个车辆，则重置为第一个车
		if (index < app.carDatas.size()) {

		} else {
			index = 0;
		}
		SharedPreferences preferences = getActivity().getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		hs_car.removeAllViews();
		carViews.clear();
		for (int i = 0; i < app.carDatas.size(); i++) {
			View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_fault, null);
			
			hs_car.addView(v);
			
			ImageView imgSwitch = (ImageView) v.findViewById(R.id.imgStealth);
			imgSwitch.setOnClickListener(onClickListener);
			
			
			ImageView ivDriveMenu = (ImageView) getActivity().findViewById(
					R.id.iv_drive_menu);
			ivDriveMenu.setOnClickListener(onClickListener);
			
			ImageView iv_update_oil = (ImageView) v.findViewById(R.id.iv_update_oil);
			iv_update_oil.setOnClickListener(onClickListener);
			RelativeLayout rl_left_complete = (RelativeLayout) v.findViewById(R.id.rl_left_complete);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			lp.setMargins(0, 0, completed, 0);
			rl_left_complete.setLayoutParams(lp);

			LinearLayout ll_adress = (LinearLayout) v.findViewById(R.id.ll_adress);
			ll_adress.setOnClickListener(onClickListener);
			TextView tv_score = (TextView) v.findViewById(R.id.tv_score);
			TextView tv_title = (TextView) v.findViewById(R.id.tv_title);
			// 当前里程数
			TextView tv_current_distance = (TextView) v.findViewById(R.id.tv_current_distance);

			TextView tv_distance = (TextView) v.findViewById(R.id.tv_distance);
			TextView tv_fee = (TextView) v.findViewById(R.id.tv_fee);
			TextView tv_fuel = (TextView) v.findViewById(R.id.tv_fuel);
			TextView tv_name = (TextView) v.findViewById(R.id.tv_name);
			TextView textLocation = (TextView) v.findViewById(R.id.textLocation);

			v.findViewById(R.id.Liner_distance).setOnClickListener(onClickListener);
			v.findViewById(R.id.Liner_fuel).setOnClickListener(onClickListener);
			v.findViewById(R.id.Liner_fee).setOnClickListener(onClickListener);

			TextView tv_drive = (TextView) v.findViewById(R.id.tv_drive);
			DialView dialHealthScore = (DialView) v.findViewById(R.id.dialHealthScore);
			
			DialView dialDriveScore = (DialView) v.findViewById(R.id.dialDriveScore);
			
			dialHealthScore.setOnClickListener(onClickListener);
			dialDriveScore.setOnClickListener(onClickListener);
			CarView carView = new CarView();
			carView.setLl_adress(ll_adress);
			carView.setTv_distance(tv_distance);
			carView.setTv_fee(tv_fee);
			carView.setTv_fuel(tv_fuel);
			carView.setTv_score(tv_score);
			carView.setTv_title(tv_title);
			carView.setTv_location(textLocation);
			carView.setTv_drive(tv_drive);
			carView.setTv_current_distance(tv_current_distance);
			//carView.setImgHealthScore(dialHealthScore);
			carView.setDialHealthScore(dialHealthScore);
			carViews.add(carView);

			tv_name.setText(app.carDatas.get(i).getNick_name());
			String Device_id = app.carDatas.get(i).getDevice_id();
			if (Device_id == null || Device_id.equals("")) {
				
				dialHealthScore.initValue(100,handler);
				tv_score.setText("0");
				tv_title.setText("未绑定终端");
				dialDriveScore.initValue(100,handler);
				tv_drive.setText("0");
			} else {
				String result = preferences.getString(Constant.sp_health_score + app.carDatas.get(i).getObj_id(), "");
				if (result.equals("")) {// 未体检过
					dialHealthScore.initValue(100,handler);
					tv_score.setText("0");
					tv_title.setText("未体检过");
				} else {
					try {
						JSONObject jsonObject = new JSONObject(result);
						// 健康指数
						int health_score = jsonObject.getInt("health_score");
						dialHealthScore.initValue(health_score,handler);
						tv_score.setText(String.valueOf(health_score));
						tv_title.setText("健康指数");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				/** 驾驶信息 **/
				String drive = preferences.getString(Constant.sp_drive_score + app.carDatas.get(i).getObj_id(), "");
				if (drive.equals("")) {
					dialHealthScore.initValue(100,handler);
					tv_drive.setText("0");
				} else {
					try {
						JSONObject jsonObject = new JSONObject(drive);
						int drive_score = jsonObject.getInt("drive_score");
						dialHealthScore.initValue(drive_score,handler);
						tv_drive.setText(String.valueOf(drive_score));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		hs_car.snapToScreen(index);
		getTotalData();
	}

	
	/** 未登录显示 **/
	public void setLoginView() {
		hs_car.removeAllViews();
		View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_fault, null);
		hs_car.addView(v);
		RelativeLayout rl_left_complete = (RelativeLayout) v.findViewById(R.id.rl_left_complete);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(0, 0, completed, 0);
		rl_left_complete.setLayoutParams(lp);

		TextView tv_score = (TextView) v.findViewById(R.id.tv_score);
		TextView tv_title = (TextView) v.findViewById(R.id.tv_title);
		tv_score.setText("0");
		tv_title.setText("未体检过");
		
		
		DialView dialHealthScore = (DialView) v.findViewById(R.id.dialHealthScore);
		dialHealthScore.initValue(100,handler);
		
		dialHealthScore.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), LoginActivity.class));
			}
		});
		TextView tv_name = (TextView) v.findViewById(R.id.tv_name);
		tv_name.setText("绑定叭叭车载智能配件");
	}

	/** 获取当前车辆需要显示的所有数据 **/
	private void getTotalData() {
		try {
			if (app.carDatas == null || app.carDatas.size() == 0) {
				return;
			}
			// 防止删除车辆后数组越界
			if (index < app.carDatas.size()) {
				CarData carData = app.carDatas.get(index);
				String device_id = carData.getDevice_id();
				if (device_id == null || device_id.equals("")) {

				} else {
					String Gas_no = "";
					if (carData.getGas_no() == null || carData.getGas_no().equals("")) {
						Gas_no = "93#(92#)";
					} else {
						Gas_no = carData.getGas_no();
					}
					// 获取设备信息
					String deviceUrl = Constant.BaseUrl + "device/" + device_id + "?auth_code=" + app.auth_code + "&brand="
							+ URLEncoder.encode(carData.getCar_brand(), "UTF-8");
					new NetThread.GetDataThread(handler, deviceUrl, get_device, index).start();
					
									// 获取当前月的数据
					String url = Constant.BaseUrl + "device/" + device_id + "/total?auth_code=" + app.auth_code + "&start_day=" + startMonth + "&end_day="
							+ endMonth + "&city=" + URLEncoder.encode(app.City, "UTF-8") + "&gas_no=" + Gas_no;
					new NetThread.GetDataThread(handler, url, getData, index).start();
					// 获取gps信息
					String gpsUrl = GetUrl.getCarGpsData(device_id, app.auth_code);
					new NetThread.GetDataThread(handler, gpsUrl, get_gps, index).start();

					// 从服务器获取体检信息
					String url1 = Constant.BaseUrl + "device/" + device_id + "/health_exam?auth_code=" + app.auth_code + "&brand="
							+ URLEncoder.encode(carData.getCar_brand(), "UTF-8");
					
					System.out.println("从服务器获取体检信息 url1 "+url1);
					new NetThread.GetDataThread(handler, url1, get_health, index).start();
					// 获取驾驶信息
					String url2 = Constant.BaseUrl + "device/" + device_id + "/day_drive?auth_code=" + app.auth_code + "&day="
							+ GetSystem.GetNowMonth().getDay() + "&city=" + URLEncoder.encode(app.City, "UTF-8") + "&gas_no=" + Gas_no;
					System.out.println("从服务器获取体检信息 url2 "+url2);
					new NetThread.GetDataThread(handler, url2, get_drive, index).start();
				}
				// 获取限行信息
				if (app.City == null || carData.getObj_name() == null || app.City.equals("") || carData.getObj_name().equals("")) {

				} else {
					String url = Constant.BaseUrl + "base/ban?city=" + URLEncoder.encode(app.City, "UTF-8") + "&obj_name="
							+ URLEncoder.encode(carData.getObj_name(), "UTF-8");
					new NetThread.GetDataThread(handler, url, Get_carLimit, index).start();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 解析本月油价 **/
	private void jsonData(String str, int index) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			if (jsonObject.toString() == null || jsonObject.toString().equals("")) {
				return;
			}
			if (index < carViews.size()) {
				CarView carView = carViews.get(index);
				carView.getTv_fee().setText(String.format("%.1f", jsonObject.getDouble("total_fee")));// 花费
				carView.getTv_fuel().setText(String.format("%.1f", jsonObject.getDouble("total_fuel")));// 油耗
				// 剩余里程显示
				if ((jsonObject.getString("left_distance")).equals("null")) {
					carView.getTv_distance().setText(String.format("%.0f", 0.0));
				} else if (jsonObject.getDouble("left_distance") == 0) {
					carView.getTv_distance().setText(String.format("%.1f", jsonObject.getDouble("total_distance")));// 里程
				} else {
					carView.getTv_current_distance().setText("剩余里程");
					try {
						carView.getTv_distance().setText(String.format("%.1f", jsonObject.getDouble("left_distance")));// 里程
					} catch (Exception e) {
						carView.getTv_distance().setText(String.format("0"));// 里程
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 解析车辆限行 **/
	private void jsonCarLinit(String result, int index) {
		try {
			CarView carView = carViews.get(index);
			if (result == null || result.equals("")) {
				carView.getTv_xx().setText("不限");
				app.carDatas.get(index).setLimit("不限");
			} else {
				JSONObject jsonObject = new JSONObject(result);
				String limit = jsonObject.getString("limit");
				carView.getTv_xx().setText(limit);
				app.carDatas.get(index).setLimit(limit);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 获取GPS信息 **/
	private void jsonGps(String str, int index) {
		try {
			if (index < app.carDatas.size()) {
				Gson gson = new Gson();
				ActiveGpsData activeGpsData = gson.fromJson(str, ActiveGpsData.class);
				if(activeGpsData == null){
					return;
				}
				GpsData gpsData = activeGpsData.getActive_gps_data();
				if(gpsData != null){
					LatLng latLng = new LatLng(gpsData.getLat(), gpsData.getLon());
					app.carDatas.get(index).setLat(gpsData.getLat());
					app.carDatas.get(index).setLon(gpsData.getLon());
					app.carDatas.get(index).setRcv_time(GetSystem.ChangeTimeZone(gpsData.getRcv_time().substring(0, 19).replace("T", " ")));
					mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));	
				}
				if(activeGpsData.getParams() != null){
					app.carDatas.get(index).setSensitivity(activeGpsData.getParams().getSensitivity());	
				}		
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
		@Override
		public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			} else {
				try {
					String adress = result.getAddress();
					int startIndex = adress.indexOf("省") + 1;
					adress = adress.substring(startIndex, adress.length());
					app.carDatas.get(index).setAdress(adress);
					String rcv_time = app.carDatas.get(index).getRcv_time();
					String gpsData = rcv_time.substring(0, 10);// 取出日期
					String nowData = GetSystem.GetNowDay();
					String showTime = "";
					if (gpsData.equals(nowData)) {
						showTime = rcv_time.substring(11, 16);
					} else if (gpsData.equals(GetSystem.GetNextData(nowData, -1))) {
						showTime = "昨天" + rcv_time.substring(11, 16);
					} else if (gpsData.equals(GetSystem.GetNextData(nowData, -2))) {
						showTime = "前天" + rcv_time.substring(11, 16);
					} else {
						showTime = rcv_time.substring(5, 16);
					}
					// 显示时间
					carViews.get(index).getLl_adress().setVisibility(View.VISIBLE);
					carViews.get(index).getTv_location().setText(adress + "  " + showTime);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void onGetGeoCodeResult(GeoCodeResult arg0) {

		}
	};

	List<CarView> carViews = new ArrayList<CarView>();


	boolean isDestroy = false;
	boolean isGetAllData = true;
	boolean isResume = true;

	@Override
	public void onResume() {
		super.onResume();
		isResume = true;
		initLoaction();
		initTotalData();
		
	}

	@Override
	public void onPause() {
		super.onPause();
		isResume = false;
	}

	@Override
	public void onDestroy() {
//		for(int i =0;i<carViews.size();i++){
//			DialView dialScroe1 = carViews.get(i).getDialHealthScore();
//			dialScroe1.freeMemory();
//			dialScroe1 = null;
//			DialView dialDriveScore = carViews.get(i).getDialDriveScore();
//			dialDriveScore.freeMemory();
//			dialDriveScore = null;
//		}
//		carViews.clear();
//		carViews = null;
		super.onDestroy();
		isDestroy = true;
		isGetAllData = false;
		
		
	}

	// 跳转类型
	public static final int DISTANCE = 1;// 里程
	public static final int FEE = 2;// 费用
	public static final int FUEL = 3;// 油耗

	// 根据跳转类型进行（里程，花费，油耗）页面显示
	private void getDataOne(int type) {
		if (app.carDatas != null && app.carDatas.size() != 0) {
			String Device_id = app.carDatas.get(index).getDevice_id();
			if (Device_id == null || Device_id.equals("")) {
				Intent intent = new Intent(getActivity(), DevicesAddActivity.class);
				intent.putExtra("car_id", app.carDatas.get(index).getObj_id());
				intent.putExtra("car_series_id", app.carDatas.get(index).getCar_series_id());
				intent.putExtra("car_series", app.carDatas.get(index).getCar_series());
				startActivityForResult(intent, 2);
			} else {
				Intent intent = new Intent(getActivity(), FuelActivity.class);
				intent.putExtra("carData", app.carDatas.get(index));
				// 传递跳转类型常量进行跳转
				intent.putExtra("type", type);
				startActivity(intent);
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 3) {
			// 修改车辆信息
			initDataView();
			// if (app.carDatas.size() == 0) {
			// rl_ad = (RelativeLayout) getActivity().findViewById(R.id.rl_ad);
			// rl_ad.setVisibility(View.VISIBLE);
			// tv_content = (TextView)
			// getActivity().findViewById(R.id.tv_content);
			// tv_content.setOnClickListener(onClickListener);
			// ll_image = (LinearLayout)
			// getActivity().findViewById(R.id.ll_image);
			// hs_photo = (HScrollLayout)
			// getActivity().findViewById(R.id.hs_photo);
			// getAD();
			// hs_photo.setOnViewChangeListener(new OnViewChangeListener() {
			// @Override
			// public void OnViewChange(int view) {
			// image_position = view;
			// tv_content.setText(adDatas.get(view).getContent());
			// changeImage(view);
			// }
			//
			// @Override
			// public void OnLastView() {
			// }
			//
			// @Override
			// public void OnFinish(int index) {
			// }
			// });
			// } else {
			// if (rl_ad != null) {
			// rl_ad.setVisibility(View.GONE);
			// }
			// }
		} else if (requestCode == 1) {
			// requestCode = 1, resultCode = 2
			// 体检返回重新布局
			initDataView();
		} else if (requestCode == 2) {
			// requestCode = 2, resultCode = 0
			// 驾驶习惯返回
			/** 驾驶信息 **/
			SharedPreferences preferences = getActivity().getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
			String drive = preferences.getString(Constant.sp_drive_score + app.carDatas.get(index).getObj_id(), "");
			if (drive.equals("")) {

			} else {
				try {
					JSONObject jsonObject = new JSONObject(drive);
					int drive_score = jsonObject.getInt("drive_score");
					carViews.get(index).getDialDriveScore().initValue(drive_score,handler);
					carViews.get(index).getTv_drive().setText(String.valueOf(drive_score));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/** 返回当前车在列表中位置 **/
	public int getIndex() {
		return index;
	}
}
