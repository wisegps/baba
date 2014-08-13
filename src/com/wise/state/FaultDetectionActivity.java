package com.wise.state;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pubclas.Constant;
import pubclas.NetThread;
import pubclas.Variable;
import com.umeng.analytics.MobclickAgent;
import com.wise.baba.R;
import com.wise.car.DevicesAddActivity;

import customView.FaultDeletionView;
import customView.OnViewChangeListener;
import data.CarData;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View.OnClickListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
/**
 * 车况检测
 * @author honesty
 *
 */
public class FaultDetectionActivity extends Activity{
	
	private static final int getData = 1;
	private static final int refresh = 2;
	private static final int getFault = 3;
	/**刷新分数**/
	private static final int refresh_score = 4;
	/**获取限行**/
	private static final int Get_carLimit = 5;
	TextView tv_name;
	TextView tv_guzhang ,tv_guzhang_icon , tv_dianyuan,tv_dianyuan_icon,
				tv_jinqi,tv_jinqi_icon, tv_daisu,tv_daisu_icon,
				tv_lengque,tv_lengque_icon,tv_paifang,tv_paifang_icon;
	RelativeLayout rl_no_device;
	LinearLayout ll_fault;
	ImageView iv_left,iv_right;
	
	private int mTotalProgress = 100;
	private int mCurrentProgress = 100;
	
	private static final int Point = 6;
	int Interval  = 0;
	String fault_content = "";
	int index;
	
