package com.wise.car;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.ListView;

import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.ui.adapter.ListSearchAdapter;

public class SearchLocationActivity extends Activity {
	private AppApplication app;
	private PoiSearch mPoiSearch = null;
	private SuggestionSearch sugSearch = null;
	private ListView listSearch;
	private ListSearchAdapter listSearchAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_search_location);
		app = (AppApplication) getApplication();
		mPoiSearch = PoiSearch.newInstance();
		sugSearch = SuggestionSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(null);
		sugSearch.setOnGetSuggestionResultListener(null);
		initView();
	}
	
	
	public void initView(){
		listSearch = (ListView) findViewById(R.id.lv_search);
		listSearchAdapter= new ListSearchAdapter(this);
		listSearch.setAdapter(listSearchAdapter);
		listSearchAdapter.notifyDataSetChanged();
		
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


}
