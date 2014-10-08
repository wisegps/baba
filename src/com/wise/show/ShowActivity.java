package com.wise.show;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import pubclas.Constant;
import pubclas.Judge;
import pubclas.NetThread;
import pubclas.Variable;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wise.baba.R;
import com.wise.car.ModelsActivity;
import com.wise.setting.LoginActivity;
import com.wise.show.MyScrollView.OnFlowClickListener;
import com.wise.show.RefreshableView.RefreshListener;

import customView.OnViewChangeListener;
import customView.ParentSlide;
import customView.PopView;
import customView.PopView.OnItemClickListener;

/** 车秀大厅 **/
public class ShowActivity extends Activity {
	private static final String TAG = "ShowActivity";

	private static final int getFristImage = 1;
	private static final int getNextImage = 2;
	private static final int praise = 3;
	private static final int getRefreshImage = 4;

	TextView tv_car, tv_baby, tv_scenery, tv_road, tv_travel;
	View v_car, v_baby, v_scenery, v_road, v_travel;
	TextView tv_time, tv_title;
	TextView tv_name, tv_ad, tv_dz, tv_bc, tv_bm, tv_all, tv_other;
	LinearLayout ll_car_choose;
	ImageView iv_choose;
	GridView car_choose_grid;

	ParentSlide hsl_photo;
	/** 个人头像路径 **/
	String logo = "";
	int page_count = 20;
	/** 是否正在加载图片 **/
	boolean isLoading = false;
	boolean is_beauty = false;
	String beauty = "&if_beauty=1";
	int car_brand_id = -1;
	/** 当前所处位置 **/
	int index = 0;
	int photo_type = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_show);
		v_car = (View) findViewById(R.id.v_car);
		v_baby = (View) findViewById(R.id.v_baby);
		v_scenery = (View) findViewById(R.id.v_scenery);
		v_road = (View) findViewById(R.id.v_road);
		v_travel = (View) findViewById(R.id.v_travel);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		ImageView iv_show_car = (ImageView) findViewById(R.id.iv_show_car);
		iv_show_car.setOnClickListener(onClickListener);
		iv_choose = (ImageView) findViewById(R.id.iv_choose);
		iv_choose.setOnClickListener(onClickListener);
		tv_time = (TextView) findViewById(R.id.tv_time);
		tv_title = (TextView) findViewById(R.id.tv_title);

		hsl_photo = (ParentSlide) findViewById(R.id.hsl_photo);
		tv_car = (TextView) findViewById(R.id.tv_car);
		tv_car.setOnClickListener(onClickListener);
		tv_baby = (TextView) findViewById(R.id.tv_baby);
		tv_baby.setOnClickListener(onClickListener);
		tv_scenery = (TextView) findViewById(R.id.tv_scenery);
		tv_scenery.setOnClickListener(onClickListener);
		tv_road = (TextView) findViewById(R.id.tv_road);
		tv_road.setOnClickListener(onClickListener);
		tv_travel = (TextView) findViewById(R.id.tv_travel);
		tv_travel.setOnClickListener(onClickListener);
		ll_car_choose = (LinearLayout) findViewById(R.id.ll_car_choose);
		tv_name = (TextView) findViewById(R.id.tv_name);
		TextView tv_ad = (TextView) findViewById(R.id.tv_ad);
		tv_ad.setOnClickListener(onClickListener);
		TextView tv_dz = (TextView) findViewById(R.id.tv_dz);
		tv_dz.setOnClickListener(onClickListener);
		TextView tv_bc = (TextView) findViewById(R.id.tv_bc);
		tv_bc.setOnClickListener(onClickListener);
		TextView tv_bm = (TextView) findViewById(R.id.tv_bm);
		tv_bm.setOnClickListener(onClickListener);
		TextView tv_all = (TextView) findViewById(R.id.tv_all);
		tv_all.setOnClickListener(onClickListener);
		TextView tv_other = (TextView) findViewById(R.id.tv_other);
		tv_other.setOnClickListener(onClickListener);
		getLogo();
		setWaterFalls();
		hsl_photo.setOnViewChangeListener(new OnViewChangeListener() {
			@Override
			public void OnViewChange(int view) {
				index = view;
				switch (index) {
				case 0:
					photo_type = 1;
					setBg();
					// tv_car.setBackgroundResource(R.drawable.bg_border_left_press);
					tv_car.setTextColor(getResources().getColor(R.color.Green));
					v_car.setBackgroundResource(R.color.Green);
					break;
				case 1:
					photo_type = 2;
					setBg();
					// tv_baby.setBackgroundResource(R.drawable.bg_border_center_press);
					tv_baby.setTextColor(getResources().getColor(R.color.Green));
					v_baby.setBackgroundResource(R.color.Green);
					break;
				case 2:
					photo_type = 3;
					setBg();
					// tv_scenery.setBackgroundResource(R.drawable.bg_border_center_press);
					tv_scenery.setTextColor(getResources().getColor(
							R.color.Green));
					v_scenery.setBackgroundResource(R.color.Green);
					break;
				case 3:
					photo_type = 4;
					setBg();
					// tv_road.setBackgroundResource(R.drawable.bg_border_center_press);
					tv_road.setTextColor(getResources().getColor(R.color.Green));
					v_road.setBackgroundResource(R.color.Green);
					break;
				case 4:
					photo_type = 5;
					setBg();
					// tv_travel.setBackgroundResource(R.drawable.bg_border_right_press);
					tv_travel.setTextColor(getResources().getColor(
							R.color.Green));
					v_travel.setBackgroundResource(R.color.Green);
					break;
				}

				if (viewDatas.get(index).getImageDatas().size() == 0) {
					getFristImages();
				}
			}

			@Override
			public void OnLastView() {
			}

			@Override
			public void OnFinish(int index) {
			}
		});
	}

	PopupWindow mPopupWindow;

	private void getCarChooseShow() {
		LayoutInflater mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View mpopView = mInflater.inflate(R.layout.car_choose_grid, null);
		car_choose_grid = (GridView) mpopView
				.findViewById(R.id.car_choose_grid);
		car_choose_grid.setAdapter(new CarChosseGrid());
		mPopupWindow = new PopupWindow(mpopView, LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopupWindow.setFocusable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.showAsDropDown(iv_choose);
		car_choose_grid
				.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						switch (position) {
						case 0:
							car_brand_id = -1;
							tv_name.setText("所有车型");
							hideChooseCar();
							getFristImages();
							break;
						case 1:
							car_brand_id = 9;
							tv_name.setText("奥迪");
							hideChooseCar();
							getFristImages();
							break;
						case 2:
							car_brand_id = 8;
							tv_name.setText("大众");
							hideChooseCar();
							getFristImages();
							break;
						case 3:
							car_brand_id = 2;
							tv_name.setText("奔驰");
							hideChooseCar();
							getFristImages();
							break;
						case 4:
							car_brand_id = 3;
							tv_name.setText("宝马");
							hideChooseCar();
							getFristImages();
							break;
						case 5:
							Intent intent = new Intent(ShowActivity.this,
									ModelsActivity.class);
							intent.putExtra("isNeedModel", false);
							startActivityForResult(intent, 3);
							hideChooseCar();
							break;
						}
						mPopupWindow.dismiss();
					}
				});
	}

	private String[] carTypes = { "所有车型", "奥迪", "大众", "奔驰", "宝马", "选择其他" };

	class CarChosseGrid extends BaseAdapter {
		private LayoutInflater inflate = LayoutInflater.from(ShowActivity.this);

		@Override
		public int getCount() {
			return carTypes.length;
		}

		@Override
		public Object getItem(int position) {
			return carTypes[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder mhHolder = null;
			if (convertView == null) {
				convertView = inflate.inflate(R.layout.item_short_province,
						null);
				mhHolder = new Holder();
				mhHolder.typeText = (TextView) convertView
						.findViewById(R.id.tv_province);
				convertView.setTag(mhHolder);
			} else {
				mhHolder = (Holder) convertView.getTag();
			}
			mhHolder.typeText.setBackgroundColor(getResources().getColor(
					R.color.title_back));
			mhHolder.typeText.setText(carTypes[position]);
			return convertView;
		}

		class Holder {
			TextView typeText;
		}
	}

	private void setWaterFalls() {
		for (int i = 0; i < 5; i++) {
			ViewData viewData = new ViewData();
			View view_waterfalls = LayoutInflater.from(this).inflate(
					R.layout.item_waterfalls, null);
			hsl_photo.addView(view_waterfalls);
			MyScrollView myScrollView = (MyScrollView) view_waterfalls
					.findViewById(R.id.my_scroll_view);
			myScrollView.setOnFlowClickListener(onFlowClickListener);
			RefreshableView ll_refresh = (RefreshableView) view_waterfalls
					.findViewById(R.id.ll_refresh);
			ll_refresh.setRefreshListener(refreshListener, index);
			viewData.setMyScrollView(myScrollView);
			viewData.setLl_refresh(ll_refresh);
			viewData.setImageDatas(new ArrayList<ImageData>());
			viewDatas.add(viewData);
		}
		getFristImages();
	}

	List<ViewData> viewDatas = new ArrayList<ViewData>();

	class ViewData {
		MyScrollView myScrollView;
		RefreshableView ll_refresh;
		List<ImageData> imageDatas;

		public MyScrollView getMyScrollView() {
			return myScrollView;
		}

		public void setMyScrollView(MyScrollView myScrollView) {
			this.myScrollView = myScrollView;
		}

		public RefreshableView getLl_refresh() {
			return ll_refresh;
		}

		public void setLl_refresh(RefreshableView ll_refresh) {
			this.ll_refresh = ll_refresh;
		}

		public List<ImageData> getImageDatas() {
			return imageDatas;
		}

		public void setImageDatas(List<ImageData> imageDatas) {
			this.imageDatas = imageDatas;
		}
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.iv_show_car:
				if (Judge.isLogin()) {
					picPop();
				} else {
					startActivityForResult(new Intent(ShowActivity.this,
							LoginActivity.class), 1);
				}
				break;
			case R.id.iv_choose:
				// TODO popupwindow显示
				getCarChooseShow();
				break;
			case R.id.tv_ad:
				car_brand_id = 9;
				tv_name.setText("奥迪");
				hideChooseCar();
				getFristImages();
				break;
			case R.id.tv_dz:
				car_brand_id = 8;
				tv_name.setText("大众");
				hideChooseCar();
				getFristImages();
				break;
			case R.id.tv_bc:
				car_brand_id = 2;
				tv_name.setText("奔驰");
				hideChooseCar();
				getFristImages();
				break;
			case R.id.tv_bm:
				car_brand_id = 3;
				tv_name.setText("宝马");
				hideChooseCar();
				getFristImages();
				break;
			case R.id.tv_all:
				car_brand_id = -1;
				tv_name.setText("所有车型");
				hideChooseCar();
				getFristImages();
				break;
			case R.id.tv_other:
				Intent intent = new Intent(ShowActivity.this,
						ModelsActivity.class);
				intent.putExtra("isNeedModel", false);
				startActivityForResult(intent, 3);
				hideChooseCar();
				break;
			case R.id.tv_car:
				hsl_photo.snapToScreen(0);
				break;
			case R.id.tv_baby:
				hsl_photo.snapToScreen(1);
				break;
			case R.id.tv_scenery:
				hsl_photo.snapToScreen(2);
				break;
			case R.id.tv_road:
				hsl_photo.snapToScreen(3);
				break;
			case R.id.tv_travel:
				hsl_photo.snapToScreen(4);
				break;
			}
		}
	};

	private void setBg() {
		tv_car.setTextColor(getResources().getColor(R.color.navy));
		// v_car.setBackgroundColor(getResources().getColor(R.color.transparent));
		v_car.setBackgroundResource(R.color.white);
		// tv_car.setBackgroundResource(R.drawable.bg_border_left);
		tv_baby.setTextColor(getResources().getColor(R.color.navy));
		v_baby.setBackgroundResource(R.color.white);
		// tv_baby.setBackgroundResource(R.drawable.bg_border_center);
		tv_scenery.setTextColor(getResources().getColor(R.color.navy));
		v_scenery.setBackgroundResource(R.color.white);
		// tv_scenery.setBackgroundResource(R.drawable.bg_border_center);
		tv_road.setTextColor(getResources().getColor(R.color.navy));
		v_road.setBackgroundResource(R.color.white);
		// tv_road.setBackgroundResource(R.drawable.bg_border_center);
		tv_travel.setTextColor(getResources().getColor(R.color.navy));
		v_travel.setBackgroundResource(R.color.white);
		// tv_travel.setBackgroundResource(R.drawable.bg_border_right);
	}

	private void hideChooseCar() {
		// ll_car_choose.setVisibility(View.GONE);
		// iv_choose.setVisibility(View.VISIBLE);
		car_choose_grid.setVisibility(View.GONE);
	}

	String refresh = "";
	RefreshListener refreshListener = new RefreshListener() {
		@Override
		public void onRefresh() {
			System.out.println("onRefresh");
			// 加标记 下拉刷新
			refresh = "";
			int Photo_id;
			if (viewDatas.get(index).getImageDatas().size() != 0) {
				Photo_id = viewDatas.get(index).getImageDatas().get(0)
						.getPhoto_id();
				String url = Constant.BaseUrl + "photo?auth_code="
						+ Variable.auth_code + "&cust_id=" + Variable.cust_id
						+ "&max_id=" + Photo_id + getBeauty();
				new NetThread.GetDataThread(handler, url, getRefreshImage,
						index).start();
			} else {
				getFristImages();
				Toast.makeText(ShowActivity.this, "图片获取中...",
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onRefreshOver(int index) {
			System.out.println("onRefreshOver");
			// List<ImageData> iDatas = jsonImages(refresh);
			// viewDatas.get(index).getImageDatas().addAll(0, iDatas);
			// viewDatas.get(index).getMyScrollView().addHeadImages(iDatas);
		}
	};

	OnFlowClickListener onFlowClickListener = new OnFlowClickListener() {
		@Override
		public void OnPraise(int position) {// 点赞
			if (Judge.isLogin()) {
				ImageData imageData = viewDatas.get(index).getImageDatas()
						.get(position);
				if (!imageData.isCust_praise()) {
					int Photo_id = imageData.getPhoto_id();
					String url = Constant.BaseUrl + "photo/" + Photo_id
							+ "/praise?auth_code=" + Variable.auth_code;
					List<NameValuePair> pairs = new ArrayList<NameValuePair>();
					pairs.add(new BasicNameValuePair("cust_id",
							Variable.cust_id));
					pairs.add(new BasicNameValuePair("cust_name",
							Variable.cust_name));
					pairs.add(new BasicNameValuePair("icon", logo));
					new NetThread.putDataThread(handler, url, pairs, praise,
							position).start();
				}
			} else {
				// 没有登录则跳转到登录
				startActivityForResult(new Intent(ShowActivity.this,
						LoginActivity.class), 1);
			}
		}

		@Override
		public void OnClick(int position) {// 点击图片
			if (Judge.isLogin()) {
				Intent intent = new Intent(ShowActivity.this,
						PhotoActivity.class);
				intent.putExtra("imageData", viewDatas.get(index)
						.getImageDatas().get(position));
				intent.putExtra("position", position);
				startActivityForResult(intent, 1);
			} else {
				// 没有登录则跳转到登录
				startActivityForResult(new Intent(ShowActivity.this,
						LoginActivity.class), 1);
			}
		}

		@Override
		public void OnLoad() {// 图片加载完毕
			// 获取更多图片链接
			// 判断还有图片,通过返回数目计算，如果是 page_count 的整数倍则读取数据，否则说明服务器数据读取完毕
			if (isLoading) {
				return;
			}
			if (viewDatas.get(index).getImageDatas() != null
					&& viewDatas.get(index).getImageDatas().size() != 0) {
				int i = viewDatas.get(index).getImageDatas().size()
						% page_count; // 取余
				if (i == 0) {
					isLoading = true;
					int Photo_id = viewDatas
							.get(index)
							.getImageDatas()
							.get(viewDatas.get(index).getImageDatas().size() - 1)
							.getPhoto_id();
					String url = Constant.BaseUrl + "photo?auth_code="
							+ Variable.auth_code + "&cust_id="
							+ Variable.cust_id + "&min_id=" + Photo_id
							+ getBeauty();
					new NetThread.GetDataThread(handler, url, getNextImage,
							index).start();
				}
			}
		}

		@Override
		public void OnScrollPosition(String Time) {
			// 滑动显示时间
			tv_time.setVisibility(View.GONE);
			tv_time.setText(Time.substring(0, 16).replace("T", " "));
			handler.removeCallbacks(hideTime);
		}

		@Override
		public void OnScrollFinish() {
			handler.postDelayed(hideTime, 1500);
		}
	};

	Runnable hideTime = new Runnable() {
		@Override
		public void run() {
			Animation operatingAnim = AnimationUtils.loadAnimation(
					ShowActivity.this, R.anim.car_show_time_gone);
			tv_time.setAnimation(operatingAnim);
			tv_time.setVisibility(View.GONE);
		}
	};

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case getFristImage:
				List<ImageData> iDatas = jsonImages(msg.obj.toString());
				viewDatas.get(msg.arg1).getImageDatas().addAll(iDatas);
				viewDatas.get(msg.arg1).getMyScrollView().resetImages(iDatas);
				break;
			case getNextImage:
				// 加载更多
				isLoading = false;
				List<ImageData> Datas = jsonImages(msg.obj.toString());
				viewDatas.get(msg.arg1).getImageDatas().addAll(Datas);
				viewDatas.get(msg.arg1).getMyScrollView().addFootImages(Datas);
				break;
			case praise:
				jsonPraise(msg.obj.toString(), msg.arg1);
				break;
			case getRefreshImage:
				refresh = msg.obj.toString();
				final int index = msg.arg1;
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						viewDatas.get(index).getLl_refresh().finishRefresh();
						List<ImageData> iDatas1 = jsonImages(refresh);
						viewDatas.get(index).getImageDatas().addAll(0, iDatas1);
						viewDatas.get(index).getMyScrollView()
								.addHeadImages(iDatas1);
					}
				}, 1000);
				break;
			}
		}
	};

	/** 判断点赞 **/
	private void jsonPraise(String result, int position) {
		try {
			JSONObject jsonObject = new JSONObject(result);
			if (jsonObject.getInt("status_code") == 0) {
				// 点赞成功
				// 考虑在连续点2次的情况
				if (!viewDatas.get(index).getImageDatas().get(position)
						.isCust_praise()) {
					viewDatas.get(index).getImageDatas().get(position)
							.setCust_praise(true);
					// 修改图片点赞状态
					viewDatas.get(index).getMyScrollView().setPraise(position);
					// 点赞次数+1;
					int Praise_count = viewDatas.get(index).getImageDatas()
							.get(position).getPraise_count() + 1;
					viewDatas.get(index).getImageDatas().get(position)
							.setPraise_count(Praise_count);
					viewDatas.get(index).getMyScrollView()
							.setPraiseCount(position, Praise_count);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 获取图片列表 **/
	private void getFristImages() {
		viewDatas.get(index).getImageDatas().clear();
		String url;
		if (car_brand_id == -1) {
			url = Constant.BaseUrl + "photo?auth_code=" + Variable.auth_code
					+ "&cust_id=" + Variable.cust_id + getBeauty();
		} else {
			url = Constant.BaseUrl + "photo?auth_code=" + Variable.auth_code
					+ "&car_brand_id=" + car_brand_id + "&cust_id="
					+ Variable.cust_id + getBeauty();
		}
		new NetThread.GetDataThread(handler, url, getFristImage, index).start();
	}

	/** 获取 **/
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

	private void getLogo() {
		SharedPreferences preferences = getSharedPreferences(
				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		String customer = preferences.getString(Constant.sp_customer
				+ Variable.cust_id, "");
		try {
			JSONObject jsonObject = new JSONObject(customer);
			logo = jsonObject.getString("logo");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void picPop() {
		List<String> items = new ArrayList<String>();
		items.add("拍照");
		items.add("从手机相册中选取");
		final PopView popView = new PopView(this);
		popView.initView(findViewById(R.id.rl_main));
		popView.setData(items);
		popView.SetOnItemClickListener(new OnItemClickListener() {
			@Override
			public void OnItemClick(int index) {
				switch (index) {
				case 0:
					File file = new File(Constant.VehiclePath);
					if (!file.exists()) {
						file.mkdirs();// 创建文件夹
					}
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					intent.putExtra(
							MediaStore.EXTRA_OUTPUT,
							Uri.fromFile(new File(Constant.VehiclePath
									+ Constant.TemporaryImage)));
					startActivityForResult(intent, 1);
					popView.dismiss();
					break;
				case 1:// TODO 从图库获取
					Intent i = new Intent(
							Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(i, 9);
					popView.dismiss();
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		System.out.println("requestCode = " + requestCode + " , resultCode = "
				+ resultCode);
		if (requestCode == 9) {
			if (data != null) {
				// TODO 获取图片路径
				Uri uri = data.getData();
				Intent intent = new Intent(ShowActivity.this,
						ShowCarAcitivity.class);
				intent.putExtra("image", getPath(uri));
				intent.putExtra("photo_type", photo_type);
				startActivity(intent);
			}
			return;
		}
		if (resultCode == Activity.RESULT_OK) {
			Intent intent = new Intent(ShowActivity.this,
					ShowCarAcitivity.class);
			intent.putExtra("image", Constant.VehiclePath
					+ Constant.TemporaryImage);
			intent.putExtra("photo_type", photo_type);
			startActivity(intent);
			return;
		}
		if (resultCode == 1) {
			// 登录返回,刷新数据
			for (int i = 0; i < viewDatas.size(); i++) {
				viewDatas.get(i).getImageDatas().clear();
			}
			getFristImages();
			getLogo();
			return;
		}
		if (resultCode == 2) {
			// 相片详细界面点赞返回
			int position = data.getIntExtra("position", 0);
			int Praise_count = data.getIntExtra("Praise_count", 0);
			viewDatas
					.get(index)
					.getImageDatas()
					.get(position)
					.setCust_praise(data.getBooleanExtra("isCust_praise", true));
			viewDatas.get(index).getImageDatas().get(position)
					.setPraise_count(Praise_count);
			// 修改图片点赞状态
			viewDatas.get(index).getMyScrollView().setPraise(position);
			viewDatas.get(index).getMyScrollView()
					.setPraiseCount(position, Praise_count);
		}
		if (requestCode == 3) {
			System.out.println("brank = " + data.getStringExtra("brank"));
			tv_name.setText(data.getStringExtra("brank"));
			car_brand_id = Integer.valueOf(data.getStringExtra("brankId"));
			getFristImages();
		}
	}

	/** 把uri 转换成 SD卡路径 **/
	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	/** 类型返回 **/
	private String getBeauty() {
		return "&photo_type=" + photo_type;
	}
}
