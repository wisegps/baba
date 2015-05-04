package com.wise.car;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;
import com.umeng.analytics.MobclickAgent;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.entity.BaseData;
import com.wise.baba.entity.BrandData;
import com.wise.baba.entity.CharacterParser;
import com.wise.baba.net.NetThread;
import com.wise.car.SideBar.OnTouchingLetterChangedListener;
import xlist.XListView;
import xlist.XListView.IXListViewListener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 车辆品牌
 */
public class ModelsActivity extends Activity implements IXListViewListener {
	private static final String TAG = "ModelsActivity";

	private static final int GET_BRANK = 1;
	private static final int REFRESH_BRANK = 2;
	private static final int GET_SERIES = 3;
	private static final int GET_TYPE = 4;
	private static final int get_image = 5;
	/**
	 * 品牌
	 */
	private RelativeLayout rl_brand; // 品牌
	private ListView lv_modles; // 车型
	private ListView lv_type; // 车款
	private ClearEditText cet_key; // 自定义搜索栏
	private XListView lv_brand = null; // 显示车的品牌
	private TextView letterIndex = null; // 字母索引选中提示框
	private SideBar sideBar = null; // 右侧字母索引栏

	private CharacterParser characterParser; // 将汉字转成拼音
	/** 品牌 **/
	private List<BrandData> brandDatas = new ArrayList<BrandData>(); // 车辆品牌集合
	/** 车型 **/
	private List<String[]> carSeries = new ArrayList<String[]>();
	/** 车款 **/
	private List<String[]> carTypes = new ArrayList<String[]>();

	private PinyinComparator comparator; // 根据拼音排序

	private BrandAdapter brandAdapter;
	private SeriesAdapter seriesAdapter;

	private ProgressDialog progressDialog;

	private TextView tv_title;
	String carSeriesId;
	String carBrankId;
	String carSerie;
	String carBrank;
	String logoUrl = "";
	private MyThread myThread = null;
	private boolean imageDownload = true;

	public static final String carBrankTitle = "carBrank"; // 数据库基础表车辆品牌的标题字段
	public static final String carSeriesTitle = "carSeries"; // 数据库基础表车辆款式的标题字段

