package com.wise.car;

import pubclas.Variable;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.wise.baba.R;

import data.CarData;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;

public class CarLocationActivity extends Activity{
	MapView mMapView = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_car_location);
		int index = getIntent().getIntExtra("index", 0);
		CarData carData = Variable.carDatas.get(index);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		mMapView = (MapView)findViewById(R.id.mv_car_location);
		BaiduMap mBaiduMap = mMapView.getMap();
		//定义Maker坐标点  
		LatLng point = new LatLng(carData.getLat(), carData.getLon()); 
		
		//构建Marker图标  
		BitmapDescriptor bitmap = BitmapDescriptorFactory  
		    .fromResource(R.drawable.icon_place);  
		//构建MarkerOption，用于在地图上添加Marker  
		OverlayOptions option = new MarkerOptions().anchor(0.5f, 0)
		    .position(point)  
		    .icon(bitmap);
		//在地图上添加Marker，并显示  
		mBaiduMap.addOverlay(option);
		
		MapStatus mapStatus = new MapStatus.Builder().target(point).zoom(18).build();
		MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
		mBaiduMap.setMapStatus(mapStatusUpdate);
		
	}
	OnClickListener onClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;

			default:
				break;
			}
		}		
	};
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
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