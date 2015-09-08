package com.wise.baba.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Const;
import com.wise.baba.ui.adapter.OnCardMenuListener;
import com.wise.baba.ui.widget.HScrollLayout;
import com.wise.baba.ui.widget.OnViewChangeListener;

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

	private OnCardMenuListener onCardMenuListener;

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
		initDataView();
		hs_air.setOnViewChangeListener(new OnViewChangeListener() {
			@Override
			public void OnViewChange(int view, int duration) {
				if (index != view) {
					index = view;
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
		for (int i = 0; i < app.carDatas.size(); i++) {
			View v = LayoutInflater.from(getActivity()).inflate(
					R.layout.page_air, null);

			hs_air.addView(v);

		}
		hs_air.snapToScreen(index);
	}

}
