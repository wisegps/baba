package com.wise.car;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.umeng.analytics.MobclickAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetDataFromUrl;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.biz.JsonData;
import com.wise.baba.entity.BrandData;
import com.wise.baba.entity.CarData;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.widget.SlidingView;
import com.wise.setting.RegisterActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 车辆列表
 * 
 * @author honesty
 * 
 */
public class CarActivity extends Activity {
	private static final int get_data = 1;
	private static final int remove_device = 2;
	private static final int delete_car = 3;
	private static final int get_image = 4;
	private static final int remove_cust = 5;
	ListView lv_cars;
	CarAdapter carAdapter;
	boolean isRefresh = false;
	AppApplication app;
	String sp_account;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_car);
		app = (AppApplication) getApplication();
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		lv_cars = (ListView) findViewById(R.id.lv_cars);
		LayoutInflater mLayoutInflater = LayoutInflater.from(this);
		View foot_view = mLayoutInflater.inflate(R.layout.foot_view, null);
		lv_cars.addFooterView(foot_view);
		carAdapter = new CarAdapter();
		lv_cars.setAdapter(carAdapter);
		lv_cars.setOnItemClickListener(onItemClickListener);
		if (app.carDatas.size() == 0) {
			getData();
		} else {
			new GetImageThread().start();
		}
		SharedPreferences preferences = getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		sp_account = preferences.getString(Constant.sp_account, "");
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				onBack();
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
				app.carDatas.clear();
				app.carDatas.addAll(JsonData.jsonCarInfo(msg.obj.toString()));
				carAdapter.notifyDataSetChanged();
				// 发送更新车辆广播
				Intent intent = new Intent(Constant.A_RefreshHomeCar);
				sendBroadcast(intent);
				new GetImageThread().start();
				break;
			case get_image:
				carAdapter.notifyDataSetChanged();
				break;
			case remove_cust:
				CarData carData = app.carDatas.get(index);
				String url = Constant.BaseUrl + "vehicle/" + carData.getObj_id() + "/device?auth_code=" + app.auth_code;
				final List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("device_id", "0"));
				AlertDialog.Builder builder = new AlertDialog.Builder(CarActivity.this);
				builder.setTitle("提示").setMessage("是否在解除绑定的同时清除终端的所有数据？").setPositiveButton("是", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						params.add(new BasicNameValuePair("deal_data", "1"));
					}
				}).setNegativeButton("否", null).show();
				new Thread(new NetThread.putDataThread(handler, url, params, remove_device, index)).start();
				break;
			case remove_device:
				jsonRemove(msg.obj.toString(), msg.arg1);
				break;
			case delete_car:
				jsonDelete(msg.obj.toString());
				break;
			}
		}
	};

	private void jsonRemove(String str, int index) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			if (jsonObject.getString("status_code").equals("0")) {
				// 刷新
				Toast.makeText(CarActivity.this, "解除绑定成功", Toast.LENGTH_SHORT).show();
				app.carDatas.get(index).setDevice_id("");
				carAdapter.notifyDataSetChanged();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jsonDelete(String str) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			if (jsonObject.getString("status_code").equals("0")) {
				Toast.makeText(CarActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
				app.carDatas.remove(index);
				carAdapter.notifyDataSetChanged();
				isRefresh = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			if (arg2 == app.carDatas.size()) {
				startActivityForResult(new Intent(CarActivity.this, CarAddActivity.class), 2);
			} else {

			}
		}
	};

	private void getData() {
		String url = Constant.BaseUrl + "customer/" + app.cust_id + "/vehicle?auth_code=" + app.auth_code;
		Log.i("CarUpdateActivity", url);
		new NetThread.GetDataThread(handler, url, get_data).start();
	}
	/**删除车辆确认**/
	private void deleteCar(final int position) {
		AlertDialog.Builder builder = new AlertDialog.Builder(CarActivity.this);
		builder.setTitle("提示");
		builder.setMessage("确定删除该车辆？");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String url = Constant.BaseUrl + "vehicle/" + app.carDatas.get(position).getObj_id() + "?auth_code=" + app.auth_code;
				new Thread(new NetThread.DeleteThread(handler, url, delete_car)).start();
			}
		}).setNegativeButton("否", null);
		builder.setNegativeButton("取消", null);
		builder.show();
	}
	/**删除车辆，或添加修改终端后，用这个标记删除内存里的数据，不用从网络上获取**/
	int index;

	class CarAdapter extends BaseAdapter {
		private LayoutInflater layoutInflater = LayoutInflater.from(CarActivity.this);

		@Override
		public int getCount() {
			return app.carDatas.size();
		}

		@Override
		public Object getItem(int arg0) {
			return app.carDatas.get(arg0);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = layoutInflater.inflate(R.layout.item_cars, null);
				holder = new ViewHolder();
				holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
				holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
				holder.tv_serial = (TextView) convertView.findViewById(R.id.tv_serial);
				holder.tv_update = (TextView) convertView.findViewById(R.id.tv_update);
				holder.tv_remove = (TextView) convertView.findViewById(R.id.tv_remove);
				holder.tv_del = (TextView) convertView.findViewById(R.id.tv_del);
				holder.bt_bind = (Button) convertView.findViewById(R.id.bt_bind);
				holder.sv = (SlidingView) convertView.findViewById(R.id.sv);
				holder.rl_car = (RelativeLayout) convertView.findViewById(R.id.rl_car);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final CarData carData = app.carDatas.get(position);
			if (new File(Constant.VehicleLogoPath + carData.getCar_brand_id() + ".png").exists()) {
				Bitmap image = BitmapFactory.decodeFile(Constant.VehicleLogoPath + carData.getCar_brand_id() + ".png");
				holder.iv_icon.setImageBitmap(image);
			} else {
				holder.iv_icon.setImageResource(R.drawable.icon_car_moren);
			}
			holder.tv_name.setText(carData.getNick_name());
			holder.tv_serial.setText(carData.getCar_series());
			holder.rl_car.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(CarActivity.this, CarUpdateActivity.class);
					intent.putExtra("index", position);
					intent.putExtra("isService", false);
					startActivityForResult(intent, 2);
				}
			});
			holder.tv_del.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (app.isTest) {
						Toast.makeText(CarActivity.this, "演示账号不支持该功能", Toast.LENGTH_SHORT).show();
						return;
					}
					// 删除车辆
					index = position;
					deleteCar(position);
				}
			});
			holder.sv.ScorllRestFast();
			if (carData.getDevice_id() == null || carData.getDevice_id().equals("") || carData.getDevice_id().equals("0")) {
				holder.tv_update.setVisibility(View.GONE);
				holder.tv_remove.setVisibility(View.GONE);
				holder.bt_bind.setVisibility(View.VISIBLE);
				holder.bt_bind.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (app.isTest) {
							Toast.makeText(CarActivity.this, "演示账号不支持该功能", Toast.LENGTH_SHORT).show();
							return;
						}
						Intent intent = new Intent(CarActivity.this, DevicesAddActivity.class);
						intent.putExtra("car_id", carData.getObj_id());
						intent.putExtra("car_series_id", carData.getCar_series_id());
						intent.putExtra("car_series", carData.getCar_series());
						intent.putExtra("isBind", true);
						startActivityForResult(intent, 2);
					}
				});
			} else {

				holder.bt_bind.setVisibility(View.GONE);
				holder.tv_update.setVisibility(View.VISIBLE);
				holder.tv_remove.setVisibility(View.VISIBLE);
				holder.tv_update.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (app.isTest) {
							Toast.makeText(CarActivity.this, "演示账号不支持该功能", Toast.LENGTH_SHORT).show();
							return;
						}
						index = position;
						Intent intent = new Intent(CarActivity.this, RegisterActivity.class);
						intent.putExtra("mark", 1);
						intent.putExtra("device_update", true);
						intent.putExtra("account", sp_account);
						startActivityForResult(intent, 7);
					}
				});
				holder.tv_remove.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (app.isTest) {
							Toast.makeText(CarActivity.this, "演示账号不支持该功能", Toast.LENGTH_SHORT).show();
							return;
						}
						index = position;
						Intent intent = new Intent(CarActivity.this, RegisterActivity.class);
						intent.putExtra("mark", 1);
						intent.putExtra("remove", true);
						intent.putExtra("account", sp_account);
						startActivityForResult(intent, REMOVE);
					}
				});
			}

			return convertView;
		}

		private class ViewHolder {
			TextView tv_name, tv_serial, tv_update, tv_del, tv_remove;
			ImageView iv_icon;
			Button bt_bind;
			SlidingView sv;
			RelativeLayout rl_car;
		}
	}

	private static final int REMOVE = 5;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REMOVE && resultCode == 6) {// 删除终端
			CarData carData = app.carDatas.get(index);
			String url_sim = Constant.BaseUrl + "device/" + carData.getDevice_id() + "/customer?auth_code=" + app.auth_code;
			List<NameValuePair> paramSim = new ArrayList<NameValuePair>();
			paramSim.add(new BasicNameValuePair("cust_id", "0"));
			new NetThread.putDataThread(handler, url_sim, paramSim, remove_cust).start();
		}
		if (requestCode == 7 && resultCode == 8) {// 修改终端
			CarData carData = app.carDatas.get(index);
			Intent intent = new Intent(CarActivity.this, DevicesAddActivity.class);
			intent.putExtra("car_id", carData.getObj_id());
			intent.putExtra("isBind", false);
			intent.putExtra("car_series_id", carData.getCar_series_id());
			intent.putExtra("car_series", carData.getCar_series());
			// 传以前终端的值
			intent.putExtra("old_device_id", carData.getDevice_id());
			startActivityForResult(intent, 2);
		}
		if (resultCode == 1) {
			// 绑定车辆信息成功
			carAdapter.notifyDataSetChanged();
		} else if (resultCode == 3) {
			getData();
			isRefresh = true;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			onBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void onBack() {
		System.out.println("isRefresh = " + isRefresh);
		if (isRefresh) {
			// 发广播
			Intent intent = new Intent(Constant.A_RefreshHomeCar);
			sendBroadcast(intent);
		}
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		carAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onPause() {
		super.onPause(); 
		MobclickAgent.onPause(this);
	}

	/** 检测本地是否有图片并下载 **/
	class GetImageThread extends Thread {
		@Override
		public void run() {
			super.run();
			// 得到车辆信息
			String result = GetDataFromUrl.getData(Constant.BaseUrl + "base/car_brand");
			List<BrandData> brandDatas = new ArrayList<BrandData>();
			if (!result.equals("")) {
				try {
					JSONArray jsonArray = new JSONArray(result);
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						BrandData brandData = new BrandData();
						brandData.setBrand(jsonObject.getString("name"));
						brandData.setId(jsonObject.getString("id"));
						if (jsonObject.opt("url_icon") != null) {
							brandData.setLogoUrl(jsonObject.getString("url_icon"));
						} else {
							brandData.setLogoUrl("");
						}
						brandDatas.add(brandData);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			for (CarData carData : app.carDatas) {
				if (new File(Constant.VehicleLogoPath + carData.getCar_brand_id() + ".png").exists()) {

				} else {
					// 获取图片
					for (BrandData brandData : brandDatas) {
						if (brandData.getId().equals(carData.getCar_brand_id())) {
							// 从网上获取图片
							Bitmap bitmap = GetSystem.getBitmapFromURL(Constant.ImageUrl + brandData.getLogoUrl());
							if (bitmap != null) {
								String imagePath = Constant.VehicleLogoPath + carData.getCar_brand_id() + ".png";
								File filePath = new File(Constant.VehicleLogoPath);
								if (!filePath.exists()) {
									filePath.mkdirs();
								}
								createImage(imagePath, bitmap);
							}
							break;
						}
					}
				}
			}
		}
	}

	// 向SD卡中添加图片
	public void createImage(String fileName, Bitmap bitmap) {
		FileOutputStream b = null;
		try {
			b = new FileOutputStream(fileName);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, b);// 把数据写入文件
			Message msg = new Message();
			msg.what = get_image;
			handler.sendMessage(msg);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				b.flush();
				b.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
