package com.wise.car;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import pubclas.Constant;
import pubclas.GetLocation;
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
import com.baidu.mapapi.navi.BaiduMapAppNotSupportNaviException;
import com.baidu.mapapi.navi.BaiduMapNavigation;
import com.baidu.mapapi.navi.NaviPara;
import com.wise.baba.R;

import data.CarData;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class CarLocationActivity extends Activity {
	MapView mMapView = null;
	LinearLayout ll_location_bottom;
	PopupWindow mPopupWindow;
	CarData carData;
	BaiduMap mBaiduMap;
	int index;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_car_location);
		index = getIntent().getIntExtra("index", 0);
		carData = Variable.carDatas.get(index);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		mMapView = (MapView) findViewById(R.id.mv_car_location);
		mBaiduMap = mMapView.getMap();
		getCarLocation();

		// 就初始化控件
		findViewById(R.id.bt_location_findCar).setOnClickListener(
				onClickListener);
		findViewById(R.id.bt_location_travel).setOnClickListener(
				onClickListener);
		findViewById(R.id.bt_location_periphery).setOnClickListener(
				onClickListener);
		findViewById(R.id.bt_location_fence)
				.setOnClickListener(onClickListener);
		ll_location_bottom = (LinearLayout) findViewById(R.id.ll_location_bottom);

		registerBroadcastReceiver();
		GetLocation getLocation = new GetLocation(CarLocationActivity.this);
	}

	private void registerBroadcastReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constant.A_City);
		registerReceiver(broadcastReceiver, intentFilter);
	}

	double latitude, longitude;
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Constant.A_City)) {
				latitude = Double.valueOf((intent.getStringExtra("Lat")));
				longitude = Double.valueOf((intent.getStringExtra("Lon")));
			}
		}
	};
	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.bt_location_findCar:// TODO 寻车,客户端导航
				LatLng startLocat = new LatLng(latitude, longitude);
				LatLng carLocat = new LatLng(carData.getLat(), carData.getLon());
				// 构建 导航参数
				NaviPara param = new NaviPara();
				param.startPoint = startLocat;
				param.startName = "";
				param.endPoint = carLocat;
				param.endName = "";
				try {
					BaiduMapNavigation.openBaiduMapNavi(param,
							CarLocationActivity.this);
				} catch (BaiduMapAppNotSupportNaviException e) {
					e.printStackTrace();
					BaiduMapNavigation.openWebBaiduMapNavi(param,
							CarLocationActivity.this);
				}
				break;
			case R.id.bt_location_travel:// 行程
				Intent i = new Intent(CarLocationActivity.this,
						TravelActivity.class);
				startActivity(i);
				// Toast.makeText(CarLocationActivity.this, "行程（更新中）",
				// Toast.LENGTH_SHORT).show();
				break;
			case R.id.bt_location_periphery:// 周边
				ShowPop();// 弹出popupwidow显示
				break;
			case R.id.bt_location_fence:// 围栏
				ShowFence();
				break;

			// 周边点击弹出Popupwindow监听事件
			case R.id.tv_item_car_location_oil:// 加油站
				ToSearchMap("加油站");
				break;
			case R.id.tv_item_car_location_Parking:// 停车场
				ToSearchMap("停车场");
				break;
			case R.id.tv_item_car_location_4s:// 4S店
				ToSearchMap("4S店");
				break;
			case R.id.tv_item_car_location_specialist:// 维修店
				ToSearchMap("维修店");
				break;
			case R.id.tv_item_car_location_automotive_beauty:// 美容店
				ToSearchMap("美容店");
				break;
			case R.id.tv_item_car_location_wash:// 洗车店
				ToSearchMap("洗车店");
				break;

			// 围栏监听
			case R.id.fence_update:
				getDate();
				break;
			case R.id.fence_delete:
				String url = Constant.BaseUrl + "vehicle/"
						+ carData.getObj_id() + "/geofence" + "?auth_code="
						+ Variable.auth_code;
				new NetThread.DeleteThread(handler, url, DELETE).start();
				break;
			}
		}
	};

	/**
	 * 根据类型跳转搜索
	 * 
	 * @param keyWord
	 */
	private void ToSearchMap(String keyWord) {
		mPopupWindow.dismiss();
		// TODO 地图搜寻
		Intent intent = new Intent(CarLocationActivity.this,
				SearchMapActivity.class);
		intent.putExtra("index", index);
		intent.putExtra("keyWord", keyWord);
		startActivity(intent);
	}

	// 报警状态
	private static final int ALARM = 0;// 进出报警
	private static final int ALARM_IN = 1;// 进入报警
	private static final int ALARM_OUT = 2;// 驶出报警

	private static final int GETDATE = 3;// 消息码
	private static final int DELETE = 4;// 删除码
	private int geo_type;
	SeekBar fence_distance;
	int distance = 0;

	CheckBox bt_alarm_in, bt_alarm_out;
	double fence_lat, fence_lon;

	/**
	 * TODO 显示围栏
	 */
	private void ShowFence() {
		int Height = ll_location_bottom.getMeasuredHeight();
		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View popunwindwow = mLayoutInflater.inflate(R.layout.activity_fence,
				null);
		mPopupWindow = new PopupWindow(popunwindwow, LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopupWindow.setFocusable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.showAtLocation(findViewById(R.id.bt_location_fence),
				Gravity.BOTTOM, 0, Height);

		popunwindwow.findViewById(R.id.fence_update).setOnClickListener(
				onClickListener);
		popunwindwow.findViewById(R.id.fence_delete).setOnClickListener(
				onClickListener);

		bt_alarm_in = (CheckBox) popunwindwow.findViewById(R.id.bt_alarm_in);
		bt_alarm_out = (CheckBox) popunwindwow.findViewById(R.id.bt_alarm_out);
		fence_distance = (SeekBar) popunwindwow
				.findViewById(R.id.fence_distance);
		System.out.println(carData.toString());
		if (carData.getGeofence() != null && !carData.getGeofence().equals("null")) {
			try {
				JSONObject json = new JSONObject(carData.getGeofence());
				distance = json.getInt("width");
				fence_lat = json.getDouble("lat");
				fence_lon = json.getDouble("lon");
				geo_type = json.getInt("geo_type");
				if (geo_type == ALARM_IN) {
					bt_alarm_in.setChecked(true);
				} else if (geo_type == ALARM_OUT) {
					bt_alarm_out.setChecked(true);
				} else if (geo_type == ALARM) {
					bt_alarm_in.setChecked(true);
					bt_alarm_out.setChecked(true);
				}
				fence_distance.setProgress(distance);
				getRange();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		System.out.println("------");
		fence_distance
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					@Override
					// 停止拖动时触发
					public void onStopTrackingTouch(SeekBar seekBar) {
						distance = fence_distance.getProgress();
						mMapView.getMap().clear();
						getRange();
					}

					@Override
					// 开始触碰时触发
					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					@Override
					// 拖动过程中
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						distance = fence_distance.getProgress();
						mMapView.getMap().clear();
						getRange();
					}
				});

	}

	// 上传数据
	private void getDate() {
		if (!bt_alarm_out.isChecked() && !bt_alarm_in.isChecked()) {
			// 提示
			Toast.makeText(CarLocationActivity.this, "未设置报警类型",
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (bt_alarm_out.isChecked() && bt_alarm_in.isChecked()) {
			geo_type = ALARM;
		} else if (bt_alarm_out.isChecked() && !bt_alarm_in.isChecked()) {
			geo_type = ALARM_OUT;
		} else if (!bt_alarm_out.isChecked() && bt_alarm_in.isChecked()) {
			geo_type = ALARM_IN;
		}
		geo = "{geo_type:" + geo_type + ",lon:" + carData.getLon() + ",lat:"
				+ carData.getLat() + ",width:" + distance + "}";
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("geo", geo));
		String url = Constant.BaseUrl + "vehicle/" + carData.getObj_id()
				+ "/geofence" + "?auth_code=" + Variable.auth_code;
		new NetThread.putDataThread(handler, url, params, GETDATE).start();
	}

	String geo = "";

	// 画圆（围栏）
	private void getRange() {
		System.out.println("getRange");
		if (carData.getGeofence() != null && !carData.getGeofence().equals("null")) {
			getCarLocation();
			LatLng circle = new LatLng(fence_lat, fence_lon);
			// 画圆
			OverlayOptions coverFence = new CircleOptions()
					.fillColor(0xAA00FF00).center(circle)
					.stroke(new Stroke(1, 0xAAFF00FF)).radius(distance);
			mBaiduMap.addOverlay(coverFence);
		} else {
			getCarLocation();
			// 围栏范围圆
			LatLng circle = new LatLng(carData.getLat(), carData.getLon());
			// 画圆
			OverlayOptions coverFence = new CircleOptions()
					.fillColor(0xAA00FF00).center(circle)
					.stroke(new Stroke(1, 0xAAFF00FF)).radius(distance);
			mBaiduMap.addOverlay(coverFence);
		}
	}

	// 当前车辆位子
	private void getCarLocation() {
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
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GETDATE:
				System.out.println(msg.obj.toString());
				Toast.makeText(CarLocationActivity.this, "设置成功",
						Toast.LENGTH_SHORT).show();
				mPopupWindow.dismiss();
				carData.setGeofence(geo);
				break;
			case DELETE:
				Toast.makeText(CarLocationActivity.this, "删除成功",
						Toast.LENGTH_SHORT).show();
				mMapView.getMap().clear();
				getCarLocation();
				mPopupWindow.dismiss();
				carData.setGeofence(null);
				break;
			}
		}
	};

	/**
	 * 弹出popupwindow 显示周边
	 */
	private void ShowPop() {
		int Height = ll_location_bottom.getMeasuredHeight();
		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View popunwindwow = mLayoutInflater.inflate(R.layout.item_car_location,
				null);
		mPopupWindow = new PopupWindow(popunwindwow, LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopupWindow.setFocusable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.showAtLocation(findViewById(R.id.bt_location_periphery),
				Gravity.BOTTOM, 0, Height);
		TextView tv_item_car_location_oil = (TextView) popunwindwow
				.findViewById(R.id.tv_item_car_location_oil);
		tv_item_car_location_oil.setOnClickListener(onClickListener);
		TextView tv_item_car_location_Parking = (TextView) popunwindwow
				.findViewById(R.id.tv_item_car_location_Parking);
		tv_item_car_location_Parking.setOnClickListener(onClickListener);
		TextView tv_item_car_location_4s = (TextView) popunwindwow
				.findViewById(R.id.tv_item_car_location_4s);
		tv_item_car_location_4s.setOnClickListener(onClickListener);
		TextView tv_item_car_location_specialist = (TextView) popunwindwow
				.findViewById(R.id.tv_item_car_location_specialist);
		tv_item_car_location_specialist.setOnClickListener(onClickListener);
		TextView tv_item_car_location_automotive_beauty = (TextView) popunwindwow
				.findViewById(R.id.tv_item_car_location_automotive_beauty);
		tv_item_car_location_automotive_beauty
				.setOnClickListener(onClickListener);
		TextView tv_item_car_location_wash = (TextView) popunwindwow
				.findViewById(R.id.tv_item_car_location_wash);
		tv_item_car_location_wash.setOnClickListener(onClickListener);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
		unregisterReceiver(broadcastReceiver);
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