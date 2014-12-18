package com.wise.car;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import pubclas.Blur;
import pubclas.Constant;
import pubclas.JsonData;
import pubclas.NetThread;
import pubclas.Uri2Path;

import com.aliyun.android.oss.task.PutObjectTask;
import com.umeng.analytics.MobclickAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.ManageActivity;
import com.wise.baba.R;
import customView.PopView;
import customView.WaitLinearLayout;
import customView.PopView.OnItemClickListener;
import customView.WaitLinearLayout.OnFinishListener;
import data.CarData;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;

import android.util.DisplayMetrics;
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

	private static final int get_near_date = 8;
	private static final int get_far_date = 9;

	private static final int REQUEST_NEAR = 10;
	private static final int REQUEST_FAR = 11;
	ImageView iv_serial;
	ImageView iv_add;
	EditText et_serial, et_sim;
	TextView tv_note;
	Button tv_jump;
	RelativeLayout rl_wait;
	WaitLinearLayout ll_wait;

	// 近景远景图
	ImageView car_icon_near, car_icon_far;
	TextView tv_icon_near_share, tv_icon_far_share;
	TextView car_name, car_own_name;

	int car_id;
	/** true绑定终端，false修改终端 **/
	boolean isBind;
	String device_id, car_series_id, car_series;
	/** 快速注册 **/
	boolean fastTrack = false;
	AppApplication app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		ManageActivity.getActivityInstance().addActivity(this);
		setContentView(R.layout.activity_devices_add);
		app = (AppApplication) getApplication();
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

		// 近景远景图
		car_icon_near = (ImageView) findViewById(R.id.car_icon_near);
		car_icon_near.setOnClickListener(onClickListener);
		car_icon_far = (ImageView) findViewById(R.id.car_icon_far);
		car_icon_far.setOnClickListener(onClickListener);
		tv_icon_near_share = (TextView) findViewById(R.id.tv_icon_near_share);
		tv_icon_near_share.setOnClickListener(onClickListener);
		tv_icon_far_share = (TextView) findViewById(R.id.tv_icon_far_share);
		tv_icon_far_share.setOnClickListener(onClickListener);
		car_name = (TextView) findViewById(R.id.car_name);
		car_own_name = (TextView) findViewById(R.id.car_own_name);

		Intent intent = getIntent();
		car_id = intent.getIntExtra("car_id", 0);
		isBind = intent.getBooleanExtra("isBind", true);
		fastTrack = intent.getBooleanExtra("fastTrack", false);
		car_series_id = intent.getStringExtra("car_series_id");
		car_series = intent.getStringExtra("car_series");

		getDeviceDate();// 获取odb近景远景照片数据

		if (car_series != null || !car_series.equals("")) {
			car_name.setText(car_series);
		}
		if (!isBind) {
			// 接收并现实以前的终端值
			String old_device_id = intent.getStringExtra("old_device_id");
			String url = Constant.BaseUrl + "/device/" + old_device_id
					+ "?auth_code=" + app.auth_code;
			new NetThread.GetDataThread(handler, url, update_serial).start();
		} else {
			startActivityForResult(new Intent(DevicesAddActivity.this,
					BarcodeActivity.class), 0);
		}
		if (fastTrack) {
			tv_jump.setVisibility(View.VISIBLE);
		} else {
			tv_jump.setVisibility(View.GONE);
		}
	}

	private void getDeviceDate() {
		// 近景
		String url_1 = Constant.BaseUrl + "base/car_series/" + car_series_id
				+ "/near_pic" + "?auth_code=" + app.auth_code;
		new NetThread.GetDataThread(handler, url_1, get_near_date).start();
		// 远景
		String url = Constant.BaseUrl + "base/car_series/" + car_series_id
				+ "/far_pic" + "?auth_code=" + app.auth_code;
		new NetThread.GetDataThread(handler, url, get_far_date).start();
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
			case R.id.tv_icon_near_share:// TODO 分享近景图
				if (oss_url_big.equals("") && oss_url_small.equals("")) {
					Toast.makeText(DevicesAddActivity.this, "请先选择图片",
							Toast.LENGTH_SHORT).show();
					return;
				}
				String naerUrl = Constant.BaseUrl + "base/car_series/"
						+ car_series_id + "/near_pic?auth_code="
						+ app.auth_code;
				List<NameValuePair> params1 = new ArrayList<NameValuePair>();
				params1.add(new BasicNameValuePair("big_pic_url", oss_url_big));
				params1.add(new BasicNameValuePair("small_pic_url",
						oss_url_small));
				params1.add(new BasicNameValuePair("author", app.cust_name));
				new NetThread.postDataThread(handler, naerUrl, params1,
						REQUEST_NEAR).start();
				break;
			case R.id.tv_icon_far_share:// 分享远景图
				if (oss_url_big.equals("") && oss_url_small.equals("")) {
					Toast.makeText(DevicesAddActivity.this, "请先选择图片",
							Toast.LENGTH_SHORT).show();
					return;
				}
				String farUrl = Constant.BaseUrl + "base/car_series/"
						+ car_series_id + "/far_pic?auth_code=" + app.auth_code;
				List<NameValuePair> params2 = new ArrayList<NameValuePair>();
				params2.add(new BasicNameValuePair("big_pic_url", oss_url_big));
				params2.add(new BasicNameValuePair("small_pic_url",
						oss_url_small));
				params2.add(new BasicNameValuePair("author", app.cust_name));
				new NetThread.postDataThread(handler, farUrl, params2,
						REQUEST_FAR).start();
				break;
			case R.id.car_icon_near:
				picPop(REQUEST_NEAR, R.id.tv_icon_near_share);
				break;
			case R.id.car_icon_far:
				picPop(REQUEST_FAR, R.id.tv_icon_far_share);
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
						final List<NameValuePair> params = new ArrayList<NameValuePair>();
						params.add(new BasicNameValuePair("device_id",
								device_id));
						if (!isBind) {
							AlertDialog.Builder builder = new AlertDialog.Builder(
									DevicesAddActivity.this);
							builder.setTitle("提示")
									.setMessage("是否在修改终端的同时将原终端的所有数据转至新终端名下？")
									.setPositiveButton(
											"是",
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													params.add(new BasicNameValuePair(
															"deal_data", "1"));
												}
											}).setNegativeButton("否", null)
									.show();
						}
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
				app.carDatas.addAll(JsonData.jsonCarInfo(msg.obj.toString()));
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
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case get_far_date:
				jsonPicDate(msg.obj.toString(), get_far_date);
				break;
			case get_near_date:
				jsonPicDate(msg.obj.toString(), get_near_date);
				break;
			case REQUEST_NEAR:
				Toast.makeText(DevicesAddActivity.this, "分享成功",
						Toast.LENGTH_SHORT).show();
				break;
			case REQUEST_FAR:
				Toast.makeText(DevicesAddActivity.this, "分享成功",
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	private Bitmap getPic(String path) {
		try {
			URL url = new URL(path);
			HttpURLConnection conn;
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setRequestMethod("GET");
			if (conn.getResponseCode() == 200) {
				InputStream inputStream = conn.getInputStream();
				Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
				return bitmap;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void jsonPicDate(String result, int type) {
		try {

			if (result.equals("") || result == null || result.equals("[]")) {

				return;
			} else {
				JSONObject jsonObject = new JSONArray(result).getJSONObject(0);
				String name = jsonObject.getString("name");
				if (type == get_near_date) {
					if (jsonObject.opt("obd_near_pic") != null) {
						// 近景图
						JSONArray jsonArrayNear = jsonObject
								.getJSONArray("obd_near_pic");
						for (int i = 0; i < jsonArrayNear.length(); i++) {
							JSONObject object = jsonArrayNear.getJSONObject(i);
							if (name.equals(car_name.getText().toString())) {
								String urlString = object
										.getString("small_pic_url");
								String author = object.getString("author");
								car_own_name.setText(author);
								if (urlString != null && !urlString.equals("")
										&& getPic(urlString) != null) {
									tv_icon_near_share.setVisibility(View.GONE);
									car_icon_near
											.setImageBitmap(getPic(urlString));
									car_icon_near
											.setOnClickListener(new OnClickListener() {
												@Override
												public void onClick(View v) {

												}
											});
								}
								break;
							}
						}
					}
				} else if (type == get_far_date) {
					if (jsonObject.opt("obd_far_pic") != null) {
						// 远景图
						JSONArray jsonArrayFar = jsonObject
								.getJSONArray("obd_far_pic");
						for (int i = 0; i < jsonArrayFar.length(); i++) {
							JSONObject object = jsonArrayFar.getJSONObject(i);
							if (name.equals(car_name.getText().toString())) {
								String urlString = object
										.getString("small_pic_url");
								String author = object.getString("author");
								car_own_name.setText(author);
								if (urlString != null && !urlString.equals("")
										&& getPic(urlString) != null) {
									tv_icon_far_share.setVisibility(View.GONE);
									car_icon_far
											.setImageBitmap(getPic(urlString));
									car_icon_far
											.setOnClickListener(new OnClickListener() {
												@Override
												public void onClick(View v) {

												}
											});
								}
								break;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

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
				int custID = jsonObject.getInt("cust_id");
				String status = jsonObject.getString("status");
				if (custID > 0) {
					Toast.makeText(DevicesAddActivity.this,
							"该终端已被其他用户绑定，无法再次绑定", Toast.LENGTH_LONG).show();
					SaveDataOver();
				} else if (custID == 0 || custID < 0) {
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
					et_sim.setText(jsonObject.getString("sim"));
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

	boolean flag = false;

	private void picPop(final int type, int id) {
		List<String> items = new ArrayList<String>();
		items.add("拍照");
		items.add("从手机相册中选取");
		final PopView popView = new PopView(this);
		popView.initView(findViewById(id));
		popView.setData(items);
		popView.SetOnItemClickListener(new OnItemClickListener() {
			@Override
			public void OnItemClick(int index) {
				switch (index) {
				case 0:
					flag = true;
					File file = new File(Constant.VehiclePath);
					if (!file.exists()) {
						file.mkdirs();// 创建文件夹
					}
					Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					intent1.putExtra(
							MediaStore.EXTRA_OUTPUT,
							Uri.fromFile(new File(Constant.VehiclePath
									+ Constant.TemporaryImage)));
					startActivityForResult(intent1, type);
					popView.dismiss();
					break;
				case 1:
					Intent intent = new Intent();
					/* 开启Pictures画面Type设定为image */
					intent.setType("image/*");
					/* 使用Intent.ACTION_GET_CONTENT这个Action */
					intent.setAction(Intent.ACTION_GET_CONTENT);
					/* 取得相片后返回本画面 */
					startActivityForResult(intent, type);
					popView.dismiss();
					break;
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		System.out.println("requestCode = " + requestCode);
		System.out.println("resultCode = " + resultCode);
		System.out.println("flag = " + flag);
		if (resultCode == 2) {
			String result = data.getStringExtra("result");
			et_serial.setText(result);
			checkSerial();
		}
		if (requestCode == REQUEST_NEAR && resultCode == Activity.RESULT_OK) {			
				if (flag) {
					System.out.println("near 拍照");
					saveImageSD(
							(Constant.VehiclePath + Constant.TemporaryImage),
							car_icon_near);
					flag = false;
				} else {
					if (data != null) {

						Uri uri = data.getData();
						saveImageSD(Uri2Path.getPath(DevicesAddActivity.this, uri),
								car_icon_near);
					}
				}
		} else if (requestCode == REQUEST_FAR
				&& resultCode == Activity.RESULT_OK) {
			
				if (flag) {
					saveImageSD(
							(Constant.VehiclePath + Constant.TemporaryImage),
							car_icon_far);
					flag = false;
				} else {
					if (data != null) {
						Uri uri = data.getData();
						saveImageSD(Uri2Path.getPath(DevicesAddActivity.this, uri),
								car_icon_far);
					}
				}
		}
	};

	String oss_url_small = "";
	String oss_url_big = "";

	private void saveImageSD(String path, ImageView showView) {
		if (path == null || path.equals("")) {
			Toast.makeText(DevicesAddActivity.this, "请选择图片或者拍照上传",
					Toast.LENGTH_SHORT).show();
			return;
		}
		// 设置图像的名称和地址
		final String small_pic = app.cust_id + System.currentTimeMillis()
				+ "small.png";
		final String big_pic = app.cust_id + System.currentTimeMillis()
				+ "big.png";
		oss_url_small = Constant.oss_url + small_pic;
		oss_url_big = Constant.oss_url + big_pic;
		// 判断文件夹是否为空
		File filePath = new File(Constant.VehiclePath);
		if (!filePath.exists()) {
			filePath.mkdirs();
		}
		// 获取手机分辨率,选出最小的
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int widthPixels = metrics.widthPixels;
		int heightPixels = metrics.heightPixels;
		int newWidth = widthPixels > heightPixels ? heightPixels : widthPixels;

		Bitmap bitmap = Blur.decodeSampledBitmapFromPath(path, newWidth,
				newWidth);
		// 存大图像
		bitmap = Blur.scaleImage(bitmap, newWidth);
		FileOutputStream bigOutputStream = null;
		final String bigFile = Constant.VehiclePath + big_pic;
		try {
			bigOutputStream = new FileOutputStream(bigFile);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bigOutputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				bigOutputStream.flush();
				bigOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// 存小图像
		bitmap = Blur.scaleImage(bitmap, newWidth / 3);
		FileOutputStream smallOutputStream = null;
		final String smallFile = Constant.VehiclePath + small_pic;
		try {
			smallOutputStream = new FileOutputStream(smallFile);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, smallOutputStream);// 把数据写入文件
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				smallOutputStream.flush();
				smallOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// 图片显示
		System.out.println("bitmap = " + bitmap);
		showView.setImageBitmap(bitmap);
		new Thread(new Runnable() {
			@Override
			public void run() {
				// 上传大图图片到阿里云
				PutObjectTask smallTask = new PutObjectTask(Constant.oss_path,
						small_pic, "image/jpg", smallFile,
						Constant.oss_accessId, Constant.oss_accessKey);
				smallTask.getResult();

				PutObjectTask bigTask = new PutObjectTask(Constant.oss_path,
						big_pic, "image/jpg", bigFile, Constant.oss_accessId,
						Constant.oss_accessKey);
				bigTask.getResult();
			}
		}).start();
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
