package com.wise.state;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.entity.CarData;
import com.wise.baba.entity.EnergyItem;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.widget.EnergyCurveView;
import com.wise.baba.ui.widget.FanView;
import com.wise.baba.ui.widget.FanView.OnViewRotateListener;


/**
 * 油耗
 * 
 * @author honesty
 */
public class FuelActivity extends Activity {
	private static final String TAG = "FuelActivity";
	private static final int getData = 1;

	EnergyCurveView ecv_fuel;
	private final DisplayMetrics dm = new DisplayMetrics();
	LinearLayout ll_chart, ll_fv;
	private TasksCompletedView mTasksView;
	TextView tv_date, tv_money;
	TextView tv_month, tv_week, tv_day;
	TextView tv_distance, tv_fuel, tv_avg_fuel;
	TextView tv_chart_title, tv_chart_unit;
	TextView tv_speed_text, tv_speed_avg_fuel, tv_speed_fuel;
	// 标题
	TextView tv_title_1, tv_title_2, tv_title_3, tv_title_map;
	// 单位内容
	TextView tv_content_1, tv_content_2, tv_content_3;

	ImageView iv_right;
	/** 日，周，月 **/
	int index = 2;
	/** 第几个车 **/
	//int index_car = 0;
	/** 跳转类型 **/
	int type = 0;
	String Month;
	String Day;
	ArrayList<EnergyItem> Efuel = new ArrayList<EnergyItem>();
	FanView fv;
	AppApplication app;
	/** 星期数组 **/
	String[] weekData;
	// 跳转类型
	public static final int DISTANCE = 1;// 里程
	public static final int FEE = 2;// 费用
	public static final int FUEL = 3;// 油耗
	
