package com.wise.baba;

import java.util.ArrayList;
import java.util.List;

import com.wise.baba.app.Msg;
import com.wise.baba.biz.HttpAir;
import com.wise.baba.entity.AQIEntity;
import com.wise.baba.ui.widget.SplineChartView;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * 空气质量指数
 * 
 * @author c
 * 
 */

public class AirQualityIndexActivity extends Activity {

	private String deviceId = "";
	private HttpAir  httpAir = null;
	private SplineChartView myChatView ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_air_quality_index);
		deviceId = getIntent().getStringExtra("deviceId");
		httpAir = new HttpAir(this, handler);
		httpAir.requestAQI(deviceId);
		initView();
	}
	
	
	
	/**
	 * 异步网络请求，消息处理
	 */
	public Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			if(msg.what == Msg.Get_Air_AQI){
				ArrayList<AQIEntity> list = (ArrayList<AQIEntity>) msg.obj;
				myChatView.setDataSet(list);
			}
		}
		
	};

	/**
	 * 初始化页面
	 */
	public void initView() {
		FrameLayout layout = (FrameLayout) findViewById(R.id.flytAirChat);
		DisplayMetrics dm = getResources().getDisplayMetrics();
		int scrWidth = (int) (dm.widthPixels * 0.9);
		int scrHeight = (int) (dm.heightPixels * 0.9);
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		myChatView = new SplineChartView(this);
		layout.addView(myChatView, layoutParams);
	}

	/**
	 * 更新空气质量数据集
	 * 
	 * @param list
	 *            ，各个时间点空气质量 列表
	 */
	public void notifyDataSet(List<AQIEntity> list) {

	}

	/**
	 * 弹出空气质量页面
	 */
	public void show() {

	}

	/**
	 * 关闭空气质量页面
	 */
	public void close() {

	}

}
