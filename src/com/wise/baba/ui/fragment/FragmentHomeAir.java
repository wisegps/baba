package com.wise.baba.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wise.baba.AirSettingActivity;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Const;
import com.wise.baba.app.Msg;
import com.wise.baba.biz.HttpAir;
import com.wise.baba.biz.HttpGetObdData;
import com.wise.baba.entity.CarData;
import com.wise.baba.ui.adapter.OnCardMenuListener;
import com.wise.baba.ui.widget.HScrollLayout;
import com.wise.baba.ui.widget.OnViewChangeListener;
import com.wise.baba.ui.widget.SwitchImageView;

/**
 * 空气质量
 * 
 * @author cyy
 **/
public class FragmentHomeAir extends Fragment {
	private static final String TAG = "FragmentHomeAir";
	HScrollLayout hs_air;
	AppApplication app;
	public int index = 0;
	public HttpGetObdData http;
	public HttpAir httpAir;
	
	private OnCardMenuListener onCardMenuListener;
	private List<View>  views = new ArrayList<View>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_home_air, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		app = (AppApplication) getActivity().getApplication();
		hs_air = (HScrollLayout) getActivity().findViewById(R.id.hs_air);
		http = new HttpGetObdData(this.getActivity(), handler);
		httpAir = new HttpAir(this.getActivity(), handler);
		initDataView();
		hs_air.setOnViewChangeListener(new OnViewChangeListener() {
			@Override
			public void OnViewChange(int view, int duration) {
				if (index != view) {
					index = view;
					http.requestAir(index);
				}
			}
		});

	}

	public void setOnCardMenuListener(OnCardMenuListener onCardMenuListener) {
		this.onCardMenuListener = onCardMenuListener;
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_air_menu:
				if (onCardMenuListener != null) {
					onCardMenuListener.showCarMenu(Const.TAG_AIR);
				}
				break;
			case R.id.iv_air_power:
				SwitchImageView ivPower = (SwitchImageView) v;
				ivPower.setChecked(!ivPower.isChecked());
				break;
			case R.id.iv_air_auto:
				SwitchImageView ivAuto = (SwitchImageView) v;
				ivAuto.setChecked(!ivAuto.isChecked());
				break;
			case R.id.iv_air_level:
				SwitchImageView ivLevel = (SwitchImageView) v;
				ivLevel.setChecked(!ivLevel.isChecked());
				break;
			case R.id.iv_air_setting:
				Intent intent = new Intent();
				intent.setClass(FragmentHomeAir.this.getActivity(), AirSettingActivity.class);
				FragmentHomeAir.this.getActivity().startActivity(intent);
				
				break;
			
			}
		}
	};

	/** 滑动车辆布局 **/
	public void initDataView() {// 布局
		// 删除车辆后重新布局，如果删除的是最后一个车辆，则重置为第一个车
		if (index < app.carDatas.size()) {
		} else {
			index = 0;
		}
		hs_air.removeAllViews();
		
		List<CarData> carDataList = app.carDatas;
		for (int i = 0; i < carDataList.size(); i++) {
			
			
			View v = LayoutInflater.from(getActivity()).inflate(
					R.layout.page_air, null);
			
			TextView tvCardTitle = (TextView) v.findViewById(R.id.tv_card_title);
			ImageView ivAirSettting = (ImageView) v.findViewById(R.id.iv_air_setting);
			v.findViewById(R.id.iv_air_power).setOnClickListener(onClickListener);
			v.findViewById(R.id.iv_air_auto).setOnClickListener(onClickListener);
			v.findViewById(R.id.iv_air_level).setOnClickListener(onClickListener);
			
			ImageView ivAirMenu = (ImageView) v.findViewById(R.id.iv_air_menu);
			
			CarData carData = carDataList.get(i);
			
			tvCardTitle.setText(carData.getNick_name());
			ivAirSettting.setOnClickListener(onClickListener);
			ivAirMenu.setOnClickListener(onClickListener);
			
			
			views.add(v);
			hs_air.addView(v);

		}
		hs_air.snapToScreen(index);
		
		http.requestAir(index);
		
	}
	
	public void initValue(Bundle bundle){
		View view = views.get(index);
		TextView tvAirscore = (TextView) view.findViewById(R.id.tvAirscore);
		TextView tvAirDesc = (TextView) view.findViewById(R.id.tvAirDesc);
		
		int air = bundle.getInt("air");
		String desc = getAirDesc(air);
		
		tvAirDesc.setText(desc);
		tvAirscore.setText(air+"");
		httpAir.request(app.carDatas.get(index).getDevice_id(), HttpAir.COMMAND_SWITCH,  "{switch: 1}");
		
		
		
		
		
	}
	
	
	public Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case Msg.Get_OBD_Data:
				initValue(msg.getData());
				break;
			
			}
		}
		
		
	};
	
	public String getAirDesc(int air){
		 String air_desc = "优";
	      if(air <= 1300){
	        air_desc = "优";
	      }else if(air > 1300 && air <= 1500){
	        air_desc = "良";
	      }else if(air > 1500 && air <= 2000){
	        air_desc = "中";
	      }else{
	        air_desc = "差";
	      }

		return   "车内空气"+air_desc;
		
	}
	
	

}
