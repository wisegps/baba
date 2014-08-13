package com.wise.state;

import java.net.URLEncoder;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pubclas.GetSystem;
import pubclas.NetThread;
import pubclas.Variable;
import com.umeng.analytics.MobclickAgent;
import com.wise.baba.R;
import customView.EnergyCurveView;
import data.EnergyItem;
import data.WeekData;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
/**
 * 油耗
 * @author honesty
 */
public class FuelActivity extends Activity{
	private static final String TAG = "FuelActivity";
	private static final int getData = 1;
	
	EnergyCurveView ecv_fuel;
	private DisplayMetrics dm = new DisplayMetrics();
	LinearLayout ll_chart;
	private TasksCompletedView mTasksView;
	TextView tv_date,tv_money;
	TextView tv_month,tv_week,tv_day;
	TextView tv_distance,tv_fuel,tv_avg_fuel;
	TextView tv_chart_title;
	ImageView iv_right;
	/**日，周，月**/
	int index = 2;
	/**第几个车**/
	int index_car = 0;
	String Month;
	String Day;
	ArrayList<EnergyItem> Efuel = new ArrayList<EnergyItem>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_fuel);
		GetSystem.myLog(TAG, "onCreate");
		ll_chart = (LinearLayout)findViewById(R.id.ll_chart);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		TextView tv_name = (TextView)findViewById(R.id.tv_name);
		tv_money = (TextView)findViewById(R.id.tv_money);
		ecv_fuel = (EnergyCurveView)findViewById(R.id.ecv_fuel);
		mTasksView = (TasksCompletedView) findViewById(R.id.tasks_view);
		mTasksView.setProgress(100,true);
		mTasksView.setRingColor(getResources().getColor(R.color.Green),true);
		mTasksView.setOnClickListener(onClickListener);
		ImageView iv_left = (ImageView)findViewById(R.id.iv_left);
		iv_left.setOnClickListener(onClickListener);
		iv_right = (ImageView)findViewById(R.id.iv_right);
		iv_right.setOnClickListener(onClickListener);
		tv_month = (TextView)findViewById(R.id.tv_month);
		tv_month.setOnClickListener(onClickListener);
		tv_week = (TextView)findViewById(R.id.tv_week);
		tv_week.setOnClickListener(onClickListener);
		tv_day = (TextView)findViewById(R.id.tv_day);
		tv_day.setOnClickListener(onClickListener);
		tv_date = (TextView)findViewById(R.id.tv_date);
		tv_distance = (TextView)findViewById(R.id.tv_distance);
		tv_fuel = (TextView)findViewById(R.id.tv_fuel);
		tv_avg_fuel = (TextView)findViewById(R.id.tv_avg_fuel);
		tv_chart_title = (TextView)findViewById(R.id.tv_chart_title);
		
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		ecv_fuel.setViewWidth(dm.widthPixels);
		index_car = getIntent().getIntExtra("index_car", 0);
		GetSystem.myLog(TAG, "Variable.carDatas.size() = " + Variable.carDatas.size());
		tv_name.setText(Variable.carDatas.get(index_car).getNick_name());//TODO 异常
		initData();
	}
	
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.tv_month:
				ll_chart.setVisibility(View.VISIBLE);
				index = 2;
				Month = GetSystem.GetNowMonth().getMonth();
				tv_date.setText(Month);
				setBg();
				tv_month.setBackgroundResource(R.drawable.bg_border_right_press);
				tv_month.setTextColor(getResources().getColor(R.color.white));
				getData(Month+"-01",Month+"-31");
				tv_chart_title.setText("百公里驾驶油耗月曲线");
				iv_right.setVisibility(View.GONE);
				break;
			case R.id.tv_week:
				ll_chart.setVisibility(View.VISIBLE);
				index = 1;
				Day = GetSystem.GetNowMonth().getDay();
				WeekData weekData = GetSystem.getWeek(Day);
				tv_date.setText(weekData.getFristDay() + " - " + weekData.getLastDay());
				setBg();
				tv_week.setBackgroundResource(R.drawable.bg_border_center_press);
				tv_week.setTextColor(getResources().getColor(R.color.white));
				getData(weekData.getFristDay(),weekData.getLastDay());
				tv_chart_title.setText("百公里驾驶油耗周曲线");
				iv_right.setVisibility(View.GONE);
				break;
			case R.id.tv_day:
				ll_chart.setVisibility(View.GONE);
				index = 0;
				Day = GetSystem.GetNowMonth().getDay();
				tv_date.setText(Day);
				setBg();
				tv_day.setBackgroundResource(R.drawable.bg_border_left_press);
				tv_day.setTextColor(getResources().getColor(R.color.white));
				getDayData(Day);
				iv_right.setVisibility(View.GONE);
				break;
			case R.id.iv_left:
				if(index == 2){
					Month = GetSystem.GetNextMonth(Month, -1).getMonth();
					tv_date.setText(Month);
					getData(Month+"-01",Month+"-31");
					iv_right.setVisibility(View.VISIBLE);
				}else if(index == 0){
					Day = GetSystem.GetNextData(Day, -1);
					tv_date.setText(Day);
					getDayData(Day);
					iv_right.setVisibility(View.VISIBLE);
				}else if(index == 1){
					Day = GetSystem.GetNextData(Day, -7);
					WeekData weekData1 = GetSystem.getWeek(Day);
					tv_date.setText(weekData1.getFristDay() + " - " + weekData1.getLastDay());
					getData(weekData1.getFristDay(),weekData1.getLastDay());
					iv_right.setVisibility(View.VISIBLE);
				}
				break;
			case R.id.iv_right:
				if(index == 2){
					Month = GetSystem.GetNextMonth(Month, 1).getMonth();
					tv_date.setText(Month);
					getData(Month+"-01",Month+"-31");
					boolean isMax = GetSystem.maxTime(Month+"-31" + " 00:00:00", GetSystem.GetNowMonth().getDay() + " 00:00:00");
					if(isMax){
						iv_right.setVisibility(View.GONE);
					}
				}else if(index == 0){
					Day = GetSystem.GetNextData(Day, 1);
					tv_date.setText(Day);
					getDayData(Day);
					boolean isMax = GetSystem.maxTime(Day + " 00:00:00", GetSystem.GetNowMonth().getDay() + " 00:00:00");
					if(isMax){
						iv_right.setVisibility(View.GONE);
					}
				}else if(index == 1){
					Day = GetSystem.GetNextData(Day, 7);
					WeekData weekData2 = GetSystem.getWeek(Day);
					tv_date.setText(weekData2.getFristDay() + " - " + weekData2.getLastDay());
					getData(weekData2.getFristDay(),weekData2.getLastDay());
					boolean isMax = GetSystem.maxTime(weekData2.getLastDay() + " 00:00:00", GetSystem.GetNowMonth().getDay() + " 00:00:00");
					if(isMax){
						iv_right.setVisibility(View.GONE);
					}
				}				
				break;
			case R.id.tasks_view://index_car
				Intent intent = new Intent(FuelActivity.this, FuelDetailsActivity.class);
				intent.putExtra("index_car", index_car);
				startActivity(intent);
				break;
			}
		}
	};
	
	int money = 9;
	
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case getData:
				jsonData(msg.obj.toString());
				break;
			}
		}		
	};
	/**获取一段时间数据**/
	private void getData(String fristDate,String lastDate){
		try {
			String url = "http://42.121.109.221:8002/device/" + 
					Variable.carDatas.get(index_car).getDevice_id() + "/total?auth_code=127a154df2d7850c4232542b4faa2c3d&start_day=" + fristDate + "&end_day=" + lastDate + "&city=" + URLEncoder.encode(Variable.City, "UTF-8") + "&gas_no=93#(92#)";
			new NetThread.GetDataThread(handler, url, getData).start();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	/**获取某一天数据**/
	private void getDayData(String Date){
		try {
			String url = "http://42.121.109.221:8002/device/" + 
					Variable.carDatas.get(index_car).getDevice_id()+ "/day_total?auth_code=127a154df2d7850c4232542b4faa2c3d&day=" + Date + "&city=" + URLEncoder.encode(Variable.City, "UTF-8") + "&gas_no=93#(92#)";
			new NetThread.GetDataThread(handler, url, getData).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**解析统计数据**/
	private void jsonData(String str){
		try {
			JSONObject jsonObject = new JSONObject(str);
			String total_fee = String.format("%.0f",jsonObject.getDouble("total_fee"));
			String total_distance = jsonObject.getString("total_distance");
			String total_fuel = String.format("%.0f",jsonObject.getDouble("total_fuel"));
			String avg_fuel = String.format("%.1f",jsonObject.getDouble("avg_fuel"));
			tv_distance.setText(total_distance);
			tv_fuel.setText(total_fuel);
			tv_avg_fuel.setText((avg_fuel.equals("null"))?"0":avg_fuel);
			//mTasksView.setProgress(100);
    		tv_money.setText(total_fee);
    		//周月，需要画图
    		if(index != 0){
    			Efuel.clear();
        		try {
        			JSONArray jsonArray = jsonObject.getJSONArray("fuel_data");
        			for(int i = 0 ; i < jsonArray.length(); i++){
        				float avg_fuel1 = Float.valueOf(jsonArray.getJSONObject(i).getString("avg_fuel"));
        				int rcv_day = Integer.valueOf(jsonArray.getJSONObject(i).getString("rcv_day").substring(8, 10));
        				Efuel.add(new EnergyItem(rcv_day, avg_fuel1));
        			}
        		} catch (JSONException e) {
        			e.printStackTrace();
        		}    		
        		ecv_fuel.initPoints(Efuel);
    			ecv_fuel.RefreshView();
    		}    		
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void initData(){
		index = 2;
		Month = GetSystem.GetNowMonth().getMonth();
		tv_date.setText(Month);
		getData(Month+"-01",Month+"-31");
	}
	private void setBg(){
		tv_month.setTextColor(getResources().getColor(R.color.Green));
		tv_month.setBackgroundResource(R.drawable.bg_border_right);
		tv_week.setTextColor(getResources().getColor(R.color.Green));
		tv_week.setBackgroundResource(R.drawable.bg_border_center);
		tv_day.setTextColor(getResources().getColor(R.color.Green));
		tv_day.setBackgroundResource(R.drawable.bg_border_left);
	}
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}