package com.wise.notice;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import widget.adapters.FriendListAdapter;
import xlist.XListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.entity.Info;
import com.wise.baba.entity.Info.FriendStatus;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.adapter.FriendCareListAdapter;
import com.wise.baba.ui.widget.CircleImageView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

/**
 * 弹出所有好友列表供选择
 * 
 * @author cyy
 **/
public class FriendListPopActivity extends Activity implements
		OnItemClickListener {

	private XListView xListView;
	private List friends;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_friend_list);
		TextView tvTitle = (TextView) findViewById(R.id.tv_title);
		tvTitle.setText("选择好友");
		xListView = (XListView) findViewById(R.id.lv_friend);
		xListView.setPullLoadEnable(false);
		xListView.setPullRefreshEnable(false);
		xListView.setOnItemClickListener(this);
		Intent intent = getIntent();
		friends = (List) intent.getSerializableExtra("friends");
		FriendCareListAdapter adapter = new FriendCareListAdapter(this);
		adapter.setFriendList(friends);
		xListView.setAdapter(adapter);
		View imgBack = findViewById(R.id.iv_fm_back);
		imgBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				FriendListPopActivity.this.finish();
				overridePendingTransition(0, R.anim.activity_out_from_top);
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int index,
			long arg3) {
		Intent intent = new Intent();
		intent.putExtra("index", index);
		setResult(1, intent);
		FriendListPopActivity.this.finish();
		
	}
	
	 @Override  
	    public boolean onKeyDown(int keyCode, KeyEvent event)  
	    {  
	        if (keyCode == KeyEvent.KEYCODE_BACK )  
	        {  
	        	FriendListPopActivity.this.finish();
	        	overridePendingTransition(0, R.anim.activity_out_from_top);
	        }  
	          
	        return false;  
	          
	    }  

}
