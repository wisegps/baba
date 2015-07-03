package com.wise.notice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BaiduMap.SnapshotReadyCallback;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;

/**
 * 选择位置发送
 * 
 * @author honesty
 * 
 */
public class LetterSendMapActivity extends Activity {

	private BaiduMap mBaiduMap = null;
	private MapView mMapView = null;

	AppApplication app;
	ListView lv_info;
	// 定位相关
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	private GeoCoder mGeoCoder = null;
	TextView tv_adress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_letter_send_map);
		app = (AppApplication) getApplication();
		TextView tv_send = (TextView) findViewById(R.id.tv_send);
		tv_send.setOnClickListener(onClickListener);
		mMapView = (MapView) findViewById(R.id.mv_search_map);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setOnMapStatusChangeListener(onMapStatusChangeListener);
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(16));
		mGeoCoder = GeoCoder.newInstance();
		mGeoCoder.setOnGetGeoCodeResultListener(listener);
		tv_adress = (TextView) findViewById(R.id.tv_adress);

		findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		lv_info = (ListView) findViewById(R.id.lv_info);
		lv_info.setOnItemClickListener(onItemClickListener);

		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(5000);
		mLocClient.setLocOption(option);
		mLocClient.start();
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.tv_send:
				if(latitude == 0 && longitude == 0){
					Toast.makeText(LetterSendMapActivity.this, "定位失败，请重试", Toast.LENGTH_SHORT).show();
					return;
				}
				send();
				
				
				break;
			}
		}
	};

	private void send() {
		// 在地图上添加标记
		LatLng latLng = new LatLng(latitude, longitude);
		OverlayOptions marker = new MarkerOptions().position(latLng).anchor(0.5f, 1.0f).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_location));
		mark = (Marker) (mBaiduMap.addOverlay(marker));
		new Handler().postDelayed(new Runnable() {// 50毫秒后截图，防止图片未加载完成
					@Override
					public void run() {
						// 截图
						mBaiduMap.snapshot(new SnapshotReadyCallback() {
							@Override
							public void onSnapshotReady(Bitmap snapshot) {
								File file = new File(Constant.TemporaryMapImage);
								FileOutputStream out;
								try {
									out = new FileOutputStream(file);
									if (snapshot.compress(Bitmap.CompressFormat.JPEG, 80, out)) {
										out.flush();
										out.close();
									}
									
									
									Intent intent = new Intent();
									intent.putExtra("adress", adress);
									intent.putExtra("latitude", latitude);
									intent.putExtra("longitude", longitude);
									intent.putExtra("mapPath", Constant.TemporaryMapImage);
									LetterSendMapActivity.this.setResult(3, intent);
									finish();
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						});
					}
				}, 100);
	}

	OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

		}
	};

	boolean isFirstLoc = true;
	Marker phoneMark;

	private class MyLocationListenner implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null || mMapView == null)
				return;
			// 如果有当前位置，则先删除
			if (phoneMark != null) {
				phoneMark.remove();
			}
			LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
			// 构建Marker图标
			BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.person);
			// 构建MarkerOption，用于在地图上添加Marker
			OverlayOptions option = new MarkerOptions().anchor(0.5f, 1.0f).position(latLng).icon(bitmap);
			// 在地图上添加Marker，并显示
			phoneMark = (Marker) (mBaiduMap.addOverlay(option));
			if (isFirstLoc) {
				isFirstLoc = false;
				if(latitude == 0 && longitude == 0){
					latitude =location.getLatitude();
					longitude =location.getLongitude();
				}
				
				MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
				mBaiduMap.setMapStatus(mapStatusUpdate);
				mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(new LatLng(location.getLatitude(), location.getLongitude())));
			}
		}
	}

	Marker mark = null;
	OnMapStatusChangeListener onMapStatusChangeListener = new OnMapStatusChangeListener() {
		@Override
		public void onMapStatusChangeStart(MapStatus arg0) {
			Log.i("LetterSendMapActivity", "onMapStatusChangeStart");
			if (mark != null) {
				mark.remove();
			}
		}

		@Override
		public void onMapStatusChangeFinish(MapStatus arg0) {
			// 移动完毕，获取地图中心位置
			
			
			Log.i("LetterSendMapActivity", "移动完毕，获取地图中心位置");
			
			mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(arg0.target));
			latitude = arg0.target.latitude;
			longitude = arg0.target.longitude;
			
			Log.i("LetterSendMapActivity", "移动完毕，获取地图中心位置"+latitude);
		}

		@Override
		public void onMapStatusChange(MapStatus arg0) {
		}
	};
	String adress = "";
	double latitude = 0;
	double longitude = 0;
	OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
		@Override
		public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
				// 没有检索到结果
			} else {
				adress = result.getAddress();
				tv_adress.setText(adress);
			}
		}

		@Override
		public void onGetGeoCodeResult(GeoCodeResult arg0) {

		}
	};

	@Override
	protected void onPause() {
		// MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		// MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 退出时销毁定位
		mLocClient.stop();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		// mMapView.onDestroy();
		mMapView = null;
	}
}
