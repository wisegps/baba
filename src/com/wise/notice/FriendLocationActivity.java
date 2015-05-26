package com.wise.notice;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.biz.JsonData;
import com.wise.baba.entity.CarData;
import com.wise.baba.net.NetThread;
import com.wise.car.CarLocationActivity;
import com.wise.car.TravelActivity;

/**
 * 查看好友的位置信息
 * 
 * @author honesty
 **/
public class FriendLocationActivity extends Activity implements
		OnMarkerClickListener {
	private static final int getAllCarData = 1;
	private static final int getGpsData = 2;

	private BaiduMap mBaiduMap = null;
	private MapView mMapView = null;

	AppApplication app;
	/** 好友id **/
	private int friendId;
	private LatLng carLatlng;// 点击marker 获取 该车辆位置
	private LocationClient locatinClient;
	/** 好友下的车辆信息 **/
	List<CarData> carDatas = new ArrayList<CarData>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_friend_location);

		findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FriendLocationActivity.this.finish();
			}
		});
		app = (AppApplication) getApplication();
		mMapView = (MapView) findViewById(R.id.mv_friend);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(16));
		mBaiduMap.setOnMarkerClickListener(this);
		friendId = getIntent().getIntExtra("FriendId", 0);
		getAllCarData();
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case getAllCarData:
				jsonAllCarData(msg.obj.toString());
				break;

			case getGpsData:
				jsonGpsData(msg.obj.toString(), msg.arg1);
				break;
			}
		}
	};

	/**
	 * 获取好友车辆数据
	 */
	private void getAllCarData() {
		String url = Constant.BaseUrl + "customer/" + friendId
				+ "/vehicle?auth_code=" + app.auth_code;
		Log.i("FriendLocationActivity", url);
		new NetThread.GetDataThread(handler, url, getAllCarData).start();
	}

	private void jsonAllCarData(String result) {
		carDatas.addAll(JsonData.jsonCarInfo(result));
		Log.i("FriendLocationActivity", "这个朋友拥有" + carDatas.size() + "辆车");
		getGpsData();
	}

	/** 获取车辆定位信息 **/
	private void getGpsData() {
		for (int i = 0; i < carDatas.size(); i++) {
			String deviceId = carDatas.get(i).getDevice_id();
			if (deviceId == null || deviceId.equals("")) {

			} else {
				String gpsUrl = Constant.BaseUrl + "device/" + deviceId
						+ "?auth_code=" + app.auth_code
						+ "&update_time=2014-01-01%2019:06:43";
				new NetThread.GetDataThread(handler, gpsUrl, getGpsData, i)
						.start();
			}

		}
	}

	/** 解析位置信息 **/
	private void jsonGpsData(String result, int position) {
		try {
			JSONObject jsonObject = new JSONObject(result)
					.getJSONObject("active_gps_data");
			double lat = jsonObject.getDouble("lat");
			double lon = jsonObject.getDouble("lon");
			String rcv_time = jsonObject.getString("rcv_time");
			carDatas.get(position).setLat(lat);
			carDatas.get(position).setLon(lon);
			carDatas.get(position).setRcv_time(rcv_time);
			showCarInMap();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	boolean isFrist = true;

	/** 在地图上显示所有车辆信息 **/
	private void showCarInMap() {

		 final List<OverlayOptions> overlayOptions = new ArrayList<OverlayOptions>();
		OverlayManager overlayManager = new OverlayManager(mBaiduMap) {
			@Override
			public boolean onMarkerClick(Marker marker) {
				return true;
			}
			@Override
			public List<OverlayOptions> getOverlayOptions() {
				return overlayOptions;
			}
		};

		for (CarData carData : carDatas) {
			Log.i("FriendLocationActivity", "在地图上显示车辆");
			if (carData.getLat() != 0 || carData.getLon() != 0) {
				LatLng latLng = new LatLng(carData.getLat(), carData.getLon());
				// 构建Marker图标
				BitmapDescriptor bitmap = BitmapDescriptorFactory
						.fromResource(R.drawable.body_icon_location2);
				// 构建MarkerOption，用于在地图上添加Marker
				Bundle bundle = new Bundle();
				bundle.putString("device_id", carData.getDevice_id());
				bundle.putString("Gas_no", carData.getGas_no());
				OverlayOptions option = new MarkerOptions().extraInfo(bundle)
						.title(carData.getNick_name()).anchor(0.5f, 1.0f)
						.position(latLng).icon(bitmap);
				overlayOptions.add(option);
			}
		}
		overlayManager.addToMap();
		overlayManager.zoomToSpan();

	}

	@Override
	public boolean onMarkerClick(Marker marker) {

		View view = LayoutInflater.from(getApplicationContext()).inflate(
				R.layout.item_map_popup, null);
		TextView tv_adress = (TextView) view.findViewById(R.id.tv_adress);

		TextView tv_navi = (TextView) view.findViewById(R.id.tv_navi);
		TextView tv_travel = (TextView) view.findViewById(R.id.tv_travel);

		Bundle bundle = marker.getExtraInfo();
		final String device_id = bundle.getString("device_id");
		final String Gas_no = bundle.getString("Gas_no");
		carLatlng = marker.getPosition();
		InfoWindow mInfoWindow = new InfoWindow(view, marker.getPosition(), -45);
		tv_navi.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mBaiduMap.hideInfoWindow();
				openLocation();
			}
		});

		tv_travel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mBaiduMap.hideInfoWindow();
				Intent i = new Intent(FriendLocationActivity.this,
						TravelActivity.class);
				i.putExtra("device_id", device_id);
				i.putExtra("Gas_no", Gas_no);
				startActivity(i);
			}
		});
		tv_adress.setText(marker.getTitle());

		mBaiduMap.showInfoWindow(mInfoWindow);
		return true;
	}

	/**
	 * 开启定位服务
	 */
	public void openLocation() {
		locatinClient = new LocationClient(this);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setCoorType("BD09_MC");// 返回的定位结果是百度经纬度,默认值gcj02
		option.setScanSpan(100);// 设置发起定位请求的间隔时间为ms
		locatinClient.setLocOption(option);
		locatinClient.registerLocationListener(new LocationListener());
		locatinClient.start();

	}

	class LocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation bdLocation) {
			LatLng dirLocation = new LatLng(bdLocation.getLatitude(),
					bdLocation.getLongitude());
			locatinClient.stop();
			GetSystem.FindCar(FriendLocationActivity.this, carLatlng,
					dirLocation, "", "");
		}
	}

}