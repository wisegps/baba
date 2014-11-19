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

import data.AdressData;

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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SearchLocationActivity extends Activity {
	private AppApplication app;
	private EditText ed_search;
	private ListView search_history;
	List<AdressData> adressDatas = new ArrayList<AdressData>();
	List<AdressData> historyDatas = new ArrayList<AdressData>();
	SearchAdapter searchAdapter = null;
	private PoiSearch mPoiSearch = null;

	public static final int HISTORY_CODE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_search_location);
		app = (AppApplication) getApplication();
		mPoiSearch = PoiSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(poiListener);

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
		search_history = (ListView) findViewById(R.id.search_history);
		SharedPreferences preferences = getSharedPreferences("history_search",
				Activity.MODE_PRIVATE);
		String historyJson = preferences.getString("historyJson", "");
		getJsonData(historyJson);
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
				SharedPreferences preferences = getSharedPreferences(
						"history_search", Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = preferences.edit();
				Intent i = new Intent();
				JSONObject historyJson = new JSONObject();
				JSONArray historyArray = new JSONArray();
				if (adressDatas.size() == 0 || adressDatas == null) {
					i.putExtra("history_lat", historyDatas.get(position)
							.getLat());
					i.putExtra("history_lon", historyDatas.get(position)
							.getLon());
					i.putExtra("re_name", historyDatas.get(position).getName());
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
					i.putExtra("history_lat", adressDatas.get(position)
							.getLat());
					i.putExtra("history_lon", adressDatas.get(position)
							.getLon());
					i.putExtra("re_name", adressDatas.get(position).getName());
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
				setResult(HISTORY_CODE, i);
				finish();
			}
		});

		findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent();
				i.putExtra("re_name", "");
				setResult(HISTORY_CODE, i);
				finish();
			}
		});
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

	OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener() {
		@Override
		public void onGetPoiResult(PoiResult result) {
			if (result == null
					|| result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
				search_history.setVisibility(View.GONE);
				return;
			}

			if (result.error == SearchResult.ERRORNO.NO_ERROR) {
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
			return show.get(position);
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
