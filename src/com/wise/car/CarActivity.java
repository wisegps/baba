package com.wise.car;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pubclas.Constant;
import pubclas.NetThread;
import pubclas.Variable;

import com.wise.baba.R;
import customView.SlidingView;
import data.CarData;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 车辆列表
 * @author honesty
 *
 */
public class CarActivity extends Activity{
	private static final int get_data = 1;
	private static final int remove_device = 2;
	private static final int delete_car = 3;
	ListView lv_cars;
	CarAdapter carAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_car);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		lv_cars = (ListView)findViewById(R.id.lv_cars);
		LayoutInflater mLayoutInflater = LayoutInflater.from(this);
        View foot_view = mLayoutInflater.inflate(R.layout.foot_view,null);
        lv_cars.addFooterView(foot_view);
        carAdapter = new CarAdapter();
		lv_cars.setAdapter(carAdapter);
		lv_cars.setOnItemClickListener(onItemClickListener);
		getData();
	}
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			}
		}
	};
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case get_data:
				jsonCarInfo(msg.obj.toString());
				break;
			case remove_device:
				jsonRemove(msg.obj.toString());
				break;
			case delete_car:
				jsonDelete(msg.obj.toString());
				break;
			}
		}		
	};
	private void jsonRemove(String str){
		try {
			JSONObject jsonObject = new JSONObject(str);
			if(jsonObject.getString("status_code").equals("0")){
				Toast.makeText(CarActivity.this, "解除绑定成功", Toast.LENGTH_SHORT).show();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	private void jsonDelete(String str){
		try {
			JSONObject jsonObject = new JSONObject(str);
			if(jsonObject.getString("status_code").equals("0")){
				Toast.makeText(CarActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
				Variable.carDatas.remove(index);
				carAdapter.notifyDataSetChanged();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			System.out.println("arg2 = " + arg2);
			if(arg2 == Variable.carDatas.size()){
				startActivity(new Intent(CarActivity.this, CarAddActivity.class));
			}else{
				
			}
		}
	};
	private void getData(){
		String url = Constant.BaseUrl + "customer/" + Variable.cust_id + "/vehicle?auth_code=" + Variable.auth_code;
		new Thread(new NetThread.GetDataThread(handler, url, get_data)).start();
	}
	private void jsonCarInfo(String str){
		try {
			Variable.carDatas.clear();
			JSONArray jsonArray = new JSONArray(str);
			for(int i = 0 ; i < jsonArray.length() ; i++){
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				CarData carData = new CarData();
				carData.setObj_id(jsonObject.getInt("obj_id"));
				carData.setNick_name(jsonObject.getString("nick_name"));
				if(jsonObject.opt("device_id") == null){
					carData.setCheck(false);
					carData.setDevice_id("");
				}else{
					if(jsonObject.opt("device_id").equals("0")){
						carData.setCheck(false);
						carData.setDevice_id("");
					}else{
						carData.setCheck(true);
						carData.setDevice_id(jsonObject.getString("device_id"));
					}
				}
				carData.setCar_brand(jsonObject.getString("car_brand"));
				carData.setCar_brand_id(jsonObject.getString("car_brand_id"));
				carData.setCar_series(jsonObject.getString("car_series"));
				carData.setCar_series_id(jsonObject.getString("car_series_id"));
				carData.setCar_type(jsonObject.getString("car_type"));
				carData.setCar_type_id(jsonObject.getString("car_type_id"));
				if(jsonObject.opt("annual_inspect_date") != null){
					carData.setAnnual_inspect_date(jsonObject.getString("annual_inspect_date").substring(0, 10));
				}
				if(jsonObject.opt("buy_date") != null){
					carData.setBuy_date(jsonObject.getString("buy_date").substring(0, 10));
				}
				if(jsonObject.opt("engine_no") != null){
					carData.setEngine_no(jsonObject.getString("engine_no"));
				}
				if(jsonObject.opt("frame_no") != null){
					carData.setFrame_no(jsonObject.getString("frame_no"));
				}
				if(jsonObject.opt("reg_no") != null){
					carData.setRegNo(jsonObject.getString("reg_no"));
				}
				if(jsonObject.opt("gas_no") != null){
					carData.setGas_no(jsonObject.getString("gas_no"));
				}
				if(jsonObject.opt("insurance_company") != null){
					carData.setInsurance_company(jsonObject.getString("insurance_company"));
				}
				if(jsonObject.opt("insurance_date") != null){
					carData.setInsurance_date(jsonObject.getString("insurance_date").substring(0, 10));
				}
				if(jsonObject.opt("insurance_tel") != null){
					carData.setInsurance_tel(jsonObject.getString("insurance_tel"));
				}
				if(jsonObject.opt("insurance_no") != null){
					carData.setInsurance_no(jsonObject.getString("insurance_no"));
				}
				if(jsonObject.opt("maintain_company") != null){
					carData.setMaintain_company(jsonObject.getString("maintain_company"));
				}
				if(jsonObject.opt("maintain_last_date") != null){
					carData.setMaintain_last_date(jsonObject.getString("maintain_last_date").substring(0, 10));
				}
				if(jsonObject.opt("maintain_last_mileage") != null){
					carData.setMaintain_last_mileage(jsonObject.getString("maintain_last_mileage"));
				}
				if(jsonObject.opt("maintain_tel") != null){
					carData.setMaintain_tel(jsonObject.getString("maintain_tel"));
				}
				
				String vio_citys = jsonObject.getString("vio_citys");
				vio_citys = "[{vio_location:\"GD_SZ\",vio_city_name:\"深圳\"},{vio_location:\"GD_CZ\",vio_city_name:\"潮州\"}]";
				JSONArray jsonArray2 = new JSONArray(vio_citys);
				for(int j = 0 ; j < jsonArray2.length() ; j++){
					JSONObject jsonObject2 = jsonArray2.getJSONObject(j);
					String vio_city_name = jsonObject2.getString("vio_city_name");
					String vio_location = jsonObject2.getString("vio_location");
					System.out.println(vio_city_name + "," + vio_location);
				}
				Variable.carDatas.add(carData);
			}
			carAdapter.notifyDataSetChanged();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	int index;
	
	class CarAdapter extends BaseAdapter{
		private LayoutInflater layoutInflater = LayoutInflater.from(CarActivity.this);
		@Override
		public int getCount() {
			return Variable.carDatas.size();
		}
		@Override
		public Object getItem(int arg0) {
			return Variable.carDatas.get(arg0);
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if(convertView == null){
				convertView = layoutInflater.inflate(R.layout.item_cars, null);
	            holder = new ViewHolder();
	            holder.iv_icon = (ImageView)convertView.findViewById(R.id.iv_icon);
	            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
	            holder.tv_update = (TextView) convertView.findViewById(R.id.tv_update);
	            holder.tv_remove = (TextView) convertView.findViewById(R.id.tv_remove);
	            holder.tv_del = (TextView) convertView.findViewById(R.id.tv_del);
	            holder.bt_bind = (Button) convertView.findViewById(R.id.bt_bind);
	            holder.sv = (SlidingView) convertView.findViewById(R.id.sv);
	            holder.rl_car = (RelativeLayout) convertView.findViewById(R.id.rl_car);
	            convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			if(new File(Constant.VehicleLogoPath+Variable.carDatas.get(position).getCar_brand() + ".png").exists()){
				Bitmap image = BitmapFactory.decodeFile(Constant.VehicleLogoPath+Variable.carDatas.get(position).getCar_brand() + ".png");
				holder.iv_icon.setImageBitmap(image);
			}else{
				holder.iv_icon.setImageResource(R.drawable.body_nothing_icon);				
			}
			holder.tv_name.setText(Variable.carDatas.get(position).getNick_name());
			holder.rl_car.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(CarActivity.this,CarUpdateActivity.class);
					intent.putExtra("index", position);
					startActivityForResult(intent, 2);
				}
			});
			holder.tv_del.setOnClickListener(new OnClickListener() {					
				@Override
				public void onClick(View v) {
					index = position;
					String url = Constant.BaseUrl + "vehicle/" + Variable.carDatas.get(position).getObj_id() + "?auth_code=" + Variable.auth_code;
				    new Thread(new NetThread.DeleteThread(handler, url, delete_car)).start();	                    
				}
			});
			holder.sv.ScorllRestFast();
			if(Variable.carDatas.get(position).isCheck()){
				holder.bt_bind.setVisibility(View.GONE);
				holder.tv_update.setVisibility(View.VISIBLE);
				holder.tv_remove.setVisibility(View.VISIBLE);
				holder.tv_update.setOnClickListener(new OnClickListener() {					
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(CarActivity.this,DevicesAddActivity.class);
						intent.putExtra("car_id", Variable.carDatas.get(position).getObj_id());
						startActivityForResult(intent, 2);
					}
				});
				holder.tv_remove.setOnClickListener(new OnClickListener() {					
					@Override
					public void onClick(View v) {
						String url = Constant.BaseUrl + "vehicle/" + Variable.carDatas.get(position).getObj_id() + "/device?auth_code=" + Variable.auth_code;
						List<NameValuePair> params = new ArrayList<NameValuePair>();
						params.add(new BasicNameValuePair("device_id","0"));
						new Thread(new NetThread.putDataThread(handler,url, params, remove_device)).start();
					}
				});
			}else{
				holder.tv_update.setVisibility(View.GONE);
				holder.tv_remove.setVisibility(View.GONE);
				holder.bt_bind.setVisibility(View.VISIBLE);
				holder.bt_bind.setOnClickListener(new OnClickListener() {					
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(CarActivity.this,DevicesAddActivity.class);
						intent.putExtra("car_id", Variable.carDatas.get(position).getObj_id());
						startActivityForResult(intent, 2);
					}
				});
			}

			return convertView;
		}
		private class ViewHolder {
	        TextView tv_name,tv_update,tv_del,tv_remove;
	        ImageView iv_icon;
	        Button bt_bind;
	        SlidingView sv;
	        RelativeLayout rl_car;
	    }
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == 2){
			//绑定车辆信息成功
			carAdapter.notifyDataSetChanged();
		}else if(resultCode == 3){
			getData();
		}
	}
}