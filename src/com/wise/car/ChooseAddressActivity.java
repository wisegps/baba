package com.wise.car;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
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
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.wise.baba.AppApplication;
import com.wise.baba.R;

import data.AdressData;

public class ChooseAddressActivity extends Activity {
	private AppApplication app;
	private PoiSearch mPoiSearch = null;
	private MapView mMapView = null;
	private BaiduMap mBaiduMap = null;
	private EditText tv_search;
	private LinearLayout linear_choose, show_choose_map;
	private GeoCoder mGeoCoder = null;

	ChooseAdapter chooseAdapter = null;
	ListView my_location;
	List<AdressData> adressDatas = new ArrayList<AdressData>();

	public static final int ADDRESSCODE = 1;
	public static final int MYCODE = 2;

	// 定位相关
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_choose_address);
		app = (AppApplication) getApplication();
		mGeoCoder = GeoCoder.newInstance();
		mGeoCoder.setOnGetGeoCodeResultListener(onCoderResultListener);

		AdressData myAdress = new AdressData();
		myAdress.setIcon(R.drawable.icon_place);
		myAdress.setAdress("我的位置");
		adressDatas.add(myAdress);

		tv_search = (EditText) findViewById(R.id.tv_search);
		mMapView = (MapView) findViewById(R.id.choose_map);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(12));
		mPoiSearch = PoiSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(poiListener);
		mBaiduMap.setOnMapClickListener(listener);

		tv_search.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (s != null && !(s.toString()).equals("")) {
					adressDatas.clear();
					// 搜索关键字
					mPoiSearch.searchInCity((new PoiCitySearchOption()).city(
							app.City).keyword(s.toString()));
				} else {
					adressDatas.clear();
					AdressData myAdress = new AdressData();
					myAdress.setIcon(R.drawable.icon_place);
					myAdress.setAdress("我的位置");
					adressDatas.add(myAdress);
					chooseAdapter.notifyDataSetChanged();
				}
			}

		});

		my_location = (ListView) findViewById(R.id.my_location);
		chooseAdapter = new ChooseAdapter();
		my_location.setAdapter(chooseAdapter);

		my_location.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent i = new Intent();
				if (position == 0) {
					i.putExtra("name", app.Adress);
				} else {
					i.putExtra("name", adressDatas.get(position).getName());
				}
				setResult(ADDRESSCODE, i);
				finish();
			}
		});

		linear_choose = (LinearLayout) findViewById(R.id.linear_choose);
		show_choose_map = (LinearLayout) findViewById(R.id.show_choose_map);

		findViewById(R.id.iv_back).setOnClickListener(onClickListener);
		findViewById(R.id.map_choose).setOnClickListener(onClickListener);
		findViewById(R.id.collection_choose)
				.setOnClickListener(onClickListener);
		findViewById(R.id.iv_back_choose).setOnClickListener(onClickListener);

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
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.map_choose:
				linear_choose.setVisibility(View.GONE);
				show_choose_map.setVisibility(View.VISIBLE);
				break;
			case R.id.collection_choose:

				break;
			case R.id.iv_back_choose:
				linear_choose.setVisibility(View.VISIBLE);
				show_choose_map.setVisibility(View.GONE);
				break;

			}
		}
	};

	String name = "";
	OnMapClickListener listener = new OnMapClickListener() {
		@Override
		/** 
		 * 地图单击事件回调函数 
		 * @param point 点击的地理坐标 
		 */
		public void onMapClick(LatLng point) {
			mGeoCoder
					.reverseGeoCode(new ReverseGeoCodeOption().location(point));
			Button button = new Button(getApplicationContext());
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent();
					i.putExtra("name", name);
					setResult(ADDRESSCODE, i);
					finish();
				}
			});
			button.setBackgroundResource(R.drawable.popup);
			button.setText("点击选择地址");
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

	OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener() {
		@Override
		public void onGetPoiResult(PoiResult result) {
			if (result == null
					|| result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
				Toast.makeText(ChooseAddressActivity.this, "抱歉，没找到结果",
						Toast.LENGTH_SHORT).show();
				return;
			}

			if (result.error == SearchResult.ERRORNO.NO_ERROR) {
				AdressData myAdress = new AdressData();
				myAdress.setIcon(R.drawable.icon_place);
				myAdress.setAdress("我的位置");
				adressDatas.add(myAdress);
				PoiOverlay overlay = new PoiOverlay(mBaiduMap);
				mBaiduMap.setOnMarkerClickListener(overlay);
				overlay.setData(result);
				overlay.addToMap();
				overlay.zoomToSpan();
				PoiInfo mkPoiInfo = null;
				String adName = "";
				for (int i = 0; i < result.getAllPoi().size(); i++) {
					mkPoiInfo = result.getAllPoi().get(i);
					AdressData adressData = new AdressData();
					adName = mkPoiInfo.name + "\n" + mkPoiInfo.address;
					adressData.setAdress(adName);
					adressData.setName(mkPoiInfo.name);
					adressData.setIcon(R.drawable.toolbar_icon_search);
					adressDatas.add(adressData);
				}
				chooseAdapter.notifyDataSetChanged();
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
				Toast.makeText(ChooseAddressActivity.this, strInfo,
						Toast.LENGTH_LONG).show();
			}
		}

		@Override
		public void onGetPoiDetailResult(PoiDetailResult result) {
			if (result.error != SearchResult.ERRORNO.NO_ERROR) {
				Toast.makeText(ChooseAddressActivity.this, "抱歉，未找到结果",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(ChooseAddressActivity.this,
						result.getName() + ": " + result.getAddress(),
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	class ChooseAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return adressDatas.size();
		}

		@Override
		public Object getItem(int position) {
			return adressDatas.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder mHolder = null;
			if (convertView == null) {
				convertView = (LayoutInflater.from(ChooseAddressActivity.this))
						.inflate(R.layout.item_choose_address, null);
				mHolder = new Holder();
				mHolder.icon = (ImageView) convertView
						.findViewById(R.id.item_icon);
				mHolder.textView = (TextView) convertView
						.findViewById(R.id.item_address);
				convertView.setTag(mHolder);
			} else {
				mHolder = (Holder) convertView.getTag();
			}
			if (adressDatas != null && adressDatas.size() != 0) {
				mHolder.icon.setImageResource(adressDatas.get(position)
						.getIcon());
				mHolder.textView.setText(adressDatas.get(position).getAdress());
			}
			return convertView;
		}

		class Holder {
			ImageView icon;
			TextView textView;
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

	protected void onDestroy() {
		super.onDestroy();
		mLocClient.stop();
		mPoiSearch.destroy();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mGeoCoder.destroy();
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
