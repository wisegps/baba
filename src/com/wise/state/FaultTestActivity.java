package com.wise.state;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.NetThread;
import pubclas.UpdateManager;
import pubclas.Variable;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UpdateConfig;
import com.wise.baba.MoreActivity;
import com.wise.baba.R;
import com.wise.baba.SelectCityActivity;
import com.wise.car.CarActivity;
import com.wise.car.CarAddActivity;
import com.wise.car.CarUpdateActivity;
import com.wise.car.DevicesAddActivity;
import com.wise.notice.NoticeFragment;
import com.wise.notice.NoticeFragment.BtnListener;
import com.wise.setting.LoginActivity;
import customView.AlwaysMarqueeTextView;
import customView.HScrollLayout;
import customView.NoticeScrollTextView;
import customView.OnViewChangeListener;
import customView.SlidingMenuView;
import data.CarData;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 车况信息【卡片布局】
 * 
 * @author honesty
 * 
 */
public class FaultTestActivity extends FragmentActivity {
	/**获取油耗信息**/
	private static final int getData = 1;
	/**获取天气信息**/
	private static final int getWeather = 2;
	/**循环读取乐一乐线程**/
	private static final int cycle = 3;
	/** 乐一乐 **/
	private static final int getJoy = 4;
	/** 获取滚动消息 **/
	private static final int getMessage = 5;
	/** 定时滚动消息 **/
	private static final int Nstv = 6;
	/**获取限行**/
	private static final int Get_carLimit = 7;
	/**获取消息数据**/
	private static final int get_counter = 8;
	/**获取版本信息**/
	private static final int get_version = 9;

	ImageView iv_weather,iv_noti;
	TextView tv_city, tv_weather_time, tv_weather, tv_advice, tv_joy, tv_happy_time;
	int index = 0;
	private FragmentManager fragmentManager;
	SlidingMenuView smv_content;
	NoticeScrollTextView nstv_message;
	HScrollLayout hs_car;
	MyBroadCastReceiver myBroadCastReceiver;
	IntentFilter intentFilter;

