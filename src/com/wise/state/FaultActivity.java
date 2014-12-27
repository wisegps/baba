package com.wise.state;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pubclas.Constant;
import pubclas.FaceConversionUtil;
import pubclas.GetLocation;
import pubclas.GetSystem;
import pubclas.Judge;
import pubclas.NetThread;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.baidu.lbsapi.auth.LBSAuthManagerListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.BNaviEngineManager.NaviEngineInitListener;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.BrowserActivity;
import com.wise.baba.MoreActivity;
import com.wise.baba.R;
import com.wise.baba.SelectCityActivity;
import com.wise.car.CarActivity;
import com.wise.car.CarAddActivity;
import com.wise.car.CarLocationActivity;
import com.wise.car.CarUpdateActivity;
import com.wise.car.DevicesAddActivity;
import com.wise.notice.NoticeActivity;
import com.wise.notice.NoticeFragment;
import com.wise.notice.NoticeFragment.BtnListener;
import com.wise.notice.SmsActivity;
import com.wise.setting.LoginActivity;
import com.wise.setting.OilUpdateActivity;
import com.wise.show.ShowActivity;
import customView.AlwaysMarqueeTextView;
import customView.HScrollLayout;
import customView.NoticeScrollTextView;
import customView.OnViewChangeListener;
import customView.ParentSlide;
import data.CarData;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
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
	/** 本地资讯 **/
	private static final int gethot_news = 4;
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
	/** 获取健康体检信息 **/
	private static final int get_health = 11;
	/** 获取驾驶指数 **/
	private static final int get_device = 12;
	/** 获取广告 **/
	private static final int get_ad = 13;

	ImageView iv_weather, iv_noti;
	TextView tv_city, tv_weather_time, tv_weather, tv_advice, tv_hot_content,
			tv_host_title, tv_content;
	RelativeLayout rl_ad;
	int index = 0;
	private FragmentManager fragmentManager;
	ParentSlide smv_content;
	NoticeScrollTextView nstv_message;
	HScrollLayout hs_car, hs_photo;
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
	AppApplication app;
	LinearLayout ll_image;
	int image_position = 0;
	String noticeUrl = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_fault);
		mQueue = Volley.newRequestQueue(this);
		app = (AppApplication) getApplication();
		GetSystem.myLog(TAG, "onCreate");
		mGeoCoder = GeoCoder.newInstance();
		mGeoCoder.setOnGetGeoCodeResultListener(listener);
		boolean isSpecify = getIntent().getBooleanExtra("isSpecify", false);
		if (isSpecify) {
			Intent intent = new Intent(FaultActivity.this, MoreActivity.class);
			intent.putExtra("isSpecify", isSpecify);
			intent.putExtras(getIntent().getExtras());
			startActivityForResult(intent, 5);
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
		tv_hot_content = (TextView) findViewById(R.id.tv_hot_content);
		tv_hot_content.setOnClickListener(onClickListener);
		tv_host_title = (TextView) findViewById(R.id.tv_host_title);
		iv_weather = (ImageView) findViewById(R.id.iv_weather);
		iv_noti = (ImageView) findViewById(R.id.iv_noti);
		ImageView iv_menu = (ImageView) findViewById(R.id.iv_menu);
		iv_menu.setOnClickListener(onClickListener);
		findViewById(R.id.iv_location_hot).setOnClickListener(onClickListener);
		findViewById(R.id.iv_hot_set).setOnClickListener(onClickListener);

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

		if (Judge.isLogin(app)) {// 已登录
			GetSystem.myLog(TAG, "已登录,app.carDatas = " + app.carDatas.size());
			initDataView();
			noticeUrl = Constant.BaseUrl + "customer/" + app.cust_id
					+ "/tips?auth_code=" + app.auth_code;
			getCounter();

			if (app.carDatas.size() == 0) {// 如果没有车则显示
				rl_ad = (RelativeLayout) findViewById(R.id.rl_ad);
				rl_ad.setVisibility(View.VISIBLE);
				tv_content = (TextView) findViewById(R.id.tv_content);
				ll_image = (LinearLayout) findViewById(R.id.ll_image);
				hs_photo = (HScrollLayout) findViewById(R.id.hs_photo);
				hs_photo.setLayoutParams(new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.MATCH_PARENT, getAdHeight()));
				getAD();
				hs_photo.setOnViewChangeListener(new OnViewChangeListener() {
					@Override
					public void OnViewChange(int view) {
						image_position = view;
						tv_content.setText(adDatas.get(view).getContent());
						changeImage(view);
					}

					@Override
					public void OnLastView() {
					}

					@Override
					public void OnFinish(int index) {
					}
				});
			}
		} else {// 未登录
			GetSystem.myLog(TAG, "未登录,app.carDatas = " + app.carDatas.size());
			// 给个临时id
			app.cust_id = "0";
			app.auth_code = "127a154df2d7850c4232542b4faa2c3d";
			setLoginView();
			noticeUrl = Constant.BaseUrl + "customer/0/tips";
			Intent intent = new Intent(FaultActivity.this, LoginActivity.class);
			startActivity(intent);
		}
		myBroadCastReceiver = new MyBroadCastReceiver();
		intentFilter = new IntentFilter();
		intentFilter.addAction(Constant.A_RefreshHomeCar);
		intentFilter.addAction(Constant.A_LoginOut);
		registerReceiver(myBroadCastReceiver, intentFilter);
		new GetLocation(FaultActivity.this);
		new CycleNstvThread().start();

		UmengUpdateAgent.update(this);
		BaiduNaviManager.getInstance().initEngine(this, getSdcardDir(),
				mNaviEngineInitListener, new LBSAuthManagerListener() {
					@Override
					public void onAuthResult(int status, String msg) {
					}
				});
		new Thread(new Runnable() {			
			@Override
			public void run() {
				FaceConversionUtil.getInstace().getFileText(getApplication());
			}
		}).start();
		// TODO 30秒定位，显示当前位子
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (isGetGps) {
					getMessage(noticeUrl);
					gethot_news();
					SystemClock.sleep(30000);
					if (app.carDatas == null || app.carDatas.size() == 0) {

					} else {
						// 防止删除车辆后数组越界
						if (index < app.carDatas.size()) {
							CarData carData = app.carDatas.get(index);
							String device_id = carData.getDevice_id();
							if (device_id == null || device_id.equals("")) {

							} else {
								// 获取gps信息
								String gpsUrl = Constant.BaseUrl + "device/"
										+ device_id + "?auth_code="
										+ app.auth_code
										+ "&update_time=2014-01-01%2019:06:43";
								new NetThread.GetDataThread(handler, gpsUrl,
										get_gps, index).start();
							}
						} else {
							Log.d(TAG, "刷新位置数组越界");
						}
					}
				}
			}
		}).start();
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (isGetAllData) {
					SystemClock.sleep(5 * 60000);
					getTotalData();
				}
			}
		}).start();
	}

	private NaviEngineInitListener mNaviEngineInitListener = new NaviEngineInitListener() {
		public void engineInitSuccess() {
		}

		public void engineInitStart() {
		}

		public void engineInitFail() {
		}
	};

	private void getAD() {
		String url = Constant.BaseUrl + "base/AD";
		new NetThread.GetDataThread(handler, url, get_ad).start();
	}

	private void setImageView(String result) {
		try {
			JSONArray jsonArray = new JSONArray(result);
			for (int i = 0; i < jsonArray.length(); i++) {
				View view_image = LayoutInflater.from(this).inflate(
						R.layout.item_nocar_image, null);
				hs_photo.addView(view_image);
				ImageView iv_pic = (ImageView) view_image
						.findViewById(R.id.iv_pic);
				iv_pic.setOnClickListener(onClickListener);
				ADView aView = new ADView();
				aView.setImageView(iv_pic);
				adViews.add(aView);
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				AData aData = new AData();
				aData.setImage(jsonObject.getString("image"));
				aData.setContent(jsonObject.getString("content"));
				aData.setUrl(jsonObject.getString("url"));
				adDatas.add(aData);

				ImageView imageView = new ImageView(this);
				imageView.setImageResource(R.drawable.round_press);
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
						15, 15);
				lp.setMargins(5, 0, 5, 0);
				imageView.setLayoutParams(lp);
				ll_image.addView(imageView);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void changeImage(int index) {
		for (int i = 0; i < ll_image.getChildCount(); i++) {
			ImageView imageView = (ImageView) ll_image.getChildAt(i);
			if (index == i) {
				imageView.setImageResource(R.drawable.round);
			} else {
				imageView.setImageResource(R.drawable.round_press);
			}
		}
	}

	RequestQueue mQueue;

	private void getImage() {
		for (final AData aData : adDatas) {
			mQueue.add(new ImageRequest(aData.getImage(),
					new Response.Listener<Bitmap>() {
						@Override
						public void onResponse(Bitmap response) {
							for (int i = 0; i < adDatas.size(); i++) {
								if (adDatas.get(i).getImage()
										.equals(aData.getImage())) {
									setImageWidthHeight(adViews.get(i)
											.getImageView(), response);
									adViews.get(i).getImageView()
											.setImageBitmap(response);
								}
							}
						}
					}, 0, 0, Config.RGB_565, new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							error.printStackTrace();
						}
					}));
		}
	}

	/** 计算设置图片的宽高 **/
	private void setImageWidthHeight(ImageView iv_pic, Bitmap bitmap) {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int widthPixels = metrics.widthPixels;

		double ratio = bitmap.getWidth() / (widthPixels * 1.0);
		int scaledHeight = (int) (bitmap.getHeight() / ratio);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				widthPixels, scaledHeight);
		iv_pic.setLayoutParams(params);
	}

	/** 获取控件的高度 **/
	public int getAdHeight() {
		// 690*512宽高
		int imageWidth = 690;
		int imageHeight = 512;
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int widthPixels = metrics.widthPixels;
		double ratio = imageWidth / (widthPixels * 1.0);
		return (int) (imageHeight / ratio);
	}

	List<ADView> adViews = new ArrayList<ADView>();
	List<AData> adDatas = new ArrayList<AData>();

	private class ADView {
		ImageView imageView;

		public ImageView getImageView() {
			return imageView;
		}

		public void setImageView(ImageView imageView) {
			this.imageView = imageView;
		}
	}

	private class AData {
		private String image;
		private String content;
		private String url;

		public String getImage() {
			return image;
		}

		public void setImage(String image) {
			this.image = image;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		@Override
		public String toString() {
			return "AData [image=" + image + ", content=" + content + ", url="
					+ url + "]";
		}
	}

	private String getSdcardDir() {
		if (Environment.getExternalStorageState().equalsIgnoreCase(
				Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStorageDirectory().toString();
		}
		return null;
	}

	/**
	 * 当菜单即将要显示时触发（每次都触发） 菜单键显示
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		startActivityForResult(new Intent(FaultActivity.this, MoreActivity.class), 5);
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
				startActivityForResult(new Intent(FaultActivity.this, MoreActivity.class), 5);
				break;
			case R.id.iv_location_hot:
				if (!Judge.isLogin(app)) {
					startActivity(new Intent(FaultActivity.this,
							LoginActivity.class));
				} else {
					if (app.carDatas == null || app.carDatas.size() == 0) {
						goCarMap(false);
						return;
					}
					CarData carData = app.carDatas.get(index);
					String device_id = carData.getDevice_id();
					if (device_id == null || device_id.equals("")) {
						goCarMap(false);
					} else {
						goCarMap(true);
					}

				}
				break;
			case R.id.iv_hot_set:

				break;
			case R.id.bt_show:
				startActivity(new Intent(FaultActivity.this, ShowActivity.class));
				break;
			case R.id.tasks_view:
				GetSystem.myLog(TAG, "tasks_view : app.carDatas.size() = "
						+ app.carDatas.size());
				if (app.carDatas != null && app.carDatas.size() != 0) {
					String Device_id = app.carDatas.get(index).getDevice_id();
					if (Device_id == null || Device_id.equals("")) {
						Intent intent = new Intent(FaultActivity.this,
								DevicesAddActivity.class);
						intent.putExtra("car_id", app.carDatas.get(index).getObj_id());
						intent.putExtra("car_series_id",app.carDatas.get(index).getCar_series_id());
						intent.putExtra("car_series", app.carDatas.get(index).getCar_series());
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
				if (app.carDatas != null && app.carDatas.size() != 0) {
					String Device_id = app.carDatas.get(index).getDevice_id();
					if (Device_id == null || Device_id.equals("")) {
						Intent intent = new Intent(FaultActivity.this,
								DevicesAddActivity.class);
						intent.putExtra("car_id", app.carDatas.get(index).getObj_id());
						intent.putExtra("car_series_id",app.carDatas.get(index).getCar_series_id());
						intent.putExtra("car_series", app.carDatas.get(index).getCar_series());
						startActivityForResult(intent, 2);
					} else {
						Intent intent = new Intent(FaultActivity.this,
								DriveActivity.class);
						intent.putExtra("index_car", index);
						startActivityForResult(intent, 2);
					}
				}
				break;
			// 油耗，花费，里程分别显示
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
			case R.id.ll_adress:
				goCarMap(true);
				break;
			case R.id.iv_pic:
				Intent intent = new Intent(Intent.ACTION_VIEW,
						Uri.parse(adDatas.get(image_position).getUrl()));
				startActivity(intent);
				break;
			case R.id.tv_hot_content:
				Intent intent_hot = new Intent(FaultActivity.this, BrowserActivity.class);
				intent_hot.putExtra("url", hot_url);
				intent_hot.putExtra("title", hot_title);
				startActivity(intent_hot);
				break;
			case R.id.iv_update_oil:
				String device_id = app.carDatas.get(index).getDevice_id();
				if(device_id == null || device_id.equals("")){
					Toast.makeText(FaultActivity.this, "您的车没有绑定终端，不能进行油耗修正", Toast.LENGTH_SHORT).show();
				}else{
					Intent intent_oil = new Intent(FaultActivity.this, OilUpdateActivity.class);
					intent_oil.putExtra("index", index);
					startActivity(intent_oil);
				}				
				break;
			}
		}
	};

	// 跳转类型
	public static final int DISTANCE = 1;// 里程
	public static final int FEE = 2;// 费用
	public static final int FUEL = 3;// 油耗

	// 根据跳转类型进行（里程，花费，油耗）页面显示
	private void getDataOne(int type) {
		if (app.carDatas != null && app.carDatas.size() != 0) {
			String Device_id = app.carDatas.get(index).getDevice_id();
			if (Device_id == null || Device_id.equals("")) {
				Intent intent = new Intent(FaultActivity.this,DevicesAddActivity.class);
				intent.putExtra("car_id", app.carDatas.get(index).getObj_id());
				intent.putExtra("car_series_id",app.carDatas.get(index).getCar_series_id());
				intent.putExtra("car_series", app.carDatas.get(index).getCar_series());
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
			case gethot_news:
				jsonhot_news(msg.obj.toString());
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
			case get_health:
				// 显示体检信息
				try {
					JSONObject jsonObject = new JSONObject(msg.obj.toString());
					// 健康指数
					int health_score = jsonObject.getInt("health_score");
					carViews.get(msg.arg1).getmTasksView()
							.setProgress(health_score);
					carViews.get(msg.arg1).getTv_score()
							.setText(String.valueOf(health_score));
					carViews.get(msg.arg1).getTv_title().setText("健康指数");
				} catch (Exception e) {
					e.printStackTrace();
				}
				SharedPreferences preferences = getSharedPreferences(
						Constant.sharedPreferencesName, Context.MODE_PRIVATE);
				Editor editor = preferences.edit();
				editor.putString(
						Constant.sp_health_score
								+ app.carDatas.get(msg.arg1).getObj_id(),
						msg.obj.toString());
				editor.commit();
				break;
			case get_device:
				try {
					JSONObject jsonObject = new JSONObject(msg.obj.toString());
					int drive_score = jsonObject.getInt("drive_score");
					if (drive_score != 0) {
						carViews.get(msg.arg1).getTcv_drive()
								.setProgress(drive_score);
						carViews.get(msg.arg1).getTv_drive()
								.setText(String.valueOf(drive_score));
						// 存在本地
						SharedPreferences preferences1 = getSharedPreferences(
								Constant.sharedPreferencesName,
								Context.MODE_PRIVATE);
						Editor editor1 = preferences1.edit();
						editor1.putString(Constant.sp_drive_score
								+ app.carDatas.get(msg.arg1).getObj_id(),
								msg.obj.toString());
						editor1.commit();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

				break;
			case get_ad:
				setImageView(msg.obj.toString());
				getImage();
				changeImage(0);
				tv_content.setText(adDatas.get(0).getContent());
				break;
			}
		}
	};

	/** 获取当前车辆需要显示的所有数据 **/
	private void getTotalData() {
		try {
			if (app.carDatas == null || app.carDatas.size() == 0) {
				return;
			}
			// 防止删除车辆后数组越界
			if (index < app.carDatas.size()) {
				CarData carData = app.carDatas.get(index);
				String device_id = carData.getDevice_id();
				if (device_id == null || device_id.equals("")) {

				} else {
					String Gas_no = "";
					if (carData.getGas_no() == null
							|| carData.getGas_no().equals("")) {
						Gas_no = "93#(92#)";
					} else {
						Gas_no = carData.getGas_no();
					}
					// 获取当前月的数据
					String url = Constant.BaseUrl + "device/" + device_id
							+ "/total?auth_code=" + app.auth_code
							+ "&start_day=" + startMonth + "&end_day="
							+ endMonth + "&city="
							+ URLEncoder.encode(app.City, "UTF-8") + "&gas_no="
							+ Gas_no;
					new NetThread.GetDataThread(handler, url, getData, index)
							.start();
					// 获取gps信息
					String gpsUrl = Constant.BaseUrl + "device/" + device_id
							+ "?auth_code=" + app.auth_code
							+ "&update_time=2014-01-01%2019:06:43";
					new NetThread.GetDataThread(handler, gpsUrl, get_gps, index)
							.start();

					// 从服务器获取体检信息
					String url1 = Constant.BaseUrl
							+ "device/"
							+ device_id
							+ "/health_exam?auth_code="
							+ app.auth_code
							+ "&brand="
							+ URLEncoder
									.encode(carData.getCar_brand(), "UTF-8");
					new NetThread.GetDataThread(handler, url1, get_health,
							index).start();
					// 获取驾驶信息
					String url2 = Constant.BaseUrl + "device/" + device_id
							+ "/day_drive?auth_code=" + app.auth_code + "&day="
							+ GetSystem.GetNowMonth().getDay() + "&city="
							+ URLEncoder.encode(app.City, "UTF-8") + "&gas_no="
							+ Gas_no;
					new NetThread.GetDataThread(handler, url2, get_device,
							index).start();
				}
				// 获取限行信息
				if (app.City == null || carData.getObj_name() == null
						|| app.City.equals("")
						|| carData.getObj_name().equals("")) {

				} else {
					String url = Constant.BaseUrl + "base/ban?city="
							+ URLEncoder.encode(app.City, "UTF-8")
							+ "&obj_name="
							+ URLEncoder.encode(carData.getObj_name(), "UTF-8");
					new NetThread.GetDataThread(handler, url, Get_carLimit,
							index).start();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 获取GPS信息 **/
	private void jsonGps(String str, int index) {
		try {
			if (index < app.carDatas.size()) {
				JSONObject jsonObject = new JSONObject(str)
						.getJSONObject("active_gps_data");
				double lat = jsonObject.getDouble("lat");
				double lon = jsonObject.getDouble("lon");
				String uni_status = jsonObject.getString("uni_status");
				String rcv_time = jsonObject.getString("rcv_time");
				LatLng latLng = new LatLng(lat, lon);
				app.carDatas.get(index).setUni_status(uni_status);
				app.carDatas.get(index).setLat(lat);
				app.carDatas.get(index).setLon(lon);
				app.carDatas.get(index).setRcv_time(
						GetSystem.ChangeTimeZone(rcv_time.substring(0, 19)
								.replace("T", " ")));
				GetSystem.myLog(TAG, "lat = " + lat + " , Lon = " + lon);
				mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption()
						.location(latLng));

				JSONObject jObject = new JSONObject(str)
						.getJSONObject("params");
				int sensitivity = 0;
				if (jObject.opt("sensitivity") != null) {
					sensitivity = jObject.getInt("sensitivity");
				}
				app.carDatas.get(index).setSensitivity(sensitivity);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/** 解析本月油价 **/
	private void jsonData(String str, int index) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			if (jsonObject.toString() == null
					|| jsonObject.toString().equals("")) {
				return;
			}
			if(index < carViews.size()){
				CarView carView = carViews.get(index);
				carView.getTv_fee().setText(
						String.format("%.1f", jsonObject.getDouble("total_fee")));// 花费
				carView.getTv_fuel().setText(
						String.format("%.1f", jsonObject.getDouble("total_fuel")));// 油耗
				// 剩余里程显示
				if ((jsonObject.getString("left_distance")).equals("null")) {
					carView.getTv_distance().setText(String.format("%.0f", 0.0));
				} else if (jsonObject.getDouble("left_distance") == 0) {
					carView.getTv_distance().setText(
							String.format("%.1f",
									jsonObject.getDouble("total_distance")));// 里程
				} else {
					carView.getTv_current_distance().setText("剩余里程");
					try {
						carView.getTv_distance().setText(
								String.format("%.1f",
										jsonObject.getDouble("left_distance")));// 里程
					} catch (Exception e) {
						carView.getTv_distance().setText(
								String.format("0"));// 里程
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 解析车辆限行 **/
	private void jsonCarLinit(String result, int index) {
		try {
			CarView carView = carViews.get(index);
			if (result == null || result.equals("")) {
				carView.getTv_xx().setText("不限");
				app.carDatas.get(index).setLimit("不限");
			} else {
				JSONObject jsonObject = new JSONObject(result);
				String limit = jsonObject.getString("limit");
				carView.getTv_xx().setText(limit);
				app.carDatas.get(index).setLimit(limit);
			}
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
		app.City = preferences.getString(Constant.sp_city, "");
		tv_city.setText("[ " + app.City + " ]");
		app.Province = preferences.getString(Constant.sp_province, "");
		try {
			String url = Constant.BaseUrl + "base/weather2?city="
					+ URLEncoder.encode(app.City, "UTF-8");
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

	/** 获取热点信息 **/
	private void gethot_news() {
		try {
			String url = Constant.BaseUrl + "base/hot_news?city=" + URLEncoder.encode(app.City, "UTF-8");
			new NetThread.GetDataThread(handler, url, gethot_news).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	String hot_url;
	String hot_title;
	/** 解析乐一下 **/
	private void jsonhot_news(String str) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			tv_hot_content.setText(jsonObject.getString("content"));
			hot_title = jsonObject.getString("title");
			tv_host_title.setText(hot_title);
			hot_url = jsonObject.getString("url");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	// 跳转到地图界面
	private void goCarMap(boolean b) {
		Intent intent = new Intent(FaultActivity.this,
				CarLocationActivity.class);
		intent.putExtra("index", index);
		intent.putExtra("isHotLocation", b);
		startActivity(intent);
	}

	/** 滑动车辆布局 **/
	private void initDataView() {// 布局
		// 删除车辆后重新布局，如果删除的是最后一个车辆，则重置为第一个车
		if (index < app.carDatas.size()) {

		} else {
			index = 0;
		}
		SharedPreferences preferences = getSharedPreferences(
				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		hs_car.removeAllViews();
		carViews.clear();
		for (int i = 0; i < app.carDatas.size(); i++) {
			View v = LayoutInflater.from(this).inflate(R.layout.item_fault,
					null);
			hs_car.addView(v);
			ImageView iv_update_oil = (ImageView)v.findViewById(R.id.iv_update_oil);
			iv_update_oil.setOnClickListener(onClickListener);
			RelativeLayout rl_left_complete = (RelativeLayout) v
					.findViewById(R.id.rl_left_complete);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			lp.setMargins(0, 0, completed, 0);
			rl_left_complete.setLayoutParams(lp);

			LinearLayout ll_adress = (LinearLayout) v
					.findViewById(R.id.ll_adress);
			ll_adress.setOnClickListener(onClickListener);
			TextView tv_score = (TextView) v.findViewById(R.id.tv_score);
			TextView tv_title = (TextView) v.findViewById(R.id.tv_title);
			TasksCompletedView mTasksView = (TasksCompletedView) v
					.findViewById(R.id.tasks_view);

			mTasksView.setOnClickListener(onClickListener);
			// 当前里程数
			TextView tv_current_distance = (TextView) v
					.findViewById(R.id.tv_current_distance);

			TextView tv_distance = (TextView) v.findViewById(R.id.tv_distance);
			TextView tv_fee = (TextView) v.findViewById(R.id.tv_fee);
			TextView tv_fuel = (TextView) v.findViewById(R.id.tv_fuel);
			TextView tv_name = (TextView) v.findViewById(R.id.tv_name);
			TextView tv_xx = (TextView) v.findViewById(R.id.tv_xx);
			TextView tv_adress = (TextView) v.findViewById(R.id.tv_adress);

			v.findViewById(R.id.Liner_distance).setOnClickListener(
					onClickListener);
			v.findViewById(R.id.Liner_fuel).setOnClickListener(onClickListener);
			v.findViewById(R.id.Liner_fee).setOnClickListener(onClickListener);

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

			tv_name.setText(app.carDatas.get(i).getNick_name());
			String Device_id = app.carDatas.get(i).getDevice_id();
			if (Device_id == null || Device_id.equals("")) {
				carView.getmTasksView().setProgress(100);
				tv_score.setText("0");
				tv_title.setText("未绑定终端");

				tcv_drive.setProgress(100);
				tv_drive.setText("0");
			} else {
				String result = preferences.getString(Constant.sp_health_score
						+ app.carDatas.get(i).getObj_id(), "");
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
						+ app.carDatas.get(i).getObj_id(), "");
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
		String url = Constant.BaseUrl + "customer/" + app.cust_id
				+ "/counter?auth_code=" + app.auth_code;
		new NetThread.GetDataThread(handler, url, get_counter).start();
	}

	/** 解析消息数据 **/
	private void jsonCounter(String str) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			if (jsonObject.opt("noti_count") != null) {
				app.noti_count = jsonObject.getInt("noti_count");
			}
			if (jsonObject.opt("vio_count") != null) {
				app.vio_count = jsonObject.getInt("vio_count");
			}
			setNotiView();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	boolean isGetGps = true;
	boolean isGetAllData = true;

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
		if (app.noti_count == 0 && app.vio_count == 0) {
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
			// TODO 没有数据不考虑
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
					//消息
					startActivity(new Intent(FaultActivity.this,
							NoticeActivity.class));
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
		if (index_message >= nsDatas.size()) {
			index_message = 0;
		}
		nstv_message.snapToScreen(index_message);
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
	boolean isCycle = true;
	// 定时调整滚动消息
	class CycleNstvThread extends Thread {
		@Override
		public void run() {
			super.run();
			while (isCycle) {
				try {
					Thread.sleep(10000);
					Message message = new Message();
					message.what = Nstv;
					handler.sendMessage(message);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/** 根据obj_id返回对应列表的位置 **/
	private int getIndexFromId(int obj_id) {
		for (int i = 0; i < app.carDatas.size(); i++) {
			if (app.carDatas.get(i).getObj_id() == obj_id) {
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
		/**
		 * requestCode = 5,跳转到更多页面
		 */
		if(requestCode == 5 && resultCode == 1){
			finish();
		}else if (resultCode == 3) {
			// 修改车辆信息
			initDataView();
			if (app.carDatas.size() == 0) {
				rl_ad = (RelativeLayout) findViewById(R.id.rl_ad);
				rl_ad.setVisibility(View.VISIBLE);
				tv_content = (TextView) findViewById(R.id.tv_content);
				tv_content.setOnClickListener(onClickListener);
				ll_image = (LinearLayout) findViewById(R.id.ll_image);
				hs_photo = (HScrollLayout) findViewById(R.id.hs_photo);
				getAD();
				hs_photo.setOnViewChangeListener(new OnViewChangeListener() {
					@Override
					public void OnViewChange(int view) {
						image_position = view;
						tv_content.setText(adDatas.get(view).getContent());
						changeImage(view);
					}

					@Override
					public void OnLastView() {
					}

					@Override
					public void OnFinish(int index) {
					}
				});
			} else {
				if (rl_ad != null) {
					rl_ad.setVisibility(View.GONE);
				}
			}
		} else if (requestCode == 0) {
			// 修改城市返回,在onResume里刷新了城市
		} else if (requestCode == 1) {
			// requestCode = 1, resultCode = 2
			// 体检返回重新布局
			initDataView();
		} else if (requestCode == 2) {
			// requestCode = 2, resultCode = 0
			// 驾驶习惯返回
			/** 驾驶信息 **/
			SharedPreferences preferences = getSharedPreferences(
					Constant.sharedPreferencesName, Context.MODE_PRIVATE);
			String drive = preferences.getString(Constant.sp_drive_score
					+ app.carDatas.get(index).getObj_id(), "");
			if (drive.equals("")) {

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
		isGetGps = false;
		isGetAllData = false;
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (smv_content.getCurrentScreen() == 1) {
				smv_content.snapToScreen(0);
			} else {
				GetSystem.myLog(TAG, "app.cust_id = " + app.cust_id);
				if(app.cust_id == null || app.cust_id.equals("")){
					finish();
				}else{
					//TODO 在幕后
					Intent intent = new Intent(); 
					intent.setAction(Intent.ACTION_MAIN); 
					intent.addCategory(Intent.CATEGORY_HOME);           
					startActivity(intent);
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
			GetSystem.myLog(TAG, action);
			if (action.equals(Constant.A_RefreshHomeCar)) {
				GetSystem.myLog(TAG, "A_RefreshHomeCar");
				initDataView();
				getTotalData();
				noticeUrl = Constant.BaseUrl + "customer/" + app.cust_id
						+ "/tips?auth_code=" + app.auth_code;
				getMessage(noticeUrl);
				getCounter();
				noticeFragment.ResetNotice();

				if (app.carDatas.size() == 0) {
					rl_ad = (RelativeLayout) findViewById(R.id.rl_ad);
					rl_ad.setVisibility(View.VISIBLE);
					tv_content = (TextView) findViewById(R.id.tv_content);
					tv_content.setOnClickListener(onClickListener);
					ll_image = (LinearLayout) findViewById(R.id.ll_image);
					hs_photo = (HScrollLayout) findViewById(R.id.hs_photo);
					getAD();
					hs_photo.setOnViewChangeListener(new OnViewChangeListener() {
						@Override
						public void OnViewChange(int view) {
							image_position = view;
							tv_content.setText(adDatas.get(view).getContent());
							changeImage(view);
						}

						@Override
						public void OnLastView() {
						}

						@Override
						public void OnFinish(int index) {
						}
					});
				} else {
					if (rl_ad != null) {
						rl_ad.setVisibility(View.GONE);
					}
				}

			} else if (action.equals(Constant.A_LoginOut)) {
				setLoginView();
				noticeUrl = Constant.BaseUrl + "customer/0/tips";
				getMessage(noticeUrl);
				noticeFragment.ClearNotice();
			} else if (action.equals(Constant.A_City)) {
				try {
					app.City = intent.getStringExtra("City");
					app.Province = intent.getStringExtra("Province");
					String url = Constant.BaseUrl + "base/weather2?city="
							+ URLEncoder.encode(app.City, "UTF-8");
					new NetThread.GetDataThread(handler, url, getWeather)
							.start();
					SharedPreferences preferences = getSharedPreferences(
							Constant.sharedPreferencesName,
							Context.MODE_PRIVATE);
					Editor editor = preferences.edit();
					editor.putString(Constant.sp_city, app.City);
					editor.putString(Constant.sp_province, app.Province);
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
					String adress = result.getAddress();
					int startIndex = adress.indexOf("省") + 1;
					adress = adress.substring(startIndex, adress.length());
					app.carDatas.get(index).setAdress(adress);
					String rcv_time = app.carDatas.get(index).getRcv_time();
					String gpsData = rcv_time.substring(0, 10);// 取出日期
					String nowData = GetSystem.GetNowDay();
					String showTime = "";
					if (gpsData.equals(nowData)) {
						showTime = rcv_time.substring(11, 16);
					} else if (gpsData.equals(GetSystem
							.GetNextData(nowData, -1))) {
						showTime = "昨天" + rcv_time.substring(11, 16);
					} else if (gpsData.equals(GetSystem
							.GetNextData(nowData, -2))) {
						showTime = "前天" + rcv_time.substring(11, 16);
					} else {
						showTime = rcv_time.substring(5, 16);
					}
					// 显示时间
					carViews.get(index).getLl_adress()
							.setVisibility(View.VISIBLE);
					carViews.get(index).getTv_adress()
							.setText(adress + "  " + showTime);
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
