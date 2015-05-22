package com.wise.baba.ui.fragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Const;
import com.wise.baba.biz.Judge;
import com.wise.baba.entity.CarData;
import com.wise.baba.ui.adapter.OnCardMenuListener;
import com.wise.baba.ui.widget.CustomGridView;
import com.wise.car.CarLocationActivity;
import com.wise.car.TravelActivity;
import com.wise.remind.RemindListActivity;
import com.wise.setting.LoginActivity;
import com.wise.state.DriveActivity;
import com.wise.state.FaultDetectionActivity;
import com.wise.state.FuelActivity;
import com.wise.state.FuelDetailsActivity;
import com.wise.violation.TrafficActivity;


/**
 * 
 * @author c 首页导航信息
 * 
 */
public class FragmentHomeNavigation extends Fragment implements
		OnItemClickListener, android.view.View.OnClickListener {

	private ImageView imgDown;// 下拉箭头
	private CustomGridView gridNav = null;// 导航信息
	private View view;
	AppApplication app;

	/**
	 * 
	 * 导航信息GridView适配器
	 */
	public SimpleAdapter getAdapter() {
		// 生成动态数组，并且转入数据
		ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
		int[] imgId = { R.drawable.ico_nav_location, R.drawable.ico_nav_trip,
				R.drawable.ico_nav_health, R.drawable.ico_nav_drive,
				R.drawable.ico_nav_oil, R.drawable.ico_nav_expenses,
				R.drawable.ico_nav_detail, R.drawable.ico_nav_notice,
				R.drawable.ico_nav_query };
		String[] text = getNavName();
		HashMap<String, Object> map;
		for (int i = 0; i < imgId.length; i++) {
			map = new HashMap<String, Object>();
			map.put("imgItem", imgId[i]);// 添加图像资源的ID
			map.put("textItem", text[i]);// 文字描述
			listItem.add(map);
		}

		SimpleAdapter saImageItems = new SimpleAdapter(this.getActivity(),
				listItem, R.layout.item_home_nav, new String[] { "imgItem",
						"textItem" }, new int[] { R.id.imgItem, R.id.textItem });

		return saImageItems;
	}

	public String[] getNavName() {
		String[] navigations = { "位置监控", "车辆行程", "车辆体检", "驾驶得分", "油耗分析",
				"费用分析", "费用明细", "车务提醒", "违章查询" };
		return navigations;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (view == null) {
			view = inflater.inflate(R.layout.fragment_home_nav, container,
					false);
		} else {
			ViewGroup parent = (ViewGroup) view.getParent();
			if (null != parent) {
				parent.removeView(view);
			}
		}

		imgDown = (ImageView) view.findViewById(R.id.imgDown);
		imgDown.setOnClickListener(this);
		gridNav = (CustomGridView) view.findViewById(R.id.gridNav);
		gridNav.setSelector(new ColorDrawable(Color.TRANSPARENT));// 取消GridView中Item选中时默认的背景色
		SimpleAdapter simpleAdapter = getAdapter();
		gridNav.setAdapter(simpleAdapter);
		simpleAdapter.notifyDataSetChanged();
		gridNav.setOnItemClickListener(this);
		app = (AppApplication) getActivity().getApplication();
		return view;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		

		Class toActivity = null;

		switch (position) {
		case 0:// 位置监控
			if (Judge.isLogin(app) == false) {
				Intent intentLogin = new Intent(getActivity(), LoginActivity.class);
				startActivity(intentLogin);
				return;
			}
			Intent intentLocation = new Intent(getActivity(), CarLocationActivity.class);
			intentLocation.putExtra("isHotLocation", true);
			intentLocation.putExtra("carDatas", (Serializable)app.carDatas);
			intentLocation.putExtra("index", app.currentCarIndex);
			startActivity(intentLocation);
			return;
		case 1:// 车辆行程
			Intent intentTravel = new Intent(getActivity(), TravelActivity.class);
			CarData carData = app.carDatas.get(app.currentCarIndex);
			intentTravel.putExtra("device_id",carData.getDevice_id());
			String Gas_no = "93#(92#)";
			if (carData.getGas_no() != null) {
				Gas_no = carData.getGas_no();
			}
			intentTravel.putExtra("Gas_no", Gas_no);
			startActivity(intentTravel);
			return;
		case 2:// 车辆体检
			Intent intentDetection = new Intent(this.getActivity(),
					FaultDetectionActivity.class);
			intentDetection.putExtra("carDatas", (Serializable)app.carDatas);
			intentDetection.putExtra("index", app.currentCarIndex);
			startActivity(intentDetection);
			return;
			
		case 3:// 驾驶得分
			toActivity = DriveActivity.class;
			break;
		case 4:// 油耗分析
			int FUEL = 3;
			Intent intentFuel = new Intent(this.getActivity(),
					FuelActivity.class);
			// 传递跳转类型常量进行跳转
			intentFuel.putExtra("type", FUEL);
			this.startActivity(intentFuel);
			return;
		case 5:// 费用分析
			int Fee = 2;
			Intent intentFee = new Intent(this.getActivity(),
					FuelActivity.class);
			// 传递跳转类型常量进行跳转
			intentFee.putExtra("type", Fee);
			this.startActivity(intentFee);
			return;
		case 6:// 费用明细
			toActivity = FuelDetailsActivity.class;
			break;
		case 7:// 车务提醒

			if (Judge.isLogin(app)) {
				Intent intentRemind = new Intent(getActivity(),RemindListActivity.class);
				intentRemind.putExtra("carDatas", (Serializable)app.carDatas);
				intentRemind.putExtra("cust_id", app.cust_id);
				startActivity(intentRemind);
				
			} else {
				Intent intent = new Intent(getActivity(), LoginActivity.class);
				intent.putExtra("ActivityState", 4);
				startActivity(intent);
			}
			return;
		case 8:// 违章查询

			if (Judge.isLogin(app)) {
				app.vio_count = 0;
				Intent intentTraffic = new Intent(getActivity(), TrafficActivity.class);
				intentTraffic.putExtra("carDatas", (Serializable)app.carDatas);
				intentTraffic.putExtra("isService", false);
				startActivity(intentTraffic);
			} else {
				Intent intent = new Intent(getActivity(), LoginActivity.class);
				intent.putExtra("ActivityState", 3);
				startActivity(intent);
			}
			return;
		}
		Intent intent = new Intent(this.getActivity(), toActivity);
		this.startActivity(intent);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.imgDown:
			if (onCardMenuListener != null) {
				onCardMenuListener.showCarMenu(Const.TAG_NAV);
			}
			break;
		}

	}

	OnCardMenuListener onCardMenuListener;

	public void setOnCardMenuListener(OnCardMenuListener onCardMenuListener) {
		this.onCardMenuListener = onCardMenuListener;

	}

}
