package com.wise.baba;

import java.util.List;

import com.wise.baba.entity.AQIEntity;
import com.wise.baba.ui.widget.SplineChartView;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_air_quality_index);
		initView();
	}

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

		SplineChartView myChatView = new SplineChartView(this);
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
