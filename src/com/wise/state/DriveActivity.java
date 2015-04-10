package com.wise.state;

import java.net.URLEncoder;
import org.json.JSONException;
import org.json.JSONObject;
import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.NetThread;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.ui.widget.DialView;
import com.wise.baba.util.DialBitmapFactory;
import com.wise.car.TravelActivity;
import data.CarData;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/** 驾驶习惯 **/
public class DriveActivity extends Activity {
	private static final int getData = 1;
	TextView tv_drive, tv_advice, tv_safe, tv_eco, tv_env, tv_distance,
			tv_fuel, tv_avg_fuel, tv_date;
	ImageView iv_right;
	DialView dialDriveScore;
	String Date = "";
	String Device_id = "";
	/** 把最近的数据存储 **/
	boolean isNearData = false;
	CarData carData;
	AppApplication app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_drive);
		app = (AppApplication) getApplication();
		dialDriveScore = (DialView) findViewById(R.id.dialDriveScore);
		Button bt_drive_rank = (Button) findViewById(R.id.bt_drive_rank);
		bt_drive_rank.setOnClickListener(onClickListener);
		Button bt_drive_travel = (Button) findViewById(R.id.bt_drive_travel);
		bt_drive_travel.setOnClickListener(onClickListener);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		ImageView iv_left = (ImageView) findViewById(R.id.iv_left);
		iv_left.setOnClickListener(onClickListener);
		iv_right = (ImageView) findViewById(R.id.iv_right);
		iv_right.setOnClickListener(onClickListener);
		tv_advice = (TextView) findViewById(R.id.tv_advice);
		tv_safe = (TextView) findViewById(R.id.tv_safe);
		tv_eco = (TextView) findViewById(R.id.tv_eco);
		tv_env = (TextView) findViewById(R.id.tv_env);
		tv_distance = (TextView) findViewById(R.id.tv_distance);
		tv_fuel = (TextView) findViewById(R.id.tv_fuel);
		tv_avg_fuel = (TextView) findViewById(R.id.tv_avg_fuel);
		tv_date = (TextView) findViewById(R.id.tv_date);
		tv_drive = (TextView) findViewById(R.id.tv_drive);

		TextView tv_name = (TextView) findViewById(R.id.tv_name);
		isNearData = getIntent().getBooleanExtra("isNearData", false);
		carData = (CarData) getIntent().getSerializableExtra("carData");
		if(carData == null && app.carDatas.size()>0){
			carData = app.carDatas.get(0);
		}
		
		tv_name.setText(carData.getNick_name());
		Device_id = carData.getDevice_id();
		Date = GetSystem.GetNowMonth().getDay();
		tv_date.setText(Date);
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
				tv_date.setText(Date);
				getDriveData();
				iv_right.setVisibility(View.VISIBLE);
				break;
			case R.id.iv_right:
				Date = GetSystem.GetNextData(Date, 1);
				tv_date.setText(Date);
				getDriveData();
				boolean isMax = GetSystem.maxTime(Date + " 00:00:00", GetSystem
						.GetNowMonth().getDay() + " 00:00:00");
				if (isMax) {
					iv_right.setVisibility(View.GONE);
				}
				break;
			case R.id.bt_drive_rank:
				// TODO 驾驶排行
				startActivity(new Intent(DriveActivity.this,
						DriveRankActivity.class));
				break;
			case R.id.bt_drive_travel:
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
			// 没有返回数据则跳过
			return;
		}
		int drive_score = 0;
		try {
			JSONObject jsonObject = new JSONObject(Data);
			drive_score = jsonObject.getInt("drive_score");
			int safe_score = jsonObject.getInt("safe_score");
			int eco_score = jsonObject.getInt("eco_score");
			int env_score = jsonObject.getInt("env_score");
			String drive_advice = jsonObject.getString("drive_advice");
			String total_fee = jsonObject.getString("total_fee");
			String total_distance = jsonObject.getString("total_distance");
			String total_fuel = jsonObject.getString("total_fuel");
			String avg_fuel = jsonObject.getString("avg_fuel");
			dialDriveScore.initValue(drive_score,handler);
			tv_advice.setText(drive_advice);
			tv_safe.setText("" + safe_score);
			tv_eco.setText("" + eco_score);
			tv_env.setText("" + env_score);
			tv_distance.setText(total_distance);
			tv_fuel.setText(total_fuel);
			tv_avg_fuel.setText(avg_fuel.equals("null") ? "0" : avg_fuel);
			tv_drive.setText("" + drive_score);
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
