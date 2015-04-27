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
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * @author c
 * @desc baba
 * @date 2015-4-22
 * 
 */
public class GridShopAdapter extends BaseAdapter {

	private Context context;
	private LayoutInflater inflater;
	private int[] value;

	public GridShopAdapter(Context context) {
		super();
		value = new int[]{0,0,0,0,0,0};
		this.context = context;
		inflater = LayoutInflater.from(context);
		
	}

	public void setValue(int value[]){
		this.value = value;
	}
	/*
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return 6;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_home_shop, null);
		}
		
		ImageView ivItem = (ImageView) convertView.findViewById(R.id.iv_item);
		
		TextView tvItem = (TextView) convertView.findViewById(R.id.tv_item);
		
		TextView tvNumber = (TextView) convertView.findViewById(R.id.tv_number);
		tvItem.setText(getText()[position]);
		tvNumber.setText(value[position]+"");
		ivItem.setImageResource(getResource("ico_shop_"+position));
		return convertView;
	}
	

	public String[] getText(){
		//new String[]{"用户总数","到期终端数","车辆数","报价车辆数","故障车辆数","未读消息数","产品数","评价数",""};
		return new String[]{"用户总数","车辆数","车务提醒数","报警车辆数","故障车辆数","到期终端数"};
	}
	
	/** 返回天气对应的r资源名称 **/
	public int getResource(String imageName) {
		int resId = context.getResources().getIdentifier(imageName, "drawable", "com.wise.baba");
		return resId;
	}

}
