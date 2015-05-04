package com.wise.state;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.R.string;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.widget.CircleImageView;
import com.wise.notice.LetterActivity;
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
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

/**油耗排行榜**/
public class FuelRankActivity extends Activity{
	
	private static final int getData = 1;
	private static final int getPersionImage = 2;
	TextView tv_month,tv_total;
	ListView lv_fuel;
	FuelAdapter fuelAdapter;
	
	String month = "month";
	String all = "all";
	List<FuelData> fuelDatas = new ArrayList<FuelData>();
	AppApplication app;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_fuel_rank);
		app = (AppApplication)getApplication();
		TextView tv_name = (TextView)findViewById(R.id.tv_name);
		tv_name.setText("油耗排行榜");
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		tv_month = (TextView)findViewById(R.id.tv_month);
		tv_month.setOnClickListener(onClickListener);
		tv_total = (TextView)findViewById(R.id.tv_total);
		tv_total.setOnClickListener(onClickListener);
		lv_fuel = (ListView)findViewById(R.id.lv_fuel);
		fuelAdapter = new FuelAdapter();
		lv_fuel.setAdapter(fuelAdapter);
		lv_fuel.setOnScrollListener(onScrollListener);
		getData(month);
	}
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.tv_month:
				setBg();
				tv_month.setBackgroundResource(R.drawable.bg_border_left_press);
				tv_month.setTextColor(getResources().getColor(R.color.white));
				getData(month);
				break;
			case R.id.tv_total:
				setBg();
				tv_total.setBackgroundResource(R.drawable.bg_border_right_press);
				tv_total.setTextColor(getResources().getColor(R.color.white));
				getData(all);
				break;
			}
		}
	};
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case getData:
				jsonData(msg.obj.toString(),msg.arg1);
				fuelAdapter.notifyDataSetChanged();
				getPersionImage();
				break;
			case getPersionImage:
				removeThreadMark(msg.arg1);
				fuelAdapter.notifyDataSetChanged();
				break;
			}
		}		
	};
	private void getData(String data){
		String url = Constant.BaseUrl + "device/fuel_rank/" + data + "?auth_code=" + app.auth_code;
		new NetThread.GetDataThread(handler, url, getData).start();
	}
	private void jsonData(String Restult,int Type){
		try {
			fuelDatas.clear();
			JSONArray jsonArray = new JSONArray(Restult);
			for(int i = 0 ; i < jsonArray.length() ; i++){
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				FuelData fuelData = new FuelData();
				fuelData.setAvg_fuel(jsonObject.getDouble("avg_fuel"));
				fuelData.setEst_fuel(jsonObject.getDouble("est_fuel"));				
				fuelData.setMileage(jsonObject.getInt("total_distance"));
				fuelData.setCust_id(jsonObject.getInt("cust_id"));
				fuelData.setCust_name(jsonObject.getString("cust_name"));
				fuelData.setLogo(jsonObject.getString("logo"));
				String brand = jsonObject.getString("car_brand");
				String series = jsonObject.getString("car_series");
				if(series.indexOf(brand) == -1){
					series = brand + series;
				}
				fuelData.setCar_type(series);
				fuelDatas.add(fuelData);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	private void setBg(){
		tv_total.setTextColor(getResources().getColor(R.color.Green));
		tv_total.setBackgroundResource(R.drawable.bg_border_right);
		tv_month.setTextColor(getResources().getColor(R.color.Green));
		tv_month.setBackgroundResource(R.drawable.bg_border_left);
	}
	
	class FuelAdapter extends BaseAdapter{
		LayoutInflater inflater = LayoutInflater.from(FuelRankActivity.this);
		@Override
		public int getCount() {
			return fuelDatas.size();
		}
		@Override
		public Object getItem(int position) {
			return fuelDatas.get(position);
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if(convertView == null){
				convertView = inflater.inflate(R.layout.item_fuel_rank, null);
				viewHolder = new ViewHolder();
				viewHolder.tv_avg = (TextView)convertView.findViewById(R.id.tv_avg);
				viewHolder.tv_est = (TextView)convertView.findViewById(R.id.tv_est);
				viewHolder.tv_name = (TextView)convertView.findViewById(R.id.tv_name);
				viewHolder.tv_level = (TextView)convertView.findViewById(R.id.tv_level);
				viewHolder.iv_icon = (CircleImageView)convertView.findViewById(R.id.iv_icon);
				viewHolder.iv_letter = (ImageView)convertView.findViewById(R.id.iv_letter);
				convertView.setTag(viewHolder);
			}else{
				viewHolder = (ViewHolder) convertView.getTag();
			}
			if(position <3){
				viewHolder.tv_level.setVisibility(View.VISIBLE);
				viewHolder.tv_level.setText(""+(position + 1));
			}else{
				viewHolder.tv_level.setVisibility(View.GONE);
			}
			final FuelData fuelData = fuelDatas.get(position);
			viewHolder.tv_name.setText(fuelData.getCust_name());
			viewHolder.tv_est.setText("里程" + fuelData.getMileage() + "km 油耗" +fuelData.getAvg_fuel() + "L/100km");
			viewHolder.tv_avg.setText(fuelData.getCar_type() + " (" +fuelData.getEst_fuel() + "L/100km)");
			//读取用户对应的图片
			if(new File(Constant.userIconPath + GetSystem.getM5DEndo(fuelData.getLogo()) + ".png").exists()){
				Bitmap image = BitmapFactory.decodeFile(Constant.userIconPath + GetSystem.getM5DEndo(fuelData.getLogo()) + ".png");
				viewHolder.iv_icon.setImageBitmap(image);
			}else{
				viewHolder.iv_icon.setImageResource(R.drawable.icon_people_no);
			}
			viewHolder.iv_letter.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					if(fuelData.getCust_id() != Integer.valueOf(app.cust_id)){
						Intent intent = new Intent(FuelRankActivity.this, LetterActivity.class);
						intent.putExtra("cust_id", ""+fuelData.getCust_id());
						intent.putExtra("cust_name", fuelData.getCust_name());
						startActivity(intent);
					}
				}
			});
			
			return convertView;
		}
		class ViewHolder{
			TextView tv_name;
			TextView tv_avg;
			TextView tv_est;
			TextView tv_level;
			ImageView iv_letter;
			CircleImageView iv_icon;
		}
	}
	
	class FuelData{
		int cust_id;
		int mileage;
		double avg_fuel;
		double est_fuel;
		String cust_name;
		String logo;
		String car_type;		
		
		public int getCust_id() {
			return cust_id;
		}
		public void setCust_id(int cust_id) {
			this.cust_id = cust_id;
		}
		public int getMileage() {
			return mileage;
		}
		public void setMileage(int mileage) {
			this.mileage = mileage;
		}
		public double getAvg_fuel() {
			return avg_fuel;
		}
		public void setAvg_fuel(double avg_fuel) {
			this.avg_fuel = avg_fuel;
		}
		public double getEst_fuel() {
			return est_fuel;
		}
		public void setEst_fuel(double est_fuel) {
			this.est_fuel = est_fuel;
		}
		public String getCust_name() {
			return cust_name;
		}
		public void setCust_name(String cust_name) {
			this.cust_name = cust_name;
		}
		public String getLogo() {
			return logo;
		}
		public void setLogo(String logo) {
			this.logo = logo;
		}
		public String getCar_type() {
			return car_type;
		}
		public void setCar_type(String car_type) {
			this.car_type = car_type;
		}		
	}
	
	OnScrollListener onScrollListener = new OnScrollListener() {		
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL://触摸状态
				break;
			case OnScrollListener.SCROLL_STATE_FLING://滑动状态				
				break;
			case OnScrollListener.SCROLL_STATE_IDLE://停止
				//读取图片
				getPersionImage();
				break;
			}
		}		
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			
		}
	};
	/**获取显示区域的图片**/
	private void getPersionImage(){
		int start = lv_fuel.getFirstVisiblePosition();
		int stop = lv_fuel.getLastVisiblePosition();		
		for(int i = start ; i <= stop ; i++){
			if(i >= fuelDatas.size()){
				break;
			}
			if(fuelDatas.get(i).getLogo() == null || fuelDatas.get(i).getLogo().equals("")){
				
			}else{
				//判断图片是否存在
				if(new File(Constant.userIconPath + GetSystem.getM5DEndo(fuelDatas.get(i).getLogo()) + ".png").exists()){
					
				}else{
					if(isThreadRun(i)){
						//如果图片正在读取则跳过
					}else{
						photoThreadId.add(i);
						new ImageThread(i).start();
					}
				}
			}						
		}
	}
	List<Integer> photoThreadId = new ArrayList<Integer>();
	/**判断图片是否开启了线程正在读图**/
	private boolean isThreadRun(int positon){
		for(int i = 0 ; i < photoThreadId.size() ; i++){
			if(positon == photoThreadId.get(i)){
				return true;
			}
		}
		return false;
	}
	/**
	 * 删除列表里正在下载的线程标识
	 * @param position
	 */
	private void removeThreadMark(int position){
		for (int i = 0; i < photoThreadId.size(); i++) {
			if (photoThreadId.get(i) == position) {
				photoThreadId.remove(i);
				break;
			}
		}
	}
	class ImageThread extends Thread{
		int position;
		public ImageThread(int position){
			this.position = position;
		}
		@Override
		public void run() {
			super.run();
			Bitmap bitmap = GetSystem.getBitmapFromURL(fuelDatas.get(position).getLogo());
			if(bitmap != null){
				GetSystem.saveImageSD(bitmap, Constant.userIconPath, GetSystem.getM5DEndo(fuelDatas.get(position).getLogo()) + ".png",100);
			}
			Message message = new Message();
			message.what = getPersionImage;
			message.arg1 = position;
			handler.sendMessage(message);
		}
	}
}