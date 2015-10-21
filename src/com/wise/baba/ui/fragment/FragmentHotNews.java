package com.wise.baba.ui.fragment;

import java.io.UnsupportedEncodingException;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
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
	private int[] titleId = { R.id.tv_title0, R.id.tv_title1, R.id.tv_title2,
			R.id.tv_title3 };
	private TextView tvContent;
	private LinearLayout llytNews;
	private HttpHotNews http;
	private List<News> newsList;

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

		ImageView iv_weather_menu = (ImageView) getActivity().findViewById(
				R.id.iv_hot_news_menu);
		llytNews.setOnClickListener(onClickListener);
		iv_weather_menu.setOnClickListener(onClickListener);
		http = new HttpHotNews(getActivity(), new Handler(this));
		http.request();

	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			int index = -1;
			switch (v.getId()) {
			case R.id.iv_hot_news_menu:
				if (onCardMenuListener != null) {
					onCardMenuListener.showCarMenu(Const.TAG_NEWS);
				}
				break;
			case R.id.llytNews:
				index = 0;
				break;
			case R.id.tv_title1:
				index = 1;
				break;
			case R.id.tv_title2:
				index = 2;
				break;
			case R.id.tv_title3:
				index = 3;
				break;
			}
			if (index != -1) {
				Intent intent_hot = new Intent(getActivity(),
						BrowserActivity.class);
				News news = newsList.get(index);
				intent_hot.putExtra("url", news.getUrl());
				intent_hot.putExtra("title", news.getTitle());
				intent_hot.putExtra("hot_content", news.getContent());
				startActivity(intent_hot);
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
			return;
		}

		int titleLength = titleId.length;
		int newsSize = newsList.size();
		int count = titleLength < newsSize ? titleLength : newsSize;
		Log.i("FragmentHotNews", "count " + count);
		for (int i = 0; i < count; i++) {
			News news = (News) newsList.get(i);
			if (i == 0) {
				String content = news.getContent().replaceAll("】", "】 ");
				Log.i("FragmentHotNews", "getContent " + content);
				tvContent.setText(content);
			}
			TextView tvTitle = (TextView) getActivity()
					.findViewById(titleId[i]);
			tvTitle.setOnClickListener(onClickListener);
			tvTitle.setText(news.getTitle());
		}
	}

	// 在标点符号后加一个空格。
	public static String stringFilter(String str) {
		str = str.replaceAll("】", "】 ").replaceAll("，", "， ")
				.replaceAll("。", "。 ");// 替换中文标号
		return str.trim();
	}

	

}
