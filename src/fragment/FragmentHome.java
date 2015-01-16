package fragment;

import org.json.JSONObject;

import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.Info;
import pubclas.Judge;
import pubclas.NetThread;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.umeng.analytics.MobclickAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.MoreActivity;
import com.wise.baba.R;
import com.wise.car.CarLocationActivity;
import com.wise.setting.LoginActivity;
import com.wise.show.ShowActivity;

import data.CarData;

/**
 * 车况信息
 * 
 * @author honesty
 * 
 */
public class FragmentHome extends Fragment {
	private static final String TAG = "FaultActivity";
	/** 获取消息数据 **/
	private static final int get_counter = 8;

	ImageView iv_noti;
	LinearLayout ll_cards;
	private FragmentManager fragmentManager;

	AppApplication app;
	FragmentCarInfo fragmentCarInfo;
	FragmentScrollMessage fragmentScrollMessage;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_home, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		app = (AppApplication) getActivity().getApplication();
		ll_cards = (LinearLayout) getActivity().findViewById(R.id.ll_cards);
		Button bt_show = (Button) getActivity().findViewById(R.id.bt_show);
		bt_show.setOnClickListener(onClickListener);
		ImageView iv_back = (ImageView) getActivity().findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		iv_noti = (ImageView) getActivity().findViewById(R.id.iv_noti);
		ImageView iv_menu = (ImageView) getActivity().findViewById(R.id.iv_menu);
		iv_menu.setOnClickListener(onClickListener);
		getActivity().findViewById(R.id.iv_location_hot).setOnClickListener(onClickListener);

		fragmentManager = getActivity().getSupportFragmentManager();

		if (Judge.isLogin(app)) {// 已登录
			GetSystem.myLog(TAG, "已登录,app.carDatas = " + app.carDatas.size());
			getCounter();

			if (app.carDatas.size() == 0) {// 如果没有车则显示广告

			}
		} else {// 未登录
			GetSystem.myLog(TAG, "未登录,app.carDatas = " + app.carDatas.size());
			// 给个临时id
			app.cust_id = "0";
			app.auth_code = "127a154df2d7850c4232542b4faa2c3d";
			// setLoginView();
			Intent intent = new Intent(getActivity(), LoginActivity.class);
			startActivity(intent);
		}
		getCards();
	}

	/** 显示卡片布局 **/
	private void getCards() {
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		// 默认显示的布局
		if (app.cust_type == Info.ServiceProvider) {// 服务商

		} else {
			// 车辆卡片
			fragmentCarInfo = new FragmentCarInfo();
			transaction.add(R.id.ll_cards, fragmentCarInfo);
			// 滚动消息卡片
			fragmentScrollMessage = new FragmentScrollMessage();
			transaction.add(R.id.ll_cards, fragmentScrollMessage);
		}
		// 可选布局
		String cards = "weather,hotNews";
		String[] sCards = cards.split(",");
		for (int i = 0; i < sCards.length; i++) {
			if (sCards[i].equals("weather")) {
				FragmentWeather fragmentWeather = new FragmentWeather();
				transaction.add(R.id.ll_cards, fragmentWeather);
			} else if (sCards[i].equals("hotNews")) {
				FragmentHotNews fragmentHotNews = new FragmentHotNews();
				transaction.add(R.id.ll_cards, fragmentHotNews);
			}
		}
		transaction.commit();
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_menu:
				startActivityForResult(new Intent(getActivity(), MoreActivity.class), 5);
				break;
			case R.id.iv_location_hot:
				if (!Judge.isLogin(app)) {
					startActivity(new Intent(getActivity(), LoginActivity.class));
				} else {
					if (app.carDatas == null || app.carDatas.size() == 0) {
						goCarMap(false);
						return;
					}
					int index = 0;
					if(fragmentCarInfo != null){
						fragmentCarInfo.getIndex();
					}
					CarData carData = app.carDatas.get(index);
					String device_id = carData.getDevice_id();
					if (device_id == null || device_id.equals("") || device_id.equals("0")) {
						goCarMap(false);
					} else {
						goCarMap(true);
					}

				}
				break;
			case R.id.bt_show:
				startActivity(new Intent(getActivity(), ShowActivity.class));
				break;
			case R.id.ll_adress:
				goCarMap(true);
				break;
			}
		}
	};

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case get_counter:
				jsonCounter(msg.obj.toString());
				break;
			}
		}
	};

	// 跳转到地图界面
	private void goCarMap(boolean b) {
		int index = 0;
		if(fragmentCarInfo != null){
			fragmentCarInfo.getIndex();
		}
		Intent intent = new Intent(getActivity(), CarLocationActivity.class);
		intent.putExtra("index", index);
		intent.putExtra("isHotLocation", b);
		startActivity(intent);
	}

	boolean isChange = false;
	/** 刷新所有布局 **/
	public void resetAllView() {
		isChange = true;
	}

	/** 刷新车辆卡片 **/
	public void refreshCarInfo() {
		fragmentCarInfo.initDataView();
		// 通知滚动消息刷新数据
		fragmentScrollMessage.getScrollMessage();
		getCounter();
	}

	/** 退出登录后刷新 **/
	public void setLoginOutView() {
		// 清除有消息的红点提醒
		clearCounter();
		// 通知车辆信息卡片退出登录
		if (fragmentCarInfo != null) {
			fragmentCarInfo.setLoginView();
		}
		// 通知滚动消息刷新数据
		if (fragmentScrollMessage != null) {
			fragmentScrollMessage.getScrollMessage();
		}
	}

	/** 清除消息红点提醒 **/
	private void clearCounter() {
		app.noti_count = 0;
		iv_noti.setVisibility(View.GONE);
	}

	/** 获取消息数据 **/
	private void getCounter() {
		String url = Constant.BaseUrl + "customer/" + app.cust_id + "/counter?auth_code=" + app.auth_code;
		new NetThread.GetDataThread(handler, url, get_counter).start();
	}

	/** 解析消息数据 **/
	private void jsonCounter(String str) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			if (jsonObject.opt("noti_count") != null) {
				app.noti_count = jsonObject.getInt("noti_count");
			}
			if (jsonObject.opt("vio_count") != null) {
				app.vio_count = jsonObject.getInt("vio_count");
			}
			setNotiView();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		setNotiView();
		MobclickAgent.onResume(getActivity());
		System.out.println("isChange = " + isChange + " , app.cust_type = " + app.cust_type);
		if (isChange) {
			isChange = false;
			// 删除所有view
			ll_cards.removeAllViews();
			// 加载对应的view
			getCards();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(getActivity());
	}

	/** 设置提醒 **/
	private void setNotiView() {
		if (app.noti_count == 0 && app.vio_count == 0) {
			// 隐藏提醒
			iv_noti.setVisibility(View.GONE);
		} else {
			// 显示提醒
			iv_noti.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 5 && resultCode == 1) {
			if (onExitListener != null) {
				onExitListener.exit();
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	OnExitListener onExitListener;

	public void setOnExitListener(OnExitListener onExitListener) {
		this.onExitListener = onExitListener;
	}

	public interface OnExitListener {
		public abstract void exit();
	}
}
