package com.wise.baba;

import java.util.Calendar;

import javax.crypto.spec.IvParameterSpec;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

public class AirSettingActivity extends Activity {

	private TextView tvDuration, tv_air_timer;
	private LinearLayout llytSetDuration, llytDuration, llytTimer;

	private RelativeLayout rlyt_air_timer;
	private ImageView imgDuration[] = new ImageView[5];
	private String time = "00:00";
	private ImageView imgRight;
	private int imgDurationId[] = new int[] { R.id.iv_duration_30,
			R.id.iv_duration_60, R.id.iv_duration_90, R.id.iv_duration_100,
			R.id.iv_duration_120 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_air_setting);
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
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus) {

			View viewLine = findViewById(R.id.viewAirLine);

			int width = viewLine.getMeasuredWidth() / 5 * 4;
			FrameLayout.LayoutParams params = (LayoutParams) viewLine
					.getLayoutParams();
			params.width = width;
			viewLine.setLayoutParams(params);
			viewLine.invalidate();
			llytDuration.setVisibility(View.INVISIBLE);
			llytDuration.setVisibility(View.GONE);
		}

	}

	public OnTouchListener onTouchListner = new OnTouchListener() {

		@Override
		public boolean onTouch(View view, MotionEvent arg1) {

			switch (view.getId()) {
			case R.id.iv_duration_30:
			case R.id.iv_duration_60:
			case R.id.iv_duration_90:
			case R.id.iv_duration_100:
			case R.id.iv_duration_120:
				onChange(view.getId());
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

		final TimePicker timePicker = new TimePicker(this);
		Calendar calendar = Calendar.getInstance();
		timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
		timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
		timePicker.setIs24HourView(true);

		timePicker.setOnTimeChangedListener(new OnTimeChangedListener() {

			@Override
			public void onTimeChanged(TimePicker timer, int arg1, int arg2) {
				time = timer.getCurrentHour() + ":" + timer.getCurrentMinute();
				if(time.length()<5){
					time = "0"+time;
				}
			}
		});
		AlertDialog ad = new AlertDialog.Builder(this)
				.setTitle("定时")
				.setView(timePicker)
				.setPositiveButton("设置", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						tv_air_timer.setText(time);
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						tv_air_timer.setText("00:00");
					}
				}).show();

	}

}
