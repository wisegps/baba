package com.wise.notice;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import org.json.JSONObject;


import widget.adapters.FriendListAdapter;
import xlist.XListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.entity.FriendData;
import com.wise.baba.entity.Info;
import com.wise.baba.entity.Info.FriendStatus;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.widget.CircleImageView;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 搜索好友列表页面
 * 
 * @author cyy
 **/
public class FriendListActivity extends Activity implements OnItemClickListener {

	private XListView xListView;
	private List friends;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_friend_list);
		xListView = (XListView) findViewById(R.id.lv_friend);
		xListView.setPullLoadEnable(false);
		xListView.setPullRefreshEnable(false);
		xListView.setOnItemClickListener(this);
		Intent intent = getIntent();
		friends = (List) intent.getSerializableExtra("friends");
		xListView.setAdapter(new FriendListAdapter(this,friends));
		View imgBack = findViewById(R.id.iv_fm_back);
		imgBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				FriendListActivity.this.finish();
			}
		});
	}


	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int index, long arg3) {
		Intent intent = new Intent(this,
				FriendDetailActivity.class);
		intent.putExtra(Info.FriendStatusKey,
				Info.FriendStatus.FriendAddFromName);
		intent.putExtra("friend", (Serializable)friends.get(index-1));
		startActivityForResult(intent, 3);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 3 && resultCode == 2) {
			// TODO 添加好友返回
			setResult(2);
			finish();
		}
	}

}
