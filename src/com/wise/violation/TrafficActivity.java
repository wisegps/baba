package com.wise.violation;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.wise.baba.R;
import com.wise.remind.DealAddressActivity;

import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.NetThread;
import pubclas.Variable;
import customView.HScrollLayout;
import customView.OnViewChangeListener;
import data.CityData;
import xlist.XListView;
import xlist.XListView.IXListViewListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 车辆违章
 * @author honesty
 */
public class TrafficActivity extends Activity implements IXListViewListener {
	private static final String TAG = "TrafficActivity";
	private static final int refresh_traffic = 2;
	private static final int load_traffic = 3;
	private static final int get_city = 4;
	private static final int add_city = 5;
	private static final int delete_city = 6;

	TextView tv_car;
	HScrollLayout hsl_traffic;
	LinearLayout ll_image;
	private List<String> Cars = new ArrayList<String>();

	PopupWindow mPopupWindow;
	String Car_name = "";
	int total_score = 0;
	int total_fine = 0;
	int screenWidth = 200;
	int index;
	int index_car = 0;
	/** 违章信息 **/
	String Traffic = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_traffic);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		ll_image = (LinearLayout) findViewById(R.id.ll_image);
		tv_car = (TextView) findViewById(R.id.tv_car);
		tv_car.setOnClickListener(onClickListener);
		hsl_traffic = (HScrollLayout) findViewById(R.id.hsl_traffic);
		hsl_traffic.setOnViewChangeListener(new OnViewChangeListener() {
			@Override
			public void OnViewChange(int view) {
				index_car = view;
				GetTraffic();
			}
			@Override
			public void OnLastView() {}
		});

		if (Variable.carDatas != null && Variable.carDatas.size() > 0) {
			showCarTraffic();
		}else{
			//showCarTraffic();
		}
		WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		screenWidth = wm.getDefaultDisplay().getWidth()/2;//屏幕宽度
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
//			case R.id.tv_car:
//				Log.d(TAG, "index = " + index);
//				int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
//				mSpinerPopWindow.refreshData(Cars, index);
//				mSpinerPopWindow.setWidth(screenWidth);
//				mSpinerPopWindow.setHeight(px * Cars.size());
//				mSpinerPopWindow.showAsDropDown(tv_car,
//						(tv_car.getWidth() - screenWidth) / 2, 0);
//				break;
//			case R.id.iv_set:
//				ShowMenuPop();
//				break;
//			case R.id.tv_add:
//				Intent intent = new Intent(TrafficActivity.this,
//						TrafficCitiyActivity.class);
//				ArrayList<String> citys = Variable.carDatas.get(index)
//						.getVio_citys();
//				intent.putStringArrayListExtra("citys", citys);
//				intent.putExtra("requestCode", 10);
//				startActivityForResult(intent, 10);
//				mPopupWindow.dismiss();
//				break;
//			case R.id.tv_delete:
//				String city = Variable.carDatas.get(index).getVio_citys()
//						.get(index_city);
//				String url = Constant.BaseUrl + "vehicle/"
//						+ Variable.carDatas.get(index).getObj_id()
//						+ "/vio_city/" + city + "?auth_code="
//						+ Variable.auth_code;
//				new Thread(
//						new NetThread.DeleteThread(handler, url, delete_city))
//						.start();
//				mPopupWindow.dismiss();
//				break;
			}
		}
	};
	List<TrafficData> trafficDatas;
	OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Intent intent = new Intent(TrafficActivity.this, ComplainActivity.class);
			intent.putExtra("Location", trafficDatas.get(arg2 - 1).getLocation());
			startActivityForResult(intent, 2);
		}
	};

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case get_city:
				jsonCity(msg.obj.toString());
				break;
			case add_city:
				jsonAddCity(msg.obj.toString());
				break;
			case delete_city:
				Log.d(TAG, msg.obj.toString());
				jsonDeleteCity(msg.obj.toString());
				break;
			case refresh_traffic:
				Log.d(TAG, msg.obj.toString());
				trafficDatas = jsonTrafficData(msg.obj.toString());
				trafficViews.get(index_car).getxListView().setAdapter(new TrafficAdapter(trafficDatas));
				 if(trafficDatas.size() == 0){
					 trafficViews.get(index_car).getRl_Note().setVisibility(View.VISIBLE);
					 trafficViews.get(index_car).getLl_info().setVisibility(View.GONE);
				 }else{
					 trafficViews.get(index_car).getRl_Note().setVisibility(View.GONE);
					 trafficViews.get(index_car).getLl_info().setVisibility(View.VISIBLE);
					 trafficViews.get(index_car).getTv_total_score().setText(String.format(getResources().getString(R.string.total_score),total_score));
					 trafficViews.get(index_car).getTv_total_fine().setText(String.format(getResources().getString(R.string.total_fine),total_fine));
				 }
				break;
			case load_traffic:
				// List<TrafficData> Datas =
				// jsonTrafficData(msg.obj.toString());
				// trafficDatas.addAll(Datas);
				// onLoad();
				// if(trafficDatas.size() == 0){
				// isNothingNote(true);
				// }else{
				// isNothingNote(false);
				// }
				// tv_total_score.setText(String.format(getResources().getString(R.string.total_score),total_score));
				// tv_total_fine.setText(String.format(getResources().getString(R.string.total_fine),total_fine));
				break;
			}
		}
	};
	private class TrafficView{
		private XListView xListView;
		private RelativeLayout rl_Note;
		private LinearLayout ll_info;
		private TextView tv_total_score;
		private TextView tv_total_fine;
		public XListView getxListView() {
			return xListView;
		}
		public void setxListView(XListView xListView) {
			this.xListView = xListView;
		}
		public RelativeLayout getRl_Note() {
			return rl_Note;
		}
		public void setRl_Note(RelativeLayout rl_Note) {
			this.rl_Note = rl_Note;
		}
		public LinearLayout getLl_info() {
			return ll_info;
		}
		public void setLl_info(LinearLayout ll_info) {
			this.ll_info = ll_info;
		}
		public TextView getTv_total_score() {
			return tv_total_score;
		}
		public void setTv_total_score(TextView tv_total_score) {
			this.tv_total_score = tv_total_score;
		}
		public TextView getTv_total_fine() {
			return tv_total_fine;
		}
		public void setTv_total_fine(TextView tv_total_fine) {
			this.tv_total_fine = tv_total_fine;
		}		
	}
	List<TrafficView> trafficViews;

	/**
	 * 显示车辆
	 */
	private void showCarTraffic() {
		trafficViews = new ArrayList<TrafficView>();
		hsl_traffic.removeAllViews();
		showImage(Variable.carDatas.size());
		for (int i = 0; i < Variable.carDatas.size(); i++) {
			TrafficView trafficView = new TrafficView();
			View v = LayoutInflater.from(this).inflate(R.layout.item_traffic_city, null);
			hsl_traffic.addView(v);
			TextView tv_total_score = (TextView) v.findViewById(R.id.tv_total_score);
			TextView tv_total_fine = (TextView) v.findViewById(R.id.tv_total_fine);
			TextView tv_city = (TextView) v.findViewById(R.id.tv_city);
			tv_city.setText( Variable.carDatas.get(i).getNick_name());
			XListView lv_activity_traffic = (XListView) v.findViewById(R.id.lv_activity_traffic);
			lv_activity_traffic.setPullLoadEnable(false);
			lv_activity_traffic.setPullRefreshEnable(false);
			lv_activity_traffic.setOnItemClickListener(onItemClickListener);
			
			RelativeLayout rl_Note = (RelativeLayout) v.findViewById(R.id.rl_Note);
			LinearLayout ll_info = (LinearLayout) v.findViewById(R.id.ll_info);
			
			trafficView.setxListView(lv_activity_traffic);
			trafficView.setLl_info(ll_info);
			trafficView.setRl_Note(rl_Note);
			trafficView.setTv_total_fine(tv_total_fine);
			trafficView.setTv_total_score(tv_total_score);
			trafficViews.add(trafficView);
			
			rl_Note.setVisibility(View.VISIBLE);
			ll_info.setVisibility(View.GONE);
			tv_total_fine.setText("没有违章记录");
			// 读取当前城市的数据
			if (i == index_car) {
				GetTraffic();
			}
		}
		hsl_traffic.snapToScreen(index_car);
	}

	private void GetData(int arg2) {
		index = arg2;
		for (int i = 0; i < Variable.carDatas.size(); i++) {
			Variable.carDatas.get(i).setCheck(false);
		}
		Variable.carDatas.get(arg2).setCheck(true);
		Car_name = Variable.carDatas.get(arg2).getObj_name();
		tv_car.setText(Car_name);
		total_score = 0;
		total_fine = 0;
		GetCity();
	}
	/** 获取违章信息 **/
	private void GetTraffic() {
		changeImage(index_car);		
		try {
			String url = Constant.BaseUrl + "vehicle/" + Variable.carDatas.get(index_car).getObj_id() + 
					"/violation?auth_code=" + Variable.auth_code;
			new Thread(new NetThread.GetDataThread(handler, url, refresh_traffic))
					.start();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	/** 获取城市信息 **/
	private void GetCity() {
		Log.d(TAG, "从服务器读取数据");
		String url = Constant.BaseUrl + "vehicle/"
				+ Variable.carDatas.get(index).getObj_id() + "?auth_code="
				+ Variable.auth_code;
		new Thread(new NetThread.GetDataThread(handler, url, get_city)).start();
		Log.d(TAG, url);
	}

	/** 解析城市信息 **/
	private void jsonCity(String result) {
		ArrayList<String> citys = new ArrayList<String>();
		ArrayList<String> citys_code = new ArrayList<String>();
		try {
			JSONObject jsonObject = new JSONObject(result);
			JSONArray jsonArray = jsonObject.getJSONArray("vio_citys");
			for (int i = 0; i < jsonArray.length(); i++) {
				citys.add(jsonArray.getJSONObject(i).getString("vio_city_name"));
				citys_code.add(jsonArray.getJSONObject(i).getString("vio_city_code"));
			}
//			Variable.carDatas.get(index).setVio_citys(citys);
//			Variable.carDatas.get(index).setVio_citys_code(citys_code);
//			if (jsonArray.length() == 0) {
//				// 添加城市信息
//				Log.d(TAG, "添加城市信息");
//				Intent intent = new Intent(TrafficActivity.this,TrafficCitiyActivity.class);
//				intent.putExtra("requestCode", 10);
//				startActivityForResult(intent, 10);
//			} else {
//				// 显示城市信息
//				Log.d(TAG, "显示城市信息");
//				showTraffic();
//			}
		} catch (JSONException e) {
			e.printStackTrace();
//			Variable.carDatas.get(index).setVio_citys(citys);
//			Variable.carDatas.get(index).setVio_citys_code(citys_code);
//			Log.d(TAG, "添加城市信息");
//			Intent intent = new Intent(TrafficActivity.this,TrafficCitiyActivity.class);
//			intent.putExtra("requestCode", 10);
//			startActivityForResult(intent, 10);
		}
	}

	/** 解析添加城市结果 **/
	private void jsonAddCity(String result) {
		Log.d(TAG, "添加城市返回" + result);
		try {
			JSONObject jsonObject = new JSONObject(result);
			if (jsonObject.getString("status_code").equals("0")) {
				// 添加城市成功
				//Variable.carDatas.get(index).getVio_citys().add(CityName);
				//Variable.carDatas.get(index).getVio_citys_code().add(City_code);
				//index_city = (Variable.carDatas.get(index).getVio_citys().size() - 1);
				//showTraffic();
				boolean isNeed = isNeedChangeTraffic(illegalCity);
				if(isNeed){
					//TODO 完善信息
					//Intent intent = new Intent(TrafficActivity.this, VehicleActivity.class);
					//intent.putExtra("index", index);
					//intent.putExtra("city", illegalCity);
					//startActivityForResult(intent, 1);
				}else{
					System.out.println("不需要完善信息");
				}				
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/** 解析删除城市结果 **/
	private void jsonDeleteCity(String result) {
		try {
			JSONObject jsonObject = new JSONObject(result);
			if (jsonObject.getString("status_code").equals("0")) {
				// 添加城市成功
				//Variable.carDatas.get(index).getVio_citys().remove(index_city);
				//index_city = (Variable.carDatas.get(index).getVio_citys().size() - 1);
				//showTraffic();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解析车辆违章信息
	 * @param result
	 */
	private List<TrafficData> jsonTrafficData(String result) {// TODO
		// 解析数据
		List<TrafficData> Datas = new ArrayList<TrafficData>();
		try {
			JSONObject jsonObject1 = new JSONObject(result);
			if (jsonObject1.opt("total_score") != null) {
				total_score = jsonObject1.getInt("total_score");
			}
			if (jsonObject1.opt("total_fine") != null) {
				total_fine = jsonObject1.getInt("total_fine");
			}
			JSONArray jsonArray = jsonObject1.getJSONArray("data");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
					String Time = GetSystem.ChangeTimeZone(jsonObject
							.getString("vio_time").replace("T", " ")
							.substring(0, 19));
					TrafficData trafficData = new TrafficData();
					trafficData.setObj_id(jsonObject.getString("vio_id"));
					trafficData.setAction(jsonObject.getString("action"));
					trafficData.setLocation(jsonObject.getString("location"));
					trafficData.setDate(Time);
					trafficData.setScore(jsonObject.getInt("score"));
					trafficData.setFine(jsonObject.getInt("fine"));
					trafficData.setStatus(jsonObject.getInt("status"));
					trafficData.setCity(jsonObject.getString("city"));
					trafficData.setVio_total(jsonObject.getInt("vio_total"));
					Datas.add(trafficData);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Datas;
	}

	private class TrafficAdapter extends BaseAdapter {
		List<TrafficData> datas;

		public TrafficAdapter(List<TrafficData> data) {
			datas = data;
		}

		LayoutInflater mInflater = LayoutInflater.from(TrafficActivity.this);

		@Override
		public int getCount() {
			return datas.size();
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
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_traffic, null);
				holder = new ViewHolder();
				holder.tv_item_traffic_data = (TextView) convertView
						.findViewById(R.id.tv_item_traffic_data);
				holder.tv_item_traffic_adress = (TextView) convertView
						.findViewById(R.id.tv_item_traffic_adress);
				holder.tv_item_traffic_content = (TextView) convertView
						.findViewById(R.id.tv_item_traffic_content);
				holder.tv_item_traffic_fraction = (TextView) convertView
						.findViewById(R.id.tv_item_traffic_fraction);
				holder.tv_item_traffic_money = (TextView) convertView
						.findViewById(R.id.tv_item_traffic_money);
				holder.tv_status = (TextView) convertView
						.findViewById(R.id.tv_status);
				holder.tv_item_traffic_total = (TextView) convertView
						.findViewById(R.id.tv_item_traffic_total);
				holder.iv_traffic_share = (ImageView) convertView
						.findViewById(R.id.iv_traffic_share);
				holder.iv_traffic_help = (ImageView) convertView
						.findViewById(R.id.iv_traffic_help);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final TrafficData trafficData = datas.get(position);
			if (trafficData.getStatus() == 0) {
				holder.tv_status.setText("(未处理)");
				holder.tv_status.setTextColor(getResources().getColor(
						R.color.red));
			} else {
				holder.tv_status.setText("(已处理)");
				holder.tv_status.setTextColor(getResources().getColor(
						R.color.common_inactive));
			}
			holder.tv_item_traffic_data.setText(trafficData.getDate()
					.substring(0, 16));
			holder.tv_item_traffic_adress.setText(trafficData.getLocation());
			holder.tv_item_traffic_content.setText(trafficData.getAction());
			holder.tv_item_traffic_fraction.setText("扣分: "
					+ trafficData.getScore() + "分");
			holder.tv_item_traffic_money.setText("罚款: " + trafficData.getFine()
					+ "元");
			holder.tv_item_traffic_total.setText("人次: "
					+ trafficData.getVio_total() + "次");
			holder.iv_traffic_share.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					StringBuffer sb = new StringBuffer();
					sb.append("【违章】 ");
					sb.append(trafficData.getDate().substring(5, 16));
					sb.append(" " + Car_name);
					sb.append(" 在" + trafficData.getLocation() + "发生违章,");
					sb.append("违章内容: " + trafficData.getAction());
					sb.append(", 扣分: " + trafficData.getScore());
					sb.append("分, 罚款: " + trafficData.getFine()+"元, 人次: 5次");
					GetSystem.share(TrafficActivity.this, sb.toString(), "", 0,
							0, "违章", "");
				}
			});
			holder.iv_traffic_help.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(TrafficActivity.this,DealAddressActivity.class);
					intent.putExtra("Title", "车辆违章");
					intent.putExtra("Type", 3);
					intent.putExtra("city", trafficData.getCity());
					startActivity(intent);
				}
			});
			return convertView;
		}

		private class ViewHolder {
			TextView tv_item_traffic_data, tv_item_traffic_adress,
					tv_item_traffic_content, tv_item_traffic_fraction,
					tv_item_traffic_money, tv_status, tv_item_traffic_total;
			ImageView iv_traffic_share, iv_traffic_help;
		}
	}

	private class TrafficData {
		String obj_id;
		String date;
		String action;
		String location;
		String city;
		int score;
		int fine;
		int status;
		int vio_total;

		public String getObj_id() {
			return obj_id;
		}

		public void setObj_id(String obj_id) {
			this.obj_id = obj_id;
		}

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public String getAction() {
			return action;
		}

		public void setAction(String action) {
			this.action = action;
		}

		public String getLocation() {
			return location;
		}

		public void setLocation(String location) {
			this.location = location;
		}

		public int getScore() {
			return score;
		}

		public void setScore(int score) {
			this.score = score;
		}

		public int getFine() {
			return fine;
		}

		public void setFine(int fine) {
			this.fine = fine;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}

		public int getVio_total() {
			return vio_total;
		}

		public void setVio_total(int vio_total) {
			this.vio_total = vio_total;
		}

		@Override
		public String toString() {
			return "TrafficData [obj_id=" + obj_id + ", date=" + date
					+ ", action=" + action + ", location=" + location
					+ ", city=" + city + ", score=" + score + ", fine=" + fine
					+ ", status=" + status + "]";
		}
	}

	@Override
	public void onRefresh() {
		try {
			// String url = Constant.BaseUrl + "vehicle/" +
			// URLEncoder.encode(Car_name, "UTF-8") + "/violation?auth_code=" +
			// Variable.auth_code + "&max_id=" +
			// trafficDatas.get(0).getObj_id();
			// new Thread(new NetThread.GetDataThread(handler, url,
			// refresh_traffic)).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onLoadMore() {
		try {
			// Log.d(TAG, trafficDatas.get(trafficDatas.size() - 1).toString());
			// String min_id = trafficDatas.get(trafficDatas.size() -
			// 1).getObj_id();
			// String url = Constant.BaseUrl + "vehicle/" +
			// URLEncoder.encode(Car_name, "UTF-8") + "/violation?auth_code=" +
			// Variable.auth_code + "&min_id=" + min_id;
			// new Thread(new NetThread.GetDataThread(handler, url,
			// load_traffic)).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void onLoad() {
		// lv_activity_traffic.stopRefresh();
		// lv_activity_traffic.stopLoadMore();
		// lv_activity_traffic.setRefreshTime(GetSystem.GetNowTime());
	}

	String CityName;
	String City_code;
	CityData illegalCity;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 10) {
			Log.d(TAG, "添加违章城市返回");
			illegalCity = (CityData) data
					.getSerializableExtra("IllegalCity");
			City_code = illegalCity.getCityCode();
			Log.d(TAG, "illegalCity = " + illegalCity.toString());
			Log.d(TAG, "illegalCityCode = " + City_code);

			String url = Constant.BaseUrl + "vehicle/"
					+ Variable.carDatas.get(index).getObj_id()
					+ "/vio_city?auth_code=" + Variable.auth_code;
			CityName = illegalCity.getCityName();
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("vio_city_name", CityName));
			params.add(new BasicNameValuePair("vio_city_code", City_code));
			new Thread(new NetThread.putDataThread(handler, url, params,add_city)).start();
		}else if(resultCode == 1){
			Log.d(TAG, "修改车辆信息成功");
			GetTraffic();
		}
	}
	/**判断当前城市是否需要修改车辆信息**/
	private boolean isNeedChangeTraffic(CityData illegalCity) {
		if (illegalCity.getEngine() == 1) {
			// 需要发动机号
			int engineno = Variable.carDatas.get(index).getEngine_no().length();
			if(illegalCity.getEngineno() == 0){//需要全部
				if(engineno < 8){
					return true;
				}
			}else{
				if(illegalCity.getEngine() > engineno){
					return true;
				}
			}			
		}
		if (illegalCity.getFrame() == 1) {
			//需要车架号
			int classno = Variable.carDatas.get(index).getFrame_no().length();
			if(illegalCity.getFrameno() == 0){//需要全部
				if(classno < 17){
					return true;
				}
			}else{
				if(illegalCity.getFrameno() > classno){
					return true;
				}
			}			
		}
		if (illegalCity.getRegist() == 1) {
			//需要需要登记证号
			int registno = Variable.carDatas.get(index).getRegNo().length();
			if(illegalCity.getRegistno() == 0){//需要全部
				if(registno < 8){
					return true;
				}
			}else{
				if(illegalCity.getRegistno() > registno){
					return true;
				}
			}			
		}
		return false;
	}
	/**显示图片**/
	private void showImage(int size){
		ll_image.removeAllViews();
        for (int i = 0; i < size; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(R.drawable.home_body_cutover);
            imageView.setPadding(5, 0, 5, 0);
            ll_image.addView(imageView);
        }
	}
	private void changeImage(int index) {		
        for (int i = 0; i < ll_image.getChildCount(); i++) {
            ImageView imageView = (ImageView) ll_image.getChildAt(i);
            if (index == i) {
                imageView.setImageResource(R.drawable.home_body_cutover_press);
            } else {
                imageView.setImageResource(R.drawable.home_body_cutover);
            }
        }
    }
}