	boolean isNeedType = true;
	boolean isNeedModel = true;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_models);
		// 初始化控件
		initViews();
		isNeedType = getIntent().getBooleanExtra("isNeedType", true);
		isNeedModel = getIntent().getBooleanExtra("isNeedModel", true);
	}

	/** 选择品牌 **/
	OnItemClickListener onBrankClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			BrandData brankModel = (BrandData) lv_brand.getItemAtPosition(arg2);
			carBrank = brankModel.getBrand();
			carBrankId = brankModel.getId();
			logoUrl = brankModel.getLogoUrl();
			if (isNeedModel) {
				// 点击品牌列表 选择车型
				getSeriesData(carBrankId);
			} else {
				Intent intent = new Intent();
				intent.putExtra("brank", carBrank);
				intent.putExtra("brankId", carBrankId);
				ModelsActivity.this.setResult(3, intent);
				ModelsActivity.this.finish();
			}
		}
	};
	/** 选择车型 **/
	OnItemClickListener onModelsClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			String[] str = (String[]) lv_modles.getItemAtPosition(arg2);
			carSeriesId = str[0];
			carSerie = str[1];
			if (isNeedType) {
				getTypeData(carSeriesId);
			} else {
				Intent intent = new Intent();
				intent.putExtra("brank", carBrank);
				intent.putExtra("brankId", carBrankId);
				intent.putExtra("series", carSerie);
				intent.putExtra("seriesId", carSeriesId);
				ModelsActivity.this.setResult(1, intent);
				ModelsActivity.this.finish();
			}
		}
	};
	/** 选择车款 **/
	OnItemClickListener onTypeClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Intent intent = new Intent();
			intent.putExtra("brank", carBrank);
			intent.putExtra("brankId", carBrankId);
			intent.putExtra("series", carSerie);
			intent.putExtra("seriesId", carSeriesId);
			intent.putExtra("typeId", carTypes.get(arg2)[0]);
			intent.putExtra("type", carTypes.get(arg2)[1]);
			intent.putExtra("logo", logoUrl);
			ModelsActivity.this.setResult(1, intent);
			ModelsActivity.this.finish();
		}
	};

	private void initViews() {
		tv_title = (TextView) findViewById(R.id.tv_title);
		rl_brand = (RelativeLayout) findViewById(R.id.rl_brand);
		lv_brand = (XListView) findViewById(R.id.lv_brand); // 品牌
		lv_brand.setOnItemClickListener(onBrankClickListener);
		lv_brand.setXListViewListener(this);
		lv_brand.setPullLoadEnable(false);
		lv_modles = (ListView) findViewById(R.id.lv_modles); // 车型
		lv_modles.setOnItemClickListener(onModelsClickListener);
		lv_type = (ListView) findViewById(R.id.lv_type); // 车款
		lv_type.setOnItemClickListener(onTypeClickListener);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		cet_key = (ClearEditText) findViewById(R.id.cet_key);
		// 根据输入框输入值的改变来过滤搜索
		cet_key.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// 当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
				filterData(s.toString());
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void afterTextChanged(Editable s) {
			}
		});
		// TODO A
		letterIndex = (TextView) findViewById(R.id.dialog);
		sideBar = (SideBar) findViewById(R.id.sidrbar);
		sideBar.setTextView(letterIndex); // 选中某个拼音索引 提示框显示
		characterParser = new CharacterParser().getInstance();
		comparator = new PinyinComparator();
		// 设置右侧触摸监听
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
			public void onTouchingLetterChanged(String s) {
				try {
					int position = brandAdapter.getPositionForSection(s
							.charAt(0));
					if (position != -1) {
						lv_brand.setSelection(position);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		getModelsData();
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (progressDialog != null) {
				progressDialog.dismiss();
			}
			switch (msg.what) {
			case GET_BRANK:
				String brankData = msg.obj.toString();
				// TODO 存到数据库
				if (msg.arg1 == 0) {// 数据库没有记录，插入数据
					insertDatabases(carBrankTitle, brankData,
							ModelsActivity.this);
				} else {// 数据库存在记录，更新数据
					if (!msg.obj.toString().equals("")) {
						// 更新数据库
						BaseData baseData = new BaseData();
						baseData.setContent(brankData);
						baseData.updateAll("Title = ?", "carBrank");
					}
				}
				if (brankData.equals("")) {
					Toast.makeText(getApplicationContext(), "获取数据失败，稍后再试",
							Toast.LENGTH_SHORT).show();
				} else {
					jsonBrands(brankData);
				}
				break;
			case REFRESH_BRANK:
				onLoad();
				String refreshData = msg.obj.toString();
				if (!"".equals(refreshData)) {
					// 更新数据库
					BaseData baseData = new BaseData();
					baseData.setContent(refreshData);
					baseData.updateAll("Title = ?", "carBrank");
				} else {
					Toast.makeText(getApplicationContext(), "获取数据失败，稍后再试", 0)
							.show();
				}
				Log.e(TAG, "imageDownload = " + imageDownload);
				imageDownload = true;
				new Thread(new Runnable() {
					public void run() {
						myThread.run();
					}
				}).start();

				break;
			case GET_SERIES: // 车型
				String seriesData = msg.obj.toString();
				if (msg.arg1 == 0) {
					insertDatabases(carBrankTitle + carBrankId, seriesData,
							ModelsActivity.this);
				} else {
					if (!seriesData.equals("")) {
						// 更新数据库
						BaseData baseData = new BaseData();
						baseData.setContent(seriesData);
						baseData.updateAll("Title = ?", carBrankTitle
								+ carBrankId);
					}
				}

				jsonSeries(seriesData);
				break;
			case GET_TYPE: // 车款
				String resultType = msg.obj.toString();
				if (msg.arg1 == 0) {
					// 更新数据库
					insertDatabases(carSeriesTitle + carSeriesId, resultType,
							ModelsActivity.this);
				} else {
					if (!resultType.equals("")) {
						// 更新数据库
						BaseData baseData = new BaseData();
						baseData.setContent(resultType);
						baseData.updateAll("Title = ?", carSeriesTitle
								+ carSeriesId);
					}
				}
				jsonType(resultType);
				break;
			case get_image:
				brandAdapter.notifyDataSetChanged();
				break;
			}
		}
	};

	/**
	 * 获取品牌
	 */
	private void getModelsData() {// TODO 获取品牌
		int on = 1;// 数据库存在记录
		List<BaseData> baseDatas = DataSupport
				.where("Title = ?", carBrankTitle).find(BaseData.class);
		if (baseDatas.size() == 0 || baseDatas.get(0).getContent() == null) {
			progressDialog = ProgressDialog.show(ModelsActivity.this,
					getString(R.string.dialog_title),
					getString(R.string.dialog_message));
			progressDialog.setCancelable(true);
			on = 0;
		} else {
			jsonBrands(baseDatas.get(0).getContent());
		}
		String url = Constant.BaseUrl + "base/car_brand";
		new NetThread.GetDataThread(handler, url, GET_BRANK, on).start();
	}

	/**
	 * 获取车型
	 */
	private void getSeriesData(String carBrankId) {
		int on = 1;// 数据库存在记录
		List<BaseData> baseDatas = DataSupport.where("Title = ?",
				carBrankTitle + carBrankId).find(BaseData.class);
		if (baseDatas.size() == 0 || baseDatas.get(0).getContent() == null) {
			on = 0;
		} else {
			jsonSeries(baseDatas.get(0).getContent());
		}
		String url = Constant.BaseUrl + "base/car_series?pid=" + carBrankId;
		new NetThread.GetDataThread(handler, url, GET_SERIES, on).start();
	}

	/**
	 * 获取车款
	 * 
	 * @param carBrankId
	 */
	private void getTypeData(String carSeriesId) {
		int on = 1;// 数据库存在记录
		List<BaseData> baseDatas = DataSupport.where("Title = ?",
				carSeriesTitle + carSeriesId).find(BaseData.class);
		if (baseDatas.size() == 0 || baseDatas.get(0).getContent() == null
				|| baseDatas.get(0).getContent().equals("")) {
			on = 0;
		} else {
			jsonType(baseDatas.get(0).getContent());
		}
		String url = Constant.BaseUrl + "base/car_type?pid=" + carSeriesId;
		new NetThread.GetDataThread(handler, url, GET_TYPE, on).start();
	}

	private void jsonBrands(String result) {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
		JSONArray jsonArray = null;
		try {
			jsonArray = new JSONArray(result);
			List<BrandData> brankList = null;
			int arrayLength = jsonArray.length();
			brankList = new ArrayList<BrandData>();
			for (int i = 0; i < arrayLength; i++) {
				JSONObject jsonObj = jsonArray.getJSONObject(i);
				BrandData brankModel = new BrandData();
				brankModel.setBrand(jsonObj.getString("name"));
				brankModel.setId(jsonObj.getString("id"));
				if (jsonObj.opt("url_icon") != null) {
					brankModel.setLogoUrl(jsonObj.getString("url_icon"));
				} else {
					brankModel.setLogoUrl("");
				}
				brankList.add(brankModel);
			}
			brandDatas = filledData(brankList);
			// 排序
			Collections.sort(brandDatas, comparator);
			lv_modles.setVisibility(View.GONE);
			lv_type.setVisibility(View.GONE);
			tv_title.setText(R.string.choice_brank);
			brandAdapter = new BrandAdapter(ModelsActivity.this, brandDatas);
			lv_brand.setAdapter(brandAdapter);

			// 刷新品牌logo
			myThread = new MyThread();
			myThread.start();
		} catch (JSONException e2) {
			e2.printStackTrace();
		}
	}

	private void jsonSeries(String result) {
		carSeries.clear();
		JSONArray jsonArray;
		try {
			jsonArray = new JSONArray(result);
			int jsonLength = jsonArray.length();
			for (int i = 0; i < jsonLength; i++) {
				String[] series = new String[2];
				try {
					series[0] = jsonArray.getJSONObject(i).getString("id");
					series[1] = jsonArray.getJSONObject(i).getString(
							"show_name");
					carSeries.add(series);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			// 隐藏车牌列表 显示车型列表
			rl_brand.setVisibility(View.GONE);
			lv_type.setVisibility(View.GONE);
			seriesAdapter = new SeriesAdapter(carSeries, ModelsActivity.this,
					1, null);
			lv_modles.setAdapter(seriesAdapter);
			tv_title.setText(R.string.choice_series);
			lv_modles.setVisibility(View.VISIBLE);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

	}

	private void jsonType(String result) {
		JSONArray jsonArray = null;
		try {
			jsonArray = new JSONArray(result);
			int jsonTypeLength = jsonArray.length();
			for (int i = 0; i < jsonTypeLength; i++) {
				String[] typeStr = new String[2];
				try {
					typeStr[0] = jsonArray.getJSONObject(i).getString("id");
					typeStr[1] = jsonArray.getJSONObject(i)
							.getString("go_name")
							+ "  "
							+ jsonArray.getJSONObject(i).getString("name");
					carTypes.add(typeStr);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			rl_brand.setVisibility(View.GONE);
			lv_modles.setVisibility(View.GONE);
			seriesAdapter = new SeriesAdapter(null, ModelsActivity.this, 2,
					carTypes);
			seriesAdapter.refresh(2, carTypes);
			lv_type.setAdapter(seriesAdapter);
			tv_title.setText(R.string.choice_type);
			lv_type.setVisibility(View.VISIBLE);
			if (jsonTypeLength == 0) {
				Intent intent = new Intent();
				intent.putExtra("brank", carBrank);
				intent.putExtra("brankId", carBrankId);
				intent.putExtra("series", carSerie);
				intent.putExtra("seriesId", carSeriesId);
				intent.putExtra("typeId", "");
				intent.putExtra("type", "");
				intent.putExtra("logo", logoUrl);
				ModelsActivity.this.setResult(1, intent);
				ModelsActivity.this.finish();
			}
		} catch (JSONException e2) {
			e2.printStackTrace();
		}
	}

	/**
	 * 为ListView填充数据
	 * 
	 * @param date
	 * @return
	 */
	private List<BrandData> filledData(List<BrandData> brankList) {
		for (int i = 0; i < brankList.size(); i++) {
			// 汉字转换成拼音
			String pinyin = characterParser.getSelling(brankList.get(i)
					.getBrand());
			String sortString = pinyin.substring(0, 1).toUpperCase();
			// 正则表达式，判断首字母是否是英文字母
			if (sortString.matches("[A-Z]")) {
				brankList.get(i).setLetter(sortString.toUpperCase());
			} else {
				brankList.get(i).setLetter("#");
			}
		}
		return brankList;

	}

	/**
	 * 根据输入框中的值来过滤数据并更新ListView
	 * 
	 * @param filterStr
	 */
	private void filterData(String filterStr) {
		try {
			List<BrandData> filterDateList = new ArrayList<BrandData>();

			// 编辑框的内容为空的时候
			if (TextUtils.isEmpty(filterStr)) {
				filterDateList = brandDatas;
			} else {
				// 匹配某些类型的品牌
				filterDateList.clear();
				for (BrandData sortModel : brandDatas) {
					String name = sortModel.getBrand();
					if (name.indexOf(filterStr.toString()) != -1
							|| characterParser.getSelling(name).startsWith(
									filterStr.toString())) {
						filterDateList.add(sortModel);
					}
				}
			}
			// 根据a-z进行排序
			Collections.sort(filterDateList, comparator);
			brandAdapter.updateListView(filterDateList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	// 下拉刷新
	public void onRefresh() {
		new Thread(new NetThread.GetDataThread(handler, Constant.BaseUrl
				+ "base/car_brand", REFRESH_BRANK)).start();
	}

	@Override
	// 上拉加载
	public void onLoadMore() {
	}

	private void onLoad() {
		// 获取当前时间
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
		String temp = sdf.format(new Date());
		String date = temp.substring(5, 16);
		lv_brand.stopRefresh();
		lv_brand.stopLoadMore();
		lv_brand.setRefreshTime(date);
	}

	protected void onDestroy() {
		super.onDestroy();
	}

	protected void onPause() {
		imageDownload = false;
		super.onPause();
	}

	protected void onRestart() {
		super.onRestart();
		MobclickAgent.onResume(this);
	}

	protected void onResume() {
		super.onResume();
		MobclickAgent.onPause(this);
	}

	// 将获取的数据存到数据库
	public static void insertDatabases(String titleName, String content,
			Context context) {
		BaseData baseData = new BaseData();
		baseData.setTitle(titleName);
		baseData.setContent(content);
		baseData.save();
	}

	public void logoImageIsExist(final String imagePath, final String name,
			final String logoUrl) {
		// 去掉最后面的斜杠
		File filePath = new File(imagePath);
		if (!filePath.exists()) {
			filePath.mkdirs();
		}
		File imageFile = new File(imagePath + name + ".png");
		if (!imageFile.exists()) {
			Bitmap bitmap = GetSystem.getBitmapFromURL(Constant.ImageUrl
					+ logoUrl);
			if (bitmap != null) {
				createImage(imagePath + name + ".png", bitmap);
			}
		}
	}

	class MyThread extends Thread {
		public void run() {
			for (int i = 0; i < brandDatas.size(); i++) {
				if (!"".equals(brandDatas.get(i).getLogoUrl())
						&& brandDatas.get(i).getLogoUrl() != null) {
					if (imageDownload) {
						logoImageIsExist(Constant.VehicleLogoPath, brandDatas
								.get(i).getId(), brandDatas.get(i).getLogoUrl());
					} else {
						continue;
					}
				}
			}
			super.run();
		}

		public void reStart() {
			run();
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
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}