package com.wise.state;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.Judge;
import pubclas.NetThread;
import pubclas.Variable;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.wise.baba.MoreActivity;
import com.wise.baba.R;
import com.wise.baba.SelectCityActivity;
import com.wise.car.CarActivity;
import com.wise.car.CarAddActivity;
import com.wise.car.CarLocationActivity;
import com.wise.car.CarUpdateActivity;
import com.wise.car.DevicesAddActivity;
import com.wise.notice.NoticeFragment;
import com.wise.notice.NoticeFragment.BtnListener;
import com.wise.setting.LoginActivity;
import com.wise.setting.RegisterActivity;
import com.wise.show.ShowActivity;
import customView.AlwaysMarqueeTextView;
import customView.HScrollLayout;
import customView.NoticeScrollTextView;
import customView.OnViewChangeListener;
import customView.ParentSlide;
import data.CarData;
import android.R.menu;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 车况信息
 * 
 * @author honesty
 * 
 */
public class FaultActivity extends FragmentActivity {
	private static final String TAG = "FaultActivity";
	/** 获取油耗信息 **/
	private static final int getData = 1;
	/** 获取天气信息 **/
	private static final int getWeather = 2;
	/** 循环读取乐一乐线程 **/
	private static final int cycle = 3;
	/** 乐一乐 **/
	private static final int getJoy = 4;
	/** 获取滚动消息 **/
	private static final int getMessage = 5;
	/** 定时滚动消息 **/
	private static final int Nstv = 6;
	/** 获取限行 **/
	private static final int Get_carLimit = 7;
	/** 获取消息数据 **/
	private static final int get_counter = 8;
	/** 获取gps信息 **/
	private static final int get_gps = 10;

	ImageView iv_weather, iv_noti;
	TextView tv_city, tv_weather_time, tv_weather, tv_advice, tv_joy,
			tv_happy_time;
	int index = 0;
	private FragmentManager fragmentManager;
	ParentSlide smv_content;
	NoticeScrollTextView nstv_message;
	HScrollLayout hs_car;
	MyBroadCastReceiver myBroadCastReceiver;
	IntentFilter intentFilter;

