package com.wise.baba.ui.fragment;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import com.wise.baba.AppApplication;
import com.wise.baba.BrowserActivity;
import com.wise.baba.R;
import com.wise.baba.app.Const;
import com.wise.baba.app.Msg;
import com.wise.baba.biz.HttpHotNews;
import com.wise.baba.entity.News;
import com.wise.baba.ui.adapter.OnCardMenuListener;

/**
 * 本地资讯
 * 
 * @author honesty
 **/
public class FragmentHotNews extends Fragment implements Callback {
	/** 获取本地资讯 **/
	AppApplication app;
	private TextView tvContent;
	private LinearLayout llytNews;
	private HttpHotNews http;
	private List<News> newsList = new ArrayList<News>();
	private ListView lvNews;
	private TextView tvTitle0;
	private NewsListAdapter newsListAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_hot_news, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		app = (AppApplication) getActivity().getApplication();

		tvContent = (TextView) getActivity().findViewById(
				R.id.tv_title0_content);
		llytNews = (LinearLayout) getActivity().findViewById(R.id.llytNews);
		tvTitle0 = (TextView) getActivity()
				.findViewById(R.id.tv_title0);
		ImageView iv_weather_menu = (ImageView) getActivity().findViewById(
				R.id.iv_hot_news_menu);
		llytNews.setOnClickListener(onClickListener);
		iv_weather_menu.setOnClickListener(onClickListener);
		lvNews = (ListView) getActivity().findViewById(R.id.lvNews);
		newsListAdapter = new NewsListAdapter();
		lvNews.setOnItemClickListener(onItemClickListener);
		lvNews.setAdapter(newsListAdapter);
		http = new HttpHotNews(getActivity(), new Handler(this));
		http.request();

	}

	OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int index,
				long arg3) {
			showNews(index + 1);
		}
	};

	public void showNews(int index) {
		Intent intent_hot = new Intent(getActivity(), BrowserActivity.class);
		News news = newsList.get(index);
		intent_hot.putExtra("url", news.getUrl());
		intent_hot.putExtra("title", news.getTitle());
		intent_hot.putExtra("hot_content", news.getContent());
		startActivity(intent_hot);
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_hot_news_menu:
				if (onCardMenuListener != null) {
					onCardMenuListener.showCarMenu(Const.TAG_NEWS);
				}
				break;
			case R.id.llytNews:
				showNews(0);
				break;
			}

		}
	};

	OnCardMenuListener onCardMenuListener;

	public void setOnCardMenuListener(OnCardMenuListener onCardMenuListener) {
		this.onCardMenuListener = onCardMenuListener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Handler.Callback#handleMessage(android.os.Message)
	 */
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case Msg.Get_News_List:
			this.newsList = (List<News>) msg.obj;
			Log.i("FragmentHotNews", newsList.size()+"");
			setNewsTitle();
			break;
		}
		return false;
	}

	/**
	 * 设置首页新闻
	 */
	public void setNewsTitle() {
		if (newsList == null) {
			newsList = new ArrayList<News>();
		}

		/*
		 * 先设置第一条新闻内容
		 */
		if (newsList.size() > 0) {
			News news = (News) newsList.get(0);
			tvTitle0.setText(news.getTitle());
			String content = news.getContent().replaceAll("】", "】 ");
			tvContent.setText(content);
		}
		
		
		
		//有时候，新闻内容改变后，listview会自动获取焦点，显示在屏幕中间，所以要让scrollView滚回原来高度
		ScrollView scrollView = (ScrollView) getActivity().findViewById(R.id.scrollView);
		int y = (int) scrollView.getY();
		Log.i("FragmentHotNews", y+"");
		newsListAdapter.notifyDataSetChanged();
		y = (int) scrollView.getY();
		Log.i("FragmentHotNews", y+"");
		scrollView.smoothScrollTo(0, y);

	}

	// 在标点符号后加一个空格。
	public static String stringFilter(String str) {
		str = str.replaceAll("】", "】 ").replaceAll("，", "， ")
				.replaceAll("。", "。 ");// 替换中文标号
		return str.trim();
	}

	class NewsListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			int size = newsList.size();
			if(size>1){
				return size-1;
			}else{
				return 0;
			}
			
		}

		@Override
		public Object getItem(int index) {
			return newsList.get(index + 1);
		}

		@Override
		public long getItemId(int index) {
			return index;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(getActivity());
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.item_news_item, null);
			}
			TextView tvTitle = (TextView) convertView
					.findViewById(R.id.tv_title);
			tvTitle.setText(newsList.get(position + 1).getTitle());
			return convertView;

		}

	}

}
