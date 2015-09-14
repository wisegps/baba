package com.wise.baba;

import java.util.List;
import com.wise.baba.entity.AQIEntity;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

/**
 * 空气质量指数
 * @author c
 *
 */


public class AirQualityIndexActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_air_quality_index);
	}
	
	
	/**
	 * 初始化页面
	 */
	public void initView(){
		
	}
	
	/**
	 * 更新空气质量数据集
	 * @param list，各个时间点空气质量 列表
	 */
	public void notifyDataSet(List<AQIEntity> list){
		
	}

	/**
	 * 弹出空气质量页面
	 */
	public void show(){
		
	}
	
	/**
	 * 关闭空气质量页面
	 */
	public void close(){
		
	}
	
	
}
