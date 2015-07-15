package com.wise.car;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Circle;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.google.gson.Gson;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.biz.GetUrl;
import com.wise.baba.entity.ActiveGpsData;
import com.wise.baba.entity.CarData;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.adapter.MyOrientationListener;
import com.wise.baba.ui.adapter.MyOrientationListener.OnOrientationListener;

public class CarLocationActivity extends Activity {
	MapView mMapView = null;
	LinearLayout ll_location_bottom;
	PopupWindow mPopupWindow;
	CarData carData;
	BaiduMap mBaiduMap;
	boolean isHotLocation;
	int index;
	// 当前位置
	double latitude, longitude;
	
	
	private static final double minDistance = 50;//路径规划  距离小于50米，划线
	// 定位相关
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	RoutePlanSearch mSearch = null;
	String POI_FLAG = null;
	/** 获取gps信息 **/
	private static final int get_gps = 1;
	private static final int set_vibrate = 2;
	
	
	

	AppApplication app;

	TextView searchAddress;
	ImageView iv_traffic, iv_tracking;

	/** 跟踪 **/
	boolean isTracking = false;
	/** 地图类型 **/
	int MapType = 1;
	/** 实时路口 **/
	boolean isTraffic = false;

	private View home, company;
	private Intent intent = null;

	/**
	 * 当前的精度
	 */
	private float mCurrentAccracy;

	/**
	 * 方向传感器的监听器
	 */
	private MyOrientationListener myOrientationListener;
	/**
	 * 方向传感器X方向的值
	 */
	private int mXDirection;

	private WalkingRoutePlanOption walkOption = null;

	private DrivingRoutePlanOption driveOption = null;
	
	private PolylineOptions polyOptions = null;
	
