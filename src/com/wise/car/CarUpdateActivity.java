package com.wise.car;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import nadapter.OpenDateDialog;
import nadapter.OpenDateDialogListener;
import pubclas.Constant;
import pubclas.NetThread;
import pubclas.Variable;
import com.wise.baba.R;
import data.CarData;
import data.CityData;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 修改车辆信息
 * @author honesty
 *
 */
public class CarUpdateActivity extends Activity{
	
	private final int inspection = 1;
	private final int buy_date = 2;
	private final int year_check = 3;
	private final int update = 4;

	EditText et_nick_name,et_obj_name,et_engine_no,et_frame_no,et_regist_no,
			et_insurance_tel,et_insurance_no,et_maintain_tel;
	TextView tv_models,tv_gas_no,tv_city,tv_insurance_company,tv_insurance_date,tv_maintain_company,
			tv_buy_date,tv_year_check;
	CarData carData;
	List<CityData> chooseCityDatas = new ArrayList<CityData>();

	String car_brand;
	String car_brand_id;
	String car_series;
	String car_series_id;
	String car_type;
	String car_type_id;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_update_car);
		int index = getIntent().getIntExtra("index", 0);
		carData = Variable.carDatas.get(index);
		init();
		setData();
		setTime();
	}
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.iv_save:
				Save();
				break;
			case R.id.tv_models:
				startActivityForResult(new Intent(CarUpdateActivity.this, ModelsActivity.class),2);
				break;
			case R.id.tv_city:
				startActivityForResult(new Intent(CarUpdateActivity.this, TrafficCitiyActivity.class),2);
				break;
			case R.id.tv_gas_no:
				startActivityForResult(new Intent(CarUpdateActivity.this, PetrolGradeActivity.class),2);
				break;
			case R.id.tv_insurance_company:
				startActivityForResult(new Intent(CarUpdateActivity.this, InsuranceActivity.class),2);
				break;
			case R.id.tv_maintain_company:
				Intent intent = new Intent(CarUpdateActivity.this, MaintainShopActivity.class);
				intent.putExtra("city", "深圳");
				intent.putExtra("brank", "大众");
				startActivityForResult(intent,2);
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
			}
		}
	};
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case update:
				jsonSave(msg.obj.toString());
				break;
			}
		}		
	};
	private void ShowDate(int index) {
        OpenDateDialog.ShowDate(CarUpdateActivity.this, index);
    }
	private void jsonSave(String str){
		System.out.println(str);
		setResult(3);
		finish();
	}
	private void Save(){
		String nick_name = et_nick_name.getText().toString();
		if(nick_name.equals("")){
			Toast.makeText(CarUpdateActivity.this, "爱车名称不能为空", Toast.LENGTH_SHORT).show();
			return;
		}

		String engine_no = et_engine_no.getText().toString();
		String frame_no = et_frame_no.getText().toString();
		String regist_no = et_regist_no.getText().toString();
		
		for(CityData cityData : chooseCityDatas){
			System.out.println(cityData.toString());
			//发送机号
			if(cityData.getEngine() == 0){
				
			}else{
				if(cityData.getEngineno() == 0){//全部
					if(engine_no.length() == 0){
						Toast.makeText(CarUpdateActivity.this, "需要完整的发送机号", Toast.LENGTH_SHORT).show();
						break;
					}
				}else{
					if(engine_no.length() < cityData.getEngineno()){
						Toast.makeText(CarUpdateActivity.this, "需要发送机号的" +cityData.getEngineno()+"位", Toast.LENGTH_SHORT).show();
						break;
					}
				}
			}
			//车架号
			if(cityData.getFrame() == 0){
				
			}else{
				if(cityData.getFrameno() == 0){//全部
					if(frame_no.length() == 0){
						Toast.makeText(CarUpdateActivity.this, "需要完整的车架号", Toast.LENGTH_SHORT).show();
						break;
					}
				}else{
					if(frame_no.length() < cityData.getFrameno()){
						Toast.makeText(CarUpdateActivity.this, "需要车架号的" +cityData.getFrameno()+"位", Toast.LENGTH_SHORT).show();
						break;
					}
				}
			}
			//登记证号
			if(cityData.getRegist() == 0){
				
			}else{
				if(cityData.getRegistno() == 0){//全部
					if(regist_no.length() == 0){
						Toast.makeText(CarUpdateActivity.this, "需要完整的登记证号", Toast.LENGTH_SHORT).show();
						break;
					}
				}else{
					if(regist_no.length() < cityData.getRegistno()){
						Toast.makeText(CarUpdateActivity.this, "需要登记证号的" +cityData.getRegistno()+"位", Toast.LENGTH_SHORT).show();
						break;
					}
				}
			}
		}
		
		String obj_name = et_obj_name.getText().toString();
		String gas_no = tv_gas_no.getText().toString();
		String insurance_company = tv_insurance_company.getText().toString();
		String insurance_tel = et_insurance_tel.getText().toString();
		String insurance_date = tv_insurance_date.getText().toString();
		String insurance_no = et_insurance_no.getText().toString();
		String maintain_company = tv_maintain_company.getText().toString();
		String maintain_tel = et_maintain_tel.getText().toString();
		String buy_date = tv_buy_date.getText().toString();
		String year_check = tv_year_check.getText().toString();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("obj_name", obj_name));
        params.add(new BasicNameValuePair("nick_name", nick_name));
        params.add(new BasicNameValuePair("car_brand", car_brand));
        params.add(new BasicNameValuePair("car_series", car_series));
        params.add(new BasicNameValuePair("car_type", car_type));
        params.add(new BasicNameValuePair("vio_citys", jsonList(chooseCityDatas)));
        params.add(new BasicNameValuePair("engine_no", engine_no));
        params.add(new BasicNameValuePair("frame_no", frame_no));
        params.add(new BasicNameValuePair("reg_no", regist_no));
        params.add(new BasicNameValuePair("insurance_company", insurance_company));
        params.add(new BasicNameValuePair("insurance_tel", insurance_tel));
        params.add(new BasicNameValuePair("insurance_date", insurance_date));
        params.add(new BasicNameValuePair("insurance_no", insurance_no));
        params.add(new BasicNameValuePair("maintain_company", maintain_company));
        params.add(new BasicNameValuePair("maintain_tel", maintain_tel));
        params.add(new BasicNameValuePair("maintain_last_mileage", "100"));
        params.add(new BasicNameValuePair("maintain_last_date", "2014-10-10"));
        params.add(new BasicNameValuePair("buy_date", buy_date));
        params.add(new BasicNameValuePair("gas_no", gas_no));
        params.add(new BasicNameValuePair("car_brand_id", car_brand_id));
        params.add(new BasicNameValuePair("car_series_id", car_series_id));
        params.add(new BasicNameValuePair("car_type_id", car_type_id));
    	//TODO 保存
        String url = Constant.BaseUrl + "vehicle/" + carData.getObj_id() + "?auth_code=" + Variable.auth_code;
        new Thread(new NetThread.putDataThread(handler, url, params, update)).start();
	}
	private void setTime(){
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
	private void setData(){
		System.out.println(carData.toString());
		car_brand = carData.getCar_brand();
		car_brand_id = carData.getCar_brand_id();
		car_series = carData.getCar_series();
		car_series_id = carData.getCar_series_id();
		car_type = carData.getCar_type();
		car_type_id = carData.getCar_type_id();
		
		et_nick_name.setText(carData.getNick_name());
		et_obj_name.setText(carData.getObj_name());
		tv_models.setText(carData.getCar_series() + carData.getCar_type());
		tv_gas_no.setText(carData.getGas_no());
		
		et_engine_no.setText(carData.getEngine_no());
		et_frame_no.setText(carData.getFrame_no());
		tv_insurance_company.setText(carData.getInsurance_company());
		et_insurance_tel.setText(carData.getInsurance_tel());
		tv_insurance_date.setText(carData.getInsurance_date());
		et_insurance_no.setText("");
		tv_maintain_company.setText(carData.getMaintain_company());
		et_maintain_tel.setText(carData.getMaintain_tel());
		tv_buy_date.setText(carData.getBuy_date());
		tv_year_check.setText(carData.getAnnual_inspect_date());
	}
	private void init(){
		ImageView iv_save = (ImageView)findViewById(R.id.iv_save);
		iv_save.setOnClickListener(onClickListener);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		et_nick_name = (EditText)findViewById(R.id.et_nick_name);
		et_obj_name = (EditText)findViewById(R.id.et_obj_name);
		tv_models = (TextView)findViewById(R.id.tv_models);
		tv_models.setOnClickListener(onClickListener);
		tv_gas_no = (TextView)findViewById(R.id.tv_gas_no);
		tv_gas_no.setOnClickListener(onClickListener);
		tv_city = (TextView)findViewById(R.id.tv_city);
		tv_city.setOnClickListener(onClickListener);
		et_engine_no = (EditText)findViewById(R.id.et_engine_no);
		et_frame_no = (EditText)findViewById(R.id.et_frame_no);
		et_regist_no = (EditText)findViewById(R.id.et_regist_no);
		tv_insurance_company = (TextView)findViewById(R.id.tv_insurance_company);
		tv_insurance_company.setOnClickListener(onClickListener);
		et_insurance_tel = (EditText)findViewById(R.id.et_insurance_tel);
		tv_insurance_date = (TextView)findViewById(R.id.tv_insurance_date);
		tv_insurance_date.setOnClickListener(onClickListener);
		et_insurance_no = (EditText)findViewById(R.id.et_insurance_no);
		tv_maintain_company = (TextView)findViewById(R.id.tv_maintain_company);
		tv_maintain_company.setOnClickListener(onClickListener);
		et_maintain_tel = (EditText)findViewById(R.id.et_maintain_tel);
		tv_buy_date = (TextView)findViewById(R.id.tv_buy_date);
		tv_buy_date.setOnClickListener(onClickListener);
		tv_year_check = (TextView)findViewById(R.id.tv_year_check);
		tv_year_check.setOnClickListener(onClickListener);
	}
	private String jsonList(List<CityData> chooseCityDatas){
		try {
			JSONArray jsonArray = new JSONArray();
			for(int i = 0 ; i < chooseCityDatas.size() ; i++){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("vio_city_name", chooseCityDatas.get(i).getCityName());
				jsonObject.put("vio_location", chooseCityDatas.get(i).getCityCode());
				jsonArray.put(jsonObject);
			}
			String jsonString = jsonArray.toString().replaceAll("\"vio_city_name\":", "vio_city_name:").replaceAll("\"vio_location\":", "vio_location:");
			return jsonString;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "[]";
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == 1){//汽车型号
			car_brand = data.getStringExtra("brank");
			car_brand_id = data.getStringExtra("brankId");
			car_series = data.getStringExtra("series");
            car_series_id = data.getStringExtra("seriesId");
            car_type = data.getStringExtra("type");
            car_type_id = data.getStringExtra("typeId");            
            tv_models.setText(car_series + car_type);
		}else if(resultCode == 2){//违章城市返回
			chooseCityDatas = (List<CityData>)data.getSerializableExtra("cityDatas");
			String city = "";
			for(CityData cityData : chooseCityDatas){
				city += cityData.getCityName() + " " ;
			}
			tv_city.setText(city);
			jsonList(chooseCityDatas);
		}else if(resultCode == 3){//汽油标号返回
			tv_gas_no.setText(data.getStringExtra("result"));
		}else if(resultCode == 4){//保险公司返回
			tv_insurance_company.setText(data.getStringExtra("insurance_name"));
			et_insurance_tel.setText(data.getStringExtra("insurance_phone"));
		}else if(resultCode == 5){//4s店返回
			tv_maintain_company.setText(data.getStringExtra("maintain_name"));
			et_maintain_tel.setText(data.getStringExtra("maintain_phone"));
		}
	}
}