	FaultDeletionView hs_car;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_fault_detection);
		initView();
		rl_no_device = (RelativeLayout)findViewById(R.id.rl_no_device);
		ll_fault = (LinearLayout)findViewById(R.id.ll_fault);
		iv_right = (ImageView)findViewById(R.id.iv_right);
		iv_right.setOnClickListener(onClickListener);
		iv_left = (ImageView)findViewById(R.id.iv_left);
		iv_left.setOnClickListener(onClickListener);
		hs_car = (FaultDeletionView) findViewById(R.id.hs_car);
		hs_car.setOnViewChangeListener(new OnViewChangeListener() {			
			@Override
			public void OnViewChange(int view) {
				//关闭线程
				mCurrentProgress = -1;
				index = view;
				fristSetLeftRight();
				initViewIsDevice();
				getSpHistoryData(index);
			}			
			@Override
			public void OnLastView() {}
			@Override
			public void OnFinish(int index) {}
		});
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		index = getIntent().getIntExtra("index", 0);

		fristSetLeftRight();
		initDataView();	
		handler.postDelayed(new Runnable() {			
			@Override
			public void run() {
				hs_car.snapFastToScreen(index);
				getSpHistoryData(index);
				initViewIsDevice();
			}
		}, 50);
	}
	
	private void initViewIsDevice(){
		if(Variable.carDatas != null && Variable.carDatas.size() != 0){
			String Device_id = Variable.carDatas.get(index).getDevice_id();
			if (Device_id == null || Device_id.equals("")) {
				rl_no_device.setVisibility(View.VISIBLE);
				ll_fault.setVisibility(View.INVISIBLE);
			}else{
				rl_no_device.setVisibility(View.GONE);
				ll_fault.setVisibility(View.VISIBLE);
			}
		}
	}
	
	private void fristSetLeftRight(){
		if(Variable.carDatas.size() == 1){
			iv_left.setVisibility(View.GONE);
			iv_right.setVisibility(View.GONE);
		}else if(index == 0){
			iv_left.setVisibility(View.GONE);
			iv_right.setVisibility(View.VISIBLE);
		}else if(index == (Variable.carDatas.size() - 1)){
			iv_left.setVisibility(View.VISIBLE);
			iv_right.setVisibility(View.GONE);
		}else{
			iv_left.setVisibility(View.VISIBLE);
			iv_right.setVisibility(View.VISIBLE);
		}
	}
	
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			if(v.getId() == R.id.iv_left){
				iv_right.setVisibility(View.VISIBLE);
				if(index != 0){
					index --;
					hs_car.snapToScreen(index);
					if(index == 0){
						iv_left.setVisibility(View.GONE);
					}
				}
			}else if(v.getId() == R.id.iv_right){
				iv_left.setVisibility(View.VISIBLE);
				if(index != (Variable.carDatas.size() - 1)){
					index ++ ;
					hs_car.snapToScreen(index);
					if(index == (Variable.carDatas.size() - 1)){
						iv_right.setVisibility(View.GONE);
					}
				}
			}else if(v.getId() == R.id.tasks_view){
				getData(index);
			}else if(v.getId() == R.id.iv_back){
				Back();
				finish();
			}else{
				try {
					Intent intent = new Intent(FaultDetectionActivity.this, DyActivity.class);
					JSONObject jsonObject = new JSONObject(result);
					switch (v.getId()) {
					case R.id.rl_guzhang:
						Intent intent1 = new Intent(FaultDetectionActivity.this, FaultDetailActivity.class);
						intent1.putExtra("fault_content", fault_content);
						startActivity(intent1);
						break;
					case R.id.rl_dianyuan:
						intent.putExtra("title", "电源系统");
						intent.putExtra("name", "蓄电池电压");
						intent.putExtra("range", jsonObject.getString("dpdy_range"));
						intent.putExtra("if_err", !jsonObject.getBoolean("if_dpdy_err"));
						intent.putExtra("current", String.format("%.2f",jsonObject.getDouble("dpdy")));
						intent.putExtra("if_lt_err", !jsonObject.getBoolean("if_lt_dpdy_err"));
						intent.putExtra("lt", String.format("%.2f",jsonObject.getDouble("lt_dpdy")));
						intent.putExtra("url", jsonObject.getString("dpdy_content"));
						startActivity(intent);
						break;
					case R.id.rl_jinqi:						
						intent.putExtra("title", "进气系统");
						intent.putExtra("name", "节气门开度");
						intent.putExtra("range", jsonObject.getString("jqmkd_range"));
						intent.putExtra("if_err", !jsonObject.getBoolean("if_jqmkd_err"));
						intent.putExtra("current", String.format("%.2f",jsonObject.getDouble("jqmkd")));
						intent.putExtra("if_lt_err", !jsonObject.getBoolean("if_lt_jqmkd_err"));
						intent.putExtra("lt", String.format("%.2f",jsonObject.getDouble("lt_jqmkd")));
						intent.putExtra("url", jsonObject.getString("jqmkd_content"));
						startActivity(intent);
						break;
					case R.id.rl_daisu:
						intent.putExtra("title", "怠速控制系统");
						intent.putExtra("name", "怠速状态");
						intent.putExtra("range", jsonObject.getString("fdjzs_range"));
						intent.putExtra("if_err", !jsonObject.getBoolean("if_fdjzs_err"));
						intent.putExtra("current", String.format("%.2f",jsonObject.getDouble("fdjzs")));
						intent.putExtra("if_lt_err", !jsonObject.getBoolean("if_lt_fdjzs_err"));
						intent.putExtra("lt", String.format("%.2f",jsonObject.getDouble("lt_fdjzs")));
						intent.putExtra("url", jsonObject.getString("fdjzs_content"));
						startActivity(intent);  
						break;
					case R.id.rl_lengque:
						intent.putExtra("title", "冷却系统");
						intent.putExtra("name", "水温状态");
						intent.putExtra("range", jsonObject.getString("sw_range"));
						intent.putExtra("if_err", !jsonObject.getBoolean("if_sw_err"));
						intent.putExtra("current", String.format("%.2f",jsonObject.getDouble("sw")));
						intent.putExtra("if_lt_err", !jsonObject.getBoolean("if_lt_sw_err"));
						intent.putExtra("lt", String.format("%.2f",jsonObject.getDouble("lt_sw")));
						intent.putExtra("url", jsonObject.getString("sw_content"));
						startActivity(intent);
						break;
					case R.id.rl_paifang:
						intent.putExtra("title", "排放系统");
						intent.putExtra("name", "三元催化剂状态");
						intent.putExtra("range", jsonObject.getString("chqwd_range"));
						intent.putExtra("if_err", !jsonObject.getBoolean("if_chqwd_err"));
						intent.putExtra("current", String.format("%.2f",jsonObject.getDouble("chqwd")));
						intent.putExtra("if_lt_err", !jsonObject.getBoolean("if_lt_chqwd_err"));
						intent.putExtra("lt", String.format("%.2f",jsonObject.getDouble("lt_chqwd")));
						intent.putExtra("url", jsonObject.getString("chqwd_content"));
						startActivity(intent);
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}						
		}
	};
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case getData:
				result = msg.obj.toString();
				jsonHealth(msg.obj.toString());
				new Thread(new ProgressRunable(msg.arg1)).start();
				break;
			case refresh:
				refreshHealth(msg.arg1);
				break;
			case getFault:
				fault_content = msg.obj.toString();
				break;
			case refresh_score:				
				carViews.get(msg.arg1).getTv_score().setText(String.valueOf(mCurrentProgress));
				break;
			case Get_carLimit:
				jsonCarLinit(msg.obj.toString(),msg.arg1);
				break;
			}
		}		
	};
	List<CarView> carViews = new ArrayList<CarView>();
	
	/** 滑动车辆布局 **/
	private void initDataView() {
		SharedPreferences preferences = getSharedPreferences(
				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		hs_car.removeAllViews();
		for (int i = 0; i < Variable.carDatas.size(); i++) {
			View v = LayoutInflater.from(this).inflate(R.layout.item_fault_detection,null);
			hs_car.addView(v);
			TasksCompletedView mTasksView = (TasksCompletedView) v.findViewById(R.id.tasks_view);
			mTasksView.setOnClickListener(onClickListener);
			TextView tv_score = (TextView) v.findViewById(R.id.tv_score);
			TextView tv_xx_detection = (TextView) v.findViewById(R.id.tv_xx_detection);			

			CarView carView = new CarView();
			carView.setmTasksView(mTasksView);
			carView.setTv_score(tv_score);
			carView.setTv_xx_detection(tv_xx_detection);
			carViews.add(carView);


			tv_name.setText(Variable.carDatas.get(i).getCar_series() + "("
					+ Variable.carDatas.get(i).getNick_name() + ")");
			String result = preferences.getString(Constant.sp_health_score
					+ Variable.carDatas.get(i).getObj_id(), "");
			if (result.equals("")) {// 未体检过
				carView.getmTasksView().setProgress(100);
				tv_score.setText("0");
			} else {
				try {
					JSONObject jsonObject = new JSONObject(result);
					//健康指数
					int health_score = jsonObject.getInt("health_score");
					carView.getmTasksView().setProgress(health_score);
					tv_score.setText(String.valueOf(health_score));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	/**获取历史消息**/
	private void getSpHistoryData(int index){
		if(Variable.carDatas == null || Variable.carDatas.size() == 0){
			return;
		}
		CarData carData = Variable.carDatas.get(index);
		tv_name.setText(carData.getNick_name());
		if(carData.getLimit() == null){
			//获取限行信息
			if(Variable.City == null || carData.getObj_name() == null || Variable.City.equals("") || carData.getObj_name().equals("")){
				
			}else{
				try {
					String url = Constant.BaseUrl + "base/ban?city="
		                    + URLEncoder.encode(Variable.City, "UTF-8")
		                    + "&obj_name=" + URLEncoder.encode(carData.getObj_name(), "UTF-8");
		            new NetThread.GetDataThread(handler, url,Get_carLimit,index).start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}			
		}else{
			carViews.get(index).getTv_xx_detection().setText(carData.getLimit());
		}
		SharedPreferences preferences = getSharedPreferences(
				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		String result = preferences.getString(Constant.sp_health_score
				+ Variable.carDatas.get(index).getObj_id(), "");
		if (result.equals("")) {// 未体检过
			carViews.get(index).getmTasksView().setProgress(100);
			carViews.get(index).getTv_score().setText(String.valueOf(0));
			
			tv_guzhang.setText("无故障码");
			tv_guzhang.setTextColor(getResources().getColor(R.color.blue_press));
			Drawable drawable= getResources().getDrawable(R.drawable.icon_guzhang_normal);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			tv_guzhang_icon.setCompoundDrawables(drawable,null,null,null);
			
			tv_dianyuan.setText("蓄电池状态良好");
			tv_dianyuan.setTextColor(getResources().getColor(R.color.blue_press));
			drawable= getResources().getDrawable(R.drawable.icon_dianyuan_normal);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			tv_dianyuan_icon.setCompoundDrawables(drawable,null,null,null);
			
			tv_jinqi.setText("节气门开度良好");
			tv_jinqi.setTextColor(getResources().getColor(R.color.blue_press));
			drawable= getResources().getDrawable(R.drawable.icon_jinqi_normal);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			tv_jinqi_icon.setCompoundDrawables(drawable,null,null,null);
			
			tv_daisu.setText("怠速稳定");
			tv_daisu.setTextColor(getResources().getColor(R.color.blue_press));
			drawable= getResources().getDrawable(R.drawable.icon_daisu_normal);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			tv_daisu_icon.setCompoundDrawables(drawable,null,null,null);
			
			tv_lengque.setText("水温正常");
			tv_lengque.setTextColor(getResources().getColor(R.color.blue_press));
			drawable= getResources().getDrawable(R.drawable.icon_lengque_normal);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			tv_lengque_icon.setCompoundDrawables(drawable,null,null,null);
			
			tv_paifang.setText("三元催化剂状态良好");
			tv_paifang.setTextColor(getResources().getColor(R.color.blue_press));
			drawable= getResources().getDrawable(R.drawable.icon_paifang_normal);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			tv_paifang_icon.setCompoundDrawables(drawable,null,null,null);
			
		} else {
			try {
				JSONObject jsonObject = new JSONObject(result);
				//健康指数
				int health_score = jsonObject.getInt("health_score");
				carViews.get(index).getTv_score().setText(String.valueOf(health_score));
				carViews.get(index).getmTasksView().setProgress(health_score);
				
				JSONArray jsonErrArray = jsonObject.getJSONArray("active_obd_err");
				if(jsonErrArray.length() > 0){
					String url = Constant.BaseUrl + "device/fault_desc?auth_code=" + Variable.auth_code;
					List<NameValuePair> params = new ArrayList<NameValuePair>();
			        params.add(new BasicNameValuePair("brand", "大众"));
			        params.add(new BasicNameValuePair("obd_err", jsonObject.getString("active_obd_err")));
			        new Thread(new NetThread.postDataThread(handler, url, params, getFault)).start();					
				}else{
					tv_guzhang.setText("无故障码");
					tv_guzhang.setTextColor(getResources().getColor(R.color.blue_press));
					Drawable drawable= getResources().getDrawable(R.drawable.icon_guzhang_normal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_guzhang_icon.setCompoundDrawables(drawable,null,null,null);
				}
				
				//电源系统
				boolean if_dpdy_err = !jsonObject.getBoolean("if_dpdy_err");	
				dpdy_content = jsonObject.getString("dpdy_content");
				if(if_dpdy_err){
					tv_dianyuan.setText("蓄电池状态良好");
					tv_dianyuan.setTextColor(getResources().getColor(R.color.blue_press));
					Drawable drawable= getResources().getDrawable(R.drawable.icon_dianyuan_normal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_dianyuan_icon.setCompoundDrawables(drawable,null,null,null);
				}else{
					tv_dianyuan.setText("蓄电池状态异常");
					tv_dianyuan.setTextColor(getResources().getColor(R.color.yellow));
					Drawable drawable= getResources().getDrawable(R.drawable.icon_dianyuan_abnormal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_dianyuan_icon.setCompoundDrawables(drawable,null,null,null);
				}
				//进气系统
				boolean if_jqmkd_err = !jsonObject.getBoolean("if_jqmkd_err");
				jqmkd_content = jsonObject.getString("jqmkd_content");
				if(if_jqmkd_err){
					tv_jinqi.setText("节气门开度良好");
					tv_jinqi.setTextColor(getResources().getColor(R.color.blue_press));
					Drawable drawable= getResources().getDrawable(R.drawable.icon_jinqi_normal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_jinqi_icon.setCompoundDrawables(drawable,null,null,null);
				}else{
					tv_jinqi.setText("节气门开度异常");
					tv_jinqi.setTextColor(getResources().getColor(R.color.yellow));
					Drawable drawable= getResources().getDrawable(R.drawable.icon_jinqi_abnormal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_jinqi_icon.setCompoundDrawables(drawable,null,null,null);
				}
				//怠速控制系统
				boolean if_fdjzs_err = !jsonObject.getBoolean("if_fdjzs_err");
				fdjzs_content = jsonObject.getString("fdjzs_content");
				if(if_fdjzs_err){
					tv_daisu.setText("怠速稳定");
					tv_daisu.setTextColor(getResources().getColor(R.color.blue_press));
					Drawable drawable= getResources().getDrawable(R.drawable.icon_daisu_normal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_daisu_icon.setCompoundDrawables(drawable,null,null,null);
				}else{
					tv_daisu.setText("怠速异常");
					tv_daisu.setTextColor(getResources().getColor(R.color.yellow));
					Drawable drawable= getResources().getDrawable(R.drawable.icon_daisu_abnormal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_daisu_icon.setCompoundDrawables(drawable,null,null,null);
				}
				//冷却系统
				boolean if_sw_err = !jsonObject.getBoolean("if_sw_err");
				sw_content = jsonObject.getString("sw_content");
				if(if_sw_err){
					tv_lengque.setText("水温正常");
					tv_lengque.setTextColor(getResources().getColor(R.color.blue_press));
					Drawable drawable= getResources().getDrawable(R.drawable.icon_lengque_normal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_lengque_icon.setCompoundDrawables(drawable,null,null,null);
				}else{
					tv_lengque.setText("水温异常");
					tv_lengque.setTextColor(getResources().getColor(R.color.yellow));
					Drawable drawable= getResources().getDrawable(R.drawable.icon_lengque_abnormal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_lengque_icon.setCompoundDrawables(drawable,null,null,null);
				}
				//排放系统
				boolean if_chqwd_err = !jsonObject.getBoolean("if_chqwd_err");
				chqwd_content = jsonObject.getString("chqwd_content");
				if(if_chqwd_err){
					tv_paifang.setText("三元催化剂状态良好");
					tv_paifang.setTextColor(getResources().getColor(R.color.blue_press));
					Drawable drawable= getResources().getDrawable(R.drawable.icon_paifang_normal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_paifang_icon.setCompoundDrawables(drawable,null,null,null);
				}else{
					tv_paifang.setText("三元催化剂状态异常");
					tv_paifang.setTextColor(getResources().getColor(R.color.yellow));
					Drawable drawable= getResources().getDrawable(R.drawable.icon_paifang_abnormal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_paifang_icon.setCompoundDrawables(drawable,null,null,null);
				}
				//获取历史消息
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**解析车辆限行**/
	private void jsonCarLinit(String result,int index) {
        try {
        	CarView carView = carViews.get(index);
            JSONObject jsonObject = new JSONObject(result);
            String limit = jsonObject.getString("limit");
            carView.getTv_xx_detection().setText(limit);
            Variable.carDatas.get(index).setLimit(limit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	private class CarView {
		TextView tv_xx_detection;
		TextView tv_score;
		TasksCompletedView mTasksView;
		
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
		public TextView getTv_xx_detection() {
			return tv_xx_detection;
		}
		public void setTv_xx_detection(TextView tv_xx_detection) {
			this.tv_xx_detection = tv_xx_detection;
		}		
	}
	/**初始化数据**/
	private void initVariable() {
		mTotalProgress = 100;
		mCurrentProgress = 100;
		Interval = (mCurrentProgress - mTotalProgress)/Point;
		carViews.get(index).getmTasksView().setProgress(100);
		carViews.get(index).getTv_score().setText(String.valueOf(mCurrentProgress));
		//TODO 初始化数据
		tv_guzhang.setText("故障检测中...");
		tv_guzhang.setTextColor(getResources().getColor(R.color.blue_press));
		Drawable drawable= getResources().getDrawable(R.drawable.icon_guzhang_normal);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		tv_guzhang_icon.setCompoundDrawables(drawable,null,null,null);
		
		tv_dianyuan.setText("蓄电池检测中...");
		tv_dianyuan.setTextColor(getResources().getColor(R.color.blue_press));
		drawable= getResources().getDrawable(R.drawable.icon_dianyuan_normal);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		tv_dianyuan_icon.setCompoundDrawables(drawable,null,null,null);
		
		tv_jinqi.setText("节气门开度检测中...");
		tv_jinqi.setTextColor(getResources().getColor(R.color.blue_press));
		drawable= getResources().getDrawable(R.drawable.icon_jinqi_normal);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		tv_jinqi_icon.setCompoundDrawables(drawable,null,null,null);
		
		tv_daisu.setText("怠速检测中...");
		tv_daisu.setTextColor(getResources().getColor(R.color.blue_press));
		drawable= getResources().getDrawable(R.drawable.icon_daisu_normal);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		tv_daisu_icon.setCompoundDrawables(drawable,null,null,null);
		
		tv_lengque.setText("水温检测中...");
		tv_lengque.setTextColor(getResources().getColor(R.color.blue_press));
		drawable= getResources().getDrawable(R.drawable.icon_lengque_normal);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		tv_lengque_icon.setCompoundDrawables(drawable,null,null,null);
		
		tv_paifang.setText("三元催化剂检测中...");
		tv_paifang.setTextColor(getResources().getColor(R.color.blue_press));
		drawable= getResources().getDrawable(R.drawable.icon_paifang_normal);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		tv_paifang_icon.setCompoundDrawables(drawable,null,null,null);
	}
	private void initView() {
		tv_name = (TextView)findViewById(R.id.tv_name);
		tv_guzhang = (TextView)findViewById(R.id.tv_guzhang);
		tv_guzhang_icon = (TextView)findViewById(R.id.tv_guzhang_icon);
		tv_dianyuan = (TextView)findViewById(R.id.tv_dianyuan);
		tv_dianyuan_icon = (TextView)findViewById(R.id.tv_dianyuan_icon);
		tv_jinqi = (TextView)findViewById(R.id.tv_jinqi);
		tv_jinqi_icon = (TextView)findViewById(R.id.tv_jinqi_icon);
		tv_daisu = (TextView)findViewById(R.id.tv_daisu);
		tv_daisu_icon = (TextView)findViewById(R.id.tv_daisu_icon);
		tv_lengque = (TextView)findViewById(R.id.tv_lengque);
		tv_lengque_icon = (TextView)findViewById(R.id.tv_lengque_icon);
		tv_paifang = (TextView)findViewById(R.id.tv_paifang);
		tv_paifang_icon = (TextView)findViewById(R.id.tv_paifang_icon);
		RelativeLayout rl_guzhang = (RelativeLayout)findViewById(R.id.rl_guzhang);
		rl_guzhang.setOnClickListener(onClickListener);
		RelativeLayout rl_dianyuan = (RelativeLayout)findViewById(R.id.rl_dianyuan);
		rl_dianyuan.setOnClickListener(onClickListener);
		RelativeLayout rl_jinqi = (RelativeLayout)findViewById(R.id.rl_jinqi);
		rl_jinqi.setOnClickListener(onClickListener);
		RelativeLayout rl_daisu = (RelativeLayout)findViewById(R.id.rl_daisu);
		rl_daisu.setOnClickListener(onClickListener);
		RelativeLayout rl_lengque = (RelativeLayout)findViewById(R.id.rl_lengque);
		rl_lengque.setOnClickListener(onClickListener);
		RelativeLayout rl_paifang = (RelativeLayout)findViewById(R.id.rl_paifang);
		rl_paifang.setOnClickListener(onClickListener);
	}
	int i = 0;
	int j = 0;
	/**
	 * 总分100，得分60，到计时40；总共有6个点
	 *
	 */
	class ProgressThread extends Thread{
		@Override
		public void run() {
			super.run();
			Message message = new Message();
			message.what = refresh_score;
			handler.sendMessage(message);
		}
	}
	class ProgressRunable implements Runnable {
		int index;
		public ProgressRunable(int index){
			this.index = index;
		}
		@Override
		public void run() {
			while (mCurrentProgress > mTotalProgress) {
				mCurrentProgress -= 1;				
				carViews.get(index).getmTasksView().setProgress(mCurrentProgress);
				Message message = new Message();
				message.what = refresh_score;
				message.arg1 = index;
				handler.sendMessage(message);
				i++;
				if(i == Interval){
					i = 0;
					j++;
					Message message1 = new Message();
					message1.what = refresh;
					message1.arg1 = j;
					handler.sendMessage(message1);
				}
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}		
	}
	/**获取健康数据**/
	private void getData(int index){
		String Device_id = Variable.carDatas.get(index).getDevice_id();
		if (Device_id == null || Device_id.equals("")) {
			Intent intent = new Intent(FaultDetectionActivity.this,DevicesAddActivity.class);
			intent.putExtra("car_id", Variable.carDatas.get(index).getObj_id());
			startActivityForResult(intent, 2);
		}else{
			initVariable();
			String url =Constant.BaseUrl + "device/" + Device_id + "/health_exam?auth_code=bba2204bcd4c1f87a19ef792f1f68404";
			new Thread(new NetThread.GetDataThread(handler, url, getData,index)).start();
		}
	}
	

	String result ="";
	String dpdy_content,jqmkd_content,fdjzs_content,sw_content,chqwd_content;
	
	private void refreshHealth(int j){
		try {
			JSONObject jsonObject = new JSONObject(result);
			switch (j) {
			case 1:
				//故障码
				JSONArray jsonErrArray = jsonObject.getJSONArray("active_obd_err");
				if(jsonErrArray.length() > 0){
					String url = Constant.BaseUrl + "device/fault_desc?auth_code=" + Variable.auth_code;
					List<NameValuePair> params = new ArrayList<NameValuePair>();
			        params.add(new BasicNameValuePair("brand", "大众"));
			        params.add(new BasicNameValuePair("obd_err", jsonObject.getString("active_obd_err")));
			        //params.add(new BasicNameValuePair("obd_err", "[\"P1024\", \"P1025\"]"));
			        new Thread(new NetThread.postDataThread(handler, url, params, getFault)).start();
					
					
					tv_guzhang.setText("有"+jsonErrArray.length() + "个故障");
					tv_guzhang.setTextColor(getResources().getColor(R.color.yellow));
					Drawable drawable= getResources().getDrawable(R.drawable.icon_guzhang_abnormal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_guzhang_icon.setCompoundDrawables(drawable,null,null,null);
				}else{
					tv_guzhang.setText("无故障码");
					tv_guzhang.setTextColor(getResources().getColor(R.color.blue_press));
					Drawable drawable= getResources().getDrawable(R.drawable.icon_guzhang_normal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_guzhang_icon.setCompoundDrawables(drawable,null,null,null);
				}				
				break;
			case 2:
				//电源系统
				boolean if_dpdy_err = !jsonObject.getBoolean("if_dpdy_err");	
				dpdy_content = jsonObject.getString("dpdy_content");
				if(if_dpdy_err){
					tv_dianyuan.setText("蓄电池状态良好");
					tv_dianyuan.setTextColor(getResources().getColor(R.color.blue_press));
					Drawable drawable= getResources().getDrawable(R.drawable.icon_dianyuan_normal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_dianyuan_icon.setCompoundDrawables(drawable,null,null,null);
				}else{
					tv_dianyuan.setText("蓄电池状态异常");
					tv_dianyuan.setTextColor(getResources().getColor(R.color.yellow));
					Drawable drawable= getResources().getDrawable(R.drawable.icon_dianyuan_abnormal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_dianyuan_icon.setCompoundDrawables(drawable,null,null,null);
				}
				break;
			case 3:
				//进气系统
				boolean if_jqmkd_err = !jsonObject.getBoolean("if_jqmkd_err");
				jqmkd_content = jsonObject.getString("jqmkd_content");
				if(if_jqmkd_err){
					tv_jinqi.setText("节气门开度良好");
					tv_jinqi.setTextColor(getResources().getColor(R.color.blue_press));
					Drawable drawable= getResources().getDrawable(R.drawable.icon_jinqi_normal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_jinqi_icon.setCompoundDrawables(drawable,null,null,null);
				}else{
					tv_jinqi.setText("节气门开度异常");
					tv_jinqi.setTextColor(getResources().getColor(R.color.yellow));
					Drawable drawable= getResources().getDrawable(R.drawable.icon_jinqi_abnormal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_jinqi_icon.setCompoundDrawables(drawable,null,null,null);
				}
				break;
			case 4:
				//怠速控制系统
				boolean if_fdjzs_err = !jsonObject.getBoolean("if_fdjzs_err");
				fdjzs_content = jsonObject.getString("fdjzs_content");
				if(if_fdjzs_err){
					tv_daisu.setText("怠速稳定");
					tv_daisu.setTextColor(getResources().getColor(R.color.blue_press));
					Drawable drawable= getResources().getDrawable(R.drawable.icon_daisu_normal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_daisu_icon.setCompoundDrawables(drawable,null,null,null);
				}else{
					tv_daisu.setText("怠速异常");
					tv_daisu.setTextColor(getResources().getColor(R.color.yellow));
					Drawable drawable= getResources().getDrawable(R.drawable.icon_daisu_abnormal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_daisu_icon.setCompoundDrawables(drawable,null,null,null);
				}
				break;
			case 5:
				//冷却系统
				boolean if_sw_err = !jsonObject.getBoolean("if_sw_err");
				sw_content = jsonObject.getString("sw_content");
				if(if_sw_err){
					tv_lengque.setText("水温正常");
					tv_lengque.setTextColor(getResources().getColor(R.color.blue_press));
					Drawable drawable= getResources().getDrawable(R.drawable.icon_lengque_normal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_lengque_icon.setCompoundDrawables(drawable,null,null,null);
				}else{
					tv_lengque.setText("水温异常");
					tv_lengque.setTextColor(getResources().getColor(R.color.yellow));
					Drawable drawable= getResources().getDrawable(R.drawable.icon_lengque_abnormal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_lengque_icon.setCompoundDrawables(drawable,null,null,null);
				}
				break;
			case 6:
				//排放系统
				boolean if_chqwd_err = !jsonObject.getBoolean("if_chqwd_err");
				chqwd_content = jsonObject.getString("chqwd_content");
				if(if_chqwd_err){
					tv_paifang.setText("三元催化剂状态良好");
					tv_paifang.setTextColor(getResources().getColor(R.color.blue_press));
					Drawable drawable= getResources().getDrawable(R.drawable.icon_paifang_normal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_paifang_icon.setCompoundDrawables(drawable,null,null,null);
				}else{
					tv_paifang.setText("三元催化剂状态异常");
					tv_paifang.setTextColor(getResources().getColor(R.color.yellow));
					Drawable drawable= getResources().getDrawable(R.drawable.icon_paifang_abnormal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_paifang_icon.setCompoundDrawables(drawable,null,null,null);
				}
				break;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}		
	}
	//存储
	private void jsonHealth(String str){
		try {
			JSONObject jsonObject = new JSONObject(str);
			int health_score = jsonObject.getInt("health_score");			
			mTotalProgress = health_score;
			mCurrentProgress = 100;
			Interval = (mCurrentProgress - mTotalProgress)/Point;
			//体检结果存起来
			SharedPreferences preferences = getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
	        Editor editor = preferences.edit();
	        editor.putString(Constant.sp_health_score + Variable.carDatas.get(index).getObj_id(), str);
	        editor.commit();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	private void Back(){
		Intent intent = new Intent();
		intent.putExtra("health_score", mTotalProgress);
		setResult(2, intent);
		finish();
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Back();
		return true;
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