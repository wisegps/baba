package com.wise.notice;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import pubclas.Constant;
import pubclas.JsonData;
import pubclas.NetThread;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

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
import com.wise.baba.AppApplication;
import com.wise.baba.R;

import data.CarData;

/**
 * 查看好友的位置信息
 * 
 * @author honesty
 **/
public class FriendLocationActivity extends Activity implements OnMarkerClickListener {
	private static final int getAllCarData = 1;
	private static final int getGpsData = 2;

	private BaiduMap mBaiduMap = null;
	private MapView mMapView = null;

	AppApplication app;
	/** 好友id **/
	private int friendId;
	/** 好友下的车辆信息 **/
	List<CarData> carDatas = new ArrayList<CarData>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_friend_location);
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
		new NetThread.GetDataThread(handler, url, getAllCarData).start();
	}

	private void jsonAllCarData(String result) {
		carDatas.addAll(JsonData.jsonCarInfo(result));
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
		System.out.println("showCarInMap");
		for (CarData carData : carDatas) {
			
			System.out.println("cardata");
			if (carData.getLat() != 0 || carData.getLon() != 0) {
				LatLng latLng = new LatLng(carData.getLat(), carData.getLon());
				// 构建Marker图标
				BitmapDescriptor bitmap = BitmapDescriptorFactory
						.fromResource(R.drawable.body_icon_location2);
				// 构建MarkerOption，用于在地图上添加Marker
				OverlayOptions option = new MarkerOptions().title(carData.getNick_name()).anchor(0.5f, 1.0f)
						.position(latLng).icon(bitmap);
				mBaiduMap.addOverlay(option);
				// 弹出框
//				View view = LayoutInflater.from(getApplicationContext())
//						.inflate(R.layout.item_map_popup, null);
//				TextView tv_adress = (TextView) view
//						.findViewById(R.id.tv_adress);
//				tv_adress.setText(carData.getNick_name());
//				InfoWindow mInfoWindow = new InfoWindow(view, latLng, -45);
//				mBaiduMap.showInfoWindow(mInfoWindow);
				System.out.println("showInfoWindow");
				if (isFrist) {// 第一次移动车的位置到地图中间
					isFrist = false;
					MapStatus mapStatus = new MapStatus.Builder()
							.target(latLng).build();
					MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
							.newMapStatus(mapStatus);
					mBaiduMap.setMapStatus(mapStatusUpdate);
				}
			}
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		
		View view = LayoutInflater.from(getApplicationContext())
				.inflate(R.layout.item_map_popup, null);
		TextView tv_adress = (TextView) view
				.findViewById(R.id.tv_adress);
		tv_adress.setText(marker.getTitle());
		InfoWindow mInfoWindow = new InfoWindow(view, marker.getPosition(), -45);
		mBaiduMap.showInfoWindow(mInfoWindow);
		return true;
	}
}