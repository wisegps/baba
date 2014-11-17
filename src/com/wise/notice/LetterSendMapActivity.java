package com.wise.notice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import pubclas.Constant;
import pubclas.NetThread;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BaiduMap.SnapshotReadyCallback;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.utils.DistanceUtil;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import data.AdressData;
import data.CarData;
/**
 * 选择位置发送
 * @author honesty
 *
 */
public class LetterSendMapActivity extends Activity {
	
	private PoiSearch mPoiSearch = null;
	private BaiduMap mBaiduMap = null;
	private MapView mMapView = null;
	CarData carData;

	private final int getIsCollect = 1;
	private final int get4s = 2;
	AppApplication app;
	ListView lv_info;
	// 定位相关
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	private GeoCoder mGeoCoder = null;
	TextView tv_adress;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_letter_send_map);
		app = (AppApplication) getApplication();
		int index = getIntent().getIntExtra("index", 0);
		carData = app.carDatas.get(index);
		//lv_info
		//String keyWord = getIntent().getStringExtra("keyWord");
		//String key = getIntent().getStringExtra("key");
		TextView tv_send = (TextView)findViewById(R.id.tv_send);
		tv_send.setOnClickListener(onClickListener);
		mMapView = (MapView) findViewById(R.id.mv_search_map);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(16));
		mBaiduMap.setOnMapStatusChangeListener(onMapStatusChangeListener);
		mPoiSearch = PoiSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(poiListener);
		mGeoCoder = GeoCoder.newInstance();
		mGeoCoder.setOnGetGeoCodeResultListener(listener);
		tv_adress = (TextView)findViewById(R.id.tv_adress);
		//TextView name = (TextView) findViewById(R.id.name);
		//name.setText(keyWord);

		findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		getCarLocation();
