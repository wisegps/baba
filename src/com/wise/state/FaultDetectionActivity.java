package com.wise.state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.gson.Gson;
import com.umeng.analytics.MobclickAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Const;
import com.wise.baba.app.Constant;
import com.wise.baba.app.Msg;
import com.wise.baba.biz.GetDataFromUrl;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.biz.GetUrl;
import com.wise.baba.entity.ActiveGpsData;
import com.wise.baba.entity.CarData;
import com.wise.baba.entity.GpsData;
import com.wise.baba.entity.Info;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.adapter.ListDetectAdapter;
import com.wise.baba.ui.widget.DialView;
import com.wise.baba.ui.widget.FaultDeletionView;
import com.wise.baba.ui.widget.OnViewChangeListener;
import com.wise.baba.util.DialBitmapFactory;
import com.wise.car.DevicesAddActivity;
import com.wise.car.SearchMapActivity;
import com.wise.notice.LetterActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 车况检测
 * 
 * @author honesty
 * 
 */
public class FaultDetectionActivity extends Activity implements OnClickListener {
	private static final int getData = 1;
	private static final int refresh = 2;
	private static final int getFault = 3;

	/** 刷新分数 **/
	private static final int refresh_score = 4;
	/** 车辆当前状态，离线or未启动 **/
	private static final int CAR_TYPE_ONLINE = 5;
	private static final int CAR_TYPE_STOP = 6;
	/** 开始体检时初始化状态 **/
	private static final int INIT_STATUS = 7;
	/** 体检动画变化的值 */
	TextView tv_name;
	LinearLayout ll_fault;

	private ListView listDetection;
	private ListDetectAdapter adapter;
	ImageView imgHealthScore;
	/** 体检返回分数 **/
	private int mTotalProgress = 100;
	/** 动画过程显示分数 **/
	private int mCurrentProgress = 100;
	/** 总共有几项体检 **/
	private static final int Point = 6;
	/** 循环几次更新一项体检 **/
	int Interval = 0;
	/** 设置要循环的次数 **/

