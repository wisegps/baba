package com.wise.car;

import java.util.ArrayList;
import java.util.Collections;
import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.wise.baba.R;
import com.wise.baba.biz.GetSystem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 离线地图
 *@author honesty
 **/
public class OfflineActivity extends Activity implements MKOfflineMapListener{
	
	TextView tv_offline,tv_citys,tv_update,tv_down,tv_pause;
	ViewPager viewPager;
	ArrayList<View> pageViews = new ArrayList<View>();
	private MKOfflineMap mOffline = null;
	/**所有离线城市**/
	ArrayList<CitysData> allCities = new ArrayList<CitysData>();
	ArrayList<ArrayList<CitysData>> lists = new ArrayList<ArrayList<CitysData>>();
	CitysAdapter citysAdapter;
	/**已下载城市信息**/
	ArrayList<MKOLUpdateElement> localMapList = new ArrayList<MKOLUpdateElement>();
	OfflineAdapter offlineAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_offline);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		tv_offline = (TextView)findViewById(R.id.tv_offline);
		tv_offline.setOnClickListener(onClickListener);
		tv_citys = (TextView)findViewById(R.id.tv_citys);
		tv_citys.setOnClickListener(onClickListener);
		mOffline = new MKOfflineMap();
		mOffline.init(this);
		viewPager = (ViewPager)findViewById(R.id.vp_offline);
		viewPager.setOnPageChangeListener(onPageChangeListener);
		setPageViews();
		viewPager.setAdapter(new GuidePageAdapter());
	}
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.tv_offline:
				viewPager.setCurrentItem(0);
				break;
			case R.id.tv_citys:
				viewPager.setCurrentItem(1);
				break;
			case R.id.tv_update:
				setMapUpdate();
				break;
			case R.id.tv_down:
				setMapDown();
				break;
			case R.id.tv_pause:
				setMapPause();
				break;				
			}
		}
	};
	/**获取所有离线城市**/
	private void setAllCityDatas(){		
		allCities.clear();
		lists.clear();
		ArrayList<MKOLSearchRecord> records2 = mOffline.getOfflineCityList();
			for (MKOLSearchRecord r : records2) {
				CitysData citysData = new CitysData();
				citysData.setExpandable(false);
				citysData.setCityID(r.cityID);
				citysData.setCityName(r.cityName);
				citysData.setCitySize(formatDataSize(r.size));
				citysData.setDown(false);
				for(MKOLUpdateElement mkolUpdateElement : localMapList){
					if(mkolUpdateElement.cityID == r.cityID){
						citysData.setDown(true);
					}
				}
				if(r.childCities != null){
					citysData.setGroup(true);
					ArrayList<CitysData> citysDatas = new ArrayList<CitysData>();
					//添加全省地图包
					CitysData cityProince = new CitysData();
					cityProince.setCityID(r.cityID);
					cityProince.setCityName("全省地图包");
					cityProince.setCitySize(formatDataSize(r.size));
					cityProince.setDown(true);
					citysDatas.add(cityProince);
					boolean isProniceDown = true;
					
					for(MKOLSearchRecord r2 : r.childCities){
						CitysData citysData1 = new CitysData();
						citysData1.setCityID(r2.cityID);
						citysData1.setCityName(r2.cityName);
						citysData1.setCitySize(formatDataSize(r2.size));
						citysData1.setDown(false);
						for(MKOLUpdateElement mkolUpdateElement : localMapList){
							if(mkolUpdateElement.cityID == r2.cityID){
								citysData1.setDown(true);
								break;
							}
						}
						citysDatas.add(citysData1);
						if(!citysData1.isDown){
							isProniceDown = false;
						}
					}
					if(!isProniceDown){
						citysDatas.get(0).setDown(false);
					}
					lists.add(citysDatas);
				}else{
					citysData.setGroup(false);
					ArrayList<CitysData> citysDatas = new ArrayList<CitysData>();
					lists.add(citysDatas);
				}
				allCities.add(citysData);
			}
	}
	/**获取所有离线城市**/
	private void setOfflineCityData(){
		localMapList = mOffline.getAllUpdateInfo();
		if (localMapList == null) {
			localMapList = new ArrayList<MKOLUpdateElement>();
		}
		Collections.sort(localMapList, new Comparator());// 排序
		offlineAdapter.notifyDataSetChanged();
		for(MKOLUpdateElement m : localMapList){
			if(m.update){
				tv_update.setEnabled(true);
				tv_update.setTextColor(getResources().getColor(R.color.black));
				break;
			}
		}
	}
	private void setMapDown(){
		tv_down.setEnabled(false);
		tv_down.setTextColor(getResources().getColor(R.color.gray));
		tv_pause.setEnabled(true);
		tv_pause.setTextColor(getResources().getColor(R.color.black));
		for(MKOLUpdateElement m : localMapList){
			if(m.ratio != 100){
				mOffline.start(m.cityID);
			}
		}
	}
	//暂停
	private void setMapPause(){
		tv_pause.setEnabled(false);
		tv_pause.setTextColor(getResources().getColor(R.color.gray));
		tv_down.setEnabled(true);
		tv_down.setTextColor(getResources().getColor(R.color.black));
		for(MKOLUpdateElement m : localMapList){
			mOffline.pause(m.cityID);
		}
	}
	/**更新**/
	private void setMapUpdate(){
		tv_update.setEnabled(false);
		tv_update.setTextColor(getResources().getColor(R.color.gray));
		for(MKOLUpdateElement m : localMapList){
			if(m.update){
				mOffline.remove(m.cityID);
				mOffline.start(m.cityID);
			}
		}
	}
	class Comparator implements java.util.Comparator<MKOLUpdateElement> {
		@Override
		public int compare(MKOLUpdateElement lhs, MKOLUpdateElement rhs) {
			int m1 = lhs.cityID;
			int m2 = rhs.cityID;
			int result = 0;
			if (m1 > m2) {
				result = 1;
			}
			if (m1 < m2) {
				result = -1;
			}
			return result;
		}
	}
	/**离线地图包大小转换**/
	public String formatDataSize(int size) {
		String ret = "";
		if (size < (1024 * 1024)) {
			ret = String.format("%dK", size / 1024);
		} else {
			ret = String.format("%.1fM", size / (1024 * 1024.0));
		}
		return ret;
	}
	private void setPageViews(){
		View view_offline_down = LayoutInflater.from(this).inflate(
				R.layout.item_vp_offline_down, null);
		ListView lv_offline = (ListView)view_offline_down.findViewById(R.id.lv_offline);
		offlineAdapter = new OfflineAdapter();
		lv_offline.setAdapter(offlineAdapter);
		tv_update = (TextView)view_offline_down.findViewById(R.id.tv_update);
		tv_update.setOnClickListener(onClickListener);
		tv_down = (TextView)view_offline_down.findViewById(R.id.tv_down);
		tv_down.setOnClickListener(onClickListener);
		tv_pause = (TextView)view_offline_down.findViewById(R.id.tv_pause);
		tv_pause.setOnClickListener(onClickListener);
		setOfflineCityData();
		
		View view_citys = LayoutInflater.from(this).inflate(
				R.layout.item_vp_citys, null);
		ExpandableListView elv_citys = (ExpandableListView)view_citys.findViewById(R.id.elv_citys);
		elv_citys.setGroupIndicator(null);
		setAllCityDatas();
		citysAdapter = new CitysAdapter();
		elv_citys.setAdapter(citysAdapter);
		elv_citys.setOnChildClickListener(onChildClickListener);
		elv_citys.setOnGroupClickListener(onGroupClickListener);
		
		pageViews.add(view_offline_down);
		pageViews.add(view_citys);
	}
	
	OnGroupClickListener onGroupClickListener = new OnGroupClickListener() {		
		@Override
		public boolean onGroupClick(ExpandableListView parent, View v,
				final int groupPosition, long id) {
			
			if(GetSystem.isWifi(getApplicationContext())){
				CitysData citysData = allCities.get(groupPosition);
				citysData.setExpandable(!citysData.isExpandable);
				if(citysData.isGroup){
					
				}else{
					if(citysData.isDown){
						
					}else{
						mOffline.start(citysData.getCityID());
						citysData.setDown(true);
						viewPager.setCurrentItem(0);
					}				
				}
				citysAdapter.notifyDataSetChanged();
			}else{
				AlertDialog.Builder dialog = new AlertDialog.Builder(OfflineActivity.this); 
				dialog.setTitle("提示");  
				dialog.setMessage("您现在处于非WIFI状态，下载需要耗费流量，是否继续？"); 
				dialog.setNegativeButton("取消", null);
				dialog.setPositiveButton("确定", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						CitysData citysData = allCities.get(groupPosition);
						citysData.setExpandable(!citysData.isExpandable);
						if(citysData.isGroup){
							
						}else{
							if(citysData.isDown){
								
							}else{
								mOffline.start(citysData.getCityID());
								citysData.setDown(true);
								viewPager.setCurrentItem(0);
							}				
						}
						citysAdapter.notifyDataSetChanged();
					}});
				dialog.show();
			}
			
			return false;
		}
	};
	OnChildClickListener onChildClickListener = new OnChildClickListener() {		
		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
				final int groupPosition, final int childPosition, long id) {
			if(GetSystem.isWifi(getApplicationContext())){
				CitysData citysData = lists.get(groupPosition).get(childPosition);
				if(citysData.isDown){
					
				}else{
					if(childPosition == 0){
						//TODO 点击的是全省地图包
						for(int i = 0 ; i < lists.get(groupPosition).size() ; i++){
							CitysData citysData2 = lists.get(groupPosition).get(i);
							citysData2.setDown(true);
							if(i == 0){
								//去掉全省										
							}else{
								//判断市是否有下载的
								boolean isDown = false;
								for(MKOLUpdateElement mkolUpdateElement : localMapList){
									if(mkolUpdateElement.cityID == citysData2.cityID){
										isDown = true;
										break;
									}else{
										
									}
								}
								if(!isDown){
									mOffline.start(citysData2.getCityID());
								}
							}
						}
					}else{
						citysData.setDown(true);
						mOffline.start(citysData.getCityID());
					}
					viewPager.setCurrentItem(0);
					citysAdapter.notifyDataSetChanged();
				}
			}else{
				AlertDialog.Builder dialog = new AlertDialog.Builder(OfflineActivity.this); 
				dialog.setTitle("提示");  
				dialog.setMessage("您现在处于非WIFI状态，下载需要耗费流量，是否继续？"); 
				dialog.setNegativeButton("取消", null);
				dialog.setPositiveButton("确定", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						CitysData citysData = lists.get(groupPosition).get(childPosition);
						if(citysData.isDown){
							
						}else{
							if(childPosition == 0){
								//TODO 点击的是全省地图包
								for(int i = 0 ; i < lists.get(groupPosition).size() ; i++){
									CitysData citysData2 = lists.get(groupPosition).get(i);
									citysData2.setDown(true);
									if(i == 0){
										//去掉全省										
									}else{
										//判断市是否有下载的
										for(MKOLUpdateElement mkolUpdateElement : localMapList){
											if(mkolUpdateElement.cityID == citysData2.cityID){
												
											}else{
												mOffline.start(citysData2.getCityID());
											}
										}
									}
								}
							}else{
								citysData.setDown(true);
								mOffline.start(citysData.getCityID());
							}
							viewPager.setCurrentItem(0);
							citysAdapter.notifyDataSetChanged();
						}
					}});
				dialog.show();
			}
			
			return false;
		}
	};
	
	class CitysAdapter extends BaseExpandableListAdapter{

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return lists.get(groupPosition).get(childPosition);
		}
		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}
		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			return getView(false, groupPosition, childPosition);
		}
		@Override
		public int getChildrenCount(int groupPosition) {
			return lists.get(groupPosition).size();
		}
		@Override
		public Object getGroup(int groupPosition) {
			return allCities.get(groupPosition);
		}
		@Override
		public int getGroupCount() {
			return allCities.size();
		}
		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}
		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			return getView(true, groupPosition, 0);
		}
		@Override
		public boolean hasStableIds() {
			return false;
		}
		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
		private View getView(boolean isGroup ,int groupPosition, int position){
			View viewCity = LayoutInflater.from(OfflineActivity.this).inflate(R.layout.item_vp_citys_item, null);
			TextView tv_city_name = (TextView)viewCity.findViewById(R.id.tv_city_name);
			TextView tv_city_size = (TextView)viewCity.findViewById(R.id.tv_city_size);
			ImageView iv_down = (ImageView)viewCity.findViewById(R.id.iv_down);
			if(isGroup){
				CitysData citysData = allCities.get(groupPosition);
				if(citysData.isGroup){
					if(citysData.isExpandable){
						iv_down.setImageResource(R.drawable.localmap_expand);
					}else{
						iv_down.setImageResource(R.drawable.localmap_no_expand);
					}
				}else{
					if(citysData.isDown){
						iv_down.setImageResource(R.drawable.localmap_download_disabled);
					}else{
						iv_down.setImageResource(R.drawable.localmap_citylist_download_btn_enabled);
					}
				}				
				tv_city_name.setText(citysData.getCityName());
				tv_city_size.setText(citysData.getCitySize());
			}else{
				CitysData citysData = lists.get(groupPosition).get(position);
				if(citysData.isDown){
					iv_down.setImageResource(R.drawable.localmap_download_disabled);
				}else{
					iv_down.setImageResource(R.drawable.localmap_citylist_download_btn_enabled);
				}
				tv_city_name.setText(citysData.getCityName());
				tv_city_size.setText(citysData.getCitySize());
			}
			return viewCity;
		}
	}
	LinearLayout opeLinearLayout;
	ImageView iv_down;
	class OfflineAdapter extends BaseAdapter{
		LayoutInflater layoutInflater = LayoutInflater.from(OfflineActivity.this);
		@Override
		public int getCount() {
			return localMapList.size();
		}
		@Override
		public Object getItem(int position) {
			return localMapList.get(position);
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder viewHolder;
			if(convertView == null){
				convertView = layoutInflater.inflate(R.layout.item_vp_offline_item, null);
				viewHolder = new ViewHolder();
				viewHolder.tv_city_name = (TextView)convertView.findViewById(R.id.tv_city_name);
				viewHolder.tv_city_size = (TextView)convertView.findViewById(R.id.tv_city_size);
				viewHolder.iv_down = (ImageView)convertView.findViewById(R.id.iv_down);
				viewHolder.bt_update = (TextView)convertView.findViewById(R.id.bt_update);
				viewHolder.bt_delete = (TextView)convertView.findViewById(R.id.bt_delete);
				viewHolder.ll_menu = (LinearLayout)convertView.findViewById(R.id.ll_menu);
				viewHolder.rl_offline = (RelativeLayout)convertView.findViewById(R.id.rl_offline);
				convertView.setTag(viewHolder);
			}else{
				viewHolder = (ViewHolder)(convertView.getTag());
			}
			final MKOLUpdateElement m = localMapList.get(position);
			viewHolder.ll_menu.setVisibility(View.GONE);
			viewHolder.tv_city_name.setText(m.cityName);
			viewHolder.rl_offline.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					if(viewHolder.ll_menu.getVisibility() == View.VISIBLE){
						viewHolder.ll_menu.setVisibility(View.GONE);
						viewHolder.iv_down.setImageResource(R.drawable.localmap_no_expand);
					}else{
						if(opeLinearLayout != null){
							opeLinearLayout.setVisibility(View.GONE);
							iv_down.setImageResource(R.drawable.localmap_no_expand);
						}
						viewHolder.ll_menu.setVisibility(View.VISIBLE);
						viewHolder.iv_down.setImageResource(R.drawable.localmap_expand);
						opeLinearLayout = viewHolder.ll_menu;
						iv_down = viewHolder.iv_down;
					}					
				}
			});
			//控制
			if(m.status == MKOLUpdateElement.SUSPENDED){
				viewHolder.bt_update.setText("下载地图");
				viewHolder.bt_update.setEnabled(true);
				viewHolder.bt_update.setVisibility(View.VISIBLE);
			}else if(m.status == MKOLUpdateElement.DOWNLOADING){
				viewHolder.bt_update.setText("暂停");
				viewHolder.bt_update.setEnabled(true);
				viewHolder.bt_update.setVisibility(View.VISIBLE);
			}else if(m.status == MKOLUpdateElement.FINISHED){
				viewHolder.bt_update.setText("下载地图");
				viewHolder.bt_update.setEnabled(false);
				viewHolder.bt_update.setVisibility(View.VISIBLE);
			}
			viewHolder.bt_update.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					if(m.status == MKOLUpdateElement.SUSPENDED){
						mOffline.start(m.cityID);
					}else if(m.status == MKOLUpdateElement.DOWNLOADING){
						mOffline.pause(m.cityID);
						setOfflineCityData();
						citysAdapter.notifyDataSetChanged();
					}else if(m.update){
						mOffline.remove(m.cityID);
						mOffline.start(m.cityID);
					}
				}
			});
			viewHolder.bt_delete.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					mOffline.remove(m.cityID);
					setOfflineCityData();
					setAllCityDatas();
					citysAdapter.notifyDataSetChanged();
				}
			});
			if (m.ratio != 100) {
				viewHolder.bt_update.setEnabled(true);
				viewHolder.bt_update.setVisibility(View.VISIBLE);
				viewHolder.tv_city_size.setText("更新进度：" + m.ratio + "%");
				viewHolder.tv_city_size.setTextColor(getResources().getColor(R.color.pink));
			}else{
				if(m.update){//可更新
					viewHolder.bt_update.setEnabled(true);
					viewHolder.bt_update.setVisibility(View.VISIBLE);
					viewHolder.tv_city_size.setText("(有更新包-" + formatDataSize(m.serversize) + ")");
					viewHolder.tv_city_size.setTextColor(getResources().getColor(R.color.Green));
				}else{
					viewHolder.bt_update.setEnabled(false);
					viewHolder.bt_update.setText("下载地图");
					viewHolder.bt_update.setVisibility(View.INVISIBLE);
					viewHolder.tv_city_size.setText("(" + formatDataSize(m.size) + ")");
					viewHolder.tv_city_size.setTextColor(getResources().getColor(R.color.navy));
				}
			}			
			return convertView;
		}
		private class ViewHolder{
			TextView tv_city_name,tv_city_size;
			TextView bt_update,bt_delete;
			ImageView iv_down;
			LinearLayout ll_menu;
			RelativeLayout rl_offline;
		}
	}
	class GuidePageAdapter extends PagerAdapter {
		@Override
		public int getCount() {
			return pageViews.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView(pageViews.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			((ViewPager) container).addView(pageViews.get(position));
			return pageViews.get(position);
		}
	}
	
	OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {		
		@Override
		public void onPageSelected(int arg0) {
			setBg();
			switch (arg0) {
			case 0:
				tv_offline.setTextColor(getResources().getColor(R.color.white));
				tv_offline.setBackgroundResource(R.drawable.bg_border_left_press);
				break;
			case 1:
				tv_citys.setTextColor(getResources().getColor(R.color.white));
				tv_citys.setBackgroundResource(R.drawable.bg_border_right_press);
				break;
			}
		}		
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {}		
		@Override
		public void onPageScrollStateChanged(int arg0) {}
	};
	private void setBg() {
		tv_offline.setTextColor(getResources().getColor(R.color.Green));
		tv_offline.setBackgroundResource(R.drawable.bg_border_left);
		tv_citys.setTextColor(getResources().getColor(R.color.Green));
		tv_citys.setBackgroundResource(R.drawable.bg_border_right);
	}
	
	private class CitysData{
		private int cityID;
		private String cityName;
		private String citySize;
		private boolean isDown;
		private boolean isGroup;
		private boolean isExpandable;
				
		public boolean isExpandable() {
			return isExpandable;
		}
		public void setExpandable(boolean isExpandable) {
			this.isExpandable = isExpandable;
		}
		public int getCityID() {
			return cityID;
		}
		public void setCityID(int cityID) {
			this.cityID = cityID;
		}
		public String getCityName() {
			return cityName;
		}
		public void setCityName(String cityName) {
			this.cityName = cityName;
		}
		public boolean isDown() {
			return isDown;
		}
		public void setDown(boolean isDown) {
			this.isDown = isDown;
		}		
		public String getCitySize() {
			return citySize;
		}
		public void setCitySize(String citySize) {
			this.citySize = citySize;
		}		
		public boolean isGroup() {
			return isGroup;
		}
		public void setGroup(boolean isGroup) {
			this.isGroup = isGroup;
		}
		@Override
		public String toString() {
			return "CitysData [cityName=" + cityName + ", isDown=" + isDown
					+ "]";
		}		
	}
	@Override
	public void onGetOfflineMapState(int arg0, int arg1) {
		switch (arg0) {
		case MKOfflineMap.TYPE_DOWNLOAD_UPDATE:
			MKOLUpdateElement update = mOffline.getUpdateInfo(arg1);
			// 处理下载进度更新提示
			if (update != null) {
				setOfflineCityData();
			}
			break;

		default:
			break;
		}
	}
}