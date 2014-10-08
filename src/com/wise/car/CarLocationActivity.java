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
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class CarLocationActivity extends Activity {
	MapView mMapView = null;
	LinearLayout ll_location_bottom;
	PopupWindow mPopupWindow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_car_location);
		int index = getIntent().getIntExtra("index", 0);
		CarData carData = Variable.carDatas.get(index);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		mMapView = (MapView) findViewById(R.id.mv_car_location);
		BaiduMap mBaiduMap = mMapView.getMap();
		// 定义Maker坐标点
		LatLng point = new LatLng(carData.getLat(), carData.getLon());

		// 构建Marker图标
		BitmapDescriptor bitmap = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_place);
		// 构建MarkerOption，用于在地图上添加Marker
		OverlayOptions option = new MarkerOptions().anchor(0.5f, 0)
				.position(point).icon(bitmap);
		// 在地图上添加Marker，并显示
		mBaiduMap.addOverlay(option);

		MapStatus mapStatus = new MapStatus.Builder().target(point).zoom(18)
				.build();
		MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
				.newMapStatus(mapStatus);
		mBaiduMap.setMapStatus(mapStatusUpdate);

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
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.bt_location_findCar:// 寻车
				Toast.makeText(CarLocationActivity.this, "寻车（更新中）",
						Toast.LENGTH_SHORT).show();
				break;
			case R.id.bt_location_travel:// 行程
				Toast.makeText(CarLocationActivity.this, "行程（更新中）",
						Toast.LENGTH_SHORT).show();
				break;
			case R.id.bt_location_periphery:// 周边
				ShowPop();// 弹出popupwidow显示
				break;
			case R.id.bt_location_fence:// 围栏
				Toast.makeText(CarLocationActivity.this, "围栏（更新中）",
						Toast.LENGTH_SHORT).show();
				break;

			// 周边点击弹出Popupwindow监听事件
			case R.id.tv_item_car_location_oil:// 加油站
				ToSearchMap("");
				break;
			case R.id.tv_item_car_location_Parking:// 停车场
				ToSearchMap("");
				break;
			case R.id.tv_item_car_location_4s:// 4S店
				ToSearchMap("");
				break;
			case R.id.tv_item_car_location_specialist:// 维修店
				ToSearchMap("");
				break;
			case R.id.tv_item_car_location_automotive_beauty:// 美容店
				ToSearchMap("");
				break;
			case R.id.tv_item_car_location_wash:// 洗车店
				ToSearchMap("");
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
		Toast.makeText(CarLocationActivity.this, "正在更新中", 2000).show();
	}

	/**
	 * 弹出popupwindow
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