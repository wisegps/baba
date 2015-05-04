package com.wise.baba;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.wise.baba.app.Constant;
import com.wise.baba.net.NetThread;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class FeedBackActivity extends Activity {
    private static final int feedBack = 1;
	EditText et_content,et_qq;
	AppApplication app;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        app = (AppApplication)getApplication();
		setContentView(R.layout.activity_feed_back);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		Button bt_send = (Button)findViewById(R.id.bt_send);
		bt_send.setOnClickListener(onClickListener);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		et_content = (EditText)findViewById(R.id.et_content);
		et_qq = (EditText)findViewById(R.id.et_qq);		
	}
	OnClickListener onClickListener = new OnClickListener() {        
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.bt_send:
                String content = et_content.getText().toString().trim();
                if(content.equals("")){
                    Toast.makeText(FeedBackActivity.this, "反馈的内容不能为空", Toast.LENGTH_SHORT).show();
                    break;
                }
                String qq = et_qq.getText().toString().trim();
                if(qq.equals("")){
                    Toast.makeText(FeedBackActivity.this, "联系方式不能为空", Toast.LENGTH_SHORT).show();
                    break;
                }
                String url = Constant.BaseUrl + "feedback?auth_code=" + app.auth_code;
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("content", content));
                params.add(new BasicNameValuePair("contact", qq));
                params.add(new BasicNameValuePair("cust_id", app.cust_id == null ? "0":app.cust_id));
                new Thread(new NetThread.postDataThread(handler, url, params, feedBack)).start();
                break;
            case R.id.iv_back:
                finish();
                break;
            }
        }
    };
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case feedBack:
                Toast.makeText(FeedBackActivity.this, "意见反馈成功", Toast.LENGTH_SHORT).show();
                finish();
                break;
            }
        }        
    };
}
