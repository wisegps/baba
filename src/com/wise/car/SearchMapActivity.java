package com.wise.car;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.model.LatLngBounds.Builder;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
import com.baidu.mapapi.utils.DistanceUtil;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.entity.AdressData;
import com.wise.baba.entity.CarData;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.adapter.AdressAdapter;
import com.wise.baba.ui.adapter.AdressAdapter.OnCollectListener;
import com.wise.setting.AccountActivity;


public class SearchMapActivity extends Activity {
	private PoiSearch mPoiSearch = null;
	private BaiduMap mBaiduMap = null;
	private MapView mMapView = null;
	CarData carData;

	private final int getIsCollect = 1;
	private final int get4s = 2;
	AppApplication app;
	List<AdressData> adressDatas = new ArrayList<AdressData>();
	ListView lv_activity_search_map;
	AdressAdapter adressAdapter;
	// 定位相关
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_search_map);
		app = (AppApplication) getApplication();
		int index = getIntent().getIntExtra("index", 0);
		carData = app.carDatas.get(index);
		String keyWord = getIntent().getStringExtra("keyWord");
		String key = getIntent().getStringExtra("key");
		mMapView = (MapView) findViewById(R.id.mv_search_map);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(12));
		mPoiSearch = PoiSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(poiListener);

		TextView name = (TextView) findViewById(R.id.name);
		name.setText(keyWord);

		findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		getCarLocation();
		if (keyWord.equals("4S店")) {
			// 4S店数据去自己服务器读取
			String car_brand = carData.getCar_brand();
			SharedPreferences preferences = getSharedPreferences(
					Constant.sharedPreferencesName, Context.MODE_PRIVATE);
			String City = preferences.getString(Constant.sp_city, "深圳");
			try {
				String url = Constant.BaseUrl + "base/dealer?city="
						+ URLEncoder.encode(City, "UTF-8") + "&brand="
						+ URLEncoder.encode(car_brand, "UTF-8") + "&lon="
						+ carData.getLon() + "&lat=" + carData.getLat()
						+ "&cust_id=" + app.cust_id;
				new Thread(new NetThread.GetDataThread(handler, url, get4s))
						.start();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} else {
			
			// 搜索关键字
			mPoiSearch.searchNearby((new PoiNearbySearchOption()).keyword(key)
					.location(new LatLng(carData.getLat(), carData.getLon()))
					.radius(5000).sortType(PoiSortType.distance_from_near_to_far));
		}

		lv_activity_search_map = (ListView) findViewById(R.id.lv_activity_search_map);
		lv_activity_search_map.setOnItemClickListener(onItemClickListener);
		adressAdapter = new AdressAdapter(SearchMapActivity.this, adressDatas,
				SearchMapActivity.this);
		lv_activity_search_map.setAdapter(adressAdapter);
		adressAdapter.setOnCollectListener(new OnCollectListener() {
			@Override
			public void OnCollect(int index) {
				if (app.isTest) {
					Toast.makeText(SearchMapActivity.this, "演示账号不支持该功能",
							Toast.LENGTH_SHORT).show();
					return;
				}
				adressDatas.get(index).setIs_collect(true);
				adressAdapter.notifyDataSetChanged();
			}

			@Override
			public void OnShare(int index) {
				AdressData adressData = adressDatas.get(index);
				String url = "http://api.map.baidu.com/geocoder?location="
						+ adressData.getLat() + "," + adressData.getLon()
						+ "&coord_type=bd09ll&output=html";
				StringBuffer sb = new StringBuffer();
				sb.append("【地点】");
				sb.append(adressData.getName());
				sb.append("," + adressData.getAdress());
				sb.append("," + adressData.getPhone());
				sb.append("," + url);
				GetSystem.share(SearchMapActivity.this, sb.toString(), "",
						(float) adressData.getLat(),
						(float) adressData.getLon(), "地点", url);
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

	// 当前车辆位子
	private void getCarLocation() {
		LatLng circle = new LatLng(carData.getLat(), carData.getLon());
		// 构建Marker图标
		BitmapDescriptor bitmap = BitmapDescriptorFactory
				.fromResource(R.drawable.body_icon_location2);
		// 构建MarkerOption，用于在地图上添加Marker
		OverlayOptions option = new MarkerOptions().anchor(0.5f, 1.0f)
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
				jsonCollect(msg.obj.toString());
				adressAdapter.notifyDataSetChanged();
				break;
			case get4s:
				jsonDealAdress(msg.obj.toString());
				adressAdapter.notifyDataSetChanged();
				break;
			}
		}

	};

	private void jsonDealAdress(String result) {
		try {
			LatLngBounds.Builder builder = new Builder();
			JSONArray jsonArray = new JSONArray(result);
			for (int i = 0; i < jsonArray.length(); i++) {// TODO
				if (i == 10) {
					// 只显示10个
					break;
				}
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				AdressData adressData = new AdressData();
				adressData.setAdress(jsonObject.getString("address"));
				adressData.setName(jsonObject.getString("name"));
				adressData.setPhone(jsonObject.getString("tel"));
				adressData.setLat(jsonObject.getDouble("lat"));
				adressData.setLon(jsonObject.getDouble("lon"));
				adressData.setDistance(jsonObject.getInt("distance"));
				if (jsonObject.getString("is_collect").equals("1")) {
					// 收藏
					adressData.setIs_collect(true);
				} else {
					// 未收藏
					adressData.setIs_collect(false);
				}
				adressDatas.add(adressData);
				LatLng latLng = new LatLng(adressDatas.get(i).getLat(),
						adressDatas.get(i).getLon());
				OverlayOptions marker;
				// 图片名称
				String imagePath = "Icon_mark" + (i + 1) + ".png";
				try {
					// 百度jar包里的图片
					Bitmap bitmap = BitmapFactory
							.decodeStream(SearchMapActivity.this.getAssets()
									.open(imagePath));
					marker = new MarkerOptions().position(latLng).icon(
							BitmapDescriptorFactory.fromBitmap(bitmap));
				} catch (IOException e) {
					e.printStackTrace();
					marker = new MarkerOptions().position(latLng).icon(
							BitmapDescriptorFactory
									.fromResource(R.drawable.body_icon_outset));
				}
				mBaiduMap.addOverlay(marker);
				builder.include(latLng);
			}
			LatLngBounds bounds = builder.build();
			MapStatusUpdate u1 = MapStatusUpdateFactory.newLatLngBounds(bounds);
			mBaiduMap.animateMapStatus(u1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解析返回的数据
	 * 
	 * @param result
	 */
	private void jsonCollect(String result) {
		try {
			JSONArray jsonArray = new JSONArray(result);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String name = jsonObject.getString("name");
				for (int j = 0; j < adressDatas.size(); j++) {
					if (adressDatas.get(j).getName().equals(name)) {
						adressDatas.get(j).setIs_collect(true);
						break;
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
					.newLatLng(new LatLng(adressDatas.get(arg2).getLat(),
							adressDatas.get(arg2).getLon()));
			mBaiduMap.setMapStatus(mapStatusUpdate);
		}
	};

	OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener() {
		@Override
		public void onGetPoiResult(PoiResult result) {

			if (result == null
					|| result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
				Toast.makeText(SearchMapActivity.this, "抱歉，没找到结果",
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
					adressDatas.add(adressData);
					str = str + mkPoiInfo.name + ",";
				}
				// Collections.sort(adressDatas, new Comparator());// 排序
				adressAdapter.notifyDataSetChanged();
				// 判断是否收藏
				String url;
				try {
					url = Constant.BaseUrl + "favorite/is_collect?auth_code="
							+ app.auth_code + "&names="
							+ URLEncoder.encode(str, "UTF-8") + "&cust_id="
							+ app.cust_id;
					new Thread(new NetThread.GetDataThread(handler, url,
							getIsCollect)).start();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
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
				Toast.makeText(SearchMapActivity.this, strInfo,
						Toast.LENGTH_LONG).show();
			}

		}

		@Override
		public void onGetPoiDetailResult(PoiDetailResult result) {
			if (result.error != SearchResult.ERRORNO.NO_ERROR) {
				Toast.makeText(SearchMapActivity.this, "抱歉，未找到结果",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(SearchMapActivity.this,
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

	Marker phoneMark;
	private class MyLocationListenner implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null || mMapView == null)
				return;
			//如果有当前位置，则先删除
			if(phoneMark != null){
				phoneMark.remove();
			}
			LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
			// 构建Marker图标
			BitmapDescriptor bitmap = BitmapDescriptorFactory
					.fromResource(R.drawable.person);
			// 构建MarkerOption，用于在地图上添加Marker
			OverlayOptions option = new MarkerOptions().anchor(0.5f, 1.0f)
					.position(latLng).icon(bitmap);
			// 在地图上添加Marker，并显示
			phoneMark = (Marker) (mBaiduMap.addOverlay(option));
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
		mPoiSearch.destroy();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		//mMapView.onDestroy();
		mMapView = null;
	}
}
