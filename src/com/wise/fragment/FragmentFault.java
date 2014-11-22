package com.wise.fragment;

import java.util.ArrayList;

import com.wise.baba.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 *@author honesty
 **/
public class FragmentFault extends Fragment{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_service_obd, container, false);
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		ListView lv_obd = (ListView)getActivity().findViewById(R.id.lv_obd);
		setData();
		ObdAdapter obdAdapter = new ObdAdapter();
		lv_obd.setAdapter(obdAdapter);
	}
	
	class ObdAdapter extends BaseAdapter{
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		@Override
		public int getCount() {
			return obdDatas.size();
		}
		@Override
		public Object getItem(int position) {
			return obdDatas.get(position);
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if(convertView == null){
				convertView = inflater.inflate(R.layout.item_obd, null);
				holder = new ViewHolder();
				holder.tv_content = (TextView)convertView.findViewById(R.id.tv_content);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			ObdData obdData = obdDatas.get(position);
			holder.tv_content.setText(obdData.getName());
			switch (obdData.type) {
			case 0:
				holder.tv_content.setTextSize(18);
				break;
			case 1:
				holder.tv_content.setTextSize(14);
				break;
			case 2:
				holder.tv_content.setTextSize(14);
				break;
			}
			return convertView;
		}
		class ViewHolder{
			TextView tv_content;
		}
	}
	
	ArrayList<ObdData> obdDatas = new ArrayList<ObdData>();
	private void setData(){
		for(int i = 0 ; i < 3 ; i++){
			ObdData obdData3 = new ObdData();
			obdData3.setName("车辆名称：" + i);
			obdData3.setType(0);
			ObdData obdData2 = new ObdData();
			obdData2.setName("OBD数据：" + i);
			obdData2.setType(1);
			ObdData obdData = new ObdData();
			obdData.setName("OBD故障：" + i);
			obdData.setType(2);
			obdDatas.add(obdData3);
			obdDatas.add(obdData2);
			obdDatas.add(obdData);
		}
	}
	private class ObdData{
		String name;
		int type;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getType() {
			return type;
		}
		public void setType(int type) {
			this.type = type;
		}		
	}
}