//		if (keyWord.equals("4S店")) {
//			// 4S店数据去自己服务器读取
//			String car_brand = carData.getCar_brand();
//			SharedPreferences preferences = getSharedPreferences(
//					Constant.sharedPreferencesName, Context.MODE_PRIVATE);
//			String City = preferences.getString(Constant.sp_city, "深圳");
//			try {
//				String url = Constant.BaseUrl + "base/dealer?city="
//						+ URLEncoder.encode(City, "UTF-8") + "&brand="
//						+ URLEncoder.encode(car_brand, "UTF-8") + "&lon="
//						+ carData.getLon() + "&lat=" + carData.getLat()
//						+ "&cust_id=" + app.cust_id;
//				new Thread(new NetThread.GetDataThread(handler, url, get4s))
//						.start();
//			} catch (UnsupportedEncodingException e) {
//				e.printStackTrace();
//			}
//		} else {
//			// 搜索关键字
//			mPoiSearch.searchNearby((new PoiNearbySearchOption()).keyword(key)
//					.location(new LatLng(carData.getLat(), carData.getLon()))
//					.radius(5000));
//		}

		lv_info = (ListView) findViewById(R.id.lv_info);
		lv_info.setOnItemClickListener(onItemClickListener);
		
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
				//截图
				mBaiduMap.snapshot(new SnapshotReadyCallback() {
					public void onSnapshotReady(Bitmap snapshot) {
						File file = new File(Constant.TemporaryMapImage);
						FileOutputStream out;
						try {
							out = new FileOutputStream(file);
							if (snapshot.compress(Bitmap.CompressFormat.JPEG,
									80, out)) {
								out.flush();
								out.close();
							}
							Intent intent = new Intent();
							intent.putExtra("adress", adress);
							intent.putExtra("latitude", latitude);
							intent.putExtra("longitude", longitude);
							intent.putExtra("mapPath", Constant.TemporaryMapImage);
							LetterSendMapActivity.this.setResult(3, intent);
							finish();
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
				break;
			}
		}
	};

	// 当前车辆位子
	private void getCarLocation() {
		LatLng circle = new LatLng(carData.getLat(), carData.getLon());
		// 构建Marker图标
		BitmapDescriptor bitmap = BitmapDescriptorFactory
				.fromResource(R.drawable.body_icon_location2);
		// 构建MarkerOption，用于在地图上添加Marker
		OverlayOptions option = new MarkerOptions().anchor(0.5f, 0.5f)
				.position(circle).icon(bitmap);
		// 在地图上添加Marker，并显示
		mBaiduMap.addOverlay(option);
	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case getIsCollect:
				break;
			case get4s:
				break;
			}
		}

	};


	OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			
		}
	};

	OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener() {
		@Override
		public void onGetPoiResult(PoiResult result) {

			if (result == null
					|| result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
				Toast.makeText(LetterSendMapActivity.this, "抱歉，没找到结果",
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (result.error == SearchResult.ERRORNO.NO_ERROR) {
				PoiOverlay overlay = new PoiOverlay(mBaiduMap);
				mBaiduMap.setOnMarkerClickListener(overlay);
				overlay.setData(result);
				overlay.addToMap();
				overlay.zoomToSpan();
				String str = "";// 用户判断是否已经收藏
				PoiInfo mkPoiInfo = null;
				for (int i = 0; i < result.getAllPoi().size(); i++) {
					mkPoiInfo = result.getAllPoi().get(i);
					int distance = (int) DistanceUtil.getDistance(new LatLng(
							carData.getLat(), carData.getLon()),
							mkPoiInfo.location);
					AdressData adressData = new AdressData();
					adressData.setName(mkPoiInfo.name);
					adressData.setAdress(mkPoiInfo.address);
					adressData.setPhone(mkPoiInfo.phoneNum);
					adressData.setLat(mkPoiInfo.location.latitude);
					adressData.setLon(mkPoiInfo.location.longitude);
					adressData.setDistance(distance);
					System.out.println("name = " + mkPoiInfo.name + " , address = " + mkPoiInfo.address);
					//adressDatas.add(adressData);
					//str = str + mkPoiInfo.name + ",";
				}
				// Collections.sort(adressDatas, new Comparator());// 排序
				//adressAdapter.notifyDataSetChanged();
				return;
			}
			if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {

				// 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
				String strInfo = "在";
				for (CityInfo cityInfo : result.getSuggestCityList()) {
					strInfo += cityInfo.city;
					strInfo += ",";
				}
				strInfo += "找到结果";
				Toast.makeText(LetterSendMapActivity.this, strInfo,
						Toast.LENGTH_LONG).show();
			}

		}

		@Override
		public void onGetPoiDetailResult(PoiDetailResult result) {
			if (result.error != SearchResult.ERRORNO.NO_ERROR) {
				Toast.makeText(LetterSendMapActivity.this, "抱歉，未找到结果",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(LetterSendMapActivity.this,
						result.getName() + ": " + result.getAddress(),
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	class Comparator implements java.util.Comparator<AdressData> {
		@Override
		public int compare(AdressData lhs, AdressData rhs) {
			int m1 = lhs.getDistance();
			int m2 = rhs.getDistance();
			int result = 0;
			if (m1 > m2) {
				result = 1;
			}
			if (m1 < m2) {
				result = -1;
			}
			return result;
		}
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
					.direction(0).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
		            .fromResource(R.drawable.person);
		    MyLocationConfiguration config = new MyLocationConfiguration(null,
		            true, mCurrentMarker);
		    mBaiduMap.setMyLocationConfigeration(config);
		    if(isFirstLoc){
		    	isFirstLoc = false;
			    MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
						.newLatLng(new LatLng(location.getLatitude(),
								location.getLongitude()));
				mBaiduMap.setMapStatus(mapStatusUpdate);
				mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(new LatLng(location.getLatitude(), location.getLongitude())));
				latitude = location.getLatitude();
				longitude = location.getLongitude();
				//在地图上添加标记
				LatLng latLng = new LatLng(latitude,longitude);
				OverlayOptions marker = new MarkerOptions().position(latLng).anchor(0.5f, 1.0f).icon(
							BitmapDescriptorFactory
									.fromResource(R.drawable.icon_location));
				mark = (Marker)(mBaiduMap.addOverlay(marker));
		    }
		}
	}
	Marker mark = null;
	OnMapStatusChangeListener onMapStatusChangeListener = new OnMapStatusChangeListener() {		
		@Override
		public void onMapStatusChangeStart(MapStatus arg0) {
			System.out.println("onMapStatusChangeStart");
			if(mark != null){
				mark.remove();
			}
		}
		
		@Override
		public void onMapStatusChangeFinish(MapStatus arg0) {
			//移动完毕，获取地图中心位置
			mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(arg0.target));
			latitude = arg0.target.latitude;
			longitude = arg0.target.longitude;

			//在地图上添加标记
			LatLng latLng = new LatLng(latitude,longitude);
			OverlayOptions marker = new MarkerOptions().position(latLng).anchor(0.5f, 1.0f).icon(
						BitmapDescriptorFactory
								.fromResource(R.drawable.icon_location));
			mark = (Marker)(mBaiduMap.addOverlay(marker));
		}
		
		@Override
		public void onMapStatusChange(MapStatus arg0) {
			System.out.println("onMapStatusChange");
		}
	};
	String adress = "";
	double latitude = 0;
	double longitude = 0;
	OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
		@Override
		public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
				// 没有检索到结果
			} else {
				adress = result.getAddress();
				tv_adress.setText(adress);
				//找到地址后找对应的周边poi
//				mPoiSearch.searchNearby((new PoiNearbySearchOption()).keyword(adress)
//						.location(new LatLng(latitude, longitude))
//						.radius(5000));
			}
		}

		@Override
		public void onGetGeoCodeResult(GeoCodeResult arg0) {

		}
	};

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
		mPoiSearch.destroy();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
	}
}
