/**
 * 
 */
package com.wise.baba.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import com.wise.baba.R;
import com.wise.baba.entity.Suggestion;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 *
 * @author c
 * @desc   baba
 * @date   2015-6-4
 *
 */
public class ListSearchAdapter extends BaseAdapter {

	
	private Context context;
	private List<Suggestion> searchList = new ArrayList<Suggestion>();
	
	public ListSearchAdapter(Context context) {
		super();
		this.context = context;
	}

	
	@Override
	public int getCount() {
		return searchList.size();
	}

	@Override
	public Object getItem(int position) {
		return searchList.get(position);
	}
	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_list_map_search, null);
		}
		
		ImageView ivSearch = (ImageView) convertView.findViewById(R.id.iv_search);
		TextView tvKey = (TextView) convertView.findViewById(R.id.tv_key);
		TextView tvCity = (TextView) convertView.findViewById(R.id.tv_city);
		
		TextView tvOneLine = (TextView) convertView.findViewById(R.id.tv_one_line);
		
		Suggestion  suggestion = searchList.get(position);
		
		//历史搜索还是新搜索的
		if(suggestion.getType() == Suggestion.Type_History){
			ivSearch.setImageResource(R.drawable.icon_poi_history);
		}else{
			ivSearch.setImageResource(R.drawable.icon_search);
		}
		
		tvKey.setText("");
		//是一行还是二行的内容
		if(suggestion.getCity()==null || suggestion.getCity().equals("")){
			tvOneLine.setText(suggestion.getKey());
			tvKey.setText("");
			tvCity.setText("");
		}else{
			tvKey.setText(suggestion.getKey());
			tvCity.setText(suggestion.getCity() + suggestion.getDistrict());
			tvOneLine.setText("");
		}
		
		
		return convertView;
	}


	/**
	 * @param searchList
	 */
	public void setData(List<Suggestion> searchList) {
		this.searchList = searchList;
	}

}
