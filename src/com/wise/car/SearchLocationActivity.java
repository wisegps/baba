package com.wise.car;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionResult.SuggestionInfo;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Const;
import com.wise.baba.db.dao.DaoMaster;
import com.wise.baba.db.dao.DaoMaster.DevOpenHelper;
import com.wise.baba.db.dao.DaoSession;
import com.wise.baba.db.dao.Suggestion;
import com.wise.baba.db.dao.SuggestionDao;
import com.wise.baba.db.dao.SuggestionDao.Properties;
import com.wise.baba.ui.adapter.ListSearchAdapter;

import de.greenrobot.dao.Property;
import de.greenrobot.dao.query.DeleteQuery;
import de.greenrobot.dao.query.QueryBuilder;

/**
 * 
 * 
 * @author c
 * @desc 百度地图搜索
 * @date 2015-6-4
 * 
 */
public class SearchLocationActivity extends Activity implements TextWatcher,
		OnGetPoiSearchResultListener, OnGetSuggestionResultListener,
		OnItemClickListener {
	private AppApplication app;
	private PoiSearch mPoiSearch = null;
	private SuggestionSearch sugSearch = null;
	private ListView lvSearch;
	private ListSearchAdapter listSearchAdapter;
	private EditText etSearch = null;

	private List<Suggestion> searchList = null;

	private DevOpenHelper helper = null;
	private SQLiteDatabase db = null;
	private DaoMaster daoMaster = null;

	private DaoSession daoSession = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_search_location);
		app = (AppApplication) getApplication();
		init();
	}

	/**
	 * 初始化
	 */
	public void init() {
		/*
		 * 先初始化百度搜索
		 */
		mPoiSearch = PoiSearch.newInstance();
		sugSearch = SuggestionSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(this);
		sugSearch.setOnGetSuggestionResultListener(this);

		/*
		 * 初始化数据库
		 */
		helper = new DaoMaster.DevOpenHelper(this, "history-db", null);
		db = helper.getWritableDatabase();
		daoMaster = new DaoMaster(db);
		daoSession = daoMaster.newSession();

		/*
		 * 再初始化界面控件
		 */
		searchList = new ArrayList<Suggestion>();
		lvSearch = (ListView) findViewById(R.id.lv_search);
		etSearch = (EditText) findViewById(R.id.et_search);

		/*
		 * 列表加载历史数据
		 */
		listSearchAdapter = new ListSearchAdapter(this);
		lvSearch.setAdapter(listSearchAdapter);
		listSearchAdapter.notifyDataSetChanged();
		loadHistory();
		// 设监听
		etSearch.addTextChangedListener(this);
		lvSearch.setOnItemClickListener(this);

		findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SearchLocationActivity.this.finish();
			}
		});

	}

	/**
	 * 
	 */
	public void notifyDataSetChanged() {
		listSearchAdapter.setData(searchList);
		listSearchAdapter.notifyDataSetChanged();
	}

	/**
	 * @desc 跳到地图界面，显示兴趣点
	 * @param history_lat
	 * @param history_lon
	 * @param re_name
	 */
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

	/**
	 * 文本框监听
	 */
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		String text = s.toString();
		// 为空就加载历史
		if (text == null || text.length() <= 0) {
			loadHistory();
			return;
		}

		SuggestionSearchOption sugOption = new SuggestionSearchOption();
		sugOption.keyword(text);
		sugOption.city(app.City);
		sugSearch.requestSuggestion(sugOption);

	}

	/**
	 * POI搜索返回
	 */
	@Override
	public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

	}

	@Override
	public void onGetPoiResult(PoiResult poiResult) {

		List<PoiInfo> poiList = poiResult.getAllPoi();
		if (poiList == null || poiList.size() == 0) {
			Toast.makeText(this, "找不到结果", Toast.LENGTH_SHORT).show();
			return;
		}

		PoiInfo poi = poiList.get(0);
		intentToMap(poi.location.latitude, poi.location.longitude, poi.name);

	}

	/**
	 * 搜索返回建议
	 */
	@Override
	public void onGetSuggestionResult(SuggestionResult suggestionResult) {
		List<SuggestionInfo> allSuggestions = suggestionResult
				.getAllSuggestions();
		if (allSuggestions == null || allSuggestions.size() == 0) {
			Toast.makeText(this, "找不到结果", Toast.LENGTH_SHORT).show();
			return;
		}

		this.searchList.clear();
		Iterator it = allSuggestions.iterator();
		while (it.hasNext()) {
			SuggestionInfo info = (SuggestionInfo) it.next();
			Suggestion suggestion = new Suggestion();
			suggestion.setType(Const.Type_Suggestion);
			suggestion.setKey(info.key);
			suggestion.setCity(info.city);
			suggestion.setDistrict(info.district);
			searchList.add(suggestion);
		}
		notifyDataSetChanged();

	}

	/**
	 * 点击搜索列表
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		Suggestion suggestion = (Suggestion) listSearchAdapter
				.getItem(position);

		if (suggestion.getType() == Const.Type_Clear_History) {
			clearHistory();
			return;
		}
		String city = suggestion.getCity();

		// 先保存道数据库

		save2db(suggestion);

		// 去别的城市搜索
		if (city != null && !city.equals("") && !city.equals(app.City)) {
			PoiCitySearchOption poiOption = new PoiCitySearchOption();
			poiOption.keyword(suggestion.getKey());
			poiOption.city(city);
			poiOption.pageNum(0);
			mPoiSearch.searchInCity(poiOption);
			return;
		}

		// 调用附近搜索
		PoiNearbySearchOption nearByOption = new PoiNearbySearchOption();
		nearByOption.keyword(suggestion.getKey());
		nearByOption.location(new LatLng(app.Lat, app.Lon));
		nearByOption.sortType(PoiSortType.distance_from_near_to_far);
		nearByOption.radius(1000000000);
		mPoiSearch.searchNearby(nearByOption);
	}

	/**
	 * 清空历史记录
	 */
	private void clearHistory() {
		SuggestionDao suggestionDao = daoSession.getSuggestionDao();
		suggestionDao.deleteAll();
		loadHistory();
	}

	/**
	 * 历史搜索记录存进数据库
	 */
	public void save2db(Suggestion suggestion) {
		suggestion.setType(Const.Type_History);
		SuggestionDao suggestionDao = daoSession.getSuggestionDao();
		
		QueryBuilder<Suggestion> qb = suggestionDao.queryBuilder();
		qb.where(Properties.Key.eq(suggestion.getKey()),
				Properties.City.eq(suggestion.getCity()),
				Properties.District.eq(suggestion.getDistrict()));
		if (qb.list().size() > 0) {
			// 已经存在就删除
			DeleteQuery<Suggestion> bd = qb.buildDelete();
			bd.executeDeleteWithoutDetachingEntities();
		}
		suggestion.setDate(new Date(System.currentTimeMillis()));
		suggestionDao.insert(suggestion);
	}

	/**
	 * 加载数据库中的内容
	 */
	public void loadHistory() {
		SuggestionDao suggestionDao = daoSession.getSuggestionDao();
		List<Suggestion> suggestionList = suggestionDao.queryBuilder()
				.orderDesc(Properties.Date).list();
		// 最后加上清空历史记录
		if (suggestionList.size() > 0) {
			Suggestion clearSuggestion = new Suggestion();
			clearSuggestion.setType(Const.Type_Clear_History);
			suggestionList.add(clearSuggestion);
		}
		listSearchAdapter.setHistory(suggestionList);
		listSearchAdapter.notifyDataSetChanged();
	}

}
