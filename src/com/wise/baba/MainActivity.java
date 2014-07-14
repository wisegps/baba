package com.wise.baba;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.Variable;
import sql.DBExcute;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qzone.QZone;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wise.state.FaultActivity;
import com.wise.state.FaultDetectionActivity;

import customView.PopView;
import data.CarData;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
/**
 * 主界面
 * @author Administrator
 *
 */
public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	
	LinearLayout ll_car;
	ListView lv_search;
	PopupWindow mPopupWindow;
	RequestQueue mQueue;
	
	Platform platformQQ;
    Platform platformSina;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		ShareSDK.initSDK(this);
		mQueue = Volley.newRequestQueue(this);
		
		ll_car = (LinearLayout)findViewById(R.id.ll_car);
		ImageView iv_menu = (ImageView)findViewById(R.id.iv_menu);
		iv_menu.setOnClickListener(onClickListener);
		TextView tv_search = (TextView)findViewById(R.id.tv_search);
		tv_search.setOnClickListener(onClickListener);
		Button bt_service = (Button)findViewById(R.id.bt_service);
		bt_service.setOnClickListener(onClickListener);
		Button bt_car = (Button)findViewById(R.id.bt_car);
		bt_car.setOnClickListener(onClickListener);
		platformQQ = ShareSDK.getPlatform(MainActivity.this, QZone.NAME);
        platformSina = ShareSDK.getPlatform(MainActivity.this, SinaWeibo.NAME);
		//isLogin();
		showCar();
	}
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {//TODO 点击事件
			case R.id.iv_menu:
				startActivity(new Intent(MainActivity.this, MoreActivity.class));
				break;
			case R.id.tv_search:
				ShowSearchPop();
				break;
			case R.id.bt_service:
				startActivity(new Intent(MainActivity.this, ShowActivity.class));
				break;
			case R.id.bt_car:
				startActivity(new Intent(MainActivity.this, AskActivity.class));
				break;
			case R.id.ll_car:
				int i = (Integer) v.getTag();
				GetSystem.Log(TAG, "点击" + i);
				Intent intent = new Intent(MainActivity.this, FaultDetectionActivity.class);
				intent.putExtra("abc", "点击" + i);
				startActivity(intent);
				break;
			}
		}
	};	
	private void showCar() {
		ll_car.removeAllViews();		
        for (int i = 0; i < 3; i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_home_car, null);
            ll_car.addView(view);
            LinearLayout ll_item_home_car = (LinearLayout)view.findViewById(R.id.ll_car);
            ll_item_home_car.setTag(i);
            ll_item_home_car.setOnClickListener(onClickListener);
            TextView tv_number = (TextView) view.findViewById(R.id.tv_number);
            tv_number.setText("车牌：粤B9876"+ i + "  品牌：奥迪"+ i + "X");
        }
    }
	
	private void isLogin() {
        SharedPreferences preferences = getSharedPreferences(
                Constant.sharedPreferencesName, Context.MODE_PRIVATE);
        String platform = preferences.getString(Constant.platform, "");
        if(platform.equals("qq")){
        	String url = "";
        	try {
        		url = Constant.BaseUrl + "login?login_id=" + platformQQ.getDb().getUserId()
                        + "&cust_name=" + URLEncoder.encode(platformQQ.getDb().getUserName(), "UTF-8")
                        + "&province=" + URLEncoder.encode("广东", "UTF-8")
                        + "&city=" + URLEncoder.encode("深圳", "UTF-8") + "&logo="
                        + URLEncoder.encode(platformQQ.getDb().getUserIcon(), "UTF-8") + "&remark="
                        + URLEncoder.encode("remark", "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
        	
            mQueue.add(new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
				@Override
				public void onResponse(JSONObject jsonObject) {
					try {
						GetSystem.Log(TAG, jsonObject.toString());
						String status_code = jsonObject.getString("status_code");
			            if(status_code.equals("0")){
			                String auth_code = jsonObject.getString("auth_code");                
			                String cust_id = jsonObject.getString("cust_id");
			                Variable.auth_code = auth_code;
			                Variable.cust_id = cust_id;
			                
			                SharedPreferences preferences = getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
			                Editor editor = preferences.edit();
			                editor.putString(Constant.sp_cust_id, cust_id);
			                editor.putString(Constant.sp_auth_code, auth_code);
			                editor.commit();
			                GetCars();
			            }
					} catch (Exception e) {
						e.printStackTrace();
					}					
				}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					// TODO Auto-generated method stub
					
				}
			}));
        }else if(platform.equals("sina")){       
                    
        }
    }
	
	private void GetCars(){
		String url = Constant.BaseUrl + "customer/" + Variable.cust_id + "/vehicle?auth_code=" + Variable.auth_code;
		mQueue.add(new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
			@Override
			public void onResponse(JSONArray jsonArray) {
				for (int i = 0; i < jsonArray.length(); i++) {
	            	try {
	            		JSONObject jsonObject = jsonArray.getJSONObject(i);
	                    int obj_id = jsonObject.getInt("obj_id");
	                    String Cust_id = jsonObject.getString("cust_id");
	                    String obj_name = jsonObject.getString("obj_name");
	                    String car_brand = jsonObject.getString("car_brand");
	                    String car_series = jsonObject.getString("car_series");
	                    String car_type = jsonObject.getString("car_type");
	                    String engine_no = jsonObject.getString("engine_no");
	                    String frame_no = jsonObject.getString("frame_no");
	                    String insurance_company = jsonObject
	                            .getString("insurance_company");
	                    String reg_no = "";
	                    String vio_location = "";
	                    if (jsonObject.opt("reg_no") != null) {
	                        reg_no = jsonObject.getString("reg_no");
	                    }
	                    if (jsonObject.opt("vio_location") != null) {
	                        vio_location = jsonObject.getString("vio_location");
	                    }
	                    String device_id = "";
	                    if (jsonObject.opt("device_id") != null) {
	                        device_id = jsonObject.getString("device_id");
	                    }
	                    String maintain_last_date = "1970-01-01 00:00:00";
	                    if (jsonObject.opt("maintain_last_date") != null) {
	                        maintain_last_date = GetSystem.ChangeTimeZone(jsonObject
	                                .getString("maintain_last_date").replace("T", " ")
	                                .substring(0, 19));
	                    }
	                    String annual_inspect_date = GetSystem
	                            .ChangeTimeZone(jsonObject
	                                    .getString("annual_inspect_date")
	                                    .replace("T", " ").substring(0, 19));
	                    String insurance_date = GetSystem.ChangeTimeZone(jsonObject
	                            .getString("insurance_date").replace("T", " ")
	                            .substring(0, 19));
	                    String maintain_company = jsonObject
	                            .getString("maintain_company");
	                    String maintain_last_mileage = jsonObject
	                            .getString("maintain_last_mileage");
	                    String maintain_next_mileage = jsonObject
	                            .getString("maintain_next_mileage");
	                    String buy_date = GetSystem.ChangeTimeZone(jsonObject
	                            .getString("buy_date").replace("T", " ")
	                            .substring(0, 19));

	                    String car_brand_id = jsonObject.getString("car_brand_id");
	                    String car_series_id = jsonObject.getString("car_series_id");
	                    String car_type_id = jsonObject.getString("car_type_id");
	                    String vio_city_name = jsonObject.getString("vio_city_name");
	                    String insurancetel = jsonObject.getString("insurance_tel");
	                    String maintain_tel = jsonObject.getString("maintain_tel");
	                    String gas_no = jsonObject.getString("gas_no");

	                    buy_date = buy_date.substring(0, 10);

	                    CarData carData = new CarData();
	                    carData.setCheck(false);
	                    carData.setObj_id(obj_id);
	                    carData.setType(0);
	                    carData.setObj_name(obj_name);
	                    carData.setCar_brand(car_brand);
	                    carData.setCar_brand_id(car_brand_id);
	                    carData.setCar_series_id(car_series_id);
	                    carData.setCar_type_id(car_type_id);
	                    carData.setVio_city_name(vio_city_name);
	                    carData.setMaintain_tel(maintain_tel);
	                    carData.setInsurance_tel(insurancetel);
	                    carData.setGas_no(gas_no);
	                    carData.setCar_series(car_series);
	                    carData.setCar_type(car_type);
	                    carData.setEngine_no(engine_no);
	                    carData.setFrame_no(frame_no);
	                    carData.setInsurance_company(insurance_company);
	                    carData.setInsurance_date(insurance_date);
	                    carData.setAnnual_inspect_date(annual_inspect_date);
	                    carData.setMaintain_company(maintain_company);
	                    carData.setMaintain_last_mileage(maintain_last_mileage);
	                    carData.setMaintain_next_mileage(maintain_next_mileage);
	                    carData.setMaintain_last_date(maintain_last_date);
	                    carData.setBuy_date(buy_date);
	                    carData.setRegNo(reg_no);
	                    carData.setVio_location(vio_location);
	                    carData.setDevice_id(device_id);
	                    String imagePath = Constant.VehicleLogoPath + car_brand
	                            + ".png";// SD卡路径
	                    if (new File(imagePath).isFile()) {// 存在
	                        carData.setLogoPath(imagePath);
	                    } else {
	                        carData.setLogoPath("");
	                    }
	                    Variable.carDatas.add(carData);
	                    // 存储在数据库
	                    DBExcute dbExcute = new DBExcute();
	                    ContentValues values = new ContentValues();
	                    values.put("obj_id", obj_id);
	                    values.put("Cust_id", Cust_id);
	                    values.put("obj_name", obj_name);
	                    values.put("car_brand", car_brand);
	                    values.put("car_series", car_series);
	                    values.put("car_type", car_type);
	                    values.put("engine_no", engine_no);
	                    values.put("frame_no", frame_no);
	                    values.put("insurance_company", insurance_company);
	                    values.put("insurance_date", insurance_date);
	                    values.put("annual_inspect_date", annual_inspect_date);
	                    values.put("maintain_company", maintain_company);
	                    values.put("maintain_last_mileage", maintain_last_mileage);
	                    values.put("maintain_last_date", maintain_last_date);
	                    values.put("maintain_next_mileage", maintain_next_mileage);
	                    values.put("buy_date", buy_date);
	                    values.put("reg_no", reg_no);
	                    values.put("vio_location", vio_location);
	                    values.put("device_id", device_id);

	                    values.put("car_brand_id", car_brand_id);
	                    values.put("car_series_id", car_series_id);
	                    values.put("car_type_id", car_type_id);
	                    values.put("vio_city_name", vio_city_name);
	                    values.put("insurance_tel", insurancetel);
	                    values.put("maintain_tel", maintain_tel);
	                    values.put("gas_no", gas_no);
					} catch (Exception e) {
						e.printStackTrace();
					}                
	            }
				showCar();
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				
			}
		}));
	}
	
	private void testPop(){
		List<String> items = new ArrayList<String>();
		items.add("拍照");
		items.add("从手机相册中选取");
		items.add("退出");
		PopView popView = new PopView(this);
		popView.initView(findViewById(R.id.iv_menu));
		popView.setData(items);
	}
	private void ShowSearchPop() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenHeigh = dm.heightPixels;
        Rect frame = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int state_heght = frame.top;// 状态栏的高度
        int popHeight = screenHeigh - state_heght;
        
        LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popunwindwow = mLayoutInflater.inflate(R.layout.item_search,null);        
        final PopupWindow mPopupWindow = new PopupWindow(popunwindwow, LayoutParams.MATCH_PARENT,popHeight);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(false);
        mPopupWindow.showAtLocation(findViewById(R.id.ll_main),Gravity.BOTTOM, 0, 0);
        popunwindwow.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				mPopupWindow.dismiss();
			}
		});
        setData();
        lv_search = (ListView)popunwindwow.findViewById(R.id.lv_search);
        lv_search.setAdapter(new ServiceAdapter());
        lv_search.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				startActivity(new Intent(MainActivity.this, ServiceActivity.class));
			}
		});
    }
	private class ServiceAdapter extends BaseAdapter{
		LayoutInflater mInflater = LayoutInflater.from(MainActivity.this);
		@Override
		public int getCount() {
			return serviceDatas.size();
		}
		@Override
		public Object getItem(int position) {
			return serviceDatas.get(position);
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_service, null);
                holder = new ViewHolder();
                holder.tv_service_name = (TextView) convertView.findViewById(R.id.tv_service_name);                
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_service_name.setText(serviceDatas.get(position).getName());
            return convertView;
		}
		private class ViewHolder {
            TextView tv_service_name;
        }
	}
	List<ServiceData> serviceDatas = new ArrayList<ServiceData>();
	private void setData(){
		for(int i = 0 ; i < 5 ; i++){
			ServiceData serviceData = new ServiceData();
			serviceData.setName("叭叭");
			serviceDatas.add(serviceData);
		}
	}	
	private class ServiceData{
		private String name;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}		
	}
}