	private Overlay planLineMarker = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_car_location);
		app = (AppApplication) getApplication();
		intent = this.getIntent();
		ImageView iv_more = (ImageView) findViewById(R.id.iv_more);
		iv_more.setOnClickListener(onClickListener);
		ImageView iv_maplayers = (ImageView) findViewById(R.id.iv_maplayers);
		iv_maplayers.setOnClickListener(onClickListener);
		ImageView iv_streetscape = (ImageView) findViewById(R.id.iv_streetscape);
		iv_streetscape.setOnClickListener(onClickListener);
		iv_tracking = (ImageView) findViewById(R.id.iv_tracking);
		iv_tracking.setOnClickListener(onClickListener);
		iv_traffic = (ImageView) findViewById(R.id.iv_traffic);
		iv_traffic.setOnClickListener(onClickListener);
		TextView tv_car_name = (TextView) findViewById(R.id.tv_car_name);
		index = intent.getIntExtra("index", 0);
		@SuppressWarnings("unchecked")
		List<CarData> carDatas = (List<CarData>) intent
				.getSerializableExtra("carDatas");
		if (carDatas == null) {
			carDatas = app.carDatas;
			index = app.currentCarIndex;
		}
		isHotLocation = intent.getBooleanExtra("isHotLocation", false);
		if (carDatas == null || index >= carDatas.size()) {

		} else {
			carData = carDatas.get(index);
			tv_car_name.setText(carData.getNick_name());
		}
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		mMapView = (MapView) findViewById(R.id.mv_car_location);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(16));

		UiSettings mUiSettings = mBaiduMap.getUiSettings();
		mUiSettings.setCompassEnabled(true);
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);

		// 定位初始化
		mLocClient = new LocationClient(getApplicationContext());
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
		option.setNeedDeviceDirect(true);
		option.setOpenGps(true);// 打开gps
		option.setIsNeedAddress(true);
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(1000);
		mLocClient.setLocOption(option);
		mLocClient.start();
		getCarLocation();

		// 就初始化控件
		searchAddress = (TextView) findViewById(R.id.search_address);
		searchAddress.setOnClickListener(onClickListener);

		home = findViewById(R.id.iv_home);
		home.setOnClickListener(onClickListener);
		company = findViewById(R.id.iv_conpany);
		company.setOnClickListener(onClickListener);

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

		if (isHotLocation) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (isStop) {
						// 获取gps信息
						try {
							Thread.sleep(30000);
							if (carData == null
									|| carData.getDevice_id() == null
									|| carData.getDevice_id().equals("0")) {
								// 不需要获取gps信息
							} else {
								String gpsUrl = GetUrl.getCarGpsData(
										carData.getDevice_id(), app.auth_code);
								new NetThread.GetDataThread(handler, gpsUrl,
										get_gps).start();
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
		}

		initOritationListener();

	}

	/**
	 * 初始化方向传感器
	 */
	private void initOritationListener() {
		myOrientationListener = new MyOrientationListener(
				getApplicationContext());
		myOrientationListener
				.setOnOrientationListener(new OnOrientationListener() {
					@Override
					public void onOrientationChanged(float x) {
						mXDirection = (int) x;
						setMyLocation();

					}
				});
	}

	public void setMyLocation() {

//		if (driveOption == null && walkOption == null) {
//			return;
//		}

		// 构造定位数据
		MyLocationData locData = new MyLocationData.Builder()
				.accuracy(mCurrentAccracy)
				// 此处设置开发者获取到的方向信息，顺时针0-360
				.direction(mXDirection).latitude(latitude).longitude(longitude)
				.build();
		// 设置定位数据
		mBaiduMap.setMyLocationData(locData);
		// 设置自定义图标
		BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
				.fromResource(R.drawable.ico_map_orient);
		MyLocationConfiguration config = new MyLocationConfiguration(
				LocationMode.NORMAL, true, mCurrentMarker);
		mBaiduMap.setMyLocationConfigeration(config);
		
	}

	public void searchLocationByKeywords() {

		final String re_name = intent.getStringExtra("re_name");
		final Double lat = intent.getDoubleExtra("history_lat", 0);
		final Double lon = intent.getDoubleExtra("history_lon", 0);
		if (re_name != null && !re_name.equals("")) {
			Log.i("CarLocationActivity", "go...");

			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					searchAddress.setText(re_name);
					LatLng llg = new LatLng(lat, lon);
					MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(llg);
					mBaiduMap.setMapStatus(u);
					setTransitRoute(myLatLng, llg);

				}

			}, 500);

		}

	}

	/** 页面destory时改为false **/
	boolean isStop = true;
	private static final int SEARCH_CODE = 8;

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.search_address:
				Intent search = new Intent(CarLocationActivity.this,
						SearchLocationActivity.class);
				startActivity(search);
				CarLocationActivity.this.finish();
				break;
			case R.id.bt_location_findCar:// 寻车,客户端导航
				if (isHotLocation) {

					LatLng carLocat = new LatLng(carData.getLat(),
							carData.getLon());
					// 定位以车辆为中心
					MapStatusUpdate u = MapStatusUpdateFactory
							.newLatLng(carLocat);
					mBaiduMap.animateMapStatus(u);
					setTransitRoute(myLatLng, carLatLng);
				} else {
					showHotDialog();
				}
				break;
			case R.id.bt_location_travel:// 行程
				if (isHotLocation) {
					Intent i = new Intent(CarLocationActivity.this,
							TravelActivity.class);
					i.putExtra("device_id", carData.getDevice_id());
					String Gas_no = "93#(92#)";
					if (carData.getGas_no() != null) {
						Gas_no = carData.getGas_no();
					}
					i.putExtra("Gas_no", Gas_no);
					startActivity(i);
				} else {
					showHotDialog();
				}
				break;
			case R.id.bt_location_periphery:// 周边
				ShowPop();// 弹出popupwidow显示
				break;
			case R.id.bt_location_fence:// 围栏
				if (isHotLocation) {
					ShowFence();
				} else {
					showHotDialog();
				}
				break;
			case R.id.iv_more:
				showMorePop();
				break;
			case R.id.tv_vibrate:
				if (mPopupWindow != null) {
					mPopupWindow.dismiss();
				}
				showVibratePop();
				break;
			case R.id.bt_set_vibrate:
				if (app.carDatas != null && app.carDatas.size() > 0) {
					String rcv_time = app.carDatas.get(index).getRcv_time();
					if ((GetSystem.spacingNowTime(rcv_time) / 60) > 10
							|| rcv_time == null) {
						// 弹出提示框
						AlertDialog.Builder dialog = new AlertDialog.Builder(
								CarLocationActivity.this);
						dialog.setTitle("提示");
						dialog.setMessage("您的车辆已离线，无法设置震动灵敏度");
						dialog.setPositiveButton("设置",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										setVibrate();
									}
								}).setNegativeButton("取消", null).show();

					} else {
						setVibrate();
					}
				} else {
					Toast.makeText(CarLocationActivity.this, "请先添加车辆",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.iv_maplayers:
				ShowPopMapLayers();
				break;
			// 周边点击弹出Popupwindow监听事件
			case R.id.tv_item_car_location_oil:// 加油站
				ToSearchMap("加油站", "加油站");
				break;
			case R.id.tv_item_car_location_Parking:// 停车场
				ToSearchMap("停车场", "停车场");
				break;
			case R.id.tv_item_car_location_4s:// 4S店
				ToSearchMap("4S店", "4S店");
				break;
			case R.id.tv_item_car_location_specialist:// 维修店
				ToSearchMap("维修店", "汽车维修");
				break;
			case R.id.tv_item_car_location_automotive_beauty:// 美容店
				ToSearchMap("汽车美容店", "汽车美容");
				break;
			case R.id.tv_item_car_location_wash:// 洗车店
				ToSearchMap("洗车店", "洗车");
				break;

			// 围栏监听
			case R.id.fence_update:
				if (app.isTest) {
					Toast.makeText(CarLocationActivity.this, "演示账号不支持该功能",
							Toast.LENGTH_SHORT).show();
					return;
				}
				getDate();
				break;
			case R.id.fence_delete:
				if (app.isTest) {
					Toast.makeText(CarLocationActivity.this, "演示账号不支持该功能",
							Toast.LENGTH_SHORT).show();
					return;
				}
				String url = Constant.BaseUrl + "vehicle/"
						+ carData.getObj_id() + "/geofence" + "?auth_code="
						+ app.auth_code;
				new NetThread.DeleteThread(handler, url, DELETE).start();
				break;
			case R.id.iv_streetscape:
				// 进入街景
				Intent intent = new Intent(CarLocationActivity.this,
						PanoramaDemoActivityMain.class);
				intent.putExtra("lat", latitude);
				intent.putExtra("lon", longitude);
				startActivity(intent);
				break;

			case R.id.iv_home:
				SharedPreferences preferences = getSharedPreferences(
						"search_name", Activity.MODE_PRIVATE);
				double homeLat = Double.valueOf(preferences.getString(
						"homeLat", "0"));
				double homeLon = Double.valueOf(preferences.getString(
						"homeLon", "0"));
				if (homeLat == 0.0 && homeLon == 0.0) {
					Toast.makeText(CarLocationActivity.this, "家的地址未设置",
							Toast.LENGTH_SHORT).show();
					startActivity(new Intent(CarLocationActivity.this,
							AddressActivity.class));
				} else {
					LatLng homeLocat = new LatLng(homeLat, homeLon);
					// 定位以车辆为中心
					MapStatusUpdate mu = MapStatusUpdateFactory
							.newLatLng(homeLocat);
					mBaiduMap.animateMapStatus(mu);
					setTransitRoute(myLatLng, homeLocat);
				}
				break;
			case R.id.iv_conpany:
				SharedPreferences preferences1 = getSharedPreferences(
						"search_name", Activity.MODE_PRIVATE);
				double companyLat = Double.valueOf(preferences1.getString(
						"companyLat", "0"));
				double companyLon = Double.valueOf(preferences1.getString(
						"companyLon", "0"));
				if (companyLat == 0 && companyLon == 0) {
					Toast.makeText(CarLocationActivity.this, "公司的地址未设置",
							Toast.LENGTH_SHORT).show();
					startActivity(new Intent(CarLocationActivity.this,
							AddressActivity.class));
				} else {
					LatLng companyLocat = new LatLng(companyLat, companyLon);
					// 定位以车辆为中心
					MapStatusUpdate mu = MapStatusUpdateFactory
							.newLatLng(companyLocat);
					mBaiduMap.animateMapStatus(mu);
					setTransitRoute(myLatLng, companyLocat);
				}
				break;
			case R.id.iv_satellite:
				MapType = 0;
				setMapLayers();
				iv_satellite
						.setBackgroundResource(R.drawable.bd_wallet_my_bank_card_list_item_bg_normal);
				mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
				break;
			case R.id.iv_plain:
				MapType = 1;
				setMapLayers();
				iv_plain.setBackgroundResource(R.drawable.bd_wallet_my_bank_card_list_item_bg_normal);
				mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
				mBaiduMap.animateMapStatus(MapStatusUpdateFactory
						.newMapStatus(new MapStatus.Builder().overlook(0)
								.build()), 1000);
				break;
			case R.id.iv_3d:
				MapType = 2;
				setMapLayers();
				iv_3d.setBackgroundResource(R.drawable.bd_wallet_my_bank_card_list_item_bg_normal);
				mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
				mBaiduMap.animateMapStatus(MapStatusUpdateFactory
						.newMapStatus(new MapStatus.Builder().overlook(-30)
								.build()), 1000);
				break;
			case R.id.iv_traffic:
				if (isTraffic) {
					isTraffic = false;
					iv_traffic.setImageResource(R.drawable.main_icon_roadcondition_off);
					Toast.makeText(CarLocationActivity.this, "实时路况已关闭",Toast.LENGTH_SHORT).show();
				} else {
					isTraffic = true;
					iv_traffic.setImageResource(R.drawable.main_icon_roadcondition_on);
					Toast.makeText(CarLocationActivity.this, "实时路况已打开",
							Toast.LENGTH_SHORT).show();
				}
				mBaiduMap.setTrafficEnabled(isTraffic);
				break;
			case R.id.tv_common_adress:
				// 常用地址
				if (mPopupWindow != null) {
					mPopupWindow.dismiss();
				}
				startActivity(new Intent(CarLocationActivity.this,
						AddressActivity.class));
				break;
			case R.id.tv_offline_map:
				// 离线地图
				if (mPopupWindow != null) {
					mPopupWindow.dismiss();
				}
				startActivity(new Intent(CarLocationActivity.this,
						OfflineActivity.class));
				break;
			case R.id.iv_tracking:
				if (isHotLocation) {
					// 追踪
					isTracking = !isTracking;
					if (isTracking) {
						iv_tracking.setImageResource(R.drawable.car_track_no);
						Toast.makeText(CarLocationActivity.this, "跟踪车辆",
								Toast.LENGTH_SHORT).show();
					} else {
						iv_tracking.setImageResource(R.drawable.car_track);
						Toast.makeText(CarLocationActivity.this, "取消跟踪",Toast.LENGTH_SHORT).show();
					}
					if (!isTracking) {
						mBaiduMap.clear();
						getCarLocation();
						setMyLocation();
						//drawPhoneLocation(latitude, longitude);
					}
				} else {
					showHotDialog();
				}
				break;
			}
		}
	};

	private void showHotDialog() {
		// 弹出提示框
		AlertDialog.Builder dialog = new AlertDialog.Builder(
				CarLocationActivity.this);
		dialog.setTitle("提示");
		if (app.carDatas == null || app.carDatas.size() == 0) {
			dialog.setMessage("请先添加车辆绑定终端后使用");
		} else {
			dialog.setMessage("请先绑定终端");
		}
		dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				startActivity(new Intent(CarLocationActivity.this,
						CarActivity.class));
			}
		}).setNegativeButton("取消", null).show();
	}

	private void setMapLayers() {
		iv_satellite.setBackgroundResource(R.drawable.bd_wallet_blue_color_bg_selector);
		iv_plain.setBackgroundResource(R.drawable.bd_wallet_blue_color_bg_selector);
		iv_3d.setBackgroundResource(R.drawable.bd_wallet_blue_color_bg_selector);
	}

	/** 弹出路径规划or导航确认框 **/
	private void showDialog(final LatLng startLocat, final LatLng carLocat) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				CarLocationActivity.this);
		builder.setTitle("提示").setMessage("是否进行路径规划或导航？");

		builder.setPositiveButton("导航", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				GetSystem.FindCar(CarLocationActivity.this, startLocat,
						carLocat, "", "");
			}
		});
		builder.setNeutralButton("路径规划", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				showDrivingOrWalking(startLocat, carLocat);
			}
		});
		builder.setNegativeButton("取消", null);
		builder.create().show();
	}

	// driving,walking
	private void showDrivingOrWalking(final LatLng startLatLng,
			final LatLng stopLatLng) {
//		Log.i("CarLocationActivity", "startLatLng" + startLatLng.longitude
//				+ " " + startLatLng.latitude);
//		Log.i("CarLocationActivity", "stopLatLng" + stopLatLng.longitude + " "
//				+ stopLatLng.latitude);
		DistanceUtil distanceUtil  = new DistanceUtil();
		final double mi = distanceUtil.getDistance(startLatLng, stopLatLng);
		
		Log.i("CarLocationActivity", "mi" +mi);
		
		final PlanNode stNode = PlanNode.withLocation(startLatLng);
		final PlanNode edNode = PlanNode.withLocation(stopLatLng);
		AlertDialog.Builder builder = new AlertDialog.Builder(
				CarLocationActivity.this);
		builder.setTitle("提示").setMessage("请确认路径规划方式！");
		builder.setPositiveButton("驾车规划",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Toast.makeText(CarLocationActivity.this, "驾车规划中...",
								Toast.LENGTH_SHORT).show();
						

						if(mi<minDistance){
							drawPlan(startLatLng,stopLatLng);
							
						}else{
							driveOption = new DrivingRoutePlanOption().from(stNode)
									.to(edNode);
							mSearch.drivingSearch(driveOption);
						}
						
						setMyLocation();
					}
				});
		builder.setNegativeButton("步行规划",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Toast.makeText(CarLocationActivity.this, "步行规划中...",
								Toast.LENGTH_SHORT).show();

						if(mi<minDistance){
							drawPlan(startLatLng,stopLatLng);
						}else{
							walkOption = new WalkingRoutePlanOption().from(stNode)
									.to(edNode);
							mSearch.walkingSearch(walkOption);
						}
						
						setMyLocation();
					}
				});
		builder.create().show();
	}
	
	
	public void drawPlan(LatLng startLatLng,LatLng stopLatLng){
		if(planLineMarker !=null){
			planLineMarker.remove();
		}
		startLatLng = new LatLng(latitude, longitude);
		PolylineOptions polyOptions = new PolylineOptions();
		polyOptions.color(Color.BLUE);
		polyOptions.width(4);
		polyOptions.dottedLine(true);
		List list = new ArrayList();
		list.add(startLatLng);
		list.add(stopLatLng);
		polyOptions.points(list);
		planLineMarker = mBaiduMap.addOverlay(polyOptions);
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(20));
	}

	public void toSearchPOI() {
		POI_FLAG = intent.getStringExtra("POI_FLAG");
		if (POI_FLAG == null || POI_FLAG.equals("")) {
			return;
		}
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				System.out.println(POI_FLAG);
				if (POI_FLAG.equals("home")) {
					home.performClick();
				} else if (POI_FLAG.equals("company")) {
					company.performClick();

					System.out.println("company.....................");
				} else if (POI_FLAG.equals("address")) {
					searchAddress.performClick();
				}
				POI_FLAG = null;
			}

		}, 1000);

	}

	/**
	 * 根据类型跳转搜索
	 * 
	 * @param keyWord
	 */
	private void ToSearchMap(String keyWord, String key) {
		mPopupWindow.dismiss();
		// 地图搜寻
		Intent intent = new Intent(CarLocationActivity.this,
				SearchMapActivity.class);
		intent.putExtra("index", index);
		intent.putExtra("keyWord", keyWord);
		intent.putExtra("key", key);
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
	 * 显示围栏
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
				fence_distance.setProgress(distance / 1000 - 1);
				fence_distance_date.setText(distance / 1000 + "km");
				// setText(distance);
				getRange();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		// SEKBAR
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
				+ "/geofence" + "?auth_code=" + app.auth_code;
		
		Log.i("CarLocationActivity", geo);
		Log.i("CarLocationActivity", url);
		new NetThread.putDataThread(handler, url, params, GETDATE).start();
	}

	String geo = "";
	Circle circleOverlay;

	// 画圆（围栏）
	private void getRange() {
		if (circleOverlay != null) {
			circleOverlay.remove();
		}
		if (carData.getGeofence() != null
				&& !carData.getGeofence().equals("null")) {
			// 如果有围栏数据，则以围栏的坐标画圆
			LatLng circle = new LatLng(fence_lat, fence_lon);
			MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
					.newLatLng(circle);
			mBaiduMap.setMapStatus(mapStatusUpdate);
			// 画圆
			OverlayOptions coverFence = new CircleOptions()
					.fillColor(0x400e6f97).center(circle)
					.stroke(new Stroke(1, 0xFF0e6f97)).radius(distance);
			circleOverlay = (Circle) mBaiduMap.addOverlay(coverFence);
		} else {
			// 围栏范围圆
			LatLng circle = new LatLng(carData.getLat(), carData.getLon());
			// 画圆
			OverlayOptions coverFence = new CircleOptions()
					.fillColor(0x400e6f97).center(circle)
					.stroke(new Stroke(1, 0xFF0e6f97)).radius(distance);
			circleOverlay = (Circle) mBaiduMap.addOverlay(coverFence);
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
		setMapZoon(llLeftTop, llCenter);
	}

	/** 根据左上角和中心的距离来缩放地图，保证围栏在地图最大显示 **/
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

	LatLng carLatLng;
	Marker carMarker = null;
	boolean isFristCarLocation = true;

	// 当前车辆位子
	private void getCarLocation() {
		try {
			if (!isHotLocation) {
				return;
			}
			carLatLng = new LatLng(carData.getLat(), carData.getLon());
			if (carMarker != null) {
				carMarker.remove();
			}

			// 构建Marker图标
			BitmapDescriptor bitmap = BitmapDescriptorFactory
					.fromResource(R.drawable.body_icon_location2);
			// 构建MarkerOption，用于在地图上添加Marker
			OverlayOptions option = new MarkerOptions().anchor(0.5f, 1.0f)
					.position(carLatLng).icon(bitmap);
			// 在地图上添加Marker，并显示
			carMarker = (Marker) (mBaiduMap.addOverlay(option));
			if (isFristCarLocation) {// 第一次移动车的位置到地图中间
				isFristCarLocation = false;
				MapStatus mapStatus = new MapStatus.Builder().target(carLatLng)
						.build();
				MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
						.newMapStatus(mapStatus);
				mBaiduMap.setMapStatus(mapStatusUpdate);
			} else {
				if (isTracking) {
					MapStatus mapStatus = new MapStatus.Builder().target(
							carLatLng).build();
					MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
							.newMapStatus(mapStatus);
					mBaiduMap.setMapStatus(mapStatusUpdate);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
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
				if (circleOverlay != null) {
					circleOverlay.remove();
				}
				mPopupWindow.dismiss();
				carData.setGeofence(null);
				break;
			case get_gps:
				jsonGps(msg.obj.toString());
				break;
			case set_vibrate:
				jsonVibrate(msg.obj.toString());
				break;
			}
		}
	};

	/** 获取GPS信息 **/
	private void jsonGps(String str) {
		if (!isStop) {
			return;
		}
		LatLng startTracking = new LatLng(carData.getLat(), carData.getLon());
		Gson gson = new Gson();
		ActiveGpsData activeGpsData = gson.fromJson(str, ActiveGpsData.class);
		if (activeGpsData != null) {
			double lat = activeGpsData.getActive_gps_data().getLat();
			double lon = activeGpsData.getActive_gps_data().getLon();
			if (isTracking) {// 跟踪需要划线
				LatLng endTracking = new LatLng(lat, lon);
				trackingCar(startTracking, endTracking);
			}
			carData.setLat(lat);
			carData.setLon(lon);
			getCarLocation();
		}
	}

	private void trackingCar(LatLng lng1, LatLng lng2) {
		List<LatLng> points = new ArrayList<LatLng>();
		points.add(lng1);
		points.add(lng2);
		OverlayOptions ooPolyline = new PolylineOptions().color(0xFF0000C6)
				.points(points);
		mBaiduMap.addOverlay(ooPolyline);

	}

	/** 显示图层 **/

	ImageView iv_satellite, iv_plain, iv_3d;

	/** 显示图层 **/

	private void ShowPopMapLayers() {
		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View popunwindwow = mLayoutInflater.inflate(R.layout.pop_maplayers,
				null);
		mPopupWindow = new PopupWindow(popunwindwow, LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopupWindow.setFocusable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.showAsDropDown(findViewById(R.id.iv_maplayers), 0, 0);
		iv_satellite = (ImageView) popunwindwow.findViewById(R.id.iv_satellite);
		iv_satellite.setOnClickListener(onClickListener);
		iv_plain = (ImageView) popunwindwow.findViewById(R.id.iv_plain);
		iv_plain.setOnClickListener(onClickListener);
		iv_3d = (ImageView) popunwindwow.findViewById(R.id.iv_3d);
		iv_3d.setOnClickListener(onClickListener);
		switch (MapType) {
		case 0:
			iv_satellite
					.setBackgroundResource(R.drawable.bd_wallet_my_bank_card_list_item_bg_normal);
			break;
		case 1:
			iv_plain.setBackgroundResource(R.drawable.bd_wallet_my_bank_card_list_item_bg_normal);
			break;
		case 2:
			iv_3d.setBackgroundResource(R.drawable.bd_wallet_my_bank_card_list_item_bg_normal);
			break;
		}
	}

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

	/** 显示更多菜单 **/
	private void showMorePop() {
		LayoutInflater mLayoutInflater = LayoutInflater
				.from(CarLocationActivity.this);
		View popunwindwow = mLayoutInflater.inflate(R.layout.pop_location_more,
				null);
		TextView tv_vibrate = (TextView) popunwindwow
				.findViewById(R.id.tv_vibrate);
		tv_vibrate.setOnClickListener(onClickListener);
		TextView tv_common_adress = (TextView) popunwindwow
				.findViewById(R.id.tv_common_adress);
		tv_common_adress.setOnClickListener(onClickListener);
		TextView tv_offline_map = (TextView) popunwindwow
				.findViewById(R.id.tv_offline_map);
		tv_offline_map.setOnClickListener(onClickListener);
		mPopupWindow = new PopupWindow(popunwindwow, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		mPopupWindow.setAnimationStyle(R.style.PopupAnimation);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopupWindow.setFocusable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.showAsDropDown(findViewById(R.id.iv_more), 0, 0);
	}

	ProgressBar pb_vibrate;

	/** 显示设置震动窗口 **/
	private void showVibratePop() {
		int Height = ll_location_bottom.getMeasuredHeight();
		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View popunwindwow = mLayoutInflater.inflate(R.layout.pop_vibrate, null);
		mPopupWindow = new PopupWindow(popunwindwow, LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopupWindow.setFocusable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.showAtLocation(findViewById(R.id.bt_location_periphery),
				Gravity.BOTTOM, 0, Height);

		Button bt_set_vibrate = (Button) popunwindwow
				.findViewById(R.id.bt_set_vibrate);

		pb_vibrate = (ProgressBar) popunwindwow.findViewById(R.id.pb_vibrate);

		bt_set_vibrate.setOnClickListener(onClickListener);

		final TextView tv_vibrate = (TextView) popunwindwow
				.findViewById(R.id.tv_vibrate);
		// 刷新
		SeekBar sb_vibrate = (SeekBar) popunwindwow
				.findViewById(R.id.sb_vibrate);
		vibrate = carData.getSensitivity();
		sb_vibrate.setProgress(carData.getSensitivity());
		if (carData.getSensitivity() == 0) {
			tv_vibrate.setText("关");
		} else {
			tv_vibrate.setText("" + carData.getSensitivity());
		}
		sb_vibrate.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (progress == 0) {
					tv_vibrate.setText("关");
					vibrate = 0;
				} else {
					tv_vibrate.setText("" + progress);
					vibrate = progress;
				}
			}
		});
	}

	private final String COMMAND_VIBRATEALERT = "16391";
	int vibrate = 0;

	/** 设置震动 **/
	private void setVibrate() {
		pb_vibrate.setVisibility(View.VISIBLE);

		String url = Constant.BaseUrl + "command?auth_code=" + app.auth_code;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("device_id", carData.getDevice_id()));
		params.add(new BasicNameValuePair("cmd_type", COMMAND_VIBRATEALERT));
		params.add(new BasicNameValuePair("params", "{sensitivity: " + vibrate
				+ "}"));
		Log.e("my_log", "vibrate :" + vibrate);
		new NetThread.postDataThread(handler, url, params, set_vibrate).start();
	}

	private void jsonVibrate(String result) {
		pb_vibrate.setVisibility(View.GONE);
		try {
			JSONObject jsonObject = new JSONObject(result);
			if (jsonObject.getInt("status_code") == 0) {
				carData.setSensitivity(vibrate);
				Toast.makeText(getApplicationContext(), "设置震动报警灵敏度成功",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(), "设置震动报警灵敏度失败",
						Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "设置震动报警灵敏度失败",
					Toast.LENGTH_SHORT).show();
		}
	}

	boolean isFirstLoc = true;
	LatLng myLatLng;

	private class MyLocationListenner implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null || mMapView == null)
				return;

			latitude = location.getLatitude();
			longitude = location.getLongitude();
			app.Lat = latitude;
			app.Lon = longitude;
			mCurrentAccracy = location.getRadius();
			
			
			//Log.i("CarLocationActivity", "定位定位"+latitude+ " "+longitude);
			setMyLocation();

			//drawPhoneLocation(latitude, longitude);
			if (isFirstLoc) {
				isFirstLoc = false;
				myLatLng = new LatLng(location.getLatitude(),
						location.getLongitude());
				if (isHotLocation) {
					LatLng carLocat = new LatLng(carData.getLat(),
							carData.getLon());
					MapStatusUpdate u = MapStatusUpdateFactory
							.newLatLng(carLocat);
					mBaiduMap.animateMapStatus(u);
				} else {
					MapStatus mapStatus = new MapStatus.Builder().target(
							new LatLng(latitude, longitude)).build();
					MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
							.newMapStatus(mapStatus);
					mBaiduMap.setMapStatus(mapStatusUpdate);
				}
			}
		}
	}

	Marker phoneMark;

	/** 在地图上标记当前位置
	private void drawPhoneLocation(double latitude, double longitude) {
		// 如果有当前位置，则先删除
		if (phoneMark != null) {
			phoneMark.remove();
		}
		LatLng latLng = new LatLng(latitude, longitude);
		// 构建Marker图标
		BitmapDescriptor bitmap = BitmapDescriptorFactory
				.fromResource(R.drawable.person);
		// 构建MarkerOption，用于在地图上添加Marker
		OverlayOptions option = new MarkerOptions().anchor(0.5f, 1.0f)
				.position(latLng).icon(bitmap);
		// 在地图上添加Marker，并显示
		phoneMark = (Marker) (mBaiduMap.addOverlay(option));
	}
 **/
	/** 画出2点之间的驾车轨迹 **/
	private void setTransitRoute(LatLng startLatLng, LatLng stopLatLng) {
		if (startLatLng == null || stopLatLng == null) {
			return;
		}

		Log.i("CarLocationActivity", "画出2点之间的驾车轨迹");
		showDialog(startLatLng, stopLatLng);// driving,walking
	}

	DrivingRouteOverlay drOverlay;
	WalkingRouteOverlay wkOverlay;
	OnGetRoutePlanResultListener onGetRoutePlanResultListener = new OnGetRoutePlanResultListener() {
		@Override
		public void onGetWalkingRouteResult(WalkingRouteResult result) {
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
				Toast.makeText(CarLocationActivity.this, "抱歉，未找到结果",
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (result.error == SearchResult.ERRORNO.NO_ERROR) {
				try {
					if (drOverlay != null) {
						drOverlay.removeFromMap();
					}
					if (wkOverlay != null) {
						wkOverlay.removeFromMap();
					}
					wkOverlay = new WalkingRouteOverlay(mBaiduMap);
					mBaiduMap.setOnMarkerClickListener(wkOverlay);
					wkOverlay.setData(result.getRouteLines().get(0));
					wkOverlay.addToMap();
					wkOverlay.zoomToSpan();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				Toast.makeText(CarLocationActivity.this, "抱歉，未找到结果",
						Toast.LENGTH_SHORT).show();
				return;
			}
		}

		@Override
		public void onGetTransitRouteResult(TransitRouteResult result) {
		}

		@Override
		public void onGetDrivingRouteResult(DrivingRouteResult result) {
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
				Toast.makeText(CarLocationActivity.this, "抱歉，未找到结果",
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (result.error == SearchResult.ERRORNO.NO_ERROR) {
				try {
					if (drOverlay != null) {
						drOverlay.removeFromMap();
					}
					if (wkOverlay != null) {
						wkOverlay.removeFromMap();
					}
					drOverlay = new DrivingRouteOverlay(mBaiduMap);
					mBaiduMap.setOnMarkerClickListener(drOverlay);
					drOverlay.setData(result.getRouteLines().get(0));
					drOverlay.addToMap();
					drOverlay.zoomToSpan();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				Toast.makeText(CarLocationActivity.this, "抱歉，未找到结果",
						Toast.LENGTH_SHORT).show();
				return;
			}
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 退出时销毁定位
		mLocClient.stop();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		// mMapView.onDestroy();
		mMapView = null;
		isStop = false;
		isTracking = false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
		mLocClient.start();
		myOrientationListener.start();
		toSearchPOI();
		searchLocationByKeywords();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);

	}

	// public void toSearchPOI() {
	//
	// if(POI_FLAG == null || POI_FLAG.equals("")){
	// POI_FLAG = null;
	// return;
	//
	// }
	// System.out.println("search poi");
	// new Handler().postDelayed(new Runnable() {
	//
	// @Override
	// public void run() {
	// System.out.println(POI_FLAG);
	// if (POI_FLAG.equals("home")) {
	// home.performClick();
	// } else if (POI_FLAG.equals("company")) {
	// company.performClick();
	// }else if (POI_FLAG.equals("address")) {
	// searchAddress.performClick();
	// }
	// POI_FLAG = null;
	// }
	//
	// }, 500);
	//
	// }

	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();
		mLocClient.stop();
		myOrientationListener.start();
	}

}
