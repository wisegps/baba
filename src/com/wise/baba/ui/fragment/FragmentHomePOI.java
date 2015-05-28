package com.wise.baba.ui.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Const;
import com.wise.baba.db.SharePOI;
import com.wise.baba.ui.adapter.OnCardMenuListener;
import com.wise.baba.ui.adapter.ViewPagerAdapter;
import com.wise.baba.ui.widget.CustomGridView;
import com.wise.car.AddressActivity;
import com.wise.car.CarLocationActivity;
import com.wise.car.SearchLocationActivity;
import com.wise.car.SearchMapActivity;

/**
 * 
 * @author c 首页周边信息
 * 
 */
public class FragmentHomePOI extends Fragment implements OnItemClickListener,
		android.view.View.OnClickListener, OnPageChangeListener {

	private ImageView iv_poi_menu;// 下拉箭头
	private AutoCompleteTextView autoTextSearch;// 搜索框
	private ViewPager viewPagerPOI;//周边信息
	private View view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (view == null) {
			view = inflater.inflate(R.layout.fragment_home_poi, container,
					false);
		} else {
			ViewGroup parent = (ViewGroup) view.getParent();
			if (null != parent) {
				parent.removeView(view);
			}
		}

		iv_poi_menu = (ImageView) view.findViewById(R.id.iv_poi_menu);
		autoTextSearch = (AutoCompleteTextView) view
				.findViewById(R.id.autoTextSearch);
		autoTextSearch.setOnClickListener(this);
		iv_poi_menu.setOnClickListener(this);

		initPagerView();

		return view;
	}

	/**
	 * 创建周边信息ViewPager
	 */
	public void initPagerView() {

		// 首先创建子项
		CustomGridView gridPOI0 = (CustomGridView) LayoutInflater.from(this.getActivity())
				.inflate(R.layout.item_pager_poi, null);
		CustomGridView gridPOI1 = (CustomGridView) LayoutInflater.from(this.getActivity())
				.inflate(R.layout.item_pager_poi, null);

		// 取消GridView中Item选中时默认的背景色
		gridPOI0.setSelector(new ColorDrawable(Color.TRANSPARENT));
		gridPOI1.setSelector(new ColorDrawable(Color.TRANSPARENT));

		// 子项适配器
		SimpleAdapter simpleAdapter0 = getAdapter(0);
		SimpleAdapter simpleAdapter1 = getAdapter(1);

		gridPOI0.setAdapter(simpleAdapter0);
		gridPOI1.setAdapter(simpleAdapter1);

		simpleAdapter0.notifyDataSetChanged();
		simpleAdapter1.notifyDataSetChanged();

		// 子项item点击事件
		gridPOI0.setOnItemClickListener(this);
		gridPOI1.setOnItemClickListener(this);

		// 分页显示
		List<View> list = new ArrayList<View>();
		list.add(gridPOI0);
		list.add(gridPOI1);
		viewPagerPOI = (ViewPager) view.findViewById(R.id.viewPagerPOI);
		viewPagerPOI.setAdapter(new ViewPagerAdapter(list));
		
		
		//切换页面时 小圆点变化
		viewPagerPOI.setOnPageChangeListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (position == 0 && viewPagerPOI.getCurrentItem() == 0) {// 家
			SharePOI share = new SharePOI(this.getActivity());
			double[] location = share.getHomeLocation();

			if (location[0] == 0.0 && location[1] == 0.0) {
				Toast.makeText(this.getActivity(), "家的地址未设置",
						Toast.LENGTH_SHORT).show();
				startActivity(new Intent(this.getActivity(),
						AddressActivity.class));
			} else {
				Intent intent = new Intent(this.getActivity(),
						CarLocationActivity.class);
				intent.putExtra("isHotLocation", true);
				intent.putExtra("POI_FLAG", "home");
				startActivity(intent);
			}

		} else if (position == 1 && viewPagerPOI.getCurrentItem() == 0) {// 公司

			SharePOI share = new SharePOI(this.getActivity());
			double[] location = share.getCompanyLocation();

			if (location[0] == 0.0 && location[1] == 0.0) {
				Toast.makeText(this.getActivity(), "公司的地址未设置",
						Toast.LENGTH_SHORT).show();
				startActivity(new Intent(this.getActivity(),
						AddressActivity.class));
			} else {
				Intent intent = new Intent(this.getActivity(),
						CarLocationActivity.class);
				intent.putExtra("isHotLocation", true);
				intent.putExtra("POI_FLAG", "company");
				startActivity(intent);
			}

		} else {
			int currentPage = viewPagerPOI.getCurrentItem();
			toSearchMap(getPOIName(currentPage)[position]);
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.autoTextSearch:
			Intent intent = new Intent(this.getActivity(),
					SearchLocationActivity.class);
			// intent.putExtra("POI_FLAG", "autoTextSearch");
			startActivity(intent);
			break;
		case R.id.iv_poi_menu:
			if (onCardMenuListener != null) {
				onCardMenuListener.showCarMenu(Const.TAG_POI);
			}
			break;
		}

	}

	/**
	 * 根据类型跳转搜索
	 * 
	 * @param keyWord
	 */
	private void toSearchMap(String keyWord) {
		AppApplication app = (AppApplication) getActivity().getApplication();
		if (app.carDatas == null || app.carDatas.size() == 0) {
			return;
		}
		// 地图搜寻
		Intent intent = new Intent(FragmentHomePOI.this.getActivity(),
				SearchMapActivity.class);
		intent.putExtra("keyWord", keyWord);
		intent.putExtra("key", keyWord);
		startActivity(intent);
	}

	OnCardMenuListener onCardMenuListener;

	public void setOnCardMenuListener(OnCardMenuListener onCardMenuListener) {
		this.onCardMenuListener = onCardMenuListener;

	}

	/**
	 * 
	 * @周边信息GridView适配器
	 */
	public SimpleAdapter getAdapter(int page) {
		// 生成动态数组，并且转入数据
		ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();

		int[] imgId = null;
		if (page == 0) {
			imgId = new int[] { R.drawable.ico_poi_home,
					R.drawable.ico_poi_company, R.drawable.ico_poi_gas,
					R.drawable.ico_poi_parking, R.drawable.ico_poi_food,
					R.drawable.ico_poi_hotel, R.drawable.ico_poi_movie,
					R.drawable.ico_poi_scenic };
		} else {
			imgId = new int[] { R.drawable.ico_poi_bank,
					R.drawable.ico_poi_hospital, R.drawable.ico_poi_shop,

					R.drawable.ico_poi_atm, R.drawable.ico_poi_metro,
					R.drawable.ico_poi_bus, R.drawable.ico_poi_repair,
					R.drawable.ico_poi_beauty };
		}

		String[] text = getPOIName(page);
		HashMap<String, Object> map;
		for (int i = 0; i < imgId.length; i++) {
			map = new HashMap<String, Object>();
			map.put("imgItem", imgId[i]);// 添加图像资源的ID
			map.put("textItem", text[i]);// 文字描述
			listItem.add(map);
		}

		SimpleAdapter saImageItems = new SimpleAdapter(this.getActivity(),
				listItem, R.layout.item_home_poi, new String[] { "imgItem",
						"textItem" }, new int[] { R.id.imgItem, R.id.textItem });

		return saImageItems;
	}

	public String[] getPOIName(int page) {
		if (page == 0) {
			return new String[] { "家", "公司", "加油站", "停车场", "美食", "酒店", "电影",
					"景点" };
		} else {
			return new String[] { "银行", "医院", "商场", "ATM", "地铁站", "公交站",
					"汽车维修", "汽车美容" };
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int page) {
		ImageView imgCircleDot0 = (ImageView) this.getActivity().findViewById(R.id.iv_circle_page_0);
		ImageView imgCircleDot1 = (ImageView) this.getActivity().findViewById(R.id.iv_circle_page_1);
		
		if(page%2 == 0){
			imgCircleDot0.setImageResource(R.drawable.img_circle_dot_blue);
			imgCircleDot1.setImageResource(R.drawable.img_circle_dot_blue_light);
		}else{
			imgCircleDot0.setImageResource(R.drawable.img_circle_dot_blue_light);
			imgCircleDot1.setImageResource(R.drawable.img_circle_dot_blue);
		}
	}

}
