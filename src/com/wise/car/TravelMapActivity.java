package com.wise.car;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.NetThread;
import pubclas.Variable;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.BaiduMap.SnapshotReadyCallback;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.model.LatLngBounds.Builder;
import com.wise.baba.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 车辆行程图
 * 
 * @author 孟
 * 
 */
public class TravelMapActivity extends Activity {
	private static final int get_data = 1;

	MapView mMapView = null;
	BaiduMap mBaiduMap;
	List<Overlay> overlays;
	ProgressDialog Dialog = null; // progress
	String device = "";
	Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_travel_map);
		ImageView iv_activity_travel_share = (ImageView) findViewById(R.id.iv_activity_travel_share);
		iv_activity_travel_share.setOnClickListener(onClickListener);
		mMapView = (MapView) findViewById(R.id.mv_travel_map);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(16));

		TextView tv_travel_startPlace = (TextView) findViewById(R.id.tv_travel_startPlace);
		TextView tv_travel_stopPlace = (TextView) findViewById(R.id.tv_travel_stopPlace);
		TextView tv_travel_startTime = (TextView) findViewById(R.id.tv_travel_startTime);
		TextView tv_travel_stopTime = (TextView) findViewById(R.id.tv_travel_stopTime);
		TextView tv_travel_spacingDistance = (TextView) findViewById(R.id.tv_travel_spacingDistance);
		TextView tv_travel_averageOil = (TextView) findViewById(R.id.tv_travel_averageOil);
		TextView tv_travel_oil = (TextView) findViewById(R.id.tv_travel_oil);
		TextView tv_travel_speed = (TextView) findViewById(R.id.tv_travel_speed);
		TextView tv_travel_cost = (TextView) findViewById(R.id.tv_travel_cost);

		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		intent = getIntent();
		device = intent.getStringExtra("device");
		tv_travel_startPlace.setText(intent.getStringExtra("Start_place"));
		tv_travel_stopPlace.setText(intent.getStringExtra("End_place"));
		tv_travel_startTime.setText(intent.getStringExtra("StartTime")
				.substring(10, 16));
		tv_travel_stopTime.setText(intent.getStringExtra("StopTime").substring(
				10, 16));
		String str = "共" + intent.getStringExtra("SpacingDistance") + "公里\\"
				+ intent.getStringExtra("SpacingTime");
		tv_travel_spacingDistance.setText(str);
		tv_travel_averageOil.setText(intent.getStringExtra("AverageOil"));
		tv_travel_oil.setText(intent.getStringExtra("Oil"));
		tv_travel_speed.setText(intent.getStringExtra("Speed"));
		tv_travel_cost.setText(intent.getStringExtra("Cost"));

		String StartTime = intent.getStringExtra("StartTime");
		String StopTime = intent.getStringExtra("StopTime");

		try {
			String url = Constant.BaseUrl + "device/" + device
					+ "/gps_data?auth_code=" + Variable.auth_code
					+ "&start_time=" + URLEncoder.encode(StartTime, "UTF-8")
					+ "&end_time=" + URLEncoder.encode(StopTime, "UTF-8");
			new NetThread.GetDataThread(handler, url, get_data).start();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.iv_activity_travel_share:
				// TODO 截图
				mBaiduMap.snapshot(new SnapshotReadyCallback() {
					public void onSnapshotReady(Bitmap snapshot) {
						File file = new File("/mnt/sdcard/test.png");
						FileOutputStream out;
						try {
							out = new FileOutputStream(file);
							if (snapshot.compress(Bitmap.CompressFormat.PNG,
									80, out)) {
								out.flush();
								out.close();
							}
							String imagePath = "/mnt/sdcard/test.png";
							StringBuffer sb = new StringBuffer();
							sb.append("【行程】");
							sb.append(intent.getStringExtra("StartTime")
									.substring(5, 16));
							sb.append(" 从"
									+ intent.getStringExtra("Start_place"));
							sb.append("到" + intent.getStringExtra("End_place"));
							sb.append("，共行驶"
									+ intent.getStringExtra("SpacingDistance"));
							sb.append("公里，耗时"
									+ intent.getStringExtra("SpacingTime"));
							sb.append("，" + intent.getStringExtra("Oil"));
							sb.append("，" + intent.getStringExtra("Cost"));
							sb.append("，" + intent.getStringExtra("AverageOil"));
							sb.append("，" + intent.getStringExtra("Speed"));
							System.out.println(sb.toString());
							GetSystem.share(TravelMapActivity.this,
									sb.toString(), imagePath, 0, 0, "行程", "");
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
				Toast.makeText(TravelMapActivity.this, "正在截取屏幕图片...",
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case get_data:
				jsonData(msg.obj.toString());
				break;
			}
		}
	};

	private void jsonData(String result) {
		try {
			LatLngBounds.Builder builder = new Builder();
			// 添加折线
			List<LatLng> points = new ArrayList<LatLng>();
			JSONArray jsonArray = new JSONArray(result);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				double Lat = Double.valueOf(jsonObject.getString("lat"));
				double Lon = Double.valueOf(jsonObject.getString("lon"));
				LatLng ll = new LatLng(Lat, Lon);
				points.add(ll);
				builder.include(ll);
			}
			LatLngBounds bounds = builder.build();
			MapStatusUpdate u1 = MapStatusUpdateFactory.newLatLngBounds(bounds);
			mBaiduMap.animateMapStatus(u1);


			if (points.size() > 0) {
				// 构建Marker图标
				BitmapDescriptor bitmap = BitmapDescriptorFactory
						.fromResource(R.drawable.body_icon_outset);
				// 构建MarkerOption，用于在地图上添加Marker
				OverlayOptions start = new MarkerOptions().anchor(0.5f, 0.5f)
						.position(points.get(0)).icon(bitmap);
				// 在地图上添加Marker，并显示
				mBaiduMap.addOverlay(start);
			}
			if (points.size() > 1) {
				// 构建Marker图标
				BitmapDescriptor bitmap_end = BitmapDescriptorFactory
						.fromResource(R.drawable.body_icon_end);
				// 构建MarkerOption，用于在地图上添加Marker
				OverlayOptions end = new MarkerOptions().anchor(0.5f, 0.5f)
						.position(points.get(points.size() - 1))
						.icon(bitmap_end);
				// 在地图上添加Marker，并显示
				mBaiduMap.addOverlay(end);
			}
			

			if (points.size() > 1) {
				OverlayOptions ooPolyline = new PolylineOptions().width(5)
						.color(0xAAFF0000).points(points);
				mBaiduMap.addOverlay(ooPolyline);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}
