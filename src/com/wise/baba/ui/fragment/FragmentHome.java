package com.wise.baba.ui.fragment;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Const;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.biz.Judge;
import com.wise.baba.db.ShareCards;
import com.wise.baba.entity.Info;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.adapter.FragmentHelper;
import com.wise.baba.ui.adapter.OnCardMenuListener;
import com.wise.baba.util.DensityUtil;
import com.wise.car.CarLocationActivity;
import com.wise.setting.LoginActivity;
import com.wise.show.ShowActivity;

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
	FragmentManager fragmentManager;
	AppApplication app;
	Map<String, Fragment> cards = new LinkedHashMap<String, Fragment>();
	private View rootView;
	private ShareCards cardsSharePreferences;

	private boolean isLoaded = false;

	FragmentHelper fragmentHelper = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i("fragment", "onCreateView");
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_home, container,
					false);
		} else {
			ViewGroup parent = (ViewGroup) rootView.getParent();
			if (null != parent) {
				parent.removeView(rootView);
			}
		}
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		app = (AppApplication) getActivity().getApplication();
		cardsSharePreferences = new ShareCards(this.getActivity());
		ll_cards = (LinearLayout) getActivity().findViewById(R.id.ll_cards);
		Button bt_show = (Button) getActivity().findViewById(R.id.bt_show);
		bt_show.setOnClickListener(onClickListener);
		ImageView iv_back = (ImageView) getActivity()
				.findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		iv_noti = (ImageView) getActivity().findViewById(R.id.iv_noti);
		fragmentManager = this.getChildFragmentManager();
		if (Judge.isLogin(app)) {// 已登录
			GetSystem.myLog(TAG, "已登录,app.carDatas = " + app.carDatas.size());
			// getCounter();
		} else {// 未登录
			GetSystem.myLog(TAG, "未登录,app.carDatas = " + app.carDatas.size());
			// 给个临时id
			app.cust_id = "0";
			app.auth_code = "127a154df2d7850c4232542b4faa2c3d";
			Intent intent = new Intent(getActivity(), LoginActivity.class);
			startActivity(intent);
		}
		getCards();

	}

	FragmentTransaction transaction;

	@Override
	public void onStart() {
		super.onStart();
	}

	/** 显示卡片布局 **/
	private void getCards() {
		isChange = false;
		cards.clear();
		Log.i("fragment", "设置卡片布局");
		// // 可选布局
		String sharedCards[] = cardsSharePreferences.get();
		for (int i = 0; i < sharedCards.length; i++) {
			String cardName = sharedCards[i];
			Log.i("fragment", "get " + i + " " + cardName);
			if (cardName.equals(Const.TAG_POI)) {
				Log.i("fragment", "设置周边卡片布局");
				removeFragment(Const.TAG_POI);
				transaction = fragmentManager.beginTransaction();
				FragmentHomePOI fragmetnHomePOI = new FragmentHomePOI();
				fragmetnHomePOI.setOnCardMenuListener(onCardMenuListener);
				transaction.add(R.id.ll_cards, fragmetnHomePOI, Const.TAG_POI);
				transaction.commit();
				cards.put(Const.TAG_POI, fragmetnHomePOI);
			} else if (cardName.equals(Const.TAG_CAR)) {
				Log.i("fragment", "设置车辆卡片布局");
				removeFragment(Const.TAG_CAR);
				transaction = fragmentManager.beginTransaction();
				FragmentCarInfo fragmentCarInfo = new FragmentCarInfo();
				fragmentCarInfo.setOnCardMenuListener(onCardMenuListener);
				transaction.add(R.id.ll_cards, fragmentCarInfo, Const.TAG_CAR);
				transaction.commit();
				cards.put(Const.TAG_CAR, fragmentCarInfo);
			} else if (cardName.equals(Const.TAG_SPEED)) {
				Log.i("fragment", "设置速度卡片布局");
				removeFragment(Const.TAG_SPEED);
				transaction = fragmentManager.beginTransaction();
				FragmentHomeSpeed fragmenSpeed = new FragmentHomeSpeed();
				fragmenSpeed.setOnCardMenuListener(onCardMenuListener);
				transaction.add(R.id.ll_cards, fragmenSpeed, Const.TAG_SPEED);
				transaction.commit();
				cards.put(Const.TAG_SPEED, fragmenSpeed);
			} else if (cardName.equals(Const.TAG_NEWS)) {
				Log.i("fragment", "设置新闻卡片布局2");
				removeFragment(Const.TAG_NEWS);
				transaction = fragmentManager.beginTransaction();
				FragmentHotNews fragmentHotNews = new FragmentHotNews();
				fragmentHotNews.setOnCardMenuListener(onCardMenuListener);
				transaction.add(R.id.ll_cards, fragmentHotNews, Const.TAG_NEWS);
				transaction.commit();
				cards.put(Const.TAG_NEWS, fragmentHotNews);
			} else if (cardName.equals(Const.TAG_WEATHER)) {
				Log.i("fragment", "设置天气卡片布局2");
				removeFragment(Const.TAG_WEATHER);
				transaction = fragmentManager.beginTransaction();
				FragmentWeather fragmentWeather = new FragmentWeather();
				fragmentWeather.setOnCardMenuListener(onCardMenuListener);
				transaction.add(R.id.ll_cards, fragmentWeather,
						Const.TAG_WEATHER);
				transaction.commit();
				cards.put(Const.TAG_WEATHER, fragmentWeather);
			} else if (cardName.equals(Const.TAG_SERVICE)
					&& app.cust_type == Info.ServiceProvider) {
				Log.i("fragment", "设置服务商卡片布局");
				removeFragment(Const.TAG_SERVICE);
				transaction = fragmentManager.beginTransaction();
				FragmentService fragmentService = new FragmentService();
				transaction.add(R.id.ll_cards, fragmentService,
						Const.TAG_SERVICE);
				transaction.commit();
				cards.put(Const.TAG_SERVICE, fragmentService);
			} else if (cardName.equals(Const.TAG_NAV)) {
				Log.i("fragment", "设置导航卡片布局");
				removeFragment(Const.TAG_NAV);
				transaction = fragmentManager.beginTransaction();
				FragmentHomeNavigation fragmenNavigation = new FragmentHomeNavigation();
				fragmenNavigation.setOnCardMenuListener(onCardMenuListener);
				transaction
						.add(R.id.ll_cards, fragmenNavigation, Const.TAG_NAV);
				transaction.commit();
				cards.put(Const.TAG_NAV, fragmenNavigation);
			} else if (cardName.equals(Const.TAG_AIR)) {
				Log.i("fragment", "设置净化器卡片布局");
				removeFragment(Const.TAG_AIR);
				transaction = fragmentManager.beginTransaction();
				FragmentHomeAir fragmentHomeAir = new FragmentHomeAir();
				fragmentHomeAir.setOnCardMenuListener(onCardMenuListener);
				transaction
						.add(R.id.ll_cards, fragmentHomeAir, Const.TAG_AIR);
				transaction.commit();
				cards.put(Const.TAG_AIR, fragmentHomeAir);
			}
		}
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
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

	/**
	 * 跳转到地图界面
	 */
	private void goCarMap(boolean b) {
		int index = 0;
		FragmentCarInfo fragmentCarInfo = (FragmentCarInfo) cards
				.get(Const.TAG_CAR);
		if (fragmentCarInfo != null) {
			fragmentCarInfo.getIndex();
		}
		Intent intent = new Intent(getActivity(), CarLocationActivity.class);
		intent.putExtra("index", index);
		intent.putExtra("isHotLocation", b);
		startActivity(intent);
	}

	public static boolean isChange = false;

	/** 刷新所有布局 **/
	public void resetAllView() {
		isChange = true;
	}

	public void removeFragment(String tag) {
		Fragment fragment = fragmentManager.findFragmentByTag(tag);
		if (fragment != null && fragment.isAdded()) {
			Log.i("fragment", "remove fragment" + tag);
			fragmentManager.beginTransaction().remove(fragment).commit();
		}

	}

	public void removeAllFragment() {

		Iterator<String> keys = cards.keySet().iterator();
		while (keys.hasNext()) {
			removeFragment(keys.next());
		}

	}

	/** 刷新车辆卡片 **/
	public void refreshCarInfo() {
		FragmentCarInfo fragmentCarInfo = (FragmentCarInfo) cards
				.get(Const.TAG_CAR);
		if (fragmentCarInfo != null) {
			fragmentCarInfo.initDataView();
		}
		// 通知滚动消息刷新数据
		getCounter();
	}

	/** 退出登录后刷新 **/
	public void setLoginOutView() {
		// 清除有消息的红点提醒
		clearCounter();
		// 通知车辆信息卡片退出登录

		FragmentCarInfo fragmentCarInfo = (FragmentCarInfo) cards
				.get(Const.TAG_CAR);
		if (fragmentCarInfo != null) {
			fragmentCarInfo.setLoginView();
		}
	}

	/** 清除消息红点提醒 **/
	private void clearCounter() {
		app.noti_count = 0;
		iv_noti.setVisibility(View.GONE);
	}

	/** 获取消息数据 **/
	private void getCounter() {
		String url = Constant.BaseUrl + "customer/" + app.cust_id
				+ "/counter?auth_code=" + app.auth_code;
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

		Log.i("fragment", "onResume");
		super.onResume();
		if (isLoaded == false) {

		}
		setNotiView();

		MobclickAgent.onResume(getActivity());
		if (isChange) {
			removeAllFragment();
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
			// TODO 暂时不显示 显示提醒
			iv_noti.setVisibility(View.GONE);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// 点击设置跳转到更多页面，退出系统后接受
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

	PopupWindow mPopupWindow;
	OnCardMenuListener onCardMenuListener = new OnCardMenuListener() {
		@Override
		public void showCarMenu(final String CardName) {

			// 弹出卡片菜单
			LayoutInflater mLayoutInflater = LayoutInflater.from(getActivity());
			View popunwindwow = mLayoutInflater.inflate(R.layout.pop_card_menu,
					null);
			TextView text_card_top = (TextView) popunwindwow
					.findViewById(R.id.text_card_top);
			text_card_top.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.i("fragment", "置顶 " + CardName);

					setCardsInSharedPreferences(CardName);
					topAnimation(CardName);
					mPopupWindow.dismiss();
				}
			});

			TextView text_card_delete = (TextView) popunwindwow
					.findViewById(R.id.text_card_delete);
			text_card_delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.i("fragment", "delete card name " + CardName);
					new Handler().post(new Runnable() {
						@Override
						public void run() {
							cards.remove(CardName);
							removeFragment(CardName);
							deleteCardsInSharedPreferences(CardName);
							mPopupWindow.dismiss();
						}
					});

				}
			});

			TextView text_card_cancle = (TextView) popunwindwow
					.findViewById(R.id.text_card_cancel);
			text_card_cancle.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mPopupWindow.dismiss();
				}
			});

			mPopupWindow = new PopupWindow(popunwindwow,
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			mPopupWindow.setAnimationStyle(R.style.PopupAnimation);
			mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
			mPopupWindow.setFocusable(true);
			mPopupWindow.setOutsideTouchable(true);
			mPopupWindow.showAtLocation(ll_cards, Gravity.BOTTOM, 0, 0);
		}
	};

	/**
	 * 置顶动画
	 */
	public void topAnimation(final String cardName) {

		Fragment fragment = fragmentManager.findFragmentByTag(cardName);
		View view = fragment.getView();
		float y = view.getY();
		int height = view.getHeight();

		Log.i("top", "当前卡片的顶部坐标:" + y);

		Log.i("top", "当前卡片高度:" + height);

		Iterator it = cards.keySet().iterator();

		float margin = DensityUtil.dip2px(this.getActivity(), 10);
		float base = DensityUtil.dip2px(this.getActivity(), 10);

		while (it.hasNext()) {
			String key = (String) it.next();
			Fragment fg = fragmentManager.findFragmentByTag(key);
			View otherView = fg.getView();
			float otherY = otherView.getY();
			if (otherY < y) {
				ObjectAnimator otherAnimator = ObjectAnimator.ofFloat(
						otherView, "y", otherY, otherY + height + margin);
				otherAnimator.setDuration(1000);
				otherAnimator.start();
			}
		}
		ObjectAnimator bbjectAnimator = ObjectAnimator.ofFloat(view, "y", y,
				base);

		bbjectAnimator.setDuration(1000);

		bbjectAnimator.start();

	}

	/** 删除卡片后保存最新的数据在本地 **/
	private void deleteCardsInSharedPreferences(String cardName) {
		String cardNames[] = cardsSharePreferences.get();
		cardsSharePreferences.clear();
		for (int i = 0; i < cardNames.length; i++) {
			if(!cardNames[i].equals(cardName)){
				cardsSharePreferences.put(cardNames[i]);
				Log.i("fragment", "保存 " + i + " " + cardNames[i]);
			}
			
		}
		
	}

	/** 置顶后保存最新的数据在本地 **/
	private void setCardsInSharedPreferences(String topCardName) {
		String cardNames[] = cardsSharePreferences.get();
		cardsSharePreferences.clear();
		cardsSharePreferences.put(topCardName);
		for (int i = 0; i < cardNames.length; i++) {
			if (!cardNames[i].equals(topCardName)) {
				cardsSharePreferences.put(cardNames[i]);
				Log.i("fragment", "保存 " + i + " " + cardNames[i]);
			}

		}

	}

}
