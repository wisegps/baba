package com.wise.car;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import pubclas.Constant;
import pubclas.NetThread;
import pubclas.Variable;
import com.wise.baba.R;
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
	TextView tv_models;
	EditText et_nick_name,et_obj_name;
	
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
				System.out.println(msg.obj.toString());
				break;

			default:
				break;
			}
		}		
	};
	
	private void addCar(){
		String nick_name = et_nick_name.getText().toString().trim();
		if(nick_name.equals("")){
			Toast.makeText(CarAddActivity.this, "车辆名称不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		if(carBrank.equals("")){
			Toast.makeText(CarAddActivity.this, "车型不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		String obj_name = et_obj_name.getText().toString().trim();
		String url = Constant.BaseUrl + "vehicle/simple?auth_code=" + Variable.auth_code;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("cust_id", Variable.cust_id));
        params.add(new BasicNameValuePair("obj_name", obj_name));
        params.add(new BasicNameValuePair("nick_name", nick_name));
        params.add(new BasicNameValuePair("car_brand", carBrank));
        params.add(new BasicNameValuePair("car_series", carSeries));
        params.add(new BasicNameValuePair("car_type", carType));
        params.add(new BasicNameValuePair("car_brand_id", carBrankId));
        params.add(new BasicNameValuePair("car_series_id", carSeriesId));
        params.add(new BasicNameValuePair("car_type_id", carTypeId));
        new Thread(new NetThread.postDataThread(handler, url, params, add_car)).start();
	}
	String carBrank;
	String carBrankId;
	String carSeries;
	String carSeriesId;
	String carType;
	String carTypeId;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == 1){
			carBrank = data.getStringExtra("brank");
			carBrankId = data.getStringExtra("brankId");
            carSeries = data.getStringExtra("series");
            carSeriesId = data.getStringExtra("seriesId");
            carType = data.getStringExtra("type");
            carTypeId = data.getStringExtra("typeId");
            
            tv_models.setText(carSeries + carType);
		}
	}
}