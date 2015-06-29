package com.wise.violation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import xlist.XListView;
import xlist.XListView.IXListViewListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.FileVoilation;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.entity.BaseData;
import com.wise.baba.entity.CarData;
import com.wise.baba.entity.CityData;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.widget.HScrollLayout;
import com.wise.baba.ui.widget.OnViewChangeListener;
import com.wise.baba.ui.widget.WaitLinearLayout;
import com.wise.baba.ui.widget.WaitLinearLayout.OnFinishListener;
import com.wise.car.CarAddActivity;
import com.wise.car.CarUpdateActivity;
import com.wise.car.TrafficCitiyActivity;
import com.wise.remind.DealAddressActivity;
import com.wise.state.ManageActivity;


/**
 * 车辆违章
 * 
 * @author honesty
 */
public class TrafficActivity extends Activity implements IXListViewListener {
	private static final String TAG = "TrafficActivity";
	private static final int frist_traffic = 1;
	private static final int refresh_traffic = 2;
	private static final int update_city = 3;
	/** 获取所有违章城市数据 **/
	private static final int get_traffic = 4;

	TextView tv_car;
	HScrollLayout hsl_traffic;
	LinearLayout ll_image;

	PopupWindow mPopupWindow;
	String Car_name = "";
	int total_score = 0;
	int total_fine = 0;
	int index_car = 0;
	/** 存放对应的数据 **/
	List<TrafficView> trafficViews;
	/**true服务商跳转，false用户自己跳转**/
	boolean isService = false;
	List<CarData> carDatas;
	AppApplication app;
	/** 所有违章数据 **/
	JSONObject jsonTraffic;

