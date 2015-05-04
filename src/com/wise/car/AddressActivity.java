package com.wise.car;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.wise.baba.R;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.entity.AdressData;
import com.wise.setting.SetActivity;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 常用地址
 * 
 */
public class AddressActivity extends Activity {
	TextView tv_home, tv_company;
	ImageView ad_delete, ad_delete_1, ad_navigation, ad_navigation_1;

	ListView addListView;
	List<AdressData> adressDatas = new ArrayList<AdressData>();
	AddressAdapter addressAdapter = null;

	int index = -1;// 记录更新常用地址的位置下标
	// 定位相关
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	LatLng ll;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.adress_activity);

		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setIsNeedAddress(true);
		option.setCoorType("all");
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(1000);
		mLocClient.setLocOption(option);
		mLocClient.start();

		tv_home = (TextView) findViewById(R.id.tv_home);
		tv_company = (TextView) findViewById(R.id.tv_company);
		LinearLayout ll_home = (LinearLayout) findViewById(R.id.ll_home);
		ll_home.setOnClickListener(onClickListener);
		LinearLayout ll_company = (LinearLayout) findViewById(R.id.ll_company);
		ll_company.setOnClickListener(onClickListener);

		ad_delete = (ImageView) findViewById(R.id.ad_delete);
		ad_delete_1 = (ImageView) findViewById(R.id.ad_delete_1);
		ad_navigation = (ImageView) findViewById(R.id.ad_navigation);
		ad_navigation_1 = (ImageView) findViewById(R.id.ad_navigation_1);

		ad_navigation_1.setOnClickListener(onClickListener);
		ad_navigation.setOnClickListener(onClickListener);
		ad_delete.setOnClickListener(onClickListener);
		ad_delete_1.setOnClickListener(onClickListener);

		findViewById(R.id.address_add).setOnClickListener(onClickListener);
		findViewById(R.id.iv_back).setOnClickListener(onClickListener);

		SharedPreferences preferences = getSharedPreferences("search_name",
				Activity.MODE_PRIVATE);
		String name = preferences.getString("name", "");
		String company = preferences.getString("company", "");
		if (name.equals("") || name == null) {
			tv_home.setText("家");
		} else {
			tv_home.setText("家\n" + name);
			ad_delete.setVisibility(View.VISIBLE);
			ad_navigation.setVisibility(View.VISIBLE);
		}
		if (company.equals("") || company == null) {
			tv_company.setText("公司");
		} else {
			tv_company.setText("公司\n" + company);
			ad_delete_1.setVisibility(View.VISIBLE);
			ad_navigation_1.setVisibility(View.VISIBLE);
		}

		addListView = (ListView) findViewById(R.id.my_address_add);
		addressAdapter = new AddressAdapter();
		addListView.setAdapter(addressAdapter);

		// 点击修改常用地址
		addListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				index = position;
				startActivityForResult(new Intent(AddressActivity.this,
						ChooseAddressActivity.class), UPDATE);
			}
		});

		getJsonData();
		if (adressDatas.size() == 0 || adressDatas == null) {
			addListView.setVisibility(View.GONE);
		}
	}

	private static final int HOME = 1;// 家请求码
	private static final int COMPANY = 2;// 公司请求码
	private static final int ADD = 3;// 添加请求码
	private static final int UPDATE = 4;// 修改请求码

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			SharedPreferences preferences = getSharedPreferences("search_name",
					Activity.MODE_PRIVATE);
			switch (v.getId()) {
			case R.id.iv_back:
				AddressActivity.this.finish();
				break;
			case R.id.ad_navigation:
				double homeLat = Double.valueOf(preferences.getString(
						"homeLat", "0"));
				double homeLon = Double.valueOf(preferences.getString(
						"homeLon", "0"));
				GetSystem.FindCar(AddressActivity.this, ll, new LatLng(homeLat,
						homeLon), "", "");
				break;
			case R.id.ad_navigation_1:
				double companyLat = Double.valueOf(preferences.getString(
						"companyLat", "0"));
				double companyLon = Double.valueOf(preferences.getString(
						"companyLon", "0"));
				GetSystem.FindCar(AddressActivity.this, ll, new LatLng(
						companyLat, companyLon), "", "");
				break;
			case R.id.ad_delete:
				AlertDialog.Builder builder = new AlertDialog.Builder(
						AddressActivity.this);
				builder.setTitle("提示")
						.setMessage("确定删除此常用地址")
						.setCancelable(false)
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										tv_home.setText("家");
										ad_delete.setVisibility(View.GONE);
										ad_navigation.setVisibility(View.GONE);
										SharedPreferences home = getSharedPreferences(
												"search_name",
												Activity.MODE_PRIVATE);
										SharedPreferences.Editor editor = home
												.edit();
										editor.remove("name");
										editor.remove("homeLat");
										editor.remove("homeLon");
										editor.commit();
									}
								}).setNegativeButton("取消", null).create()
						.show();
				break;
			case R.id.ad_delete_1:
				AlertDialog.Builder builder_1 = new AlertDialog.Builder(
						AddressActivity.this);
				builder_1
						.setTitle("提示")
						.setMessage("确定删除此常用地址")
						.setCancelable(false)
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										tv_company.setText("公司");
										ad_delete_1.setVisibility(View.GONE);
										ad_navigation_1
												.setVisibility(View.GONE);
										SharedPreferences company = getSharedPreferences(
												"search_name",
												Activity.MODE_PRIVATE);
										SharedPreferences.Editor editor_1 = company
												.edit();
										editor_1.remove("company");
										editor_1.remove("companyLat");
										editor_1.remove("companyLon");
										editor_1.commit();
									}
								}).setNegativeButton("取消", null).create()
						.show();
				break;
			case R.id.ll_home:
				startActivityForResult(new Intent(AddressActivity.this,
						ChooseAddressActivity.class), HOME);
				break;
			case R.id.ll_company:
				startActivityForResult(new Intent(AddressActivity.this,
						ChooseAddressActivity.class), COMPANY);
				break;
			case R.id.address_add:
				startActivityForResult(new Intent(AddressActivity.this,
						ChooseAddressActivity.class), ADD);
				break;
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == ChooseAddressActivity.ADDRESSCODE) {
			String adress = data.getExtras().getString("name");
			double latitude = data.getExtras().getDouble("latitude");
			double longitude = data.getExtras().getDouble("longitude");
			boolean myLocat = data.getExtras().getBoolean("myLoct");

			SharedPreferences preferences = getSharedPreferences("search_name",
					Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = preferences.edit();
			if (requestCode == HOME) {
				if (adress != null && !adress.equals("")) {
					tv_home.setText("家\n" + adress);
					ad_delete.setVisibility(View.VISIBLE);
					ad_navigation.setVisibility(View.VISIBLE);
					editor.putString("homeLat", String.valueOf(latitude));
					editor.putString("homeLon", String.valueOf(longitude));
					editor.putString("name", adress);
				} else if (myLocat) {
					tv_home.setText("家");
					Toast.makeText(AddressActivity.this, "家地址定位失败",
							Toast.LENGTH_SHORT).show();
				} else {
					tv_home.setText("家");
					Toast.makeText(AddressActivity.this, "未搜索到结果",
							Toast.LENGTH_SHORT).show();
				}
			} else if (requestCode == COMPANY) {
				if (adress != null && !adress.equals("")) {
					tv_company.setText("公司\n" + adress);
					ad_delete_1.setVisibility(View.VISIBLE);
					ad_navigation_1.setVisibility(View.VISIBLE);
					editor.putString("companyLat", String.valueOf(latitude));
					editor.putString("companyLon", String.valueOf(longitude));
					editor.putString("company", adress);
				} else if (myLocat) {
					tv_company.setText("公司");
					Toast.makeText(AddressActivity.this, "公司地址定位失败",
							Toast.LENGTH_SHORT).show();
				} else {
					tv_company.setText("公司");
					Toast.makeText(AddressActivity.this, "未搜索到结果",
							Toast.LENGTH_SHORT).show();
				}
			} else if (requestCode == ADD) {
				if (adress != null && !adress.equals("")) {
					SharedPreferences preferences2 = getSharedPreferences(
							"address_add", Activity.MODE_PRIVATE);
					SharedPreferences.Editor editor2 = preferences2.edit();
					JSONObject object = new JSONObject();
					JSONArray addJsonArray = new JSONArray();
					boolean flag = true;
					for (AdressData adres : adressDatas) {
						if (adres.getAdress().equals(adress)) {
							Toast.makeText(AddressActivity.this, "地址已存在",
									Toast.LENGTH_SHORT).show();
							flag = false;
							break;
						}
					}
					try {
						for (AdressData adres : adressDatas) {
							JSONObject jsonObject = new JSONObject();
							jsonObject.put("nameMark", adres.getName());
							jsonObject.put("addressName", adres.getAdress());
							jsonObject.put("addressLat", adres.getLat());
							jsonObject.put("addressLon", adres.getLon());
							addJsonArray.put(jsonObject);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					if (flag) {
						AdressData adressData = new AdressData();
						adressData.setName("");
						adressData.setAdress(adress);
						adressData.setLat(latitude);
						adressData.setLon(longitude);
						adressDatas.add(adressData);
						try {
							JSONObject jsonObject = new JSONObject();
							jsonObject.put("nameMark", "");
							jsonObject.put("addressName", adress);
							jsonObject.put("addressLat", latitude);
							jsonObject.put("addressLon", longitude);
							addJsonArray.put(jsonObject);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					addressAdapter.notifyDataSetChanged();
					addListView.setVisibility(View.VISIBLE);
					try {
						object.put("addJsonArray", addJsonArray);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					editor2.putString("addJsonArray", object.toString());
					editor2.commit();

				} else if (myLocat) {
					Toast.makeText(AddressActivity.this, "添加失败",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(AddressActivity.this, "未搜索到结果",
							Toast.LENGTH_SHORT).show();
				}
			} else if (requestCode == UPDATE) {
				if (adress != null && !adress.equals("")) {
					boolean flag = true;
					for (AdressData adres : adressDatas) {
						if (adres.getAdress().equals(adress)) {
							Toast.makeText(AddressActivity.this, "常用地址已存在",
									Toast.LENGTH_SHORT).show();
							flag = false;
							break;
						}
					}
					if (flag) {
						AdressData adressData = new AdressData();
						adressData.setName("");
						adressData.setAdress(adress);
						adressData.setLat(latitude);
						adressData.setLon(longitude);
						adressDatas.set(index, adressData);
						SharedPreferences preferences2 = getSharedPreferences(
								"address_add", Activity.MODE_PRIVATE);
						SharedPreferences.Editor editor2 = preferences2.edit();
						JSONObject object = new JSONObject();
						JSONArray addJsonArray = new JSONArray();
						try {
							for (AdressData adres : adressDatas) {
								JSONObject jsonObject = new JSONObject();
								jsonObject.put("nameMark", "");
								jsonObject.put("addressName", adres.getAdress());
								jsonObject.put("addressLat", adres.getLat());
								jsonObject.put("addressLon", adres.getLon());
								addJsonArray.put(jsonObject);
							}
							object.put("addJsonArray", addJsonArray);
						} catch (JSONException e) {
							e.printStackTrace();
						}
						editor2.putString("addJsonArray", object.toString());
						editor2.commit();
						addressAdapter.notifyDataSetChanged();
					}
				} else if (myLocat) {
					Toast.makeText(AddressActivity.this, "更新定位失败",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(AddressActivity.this, "未搜索到结果",
							Toast.LENGTH_SHORT).show();
				}
			}
			editor.commit();
		}
	}

	private void getJsonData() {
		SharedPreferences preferences = getSharedPreferences("address_add",
				Activity.MODE_PRIVATE);
		String addJsonArray = preferences.getString("addJsonArray", "");
		if (addJsonArray == null || addJsonArray.equals("")) {
			return;
		}
		try {
			JSONObject jsonObject = new JSONObject(addJsonArray);
			JSONArray jsonArray = jsonObject.getJSONArray("addJsonArray");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject object = jsonArray.getJSONObject(i);
				String nameMark = "";
				if(object.opt("nameMark") == null){
					nameMark = "";
				}else{
					nameMark = object.getString("nameMark");
				}
				String addressName = object.getString("addressName");
				double addressLat = object.getDouble("addressLat");
				double addressLon = object.getDouble("addressLon");
				AdressData adressData = new AdressData();
				adressData.setName(nameMark);
				adressData.setAdress(addressName);
				adressData.setLat(addressLat);
				adressData.setLon(addressLon);
				adressDatas.add(adressData);
			}
			addListView.setVisibility(View.VISIBLE);
			addressAdapter.notifyDataSetChanged();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private class MyLocationListenner implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null) {
				return;
			}
			if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
				ll = new LatLng(location.getLatitude(), location.getLongitude());
			}
			mLocClient.stop();
			mLocClient.unRegisterLocationListener(myListener);
		}
	}

	class AddressAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return adressDatas.size();
		}

		@Override
		public Object getItem(int position) {
			return adressDatas.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			Holder mHolder = null;
			if (convertView == null) {
				mHolder = new Holder();
				convertView = (LayoutInflater.from(AddressActivity.this))
						.inflate(R.layout.item_address_add, null);
				mHolder.addressName = (TextView) convertView
						.findViewById(R.id.address_add_name);
				mHolder.addressDelete = (ImageView) convertView
						.findViewById(R.id.address_delete);
				mHolder.addressNavigation = (ImageView) convertView
						.findViewById(R.id.address_navigation);
				convertView.setTag(mHolder);
			} else {
				mHolder = (Holder) convertView.getTag();
			}
			AdressData adressData = adressDatas.get(position);
			if(adressData.getName() == null || adressData.getName().equals("")){
				mHolder.addressName.setText(adressData.getAdress());
			}else{
				mHolder.addressName.setText(adressData.getName() + "\n" +adressData.getAdress());
			}

			mHolder.addressDelete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							AddressActivity.this);
					builder.setTitle("提示")
							.setMessage("确定删除此常用地址")
							.setCancelable(false)
							.setPositiveButton("确定",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											SharedPreferences preferences = getSharedPreferences(
													"address_add",
													Activity.MODE_PRIVATE);
											SharedPreferences.Editor editor = preferences
													.edit();
											if (adressDatas.size() == 1) {
												editor.clear();
												editor.commit();
												adressDatas.clear();
												addListView
														.setVisibility(View.GONE);
											} else {
												addListView
														.setVisibility(View.VISIBLE);
												editor.clear();
												adressDatas.remove(position);
												JSONObject object = new JSONObject();
												JSONArray addJsonArray = new JSONArray();
												try {
													for (AdressData adressData : adressDatas) {
														JSONObject jsonObject = new JSONObject();
														jsonObject.put("nameMark", adressData.getName());
														jsonObject.put("addressName",
																		adressData.getAdress());
														jsonObject
																.put("addressLat",
																		adressData
																				.getLat());
														jsonObject
																.put("addressLon",
																		adressData
																				.getLon());
														addJsonArray
																.put(jsonObject);
													}
													object.put("addJsonArray",
															addJsonArray);
													editor.putString(
															"addJsonArray",
															object.toString());
													editor.commit();
													addressAdapter
															.notifyDataSetChanged();
												} catch (JSONException e) {
													e.printStackTrace();
												}
											}
										}
									}).setNegativeButton("取消", null).create()
							.show();

				}
			});

			mHolder.addressNavigation.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					GetSystem.FindCar(AddressActivity.this, ll, new LatLng(
							adressDatas.get(position).getLat(), adressDatas
									.get(position).getLon()), "", "");
				}
			});
			return convertView;
		}

		class Holder {
			TextView addressName;
			ImageView addressDelete, addressNavigation;
		}

	}
}
