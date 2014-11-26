package com.wise.car;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import pubclas.Constant;
import pubclas.JsonData;
import pubclas.NetThread;
import com.umeng.analytics.MobclickAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.ManageActivity;
import com.wise.baba.R;
import customView.WaitLinearLayout;
import customView.WaitLinearLayout.OnFinishListener;
import data.CarData;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 添加绑定终端
 * 
 * @author honesty
 */
public class DevicesAddActivity extends Activity {
	private static final String TAG = "DevicesAddActivity";

	private static final int check_serial = 1;
	private static final int add_serial = 2;
	private static final int update_sim = 3;
	private static final int update_user = 4;
	private static final int update_car = 5;
	private static final int get_data = 6;
	private static final int update_serial = 7;

	ImageView iv_serial;
	ImageView iv_add;
	EditText et_serial, et_sim;
	TextView tv_note;
	Button tv_jump;
	RelativeLayout rl_wait;
	WaitLinearLayout ll_wait;

	int car_id;
	/** true绑定终端，false修改终端 **/
	boolean isBind;
	String device_id;
	/** 快速注册 **/
	boolean fastTrack = false;
	AppApplication app;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		ManageActivity.getActivityInstance().addActivity(this);
		setContentView(R.layout.activity_devices_add);
		app = (AppApplication)getApplication();
		ll_wait = (WaitLinearLayout) findViewById(R.id.ll_wait);
		ll_wait.setOnFinishListener(onFinishListener);
		tv_note = (TextView) findViewById(R.id.tv_note);
		tv_jump = (Button) findViewById(R.id.tv_jump);
		tv_jump.setOnClickListener(onClickListener);
		rl_wait = (RelativeLayout) findViewById(R.id.rl_wait);
		et_serial = (EditText) findViewById(R.id.et_serial);
		et_serial.setOnFocusChangeListener(onFocusChangeListener);
		et_sim = (EditText) findViewById(R.id.et_sim);
		et_sim.setOnFocusChangeListener(onFocusChangeListener);
		iv_serial = (ImageView) findViewById(R.id.iv_serial);
		iv_serial.setOnClickListener(onClickListener);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		iv_add = (ImageView) findViewById(R.id.iv_add);
		iv_add.setOnClickListener(onClickListener);

