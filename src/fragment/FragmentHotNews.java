package fragment;

import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import pubclas.Constant;
import pubclas.NetThread;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wise.baba.AppApplication;
import com.wise.baba.BrowserActivity;
import com.wise.baba.R;

import customView.BidirSlidingLayout;

/**
 * 本地资讯
 * @author honesty
 **/
public class FragmentHotNews extends Fragment {
	/** 获取本地资讯 **/
	private static final int startGetNewThread = 1;
	private static final int gethot_news = 2;
	private BidirSlidingLayout bidirSldingLayout;
	TextView tv_hot_content, tv_host_title;
	AppApplication app;
	boolean isDestory = false;
	boolean isPause = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_hot_news, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		app = (AppApplication) getActivity().getApplication();
		bidirSldingLayout = (BidirSlidingLayout) getActivity().findViewById(R.id.bidir_sliding_layout);
		tv_hot_content = (TextView) getActivity().findViewById(R.id.tv_hot_content);
		bidirSldingLayout.setScrollEvent(tv_hot_content);
		tv_hot_content.setOnClickListener(onClickListener);
		tv_host_title = (TextView) getActivity().findViewById(R.id.tv_host_title);
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(!isDestory){
					try {	
						if(!isPause){
							System.out.println("startGetNewThread");
							Message message = new Message();
							message.what = startGetNewThread;
							handler.sendMessage(message);
						}
						Thread.sleep(30000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}				
			}
		}).start();
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.tv_hot_content:
				Intent intent_hot = new Intent(getActivity(), BrowserActivity.class);
				intent_hot.putExtra("url", hot_url);
				intent_hot.putExtra("title", hot_title);
				intent_hot.putExtra("hot_content", hot_content);
				startActivity(intent_hot);
				break;
			}
		}
	};

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case startGetNewThread:
				gethot_news();
				break;
			case gethot_news:
				jsonhot_news(msg.obj.toString());
				break;
			}
		}

	};

	/** 获取热点信息 **/
	private void gethot_news() {
		try {
			String url = Constant.BaseUrl + "base/hot_news?city=" + URLEncoder.encode(app.City, "UTF-8");
			new NetThread.GetDataThread(handler, url, gethot_news).start();
			System.out.println("startGetNewThread = " + url);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	String hot_url;
	String hot_title;
	String hot_content;

	/** 解析乐一下 **/
	private void jsonhot_news(String str) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			hot_content = jsonObject.getString("content");
			tv_hot_content.setText(hot_content);
			hot_title = jsonObject.getString("title");
			tv_host_title.setText(hot_title);
			hot_url = jsonObject.getString("url");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void onResume() {
		super.onResume();
		isPause = false;
	}
	@Override
	public void onPause() {
		super.onPause();
		isPause = true;
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		isDestory = true;
		System.out.println("FragmentHotNews onDestroy");
	}
}