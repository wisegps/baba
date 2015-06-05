/**
 * 
 */
package com.wise.baba.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import com.wise.baba.R;
import com.wise.baba.app.Const;
import com.wise.baba.db.dao.Suggestion;

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
 * @desc baba
 * @date 2015-6-4
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

	/**
	 * 正常情况是搜索建议两行显示，其它情况需要调整布局控件显示
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_list_map_search, null);
		}
		Holder holder = (Holder) convertView.getTag();
		if (holder == null) {
			initHoler(convertView);
			holder = (Holder) convertView.getTag();
		}

		resetItem(holder);

		Suggestion suggestion = searchList.get(position);

		// 一行还是二行的内容
		if (suggestion.getCity() == null || suggestion.getCity().equals("")) {
			holder.tvOneLine.setText(suggestion.getKey());
		} else {
			holder.tvKey.setText(suggestion.getKey());
			holder.tvCity.setText(suggestion.getCity()
					+ suggestion.getDistrict());
		}

		// 历史搜索还是清空理历史
		if (suggestion.getType() == Const.Type_History) {
			holder.ivSearch.setImageResource(R.drawable.icon_poi_history);
		} else if (suggestion.getType() == Const.Type_Clear_History) {
			holder.ivSearch.setVisibility(View.INVISIBLE);
			holder.ivRetrieval.setVisibility(View.INVISIBLE);
			holder.tvClearHistory.setText("清空历史记录      ");
		}

		return convertView;
	}

	public void initHoler(View convertView) {
		ImageView ivSearch = (ImageView) convertView
				.findViewById(R.id.iv_search);
		TextView tvKey = (TextView) convertView.findViewById(R.id.tv_key);
		TextView tvCity = (TextView) convertView.findViewById(R.id.tv_city);

		TextView tvOneLine = (TextView) convertView
				.findViewById(R.id.tv_one_line);
		TextView tvClearHistory = (TextView) convertView
				.findViewById(R.id.tv_clear_history);
		ImageView ivRetrieval = (ImageView) convertView
				.findViewById(R.id.iv_retrieval);

		Holder holder = new Holder();
		holder.ivSearch = ivSearch;
		holder.tvKey = tvKey;
		holder.tvCity = tvCity;
		holder.tvOneLine = tvOneLine;
		holder.tvClearHistory = tvClearHistory;
		holder.ivRetrieval = ivRetrieval;

		convertView.setTag(holder);
	}

	/**
	 * 重置内容显示
	 */
	public void resetItem(Holder holder) {
		holder.ivSearch.setVisibility(View.VISIBLE);
		holder.ivSearch.setImageResource(R.drawable.icon_search);
		holder.tvKey.setText("");
		holder.tvCity.setText("");
		holder.tvOneLine.setText("");
		holder.tvClearHistory.setText("");
		holder.ivRetrieval.setVisibility(View.VISIBLE);

	}

	/**
	 * @param searchList
	 */
	public void setData(List<Suggestion> searchList) {
		this.searchList = searchList;
	}

	class Holder {
		ImageView ivSearch;

		TextView tvKey;
		TextView tvOneLine;
		TextView tvClearHistory;

		TextView tvCity;

		ImageView ivRetrieval;

	}

}
