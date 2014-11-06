package com.wise.notice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.model.LatLng;
import com.wise.baba.AppApplication;
import com.wise.baba.R;

public class MapFriendLocationActivity extends Activity {
	
	private BaiduMap mBaiduMap = null;
	private MapView mMapView = null;
	
	private AppApplication app;
	
	// 定位相关
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_map_friend_location);
		app = (AppApplication) getApplication();
		Intent intent = getIntent();
		String adress = intent.getStringExtra("adress");
		double latitude = intent.getDoubleExtra("latitude", 0);
		double longitude = intent.getDoubleExtra("longitude", 0);
		
		TextView tv_send = (TextView)findViewById(R.id.tv_send);
		tv_send.setOnClickListener(onClickListener);
		mMapView = (MapView) findViewById(R.id.mv_search_map);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(12));
		//TextView name = (TextView) findViewById(R.id.name);
		//name.setText(keyWord);
		//朋友位置
		BitmapDescriptor bdC = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_location);
		LatLng llF = new LatLng(latitude, longitude);
		OverlayOptions ooF = new MarkerOptions().position(llF).icon(bdC)
				.perspective(false).anchor(0.5f, 0.5f);
		mBaiduMap.addOverlay(ooF);
		//居中
		MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(llF);
		mBaiduMap.setMapStatus(mapStatusUpdate);
		//弹出框
		View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_map_popup, null);
		InfoWindow mInfoWindow = new InfoWindow(view, llF, 1);
		mBaiduMap.showInfoWindow(mInfoWindow);
		findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(30000);
		mLocClient.setLocOption(option);
		mLocClient.start();
	}
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.tv_send:
				break;
			}
		}
	};

	double latitude = 0;
	double longitude = 0;

	private class MyLocationListenner implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null || mMapView == null)
				return;
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(0).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
		            .fromResource(R.drawable.person);
		    MyLocationConfiguration config = new MyLocationConfiguration(null,
		            true, mCurrentMarker);
		    mBaiduMap.setMyLocationConfigeration(config);
		    latitude = location.getLatitude();
			longitude = location.getLongitude();
		}
	}
	
	@Override
	protected void onPause() {
		// MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		// MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()
		mMapView.onResume();
		super.onResume();
	}

	protected void onDestroy() {
		super.onDestroy();
		// 退出时销毁定位
		mLocClient.stop();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
	}
}
