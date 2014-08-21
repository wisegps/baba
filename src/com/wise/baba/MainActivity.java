package com.wise.baba;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import pubclas.Constant;
import pubclas.NetThread;
import pubclas.Variable;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qzone.QZone;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.wise.notice.NoticeFragment;
import com.wise.notice.NoticeFragment.BtnListener;
import com.wise.state.FaultActivity;

import customView.EnergyGroup;
import customView.PopView;
import data.CarData;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
/**
 * 主界面
 * @author Administrator
 *
 */
public class MainActivity extends FragmentActivity {
	private static final String TAG = "MainActivity";
	private static final int cycle = 1;
	private static final int getJoy = 2;
	
	EnergyGroup energyGroup;
	LinearLayout ll_car;
	ListView lv_search;
	TextView tv_joy;
	PopupWindow mPopupWindow;
	RequestQueue mQueue;
	
	Platform platformQQ;
    Platform platformSina;
    
    
    private FragmentManager fragmentManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		ShareSDK.initSDK(this);
		mQueue = Volley.newRequestQueue(this);
		tv_joy = (TextView)findViewById(R.id.tv_joy);
		ll_car = (LinearLayout)findViewById(R.id.ll_car);
		ImageView iv_menu = (ImageView)findViewById(R.id.iv_menu);
		iv_menu.setOnClickListener(onClickListener);
		TextView tv_search = (TextView)findViewById(R.id.tv_search);
		tv_search.setOnClickListener(onClickListener);
		Button bt_service = (Button)findViewById(R.id.bt_service);
		bt_service.setOnClickListener(onClickListener);
		Button bt_car = (Button)findViewById(R.id.bt_car);
		bt_car.setOnClickListener(onClickListener);
		platformQQ = ShareSDK.getPlatform(MainActivity.this, QZone.NAME);
        platformSina = ShareSDK.getPlatform(MainActivity.this, SinaWeibo.NAME);
		showCar();

        energyGroup = (EnergyGroup)findViewById(R.id.eg_content);
		fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		NoticeFragment noticeFragment = new NoticeFragment();
        transaction.add(R.id.ll_notice, noticeFragment); 
        transaction.commit();
        noticeFragment.SetBtnListener(new BtnListener() {			
			@Override
			public void Back() {
				energyGroup.snapToScreen(0);
			}
		});
        new CycleThread().start();
	}
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {//TODO 点击事件
			case R.id.iv_menu:
				startActivity(new Intent(MainActivity.this, MoreActivity.class));
				break;
			case R.id.tv_search:
				ShowSearchPop();
				break;
			case R.id.bt_car:
				startActivity(new Intent(MainActivity.this, AskActivity.class));
				break;
			case R.id.ll_car:
				int i = (Integer) v.getTag();
				Intent intent = new Intent(MainActivity.this, FaultActivity.class);
				intent.putExtra("index", i);
				startActivity(intent);
				break;
			}
		}
	};
	
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case getJoy:
				jsonJoy(msg.obj.toString());
				break;
			case cycle:
				getJoy();
				break;
			}
		}		
	};
	
	private void showCar() {
		ll_car.removeAllViews();		
        for (int i = 0; i < Variable.carDatas.size(); i++) {
        	CarData carData = Variable.carDatas.get(i);
            View view = LayoutInflater.from(this).inflate(R.layout.item_home_car, null);
            ll_car.addView(view);
            LinearLayout ll_item_home_car = (LinearLayout)view.findViewById(R.id.ll_car);
            ll_item_home_car.setTag(i);
            ll_item_home_car.setOnClickListener(onClickListener);
            TextView tv_number = (TextView) view.findViewById(R.id.tv_number);
            tv_number.setText("车牌："+ carData.getNick_name() + "  品牌："+ carData.getCar_brand());
        }
    }
	
	private void testPop(){
		List<String> items = new ArrayList<String>();
		items.add("拍照");
		items.add("从手机相册中选取");
		items.add("退出");
		PopView popView = new PopView(this);
		popView.initView(findViewById(R.id.iv_menu));
		popView.setData(items);
	}
	private void ShowSearchPop() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenHeigh = dm.heightPixels;
        Rect frame = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int state_heght = frame.top;// 状态栏的高度
        int popHeight = screenHeigh - state_heght;
        
        LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popunwindwow = mLayoutInflater.inflate(R.layout.item_search,null);        
        final PopupWindow mPopupWindow = new PopupWindow(popunwindwow, LayoutParams.MATCH_PARENT,popHeight);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(false);
        mPopupWindow.showAtLocation(findViewById(R.id.ll_main),Gravity.BOTTOM, 0, 0);
        popunwindwow.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				mPopupWindow.dismiss();
			}
		});
        setData();
        lv_search = (ListView)popunwindwow.findViewById(R.id.lv_search);
        lv_search.setAdapter(new ServiceAdapter());
        lv_search.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				startActivity(new Intent(MainActivity.this, ServiceActivity.class));
			}
		});
    }
	private class ServiceAdapter extends BaseAdapter{
		LayoutInflater mInflater = LayoutInflater.from(MainActivity.this);
		@Override
		public int getCount() {
			return serviceDatas.size();
		}
		@Override
		public Object getItem(int position) {
			return serviceDatas.get(position);
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_service, null);
                holder = new ViewHolder();
                holder.tv_service_name = (TextView) convertView.findViewById(R.id.tv_service_name);                
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_service_name.setText(serviceDatas.get(position).getName());
            return convertView;
		}
		private class ViewHolder {
            TextView tv_service_name;
        }
	}
	List<ServiceData> serviceDatas = new ArrayList<ServiceData>();
	private void setData(){
		for(int i = 0 ; i < 5 ; i++){
			ServiceData serviceData = new ServiceData();
			serviceData.setName("叭叭");
			serviceDatas.add(serviceData);
		}
	}	
	private class ServiceData{
		private String name;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}		
	}
	boolean isCycle = true;
	class CycleThread extends Thread{
		@Override
		public void run() {
			super.run();
			while (isCycle) {
				try {
					Message message = new Message();
					message.what = cycle;
					handler.sendMessage(message);
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}			
		}
	}
	private void getJoy(){
		String url = Constant.BaseUrl + "base/joy";
		new NetThread.GetDataThread(handler, url, getJoy).start();
	}
	private void jsonJoy(String str){
		try {
			JSONObject jsonObject = new JSONObject(str);
			tv_joy.setText(jsonObject.getString("content"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		isCycle = false;
	}
}