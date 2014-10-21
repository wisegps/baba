package com.wise.car;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import pubclas.Constant;
import pubclas.DensityUtil;
import pubclas.NetThread;
import pubclas.Variable;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.navi.BaiduMapAppNotSupportNaviException;
import com.baidu.mapapi.navi.BaiduMapNavigation;
import com.baidu.mapapi.navi.NaviPara;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.wise.baba.R;
import data.CarData;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
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
	// 当前位置
	double latitude, longitude;
	// 定位相关
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	RoutePlanSearch mSearch = null;

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
		mSearch = RoutePlanSearch.newInstance();
		mSearch.setOnGetRoutePlanResultListener(onGetRoutePlanResultListener);
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.bt_location_findCar:// TODO 寻车,客户端导航
				LatLng carLocat = new LatLng(carData.getLat(), carData.getLon());
				// 定位以车辆为中心
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(carLocat);
				mBaiduMap.animateMapStatus(u);
				setTransitRoute(ll, circle);
				break;
			case R.id.bt_location_travel:// 行程
				Intent i = new Intent(CarLocationActivity.this,
						TravelActivity.class);
				i.putExtra("index", index);
				startActivity(i);
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
				ToSearchMap("洗车美容");
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

	private void showDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				CarLocationActivity.this);
		builder.setTitle("寻车").setMessage("是否进行路径导航？");

		builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
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
			}
		});
		builder.setPositiveButton("取消", null);
		builder.create().show();
	}

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
		intent.putExtra("latitude", latitude);
		intent.putExtra("longitude", longitude);
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
	TextView fence_distance_date;

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
		fence_distance_date = (TextView) popunwindwow
				.findViewById(R.id.fence_distance_date);

		if (carData.getGeofence() != null
				&& !carData.getGeofence().equals("null")) {
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
				fence_distance.setProgress((int) (distance / 1000) - 1);
				fence_distance_date.setText(distance / 1000 + "km");
				// setText(distance);
				getRange();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		fence_distance
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					@Override
					// 停止拖动时触发
					public void onStopTrackingTouch(SeekBar seekBar) {
					}

					@Override
					// 开始触碰时触发
					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					@Override
					// 拖动过程中
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						distance = (fence_distance.getProgress() + 1) * 1000;
						fence_distance_date.setText(distance / 1000 + "km");
						mMapView.getMap().clear();
						getRange();
						// setText(distance);
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
		if (carData.getGeofence() != null
				&& !carData.getGeofence().equals("null")) {
			getCarLocation();
			LatLng circle = new LatLng(fence_lat, fence_lon);
			MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
					.newLatLng(circle);
			mBaiduMap.setMapStatus(mapStatusUpdate);
			// 画圆
			OverlayOptions coverFence = new CircleOptions()
					.fillColor(0x400e6f97).center(circle)
					.stroke(new Stroke(1, 0xFF0e6f97)).radius(distance);
			mBaiduMap.addOverlay(coverFence);
		} else {
			// 围栏范围圆
			LatLng circle = new LatLng(carData.getLat(), carData.getLon());
			// 画圆
			OverlayOptions coverFence = new CircleOptions()
					.fillColor(0x400e6f97).center(circle)
					.stroke(new Stroke(1, 0xFF0e6f97)).radius(distance);
			mBaiduMap.addOverlay(coverFence);
			getCarLocation();
		}
		// 获取左上角坐标
		LatLng llLeftTop = mBaiduMap.getProjection().fromScreenLocation(
				new Point(0, 0));
		LatLng llCenter;
		if (carData.getGeofence() != null
				&& !carData.getGeofence().equals("null")) {
			llCenter = new LatLng(fence_lat, fence_lon);
		} else {
			llCenter = new LatLng(carData.getLat(), carData.getLon());
		}
		// 计算2点之间的距离
		setMapZoon(llLeftTop, llCenter);
	}

	private void setMapZoon(LatLng llLeftTop, LatLng llCenter) {
		double nowDistance = DistanceUtil.getDistance(llLeftTop, llCenter);
		float zoom = mBaiduMap.getMapStatus().zoom;
		// 放大地图
		if (nowDistance < distance * 2.5) {
			mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(zoom - 1));
		} else if (nowDistance > distance * 4) {
			mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(zoom + 1));
		}
	}

	/** 添加文字 **/
	private void setText(int distance) {
		int px = DensityUtil.dip2px(CarLocationActivity.this, 25);
		// LatLng llText = new LatLng(latitude, longitude);
		LatLng llText = new LatLng(fence_lat, fence_lon);
		OverlayOptions ooText = new TextOptions()
				.align(TextOptions.ALIGN_BOTTOM, 500).fontSize(px)
				.fontColor(getResources().getColor(R.color.white))
				.text("    " + distance / 1000 + "km").position(llText);
		mBaiduMap.addOverlay(ooText);
	}

	LatLng circle;

	// 当前车辆位子
	private void getCarLocation() {
		circle = new LatLng(carData.getLat(), carData.getLon());
		// 构建Marker图标
		BitmapDescriptor bitmap = BitmapDescriptorFactory
				.fromResource(R.drawable.body_icon_location2);
		// 构建MarkerOption，用于在地图上添加Marker
		OverlayOptions option = new MarkerOptions().anchor(0.5f, 1.0f)
				.position(circle).icon(bitmap);
		// 在地图上添加Marker，并显示
		mBaiduMap.addOverlay(option);

		MapStatus mapStatus = new MapStatus.Builder().target(circle).build();
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

	boolean isFirstLoc = true;
	LatLng ll;

	private class MyLocationListenner implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null || mMapView == null)
				return;
			latitude = location.getLatitude();
			longitude = location.getLongitude();
			Variable.Lat = latitude;
			Variable.Lon = longitude;
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			if (isFirstLoc) {
				isFirstLoc = false;
				ll = new LatLng(location.getLatitude(), location.getLongitude());
				LatLng carLocat = new LatLng(carData.getLat(), carData.getLon());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(carLocat);
				mBaiduMap.animateMapStatus(u);
			}
		}

		@Override
		public void onReceivePoi(BDLocation arg0) {
			// TODO Auto-generated method stub

		}
	}

	/** 画出2点之间的驾车轨迹 **/
	private void setTransitRoute(LatLng startLatLng, LatLng stopLatLng) {
		if (startLatLng == null || stopLatLng == null) {
			return;
		}
		System.out.println("轨迹");
		PlanNode stNode = PlanNode.withLocation(startLatLng);
		PlanNode edNode = PlanNode.withLocation(stopLatLng);
		mSearch.drivingSearch(new DrivingRoutePlanOption().from(stNode).to(
				edNode));
	}

	OnGetRoutePlanResultListener onGetRoutePlanResultListener = new OnGetRoutePlanResultListener() {
		@Override
		public void onGetWalkingRouteResult(WalkingRouteResult arg0) {
		}

		@Override
		public void onGetTransitRouteResult(TransitRouteResult result) {
		}

		@Override
		public void onGetDrivingRouteResult(DrivingRouteResult result) {
			DrivingRouteOverlay overlay = new DrivingRouteOverlay(mBaiduMap);
			mBaiduMap.setOnMarkerClickListener(overlay);
			overlay.setData(result.getRouteLines().get(0));
			overlay.addToMap();
			//overlay.zoomToSpan();
			showDialog();
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 退出时销毁定位
		mLocClient.stop();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
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
