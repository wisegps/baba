package com.wise.remind;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import wheel.widget.OnWheelScrollListener;
import wheel.widget.WheelView;
import widget.adapters.ArrayWheelAdapter;
import widget.adapters.NumericWheelAdapter;
import com.umeng.analytics.MobclickAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.entity.CarData;
import com.wise.baba.net.NetThread;
import com.wise.car.CarActivity;
import com.wise.car.CarAddActivity;
import com.wise.setting.CaptchaActivity;
import com.wise.setting.RegisterActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class RemindAddActivity extends Activity {

	private static final int save = 1;
	String remind_way = "2";
	String remind_time;
	Spinner s_type, s_car, s_mode;
	TextView tv_before0, tv_before1, tv_before3, tv_before7, tv_before30,
			tv_remind_time, tv_before_note;
	EditText et_mileage, et_content;
	LinearLayout ll_car, ll_mileage, ll_content;
	AppApplication app;
	String cust_id;
	List<CarData> carDatas ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_car_remind_add);
		app = (AppApplication) getApplication();
		cust_id = getIntent().getStringExtra("cust_id");
        carDatas = app.carDatas;
        //(List<CarData>) getIntent().getSerializableExtra("carDatas");
		ll_car = (LinearLayout) findViewById(R.id.ll_car);
		ll_mileage = (LinearLayout) findViewById(R.id.ll_mileage);
		ll_content = (LinearLayout) findViewById(R.id.ll_content);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		ImageView iv_ok = (ImageView) findViewById(R.id.iv_ok);
		iv_ok.setOnClickListener(onClickListener);

		tv_before_note = (TextView) findViewById(R.id.tv_before_note);
		tv_before0 = (TextView) findViewById(R.id.tv_before0);
		tv_before0.setOnClickListener(onClickListener);
		tv_before1 = (TextView) findViewById(R.id.tv_before1);
		tv_before1.setOnClickListener(onClickListener);
		tv_before3 = (TextView) findViewById(R.id.tv_before3);
		tv_before3.setOnClickListener(onClickListener);
		tv_before7 = (TextView) findViewById(R.id.tv_before7);
		tv_before7.setOnClickListener(onClickListener);
		tv_before30 = (TextView) findViewById(R.id.tv_before30);
		tv_before30.setOnClickListener(onClickListener);
		et_mileage = (EditText) findViewById(R.id.et_mileage);
		et_content = (EditText) findViewById(R.id.et_content);
		tv_remind_time = (TextView) findViewById(R.id.tv_remind_time);

		s_type = (Spinner) findViewById(R.id.s_type);
		ArrayAdapter<String> type = new ArrayAdapter<String>(
				RemindAddActivity.this, android.R.layout.simple_spinner_item,
				Constant.items_note_type);
		type.setDropDownViewResource(R.layout.drop_down_item);
		s_type.setAdapter(type);
		s_type.setOnItemSelectedListener(onTypeItemSelectedListener);
		s_car = (Spinner) findViewById(R.id.s_car);
		ArrayAdapter<String> car = new ArrayAdapter<String>(
				RemindAddActivity.this, android.R.layout.simple_spinner_item,
				getCars());
		car.setDropDownViewResource(R.layout.drop_down_item);
		s_car.setAdapter(car);
		s_mode = (Spinner) findViewById(R.id.s_mode);
		ArrayAdapter<String> mode = new ArrayAdapter<String>(
				RemindAddActivity.this, android.R.layout.simple_spinner_item,
				Constant.items_note_mode);
		mode.setDropDownViewResource(R.layout.drop_down_item);
		s_mode.setAdapter(mode);
		setDate();
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.iv_ok:
				if (app.isTest) {
					Toast.makeText(RemindAddActivity.this, "演示账号不支持该功能",
							Toast.LENGTH_SHORT).show();
					return;
				}
				save();
				break;
			case R.id.tv_before0:
				remind_way = "0";
				setWhite();
				tv_before0.setBackgroundResource(R.drawable.bg_qianlan);
				tv_before_note.setText("提醒 不提前");
				break;
			case R.id.tv_before1:
				remind_way = "1";
				setWhite();
				tv_before1.setBackgroundResource(R.drawable.bg_qianlan);
				tv_before_note.setText("提醒 提前一天");
				break;
			case R.id.tv_before3:
				remind_way = "2";
				setWhite();
				tv_before3.setBackgroundResource(R.drawable.bg_qianlan);
				tv_before_note.setText("提醒 提前三天");
				break;
			case R.id.tv_before7:
				remind_way = "3";
				setWhite();
				tv_before7.setBackgroundResource(R.drawable.bg_qianlan);
				tv_before_note.setText("提醒 提前七天");
				break;
			case R.id.tv_before30:
				remind_way = "4";
				setWhite();
				tv_before30.setBackgroundResource(R.drawable.bg_qianlan);
				tv_before_note.setText("提醒 提前一个月");
				break;
			}
		}
	};
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case save:
				try {
					JSONObject jsonObject = new JSONObject(msg.obj.toString());
					if (jsonObject.getString("status_code").equals("0")) {
						setResult(3);
						finish();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			}
		}
	};

	OnItemSelectedListener onTypeItemSelectedListener = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if (arg2 == 0) {// 驾照换证
				ll_car.setVisibility(View.GONE);
				ll_mileage.setVisibility(View.GONE);
				ll_content.setVisibility(View.GONE);
			} else if (arg2 == 5) {// 通用提醒
				ll_car.setVisibility(View.GONE);
				ll_mileage.setVisibility(View.GONE);
				ll_content.setVisibility(View.VISIBLE);
			} else if (arg2 == 2) {// 车辆保养
				ll_car.setVisibility(View.VISIBLE);
				ll_mileage.setVisibility(View.VISIBLE);
				ll_content.setVisibility(View.GONE);
				if (carDatas.size() == 0 || carDatas == null) {
					Toast.makeText(RemindAddActivity.this, "您还没有添加爱车",
							Toast.LENGTH_SHORT).show();
				}
			} else {
				ll_car.setVisibility(View.VISIBLE);
				ll_mileage.setVisibility(View.GONE);
				ll_content.setVisibility(View.GONE);
				if (carDatas.size() == 0 || carDatas == null) {
					AlertDialog.Builder dialog = new AlertDialog.Builder(
							RemindAddActivity.this);
					dialog.setTitle("提示");
					dialog.setMessage("您的账户下没有车辆，是否添加。");
					dialog.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Intent intent = new Intent(
											RemindAddActivity.this,
											CarAddActivity.class);
									startActivityForResult(intent, 2);
								}
							});
					dialog.setNegativeButton("取消", null);
					dialog.show();
				}
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	private void save() {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		if (s_type.getSelectedItemPosition() != 0
				&& s_type.getSelectedItemPosition() != 5) {
			if (carDatas.size() != 0 && carDatas != null) {
				params.add(new BasicNameValuePair("obj_id", String
						.valueOf(carDatas.get(
								s_car.getSelectedItemPosition()).getObj_id())));
			} else {
				AlertDialog.Builder dialog = new AlertDialog.Builder(
						RemindAddActivity.this);
				dialog.setTitle("提示");
				dialog.setMessage("您的账户下没有车辆，是否添加。");
				dialog.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent intent = new Intent(
										RemindAddActivity.this,
										CarAddActivity.class);
								startActivityForResult(intent, 2);
							}
						});
				dialog.setNegativeButton("取消", null);
				dialog.show();
				return;
			}
		} else {
			params.add(new BasicNameValuePair("obj_id", "0"));
		}
		String mileage = et_mileage.getText().toString().trim();
		String url = Constant.BaseUrl + "reminder?auth_code=" + app.auth_code;
		params.add(new BasicNameValuePair("cust_id", cust_id));
		params.add(new BasicNameValuePair("remind_type", String.valueOf(s_type
				.getSelectedItemPosition())));
		params.add(new BasicNameValuePair("mileage", mileage.equals("") ? "0"
				: mileage));
		params.add(new BasicNameValuePair("remind_way", remind_way));
		params.add(new BasicNameValuePair("repeat_type", String.valueOf(s_mode
				.getSelectedItemPosition())));
		params.add(new BasicNameValuePair("content", et_content.getText()
				.toString().trim()));
		params.add(new BasicNameValuePair("remind_time", remind_time));
		new NetThread.postDataThread(handler, url, params, save).start();
	}

	private void setWhite() {
		tv_before0.setBackgroundResource(R.drawable.bg_white);
		tv_before1.setBackgroundResource(R.drawable.bg_white);
		tv_before3.setBackgroundResource(R.drawable.bg_white);
		tv_before7.setBackgroundResource(R.drawable.bg_white);
		tv_before30.setBackgroundResource(R.drawable.bg_white);
	}

	private List<String> getCars() {
		List<String> strs = new ArrayList<String>();
		for (CarData carData : carDatas) {
			strs.add(carData.getNick_name());
		}
		return strs;
	}

	OnWheelScrollListener onWheelScrollListener = new OnWheelScrollListener() {
		@Override
		public void onScrollingStarted(WheelView wheel) {
		}

		@Override
		public void onScrollingFinished(WheelView wheel) {
			remind_time = (curYear + year.getCurrentItem()) + "-"
					+ GetSystem.ChangeTime(month.getCurrentItem() + 1) + "-"
					+ GetSystem.ChangeTime(day.getCurrentItem() + 1);
			tv_remind_time.setText("日期 " + remind_time);
			updateDays(year, month, day);
			updateWeek();
		}
	};
	OnWheelScrollListener onDateWheelScrollListener = new OnWheelScrollListener() {
		@Override
		public void onScrollingStarted(WheelView wheel) {
		}

		@Override
		public void onScrollingFinished(WheelView wheel) {
			remind_time = (curYear + year.getCurrentItem()) + "-"
					+ GetSystem.ChangeTime(month.getCurrentItem() + 1) + "-"
					+ GetSystem.ChangeTime(day.getCurrentItem() + 1);
			tv_remind_time.setText("日期 " + remind_time);
			updateWeek();
		}
	};
	int curYear;
	WheelView year;
	WheelView month;
	WheelView day;
	WheelView week;
	String weeks[] = new String[] { "星期一", "星期二", "星期三", "星期四", "星期五", "星期六",
			"星期日" };

	private void setDate() {
		year = (WheelView) findViewById(R.id.data_year);
		month = (WheelView) findViewById(R.id.data_month);
		day = (WheelView) findViewById(R.id.data_day);
		week = (WheelView) findViewById(R.id.data_week);
		Calendar calendar = Calendar.getInstance();
		// year
		curYear = calendar.get(Calendar.YEAR);
		year.setViewAdapter(new DateNumericAdapter(RemindAddActivity.this,
				curYear, curYear + 10, 0));
		year.setCurrentItem(0);
		year.addScrollingListener(onWheelScrollListener);
		// month
		int curMonth = calendar.get(Calendar.MONTH);
		String months[] = new String[] { "1月", "2月", "3月", "4月", "5月", "6月",
				"7月", "8月", "9月", "10月", "11月", "12月" };
		month.setViewAdapter(new DateArrayAdapter(RemindAddActivity.this,
				months, curMonth));
		month.setCurrentItem(curMonth);
		month.addScrollingListener(onWheelScrollListener);
		// day
		day.addScrollingListener(onDateWheelScrollListener);
		updateFristDays(year, month, day,
				(calendar.get(Calendar.DAY_OF_MONTH) - 1));

		remind_time = (curYear + year.getCurrentItem()) + "-"
				+ GetSystem.ChangeTime(month.getCurrentItem() + 1) + "-"
				+ GetSystem.ChangeTime(day.getCurrentItem() + 1);
		tv_remind_time.setText("日期 " + remind_time);
		// week
		updateWeek();
	}

	private void updateFristDays(WheelView year, WheelView month,
			WheelView day, int date) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(calendar.get(Calendar.YEAR) + year.getCurrentItem(),
				month.getCurrentItem(), 1);
		int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		day.setViewAdapter(new DateNumericAdapter(RemindAddActivity.this, 1,
				maxDays, calendar.get(Calendar.DAY_OF_MONTH) - 1));
		int curDay = Math.min(maxDays, date);
		day.setCurrentItem(curDay);
	}

	private void updateWeek() {
		week.setViewAdapter(new DateArrayAdapter(RemindAddActivity.this, weeks,
				GetSystem.getWeekOfDate(remind_time)));
		week.setCurrentItem(GetSystem.getWeekOfDate(remind_time));
	}

	/**
	 * Updates day wheel. Sets max days according to selected month and year
	 */
	private void updateDays(WheelView year, WheelView month, WheelView day) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(calendar.get(Calendar.YEAR) + year.getCurrentItem(),
				month.getCurrentItem(), 1);
		int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		day.setViewAdapter(new DateNumericAdapter(RemindAddActivity.this, 1,
				maxDays, calendar.get(Calendar.DAY_OF_MONTH) - 1));
		int curDay = Math.min(maxDays, day.getCurrentItem());
		day.setCurrentItem(curDay);
	}

	/**
	 * Adapter for numeric wheels. Highlights the current value.
	 */
	private static class DateNumericAdapter extends NumericWheelAdapter {
		// Index of current item
		int currentItem;
		// Index of item to be highlighted
		int currentValue;

		/**
		 * Constructor
		 */
		public DateNumericAdapter(Context context, int minValue, int maxValue,
				int current) {
			super(context, minValue, maxValue);
			this.currentValue = current;
			setTextSize(20);
		}

		@Override
		protected void configureTextView(TextView view) {
			super.configureTextView(view);
			if (currentItem == currentValue) {
				// view.setTextColor(0xFF0000F0);
			}
			view.setTypeface(Typeface.SANS_SERIF);
		}

		@Override
		public View getItem(int index, View cachedView, ViewGroup parent) {
			currentItem = index;
			return super.getItem(index, cachedView, parent);
		}
	}

	/**
	 * Adapter for string based wheel. Highlights the current value.
	 */
	private static class DateArrayAdapter extends ArrayWheelAdapter<String> {
		// Index of current item
		int currentItem;
		// Index of item to be highlighted
		int currentValue;

		/**
		 * Constructor
		 */
		public DateArrayAdapter(Context context, String[] items, int current) {
			super(context, items);
			this.currentValue = current;
			setTextSize(20);
		}

		@Override
		protected void configureTextView(TextView view) {
			super.configureTextView(view);
			if (currentItem == currentValue) {
				// view.setTextColor(0xFF0000F0);
			}
			view.setTypeface(Typeface.SANS_SERIF);
		}

		@Override
		public View getItem(int index, View cachedView, ViewGroup parent) {
			currentItem = index;
			return super.getItem(index, cachedView, parent);
		}
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 2 && resultCode == 3) {
			// 添加车辆返回
			ArrayAdapter<String> car = new ArrayAdapter<String>(
					RemindAddActivity.this,
					android.R.layout.simple_spinner_item, getCars());
			car.setDropDownViewResource(R.layout.drop_down_item);
			s_car.setAdapter(car);
		}
	}
}