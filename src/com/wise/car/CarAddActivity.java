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
import pubclas.Variable;
import com.umeng.analytics.MobclickAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import data.CarData;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 添加车辆
 * @author honesty
 *
 */
public class CarAddActivity extends Activity{
	private static final String TAG = "CarAddActivity";
	private static final int add_car = 1;
	private static final int update_user = 2;
	private static final int update_car = 3;
	private static final int get_data = 4;
	TextView tv_models;
	EditText et_nick_name,et_obj_name;
	CarData carNewData = new CarData();
	boolean fastTrack = false;
	String device_id = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_car_add);
		et_nick_name = (EditText)findViewById(R.id.et_nick_name);
		et_obj_name = (EditText)findViewById(R.id.et_obj_name);
		tv_models = (TextView)findViewById(R.id.tv_models);
		tv_models.setOnClickListener(onClickListener);
		ImageView iv_add = (ImageView)findViewById(R.id.iv_add);
		iv_add.setOnClickListener(onClickListener);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		fastTrack = getIntent().getBooleanExtra("fastTrack", false);
		device_id = getIntent().getStringExtra("device_id");
	}
	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.tv_models:
				startActivityForResult(new Intent(CarAddActivity.this, ModelsActivity.class), 2);
				break;
			case R.id.iv_back:
				finish();
				break;
			case R.id.iv_add:
				addCar();
				break;
			}
		}
	};
	
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case add_car:
				jsonCar(msg.obj.toString());
				break;

			case update_user:
				try {
					String status_code = new JSONObject(msg.obj.toString()).getString("status_code");
					if (status_code.equals("0")) {
						// 绑定车辆
						String url = Constant.BaseUrl + "vehicle/" + car_id
								+ "/device?auth_code=" + Variable.auth_code;
						List<NameValuePair> params = new ArrayList<NameValuePair>();
						params.add(new BasicNameValuePair("device_id",device_id));
						new NetThread.putDataThread(handler,url, params, update_car).start();
					} else {
						
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case update_car:
				try {
					String status_code = new JSONObject(msg.obj.toString()).getString("status_code");
					if (status_code.equals("0")) {
						//TODO 快速注册完毕
						String url = Constant.BaseUrl + "customer/" + Variable.cust_id
								+ "/vehicle?auth_code=" + Variable.auth_code;
						new NetThread.GetDataThread(handler, url, get_data).start();						
					}else{
						
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case get_data:
				Variable.carDatas.clear();
				Variable.carDatas.addAll(JsonData.jsonCarInfo(msg.obj.toString()));
				Intent intent = new Intent(Constant.A_RefreshHomeCar);
	            sendBroadcast(intent);
	            finish();
				AppApplication.getActivityInstance().exit();
				break;
			}
		}		
	};
	int car_id = 0;
	private void jsonCar(String str){
		try {
			JSONObject jsonObject = new JSONObject(str);
			if(jsonObject.getString("status_code").equals("0")){
				car_id = jsonObject.getInt("obj_id");
				if(fastTrack){
					//终端和用户,车辆信息绑定
					String url_sim = Constant.BaseUrl + "device/" + device_id + "/customer?auth_code=" + Variable.auth_code;
					List<NameValuePair> paramSim = new ArrayList<NameValuePair>();
					paramSim.add(new BasicNameValuePair("cust_id",Variable.cust_id));
					new NetThread.putDataThread(handler,url_sim, paramSim, update_user).start();
				}else{
					carNewData.setObj_id(car_id);
					Variable.carDatas.add(carNewData);
					Toast.makeText(CarAddActivity.this, "车辆添加成功", Toast.LENGTH_SHORT).show();
					setResult(3);
					finish();
				}				
			}else{
				Toast.makeText(CarAddActivity.this, "添加失败", Toast.LENGTH_SHORT).show();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			setResult(3);
			finish();
		}
	}
	
	private void addCar(){
		String nick_name = et_nick_name.getText().toString().trim();
		if(nick_name.equals("")){
			Toast.makeText(CarAddActivity.this, "车辆名称不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		if(car_brand == null || car_brand.equals("")){
			Toast.makeText(CarAddActivity.this, "车型不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		String obj_name = et_obj_name.getText().toString().trim();
		String url = Constant.BaseUrl + "vehicle/simple?auth_code=" + Variable.auth_code;
		List<NameValuePair> params = new ArrayList<NameValuePair>();		

        carNewData.setObj_name(obj_name);
        carNewData.setNick_name(nick_name);
        carNewData.setCar_brand(car_brand);
        carNewData.setCar_series(car_series);
        carNewData.setCar_type(car_type);
        carNewData.setCar_brand_id(car_brand_id);
        carNewData.setCar_series_id(car_series_id);
        carNewData.setCar_type_id(car_type_id);
		
        params.add(new BasicNameValuePair("cust_id", Variable.cust_id));
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
		if(resultCode == 1){
			car_brand = data.getStringExtra("brank");
			car_brand_id = data.getStringExtra("brankId");
			car_series = data.getStringExtra("series");
			car_series_id = data.getStringExtra("seriesId");
			car_type = data.getStringExtra("type");
			car_type_id = data.getStringExtra("typeId");
            
            tv_models.setText(car_series + car_type);
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