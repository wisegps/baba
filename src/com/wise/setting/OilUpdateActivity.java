package com.wise.setting;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.net.NetThread;
import com.wise.car.TravelActivity;

/** 油耗修正 **/
public class OilUpdateActivity extends Activity {

	private static final int Data2Judge = 1;
	private static final int SetFristData2Cloud = 2;
	private static final int SetSecondData2Cloud = 3;
	private static final int reset = 4;
	EditText et_oil_record;
	AppApplication app;
	int index;
	String device_id;
	Button bt_oil_add_1, bt_oil_add_2;
	ProgressDialog dialog = null;
	ScrollView scrollView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_oil_update);
		app = (AppApplication) getApplication();
		index = getIntent().getIntExtra("index", 0);
		device_id = app.carDatas.get(index).getDevice_id();
		et_oil_record = (EditText) findViewById(R.id.et_oil_record);
		findViewById(R.id.iv_back).setOnClickListener(onClickListener);
		findViewById(R.id.bt_oil_update).setOnClickListener(onClickListener);
		Button bt_reset = (Button) findViewById(R.id.bt_reset);
		bt_reset.setOnClickListener(onClickListener);
		bt_oil_add_1 = (Button) findViewById(R.id.bt_oil_add_1);
		bt_oil_add_1.setOnClickListener(onClickListener);
		bt_oil_add_2 = (Button) findViewById(R.id.bt_oil_add_2);
		bt_oil_add_2.setOnClickListener(onClickListener);
		scrollView = (ScrollView) findViewById(R.id.scrollView);
		getData2Judge();
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.bt_oil_update:// 车上有车行电脑，直接开始修正
				Intent intent = new Intent(OilUpdateActivity.this, TravelActivity.class);
				intent.putExtra("index", index);
				startActivity(intent);
				break;
			case R.id.bt_oil_add_1:// 无车行电脑，第一次加油
				setFristData2Cloud(1, 0);
				break;
			case R.id.bt_oil_add_2:// 第二次加油，并录入
				String oil = et_oil_record.getText().toString().trim();
				if (oil.equals("")) {
					Toast.makeText(OilUpdateActivity.this, "加油量不能为空", Toast.LENGTH_SHORT).show();
				} else {
					setSecondData2Cloud(2, Float.valueOf(oil));
				}
				break;
			case R.id.bt_reset:
				resetOil();
				break;
			}
		}
	};
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case Data2Judge:
				jsonData2Judge(msg.obj.toString());
				scrollView.scrollTo(0, 0);
				break;
			case SetFristData2Cloud:
				jsonFristData2Cloud(msg.obj.toString());
				break;
			case SetSecondData2Cloud:
				jsonSecondData2Cloud(msg.obj.toString());
				break;
			case reset:
				jsonResetOil(msg.obj.toString());
				break;
			}
		}
	};

	/** 重置油耗 **/
	private void resetOil() {
		dialog = ProgressDialog.show(OilUpdateActivity.this, "提示", "重置加油修正中");
		dialog.setCancelable(true);
		String url = Constant.BaseUrl + "device/" + device_id + "/refuel/reset?auth_code=" + app.auth_code;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		new NetThread.postDataThread(handler, url, params, reset).start();
	}

	private void jsonResetOil(String result) {
		if (dialog != null) {
			dialog.dismiss();
		}
		try {
			JSONObject jsonObject = new JSONObject(result);
			if (jsonObject.getInt("status_code") == 0) {
				Toast.makeText(OilUpdateActivity.this, "重置加油修正成功，请重新开始进行加油修正。", Toast.LENGTH_SHORT).show();
				bt_oil_add_1.setEnabled(true);
				bt_oil_add_2.setEnabled(false);
			} else {
				Toast.makeText(OilUpdateActivity.this, "重置加油修正失败", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(OilUpdateActivity.this, "重置加油修正失败", Toast.LENGTH_SHORT).show();
		}
	}

	/** 获取服务器上数据判断 **/
	private void getData2Judge() {
		dialog = ProgressDialog.show(OilUpdateActivity.this, "提示", "获取数据中");
		dialog.setCancelable(true);
		String url = Constant.BaseUrl + "device/" + device_id + "/?auth_code=" + app.auth_code;
		new NetThread.GetDataThread(handler, url, Data2Judge).start();
	}

	/** 判断服务器上的数据 **/
	private void jsonData2Judge(String result) {
		if (dialog != null) {
			dialog.dismiss();
		}
		try {
			JSONObject jsonObject = new JSONObject(result);
			if (jsonObject.opt("refuel_number") == null) {
				// 没有进行过油耗修正
				bt_oil_add_2.setEnabled(false);
			} else {
				int refuel_number = jsonObject.getInt("refuel_number");
				System.out.println("refuel_number = " + refuel_number);
				if (refuel_number == 1) {
					bt_oil_add_1.setEnabled(false);
					bt_oil_add_2.setEnabled(true);
				} else if (refuel_number == 2) {
					bt_oil_add_1.setEnabled(true);
					bt_oil_add_2.setEnabled(false);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 第一次加油 **/
	private void setFristData2Cloud(int number, float quantity) {
		dialog = ProgressDialog.show(OilUpdateActivity.this, "提示", "获取数据中");
		dialog.setCancelable(true);
		String url = Constant.BaseUrl + "device/" + device_id + "/refuel?auth_code=" + app.auth_code;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("number", String.valueOf(number)));
		params.add(new BasicNameValuePair("quantity", String.valueOf(quantity)));
		new NetThread.postDataThread(handler, url, params, SetFristData2Cloud).start();
	}

	/** 解析设置油耗结果 **/
	private void jsonFristData2Cloud(String result) {
		if (dialog != null) {
			dialog.dismiss();
		}
		try {
			JSONObject jsonObject = new JSONObject(result);
			if (jsonObject.getInt("status_code") == 0) {
				Toast.makeText(OilUpdateActivity.this, "完成第一次加油成功", Toast.LENGTH_SHORT).show();
				bt_oil_add_1.setEnabled(false);
				bt_oil_add_2.setEnabled(true);
			} else {
				Toast.makeText(OilUpdateActivity.this, "完成第一次加油失败", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(OilUpdateActivity.this, "完成第一次加油失败", Toast.LENGTH_SHORT).show();
		}
	}

	/** 第二次加油 **/
	private void setSecondData2Cloud(int number, float quantity) {
		dialog = ProgressDialog.show(OilUpdateActivity.this, "提示", "获取数据中");
		dialog.setCancelable(true);
		String url = Constant.BaseUrl + "device/" + device_id + "/refuel?auth_code=" + app.auth_code;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("number", String.valueOf(number)));
		params.add(new BasicNameValuePair("quantity", String.valueOf(quantity)));
		new NetThread.postDataThread(handler, url, params, SetSecondData2Cloud).start();
	}

	/** 解析设置油耗结果 **/
	private void jsonSecondData2Cloud(String result) {
		if (dialog != null) {
			dialog.dismiss();
		}
		try {
			JSONObject jsonObject = new JSONObject(result);
			if (jsonObject.getInt("status_code") == 0) {
				Toast.makeText(OilUpdateActivity.this, "完成第二次加油成功", Toast.LENGTH_SHORT).show();
				bt_oil_add_1.setEnabled(true);
				bt_oil_add_2.setEnabled(false);
			} else {
				Toast.makeText(OilUpdateActivity.this, "完成第二次加油失败", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(OilUpdateActivity.this, "完成第二次加油失败", Toast.LENGTH_SHORT).show();
		}
	}
}
