package com.wise.car;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pubclas.Constant;
import pubclas.NetThread;
import pubclas.Variable;
import com.wise.baba.R;
import data.CharacterParser;
import data.CityData;
import data.ProvinceData;
import sql.DBExcute;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 违章城市选择 
 * @author
 */
public class TrafficCitiyActivity extends Activity {
	
	public static final String showProvinceAction = "province";
	public static final String showCityAction = "city";
	
	ListView lv_provnice, lv_city;
	GridView gv_choose_city;
	ImageView iv_back;

	ProgressDialog myDialog = null;	
	private DBExcute dBExcute = new DBExcute();
	CharacterParser characterParser;
	PinyinComparator comparator;
	
	CityAdapter cityAdapter;
	ProvinceAdapter provinceAdapter;
	ChooseAdapter chooseAdapter;
	List<CityData> cityDatas;
	List<CityData> chooseCityDatas = new ArrayList<CityData>();
	
	int index = 0;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_traffic_city);
		lv_provnice = (ListView) findViewById(R.id.lv_provnice);
		lv_city = (ListView) findViewById(R.id.lv_city);
		gv_choose_city = (GridView)findViewById(R.id.gv_choose_city);
		chooseAdapter = new ChooseAdapter();
		gv_choose_city.setAdapter(chooseAdapter);
		iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);

		//citys = getIntent().getStringArrayListExtra("citys");		
		
		characterParser = new CharacterParser().getInstance();
		comparator = new PinyinComparator();
		// 相关监听
		lv_provnice.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				// 选择省份之后选择城市
				index = arg2;
				provinceAdapter.notifyDataSetChanged();
				showCity(arg2);
			}
		});
		// 点击选择城市
		lv_city.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				if(!cityDatas.get(arg2).isCheck()){
					if(chooseCityDatas.size() == 2){
						Toast.makeText(TrafficCitiyActivity.this, "最多只能添加2个城市", Toast.LENGTH_SHORT).show();
						return;
					}
					cityDatas.get(arg2).setCheck(true);
					cityAdapter.notifyDataSetChanged();
					//添加
					String CityCode = cityDatas.get(arg2).getCityCode();
					boolean isNeed = true;
					for(CityData cityData : chooseCityDatas){
						if(cityData.getCityCode().equals(CityCode)){
							isNeed = false;
							break;
						}
					}
					if(isNeed){
						chooseCityDatas.add(cityDatas.get(arg2));
						chooseAdapter.notifyDataSetChanged();
					}
				}				
			}
		});
		
		if(Variable.provinceDatas == null) {
			String jsonData = dBExcute.selectIllegal(TrafficCitiyActivity.this);
			if(jsonData == null){
				myDialog = ProgressDialog.show(TrafficCitiyActivity.this,
						getString(R.string.dialog_title),
						getString(R.string.dialog_message));
				myDialog.setCancelable(true);
				new Thread(new NetThread.GetDataThread(handler, Constant.BaseUrl
						+ "violation/city?cuth_code=" + Variable.auth_code, 0))
						.start();
			}else{
				// 解析数据 并且更新
				Variable.provinceDatas = parseJson(jsonData);
				provinceAdapter = new ProvinceAdapter(Variable.provinceDatas);
				lv_provnice.setAdapter(provinceAdapter);
				showCity(0);
			}	
		} else {		
			provinceAdapter = new ProvinceAdapter(Variable.provinceDatas);
			lv_provnice.setAdapter(provinceAdapter);
			showCity(0);
		}
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				Intent intent = new Intent();
				intent.putExtra("cityDatas", (Serializable)chooseCityDatas);
				setResult(2, intent);
				TrafficCitiyActivity.this.finish();
				break;
			}
		}
	};
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (!"".equals(msg.obj.toString())) {
				ContentValues values = new ContentValues();
				values.put("json_data", msg.obj.toString());
				dBExcute.InsertDB(TrafficCitiyActivity.this, values,Constant.TB_IllegalCity);
				Variable.provinceDatas = parseJson(msg.obj.toString());
				provinceAdapter = new ProvinceAdapter(Variable.provinceDatas);
				lv_provnice.setAdapter(provinceAdapter);
				myDialog.dismiss();
				showCity(0);
			}
		}
	};
	/**
	 * 显示城市
	 * @param index
	 */
	private void showCity(int index){
		cityDatas = Variable.provinceDatas.get(index).getIllegalCityList();				
		if(chooseCityDatas != null){
			for(int j = 0 ; j < cityDatas.size() ; j++){
				for(int i = 0 ; i < chooseCityDatas.size() ; i++){
					if(cityDatas.get(j).getCityCode().equals(chooseCityDatas.get(i).getCityCode())){
						cityDatas.get(j).setCheck(true);
						break;
					}
				}
			}
			
		}else{
			System.out.println("citys为空");
		}
		cityAdapter = new CityAdapter(cityDatas);
		lv_city.setAdapter(cityAdapter);
	}
	// 获取省份 TODO
	public List<ProvinceData> parseJson(String jsonData) {
		List<ProvinceData> provinceDatas = new ArrayList<ProvinceData>();
		try {
			JSONObject jsonObj = new JSONObject(jsonData);
			JSONObject result = jsonObj.getJSONObject("result");
			Iterator it = result.keys();
			while (it.hasNext()) {
				List<CityData> illegalCityList = new ArrayList<CityData>();
				ProvinceData provinceModel = new ProvinceData();

				String key = it.next().toString();
				JSONObject jsonObject = result.getJSONObject(key);
				String province = jsonObject.getString("province"); // 省份

				JSONArray jsonArray = jsonObject.getJSONArray("citys"); // 城市
				for (int i = 0; i < jsonArray.length(); i++) {
					CityData illegalCity = new CityData();
					JSONObject jsonObject3 = jsonArray.getJSONObject(i);
					illegalCity.setAbbr(jsonObject3.getString("abbr"));
					illegalCity.setCityCode(jsonObject3.getString("city_code"));
					illegalCity.setCityName(jsonObject3.getString("city_name"));
					illegalCity.setEngine(jsonObject3.getInt("engine"));
					illegalCity.setEngineno(jsonObject3.getInt("engineno"));
					illegalCity.setRegist(jsonObject3.getInt("regist"));
					illegalCity.setRegistno(jsonObject3.getInt("registno"));
					illegalCity.setFrame(jsonObject3.getInt("class"));
					illegalCity.setFrameno(jsonObject3.getInt("classno"));
					illegalCityList.add(illegalCity);
				}
				provinceModel.setIllegalCityList(illegalCityList);
				provinceModel.setProvinceName(province);
				provinceDatas.add(provinceModel);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return filledData(provinceDatas);
	}

	/**将省份汉字转为拼音**/
	private List<ProvinceData> filledData(List<ProvinceData> provinceModelList) {
		for (int i = 0; i < provinceModelList.size(); i++) {
			ProvinceData sortModel = provinceModelList.get(i);
			// 汉字转换成拼音
			String pinyin = characterParser.getSelling(provinceModelList.get(i)
					.getProvinceName());
			String sortString = pinyin.substring(0, 1).toUpperCase();
			sortModel.setProvinceLetter(sortString.toUpperCase()); // 设置拼音
		}
		Collections.sort(provinceModelList, comparator);
		return provinceModelList;
	}

	/**根据拼音首字母排序**/
	class PinyinComparator implements Comparator<ProvinceData> {
		public int compare(ProvinceData o1, ProvinceData o2) {
			if (o1.getProvinceLetter().equals("@")
					|| o2.getProvinceLetter().equals("#")) {
				return -1;
			} else if (o1.getProvinceLetter().equals("#")
					|| o2.getProvinceLetter().equals("@")) {
				return 1;
			} else {
				return o1.getProvinceLetter().compareTo(o2.getProvinceLetter());
			}
		}
	}
	/**省**/
	class ProvinceAdapter extends BaseAdapter{
		LayoutInflater mInflater = LayoutInflater.from(TrafficCitiyActivity.this);
		List<ProvinceData> provinceDatas;
		/**省**/
		private ProvinceAdapter(List<ProvinceData> Datas){
			provinceDatas = Datas;
		}
		@Override
		public int getCount() {
			return provinceDatas.size();
		}
		@Override
		public Object getItem(int position) {
			return provinceDatas.get(position);
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_traffic_city_name, null);
				holder = new ViewHolder();
				holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tv_name.setText(provinceDatas.get(position).getProvinceName());
			if(index == position){
				holder.tv_name.setBackgroundColor(Color.GRAY);
			}else{
				holder.tv_name.setBackgroundColor(Color.WHITE);
			}
			return convertView;
		}
		class ViewHolder {
			TextView tv_name;
		}
	}
	/**市**/
	class CityAdapter extends BaseAdapter{
		LayoutInflater mInflater = LayoutInflater.from(TrafficCitiyActivity.this);
		List<CityData> cityDatas;
		/**市**/
		private CityAdapter(List<CityData> Datas){
			cityDatas = Datas;
		}
		@Override
		public int getCount() {
			return cityDatas.size();
		}
		@Override
		public Object getItem(int position) {
			return cityDatas.get(position);
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_traffic_city_name, null);
				holder = new ViewHolder();
				holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			CityData cityData = cityDatas.get(position);
			if(cityData.isCheck()){
				holder.tv_name.setText(cityDatas.get(position).getCityName());
				holder.tv_name.setBackgroundResource(R.color.gray_light);
			}else{
				holder.tv_name.setText(cityDatas.get(position).getCityName());
				holder.tv_name.setBackgroundResource(R.color.white);
			}
			return convertView;
		}
		class ViewHolder {
			TextView tv_name;
		}
	}
	public class ChooseAdapter extends BaseAdapter {
        LayoutInflater mInflater = LayoutInflater.from(TrafficCitiyActivity.this);
        @Override
        public int getCount() {
            return chooseCityDatas.size();
        }
        @Override
        public Object getItem(int position) {
            return chooseCityDatas.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_hot, null);
                holder = new ViewHolder();
                holder.tv_item_hot = (TextView) convertView.findViewById(R.id.tv_item_hot);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_item_hot.setText(chooseCityDatas.get(position).getCityName());
            return convertView;
        }
        private class ViewHolder {
            TextView tv_item_hot;
        }
    }
}