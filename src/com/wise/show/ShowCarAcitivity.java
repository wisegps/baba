package com.wise.show;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;
import com.aliyun.android.oss.task.PutObjectTask;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetLocation;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.entity.BaseData;
import com.wise.baba.net.NetThread;
import com.wise.baba.util.Blur;
import com.wise.car.ModelsActivity;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**秀我的爱车**/
public class ShowCarAcitivity extends Activity{
	private static final String TAG = "ShowCarAcitivity";
	private static final int putOss = 1;
	private static final int postCloudStorage = 2;
	private static final int getBrand = 3;
	
	int photo_type = 1;
	Bitmap bitmap;
	EditText et_content;
	TextView tv_adress,tv_series;
	String content = "";
	String city = "";
	String Lat = "";
	String Lon = "";
	String AddrStr = "";
	ProgressDialog progressDialog = null;
	AppApplication app;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_show_mycar);
		app = (AppApplication)getApplication();
		//照相机拍照，传过来的是SD卡路径
		DisplayMetrics metrics=new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int widthPixels=metrics.widthPixels;
		int heightPixels=metrics.heightPixels;
		int newWidth = widthPixels > heightPixels ? heightPixels : widthPixels;	
		photo_type = getIntent().getIntExtra("photo_type", 1);
		String image = getIntent().getStringExtra("image");
		bitmap = Blur.decodeSampledBitmapFromPath(image, newWidth, newWidth);
		
		ImageView iv_car = (ImageView)findViewById(R.id.iv_car);
		iv_car.setImageBitmap(bitmap);
		ImageView iv_add = (ImageView)findViewById(R.id.iv_add);
		iv_add.setOnClickListener(onClickListener);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		tv_adress = (TextView)findViewById(R.id.tv_adress);
		tv_series = (TextView)findViewById(R.id.tv_series);
		tv_series.setOnClickListener(onClickListener);
		et_content = (EditText)findViewById(R.id.et_content);
		getLocation();
		getDefaultCarSeries();
	} 
	
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_add:
				checkContent();
				break;
			case R.id.iv_back:
				finish();
				break;
			case R.id.tv_series:
				Intent intent = new Intent(ShowCarAcitivity.this, ModelsActivity.class);
				intent.putExtra("isNeedType", false);
				startActivityForResult(intent, 2);
				break;
			}
		}
	};
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case putOss:
				saveInCloudStorage();
				break;
			case postCloudStorage:
				if(progressDialog != null){
                	progressDialog.dismiss();
                }
				jsonSaveResult(msg.obj.toString());
				break;
			case getBrand:
				//保存到数据库
				BaseData baseData = new BaseData();
				baseData.setTitle("carBrank");
				baseData.setContent("msg.obj.toString()");
				baseData.save();
				result = msg.obj.toString();
				jsonBrand(msg.obj.toString());
				break;
			}
		}		
	};
	private void getLocation(){
		registerBroadcastReceiver();
        new GetLocation(ShowCarAcitivity.this);
	}
	private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.A_City);
        registerReceiver(broadcastReceiver, intentFilter);
    }
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Constant.A_City)) {
				city = intent.getStringExtra("City");
				Lat = intent.getStringExtra("Lat");
				Lon = intent.getStringExtra("Lon");
				AddrStr = intent.getStringExtra("AddrStr");
				tv_adress.setText(AddrStr);
            }
		}		
	};
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(broadcastReceiver);
	}
	
	private void checkContent(){
		content = et_content.getText().toString().trim();
		if(content.equals("")){
			Toast.makeText(ShowCarAcitivity.this, "描述不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		if(carBrankId.equals("")){
			Toast.makeText(ShowCarAcitivity.this, "车辆品牌不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		SavaImage();
	}
	
	String small_pic;
	String big_pic;
	
	private void SavaImage(){
		if(progressDialog == null){
    		progressDialog = ProgressDialog.show(ShowCarAcitivity.this, getString(R.string.dialog_title), "爱车上传中");
    		progressDialog.setCancelable(true);
    	}
        new Thread(new Runnable() {
			@Override
			public void run() {
				//判断文件夹是否为空
				File filePath = new File(Constant.VehiclePath);
		        if (!filePath.exists()) {
					filePath.mkdirs();
				}
		        
				//获取手机分辨率,选出最小的
				DisplayMetrics metrics=new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(metrics);
				int widthPixels=metrics.widthPixels;
				int heightPixels=metrics.heightPixels;
				int newWidth = widthPixels > heightPixels ? heightPixels : widthPixels;
				
				//设置大图形和小图像的名称
				small_pic = app.cust_id + System.currentTimeMillis() + ".png";
				big_pic = app.cust_id + (System.currentTimeMillis() + 1) + ".png";
				
				//存大图像
				bitmap = Blur.scaleImage(bitmap, newWidth);
								
				FileOutputStream bigOutputStream = null;        
		        final String bigFile = Constant.VehiclePath + big_pic;
		        try {
		        	bigOutputStream = new FileOutputStream(bigFile);
		            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bigOutputStream);// 把数据写入文件
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
		        
		        //存小图像
				bitmap = Blur.scaleImage(bitmap, newWidth/3);
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
				
				//上传大图图片到阿里云
				PutObjectTask bigTask = new PutObjectTask(Constant.oss_path, big_pic, "image/jpg", bigFile,Constant.oss_accessId, Constant.oss_accessKey);
				bigTask.getResult();
				//上传小图图图片到阿里云
				PutObjectTask smallTask = new PutObjectTask(Constant.oss_path, small_pic, "image/jpg", smallFile,Constant.oss_accessId, Constant.oss_accessKey);
				smallTask.getResult();
				
				Message message = new Message();
				message.what = putOss;
				handler.sendMessage(message);
			}
		}).start();
		
		
	}
		
	public static void compressBmpToFile(Bitmap bmp,File file){  
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        int options = 80;//个人喜欢从80开始,  
        bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);  
        while (baos.toByteArray().length / 1024 > 100) {   
            baos.reset();  
            options -= 10;  
            bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);  
        }  
        try {  
            FileOutputStream fos = new FileOutputStream(file);  
            fos.write(baos.toByteArray());  
            fos.flush();  
            fos.close();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }
	
	private void jsonSaveResult(String result){
		GetSystem.myLog(TAG, result);
		finish();
	}
	private void saveInCloudStorage(){		
		String logo = "";
		String sex = "0";
		SharedPreferences preferences = getSharedPreferences(
				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		String customer = preferences.getString(Constant.sp_customer + app.cust_id, "");
		try {
			JSONObject jsonObject = new JSONObject(customer);
			logo = jsonObject.getString("logo");
			sex = jsonObject.getString("sex");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/**在阿里云上对应的图片url**/
		String big_pic_url = Constant.oss_url + big_pic;
		String small_pic_url = Constant.oss_url + small_pic;
		
		String url = Constant.BaseUrl + "photo?auth_code=" + app.auth_code;
		//上传数据到服务器
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("cust_id", app.cust_id));
		params.add(new BasicNameValuePair("cust_name", app.cust_name));
		params.add(new BasicNameValuePair("icon", logo));
		params.add(new BasicNameValuePair("sex", sex));
		params.add(new BasicNameValuePair("car_brand_id", carBrankId));
		params.add(new BasicNameValuePair("brand_logo_url", LogUrl));
		params.add(new BasicNameValuePair("car_series_id", carSeriesId));
		//如果车型里没有品牌，则加上品牌
		params.add(new BasicNameValuePair("car_series", carSeries.indexOf(carBrank) >= 0 ? carSeries : (carBrank + carSeries)));
		params.add(new BasicNameValuePair("small_pic_url", small_pic_url));
		params.add(new BasicNameValuePair("big_pic_url", big_pic_url));
		params.add(new BasicNameValuePair("content", content));
		params.add(new BasicNameValuePair("photo_type", ""+ photo_type));
		params.add(new BasicNameValuePair("lon", Lon));
		params.add(new BasicNameValuePair("lat", Lat));
		params.add(new BasicNameValuePair("city", AddrStr));
		new NetThread.postDataThread(handler, url, params, postCloudStorage).start();
	}	
	
	/**获取默认车型**/
	private void getDefaultCarSeries(){
		List<BaseData> baseDatas = DataSupport.where("Title = ?","carBrank").find(BaseData.class);
		if(baseDatas.size() == 0 || baseDatas.get(0).getContent() == null || baseDatas.get(0).getContent().equals("")){
			new NetThread.GetDataThread(handler, Constant.BaseUrl + "base/car_brand",getBrand).start();
		}else{
			jsonBrand(baseDatas.get(0).getContent());
		}	
	}
	
	private void jsonBrand(String result){
		//先获取个人信息里是否有选车型(取数据麻烦)
		boolean isCustomer = false;
//		SharedPreferences preferences = getSharedPreferences(
//				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
//		String customer = preferences.getString(Constant.sp_customer + Variable.cust_id, "");
//		try {
//			JSONObject jsonObject = new JSONObject(customer);			
//			if(jsonObject.opt("status_code") == null){
//				if(jsonObject.opt("car_brand") == null || jsonObject.opt("car_series") == null){
//					isCustomer = false;
//				}else{
//					String car_brand = jsonObject.getString("car_brand");
//					carSeries = jsonObject.getString("car_series");//车型
//					if(car_brand.equals("") || carSeries.equals("")){
//						isCustomer = false;
//					}else{
//						JSONObject jsonObject2 = getJsonObjectFromJsonArray(result,car_brand);
//						carBrankId = jsonObject2.getString("id");//品牌id;
//						LogUrl = Constant.BaseUrl +"/logo" + jsonObject2.getString("url_icon");//品牌logo地址
//					}
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		if(!isCustomer){
			//获取用户下车辆的车型
			if(app.carDatas != null || app.carDatas.size() != 0){
				try {
					String car_brand = app.carDatas.get(0).getCar_brand();
					carBrankId = app.carDatas.get(0).getCar_brand_id();
					carSeriesId = app.carDatas.get(0).getCar_series_id();
					carSeries = app.carDatas.get(0).getCar_series();
					carBrank = app.carDatas.get(0).getCar_brand();
					tv_series.setText(carSeries);
					JSONObject jsonObject2 = getJsonObjectFromJsonArray(result,car_brand);
					LogUrl = Constant.BaseUrl +"logo/" + jsonObject2.getString("url_icon");//品牌logo地址
				} catch (Exception e) {
					e.printStackTrace();
				}				
			}
		}
	}
	/**返回对应的JsonObject**/
	private JSONObject getJsonObjectFromJsonArray(String result,String car_brand){
		try {
			JSONArray jsonArray = new JSONArray(result);
			for(int i = 0 ; i < jsonArray.length() ; i++){
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				if(jsonObject.getString("name").equals(car_brand)){
					return jsonObject;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	String result = "";
	String carBrank = "";
	String carSeries = "";
	String carBrankId = "";
	String carSeriesId = "";
	String LogUrl = "";
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == 1){
			carBrank = data.getStringExtra("brank");
			carBrankId = data.getStringExtra("brankId");
            carSeries = data.getStringExtra("series");
            carSeriesId = data.getStringExtra("seriesId");            
            tv_series.setText(carSeries);
            try {
            	JSONObject jsonObject2 = getJsonObjectFromJsonArray(result,carBrank);
				LogUrl = Constant.BaseUrl +"/logo" + jsonObject2.getString("url_icon");//品牌logo地址
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}	
}