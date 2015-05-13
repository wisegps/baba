package com.wise.baba;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetLocation;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.entity.BaseData;
import com.wise.baba.entity.CharacterParser;
import com.wise.baba.net.NetThread;
import com.wise.car.ClearEditText;
import com.wise.car.SideBar;
import com.wise.car.SideBar.OnTouchingLetterChangedListener;
import com.wise.state.MainActivity;


/**
 * 选择城市 欢迎界面进入 isWelcome = true; 天气界面或定位界面进入 ， 返回并保存数据在本地 秀一下选择城市进入，返回数据不保存在本地
 * 
 * @author honesty
 */
public class SelectCityActivity extends Activity {
	private static final String TAG = "SelectCityActivity";

	private static final int Get_city = 1;
	private static final int Get_host_city = 2;

	ListView lv_activity_select_city;
	LinearLayout ll_activity_select_city;
	TextView tv_select_city_title, tv_activity_select_city_location;
	private TextView letterIndex = null; // 字母索引选中提示框
	private SideBar sideBar = null; // 右侧字母索引栏

	List<CityData> cityDatas = new ArrayList<CityData>();
	List<CityData> filterCityDatas = new ArrayList<CityData>();
	List<CityData> hotDatas = new ArrayList<CityData>();

	AllCityAdapter allCityAdapter;
	CharacterParser characterParser = new CharacterParser().getInstance(); // 将汉字转成拼音
	private final PinyinComparator comparator = new PinyinComparator();; // 根据拼音排序
	String LocationCity = "";

