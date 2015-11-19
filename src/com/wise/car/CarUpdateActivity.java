package com.wise.car;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;
import com.umeng.analytics.MobclickAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.app.Msg;
import com.wise.baba.biz.CarManage;
import com.wise.baba.entity.BaseData;
import com.wise.baba.entity.CarData;
import com.wise.baba.entity.CityData;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.adapter.OpenDateDialog;
import com.wise.baba.ui.adapter.OpenDateDialogListener;
import com.wise.setting.RegisterActivity;
import com.wise.state.ManageActivity;
import com.wise.violation.ShortProvincesActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 修改车辆信息
 * 
 * @author honesty
 * 
 */
public class CarUpdateActivity extends Activity {
	private final String TAG = "CarUpdateActivity";
	private final int inspection = 1;
	private final int buy_date = 2;
	private final int year_check = 3;
	private final int update = 4;
	private final int get_traffic = 5;

	private static final int getFuelPrice = 6;

	private static final int Request_Car_Model = 7;// 选择汽车型号
	private static final int Request_Insruance = 8;// 选择保险公司
	private static final int Request_City = 9; // 选择违章城市
	private static final int Request_Gas_NO = 10; // 选择汽油标号
	private static final int Request_Short = 11; // 选择城市简称
	private static final int Request_Maintain = 12;// 选择4S店

	public static final int Request_REMOVE = 13;// 请求删除验证码
	public static final int Request_UPDATE = 14;// 请求更改验证码

	public static final int Result_REMOVE = 15;// 通过验证可以删除
	public static final int Result_Update = 16;// 通过验证可以修改了

	public static final int Request_Device_Update = 17;// 终端修改成功返回
	public static final int Result_Device_Update = 18;// 终端修改成功返回

	LinearLayout ll_engine, ll_frame;
	EditText et_nick_name, et_obj_name, et_engine_no, et_frame_no,
			et_insurance_tel, et_insurance_no, et_maintain_tel;
	EditText et_oil_price;// 加油价格
	TextView tv_models, tv_gas_no, tv_city, tv_insurance_company,
			tv_insurance_date, tv_maintain_company, tv_buy_date, tv_year_check;
	int index = 0;
	/** true服务商跳转，false用户自己跳转 **/
	boolean isService = false;
	CarData carData;
	CarData carNewData = new CarData();