	String fault_content = "";
	/** 当前列表位置 **/
	int index;
	boolean inProgress = false;
	FaultDeletionView hs_car;
	AppApplication app;
	List<CarData> carDatas;
	List<CarView> carViews = new ArrayList<CarView>();
	private boolean isFirstCreated = false;// 界面第一次进入
	// public long peroidRefersh = 1;
	public long duration = 300;// 单项体检耗时

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_fault_detection);
		isFirstCreated = true;
		app = (AppApplication) getApplication();
		index = this.getIntent().getIntExtra("index", 0);
		Log.i("FaultDetectionActivity", "当前车辆位置" + index);
		carDatas = (List<CarData>) this.getIntent().getSerializableExtra(
				"carDatas");
		if (carDatas == null) {
			carDatas = app.carDatas;
			index = app.currentCarIndex;
		}
		
		Log.i("FaultDetectionActivity", "carDatas: " + carDatas.size());
		initView();
		initDataView();
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case Msg.Dial_Refresh_Value:
				int value = msg.arg1;
				carViews.get(index).getTv_score().setText("" + value);
				// int peroid = mTotalProgress / Point;
				// int j = 0;
				// if (value == 100) {
				// j = 1;
				// } else if (value == 100 - peroid) {
				// j = 2;
				// } else if (value == 100 - peroid * 2) {
				// j = 3;
				// } else if (value == 100 - peroid * 3) {
				// j = 4;
				// } else if (value == 100 - peroid * 4) {
				// j = 5;
				// } else if (value == 100 - peroid * 5) {
				// j = 6;
				// } else if (value == mTotalProgress) {
				// Log.i("FaultDetectionActivity", "inProgress = false");
				// inProgress = false;
				// }
				// Message message1 = new Message();
				// message1.what = refresh;
				// message1.arg1 = j;
				// handler.sendMessage(message1);
				break;
			case getData:
				Log.i("FaultDetectionActivity", "getData");
				result = msg.obj.toString();
				jsonHealth(msg.obj.toString());
				// 体检
				DialView view = carViews.get(index).getDialHealthScore();
				long totalTime = duration * Point;
				view.startAnimation(mTotalProgress, handler, totalTime);
				refreshHealth(1);
				break;
			case refresh:
				// refreshHealth(msg.arg1);
				break;
			case getFault:
				fault_content = msg.obj.toString();
				break;
			case refresh_score:
				carViews.get(index).getTv_score()
						.setText(String.valueOf(mCurrentProgress));

				carViews.get(index).getDialHealthScore()
						.initValue(mCurrentProgress, handler);
				if (msg.arg2 == 0) {
					// carViews.get(index).getTv_detection_status().setText("点击体检");
				}
				break;
			case CAR_TYPE_ONLINE:
				showDialogOnLine();
				break;
			case CAR_TYPE_STOP:
				showDialogStop();
				break;
			case INIT_STATUS:
				initapp();
				break;
			}
		}
	};

	/** 滑动车辆布局 **/
	private void initDataView() {
		SharedPreferences preferences = getSharedPreferences(
				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		hs_car.removeAllViews();
		carViews = new ArrayList<CarView>();
		for (int i = 0; i < carDatas.size(); i++) {
			View v = LayoutInflater.from(this).inflate(
					R.layout.item_fault_detection, null);
			hs_car.addView(v);

			DialView dialHealthScore = (DialView) v
					.findViewById(R.id.dialHealthScore);
			dialHealthScore.setOnClickListener(this);
			TextView tv_score = (TextView) v.findViewById(R.id.tv_score);
			TextView tv_title = (TextView) v.findViewById(R.id.tv_title);
			CarView carView = new CarView();
			carView.setDialHealthScore(dialHealthScore);
			carView.setTv_score(tv_score);
			carView.setTv_title(tv_title);
			carViews.add(carView);

			tv_name.setText(carDatas.get(i).getCar_series() + "("
					+ carDatas.get(i).getNick_name() + ")");
			String result = preferences.getString(Constant.sp_health_score
					+ carDatas.get(i).getObj_id(), "");
			if (result.equals("")) {// 未体检过
				carViews.get(i).getDialHealthScore()
						.initValue(100, handler);
				tv_score.setText("0");
				tv_title.setText("未体检过");
			} else {
				try {
					JSONObject jsonObject = new JSONObject(result);
					// 健康指数
					int health_score = jsonObject.getInt("health_score");
					carViews.get(i).getDialHealthScore()
							.initValue(health_score, handler);
					tv_score.setText(String.valueOf(health_score));
					tv_title.setText("健康指数");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	/** 获取历史消息 **/
	private void getSpHistoryData(int index) {

		Log.i("FaultDetectionActivity", "getSpHistoryData");
		if (carDatas == null || carDatas.size() == 0) {
			return;
		}
		CarData carData = carDatas.get(index);
		tv_name.setText(carData.getNick_name());

		String Device_id = carData.getDevice_id();
		if (Device_id == null || Device_id.equals("")) {
			carViews.get(index).getDialHealthScore().initValue(100, handler);
			carViews.get(index).getTv_score().setText(String.valueOf(0));
			notifyListView(Const.DETECT_NO_DEVICE, null);
		} else {
			SharedPreferences preferences = getSharedPreferences(
					Constant.sharedPreferencesName, Context.MODE_PRIVATE);
			result = preferences.getString(Constant.sp_health_score
					+ carDatas.get(index).getObj_id(), "");
			if (result.equals("")) {// 未体检过

				Log.i("FaultDetectionActivity", "未体检过");
				carViews.get(index).getDialHealthScore()
						.initValue(100, handler);
				carViews.get(index).getTv_score().setText(String.valueOf(0));
				notifyListView(Const.DETECT_NOT_DETECTED, null);

			} else {
				try {
					Log.i("FaultDetectionActivity", "有历史体检数据" + result);
					JSONObject jsonObject = new JSONObject(result);
					// 健康指数
					int health_score = jsonObject.getInt("health_score");
					carViews.get(index).getTv_score()
							.setText(String.valueOf(health_score));
					carViews.get(index).getDialHealthScore()
							.initValue(health_score, handler);
					int faults[] = { 0, 0, 0, 0, 0, 0 };
					JSONArray jsonErrArray = jsonObject
							.getJSONArray("active_obd_err");
					if (jsonErrArray.length() > 0) {
						String url = Constant.BaseUrl
								+ "device/fault_desc_new?auth_code="
								+ app.auth_code;
						List<NameValuePair> params = new ArrayList<NameValuePair>();
						params.add(new BasicNameValuePair("brand", carDatas
								.get(index).getCar_brand()));
						params.add(new BasicNameValuePair("obd_err", jsonObject
								.getString("active_obd_err")));
						new NetThread.postDataThread(handler, url, params,
								getFault).start();
						faults[0] = jsonErrArray.length();
					} else {
						faults[0] = 0;
					}

					// 电源系统
					boolean if_dpdy_err = !jsonObject.getBoolean("if_dpdy_err");
					dpdy_content = jsonObject.getString("dpdy_content");
					faults[1] = if_dpdy_err ? 0 : 1;
					// 进气系统
					boolean if_jqmkd_err = !jsonObject
							.getBoolean("if_jqmkd_err");
					jqmkd_content = jsonObject.getString("jqmkd_content");
					faults[2] = if_jqmkd_err ? 0 : 1;
					// 怠速控制系统
					boolean if_fdjzs_err = !jsonObject
							.getBoolean("if_fdjzs_err");
					fdjzs_content = jsonObject.getString("fdjzs_content");
					faults[3] = if_fdjzs_err ? 0 : 1;
					// 冷却系统
					boolean if_sw_err = !jsonObject.getBoolean("if_sw_err");
					sw_content = jsonObject.getString("sw_content");
					faults[4] = if_sw_err ? 0 : 1;
					// 排放系统
					boolean if_chqwd_err = !jsonObject
							.getBoolean("if_chqwd_err");
					chqwd_content = jsonObject.getString("chqwd_content");
					faults[5] = if_chqwd_err ? 0 : 1;
					// 获取历史消息
					Log.i("FaultDetectionActivity", "获取历史消息");
					notifyListView(Const.DETECT_HISTORY, faults);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	private class CarView {
		TextView tv_score;
		TextView tv_title;
		DialView dialHealthScore;

		public DialView getDialHealthScore() {
			return dialHealthScore;
		}

		public void setDialHealthScore(DialView dialHealthScore) {
			this.dialHealthScore = dialHealthScore;
		}

		public TextView getTv_title() {
			return tv_title;
		}

		public void setTv_title(TextView tv_title) {
			this.tv_title = tv_title;
		}

		public TextView getTv_score() {
			return tv_score;
		}

		public void setTv_score(TextView tv_score) {
			this.tv_score = tv_score;
		}
	}

	/** 初始化数据 **/
	private void initapp() {
		// j = 0;
		// mCurrent = 0;
		inProgress = true;
		mTotalProgress = 100;
		Interval = 30 / Point;
		carViews.get(index).getDialHealthScore().initValue(100, handler);
		carViews.get(index).getTv_score().setText(String.valueOf(100));
		// carViews.get(index).getTv_detection_status().setText("体检中");
		Log.i("FaultDetectionActivity", "体检中 初始化数据 ");
		// 始化数据
		notifyListView(Const.DETECT_IN_PROGRESS, new int[] { -1, -1, -1, -1,
				-1, -1 });
	}

	public void notifyListView(final int flag, final int[] faults) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				adapter.change(flag, faults);
				adapter.notifyDataSetChanged();
			}

		});
	}

	private void initView() {
		ll_fault = (LinearLayout) findViewById(R.id.ll_fault);
		hs_car = (FaultDeletionView) findViewById(R.id.hs_car);
		hs_car.setOnViewChangeListener(new OnViewChangeListener() {
			@Override
			public void OnViewChange(int view, int duration) {
				// 关闭线程
				index = view;
				getSpHistoryData(index);
			}
		});

		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(this);
		tv_name = (TextView) findViewById(R.id.tv_name);
		listDetection = (ListView) findViewById(R.id.listDetection);
		adapter = new ListDetectAdapter(this);
		listDetection.setAdapter(adapter);
		listDetection.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				clickItem(position);
			}
		});
		adapter.notifyDataSetChanged();
	}

	public void clickItem(int item) {

		String Device_id = carDatas.get(index).getDevice_id();

		Intent intent2 = new Intent(FaultDetectionActivity.this,
				DevicesAddActivity.class);
		intent2.putExtra("car_series_id", carDatas.get(index)
				.getCar_series_id());
		intent2.putExtra("car_series", carDatas.get(index).getCar_series());
		Intent intent = new Intent(FaultDetectionActivity.this,
				DyActivity.class);
		intent.putExtra("device_id", Device_id);
		intent.putExtra("state", carDatas.get(index).isStop());

		if (Device_id == null || Device_id.equals("")) {
			intent.putExtra("car_id", carDatas.get(index).getObj_id());
			startActivityForResult(intent2, 2);
			return;
		}

		try {
			JSONObject jsonObject = new JSONObject(result);
			switch (item) {
			case 0:
				Intent intent1 = new Intent(FaultDetectionActivity.this,
						FaultDetailActivity.class);
				intent1.putExtra("fault_content", fault_content);
				intent1.putExtra("device_id", Device_id);
				intent1.putExtra("index", index);
				startActivity(intent1);
				break;
			case 1:
				intent.putExtra("type", 1);
				intent.putExtra("title", "电源系统");
				intent.putExtra("name", "蓄电池电压");
				intent.putExtra("range", jsonObject.getString("dpdy_range"));
				intent.putExtra("if_err", !jsonObject.getBoolean("if_dpdy_err"));
				intent.putExtra("current", jsonObject.getString("dpdy"));
				intent.putExtra("if_lt_err",
						!jsonObject.getBoolean("if_lt_dpdy_err"));
				intent.putExtra("lt", jsonObject.getString("lt_dpdy"));
				intent.putExtra("url", jsonObject.getString("dpdy_content"));
				startActivity(intent);
				break;
			case 2:
				intent.putExtra("type", 2);
				intent.putExtra("title", "进气系统");
				intent.putExtra("name", "节气门开度");
				intent.putExtra("range", jsonObject.getString("jqmkd_range"));
				intent.putExtra("if_err",
						!jsonObject.getBoolean("if_jqmkd_err"));
				intent.putExtra("current", jsonObject.getString("jqmkd"));
				intent.putExtra("if_lt_err",
						!jsonObject.getBoolean("if_lt_jqmkd_err"));
				intent.putExtra("lt", jsonObject.getString("lt_jqmkd"));
				intent.putExtra("url", jsonObject.getString("jqmkd_content"));
				startActivity(intent);
				break;
			case 3:
				intent.putExtra("type", 3);
				intent.putExtra("title", "怠速控制系统");
				intent.putExtra("name", "怠速状态");
				intent.putExtra("range", jsonObject.getString("fdjzs_range"));
				intent.putExtra("if_err",
						!jsonObject.getBoolean("if_fdjzs_err"));
				intent.putExtra("current", jsonObject.getString("fdjzs"));
				intent.putExtra("if_lt_err",
						!jsonObject.getBoolean("if_lt_fdjzs_err"));
				intent.putExtra("lt", jsonObject.getString("lt_fdjzs"));
				intent.putExtra("url", jsonObject.getString("fdjzs_content"));
				startActivity(intent);
				break;
			case 4:
				intent.putExtra("type", 4);
				intent.putExtra("title", "冷却系统");
				intent.putExtra("name", "水温状态");
				intent.putExtra("range", jsonObject.getString("sw_range"));
				intent.putExtra("if_err", !jsonObject.getBoolean("if_sw_err"));
				intent.putExtra("current", jsonObject.getString("sw"));
				intent.putExtra("if_lt_err",
						!jsonObject.getBoolean("if_lt_sw_err"));
				intent.putExtra("lt", jsonObject.getString("lt_sw"));
				intent.putExtra("url", jsonObject.getString("sw_content"));
				startActivity(intent);
				break;
			case 5:
				intent.putExtra("type", 5);
				intent.putExtra("title", "排放系统");
				intent.putExtra("name", "三元催化器状态");
				intent.putExtra("range", jsonObject.getString("chqwd_range"));
				intent.putExtra("if_err",
						!jsonObject.getBoolean("if_chqwd_err"));
				intent.putExtra("current", jsonObject.getString("chqwd"));
				intent.putExtra("if_lt_err",
						!jsonObject.getBoolean("if_lt_chqwd_err"));
				intent.putExtra("lt", jsonObject.getString("lt_chqwd"));
				intent.putExtra("url", jsonObject.getString("chqwd_content"));
				startActivity(intent);
				break;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	/** 点击体检 **/
	private void clickHealth() {
		if (inProgress) {// 判断体检进行中
			Toast.makeText(FaultDetectionActivity.this, "体检进行中",
					Toast.LENGTH_SHORT).show();
			return;
		}
		CarData carData = carDatas.get(index);
		String Device_id = carData.getDevice_id();
		if (Device_id == null || Device_id.equals("")) {// 服务商如何处理
			Intent intent = new Intent(FaultDetectionActivity.this,
					DevicesAddActivity.class);
			intent.putExtra("car_id", carData.getObj_id());
			intent.putExtra("car_series_id", carData.getCar_series_id());
			intent.putExtra("car_series", carData.getCar_series());
			startActivityForResult(intent, 2);
			return;
		}
		// 开启线程获取数据
		new Thread(new MyThread()).start();
	}

	/**
	 * 开启线程获取gps信息和车辆健康信息
	 * 
	 * @author honesty
	 * 
	 */
	class MyThread implements Runnable {
		@Override
		public void run() {
			try {
				Log.i("FaultDetectionActivity", "开启线程获取gps信息和车辆健康信息");

				String Device_id = carDatas.get(index).getDevice_id();
				// 获取车的最新信息
				String gpsUrl = GetUrl.getCarGpsData(Device_id, app.auth_code);
				String gpsResult = GetDataFromUrl.getData(gpsUrl);
				// 解析gps数据
				Gson gson = new Gson();
				// 获取所有gps数据
				ActiveGpsData activeGpsData = gson.fromJson(gpsResult,
						ActiveGpsData.class);
				if (activeGpsData == null
						|| activeGpsData.getActive_gps_data() == null
						|| activeGpsData.getActive_gps_data().equals("")) {
					// 没有定位信息
					Log.i("FaultDetectionActivity", "没有定位信息");
					Message message = new Message();
					message.what = CAR_TYPE_ONLINE;
					handler.sendMessage(message);
					return;
				}
				// 解析gps数据
				GpsData gpsData = activeGpsData.getActive_gps_data();
				// 先判断离线
				if (gpsData == null
						|| gpsData.getRcv_time() == null
						|| (GetSystem.spacingNowTime(GetSystem
								.ChangeTimeZone(gpsData.getRcv_time()
										.substring(0, 19).replace("T", " "))) / 60) > 10) {
					// 提示离线
					Message message = new Message();
					message.what = CAR_TYPE_ONLINE;
					handler.sendMessage(message);
					return;
				}
				boolean isStop = true;
				for (int i = 0; i < gpsData.getUni_status().size(); i++) {
					if (gpsData.getUni_status().get(i) == Info.CarStartStatus) {
						isStop = false;
						break;
					}
				}

				if (isStop) {
					// 提示车辆未启动
					Log.i("FaultDetectionActivity", "提示车辆未启动");
					Message message = new Message();
					message.what = CAR_TYPE_STOP;
					handler.sendMessage(message);
					return;
				}

				carDatas.get(index).setStop(isStop); // 几下车辆启动状态

				Log.i("FaultDetectionActivity", " 初始化状态");

				// 初始化状态
				Message msgInit = new Message();
				msgInit.what = INIT_STATUS;
				handler.sendMessage(msgInit);
				// 获取体检数据
				Log.i("FaultDetectionActivity", "获取体检数据");
				String healthUrl = GetUrl.getHealthData(Device_id,
						app.auth_code, carDatas.get(index).getCar_brand());
				Log.i("FaultDetectionActivity", "healthUrl " + healthUrl);
				String healthResult = GetDataFromUrl.getData(healthUrl);

				Message msgGetData = new Message();
				msgGetData.what = getData;
				msgGetData.obj = healthResult;
				handler.sendMessage(msgGetData);

				/*
				 * Message message = new Message(); message.what = INIT_STATUS;
				 * handler.sendMessage(message);
				 * 
				 * SharedPreferences preferences = getSharedPreferences(
				 * Constant.sharedPreferencesName, Context.MODE_PRIVATE); result
				 * = preferences.getString(Constant.sp_health_score +
				 * carDatas.get(index).getObj_id(), "");
				 * 
				 * message = new Message(); message.what = getData; message.obj
				 * = result; handler.sendMessage(message);
				 */
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	/** 提示车辆离线 **/
	private void showDialogOnLine() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				FaultDetectionActivity.this);
		builder.setTitle("提示");
		builder.setMessage("车辆处于熄火状态，请先启动车辆，等待1到3分钟后，再进行车辆体检!");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		}).show();
	}

	/** 提示车辆未启动 **/
	private void showDialogStop() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				FaultDetectionActivity.this);
		builder.setTitle("提示");
		builder.setMessage("车辆未启动，您可以点击查看历史平均数据。");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		}).show();
	}

	String result = "";
	String dpdy_content, jqmkd_content, fdjzs_content, sw_content,
			chqwd_content;

	private void refreshHealth(final int r) {

		if (r == 0) {
			return;
		}
		Log.i("FaultDetectionActivity", "refreshHealth" + r);
		try {
			JSONObject jsonObject = new JSONObject(result);
			switch (r) {
			case 1:
				JSONArray jsonErrArray = jsonObject
						.getJSONArray("active_obd_err");
				if (jsonErrArray.length() > 0) {
					String url = Constant.BaseUrl
							+ "device/fault_desc_new?auth_code="
							+ app.auth_code;
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("brand", app.carDatas
							.get(index).getCar_brand()));
					params.add(new BasicNameValuePair("obd_err", jsonObject
							.getString("active_obd_err")));
					new NetThread.postDataThread(handler, url, params, getFault)
							.start();
					changeItem(0, jsonErrArray.length());
				} else {
					changeItem(0, 0);
				}
				break;
			case 2:
				// 电源系统
				boolean if_dpdy_err = !jsonObject.getBoolean("if_dpdy_err");
				dpdy_content = jsonObject.getString("dpdy_content");
				if (if_dpdy_err) {
					changeItem(1, 0);
				} else {
					changeItem(1, 1);
				}
				break;
			case 3:
				// 进气系统
				boolean if_jqmkd_err = !jsonObject.getBoolean("if_jqmkd_err");
				jqmkd_content = jsonObject.getString("jqmkd_content");
				if (if_jqmkd_err) {
					changeItem(2, 0);
				} else {
					changeItem(2, 1);
				}
				break;
			case 4:
				// 怠速控制系统

				boolean if_fdjzs_err = !jsonObject.getBoolean("if_fdjzs_err");
				fdjzs_content = jsonObject.getString("fdjzs_content");
				if (if_fdjzs_err) {
					changeItem(3, 0);
				} else {
					changeItem(3, 1);
				}
				break;
			case 5:
				// 冷却系统
				boolean if_sw_err = !jsonObject.getBoolean("if_sw_err");
				sw_content = jsonObject.getString("sw_content");
				if (if_sw_err) {
					changeItem(4, 0);
				} else {
					changeItem(4, 1);
				}
				break;
			case 6:
				// 排放系统
				boolean if_chqwd_err = !jsonObject.getBoolean("if_chqwd_err");
				chqwd_content = jsonObject.getString("chqwd_content");
				if (if_chqwd_err) {
					changeItem(5, 0);
				} else {
					changeItem(5, 1);
				}
				inProgress = false;
				break;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (r == 6) {
			inProgress = false;
			return;
		}

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {

				int next = r + 1;
				refreshHealth(next);
			}
		}, duration);

	}

	public void changeItem(final int flag, final int faultCode) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				Log.i("FaultDetectionActivity", "改变一个");
				adapter.changeItem(flag, faultCode);
				adapter.notifyDataSetChanged();
			}
		});
	}

	// 存储
	private void jsonHealth(String str) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			int health_score = jsonObject.getInt("health_score");
			// 分数
			mTotalProgress = health_score;
			Interval = 30 / Point;
			// 体检结果存起来
			SharedPreferences preferences = getSharedPreferences(
					Constant.sharedPreferencesName, Context.MODE_PRIVATE);
			Editor editor = preferences.edit();
			editor.putString(Constant.sp_health_score
					+ carDatas.get(index).getObj_id(), str);
			editor.commit();
			carViews.get(index).getTv_title().setText("健康指数");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void Back() {
		Intent intent = new Intent();
		intent.putExtra("health_score", mTotalProgress);
		setResult(2, intent);
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Back();
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);

		if (isFirstCreated == false) {
			// 若果不是第一次创建就返回
			return;
		}
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				hs_car.snapFastToScreen(index);
				getSpHistoryData(index);
				clickHealth();
			}
		}, 50);
		isFirstCreated = false;

	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 1) {// 绑定终端返回
			initDataView();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					hs_car.snapFastToScreen(index);
					getSpHistoryData(index);
				}
			}, 50);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.dialHealthScore: // 点击体检
			clickHealth();
			break;
		case R.id.iv_back:
			Back();
			finish();
			break;
		case R.id.llytExpert:
			Intent intent = new Intent(FaultDetectionActivity.this,
					LetterActivity.class);
			intent.putExtra("cust_id", "12");
			intent.putExtra("cust_name", "专家");
			startActivity(intent);
			break;
		// 救援
		case R.id.llytRescue:
			try {// 平板没有电话模块异常
				String phone = carDatas.get(index).getInsurance_tel();
				Intent in_1 = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
						+ (phone != null ? phone : "")));
				startActivity(in_1);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		// 报险
		case R.id.llytInsurance:
			try {// 平板没有电话模块异常
				String tel = carDatas.get(index).getMaintain_tel();
				Intent in_2 = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
						+ (tel != null ? tel : "")));
				startActivity(in_2);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		// 问一下
		case R.id.llytAsk:
			Toast.makeText(FaultDetectionActivity.this, "更新中.....",
					Toast.LENGTH_SHORT).show();
			break;
		// 找气修
		case R.id.llytRepair:
			Intent in = new Intent(FaultDetectionActivity.this,
					SearchMapActivity.class);
			in.putExtra("index", index);
			in.putExtra("keyWord", "维修店");
			in.putExtra("key", "汽车维修");
			in.putExtra("latitude", 0);
			in.putExtra("longitude", 0);
			startActivity(in);
			break;
		}

	}
}
