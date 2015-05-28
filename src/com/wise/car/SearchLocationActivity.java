package com.wise.car;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.entity.AdressData;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SearchLocationActivity extends Activity {
	private AppApplication app;
	private EditText ed_search;
	private ListView search_history;
	private ListView oftenAdress;
	LinearLayout ll_oftenAddress;
	List<AdressData> adressDatas = new ArrayList<AdressData>();
	List<AdressData> historyDatas = new ArrayList<AdressData>();
	List<AdressData> oftenDatas = new ArrayList<AdressData>();
	SearchAdapter searchAdapter = null;
	SearchAdapter oftenAdapter = null;
	private PoiSearch mPoiSearch = null;

	public static final int HISTORY_CODE = 1;

	// private String POI_FLAG = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_search_location);
		app = (AppApplication) getApplication();
		mPoiSearch = PoiSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(poiListener);

		// POI_FLAG = this.getIntent().getStringExtra("POI_FLAG");

		ed_search = (EditText) findViewById(R.id.ed_search);
		ed_search.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (s != null && !(s.toString()).equals("")) {
					// 搜索关键字
					mPoiSearch.searchNearby(new PoiNearbySearchOption()
							.radius(1000000000).keyword(s.toString())
							.location(new LatLng(app.Lat, app.Lon)));
				} else {
					adressDatas.clear();
					if (historyDatas.size() == 0 || historyDatas == null) {
						search_history.setVisibility(View.GONE);
					} else {
						search_history.setVisibility(View.VISIBLE);
					}
					searchAdapter.setDate(historyDatas);
					searchAdapter.notifyDataSetChanged();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		ll_oftenAddress = (LinearLayout) findViewById(R.id.ll_oftenAddress);
		oftenAdress = (ListView) findViewById(R.id.list_often_adres);
		oftenAdapter = new SearchAdapter();
		getOftenJsonData();
		oftenAdress.setAdapter(oftenAdapter);

		// 常用地址监听
		oftenAdress.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Intent i = new Intent();
				// i.putExtra("history_lat", oftenDatas.get(position).getLat());
				// i.putExtra("history_lon", oftenDatas.get(position).getLon());
				// i.putExtra("re_name", oftenDatas.get(position).getAdress());
				// setResult(HISTORY_CODE, i);
				// finish();

				Log.i("SearchLocationActivity", "点击常用地址");
				Double lat = oftenDatas.get(position).getLat();
				Double lon = oftenDatas.get(position).getLon();
				String address = oftenDatas.get(position).getAdress();

				Log.i("SearchLocationActivity", "点击常用地址" + address);
				intentToMap(lat, lon, address);

			}
		});

		search_history = (ListView) findViewById(R.id.search_history);
		SharedPreferences preferences = getSharedPreferences("history_search",
				Activity.MODE_PRIVATE);
		String historyJson = preferences.getString("historyJson", "");
		if (historyJson != null && !historyJson.equals("")) {
			getJsonData(historyJson);
		}
		if (historyDatas.size() == 0 || historyDatas == null) {
			search_history.setVisibility(View.GONE);
		} else {
			search_history.setVisibility(View.VISIBLE);
		}
		searchAdapter = new SearchAdapter();
		searchAdapter.setDate(historyDatas);
		search_history.setAdapter(searchAdapter);

		search_history.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Log.i("SearchLocationActivity", "点击历史数据");
				Double intentLat = 0.0;
				Double intentLon = 0.0;
				String intentAddress = null;

				SharedPreferences preferences = getSharedPreferences(
						"history_search", Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = preferences.edit();
				Intent i = new Intent();
				JSONObject historyJson = new JSONObject();
				JSONArray historyArray = new JSONArray();
				if (adressDatas.size() == 0 || adressDatas == null) {

					intentLat = historyDatas.get(position).getLat();
					intentLon = historyDatas.get(position).getLon();
					intentAddress = historyDatas.get(position).getAdress();
					Log.i("SearchLocationActivity", "历史数据：" + intentAddress);

					AdressData data_1 = historyDatas.get(position);
					historyDatas.remove(position);
					historyDatas.add(0, data_1);
					try {
						for (AdressData data : historyDatas) {
							JSONObject adressData = new JSONObject();
							adressData.put("adName", data.getAdress());
							adressData.put("name", data.getName());
							adressData.put("history_lat", data.getLat());
							adressData.put("history_lon", data.getLon());
							historyArray.put(adressData);
						}
						historyJson.put("historyJson", historyArray);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {

					intentLat = adressDatas.get(position).getLat();
					intentLon = adressDatas.get(position).getLon();
					intentAddress = adressDatas.get(position).getName();
					Log.i("SearchLocationActivity", "点击搜索纪录：" + intentAddress);
					try {
						if (historyDatas.size() == 0 || historyDatas == null) {
							JSONObject jsonObject = new JSONObject();
							jsonObject.put("adName", adressDatas.get(position)
									.getAdress());
							jsonObject.put("name", adressDatas.get(position)
									.getName());
							jsonObject.put("history_lat",
									adressDatas.get(position).getLat());
							jsonObject.put("history_lon",
									adressDatas.get(position).getLon());
							historyArray.put(jsonObject);
						} else {
							int one = 0;
							boolean flag = false;
							for (int j = 0; j < historyDatas.size(); j++) {
								if (historyDatas
										.get(j)
										.getAdress()
										.equals(adressDatas.get(position)
												.getAdress())) {
									flag = true;
									one = j;
									break;
								}
							}
							if (flag) {
								AdressData data_1 = historyDatas.get(one);
								historyDatas.remove(one);
								historyDatas.add(0, data_1);
							} else {
								JSONObject object = new JSONObject();
								object.put("adName", adressDatas.get(position)
										.getAdress());
								object.put("name", adressDatas.get(position)
										.getName());
								object.put("history_lat",
										adressDatas.get(position).getLat());
								object.put("history_lon",
										adressDatas.get(position).getLon());
								historyArray.put(object);
							}
							for (AdressData data : historyDatas) {
								JSONObject adressData = new JSONObject();
								adressData.put("adName", data.getAdress());
								adressData.put("name", data.getName());
								adressData.put("history_lat", data.getLat());
								adressData.put("history_lon", data.getLon());
								historyArray.put(adressData);
							}
						}
						historyJson.put("historyJson", historyArray);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				editor.putString("historyJson", historyJson.toString());

				editor.commit();
				intentToMap(intentLat, intentLon, intentAddress);
			}
		});

		findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Intent i = new Intent();
				// i.putExtra("re_name", "");
				// setResult(HISTORY_CODE, i);
				finish();
			}
		});
	}

	private void intentToMap(double history_lat, double history_lon,
			String re_name) {

		

		Intent intent = new Intent(this, CarLocationActivity.class);
		intent.putExtra("history_lat", history_lat);
		intent.putExtra("history_lon", history_lon);
		intent.putExtra("re_name", re_name);
		intent.putExtra("isHotLocation", true);
		startActivity(intent);
		this.finish();
	}

	private void getOftenJsonData() {
		SharedPreferences preferences = getSharedPreferences("address_add",
				Activity.MODE_PRIVATE);
		String addJsonArray = preferences.getString("addJsonArray", "");
		if (addJsonArray == null || addJsonArray.equals("")) {
			ll_oftenAddress.setVisibility(View.GONE);
			oftenAdapter.setDate(oftenDatas);
		} else {
			ll_oftenAddress.setVisibility(View.VISIBLE);
			try {
				JSONObject jsonObject = new JSONObject(addJsonArray);
				JSONArray jsonArray = jsonObject.getJSONArray("addJsonArray");
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject object = jsonArray.getJSONObject(i);
					String addressName = object.getString("addressName");
					double addressLat = object.getDouble("addressLat");
					double addressLon = object.getDouble("addressLon");
					AdressData adressData = new AdressData();
					adressData.setAdress(addressName);
					adressData.setLat(addressLat);
					adressData.setLon(addressLon);
					oftenDatas.add(adressData);
				}
				oftenAdapter.setDate(oftenDatas);
				oftenAdapter.notifyDataSetChanged();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private void getJsonData(String str) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			JSONArray jsonArray = jsonObject.getJSONArray("historyJson");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject object = jsonArray.getJSONObject(i);
				String name = object.getString("name");
				String adName = object.getString("adName");
				double history_lat = object.getDouble("history_lat");
				double history_lon = object.getDouble("history_lon");
				AdressData adressData = new AdressData();
				adressData.setAdress(adName);
				adressData.setName(name);
				adressData.setLat(history_lat);
				adressData.setLon(history_lon);
				historyDatas.add(adressData);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 搜索返回
	 */
	OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener() {
		@Override
		public void onGetPoiResult(PoiResult result) {
			try {

				// 返回空，历史搜索框隐藏
				if (result == null
						|| result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
					search_history.setVisibility(View.GONE);
					return;
				}

				// 返回有数据，显示历史搜索框

				// adressDatas 清空，重新设置搜索到的数据
				if (result.error == SearchResult.ERRORNO.NO_ERROR) {

					Log.i("SearchLocationActivity", "搜索返回 ");
					search_history.setVisibility(View.VISIBLE);
					adressDatas.clear();
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
						adressDatas.add(adressData);
					}
					searchAdapter.setDate(adressDatas);
					searchAdapter.notifyDataSetChanged();
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onGetPoiDetailResult(PoiDetailResult result) {
			if (result.error != SearchResult.ERRORNO.NO_ERROR) {
				Toast.makeText(SearchLocationActivity.this, "抱歉，未找到结果",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(SearchLocationActivity.this,
						result.getName() + ": " + result.getAddress(),
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	class SearchAdapter extends BaseAdapter {
		private List<AdressData> show = null;

		public void setDate(List<AdressData> list) {
			this.show = list;
		}

		@Override
		public int getCount() {
			return show.size();
		}

		@Override
		public Object getItem(int position) {
			return show != null ? show.get(position) : null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder mHolder = null;
			if (convertView == null) {
				convertView = (LayoutInflater.from(SearchLocationActivity.this))
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
			if (show != null && show.size() != 0) {
				// mHolder.icon.setImageResource(show.get(position).getIcon());
				mHolder.textView.setText(show.get(position).getAdress());
			}
			return convertView;
		}

		class Holder {
			ImageView icon;
			TextView textView;
		}
	}

}
