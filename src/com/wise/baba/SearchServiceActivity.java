package com.wise.baba;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SearchServiceActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.item_search);
		setData();
		ListView lv_search = (ListView)findViewById(R.id.lv_search);
        lv_search.setAdapter(new ServiceAdapter());
        lv_search.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				startActivity(new Intent(SearchServiceActivity.this, ServiceShowActivity.class));
			}
		});
	}
	
	private class ServiceAdapter extends BaseAdapter{
		LayoutInflater mInflater = LayoutInflater.from(SearchServiceActivity.this);
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
                convertView = mInflater.inflate(R.layout.item_service, null);
                holder = new ViewHolder();
                holder.tv_service_name = (TextView) convertView.findViewById(R.id.tv_service_name);                
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_service_name.setText(serviceDatas.get(position).getName());
            return convertView;
		}
		private class ViewHolder {
            TextView tv_service_name;
        }
	}
	
	List<ServiceData> serviceDatas = new ArrayList<ServiceData>();
	private void setData(){
		for(int i = 0 ; i < 5 ; i++){
			ServiceData serviceData = new ServiceData();
			serviceData.setName("叭叭");
			serviceDatas.add(serviceData);
		}
	}
	
	private class ServiceData{
		private String name;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}		
	}
	
}
