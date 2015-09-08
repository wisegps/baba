package com.wise.setting;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Const;
import com.wise.baba.app.Constant;
import com.wise.baba.db.ShareCards;
import com.wise.baba.entity.CardsData;
import com.wise.baba.ui.fragment.FragmentHome;

public class ChooseCard extends Activity {
	ListView card_choose;
	List<CardsData> list = new ArrayList<CardsData>();;
	// String cardsString;
	// JSONArray cardsJson = new JSONArray();

	public static final int CARDCODE = 1;
	private String[] cards = { Const.TAG_POI, Const.TAG_CAR, Const.TAG_SPEED,
			Const.TAG_NEWS, Const.TAG_WEATHER, Const.TAG_SERVICE, Const.TAG_NAV ,Const.TAG_AIR};
	private ShareCards cardsSharePreferences;
	private String[] sharedCards = null;
	private int custType = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_info_card);
		AppApplication app = (AppApplication) getApplication();
		custType = app.cust_type;
		cardsSharePreferences = new ShareCards(this);
		sharedCards = cardsSharePreferences.get();

		for (int j = 0; j < cards.length; j++) {

			if (cards[j].equals(Const.TAG_SERVICE) && (custType == 1)) {
				// 个人不要服务商卡片
				continue;
			}

			boolean isExist = false;

			if (sharedCards != null) {
				for (int i = 0; i < sharedCards.length; i++) {
					if (sharedCards[i].equals(cards[j])) {
						isExist = true;
						break;
					}
				}
			}

			if (isExist == false) {
				CardsData inItem = new CardsData();
				inItem.setIcon(Constant.picture[j]);
				inItem.setTitle(Constant.title[j]);
				inItem.setContent(Constant.content[j]);
				inItem.setCardName(cards[j]);
				list.add(inItem);
			}
		}

		// SharedPreferences sharedPreferences = getSharedPreferences(
		// "card_choose", Activity.MODE_PRIVATE);
		// cardsString = sharedPreferences.getString("cardsJson", "");
		card_choose = (ListView) findViewById(R.id.card_choose);
		CardAdapter cardAdapter = new CardAdapter();
		card_choose.setAdapter(cardAdapter);
		card_choose.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String cardName = list.get(position).getCardName();
				cardsSharePreferences.put(cardName);
				ChooseCard.this.finish();
			}
		});
		findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// SharedPreferences sharedPreferences = getSharedPreferences(
				// "card_choose", Activity.MODE_PRIVATE);
				// SharedPreferences.Editor editor = sharedPreferences.edit();
				// editor.putString("cardsJson", cardsJson.toString());
				// editor.commit();
				// Intent intent = new Intent();
				// intent.putExtra("cardsJson", cardsJson.toString());
				// setResult(CARDCODE, intent);
				FragmentHome.isChange = true;
				finish();
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// SharedPreferences sharedPreferences = getSharedPreferences(
			// "card_choose", Activity.MODE_PRIVATE);
			// SharedPreferences.Editor editor = sharedPreferences.edit();
			// editor.putString("cardsJson", cardsJson.toString());
			// editor.commit();
			// Intent intent = new Intent();
			// intent.putExtra("cardsJson", cardsJson.toString());
			// setResult(CARDCODE, intent);
			FragmentHome.isChange = true;
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	class CardAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return list == null ? 0 : list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			final Holder mHolder;
			if (convertView == null) {
				mHolder = new Holder();
				convertView = (LayoutInflater.from(ChooseCard.this)).inflate(
						R.layout.item_info_add, null);
				mHolder.info_icon = (ImageView) convertView
						.findViewById(R.id.info_icon);
				mHolder.tv_info_title = (TextView) convertView
						.findViewById(R.id.tv_info_title);
				mHolder.tv_info_content = (TextView) convertView
						.findViewById(R.id.tv_info_content);
				mHolder.item_add = (TextView) convertView
						.findViewById(R.id.item_add);
				convertView.setTag(mHolder);
			} else {
				mHolder = (Holder) convertView.getTag();
			}
			mHolder.info_icon.setImageResource(list.get(position).getIcon());
			mHolder.tv_info_title.setText(list.get(position).getTitle());
			mHolder.tv_info_content.setText(list.get(position).getContent());
			// if (sharedCards != null) {
			// for (int i = 0; i < sharedCards.length; i++) {
			// if (list.get(position).getCardName().equals(sharedCards[i])) {
			// return null;
			// }
			// }
			// }

			mHolder.item_add.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					// mHolder.item_add.setText("已添加");
					// mHolder.item_add.setEnabled(false);
				}
			});
			return convertView;
		}

		class Holder {
			ImageView info_icon;
			TextView tv_info_title, tv_info_content;
			TextView item_add;
		}
	}
}
