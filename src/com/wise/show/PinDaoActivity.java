package com.wise.show;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.BaseData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import pubclas.Constant;
import pubclas.NetThread;

import com.wise.baba.R;
import com.wise.car.ModelsActivity;
import com.wise.car.PinyinComparator;

import data.BrandData;
import data.CharacterParser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PinDaoActivity extends Activity {
	// 返回类型码
	public static final int CARTYPE = 3;// 车型
	public static final int SEX = 4;// 性别
	public static final int CITY = 5;// 城市

	ListView pindaoListView, pindao_show;
	List<String> pindaoData = new ArrayList<String>();
	PindaoAdapter mAdapter;

	private CharacterParser characterParser; // 将汉字转成拼音
	private PinyinComparator comparator;
	private ProgressDialog progressDialog;
	private static final int GET_BRANK = 1;
	private static final int Get_city = 2;
	/** 品牌 **/
	private List<BrandData> brandDatas = new ArrayList<BrandData>(); // 车辆品牌集合

	List<String> cityDatas = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_pindao);

		pindaoData.add("车型");
		pindaoData.add("性别");
		pindaoData.add("城市");
		comparator = new PinyinComparator();
		characterParser = new CharacterParser().getInstance();
		getDate(ModelsActivity.carBrankTitle, Constant.BaseUrl
				+ "base/car_brand", GET_BRANK);
		getCity();

		pindaoListView = (ListView) findViewById(R.id.list_pindao);
		pindao_show = (ListView) findViewById(R.id.list_pindao_show);

		mAdapter = new PindaoAdapter(pindaoData);
		pindaoListView.setAdapter(mAdapter);

		pindaoListView.setOnItemClickListener(onItemClickListener);

		findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			switch (position) {
			case 0:// 车型
				pindao_show.setVisibility(View.VISIBLE);
				List<String> carTypes = new ArrayList<String>();
				for (BrandData b : brandDatas) {
					carTypes.add(b.getBrand());
				}
				PindaoAdapter carAdapter = new PindaoAdapter(carTypes);
				pindao_show.setAdapter(carAdapter);
				getResult(carTypes, CARTYPE);
				break;
			case 1:// 性别
				pindao_show.setVisibility(View.VISIBLE);
				List<String> sex = new ArrayList<String>();
				sex.add("男");
				sex.add("女");
				PindaoAdapter sexAdapter = new PindaoAdapter(sex);
				pindao_show.setAdapter(sexAdapter);
				getResult(sex, SEX);
				break;
			case 2:// 城市
				pindao_show.setVisibility(View.VISIBLE);
				PindaoAdapter cityAdapter = new PindaoAdapter(cityDatas);
				pindao_show.setAdapter(cityAdapter);
				getResult(cityDatas, CITY);
				break;
			}
		}
	};

	private void getResult(final List<String> s, final int resultCode) {
		pindao_show.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent();
				intent.putExtra("result", s.get(position));
				setResult(resultCode);
				PinDaoActivity.this.finish();
			}
		});
	}

	class PindaoAdapter extends BaseAdapter {
		List<String> datas = null;
		LayoutInflater mInflater = LayoutInflater.from(PinDaoActivity.this);

		public PindaoAdapter(List<String> data) {
			datas = data;
		}

		@Override
		public int getCount() {
			return datas == null ? 0 : datas.size();
		}

		@Override
		public Object getItem(int position) {
			return datas.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder mHolder = null;
			if (convertView == null) {
				mHolder = new Holder();
				convertView = mInflater.inflate(R.layout.item_short_province,
						null);
				mHolder.pindaoView = (TextView) convertView
						.findViewById(R.id.tv_province);
				convertView.setTag(mHolder);
			} else {
				mHolder = (Holder) convertView.getTag();
			}
			mHolder.pindaoView.setGravity(Gravity.LEFT);
			mHolder.pindaoView.setText(datas.get(position));
			return convertView;
		}

		class Holder {
			TextView pindaoView;
		}
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
				if (!"".equals(brankData)) {
					BaseData baseData = new BaseData();
					baseData.setTitle(ModelsActivity.carBrankTitle);
					baseData.setContent(brankData);
					baseData.save();
					jsonBrands(brankData);
				} else {
					Toast.makeText(getApplicationContext(), "获取数据失败，稍后再试", 0)
							.show();
				}
				break;
			}

		}
	};

	private void getCity() {
		List<BaseData> baseDatas = DataSupport.where("Title = ?", "City").find(
				BaseData.class);
		System.out.println("baseDatas.size() = " + baseDatas.size());
		if (baseDatas.size() == 0 || baseDatas.get(0).getContent() == null
				|| baseDatas.get(0).getContent().equals("")) {
			if (progressDialog == null) {
				progressDialog = ProgressDialog.show(PinDaoActivity.this,
						getString(R.string.dialog_title), "城市信息获取中");
				progressDialog.setCancelable(true);
			}
			String url = Constant.BaseUrl + "base/city?is_hot=0";
			new Thread(new NetThread.GetDataThread(handler, url, Get_city))
					.start();
		} else {
			String Citys = baseDatas.get(0).getContent();
			cityDatas = GetCityList(Citys);
		}
	}

	/**
	 * 解析城市列表
	 * 
	 * @param Citys
	 */
	private List<String> GetCityList(String Citys) {
		List<String> cityType = new ArrayList<String>();
		try {
			JSONArray jsonArray = new JSONArray(Citys);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				cityType.add(jsonObject.getString("city"));
			}
			return cityType;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return cityType;
	}

	/**
	 * @param whereValues
	 *            查询数据库时搜索条件
	 * @param url
	 *            数据库没有数据 服务器获取的地址
	 * @param handlerWhat
	 *            服务器获取handler异步处理的标识
	 */
	private void getDate(String whereValues, String url, int handlerWhat) {
		List<BaseData> baseDatas = DataSupport.where("Title = ?", "carBrank")
				.find(BaseData.class);
		if (baseDatas.size() == 0 || baseDatas.get(0).getContent() == null
				|| baseDatas.get(0).getContent().equals("")) {
			progressDialog = ProgressDialog.show(PinDaoActivity.this,
					getString(R.string.dialog_title),
					getString(R.string.dialog_message));
			progressDialog.setCancelable(true);
			new NetThread.GetDataThread(handler, url, handlerWhat).start();
		} else {
			if (handlerWhat == GET_BRANK) {
				jsonBrands(baseDatas.get(0).getContent());
			}
		}
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
}
