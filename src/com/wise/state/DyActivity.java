package com.wise.state;

import java.util.ArrayList;

import org.json.JSONArray;
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
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.entity.EnergyItem;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.widget.EnergyCurveView;


/**
 * 电源电压
 */
public class DyActivity extends Activity {

	private static final int getData = 1;
	private static final int refresh = 2;
	private static final int getActiveData = 3;
	private static final int getChartData = 4;

	WebView shareView;
	TextView tv_dpdy_range, tv_lt_dpdy, tv_if_lt_dpdy_err, tv_dpdy,
			tv_if_dpdy_err, tv_real_dpdy, tv_if_real_dpdy;
	LinearLayout ll_real_dpdy;
	EnergyCurveView ecv_real_dpdy;
	// 曲线图数据集合
	ArrayList<EnergyItem> Efuel = new ArrayList<EnergyItem>();
	AppApplication app;
	String device_id;
	/** 实时刷新数据 **/
	boolean isRefresh = true;
	/**
	 * 1=电源，2=进气，节气门，3=怠速，4=冷却，水温，5=排放，三元 节气门开度和三元催化器值如果为0，则显示‘0.00’改为‘未检测数据’,
	 * ‘状态异常或者良好’改为‘未检测数据’
	 */
	int type;
	boolean state = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_dy);
		app = (AppApplication) getApplication();
		Intent intent = getIntent();
		device_id = intent.getStringExtra("device_id");
		type = intent.getIntExtra("type", 1);
		String title = intent.getStringExtra("title");
		String name = intent.getStringExtra("name");
		ll_real_dpdy = (LinearLayout) findViewById(R.id.ll_real_dpdy);
		ecv_real_dpdy = (EnergyCurveView) findViewById(R.id.ecv_real_dpdy);
		state = intent.getBooleanExtra("state", false);
		LinearLayout dpdy_all = (LinearLayout) findViewById(R.id.dpdy_all);
		if (state) {
			dpdy_all.setVisibility(View.VISIBLE);
		} else {
			dpdy_all.setVisibility(View.GONE);
		}
		TextView tv_dpdy_title = (TextView) findViewById(R.id.tv_dpdy_title);
		TextView tv_dpdy_unit = (TextView) findViewById(R.id.tv_dpdy_unit);
		switch (type) {
		case 1:
			tv_dpdy_unit.setText("V");
			tv_dpdy_title.setText("最近30天每日均值曲线");
			break;
		case 2:
			tv_dpdy_unit.setText("%");
			tv_dpdy_title.setText("最近30天每日均值曲线");
			break;
		case 3:
			tv_dpdy_unit.setText("rpm");
			tv_dpdy_title.setText("最后行程怠速曲线");
			break;
		case 4:
			tv_dpdy_unit.setText("°C");
			tv_dpdy_title.setText("最近30天每日均值曲线");
			break;
		case 5:
			tv_dpdy_unit.setText("°C");
			tv_dpdy_title.setText("最近30天每日均值曲线");
			break;
		}
		ecv_real_dpdy = (EnergyCurveView) findViewById(R.id.ecv_real_dpdy);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		ecv_real_dpdy.setViewWidth(dm.widthPixels, false);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		TextView tv_title = (TextView) findViewById(R.id.tv_title);
		tv_title.setText(title);
		TextView tv_name = (TextView) findViewById(R.id.tv_name);
		tv_name.setText(name);
		tv_dpdy_range = (TextView) findViewById(R.id.tv_dpdy_range);
		tv_lt_dpdy = (TextView) findViewById(R.id.tv_lt_dpdy);
		tv_if_dpdy_err = (TextView) findViewById(R.id.tv_if_dpdy_err);
		tv_dpdy = (TextView) findViewById(R.id.tv_dpdy);
		tv_if_lt_dpdy_err = (TextView) findViewById(R.id.tv_if_lt_dpdy_err);
		tv_real_dpdy = (TextView) findViewById(R.id.tv_real_dpdy);
		tv_if_real_dpdy = (TextView) findViewById(R.id.tv_if_real_dpdy);
		shareView = (WebView) findViewById(R.id.share_web);
		shareView.requestFocus(); // 设置可获取焦点
		shareView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY); // 取消滚动条
		getData();
		getChartData();
		/** 定时刷新实时数据 **/
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (isRefresh) {
					try {
						Thread.sleep(60000);
						Message message = new Message();
						message.what = refresh;
						handler.sendMessage(message);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
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
			case refresh:
				refreshData();
				break;
			case getActiveData:
				jsonRefreshData(msg.obj.toString());
				break;
			case getChartData:
				jsonCharData(msg.obj.toString());
				break;
			}
		}
	};

	/** 获取要显示的数据 **/
	private void getData() {
		String url = Constant.BaseUrl + "device/" + device_id
				+ "/obd_data?auth_code=" + app.auth_code + "&type=" + type;
		new NetThread.GetDataThread(handler, url, getData).start();
	}

	/**
	 * 解析数据 type = 2，5节气门开度和三元催化器值如果为0，则显示‘0.00’改为‘未检测数据’, ‘状态异常或者良好’改为‘未检测数据’
	 **/
	private void jsonData(String result) {
		try {
			JSONObject jsonObject = new JSONObject(result);
			// 正常范围
			String range = jsonObject.getString("range");
			tv_dpdy_range.setText(range);
			// 长期检测值
			double long_term_value = jsonObject.getDouble("long_term_value");
			if ((type == 2 || type == 5) & long_term_value == 0) {
				tv_lt_dpdy.setText("未检测数据");
				tv_if_lt_dpdy_err.setText("未检测数据");
				tv_if_lt_dpdy_err.setTextColor(getResources().getColor(
						R.color.yellow));
			} else {
				tv_lt_dpdy.setText(String.format("%.2f",
						Double.valueOf(long_term_value)));
				// 长期检测结果
				boolean if_lt_err = jsonObject.getBoolean("if_lt_err");
				if (if_lt_err) {
					tv_if_lt_dpdy_err.setText("状态异常");
					tv_if_lt_dpdy_err.setTextColor(getResources().getColor(
							R.color.yellow));
				} else {
					tv_if_lt_dpdy_err.setText("状态良好");
					tv_if_lt_dpdy_err.setTextColor(getResources().getColor(
							R.color.blue));
				}
			}
			// 本次检测
			double last_trip_value = jsonObject.getDouble("last_trip_value");
			if ((type == 2 || type == 5) & last_trip_value == 0) {
				tv_dpdy.setText("未检测数据");
				tv_if_dpdy_err.setText("未检测数据");
				tv_if_dpdy_err.setTextColor(getResources().getColor(
						R.color.yellow));
			} else {
				tv_dpdy.setText(String.format("%.2f", last_trip_value));
				// 本次检测结果
				boolean if_err = jsonObject.getBoolean("if_err");
				if (if_err) {
					tv_if_dpdy_err.setText("状态异常");
					tv_if_dpdy_err.setTextColor(getResources().getColor(
							R.color.yellow));
				} else {
					tv_if_dpdy_err.setText("状态良好");
					tv_if_dpdy_err.setTextColor(getResources().getColor(
							R.color.blue));
				}
			}
			// 实时检测
			double real_value = jsonObject.getDouble("real_value");
			if ((type == 2 || type == 5) & last_trip_value == 0) {
				tv_real_dpdy.setText("未检测数据");
				tv_if_real_dpdy.setText("未检测数据");
				tv_if_real_dpdy.setTextColor(getResources().getColor(
						R.color.yellow));
			} else {
				tv_real_dpdy.setText(String.format("%.2f", real_value));
				// 实时检测结果
				boolean if_rl_err = jsonObject.getBoolean("if_rl_err");
				if (if_rl_err) {
					tv_if_real_dpdy.setText("状态异常");
					tv_if_real_dpdy.setTextColor(getResources().getColor(
							R.color.yellow));
				} else {
					tv_if_real_dpdy.setText("状态良好");
					tv_if_real_dpdy.setTextColor(getResources().getColor(
							R.color.blue));
				}
			}
			String url = jsonObject.getString("url");
			shareView.loadUrl(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 刷新实时数据 **/
	private void refreshData() {
		String url = Constant.BaseUrl + "device/" + device_id
				+ "/active_obd_data?auth_code=" + app.auth_code + "&type="
				+ type;
		new NetThread.GetDataThread(handler, url, getActiveData).start();
	}

	/** 解析实时数据 **/
	private void jsonRefreshData(String result) {
		try {
			JSONObject jsonObject = new JSONObject(result);
			double real_value = jsonObject.getDouble("real_value");
			boolean if_rl_err = jsonObject.getBoolean("if_rl_err");
			if ((type == 2 || type == 5) & real_value == 0) {
				tv_real_dpdy.setText("未检测数据");
				tv_if_real_dpdy.setText("未检测数据");
				tv_if_real_dpdy.setTextColor(getResources().getColor(
						R.color.yellow));
			} else {
				// 实时检测
				tv_real_dpdy.setText(String.format("%.2f", real_value));
				// 实时检测结果
				if (if_rl_err) {
					tv_if_real_dpdy.setText("状态异常");
					tv_if_real_dpdy.setTextColor(getResources().getColor(
							R.color.yellow));
				} else {
					tv_if_real_dpdy.setText("状态良好");
					tv_if_real_dpdy.setTextColor(getResources().getColor(
							R.color.blue));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 获取近一个月的曲线 **/
	private void getChartData() {
		String url = Constant.BaseUrl + "device/" + device_id
				+ "/obd_data_chart?auth_code=" + app.auth_code + "&type="
				+ type;
		new NetThread.GetDataThread(handler, url, getChartData).start();
	}

	/**
	 * 解析近一个月的数据 如果曲线返回数据为空，则不显示曲线部分
	 **/
	private void jsonCharData(String result) {
		try {
			JSONArray jsonArray = new JSONArray(result);
			if (jsonArray.length() == 0) {
				ll_real_dpdy.setVisibility(View.GONE);
			} else {
				ll_real_dpdy.setVisibility(View.VISIBLE);
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					int data = jsonObject.getInt("_id");
					String avg_value = jsonObject.getString("avg_value");
					Efuel.add(new EnergyItem(data, Float.valueOf(avg_value), ""));
				}
				ecv_real_dpdy.initPoints(Efuel, 2, 0);
				ecv_real_dpdy.RefreshView();
			}
		} catch (Exception e) {
			e.printStackTrace();
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
	protected void onDestroy() {
		super.onDestroy();
		isRefresh = false;
	}
}
