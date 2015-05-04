package com.wise.show;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.biz.Judge;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.widget.PopView;
import com.wise.baba.ui.widget.PopView.OnItemClickListener;
import com.wise.baba.util.Uri2Path;
import com.wise.notice.LetterActivity;
import com.wise.setting.LoginActivity;
import com.wise.show.MyScrollView.OnFlowClickListener;
import com.wise.show.RefreshableView.RefreshListener;

/** 车秀大厅 **/
public class ShowActivity extends Activity {
	private static final String TAG = "ShowActivity";

	public static final int PINDAO = 5;// 频道请求码
	private static final int getFristImage = 1;
	private static final int getNextImage = 2;
	private static final int praise = 3;
	private static final int getRefreshImage = 4;

	TextView tv_car, tv_baby, tv_scenery, tv_road, tv_travel;
	TextView tv_time, tv_title;
	TextView tv_name, tv_ad, tv_dz, tv_bc, tv_bm, tv_all, tv_other;
	ImageView iv_choose;

	ViewPager viewPager;
	ArrayList<View> pageViews = new ArrayList<View>();

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
	AppApplication app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_show);
		app = (AppApplication)getApplication();
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		ImageView iv_show_car = (ImageView) findViewById(R.id.iv_show_car);
		iv_show_car.setOnClickListener(onClickListener);
		iv_choose = (ImageView) findViewById(R.id.iv_choose);
		iv_choose.setOnClickListener(onClickListener);
		tv_time = (TextView) findViewById(R.id.tv_time);
		tv_title = (TextView) findViewById(R.id.tv_title);

		viewPager = (ViewPager) findViewById(R.id.vp_photo);

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
		tv_name = (TextView) findViewById(R.id.tv_name);
		getLogo();
		setWaterFalls();
		viewPager.setAdapter(new GuidePageAdapter());
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				GetSystem.myLog(TAG, "onPageSelected");
				index = arg0;
				switch (index) {
				case 0:
					photo_type = 1;
					setBg();
					tv_car.setTextColor(getResources().getColor(R.color.white));
					tv_car.setBackgroundResource(R.color.Green);
					break;
				case 1:
					photo_type = 2;
					setBg();
					tv_baby.setTextColor(getResources().getColor(R.color.white));
					tv_baby.setBackgroundResource(R.color.Green);
					break;
				case 2:
					photo_type = 3;
					setBg();
					tv_scenery.setTextColor(getResources().getColor(
							R.color.white));
					tv_scenery.setBackgroundResource(R.color.Green);
					break;
				case 3:
					photo_type = 4;
					setBg();
					tv_road.setTextColor(getResources().getColor(R.color.white));
					tv_road.setBackgroundResource(R.color.Green);
					break;
				case 4:
					photo_type = 5;
					setBg();
					tv_travel.setTextColor(getResources().getColor(
							R.color.white));
					tv_travel.setBackgroundResource(R.color.Green);
					break;
				}
				if (viewDatas.get(index).getImageDatas().size() == 0) {
					getFristImages();
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}

	private void setWaterFalls() {
		for (int i = 0; i < 5; i++) {
			ViewData viewData = new ViewData();
			View view_waterfalls = LayoutInflater.from(this).inflate(
					R.layout.item_waterfalls, null);
			pageViews.add(view_waterfalls);
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
				if (Judge.isLogin(app)) {
					picPop();
				} else {
					startActivityForResult(new Intent(ShowActivity.this,
							LoginActivity.class), 1);
				}
				break;
			case R.id.iv_choose:
				// TODO 点击频道跳转频道页面
				Intent i = new Intent(ShowActivity.this, PinDaoActivity.class);
				startActivityForResult(i, PINDAO);
				// getCarChooseShow();
				break;
			case R.id.tv_car:
				viewPager.setCurrentItem(0);
				break;
			case R.id.tv_baby:
				viewPager.setCurrentItem(1);
				break;
			case R.id.tv_scenery:
				viewPager.setCurrentItem(2);
				break;
			case R.id.tv_road:
				viewPager.setCurrentItem(3);
				break;
			case R.id.tv_travel:
				viewPager.setCurrentItem(4);
				break;
			}
		}
	};

	private void setBg() {
		tv_car.setTextColor(getResources().getColor(R.color.Green));
		tv_car.setBackgroundResource(R.color.white);
		tv_baby.setTextColor(getResources().getColor(R.color.Green));
		tv_baby.setBackgroundResource(R.color.white);
		tv_scenery.setTextColor(getResources().getColor(R.color.Green));
		tv_scenery.setBackgroundResource(R.color.white);
		tv_road.setTextColor(getResources().getColor(R.color.Green));
		tv_road.setBackgroundResource(R.color.white);
		tv_travel.setTextColor(getResources().getColor(R.color.Green));
		tv_travel.setBackgroundResource(R.color.white);
	}

	String refresh = "";
	RefreshListener refreshListener = new RefreshListener() {
		@Override
		public void onRefresh() {
			// 加标记 下拉刷新
			refresh = "";
			int Photo_id;
			if (viewDatas.get(index).getImageDatas().size() != 0) {
				Photo_id = viewDatas.get(index).getImageDatas().get(0)
						.getPhoto_id();
				String url = Constant.BaseUrl + "photo?auth_code="
						+ app.auth_code + "&cust_id=" + app.cust_id
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
		}
	};

	OnFlowClickListener onFlowClickListener = new OnFlowClickListener() {
		@Override
		public void OnPraise(int position) {// 点赞
			if (Judge.isLogin(app)) {
				ImageData imageData = viewDatas.get(index).getImageDatas()
						.get(position);
				if (!imageData.isCust_praise()) {
					int Photo_id = imageData.getPhoto_id();
					String url = Constant.BaseUrl + "photo/" + Photo_id
							+ "/praise?auth_code=" + app.auth_code;
					List<NameValuePair> pairs = new ArrayList<NameValuePair>();
					pairs.add(new BasicNameValuePair("cust_id",
							app.cust_id));
					pairs.add(new BasicNameValuePair("cust_name",
							app.cust_name));
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
			if (Judge.isLogin(app)) {
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
							+ app.auth_code + "&cust_id="
							+ app.cust_id + "&min_id=" + Photo_id
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
				// TODO 解析数据
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
						List<ImageData> iDatas1 = jsonImages(refresh);
						// 判断数据是否更新
						if (iDatas1.size() != 0) {
							// 判断返回的第一条数据和本地的第一条数据是否相等
							if (iDatas1.get(0).getPhoto_id() != viewDatas
									.get(index).getImageDatas().get(0)
									.getPhoto_id()) {
								viewDatas.get(index).getImageDatas()
										.addAll(0, iDatas1);
								viewDatas.get(index).getMyScrollView()
										.addHeadImages(iDatas1);
							}
						}
						viewDatas.get(index).getLl_refresh().finishRefresh();
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
			url = Constant.BaseUrl + "photo?auth_code=" + app.auth_code
					+ "&cust_id=" + app.cust_id + getBeauty();
		} else {
			url = Constant.BaseUrl + "photo?auth_code=" + app.auth_code
					+ "&car_brand_id=" + car_brand_id + "&cust_id="
					+ app.cust_id + getBeauty();
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
				+ app.cust_id, "");
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
				case 1:// 从图库获取
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
		if (requestCode == 9) {
			if (data != null) {
				// 获取图片路径
				Uri uri = data.getData();
				Intent intent = new Intent(ShowActivity.this,
						ShowCarAcitivity.class);
				intent.putExtra("image", Uri2Path.getPath(ShowActivity.this, uri));
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
			tv_name.setText(data.getStringExtra("brank"));
			car_brand_id = Integer.valueOf(data.getStringExtra("brankId"));
			getFristImages();
		}
	}

	/** 类型返回 **/
	private String getBeauty() {
		return "&photo_type=" + photo_type;
	}
}