	/**滚动消息数据**/
	List<NsData> nsDatas = new ArrayList<NsData>();
	/**获取油耗数据开始时间**/
	String startMonth;
	/**获取油耗数据结束时间**/
	String endMonth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_fault_test);
		boolean isSpecify = getIntent().getBooleanExtra("isSpecify", false);
		if(isSpecify){
			Intent intent = new Intent(FaultTestActivity.this, MoreActivity.class);
			intent.putExtra("isSpecify", isSpecify);
			intent.putExtras(getIntent().getExtras());
			startActivity(intent);
		}
		setData();
		initView();
		
		tv_city = (TextView) findViewById(R.id.tv_city);
		tv_city.setOnClickListener(onClickListener);
		tv_weather_time = (TextView) findViewById(R.id.tv_weather_time);
		tv_weather = (TextView) findViewById(R.id.tv_weather);
		tv_advice = (TextView) findViewById(R.id.tv_advice);
		tv_joy = (TextView) findViewById(R.id.tv_joy);
		tv_happy_time = (TextView) findViewById(R.id.tv_happy_time);
		iv_weather = (ImageView) findViewById(R.id.iv_weather);
		iv_noti = (ImageView) findViewById(R.id.iv_noti);
		ImageView iv_menu = (ImageView) findViewById(R.id.iv_menu);
		iv_menu.setOnClickListener(onClickListener);

		smv_content = (SlidingMenuView) findViewById(R.id.smv_content);
		nstv_message = (NoticeScrollTextView) findViewById(R.id.nstv_message);
		hs_car = (HScrollLayout) findViewById(R.id.hs_car);
		fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		NoticeFragment noticeFragment = new NoticeFragment();
		transaction.add(R.id.ll_notice, noticeFragment);
		transaction.commit();
		noticeFragment.SetBtnListener(new BtnListener() {
			@Override
			public void Back() {
				smv_content.snapToScreen(0);
			}
		});
		new CycleThread().start();
		hs_car.setOnViewChangeListener(new OnViewChangeListener() {
			@Override
			public void OnViewChange(int view) {
				index = view;
				getTotalData();
			}
			@Override
			public void OnLastView() {}
			@Override
			public void OnFinish(int index) {}
		});
		String Month = GetSystem.GetNowMonth().getMonth();
		startMonth = Month+"-01";
		endMonth = Month+"-31";
		//未登录
		if (Variable.cust_id == null || Variable.cust_id.equals("")) {
			setLoginView();
			String url =  Constant.BaseUrl + "customer/0/tips";
			getMessage(url);
		} else {
			initDataView();
			getTotalData();
			String url = Constant.BaseUrl + "customer/" + Variable.cust_id
					+ "/tips?auth_code=" + Variable.auth_code;
			getMessage(url);
			getCounter();			
		}
		myBroadCastReceiver = new MyBroadCastReceiver();
		intentFilter = new IntentFilter();
		intentFilter.addAction(Constant.A_RefreshHomeCar);
		registerReceiver(myBroadCastReceiver, intentFilter);
		getWeather();
		new CycleNstvThread().start();
		//getVersion();

		UpdateConfig.setDebug(true);
		UmengUpdateAgent.update(this);
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_menu:
				startActivity(new Intent(FaultTestActivity.this, MoreActivity.class));
				break;
			case R.id.tasks_view:
				String Device_id = Variable.carDatas.get(index).getDevice_id();
				if (Device_id == null || Device_id.equals("")) {
					Intent intent = new Intent(FaultTestActivity.this,DevicesAddActivity.class);
					intent.putExtra("car_id", Variable.carDatas.get(index).getObj_id());
					startActivityForResult(intent, 2);
				} else {
					Intent intent = new Intent(FaultTestActivity.this,FaultDetectionActivity.class);
					intent.putExtra("index", index);
					startActivityForResult(intent, 1);
				}
				break;
			case R.id.ll_fee:
				Intent Intent = new Intent(FaultTestActivity.this,FuelActivity.class);
				Intent.putExtra("index_car", index);
				Intent.putExtra("abc", "abc");
				startActivity(Intent);
				break;
			case R.id.tv_message:
				nstvClick();
				break;
			case R.id.tv_city:
				startActivityForResult(new Intent(FaultTestActivity.this, SelectCityActivity.class),0);
				break;
			}
		}
	};
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case getData:
				jsonData(msg.obj.toString(),msg.arg1);
				break;
			case getWeather:
				jsonWeather(msg.obj.toString());
				break;
			case getJoy:
				jsonJoy(msg.obj.toString());
				break;
			case cycle:
				getJoy();
				break;
			case getMessage:
				setMessageView(msg.obj.toString());
				break;
			case Nstv:
				ScrollMessage();
				break;
			case Get_carLimit:
				jsonCarLinit(msg.obj.toString(),msg.arg1);
				break;
			case get_counter:
				jsonCounter(msg.obj.toString());
				break;
			case get_version:
				jsonVersion(msg.obj.toString());
				break;
			}
		}
	};
	/**获取统计数据**/
	private void getTotalData() {
		if(Variable.carDatas == null || Variable.carDatas.size() ==0 ){
			return;
		}
		CarData carData = Variable.carDatas.get(index);
		String device_id = carData.getDevice_id();
		if (device_id == null || device_id.equals("")) {

		} else {
			try {
				//获取当前月的数据
				String url = Constant.BaseUrl + "device/"+ device_id
						+ "/total?auth_code=127a154df2d7850c4232542b4faa2c3d&start_day=" + startMonth + 
						"&end_day=" + endMonth + "&city="
						+ URLEncoder.encode(Variable.City, "UTF-8")
						+ "&gas_no=93#(92#)";
				new NetThread.GetDataThread(handler, url, getData,index).start();				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//获取限行信息
		if(Variable.City == null || carData.getObj_name() == null || Variable.City.equals("") || carData.getObj_name().equals("")){
			
		}else{
			try {
				String url = Constant.BaseUrl + "base/ban?city="
	                    + URLEncoder.encode(Variable.City, "UTF-8")
	                    + "&obj_name="
	                    + URLEncoder.encode(carData.getObj_name(), "UTF-8");
	            new NetThread.GetDataThread(handler, url,Get_carLimit,index).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/**解析本月油价**/
	private void jsonData(String str,int index) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			CarView carView = carViews.get(index);
			carView.getTv_fee().setText(jsonObject.getString("total_fee"));// 花费
			carView.getTv_fuel().setText(jsonObject.getString("total_fuel"));// 油耗
			carView.getTv_distance().setText(
					jsonObject.getString("total_distance"));// 里程
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	/**解析车辆限行**/
	private void jsonCarLinit(String result,int index) {
        try {
        	CarView carView = carViews.get(index);
            JSONObject jsonObject = new JSONObject(result);
            String limit = jsonObject.getString("limit");
            carView.getTv_xx().setText(limit);
            Variable.carDatas.get(index).setLimit(limit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	/** 获取天气 **/
	private void getWeather() {
		SharedPreferences preferences = getSharedPreferences(
				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		Variable.City = preferences.getString(Constant.sp_city, "");
		tv_city.setText("[ " + Variable.City + " ]");
		Variable.Province = preferences.getString(Constant.sp_province, "");
		try {
			String url = Constant.BaseUrl + "base/weather2?city="
					+ URLEncoder.encode(Variable.City, "UTF-8");
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
			String temperature = jsonObject.getJSONObject("today").getString("temperature");
			String weather = jsonObject.getJSONObject("today").getString("weather");
			String quality = jsonObject.getString("quality");
			tv_weather.setText(temperature + "  " + weather + "   空气质量" +quality);
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

	public int getResource(String imageName) {
		int resId = getResources().getIdentifier(imageName, "drawable",
				"com.wise.baba");
		return resId;
	}

	boolean isCycle = true;

	class CycleThread extends Thread {
		@Override
		public void run() {
			super.run();
			while (isCycle) {
				try {
					Message message = new Message();
					message.what = cycle;
					handler.sendMessage(message);
					Thread.sleep(300000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/** 获取乐一下 **/
	private void getJoy() {
		String url = Constant.BaseUrl + "base/joy";
		new NetThread.GetDataThread(handler, url, getJoy).start();
	}

	/** 解析乐一下 **/
	private void jsonJoy(String str) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			tv_joy.setText(jsonObject.getString("content"));
			tv_happy_time.setText(GetSystem.ChangeTimeZone(
					jsonObject.getString("rcv_time").substring(0, 19)
							.replace("T", " ")).substring(11, 16));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/** 滑动车辆布局 **/
	private void initDataView() {
		SharedPreferences preferences = getSharedPreferences(
				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		hs_car.removeAllViews();
		for (int i = 0; i < Variable.carDatas.size(); i++) {
			View v = LayoutInflater.from(this).inflate(R.layout.item_fault,null);
			hs_car.addView(v);
			TextView tv_score = (TextView) v.findViewById(R.id.tv_score);
			TextView tv_title = (TextView) v.findViewById(R.id.tv_title);
			TasksCompletedView mTasksView = (TasksCompletedView) v.findViewById(R.id.tasks_view);
			mTasksView.setOnClickListener(onClickListener);
			LinearLayout ll_fee = (LinearLayout) v.findViewById(R.id.ll_fee);
			ll_fee.setOnClickListener(onClickListener);
			TextView tv_distance = (TextView) v.findViewById(R.id.tv_distance);
			TextView tv_fee = (TextView) v.findViewById(R.id.tv_fee);
			TextView tv_fuel = (TextView) v.findViewById(R.id.tv_fuel);
			TextView tv_name = (TextView) v.findViewById(R.id.tv_name);
			TextView tv_xx = (TextView) v.findViewById(R.id.tv_xx);

			CarView carView = new CarView();
			carView.setmTasksView(mTasksView);
			carView.setTv_distance(tv_distance);
			carView.setTv_fee(tv_fee);
			carView.setTv_fuel(tv_fuel);
			carView.setTv_score(tv_score);
			carView.setTv_title(tv_title);
			carView.setTv_xx(tv_xx);
			carViews.add(carView);

			tv_name.setText(Variable.carDatas.get(i).getCar_series() + "("
					+ Variable.carDatas.get(i).getNick_name() + ")");
			String result = preferences.getString(Constant.sp_health_score
					+ Variable.carDatas.get(i).getObj_id(), "");
			if (result.equals("")) {// 未体检过
				carView.getmTasksView().setProgress(100);
				tv_score.setText("0");
				tv_title.setText("未体检过");
			} else {
				try {
					JSONObject jsonObject = new JSONObject(result);
					//健康指数
					int health_score = jsonObject.getInt("health_score");
					carView.getmTasksView().setProgress(health_score);
					tv_score.setText(String.valueOf(health_score));
					tv_title.setText("上次体检");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		hs_car.snapToScreen(index);
	}

	/** 未登录显示 **/
	private void setLoginView() {
		hs_car.removeAllViews();
		View v = LayoutInflater.from(this).inflate(R.layout.item_fault, null);
		hs_car.addView(v);
		TextView tv_score = (TextView) v.findViewById(R.id.tv_score);
		TextView tv_title = (TextView) v.findViewById(R.id.tv_title);
		TasksCompletedView mTasksView = (TasksCompletedView) v.findViewById(R.id.tasks_view);
		mTasksView.setProgress(100);
		tv_score.setText("0");
		tv_title.setText("未体检过");
		mTasksView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(FaultTestActivity.this,
						LoginActivity.class));
			}
		});
		TextView tv_name = (TextView) v.findViewById(R.id.tv_name);
		tv_name.setText("绑定叭叭车载智能配件");
	}
	
	/**获取消息数据**/
	private void getCounter(){
		String url = Constant.BaseUrl + "customer/" + Variable.cust_id + "/counter?auth_code=" + Variable.auth_code;
		new NetThread.GetDataThread(handler, url, get_counter).start();
	}
	/**解析消息数据**/
	private void jsonCounter(String str){
		try {
			JSONObject jsonObject = new JSONObject(str);
			Variable.noti_count = jsonObject.getInt("noti_count");
			Variable.vio_count = jsonObject.getInt("vio_count");
			setNotiView();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		setNotiView();
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	/**设置提醒**/
	private void setNotiView(){
		if(Variable.noti_count == 0 && Variable.vio_count == 0){
			//显示提醒
			iv_noti.setVisibility(View.GONE);
		}else{
			//隐藏提醒
			iv_noti.setVisibility(View.VISIBLE);
		}
	}
	
	/**获取最新版本**/
	private void getVersion(){
		String url = Constant.BaseUrl + "upgrade/android/baba";
        new NetThread.GetDataThread(handler, url,get_version).start();
	}
	private void jsonVersion(String result) {
        try {
            double Version = Double.valueOf(GetSystem.GetVersion(FaultTestActivity.this, Constant.PackageName));
            double logVersion = Double.valueOf(new JSONObject(result).getString("version"));
            String VersonUrl = new JSONObject(result).getString("app_path");
            String logs = new JSONObject(result).getString("logs");
            if (logVersion > Version) {
            	UpdateManager mUpdateManager = new UpdateManager(
                        FaultTestActivity.this, VersonUrl, logs, Version);
                mUpdateManager.checkUpdateInfo();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
			if(jsonArray.length() > 0){
				nstv_message.setVisibility(View.VISIBLE);
			}else{
				nstv_message.setVisibility(View.GONE);
			}
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String content = jsonObject.getString("content");
				View v = LayoutInflater.from(this).inflate(R.layout.item_nstv,null);
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
			// 没有数据不考虑
		} else {
			if (index_message < nsDatas.size()) {
				// 防止数组越界
				int type = nsDatas.get(index_message).getType();
				switch (type) {
				case 0:
					// 注册用户
					startActivity(new Intent(FaultTestActivity.this, LoginActivity.class));
					break;
				case 1:
					// 注册车辆
					startActivity(new Intent(FaultTestActivity.this,
							CarAddActivity.class));
					break;
				case 2:
					//修改车辆
					int index = getIndexFromId(nsDatas.get(index_message)
							.getObj_id());
					if (index == -1) {
						// 没有在列表找到对应的车
						startActivity(new Intent(FaultTestActivity.this,
								CarActivity.class));
					} else {
						Intent intent = new Intent(FaultTestActivity.this,
								CarUpdateActivity.class);
						intent.putExtra("index", index);
						startActivityForResult(intent, 2);
					}
					break;
				case 3:
					// 绑定终端
					startActivity(new Intent(FaultTestActivity.this,
							CarActivity.class));
					break;
				case 4:
					// 通知
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
		nstv_message.snapToScreen(index_message);
		if (index_message >= (nsDatas.size() - 1)) {
			index_message = 0;
		}
	}
	/**滚动消息**/
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

	// 定时调整滚动消息
	class CycleNstvThread extends Thread {
		@Override
		public void run() {
			super.run();
			while (isCycle) {
				try {
					Message message = new Message();
					message.what = Nstv;
					handler.sendMessage(message);
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/** 根据obj_id返回对应列表的位置 **/
	private int getIndexFromId(int obj_id) {
		for (int i = 0; i < Variable.carDatas.size(); i++) {
			if (Variable.carDatas.get(i).getObj_id() == obj_id) {
				return i;
			}
		}
		return -1;
	}

	List<CarView> carViews = new ArrayList<CarView>();

	private class CarView {
		TextView tv_distance;
		TextView tv_fee;
		TextView tv_fuel;
		TextView tv_score;
		TextView tv_title;
		TextView tv_xx;
		TasksCompletedView mTasksView;

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
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 3) {
			// 修改车辆信息
			initDataView();
		}else if(requestCode == 0){
			getWeather();
		}else if(requestCode == 1){
			initDataView();
//			int Progress = data.getIntExtra("health_score", 1000);
//			CarView carView = carViews.get(index);
//			carView.getmTasksView().setProgress(Progress);
//			carView.getTv_score().setText(String.valueOf(Progress));
//			carView.getTv_title().setText("上次体检");
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(myBroadCastReceiver);
		isCycle = false;
	}

	long waitTime = 2000;
	long touchTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (smv_content.getCurrentScreen() == 1) {
				smv_content.snapToScreen(0);
			} else {
				long currentTime = System.currentTimeMillis();
				if (touchTime == 0 || (currentTime - touchTime) >= waitTime) {
					Toast.makeText(this, "再按一次退出客户端", Toast.LENGTH_SHORT)
							.show();
					touchTime = currentTime;
				} else {
					finish();
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
			if (action.equals(Constant.A_RefreshHomeCar)) {
				initDataView();
				getTotalData();
				String url = Constant.BaseUrl + "customer/" + Variable.cust_id
						+ "/tips?auth_code=" + Variable.auth_code;
				getMessage(url);
				getCounter();
			} else if(action.equals(Constant.A_LoginOut)){
				setLoginView();
				String url =  Constant.BaseUrl + "customer/0/tips";
				getMessage(url);
			} else if (action.equals(Constant.A_City)) {
				try {
					Variable.City = intent.getStringExtra("City");
					Variable.Province = intent.getStringExtra("Province");
					String url = Constant.BaseUrl + "base/weather2?city="
							+ URLEncoder.encode(Variable.City, "UTF-8");
					new NetThread.GetDataThread(handler, url, getWeather)
							.start();
					SharedPreferences preferences = getSharedPreferences(
							Constant.sharedPreferencesName,
							Context.MODE_PRIVATE);
					Editor editor = preferences.edit();
					editor.putString(Constant.sp_city, Variable.City);
					editor.putString(Constant.sp_province, Variable.Province);
					editor.commit();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	List<ViewData> viewDatas = new ArrayList<ViewData>();
	/**布局首页模块**/
	private void initView(){//TODO 布局
		LinearLayout ll_fault_content = (LinearLayout)findViewById(R.id.ll_fault_content);
		View viewCar = LayoutInflater.from(this).inflate(R.layout.item_fault_car,null);
		ll_fault_content.addView(viewCar);
		View viewNstv = LayoutInflater.from(this).inflate(R.layout.item_fault_nstv,null);
		ll_fault_content.addView(viewNstv);
		View viewWeather = LayoutInflater.from(this).inflate(R.layout.item_fault_weather,null);
		ll_fault_content.addView(viewWeather);
		View viewHappy = LayoutInflater.from(this).inflate(R.layout.item_fault_happy,null);
		ll_fault_content.addView(viewHappy);
	}
	/**获取首页要显示的模块**/
	private void setData(){
		ViewData viewData0 = new ViewData();
		viewData0.setId(0);
		viewData0.setName("车辆信息");
		viewDatas.add(viewData0);
		
		ViewData viewData1 = new ViewData();
		viewData1.setId(0);
		viewData1.setName("滚动信息");
		viewDatas.add(viewData1);
		
		ViewData viewData2 = new ViewData();
		viewData2.setId(0);
		viewData2.setName("天气");
		viewDatas.add(viewData2);
		
		ViewData viewData3 = new ViewData();
		viewData3.setId(0);
		viewData3.setName("了一下");
		viewDatas.add(viewData3);
	}
	class ViewData{
		public int id;
		public String name;
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}		
	}
}