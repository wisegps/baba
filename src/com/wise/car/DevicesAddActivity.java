package com.wise.car;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.aliyun.android.oss.model.OSSObject;
import com.aliyun.android.oss.task.GetObjectTask;
import com.umeng.analytics.MobclickAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.ManageActivity;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.JsonData;
import com.wise.baba.entity.CarData;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.widget.WaitLinearLayout;
import com.wise.baba.ui.widget.WaitLinearLayout.OnFinishListener;
import com.wise.baba.util.Blur;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
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
import com.wise.baba.util.DateUtil;

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
	EditText et_serial, et_sim, et_hardware_version, et_software_version,
			et_end_time;
	TextView tv_note;
	Button tv_jump;
	RelativeLayout rl_wait;
	WaitLinearLayout ll_wait;

	// 近景远景图
	ImageView car_icon_near, car_icon_far;
	TextView tv_pic_share, tv_near, tv_far, tv_prompt;
	TextView car_name;

	int car_id;
	/** true绑定终端，false修改终端 **/
	boolean isBind;
	String old_device_id, device_id, car_series_id, car_series;
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
		old_device_id = getIntent().getStringExtra("old_device_id");

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

		et_hardware_version = (EditText) findViewById(R.id.et_hardware_version);
		et_software_version = (EditText) findViewById(R.id.et_software_version);
		et_end_time = (EditText) findViewById(R.id.et_end_time);
		et_hardware_version.setEnabled(false);
		et_software_version.setEnabled(false);
		et_end_time.setEnabled(false);

		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		iv_add = (ImageView) findViewById(R.id.iv_add);
		iv_add.setOnClickListener(onClickListener);
		tv_prompt = (TextView) findViewById(R.id.tv_prompt);
		SpannableString sp = new SpannableString(
				"请扫描终端的二维码或者输入对应的序列号进行绑定(常用OBD安装位置)");
		sp.setSpan(new URLSpan("http://api.bibibaba.cn/help/obd"), 24, 33,
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		tv_prompt.setText(sp);
		tv_prompt.setMovementMethod(LinkMovementMethod.getInstance());

		// 显示续费链接
		if (old_device_id != null && old_device_id.length() > 0) {
			Button btnRecharge = (Button) findViewById(R.id.btn_recharge);
			btnRecharge.setVisibility(View.VISIBLE);
			btnRecharge.setOnClickListener(onClickListener);
		}

		// 近景远景图
		car_icon_near = (ImageView) findViewById(R.id.car_icon_near);
		car_icon_near.setOnClickListener(onClickListener);
		car_icon_far = (ImageView) findViewById(R.id.car_icon_far);
		car_icon_far.setOnClickListener(onClickListener);
		tv_near = (TextView) findViewById(R.id.tv_near);
		tv_far = (TextView) findViewById(R.id.tv_far);

		tv_pic_share = (TextView) findViewById(R.id.tv_pic_share);
		tv_pic_share.setOnClickListener(onClickListener);
		car_name = (TextView) findViewById(R.id.car_name);
		car_name.setOnClickListener(onClickListener);

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
			String url = Constant.BaseUrl + "/device/" + old_device_id
					+ "?auth_code=" + app.auth_code;
			Log.i("DevicesAddActivity", url);
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
			case R.id.tv_pic_share:// TODO 分享图片
				Intent intent = new Intent(DevicesAddActivity.this,
						PictureChoose.class);
				intent.putExtra("car_series_id", car_series_id);
				startActivityForResult(intent, 20);
				break;
			case R.id.car_icon_near:
				pictureChoose(get_near_date);
				break;
			case R.id.car_icon_far:
				pictureChoose(get_far_date);
				break;
			case R.id.car_name:
				Intent in = new Intent(DevicesAddActivity.this,
						ModelsActivity.class);
				in.putExtra("isNeedType", false);
				startActivityForResult(in, 2);
				break;
			case R.id.btn_recharge:
				Intent web = new Intent(DevicesAddActivity.this,
						WebActivity.class);
				String sim = et_sim.getText().toString().trim();
				String urlRecharge = "http://api.bibibaba.cn/device/pay?sim="
						+ sim + "&cust_id=" + app.cust_id;
				web.putExtra("webUrl", urlRecharge);
				startActivityForResult(web, 10);
				break;

			}
		}
	};

	/**
	 * OBD实景图片选择
	 */
	private void pictureChoose(int type) {
		if ((picNearSmall == null || picNearSmall.size() == 0)
				&& (picFarSmall == null || picFarSmall.size() == 0)) {
			return;
		}
		Intent intent = new Intent(DevicesAddActivity.this,
				ImagePageActivity.class);
		if (type == get_near_date && picNearSmall != null
				&& picNearSmall.size() != 0) {
			intent.putStringArrayListExtra("pathList",
					(ArrayList<String>) picNearBig);
		} else if (type == get_far_date && picFarSmall != null
				&& picFarSmall.size() != 0) {
			intent.putStringArrayListExtra("pathList",
					(ArrayList<String>) picFarBig);
		}
		startActivity(intent);
		// View view = (LayoutInflater.from(DevicesAddActivity.this)).inflate(
		// R.layout.pop_pic_choose, null);
		// Gallery mGallery = (Gallery) view.findViewById(R.id.m_gallery);
		// ChoosePicAdapter adapter = null;
		// if (type == get_near_date) {
		// adapter = new ChoosePicAdapter(nearBitmaps);
		// } else if (type == get_far_date) {
		// adapter = new ChoosePicAdapter(farBitmaps);
		// }
		// mGallery.setAdapter(adapter);
		// mGallery.setOnItemClickListener(new OnItemClickListener() {
		// @Override
		// public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
		// long arg3) {
		// Intent intent = new Intent(DevicesAddActivity.this,
		// ImageDetailsActivity.class);
		// String image_path = "";
		// if (type == get_near_date) {
		// image_path = picNear.get(arg2);
		// } else if (type == get_far_date) {
		// image_path = picFar.get(arg2);
		// }
		// intent.putExtra("image_path", image_path);
		// startActivity(intent);
		//
		// if (type == get_near_date) {
		// car_icon_near.setImageBitmap(Blur
		// .getSquareBitmap(nearBitmaps.get(arg2)));
		// } else if (type == get_far_date) {
		// car_icon_far.setImageBitmap(Blur.getSquareBitmap(farBitmaps
		// .get(arg2)));
		// }
		// }
		// });
		// PopupWindow popupWindow = new PopupWindow(view,
		// LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		// popupWindow.setBackgroundDrawable(new BitmapDrawable());
		// popupWindow.setFocusable(true);
		// popupWindow.setOutsideTouchable(true);
		// popupWindow.showAsDropDown(et_sim);
	}

	// class ChoosePicAdapter extends BaseAdapter {
	// LayoutInflater mInflater = LayoutInflater.from(DevicesAddActivity.this);
	// private List<Bitmap> list = null;
	//
	// public ChoosePicAdapter(List<Bitmap> listBitmaps) {
	// list = listBitmaps;
	// }
	//
	// @Override
	// public int getCount() {
	// return list == null ? 0 : list.size();
	// }
	//
	// @Override
	// public Object getItem(int position) {
	// return list.get(position);
	// }
	//
	// @Override
	// public long getItemId(int position) {
	// return position;
	// }
	//
	// @Override
	// public View getView(int position, View convertView, ViewGroup parent) {
	// Holder mHolder;
	// if (convertView == null) {
	// mHolder = new Holder();
	// convertView = mInflater.inflate(R.layout.item_pic_choose, null);
	// mHolder.image_pic_choose = (ImageView) convertView
	// .findViewById(R.id.image_pic_choose);
	// convertView.setTag(mHolder);
	// } else {
	// mHolder = (Holder) convertView.getTag();
	// }
	// Bitmap showBitmap = Blur.getSquareBitmap(list.get(position));
	// mHolder.image_pic_choose.setImageBitmap(showBitmap);
	// return convertView;
	// }
	//
	// class Holder {
	// ImageView image_pic_choose;
	// }
	// }

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
				jsonSerial(msg.obj.toString());
				break;
			case add_serial:
				jsonAddSerial(msg.obj.toString());
				break;
			case update_sim:
				try {

					Log.i("DevicesAddActivity", msg.obj.toString());
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
					Log.i("DevicesAddActivity",
							"update_user" + msg.obj.toString());
					String status_code = new JSONObject(msg.obj.toString())
							.getString("status_code");
					if (status_code.equals("0")) {
						// 绑定车辆
						final String url = Constant.BaseUrl + "vehicle/"
								+ car_id + "/device?auth_code=" + app.auth_code;
						final List<NameValuePair> params = new ArrayList<NameValuePair>();
						params.add(new BasicNameValuePair("device_id",
								device_id));
						Log.i("DevicesAddActivity", isBind + "");
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
													new NetThread.putDataThread(
															handler, url,
															params, update_car)
															.start();
												}
											})
									.setNegativeButton(
											"否",
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													new NetThread.putDataThread(
															handler, url,
															params, update_car)
															.start();
												}
											}).show();
						} else {

							new NetThread.putDataThread(handler, url, params,
									update_car).start();
						}

					} else {

						Log.i("DevicesAddActivity", "update_user"
								+ "SaveDataOver");
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
					String hardwareVersion = json.getString("hardware_version");
					String softwareVersion = json.getString("software_version");
					String endTime = json.optString("end_time", "");
					endTime = DateUtil.getEndTime(endTime);

					et_serial.setText(old_serial);
					et_sim.setText(sim_card);
					et_hardware_version.setText(hardwareVersion);
					et_software_version.setText(softwareVersion);
					et_end_time.setText(endTime);

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
				Toast.makeText(DevicesAddActivity.this, "近景图分享成功",
						Toast.LENGTH_SHORT).show();
				break;
			case REQUEST_FAR:
				Toast.makeText(DevicesAddActivity.this, "远景图分享成功",
						Toast.LENGTH_SHORT).show();
				break;
			case getBitmap:
				int type = msg.arg1;
				if (type == REQUEST_NEAR) {
					tv_near.setVisibility(View.GONE);
					car_icon_near.setVisibility(View.VISIBLE);
					car_icon_near.setImageBitmap(Blur
							.getSquareBitmap(nearBitmaps.get(0)));
				} else if (type == REQUEST_FAR) {
					tv_far.setVisibility(View.GONE);
					car_icon_far.setVisibility(View.VISIBLE);
					car_icon_far.setImageBitmap(Blur.getSquareBitmap(farBitmaps
							.get(0)));
				}
				break;
			}
		}
	};

	private String getImagePath(String imageUrl) {
		int lastSlashIndex = imageUrl.lastIndexOf("/");
		String imageName = imageUrl.substring(lastSlashIndex + 1);
		String imageDir = Constant.VehiclePath;
		File file = new File(imageDir);
		if (!file.exists()) {
			file.mkdirs();
		}
		String imagePath = imageDir + imageName;
		return imagePath;
	}

	private static final int getBitmap = 18;

	private void getPic(List<String> listPath, final int type,
			final boolean flag) {
		for (int i = 0; i < listPath.size(); i++) {
			String path = listPath.get(i);
			if (type == REQUEST_NEAR) {
				if (flag) {
					picNearSmall.add(getImagePath(path));
				} else {
					picNearBig.add(getImagePath(path));
				}
			} else if (type == REQUEST_FAR) {
				if (flag) {
					picFarSmall.add(getImagePath(path));
				} else {
					picFarBig.add(getImagePath(path));
				}
			}
			int lastSlashIndex = path.lastIndexOf("/");
			final String imageName = path.substring(lastSlashIndex + 1);
			final File imageFile = new File(getImagePath(path));
			if (imageFile.exists()) {
				if (flag) {
					Bitmap bitmap = BitmapFactory
							.decodeFile(getImagePath(path));
					if (type == REQUEST_NEAR) {
						nearBitmaps.add(bitmap);
						tv_near.setVisibility(View.GONE);
						car_icon_near.setVisibility(View.VISIBLE);
						car_icon_near.setImageBitmap(Blur
								.getSquareBitmap(nearBitmaps.get(0)));
					} else if (type == REQUEST_FAR) {
						farBitmaps.add(bitmap);
						tv_far.setVisibility(View.GONE);
						car_icon_far.setVisibility(View.VISIBLE);
						car_icon_far.setImageBitmap(Blur
								.getSquareBitmap(farBitmaps.get(0)));
					}
				}
			} else {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							// 获取阿里云上的图片
							GetObjectTask task = new GetObjectTask(
									Constant.oss_path, imageName,
									Constant.oss_accessId,
									Constant.oss_accessKey);
							OSSObject obj = task.getResult();
							if (flag) {
								Bitmap bitmap = BitmapFactory.decodeByteArray(
										obj.getData(), 0,
										(obj.getData()).length);
								if (type == REQUEST_NEAR) {
									nearBitmaps.add(bitmap);
								} else if (type == REQUEST_FAR) {
									farBitmaps.add(bitmap);
								}
								Message msg = new Message();
								msg.arg1 = type;
								msg.what = getBitmap;
								handler.sendMessage(msg);
							}
							FileOutputStream fileOutputStream = new FileOutputStream(
									imageFile);
							fileOutputStream.write(obj.getData());
							fileOutputStream.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
		}
	}

	// 本地图片显示集合地址
	List<String> picNearSmall = new ArrayList<String>();
	List<String> picNearBig = new ArrayList<String>();
	List<String> picFarSmall = new ArrayList<String>();
	List<String> picFarBig = new ArrayList<String>();

	List<Bitmap> nearBitmaps = new ArrayList<Bitmap>();
	List<Bitmap> farBitmaps = new ArrayList<Bitmap>();

	// 阿里云图片地址
	List<String> nearSmallPath = new ArrayList<String>();
	List<String> nearBigPath = new ArrayList<String>();
	List<String> farSmallPath = new ArrayList<String>();
	List<String> farBigPath = new ArrayList<String>();

	private void jsonPicDate(String result, int type) {
		try {
			if (result.equals("") || result == null || result.equals("[]")) {
				return;
			} else {
				JSONObject jsonObject = (new JSONArray(result))
						.getJSONObject(0);
				if (type == get_near_date) {
					if (jsonObject.opt("obd_near_pic") != null) {
						// 近景图
						JSONArray jsonArrayNear = jsonObject
								.getJSONArray("obd_near_pic");
						for (int i = 0; i < jsonArrayNear.length(); i++) {
							JSONObject object = jsonArrayNear.getJSONObject(i);
							boolean is_auth = object.getBoolean("is_auth");
							if (is_auth) {
								String urlString = object
										.getString("small_pic_url");
								String urlStringBig = object
										.getString("big_pic_url");
								if (urlString != null && !urlString.equals("")) {
									nearSmallPath.add(urlString);
								}
								if (urlStringBig != null
										&& !urlStringBig.equals("")) {
									nearBigPath.add(urlStringBig);
								}
							}
						}
						if (nearSmallPath != null && nearSmallPath.size() != 0) {
							getPic(nearSmallPath, REQUEST_NEAR, true);
						}
						if (nearBigPath != null && nearBigPath.size() != 0) {
							getPic(nearBigPath, REQUEST_NEAR, false);
						}
					}
				} else if (type == get_far_date) {
					if (jsonObject.opt("obd_far_pic") != null) {
						// 远景图
						JSONArray jsonArrayFar = jsonObject
								.getJSONArray("obd_far_pic");
						for (int i = 0; i < jsonArrayFar.length(); i++) {
							JSONObject object = jsonArrayFar.getJSONObject(i);
							boolean is_auth = object.getBoolean("is_auth");
							if (is_auth) {
								String urlString = object
										.getString("small_pic_url");
								String urlStringBig = object
										.getString("big_pic_url");
								if (urlString != null && !urlString.equals("")) {
									farSmallPath.add(urlString);
								}
								if (urlStringBig != null
										&& !urlStringBig.equals("")) {
									farBigPath.add(urlStringBig);
								}
							}
						}
						if (farSmallPath != null && farSmallPath.size() != 0) {
							getPic(farSmallPath, REQUEST_FAR, true);
						}
						if (farBigPath != null && farBigPath.size() != 0) {
							getPic(farBigPath, REQUEST_FAR, false);
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

			Log.i("DevicesAddActivity", url);
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
				if (custID == Integer.valueOf(app.cust_id) || custID == 0) {
					String sim = et_sim.getText().toString().trim();
					device_id = jsonObject.getString("device_id");
					String url = Constant.BaseUrl + "device/" + device_id
							+ "/sim?auth_code=" + app.auth_code;
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("sim", sim));
					new Thread(new NetThread.putDataThread(handler, url,
							params, update_sim)).start();
				} else {
					Toast.makeText(DevicesAddActivity.this,
							"该终端已被其他用户绑定，无法再次绑定", Toast.LENGTH_LONG).show();
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == 1) {
			car_series = data.getStringExtra("series");
			car_series_id = data.getStringExtra("seriesId");
			car_name.setText(car_series);
			tv_near.setVisibility(View.VISIBLE);
			tv_far.setVisibility(View.VISIBLE);
			car_icon_near.setVisibility(View.GONE);
			car_icon_far.setVisibility(View.GONE);
			// 阿里云地址
			nearBigPath.clear();
			nearSmallPath.clear();
			farSmallPath.clear();
			farBigPath.clear();

			nearBitmaps.clear();
			farBitmaps.clear();

			// 本地地址
			picNearSmall.clear();
			picNearBig.clear();
			picFarSmall.clear();
			picFarBig.clear();

			getDeviceDate();
		}
		if (resultCode == 2) {
			String result = data.getStringExtra("result");
			et_serial.setText(result);
			checkSerial();
		}

		if (requestCode == 10 && resultCode == 10) {
			// 接收并现实以前的终端值
			String url = Constant.BaseUrl + "/device/" + old_device_id
					+ "?auth_code=" + app.auth_code;
			new NetThread.GetDataThread(handler, url, update_serial).start();
		}

		if (resultCode == PictureChoose.Pictrue) {
			int type = data.getIntExtra("type", 0);
			if (type == PictureChoose.PIC_NEAR) {
				String near_small = data.getStringExtra("near_small");
				if (near_small != null && !near_small.equals("")) {
					Bitmap picBitmap = Blur.getSquareBitmap(BitmapFactory
							.decodeFile(Constant.VehiclePath + near_small));
					// 显示图像
					tv_near.setVisibility(View.GONE);
					car_icon_near.setVisibility(View.VISIBLE);
					car_icon_near.setImageBitmap(picBitmap);
				}
			} else if (type == PictureChoose.PIC_FAR) {
				String far_small = data.getStringExtra("far_small");
				if (far_small != null && !far_small.equals("")) {
					Bitmap picBitmap = Blur.getSquareBitmap(BitmapFactory
							.decodeFile(Constant.VehiclePath + far_small));
					tv_far.setVisibility(View.GONE);
					car_icon_far.setVisibility(View.VISIBLE);
					car_icon_far.setImageBitmap(picBitmap);
				}
			} else if (type == PictureChoose.PIC_ALL) {
				String near_small = data.getStringExtra("near_small");
				String far_small = data.getStringExtra("far_small");
				if (near_small != null && !near_small.equals("")
						&& far_small != null && !far_small.equals("")) {
					Bitmap picBitmap1 = Blur.getSquareBitmap(BitmapFactory
							.decodeFile(Constant.VehiclePath + near_small));
					Bitmap picBitmap2 = Blur.getSquareBitmap(BitmapFactory
							.decodeFile(Constant.VehiclePath + far_small));
					tv_near.setVisibility(View.GONE);
					tv_far.setVisibility(View.GONE);
					car_icon_near.setVisibility(View.VISIBLE);
					car_icon_far.setVisibility(View.VISIBLE);
					car_icon_near.setImageBitmap(picBitmap1);
					car_icon_far.setImageBitmap(picBitmap2);
				}
			}
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
