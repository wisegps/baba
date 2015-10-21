package com.wise.baba.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
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

import com.wise.baba.AirQualityIndexActivity;
import com.wise.baba.AirSettingActivity;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Const;
import com.wise.baba.app.Constant;
import com.wise.baba.app.Msg;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.biz.GetUrl;
import com.wise.baba.biz.HttpAir;
import com.wise.baba.biz.HttpCarInfo;
import com.wise.baba.biz.HttpGetObdData;
import com.wise.baba.biz.HttpWeather;
import com.wise.baba.entity.ActiveGpsData;
import com.wise.baba.entity.Air;
import com.wise.baba.entity.CarData;
import com.wise.baba.entity.GpsData;
import com.wise.baba.entity.Weather;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.adapter.OnCardMenuListener;
import com.wise.baba.ui.widget.HScrollLayout;
import com.wise.baba.ui.widget.OnViewChangeListener;
import com.wise.baba.ui.widget.SwitchImageView;
import com.wise.baba.util.ColorText;

/**
 * 空气质量
 * 
 * @author cyy
 **/
public class FragmentHomeAir extends Fragment {

	private static final String TAG = "FragmentHomeAir";

	private GeoCoder mGeoCoder = null;

	HScrollLayout hs_air;
	private TextView tvAirValue;
	AppApplication app;
	public int carIndex = 0;
	public int pageIndex = 0;
	public HttpGetObdData httpObd;
	public HttpAir httpAir;
	private HttpWeather httpWeather = null;
	private HttpCarInfo httpCarInfo;
	private OnCardMenuListener onCardMenuListener;
	private List<View> views = new ArrayList<View>();;
	public final static int POWER_ON = 1;
	public final static int POWER_OFF = 0;
	/** 获取gps信息 **/
	private static final int get_gps = 10;
	public RotateAnimation rolateAnimation = null;
	boolean isDestroy = false;
	boolean isResumed = false;
	public float dpdy = 0;// 电瓶电压
	private Handler uiHander = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i("FragmentHomeAir", "onCreateView");
		return inflater.inflate(R.layout.fragment_home_air, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		app = (AppApplication) getActivity().getApplication();
		uiHander = new Handler(handleCallBack);
		httpCarInfo = new HttpCarInfo(this.getActivity(), uiHander);
		httpObd = new HttpGetObdData(this.getActivity(), uiHander);
		httpAir = new HttpAir(this.getActivity(), uiHander);
		mGeoCoder = GeoCoder.newInstance();
		mGeoCoder.setOnGetGeoCodeResultListener(listener);
		httpWeather = new HttpWeather(this.getActivity(), uiHander);
		hs_air = (HScrollLayout) getActivity().findViewById(R.id.hs_air);
		initDataView();
		hs_air.setOnViewChangeListener(new OnViewChangeListener() {
			@Override
			public void OnViewChange(int view, int duration) {
				carIndex = (Integer) hs_air.getChildAt(view).getTag();
				Log.i("FragmentHomeAir", "OnViewChange: "+carIndex);
				if (view != pageIndex) {
					pageIndex = view;

					initLoaction(carIndex);

					httpObd.requestAir(carIndex);

					//httpWeather.requestWeather(app.City);
					// getWeather();

					// httpWeather.requestWeather(app.carDatas.get(carIndex).getCar_city());

				}
			}
		});

	}

	/**
	 * 获取室外天气信息 getWeather
	 */
	public void getWeather() {
		httpWeather.requestWeather();
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
		app.carDatas.get(carIndex).setLat(lat);
		app.carDatas.get(carIndex).setLon(lon);
		app.carDatas.get(carIndex).setRcv_time(rcv_time);
		mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
		app.carDatas.get(carIndex).setSensitivity(sensitivity);
	}

	OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
		@Override
		public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			} else {
				try {
					String adress = result.getAddress();
					int startIndex = adress.indexOf("省") + 1;
					int endIndex = adress.indexOf("市");
					adress = adress.substring(startIndex, endIndex);
					app.carDatas.get(carIndex).setCar_city(adress);

					httpWeather.requestWeather(adress);
					// Log.e("百度地图反解析"," " + carIndex + "--->" +
					// app.carDatas.get(carIndex).getCar_city());

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void onGetGeoCodeResult(GeoCodeResult arg0) {

		}
	};

	public void initLoaction(final int index) {
		// 30秒定位，显示当前位子
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (isResumed) {
					if (app.carDatas == null || app.carDatas.size() == 0) {
						continue;
					}
					// 防止删除车辆后数组越界
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
		}).start();
	}

	public void setOnCardMenuListener(OnCardMenuListener onCardMenuListener) {
		this.onCardMenuListener = onCardMenuListener;
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_air_menu:
				if (onCardMenuListener != null) {
					onCardMenuListener.showCarMenu(Const.TAG_AIR);
				}
				break;
			case R.id.iv_air_power:
				if (dpdy < 12) {
					String msg = getResources()
							.getString(R.string.air_low_dpdy);
					Toast.makeText(FragmentHomeAir.this.getActivity(), msg,
							Toast.LENGTH_SHORT).show();
					return;
				}

				SwitchImageView ivPower = (SwitchImageView) v;
				boolean isChecked = ivPower.isChecked();
				ivPower.setChecked(!isChecked);
				httpAir.setPower(app.carDatas.get(carIndex).getDevice_id(),
						!isChecked);
				Log.i("FragmentHomeAir", "点击电源: ");
				startAirAnimation(!isChecked);

				break;
			case R.id.iv_air_auto:
				SwitchImageView ivAuto = (SwitchImageView) v;
				ivAuto.setChecked(!ivAuto.isChecked());
				int mode = Const.AIR_MODE_MANUL;
				if (ivAuto.isChecked()) {
					mode = Const.AIR_MODE_SMART;
				}

				String deviceId = app.carDatas.get(carIndex).getDevice_id();
				httpAir.setMode(deviceId, mode, "", 0);
				break;
			case R.id.iv_air_level:
				SwitchImageView ivLevel = (SwitchImageView) v;
				ivLevel.setChecked(!ivLevel.isChecked());
				break;
			case R.id.iv_air_setting:
				Intent intent = new Intent();
				intent.setClass(FragmentHomeAir.this.getActivity(),
						AirSettingActivity.class);
				intent.putExtra("carIndex", carIndex);
				intent.putExtra("deviceId", app.carDatas.get(carIndex)
						.getDevice_id());
				FragmentHomeAir.this.getActivity().startActivity(intent);
				break;
			case R.id.flytAirDialView:
				intent = new Intent();
				intent.setClass(FragmentHomeAir.this.getActivity(),
						AirQualityIndexActivity.class);
				intent.putExtra("deviceId", app.carDatas.get(carIndex)
						.getDevice_id());
				FragmentHomeAir.this.startActivity(intent);
				break;
			}
		}
	};

	public void startAirAnimation(boolean isChecked) {
		if (rolateAnimation != null) {
			rolateAnimation.cancel();
			rolateAnimation = null;
		}
		if (isChecked == false) {
			return;
		}
		new Thread((new Runnable() {

			@Override
			public void run() {
				View imgCursor = views.get(pageIndex).findViewById(
						R.id.iv_page_air_circle_cursor);
				imgCursor.clearAnimation();

				rolateAnimation = new RotateAnimation(0, 360,
						Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				rolateAnimation.setInterpolator(new LinearInterpolator());
				rolateAnimation.setDuration(1500);
				rolateAnimation.setRepeatCount(Animation.INFINITE);
				imgCursor.startAnimation(rolateAnimation);

			}
		})).start();

	}

	@Override
	public void onPause() {
		isResumed = false;
		super.onPause();
		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		isDestroy = true;
	}

	@Override
	public void onStop() {
		super.onStop();

	}

	@Override
	public void onResume() {
		isResumed = true;
		super.onResume();
		refreshAir();
	}

	/** 滑动车辆布局 **/
	public void initDataView() {// 布局
		List<CarData> carDataList = app.carDatas;
		// 删除车辆后重新布局，如果删除的是最后一个车辆，则重置为第一个车
		if (carDataList == null || carDataList.size() == 0) {
			Log.i("FragmentHomeAir", "initDataView");
			return;
		}

		if (carIndex >= carDataList.size()) {
			
			carIndex = 0;
			pageIndex = 0;
		}
		hs_air.removeAllViews();
		views.clear();
		
		for (int i = 0; i < carDataList.size(); i++) {

			if (carDataList.get(i).isIfAir() == false) {
				Log.i("FragmentHomeAir", "isIfAir");
				Log.i("FragmentHomeAir", carDataList.get(i).getDevice_id());
				continue;
			}
			
			View v = LayoutInflater.from(getActivity()).inflate(
					R.layout.page_air, null);
			v.setTag(i);
			Log.i("FragmentHomeAir", "initDataView"+i);
			TextView tvCardTitle = (TextView) v
					.findViewById(R.id.tv_card_title);
			SwitchImageView ivAirSettting = (SwitchImageView) v
					.findViewById(R.id.iv_air_setting);
			SwitchImageView ivAirPower = (SwitchImageView) v
					.findViewById(R.id.iv_air_power);
			SwitchImageView ivAirAuto = (SwitchImageView) v
					.findViewById(R.id.iv_air_auto);
			SwitchImageView ivAirLevel = (SwitchImageView) v
					.findViewById(R.id.iv_air_level);

			View flytAirDialView = v.findViewById(R.id.flytAirDialView);

			ImageView ivAirMenu = (ImageView) v.findViewById(R.id.iv_air_menu);

			CarData carData = carDataList.get(i);

			tvCardTitle.setText(carData.getNick_name());

			ivAirSettting.setOnClickListener(onClickListener);
			ivAirPower.setOnClickListener(onClickListener);
			ivAirAuto.setOnClickListener(onClickListener);
			ivAirLevel.setOnClickListener(onClickListener);
			ivAirMenu.setOnClickListener(onClickListener);

			flytAirDialView.setOnClickListener(onClickListener);

			views.add(v);
			hs_air.addView(v);

		}
		//有空气净化设备
		if(views.size()>0){
			carIndex = (Integer) hs_air.getChildAt(pageIndex).getTag();
			hs_air.snapToScreen(pageIndex);
		}
		

	}

	/**
	 * 刷新数据 requestAir
	 */
	public void refreshAir() {

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (isResumed) {
					httpAir.requestAir(carIndex);
					Log.i("FragmentHomeAir", "carIndex: "+carIndex);
					SystemClock.sleep(30000);
					//SystemClock.sleep(10000);
				}
			}
		}).start();
	}

	/**
	 * 设置车外天气信息
	 * 
	 * @param weather
	 *            车外天气预报
	 */
	public void setWeather(Weather weather) {
		View view = views.get(pageIndex);
		TextView tvQuality = (TextView) view.findViewById(R.id.tvQuality);
		TextView tvCity = (TextView) view.findViewById(R.id.tvCity);
		TextView tvCityAQI = (TextView) view.findViewById(R.id.tvCityAQI);
		String quality = weather.getQuality();
		tvQuality.setText(ColorText.getAirQuality(quality));
		tvCity.setText(weather.getCity());
	}

	/**
	 * 设置空气质量信息
	 * 
	 * @param bundle
	 */
	public void initValue(Bundle bundle) {

		/*
		 * 空气质量指数
		 */
		String strDpdy = bundle.getString("dpdy");
		try {
			this.dpdy = Float.parseFloat(strDpdy);
		} catch (Exception e) {
			this.dpdy = 0;
		}

		Air mAir = new Air();
		int airValue = bundle.getInt("air");
		int airSwitch = bundle.getInt("switch");
		int airMode = bundle.getInt("air_mode");
		String airTime = bundle.getString("air_time");
		int airDuration = bundle.getInt("airDuration");
		mAir.setAir(airValue);
		mAir.setAirSwitch(airSwitch);
		mAir.setAirMode(airMode);
		mAir.setAirDuration(airDuration);
		mAir.setAirTime(airTime);

		refreshValue(mAir);
	}

	/**
	 * 刷新空气质量信息
	 * 
	 * @param bundle
	 */
	public void refreshValue(Air air) {
		
		Log.i("FragmentHomeAir", "refreshValue");
		View view = views.get(pageIndex);
		tvAirValue = (TextView) view.findViewById(R.id.tvAirscore);
		TextView tvAirDesc = (TextView) view.findViewById(R.id.tvAirDesc);
		TextView tvModeDesc = (TextView) view.findViewById(R.id.tv_mode_desc);

		SwitchImageView ivAirPower = (SwitchImageView) view
				.findViewById(R.id.iv_air_power);
		SwitchImageView ivAirAuto = (SwitchImageView) view
				.findViewById(R.id.iv_air_auto);
		SwitchImageView ivAirLevel = (SwitchImageView) view
				.findViewById(R.id.iv_air_level);
		SwitchImageView ivAirSetting = (SwitchImageView) view
				.findViewById(R.id.iv_air_setting);
		String desc = getAirDesc(air.getAir());
		tvAirDesc.setText(desc);
		tvAirValue.setText(air.getAir() + "");

		/*
		 * 开关控制
		 */
		int vSwitch = air.getAirSwitch();
		boolean isChecked = (vSwitch == POWER_ON) ? true : false;
		Log.i("FragmentHomeAir", "开关控制: " + isChecked);
		ivAirPower.setChecked(isChecked);
		String modeDesc = getModeDesc(air.getAirMode());
		tvModeDesc.setText(modeDesc);
		if (isChecked) {
			tvModeDesc.setVisibility(View.VISIBLE);
			if (rolateAnimation == null) {// 为空已经停止了，需要重新启动
				startAirAnimation(isChecked);
			}
		} else {
			tvModeDesc.setVisibility(View.INVISIBLE);
			if (rolateAnimation != null) {
				rolateAnimation.cancel();
				rolateAnimation = null;
			}
		}

		int vAirMode = air.airMode;
		isChecked = (vAirMode == Const.AIR_MODE_MANUL) ? false : true;
		ivAirAuto.setChecked(isChecked);
		ivAirSetting.setChecked(isChecked);

	}

	/**
	 * @param mode
	 * @return
	 */
	public String getModeDesc(int mode) {
		String desc = "自动模式";
		if (mode == Const.AIR_MODE_MANUL) {
			desc = "手动模式";
		} else if (mode == Const.AIR_MODE_SMART) {
			desc = "自动模式";
		} else if (mode == Const.AIR_MODE_TIMER) {
			desc = "定时模式";
		}
		return desc;
	}

	public Handler.Callback handleCallBack = new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			
			Log.i("FragmentHomeAir", "handleMessage" + isResumed);
			if (!isResumed || views.size() == 0) {
				return true;
			}
			switch (msg.what) {

			case Msg.Get_OBD_Data:
				initValue(msg.getData());
				break;
			case Msg.Get_Air_Value:
				refreshValue((Air) msg.obj);
				break;
			case Msg.Set_Air_Response:
				httpObd.requestAir(carIndex);
				break;
			case Msg.Get_Weather:
				setWeather((Weather) msg.obj);
				break;
			case Msg.Get_Car_GPS:
				setGps(msg.getData());
				break;

			}
			return true;
		}
	};

	public String getAirDesc(int air) {
		String air_desc = "优";
		if (air <= 1300) {
			air_desc = "优";
		} else if (air > 1300 && air <= 1500) {
			air_desc = "良";
		} else if (air > 1500 && air <= 2000) {
			air_desc = "中";
		} else {
			air_desc = "差";
		}

		return "车内空气" + air_desc;

	}

}