	CarData carData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_fuel);
		app = (AppApplication) getApplication();
		GetSystem.myLog(TAG, "onCreate");
		fv = (FanView) findViewById(R.id.fv);
		tv_speed_text = (TextView) findViewById(R.id.tv_speed_text);
		tv_speed_avg_fuel = (TextView) findViewById(R.id.tv_speed_avg_fuel);
		tv_speed_fuel = (TextView) findViewById(R.id.tv_speed_fuel);
		ll_chart = (LinearLayout) findViewById(R.id.ll_chart);
		ll_fv = (LinearLayout) findViewById(R.id.ll_fv);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		TextView tv_name = (TextView) findViewById(R.id.tv_name);
		tv_money = (TextView) findViewById(R.id.tv_money);

		// 各类型控件
		tv_title_1 = (TextView) findViewById(R.id.tv_title_1);
		tv_title_2 = (TextView) findViewById(R.id.tv_title_2);
		tv_title_3 = (TextView) findViewById(R.id.tv_title_3);
		tv_title_map = (TextView) findViewById(R.id.tv_title_map);

		tv_content_1 = (TextView) findViewById(R.id.tv_content_1);
		tv_content_2 = (TextView) findViewById(R.id.tv_content_2);
		tv_content_3 = (TextView) findViewById(R.id.tv_content_3);

		ecv_fuel = (EnergyCurveView) findViewById(R.id.ecv_fuel);
		mTasksView = (TasksCompletedView) findViewById(R.id.tasks_view);
		mTasksView.setProgress(100, true);
		mTasksView.setRingColor(getResources().getColor(R.color.Green), true);
		mTasksView.setOnClickListener(onClickListener);
		ImageView iv_left = (ImageView) findViewById(R.id.iv_left);
		iv_left.setOnClickListener(onClickListener);
		iv_right = (ImageView) findViewById(R.id.iv_right);
		iv_right.setOnClickListener(onClickListener);
		tv_month = (TextView) findViewById(R.id.tv_month);
		tv_month.setOnClickListener(onClickListener);
		tv_week = (TextView) findViewById(R.id.tv_week);
		tv_week.setOnClickListener(onClickListener);
		tv_day = (TextView) findViewById(R.id.tv_day);
		tv_day.setOnClickListener(onClickListener);
		tv_date = (TextView) findViewById(R.id.tv_date);
		tv_distance = (TextView) findViewById(R.id.tv_distance);
		tv_fuel = (TextView) findViewById(R.id.tv_fuel);
		tv_avg_fuel = (TextView) findViewById(R.id.tv_avg_fuel);
		tv_chart_title = (TextView) findViewById(R.id.tv_chart_title);
		tv_chart_unit = (TextView) findViewById(R.id.tv_chart_unit);

		Button bt_FuelRank = (Button) findViewById(R.id.bt_FuelRank);
		bt_FuelRank.setOnClickListener(onClickListener);

		getWindowManager().getDefaultDisplay().getMetrics(dm);
		fv.setViewSize(dm.widthPixels * 3 / 8);
		ecv_fuel.setViewWidth(dm.widthPixels, true);
		carData = (CarData)getIntent().getSerializableExtra("carData");
		if(carData == null && app.carDatas.size()>0){
			carData = app.carDatas.get(app.currentCarIndex);
		}
		
		// 获取显示页面类型
		type = getIntent().getIntExtra("type", 0);

		if (type == FUEL) {
			tv_chart_title.setText("百公里驾驶油耗月曲线");
		} else if (type == DISTANCE) {
			tv_chart_title.setText("里程月曲线");
			tv_chart_unit.setText("km");
		} else if (type == FEE) {
			tv_chart_title.setText("花费月曲线");
			tv_chart_unit.setText("￥");
		} else {
			tv_chart_title.setText("无消耗");
		}

		tv_name.setText(carData.getNick_name());
		initData();
		fv.setOnViewRotateListener(new OnViewRotateListener() {
			@Override
			public void viewRotate(int rotateRanges) {
				RangeData rangeData = rangeDatas.get(rotateRanges);
				if (type == FUEL) {
					tv_speed_text.setText(rangeData.getSpeed_text());
					tv_speed_avg_fuel.setText("平均油耗：" + rangeData.getAvg_fuel());
					tv_speed_fuel.setText("油耗：" + rangeData.getFuel());
				} else if (type == DISTANCE) {
					tv_speed_text.setText(rangeData.getSpeed_text());
					tv_speed_avg_fuel.setText("里程：" + rangeData.getDistance());
					tv_speed_fuel.setText("油耗：" + rangeData.getFuel());
				} else {
					tv_speed_text.setText(rangeData.getSpeed_text());
					tv_speed_avg_fuel.setText("花费：" + rangeData.getFee());
					tv_speed_fuel.setText("油耗：" + rangeData.getFuel());
				}
			}
		});
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.bt_FuelRank:
				Intent Intent = new Intent(FuelActivity.this, FuelRankActivity.class);
				startActivity(Intent);
				break;
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
				getData(Month + "-01", GetSystem.getMonthLastDay(Month));
				// 曲线图类型
				if (type == FUEL) {
					tv_chart_title.setText("百公里驾驶油耗月曲线");
				} else if (type == DISTANCE) {
					tv_chart_title.setText("里程月曲线");
					tv_chart_unit.setText("km");
				} else if (type == FEE) {
					tv_chart_title.setText("花费月曲线");
					tv_chart_unit.setText("￥");
				} else {
					tv_chart_title.setText("无消耗");
				}
				iv_right.setVisibility(View.GONE);
				break;
			case R.id.tv_week:
				ll_chart.setVisibility(View.VISIBLE);
				index = 1;
				Day = GetSystem.GetNowMonth().getDay();
				weekData = GetSystem.getWeek(Day);
				tv_date.setText(weekData[0] + " - " + weekData[6]);
				setBg();
				tv_week.setBackgroundResource(R.drawable.bg_border_center_press);
				tv_week.setTextColor(getResources().getColor(R.color.white));
				getData(weekData[0], weekData[6]);

				if (type == FUEL) {
					tv_chart_title.setText("百公里驾驶油耗周曲线");
				} else if (type == DISTANCE) {
					tv_chart_title.setText("里程周曲线");
					tv_chart_unit.setText("km");
				} else if (type == FEE) {
					tv_chart_title.setText("花费周曲线");
					tv_chart_unit.setText("￥");
				} else {
					tv_chart_title.setText("无消耗");
				}
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
				if (index == 2) {
					Month = GetSystem.GetNextMonth(Month, -1).getMonth();
					tv_date.setText(Month);
					getData(Month + "-01", GetSystem.getMonthLastDay(Month));
					GetSystem.getMonthLastDay(Month);
					iv_right.setVisibility(View.VISIBLE);
				} else if (index == 0) {
					Day = GetSystem.GetNextData(Day, -1);
					tv_date.setText(Day);
					getDayData(Day);
					iv_right.setVisibility(View.VISIBLE);
				} else if (index == 1) {
					Day = GetSystem.GetNextData(Day, -7);
					weekData = GetSystem.getWeek(Day);
					tv_date.setText(weekData[0] + " - " + weekData[6]);
					getData(weekData[0], weekData[6]);
					iv_right.setVisibility(View.VISIBLE);
				}
				break;
			case R.id.iv_right:
				if (index == 2) {
					Month = GetSystem.GetNextMonth(Month, 1).getMonth();
					tv_date.setText(Month);
					getData(Month + "-01", GetSystem.getMonthLastDay(Month));
					boolean isMax = GetSystem.maxTime(GetSystem.getMonthLastDay(Month) + " 00:00:00", GetSystem.GetNowMonth().getDay() + " 00:00:00");
					if (isMax) {
						iv_right.setVisibility(View.GONE);
					}
				} else if (index == 0) {
					Day = GetSystem.GetNextData(Day, 1);
					tv_date.setText(Day);
					getDayData(Day);
					boolean isMax = GetSystem.maxTime(Day + " 00:00:00", GetSystem.GetNowMonth().getDay() + " 00:00:00");
					if (isMax) {
						iv_right.setVisibility(View.GONE);
					}
				} else if (index == 1) {
					Day = GetSystem.GetNextData(Day, 7);
					weekData = GetSystem.getWeek(Day);
					tv_date.setText(weekData[0] + " - " + weekData[6]);
					getData(weekData[0], weekData[6]);
					boolean isMax = GetSystem.maxTime(weekData[6] + " 00:00:00", GetSystem.GetNowMonth().getDay() + " 00:00:00");
					if (isMax) {
						iv_right.setVisibility(View.GONE);
					}
				}
				break;
			case R.id.tasks_view:// index_car
				Intent intent = new Intent(FuelActivity.this, FuelDetailsActivity.class);
				intent.putExtra("carData", carData);
				startActivity(intent);
				break;
			}
		}
	};

	int money = 9;

	Handler handler = new Handler() {
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

	/** 获取一段时间数据 **/
	private void getData(String fristDate, String lastDate) {
		try {
			String Gas_no = "";
			if (carData.getGas_no() == null || carData.getGas_no().equals("")) {
				Gas_no = "93#(92#)";
			} else {
				Gas_no = carData.getGas_no();
			}
			String url = Constant.BaseUrl + "device/" + carData.getDevice_id() + "/total?auth_code=" + app.auth_code + "&start_day=" + fristDate + "&end_day="
					+ lastDate + "&city=" + URLEncoder.encode(app.City, "UTF-8") + "&gas_no=" + Gas_no;
			new NetThread.GetDataThread(handler, url, getData).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 获取某一天数据 **/
	private void getDayData(String Date) {
		try {
			String Gas_no = "";
			if (carData.getGas_no() == null || carData.getGas_no().equals("")) {
				Gas_no = "93#(92#)";
			} else {
				Gas_no = carData.getGas_no();
			}
			String url = Constant.BaseUrl + "device/" + carData.getDevice_id() + "/day_total?auth_code=" + app.auth_code + "&day=" + Date + "&city="
					+ URLEncoder.encode(app.City, "UTF-8") + "&gas_no=" + Gas_no;
			new NetThread.GetDataThread(handler, url, getData).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 解析统计数据 **/
	private void jsonData(String str) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			String total_fee = String.format("%.1f", jsonObject.getDouble("total_fee"));
			String total_distance = String.format("%.1f", jsonObject.getDouble("total_distance"));

			if (total_fee.equals("0") && total_distance.equals("0")) {
				ll_chart.setVisibility(View.GONE);
				ll_fv.setVisibility(View.GONE);
			} else {
				if (index == 0) {
					ll_chart.setVisibility(View.GONE);
				} else {
					ll_chart.setVisibility(View.VISIBLE);
				}
				ll_fv.setVisibility(View.VISIBLE);
			}
			String total_fuel = String.format("%.1f", jsonObject.getDouble("total_fuel"));
			String avg_fuel = jsonObject.getString("avg_fuel");
			if (avg_fuel.equals("NaN") || avg_fuel.equals("null")) {
				avg_fuel = "0";
			} else {
				avg_fuel = String.format("%.1f", jsonObject.getDouble("avg_fuel"));
			}
			tv_avg_fuel.setText(avg_fuel);

			// 根据类型分别显示相应的界面
			if (type == FUEL) {
				tv_title_1.setText("总油耗");
				tv_title_3.setText("总花费");
				tv_content_1.setTextSize(30);
				tv_content_1.setText(total_fuel);
				tv_money.setTextSize(16);
				tv_money.setText("L");

				tv_content_3.setTextSize(24);
				tv_content_3.setText(total_fee);
				tv_fuel.setTextSize(16);
				tv_fuel.setText("￥");

				tv_distance.setText(total_distance);
				tv_title_map.setText("百公里平均油耗图");

			} else if (type == DISTANCE) {
				tv_title_1.setText("总行驶");
				tv_title_2.setText("总花费");
				tv_content_1.setTextSize(30);
				tv_content_1.setText(total_distance);
				tv_money.setTextSize(16);
				tv_money.setText("km");

				tv_content_2.setTextSize(24);
				tv_content_2.setText(total_fee);
				tv_distance.setTextSize(16);
				tv_distance.setText("￥");

				tv_fuel.setText(total_fuel);
				tv_title_map.setText("里程-油耗图");

			} else if (type == FEE) {
				tv_distance.setText(total_distance);
				tv_fuel.setText(total_fuel);
				tv_money.setText(total_fee);
				tv_title_map.setText("花费-油耗图");
			}

			// 周月，需要画图
			if (index != 0) {
				Efuel.clear();
				try {
					JSONArray jsonArray = jsonObject.getJSONArray("fuel_data");
					for (int i = 0; i < jsonArray.length(); i++) {
						float avg_fuel1 = 0.0f;
						if (type == FUEL) {
							if (jsonArray.getJSONObject(i).opt("avg_fuel") == null) {
								avg_fuel1 = 0;
							} else {
								avg_fuel1 = Float.valueOf(jsonArray.getJSONObject(i).getString("avg_fuel"));

							}
						} else if (type == DISTANCE) {
							avg_fuel1 = Float.valueOf(jsonArray.getJSONObject(i).getString("total_distance"));
						} else {
							avg_fuel1 = Float.valueOf(jsonArray.getJSONObject(i).getString("total_fee"));
						}
						int rcv_day = Integer.valueOf(jsonArray.getJSONObject(i).getString("rcv_day").substring(8, 10));
						String weekDate = jsonArray.getJSONObject(i).getString("rcv_day").substring(0, 10);
						Efuel.add(new EnergyItem(rcv_day, avg_fuel1, weekDate));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (index == 1) {
					// 周,把没返回的数据补齐
					// for(int i = 0 ; i < Efuel.size() ; i++){
					// //weekData的数据大于等于Efuel里的数据长度
					// if(!Efuel.get(i).getWeekDate().equals(weekData[i])){
					// int day = Integer.valueOf(weekData[i].substring(8,10));
					// Efuel.add(i,new EnergyItem(day, 0, weekData[i]));
					// }
					// }
					for (int i = 0; i < 7; i++) {
						int day = Integer.valueOf(weekData[i].substring(8, 10));
						if (Efuel.size() <= i) {
							Efuel.add(i, new EnergyItem(day, 0, weekData[i]));
						} else if (!Efuel.get(i).getWeekDate().equals(weekData[i])) {
							Efuel.add(i, new EnergyItem(day, 0, weekData[i]));
						}
					}
				} else {
					// 月
					// TODO 把没有返回的日期数据加上
					for (int i = 0; i < Efuel.size(); i++) {
						if (Efuel.get(i).getDate() != (i + 1)) {
							Efuel.add(i, new EnergyItem((i + 1), 0, ""));
						}
					}
				}
				for (int i = 0; i < Efuel.size(); i++) {
					System.out.println(Efuel.get(i).toString());
				}
				ecv_fuel.initPoints(Efuel, index, type);
				ecv_fuel.RefreshView();
			}
			// 画饼图
			rangeDatas.clear();

			JSONObject jsonObject2 = jsonObject.getJSONObject("pie");

			JSONObject idle = jsonObject2.getJSONObject("idle_range");
			String spercent = idle.getString("percent");
			if (spercent.equals("null")) {

			} else {
				int percent = idle.getInt("percent");
				if (percent > 0) {
					RangeData rangeData = new RangeData();
					rangeData.setSpeed_text(idle.getString("speed_text"));
					rangeData.setAvg_fuel(idle.getString("avg_fuel"));
					rangeData.setPercent(percent);
					rangeData.setFuel(idle.getString("fuel"));

					rangeData.setDistance(idle.getString("distance"));
					rangeData.setFee(idle.getString("fee"));
					rangeDatas.add(rangeData);
				}
			}

			JSONObject speed1 = jsonObject2.getJSONObject("speed1_range");
			String spercent1 = speed1.getString("percent");
			if (spercent1.equals("null")) {

			} else {
				int percent1 = speed1.getInt("percent");
				if (percent1 > 0) {
					RangeData rangeData1 = new RangeData();
					rangeData1.setSpeed_text(speed1.getString("speed_text"));
					rangeData1.setAvg_fuel(speed1.getString("avg_fuel"));
					rangeData1.setPercent(percent1);
					rangeData1.setFuel(speed1.getString("fuel"));

					rangeData1.setDistance(speed1.getString("distance"));
					rangeData1.setFee(speed1.getString("fee"));
					rangeDatas.add(rangeData1);
				}
			}

			JSONObject speed2 = jsonObject2.getJSONObject("speed2_range");
			String spercent2 = speed2.getString("percent");
			if (spercent2.equals("null")) {

			} else {
				int percent2 = speed2.getInt("percent");
				if (percent2 > 0) {
					RangeData rangeData2 = new RangeData();
					rangeData2.setSpeed_text(speed2.getString("speed_text"));
					rangeData2.setAvg_fuel(speed2.getString("avg_fuel"));
					rangeData2.setPercent(percent2);
					rangeData2.setFuel(speed2.getString("fuel"));

					rangeData2.setDistance(speed2.getString("distance"));
					rangeData2.setFee(speed2.getString("fee"));
					rangeDatas.add(rangeData2);
				}
			}

			JSONObject speed3 = jsonObject2.getJSONObject("speed3_range");
			String spercent3 = speed3.getString("percent");
			if (spercent3.equals("null")) {

			} else {
				int percent3 = speed3.getInt("percent");
				if (percent3 > 0) {
					RangeData rangeData3 = new RangeData();
					rangeData3.setSpeed_text(speed3.getString("speed_text"));
					rangeData3.setAvg_fuel(speed3.getString("avg_fuel"));
					rangeData3.setPercent(percent3);
					rangeData3.setFuel(speed3.getString("fuel"));

					rangeData3.setDistance(speed3.getString("distance"));
					rangeData3.setFee(speed3.getString("fee"));
					rangeDatas.add(rangeData3);
				}
			}

			JSONObject speed4 = jsonObject2.getJSONObject("speed4_range");
			String spercent4 = speed4.getString("percent");
			if (spercent4.equals("null")) {

			} else {
				int percent4 = speed4.getInt("percent");
				if (percent4 > 0) {
					RangeData rangeData4 = new RangeData();
					rangeData4.setSpeed_text(speed4.getString("speed_text"));
					rangeData4.setAvg_fuel(speed4.getString("avg_fuel"));
					rangeData4.setPercent(percent4);
					rangeData4.setFuel(speed4.getString("fuel"));

					rangeData4.setDistance(speed4.getString("distance"));
					rangeData4.setFee(speed4.getString("fee"));
					rangeDatas.add(rangeData4);
				}
			}

			fv.setDatas(rangeDatas, 0, type);
			if (rangeDatas.size() > 0) {
				RangeData rangeData = rangeDatas.get(0);
				if (type == FUEL) {
					tv_speed_text.setText(rangeData.getSpeed_text());
					tv_speed_avg_fuel.setText("平均油耗：" + rangeData.getAvg_fuel());
					tv_speed_fuel.setText("油耗：" + rangeData.getFuel());
				} else if (type == DISTANCE) {
					tv_speed_text.setText(rangeData.getSpeed_text());
					tv_speed_avg_fuel.setText("里程：" + rangeData.getDistance());
					tv_speed_fuel.setText("油耗：" + rangeData.getFuel());
				} else {
					tv_speed_text.setText(rangeData.getSpeed_text());
					tv_speed_avg_fuel.setText("花费：" + rangeData.getFee());
					tv_speed_fuel.setText("油耗：" + rangeData.getFuel());
				}
			} else {
				tv_speed_text.setText("");
				tv_speed_avg_fuel.setText("");
				tv_speed_fuel.setText("");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	List<RangeData> rangeDatas = new ArrayList<RangeData>();

	public class RangeData {
		String speed_text;
		int percent;
		String avg_fuel;
		String fuel;
		String distance;// 路程
		String fee;// 花费

		public String getDistance() {
			return distance;
		}

		public void setDistance(String distance) {
			this.distance = distance;
		}

		public String getFee() {
			return fee;
		}

		public void setFee(String fee) {
			this.fee = fee;
		}

		public String getSpeed_text() {
			return speed_text;
		}

		public void setSpeed_text(String speed_text) {
			this.speed_text = speed_text;
		}

		public int getPercent() {
			return percent;
		}

		public void setPercent(int percent) {
			this.percent = percent;
		}

		public String getAvg_fuel() {
			return avg_fuel;
		}

		public void setAvg_fuel(String avg_fuel) {
			this.avg_fuel = avg_fuel;
		}

		public String getFuel() {
			return fuel;
		}

		public void setFuel(String fuel) {
			this.fuel = fuel;
		}
	}

	private void initData() {
		index = 2;
		Month = GetSystem.GetNowMonth().getMonth();
		tv_date.setText(Month);
		getData(Month + "-01", GetSystem.getMonthLastDay(Month));
	}

	private void setBg() {
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