package com.wise.car;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.entity.AdressData;

public class ChooseAddressActivity extends Activity {
	private AppApplication app;
	private PoiSearch mPoiSearch = null;
	private EditText tv_search;
	TextView map_choose, collection_choose;
	// 定位相关
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();

	ChooseAdapter chooseAdapter = null;
	ListView my_location;
	List<AdressData> adressDatas = new ArrayList<AdressData>();

	public static final int ADDRESSCODE = 1;
	public static final int MAPCHOOSE = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_choose_address);
		app = (AppApplication) getApplication();

		AdressData myAdress = new AdressData();
		myAdress.setIcon(R.drawable.toolbar_icon_search);
		myAdress.setAdress("我的位置");
		adressDatas.add(myAdress);

		tv_search = (EditText) findViewById(R.id.tv_search);

		mPoiSearch = PoiSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(poiListener);

		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setIsNeedAddress(true);
		option.setCoorType("all");
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(1000);
		mLocClient.setLocOption(option);
		mLocClient.start();

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
					// 搜索关键字
					mPoiSearch.searchInCity((new PoiCitySearchOption()).city(
							app.City).keyword(s.toString()));
				} else {
					adressDatas.clear();
					AdressData myAdress = new AdressData();
					myAdress.setIcon(R.drawable.toolbar_icon_search);
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
					i.putExtra("name", myLocation);
					i.putExtra("latitude", latitude);
					i.putExtra("longitude", longitude);
					i.putExtra("myLoct", true);
				} else {
					i.putExtra("name", adressDatas.get(position).getName());
					i.putExtra("latitude", adressDatas.get(position).getLat());
					i.putExtra("longitude", adressDatas.get(position).getLon());
					i.putExtra("myLoct", false);
				}
				setResult(ADDRESSCODE, i);
				finish();
			}
		});

		findViewById(R.id.iv_back).setOnClickListener(onClickListener);
		map_choose = (TextView) findViewById(R.id.map_choose);
		map_choose.setOnClickListener(onClickListener);
		collection_choose = (TextView) findViewById(R.id.collection_choose);
		collection_choose.setOnClickListener(onClickListener);

	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.map_choose:
				Intent i = new Intent(ChooseAddressActivity.this,
						MapChooseActivity.class);
				startActivityForResult(i, MAPCHOOSE);
				break;
			case R.id.collection_choose:

				break;
			}
		}
	};

	String myLocation = "";
	double latitude = 0;
	double longitude = 0;

	private class MyLocationListenner implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			System.out.println("onReceiveLocation");
			if (location == null) {
				return;
			}
			System.out.println("location.getLocType() = "
					+ location.getLocType());
			if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
				myLocation = location.getAddrStr();
				latitude = location.getLatitude();
				longitude = location.getLongitude();
			} else {
				myLocation = "";
			}
			mLocClient.stop();
			mLocClient.unRegisterLocationListener(myListener);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == MapChooseActivity.MAPPOINT) {
			String name = data.getExtras().getString("name");
			Intent i = new Intent();
			i.putExtra("name", name);
			i.putExtra("latitude", data.getExtras().getDouble("latitude"));
			i.putExtra("longitude", data.getExtras().getDouble("longitude"));
			i.putExtra("myLoct", false);
			setResult(ADDRESSCODE, i);
			finish();
		}
	}

	OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener() {
		@Override
		public void onGetPoiResult(PoiResult result) {
			if (result == null
					|| result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
				Toast.makeText(ChooseAddressActivity.this, "抱歉，未找到结果",
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (result.error == SearchResult.ERRORNO.NO_ERROR) {
				adressDatas.clear();
				AdressData myAdress = new AdressData();
				myAdress.setIcon(R.drawable.toolbar_icon_search);
				myAdress.setAdress("我的位置");
				adressDatas.add(myAdress);
				PoiInfo mkPoiInfo = null;
				String adName = "";
				for (int i = 0; i < result.getAllPoi().size(); i++) {
					mkPoiInfo = result.getAllPoi().get(i);
					AdressData adressData = new AdressData();
					adName = mkPoiInfo.name + "\n" + mkPoiInfo.address;
					adressData.setAdress(adName);
					adressData.setName(mkPoiInfo.name);
					adressData.setLat(mkPoiInfo.location.latitude);
					adressData.setLon(mkPoiInfo.location.longitude);
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

	protected void onDestroy() {
		super.onDestroy();
		mPoiSearch.destroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}
