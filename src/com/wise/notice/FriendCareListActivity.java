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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 特别关心账户列表
 * 
 * @author cyy
 **/
public class FriendCareListActivity extends Activity implements
		OnItemClickListener, OnClickListener {

	private XListView xListView;
	private List friends;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_friend_list);
		xListView = (XListView) findViewById(R.id.lv_friend);

		View footer = LayoutInflater.from(this).inflate(R.layout.footer_add,
				null);

		xListView.addFooterView(footer);
		footer.findViewById(R.id.btn_add).setOnClickListener(this);
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

				FriendCareListActivity.this.finish();
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int index,
			long arg3) {
		Log.i("FriendCareListActivity", "iiii" + index);
		Intent intent = new Intent(FriendCareListActivity.this,
				FriendCarListActivity.class);
		FriendCareListActivity.this.startActivity(intent);
		FriendCareListActivity.this.finish();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_add:
			Intent intent = new Intent(FriendCareListActivity.this,FriendListPopActivity.class);
			intent.putExtra("friends", (Serializable)friends);
			startActivityForResult(intent, 1);
			overridePendingTransition(R.anim.activity_in_from_bottom, R.anim.activity_fixed);
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

}
