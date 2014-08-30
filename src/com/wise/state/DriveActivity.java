package com.wise.state;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.json.JSONException;
import org.json.JSONObject;
import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.NetThread;
import pubclas.Variable;
import com.wise.baba.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**驾驶习惯**/
public class DriveActivity extends Activity{
	
	private static final int getData = 1;
	
	TextView tv_drive, tv_advice,tv_safe,tv_eco,tv_env,tv_distance,tv_fuel,tv_avg_fuel,tv_date;
	ImageView iv_right;
	TasksCompletedView mTasksView;
	String Date = "";
	String Device_id = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_drive);
		mTasksView = (TasksCompletedView)findViewById(R.id.tasks_view);
		Button bt_drive_rank = (Button)findViewById(R.id.bt_drive_rank);
		bt_drive_rank.setOnClickListener(onClickListener);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		ImageView iv_left = (ImageView)findViewById(R.id.iv_left);
		iv_left.setOnClickListener(onClickListener);
		iv_right = (ImageView)findViewById(R.id.iv_right);
		iv_right.setOnClickListener(onClickListener);
		tv_advice = (TextView)findViewById(R.id.tv_advice);
		tv_safe = (TextView)findViewById(R.id.tv_safe);
		tv_eco = (TextView)findViewById(R.id.tv_eco);
		tv_env = (TextView)findViewById(R.id.tv_env);
		tv_distance = (TextView)findViewById(R.id.tv_distance);
		tv_fuel = (TextView)findViewById(R.id.tv_fuel);
		tv_avg_fuel = (TextView)findViewById(R.id.tv_avg_fuel);
		tv_date = (TextView)findViewById(R.id.tv_date);
		tv_drive = (TextView)findViewById(R.id.tv_drive);
		
		TextView tv_name = (TextView)findViewById(R.id.tv_name);
		int index_car = getIntent().getIntExtra("index_car", 0);
		tv_name.setText(Variable.carDatas.get(index_car).getNick_name());
		Device_id = Variable.carDatas.get(index_car).getDevice_id();
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
				boolean isMax = GetSystem.maxTime(Date + " 00:00:00", GetSystem.GetNowMonth().getDay() + " 00:00:00");
				if(isMax){
					iv_right.setVisibility(View.GONE);
				}
				break;
			case R.id.bt_drive_rank:
				//TODO 驾驶排行
				startActivity(new Intent(DriveActivity.this, DriveRankActivity.class));
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
				jsonData(msg.obj.toString());
				break;

			default:
				break;
			}
		}			
	};
	/**获取驾驶习惯**/
	private void getDriveData(){
		try {
			String url = Constant.BaseUrl + "device/" + Device_id + "/day_drive?auth_code=" + Variable.auth_code + 
						"&day=" + Date + "&city=" + URLEncoder.encode(Variable.City, "UTF-8") + "&gas_no=93#";
			new NetThread.GetDataThread(handler, url, getData).start();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	private void jsonData(String Data){
		try {
			JSONObject jsonObject = new JSONObject(Data);
			int drive_score = jsonObject.getInt("drive_score");
			int safe_score = jsonObject.getInt("safe_score");
			int eco_score = jsonObject.getInt("eco_score");
			int env_score = jsonObject.getInt("env_score");
			String drive_advice = jsonObject.getString("drive_advice");
			String total_fee = jsonObject.getString("total_fee");
			String total_distance = jsonObject.getString("total_distance");
			String total_fuel = jsonObject.getString("total_fuel");
			String avg_fuel = jsonObject.getString("avg_fuel");
			mTasksView.setProgress(drive_score);
			tv_advice.setText(drive_advice);
			tv_safe.setText(""+safe_score);
			tv_eco.setText(""+eco_score);
			tv_env.setText(""+env_score);
			tv_distance.setText(total_distance);
			tv_fuel.setText(total_fuel);
			tv_avg_fuel.setText(avg_fuel.equals("null")? "0":avg_fuel);
			tv_drive.setText(""+drive_score);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}