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
import com.wise.baba.biz.HttpGetObdData;
import com.wise.baba.biz.HttpWeather;
import com.wise.baba.entity.ActiveGpsData;
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
	public HttpGetObdData http;
	public HttpAir httpAir;
	private HttpWeather httpWeather = null;

	private OnCardMenuListener onCardMenuListener;
	private List<View> views = new ArrayList<View>();

	public final static int POWER_ON = 1;
	public final static int POWER_OFF = 0;
	/** 获取gps信息 **/
	private static final int get_gps = 10;
	
	public final static int MODE_AUTO = 1;
	public final static int MODE_MAN = 0;

	public RotateAnimation rolateAnimation = new RotateAnimation(0, 360,
			Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

	public int PageStatus = 0;// 页面出现0，页面销毁1
	
	boolean isDestroy = false;
	boolean isResume = false;
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		isDestroy = false;
		isResume = true;
		
		return inflater.inflate(R.layout.fragment_home_air, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		app = (AppApplication) getActivity().getApplication();
		hs_air = (HScrollLayout) getActivity().findViewById(R.id.hs_air);

		http = new HttpGetObdData(this.getActivity(), handler);
		httpAir = new HttpAir(this.getActivity(), handler);
		
		
		mGeoCoder = GeoCoder.newInstance();
		mGeoCoder.setOnGetGeoCodeResultListener(listener);
		
		
		
		httpWeather = new HttpWeather(this.getActivity(), handler);
		
		initDataView();
		
		
		hs_air.setOnViewChangeListener(new OnViewChangeListener() {
			@Override
			public void OnViewChange(int view, int duration) {
				carIndex = (Integer) hs_air.getChildAt(view).getTag();
				
				if (view != pageIndex) {	
					pageIndex = view;

					initLoaction(carIndex);
					
					http.requestAir(carIndex);
					
					
					Log.e("百度地图反解析"," " + carIndex + "--->" + app.carDatas.get(carIndex).getCar_city());
					
					
//					httpWeather.requestWeather(app.carDatas.get(carIndex).getCar_city());
					
					
				}
			}
		});
	}
	

	/** 获取GPS信息 **/
	private void jsonGps(String str, int index) {
		try {
			if (index < app.carDatas.size()) {
				Gson gson = new Gson();
				ActiveGpsData activeGpsData = gson.fromJson(str,
						ActiveGpsData.class);
				if (activeGpsData == null) {
					return;
				}
				GpsData gpsData = activeGpsData.getActive_gps_data();
				if (gpsData != null) {
					LatLng latLng = new LatLng(gpsData.getLat(),
							gpsData.getLon());
					app.carDatas.get(index).setLat(gpsData.getLat());
					app.carDatas.get(index).setLon(gpsData.getLon());
					app.carDatas.get(index).setRcv_time(
							GetSystem.ChangeTimeZone(gpsData.getRcv_time()
									.substring(0, 19).replace("T", " ")));
					mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption()
							.location(latLng));
				}
				if (activeGpsData.getParams() != null) {
					app.carDatas.get(index).setSensitivity(
							activeGpsData.getParams().getSensitivity());
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
					int endIndex = adress.indexOf("市");
					adress = adress.substring(startIndex, endIndex);				
					app.carDatas.get(carIndex).setCar_city(adress);
					
					httpWeather.requestWeather(adress);			
//					Log.e("百度地图反解析"," " + carIndex + "--->" + app.carDatas.get(carIndex).getCar_city());
	
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
									String gpsUrl = GetUrl.getCarGpsData(
											device_id, app.auth_code);
									new NetThread.GetDataThread(handler,
											gpsUrl, get_gps, index).start();
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
			case R.id.iv_air_menu:
				if (onCardMenuListener != null) {
					onCardMenuListener.showCarMenu(Const.TAG_AIR);
				}
				break;
			case R.id.iv_air_power:
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
				break;
			case R.id.iv_air_level:
				SwitchImageView ivLevel = (SwitchImageView) v;
				ivLevel.setChecked(!ivLevel.isChecked());
				break;
			case R.id.iv_air_setting:
				Intent intent = new Intent();
				intent.setClass(FragmentHomeAir.this.getActivity(),
						AirSettingActivity.class);
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
		rolateAnimation.cancel();

		if (isChecked == false) {
			return;
		}

		new Handler().post(new Runnable() {
			@Override
			public void run() {
				View imgCursor = views.get(pageIndex).findViewById(
						R.id.iv_page_air_circle_cursor);
				imgCursor.clearAnimation();
				rolateAnimation.setInterpolator(new LinearInterpolator());
				rolateAnimation.setDuration(1500);
				rolateAnimation.setRepeatCount(Animation.INFINITE);
				imgCursor.startAnimation(rolateAnimation);
			}
		});

	}
	
	
	
	




	@Override
	public void onPause() {
		super.onPause();
		isResume = false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		isDestroy = true;
	}
	

	@Override
	public void onStop() {
		super.onStop();
		PageStatus = 1;
	}

	@Override
	public void onResume() {
		super.onResume();
		isResume = true;
		PageStatus = 0;
		refreshAir();
	}

	/** 滑动车辆布局 **/
	public void initDataView() {// 布局
		// 删除车辆后重新布局，如果删除的是最后一个车辆，则重置为第一个车
		if (carIndex < app.carDatas.size()) {
		} else {
			carIndex = 0;
			pageIndex = 0;
		}
		hs_air.removeAllViews();

		List<CarData> carDataList = app.carDatas;
		for (int i = 0; i < carDataList.size(); i++) {

			if (carDataList.get(i).isIfAir() == false) {
				continue;
			}
			View v = LayoutInflater.from(getActivity()).inflate(
					R.layout.page_air, null);
			v.setTag(i);
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

		carIndex = (Integer) hs_air.getChildAt(pageIndex).getTag();
		hs_air.snapToScreen(pageIndex);

		http.requestAir(carIndex);

		httpWeather.requestWeather(app.City);

	}

	/**
	 * 刷新数据 requestAir
	 */
	public void refreshAir() {

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (PageStatus == 0) {
					refreshAir();
				}
			}
		}, 30000);
		httpAir.requestAir(carIndex);
	}

	/**
	 * 设置车外天气信息
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
		View view = views.get(pageIndex);
		tvAirValue = (TextView) view.findViewById(R.id.tvAirscore);
		TextView tvAirDesc = (TextView) view.findViewById(R.id.tvAirDesc);

		SwitchImageView ivAirPower = (SwitchImageView) view
				.findViewById(R.id.iv_air_power);
		SwitchImageView ivAirAuto = (SwitchImageView) view
				.findViewById(R.id.iv_air_auto);
		SwitchImageView ivAirLevel = (SwitchImageView) view
				.findViewById(R.id.iv_air_level);

		/*
		 * 空气质量指数
		 */
		int air = bundle.getInt("air");
		String desc = getAirDesc(air);
		tvAirDesc.setText(desc);
		tvAirValue.setText(air + "");

		/*
		 * 开关控制
		 */
		int vSwitch = bundle.getInt("switch");
		boolean isChecked = (vSwitch == POWER_ON) ? true : false;
		Log.i("FragmentHomeAir", "开关控制: " + isChecked);
		ivAirPower.setChecked(isChecked);

	}

	/**
	 * 只刷新空气质量数值
	 * 
	 * @param value
	 *            空气指数
	 */
	public void refreshValue(int value) {
		if (tvAirValue != null) {

			Log.i("FragmentHomeAir", "refreshValue: " + value);
			tvAirValue.setText(value + "");
		}

	}

	public Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Msg.Get_OBD_Data:
				initValue(msg.getData());
				break;
			case Msg.Get_Air_Value:
				refreshValue((Integer) msg.obj);
				break;
			case Msg.Set_Air_Response:
				http.requestAir(carIndex);
				break;
			case Msg.Get_Weather:
				setWeather((Weather) msg.obj);
				break;
			case get_gps:
				jsonGps(msg.obj.toString(), msg.arg1);
				break;
				
				
			}
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