		Intent intent = getIntent();
		car_id = intent.getIntExtra("car_id", 0);
		isBind = intent.getBooleanExtra("isBind", true);
		fastTrack = intent.getBooleanExtra("fastTrack", false);
		if(!isBind){
			//接收并现实以前的终端值
			String old_device_id = intent.getStringExtra("old_device_id");
			String url = Constant.BaseUrl + "device/" + old_device_id
					+ "?auth_code=" + app.auth_code;
			new NetThread.GetDataThread(handler, url, update_serial).start();
		}
		if (fastTrack) {
			tv_jump.setVisibility(View.VISIBLE);
		} else {
			tv_jump.setVisibility(View.GONE);
		}
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.iv_serial:
				startActivityForResult(new Intent(DevicesAddActivity.this,
						BarcodeActivity.class), 0);
				break;
			case R.id.iv_add:
				Add();
				break;
			case R.id.tv_jump:
				String url = Constant.BaseUrl + "customer/" + app.cust_id
						+ "/vehicle?auth_code=" + app.auth_code;
				new NetThread.GetDataThread(handler, url, get_data).start();
				break;
			}
		}
	};

	OnFinishListener onFinishListener = new OnFinishListener() {
		@Override
		public void OnFinish(int index) {
			SaveDataOver();
			if (fastTrack) {
				String url = Constant.BaseUrl + "customer/" + app.cust_id
						+ "/vehicle?auth_code=" + app.auth_code;
				new NetThread.GetDataThread(handler, url, get_data).start();
			} else {
				updateVariableCarData();
				Intent intent = new Intent();
				setResult(1, intent);
				finish();
				Intent intent1 = new Intent(Constant.A_RefreshHomeCar);
				sendBroadcast(intent1);
			}
		}
	};

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case check_serial:
				Log.d(TAG, "返回=" + msg.obj.toString());
				jsonSerial(msg.obj.toString());
				break;
			case add_serial:
				jsonAddSerial(msg.obj.toString());
				break;
			case update_sim:
				try {
					String status_code = new JSONObject(msg.obj.toString())
							.getString("status_code");
					if (status_code.equals("0")) {
						String url_sim = Constant.BaseUrl + "device/"
								+ device_id + "/customer?auth_code="
								+ app.auth_code;
						List<NameValuePair> paramSim = new ArrayList<NameValuePair>();
						paramSim.add(new BasicNameValuePair("cust_id",
								app.cust_id));
						new NetThread.putDataThread(handler, url_sim, paramSim,
								update_user).start();
					} else {
						SaveDataOver();
						showToast();
					}
				} catch (Exception e) {
					e.printStackTrace();
					SaveDataOver();
					showToast();
				}

				break;
			case update_user:
				try {
					String status_code = new JSONObject(msg.obj.toString())
							.getString("status_code");
					if (status_code.equals("0")) {
						// 绑定车辆
						String url = Constant.BaseUrl + "vehicle/" + car_id
								+ "/device?auth_code=" + app.auth_code;
						List<NameValuePair> params = new ArrayList<NameValuePair>();
						params.add(new BasicNameValuePair("device_id",
								device_id));
						new NetThread.putDataThread(handler, url, params,
								update_car).start();
					} else {
						SaveDataOver();
						showToast();
					}
				} catch (Exception e) {
					e.printStackTrace();
					SaveDataOver();
					showToast();
				}
				break;
			case update_car:
				// TODO 更新车辆数据
				ll_wait.runFast();
				break;
			case get_data:
				app.carDatas.clear();
				app.carDatas.addAll(JsonData.jsonCarInfo(msg.obj
						.toString()));
				Intent intent = new Intent(Constant.A_RefreshHomeCar);
				sendBroadcast(intent);
				ManageActivity.getActivityInstance().exit();
				break;
			case update_serial:
				try {
					JSONObject json = new JSONObject(msg.obj.toString());
					String sim_card = json.getString("sim");
					String old_serial = json.getString("serial");
					et_serial.setText(old_serial);
					et_sim.setText(sim_card);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			}
		}
	};

	/**
	 * 更新内存里的数据
	 */
	private void updateVariableCarData() {
		for (CarData carData : app.carDatas) {
			if (carData.getObj_id() == Integer.valueOf(car_id)) {
				carData.setDevice_id(device_id);
				carData.setSerial(et_serial.getText().toString().trim());
				break;
			}
		}
	}

	private void showToast() {
		if (isBind) {
			Toast.makeText(DevicesAddActivity.this, "绑定终端失败",
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(DevicesAddActivity.this, "修改终端失败",
					Toast.LENGTH_SHORT).show();
		}
	}

	private void Add() {
		String serial = et_serial.getText().toString().trim();
		String sim = et_sim.getText().toString().trim();
		if (serial.equals("")) {
			et_serial.setError("序列号不能为空");
		} else if (sim.length() != 11) {
			et_sim.setError("sim格式不对");
		} else {
			if (isBind) {
				tv_note.setText("终端绑定中");
			} else {
				tv_note.setText("终端修改中");
			}
			rl_wait.setVisibility(View.VISIBLE);
			ll_wait.startWheel();
			String url = Constant.BaseUrl + "device/serial/" + serial
					+ "?auth_code=" + app.auth_code;
			new NetThread.GetDataThread(handler, url, add_serial).start();
			SaveDataIn();
		}
	}

	private void jsonAddSerial(String result) {
		try {
			if (result.equals("")) {
				et_serial.setError("序列号不存在");
				SaveDataOver();
			} else {
				JSONObject jsonObject = new JSONObject(result);
				String status = jsonObject.getString("status");
				if (status.equals("0") || status.equals("1")) {
					String sim = et_sim.getText().toString().trim();
					device_id = jsonObject.getString("device_id");
					String url = Constant.BaseUrl + "device/" + device_id
							+ "/sim?auth_code=" + app.auth_code;
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("sim", sim));
					new Thread(new NetThread.putDataThread(handler, url,
							params, update_sim)).start();
				} else if (status.equals("2")) {
					et_serial.setError("序列号已经使用");
					SaveDataOver();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void SaveDataIn() {
		et_serial.setEnabled(false);
		et_sim.setEnabled(false);
		iv_serial.setEnabled(false);
		iv_add.setEnabled(false);
	}

	private void SaveDataOver() {
		et_serial.setEnabled(true);
		et_sim.setEnabled(true);
		iv_serial.setEnabled(true);
		iv_add.setEnabled(true);
		ll_wait.refreshView();
		rl_wait.setVisibility(View.GONE);
	}

	private void checkSerial() {
		String serial = et_serial.getText().toString().trim();
		String url = Constant.BaseUrl + "device/serial/" + serial
				+ "?auth_code=" + app.auth_code;
		new NetThread.GetDataThread(handler, url, check_serial).start();
	}

	private void jsonSerial(String result) {
		try {
			if (result.equals("")) {
				et_serial.setError("序列号不存在");
			} else {
				JSONObject jsonObject = new JSONObject(result);
				String status = jsonObject.getString("status");
				if (status.equals("0") || status.equals("1")) {
					String sim = jsonObject.getString("sim");
					et_sim.setText(sim);
				} else if (status.equals("2")) {
					et_serial.setError("序列号已经使用");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	OnFocusChangeListener onFocusChangeListener = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (!hasFocus) {
				switch (v.getId()) {
				case R.id.et_serial:
					checkSerial();
					break;
				case R.id.et_sim:
					String sim = et_sim.getText().toString().trim();
					if (sim.length() != 11) {
						et_sim.setError("sim格式不对");
					}
					break;
				}
			}
		}
	};

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == 2) {
			String result = data.getStringExtra("result");
			et_serial.setText(result);
			checkSerial();
		}
	};

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