	FileVoilation fileVoilation = null;//文件存数违章数据
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_traffic);
		app = (AppApplication) getApplication();
		fileVoilation = new FileVoilation(TrafficActivity.this);
		ImageView iv_update_car = (ImageView) findViewById(R.id.iv_update_car);
		iv_update_car.setOnClickListener(onClickListener);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		ll_image = (LinearLayout) findViewById(R.id.ll_image);
		tv_car = (TextView) findViewById(R.id.tv_car);
		tv_car.setOnClickListener(onClickListener);
		hsl_traffic = (HScrollLayout) findViewById(R.id.hsl_traffic);
		hsl_traffic.setOnViewChangeListener(new OnViewChangeListener() {
			@Override
			public void OnViewChange(int view, int duration) {
				index_car = view;
				GetTraffic();
			}
		});
		isService = getIntent().getBooleanExtra("isService", false);
		if(isService){
			carDatas = ManageActivity.carDatas;
			//index_car = this.getIntent().getIntExtra("index", 0);
		}else{
			carDatas = (List<CarData>) this.getIntent().getSerializableExtra("carDatas");
			if(carDatas == null || carDatas.size() == 0){
				carDatas = app.carDatas;
			}
		}
		getTraffic();
		if (carDatas != null && carDatas.size() > 0) {
			showCarTraffic();
		} else {
			AlertDialog.Builder dialog = new AlertDialog.Builder(TrafficActivity.this);
			dialog.setTitle("提示");
			dialog.setMessage("您的账户下没有车辆，是否添加。");
			dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(TrafficActivity.this, CarAddActivity.class);
					startActivityForResult(intent, 4);
				}
			});
			dialog.setNegativeButton("取消", null);
			dialog.show();
		}
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.tv_note:
				// 判断
				int Type = trafficViews.get(index_car).getType();
				if (Type == 1) {// 选择城市
					List<CityData> chooseCityDatas = new ArrayList<CityData>();
					Intent intent = new Intent(TrafficActivity.this, TrafficCitiyActivity.class);
					intent.putExtra("cityDatas", (Serializable) chooseCityDatas);
					startActivityForResult(intent, 1);
				} else if (Type == 2) {// TODO 完善车架号等
					turnCarUpdate();
				}
				break;
			case R.id.iv_update_car:
				if (carDatas.size() > 0) {
					Intent intent = new Intent(TrafficActivity.this, CarUpdateActivity.class);
					intent.putExtra("index", index_car);
					intent.putExtra("isService", isService);
					startActivity(intent);
				}
				break;
			}
		}
	};

	OnFinishListener onFristFinishListener = new OnFinishListener() {
		@Override
		public void OnFinish(int index) {
			TrafficView trafficView = trafficViews.get(index);
			trafficView.getxListView().setAdapter(new TrafficAdapter(trafficView.getTrafficDatas()));
			trafficView.getTv_total().setText(String.valueOf(trafficView.getTrafficDatas().size()));
			trafficView.getLl_wait().setVisibility(View.GONE);
			if (trafficView.getTrafficDatas().size() == 0) {
				trafficViews.get(index).getRl_Note().setVisibility(View.VISIBLE);
				trafficViews.get(index).getLl_info().setVisibility(View.GONE);
			} else {
				trafficViews.get(index).getRl_Note().setVisibility(View.GONE);
				trafficViews.get(index).getLl_info().setVisibility(View.VISIBLE);
				if (trafficViews.get(index).getTv_total_score() == null) {

				}
				trafficViews.get(index).getTv_total_score().setText(String.valueOf(total_score));
				trafficViews.get(index).getTv_total_fine().setText(String.valueOf(total_fine));
			}
		}
	};

	OnFinishListener onRefreshFinishListener = new OnFinishListener() {
		@Override
		public void OnFinish(int index) {
			TrafficView trafficView = trafficViews.get(index);
			trafficView.getxListView().setAdapter(new TrafficAdapter(trafficView.getTrafficDatas()));
			trafficView.getTv_total().setText(String.valueOf(trafficView.getTrafficDatas().size()));
			trafficView.getLl_wait().setVisibility(View.GONE);
			if (trafficView.getTrafficDatas().size() == 0) {
				trafficViews.get(index).getRl_Note().setVisibility(View.VISIBLE);
				trafficViews.get(index).getTv_note().setText("没有违章记录");
				trafficViews.get(index).getLl_info().setVisibility(View.GONE);
			} else {
				trafficViews.get(index).getRl_Note().setVisibility(View.GONE);
				trafficViews.get(index).getLl_info().setVisibility(View.VISIBLE);
				if (trafficViews.get(index).getTv_total_score() == null) {

				}
				trafficViews.get(index).getTv_total_score().setText(String.valueOf(total_score));
				trafficViews.get(index).getTv_total_fine().setText(String.valueOf(total_fine));
			}
			onLoad(index);
		}
	};

	private void onLoad(int index) {
		XListView xLV = trafficViews.get(index).getxListView();
		xLV.refreshHeaderView();
		xLV.refreshBottomView();
		xLV.stopRefresh();
		xLV.stopLoadMore();
		xLV.setRefreshTime(GetSystem.GetNowTime());
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case frist_traffic:
				trafficViews.get(msg.arg1).getLl_wait().runFast();
				trafficViews.get(msg.arg1).setTrafficDatas(jsonTrafficData(msg.obj.toString()));
				int id = carDatas.get(index_car).getObj_id();
				fileVoilation.putVoilation(id, msg.obj.toString());
				Log.i("TrafficActivity", "frist_traffic native json save"+msg.obj.toString());
				break;
			case refresh_traffic:
				trafficViews.get(msg.arg1).getxListView().runFast(msg.arg1);
				trafficViews.get(msg.arg1).setTrafficDatas(jsonTrafficData(msg.obj.toString()));
				id = carDatas.get(index_car).getObj_id();
				fileVoilation.putVoilation(id, msg.obj.toString());
				Log.i("TrafficActivity", "refresh_traffic native json save"+msg.obj.toString());
				break;
			case update_city:
				GetSystem.myLog(TAG, msg.obj.toString());
				break;
			case get_traffic:
				try {
					JSONObject jsonObj = new JSONObject(msg.obj.toString());
					jsonTraffic = jsonObj.getJSONObject("result");
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
		}
	};

	/**
	 * 显示车辆
	 */
	private void showCarTraffic() {
		trafficViews = new ArrayList<TrafficView>();
		hsl_traffic.removeAllViews();
		showImage(carDatas.size());
		for (int i = 0; i < carDatas.size(); i++) {
			TrafficView trafficView = new TrafficView();
			View v = LayoutInflater.from(this).inflate(R.layout.item_traffics, null);
			hsl_traffic.addView(v);
			TextView tv_total_score = (TextView) v.findViewById(R.id.tv_total_score);
			TextView tv_total_fine = (TextView) v.findViewById(R.id.tv_total_fine);
			TextView tv_total = (TextView) v.findViewById(R.id.tv_total);
			XListView lv_activity_traffic = (XListView) v.findViewById(R.id.lv_activity_traffic);
			lv_activity_traffic.setPullLoadEnable(false);
			lv_activity_traffic.setPullRefreshEnable(true);
			lv_activity_traffic.setXListViewListener(this);
			lv_activity_traffic.setOnFinishListener(onRefreshFinishListener);

			RelativeLayout rl_Note = (RelativeLayout) v.findViewById(R.id.rl_Note);
			LinearLayout ll_info = (LinearLayout) v.findViewById(R.id.ll_info);
			LinearLayout ll_wait_show = (LinearLayout) v.findViewById(R.id.ll_wait_show);
			WaitLinearLayout ll_wait = (WaitLinearLayout) v.findViewById(R.id.ll_wait);
			ll_wait.setOnFinishListener(onFristFinishListener);
			ll_wait.setWheelImage(R.drawable.wheel_blue);
			TextView tv_note = (TextView) v.findViewById(R.id.tv_note);
			tv_note.setOnClickListener(onClickListener);

			trafficView.setTv_note(tv_note);
			trafficView.setLl_wait(ll_wait);
			trafficView.setLl_wait_show(ll_wait_show);
			trafficView.setxListView(lv_activity_traffic);
			trafficView.setLl_info(ll_info);
			trafficView.setRl_Note(rl_Note);
			trafficView.setTv_total_fine(tv_total_fine);
			trafficView.setTv_total_score(tv_total_score);
			trafficView.setTv_total(tv_total);
			trafficView.setType(0);
			trafficViews.add(trafficView);

			rl_Note.setVisibility(View.GONE);
			ll_info.setVisibility(View.GONE);
			tv_total_fine.setText("没有违章记录");
			// 读取当前城市的数据
			if (i == index_car) {
				GetTraffic();
			}
		}
		hsl_traffic.snapToScreen(index_car);
	}

	/** 获取违章信息 **/
	private void GetTraffic() {
		// 判断数据时候读取，读取了就不再读
		String Nick_name = carDatas.get(index_car).getNick_name();
		if (Nick_name == null) {
			tv_car.setText("");
		} else {
			tv_car.setText(Nick_name);
		}
		changeImage(index_car);
		TrafficView trafficView = trafficViews.get(index_car);
		if (trafficView.getTrafficDatas() == null) {
			GetSystem.myLog(TAG, carDatas.get(index_car).toString());
			CarData carData = carDatas.get(index_car);
			ArrayList<String> citys = carData.getVio_citys();
			if (citys == null || citys.size() == 0) {// 提示添加违章城市
				trafficView.setType(1);
				List<CityData> chooseCityDatas = new ArrayList<CityData>();
				Intent intent = new Intent(TrafficActivity.this, TrafficCitiyActivity.class);
				intent.putExtra("cityDatas", (Serializable) chooseCityDatas);
				startActivityForResult(intent, 1);
				return;
			}
			// TODO 还要判断车架号和登记证号
			List<CityData> chooseCityDatas = new ArrayList<CityData>();
			for (int i = 0; i < carData.getVio_citys().size(); i++) {
				CityData cityData = new CityData();
				cityData.setCityName(carData.getVio_citys().get(i));
				cityData.setCityCode(carData.getVio_citys_code().get(i));
				// 防止数组越界
				if (i >= carData.getProvince().size()) {
					cityData.setProvince("");
				} else {// TODO 异常
					cityData.setProvince(carData.getProvince().get(i));
				}
				chooseCityDatas.add(cityData);
			}
			if (jsonTraffic != null) {
				try {
					Iterator it = jsonTraffic.keys();
					while (it.hasNext()) {
						String key = it.next().toString();
						JSONObject jsonObject = jsonTraffic.getJSONObject(key);
						JSONArray jsonArray = jsonObject.getJSONArray("citys"); // 城市
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject jsonObject3 = jsonArray.getJSONObject(i);
							String city_code = jsonObject3.getString("city_code"); // 城市编码
							int engine = jsonObject3.getInt("engine"); // 是否需要发动机号
							int engineno = jsonObject3.getInt("engineno");// 发送机好的几位
							int frame = jsonObject3.getInt("class");// 是否需要车架号
							int frameno = jsonObject3.getInt("classno");// 需要车架号的几位
							for (CityData cityData : chooseCityDatas) {
								if (cityData.getCityCode().equals(city_code)) {
									// 判断需要的车架号，发动机号是否一致
									// 发动机号
									if (engine == 0) {
										// 不需要发发动机号
									} else {
										if (engineno == 1) {// 全部
											if (carData.getEngine_no() == null || carData.getEngine_no().length() == 0) {
												trafficView.setType(2);
												trafficViews.get(index_car).getRl_Note().setVisibility(View.VISIBLE);
												trafficViews.get(index_car).getLl_info().setVisibility(View.GONE);
												trafficViews.get(index_car).getTv_note().setText("需要完整的发动机号");
												trafficViews.get(index_car).getLl_wait_show().setVisibility(View.GONE);
												return;
											}
										} else {
											if (carData.getEngine_no().length() < engineno) {
												trafficView.setType(2);
												trafficViews.get(index_car).getRl_Note().setVisibility(View.VISIBLE);
												trafficViews.get(index_car).getLl_info().setVisibility(View.GONE);
												trafficViews.get(index_car).getTv_note().setText("需要发动机号的后" + engineno + "位");
												trafficViews.get(index_car).getLl_wait_show().setVisibility(View.GONE);
												return;
											}
										}
									}
									// 车架号
									if (frame == 0) {

									} else {
										if (frameno == 1) {// 全部
											if (carData.getFrame_no() == null || carData.getFrame_no().length() == 0) {
												trafficView.setType(2);
												trafficViews.get(index_car).getRl_Note().setVisibility(View.VISIBLE);
												trafficViews.get(index_car).getLl_info().setVisibility(View.GONE);
												trafficViews.get(index_car).getTv_note().setText("需要完整的车架号");
												trafficViews.get(index_car).getLl_wait_show().setVisibility(View.GONE);
												return;
											}
										} else {
											if (carData.getFrame_no().length() < frameno) {
												trafficView.setType(2);
												trafficViews.get(index_car).getRl_Note().setVisibility(View.VISIBLE);
												trafficViews.get(index_car).getLl_info().setVisibility(View.GONE);
												trafficViews.get(index_car).getTv_note().setText("需要车架号的后" + frameno + "位");
												trafficViews.get(index_car).getLl_wait_show().setVisibility(View.GONE);
												return;
											}
										}
									}
									break;
								}
							}
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			getFristTraffic();
		}
	}

	/**
	 * 解析车辆违章信息
	 * 
	 * @param result
	 */
	private List<TrafficData> jsonTrafficData(String result) {
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
				String Time = GetSystem.ChangeTimeZone(jsonObject.getString("vio_time").replace("T", " ").substring(0, 19));
				TrafficData trafficData = new TrafficData();
				trafficData.setObj_id(jsonObject.getString("vio_id"));
				trafficData.setAction(jsonObject.getString("action"));
				trafficData.setLocation(jsonObject.getString("location"));
				trafficData.setDate(Time);
				trafficData.setScore(jsonObject.getInt("score"));
				trafficData.setFine(jsonObject.getInt("fine"));
				trafficData.setStatus(jsonObject.getInt("status"));
				trafficData.setCity(jsonObject.getString("city"));
				trafficData.setVio_total(jsonObject.getInt("total_vio"));
				trafficData.setTotal_complain(jsonObject.getInt("total_complain"));
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
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_traffic, null);
				holder = new ViewHolder();
				holder.tv_item_traffic_data = (TextView) convertView.findViewById(R.id.tv_item_traffic_data);
				holder.tv_item_traffic_adress = (TextView) convertView.findViewById(R.id.tv_item_traffic_adress);
				holder.tv_item_traffic_content = (TextView) convertView.findViewById(R.id.tv_item_traffic_content);
				holder.tv_item_traffic_fraction = (TextView) convertView.findViewById(R.id.tv_item_traffic_fraction);
				holder.tv_item_traffic_money = (TextView) convertView.findViewById(R.id.tv_item_traffic_money);
				holder.tv_status = (TextView) convertView.findViewById(R.id.tv_status);
				holder.tv_item_traffic_total = (TextView) convertView.findViewById(R.id.tv_item_traffic_total);
				holder.tv_item_traffic_complain = (TextView) convertView.findViewById(R.id.tv_item_traffic_complain);
				holder.tv_item_traffic_city = (TextView) convertView.findViewById(R.id.tv_item_traffic_city);
				holder.iv_traffic_share = (ImageView) convertView.findViewById(R.id.iv_traffic_share);
				holder.rl_adress = (RelativeLayout) convertView.findViewById(R.id.rl_adress);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final TrafficData trafficData = datas.get(position);
			if (trafficData.getStatus() == 0) {
				holder.tv_status.setText("(未处理)");
				holder.tv_status.setTextColor(getResources().getColor(R.color.red));
			} else {
				holder.tv_status.setText("(已处理)");
				holder.tv_status.setTextColor(getResources().getColor(R.color.common_inactive));
			}
			holder.tv_item_traffic_city.setText(trafficData.getCity());
			holder.tv_item_traffic_data.setText(trafficData.getDate().substring(0, 16));
			holder.tv_item_traffic_adress.setText(trafficData.getLocation());
			holder.tv_item_traffic_content.setText(trafficData.getAction());
			holder.tv_item_traffic_fraction.setText(String.valueOf(trafficData.getScore()));
			holder.tv_item_traffic_money.setText(String.valueOf(trafficData.getFine()));
			holder.tv_item_traffic_total.setText(String.valueOf(trafficData.getVio_total()));
			holder.tv_item_traffic_complain.setText(String.valueOf(trafficData.getTotal_complain()));
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
					sb.append("分, 罚款: " + trafficData.getFine() + "元, 人次: 5次");
					GetSystem.share(TrafficActivity.this, sb.toString(), "", 0, 0, "违章", "");
				}
			});
			holder.rl_adress.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(TrafficActivity.this, DealAddressActivity.class);
					intent.putExtra("Title", "车辆违章");
					intent.putExtra("Type", 3);
					intent.putExtra("city", trafficData.getCity());
					startActivity(intent);
				}
			});
			holder.tv_item_traffic_content.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(TrafficActivity.this, ComplainActivity.class);
					intent.putExtra("index", position);
					intent.putExtra("Location", trafficData.getLocation());
					intent.putExtra("total_vio", trafficData.getVio_total());
					startActivityForResult(intent, 1);
				}
			});
			return convertView;
		}

		private class ViewHolder {
			TextView tv_item_traffic_data, tv_item_traffic_adress, tv_item_traffic_content, tv_item_traffic_fraction, tv_item_traffic_money, tv_status,
					tv_item_traffic_total, tv_item_traffic_complain, tv_item_traffic_city;
			ImageView iv_traffic_share;
			RelativeLayout rl_adress;
		}
	}

	@Override
	public void onRefresh() {
		// 刷新 需不需要标记
		TrafficView trafficView = trafficViews.get(index_car);
		trafficView.getxListView().startHeaderWheel();
		try {
			String url = Constant.BaseUrl + "vehicle/" + carDatas.get(index_car).getObj_id() + "/violation?auth_code=" + app.auth_code;
			new NetThread.GetDataThread(handler, url, refresh_traffic, index_car).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onLoadMore() {
		try {
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	String CityName;
	String City_code;
	CityData illegalCity;

	/** 显示图片 **/
	private void showImage(int size) {
		ll_image.removeAllViews();
		for (int i = 0; i < size; i++) {
			ImageView imageView = new ImageView(this);
			imageView.setImageResource(R.drawable.round_press);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(15, 15); // ,
																					// 1是可选写的
			lp.setMargins(5, 0, 5, 0);
			imageView.setLayoutParams(lp);

			ll_image.addView(imageView);
		}
	}

	private void changeImage(int index) {
		for (int i = 0; i < ll_image.getChildCount(); i++) {
			ImageView imageView = (ImageView) ll_image.getChildAt(i);
			if (index == i) {
				imageView.setImageResource(R.drawable.round);
			} else {
				imageView.setImageResource(R.drawable.round_press);
			}
		}
	}

	private class TrafficView {
		private XListView xListView;
		private RelativeLayout rl_Note;
		private LinearLayout ll_info;
		private LinearLayout ll_wait_show;
		private WaitLinearLayout ll_wait;
		private TextView tv_total_score;
		private TextView tv_total_fine;
		private TextView tv_total;
		private List<TrafficData> trafficDatas;
		private TextView tv_note;
		private int status;
		private int Type;

		public int getType() {
			return Type;
		}

		/** 状态，默认0 ， 没选择城市1，车架号不对是2 **/
		public void setType(int type) {
			Type = type;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public TextView getTv_note() {
			return tv_note;
		}

		public void setTv_note(TextView tv_note) {
			this.tv_note = tv_note;
		}

		public List<TrafficData> getTrafficDatas() {
			return trafficDatas;
		}

		public void setTrafficDatas(List<TrafficData> trafficDatas) {
			this.trafficDatas = trafficDatas;
		}

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

		public TextView getTv_total() {
			return tv_total;
		}

		public void setTv_total(TextView tv_total) {
			this.tv_total = tv_total;
		}

		public LinearLayout getLl_wait_show() {
			return ll_wait_show;
		}

		public void setLl_wait_show(LinearLayout ll_wait_show) {
			this.ll_wait_show = ll_wait_show;
		}

		public WaitLinearLayout getLl_wait() {
			return ll_wait;
		}

		public void setLl_wait(WaitLinearLayout ll_wait) {
			this.ll_wait = ll_wait;
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
		int total_complain;
		
		

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

		public int getTotal_complain() {
			return total_complain;
		}

		public void setTotal_complain(int total_complain) {
			this.total_complain = total_complain;
		}

		@Override
		public String toString() {
			return "TrafficData [obj_id=" + obj_id + ", date=" + date + ", action=" + action + ", location=" + location + ", city=" + city + ", score=" + score
					+ ", fine=" + fine + ", status=" + status + ", vio_total=" + vio_total + ", total_complain=" + total_complain + "]";
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		System.out.println("requestCode = " + requestCode + " , resultCode = " + resultCode);
		// 返回
		if (requestCode == 1 && resultCode == 2) {
			// 添加违章城市返回
			@SuppressWarnings("unchecked")
			List<CityData> chooseCityDatas = (List<CityData>) data.getSerializableExtra("cityDatas");
			if (chooseCityDatas.size() == 0) {
				// 没有选择违章城市
				trafficViews.get(index_car).getRl_Note().setVisibility(View.VISIBLE);
				trafficViews.get(index_car).getLl_info().setVisibility(View.GONE);
				trafficViews.get(index_car).getTv_note().setText("没选择违章城市");
				trafficViews.get(index_car).getLl_wait_show().setVisibility(View.GONE);
			} else {
				CarData carData = carDatas.get(index_car);
				// 违章信息保存
				ArrayList<String> vio_citys = new ArrayList<String>();
				ArrayList<String> vio_citys_code = new ArrayList<String>();
				ArrayList<String> provinces = new ArrayList<String>();
				for (int j = 0; j < chooseCityDatas.size(); j++) {
					String vio_city_name = chooseCityDatas.get(j).getCityName();
					String vio_location = chooseCityDatas.get(j).getCityCode();
					String province = chooseCityDatas.get(j).getProvince();
					vio_citys.add(vio_city_name);
					vio_citys_code.add(vio_location);
					provinces.add(province);
				}
				carData.setVio_citys(vio_citys);
				carData.setVio_citys_code(vio_citys_code);
				carData.setProvince(provinces);

				// 保存
				String url = Constant.BaseUrl + "vehicle/" + carData.getObj_id() + "/vio_city?auth_code=" + app.auth_code;
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("vio_citys", jsonList(chooseCityDatas)));
				new NetThread.putDataThread(handler, url, params, update_city).start();

				String engine_no = carData.getEngine_no();
				String frame_no = carData.getFrame_no();

				for (CityData cityData : chooseCityDatas) {
					// 发送机号
					if (cityData.getEngine() == 0) {

					} else {
						if (cityData.getEngineno() == 1) {// 全部
							if (engine_no == null || engine_no.length() == 0) {
								// TODO 还要判断车架号
								turnCarUpdate();
								return;
							}
						} else {
							if (engine_no == null || engine_no.length() < cityData.getEngineno()) {
								turnCarUpdate();
								return;
							}
						}
					}
					// 车架号
					if (cityData.getFrame() == 0) {

					} else {
						if (cityData.getFrameno() == 1) {// 全部
							if (frame_no == null || frame_no.length() == 0) {
								turnCarUpdate();
								return;
							}
						} else {
							if (frame_no == null || frame_no.length() < cityData.getFrameno()) {
								turnCarUpdate();
								return;
							}
						}
					}
				}
				getFristTraffic();
			}
		} else if (requestCode == 2 && resultCode == 3) {
			// 跳转到车辆信息界面，保存返回，直接读取数据
			GetSystem.myLog(TAG, carDatas.get(index_car).toString());
			getFristTraffic();
		} else if (requestCode == 2 && resultCode == 0) {
			// 跳转到车辆信息界面，未保存数据返回，判断判断原有的车架号是否符合规则
			TrafficView trafficView = trafficViews.get(index_car);
			CarData carData = carDatas.get(index_car);
			List<CityData> chooseCityDatas = new ArrayList<CityData>();
			for (int i = 0; i < carData.getVio_citys().size(); i++) {
				CityData cityData = new CityData();
				cityData.setCityName(carData.getVio_citys().get(i));
				cityData.setCityCode(carData.getVio_citys_code().get(i));
				// 防止数组越界
				if (i >= carData.getProvince().size()) {
					cityData.setProvince("");
				} else {// TODO 异常
					cityData.setProvince(carData.getProvince().get(i));
				}
				chooseCityDatas.add(cityData);
			}
			if (jsonTraffic != null) {
				try {
					Iterator it = jsonTraffic.keys();
					while (it.hasNext()) {
						String key = it.next().toString();
						JSONObject jsonObject = jsonTraffic.getJSONObject(key);
						JSONArray jsonArray = jsonObject.getJSONArray("citys"); // 城市
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject jsonObject3 = jsonArray.getJSONObject(i);
							String city_code = jsonObject3.getString("city_code"); // 城市编码
							int engine = jsonObject3.getInt("engine"); // 是否需要发动机号
							int engineno = jsonObject3.getInt("engineno");// 发送机好的几位
							int frame = jsonObject3.getInt("class");// 是否需要车架号
							int frameno = jsonObject3.getInt("classno");// 需要车架号的几位
							for (CityData cityData : chooseCityDatas) {
								if (cityData.getCityCode().equals(city_code)) {
									// 判断需要的车架号，发动机号是否一致
									// 发动机号
									if (engine == 0) {
										// 不需要发发动机号
									} else {
										if (engineno == 1) {// 全部
											if (carData.getEngine_no() == null || carData.getEngine_no().length() == 0) {
												trafficView.setType(2);
												trafficViews.get(index_car).getRl_Note().setVisibility(View.VISIBLE);
												trafficViews.get(index_car).getLl_info().setVisibility(View.GONE);
												trafficViews.get(index_car).getTv_note().setText("需要完整的发动机号");
												trafficViews.get(index_car).getLl_wait_show().setVisibility(View.GONE);
												return;
											}
										} else {
											if (carData.getEngine_no() == null || carData.getEngine_no().length() < engineno) {
												trafficView.setType(2);
												trafficViews.get(index_car).getRl_Note().setVisibility(View.VISIBLE);
												trafficViews.get(index_car).getLl_info().setVisibility(View.GONE);
												trafficViews.get(index_car).getTv_note().setText("需要发动机号的后" + engineno + "位");
												trafficViews.get(index_car).getLl_wait_show().setVisibility(View.GONE);
												return;
											}
										}
									}
									// 车架号
									if (frame == 0) {

									} else {
										if (frameno == 1) {// 全部
											if (carData.getFrame_no() == null || carData.getFrame_no().length() == 0) {
												trafficView.setType(2);
												trafficViews.get(index_car).getRl_Note().setVisibility(View.VISIBLE);
												trafficViews.get(index_car).getLl_info().setVisibility(View.GONE);
												trafficViews.get(index_car).getTv_note().setText("需要完整的车架号");
												trafficViews.get(index_car).getLl_wait_show().setVisibility(View.GONE);
												return;
											}
										} else {
											if (carData.getFrame_no() == null || carData.getFrame_no().length() < frameno) {
												trafficView.setType(2);
												trafficViews.get(index_car).getRl_Note().setVisibility(View.VISIBLE);
												trafficViews.get(index_car).getLl_info().setVisibility(View.GONE);
												trafficViews.get(index_car).getTv_note().setText("需要车架号的后" + frameno + "位");
												trafficViews.get(index_car).getLl_wait_show().setVisibility(View.GONE);
												return;
											}
										}
									}
									break;
								}
							}
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		} else if (requestCode == 1 && resultCode == 3) {
			// 评论违章返回
			int index = data.getIntExtra("index", 0);
			int size = data.getIntExtra("size", 0);
			trafficViews.get(index_car).getTrafficDatas().get(index).setTotal_complain(size);
			trafficViews.get(index).getxListView().setAdapter(new TrafficAdapter(trafficViews.get(index_car).getTrafficDatas()));
		} else if (requestCode == 4 && resultCode == 3) {
			// 添加车辆返回
			if (carDatas != null && carDatas.size() > 0) {
				showCarTraffic();
			}
		}
	}

	private String jsonList(List<CityData> cityDatas) {
		try {
			JSONArray jsonArray = new JSONArray();
			for (int i = 0; i < cityDatas.size(); i++) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("vio_city_name", cityDatas.get(i).getCityName());
				jsonObject.put("vio_location", cityDatas.get(i).getCityCode());
				jsonObject.put("province", cityDatas.get(i).getProvince());
				jsonArray.put(jsonObject);
			}
			String jsonString = jsonArray.toString().replaceAll("\"vio_city_name\":", "vio_city_name:").replaceAll("\"vio_location\":", "vio_location:")
					.replaceAll("\"province\":", "province:");
			return jsonString;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "[]";
	}

	/** 跳转到更新车辆信息界面 **/
	private void turnCarUpdate() {
		Intent intent = new Intent(TrafficActivity.this, CarUpdateActivity.class);
		intent.putExtra("index", index_car);
		intent.putExtra("isService", isService);
		startActivityForResult(intent, 2);
	}

	/** 获取违章数据 **/
	private void getFristTraffic() {
		trafficViews.get(index_car).getRl_Note().setVisibility(View.GONE);
		trafficViews.get(index_car).getLl_wait_show().setVisibility(View.VISIBLE);
		trafficViews.get(index_car).getLl_wait().startWheel(index_car);
		try {
			int id = carDatas.get(index_car).getObj_id();
			String url = Constant.BaseUrl + "vehicle/" + id + "/violation?auth_code=" + app.auth_code;
			Log.i("TrafficActivity", "getFristTraffic url "+url);
			
			String json = fileVoilation.getVoilation(id);
			
			Log.i("TrafficActivity", "getJson json " + json);
			if(json!= null && !json.equals("")){
				trafficViews.get(index_car).getLl_wait().runFast();
				trafficViews.get(index_car).setTrafficDatas(jsonTrafficData(json));
			}
			new NetThread.GetDataThread(handler, url, frist_traffic, index_car).start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取违章城市需要的车架号和发送机号
	 */
	private void getTraffic() {
		// 获取本地存储数据
		List<BaseData> baseDatas = DataSupport.where("Title = ?", "Violation").find(BaseData.class);
		if (baseDatas.size() == 0 || baseDatas.get(0).getContent() == null || baseDatas.get(0).getContent().equals("")) {
			// 到服务器读取违章数据
			String url = Constant.BaseUrl + "violation/city?cuth_code=" + app.auth_code;
			new NetThread.GetDataThread(handler, url, get_traffic).start();
		} else {
			try {
				JSONObject jsonObj = new JSONObject(baseDatas.get(0).getContent());
				jsonTraffic = jsonObj.getJSONObject("result");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}