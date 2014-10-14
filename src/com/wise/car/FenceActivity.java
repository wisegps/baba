package com.wise.car;

import pubclas.Variable;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.wise.baba.R;

import data.CarData;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class FenceActivity extends Activity {
	private MapView mMapView;
	private BaiduMap mBaiduMap = null;
	CarData carData;

	private EditText fence_distance;
	private Button fence_update;

	RadioGroup group_alarm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fence);

		int index = getIntent().getIntExtra("index", 0);
		carData = Variable.carDatas.get(index);

		// 初始化地图
		mMapView = (MapView) findViewById(R.id.fence_map);
		mBaiduMap = mMapView.getMap();
		// 初始化控件
		fence_distance = (EditText) findViewById(R.id.fence_distance);
		fence_update = (Button) findViewById(R.id.fence_update);
		group_alarm = (RadioGroup) findViewById(R.id.group_alarm);
		group_alarm.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.bt_alarm:

					break;
				case R.id.bt_alarm_in:

					break;
				case R.id.bt_alarm_out:

					break;
				}
			}
		});

		fence_update.setOnClickListener(onClickListener);

		getRange();
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.fence_update:
				mMapView.getMap().clear();
				getRange();
				break;
			}
		}
	};

	private void getRange() {
		// 围栏范围圆
		LatLng circle = new LatLng(carData.getLat(), carData.getLon());

		// 构建Marker图标
		BitmapDescriptor bitmap = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_place);
		// 构建MarkerOption，用于在地图上添加Marker
		OverlayOptions option = new MarkerOptions().anchor(0.5f, 0)
				.position(circle).icon(bitmap);
		// 在地图上添加Marker，并显示
		mBaiduMap.addOverlay(option);

		MapStatus mapStatus = new MapStatus.Builder().target(circle).zoom(18)
				.build();
		MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
				.newMapStatus(mapStatus);
		mBaiduMap.setMapStatus(mapStatusUpdate);

		OverlayOptions coverFence;
		if ((fence_distance.getText().toString()) != null
				&& !(fence_distance.getText().toString()).equals("")) {
			coverFence = new CircleOptions()
					.fillColor(0xAA00FF00)
					.center(circle)
					.stroke(new Stroke(1, 0xAAFF00FF))
					.radius(Integer
							.valueOf(fence_distance.getText().toString()));

		} else {
			coverFence = new CircleOptions().fillColor(0xAA00FF00)
					.center(circle).stroke(new Stroke(1, 0xAAFF00FF))
					.radius(100);
		}
		mBaiduMap.addOverlay(coverFence);
	}
}
