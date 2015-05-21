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
import com.wise.baba.entity.RemindData;
import com.wise.baba.net.NetThread;
import com.wise.car.CarActivity;

import android.app.Activity;
import android.content.Context;
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

public class CarRemindUpdateActivity extends Activity {
	private static final String TAG = "CarRemindUpdateActivity";
	private static final int save = 1;
	String remind_way = "2";
	String remind_time;
	Spinner s_type, s_car, s_mode;
	LinearLayout ll_car, ll_mileage, ll_content;
	TextView tv_before0, tv_before1, tv_before3, tv_before7, tv_before30,
			tv_remind_time, tv_before_note;
	EditText et_mileage, et_content;
	RemindData remindData;
	AppApplication app;
	List<CarData> carDatas = new ArrayList<CarData>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_car_remind_add);
		app = (AppApplication) getApplication();
		
		carDatas = (List<CarData>) getIntent().getSerializableExtra("carDatas");
		remindData = (RemindData) getIntent()
				.getSerializableExtra("remindData");
		GetSystem.myLog(TAG, remindData.toString());
		
		
		ll_car = (LinearLayout) findViewById(R.id.ll_car);
		ll_mileage = (LinearLayout) findViewById(R.id.ll_mileage);
		ll_content = (LinearLayout) findViewById(R.id.ll_content);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		ImageView iv_ok = (ImageView) findViewById(R.id.iv_ok);
		iv_ok.setOnClickListener(onClickListener);
		TextView tv_title = (TextView) findViewById(R.id.tv_title);
		tv_title.setText("修改提醒");

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
		tv_before_note = (TextView) findViewById(R.id.tv_before_note);

		s_type = (Spinner) findViewById(R.id.s_type);
		ArrayAdapter<String> type = new ArrayAdapter<String>(
				CarRemindUpdateActivity.this,
				android.R.layout.simple_spinner_item, Constant.items_note_type);
		type.setDropDownViewResource(R.layout.drop_down_item);
		s_type.setAdapter(type);
		s_type.setOnItemSelectedListener(onTypeItemSelectedListener);
		s_car = (Spinner) findViewById(R.id.s_car);
		ArrayAdapter<String> car = new ArrayAdapter<String>(
				CarRemindUpdateActivity.this,
				android.R.layout.simple_spinner_item, getCars());
		car.setDropDownViewResource(R.layout.drop_down_item);
		s_car.setAdapter(car);
		s_mode = (Spinner) findViewById(R.id.s_mode);
		ArrayAdapter<String> mode = new ArrayAdapter<String>(
				CarRemindUpdateActivity.this,
				android.R.layout.simple_spinner_item, Constant.items_note_mode);
		mode.setDropDownViewResource(R.layout.drop_down_item);
		s_mode.setAdapter(mode);
		
		s_type.setSelection(remindData.getRemind_type());
		s_car.setSelection(getSelectedItemPosition(remindData.getObj_id()));
		s_mode.setSelection(remindData.getRepeat_type());
		setRemind_way(remindData.getRemind_way());
		remind_time = remindData.getRemind_time();
		int year = Integer.valueOf(remind_time.substring(0, 4));
		int Month = Integer.valueOf(remind_time.substring(5, 7)) - 1;
		int Date = Integer.valueOf(remind_time.substring(8, 10)) - 1;
		setDate(year, Month, Date);
		setRemind(remindData.getRemind_type());
		et_mileage.setText(String.valueOf(remindData.getMileages()));
		et_content.setText(remindData.getContent());
	}

	/** 得到车辆对应的位置 **/
	private int getSelectedItemPosition(int Obj_id) {
		for (int i = 0; i < carDatas.size(); i++) {
			if (carDatas.get(i).getObj_id() == Obj_id) {
				return i;
			}
		}
		return 0;
	}

	private void setRemind(int arg2) {
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
		} else {
			ll_car.setVisibility(View.VISIBLE);
			ll_mileage.setVisibility(View.GONE);
			ll_content.setVisibility(View.GONE);
		}
	}

	private void setRemind_way(int type) {
		switch (type) {
		case 0:
			tv_before_note.setText("提醒 不提前");
			remind_way = "0";
			setWhite();
			tv_before0.setBackgroundResource(R.drawable.bg_qianlan);
			break;
		case 1:
			tv_before_note.setText("提醒 提前一天");
			remind_way = "1";
			setWhite();
			tv_before1.setBackgroundResource(R.drawable.bg_qianlan);
			break;
		case 2:
			tv_before_note.setText("提醒 提前三天");
			remind_way = "2";
			setWhite();
			tv_before3.setBackgroundResource(R.drawable.bg_qianlan);
			break;
		case 3:
			tv_before_note.setText("提醒 提前七天");
			remind_way = "3";
			setWhite();
			tv_before7.setBackgroundResource(R.drawable.bg_qianlan);
			break;
		case 4:
			tv_before_note.setText("提醒 提前一个月");
			remind_way = "4";
			setWhite();
			tv_before30.setBackgroundResource(R.drawable.bg_qianlan);
			break;
		}
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
					Toast.makeText(CarRemindUpdateActivity.this, "演示账号不支持该功能",
							Toast.LENGTH_SHORT).show();
					return;
				}
				save();
				break;
			case R.id.tv_before0:
				tv_before_note.setText("提醒 不提前");
				remind_way = "0";
				setWhite();
				tv_before0.setBackgroundResource(R.drawable.bg_qianlan);
				break;
			case R.id.tv_before1:
				tv_before_note.setText("提醒 提前一天");
				remind_way = "1";
				setWhite();
				tv_before1.setBackgroundResource(R.drawable.bg_qianlan);
				break;
			case R.id.tv_before3:
				tv_before_note.setText("提醒 提前三天");
				remind_way = "2";
				setWhite();
				tv_before3.setBackgroundResource(R.drawable.bg_qianlan);
				break;
			case R.id.tv_before7:
				tv_before_note.setText("提醒 提前七天");
				remind_way = "3";
				setWhite();
				tv_before7.setBackgroundResource(R.drawable.bg_qianlan);
				break;
			case R.id.tv_before30:
				tv_before_note.setText("提醒 提前一个月");
				remind_way = "4";
				setWhite();
				tv_before30.setBackgroundResource(R.drawable.bg_qianlan);
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
						Toast.makeText(CarRemindUpdateActivity.this, "修改提醒成功",
								Toast.LENGTH_SHORT).show();
						setResult(2);
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

		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	private void save() {
		String mileage = et_mileage.getText().toString().trim();
		String url = Constant.BaseUrl + "reminder/"
				+ remindData.getReminder_id() + "?auth_code=" + app.auth_code;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("remind_type", String.valueOf(s_type
				.getSelectedItemPosition())));
		if (s_type.getSelectedItemPosition() == 5) {
			// 通用提醒
			params.add(new BasicNameValuePair("obj_id", "0"));
		} else {
			params.add(new BasicNameValuePair("obj_id", String
					.valueOf(carDatas.get(s_car.getSelectedItemPosition())
							.getObj_id())));
		}
		params.add(new BasicNameValuePair("mileage", mileage.equals("") ? "0"
				: mileage));
		params.add(new BasicNameValuePair("remind_way", remind_way));
		params.add(new BasicNameValuePair("repeat_type", String.valueOf(s_mode
				.getSelectedItemPosition())));
		params.add(new BasicNameValuePair("content", et_content.getText()
				.toString().trim()));
		params.add(new BasicNameValuePair("remind_time", remind_time));
		new Thread(new NetThread.putDataThread(handler, url, params, save))
				.start();
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

	private void setDate(int y, int m, int d) {
		month = (WheelView) findViewById(R.id.data_month);
		year = (WheelView) findViewById(R.id.data_year);
		day = (WheelView) findViewById(R.id.data_day);
		week = (WheelView) findViewById(R.id.data_week);
		Calendar calendar = Calendar.getInstance();
		// year
		curYear = calendar.get(Calendar.YEAR);
		year.setViewAdapter(new DateNumericAdapter(
				CarRemindUpdateActivity.this, curYear, curYear + 10, 0));
		year.setCurrentItem(y - curYear);
		year.addScrollingListener(onWheelScrollListener);
		// month
		int curMonth = calendar.get(Calendar.MONTH);
		String months[] = new String[] { "1月", "2月", "3月", "4月", "5月", "6月",
				"7月", "8月", "9月", "10月", "11月", "12月" };
		month.setViewAdapter(new DateArrayAdapter(CarRemindUpdateActivity.this,
				months, curMonth));
		month.setCurrentItem(m);
		month.addScrollingListener(onWheelScrollListener);
		// day
		day.addScrollingListener(onDateWheelScrollListener);
		updateFristDays(year, month, day, d);
		updateWeek();
	}

	private void updateFristDays(WheelView year, WheelView month,
			WheelView day, int date) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(calendar.get(Calendar.YEAR) + year.getCurrentItem(),
				month.getCurrentItem(), 1);
		int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		day.setViewAdapter(new DateNumericAdapter(CarRemindUpdateActivity.this,
				1, maxDays, calendar.get(Calendar.DAY_OF_MONTH) - 1));
		int curDay = Math.min(maxDays, date);
		day.setCurrentItem(curDay);
	}

	/**
	 * Updates day wheel. Sets max days according to selected month and year
	 */
	private void updateDays(WheelView year, WheelView month, WheelView day) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(calendar.get(Calendar.YEAR) + year.getCurrentItem(),
				month.getCurrentItem(), 1);
		int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		day.setViewAdapter(new DateNumericAdapter(CarRemindUpdateActivity.this,
				1, maxDays, calendar.get(Calendar.DAY_OF_MONTH) - 1));
		int curDay = Math.min(maxDays, day.getCurrentItem() + 1);
		day.setCurrentItem(curDay - 1);
	}

	private void updateWeek() {
		remind_time = (curYear + year.getCurrentItem()) + "-"
				+ GetSystem.ChangeTime(month.getCurrentItem() + 1) + "-"
				+ GetSystem.ChangeTime(day.getCurrentItem() + 1);
		tv_remind_time.setText("日期 " + remind_time);
		week.setViewAdapter(new DateArrayAdapter(CarRemindUpdateActivity.this,
				weeks, GetSystem.getWeekOfDate(remind_time)));
		week.setCurrentItem(GetSystem.getWeekOfDate(remind_time));

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
}