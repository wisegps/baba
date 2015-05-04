package com.wise.baba.ui.fragment;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.wise.baba.R;

/**
 *@author honesty
 **/
public class FragmentFault extends Fragment{
	
	static int friend_id;
	ArrayList<ObdData> obdDatas = new ArrayList<ObdData>();
	
	public static FragmentFault newInstance(int Friend_id){
		FragmentFault fragmentFault = new FragmentFault();
		friend_id = Friend_id;
		return fragmentFault;
	}
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
		//getData();
	}
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
		}		
	};
	private void getData(){
		
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
				holder.tv_car_name = (TextView)convertView.findViewById(R.id.tv_car_name);
				holder.tv_obd_data = (TextView)convertView.findViewById(R.id.tv_obd_data);
				holder.tv_obd_fault = (TextView)convertView.findViewById(R.id.tv_obd_fault);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			ObdData obdData = obdDatas.get(position);
			holder.tv_car_name.setText(obdData.getName());
			holder.tv_obd_data.setText(obdData.getData());
			holder.tv_obd_fault.setText(obdData.getFault());
			return convertView;
		}
		class ViewHolder{
			TextView tv_car_name;
			TextView tv_obd_data;
			TextView tv_obd_fault;
		}
	}
	
	private void setData(){
		for(int i = 0 ; i < 3 ; i++){
			ObdData obdData = new ObdData();
			obdData.setName("车辆名称：" + i);
			obdData.setData("OBD数据：车辆油耗正常，没有急刹车");
			obdData.setFault("OBD故障：车辆温度过高。空气质量超标");
			obdDatas.add(obdData);
		}
	}
	private class ObdData{
		String name;
		String data;
		String fault;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getData() {
			return data;
		}
		public void setData(String data) {
			this.data = data;
		}
		public String getFault() {
			return fault;
		}
		public void setFault(String fault) {
			this.fault = fault;
		}			
	}
}