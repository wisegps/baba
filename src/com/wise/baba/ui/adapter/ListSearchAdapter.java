/**
 * 
 */
package com.wise.baba.ui.adapter;

import com.wise.baba.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 *
 * @author c
 * @desc   baba
 * @date   2015-6-4
 *
 */
public class ListSearchAdapter extends BaseAdapter {

	
	private Context context;
	
	public ListSearchAdapter(Context context) {
		super();
		this.context = context;
	}

	
	@Override
	public int getCount() {
		return 5;
	}

	@Override
	public Object getItem(int position) {
		return null;
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
		return convertView;
	}

}
