package com.wise.baba.ui.fragment;

import java.net.URLEncoder;


import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.SelectCityActivity;
import com.wise.baba.app.Const;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.adapter.OnCardMenuListener;

/**
 * @author honesty
 **/
public class FragmentWeather extends Fragment {
	/** 获取天气信息 **/
	private static final int getWeather = 2;
	private AppApplication app;
	private TextView tvCity , tvDate, tvTemperature ,tvWeather,tvAirQuality;
	private ImageView ivIconCity,ivWeather;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_weather, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		app = (AppApplication) getActivity().getApplication();
		
		tvCity = (TextView) getActivity().findViewById(R.id.tv_city);
		tvCity.setOnClickListener(onClickListener);
		
		tvDate = (TextView) getActivity().findViewById(R.id.tv_date);
		tvTemperature = (TextView) getActivity().findViewById(R.id.tv_temperature);
		tvWeather = (TextView) getActivity().findViewById(R.id.tv_weather);
		tvAirQuality = (TextView) getActivity().findViewById(R.id.tv_air_quality);
		
		ivIconCity = (ImageView) getActivity().findViewById(R.id.iv_icon);
		ivIconCity.setOnClickListener(onClickListener);
		
		
		ivWeather = (ImageView) getActivity().findViewById(R.id.iv_weather);
		ImageView iv_weather_menu = (ImageView) getActivity().findViewById(R.id.iv_weather_menu);
		iv_weather_menu.setOnClickListener(onClickListener);
	}
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}


	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.tv_city:
			case R.id.iv_icon:
				/** 跳转到修改城市，天气信息在resume里刷新了 **/
				startActivity(new Intent(getActivity(), SelectCityActivity.class));
				break;
			case R.id.iv_weather_menu:
				if(onCardMenuListener != null){
					onCardMenuListener.showCarMenu(Const.TAG_WEATHER);
				}
				break;
			}
		}
	};
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case getWeather:
				if(!isDestroy){
					jsonWeather(msg.obj.toString());
				}
				break;
			}
		}
	};

	/**
	 * 获取天气 onResume里获取，应为在设置页面改了城市后需要刷新
	 **/
	private void getWeather() {
		SharedPreferences preferences = getActivity().getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		app.City = preferences.getString(Constant.sp_city, "");
		tvCity.setText( app.City);
		app.Province = preferences.getString(Constant.sp_province, "");
		try {
			String url = Constant.BaseUrl + "base/weather2?city=" + URLEncoder.encode(app.City, "UTF-8");
			new NetThread.GetDataThread(handler, url, getWeather).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public Spanned getAirQuality(String quality){
		String color = "";
		if(quality.contains("优")){
			color = "#6eb720";
		}else if(quality.contains("良")){
			color = "#d6c60f";
		}else if(quality.contains("轻度污染")){
			color = "#ec7e22";
		}else if(quality.contains("中度污染")){
			color = "#df2d00";
		}else if(quality.contains("重度污染")){
			color = "#b414bb";
		}else if(quality.contains("严重污染")){
			color = "#6f0474";
			
		}
		
		Spanned html = Html.fromHtml("空气质量:    "+"<font color = "+color+">"+quality+"</font>");
		
		return html;
		
	}
	private void jsonWeather(String str) {
		Log.i("FragmentWeather", str);
		try {
			JSONObject jsonObject = new JSONObject(str);
			String date = jsonObject.getJSONObject("today").getString("week");
			tvDate.setText("今天  "+date);
			String temperature = jsonObject.getJSONObject("today").getString("temperature");
			String weather = jsonObject.getJSONObject("today").getString("weather");
			String wind = jsonObject.getJSONObject("today").getString("wind");
			String quality = jsonObject.getString("quality");
			
			
			tvTemperature.setText(temperature);
			tvWeather.setText(weather +"  "+wind);
			tvAirQuality.setText(getAirQuality(quality));
			
			int fa = jsonObject.getJSONObject("today").getJSONObject("weather_id").getInt("fa");
			ivWeather.setImageResource(getResource("weather" + fa));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/** 返回天气对应的r资源名称 **/
	public int getResource(String imageName) {
		System.out.println("getResource");
		int resId = getResources().getIdentifier(imageName, "drawable", "com.wise.baba");
		return resId;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		isDestroy = false;
		getWeather();
	}
	boolean isDestroy = false;
	@Override
	public void onDestroy() {
		super.onDestroy();
		isDestroy = true;
	}


	OnCardMenuListener onCardMenuListener;
	public void setOnCardMenuListener(OnCardMenuListener onCardMenuListener){
		this.onCardMenuListener = onCardMenuListener;
	}
}
