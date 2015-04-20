package fragment;

import java.net.URLEncoder;

import listener.OnCardMenuListener;

import org.json.JSONException;
import org.json.JSONObject;

import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.NetThread;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
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

/**
 * @author honesty
 **/
public class FragmentWeather extends Fragment {
	/** 获取天气信息 **/
	private static final int getWeather = 2;
	AppApplication app;
	TextView tv_advice, tv_weather_time, tv_weather, tv_city;
	ImageView iv_weather;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_weather, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		app = (AppApplication) getActivity().getApplication();
		tv_city = (TextView) getActivity().findViewById(R.id.tv_city);
		tv_city.setOnClickListener(onClickListener);
		tv_weather_time = (TextView) getActivity().findViewById(R.id.tv_weather_time);
		tv_weather = (TextView) getActivity().findViewById(R.id.tv_weather);
		tv_advice = (TextView) getActivity().findViewById(R.id.tv_advice);
		iv_weather = (ImageView) getActivity().findViewById(R.id.iv_weather);
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
		tv_city.setText("[ " + app.City + " ]");
		app.Province = preferences.getString(Constant.sp_province, "");
		try {
			String url = Constant.BaseUrl + "base/weather2?city=" + URLEncoder.encode(app.City, "UTF-8");
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
			tv_weather.setText(temperature + "  " + weather + "   空气质量" + quality);
			String tips = jsonObject.getString("tips");
			if (tips.equals("")) {
				tv_advice.setText(jsonObject.getJSONObject("today").getString("dressing_advice"));
			} else {
				tv_advice.setText(tips);
			}
			int fa = jsonObject.getJSONObject("today").getJSONObject("weather_id").getInt("fa");
			iv_weather.setImageResource(getResource("x" + fa));
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
