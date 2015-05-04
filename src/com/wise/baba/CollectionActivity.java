package com.wise.baba;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.entity.AdressData;
import com.wise.baba.entity.CollectionData;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.adapter.CollectionAdapter;
import com.wise.baba.ui.adapter.CollectionAdapter.CollectionItemListener;
import com.wise.baba.ui.widget.WaitLinearLayout;
import com.wise.baba.ui.widget.WaitLinearLayout.OnFinishListener;

import xlist.XListView;
import xlist.XListView.IXListViewListener;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.app.Activity;

/** 收藏 **/
public class CollectionActivity extends Activity implements IXListViewListener {

	private static final int frist_getdata = 1;
	private static final int refresh_getdata = 2;
	private static final int load_getdata = 3;

	RelativeLayout rl_Note;
	WaitLinearLayout ll_wait;
	private XListView lv_collection;
	private CollectionAdapter collectionAdapter;

	List<AdressData> adressDatas = new ArrayList<AdressData>();

	boolean isGetDB = true; // 上拉是否继续读取数据库
	int Toal = 0; // 从那条记录读起
	int pageSize = 10; // 每次读取的记录数目
	AppApplication app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_collection);
		app = (AppApplication) getApplication();
		rl_Note = (RelativeLayout) findViewById(R.id.rl_Note);
		ll_wait = (WaitLinearLayout) findViewById(R.id.ll_wait);
		ll_wait.setOnFinishListener(onFinishListener);

		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);

		lv_collection = (XListView) findViewById(R.id.lv_collection);
		collectionAdapter = new CollectionAdapter(CollectionActivity.this,
				adressDatas);
		collectionAdapter.setCollectionItem(collectionItemListener);
		lv_collection.setAdapter(collectionAdapter);
		lv_collection.setPullRefreshEnable(true);
		lv_collection.setPullLoadEnable(true);
		lv_collection.setXListViewListener(this);
		lv_collection.setOnFinishListener(onFinishListener);
		lv_collection.setBottomFinishListener(onFinishListener);

		if (isGetDataUrl()) {
			// 服务器取数据
			isGetDB = false;
			ll_wait.startWheel();
			String url = Constant.BaseUrl + "customer/" + app.cust_id
					+ "/favorite?auth_code=" + app.auth_code;
			new Thread(new NetThread.GetDataThread(handler, url, frist_getdata))
					.start();
		} else {
			// 本地取数据
			getCollectionDatas(Toal, pageSize);
			collectionAdapter.notifyDataSetChanged();
			isNothingNote(false);
		}
	}

	String refresh = "";
	String load = "";

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case frist_getdata:
				ll_wait.runFast(0);
				refresh = msg.obj.toString();
				DataSupport.deleteAll(CollectionData.class);
				adressDatas.clear();
				break;
			case refresh_getdata:
				refresh = msg.obj.toString();
				lv_collection.runFast(1);
				break;
			case load_getdata:
				lv_collection.runBottomFast(2);
				load = msg.obj.toString();
				break;
			}
		}
	};

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			}
		}
	};

	OnFinishListener onFinishListener = new OnFinishListener() {
		@Override
		public void OnFinish(int index) {
			if (index == 0) {
				adressDatas.addAll(jsonCollectionData(refresh));
				collectionAdapter.notifyDataSetChanged();
				if (adressDatas.size() > 0) {
					isNothingNote(false);
				} else {
					isNothingNote(true);
				}
			} else if (index == 1) {
				DataSupport.deleteAll(CollectionData.class);
				adressDatas.clear();
				adressDatas.addAll(0, jsonCollectionData(refresh));
				collectionAdapter.notifyDataSetChanged();
			} else if (index == 2) {
				List<AdressData> ads = jsonCollectionData(load);
				adressDatas.addAll(ads);
				onLoad();
				if (ads.size() == 0) {// 没有数据，取消上拉加载
					collectionAdapter.notifyDataSetChanged();
					lv_collection.setPullLoadEnable(false);
				} else {
					collectionAdapter.notifyDataSetChanged();
				}
			}
			onLoad();
		}
	};

	CollectionItemListener collectionItemListener = new CollectionItemListener() {
		@Override
		public void Delete(int position) {
			if (app.isTest) {
				Toast.makeText(CollectionActivity.this, "演示账号不支持该功能",
						Toast.LENGTH_SHORT).show();
				return;
			}
			String url = Constant.BaseUrl + "favorite/"
					+ adressDatas.get(position).get_id() + "?auth_code="
					+ app.auth_code;
			// 删除服务器记录
			new Thread(new NetThread.DeleteThread(handler, url, 999)).start();
			// 删除本地数据库
			DataSupport.deleteAll(CollectionData.class, "favorite_id = ?",
					String.valueOf(adressDatas.get(position).get_id()));
			adressDatas.remove(position);
			collectionAdapter.notifyDataSetChanged();
		}

		@Override
		public void share(int position) {
			AdressData adressData = adressDatas.get(position);
			String url = "http://api.map.baidu.com/geocoder?location="
					+ adressData.getLat() + "," + adressData.getLon()
					+ "&coord_type=bd09ll&output=html";
			StringBuffer sb = new StringBuffer();
			sb.append("【地点】 ");
			sb.append(adressData.getName());
			sb.append(" 地址: " + adressData.getAdress());
			sb.append(" 电话: " + adressData.getPhone());
			sb.append(" " + url);
			GetSystem.share(CollectionActivity.this, sb.toString(), "",
					(float) adressData.getLat(), (float) adressData.getLon(),
					"地点", url);
		}
	};

	private void isNothingNote(boolean isNote) {
		ll_wait.setVisibility(View.GONE);
		if (isNote) {
			rl_Note.setVisibility(View.VISIBLE);
			lv_collection.setVisibility(View.GONE);
		} else {
			rl_Note.setVisibility(View.GONE);
			lv_collection.setVisibility(View.VISIBLE);
		}
	}

	private boolean isGetDataUrl() {
		List<CollectionData> collectionDatas = DataSupport.where("Cust_id = ?",
				app.cust_id).find(CollectionData.class);
		if (collectionDatas.size() == 0) {
			return true;
		} else {
			return false;
		}
	}

	private List<AdressData> jsonCollectionData(String result) {
		List<AdressData> adressDatas = new ArrayList<AdressData>();
		try {
			JSONArray jsonArray = new JSONArray(result);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				AdressData adrDatas = new AdressData();
				adrDatas.set_id(jsonObject.getInt("favorite_id"));
				adrDatas.setAdress(jsonObject.getString("address"));
				adrDatas.setName(jsonObject.getString("name"));
				adrDatas.setPhone(jsonObject.getString("tel"));
				adrDatas.setLat(Double.parseDouble(jsonObject.getString("lat")));
				adrDatas.setLon(Double.parseDouble(jsonObject.getString("lon")));
				adressDatas.add(adrDatas);

				CollectionData collectionData = new CollectionData();
				collectionData.setCust_id(app.cust_id);
				collectionData
						.setFavorite_id(String.valueOf(adrDatas.get_id()));
				collectionData.setName(adrDatas.getName());
				collectionData.setAddress(adrDatas.getAdress());
				collectionData.setTel(adrDatas.getPhone());
				collectionData.setLon(jsonObject.getString("lon"));
				collectionData.setLat(jsonObject.getString("lat"));
				collectionData.save();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return adressDatas;
	}

	@Override
	public void onRefresh() {
		refresh = "";
		lv_collection.startHeaderWheel();
		String url = Constant.BaseUrl + "customer/" + app.cust_id
				+ "/favorite?auth_code=" + app.auth_code;
		new Thread(new NetThread.GetDataThread(handler, url, refresh_getdata))
				.start();
	}

	@Override
	public void onLoadMore() {
		if (isGetDB) {// 读取数据库
			getCollectionDatas(Toal, pageSize);
			collectionAdapter.notifyDataSetChanged();
			onLoad();
		} else {// 读取服务器
			if (adressDatas.size() != 0) {
				int id = adressDatas.get(adressDatas.size() - 1).get_id();
				String url = Constant.BaseUrl + "customer/" + app.cust_id
						+ "/favorite?auth_code=" + app.auth_code + "&&min_id="
						+ id;
				new Thread(new NetThread.GetDataThread(handler, url,
						load_getdata)).start();
				lv_collection.startBottomWheel();
			}
		}
	}

	private void onLoad() {
		lv_collection.refreshHeaderView();
		lv_collection.refreshBottomView();
		lv_collection.stopRefresh();
		lv_collection.stopLoadMore();
		lv_collection.setRefreshTime(GetSystem.GetNowTime());
	}

	/**
	 * 
	 * @param start
	 *            从第几条读起
	 * @param pageSize
	 *            一次读取多少条
	 */
	private void getCollectionDatas(int start, int pageSize) {
		String sql = "select * from CollectionData where Cust_id="
				+ app.cust_id + " order by favorite_id desc limit " + start
				+ "," + pageSize;
		Cursor cursor = DataSupport.findBySQL(sql);
		List<AdressData> datas = new ArrayList<AdressData>();
		while (cursor.moveToNext()) {
			AdressData adrDatas = new AdressData();
			adrDatas.set_id(cursor.getInt(cursor.getColumnIndex("favorite_id")));
			adrDatas.setAdress(cursor.getString(cursor
					.getColumnIndex("address")));
			adrDatas.setName(cursor.getString(cursor.getColumnIndex("name")));
			adrDatas.setPhone(cursor.getString(cursor.getColumnIndex("tel")));
			adrDatas.setLat(Double.parseDouble(cursor.getString(cursor
					.getColumnIndex("lat"))));
			adrDatas.setLon(Double.parseDouble(cursor.getString(cursor
					.getColumnIndex("lon"))));
			datas.add(adrDatas);
		}
		if (cursor != null) {
			cursor.close();
		}
		adressDatas.addAll(datas);
		Toal += datas.size();// 记录位置
		if (datas.size() == pageSize) {
			// 继续读取数据库
		} else {
			// 数据库读取完毕
			isGetDB = false;
		}
	}
}