	/** 滚动消息数据 **/
	List<NsData> nsDatas = new ArrayList<NsData>();
	/** 获取油耗数据开始时间 **/
	String startMonth;
	/** 获取油耗数据结束时间 **/
	String endMonth;
	NoticeFragment noticeFragment;
	private GeoCoder mGeoCoder = null;
	int completed;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_fault);
		GetSystem.myLog(TAG, "onCreate");
		mGeoCoder = GeoCoder.newInstance();
		mGeoCoder.setOnGetGeoCodeResultListener(listener);
		boolean isSpecify = getIntent().getBooleanExtra("isSpecify", false);
		if (isSpecify) {
			Intent intent = new Intent(FaultActivity.this, MoreActivity.class);
			intent.putExtra("isSpecify", isSpecify);
			intent.putExtras(getIntent().getExtras());
			startActivity(intent);
		}
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int twoCompleted = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 280, getResources()
						.getDisplayMetrics());
		completed = (width - twoCompleted) / 3;
		Button bt_show = (Button) findViewById(R.id.bt_show);
		bt_show.setOnClickListener(onClickListener);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		tv_city = (TextView) findViewById(R.id.tv_city);
		tv_city.setOnClickListener(onClickListener);
		tv_weather_time = (TextView) findViewById(R.id.tv_weather_time);
		tv_weather = (TextView) findViewById(R.id.tv_weather);
		tv_advice = (TextView) findViewById(R.id.tv_advice);
		tv_joy = (TextView) findViewById(R.id.tv_joy);
		tv_happy_time = (TextView) findViewById(R.id.tv_happy_time);
		iv_weather = (ImageView) findViewById(R.id.iv_weather);
		iv_noti = (ImageView) findViewById(R.id.iv_noti);
		ImageView iv_menu = (ImageView) findViewById(R.id.iv_menu);
		iv_menu.setOnClickListener(onClickListener);

		smv_content = (ParentSlide) findViewById(R.id.smv_content);
		nstv_message = (NoticeScrollTextView) findViewById(R.id.nstv_message);
		hs_car = (HScrollLayout) findViewById(R.id.hs_car);
		fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		noticeFragment = new NoticeFragment();
		transaction.add(R.id.ll_notice, noticeFragment);
		transaction.commit();
		noticeFragment.SetBtnListener(new BtnListener() {
			@Override
			public void Back() {
				smv_content.snapToScreen(0);
			}
		});
		new CycleThread().start();
		hs_car.setOnViewChangeListener(new OnViewChangeListener() {
			@Override
			public void OnViewChange(int view) {
				index = view;
				getTotalData();
			}

			@Override
			public void OnLastView() {
			}

			@Override
			public void OnFinish(int index) {
			}
		});
		String Month = GetSystem.GetNowMonth().getMonth();
		startMonth = Month + "-01";
		endMonth = GetSystem.getMonthLastDay(Month);
		
		if (Judge.isLogin()) {//已登录
			GetSystem.myLog(TAG, "Variable.carDatas.size() = "
					+ Variable.carDatas.size());
			initDataView();
			String url = Constant.BaseUrl + "customer/" + Variable.cust_id
					+ "/tips?auth_code=" + Variable.auth_code;
			getMessage(url);
			getCounter();
		} else {// 未登录
			// 给个临时id
			Variable.cust_id = "0";
			Variable.auth_code = "127a154df2d7850c4232542b4faa2c3d";
			setLoginView();
			String url = Constant.BaseUrl + "customer/0/tips";
			getMessage(url);
			Intent intent = new Intent(FaultActivity.this, LoginActivity.class);
			intent.putExtra("fastTrack", true);
			startActivity(intent);
			// 给出快速注册
//			new AlertDialog.Builder(FaultActivity.this)
//					.setTitle("确认")
//					.setMessage("如果您有终端且没有注册，可使用快速注册！")
//					.setPositiveButton("好",
//							new DialogInterface.OnClickListener() {
//								@Override
//								public void onClick(DialogInterface dialog,
//										int which) {
//									Intent intent = new Intent(
//											FaultActivity.this,
//											RegisterActivity.class);
//									intent.putExtra("fastTrack", true);
//									startActivity(intent);
//								}
//							}).setNegativeButton("取消", null).show();
		}
		myBroadCastReceiver = new MyBroadCastReceiver();
		intentFilter = new IntentFilter();
		intentFilter.addAction(Constant.A_RefreshHomeCar);
		intentFilter.addAction(Constant.A_LoginOut);
		registerReceiver(myBroadCastReceiver, intentFilter);

		new CycleNstvThread().start();

		UmengUpdateAgent.update(this);
	}

	/**
	 * 当菜单即将要显示时触发（每次都触发） 菜单键显示
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		// 想要动态的改变现有的菜单可以在此添加代码
		startActivity(new Intent(FaultActivity.this, MoreActivity.class));
		return true;
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.iv_menu:
				startActivity(new Intent(FaultActivity.this, MoreActivity.class));
				break;
			case R.id.bt_show:
				startActivity(new Intent(FaultActivity.this, ShowActivity.class));
				break;
			case R.id.tasks_view:
				GetSystem.myLog(TAG, "tasks_view : Variable.carDatas.size() = "
						+ Variable.carDatas.size());
				if (Variable.carDatas != null && Variable.carDatas.size() != 0) {
					String Device_id = Variable.carDatas.get(index)
							.getDevice_id();
					if (Device_id == null || Device_id.equals("")) {
						Intent intent = new Intent(FaultActivity.this,
								DevicesAddActivity.class);
						intent.putExtra("car_id", Variable.carDatas.get(index)
								.getObj_id());
						startActivityForResult(intent, 2);
					} else {
						Intent intent = new Intent(FaultActivity.this,
								FaultDetectionActivity.class);
						intent.putExtra("index", index);
						startActivityForResult(intent, 1);
					}
				}
				break;
			case R.id.tcv_drive:
				if (Variable.carDatas != null && Variable.carDatas.size() != 0) {
					String Device_id = Variable.carDatas.get(index)
							.getDevice_id();
					if (Device_id == null || Device_id.equals("")) {
						Intent intent = new Intent(FaultActivity.this,
								DevicesAddActivity.class);
						intent.putExtra("car_id", Variable.carDatas.get(index)
								.getObj_id());
						startActivityForResult(intent, 2);
					} else {
						Intent intent = new Intent(FaultActivity.this,
								DriveActivity.class);
						intent.putExtra("index_car", index);
						// TODO 刷新
						startActivityForResult(intent, 2);
					}
				}
				break;
			// TODO 油耗，花费，里程分别显示
			case R.id.Liner_distance:
				getDataOne(FaultActivity.DISTANCE);
				break;
			case R.id.Liner_fuel:
				getDataOne(FaultActivity.FUEL);
				break;
			case R.id.Liner_fee:
				getDataOne(FaultActivity.FEE);
				break;

			case R.id.tv_message:
				nstvClick();
				break;
			case R.id.tv_city:
				startActivityForResult(new Intent(FaultActivity.this,
						SelectCityActivity.class), 0);
				break;
			case R.id.tv_adress:
				goCarMap();
				break;
			}
		}
	};

	// 跳转类型
	public static final int DISTANCE = 1;// 里程
	public static final int FEE = 2;// 费用
	public static final int FUEL = 3;// 油耗

	// TODO 根据跳转类型进行（里程，花费，油耗）页面显示
	private void getDataOne(int type) {
		if (Variable.carDatas != null && Variable.carDatas.size() != 0) {
			String Device_id = Variable.carDatas.get(index).getDevice_id();
			if (Device_id == null || Device_id.equals("")) {
				Intent intent = new Intent(FaultActivity.this,
						DevicesAddActivity.class);
				intent.putExtra("car_id", Variable.carDatas.get(index)
						.getObj_id());
				startActivityForResult(intent, 2);
			} else {
				Intent intent = new Intent(FaultActivity.this,
						FuelActivity.class);
				intent.putExtra("index_car", index);
				// 传递跳转类型常量进行跳转
				intent.putExtra("type", type);
				startActivity(intent);
			}
		}
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case getData:
				jsonData(msg.obj.toString(), msg.arg1);
				break;
			case getWeather:
				jsonWeather(msg.obj.toString());
				break;
			case getJoy:
				jsonJoy(msg.obj.toString());
				break;
			case cycle:
				getJoy();
				break;
			case getMessage:
				setMessageView(msg.obj.toString());
				break;
			case Nstv:
				ScrollMessage();
				break;
			case Get_carLimit:
				jsonCarLinit(msg.obj.toString(), msg.arg1);
				break;
			case get_counter:
				jsonCounter(msg.obj.toString());
				break;
			case get_gps:
				jsonGps(msg.obj.toString(), msg.arg1);
				break;
			}
		}
	};

	/** 获取所有数据 **/
	private void getTotalData() {
		if (Variable.carDatas == null || Variable.carDatas.size() == 0) {
			return;
		}
		CarData carData = Variable.carDatas.get(index);
		String device_id = carData.getDevice_id();
		if (device_id == null || device_id.equals("")) {

		} else {
			try {
				String Gas_no = "";
				if (carData.getGas_no() == null
						|| carData.getGas_no().equals("")) {
					Gas_no = "93#(92#)";
				} else {
					Gas_no = carData.getGas_no();
				}
				// 获取当前月的数据
				String url = Constant.BaseUrl + "device/" + device_id
						+ "/total?auth_code=" + Variable.auth_code
						+ "&start_day=" + startMonth + "&end_day=" + endMonth
						+ "&city=" + URLEncoder.encode(Variable.City, "UTF-8")
						+ "&gas_no=" + Gas_no;
				new NetThread.GetDataThread(handler, url, getData, index)
						.start();
				// 获取gps信息
				String gpsUrl = Constant.BaseUrl + "device/" + device_id
						+ "/active_gps_data?auth_code=" + Variable.auth_code
						+ "&update_time=2014-01-01%2019:06:43";
				new NetThread.GetDataThread(handler, gpsUrl, get_gps, index)
						.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// 获取限行信息
		if (Variable.City == null || carData.getObj_name() == null
				|| Variable.City.equals("") || carData.getObj_name().equals("")) {

		} else {
			try {
				String url = Constant.BaseUrl + "base/ban?city="
						+ URLEncoder.encode(Variable.City, "UTF-8")
						+ "&obj_name="
						+ URLEncoder.encode(carData.getObj_name(), "UTF-8");
				new NetThread.GetDataThread(handler, url, Get_carLimit, index)
						.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/** 获取GPS信息 **/
	private void jsonGps(String str, int index) {
		try {
			JSONObject jsonObject = new JSONObject(str)
					.getJSONObject("active_gps_data");
			double lat = jsonObject.getDouble("lat");
			double lon = jsonObject.getDouble("lon");
			String gpsTime = jsonObject.getString("gps_time");
			LatLng latLng = new LatLng(lat, lon);
			Variable.carDatas.get(index).setLat(lat);
			Variable.carDatas.get(index).setLon(lon);
			Variable.carDatas.get(index).setGps_time(
					GetSystem.ChangeTimeZone(gpsTime.substring(0, 19).replace(
							"T", " ")));
			GetSystem.myLog(TAG, "lat = " + lat + " , Lon = " + lon);
			mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption()
					.location(latLng));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/** 解析本月油价 **/
	private void jsonData(String str, int index) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			CarView carView = carViews.get(index);
			carView.getTv_fee().setText(
					String.format("%.0f", jsonObject.getDouble("total_fee")));// 花费
			carView.getTv_fuel().setText(
					String.format("%.0f", jsonObject.getDouble("total_fuel")));// 油耗

			// TODO 剩余里程显示
			if (jsonObject.getDouble("left_distance") == 0) {
				carView.getTv_distance().setText(
						String.format("%.0f",
								jsonObject.getDouble("total_distance")));// 里程
			} else {
				carView.getTv_current_distance().setText("剩余里程");
				carView.getTv_distance().setText(
						String.format("%.0f",
								jsonObject.getDouble("left_distance")));// 里程
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/** 解析车辆限行 **/
	private void jsonCarLinit(String result, int index) {
		try {
			CarView carView = carViews.get(index);
			JSONObject jsonObject = new JSONObject(result);
			String limit = jsonObject.getString("limit");
			carView.getTv_xx().setText(limit);
			Variable.carDatas.get(index).setLimit(limit);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取天气 onResume里获取，应为在设置页面改了城市后需要刷新
	 **/
	private void getWeather() {
		SharedPreferences preferences = getSharedPreferences(
				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		Variable.City = preferences.getString(Constant.sp_city, "");
		tv_city.setText("[ " + Variable.City + " ]");
		Variable.Province = preferences.getString(Constant.sp_province, "");
		try {
			String url = Constant.BaseUrl + "base/weather2?city="
					+ URLEncoder.encode(Variable.City, "UTF-8");
			new NetThread.GetDataThread(handler, url, getWeather).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jsonWeather(String str) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			String time = jsonObject.getJSONObject("sk").getString("time");
			tv_weather_time.setText(GetSystem.getTime(time));
			String temperature = jsonObject.getJSONObject("today").getString(
					"temperature");
			String weather = jsonObject.getJSONObject("today").getString(
					"weather");
			String quality = jsonObject.getString("quality");
			tv_weather.setText(temperature + "  " + weather + "   空气质量"
					+ quality);
			String tips = jsonObject.getString("tips");
			if (tips.equals("")) {
				tv_advice.setText(jsonObject.getJSONObject("today").getString(
						"dressing_advice"));
			} else {
				tv_advice.setText(tips);
			}
			int fa = jsonObject.getJSONObject("today")
					.getJSONObject("weather_id").getInt("fa");
			iv_weather.setImageResource(getResource("x" + fa));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/** 返回天气对应的r资源名称 **/
	public int getResource(String imageName) {
		int resId = getResources().getIdentifier(imageName, "drawable",
				"com.wise.baba");
		return resId;
	}

	boolean isCycle = true;

	class CycleThread extends Thread {
		@Override
		public void run() {
			super.run();
			while (isCycle) {
				try {
					Message message = new Message();
					message.what = cycle;
					handler.sendMessage(message);
					Thread.sleep(300000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/** 获取乐一下 **/
	private void getJoy() {
		String url = Constant.BaseUrl + "base/joy";
		new NetThread.GetDataThread(handler, url, getJoy).start();
	}

	/** 解析乐一下 **/
	private void jsonJoy(String str) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			tv_joy.setText(jsonObject.getString("content"));
			tv_happy_time.setText(GetSystem.ChangeTimeZone(
					jsonObject.getString("rcv_time").substring(0, 19)
							.replace("T", " ")).substring(11, 16));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	// 跳转到地图界面
	private void goCarMap() {
		Intent intent = new Intent(FaultActivity.this,
				CarLocationActivity.class);
		intent.putExtra("index", index);
		startActivity(intent);
	}

	/** 滑动车辆布局 **/
	private void initDataView() {// TODO 布局
		SharedPreferences preferences = getSharedPreferences(
				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		hs_car.removeAllViews();
		carViews.clear();
		for (int i = 0; i < Variable.carDatas.size(); i++) {
			View v = LayoutInflater.from(this).inflate(R.layout.item_fault,
					null);
			hs_car.addView(v);
			RelativeLayout rl_left_complete = (RelativeLayout) v
					.findViewById(R.id.rl_left_complete);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			lp.setMargins(0, 0, completed, 0);
			rl_left_complete.setLayoutParams(lp);

			LinearLayout ll_adress = (LinearLayout) v
					.findViewById(R.id.ll_adress);
			TextView tv_score = (TextView) v.findViewById(R.id.tv_score);
			TextView tv_title = (TextView) v.findViewById(R.id.tv_title);
			TasksCompletedView mTasksView = (TasksCompletedView) v
					.findViewById(R.id.tasks_view);

			mTasksView.setOnClickListener(onClickListener);
			LinearLayout ll_fee = (LinearLayout) v.findViewById(R.id.ll_fee);
			ll_fee.setOnClickListener(onClickListener);
			// 当前里程数
			TextView tv_current_distance = (TextView) v
					.findViewById(R.id.tv_current_distance);

			TextView tv_distance = (TextView) v.findViewById(R.id.tv_distance);
			TextView tv_fee = (TextView) v.findViewById(R.id.tv_fee);
			TextView tv_fuel = (TextView) v.findViewById(R.id.tv_fuel);
			TextView tv_name = (TextView) v.findViewById(R.id.tv_name);
			TextView tv_xx = (TextView) v.findViewById(R.id.tv_xx);
			TextView tv_adress = (TextView) v.findViewById(R.id.tv_adress);

			// 监听事件
			v.findViewById(R.id.Liner_distance).setOnClickListener(
					onClickListener);
			v.findViewById(R.id.Liner_fuel).setOnClickListener(onClickListener);
			v.findViewById(R.id.Liner_fee).setOnClickListener(onClickListener);
			tv_adress.setOnClickListener(onClickListener);

			TasksCompletedView tcv_drive = (TasksCompletedView) v
					.findViewById(R.id.tcv_drive);
			tcv_drive.setOnClickListener(onClickListener);
			TextView tv_drive = (TextView) v.findViewById(R.id.tv_drive);

			CarView carView = new CarView();
			carView.setLl_adress(ll_adress);
			carView.setmTasksView(mTasksView);
			carView.setTv_distance(tv_distance);
			carView.setTv_fee(tv_fee);
			carView.setTv_fuel(tv_fuel);
			carView.setTv_score(tv_score);
			carView.setTv_title(tv_title);
			carView.setTv_xx(tv_xx);
			carView.setTv_adress(tv_adress);
			carView.setTcv_drive(tcv_drive);
			carView.setTv_drive(tv_drive);
			carView.setTv_current_distance(tv_current_distance);
			carViews.add(carView);

			tv_name.setText(Variable.carDatas.get(i).getCar_series() + "("
					+ Variable.carDatas.get(i).getNick_name() + ")");
			String Device_id = Variable.carDatas.get(i).getDevice_id();
			if (Device_id == null || Device_id.equals("")) {
				carView.getmTasksView().setProgress(100);
				tv_score.setText("0");
				tv_title.setText("未绑定终端");

				tcv_drive.setProgress(100);
				tv_drive.setText("0");
			} else {
				String result = preferences.getString(Constant.sp_health_score
						+ Variable.carDatas.get(i).getObj_id(), "");
				if (result.equals("")) {// 未体检过
					carView.getmTasksView().setProgress(100);
					tv_score.setText("0");
					tv_title.setText("未体检过");
				} else {
					try {
						JSONObject jsonObject = new JSONObject(result);
						// 健康指数
						int health_score = jsonObject.getInt("health_score");
						carView.getmTasksView().setProgress(health_score);
						tv_score.setText(String.valueOf(health_score));
						tv_title.setText("健康指数");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				/** 驾驶信息 **/
				String drive = preferences.getString(Constant.sp_drive_score
						+ Variable.carDatas.get(i).getObj_id(), "");
				if (drive.equals("")) {
					tcv_drive.setProgress(100);
					tv_drive.setText("0");
				} else {
					try {
						JSONObject jsonObject = new JSONObject(drive);
						int drive_score = jsonObject.getInt("drive_score");
						carView.getTcv_drive().setProgress(drive_score);
						tv_drive.setText(String.valueOf(drive_score));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		hs_car.snapToScreen(index);
		getTotalData();
	}

	/** 未登录显示 **/
	private void setLoginView() {
		hs_car.removeAllViews();
		View v = LayoutInflater.from(this).inflate(R.layout.item_fault, null);
		hs_car.addView(v);
		RelativeLayout rl_left_complete = (RelativeLayout) v
				.findViewById(R.id.rl_left_complete);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(0, 0, completed, 0);
		rl_left_complete.setLayoutParams(lp);

		TextView tv_score = (TextView) v.findViewById(R.id.tv_score);
		TextView tv_title = (TextView) v.findViewById(R.id.tv_title);
		TasksCompletedView mTasksView = (TasksCompletedView) v
				.findViewById(R.id.tasks_view);
		mTasksView.setProgress(100);
		tv_score.setText("0");
		tv_title.setText("未体检过");
		mTasksView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(FaultActivity.this,
						LoginActivity.class));
			}
		});
		TextView tv_name = (TextView) v.findViewById(R.id.tv_name);
		tv_name.setText("绑定叭叭车载智能配件");
	}

	/** 获取消息数据 **/
	private void getCounter() {
		String url = Constant.BaseUrl + "customer/" + Variable.cust_id
				+ "/counter?auth_code=" + Variable.auth_code;
		new NetThread.GetDataThread(handler, url, get_counter).start();
	}

	/** 解析消息数据 **/
	private void jsonCounter(String str) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			if (jsonObject.opt("noti_count") != null) {
				Variable.noti_count = jsonObject.getInt("noti_count");
			}
			if (jsonObject.opt("vio_count") != null) {
				Variable.vio_count = jsonObject.getInt("vio_count");
			}
			setNotiView();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		setNotiView();
		getWeather();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	/** 设置提醒 **/
	private void setNotiView() {
		if (Variable.noti_count == 0 && Variable.vio_count == 0) {
			// 显示提醒
			iv_noti.setVisibility(View.GONE);
		} else {
			// 隐藏提醒
			iv_noti.setVisibility(View.VISIBLE);
		}
	}

	/** 获取滚动消息通知 **/
	private void getMessage(String url) {
		new NetThread.GetDataThread(handler, url, getMessage).start();
	}

	/** 当前显示消息数目 **/
	int index_message = 0;

	/** 解析滚动消息并绑定 **/
	private void setMessageView(String str) {
		nsDatas.clear();
		try {
			JSONArray jsonArray = new JSONArray(str);
			if (jsonArray.length() > 0) {
				nstv_message.setVisibility(View.VISIBLE);
			} else {
				nstv_message.setVisibility(View.GONE);
			}
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String content = jsonObject.getString("content");
				View v = LayoutInflater.from(this).inflate(R.layout.item_nstv,
						null);
				AlwaysMarqueeTextView tv_message = (AlwaysMarqueeTextView) v
						.findViewById(R.id.tv_message);
				tv_message.setText(content);
				tv_message.setOnClickListener(onClickListener);
				nstv_message.addView(v);
				NsData nsData = new NsData();
				nsData.setType(jsonObject.getInt("type"));
				if (jsonObject.opt("obj_id") == null) {
					nsData.setObj_id(0);
				} else {
					nsData.setObj_id(jsonObject.getInt("obj_id"));
				}
				nsDatas.add(nsData);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/** 滚动消息点击 **/
	private void nstvClick() {
		if (nsDatas == null || nsDatas.size() == 0) {
			// 没有数据不考虑
		} else {
			if (index_message < nsDatas.size()) {
				// 防止数组越界
				int type = nsDatas.get(index_message).getType();
				switch (type) {
				case 0:
					// 注册用户
					startActivity(new Intent(FaultActivity.this,
							LoginActivity.class));
					break;
				case 1:
					// 注册车辆
					startActivity(new Intent(FaultActivity.this,
							CarAddActivity.class));
					break;
				case 2:
					// 修改车辆
					int index = getIndexFromId(nsDatas.get(index_message)
							.getObj_id());
					if (index == -1) {
						// 没有在列表找到对应的车
						startActivity(new Intent(FaultActivity.this,
								CarActivity.class));
					} else {
						Intent intent = new Intent(FaultActivity.this,
								CarUpdateActivity.class);
						intent.putExtra("index", index);
						startActivityForResult(intent, 2);
					}
					break;
				case 3:
					// 绑定终端
					startActivity(new Intent(FaultActivity.this,
							CarActivity.class));
					break;
				case 4:
					// 通知
					break;
				case 5:
					// 问答
					break;
				case 6:
					// 私信
					break;
				}
			}
		}
	}

	/** 定时滚动 **/
	private void ScrollMessage() {
		index_message++;
		nstv_message.snapToScreen(index_message);
		if (index_message >= (nsDatas.size() - 1)) {
			index_message = 0;
		}
	}

	/** 滚动消息 **/
	class NsData {
		int type;
		int obj_id;

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public int getObj_id() {
			return obj_id;
		}

		public void setObj_id(int obj_id) {
			this.obj_id = obj_id;
		}
	}

	// 定时调整滚动消息
	class CycleNstvThread extends Thread {
		@Override
		public void run() {
			super.run();
			while (isCycle) {
				try {
					Message message = new Message();
					message.what = Nstv;
					handler.sendMessage(message);
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/** 根据obj_id返回对应列表的位置 **/
	private int getIndexFromId(int obj_id) {
		for (int i = 0; i < Variable.carDatas.size(); i++) {
			if (Variable.carDatas.get(i).getObj_id() == obj_id) {
				return i;
			}
		}
		return -1;
	}

	List<CarView> carViews = new ArrayList<CarView>();

	private class CarView {
		LinearLayout ll_adress;
		TextView tv_current_distance;
		TextView tv_distance;
		TextView tv_adress;
		TextView tv_fee;
		TextView tv_fuel;
		TextView tv_score;
		TextView tv_title;
		TextView tv_xx;
		TasksCompletedView mTasksView;
		TasksCompletedView tcv_drive;
		TextView tv_drive;

		public TextView getTv_current_distance() {
			return tv_current_distance;
		}

		public void setTv_current_distance(TextView tv_current_distance) {
			this.tv_current_distance = tv_current_distance;
		}

		public LinearLayout getLl_adress() {
			return ll_adress;
		}

		public void setLl_adress(LinearLayout ll_adress) {
			this.ll_adress = ll_adress;
		}

		public TextView getTv_adress() {
			return tv_adress;
		}

		public void setTv_adress(TextView tv_adress) {
			this.tv_adress = tv_adress;
		}

		public TextView getTv_distance() {
			return tv_distance;
		}

		public void setTv_distance(TextView tv_distance) {
			this.tv_distance = tv_distance;
		}

		public TextView getTv_fee() {
			return tv_fee;
		}

		public void setTv_fee(TextView tv_fee) {
			this.tv_fee = tv_fee;
		}

		public TextView getTv_fuel() {
			return tv_fuel;
		}

		public void setTv_fuel(TextView tv_fuel) {
			this.tv_fuel = tv_fuel;
		}

		public TasksCompletedView getmTasksView() {
			return mTasksView;
		}

		public void setmTasksView(TasksCompletedView mTasksView) {
			this.mTasksView = mTasksView;
		}

		public TextView getTv_score() {
			return tv_score;
		}

		public void setTv_score(TextView tv_score) {
			this.tv_score = tv_score;
		}

		public TextView getTv_title() {
			return tv_title;
		}

		public void setTv_title(TextView tv_title) {
			this.tv_title = tv_title;
		}

		public TextView getTv_xx() {
			return tv_xx;
		}

		public void setTv_xx(TextView tv_xx) {
			this.tv_xx = tv_xx;
		}

		public TasksCompletedView getTcv_drive() {
			return tcv_drive;
		}

		public void setTcv_drive(TasksCompletedView tcv_drive) {
			this.tcv_drive = tcv_drive;
		}

		public TextView getTv_drive() {
			return tv_drive;
		}

		public void setTv_drive(TextView tv_drive) {
			this.tv_drive = tv_drive;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 3) {
			// 修改车辆信息
			initDataView();
		} else if (requestCode == 0) {
			// 修改城市返回,在onResume里刷新了城市
		} else if (requestCode == 1) {
			// 体检返回重新布局
			initDataView();
		} else if (requestCode == 2) {
			// TODO 驾驶习惯返回
			/** 驾驶信息 **/
			SharedPreferences preferences = getSharedPreferences(
					Constant.sharedPreferencesName, Context.MODE_PRIVATE);
			String drive = preferences.getString(Constant.sp_drive_score
					+ Variable.carDatas.get(index).getObj_id(), "");
			if (drive.equals("")) {
				System.out.println("没有驾驶信息");
			} else {
				try {
					JSONObject jsonObject = new JSONObject(drive);
					int drive_score = jsonObject.getInt("drive_score");
					carViews.get(index).getTcv_drive().setProgress(drive_score);
					carViews.get(index).getTv_drive()
							.setText(String.valueOf(drive_score));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(myBroadCastReceiver);
		isCycle = false;
	}

	long waitTime = 2000;
	long touchTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (smv_content.getCurrentScreen() == 1) {
				smv_content.snapToScreen(0);
			} else {
				long currentTime = System.currentTimeMillis();
				if (touchTime == 0 || (currentTime - touchTime) >= waitTime) {
					Toast.makeText(this, "再按一次退出客户端", Toast.LENGTH_SHORT)
							.show();
					touchTime = currentTime;
				} else {
					finish();
				}
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	class MyBroadCastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Constant.A_RefreshHomeCar)) {
				GetSystem.myLog(TAG, "A_RefreshHomeCar");
				initDataView();
				getTotalData();
				String url = Constant.BaseUrl + "customer/" + Variable.cust_id
						+ "/tips?auth_code=" + Variable.auth_code;
				getMessage(url);
				getCounter();
				noticeFragment.ResetNotice();
			} else if (action.equals(Constant.A_LoginOut)) {
				setLoginView();
				String url = Constant.BaseUrl + "customer/0/tips";
				getMessage(url);
				noticeFragment.ClearNotice();
			} else if (action.equals(Constant.A_City)) {
				try {
					Variable.City = intent.getStringExtra("City");
					Variable.Province = intent.getStringExtra("Province");
					String url = Constant.BaseUrl + "base/weather2?city="
							+ URLEncoder.encode(Variable.City, "UTF-8");
					new NetThread.GetDataThread(handler, url, getWeather)
							.start();
					SharedPreferences preferences = getSharedPreferences(
							Constant.sharedPreferencesName,
							Context.MODE_PRIVATE);
					Editor editor = preferences.edit();
					editor.putString(Constant.sp_city, Variable.City);
					editor.putString(Constant.sp_province, Variable.Province);
					editor.commit();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
		@Override
		public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
				// 没有检索到结果
			} else {
				try {
					GetSystem.myLog(TAG, "获取位置信息");
					Variable.carDatas.get(index).setAdress(result.getAddress());
					String gpsTime = Variable.carDatas.get(index).getGps_time();
					String gpsData = gpsTime.substring(0, 10);// 取出日期
					String nowData = GetSystem.GetNowDay();
					String showTime = "";
					if (gpsData.equals(nowData)) {
						showTime = gpsTime.substring(11, 16);
					} else if (gpsData.equals(GetSystem
							.GetNextData(nowData, -1))) {
						showTime = "昨天" + gpsTime.substring(11, 16);
					} else if (gpsData.equals(GetSystem
							.GetNextData(nowData, -2))) {
						showTime = "前天" + gpsTime.substring(11, 16);
					} else {
						showTime = gpsTime.substring(5, 16);
					}
					// 显示时间
					carViews.get(index).getLl_adress()
							.setVisibility(View.VISIBLE);
					carViews.get(index).getTv_adress()
							.setText(result.getAddress() + "  " + showTime);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void onGetGeoCodeResult(GeoCodeResult arg0) {

		}
	};
}