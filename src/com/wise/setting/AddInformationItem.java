package com.wise.setting;

import java.util.ArrayList;
import java.util.List;

import com.wise.baba.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class AddInformationItem extends Activity {
	ListView infoListView;
	List<InformationItem> list = new ArrayList<InformationItem>();
	int[] picture = { R.drawable.icon_dianyuan_normal,
			R.drawable.icon_jinqi_normal };
	String[] title = { "天气", "新闻" };
	String[] content = { "今天天气概况", "最新新闻更新和内容" };
	InforAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_info);

		infoListView = (ListView) findViewById(R.id.info_add);
		LayoutInflater mLayoutInflater = LayoutInflater.from(this);
		View foot_view = mLayoutInflater.inflate(R.layout.foot_view, null);
		TextView tv_end = (TextView) foot_view.findViewById(R.id.tv_end);
		tv_end.setText("添加卡片");
		infoListView.addFooterView(foot_view);
		adapter = new InforAdapter();
		infoListView.setAdapter(adapter);

		infoListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (arg2 == list.size()) {
					// 添加卡片跳转到添加页面
					startActivity(new Intent(AddInformationItem.this,
							ChooseInfoCard.class));
				}
			}
		});
		findViewById(R.id.tv_info_add).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						// 添加卡片跳转到添加页面
						startActivity(new Intent(AddInformationItem.this,
								ChooseInfoCard.class));
					}
				});

		findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		adapter.notifyDataSetChanged();
		list.clear();

		SharedPreferences sharedPreferences = getSharedPreferences(
				"card_choose", Activity.MODE_PRIVATE);
		String weather = sharedPreferences.getString("weather", "");
		String hotNews = sharedPreferences.getString("hotNews", "");
		if (weather.equals(ChooseInfoCard.cards[0])) {
			InformationItem inItem = new InformationItem();
			inItem.setIcon(picture[0]);
			inItem.setTitle(title[0]);
			inItem.setContent(content[0]);
			inItem.setCardName(weather);
			list.add(inItem);
		}

		if (hotNews.equals(ChooseInfoCard.cards[1])) {
			InformationItem inItem = new InformationItem();
			inItem.setIcon(picture[1]);
			inItem.setTitle(title[1]);
			inItem.setContent(content[1]);
			inItem.setCardName(hotNews);
			list.add(inItem);
		}
	}

	class InforAdapter extends BaseAdapter {

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
			if (convertView == null) {
				mHolder = new Holder();
				convertView = (LayoutInflater.from(AddInformationItem.this))
						.inflate(R.layout.item_info_add, null);
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
			mHolder.item_add.setText("删除");
			mHolder.item_add.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					SharedPreferences sharedPreferences = getSharedPreferences(
							"card_choose", Activity.MODE_PRIVATE);
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.remove(list.get(position).getCardName());
					editor.commit();
					list.remove(position);
					adapter.notifyDataSetChanged();
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

	class InformationItem {
		private int icon;
		private String title;
		private String content;
		private String cardName;

		public String getCardName() {
			return cardName;
		}

		public void setCardName(String cardName) {
			this.cardName = cardName;
		}

		public int getIcon() {
			return icon;
		}

		public void setIcon(int icon) {
			this.icon = icon;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		@Override
		public String toString() {
			return "InformationItem [icon=" + icon + ", title=" + title
					+ ", content=" + content + "]";
		}
	}
}
