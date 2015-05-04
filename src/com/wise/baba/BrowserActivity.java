package com.wise.baba;

import com.wise.baba.biz.GetSystem;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author honesty
 **/
public class BrowserActivity extends Activity {
	String url = "";
	String hot_title;
	String hot_content;
	WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_browser);
		url = getIntent().getStringExtra("url");
		
		hot_title = getIntent().getStringExtra("title");
		hot_content = getIntent().getStringExtra("hot_content");
		TextView tv_title = (TextView) findViewById(R.id.tv_title);
		tv_title.setText(hot_title);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		ImageView iv_share = (ImageView) findViewById(R.id.iv_share);
		iv_share.setOnClickListener(onClickListener);
		webView = (WebView) findViewById(R.id.webView);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});
		webView.loadUrl(url);
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.iv_share:
				GetSystem.share(BrowserActivity.this, hot_content, "", 0, 0,
						hot_title, url);
				break;
			}
		}
	};
}