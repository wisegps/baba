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
import android.view.View.OnLongClickListener;
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
import com.wise.baba.app.Msg;
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
	HScrollLayout hs_car;
	AppApplication app;
	/** 当前车在列表中位置 **/
	public int index = 0;
	/** 仪表盘的间距 **/
	int completed;
	private GeoCoder mGeoCoder = null;
	private final int Stealth_Mode_True = 1, Stealth_Mode_False = 0;// 是否隐身 1：隐身
	private HttpCarInfo httpCarInfo;
	private OnCardMenuListener onCardMenuListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_car_info, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		app = (AppApplication) getActivity().getApplication();
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int twoCompleted = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 280, getResources()
						.getDisplayMetrics());
		completed = (width - twoCompleted) / 3;
		mGeoCoder = GeoCoder.newInstance();
		mGeoCoder.setOnGetGeoCodeResultListener(listener);
		httpCarInfo = new HttpCarInfo(this.getActivity(), handler);
		hs_car = (HScrollLayout) getActivity().findViewById(R.id.hs_car);
		initDataView();
		hs_car.setOnViewChangeListener(new OnViewChangeListener() {
			@Override
			public void OnViewChange(int changedIndex, int duration) {
				if (index == changedIndex) {
					return;
				}
				index = changedIndex;
				app.currentCarIndex = changedIndex;
				Log.i("FragmentCarInfo", "当前车辆" + app.currentCarIndex);
				// 等待滚动完毕后查询数据
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						httpCarInfo.requestAllData();
					}
				}, duration);
			}
		});

	}

	/**
	 * 获取车辆信息 refreshAllData
	 */
	public void refreshAllData() {

		// 刷新车辆信息的线程
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				while (resumed) {
					SystemClock.sleep(5 * 60000);
					// SystemClock.sleep(5000);
					httpCarInfo.requestAllData();
					Log.i("ThreadTest", "refreshAllData");
				}
			}
		};
		Thread refreshThread = new Thread(runnable);
		refreshThread.start();
	}

	/**
	 * 开启更新位置线程 refreshLoaction
	 */
	public void refreshLoaction() {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				while (resumed) {
					if (app.carDatas == null || app.carDatas.size() == 0) {
						continue;
					}

					if (index >= app.carDatas.size()) {
						continue;
					}

					CarData carData = app.carDatas.get(index);
					String device_id = carData.getDevice_id();
					if (device_id == null || device_id.equals("")) {
						continue;
					}
					httpCarInfo.requestGps(device_id);
					SystemClock.sleep(30000);
				}

			}

		};
		// 30秒定位，显示当前位子
		new Thread(runnable).start();

	}

	public void setOnCardMenuListener(OnCardMenuListener onCardMenuListener) {
		this.onCardMenuListener = onCardMenuListener;
	}

	OnLongClickListener onLongClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {

			switch (v.getId()) {
			case R.id.imgSingal:

				// 信号强度 0 离线(灰色)，1 差(蓝色1格)，2中(蓝色2格)，3优(蓝色3格)

				int singal = (Integer) v.getTag();
				String tip = "";
				if (singal == 0) {
					tip = "终端已离线";
				} else if (singal == 1) {
					tip = "终端信号较差";
				} else if (singal == 2) {
					tip = "终端信号强度一般";
				} else if (singal == 3) {
					tip = "终端信号非常好";
				}

				Toast.makeText(getActivity(), tip, Toast.LENGTH_SHORT).show();
				break;
			case R.id.imgState:

				// 车辆状态 0: 静止 1：运行 2：设防 3：报警

				int state = (Integer) v.getTag();
				String strState = "";
				if (state == 0) {
					strState = "车辆已停止运行";
				} else if (state == 1) {
					strState = "车辆运行中";
				} else if (state == 2) {
					strState = "车辆设防状态";
				} else if (state == 3) {
					strState = "车辆报警状态";
				}

				Toast.makeText(getActivity(), strState, Toast.LENGTH_SHORT)
						.show();
				break;
			}
			return false;

		}
	};

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.dialHealthScore:
				if (app.carDatas != null && app.carDatas.size() != 0) {
					String Device_id = app.carDatas.get(index).getDevice_id();
					if (Device_id == null || Device_id.equals("")) {
						Intent intent = new Intent(getActivity(),
								DevicesAddActivity.class);
						intent.putExtra("car_id", app.carDatas.get(index)
								.getObj_id());
						intent.putExtra("car_series_id", app.carDatas
								.get(index).getCar_series_id());
						intent.putExtra("car_series", app.carDatas.get(index)
								.getCar_series());
						startActivityForResult(intent, 2);
					} else {
						Intent intent = new Intent(getActivity(),
								FaultDetectionActivity.class);
						intent.putExtra("carDatas", (Serializable) app.carDatas);
						intent.putExtra("index", index);
						startActivityForResult(intent, 1);
					}
				}
				break;
			case R.id.dialDriveScore:
				if (app.carDatas != null && app.carDatas.size() != 0) {
					String Device_id = app.carDatas.get(index).getDevice_id();
					if (Device_id == null || Device_id.equals("")) {
						Intent intent = new Intent(getActivity(),
								DevicesAddActivity.class);
						intent.putExtra("car_id", app.carDatas.get(index)
								.getObj_id());
						intent.putExtra("car_series_id", app.carDatas
								.get(index).getCar_series_id());
						intent.putExtra("car_series", app.carDatas.get(index)
								.getCar_series());
						startActivityForResult(intent, 2);
					} else {
						Intent intent = new Intent(getActivity(),
								DriveActivity.class);
						intent.putExtra("carData", app.carDatas.get(index));
						ViewGroup carLayout = (ViewGroup) hs_car
								.getChildAt(index);
						Boolean is_online = (Boolean) carLayout.findViewById(
								R.id.imgLocation).getTag();
						intent.putExtra("is_online", is_online);
						startActivityForResult(intent, 2);
					}
				}
				break;
			case R.id.iv_update_oil:
				String device_id = app.carDatas.get(index).getDevice_id();
				if (device_id == null || device_id.equals("")) {
					Toast.makeText(getActivity(), "您的车没有绑定终端，不能进行油耗修正",
							Toast.LENGTH_SHORT).show();
				} else {
					Intent intent_oil = new Intent(getActivity(),
							OilUpdateActivity.class);
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
				Intent intent = new Intent(getActivity(),
						CarLocationActivity.class);
				intent.putExtra("index", index);
				intent.putExtra("isHotLocation", true);
				startActivity(intent);
				break;
			case R.id.imgStealth:
				if (v.getTag().equals(Stealth_Mode_True)) {// 当前隐身模式，切换到非隐身模式
					((ImageView) v).setImageResource(R.drawable.ico_key);
					v.setTag(Stealth_Mode_False);
					httpCarInfo.putStealthMode(Stealth_Mode_False);
					Toast.makeText(getActivity(), "开启在线模式", Toast.LENGTH_SHORT)
							.show();
				} else {
					((ImageView) v).setImageResource(R.drawable.ico_key_close);
					v.setTag(Stealth_Mode_True);
					httpCarInfo.putStealthMode(Stealth_Mode_True);

					Toast.makeText(getActivity(), "开启隐身模式", Toast.LENGTH_SHORT)
							.show();
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
			if (!resumed || carViews.size() == 0) {// 关闭后直接跳出
				return;
			}
			super.handleMessage(msg);
			Bundle bundle = msg.getData();
			switch (msg.what) {
			case Msg.Get_Car_Device:
				setDevice(bundle);
				break;
			case Msg.Get_Car_Month_Data:
				setMonthData(bundle);
				break;
			case Msg.Get_Car_Limit:
				setCarLimit(bundle);
				break;
			case Msg.Get_Car_GPS:
				setGps(bundle);
				break;
			case Msg.Get_Car_Health:
				setCarHealth(bundle);
				break;
			case Msg.Get_Car_Drive:
				setCarDrive(bundle);
				break;
			}
		}
	};

	public void setDevice(Bundle bundle) {
		Log.i("FragmentCarInfo", "设置设备信息");
		// SIM卡总流量，单位M
		Double total_traffic = bundle.getDouble("total_traffic");
		// SIM卡剩余流量，单位M
		Double remain_traffic = bundle.getDouble("remain_traffic");

		// 车辆状态 0: 静止 1：运行 2：设防 3：报警
		int device_flag = bundle.getInt("device_flag");

		// 信号强度 0 离线(灰色)，1 差(蓝色1格)，2中(蓝色2格)，3优(蓝色3格)
		int signal_level = bundle.getInt("signal_level");
		// 是否在线
		boolean is_online = bundle.getBoolean("is_online");

		// 是否隐身 1：隐身 0：不隐身
		int stealthMode = bundle.getInt("stealth_mode");

		ViewGroup carLayout = (ViewGroup) hs_car
				.getChildAt(app.currentCarIndex);

		ImageView imgLocation = (ImageView) carLayout
				.findViewById(R.id.imgLocation);
		TextView textAddress = (TextView) carLayout
				.findViewById(R.id.textLocation);

		TextView textSIM = (TextView) carLayout.findViewById(R.id.textSIM);
		ImageView imgState = (ImageView) carLayout.findViewById(R.id.imgState);
		ImageView imgSingal = (ImageView) carLayout
				.findViewById(R.id.imgSingal);
		ImageView imgStealth = (ImageView) carLayout
				.findViewById(R.id.imgStealth);

		imgState.setOnLongClickListener(onLongClickListener);

		imgSingal.setOnLongClickListener(onLongClickListener);

		String simValue = "SIM:  " + remain_traffic.intValue() + "M/"
				+ total_traffic.intValue() + "M";
		textSIM.setText(simValue);

		int[] stateDrawable = { R.drawable.ico_state_0, R.drawable.ico_state_1,
				R.drawable.ico_state_2, R.drawable.ico_state_3 };
		imgState.setTag(device_flag);
		imgState.setImageResource(stateDrawable[device_flag]);

		int[] signalDrawable = { R.drawable.ico_wifi_0, R.drawable.ico_wifi_1,
				R.drawable.ico_wifi_2, R.drawable.ico_wifi_3 };
		imgSingal.setImageResource(signalDrawable[signal_level]);
		imgSingal.setTag(signal_level);

		if (is_online) {
			imgLocation.setImageResource(R.drawable.ico_location_on);
			imgLocation.setTag(is_online);
			textAddress.setTextColor(Color.parseColor("#50b7de"));
			textAddress.setAlpha(0.6f);
		} else {
			imgLocation.setImageResource(R.drawable.ico_location_off);
			imgLocation.setTag(is_online);
			textAddress.setTextColor(Color.BLACK);
			textAddress.setAlpha(0.3f);
		}

		// 隐身的
		if (stealthMode == Stealth_Mode_True) {
			imgStealth.setImageResource(R.drawable.ico_key_close);
			imgStealth.setTag(stealthMode);
		} else {
			// 在线的
			imgStealth.setImageResource(R.drawable.ico_key);
			imgStealth.setTag(Stealth_Mode_False);
		}

	}

	/** 滑动车辆布局 **/
	public void initDataView() {// 布局
		// 删除车辆后重新布局，如果删除的是最后一个车辆，则重置为第一个车

		if (app.carDatas == null || app.carDatas.size() == 0) {
			return;
		}
		if (index >= app.carDatas.size()) {
			index = 0;
		}

		SharedPreferences preferences = getActivity().getSharedPreferences(
				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		hs_car.removeAllViews();
		carViews.clear();
		for (int i = 0; i < app.carDatas.size(); i++) {
			View v = LayoutInflater.from(getActivity()).inflate(
					R.layout.item_fault, null);

			hs_car.addView(v);

			ImageView imgSwitch = (ImageView) v.findViewById(R.id.imgStealth);
			imgSwitch.setOnClickListener(onClickListener);

			ImageView ivDriveMenu = (ImageView) getActivity().findViewById(
					R.id.iv_drive_menu);
			ivDriveMenu.setOnClickListener(onClickListener);

			ImageView iv_update_oil = (ImageView) v
					.findViewById(R.id.iv_update_oil);
			iv_update_oil.setOnClickListener(onClickListener);
			RelativeLayout rl_left_complete = (RelativeLayout) v
					.findViewById(R.id.rl_left_complete);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			lp.setMargins(0, 0, completed, 0);
			rl_left_complete.setLayoutParams(lp);

			LinearLayout ll_adress = (LinearLayout) v
					.findViewById(R.id.ll_adress);
			ll_adress.setOnClickListener(onClickListener);
			TextView tv_score = (TextView) v.findViewById(R.id.tv_score);
			TextView tv_title = (TextView) v.findViewById(R.id.tv_title);
			// 当前里程数
			TextView tv_current_distance = (TextView) v
					.findViewById(R.id.tv_current_distance);

			TextView tv_distance = (TextView) v.findViewById(R.id.tv_distance);
			TextView tv_fee = (TextView) v.findViewById(R.id.tv_fee);
			TextView tv_fuel = (TextView) v.findViewById(R.id.tv_fuel);
			TextView tv_name = (TextView) v.findViewById(R.id.tv_name);
			TextView textLocation = (TextView) v
					.findViewById(R.id.textLocation);

			v.findViewById(R.id.Liner_distance).setOnClickListener(
					onClickListener);
			v.findViewById(R.id.Liner_fuel).setOnClickListener(onClickListener);
			v.findViewById(R.id.Liner_fee).setOnClickListener(onClickListener);

			TextView tv_drive = (TextView) v.findViewById(R.id.tv_drive);
			DialView dialHealthScore = (DialView) v
					.findViewById(R.id.dialHealthScore);

			DialView dialDriveScore = (DialView) v
					.findViewById(R.id.dialDriveScore);

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
			carView.setDialHealthScore(dialHealthScore);
			carView.setDialDriveScore(dialDriveScore);
			carViews.add(carView);

			tv_name.setText(app.carDatas.get(i).getNick_name());
			String Device_id = app.carDatas.get(i).getDevice_id();
			if (Device_id == null || Device_id.equals("")) {

				dialHealthScore.initValue(0, handler);
				tv_score.setText("0");
				tv_title.setText("未绑定终端");
				dialDriveScore.initValue(0, handler);
				tv_drive.setText("0");
			} else {
				String result = preferences.getString(Constant.sp_health_score
						+ app.carDatas.get(i).getObj_id(), "");
				if (result.equals("")) {// 未体检过
					dialHealthScore.initValue(0, handler);
					tv_score.setText("0");
					tv_title.setText("未体检过");
				} else {
					try {
						JSONObject jsonObject = new JSONObject(result);
						// 健康指数
						int health_score = jsonObject.getInt("health_score");
						dialHealthScore.initValue(health_score, handler);
						tv_score.setText(String.valueOf(health_score));
						tv_title.setText("健康指数");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				/** 驾驶信息 **/
				String drive = preferences.getString(Constant.sp_drive_score
						+ app.carDatas.get(i).getObj_id(), "");
				if (drive.equals("")) {
					dialHealthScore.initValue(0, handler);
					tv_drive.setText("0");
				} else {
					try {
						JSONObject jsonObject = new JSONObject(drive);
						int drive_score = jsonObject.getInt("drive_score");
						dialHealthScore.initValue(drive_score, handler);
						tv_drive.setText(String.valueOf(drive_score));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		hs_car.snapToScreen(index);
		httpCarInfo.requestAllData();
	}

	/** 未登录显示 **/
	public void setLoginView() {
		hs_car.removeAllViews();
		View v = LayoutInflater.from(getActivity()).inflate(
				R.layout.item_fault, null);
		hs_car.addView(v);
		RelativeLayout rl_left_complete = (RelativeLayout) v
				.findViewById(R.id.rl_left_complete);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(0, 0, completed, 0);
		rl_left_complete.setLayoutParams(lp);

		TextView tv_score = (TextView) v.findViewById(R.id.tv_score);
		TextView tv_title = (TextView) v.findViewById(R.id.tv_title);
		tv_score.setText("0");
		tv_title.setText("未体检过");

		DialView dialHealthScore = (DialView) v
				.findViewById(R.id.dialHealthScore);
		dialHealthScore.initValue(0, handler);

		dialHealthScore.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), LoginActivity.class));
			}
		});
		TextView tv_name = (TextView) v.findViewById(R.id.tv_name);
		tv_name.setText("绑定叭叭车载智能配件");
	}

	/** 设置本月油耗等信息 **/
	private void setMonthData(Bundle bundle) {

		if (bundle == null) {
			return;
		}
		if (index < carViews.size()) {
			CarView carView = carViews.get(index);
			String total_fee = bundle.getString("total_fee");
			carView.getTv_fee().setText(total_fee);// 花费
			String total_fuel = bundle.getString("total_fuel");
			carView.getTv_fuel().setText(total_fuel);// 油耗
			// 剩余里程显示
			String left_distance = bundle.getString("left_distance");
			carView.getTv_distance().setText(left_distance);
		}

	}

	/** 设置车辆限行 **/
	private void setCarLimit(Bundle bundle) {
		// CarView carView = carViews.get(index);
		String limit = bundle.getString("limit");
		app.carDatas.get(index).setLimit(limit);
	}

	/** 设置GPS信息 **/
	private void setGps(Bundle bundle) {
		if (bundle == null) {
			return;
		}
		Double lat = bundle.getDouble("lat");
		Double lon = bundle.getDouble("lon");
		String rcv_time = bundle.getString("rcv_time");
		int sensitivity = bundle.getInt("sensitivity", 0);
		LatLng latLng = new LatLng(lat, lon);
		app.carDatas.get(index).setLat(lat);
		app.carDatas.get(index).setLon(lon);
		app.carDatas.get(index).setRcv_time(rcv_time);
		mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
		app.carDatas.get(index).setSensitivity(sensitivity);
	}

	/** 设置体检信息 **/
	public void setCarHealth(Bundle bundle) {
		int health_score = bundle.getInt("health_score");

		carViews.get(index).getDialHealthScore()
				.initValue(health_score, handler);
		carViews.get(index).getTv_score().setText(String.valueOf(health_score));
		carViews.get(index).getTv_title().setText("健康指数");

	}

	/** 设置驾驶信息 **/
	public void setCarDrive(Bundle bundle) {
		int drive_score = bundle.getInt("drive_score");
		carViews.get(index).getDialDriveScore().initValue(drive_score, handler);
		carViews.get(index).getTv_drive().setText(String.valueOf(drive_score));

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
					} else if (gpsData.equals(GetSystem
							.GetNextData(nowData, -1))) {
						showTime = "昨天" + rcv_time.substring(11, 16);
					} else if (gpsData.equals(GetSystem
							.GetNextData(nowData, -2))) {
						showTime = "前天" + rcv_time.substring(11, 16);
					} else {
						showTime = rcv_time.substring(5, 16);
					}
					// 显示时间
					carViews.get(index).getLl_adress()
							.setVisibility(View.VISIBLE);
					carViews.get(index).getTv_location()
							.setText(adress + "  " + showTime);
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

	boolean resumed = false;

	@Override
	public void onResume() {
		super.onResume();
		resumed = true;
		// 更新车辆位置
		refreshLoaction();
		// 刷新车辆信息
		refreshAllData();

	}

	@Override
	public void onPause() {
		super.onPause();
		resumed = false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
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
				Intent intent = new Intent(getActivity(),
						DevicesAddActivity.class);
				intent.putExtra("car_id", app.carDatas.get(index).getObj_id());
				intent.putExtra("car_series_id", app.carDatas.get(index)
						.getCar_series_id());
				intent.putExtra("car_series", app.carDatas.get(index)
						.getCar_series());
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
		} else if (requestCode == 1) {
			// requestCode = 1, resultCode = 2
			// 体检返回重新布局
			initDataView();
		} else if (requestCode == 2) {
			// requestCode = 2, resultCode = 0
			// 驾驶习惯返回
			/** 驾驶信息 **/
			SharedPreferences preferences = getActivity().getSharedPreferences(
					Constant.sharedPreferencesName, Context.MODE_PRIVATE);
			String drive = preferences.getString(Constant.sp_drive_score
					+ app.carDatas.get(index).getObj_id(), "");
			if (drive.equals("")) {

			} else {
				try {
					JSONObject jsonObject = new JSONObject(drive);
					int drive_score = jsonObject.getInt("drive_score");
					carViews.get(index).getDialDriveScore()
							.initValue(drive_score, handler);
					carViews.get(index).getTv_drive()
							.setText(String.valueOf(drive_score));
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
