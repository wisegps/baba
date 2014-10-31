package com.wise.car;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import pubclas.Constant;
import pubclas.NetThread;
import com.umeng.analytics.MobclickAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.violation.ShortProvincesActivity;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 添加车辆
 * 
 * @author honesty
 * 
 */
public class CarAddActivity extends Activity {
	private static final String TAG = "CarAddActivity";
	private static final int add_car = 1;
	TextView tv_models, choose_car_province;
	EditText et_nick_name, et_obj_name;
	CarData carNewData = new CarData();
	boolean fastTrack = false;
	String device_id = "";
	Button bt_jump;
	AppApplication app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_car_add);
		app = (AppApplication) getApplication();
		LinearLayout btn_choose = (LinearLayout) findViewById(R.id.btn_choose);
		btn_choose.setOnClickListener(onClickListener);
		choose_car_province = (TextView) findViewById(R.id.choose_car_province);
		et_nick_name = (EditText) findViewById(R.id.et_nick_name);
		et_obj_name = (EditText) findViewById(R.id.et_obj_name);
		tv_models = (TextView) findViewById(R.id.tv_models);
		tv_models.setOnClickListener(onClickListener);
		ImageView iv_add = (ImageView) findViewById(R.id.iv_add);
		iv_add.setOnClickListener(onClickListener);
		bt_jump = (Button) findViewById(R.id.bt_jump);
		bt_jump.setOnClickListener(onClickListener);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		fastTrack = getIntent().getBooleanExtra("fastTrack", false);
		if (fastTrack) {
			bt_jump.setVisibility(View.VISIBLE);
		} else {
			bt_jump.setVisibility(View.GONE);
		}
		device_id = getIntent().getStringExtra("device_id");

		// 根据定位城市选定车牌号
		for (int i = 0; i < provinces.length; i++) {
			if (provinces[i][0].equals(app.Province)) {
				choose_car_province.setText(provinces[i][1]);
				break;
			}
		}
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.tv_models:
				startActivityForResult(new Intent(CarAddActivity.this,
						ModelsActivity.class), 2);
				break;
			case R.id.iv_back:
				finish();
				break;
			case R.id.iv_add:
				if (app.isTest) {
					Toast.makeText(CarAddActivity.this, "演示账号不支持该功能",
							Toast.LENGTH_SHORT).show();

					return;
				}
				addCar();
				break;
			case R.id.bt_jump:
				finish();
				break;
			case R.id.btn_choose:
				startActivityForResult(new Intent(CarAddActivity.this,
						ShortProvincesActivity.class), 3);
				break;
			}
		}
	};

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case add_car:
				jsonCar(msg.obj.toString());
				break;
			}
		}
	};
	int car_id = 0;

	private void jsonCar(String str) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			if (jsonObject.getString("status_code").equals("0")) {
				car_id = jsonObject.getInt("obj_id");
				if (fastTrack) {
					Intent intent = new Intent(CarAddActivity.this,
							DevicesAddActivity.class);
					intent.putExtra("fastTrack", true);
					intent.putExtra("car_id", car_id);
					startActivity(intent);
				} else {
					carNewData.setObj_id(car_id);
					app.carDatas.add(carNewData);
					Toast.makeText(CarAddActivity.this, "车辆添加成功",
							Toast.LENGTH_SHORT).show();
					setResult(3);
				}
				finish();
			} else {
				Toast.makeText(CarAddActivity.this, "添加失败", Toast.LENGTH_SHORT)
						.show();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(CarAddActivity.this, "添加失败", Toast.LENGTH_SHORT)
					.show();
		}
	}

	String provinces[][] = { { "北京市", "京" }, { "天津市", "津" }, { "河北省", "冀" },
			{ "山西省", "晋" }, { "内蒙古自治区", "蒙" }, { "辽宁省", "辽" }, { "吉林省", "吉" },
			{ "黑龙江省", "黑" }, { "上海市", "沪" }, { "江苏省", "苏" }, { "浙江省", "浙" },
			{ "安徽省", "皖" }, { "福建省", "闽" }, { "江西省", "赣" }, { "山东省", "鲁" },
			{ "河南省", "豫" }, { "湖北省", "鄂" }, { "湖南省", "湘" }, { "广东省", "粤" },
			{ "广西壮族自治区", "桂" }, { "海南省", "琼" }, { "重庆市", "渝" }, { "四川省", "川" },
			{ "贵州省", "黔" }, { "云南省", "云" }, { "西藏自治区", "藏" }, { "陕西省", "陕" },
			{ "甘肃省", "甘" }, { "青海省", "青" }, { "宁夏回族自治区", "宁" },
			{ "新疆维吾尔自治区", "新" }, { "香港特别行政区", "港" }, { "澳门特别行政区", "澳" },
			{ "台湾省", "台" } };

	private void addCar() {
		String nick_name = et_nick_name.getText().toString().trim();
		if (nick_name.equals("")) {
			Toast.makeText(CarAddActivity.this, "车辆名称不能为空", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		if (car_brand == null || car_brand.equals("")) {
			Toast.makeText(CarAddActivity.this, "车型不能为空", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		String obj_name = choose_car_province.getText().toString()
				+ et_obj_name.getText().toString().trim();
		if (obj_name.length() == 1) {
			obj_name = "";
		}
		String url = Constant.BaseUrl + "vehicle/simple?auth_code="
				+ app.auth_code;
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		carNewData.setObj_name(obj_name);
		carNewData.setNick_name(nick_name);
		carNewData.setCar_brand(car_brand);
		carNewData.setCar_series(car_series);
		carNewData.setCar_type(car_type);
		carNewData.setCar_brand_id(car_brand_id);
		carNewData.setCar_series_id(car_series_id);
		carNewData.setCar_type_id(car_type_id);

		params.add(new BasicNameValuePair("cust_id", app.cust_id));
		params.add(new BasicNameValuePair("obj_name", obj_name));
		params.add(new BasicNameValuePair("nick_name", nick_name));
		params.add(new BasicNameValuePair("car_brand", car_brand));
		params.add(new BasicNameValuePair("car_series", car_series));
		params.add(new BasicNameValuePair("car_type", car_type));
		params.add(new BasicNameValuePair("car_brand_id", car_brand_id));
		params.add(new BasicNameValuePair("car_series_id", car_series_id));
		params.add(new BasicNameValuePair("car_type_id", car_type_id));
		new NetThread.postDataThread(handler, url, params, add_car).start();

	}

	String car_brand = "";
	String car_brand_id = "";
	String car_series = "";
	String car_series_id = "";
	String car_type = "";
	String car_type_id = "";

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 1) {
			car_brand = data.getStringExtra("brank");
			car_brand_id = data.getStringExtra("brankId");
			car_series = data.getStringExtra("series");
			car_series_id = data.getStringExtra("seriesId");
			car_type = data.getStringExtra("type");
			car_type_id = data.getStringExtra("typeId");

			tv_models.setText(car_series + car_type);
		}
		if (requestCode == 3 && resultCode == 6) {
			// 选择省份返回
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
}