	List<CityData> chooseCityDatas = new ArrayList<CityData>();
	String car_brand = "";
	String car_brand_id = "";
	String car_series = "";
	String car_series_id = "";
	String car_type = "";
	String car_type_id = "";
	AppApplication app;
	CarManage carManage = null;
	private Handler handler;
	private HandlerThread handlerThread = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_car_update);
		app = (AppApplication) getApplication();
		index = getIntent().getIntExtra("index", 0);

		handlerThread = new HandlerThread("HttpAir");
		handlerThread.start();

		Looper looper = handlerThread.getLooper();
		handler = new Handler(looper, callback);

		isService = getIntent().getBooleanExtra("isService", false);
		if (isService) {
			carData = ManageActivity.carDatas.get(index);
		} else {
			carData = app.carDatas.get(index);
		}
		carNewData = carData;
		carManage = new CarManage(this, app, handler);
		init();
		setData();
		setTime();
		getTraffic();
		getFuelPrice();

	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.iv_save:
				if (app.isTest) {
					Toast.makeText(CarUpdateActivity.this, "演示账号不支持该功能",
							Toast.LENGTH_SHORT).show();
					return;
				}
				Save();
				break;
			case R.id.tv_models:
				startActivityForResult(new Intent(CarUpdateActivity.this,
						ModelsActivity.class), Request_Car_Model);
				break;
			case R.id.tv_city:
				Intent intent1 = new Intent(CarUpdateActivity.this,
						TrafficCitiyActivity.class);
				intent1.putExtra("cityDatas", (Serializable) chooseCityDatas);
				startActivityForResult(intent1, Request_City);
				break;
			case R.id.tv_gas_no:
				startActivityForResult(new Intent(CarUpdateActivity.this,
						PetrolGradeActivity.class), Request_Gas_NO);
				break;
			case R.id.tv_insurance_company:
				startActivityForResult(new Intent(CarUpdateActivity.this,

				InsuranceActivity.class), Request_Insruance);
				break;
			case R.id.tv_maintain_company:
				Intent intent = new Intent(CarUpdateActivity.this,
						FoursActivity.class);
				intent.putExtra("city", app.City);
				intent.putExtra("brank", car_brand);

				startActivityForResult(intent, Request_Maintain);
				break;
			case R.id.tv_insurance_date:
				ShowDate(inspection);
				break;
			case R.id.tv_buy_date:
				ShowDate(buy_date);
				break;
			case R.id.tv_year_check:
				ShowDate(year_check);
				break;
			case R.id.btn_choose:
				Intent intent2 = new Intent(CarUpdateActivity.this,
						ShortProvincesActivity.class);
				startActivityForResult(intent2, Request_Short);
				break;
			case R.id.image_help_1:// 帮助图片显示
				helpPopView();
				break;
			case R.id.image_help_2:
				helpPopView();
				break;
			case R.id.btnUnbind:
				if (hasDeviceId()) {
					intentToRegister(Request_REMOVE);
				}
				break;
			case R.id.btnUpdate:
				if (hasDeviceId()) {
					// carManage.updateDevice(index);//测试用
					intentToRegister(Request_UPDATE);
				}

				break;
			case R.id.btnDelete:
				if (app.isTest) {
					Toast.makeText(CarUpdateActivity.this, "演示账号不支持该功能",
							Toast.LENGTH_SHORT).show();
					return;
				}
				carManage.delete(index);
				break;
			}

		}
	};

	public boolean hasDeviceId() {
		boolean b = true;
		if (carNewData.getDevice_id() == null
				|| carNewData.getDevice_id().equals("")
				|| carNewData.getDevice_id().equals("0")) {
			b = false;
			Toast.makeText(this, "未绑定智能终端", Toast.LENGTH_SHORT).show();
		}
		return b;
	}

	public void intentToRegister(int request) {
		SharedPreferences preferences = getSharedPreferences(
				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		String sp_account = preferences.getString(Constant.sp_account, "");
		if (app.isTest) {
			Toast.makeText(CarUpdateActivity.this, "演示账号不支持该功能",
					Toast.LENGTH_SHORT).show();
			return;
		}
		Intent intent = new Intent(CarUpdateActivity.this,
				RegisterActivity.class);
		intent.putExtra("mark", 1);
		if (request == Request_REMOVE) {
			intent.putExtra("remove", true);
		} else if (request == Request_UPDATE) {
			intent.putExtra("device_update", true);
		}

		intent.putExtra("account", sp_account);
		startActivityForResult(intent, request);
	}

	Handler.Callback callback = new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case update:
				jsonSave(msg.obj.toString());
				break;
			case get_traffic:
				parseJson(msg.obj.toString());
				break;
			case getFuelPrice:
				jsonFuelPrice(msg.obj.toString());
				break;
			case Msg.Delete_Car_Success:
				app.carDatas.remove(index);
				CarUpdateActivity.this.finish();
				break;

			}
			return false;
		}
	};

	/** 获取油价 **/
	private void getFuelPrice() {
		try {
			String url = Constant.BaseUrl + "base/city/"
					+ URLEncoder.encode(app.City, "UTF-8");
			new NetThread.GetDataThread(handler, url, getFuelPrice).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	String fuel90 = "0";
	String fuel93 = "0";
	String fuel97 = "0";
	String fuel0 = "0";

	/** 解析油价 **/
	private void jsonFuelPrice(String result) {
		try {
			JSONObject jsonObject = new JSONObject(result)
					.getJSONObject("fuel_price");
			fuel90 = jsonObject.getString("fuel90");
			fuel93 = jsonObject.getString("fuel93");
			fuel97 = jsonObject.getString("fuel97");
			fuel0 = jsonObject.getString("fuel0");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void helpPopView() {
		LayoutInflater inflater = LayoutInflater.from(CarUpdateActivity.this);
		final View mView = inflater.inflate(R.layout.help_image, null);
		final PopupWindow pop = new PopupWindow(mView,
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, false);
		pop.setOutsideTouchable(true);
		pop.setFocusable(true);
		pop.update();
		pop.setBackgroundDrawable(new BitmapDrawable());
		// pop视图在布局中显示的位置
		pop.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
		mView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (pop.isShowing()) {
					// 隐藏pop视图
					pop.dismiss();
				}
			}
		});
	}

	private void ShowDate(int index) {
		OpenDateDialog.ShowDate(CarUpdateActivity.this, index);
	}

	private void jsonSave(String str) {
		Log.i("CarUpdateActivity", "jsonSave " + str);
		try {
			JSONObject jsonObject = new JSONObject(str);
			if (jsonObject.getInt("status_code") == 0) {
				if (isService) {
					ManageActivity.carDatas.set(index, carNewData);
				} else {
					app.carDatas.set(index, carNewData);
				}
				setResult(3);
				finish();
			} else {
				Toast.makeText(CarUpdateActivity.this, "保存失败，请重试",
						Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(CarUpdateActivity.this, "保存失败，请重试",
					Toast.LENGTH_SHORT).show();
		}

	}

	private void Save() {
		String nick_name = et_nick_name.getText().toString();
		if (nick_name.equals("")) {
			Toast.makeText(CarUpdateActivity.this, "爱车名称不能为空",
					Toast.LENGTH_SHORT).show();
			return;
		}

		String engine_no = et_engine_no.getText().toString();
		String frame_no = et_frame_no.getText().toString();

		for (CityData cityData : chooseCityDatas) {
			// 发动机号
			if (cityData.getEngine() == 0) {

			} else {
				if (cityData.getEngineno() == 1) {// 全部
					if (engine_no.length() == 0) {
						Toast.makeText(CarUpdateActivity.this, "需要完整的发动机号",
								Toast.LENGTH_SHORT).show();
						return;
					}
				} else {
					if (engine_no.length() < cityData.getEngineno()) {
						Toast.makeText(CarUpdateActivity.this,
								"需要发动机号的后" + cityData.getEngineno() + "位",
								Toast.LENGTH_SHORT).show();
						return;
					}
				}
			}
			// 车架号
			if (cityData.getFrame() == 0) {

			} else {
				if (cityData.getFrameno() == 1) {// 全部
					if (frame_no.length() == 0) {
						Toast.makeText(CarUpdateActivity.this, "需要完整的车架号",
								Toast.LENGTH_SHORT).show();
						return;
					}
				} else {
					if (frame_no.length() < cityData.getFrameno()) {
						Toast.makeText(CarUpdateActivity.this,
								"需要车架号的后" + cityData.getFrameno() + "位",
								Toast.LENGTH_SHORT).show();
						return;
					}
				}
			}
		}

		String obj_name = choose_car_province.getText()
				+ et_obj_name.getText().toString();
		String gas_no = tv_gas_no.getText().toString();
		String insurance_company = tv_insurance_company.getText().toString();
		String insurance_tel = et_insurance_tel.getText().toString();
		String insurance_date = tv_insurance_date.getText().toString();
		String insurance_no = et_insurance_no.getText().toString();
		String maintain_company = tv_maintain_company.getText().toString();
		String maintain_tel = et_maintain_tel.getText().toString();
		String buy_date = tv_buy_date.getText().toString();
		String year_check = tv_year_check.getText().toString();
		String fuel_price = et_oil_price.getText().toString().trim();

		carNewData.setDevice_id(carData.getDevice_id());
		carNewData.setObj_name(obj_name);
		carNewData.setNick_name(nick_name);
		carNewData.setCar_brand(car_brand);
		carNewData.setCar_series(car_series);
		carNewData.setCar_type(car_type);

		ArrayList<String> vio_citys = new ArrayList<String>();
		ArrayList<String> vio_citys_code = new ArrayList<String>();
		for (int j = 0; j < chooseCityDatas.size(); j++) {
			String vio_city_name = chooseCityDatas.get(j).getCityName();
			String vio_location = chooseCityDatas.get(j).getCityCode();
			vio_citys.add(vio_city_name);
			vio_citys_code.add(vio_location);
		}
		carNewData.setVio_citys(vio_citys);
		carNewData.setVio_citys_code(vio_citys_code);
		carNewData.setEngine_no(engine_no);
		carNewData.setFrame_no(frame_no);
		carNewData.setRegNo("");
		carNewData.setInsurance_company(insurance_company);
		carNewData.setInsurance_tel(insurance_tel);
		carNewData.setInsurance_date(insurance_date);
		carNewData.setInsurance_no(insurance_no);
		carNewData.setMaintain_company(maintain_company);
		carNewData.setMaintain_tel(maintain_tel);
		carNewData.setBuy_date(buy_date);
		carNewData.setGas_no(gas_no);
		carNewData.setCar_brand_id(car_brand_id);
		carNewData.setCar_series_id(car_series_id);
		carNewData.setCar_type_id(car_type_id);

		if (fuel_price.length() > 0) {
			carNewData.setFuel_price(Double.valueOf(fuel_price));
		}

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("obj_name", obj_name));
		params.add(new BasicNameValuePair("nick_name", nick_name));
		params.add(new BasicNameValuePair("car_brand", car_brand));
		params.add(new BasicNameValuePair("car_series", car_series));
		params.add(new BasicNameValuePair("car_type", car_type));
		params.add(new BasicNameValuePair("vio_citys",
				jsonList(chooseCityDatas)));
		params.add(new BasicNameValuePair("engine_no", engine_no));
		params.add(new BasicNameValuePair("frame_no", frame_no));
		params.add(new BasicNameValuePair("reg_no", ""));
		params.add(new BasicNameValuePair("insurance_company",
				insurance_company));
		params.add(new BasicNameValuePair("insurance_tel", insurance_tel));
		params.add(new BasicNameValuePair("insurance_date", insurance_date));
		params.add(new BasicNameValuePair("insurance_no", insurance_no));
		params.add(new BasicNameValuePair("maintain_company", maintain_company));
		params.add(new BasicNameValuePair("maintain_tel", maintain_tel));
		params.add(new BasicNameValuePair("maintain_last_mileage", "0"));
		params.add(new BasicNameValuePair("maintain_last_date", "2014-10-10"));
		params.add(new BasicNameValuePair("buy_date", buy_date));
		params.add(new BasicNameValuePair("gas_no", gas_no));
		params.add(new BasicNameValuePair("car_brand_id", car_brand_id));
		params.add(new BasicNameValuePair("car_series_id", car_series_id));
		params.add(new BasicNameValuePair("car_type_id", car_type_id));
		params.add(new BasicNameValuePair("fuel_price", fuel_price));

		String url = Constant.BaseUrl + "vehicle/" + carData.getObj_id()
				+ "?auth_code=" + app.auth_code;
		Log.i("CarUpdateActivity", "AA " + url);
		Log.i("CarUpdateActivity", "carData.getObj_id " + carData.getObj_id());
		Log.i("CarUpdateActivity", "insurance_company " + insurance_company);
		new NetThread.putDataThread(handler, url, params, update).start();
	}

	private void setTime() {
		OpenDateDialog.SetCustomDateListener(new OpenDateDialogListener() {
			@Override
			public void OnDateChange(String Date, int index) {
				switch (index) {
				case inspection:
					tv_insurance_date.setText(Date);
					break;
				case buy_date:
					tv_buy_date.setText(Date);
					break;
				case year_check:
					tv_year_check.setText(Date);
					break;
				}
			}
		});
	}

	private void setData() {
		Log.d(TAG, carData.toString());
		car_brand = carData.getCar_brand();
		car_brand_id = carData.getCar_brand_id();
		car_series = carData.getCar_series();
		car_series_id = carData.getCar_series_id();
		car_type = carData.getCar_type();
		car_type_id = carData.getCar_type_id();
		et_nick_name.setText(carData.getNick_name());

		if (carData.getObj_name() != null && !carData.getObj_name().equals("")) {
			if (app.isTest) {
				et_obj_name.setText(carData.getObj_name().substring(1, 4)
						+ "***");
				choose_car_province.setText(carData.getObj_name().substring(0,
						1));
			} else {
				et_obj_name.setText(carData.getObj_name().substring(1,
						carData.getObj_name().length()));
				choose_car_province.setText(carData.getObj_name().substring(0,
						1));
			}
		} else {
			et_obj_name.setText("");
		}
		tv_models.setText(carData.getCar_series() + carData.getCar_type());
		tv_gas_no.setText(carData.getGas_no());
		if (app.isTest) {
			et_engine_no.setText("01****");
			et_frame_no.setText("61**");
		} else {
			et_engine_no.setText(carData.getEngine_no());
			et_frame_no.setText(carData.getFrame_no());
		}
		et_oil_price.setText("" + carData.getFuel_price());
		tv_insurance_company.setText(carData.getInsurance_company());
		et_insurance_tel.setText(carData.getInsurance_tel());
		tv_insurance_date.setText(carData.getInsurance_date());
		et_insurance_no.setText(carData.getInsurance_no());
		tv_maintain_company.setText(carData.getMaintain_company());
		et_maintain_tel.setText(carData.getMaintain_tel());
		tv_buy_date.setText(carData.getBuy_date());
		tv_year_check.setText(carData.getAnnual_inspect_date());
		String citys = "";
		for (int i = 0; i < carData.getVio_citys().size(); i++) {
			citys += carData.getVio_citys().get(i) + " ";
			CityData cityData = new CityData();
			cityData.setCityName(carData.getVio_citys().get(i));
			cityData.setCityCode(carData.getVio_citys_code().get(i));
			// 防止数组越界
			if (i >= carData.getProvince().size()) {
				cityData.setProvince("");
			} else {// TODO 异常
				cityData.setProvince(carData.getProvince().get(i));
			}
			chooseCityDatas.add(cityData);
		}
		tv_city.setText(citys);
		if (citys != null && !citys.equals("")) {
			ll_engine.setVisibility(View.VISIBLE);
			ll_frame.setVisibility(View.VISIBLE);
		}
	}

	ImageButton btn_help_1, btn_help_2;
	TextView choose_car_province;
	LinearLayout choose_province;

	Button btnUnbind, btnUpdate, btnDelete;

	private void init() {
		ll_engine = (LinearLayout) findViewById(R.id.ll_engine);
		ll_frame = (LinearLayout) findViewById(R.id.ll_frame);

		ImageView iv_save = (ImageView) findViewById(R.id.iv_save);
		iv_save.setOnClickListener(onClickListener);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		et_nick_name = (EditText) findViewById(R.id.et_nick_name);
		et_obj_name = (EditText) findViewById(R.id.et_obj_name);
		// 油价价格
		et_oil_price = (EditText) findViewById(R.id.et_oil_price);
		tv_models = (TextView) findViewById(R.id.tv_models);
		tv_models.setOnClickListener(onClickListener);
		tv_gas_no = (TextView) findViewById(R.id.tv_gas_no);
		tv_gas_no.setOnClickListener(onClickListener);
		tv_city = (TextView) findViewById(R.id.tv_city);
		tv_city.setOnClickListener(onClickListener);
		et_engine_no = (EditText) findViewById(R.id.et_engine_no);
		et_frame_no = (EditText) findViewById(R.id.et_frame_no);
		tv_insurance_company = (TextView) findViewById(R.id.tv_insurance_company);
		tv_insurance_company.setOnClickListener(onClickListener);
		et_insurance_tel = (EditText) findViewById(R.id.et_insurance_tel);
		tv_insurance_date = (TextView) findViewById(R.id.tv_insurance_date);
		tv_insurance_date.setOnClickListener(onClickListener);
		et_insurance_no = (EditText) findViewById(R.id.et_insurance_no);
		tv_maintain_company = (TextView) findViewById(R.id.tv_maintain_company);
		tv_maintain_company.setOnClickListener(onClickListener);
		et_maintain_tel = (EditText) findViewById(R.id.et_maintain_tel);
		tv_buy_date = (TextView) findViewById(R.id.tv_buy_date);
		tv_buy_date.setOnClickListener(onClickListener);
		tv_year_check = (TextView) findViewById(R.id.tv_year_check);
		tv_year_check.setOnClickListener(onClickListener);
		// 省份添加
		choose_car_province = (TextView) findViewById(R.id.choose_car_province);
		choose_province = (LinearLayout) findViewById(R.id.btn_choose);
		choose_province.setOnClickListener(onClickListener);
		// 车架号和发送号help图标
		btn_help_1 = (ImageButton) findViewById(R.id.image_help_1);
		btn_help_2 = (ImageButton) findViewById(R.id.image_help_2);
		btn_help_1.setOnClickListener(onClickListener);
		btn_help_2.setOnClickListener(onClickListener);

		btnUnbind = (Button) findViewById(R.id.btnUnbind);
		btnUpdate = (Button) findViewById(R.id.btnUpdate);
		btnDelete = (Button) findViewById(R.id.btnDelete);

		btnUnbind.setOnClickListener(onClickListener);
		btnUpdate.setOnClickListener(onClickListener);
		btnDelete.setOnClickListener(onClickListener);

	}

	private String jsonList(List<CityData> chooseCityDatas) {
		try {
			JSONArray jsonArray = new JSONArray();
			for (int i = 0; i < chooseCityDatas.size(); i++) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("vio_city_name", chooseCityDatas.get(i)
						.getCityName());
				jsonObject.put("vio_location", chooseCityDatas.get(i)
						.getCityCode());
				jsonObject
						.put("province", chooseCityDatas.get(i).getProvince());
				jsonArray.put(jsonObject);
			}
			String jsonString = jsonArray.toString()
					.replaceAll("\"vio_city_name\":", "vio_city_name:")
					.replaceAll("\"vio_location\":", "vio_location:")
					.replaceAll("\"province\":", "province:");
			return jsonString;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "[]";
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/* 解除绑定 */
		if (requestCode == Request_REMOVE && resultCode == Result_REMOVE) {// 删除终端验证码验证通过
			carManage.unbind(index);
			return;

		}

		/* 修改终端 */
		if (requestCode == Request_UPDATE && resultCode == Result_Update) {// 修改终端
																			// 验证码验证通过
			Log.i("CarUpdateActivity", "修改终端");
			carManage.updateDevice(index);
			return;

		}

		/* 成功修改终端 */
		if (requestCode == Request_Device_Update && resultCode == 1) {// 修改终端成功
			this.finish();
			Log.i("CarUpdateActivity", "修改终端成功");
			return;

		}

		if (resultCode == 1) {// 汽车型号
			car_brand = data.getStringExtra("brank");
			car_brand_id = data.getStringExtra("brankId");
			car_series = data.getStringExtra("series");
			car_series_id = data.getStringExtra("seriesId");
			car_type = data.getStringExtra("type");
			car_type_id = data.getStringExtra("typeId");
			tv_models.setText(car_series + car_type);
		} else if (resultCode == 2) {// 违章城市返回
			chooseCityDatas = (List<CityData>) data
					.getSerializableExtra("cityDatas");
			String city = "";
			for (CityData cityData : chooseCityDatas) {
				city += cityData.getCityName() + " ";
			}
			tv_city.setText(city);
			setNote();
		} else if (resultCode == 3) {// 汽油标号返回
			tv_gas_no.setText(data.getStringExtra("result"));
			// 0#,90#,93#,97#
			int position = data.getIntExtra("position", 0);
			switch (position) {
			case 0:
				et_oil_price.setText(fuel0);
				break;
			case 1:
				et_oil_price.setText(fuel90);
				break;
			case 2:
				et_oil_price.setText(fuel93);
				break;
			case 3:
				et_oil_price.setText(fuel97);
				break;
			}
		} else if (resultCode == 4) {// 保险公司返回
			tv_insurance_company.setText(data.getStringExtra("insurance_name"));
			et_insurance_tel.setText(data.getStringExtra("insurance_phone"));
		} else if (resultCode == 5) {// 4s店返回
			tv_maintain_company.setText(data.getStringExtra("maintain_name"));
			et_maintain_tel.setText(data.getStringExtra("maintain_phone"));
		} else if (resultCode == 6) {// 返回所选省份
			choose_car_province.setText(data.getStringExtra("province"));
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

	/** 获取各个市违章信息 **/
	private void getTraffic() {
		List<BaseData> bDatas = DataSupport.findAll(BaseData.class);
		List<BaseData> baseDatas = DataSupport.where("Title = ?", "Violation")
				.find(BaseData.class);
		if (baseDatas.size() == 0 || baseDatas.get(0).getContent() == null
				|| baseDatas.get(0).getContent().equals("")) {
			String url = Constant.BaseUrl + "violation/city?cuth_code="
					+ app.auth_code;
			new NetThread.GetDataThread(handler, url, get_traffic).start();
		} else {
			parseJson(baseDatas.get(0).getContent());
		}
	}

	public void parseJson(String jsonData) {
		try {
			JSONObject jsonObj = new JSONObject(jsonData);
			JSONObject result = jsonObj.getJSONObject("result");
			Iterator it = result.keys();
			while (it.hasNext()) {
				String key = it.next().toString();
				JSONObject jsonObject = result.getJSONObject(key);
				JSONArray jsonArray = jsonObject.getJSONArray("citys"); // 城市
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject3 = jsonArray.getJSONObject(i);
					String city_code = jsonObject3.getString("city_code");
					int engine = jsonObject3.getInt("engine");
					int engineno = jsonObject3.getInt("engineno");
					int frame = jsonObject3.getInt("class");
					int frameno = jsonObject3.getInt("classno");
					for (CityData cityData : chooseCityDatas) {
						if (cityData.getCityCode().equals(city_code)) {
							cityData.setEngine(engine);
							cityData.setEngineno(engineno);
							cityData.setFrame(frame);
							cityData.setFrameno(frameno);
						}
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				setNote();
			}

		});

	}

	/** 设置提示 **/
	private void setNote() {
		boolean isEngine = false;
		for (CityData cityData : chooseCityDatas) {
			if (cityData.getEngine() != 0) {
				isEngine = true;
				break;
			}
		}
		if (isEngine) {// 发送机号
			ll_engine.setVisibility(View.VISIBLE);
			boolean isNeedAllEngine = false;
			int Engineno = 0;
			for (CityData cityData : chooseCityDatas) {
				if (cityData.getEngineno() == 1) {// 全部
					isNeedAllEngine = true;
				} else {
					if (cityData.getEngineno() > Engineno) {
						Engineno = cityData.getEngineno();
					}
				}
			}
			if (isNeedAllEngine) {
				et_engine_no.setHint("需要完整的发动机号");
			} else {
				et_engine_no.setHint("需要发动机号的后" + Engineno + "位");
			}
		} else {
			// 选填，隐藏
			ll_engine.setVisibility(View.GONE);
		}

		boolean isFrame = false;
		for (CityData cityData : chooseCityDatas) {
			if (cityData.getFrame() != 0) {
				isFrame = true;
				break;
			}
		}
		if (isFrame) {// 车架号
			ll_frame.setVisibility(View.VISIBLE);
			boolean isNeedAllFrame = false;
			int Frameno = 0;
			for (CityData cityData : chooseCityDatas) {
				if (cityData.getFrameno() == 1) {// 全部
					isNeedAllFrame = true;
				} else {
					if (cityData.getFrameno() > Frameno) {
						Frameno = cityData.getFrameno();
					}
				}
			}
			if (isNeedAllFrame) {
				et_frame_no.setHint("需要完整的车架号");
			} else {
				et_frame_no.setHint("需要车架号的后" + Frameno + "位");
			}
		} else {
			// 选填，隐藏
			ll_frame.setVisibility(View.GONE);
		}
	}
}
