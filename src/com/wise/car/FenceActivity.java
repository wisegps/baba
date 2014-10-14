package com.wise.car;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import pubclas.Constant;
import pubclas.NetThread;
import pubclas.Variable;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
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
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class FenceActivity extends Activity {
	// 报警状态
	private static final int ALARM = 0;// 进出报警
	private static final int ALARM_IN = 1;// 进入报警
	private static final int ALARM_OUT = 2;// 驶出报警

	private static final int GETDATE = 3;// 消息码
	private static final int DELETE = 4;// 删除码
	private int geo_type;
	private MapView mMapView;
	private BaiduMap mBaiduMap = null;
	CarData carData;

	private EditText fence_distance;

	RadioGroup group_alarm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fence);

		int index = getIntent().getIntExtra("index", 0);
		carData = Variable.carDatas.get(index);
		geo_type = ALARM;

		// 初始化地图
		mMapView = (MapView) findViewById(R.id.fence_map);
		mBaiduMap = mMapView.getMap();
		// 初始化控件
		fence_distance = (EditText) findViewById(R.id.fence_distance);
		group_alarm = (RadioGroup) findViewById(R.id.group_alarm);
		group_alarm.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.bt_alarm:
					geo_type = ALARM;
					break;
				case R.id.bt_alarm_in:
					geo_type = ALARM_IN;
					break;
				case R.id.bt_alarm_out:
					geo_type = ALARM_OUT;
					break;
				}
				getDate();
			}
		});

		findViewById(R.id.fence_update).setOnClickListener(onClickListener);
		findViewById(R.id.fence_delete).setOnClickListener(onClickListener);
		findViewById(R.id.iv_back).setOnClickListener(onClickListener);
		getDate();

	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GETDATE:
				getRange();
				break;
			case DELETE:
				mMapView.getMap().clear();
				break;
			}
		}
	};

	private void getDate() {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("geo_type", String.valueOf(geo_type)));
		params.add(new BasicNameValuePair("lon", String.valueOf(carData
				.getLon())));
		params.add(new BasicNameValuePair("lat", String.valueOf(carData
				.getLat())));
		params.add(new BasicNameValuePair("width", fence_distance.getText()
				.toString()));

		String url = Constant.BaseUrl + "vehicle/" + carData.getObj_id()
				+ "?auth_code=" + Variable.auth_code;
		new NetThread.putDataThread(handler, url, params, GETDATE).start();
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				FenceActivity.this.finish();
				break;
			case R.id.fence_update:
				mMapView.getMap().clear();
				getRange();
				break;
			case R.id.fence_delete:
				String url = Constant.BaseUrl + "vehicle/"
						+ carData.getObj_id() + "?auth_code="
						+ Variable.auth_code;
				new NetThread.DeleteThread(handler, url, DELETE).start();
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
