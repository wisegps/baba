package com.wise.state;

import java.net.URLEncoder;
import org.json.JSONException;
import org.json.JSONObject;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.biz.GetUrl;
import com.wise.baba.entity.CarData;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.widget.DialView;
import com.wise.baba.util.DateUtil;
import com.wise.baba.util.DialBitmapFactory;
import com.wise.car.CarLocationActivity;
import com.wise.car.TravelActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/** 驾驶习惯 **/
public class DriveActivity extends Activity {
	private static final int getData = 1;
	private static final int getGps = 2;
	TextView tv_drive, tv_advice, tv_safe, tv_eco, tv_env, tv_distance,
			tv_fuel, tv_avg_fuel, tv_date,tv_spd_up,tv_spd_down,tv_spd_stop,tv_name,tv_location;
	ImageView iv_right,iv_location;
	DialView dialDriveScore;
	String Date = "";
	String Device_id = "";
	/** 把最近的数据存储 **/
	boolean isNearData = false;
	CarData carData;
	AppApplication app;
	DateUtil dateUtil;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_drive);
		dateUtil = new DateUtil();
		app = (AppApplication) getApplication();
		dialDriveScore = (DialView) findViewById(R.id.dialDriveScore);
		TextView tv_drive_rank = (TextView) findViewById(R.id.tv_drive_rank);
		tv_drive_rank.setOnClickListener(onClickListener);
		TextView tv_drive_travel = (TextView) findViewById(R.id.tv_drive_travel);
		tv_drive_travel.setOnClickListener(onClickListener);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		ImageView iv_left = (ImageView) findViewById(R.id.iv_left);
		iv_left.setOnClickListener(onClickListener);
		iv_right = (ImageView) findViewById(R.id.iv_right);
		iv_right.setOnClickListener(onClickListener);
		iv_right.setEnabled(false);
		tv_advice = (TextView) findViewById(R.id.tv_advice);
		tv_safe = (TextView) findViewById(R.id.tv_safe);
		tv_eco = (TextView) findViewById(R.id.tv_eco);
		tv_env = (TextView) findViewById(R.id.tv_env);
		tv_distance = (TextView) findViewById(R.id.tv_distance);
		tv_fuel = (TextView) findViewById(R.id.tv_fuel);
		tv_avg_fuel = (TextView) findViewById(R.id.tv_avg_fuel);
		tv_date = (TextView) findViewById(R.id.tv_date);
		tv_drive = (TextView) findViewById(R.id.tv_drive);
		
		tv_spd_up = (TextView) findViewById(R.id.tv_speed_up);
		tv_spd_down = (TextView) findViewById(R.id.tv_speed_down);
		tv_spd_stop = (TextView) findViewById(R.id.tv_speed_stop);
		
		tv_name = (TextView) findViewById(R.id.tv_name);
		tv_location = (TextView) findViewById(R.id.tv_location);
		iv_location = (ImageView) findViewById(R.id.iv_location);
		
		findViewById(R.id.rlytLocation).setOnClickListener(onClickListener);
		
		isNearData = getIntent().getBooleanExtra("isNearData", false);
		carData = (CarData) getIntent().getSerializableExtra("carData");
		if(carData == null && app.carDatas.size()>0){
			carData = app.carDatas.get(app.currentCarIndex);
		}
		
		tv_name.setText(carData.getNick_name());
		
		tv_location.setText(carData.getAdress());
		
		boolean is_online = getIntent().getBooleanExtra("is_online", false);
		if(is_online){
			iv_location.setImageResource(R.drawable.ico_location_on);
			iv_location.setTag(is_online);
			tv_location.setTextColor(Color.parseColor("#50b7de"));
			tv_location.setAlpha(0.6f);
			
			tv_date.setTextColor(Color.parseColor("#50b7de"));
			tv_date.setAlpha(1.0f);
			
			//imgOnLine.setImageResource(R.drawable.ico_key);
		}else{
			//imgOnLine.setImageResource(R.drawable.ico_key_close);
			iv_location.setImageResource(R.drawable.ico_location_off);
			iv_location.setTag(is_online);
			tv_location.setTextColor(Color.BLACK);
			tv_location.setAlpha(0.3f);
			
			tv_date.setTextColor(Color.BLACK);
			tv_date.setAlpha(0.3f);
			
		}
		
		
		
		Device_id = carData.getDevice_id();
		Date = GetSystem.GetNowMonth().getDay();
		
		
		tv_date.setText(dateUtil.toChineseDate(Date));
		getDriveData();
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.iv_left:
				Date = GetSystem.GetNextData(Date, -1);
				tv_date.setText(dateUtil.toChineseDate(Date));
				getDriveData();
				iv_right.setEnabled(true);
				break;
			case R.id.iv_right:
				Date = GetSystem.GetNextData(Date, 1);
				tv_date.setText(dateUtil.toChineseDate(Date));
				getDriveData();
				boolean isMax = GetSystem.maxTime(Date + " 00:00:00", GetSystem
						.GetNowMonth().getDay() + " 00:00:00");
				if (isMax) {
					iv_right.setEnabled(false);
				}
				break;
			case R.id.tv_drive_rank:
				// TODO 驾驶排行
				startActivity(new Intent(DriveActivity.this,
						DriveRankActivity.class));
				break;
				
			case R.id.tv_drive_travel:
				Intent intent = new Intent(DriveActivity.this,
						TravelActivity.class);
				intent.putExtra("device_id", Device_id);
				String Gas_no = "93#(92#)";
				;
				if (carData.getGas_no() != null) {
					Gas_no = carData.getGas_no();
				}
				intent.putExtra("Gas_no", Gas_no);
				intent.putExtra("Date", Date);
				startActivity(intent);
				break;
			case R.id.rlytLocation:
				Intent intentLocation = new Intent(DriveActivity.this, CarLocationActivity.class);
				intentLocation.putExtra("index", app.currentCarIndex);
				intentLocation.putExtra("isHotLocation", true);
				startActivity(intentLocation);
				break;
			}
		}
	};
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case getData:
				jsonData(msg.obj.toString());
				break;
			case getGps:
				break;
			default:
				break;
			}
		}
	};

	/** 获取驾驶习惯 **/
	private void getDriveData() {
		try {
			String Gas_no = "";
			if (carData.getGas_no() == null || carData.getGas_no().equals("")) {
				Gas_no = "93#(92#)";
			} else {
				Gas_no = carData.getGas_no();
			}
			String url = Constant.BaseUrl + "device/" + Device_id
					+ "/day_drive?auth_code=" + app.auth_code + "&day=" + Date
					+ "&city=" + URLEncoder.encode(app.City, "UTF-8")
					+ "&gas_no=" + Gas_no;
			new NetThread.GetDataThread(handler, url, getData).start();
			// 获取gps信息
			String gpsUrl = GetUrl.getCarGpsData(Device_id, app.auth_code);
			new NetThread.GetDataThread(handler, gpsUrl, getGps, app.currentCarIndex).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	// 设置圆环刻度值
//	public void setCircleBitmapValue(ImageView imgView, int value) {
//		DialBitmapFactory factory = new DialBitmapFactory(this);
//		Bitmap mBitmap = factory.getBitmapByValue(value,true);
//		imgView.setImageBitmap(mBitmap);
//	}

	private void jsonData(String Data) {
		
		Log.i("DriveActivity", Data);
		if (Data == null || Data.equals("")) {

			dialDriveScore.initValue(0,handler);
			tv_advice.setText("");
			tv_safe.setText("" + 0);
			tv_eco.setText("" + 0);
			tv_env.setText("" + 0);
			tv_distance.setText("" + 0);
			tv_fuel.setText("" + 0);
			tv_avg_fuel.setText("" + 0);
			tv_drive.setText("" + 0);
			tv_spd_up.setText("" + 0);;
			tv_spd_down.setText("" + 0);;
			tv_spd_stop.setText("" + 0);;

			// 没有返回数据则跳过
			return;
		}
		int drive_score = 0;
		try {
			JSONObject jsonObject = new JSONObject(Data);
			
			Log.i("DriveActivity", Data);
			drive_score = jsonObject.getInt("drive_score");
			int safe_score = jsonObject.getInt("safe_score");
			int eco_score = jsonObject.getInt("eco_score");
			int env_score = jsonObject.getInt("env_score");
			String drive_advice = jsonObject.getString("drive_advice");
			String total_fee = jsonObject.getString("total_fee");
			String total_distance = jsonObject.getString("total_distance");
			String total_fuel = jsonObject.getString("total_fuel");
			String avg_fuel = jsonObject.getString("avg_fuel");
			
			int quick_accel = jsonObject.getInt("quick_accel");
			int quick_break = jsonObject.getInt("quick_break");
			int quick_reflexes = jsonObject.getInt("quick_reflexes");
			
			dialDriveScore.initValue(drive_score,handler);
			tv_advice.setText(drive_advice);
			tv_safe.setText("" + safe_score);
			tv_eco.setText("" + eco_score);
			tv_env.setText("" + env_score);
			tv_distance.setText(total_distance);
			tv_fuel.setText(total_fuel);
			tv_avg_fuel.setText(avg_fuel.equals("null") ? "0" : avg_fuel);
			tv_drive.setText("" + drive_score);
			
			tv_spd_up.setText("" + quick_accel);;
			tv_spd_down.setText("" + quick_break);;
			tv_spd_stop.setText("" + quick_reflexes);;
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (isNearData) {

		} else {
			// 最近值，且不为0，需要存储
			if (drive_score != 0) {
				isNearData = true;
				SharedPreferences preferences = getSharedPreferences(
						Constant.sharedPreferencesName, Context.MODE_PRIVATE);
				Editor editor = preferences.edit();
				editor.putString(Constant.sp_drive_score + carData.getObj_id(),
						Data);
				editor.commit();
			}
		}
	}
}
