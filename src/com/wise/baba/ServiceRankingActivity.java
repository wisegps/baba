package com.wise.baba;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class ServiceRankingActivity extends Activity{
	ListView lv_service;
	int screenWidth;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_service_ranking);
		TextView tv_service_providers = (TextView)findViewById(R.id.tv_service_providers);
		tv_service_providers.setOnClickListener(onClickListener);
		TextView tv_service = (TextView)findViewById(R.id.tv_service);
		tv_service.setOnClickListener(onClickListener);
		TextView tv_distance = (TextView)findViewById(R.id.tv_distance);
		tv_distance.setOnClickListener(onClickListener);
		Button bt_show = (Button)findViewById(R.id.bt_show);
		bt_show.setOnClickListener(onClickListener);
		DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels/3;
        setData();
        lv_service = (ListView)findViewById(R.id.lv_service);
        lv_service.setAdapter(new ServiceAdapter());
        lv_service.setOnItemClickListener(onItemClickListener);
	}
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.tv_service_providers:
				ShowMenuPop(v);
				break;
			case R.id.tv_service:
				ShowMenuPop(v);
				break;
			case R.id.tv_distance:
				ShowMenuPop(v);
				break;
			case R.id.bt_show:
				startActivity(new Intent(ServiceRankingActivity.this, SearchServiceActivity.class));
				break;
			}
		}
	};
	OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			startActivity(new Intent(ServiceRankingActivity.this, ServiceInfoActivity.class));
		}
	};
	
	private void ShowMenuPop(View auchor) {
        LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popunwindwow = mLayoutInflater.inflate(R.layout.fragment_more,null);
        PopupWindow mPopupWindow = new PopupWindow(popunwindwow, screenWidth,LayoutParams.WRAP_CONTENT);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.showAsDropDown(auchor);
    }
	
	List<ServiceData> serviceDatas = new ArrayList<ServiceData>();
	private void setData(){
		for(int i = 0 ; i < 15 ; i++){
			ServiceData serviceData = new ServiceData();
			serviceData.setName("叭叭");
			serviceData.setType("洗车");
			serviceData.setScore(5);
			serviceData.setDistance(4.4);
			serviceDatas.add(serviceData);
		}
	}
	
	private class ServiceAdapter extends BaseAdapter{
		LayoutInflater mInflater = LayoutInflater.from(ServiceRankingActivity.this);
		@Override
		public int getCount() {
			return serviceDatas.size();
		}
		@Override
		public Object getItem(int position) {
			return serviceDatas.get(position);
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_service_specific, null);
                holder = new ViewHolder();
                holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);                
                holder.tv_type = (TextView) convertView.findViewById(R.id.tv_type);                
                holder.tv_distance = (TextView) convertView.findViewById(R.id.tv_distance);                
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_name.setText(serviceDatas.get(position).getName());
            holder.tv_type.setText(serviceDatas.get(position).getType());
            holder.tv_distance.setText(serviceDatas.get(position).getDistance() + "KM");
            return convertView;
		}
		private class ViewHolder {
            TextView tv_name,tv_type,tv_distance;
        }
	}
	
	private class ServiceData{
		private String name;
		private String type;
		private int score;
		private double distance;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public int getScore() {
			return score;
		}
		public void setScore(int score) {
			this.score = score;
		}
		public double getDistance() {
			return distance;
		}
		public void setDistance(double distance) {
			this.distance = distance;
		}		
	}
}
