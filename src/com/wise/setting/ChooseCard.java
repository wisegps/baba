package com.wise.setting;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pubclas.Constant;

import com.wise.baba.R;

import data.CardsData;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ChooseCard extends Activity {
	ListView card_choose;
	List<CardsData> list = new ArrayList<CardsData>();
	String weather;
	String hotNews;
	JSONArray cardsJson = new JSONArray();
	public static final int CARDCODE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_info_card);

		int[] picture = { R.drawable.icon_dianyuan_normal,
				R.drawable.icon_jinqi_normal };
		String[] title = { "天气", "新闻" };
		String[] content = { "今天天气概况", "最新新闻更新和内容" };
		String[] cardName = { "weather", "hotNews" };

		for (int i = 0; i < 2; i++) {
			CardsData inItem = new CardsData();
			inItem.setIcon(picture[i]);
			inItem.setTitle(title[i]);
			inItem.setContent(content[i]);
			inItem.setCardName(cardName[i]);
			list.add(inItem);
		}

		SharedPreferences sharedPreferences = getSharedPreferences(
				"card_choose", Activity.MODE_PRIVATE);
		weather = sharedPreferences.getString("weather", "");
		hotNews = sharedPreferences.getString("hotNews", "");

		card_choose = (ListView) findViewById(R.id.card_choose);
		CardAdapter cardAdapter = new CardAdapter();
		card_choose.setAdapter(cardAdapter);

		findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("cardsJson", cardsJson.toString());
				setResult(CARDCODE, intent);
				finish();
			}
		});
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
		public View getView(final int position, View convertView, ViewGroup arg2) {
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
				mHolder.item_add = (Button) convertView
						.findViewById(R.id.item_add);
				convertView.setTag(mHolder);
			} else {
				mHolder = (Holder) convertView.getTag();
			}
			mHolder.info_icon.setImageResource(list.get(position).getIcon());
			mHolder.tv_info_title.setText(list.get(position).getTitle());
			mHolder.tv_info_content.setText(list.get(position).getContent());

			if (position == 0) {
				if (weather.equals(Constant.cards[position])) {
					mHolder.item_add.setText("已添加");
					mHolder.item_add.setEnabled(false);
				} else {
					mHolder.item_add.setText("添加");
					mHolder.item_add.setEnabled(true);
				}
			}
			if (position == 1) {
				if (hotNews.equals(Constant.cards[position])) {
					mHolder.item_add.setText("已添加");
					mHolder.item_add.setEnabled(false);
				} else {
					mHolder.item_add.setText("添加");
					mHolder.item_add.setEnabled(true);
				}
			}
			mHolder.item_add.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						JSONObject object = new JSONObject();
						object.put("icon", list.get(position).getIcon());
						object.put("title", list.get(position).getTitle());
						object.put("content", list.get(position).getContent());
						object.put("cardName", list.get(position).getCardName());
						cardsJson.put(object);
					} catch (JSONException e) {
						e.printStackTrace();
					}

					SharedPreferences sharedPreferences = getSharedPreferences(
							"card_choose", Activity.MODE_PRIVATE);
					SharedPreferences.Editor editor = sharedPreferences.edit();
					if (position == 0) {
						editor.putString("weather", Constant.cards[0]);
					} else if (position == 1) {
						editor.putString("hotNews", Constant.cards[1]);
					}
					editor.commit();
					mHolder.item_add.setText("已添加");
					mHolder.item_add.setEnabled(false);
				}
			});
			return convertView;
		}

		class Holder {
			LinearLayout llLayout;
			ImageView info_icon;
			TextView tv_info_title, tv_info_content;
			Button item_add;
		}
	}
}
