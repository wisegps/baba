package com.wise.setting;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import xlist.DragListView;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wise.baba.R;

import data.CardsData;

public class AddCards extends Activity {
	DragListView infoListView;

	List<CardsData> list = new ArrayList<CardsData>();
	InforAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_info);
		SharedPreferences sharedPreferences = getSharedPreferences(
				"card_choose", Activity.MODE_PRIVATE);
		String cardsJson = sharedPreferences.getString("cardsJson", "");
		if (!cardsJson.equals("") && cardsJson != null) {
			try {
				JSONArray jsonArray = new JSONArray(cardsJson);
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject object = jsonArray.getJSONObject(i);
					CardsData cardsData = new CardsData();
					cardsData.setIcon(object.getInt("icon"));
					cardsData.setTitle(object.getString("title"));
					cardsData.setContent(object.getString("content"));
					cardsData.setCardName(object.getString("cardName"));
					list.add(cardsData);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		infoListView = (DragListView) findViewById(R.id.info_add);
		adapter = new InforAdapter();
		infoListView.setAdapter(adapter);
		infoListView.setDragListener(mDrapListener);
		// infoListView.setOnItemClickListener(new OnItemClickListener() {
		//
		// @Override
		// public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
		// long arg3) {
		// infoListView.setDoTouch(true);
		// Toast.makeText(AddCards.this, "1111111", Toast.LENGTH_SHORT)
		// .show();
		// }
		// });

		findViewById(R.id.tv_info_add).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						// 添加卡片跳转到添加页面
						Intent intent = new Intent(AddCards.this,
								ChooseCard.class);
						startActivityForResult(intent, 6);
					}
				});

		findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				list.clear();
				finish();
			}
		});
	}

	// 交换listview的数据
	private DragListView.DragListener mDrapListener = new DragListView.DragListener() {
		@Override
		public void drag(int from, int to, int i) {
			adapter.index = i;
			CardsData item = new CardsData();
			item = list.get(from);
			list.set(from, list.get(to));
			list.set(to, item);
			// list.remove(from);
			// list.add(to, item);
			adapter.notifyDataSetChanged();
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == ChooseCard.CARDCODE) {
			String cardsJson = data.getStringExtra("cardsJson");
			try {
				JSONArray jsonArray = new JSONArray(cardsJson);
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject object = jsonArray.getJSONObject(i);
					CardsData cardsData = new CardsData();
					cardsData.setIcon(object.getInt("icon"));
					cardsData.setTitle(object.getString("title"));
					cardsData.setContent(object.getString("content"));
					cardsData.setCardName(object.getString("cardName"));
					list.add(cardsData);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			adapter.notifyDataSetChanged();

			SharedPreferences sharedPreferences = getSharedPreferences(
					"card_choose", Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			JSONArray jsonArray = new JSONArray();
			try {
				for (int i = 0; i < list.size(); i++) {
					JSONObject object = new JSONObject();
					object.put("icon", list.get(i).getIcon());
					object.put("title", list.get(i).getTitle());
					object.put("content", list.get(i).getContent());
					object.put("cardName", list.get(i).getCardName());
					jsonArray.put(object);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			editor.putString("cardsJson", jsonArray.toString());
			editor.commit();
		}
	}

	class InforAdapter extends BaseAdapter {

		private int index = -1;

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

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
			Holder mHolder;
			// if (convertView == null) {
			mHolder = new Holder();
			convertView = (LayoutInflater.from(AddCards.this)).inflate(
					R.layout.item_info_add, null);
			mHolder.info_icon = (ImageView) convertView
					.findViewById(R.id.info_icon);
			mHolder.tv_info_title = (TextView) convertView
					.findViewById(R.id.tv_info_title);
			mHolder.tv_info_content = (TextView) convertView
					.findViewById(R.id.tv_info_content);
			mHolder.item_add = (Button) convertView.findViewById(R.id.item_add);
			convertView.setTag(mHolder);
			// } else {
			// mHolder = (Holder) convertView.getTag();
			// }
			mHolder.info_icon.setImageResource(list.get(position).getIcon());
			mHolder.tv_info_title.setText(list.get(position).getTitle());
			mHolder.tv_info_content.setText(list.get(position).getContent());
			mHolder.item_add.setText("删除");
			mHolder.item_add.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					infoListView.stopDrag();
					SharedPreferences sharedPreferences = getSharedPreferences(
							"card_choose", Activity.MODE_PRIVATE);
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.remove(list.get(position).getCardName());
					list.remove(position);
					adapter.notifyDataSetChanged();
					JSONArray cardsJson = new JSONArray();
					try {
						for (int i = 0; i < list.size(); i++) {
							JSONObject object = new JSONObject();
							object.put("icon", list.get(i).getIcon());
							object.put("title", list.get(i).getTitle());
							object.put("content", list.get(i).getContent());
							object.put("cardName", list.get(i).getCardName());
							cardsJson.put(object);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					editor.putString("cardsJson", cardsJson.toString());
					editor.commit();
				}
			});
			if (index == position) {
				convertView.setVisibility(View.INVISIBLE);
			} else {
				convertView.setVisibility(View.VISIBLE);
			}
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
