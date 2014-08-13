package com.wise.state;

import com.umeng.analytics.MobclickAgent;
import com.wise.baba.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * 电源电压
 */
public class DyActivity extends Activity{	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_dy);
		Intent intent = getIntent();
		String title = intent.getStringExtra("title");
		String name = intent.getStringExtra("name");
		String dpdy_range = intent.getStringExtra("range");
		String dpdy = intent.getStringExtra("current");
		String lt_dpdy = intent.getStringExtra("lt");
		String dpdy_content = intent.getStringExtra("url");
		boolean if_dpdy_err = intent.getBooleanExtra("if_err", true);
		boolean if_lt_dpdy_err = intent.getBooleanExtra("if_lt_err", true);
		
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		TextView tv_title = (TextView)findViewById(R.id.tv_title);
		tv_title.setText(title);
		TextView tv_name = (TextView)findViewById(R.id.tv_name);
		tv_name.setText(name);
		TextView tv_dpdy_range = (TextView)findViewById(R.id.tv_dpdy_range);
		tv_dpdy_range.setText(dpdy_range);
		TextView tv_lt_dpdy = (TextView)findViewById(R.id.tv_lt_dpdy);
		tv_lt_dpdy.setText(lt_dpdy);
		TextView tv_if_dpdy_err = (TextView)findViewById(R.id.tv_if_dpdy_err);
		if(if_dpdy_err){
			tv_if_dpdy_err.setText("状态良好");
			tv_if_dpdy_err.setTextColor(getResources().getColor(R.color.blue));
		}else{
			tv_if_dpdy_err.setText("状态异常");
			tv_if_dpdy_err.setTextColor(getResources().getColor(R.color.yellow));
		}
		TextView tv_dpdy = (TextView)findViewById(R.id.tv_dpdy);
		tv_dpdy.setText(dpdy);
		TextView tv_if_lt_dpdy_err = (TextView)findViewById(R.id.tv_if_lt_dpdy_err);
		if(if_lt_dpdy_err){
			tv_if_lt_dpdy_err.setText("状态良好");
			tv_if_lt_dpdy_err.setTextColor(getResources().getColor(R.color.blue));
		}else{
			tv_if_lt_dpdy_err.setText("状态异常");
			tv_if_lt_dpdy_err.setTextColor(getResources().getColor(R.color.yellow));
		}
		WebView shareView = (WebView) findViewById(R.id.share_web);
		shareView.requestFocus();   //设置可获取焦点
		shareView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);  //取消滚动条
		shareView.loadUrl(dpdy_content);
	}
	
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;

			default:
				break;
			}
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}