package com.wise.baba;

import java.util.Date;
import java.util.HashMap;

import org.xclcharts.common.DensityUtil;

import com.wise.baba.app.Const;
import com.wise.baba.app.Msg;
import com.wise.baba.biz.HttpAir;
import com.wise.baba.entity.Air;
import com.wise.baba.util.DateUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

public class AirSettingActivity extends Activity {

	private TextView tvDuration, tv_air_timer;
	private LinearLayout llytSetDuration, llytDuration, llytTimer;
	private Switch switchMode, switchTimer;
	private RelativeLayout rlyt_air_timer;
	private ImageView imgDuration[] = new ImageView[5];
	private String time = "00:00";
	private ImageView imgRight;

	private int imgDurationId[] = new int[] { R.id.iv_duration_30,
			R.id.iv_duration_60, R.id.iv_duration_90, R.id.iv_duration_100,
			R.id.iv_duration_120 };

	private HashMap<Integer, Integer> mapDurationId = new HashMap<Integer, Integer>();

	public HttpAir httpAir;

	private int carIndex = 0;

	private String deviceId = "";
	

	private Handler uiHandler = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_air_setting);

		uiHandler = new Handler( handleCallBack);
		
		carIndex = getIntent().getIntExtra("carIndex", 0);
		deviceId = getIntent().getStringExtra("deviceId");

		httpAir = new HttpAir(this, uiHandler);
		httpAir.requestAir(carIndex);
		switchMode = (Switch) findViewById(R.id.switchMode);
		switchTimer = (Switch) findViewById(R.id.switchTimer);

		switchMode.setOnTouchListener(onTouchListner);
		switchTimer.setOnTouchListener(onTouchListner);

		findViewById(R.id.iv_back).setOnClickListener(onClickListener);
		llytSetDuration = (LinearLayout) findViewById(R.id.llytSetDuration);
		llytDuration = (LinearLayout) findViewById(R.id.llytDuration);
		llytTimer = (LinearLayout) findViewById(R.id.llytTimer);
		llytSetDuration.setOnClickListener(onClickListener);
		llytDuration.setOnClickListener(onClickListener);
		llytTimer.setOnClickListener(onClickListener);
		tvDuration = (TextView) findViewById(R.id.tv_duration);
		tv_air_timer = (TextView) findViewById(R.id.tv_air_timer);
		imgRight = (ImageView) findViewById(R.id.imgRight);
		rlyt_air_timer = (RelativeLayout) findViewById(R.id.rlyt_air_timer);
		rlyt_air_timer.setOnClickListener(onClickListener);

		for (int i = 0; i < 5; i++) {
			int id = imgDurationId[i];
			imgDuration[i] = (ImageView) findViewById(id);
			imgDuration[i].setOnTouchListener(onTouchListner);
		}

		initDurationLayout();
	}

	/**
	 * 初始化界面
	 * 
	 * @param air
	 */
	public void initValue(Air air) {
		int mode = air.getAirMode();
		switchMode.setChecked(false);
		switchTimer.setChecked(false);
		if (mode == Const.AIR_MODE_SMART) {
			switchMode.setChecked(true);
		} else if (mode == Const.AIR_MODE_TIMER) {
			switchTimer.setChecked(true);
			time = air.getAirTime();
			tv_air_timer.setText(air.getAirTime());
			mapDurationId.put(0, R.id.iv_duration_30);
			mapDurationId.put(30, R.id.iv_duration_30);
			mapDurationId.put(60, R.id.iv_duration_60);
			mapDurationId.put(90, R.id.iv_duration_90);
			mapDurationId.put(100, R.id.iv_duration_100);
			mapDurationId.put(120, R.id.iv_duration_120);

			int duration = air.getAirDuration();
			Log.i("AirSettingActivity", duration+"");
			int id = mapDurationId.get(duration);
			onChange(id);
			llytDuration.setVisibility(View.VISIBLE);
			
		}
	}

	/**
	 * 设置下面时间设置布局，
	 */
	public void initDurationLayout() {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int mScreenWidth = dm.widthPixels;// 获取屏幕分辨率宽度
		int padding = DensityUtil.dip2px(this, 20); // llytDuration padding*2

		View viewLine = findViewById(R.id.viewAirLine);

		int width = (mScreenWidth - padding) / 5 * 4;
		FrameLayout.LayoutParams params = (LayoutParams) viewLine
				.getLayoutParams();
		params.width = width;
		viewLine.setLayoutParams(params);
		viewLine.invalidate();
		llytDuration.setVisibility(View.INVISIBLE);
		llytDuration.setVisibility(View.GONE);

	}

	public Handler.Callback handleCallBack = new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case Msg.Set_Air_Response:
				// Toast.makeText(AirSettingActivity.this, "设置成功",
				// Toast.LENGTH_SHORT).show();
				break;

			case Msg.Get_Air_Value:
				initValue((Air) msg.obj);
				break;
			}
			return false;
		}

	};

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus) {

		}

	}

	public OnTouchListener onTouchListner = new OnTouchListener() {

		@Override
		public boolean onTouch(View view, MotionEvent event) {

			switch (view.getId()) {
			case R.id.iv_duration_30:
			case R.id.iv_duration_60:
			case R.id.iv_duration_90:
			case R.id.iv_duration_100:
			case R.id.iv_duration_120:
				onChange(view.getId());
				break;
			case R.id.switchMode:
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					switchTimer.setChecked(false);
					llytDuration.setVisibility(View.GONE);
				}
				break;
			case R.id.switchTimer:
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					switchMode.setChecked(false);
					if (switchTimer.isChecked()) {
						llytDuration.setVisibility(View.GONE);
					} else {
						llytDuration.setVisibility(View.VISIBLE);
					}
				}
				break;
			}

			return false;
		}
	};

	public OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.iv_back:
				setMode();
				finish();
				break;
			case R.id.llytSetDuration:

				if (llytDuration.getVisibility() == View.VISIBLE) {
					llytDuration.setVisibility(View.GONE);
					imgRight.invalidate();

				} else {
					llytDuration.setVisibility(View.VISIBLE);

					RotateAnimation ra = new RotateAnimation(0, 90, 0.5f, 0.5f);
					ra.setDuration(100);
					ra.setFillAfter(true);
					imgRight.startAnimation(ra);
				}

				break;
			case R.id.rlyt_air_timer:
				showTimeDialog();
				break;
			}
		}
	};

	public void onChange(int id) {
		int values[] = { 30, 60, 90, 100, 120 };
		int index = 0;
		for (int i = 0; i < 5; i++) {
			ImageView img = (ImageView) findViewById(imgDurationId[i]);
			if (imgDurationId[i] == id) {
				index = i;
				img.setImageResource(R.drawable.ico_switch_thum);
			} else {
				img.setImageResource(R.drawable.ico_circle_gray);
			}

		}

		tvDuration.setText(values[index] + "");

	}

	public void showTimeDialog() {

		String current = tv_air_timer.getText().toString().trim();

		final TimePicker timePicker = new TimePicker(this);
		Date date = DateUtil.getDialogTime(current);
		timePicker.setCurrentHour(date.getHours());
		timePicker.setCurrentMinute(date.getMinutes());
		timePicker.setIs24HourView(true);

		// timePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
		//
		// @Override
		// public void onTimeChanged(TimePicker timer, int arg1, int arg2) {
		//
		// int hour = timer.getCurrentHour();
		// int minute = timer.getCurrentMinute();
		// if(){}
		//
		// time = timer.getCurrentHour() + ":" + timer.getCurrentMinute();
		// if (time.length() < 5) {
		// time = "0" + time;
		// }
		// }
		// });
		AlertDialog ad = new AlertDialog.Builder(this)
				.setTitle("定时")
				.setView(timePicker)
				.setPositiveButton("设置", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						String hour = timePicker.getCurrentHour() + "";

						String minute = timePicker.getCurrentMinute() + "";

						if (hour.length() < 2) {
							hour = "0" + hour;
						}

						if (minute.length() < 2) {
							minute = "0" + minute;
						}
						time = hour + ":" + minute;
						tv_air_timer.setText(time);
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						tv_air_timer.setText(time);
					}
				}).show();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setMode();
		}
		
		return super.onKeyDown(keyCode, event);
	}

	public void setMode() {
		int mode = Const.AIR_MODE_MANUL;
		if (switchMode.isChecked()) {
			mode = Const.AIR_MODE_SMART;
		}

		if (switchTimer.isChecked()) {
			mode = Const.AIR_MODE_TIMER;
		}

		int duration = Integer.parseInt(tvDuration.getText().toString());
		httpAir.setMode(deviceId, mode, time, duration);
	}

}
