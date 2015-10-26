package com.wise.car;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * RechargeActivity
 * 
 * @author c
 * @date 2015-10-26
 */
public class WebActivity extends Activity {

	WebView webview;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		 webview = new WebView(this);
		setContentView(webview);
		String url = getIntent().getStringExtra("webUrl");
		webview.loadUrl(url);
		// 覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
		webview.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// TODO Auto-generated method stub
				// 返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
				view.loadUrl(url);
				return true;
			}
		});
		
		//启用支持javascript
		WebSettings settings = webview.getSettings();
		settings.setJavaScriptEnabled(true);
	}

	
}
