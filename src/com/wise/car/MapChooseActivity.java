package com.wise.car;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.wise.baba.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MapChooseActivity extends Activity {
	private MapView mMapView = null;
	private BaiduMap mBaiduMap = null;
	private GeoCoder mGeoCoder = null;

	// 定位相关
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_map_choose);
		mGeoCoder = GeoCoder.newInstance();
		mGeoCoder.setOnGetGeoCodeResultListener(onCoderResultListener);

		mMapView = (MapView) findViewById(R.id.choose_map);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(12));
		mBaiduMap.setOnMapClickListener(listener);
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(1000);
		mLocClient.setLocOption(option);
		mLocClient.start();

		findViewById(R.id.iv_back_choose).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						switch (v.getId()) {
						case R.id.iv_back_choose:
							finish();
							break;
						}
					}
				});
	}

	boolean isFirstLoc = true;

	private class MyLocationListenner implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null || mMapView == null)
				return;
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			if (isFirstLoc) {
				isFirstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
			}
		}
	}

	String name = "";
	public static final int MAPPOINT = 1;
	OnMapClickListener listener = new OnMapClickListener() {
		@Override
		/** 
		 * 地图单击事件回调函数 
		 * @param point 点击的地理坐标 
		 */
		public void onMapClick(final LatLng point) {
			mGeoCoder
					.reverseGeoCode(new ReverseGeoCodeOption().location(point));
			Button button = new Button(getApplicationContext());
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent();
					i.putExtra("name", name);
					i.putExtra("latitude", point.latitude);
					i.putExtra("longitude", point.longitude);
					setResult(MAPPOINT, i);
					finish();
				}
			});
			button.setBackgroundResource(R.drawable.popup);
			button.setText("点击选择地址");
			button.setTextColor(0xff333333);
			InfoWindow infoWindow = new InfoWindow(button, point, 0);
			mBaiduMap.showInfoWindow(infoWindow);
		}

		@Override
		/** 
		 * 地图内 Poi 单击事件回调函数 
		 * @param poi 点击的 poi 信息 
		 */
		public boolean onMapPoiClick(MapPoi poi) {
			return true;
		}
	};

	// 根据坐标点返回地方名
	OnGetGeoCoderResultListener onCoderResultListener = new OnGetGeoCoderResultListener() {
		@Override
		public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {

			} else {
				name = result.getAddress();
			}

		}

		@Override
		public void onGetGeoCodeResult(GeoCodeResult result) {
		}
	};

	protected void onDestroy() {
		super.onDestroy();
		mLocClient.stop();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mGeoCoder.destroy();
		//mMapView.onDestroy();
		mMapView = null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();
	}
}
