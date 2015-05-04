package com.wise.show;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.net.NetThread;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 *@author honesty
 **/
public class TestShowActivity extends Activity{
	private static final int getFristImage = 1;
	MyScrollView myScrollView;
	AppApplication app;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_waterfalls);
		app = (AppApplication)getApplication();
		myScrollView = (MyScrollView)findViewById(R.id.my_scroll_view);
		RefreshableView ll_refresh = (RefreshableView)findViewById(R.id.ll_refresh);
		String url = Constant.BaseUrl + "photo?auth_code=" + app.auth_code
				+  "&cust_id=" + app.cust_id + "&photo_type=1";
		new NetThread.GetDataThread(handler, url, getFristImage).start();
	}
	List<ImageData> imageDatas = new ArrayList<ImageData>();
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			List<ImageData> iDatas = jsonImages(msg.obj.toString());
			imageDatas.addAll(iDatas);
			myScrollView.resetImages(iDatas);
		}		
	};
	private List<ImageData> jsonImages(String result) {
		List<ImageData> Datas = new ArrayList<ImageData>();
		try {
			JSONArray jsonArray = new JSONArray(result);
			for (int i = 0; i < jsonArray.length(); i++) {
				ImageData imageData = new ImageData();
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				imageData.setCust_praise(jsonObject.getBoolean("cust_praise"));
				imageData.setCar_series(jsonObject.getString("car_series"));
				imageData.setCreate_time(jsonObject.getString("create_time"));
				imageData.setPhoto_id(jsonObject.getInt("photo_id"));
				imageData.setPraise_count(jsonObject.getInt("praise_count"));
				imageData.setSmall_pic_url(jsonObject
						.getString("small_pic_url"));
				imageData.setCar_brand_id(jsonObject.getString("car_brand_id"));
				imageData.setSex(jsonObject.getInt("sex") == 0 ? true : false);
				Datas.add(imageData);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Datas;
	}
}