	boolean isWelcome = false;
	boolean isShow = false;
	ProgressDialog progressDialog = null;
	AppApplication app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_select_city);
		app = (AppApplication) getApplication();
		ImageView iv_select_city_back = (ImageView) findViewById(R.id.iv_select_city_back);
		iv_select_city_back.setOnClickListener(onClickListener);
		ll_activity_select_city = (LinearLayout) findViewById(R.id.ll_activity_select_city);
		tv_select_city_title = (TextView) findViewById(R.id.tv_select_city_title);
		tv_activity_select_city_location = (TextView) findViewById(R.id.tv_activity_select_city_location);
		tv_activity_select_city_location.setOnClickListener(onClickListener);
		lv_activity_select_city = (ListView) findViewById(R.id.lv_activity_select_city);
		allCityAdapter = new AllCityAdapter(filterCityDatas);
		lv_activity_select_city.setAdapter(allCityAdapter);
		lv_activity_select_city.setOnItemClickListener(lvOnItemClickListener);

		Intent intent = getIntent();
		isWelcome = intent.getBooleanExtra("Welcome", false);
		isShow = intent.getBooleanExtra("isShow", false);
		GetCity();
		setupListView();
		letterIndex = (TextView) findViewById(R.id.dialog);
		sideBar = (SideBar) findViewById(R.id.sidrbar);
		sideBar.setTextView(letterIndex); // 选中某个拼音索引 提示框显示
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
			@Override
			public void onTouchingLetterChanged(String s) {
				for (int i = 0; i < cityDatas.size(); i++) {
					if (cityDatas.get(i).getFirst_letter().equals(s)) {
						lv_activity_select_city.setSelection(i);
						break;
					}
				}
			}
		});
		registerBroadcastReceiver();
		ClearEditText mClearEditText = (ClearEditText) findViewById(R.id.filter_edit);
		mClearEditText.addTextChangedListener(textWatcher);
		GetLocation getLocation = new GetLocation(SelectCityActivity.this);
	}

	private void InsertCity(String result, String Title) {
		BaseData baseData = new BaseData();
		baseData.setTitle(Title);
		baseData.setContent(result);
		baseData.save();
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case Get_host_city:
				String hot_citys = msg.obj.toString();
				InsertCity(hot_citys, "hotCity");
				hotDatas = GetCityList(hot_citys);
				allCityAdapter.notifyDataSetChanged();
				if (progressDialog != null) {
					progressDialog.dismiss();
				}
				List<BaseData> bDatas = DataSupport.findAll(BaseData.class);
				GetSystem.myLog(TAG, "bDatas.size() = " + bDatas.size());
				break;

			case Get_city:
				String citys = msg.obj.toString();
				InsertCity(citys, "City");
				cityDatas = GetCityList(citys);
				// 排序,添加热门
				ProcessCitys();
				filterCityDatas.addAll(cityDatas);
				allCityAdapter.notifyDataSetChanged();
				if (progressDialog != null) {
					progressDialog.dismiss();
				}
				List<BaseData> bDatas1 = DataSupport.findAll(BaseData.class);
				GetSystem.myLog(TAG, "bDatas1.size() = " + bDatas1.size());
				break;
			}
		}
	};

	private void GetCity() {
		List<BaseData> hotBaseDatas = DataSupport.where("Title = ?", "hotCity").find(BaseData.class);
		if (hotBaseDatas.size() == 0 || hotBaseDatas.get(0).getContent() == null || hotBaseDatas.get(0).getContent().equals("")) {
			if (progressDialog == null) {
				progressDialog = ProgressDialog.show(SelectCityActivity.this, getString(R.string.dialog_title), "城市信息获取中");
				progressDialog.setCancelable(true);
			}
			String url_hot = Constant.BaseUrl + "base/city?is_hot=1";
			new NetThread.GetDataThread(handler, url_hot, Get_host_city).start();
		} else {
			String Hot_Citys = hotBaseDatas.get(0).getContent();
			hotDatas = GetCityList(Hot_Citys);
		}
		List<BaseData> baseDatas = DataSupport.where("Title = ?", "City").find(BaseData.class);
		if (baseDatas.size() == 0 || baseDatas.get(0).getContent() == null || baseDatas.get(0).getContent().equals("")) {
			if (progressDialog == null) {
				progressDialog = ProgressDialog.show(SelectCityActivity.this, getString(R.string.dialog_title), "城市信息获取中");
				progressDialog.setCancelable(true);
			}
			String url = Constant.BaseUrl + "base/city?is_hot=0";
			new Thread(new NetThread.GetDataThread(handler, url, Get_city)).start();
		} else {
			String Citys = baseDatas.get(0).getContent();
			cityDatas = GetCityList(Citys);
			// 排序,添加热门
			ProcessCitys();
			filterCityDatas.addAll(cityDatas);
			allCityAdapter.notifyDataSetChanged();
		}
	}

	TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// 文本框里的内容改变触发
			filterData(s.toString());
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};

	private void filterData(String filterStr) {
		// 编辑框的内容为空的时候
		if (TextUtils.isEmpty(filterStr)) {
			ll_activity_select_city.setVisibility(View.VISIBLE);
			tv_select_city_title.setText("热门城市");
			filterCityDatas.clear();
			filterCityDatas.addAll(cityDatas);
		} else {
			filterCityDatas.clear();
			ll_activity_select_city.setVisibility(View.GONE);
			for (CityData cityData : cityDatas) {
				String name = cityData.getCity();
				if (name.indexOf(filterStr.toString()) != -1 || characterParser.getSelling(name).startsWith(filterStr.toString())) {
					filterCityDatas.add(cityData);
				}
			}
		}
		allCityAdapter.notifyDataSetChanged();
	}

	OnItemClickListener lvOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			CityData cityData = filterCityDatas.get(arg2);
			if (cityData.getCity_code() != null) {
				Log.e(TAG, cityData.getCity_spell());
				SaveCityInfo(cityData);
			}
		}
	};
	OnItemClickListener gvOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			CityData HotCityData = hotDatas.get(arg2);
			Log.d(TAG, HotCityData.toString());
			SaveCityInfo(HotCityData);
		}
	};

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.tv_activity_select_city_location:
				clickLocationCity();
				break;

			case R.id.iv_select_city_back:
				finish();
				break;
			}
		}
	};

	private void clickLocationCity() {
		if (!LocationCity.equals("")) {
			for (int i = 0; i < cityDatas.size(); i++) {
				CityData cityData = cityDatas.get(i);
				if (cityData.Type == 1 && cityData.City_code != null) {
					if (cityData.getCity().equals(LocationCity)) {
						Log.d(TAG, cityData.toString());
						SaveCityInfo(cityData);
						break;
					}
				}
			}
		}
	}

	/**
	 * 存储城市信息
	 * 
	 * @param cityData
	 */
	private void SaveCityInfo(CityData cityData) {
		if (isShow) {
			// 如果是秀一下界面进入，把城市返回即可
			Intent intent = new Intent();
			intent.putExtra("city", cityData.getCity());
			setResult(2, intent);
			finish();
			return;
		}
		app.City = cityData.getCity();
		app.Province = cityData.getProvince();
		SharedPreferences preferences = getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(Constant.sp_city, cityData.getCity());
		editor.putString(Constant.LocationCityCode, cityData.getCity_code());
		editor.putString(Constant.sp_province, cityData.getProvince());
		editor.putString(Constant.LocationCityFuel, cityData.getFuel_price());
		editor.putString(Constant.FourShopParmeter, cityData.getCity_spell());
		editor.commit();
		Toast.makeText(SelectCityActivity.this, "您选择了城市：" + cityData.getCity(), Toast.LENGTH_LONG).show();
		// 释放内存
		cityDatas.clear();
		filterCityDatas.clear();
		hotDatas.clear();
		System.gc();
		if (isWelcome) {
			startActivity(new Intent(SelectCityActivity.this, MainActivity.class));
		} else {
			setResult(2);
		}
		finish();
	}

	private void setupListView() {
		lv_activity_select_city.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (cityDatas.size() == 0) {
					return;
				}
				if (firstVisibleItem != 0) {
					String letter = cityDatas.get(firstVisibleItem).getFirst_letter();
					String NextLetter = cityDatas.get(firstVisibleItem + 1).getFirst_letter();
					// Log.d(TAG, "Item = " + firstVisibleItem + "letter = " +
					// letter + ",NextLetter = " + NextLetter);
					tv_select_city_title.setText(letter);
					if (!letter.equals(NextLetter)) {
						// 产生碰撞挤压效果
						View childView = view.getChildAt(0);
						if (childView != null) {
							int titleHeight = ll_activity_select_city.getHeight();
							int bottom = childView.getBottom();
							MarginLayoutParams params = (MarginLayoutParams) ll_activity_select_city.getLayoutParams();
							// Log.d(TAG, "bottom = " + bottom +
							// ",titleHeight = " + titleHeight);
							if (bottom < titleHeight) {
								float pushedDistance = bottom - titleHeight;
								params.topMargin = (int) pushedDistance;
								ll_activity_select_city.setLayoutParams(params);
							} else {
								if (params.topMargin != 0) {
									params.topMargin = 0;
									ll_activity_select_city.setLayoutParams(params);
								}
							}
						}
					} else {
						// Log.d(TAG, "相等");
						MarginLayoutParams params = (MarginLayoutParams) ll_activity_select_city.getLayoutParams();
						params.topMargin = 0;
						ll_activity_select_city.setLayoutParams(params);
					}
				} else {

				}
			}
		});
	}

	/**
	 * 解析城市列表
	 * 
	 * @param Citys
	 */
	private List<CityData> GetCityList(String Citys) {
		List<CityData> Datas = new ArrayList<CityData>();
		try {
			JSONArray jsonArray = new JSONArray(Citys);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				CityData cityData = new CityData();
				if (jsonObject.opt("city_code") == null) {
					cityData.setCity_code("");
				} else {
					cityData.setCity_code(jsonObject.getString("city_code"));
				}
				cityData.setType(1);
				cityData.setCity(jsonObject.getString("city"));
				cityData.setProvince(jsonObject.getString("province"));
				cityData.setCity_spell(jsonObject.getString("spell"));
				cityData.setFirst_letter(GetFristLetter(jsonObject.getString("city")));
				if (jsonObject.opt("fuel_price") == null) {
					cityData.setFuel_price("");
				} else {
					cityData.setFuel_price(jsonObject.getString("fuel_price"));
				}
				Datas.add(cityData);
			}
			return Datas;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return Datas;
	}

	/**
	 * 给城市排序，添加热门城市
	 */
	private void ProcessCitys() {
		Collections.sort(cityDatas, comparator);
		String Letter = "";
		for (int i = 0; i < cityDatas.size(); i++) {
			if (!Letter.equals(cityDatas.get(i).getFirst_letter())) {
				// 增加标题
				Letter = cityDatas.get(i).getFirst_letter();
				CityData cityData = new CityData();
				cityData.setType(1);
				cityData.setCity(Letter);
				cityData.setFirst_letter(Letter);
				cityDatas.add(i, cityData);
			}
		}

		CityData cityData = new CityData();
		cityData.setType(0);
		cityData.setCity_code("10");
		cityData.setCity("1231231231231");
		cityData.setProvince("12312312312");
		cityData.setFirst_letter("热门城市");
		cityDatas.add(0, cityData);

		CityData cityData1 = new CityData();
		cityData1.setType(1);
		cityData1.setCity("热门城市");
		cityData1.setProvince("12312312312");
		cityData1.setFirst_letter("热门城市");
		cityDatas.add(0, cityData1);
	}

	private String GetFristLetter(String city) {
		String pinyin = characterParser.getSelling(city);
		String sortString = pinyin.substring(0, 1).toUpperCase();
		// 正则表达式，判断首字母是否是英文字母
		if (sortString.matches("[A-Z]")) {
			return sortString.toUpperCase();
		}
		return "#";
	}

	private class PinyinComparator implements Comparator<CityData> {
		@Override
		public int compare(CityData o1, CityData o2) {
			if (o1.getFirst_letter().equals("@") || o2.getFirst_letter().equals("#")) {
				return -1;
			} else if (o1.getFirst_letter().equals("#") || o2.getFirst_letter().equals("@")) {
				return 1;
			} else {
				return o1.getFirst_letter().compareTo(o2.getFirst_letter());
			}
		}
	}

	private class AllCityAdapter extends BaseAdapter {
		private static final int VALUE_HOT = 0;
		private static final int VALUE_CITY = 1;
		List<CityData> citys;
		LayoutInflater mInflater;

		public AllCityAdapter(List<CityData> citys) {
			this.citys = citys;
			mInflater = LayoutInflater.from(SelectCityActivity.this);
		}

		@Override
		public int getCount() {
			return citys.size();
		}

		@Override
		public Object getItem(int position) {
			return citys.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			int type = getItemViewType(position);
			ViewHot hotholder = null;
			ViewCity cityHolder = null;
			if (convertView == null) {
				switch (type) {
				case VALUE_HOT:
					hotholder = new ViewHot();
					convertView = mInflater.inflate(R.layout.hot_city, null);
					hotholder.gv = (GridView) convertView.findViewById(R.id.gv_hot_city);
					int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
					LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, (hotDatas.size() / 4) * px);
					hotholder.gv.setLayoutParams(params);
					hotholder.gv.setAdapter(new hotAdapter());
					hotholder.gv.setOnItemClickListener(gvOnItemClickListener);
					convertView.setTag(hotholder);
					break;
				case VALUE_CITY:
					cityHolder = new ViewCity();
					convertView = mInflater.inflate(R.layout.item_select_city, null);
					cityHolder.tv_item_select_city = (TextView) convertView.findViewById(R.id.tv_item_select_city);
					cityHolder.tv_item_select_city_title = (TextView) convertView.findViewById(R.id.tv_item_select_city_title);
					if (citys.get(position).getCity_code() == null) {
						cityHolder.tv_item_select_city_title.setVisibility(View.VISIBLE);
						cityHolder.tv_item_select_city_title.setText(citys.get(position).getFirst_letter());
						cityHolder.tv_item_select_city.setVisibility(View.GONE);
					} else {
						cityHolder.tv_item_select_city.setVisibility(View.VISIBLE);
						cityHolder.tv_item_select_city.setText(citys.get(position).getCity());
						cityHolder.tv_item_select_city_title.setVisibility(View.GONE);
					}
					convertView.setTag(cityHolder);
					break;
				default:
					break;
				}
			} else {
				switch (type) {
				case VALUE_HOT:
					hotholder = (ViewHot) convertView.getTag();
					break;

				case VALUE_CITY:
					cityHolder = (ViewCity) convertView.getTag();
					if (citys.get(position).getCity_code() == null) {
						cityHolder.tv_item_select_city_title.setVisibility(View.VISIBLE);
						cityHolder.tv_item_select_city_title.setText(citys.get(position).getFirst_letter());
						cityHolder.tv_item_select_city.setVisibility(View.GONE);
					} else {
						cityHolder.tv_item_select_city.setVisibility(View.VISIBLE);
						cityHolder.tv_item_select_city.setText(citys.get(position).getCity());
						cityHolder.tv_item_select_city_title.setVisibility(View.GONE);
					}
					break;
				default:
					break;
				}
			}
			return convertView;
		}

		@Override
		public int getItemViewType(int position) {
			CityData cityData = citys.get(position);
			int type = cityData.getType();
			return type;
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		private class ViewCity {// 城市列表
			TextView tv_item_select_city, tv_item_select_city_title;
		}

		private class ViewHot {// 热门城市
			GridView gv;
		}
	}

	private class CityData {
		int Type;
		String City_code;
		String city;
		String Province;
		String First_letter;
		String Fuel_price;
		String city_spell;

		public int getType() {
			return Type;
		}

		public void setType(int type) {
			Type = type;
		}

		public String getCity_code() {
			return City_code;
		}

		public void setCity_code(String city_code) {
			City_code = city_code;
		}

		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}

		public String getProvince() {
			return Province;
		}

		public void setProvince(String province) {
			Province = province;
		}

		public String getFirst_letter() {
			return First_letter;
		}

		public void setFirst_letter(String first_letter) {
			First_letter = first_letter;
		}

		public String getFuel_price() {
			return Fuel_price;
		}

		public void setFuel_price(String fuel_price) {
			Fuel_price = fuel_price;
		}

		public String getCity_spell() {
			return city_spell;
		}

		public void setCity_spell(String city_spell) {
			this.city_spell = city_spell;
		}

		@Override
		public String toString() {
			return "CityData [Type=" + Type + ", City_code=" + City_code + ", city=" + city + ", Province=" + Province + ", First_letter=" + First_letter + "]";
		}
	}

	public class hotAdapter extends BaseAdapter {
		LayoutInflater mInflater = LayoutInflater.from(SelectCityActivity.this);

		@Override
		public int getCount() {
			return hotDatas.size();
		}

		@Override
		public Object getItem(int position) {
			return hotDatas.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_hot_city, null);
				holder = new ViewHolder();
				holder.tv_item_hot = (TextView) convertView.findViewById(R.id.tv_item_hot);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tv_item_hot.setText(hotDatas.get(position).getCity());
			return convertView;
		}

		private class ViewHolder {
			TextView tv_item_hot;
		}
	}

	private void registerBroadcastReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constant.A_City);
		registerReceiver(broadcastReceiver, intentFilter);
	}

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Constant.A_City)) {
				LocationCity = intent.getStringExtra("City");
				tv_activity_select_city_location.setText(LocationCity);
			}
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(broadcastReceiver);
	}
}
